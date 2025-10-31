// File          : JDRCanvas.java
// Description   : Panel on which to draw JDR images
// Creation Date : 5th June 2008
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2025 Nicola L.C. Talbot

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package com.dickimawbooks.flowframtk;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.image.*;
import java.awt.print.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.undo.*;

import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.filter.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.dialog.*;

public class JDRCanvas extends JPanel
   implements MouseMotionListener,MouseListener,
              Scrollable,SymbolSelectorListener,Printable,
              ChangeListener,JDRImage,JDRConstants,
              DropTargetListener
{
   public JDRCanvas(JDRFrame frame, CanvasGraphics cg)
   {
      super();
      frame_ = frame;

      setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, this, true));

      initCanvas(cg);
   }

   private void initCanvas(CanvasGraphics cg)
   {
      generalActionList = new Vector<CanvasAction>();
      textConstructionActionList = new Vector<CanvasAction>();
      selectPathActionList = new Vector<CanvasSelectAction>();
      selectTextActionList = new Vector<CanvasSelectAction>();
      selectGroupActionList = new Vector<CanvasSelectAction>();
      selectBitmapActionList = new Vector<CanvasSelectAction>();
      selectGeneralActionList = new Vector<CanvasSelectAction>();
      editPathActionList = new Vector<CanvasAction>();
      appActionList = new Vector<FlowframTkAction>();

      cg.setComponent(this);
      paths = new JDRGroup(cg);

      setBackground(Color.white);
      mouse = new Point2D.Double(0,0);

      anchor = null;
      currentPath = null;
      currentSegment = null;
      selectedIndex = -1;
      scanshape = null;
      mouseDown = false;
      displayPage = PAGES_ALL;

      setLayout(null);

      textField = new CanvasTextField(this, getApplication());
      add(textField);
      setTextFieldFont(frame_.getCurrentFont());
      textField.setVisible(false);
      updateTextFieldBounds();

      try
      {
         init_keymaps();

         init_app_actionlist();

         init_popups();
      }
      catch (Throwable e)
      {
         getResources().internalError(this, e);
      }

      addMouseListener(this);
      addMouseMotionListener(this);
   }

   private void init_keymaps()
   {
      FlowframTk application = getApplication();

      // Keystroke actions without items in the canvas popup menus

      addCanvasAction(new CanvasAction(this, "action.construct_click",
         getResources().getAccelerator("action.construct_click"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               doConstructMouseClick();
            }
         },
         TOOL_FLAG_ANY_PATHS | TOOL_FLAG_ANY_TEXT,
         EDIT_FLAG_NONE,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_NONE,
         FlowframTkAction.SELECTION_IGNORE_COUNT
      ));

      addCanvasAction(new CanvasAction(this, "cursor_left",
         getResources().getAccelerator("action.cursor_left"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               moveLeft(evt.getModifiers());
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

      addCanvasAction(new CanvasAction(this, "cursor_right",
         getResources().getAccelerator("action.cursor_right"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               moveRight(evt.getModifiers());
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

      addCanvasAction(new CanvasAction(this, "cursor_up",
         getResources().getAccelerator("action.cursor_up"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               moveUp(evt.getModifiers());
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

      addCanvasAction(new CanvasAction(this, "cursor_down",
         getResources().getAccelerator("action.cursor_down"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               moveDown(evt.getModifiers());
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

      addCanvasAction(new CanvasAction(this, "scroll_home_up",
         getResources().getAccelerator("action.scroll_home_up"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               scrollHomeUp();
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

      addCanvasAction(new CanvasAction(this, "scroll_home_left",
         getResources().getAccelerator("action.scroll_home_left"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               scrollHomeLeft();
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

      addCanvasAction(new CanvasAction(this, "scroll_end_down",
         getResources().getAccelerator("action.scroll_end_down"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               scrollEndDown();
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

      addCanvasAction(new CanvasAction(this, "scroll_end_right",
         getResources().getAccelerator("action.scroll_end_right"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               scrollEndRight();
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

      addCanvasAction(new CanvasAction(this, "scroll_block_down",
         getResources().getAccelerator("action.scroll_block_down"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               blockScrollDown();
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

      addCanvasAction(new CanvasAction(this, "scroll_block_right",
         getResources().getAccelerator("action.scroll_block_right"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               blockScrollRight();
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

      addCanvasAction(new CanvasAction(this, "scroll_block_up",
         getResources().getAccelerator("action.scroll_block_up"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               blockScrollUp();
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

      addCanvasAction(new CanvasAction(this, "scroll_block_left",
         getResources().getAccelerator("action.scroll_block_left"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               blockScrollLeft();
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

      addCanvasAction(new CanvasAction(this, "delete_last",
         getResources().getAccelerator("action.delete_last"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               deleteLast();
            }
         },
         TOOL_FLAG_ANY,
         EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT));

       CanvasAction popupAction = new CanvasAction(this,
         "show-popup",
         getResources().getAccelerator("action.popup"),
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               showPopup();
            }
         },
         FlowframTkAction.TOOL_FLAG_ANY, 
         FlowframTkAction.EDIT_FLAG_ANY,
         FlowframTkAction.CONSTRUCTION_FLAG_ANY,
         FlowframTkAction.SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT);
      addCanvasAction(popupAction);

      popupAction.setAccelerator(
         getResources().getAccelerator("action.context_menu"));

   }

   private void init_app_actionlist()
   {
      // List of application actions that need to be notified
      // but don't have an item in a popup menu.

      addAppAction("save");
      addAppAction("save_as");
      addAppAction("export");
      addAppSelectAction("moveby");
      addAppSelectAction("textarea.matrix");
      addAppSelectAction("path.style");
      addAppSelectAction("adjustcol");
      addAppSelectAction("adjustcol.togrey");
      addAppSelectAction("adjustcol.cmyk");
      addAppSelectAction("adjustcol.rgb");
      addAppSelectAction("adjustcol.hsb");
      addAppSelectAction("adjustcol.fade");
      addAppSelectAction("adjustcol.removetrans");
      addAppSelectAction("front");
      addAppSelectAction("back");
      addAppSelectAction("moveup");
      addAppSelectAction("movedown");
      addAppSelectAction("rotate");
      addAppSelectAction("scale");
      addAppSelectAction("shear");
      addAppSelectAction("reverse");
      addAppSelectAction("merge");
      addAppSelectAction("union");
      addAppSelectAction("xor");
      addAppSelectAction("intersect");
      addAppSelectAction("subtract");
      addAppSelectAction("pattern");
      addAppSelectAction("pattern.set");
      addAppSelectAction("pattern.edit");
      addAppSelectAction("pattern.remove");
      addAppSelectAction("convert");
      addAppSelectAction("convert_to_full");
      addAppSelectAction("split");
      addAppSelectAction("textpath");
      addAppSelectAction("separate");
      addAppAction("navigate.select");
      addAppAction("navigate.add_next");
      addAppAction("navigate.skip");
      addAppAction("navigate.find");
      addAppAction("navigate.by_description");
      addAppAction("navigate.add_description");
      addAppSelectAction("parshape");
      addAppSelectAction("shapepar");
      addAppSelectAction("flowframe.set_frame");
   }

   private void init_popups()
   {
      init_textedit_popup();
      init_editpath_popup();
      init_select_popups();
   }

   private void init_textedit_popup()
   {
      // construct/edit text area popup menu

      texteditPathPopupMenu = new JPopupMenu();

      copyText = getResources().createAppMenuItem("menu", "textarea.copy",
      new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               textField.copy();
            }
         });

      texteditPathPopupMenu.add(copyText);

      cutText = getResources().createAppMenuItem("menu", "textarea.cut",
      new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               textField.cut();
            }
         });

      texteditPathPopupMenu.add(cutText);

      texteditPathPopupMenu.add(
         getResources().createAppMenuItem("menu", "textarea.paste",
      new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               textField.paste();
            }
         }));

      texteditPathPopupMenu.add(
      getResources().createAppMenuItem("menu", "textarea.select_all",
      new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               textField.selectAll();
            }
         }));

      texteditPathPopupMenu.add(CanvasAction.createMenuItem(this,
        "menu.textarea", "insert_symbol",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               showSymbolSelector();
            }
         },
         TOOL_FLAG_ANY_TEXT,
         EDIT_FLAG_NONE,
         FlowframTkAction.CONSTRUCTION_FLAG_TEXT,
         SELECT_FLAG_NONE, FlowframTkAction.SELECTION_IGNORE_COUNT
      ));

      textField.addMouseListener(new MouseAdapter()
        {
           public void mousePressed(MouseEvent evt)
           {
              checkForPopupTrigger(evt);
           }

           public void mouseReleased(MouseEvent evt)
           {
              checkForPopupTrigger(evt);
           }
        });
   }

   private void init_editpath_popup()
   {
      // edit path menu

      editPathPopupMenu = new JPopupMenu();

      // exit edit path

      CanvasSelectAction editPathAction
         = addCanvasSelectAction("path.edit");

      editPathPopupMenu.add(editPathAction.createCheckBoxMenuItem(
         "menu.selectedpath.edit", true, "menu.edit.path.edit.tooltip"));

      editPathPopupMenu.add(new JPopupMenu.Separator());

      // Next control

      editPathPopupMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "next_control",
         new FlowframTkActionListener ()
         {
             public void doAction(FlowframTkAction action, ActionEvent evt)
             {
                selectNextControl();
             }
         }
      ));

      // Previous Control

      editPathPopupMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "prev_control",
         new FlowframTkActionListener ()
         {
             public void doAction(FlowframTkAction action, ActionEvent evt)
             {
                selectPrevControl();
             }
         }
      ));

      // Delete control

      editPathPopupMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "delete_control",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               deleteCurrentControlPoint();
            }
         },
         SELECT_FLAG_SHAPE,
         SEGMENT_FLAG_ANY_FULL,
         CONTROL_FLAG_START | CONTROL_FLAG_END,
         new ValidControlListener()
         {
             public boolean isValid(JDRSelection currentSelection,
                int currentSegmentFlag, int currentControlFlag)
             {
                if (((currentSelection.getSelectionFlag()
                     & SELECT_FLAG_SYMMETRIC_ANCHORED_CLOSE) != 0)
                 && ((currentSegmentFlag & SEGMENT_FLAG_FIRST) != 0)
                 && ((currentControlFlag & CONTROL_FLAG_START) != 0)
                 )
                {
                   return false;
                }

                return true;
             }
         }));

      // Insert Control

      editPathPopupMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "add_control",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               addControlPoint();
            }
         },
         SELECT_FLAG_SHAPE,
         SEGMENT_FLAG_ANY_FULL,
         CONTROL_FLAG_REGULAR
         ));

      editPathPopupMenu.addSeparator();

      // Convert to line

      editPathPopupMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "convert_to_line",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               convertToLine();
            }
         },
         SELECT_FLAG_SHAPE,
         SEGMENT_FLAG_MOVE
       | SEGMENT_FLAG_CURVE
       | SEGMENT_FLAG_PARTIAL_MOVE
       | SEGMENT_FLAG_PARTIAL_CURVE
         ));

      // Convert to curve

      editPathPopupMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "convert_to_curve",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               convertToCurve();
            }
         },
         SELECT_FLAG_SHAPE,
         SEGMENT_FLAG_MOVE
       | SEGMENT_FLAG_LINE
       | SEGMENT_FLAG_PARTIAL_MOVE
       | SEGMENT_FLAG_PARTIAL_LINE
         ));

      // Convert to move

      editPathPopupMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "convert_to_move",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               convertToMove();
            }
         },
         SELECT_FLAG_SHAPE,
         SEGMENT_FLAG_CURVE
       | SEGMENT_FLAG_LINE
       | SEGMENT_FLAG_PARTIAL_CURVE
       | SEGMENT_FLAG_PARTIAL_LINE
         ));

      editPathPopupMenu.addSeparator();

      // Symmetry submenu

      JMenu symmetryMenu = getResources().createAppMenu("menu.editpath.symmetry");
      editPathPopupMenu.add(symmetryMenu);

      // Toggle symmetry

      symmetryMenu.add(EditPathAction.createCheckBoxMenuItem(this,
          "menu.editpath.symmetry", "has_symmetry", false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               setSymmetry();
            }
         },
         SELECT_FLAG_SHAPE,
         SEGMENT_FLAG_ANY
      ));

      // Toggle join anchor

      symmetryMenu.add(EditPathAction.createCheckBoxMenuItem(this,
          "menu.editpath.symmetry", "join_anchor", false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               anchorSymmetry();
            }
         },
         SELECT_FLAG_SYMMETRIC | SELECT_FLAG_SYMTEXTPATH,
         SEGMENT_FLAG_ANY
      ));

      // Toggle close anchor 

      symmetryMenu.add(EditPathAction.createCheckBoxMenuItem(this,
          "menu.editpath.symmetry", "close_anchor", false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               closeAnchorSymmetry();
            }
         },
         SELECT_FLAG_SYMMETRIC | SELECT_FLAG_SYMTEXTPATH,
         FlowframTkAction.SELECTION_SINGLE_CLOSED_SHAPE,
         SEGMENT_FLAG_ANY
      ));

      // Continuous 

      JMenu continuityMenu = EditPathAction.createMenu(this,
         "menu.editpath", "continuity",
         SELECT_FLAG_SHAPE,
         SEGMENT_FLAG_CURVE | SEGMENT_FLAG_PARTIAL_CURVE,
         CONTROL_FLAG_CAN_MAKE_JOIN_CONTINUOUS | CONTROL_FLAG_CAN_ANCHOR);
      editPathPopupMenu.add(continuityMenu);

      continuityMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "continuity.equi",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               makeContinuous(true);
            }
         },
         SELECT_FLAG_SHAPE,
         SEGMENT_FLAG_CURVE | SEGMENT_FLAG_PARTIAL_CURVE,
         CONTROL_FLAG_CAN_MAKE_JOIN_CONTINUOUS));

      continuityMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "continuity.relative",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               makeContinuous(false);
            }
         },
         SELECT_FLAG_SHAPE,
         SEGMENT_FLAG_CURVE | SEGMENT_FLAG_PARTIAL_CURVE,
         CONTROL_FLAG_CAN_MAKE_JOIN_CONTINUOUS));

      continuityMenu.add(EditPathAction.createCheckBoxMenuItem(this,
         "menu.editpath", "continuity.anchor", false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               toggleContinuityAnchor();
            }
         },
         SELECT_FLAG_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT,
         SEGMENT_FLAG_CURVE | SEGMENT_FLAG_PARTIAL_CURVE,
         CONTROL_FLAG_CAN_ANCHOR));

      // Open path submenu

      JMenu openPathMenu = EditPathAction.createMenu(this,
        "menu.editpath", "open_path",
        SELECT_FLAG_CLOSED,
        SEGMENT_FLAG_ANY);
      editPathPopupMenu.add(openPathMenu);

      // Open (remove last segment)

      openPathMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "open_path.remove_last",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               openPath(true);
            }
         },
         SELECT_FLAG_CLOSED,
         SEGMENT_FLAG_ANY
         ));

      // Open (remove keep segment)

      openPathMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "open_path.keep_last",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               openPath(false);
            }
         },
         SELECT_FLAG_CLOSED,
         FlowframTkAction.SELECTION_NO_SYMMETRIC_SHAPES,
         SEGMENT_FLAG_ANY
         ));

      // Close path submenu

      JMenu closePathMenu = EditPathAction.createMenu(this,
         "menu.editpath", "close_path",
         SELECT_FLAG_OPEN,
         SEGMENT_FLAG_ANY);
      editPathPopupMenu.add(closePathMenu);

      // Close with line

      closePathMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "close_path.line",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               closePath(JDRShape.CLOSE_LINE);
            }
         },
         SELECT_FLAG_OPEN,
         SEGMENT_FLAG_ANY));

      // Close path with continuous curve

      closePathMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "close_path.cont",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               closePath(JDRShape.CLOSE_CONT);
            }
         },
         SELECT_FLAG_OPEN,
         SEGMENT_FLAG_ANY));

      // Close path, merging end points

      closePathMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "close_path.merge",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               closePath(JDRShape.CLOSE_MERGE_ENDS);
            }
         },
         SELECT_FLAG_OPEN,
         SEGMENT_FLAG_ANY,
         new ValidSegmentListener()
         {
            public boolean isValid(JDRSelection selection, 
               int currentSegmentFlag)
            {
               return (editedPath.size() > 1);
            }
         }));

      // Close subpath

      editPathPopupMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "close_sub_path",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               convertToClosingMove();
            }
         },
         SELECT_FLAG_SHAPE,
         SEGMENT_FLAG_MOVE | SEGMENT_FLAG_LAST ,
         CONTROL_FLAG_REGULAR
         ));

      editPathPopupMenu.addSeparator();

      // Move point

      movePtDialog = new MovePointDialog(frame_);

      editPathPopupMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "coordinates",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               movePtDialog.display();
            }
         },
         SELECT_FLAG_SHAPE,
         SEGMENT_FLAG_ANY,
         CONTROL_FLAG_ANY & ~CONTROL_FLAG_NONE
         ));

      // Segment info

      editPathPopupMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "info",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               segmentInfo();
            }
         }
      ));

      // Snap to grid

      editPathPopupMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "snap",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               snapToGrid();
            }
         }
         ));

      // Break path

      editPathPopupMenu.add(EditPathAction.createMenuItem(this,
         "menu.editpath", "break_path",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               breakPath();
            }
         },
         SELECT_FLAG_SHAPE,
         SEGMENT_FLAG_FIRST | SEGMENT_FLAG_MID,
         CONTROL_FLAG_REGULAR,
         new ValidSegmentListener()
         {
            public boolean isValid(JDRSelection selection,
               int currentSegmentFlag)
            {
               return (editedPath.size() > 1);
            }
         }
         ));

   }

   private void init_select_popups()
   {
      // Get select actions from main application

      // General

      CanvasSelectAction objectDescriptionAction 
         = addCanvasSelectAction("object_description");
      CanvasSelectAction objectTagAction 
         = addCanvasSelectAction("object_tag");
      CanvasSelectAction cutAction
         = addCanvasSelectAction("cut");
      CanvasSelectAction copyAction
         = addCanvasSelectAction("copy");
      CanvasSelectAction pasteAction
         = addCanvasSelectAction("paste");
      CanvasSelectAction selectAllAction
         = addCanvasSelectAction("select_all");
      CanvasSelectAction deselectAllAction
         = addCanvasSelectAction("deselect_all");
      CanvasSelectAction groupAction
         = addCanvasSelectAction("group");
      CanvasSelectAction resetAction
         = addCanvasSelectAction("reset");

      // Text

      CanvasSelectAction editTextAction
         = addCanvasSelectAction("textarea.edit");
      CanvasSelectAction textPaintAction
         = addCanvasSelectAction("textarea.colour");
      CanvasSelectAction textOutlineAction
         = addCanvasSelectAction("textarea.outline");
      CanvasSelectAction allFontStylesAction
         = addCanvasSelectAction("font.all_styles");
      CanvasSelectAction fontFamilyAction
         = addCanvasSelectAction("font.family");
      CanvasSelectAction fontSizeAction
         = addCanvasSelectAction("font.size");
      CanvasSelectAction fontShapeAction
         = addCanvasSelectAction("font.shape");
      CanvasSelectAction fontSeriesAction
         = addCanvasSelectAction("font.series");
      CanvasSelectAction fontAnchorBothAction
         = addCanvasSelectAction("font.anchor.both");
      CanvasSelectAction fontAnchorHorizontalAction
         = addCanvasSelectAction("font.anchor.horizontal");
      CanvasSelectAction fontAnchorVerticalAction
         = addCanvasSelectAction("font.anchor.vertical");

      // Shape

      CanvasSelectAction editPathAction
         = addCanvasSelectAction("path.edit");
      CanvasSelectAction distortAction
         = addCanvasSelectAction("distort");
      CanvasSelectAction linePaintAction
         = addCanvasSelectAction("line_colour");
      CanvasSelectAction fillPaintAction
         = addCanvasSelectAction("fill_colour");
      CanvasSelectAction pathStyleAction
         = addCanvasSelectAction("path.style.all_styles");
      CanvasSelectAction lineWidthAction
         = addCanvasSelectAction("linewidth");
      CanvasSelectAction dashPatternAction
         = addCanvasSelectAction("dashpattern");
      CanvasSelectAction capStyleButtAction
         = addCanvasSelectAction("capstyle.butt");
      CanvasSelectAction capStyleRoundAction
         = addCanvasSelectAction("capstyle.round");
      CanvasSelectAction capStyleSquareAction
         = addCanvasSelectAction("capstyle.square");
      CanvasSelectAction joinStyleAction
         = addCanvasSelectAction("joinstyle");
      CanvasSelectAction allMarkersAction
         = addCanvasSelectAction("all_markers");
      CanvasSelectAction startMarkerAction
         = addCanvasSelectAction("path.style.startarrow");
      CanvasSelectAction midMarkerAction
         = addCanvasSelectAction("path.style.midarrow");
      CanvasSelectAction endMarkerAction
         = addCanvasSelectAction("path.style.endarrow");
      CanvasSelectAction evenOddAction
         = addCanvasSelectAction("windingrule.evenodd");
      CanvasSelectAction nonZeroAction
         = addCanvasSelectAction("windingrule.nonzero");

      // Bitmap

      CanvasSelectAction bitmapPropsAction
         = addCanvasSelectAction("bitmap.properties");
      CanvasSelectAction bitmapInsertAction
         = addCanvasSelectAction("bitmap.insert");

      CanvasSelectAction vectorizeAction = null;

      if (getApplication().isVectorizeSupported())
      {
         vectorizeAction = addCanvasSelectAction("vectorize");
      }

      // only text selected popup menu

      selectTextPopupMenu = new JPopupMenu();

      // Description

      selectTextPopupMenu.add(objectDescriptionAction.createMenuItem(
         "menu.selectedtext.textarea_description"));

      // Tag

      selectTextPopupMenu.add(objectTagAction.createMenuItem(
         "menu.selectedtext.textarea_tag"));

      // Edit text

      selectTextPopupMenu.add(editTextAction.createMenuItem(
         "menu.selectedtext.edit",
         "menu.edit.textarea.edit.tooltip"));

      selectTextPopupMenu.add(new JPopupMenu.Separator());

      // Cut

      selectTextPopupMenu.add(cutAction.createMenuItem(
         "menu.selected.cut", "tooltip.cut"));

      // Copy

      selectTextPopupMenu.add(copyAction.createMenuItem(
         "menu.selected.copy", "tooltip.copy"));

      // Paste

      selectTextPopupMenu.add(pasteAction.createMenuItem(
         "menu.selected.paste", "tooltip.paste"));

      selectTextPopupMenu.add(new JPopupMenu.Separator());

      // Select all

      selectTextPopupMenu.add(selectAllAction.createMenuItem(
         "menu.selected.select_all", "tooltip.select_all"));

      // Deselect all

      selectTextPopupMenu.add(deselectAllAction.createMenuItem(
         "menu.selected.deselect_all", "tooltip.deselect_all"));

      // Group

      selectTextPopupMenu.add(groupAction.createMenuItem(
         "menu.selected.group", "tooltip.group"));

      selectTextPopupMenu.add(new JPopupMenu.Separator());

      // Text paint

      selectTextPopupMenu.add(textPaintAction.createMenuItem(
         "menu.selectedtext.colour",
         "menu.edit.textarea.colour.tooltip"));

      selectTextPopupMenu.add(textOutlineAction.createCheckBoxMenuItem(
         "menu.selectedtext.outline", false,
         "menu.edit.textarea.outline.tooltip"));

      selectTextPopupMenu.add(fillPaintAction.createMenuItem(
         "menu.selectedtext.fill_colour",
         "menu.edit.fill_colour.tooltip"));

      selectTextPopupMenu.add(new JPopupMenu.Separator());

      // All font styles

      selectTextPopupMenu.add(allFontStylesAction.createMenuItem(
         "menu.selectedtext.all_styles",
         "menu.edit.textarea.font.all_styles.tooltip"));

      // Family

      selectTextPopupMenu.add(fontFamilyAction.createMenuItem(
         "menu.selectedtext.family",
         "menu.edit.textarea.font.family.tooltip"));

      // Size

      selectTextPopupMenu.add(fontSizeAction.createMenuItem(
         "menu.selectedtext.size",
         "menu.edit.textarea.font.size.tooltip"));

      // Shape

      selectTextPopupMenu.add(fontShapeAction.createMenuItem(
         "menu.selectedtext.shape",
         "menu.edit.textarea.font.shape.tooltip"));

      // Series

      selectTextPopupMenu.add(fontSeriesAction.createMenuItem(
         "menu.selectedtext.series",
         "menu.edit.textarea.font.series.tooltip"));

      // anchor

      JMenu sTextAnchorM = getResources().createAppMenu(
         "menu.selectedtext.anchor");
      selectTextPopupMenu.add(sTextAnchorM);

      // both

      sTextAnchorM.add(fontAnchorBothAction.createMenuItem(
         "menu.edit.textarea.font.anchor.both",
         "menu.edit.textarea.font.anchor.both.tooltip"));

      // horizontal

      sTextAnchorM.add(fontAnchorHorizontalAction.createMenuItem(
         "menu.edit.textarea.font.anchor.horizontal",
         "menu.edit.textarea.font.anchor.horizontal.tooltip"));

      // vertical

      sTextAnchorM.add(fontAnchorVerticalAction.createMenuItem(
         "menu.edit.textarea.font.anchor.vertical",
         "menu.edit.textarea.font.anchor.vertical.tooltip"));

      // Reset Matrix

      selectTextPopupMenu.add(resetAction.createMenuItem(
         "menu.selectedtext.reset",
         "menu.transform.reset.tooltip"));

      // Only path selected popup menu

      selectPathPopupMenu = new JPopupMenu();

      // Description

      selectPathPopupMenu.add(objectDescriptionAction.createMenuItem(
         "menu.selectedpath.path_description"));

      // Tag

      selectPathPopupMenu.add(objectTagAction.createMenuItem(
         "menu.selectedpath.path_tag"));

      // Edit path

      selectPathPopupMenu.add(editPathAction.createCheckBoxMenuItem(
         "menu.selectedpath.edit", false, "menu.edit.path.edit.tooltip"));

      selectPathPopupMenu.add(new JPopupMenu.Separator());

      // Cut

      selectPathPopupMenu.add(cutAction.createMenuItem(
         "menu.selected.cut", "tooltip.cut"));

      // Copy

      selectPathPopupMenu.add(copyAction.createMenuItem(
         "menu.selected.copy", "tooltip.copy"));

      // Paste

      selectPathPopupMenu.add(pasteAction.createMenuItem(
         "menu.selected.paste", "tooltip.paste"));

      selectPathPopupMenu.add(new JPopupMenu.Separator());

      // Select all

      selectPathPopupMenu.add(selectAllAction.createMenuItem(
         "menu.selected.select_all", "tooltip.select_all"));

      // Deselect all

      selectPathPopupMenu.add(deselectAllAction.createMenuItem(
         "menu.selected.deselect_all", "tooltip.deselect_all"));

      // Group

      selectPathPopupMenu.add(groupAction.createMenuItem(
         "menu.selected.group", "tooltip.group"));

      selectPathPopupMenu.add(new JPopupMenu.Separator());

      // Distort

      selectPathPopupMenu.add(distortAction.createCheckBoxMenuItem(
         "menu.selectedpath.distort", false, "menu.transform.distort.tooltip"));

      selectPathPopupMenu.add(new JPopupMenu.Separator());

      // Line paint

      selectPathPopupMenu.add(linePaintAction.createMenuItem(
         "menu.selectedpath.line_colour",
         "menu.edit.path.line_colour.tooltip"));

      // Fill paint

      selectPathPopupMenu.add(fillPaintAction.createMenuItem(
         "menu.selectedpath.fill_colour",
         "menu.edit.fill_colour.tooltip"));

      selectPathPopupMenu.add(new JPopupMenu.Separator());

      // All styles

      selectPathPopupMenu.add(pathStyleAction.createMenuItem(
         "menu.selectedpath.all_styles",
         "menu.edit.path.style.all_styles.tooltip"));

      // Pen width

      selectPathPopupMenu.add(lineWidthAction.createMenuItem(
         "menu.selectedpath.linewidth",
         "menu.edit.path.style.linewidth.tooltip"));

      // Dash pattern

      selectPathPopupMenu.add(dashPatternAction.createMenuItem(
         "menu.selectedpath.dashpattern",
         "menu.edit.path.style.dashpattern.tooltip"));

      // Cap submenu

      JMenu capMenu = getResources().createAppMenu("menu.selectedpath.capstyle");
      selectPathPopupMenu.add(capMenu);

      ButtonGroup capGroup = new ButtonGroup();

      // Butt cap

      capMenu.add(capStyleButtAction.createRadioButtonMenuItem(
         "menu.edit.path.style.capstyle.butt", capGroup, false, 
         "menu.edit.path.style.capstyle.butt.tooltip"));

      // Round cap

      capMenu.add(capStyleRoundAction.createRadioButtonMenuItem(
         "menu.edit.path.style.capstyle.round", capGroup, false, 
         "menu.edit.path.style.capstyle.round.tooltip"));

      // Square cap

      capMenu.add(capStyleSquareAction.createRadioButtonMenuItem(
         "menu.edit.path.style.capstyle.square", capGroup, false, 
         "menu.edit.path.style.capstyle.square.tooltip"));

      // Join

      selectPathPopupMenu.add(joinStyleAction.createMenuItem(
         "menu.selectedpath.joinstyle",
         "menu.edit.path.style.joinstyle.tooltip"));

      // Markers

      JMenu markerMenu = getResources().createAppMenu("menu.selectedpath.marker");
      selectPathPopupMenu.add(markerMenu);

      // All markers

      markerMenu.add(allMarkersAction.createMenuItem(
         "menu.edit.path.style.all_markers",
         "menu.edit.path.style.all_markers.tooltip"));

      markerMenu.addSeparator();

      // Start Marker

      markerMenu.add(startMarkerAction.createMenuItem(
         "menu.edit.path.style.startarrow",
         "menu.edit.path.style.startarrow.tooltip"));

      // Mid Marker

      markerMenu.add(midMarkerAction.createMenuItem(
         "menu.edit.path.style.midarrow",
         "menu.edit.path.style.midarrow.tooltip"));

      // End Marker

      markerMenu.add(endMarkerAction.createMenuItem(
         "menu.edit.path.style.endarrow",
         "menu.edit.path.style.endarrow.tooltip"));

      // Winding rule sub menu

      JMenu windingMenu = getResources().createAppMenu(
         "menu.selectedpath.windingrule");
      selectPathPopupMenu.add(windingMenu);

      ButtonGroup windingGroup = new ButtonGroup();

      // Even odd

      windingMenu.add(evenOddAction.createRadioButtonMenuItem(
         "menu.edit.path.style.windingrule.evenodd", windingGroup, false,
         "menu.edit.path.style.windingrule.evenodd.tooltip"));

      // Non zero

      windingMenu.add(nonZeroAction.createRadioButtonMenuItem(
         "menu.edit.path.style.windingrule.nonzero", windingGroup, false,
         "menu.edit.path.style.windingrule.nonzero.tooltip"));

      // only textpaths selected popup menu

      selectTextPathPopupMenu = new JPopupMenu();

      // Description

      selectTextPathPopupMenu.add(objectDescriptionAction.createMenuItem(
         "menu.selectedtextpath.textpath_description"));

      // Tag

      selectTextPathPopupMenu.add(objectTagAction.createMenuItem(
         "menu.selectedtextpath.textpath_tag"));

      // Edit text

      selectTextPathPopupMenu.add(editTextAction.createMenuItem(
         "menu.selectedtext.edit",
         "menu.edit.textarea.edit.tooltip"));

      // Edit Path

      selectTextPathPopupMenu.add(editPathAction.createCheckBoxMenuItem(
         "menu.selectedpath.edit", false, "menu.edit.path.edit.tooltip"));

      selectTextPathPopupMenu.add(new JPopupMenu.Separator());

      // Cut

      selectTextPathPopupMenu.add(cutAction.createMenuItem(
         "menu.selected.cut", "tooltip.cut"));

      // Copy

      selectTextPathPopupMenu.add(copyAction.createMenuItem(
         "menu.selected.copy", "tooltip.copy"));

      // Paste

      selectTextPathPopupMenu.add(pasteAction.createMenuItem(
         "menu.selected.paste", "tooltip.paste"));

      selectTextPathPopupMenu.add(new JPopupMenu.Separator());

      // Select all

      selectTextPathPopupMenu.add(selectAllAction.createMenuItem(
         "menu.selected.select_all", "tooltip.select_all"));

      // Deselect all

      selectTextPathPopupMenu.add(deselectAllAction.createMenuItem(
         "menu.selected.deselect_all", "tooltip.deselect_all"));

      // Group

      selectTextPathPopupMenu.add(groupAction.createMenuItem(
         "menu.selected.group", "tooltip.group"));

      selectTextPathPopupMenu.add(new JPopupMenu.Separator());

      // Text paint

      selectTextPathPopupMenu.add(textPaintAction.createMenuItem(
         "menu.selectedtext.colour", "menu.edit.textarea.colour.tooltip"));

      selectTextPathPopupMenu.add(textOutlineAction.createCheckBoxMenuItem(
         "menu.selectedtext.outline", false,
         "menu.edit.textarea.outline.tooltip"));

      selectTextPathPopupMenu.add(fillPaintAction.createMenuItem(
         "menu.selectedtext.fill_colour",
         "menu.edit.fill_colour.tooltip"));

      selectTextPathPopupMenu.add(new JPopupMenu.Separator());

      // All font styles

      selectTextPathPopupMenu.add(allFontStylesAction.createMenuItem(
         "menu.selectedtext.all_styles",
         "menu.edit.textarea.font.all_styles.tooltip"));

      // Family

      selectTextPathPopupMenu.add(fontFamilyAction.createMenuItem(
         "menu.selectedtext.family",
         "menu.edit.textarea.font.family.tooltip"));

      // Size

      selectTextPathPopupMenu.add(fontSizeAction.createMenuItem(
         "menu.selectedtext.size",
         "menu.edit.textarea.font.size.tooltip"));

      // Shape

      selectTextPathPopupMenu.add(fontShapeAction.createMenuItem(
         "menu.selectedtext.shape",
         "menu.edit.textarea.font.shape.tooltip"));

      // Series

      selectTextPathPopupMenu.add(fontSeriesAction.createMenuItem(
         "menu.selectedtext.series",
         "menu.edit.textarea.font.series.tooltip"));

      // anchor

      sTextAnchorM = getResources().createAppMenu(
         "menu.selectedtext.anchor");
      selectTextPathPopupMenu.add(sTextAnchorM);

      // both

      sTextAnchorM.add(fontAnchorBothAction.createMenuItem(
         "menu.edit.textarea.font.anchor.both",
         "menu.edit.textarea.font.anchor.both.tooltip"));

      // horizontal

      sTextAnchorM.add(fontAnchorHorizontalAction.createMenuItem(
         "menu.edit.textarea.font.anchor.horizontal",
         "menu.edit.textarea.font.anchor.horizontal.tooltip"));

      // vertical

      sTextAnchorM.add(fontAnchorVerticalAction.createMenuItem(
         "menu.edit.textarea.font.anchor.vertical",
         "menu.edit.textarea.font.anchor.vertical.tooltip"));

      // Reset Matrix

      selectTextPathPopupMenu.add(resetAction.createMenuItem(
         "menu.selectedtext.reset",
         "menu.transform.reset.tooltip"));

      // only bitmaps selected popup menu

      selectBitmapPopupMenu = new JPopupMenu();

      // Description

      selectBitmapPopupMenu.add(objectDescriptionAction.createMenuItem(
         "menu.selectedbitmap.bitmap_description"));

      // Tag

      selectBitmapPopupMenu.add(objectTagAction.createMenuItem(
         "menu.selectedbitmap.bitmap_tag"));

      // Properties

      selectBitmapPopupMenu.add(bitmapPropsAction.createMenuItem(
         "menu.selectedbitmap.properties",
         "menu.bitmap.properties.tooltip"));

      // Reset

      selectBitmapPopupMenu.add(resetAction.createMenuItem(
         "menu.selectedbitmap.reset", 
         "menu.transform.reset.tooltip"));

      // Insert

      selectBitmapPopupMenu.add(bitmapInsertAction.createMenuItem(
         "menu.selectedbitmap.insert", 
         "menu.bitmap.insert.tooltip"));

      selectBitmapPopupMenu.add(new JPopupMenu.Separator());

      // Cut

      selectBitmapPopupMenu.add(cutAction.createMenuItem(
         "menu.selected.cut", "tooltip.cut"));

      // Copy

      selectBitmapPopupMenu.add(copyAction.createMenuItem(
         "menu.selected.copy", "tooltip.copy"));

      // Paste

      selectBitmapPopupMenu.add(pasteAction.createMenuItem(
         "menu.selected.paste", "tooltip.paste"));

      selectBitmapPopupMenu.add(new JPopupMenu.Separator());

      // Select all

      selectBitmapPopupMenu.add(selectAllAction.createMenuItem(
         "menu.selected.select_all", "tooltip.select_all"));

      // Deselect all

      selectBitmapPopupMenu.add(deselectAllAction.createMenuItem(
         "menu.selected.deselect_all", "tooltip.deselect_all"));

      // Group

      selectBitmapPopupMenu.add(groupAction.createMenuItem(
         "menu.selected.group", "tooltip.group"));

      if (vectorizeAction != null)
      {
         selectBitmapPopupMenu.add(new JPopupMenu.Separator());

         // Vectorize

         selectBitmapPopupMenu.add(vectorizeAction.createMenuItem(
            "menu.selectedbitmap.vectorize",
            "menu.bitmap.vectorize.tooltip"));
      }

      // selected objects popup menu

      selectPopupMenu = new JPopupMenu();

      // Description

      selectPopupMenu.add(objectDescriptionAction.createMenuItem(
         "menu.selected.object_description"));

      // Tag

      selectPopupMenu.add(objectTagAction.createMenuItem(
         "menu.selected.object_tag"));

      selectPopupMenu.add(new JPopupMenu.Separator());

      // Cut

      selectPopupMenu.add(cutAction.createMenuItem(
         "menu.selected.cut", "tooltip.cut"));

      // Copy

      selectPopupMenu.add(copyAction.createMenuItem(
         "menu.selected.copy", "tooltip.copy"));

      // Paste

      selectPopupMenu.add(pasteAction.createMenuItem(
         "menu.selected.paste", "tooltip.paste"));

      selectPopupMenu.add(new JPopupMenu.Separator());

      // Select all

      selectPopupMenu.add(selectAllAction.createMenuItem(
         "menu.selected.select_all", "tooltip.select_all"));

      // Deselect all

      selectPopupMenu.add(deselectAllAction.createMenuItem(
         "menu.selected.deselect_all", "tooltip.deselect_all"));

      // Group

      selectPopupMenu.add(groupAction.createMenuItem(
         "menu.selected.group", "tooltip.group"));

      // Ungroup

      selectPopupMenu.add(addCanvasSelectAction("ungroup").createMenuItem(
         "menu.selected.ungroup", "tooltip.ungroup"));

      selectPopupMenu.add(new JPopupMenu.Separator());

      // Paths sub menu

      JMenu pathMenu = addCanvasSelectAction("edit.path").createMenu(
         "menu.selected.path",
         "menu.edit.path.edit.tooltip");

      selectPopupMenu.add(pathMenu);

      // Line paint

      pathMenu.add(linePaintAction.createMenuItem(
         "menu.selectedpath.line_colour",
         "menu.edit.path.line_colour.tooltip"));

      selectPopupMenu.add(pathMenu);

      // All styles

      pathMenu.add(pathStyleAction.createMenuItem(
         "menu.selectedpath.all_styles",
         "menu.edit.path.style.all_styles.tooltip"));

      // Pen width

      pathMenu.add(lineWidthAction.createMenuItem(
         "menu.selectedpath.linewidth",
         "menu.edit.path.style.linewidth.tooltip"));

      // Dash pattern

      pathMenu.add(dashPatternAction.createMenuItem(
         "menu.selectedpath.dashpattern",
         "menu.edit.path.style.dashpattern.tooltip"));

      // Cap submenu

      capMenu = getResources().createAppMenu("menu.selectedpath.capstyle");
      pathMenu.add(capMenu);

      capGroup = new ButtonGroup();

      // Butt cap

      capMenu.add(capStyleButtAction.createRadioButtonMenuItem(
         "menu.edit.path.style.capstyle.butt", capGroup, false, 
         "menu.edit.path.style.capstyle.butt.tooltip"));

      // Round cap

      capMenu.add(capStyleRoundAction.createRadioButtonMenuItem(
         "menu.edit.path.style.capstyle.round", capGroup, false, 
         "menu.edit.path.style.capstyle.round.tooltip"));

      // Square cap

      capMenu.add(capStyleSquareAction.createRadioButtonMenuItem(
         "menu.edit.path.style.capstyle.square", capGroup, false, 
         "menu.edit.path.style.capstyle.square.tooltip"));

      // Join

      pathMenu.add(joinStyleAction.createMenuItem(
         "menu.selectedpath.joinstyle",
         "menu.edit.path.style.joinstyle.tooltip"));

      // Markers

      markerMenu = getResources().createAppMenu("menu.selectedpath.marker");
      pathMenu.add(markerMenu);

      // All markers

      markerMenu.add(allMarkersAction.createMenuItem(
         "menu.edit.path.style.all_markers",
         "menu.edit.path.style.all_markers.tooltip"));

      markerMenu.addSeparator();

      // Start Marker

      markerMenu.add(startMarkerAction.createMenuItem(
         "menu.edit.path.style.startarrow",
         "menu.edit.path.style.startarrow.tooltip"));

      // Mid Marker

      markerMenu.add(midMarkerAction.createMenuItem(
         "menu.edit.path.style.midarrow",
         "menu.edit.path.style.midarrow.tooltip"));

      // End Marker

      markerMenu.add(endMarkerAction.createMenuItem(
         "menu.edit.path.style.endarrow",
         "menu.edit.path.style.endarrow.tooltip"));

      // Winding rule sub menu

      windingMenu = getResources().createAppMenu(
         "menu.selectedpath.windingrule");
      pathMenu.add(windingMenu);

      windingGroup = new ButtonGroup();

      // Even odd

      windingMenu.add(evenOddAction.createRadioButtonMenuItem(
         "menu.edit.path.style.windingrule.evenodd", windingGroup, false,
         "menu.edit.path.style.windingrule.evenodd.tooltip"));

      // Non zero

      windingMenu.add(nonZeroAction.createRadioButtonMenuItem(
         "menu.edit.path.style.windingrule.nonzero", windingGroup, false,
         "menu.edit.path.style.windingrule.nonzero.tooltip"));

      // Text sub menu

      JMenu textMenu = addCanvasSelectAction("edit.textarea").createMenu(
         "menu.selected.textarea",
         "menu.edit.textarea.edit.tooltip");
      selectPopupMenu.add(textMenu);

      // Text paint

      textMenu.add(textPaintAction.createMenuItem(
         "menu.selectedtext.colour",
         "menu.edit.textarea.colour.tooltip"));

      textMenu.add(textOutlineAction.createCheckBoxMenuItem(
         "menu.selectedtext.outline", false,
         "menu.edit.textarea.outline.tooltip"));

      textMenu.add(new JPopupMenu.Separator());

      // All font styles

      textMenu.add(allFontStylesAction.createMenuItem(
         "menu.selectedtext.all_styles",
         "menu.edit.textarea.font.all_styles.tooltip"));

      // Family

      textMenu.add(fontFamilyAction.createMenuItem(
         "menu.selectedtext.family",
         "menu.edit.textarea.font.family.tooltip"));

      // Size

      textMenu.add(fontSizeAction.createMenuItem(
         "menu.selectedtext.size",
         "menu.edit.textarea.font.size.tooltip"));

      // Shape

      textMenu.add(fontShapeAction.createMenuItem(
         "menu.selectedtext.shape",
         "menu.edit.textarea.font.shape.tooltip"));

      // Series

      textMenu.add(fontSeriesAction.createMenuItem(
         "menu.selectedtext.series",
         "menu.edit.textarea.font.series.tooltip"));

      // anchor

      sTextAnchorM = getResources().createAppMenu(
         "menu.selectedtext.anchor");
      textMenu.add(sTextAnchorM);

      // both

      sTextAnchorM.add(fontAnchorBothAction.createMenuItem(
         "menu.edit.textarea.font.anchor.both"));

      // horizontal

      sTextAnchorM.add(fontAnchorHorizontalAction.createMenuItem(
         "menu.edit.textarea.font.anchor.horizontal"));

      // vertical

      sTextAnchorM.add(fontAnchorVerticalAction.createMenuItem(
         "menu.edit.textarea.font.anchor.vertical"));

      // Reset

      textMenu.add(resetAction.createMenuItem(
         "menu.selectedtext.reset", "menu.transform.reset"));

      // Fill paint

      selectPopupMenu.add(fillPaintAction.createMenuItem(
         "menu.selected.fill_colour",
         "menu.edit.fill_colour.tooltip"));

      // bitmap sub menu

      JMenu bitmapMenu = addCanvasSelectAction("bitmap").createMenu(
         "menu.selected.bitmap");
      selectPopupMenu.add(bitmapMenu);

      // Insert Bitmap 

      bitmapMenu.add(bitmapInsertAction.createMenuItem(
         "menu.selectedbitmap.insert", "menu.bitmap.insert.tooltip"));

      // Bitmap Properties

      bitmapMenu.add(bitmapPropsAction.createMenuItem(
         "menu.selectedbitmap.properties",
         "menu.bitmap.properties.tooltip"));

      bitmapMenu.add(resetAction.createMenuItem(
         "menu.selectedbitmap.reset",
         "menu.transform.reset.tooltip"));

      selectPopupMenu.add(new JPopupMenu.Separator());

      // Justify sub menu

      JMenu justifyMenu = addCanvasSelectAction("justify").createMenu(
         "menu.selected.justify",
         "menu.transform.justify.tooltip");
      selectPopupMenu.add(justifyMenu);

      // Left align

      justifyMenu.add(addCanvasSelectAction("justify.left").createMenuItem(
         "menu.transform.justify.left",
         "menu.transform.justify.left.tooltip"));

      // Centre align

      justifyMenu.add(addCanvasSelectAction("justify.centre").createMenuItem(
         "menu.transform.justify.centre",
         "menu.transform.justify.centre.tooltip"));

      // Right align

      justifyMenu.add(addCanvasSelectAction("justify.right").createMenuItem(
         "menu.transform.justify.right",
         "menu.transform.justify.right.tooltip"));

      justifyMenu.add(new JPopupMenu.Separator());

      // Top align

      justifyMenu.add(addCanvasSelectAction("justify.top").createMenuItem(
         "menu.transform.justify.top",
         "menu.transform.justify.top.tooltip"));

      // middle align

      justifyMenu.add(addCanvasSelectAction("justify.middle").createMenuItem(
         "menu.transform.justify.middle",
         "menu.transform.justify.middle.tooltip"));

      // bottom align

      justifyMenu.add(addCanvasSelectAction("justify.bottom").createMenuItem(
         "menu.transform.justify.bottom",
         "menu.transform.justify.bottom.tooltip"));

      // none selected popup menu

      noneSelectedPopupMenu = new JPopupMenu();

      // Image description

      noneSelectedPopupMenu.add(
         addCanvasSelectAction("image_description").createMenuItem(
         "menu.none.image_description"));

      // Select all

      noneSelectedPopupMenu.add(selectAllAction.createMenuItem(
         "menu.selected.select_all", "tooltip.select_all"));

      // Find by description

      noneSelectedPopupMenu.add(
         addCanvasSelectAction("navigate.by_description").createMenuItem(
         "menu.none.find_by_description",
         "menu.navigate.by_description.tooltip"));

      // Paste

      noneSelectedPopupMenu.add(pasteAction.createMenuItem(
         "menu.selected.paste", "tooltip.paste"));

      // Insert bitmap

      noneSelectedPopupMenu.add(bitmapInsertAction.createMenuItem(
         "menu.none.insert_bitmap",
         "menu.bitmap.insert.tooltip"));

      // Distortion Popup Menu

      distortPopupMenu = new JPopupMenu();

      distortPopupMenu.add(distortAction.createCheckBoxMenuItem(
         "menu.distort.state", false, "menu.transform.distort.tooltip"));

      distortPopupMenu.add(resetAction.createMenuItem(
         "menu.distort.reset",
         "menu.distort.reset.tooltip"));
   }

   protected void addAppAction(String name)
   {
      FlowframTkAction action = getApplication().getAppAction(name);

      if (action == null)
      {
         throw new IllegalArgumentException("Unknown action '"+name+"'");
      }

      appActionList.add(action);
   }

   protected void addAppSelectAction(String name)
   {
      FlowframTkAction action = getApplication().getSelectAction(name);

      if (action == null)
      {
         throw new IllegalArgumentException("Unknown action '"+name+"'");
      }

      appActionList.add(action);
   }

   protected void addAppToolAction(String name)
   {
      FlowframTkAction action = getApplication().getToolAction(name);

      if (action == null)
      {
         throw new IllegalArgumentException("Unknown action '"+name+"'");
      }

      appActionList.add(action);

   }

   protected CanvasAction addCanvasGeneralAction(String name)
   {
      try
      {
         CanvasAction action = new CanvasAction(this,
            getApplication().getAppAction(name));
         addCanvasAction(action);

         return action;
      }
      catch (NullPointerException e)
      {
         throw new IllegalArgumentException("Unknown action '"+name+"'", e);
      }
   }

   protected CanvasSelectAction addCanvasSelectAction(String name)
   {
      try
      {
         CanvasSelectAction action = new CanvasSelectAction(this,
            getApplication().getAppAction(name));
         addCanvasAction(action);

         return action;
      }
      catch (NullPointerException e)
      {
         throw new IllegalArgumentException("Unknown action '"+name+"'", e);
      }
   }

   public void addCanvasAction(CanvasAction action)
   {
      if (action == null)
      {
         throw new NullPointerException();
      }

      if (action instanceof EditPathAction)
      {
         editPathActionList.add(action);
      }
      else if (action.getValidConstructionFlag()
       == FlowframTkAction.CONSTRUCTION_FLAG_TEXT)
      {
         textConstructionActionList.add(action);
      }
      else if (action instanceof CanvasSelectAction)
      {
         int selectFlag = action.getValidSelectionFlag();

         if (selectFlag == SELECT_FLAG_ANY)
         {
            selectGeneralActionList.add((CanvasSelectAction)action);
         }
         else if (((selectFlag & SELECT_FLAG_TEXTUAL) != 0)
               || ((selectFlag & SELECT_FLAG_TEXT) != 0))
         {
            selectTextActionList.add((CanvasSelectAction)action);
         }
         else if ((selectFlag &
                  (SELECT_FLAG_SHAPE | SELECT_FLAG_NON_TEXTUAL_SHAPE)) != 0)
         {
            selectPathActionList.add((CanvasSelectAction)action);
         }
         else if ((selectFlag & SELECT_FLAG_GROUP) != 0)
         {
            selectGroupActionList.add((CanvasSelectAction)action);
         }
         else if ((selectFlag & SELECT_FLAG_BITMAP) != 0)
         {
            selectBitmapActionList.add((CanvasSelectAction)action);
         }
         else
         {
            selectGeneralActionList.add((CanvasSelectAction)action);
         }
      }
      else
      {
         generalActionList.add(action);
      }
   }

   public CanvasAction getEditPathAction(String actionName)
   {
      for (CanvasAction action : editPathActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      return null;
   }

   public CanvasAction getTextConstructionAction(String actionName)
   {
      for (CanvasAction action : textConstructionActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      return null;
   }

   public CanvasSelectAction getSelectTextAction(String actionName)
   {
      for (CanvasSelectAction action : selectTextActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      return null;
   }

   public CanvasSelectAction getSelectGroupAction(String actionName)
   {
      for (CanvasSelectAction action : selectGroupActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      return null;
   }

   public CanvasSelectAction getSelectBitmapAction(String actionName)
   {
      for (CanvasSelectAction action : selectBitmapActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      return null;
   }

   public CanvasSelectAction getSelectPathAction(String actionName)
   {
      for (CanvasSelectAction action : selectPathActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      return null;
   }

   public CanvasSelectAction getSelectGeneralAction(String actionName)
   {
      for (CanvasSelectAction action : selectGeneralActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      return null;
   }


   public CanvasAction getGeneralAction(String actionName)
   {
      for (CanvasAction action : generalActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      return null;
   }

   public void updateTextConstructionActions()
   {
      boolean ioInProgress = getFrame().isIoInProgress();
      int currentTool = getCanvasGraphics().getTool();
      byte currentEditFlag = getEditFlag();
      byte currentConstructionFlag = getConstructionFlag();

      for (CanvasAction action : textConstructionActionList)
      {
          action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, null);
      }
   }

   public void updateEditPathActions()
   {
      boolean ioInProgress = getFrame().isIoInProgress();
      int currentTool = getCanvasGraphics().getTool();
      byte currentEditFlag = getEditFlag();
      byte currentConstructionFlag = getConstructionFlag();

      JDRSelection selection;

      if (editedPath == null)
      {
         selection = null;
      }
      else
      {
         selection = new JDRSelection();
         selection.addToSelection(editedPath);
      }

      for (CanvasAction action : editPathActionList)
      {
          action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);

          AbstractButton button = action.getActionButton();

          if (button.isEnabled())
          {
             String actionCmd = action.getActionCommand();

             if (actionCmd.equals("has_symmetry"))
             {
                button.setSelected(
                  (selection.getSelectionFlag() & 
                   (SELECT_FLAG_SYMMETRIC | SELECT_FLAG_SYMTEXTPATH)) != 0);
             }
             else if (actionCmd.equals("join_anchor"))
             {
                button.setSelected(
                  (selection.getSelectionFlag()
                 & SELECT_FLAG_SYMMETRIC_ANCHORED_JOIN) != 0);
             }
             else if (actionCmd.equals("close_anchor"))
             {
                button.setSelected(
                  (selection.getSelectionFlag()
                 & SELECT_FLAG_SYMMETRIC_ANCHORED_CLOSE) != 0);
             }
             else if (actionCmd.equals("continuity.anchor"))
             {
                int controlFlag = getSelectedControlFlag();
                button.setSelected(
                  (controlFlag & CONTROL_FLAG_ANCHORED) != 0);
             }
          }
      }
   }

   public JPopupMenu updateSelectActions()
   {
      boolean ioInProgress = getFrame().isIoInProgress();
      int currentTool = getCanvasGraphics().getTool();
      byte currentEditFlag = getEditFlag();
      byte currentConstructionFlag = getConstructionFlag();

      JDRSelection selection = getSelectionFlags();

      for (CanvasAction action : selectGeneralActionList)
      {
          action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);

          if (action.isEnabled())
          {
             String actionCmd = action.getActionCommand();

             if (actionCmd.equals("distort"))
             {
                action.setSelected(editedDistortion != null);
             }
          }
      }

      int selectionFlag = (selection == null ? SELECT_FLAG_NONE :
          selection.getSelectionFlag());

      JDRBasicStroke stroke = null;

      if ((selectionFlag & SELECT_FLAG_NON_TEXTUAL_SHAPE) != 0)
      {
         stroke = getSelectedBasicStroke();
      }

      for (CanvasAction action : selectPathActionList)
      {
          action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);

          if (action.isEnabled())
          {
             String actionCmd = action.getActionCommand();

             if (actionCmd.equals("path.edit"))
             {
                action.setSelected(currentEditFlag == EDIT_FLAG_PATH);
             }
             else if (stroke != null)
             {
                if (actionCmd.equals("capstyle.butt"))
                {
                   action.setSelected(
                     stroke.getCapStyle() == BasicStroke.CAP_BUTT);
                }
                else if (actionCmd.equals("capstyle.round"))
                {
                   action.setSelected(
                     stroke.getCapStyle() == BasicStroke.CAP_ROUND);
                }
                else if (actionCmd.equals("capstyle.square"))
                {
                   action.setSelected(
                     stroke.getCapStyle() == BasicStroke.CAP_SQUARE);
                }
                else if (actionCmd.equals("windingrule.evenodd"))
                {
                   action.setSelected(
                     stroke.getWindingRule() == GeneralPath.WIND_EVEN_ODD);
                }
                else if (actionCmd.equals("windingrule.nonzero"))
                {
                   action.setSelected(
                     stroke.getWindingRule() == GeneralPath.WIND_NON_ZERO);
                }
             }
          }
      }

      for (CanvasAction action : selectTextActionList)
      {
          action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);

          if (action.isEnabled())
          {
             String actionCmd = action.getActionCommand();

             if (actionCmd.equals("text.outline"))
             {
                action.setSelected(getSelectedTextual().isOutline());
             }
          }
      }

      for (CanvasAction action : selectBitmapActionList)
      {
          action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);
      }

      for (CanvasAction action : selectGroupActionList)
      {
          action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);
      }

      if (editedDistortion != null)
      {
         return distortPopupMenu;
      }

      if (selectionFlag == SELECT_FLAG_NONE)
      {
         return noneSelectedPopupMenu;
      }

      if ((selectionFlag & SELECT_FLAG_GROUP) != 0)
      {
         return selectPopupMenu;
      }

      if ((selectionFlag & 
           (SELECT_FLAG_TEXTPATH | SELECT_FLAG_SYMTEXTPATH)) != 0)
      {
         if (((selectionFlag & SELECT_FLAG_TEXT) != 0)
          || ((selectionFlag & SELECT_FLAG_NON_TEXTUAL_SHAPE) != 0)
          || ((selectionFlag & SELECT_FLAG_BITMAP) != 0))
         {
            return selectPopupMenu;
         }
         else
         {
            return selectTextPathPopupMenu;
         }
      }

      if ((selectionFlag & SELECT_FLAG_TEXT) != 0)
      {
         if (((selectionFlag & SELECT_FLAG_SHAPE) != 0)
          || ((selectionFlag & SELECT_FLAG_BITMAP) != 0))
         {
            return selectPopupMenu;
         }
         else
         {
            return selectTextPopupMenu;
         }
      }

      if ((selectionFlag & SELECT_FLAG_SHAPE) != 0)
      {
         if ((selectionFlag & SELECT_FLAG_BITMAP) != 0)
         {
            return selectPopupMenu;
         }
         else
         {
            return selectPathPopupMenu;
         }
      }

      if ((selectionFlag & SELECT_FLAG_BITMAP) != 0)
      {
         return selectBitmapPopupMenu;
      }

      return selectPopupMenu;
   }

   public void updateGeneralActions(boolean useSelectionFlags)
   {
      boolean ioInProgress = getFrame().isIoInProgress();
      int currentTool = getCanvasGraphics().getTool();
      byte currentEditFlag = getEditFlag();
      byte currentConstructionFlag = getConstructionFlag();

      JDRSelection selection = null;

      if (useSelectionFlags)
      {
         selection = getSelectionFlags();
      }

      for (CanvasAction action : generalActionList)
      {
          action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);
      }

      for (FlowframTkAction action : appActionList)
      {
          action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);
      }
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      if (paths.getCanvasGraphics() != cg)
      {
         paths.applyCanvasGraphics(cg);
      }

      paths.getCanvasGraphics().setComponent(this);
   }

   public void setStorageUnit(byte unitId)
   {
      if (getCanvasGraphics().getStorageUnitID() == unitId)
      {
         return;
      }

      try
      {
         if (editedPath != null)
         {
            JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
            ce.addEdit(new EditPath(editedPath,false));
            ce.addEdit(new SetStorageUnit(unitId));

            ce.end();
            frame_.postEdit(ce);
         }
         else
         {
            UndoableEdit edit = new SetStorageUnit(unitId);
            frame_.postEdit(edit);
         }
      }
      catch (Throwable e)
      {
         getResources().internalError(this, e);
      }
   }

   public void markAsModified()
   {
      frame_.markAsModified();
   }

   public JDRGroup getAllPaths()
   {
      return paths;
   }

   public Point2D mouseToStorage()
   {
      Point pt = getMousePosition(true);

      if (pt == null)
      {
         return null;
      }

      return getCanvasGraphics().componentToStorage(pt);
   }

   public Point2D mouseToBp()
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (cg.getStorageUnitID() == JDRUnit.BP)
      {
         return mouseToStorage();
      }

      Point pt = getMousePosition(true);

      if (pt == null)
      {
         return null;
      }

      return new Point2D.Double(
         cg.componentXToBp(pt.getX()), cg.componentYToBp(pt.getY()));
   }

   public void deleteCurrentControlPoint()
   {
      int tool = frame_.currentTool();

      if (tool != ACTION_SELECT) return;

      if (editedPath == null) return;

      if (editedPath.getSelectedSegment() instanceof JDRBezier)
      {
         JDRBezier curve = (JDRBezier)editedPath.getSelectedSegment();

         JDRPoint editedPt = editedPath.getSelectedControl();

         if (editedPt == curve.getControl1()
          || editedPt == curve.getControl2())
         {
            return;
         }
      }

      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         int n = editedPath.size();

         if (n == 1 || (n == 2 && editedPath.isClosed()))
         {
            JDRCompleteObject object = null;

            // Remove path

            for (int i = 0, m = paths.size(); i < m; i++)
            {
               object = paths.get(i);

               if (object == editedPath)
               {
                  UndoableEdit edit = new EditPath(editedPath,false);
                  ce.addEdit(edit);
                  edit = new RemoveObject(object, i);
                  ce.addEdit(edit);
                  break;
               }
            }
         }
         else
         {
            UndoableEdit edit = new DeletePoint(editedPath);
            ce.addEdit(edit);
         }

         ce.end();
         frame_.postEdit(ce);
      }
      catch (ClosingMoveException e)
      {
         closingMoveError(getMessage("undo.delete_point"), e);
      }
      catch (InvalidPathException e)
      {
         getResources().internalError(frame_, e);
      }
   }

   public void addControlPoint()
   {
      if (editedPath == null) return;

      UndoableEdit edit = new AddPoint(editedPath);
      frame_.postEdit(edit);
   }

   public void segmentInfo()
   {
      if (editedPath == null) return;

      JDRPathSegment segment = editedPath.getSelectedSegment();

      if (segment == null) return;

      frame_.displaySegmentInfoDialog(editedPath, segment);
   }

   public void convertToLine()
   {
      if (editedPath == null) return;

      // If the current path is a symmetric path and the
      // selected segment is the line of symmetry, do
      // nothing. (Menu item should be disabled to prevent
      // this.)

      JDRPathSegment segment = editedPath.getSelectedSegment();

      if (segment == null) return;

      UndoableEdit edit;

      try
      {
         if (editedPath instanceof JDRSymmetricPath)
         {
            JDRSymmetricPath path = (JDRSymmetricPath)editedPath;

            if (segment == path.getSymmetry())
            {
               return;
            }
            else if (segment == path.getJoin())
            {
               edit = new JoinToLine(path);
            }
            else if (segment == path.getClosingSegment())
            {
               edit = new ClosingToLine(path);
            }
            else
            {
               edit = new ConvertToLine(editedPath, segment);
            }
         }
         else
         {
            edit = new ConvertToLine(editedPath, segment);
         }

         frame_.postEdit(edit);
      }
      catch (ClosingMoveException e)
      {
         closingMoveError(getMessage("undo.convert_to_line"), e);
      }
      catch (Exception e)
      {
         getResources().internalError(frame_, e);
      }
   }

   public void convertToCurve()
   {
      if (editedPath == null) return;

      // If the current path is a symmetric path and the
      // selected segment is the line of symmetry, do
      // nothing. (Menu item should be disabled to prevent
      // this.)

      JDRPathSegment segment = editedPath.getSelectedSegment();

      if (segment == null) return;

      UndoableEdit edit;

      try
      {
         if (editedPath instanceof JDRSymmetricPath)
         {
            JDRSymmetricPath path = (JDRSymmetricPath)editedPath;

            if (segment == path.getSymmetry())
            {
               return;
            }
            else if (segment == path.getJoin())
            {
               edit = new JoinToCurve(path);
            }
            else if (segment == path.getClosingSegment())
            {
               edit = new ClosingToCurve(path);
            }
            else
            {
               edit = new ConvertToCurve(editedPath, segment);
            }
         }
         else
         {
            edit = new ConvertToCurve(editedPath, segment);
         }

         frame_.postEdit(edit);
      }
      catch (ClosingMoveException e)
      {
         closingMoveError(getMessage("undo.convert_to_curve"), e);
      }
      catch (Exception e)
      {
         getResources().internalError(frame_, e);
      }
   }

   public void convertToMove()
   {
      if (editedPath == null) return;

      // If the current path is a symmetric path and the
      // selected segment is the line of symmetry, do
      // nothing. (Menu item should be disabled to prevent
      // this.)

      JDRPathSegment segment = editedPath.getSelectedSegment();

      if (segment == null) return;

      UndoableEdit edit;

      try
      {
         if (editedPath instanceof JDRSymmetricPath)
         {
            JDRSymmetricPath path = (JDRSymmetricPath)editedPath;

            if (segment == path.getSymmetry())
            {
               return;
            }
            else if (segment == path.getJoin())
            {
               edit = new JoinToMove(path);
            }
            else if (segment == path.getClosingSegment())
            {
               edit = new ClosingToMove(path);
            }
            else
            {
               edit = new ConvertToSegment(editedPath, segment);
            }
         }
         else
         {
            edit = new ConvertToSegment(editedPath, segment);
         }

         frame_.postEdit(edit);
      }
      catch (ClosingMoveException e)
      {
         closingMoveError(getMessage("undo.convert_to_move"), e);
      }
      catch (Exception e)
      {
         getResources().internalError(frame_, e);
      }
   }

   public void convertToClosingMove()
   {
      if (editedPath == null) return;

      JDRPathSegment segment = editedPath.getSelectedSegment();

      if (segment == null) return;

      if (editedPath.size() < 2)
      {
         getResources().error(frame_, getMessage("error.close_lonely_subpath"));
         return;
      }

      try
      {
         if (editedPath instanceof JDRSymmetricPath)
         {
            JDRSymmetricPath path = (JDRSymmetricPath)editedPath;

            if (segment == path.getSymmetry()
              || segment == path.getJoin()
              || segment == path.getClosingSegment()
               )
            {
               return;
            }

         }

         JDRPath basePath = editedPath.getBaseUnderlyingPath();

         if (!segment.isGap() && segment == basePath.getLastSegment())
         {
            JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

            if (basePath.isClosed())
            {
               ce.addEdit(new OpenPath(editedPath, true));
               JDRPoint p0 = editedPath.getLastSegment().getEnd();
               JDRPoint p1 = segment.getStart();

               JDRClosingMove move = new JDRClosingMove(p0.getX(), p0.getY(),
                p1.getX(), p1.getY(), basePath, basePath.size());

               ce.addEdit(new AppendSegment(basePath, move));
               ce.addEdit(new AppendSegment(basePath, (JDRSegment)segment));

               ce.addEdit(new ClosePath(editedPath, JDRShape.CLOSE_MERGE_ENDS));
            }
            else
            {
               JDRPoint p0 = segment.getEnd();
               JDRPoint p1 = basePath.getFirstSegment().getStart();

               JDRClosingMove move = new JDRClosingMove(p0.getX(), p0.getY(),
                p1.getX(), p1.getY(), basePath, basePath.size());

               ce.addEdit(new AppendSegment(basePath, move));
               ce.addEdit(new ClosePath(editedPath, JDRShape.CLOSE_MERGE_ENDS));
            }

            ce.end();
            frame_.postEdit(ce);
         }
         else
         {
            UndoableEdit edit = new ConvertToClosingMove(editedPath, segment);

            frame_.postEdit(edit);
         }
      }
      catch (ClosingMoveException e)
      {
         closingMoveError(getMessage("undo.close_sub_path"), e);
      }
      catch (Exception e)
      {
         getResources().internalError(frame_, e);
      }
   }

   public void closingMoveError(ClosingMoveException e)
   {
      JDRClosingMove seg = e.getSegment();
      JDRPoint p0 = seg.getStart();
      JDRPoint p1 = seg.getEnd();

      getResources().error(frame_, getResources().getMessage(
       "error.closed_subpath_action",
       e.getSegmentIndex(), p0.getX(), p0.getY(), p1.getX(), p1.getY()),
       e);
   }

   public void closingMoveError(String actionName, ClosingMoveException e)
   {
      JDRClosingMove seg = e.getSegment();
      JDRPoint p0 = seg.getStart();
      JDRPoint p1 = seg.getEnd();

      getResources().error(frame_, getResources().getMessage(
       "error.closed_subpath_named_action", actionName,
       e.getSegmentIndex(), p0.getX(), p0.getY(), p1.getX(), p1.getY()),
       e);
   }

   public void anchorSymmetry()
   {
      if (editedPath == null
       || !(editedPath instanceof JDRSymmetricPath)) return;

      boolean flag = !((JDRSymmetricPath)editedPath).isAnchored();

      try
      {
         UndoableEdit edit = new SetSymmetryJoinAnchor(flag);

         frame_.postEdit(edit);
      }
      catch (InvalidClassException e)
      {
         // This shouldn't happen

         getResources().internalError(this, e);
      }
   }

   public void closeAnchorSymmetry()
   {
      if (editedPath == null
      || !(editedPath instanceof JDRSymmetricPath)) return;

      boolean flag = ((JDRSymmetricPath)editedPath).getClosingSegment() != null;

      try
      {
         UndoableEdit edit = new SetSymmetryCloseAnchor(flag);

         frame_.postEdit(edit);
      }
      catch (Exception e)
      {
         getResources().internalError(frame_, e);
      }
   }

   public void snapToGrid()
   {
      JDRPoint selectedPoint = getSelectedStoragePoint();

      if (selectedPoint == null) return;

      boolean lock = frame_.getGridLock();
      frame_.setGridLock(true);

      Point2D p = getNearestStorageTicFromStorage(selectedPoint.x,
                                                  selectedPoint.y);
      frame_.setGridLock(lock);

      setSelectedStoragePoint(p.getX(),p.getY());
   }

   public void breakPath()
   {
      if (editedPath == null || getSelectedStoragePoint()==null)
      {
         return;
      }

      try
      {
         UndoableEdit edit = new BreakPath(editedPath);
         frame_.postEdit(edit);
      }
      catch (ClosingMoveException e)
      {
         closingMoveError(getMessage("undo.break"), e);
      }
      catch (Throwable e)
      {
         // This shouldn't happen

         getResources().internalError(frame_,e);
      }
   }

   public void toggleContinuityAnchor()
   {
      if (editedPath == null) return;

      JDRPathSegment editedSegment = editedPath.getSelectedSegment();
      JDRPoint selectedPoint = getSelectedStoragePoint();

      if (editedSegment == null || selectedPoint == null) return;

      JDRPoint point = null;

      if (editedSegment instanceof JDRBezier)
      {
         JDRBezier bezier = (JDRBezier)editedSegment;

         if (selectedPoint == bezier.getControl1())
         {
            point = bezier.getStart();
         }
         else if (selectedPoint == bezier.getControl2())
         {
            point = bezier.getEnd();
         }
         else
         {
            point = selectedPoint;
         }
      }
      else if (editedSegment instanceof JDRPartialBezier)
      {
         JDRPartialBezier bezier = (JDRPartialBezier)editedSegment;

         if (selectedPoint == bezier.getControl1())
         {
            point = bezier.getStart();
         }
         else
         {
            point = selectedPoint;
         }
      }

      if (point == null) return;

      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         ce.addEdit(new MakeContinuous(false));

         ce.addEdit(new AnchorControl(point));

         ce.end();
         frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // This shouldn't happen

         getResources().internalError(frame_, e);
      }
   }

   public void makeContinuous(boolean isEqui)
   {
      if (editedPath == null) return;

      try
      {
         UndoableEdit edit = new MakeContinuous(isEqui);
         frame_.postEdit(edit);
      }
      catch (Throwable e)
      {
         getResources().internalError(frame_, e);
      }
   }

   public void openPath(boolean removeSeg)
   {
      if (editedPath == null) return;

      try
      {
         UndoableEdit edit = new OpenPath(editedPath, removeSeg);
         frame_.postEdit(edit);
      }
      catch (ClosingMoveException e)
      {
         closingMoveError(getMessage("undo.open"), e);
      }
      catch (Throwable e)
      {
         getResources().internalError(frame_, e);
      }
   }

   public void closePath(int closeType)
   {
      if (editedPath == null) return;

      try
      {
         UndoableEdit edit = new ClosePath(editedPath, closeType);
         frame_.postEdit(edit);
      }
      catch (ClosingMoveException e)
      {
         closingMoveError(getMessage("undo.close"), e);
      }
      catch (Throwable e)
      {
         getResources().internalError(frame_, e);
      }
   }

   public byte getEditFlag()
   {
      if (editedDistortion != null)
      {
         return EDIT_FLAG_DISTORT;
      }

      if (editedPath != null)
      {
         return EDIT_FLAG_PATH;
      }

      return EDIT_FLAG_NONE;
   }

   public byte getConstructionFlag()
   {
      if (currentPath != null)
      {
         return FlowframTkAction.CONSTRUCTION_FLAG_NON_GEOMETRIC;
      }

      if (textField.isVisible())
      {
         return FlowframTkAction.CONSTRUCTION_FLAG_TEXT;
      }

      if (anchor != null)
      {
         int tool = getCanvasGraphics().getTool();

         if (tool == ACTION_RECTANGLE || tool == ACTION_ELLIPSE)
         {
            return FlowframTkAction.CONSTRUCTION_FLAG_GEOMETRIC;
         }
      }

      return FlowframTkAction.CONSTRUCTION_FLAG_NONE;
   }

   public JDRSelection getSelectionFlags()
   {
      if (getCurrentTool() != ACTION_SELECT || paths == null
       || paths.size() == 0)
      {
         return null;
      }

      return JDRSelection.getSelections(paths);
   }

   public int getSelectedControlFlag()
   {
      if (editedPath == null) return CONTROL_FLAG_NONE;

      return editedPath.getSelectedControlFlag();
   }

   public JDRShape getEditedPath()
   {
      return editedPath;
   }

   public void showSymbolSelector()
   {
      getApplication().getCharacterSelector().display(
       getCanvasGraphics().getTool() == ACTION_MATH ?
       getApplication().getMathModeMappings() :
       getApplication().getTextModeMappings());
   }

   public void showPopup()
   {
      showPopup(this, (int)mouse.getX(), (int)mouse.getY());
   }

   public void showPopup(Component comp, int x, int y)
   {
      LaTeXCodeEditor editor = getFrame().getLaTeXCodeEditor();

      if (editor.isEditing())
      {
         editor.showPopup(comp, x, y);
      }
      else if (textField.isVisible())
      {
         showTextFieldPopup(comp, x, y);
      }
      else if (editedPath != null)
      {
         showEditPathPopup(comp, x, y);
      }
      else if (getCurrentTool() == ACTION_SELECT)
      {
         showSelectPopup(comp, x, y);
      }
   }

   private boolean checkForPopupTrigger(MouseEvent evt)
   {
      if (evt.isPopupTrigger())
      {
         showPopup(evt.getComponent(), evt.getX(), evt.getY());

         return true;
      }

      return false;
   }

   public void showTextFieldPopup(Component comp, int x, int y)
   {
      updateTextConstructionActions();

      String selectedText = textField.getSelectedText();

      copyText.setEnabled(selectedText != null);
      cutText.setEnabled(selectedText != null);

      texteditPathPopupMenu.show(comp, x, y);
   }

   public void showEditPathPopup(Component component, int x, int y)
   {
       editPathPopupMenu.show(component, x, y);
   }

   public void showSelectPopup(Component component, int x, int y)
   {
      JPopupMenu popup = updateSelectActions();

      if (popup != null)
      {
         popup.show(component, x, y);
      }
   }

   public void moveLeft(int modifiers)
   {
      if ((modifiers & KeyEvent.BUTTON1_MASK)
            == KeyEvent.BUTTON1_MASK)
      {
         if (!mouseDown || !getApplication().isRobotEnabled()) return;

         Point pt = getMousePosition(true);
         if (pt == null) return;
         Point location = getLocationOnScreen();

         getApplication().moveMouse((int)Math.round(pt.x-1+location.getX()),
                            (int)Math.round(pt.y+location.getY()));
      }
      else if (!textField.isVisible())
      {
         unitScrollLeft();
      }
   }

   public void moveRight(int modifiers)
   {
      if ((modifiers & KeyEvent.BUTTON1_MASK)
            == KeyEvent.BUTTON1_MASK)
      {
         if (!mouseDown || !getApplication().isRobotEnabled()) return;

         Point pt = getMousePosition(true);
         if (pt == null) return;
         Point location = getLocationOnScreen();

         getApplication().moveMouse((int)Math.round(pt.x+1+location.getX()),
                            (int)Math.round(pt.y+location.getY()));
      }
      else if (!textField.isVisible())
      {
         unitScrollRight();
      }
   }

   public void moveUp(int modifiers)
   {
      if ((modifiers & KeyEvent.BUTTON1_MASK)
            == KeyEvent.BUTTON1_MASK)
      {
         if (!mouseDown || !getApplication().isRobotEnabled()) return;

         Point pt = getMousePosition(true);
         if (pt == null) return;
         Point location = getLocationOnScreen();

         getApplication().moveMouse((int)Math.round(pt.x+location.getX()),
                            (int)Math.round(pt.y-1+location.getY()));
      }
      else if (!textField.isVisible())
      {
         unitScrollUp();
      }
   }

   public void moveDown(int modifiers)
   {
      if ((modifiers & KeyEvent.BUTTON1_MASK)
            == KeyEvent.BUTTON1_MASK)
      {
         if (!mouseDown || !getApplication().isRobotEnabled()) return;

         Point pt = getMousePosition(true);
         if (pt == null) return;
         Point location = getLocationOnScreen();

         getApplication().moveMouse((int)Math.round(pt.x+location.getX()),
                            (int)Math.round(pt.y+1+location.getY()));
      }
      else if (!textField.isVisible())
      {
         unitScrollDown();
      }
   }

   public void goToStorageCoordinate(double x, double y)
   {
      CanvasGraphics cg = getCanvasGraphics();

      goToComponentCoordinate(cg.storageToComponentX(x)+cg.getComponentOriginX(),
                              cg.storageToComponentY(y)+cg.getComponentOriginY());
   }

   public void goToComponentCoordinate(double compX, double compY)
   {
      Point2D location = scrollToComponentLocation(compX, compY);

      Point pt = new Point((int)Math.round(location.getX()),
                           (int)Math.round(location.getY()));

      SwingUtilities.convertPointToScreen(pt, this);

      getApplication().moveMouse(pt.x, pt.y);

      CanvasGraphics cg = getCanvasGraphics();

      location.setLocation(cg.componentXToStorage(location.getX()),
                           cg.componentYToStorage(location.getY()));

      moveToStorage(location);

      frame_.updateRulersFromStorage(location.getX(), location.getY());
   }

   public void deleteLast()
   {
      if (currentPath == null || currentSegment == null) return;

      int n = currentPath.size();

      if (n == 1)
      {
         abandonPath();
         return;
      }

      BBox box = currentPath.getComponentControlBBox();
      currentSegment.mergeComponentControlBBox(box);

      JDRPathSegment seg = currentPath.removeLastSegment();

      currentSegment.setStart(seg.getStartX(), seg.getStartY());

      repaint(box.getRectangle(), true);
   }

   public void findSelectedObjects()
   {
      BBox minBox = null;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            BBox box = object.getStorageBBox();

            if (minBox == null
             || box.getMinX() < minBox.getMinX()
             || box.getMinY() < minBox.getMinY())
            {
               minBox = box;
            }
         }
      }

      if (minBox != null) 
      {
         CanvasGraphics cg = getCanvasGraphics();

         double canvasMinX = -cg.getStorageOriginX();
         double canvasMinY = -cg.getStorageOriginY();

         double paperWidth = cg.getStoragePaperWidth();
         double paperHeight = cg.getStoragePaperHeight();

         double canvasMaxX = paperWidth + canvasMinX;
         double canvasMaxY = paperHeight + canvasMinY;

         double minX = minBox.getMinX();
         double minY = minBox.getMinY();
         double maxX = minBox.getMaxX();
         double maxY = minBox.getMaxY();

         boolean originChange = false;

         if (minX < canvasMinX)
         {
            if (maxX > canvasMinX)
            {
               // object bounding box overlaps left border
               minX = canvasMinX;
            }
            else
            {
               cg.setStorageOriginX(-minX);
               originChange = true;
            }
         }
         else if (minX > canvasMaxX)
         {
            if (minX >= 0 && minX <= paperWidth)
            {
               cg.setOriginX(0);
            }
            else
            {
               double w = minBox.getWidth();

               if (w < paperWidth)
               {
                  cg.setStorageOriginX(paperWidth-maxX);
               }
               else
               {
                  cg.setStorageOriginX(-minX);
               }
            }

            originChange = true;
         }

         if (minY < canvasMinY)
         {
            if (maxY > canvasMinY)
            {
               // object bounding box overlaps top border
               minY = canvasMinY;
            }
            else
            {
               cg.setStorageOriginY(-minY);
               originChange = true;
            }
         }
         else if (minY > canvasMaxY)
         {
            if (minY >= 0 && minY <= paperHeight)
            {
               cg.setOriginY(0);
            }
            else
            {
               double h = minBox.getHeight();

               if (h < paperHeight)
               {
                  cg.setStorageOriginY(paperHeight-maxY);
               }
               else
               {
                  cg.setStorageOriginY(-minY);
               }
            }

            originChange = true;
         }

         if (originChange)
         {
            frame_.forceUpdate();
         }

         goToStorageCoordinate(minX, minY);
      }
   }

   public void doConstructMouseClick()
   {
      Point2D location
         = new Point2D.Double(mouse.getX(), mouse.getY());

      doConstructMouseClick(location);
   }

   public void doConstructMouseClick(Point2D currentPos)
   {
      JDRPaint currentLinePaint = frame_.getCurrentLinePaint();
      JDRPaint currentFillPaint = frame_.getCurrentFillPaint();
      JDRBasicStroke currentStroke = frame_.getCurrentStroke();
      int tool = frame_.currentTool();

      try
      {
         switch (tool)
         {
            case ACTION_TEXT :
            case ACTION_MATH :
               startTextAndPostEdit(currentPos);
               break;
            case ACTION_OPEN_LINE :
            case ACTION_CLOSED_LINE :
               if (anchor != null && currentSegment!=null
                  && currentPath != null)
               {
                  currentPath.add(currentSegment);
               }
               else
               {
                  currentPath = new JDRPath(getCanvasGraphics(),
                                         currentLinePaint,
                                         currentFillPaint,
                                         (JDRBasicStroke)currentStroke.clone());
                  frame_.disableUndoRedo();
               }

               currentSegment = new JDRLine(getCanvasGraphics(),
                                            currentPos,
                                            currentPos);
               anchor = currentPos;
               break;
            case ACTION_OPEN_CURVE :
            case ACTION_CLOSED_CURVE :
               if (anchor != null && currentSegment != null
                   && currentPath!=null)
               {
                  currentPath.add(currentSegment);
               }
               else
               {
                  currentPath = new JDRPath(getCanvasGraphics(),
                                         currentLinePaint,
                                         currentFillPaint,
                                         (JDRBasicStroke)currentStroke.clone());
                  frame_.disableUndoRedo();
               }
               currentSegment = new JDRBezier(getCanvasGraphics(), 
                                              currentPos,
                                              currentPos);
               anchor = currentPos;
               break;
         case ACTION_RECTANGLE :
         case ACTION_ELLIPSE :
            if (anchor == null)
            {
               frame_.disableUndoRedo();
               anchor = currentPos;
            }
            else
            {
               finishPath();
            }
            break;
         }
      }
      catch (Throwable e)
      {
         // This shouldn't happen

         getResources().internalError(frame_,e);
      }

      getApplication().updateToolActionButtons(false);
   }

   public void moveSelectedObjects(JDRLength shift_left, JDRLength shift_up)
   {
      JDRUnit unit = getCanvasGraphics().getStorageUnit();

      moveSelectedObjects(shift_left.getValue(unit),
                          shift_up.getValue(unit));
   }

   protected void moveSelectedObjects(double shift_left, double shift_up)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit = null;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            edit = new MoveObject(object, -shift_left, -shift_up);
            ce.addEdit(edit);
         }
      }

      if (edit != null)
      {
         ce.end();
         frame_.postEdit(ce);
      }
   }

   public Vector<JDRCompleteObject> getVisibleObjects()
   {
      if (displayPage == PAGES_ALL)
      {
         return null;
      }

      int n = paths.size();

      Vector<JDRCompleteObject> visibleObjects;

      if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         visibleObjects = new Vector<JDRCompleteObject>(n);
      }
      else
      {
         visibleObjects = new Vector<JDRCompleteObject>();
      }

      for (int i = 0; i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (isObjectVisible(object))
         {
            visibleObjects.add(object);
         }
      }

      return visibleObjects;
   }

   public void selectObject(JDRCanvasCompoundEdit ce,
     JDRCompleteObject obj, boolean selected)
   {
      if (selected)
      {
         ce.addEdit(new SelectObject(obj, true,
             getResources().getMessage("undo.select")));
      }
      else
      {
         ce.addEdit(new SelectObject(obj, false,
             getResources().getMessage("undo.deselect")));
      }
   }

   public void selectNextObject()
   {
      // select next object in stack

      boolean done=false;
      JDRCompleteObject prevobject = null;
      JDRCompleteObject object = null;

      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      Vector<JDRCompleteObject> visible = getVisibleObjects();

      int n = (visible == null ? paths.size() : visible.size());

      for (int i = 0; i < n; i++)
      {
         JDRCompleteObject thisobject = 
             (visible==null ? paths.get(i) : visible.get(i));

         if (thisobject.isSelected())
         {
             if (object == null) object = prevobject;

             UndoableEdit edit = new SelectObject(thisobject,false,
                 getResources().getMessage("undo.deselect"));
             ce.addEdit(edit);
             done = true;
         }

         prevobject = thisobject;
      }

      if (object == null)
      {
         object = prevobject;
      }

      if (object != null)
      {
         UndoableEdit edit = new SelectObject(object,true,
            getResources().getMessage("undo.select"));

         ce.addEdit(edit);
         done = true;
      }

      if (done)
      {
         ce.end();
         frame_.postEdit(ce);
      }
   }

   public void addNextObject()
   {
      // select next object in stack (without deselecting others)

      JDRCompleteObject prevobject = null;
      JDRCompleteObject object = null;

      Vector<JDRCompleteObject> visible = getVisibleObjects();

      int n = (visible == null ? paths.size() : visible.size());

      for (int i = 0; i < n; i++)
      {
         JDRCompleteObject thisobject = 
           (visible == null ? paths.get(i) : visible.get(i));

         if (thisobject.isSelected())
         {
             if (object == null) object = prevobject;
         }

         prevobject = thisobject;
      }

      if (object == null)
      {
         object = prevobject;
      }

      if (object != null)
      {
         UndoableEdit edit = new SelectObject(object,true,
            getResources().getMessage("undo.select"));

         frame_.postEdit(edit);
      }
   }

   public void skipObject()
   {
      // deselects lowest selected object, and selects object behind

      JDRCompleteObject prevobject = null;
      JDRCompleteObject object = null;
      JDRCompleteObject oldObject=null; // object marked for deselection

      Vector<JDRCompleteObject> visible = getVisibleObjects();

      int n = (visible == null ? paths.size() : visible.size());

      for (int i = 0; i < n; i++)
      {
         JDRCompleteObject thisobject =
           (visible == null ? paths.get(i) : visible.get(i));

         if (thisobject.isSelected())
         {
             if (object == null)
             {
                object = prevobject;
                oldObject = thisobject;
             }
         }

         prevobject = thisobject;
      }

      if (object == null)
      {
         object = prevobject;
      }

      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      boolean done=false;

      if (oldObject != null)
      {
         UndoableEdit edit = new SelectObject(oldObject,false,
            getResources().getMessage("undo.deselect"));

         ce.addEdit(edit);
         done = true;
      }

      if (object != null)
      {
         UndoableEdit edit = new SelectObject(object,true,
            getResources().getMessage("undo.select"));

         ce.addEdit(edit);
         done = true;
      }

      if (done)
      {
         ce.end();
         frame_.postEdit(ce);
      }
   }

   public void selectNextControl()
   {
      if (editedPath == null) return;

      UndoableEdit edit = new SelectNextControl();
      frame_.postEdit(edit);
   }

   public void selectPrevControl()
   {
      if (editedPath == null) return;

      UndoableEdit edit = new SelectPrevControl();
      frame_.postEdit(edit);
   }

   public JDRGrid getGrid()
   {
      return frame_.getGrid();
   }

   public JDRUnit getUnit()
   {
      return frame_.getUnit();
   }

   public FlowframTk getApplication()
   {
      return frame_.getApplication();
   }

   public JDRResources getResources()
   {
      return frame_.getResources();
   }

   public String getMessage(String tag, Object... params)
   {
      return getResources().getMessage(tag, params);
   }

   public JDRGuiMessage getMessageSystem()
   {
      return getApplication().getMessageSystem();
   }

   public void refresh()
   {
      getCanvasGraphics().setBitmapReplaced(false);

      paths.refresh();

      if (getCanvasGraphics().isBitmapReplaced())
      {
         markAsModified();
      }

      getCanvasGraphics().setBitmapReplaced(false);

      setBackgroundImage(true);

      repaint();
   }

   public void dragEnter(DropTargetDragEvent dtde)
   {
      int tool = getCurrentTool();

      if (!(tool == ACTION_SELECT
          ||tool == ACTION_TEXT
          ||tool == ACTION_MATH)
       || isDistorting() || isInEditMode())
      {
         dtde.rejectDrag();
         return;
      }

      Transferable transferable = dtde.getTransferable();

      if (tool == ACTION_SELECT
      && !(transferable.isDataFlavorSupported(
               DataFlavor.javaFileListFlavor)
        ||transferable.isDataFlavorSupported(
               DataFlavor.stringFlavor)))
      {
         dtde.rejectDrag();
         return;
      }

      if (tool != ACTION_SELECT
       && !transferable.isDataFlavorSupported(
               DataFlavor.stringFlavor))
      {
         dtde.rejectDrag();
      }
   }

   public void dragExit(DropTargetEvent dte)
   {
   }

   public void dragOver(DropTargetDragEvent dtde)
   {
   }

   public void dropActionChanged(DropTargetDragEvent dtde)
   {
   }

   public void drop(DropTargetDropEvent dtde)
   {
      int tool = getCurrentTool();

      if (!(tool == ACTION_SELECT
          ||tool == ACTION_TEXT
          ||tool == ACTION_MATH)
       || isDistorting() || isInEditMode())
      {
         dtde.rejectDrop();
         return;
      }

      Transferable transferable = dtde.getTransferable();

      dtde.acceptDrop(DnDConstants.ACTION_COPY);

      int n = 0;
      boolean done=false;

      String undoName = getResources().getMessage("undo.drag_and_drop");

      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this, undoName);

      CanvasGraphics cg = getCanvasGraphics();

      Point2D p = cg.componentToStorage(dtde.getLocation());

      double dx = -p.getX();
      double dy = -p.getY();

      try
      {
         if (tool == ACTION_SELECT
          && transferable.isDataFlavorSupported(
               DataFlavor.javaFileListFlavor))
         {
            ArrayList list = (ArrayList)transferable.getTransferData(
               DataFlavor.javaFileListFlavor);

            for (Object object : list)
            {
               File file = (File)object;

               try
               {
                  if (getApplication().isJdrExtension(file))
                  {
                     (new AddJdr(frame_, file, undoName)).execute();
                     done = true;
                  }
                  else if (getApplication().isAjrExtension(file))
                  {
                     (new AddAjr(frame_, file, undoName)).execute();
                     done = true;
                  }
                  else if (getApplication().isBitmapExtension(file))
                  {
                     JDRBitmap bitmap = insertBitmap(ce, file);
                     ce.addEdit(new MoveObject(bitmap, dx, dy));
   
                     n++;
                     done = true;
                  }
                  else
                  {
                     getMessageSystem().warning(getResources().getMessage(
                        "warning.unknown_drop", file.toString()));
                     getMessageSystem().displayMessages();
                  }
               }
               catch (InvalidImageFormatException e)
               {
                  getMessageSystem().error(e);
               }
               catch (InvalidFormatException e)
               {
                  getMessageSystem().error(e);
               }
               catch (IOException e)
               {
                  getMessageSystem().error(e);
               }
            }
         }
         else if (transferable.isDataFlavorSupported(
               DataFlavor.stringFlavor))
         {
            String object = transferable.getTransferData(
               DataFlavor.stringFlavor).toString();
            done = true;

            if (textField.isVisible() && currentText != null)
            {
               ce.addEdit(finishText());
               n++;
            }

            String[] split = object.split("\\R");

            for (int i = 0; i < split.length; i++)
            {
               if (split[i].matches("^\\s*$"))
               {
                  p.setLocation(p.getX(),
                     p.getY()
                    +cg.componentYToStorage(textField.getHeight()));
                  continue;
               }

               currentText = new JDRText(cg, p);
               JDRText text = addText(ce, split[i]);

               p.setLocation(p.getX(),
                     p.getY()
                    +cg.componentYToStorage(textField.getHeight()));

               if (tool == ACTION_SELECT)
               {
                  ce.addEdit(new SelectObject(text, true));
               }
               n++;
            }
         }

      }
      catch (UnsupportedFlavorException e)
      {
         getResources().error(this, e);
      }
      catch (IOException e)
      {
         getResources().error(this, e);
      }

      if (n > 0)
      {
         ce.end();
         frame_.postEdit(ce);

         dtde.dropComplete(true);
      }

      if (!done)
      {
         dtde.rejectDrop();
      }

      getApplication().setStatusInfo(
        getResources().getMessage("info.select"), "sec:selectobjects");
   }

   public void insertBitmap(File imageFile)
   {
      insertBitmap(imageFile.getAbsolutePath());
   }

   public void insertBitmap(String imagefilename)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this,
            getResources().getMessage("undo.new_bitmap"));

         insertBitmap(ce, imagefilename);

         ce.end();
         frame_.postEdit(ce);
      }
      catch (InvalidImageFormatException e)
      {
         getResources().error(frame_,
            getResources().getMessage("error.invalid_imageformat"));
      }
      catch (InvalidFormatException e)
      {
         getResources().error(frame_, e);
      }
      catch (IOException e)
      {
         getResources().error(frame_, e);
      }
   }

   public JDRBitmap insertBitmap(JDRCanvasCompoundEdit ce, File imageFile)
     throws InvalidImageFormatException,IOException,InvalidFormatException
   {
      return insertBitmap(ce, imageFile.getAbsolutePath());
   }

   public JDRBitmap insertBitmap(JDRCanvasCompoundEdit ce, String imagefilename)
     throws InvalidImageFormatException,IOException,InvalidFormatException
   {
      JDRBitmap bitmap = new JDRBitmap(getCanvasGraphics(), imagefilename);
      bitmap.setLaTeXCommand(getApplication().getDefaultBitmapCommand());

      if (bitmap.isDraft())
      {
         getCanvasGraphics().getMessageSystem().getPublisher().publishMessages(
            MessageInfo.createWarning(
               getResources().getMessage("warning.draft_bitmap")));
      }

      UndoableEdit edit;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         edit = new SelectObject(
            paths.get(i), false,
            getResources().getMessage("undo.deselect_all"));
         ce.addEdit(edit);
      }

      edit = new AddObject(bitmap,
         getResources().getMessage("undo.new_bitmap"));
      ce.addEdit(edit);

      edit = new SelectObject(
         bitmap, true, getResources().getMessage("undo.new_bitmap"));
      ce.addEdit(edit);

      return bitmap;
   }

   public void parshape(boolean useOutline)
   {
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected() && object instanceof JDRPath)
         {
            Graphics2D g2 = (Graphics2D)getGraphics();
            JDRPath p = (JDRPath)object;

            CanvasGraphics cg = p.getCanvasGraphics();
            cg.setGraphicsDevice(g2);

            try
            {
               scanshape = p.parshape(
                  getCanvasGraphics().getStorageBaselineskip(LaTeXFontBase.NORMALSIZE),
                  useOutline);

               frame_.saveString(scanshape.string);
            }
            catch (InvalidShapeException e)
            {
               JOptionPane.showMessageDialog(frame_,
               new String[]
               {getResources().getMessage("error.parshape.convert"),
                e.getMessage()},
               getResources().getMessage("error.shape.incompatible"),
               JOptionPane.ERROR_MESSAGE);
            }
            finally
            {
               cg.setGraphicsDevice(null);
               g2.dispose();
            }

            scanshape = null;
            repaint(p.getComponentBBox().getRectangle(), true);
            return;
         }
      }
   }

   public void shapepar(boolean useOutline)
   {
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected() && object instanceof JDRPath)
         {
            Graphics2D g2 = (Graphics2D)getGraphics();
            JDRPath p = (JDRPath)object;

            CanvasGraphics cg = p.getCanvasGraphics();
            cg.setGraphicsDevice(g2);

            try
            {
               scanshape = p.shapepar(
                  getApplication().useHPaddingShapepar(),
                  getCanvasGraphics().getStorageBaselineskip(LaTeXFontBase.NORMALSIZE),
                  useOutline);

               frame_.saveString(scanshape.string);
            }
            catch (InvalidShapeException e)
            {
               JOptionPane.showMessageDialog(frame_,
               new String[]
               {getResources().getMessage("error.shapepar.convert"),
                e.getMessage()},
               getResources().getMessage("error.shape.incompatible"),
               JOptionPane.ERROR_MESSAGE);
            }
            finally
            {
               cg.setGraphicsDevice(null);
               g2.dispose();
            }
            
            scanshape = null;
            repaint(p.getComponentBBox().getRectangle(), true);
            return;
         }
      }
   }

   public void updatePattern(int index, JDRPattern pattern)
   {
      try
      {
         frame_.postEdit(new UpdatePattern(index, pattern));
      }
      catch (Exception e)
      {
         getResources().internalError(this, e);
      }
   }

   public void convertSelectedToPattern(JDRPattern pattern)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         boolean flag = false;

         for (int i = 0, n = paths.size(); i < n; i++)
         {
            JDRCompleteObject object = paths.get(i);

            if (object.isSelected() && object instanceof JDRShape)
            {
               UndoableEdit edit =
                  new ConvertToPattern((JDRShape)object, pattern);
               ce.addEdit(edit);

               flag = true;
            }
         }

         ce.end();
         if (flag) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // this shouldn't happen
         getResources().internalError(this, e);
      }
   }

   public void removeSelectedPattern()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected() && object instanceof JDRPattern)
         {
            UndoableEdit edit =
               new RemovePattern((JDRPattern)object);
            ce.addEdit(edit);

            flag = true;
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   public void leftAlign()
   {
      alignSelectedGroups(AlignGroup.LEFT);
   }

   public void centreAlign()
   {
      alignSelectedGroups(AlignGroup.CENTRE);
   }

   public void rightAlign()
   {
      alignSelectedGroups(AlignGroup.RIGHT);
   }

   public void topAlign()
   {
      alignSelectedGroups(AlignGroup.TOP);
   }

   public void middleAlign()
   {
      alignSelectedGroups(AlignGroup.MIDDLE);
   }

   public void bottomAlign()
   {
      alignSelectedGroups(AlignGroup.BOTTOM);
   }

   public void alignSelectedGroups(int align)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit = null;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected() && object instanceof JDRGroup)
         {
            edit = new AlignGroup((JDRGroup)object,i, align);
            ce.addEdit(edit);
         }
      }

      ce.end();
      if (edit != null) frame_.postEdit(ce);
   }

   protected void setSelectedLineWidth(JDRLength lineWidth)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         boolean flag = false;

         for (int i = 0, n = paths.size(); i < n; i++)
         {
            JDRCompleteObject object = paths.get(i);

            if (object.isSelected())
            {
               flag = setLineWidth(lineWidth, object, ce);
            }
         }

         ce.end();
         if (flag) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // this shouldn't happen
         getResources().internalError(this, e);
      }
   }

   private boolean setLineWidth(JDRLength width, JDRCompleteObject object, 
                            JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setLineWidth(width, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object instanceof JDRShape
       && !(object instanceof JDRTextual))
      {
         UndoableEdit edit = new SetLineWidth((JDRShape)object, width);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   protected void setSelectedDashPattern(DashPattern pattern)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = setDashPattern(pattern, object, ce);
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   private boolean setDashPattern(DashPattern pattern,
                            JDRCompleteObject object, 
                            JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setDashPattern(pattern, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if ((object instanceof JDRShape)
        && !(object instanceof JDRTextual))
      {
         UndoableEdit edit = new SetDashPattern((JDRShape)object, pattern);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   public void setSelectedCapStyle(int capStyle)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         boolean flag = false;

         for (int i = 0, n = paths.size(); i < n; i++)
         {
            JDRCompleteObject object = paths.get(i);

            if (object.isSelected())
            {
               flag = setCapStyle(capStyle, object, ce);
            }
         }

         ce.end();
         if (flag) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // this shouldn't happen
         getResources().internalError(this, e);
      }
   }

   private boolean setCapStyle(int style, JDRCompleteObject object, 
                            JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setCapStyle(style, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object instanceof JDRShape
       && !(object instanceof JDRTextual))
      {
         UndoableEdit edit = new SetCapStyle((JDRShape)object, style);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   public void setSelectedJoinStyle(int joinStyle)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         boolean flag = false;

         for (int i = 0, n = paths.size(); i < n; i++)
         {
            JDRCompleteObject object = paths.get(i);

            if (object.isSelected())
            {
               flag = setJoinStyle(joinStyle, object, ce);
            }
         }

         ce.end();
         if (flag) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // this shouldn't happen
         getResources().internalError(this, e);
      }
   }

   private boolean setJoinStyle(int style, JDRCompleteObject object, 
                            JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setJoinStyle(style, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object instanceof JDRShape
        && !(object instanceof JDRTextual))
      {
         UndoableEdit edit = new SetJoinStyle((JDRShape)object, style);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   public void setSelectedJoinStyle(int joinStyle, double mitreLimit)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         boolean flag = false;

         for (int i = 0, n = paths.size(); i < n; i++)
         {
            JDRCompleteObject object = paths.get(i);

            if (object.isSelected())
            {
               flag = setJoinStyle(joinStyle, mitreLimit, object, ce);
            }
         }

         ce.end();
         if (flag) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // this shouldn't happen
         getResources().internalError(this, e);
      }
   }

   private boolean setJoinStyle(int style, double mitreLimit,
      JDRCompleteObject object, JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setJoinStyle(style, mitreLimit, grp.get(i), ce)
                 || flag;
         }
         return flag;
      }
      else if (object instanceof JDRShape
        && !(object instanceof JDRTextual))
      {
         UndoableEdit edit = new SetJoinStyle((JDRShape)object, style);
         ce.addEdit(edit);
         edit = new SetMitreLimit((JDRPath)object, mitreLimit);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   public void setSelectedMitreLimit(double limit)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         boolean flag = false;

         for (int i = 0, n = paths.size(); i < n; i++)
         {
            JDRCompleteObject object = paths.get(i);

            if (object.isSelected())
            {
               flag = setMitreLimit(limit, object, ce);
            }
         }

         ce.end();
         if (flag) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // this shouldn't happen
         getResources().internalError(this, e);
      }
   }

   private boolean setMitreLimit(double limit, JDRCompleteObject object, 
                            JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setMitreLimit(limit, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object instanceof JDRShape
       && !(object instanceof JDRTextual))
      {
         UndoableEdit edit = new SetMitreLimit((JDRShape)object, limit);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   protected void setSelectedStartArrow(JDRMarker marker)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = setStartArrow(marker, object, ce) || flag;
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   private boolean setStartArrow(JDRMarker marker,
       JDRCompleteObject object, JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setStartArrow(marker, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object instanceof JDRShape && !object.hasTextual())
      {
         try
         {
            UndoableEdit edit = new SetStartArrow((JDRShape)object, marker);
            ce.addEdit(edit);
            return true;
         }
         catch (Throwable e)
         {
            // This shouldn't happen
            getResources().internalError(frame_, e);
         }
      }

      return false;
   }

   protected void setSelectedMidArrow(JDRMarker marker)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = setMidArrow(marker, object, ce) || flag;
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   private boolean setMidArrow(JDRMarker marker,
       JDRCompleteObject object, JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setMidArrow(marker, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object instanceof JDRShape && !object.hasTextual())
      {
         try
         {
            UndoableEdit edit = new SetMidArrow((JDRShape)object, marker);
            ce.addEdit(edit);
            return true;
         }
         catch (Throwable e)
         {
            // This shouldn't happen

            getResources().internalError(frame_, e);
         }
      }

      return false;
   }

   protected void setSelectedEndArrow(JDRMarker marker)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = setEndArrow(marker, object, ce) || flag;
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   private boolean setEndArrow(JDRMarker marker,
       JDRCompleteObject object, JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setEndArrow(marker, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object instanceof JDRShape && !object.hasTextual())
      {
         try
         {
            UndoableEdit edit = new SetEndArrow((JDRShape)object, marker);
            ce.addEdit(edit);
            return true;
         }
         catch (Throwable e)
         {
            // This shouldn't happen

            getResources().internalError(frame_, e);
         }
      }

      return false;
   }

   protected void setSelectedMarkers(JDRMarker marker)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = setMarkers(marker, object, ce) || flag;
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   private boolean setMarkers(JDRMarker marker,
       JDRCompleteObject object, JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setMarkers(marker, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object instanceof JDRShape && !object.hasTextual())
      {
         try
         {
            UndoableEdit edit = new SetMarkers((JDRShape)object, marker);
            ce.addEdit(edit);
            return true;
         }
         catch (Throwable e)
         {
            // This shouldn't happen

            getResources().internalError(frame_, e);
         }
      }

      return false;
   }

   public void setSelectedWindingRule(int rule)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         boolean flag = false;

         for (int i = 0, n = paths.size(); i < n; i++)
         {
            JDRCompleteObject object = paths.get(i);

            if (object.isSelected())
            {
               flag = setWindingRule(rule, object, ce);
            }
         }

         ce.end();
         if (flag) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // this shouldn't happen
         getResources().internalError(this, e);
      }
   }

   private boolean setWindingRule(int style, JDRCompleteObject object, 
                            JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setWindingRule(style, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object instanceof JDRShape && !object.hasTextual())
      {
         UndoableEdit edit = new SetWindingRule((JDRShape)object, style);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   public void setSelectedHalign(int align)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         boolean flag = false;

         for (int i = 0, n = paths.size(); i < n; i++)
         {
            JDRCompleteObject object = paths.get(i);

            if (object.isSelected())
            {
               flag = setHalign(align, object, ce);
            }
         }

         ce.end();
         if (flag) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // this shouldn't happen
         getResources().internalError(this, e);
      }
   }

   private boolean setHalign(int align, JDRCompleteObject object, 
                            JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setHalign(align, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object.hasTextual())
      {
         UndoableEdit edit
            = new SetHAlign(object.getTextual(), align);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   public void setSelectedValign(int align)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         boolean flag = false;

         for (int i = 0, n = paths.size(); i < n; i++)
         {
            JDRCompleteObject object = paths.get(i);

            if (object.isSelected())
            {
               flag = setValign(align, object, ce);
            }
         }

         ce.end();
         if (flag) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // this shouldn't happen
         getResources().internalError(this, e);
      }
   }

   private boolean setValign(int align, JDRCompleteObject object, 
                            JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setValign(align, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object.hasTextual())
      {
         UndoableEdit edit
            = new SetVAlign(object.getTextual(), align);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   public void setSelectedAnchor(int halign, int valign)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         boolean flag = false;

         for (int i = 0, n = paths.size(); i < n; i++)
         {
            JDRCompleteObject object = paths.get(i);

            if (object.isSelected())
            {
               flag = setAnchor(halign, valign, object, ce);
            }
         }

         ce.end();
         if (flag) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // this shouldn't happen
         getResources().internalError(this, e);
      }
   }

   private boolean setAnchor(int halign, int valign, JDRCompleteObject object, 
                            JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setAnchor(halign, valign, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object.hasTextual())
      {
         UndoableEdit edit
            = new SetAnchor(object.getTextual(), halign, valign);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   public void setSelectedTextOutlineMode(boolean outline)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

         boolean flag = false;

         for (int i = 0, n = paths.size(); i < n; i++)
         {
            JDRCompleteObject object = paths.get(i);

            if (object.isSelected())
            {
               flag = setTextOutlineMode(outline, object, ce);
            }
         }

         ce.end();
         if (flag) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // this shouldn't happen
         getResources().internalError(this, e);
      }
   }

   private boolean setTextOutlineMode(boolean outline, 
      JDRCompleteObject object, JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setTextOutlineMode(outline, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object.hasTextual())
      {
         UndoableEdit edit
            = new SetTextOutlineMode(object.getTextual(), outline);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   protected void setSelectedStroke(JDRBasicStroke s)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = setStroke(s, object, ce);
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   private boolean setStroke(JDRBasicStroke s, JDRCompleteObject object, 
                            JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setStroke(s, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object instanceof JDRShape)
      {
         UndoableEdit edit = new SetLineStyle((JDRShape)object, s);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   private void unsetFlowFrame(JDRCompleteObject object,
      JDRCanvasCompoundEdit ce)
   {
      FlowFrame f = null;

      if (object instanceof JDRGroup)
      {
         JDRGroup group = (JDRGroup)object;

         for (int i = 0, n = group.size(); i < n; i++)
         {
            unsetFlowFrame(group.get(i), ce);
         }
      }

      UndoableEdit edit = new SetFlowFrame(object, f,
         getResources().getMessage("undo.clear_flowframes"));

      ce.addEdit(edit);
   }

   public void unsetAllFlowFrames()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      unsetFlowFrame(paths, ce);

      ce.end();
      frame_.postEdit(ce);
      setBackgroundImage(true);
      repaint();
   }

   public FlowFrame getTypeblock()
   {
      return paths.getFlowFrame();
   }

   public Rectangle2D getTypeblockBounds()
   {
      if (paths.getFlowFrame() == null) return null;

      CanvasGraphics cg = getCanvasGraphics();

      return paths.getFlowFrame().getBounds2D(
         cg.getStoragePaperWidth(),
         cg.getStoragePaperHeight());
   }

   public void setTypeblock(double left, double right,
                            double top, double bottom,
                            double evenHshift)
   {
      UndoableEdit edit = new SetTypeblock(left, right, top, bottom, evenHshift);
      frame_.postEdit(edit);
   }

   public boolean isUniqueLabel(JDRGroup group, int frameType,
      JDRCompleteObject object, String label)
   {
      for (int i = 0, n = group.size(); i < n; i++)
      {
         JDRCompleteObject o = group.get(i);

         FlowFrame flowframe = o.getFlowFrame();

         if (flowframe != null && o != object)
         {
            if (flowframe.getType() == frameType 
              && flowframe.getLabel().equals(label))
            {
               return false;
            }
         }

         if (o instanceof JDRGroup)
         {
            if (!isUniqueLabel((JDRGroup)o,frameType,object,label))
            {
               return false;
            }
         }
      }

      return true;
   }

   public boolean isUniqueLabel(int frameType, JDRCompleteObject object,
                                   String label)
   {
      return isUniqueLabel(paths, frameType, object, label);
   }

   public boolean setFlowFrame(JDRCompleteObject object, FlowFrame f)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      if (f.getShape() == FlowFrame.SHAPEPAR)
      {
         if (!preambleHasPackage("shapepar"))
         {
            addPackageToPreamble("shapepar");
         }
      }

      if (object instanceof JDRGroup)
      {
         JDRGroup group = (JDRGroup)object;

         if (f != null && group.containsFlowFrameData())
         {
            int response = getResources().confirm(frame_,
               getResources().getMessage("flowframe.confirm.group"),
               getResources().getMessage("flowframe.confirm.group.title"));

            if (response == JOptionPane.YES_OPTION)
            {
               for (int i = 0, n = group.size(); i < n; i++)
               {
                  JDRCompleteObject o = group.get(i);
                  UndoableEdit edit = new SetFlowFrame(o, null);
                  ce.addEdit(edit);
               }
            }
            else
            {
               return false;
            }
         }
      }

      UndoableEdit edit = new SetFlowFrame(object, f);
      ce.addEdit(edit);

      ce.end();
      frame_.postEdit(ce);

      return true;
   }

   /**
    * Sets the early preamble text.
    */
   public void setPreamble(String preamble)
   {
      getCanvasGraphics().setPreamble(preamble);
   }

   /**
    * Gets the early preamble text.
    */
   public String getPreamble()
   {
      return frame_.getLaTeXCodeEditor().getEarlyPreambleText();
   }

   /**
    * Sets the mid preamble text.
    */
   public void setMidPreamble(String preamble)
   {
      getCanvasGraphics().setMidPreamble(preamble);
   }

   /**
    * Gets the mid preamble text.
    */
   public String getMidPreamble()
   {
      return frame_.getLaTeXCodeEditor().getMidPreambleText();
   }

   /**
    * Sets the end preamble text.
    */
   public void setEndPreamble(String preamble)
   {
      getCanvasGraphics().setEndPreamble(preamble);
   }

   /**
    * Gets the end preamble text.
    */
   public String getEndPreamble()
   {
      return frame_.getLaTeXCodeEditor().getEndPreambleText();
   }

   /**
    * Sets the document text.
    */
   public void setDocumentBody(String body)
   {
      getCanvasGraphics().setDocBody(body);
   }

   /**
    * Gets the document text.
    */
   public String getDocumentBody()
   {
      return frame_.getLaTeXCodeEditor().getDocumentText();
   }

   /**
    * Sets the magic comments.
    */
   public void setMagicComments(String comments)
   {
      getCanvasGraphics().setMagicComments(comments);
   }

   /**
    * Gets the magic comments.
    */
   public String getMagicComments()
   {
      return frame_.getLaTeXCodeEditor().getMagicComments();
   }

   public void addPackagesToPreamble(Vector<String> styNames)
     throws BadLocationException
   {
      String preamble = getPreamble();

      StringBuffer append = new StringBuffer();

      if (preamble == null || preamble.isEmpty())
      {
         for (String styName : styNames)
         {
            Matcher styMatcher = TeXMappings.STY_PATTERN.matcher(styName);

            if (styMatcher.matches())
            {
               String opt = styMatcher.group(1);
               String name = styMatcher.group(2);

               if (opt == null)
               {
                  append.append(String.format("\\usepackage{%s}%n", name));
               }
               else
               {
                  append.append(String.format("\\usepackage[%s]{%s}%n", 
                     opt, name));
               }
            }
         }

         if (append.length() > 0)
         {
            frame_.getLaTeXCodeEditor().appendToLaTeXCode(append.toString());
         }

         return;
      }

      Matcher m = STY_PATTERN.matcher(preamble);

      while (m.find())
      {
         int startIdx = m.start();
         int endIdx = m.end();

         String cs  = m.group(1);
         String opt = m.group(2);
         String name = m.group(3);

         String[] splitOpt = null;

         if (opt != null)
         {
            opt = opt.trim();

            if (opt.isEmpty())
            {
               opt = null;
            }
            else
            {
               splitOpt = opt.split(" *, *");
            }
         }

         for (int i = 0; i < styNames.size(); i++)
         {
            Matcher mi = TeXMappings.STY_PATTERN.matcher(styNames.get(i));

            if (!mi.matches())
            {
               // shouldn't happen
               getResources().debugMessage("Can't match '"+styNames.get(i)
                 +"'. Matcher: "+mi);
               continue;
            }

            String opti = mi.group(1);
            String namei = mi.group(2);

            if (namei.equals(name))
            {

               if (opt != null)
               {
                  if (opti == null)
                  {
                     frame_.getLaTeXCodeEditor().earlyReplace(startIdx, endIdx,
                        String.format("%s[%s]{%s}", cs, opt, name));
                  }
                  else if (!opt.equals(opti))
                  {
                     String[] splitOpti = opti.split(" *, *");

                     String extraOpts = "";

                     for (int j = 0; j < splitOpt.length; j++)
                     {
                        boolean found = false;

                        for (int k = 0; k < splitOpti.length; k++)
                        {
                           if (splitOpti[k].equals(splitOpt[j]))
                           {
                              found = true;
                              break;
                           }
                        }

                        if (!found)
                        {
                           extraOpts = extraOpts + ","+splitOpt[j];
                        }
                     }

                     if (!extraOpts.isEmpty())
                     {
                        frame_.getLaTeXCodeEditor().earlyReplace(
                           startIdx, endIdx,
                           String.format("%s[%s%s]{%s}", cs, opti, extraOpts, 
                              name));
                     }
                  }
               }

               styNames.remove(i);
               break;
            }
         }
      }

      for (int i = 0; i < styNames.size(); i++)
      {
         Matcher mi = TeXMappings.STY_PATTERN.matcher(styNames.get(i));
         
         if (!mi.matches())
         {
            // shouldn't happen
            getResources().debugMessage("Can't match '"+styNames.get(i)
              +"'. Matcher: "+mi);

            continue;
         }

         String opti = mi.group(1);
         String namei = mi.group(2);

         if (opti == null)
         {
            append.append(
             String.format("\\usepackage{%s}%n", namei));
         }
         else
         {
            append.append(
             String.format("\\usepackage[%s]{%s}%n", opti, namei));
         }
      }

      if (append.length() > 0)
      {
         frame_.getLaTeXCodeEditor().appendToLaTeXCode(append.toString());
      }
   }

   public boolean preambleHasPackage(String styName)
   {
      String preamble = getPreamble();

      if (preamble == null) return false;

      return (preamble.contains("\\usepackage{"+styName+"}")
      ||  preamble.contains("\\RequirePackage{"+styName+"}"));
   }

   public String addPackageToPreamble(String styName)
   {
      String preamble = getPreamble();
      String eol = System.getProperty("line.separator", "\n");

      if (preamble == null)
      {
         preamble = "";
      }

      if (!preamble.isEmpty() && !preamble.endsWith(eol))
      {
         preamble += eol;
      }

      preamble += "\\usepackage{"+styName+"}"+eol;

      return preamble;
   }

   public void setDescription(JDRCompleteObject object, String description)
   {
      frame_.postEdit(new SetDescription(object, description));
   }

   public void setSelectedTag(String tag)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit = null;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            edit = new SetTag(object, tag);
            ce.addEdit(edit);
         }
      }

      ce.end();
      if (edit != null) frame_.postEdit(ce);
   }

   public void setUseAbsolutePages(boolean useAbsolutePages)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (cg.useAbsolutePages() == useAbsolutePages)
      {
         getApplication().setUseAbsolutePages(useAbsolutePages);
         return;
      }

      frame_.postEdit(new SetAbsolutePages(useAbsolutePages));
   }

   public JDRCompleteObject getSelectedObject()
   {
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            return object;
         }
      }

      return null;
   }

   public String getSelectedTag()
   {
      Vector<String> list = new Vector<String>();
 
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            String tag = object.getTag().trim();

            for (String s : tag.split(" +"))
            {
               if (!list.contains(s))
               {
                  list.add(s);
               }
            }
         }
      }

      if (list.isEmpty())
      {
         return null;
      }
      else if (list.size() == 1)
      {
         return list.firstElement();
      }

      list.remove("");

      return String.join(" ", list);
   }

   public JDRCompleteObject getObject(int index)
   {
      JDRCompleteObject object = null;

      try
      {
         object = paths.get(index);
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         getResources().internalError(frame_, "Can't located object "+index, e);
      }

      return object;
   }

   public int getSelectedPatternIndex()
   {
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.hasPattern() && object.isSelected())
         {
            return i;
         }
      }

      return -1;
   }


   public void setBitmapProperties(JDRBitmap bitmap,
      String newfilename, String newlatexfilename,
      String command, double[] matrix)
   {
      UndoableEdit edit = new SetBitmapProperties(
                             bitmap, newfilename,
                             newlatexfilename,
                             command, matrix);
      frame_.postEdit(edit);

      if (bitmap.isDraft())
      {
         getResources().warning(frame_,
                          getResources().getMessage("warning.draft_bitmap"));
      }
   }

   public void setSymmetry()
   {
      if (editedPath == null) return;

      boolean flag = !editedPath.hasSymmetricPath();

      setSymmetry(flag);
   }

   private void setSymmetry(boolean addSymmetry)
   {
      try
      {
         for (int i = 0; i < paths.size(); i++)
         {
            if (paths.get(i) == editedPath)
            {
               UndoableEdit edit = new SetSymmetricPath(editedPath, addSymmetry);

               frame_.postEdit(edit);

               return;
            }
         }
      }
      catch (Throwable e)
      {
         // This shouldn't happen

         getResources().internalError(frame_, e);
         getResources().debugMessage("setSymmetry("+addSymmetry+") failed.");
         e.printStackTrace();
      }
   }

   public JDRBasicStroke getSelectedBasicStroke()
   {
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected() && object instanceof JDRShape)
         {
            JDRShape shape = (JDRShape)object;
            JDRStroke stroke = shape.getStroke();

            if (stroke instanceof JDRBasicStroke)
            {
               return (JDRBasicStroke) stroke;
            }
         }
      }

      return frame_.getCurrentStroke();
   }

   public boolean setLinePaint(JDRPaint paint, JDRCompleteObject object, 
                               JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setLinePaint(paint, grp.get(i), ce) || flag;
         }
         return flag;
      }
      else if (object instanceof JDRShape && !object.hasTextual())
      {
         UndoableEdit edit = new SetLinePaint((JDRShape)object, paint);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   public boolean setTextPaint(JDRPaint paint, JDRCompleteObject object, 
                               JDRCanvasCompoundEdit ce)
   {
      boolean flag=false;

      if (object instanceof JDRGroup)
      {
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setTextPaint(paint, grp.get(i), ce) || flag;
         }
      }
      else if (object.hasTextual())
      {
         UndoableEdit edit
            = new SetTextPaint(object.getTextual(), paint);
         ce.addEdit(edit);
         return true;
      }

      return flag;
   }

   public boolean setFillPaint(JDRPaint paint, JDRCompleteObject object, 
                               JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = setFillPaint(paint, grp.get(i), ce) || flag;
         }

         return flag;
      }
      else if (object.hasTextual())
      {
         if (object.getTextual().isOutline())
         {
            UndoableEdit edit = new SetFillPaint(object, paint);
            ce.addEdit(edit);
            return true;
         }
      }
      else if (object instanceof JDRShape)
      {
         UndoableEdit edit = new SetFillPaint(object, paint);
         ce.addEdit(edit);
         return true;
      }

      return false;
   }

   public void setSelectedLinePaint(JDRPaint paint)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = setLinePaint(paint, object, ce);
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   public void setSelectedTextPaint(JDRPaint paint)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = setTextPaint(paint, object, ce);
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   public JDRPaint getSelectedLinePaint()
   {
      JDRShape shape = getSelectedNonTextShape();

      if (shape != null)
      {
         return shape.getLinePaint();
      }

      return frame_.getCurrentLinePaint();
   }

   public JDRPaint getSelectedTextPaint()
   {
      JDRTextual txt = getSelectedTextual();

      if (txt != null)
      {
         return txt.getTextPaint();
      }

      return frame_.getCurrentTextPaint();
   }

   public void setSelectedFillPaint(JDRPaint paint)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = setFillPaint(paint, object, ce);
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   public void reduceToGrey()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = reduceToGrey(object, ce);
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   public boolean reduceToGrey(JDRCompleteObject object, 
                               JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = reduceToGrey(grp.get(i), ce) || flag;
         }

         return flag;
      }
      else if (object instanceof JDRShape && !object.hasTextual())
      {
         JDRShape path = (JDRShape)object;

         JDRPaint linePaint = path.getLinePaint();

         if (linePaint instanceof JDRShading)
         {
            linePaint = (JDRPaint)linePaint.clone();
            ((JDRShading)linePaint).reduceToGreyScale();
         }
         else if (!(linePaint instanceof JDRTransparent))
         {
            linePaint = linePaint.getJDRGray();
         }

         JDRPaint fillPaint = path.getFillPaint();

         if (fillPaint instanceof JDRShading)
         {
            fillPaint = (JDRPaint)fillPaint.clone();
            ((JDRShading)fillPaint).reduceToGreyScale();
         }
         else if (!(fillPaint instanceof JDRTransparent))
         {
            fillPaint = fillPaint.getJDRGray();
         }

         UndoableEdit edit;

         edit = new SetFillPaint(path, fillPaint);
         ce.addEdit(edit);
         edit = new SetLinePaint(path, linePaint);
         ce.addEdit(edit);

         JDRBasicStroke stroke = (JDRBasicStroke)path.getStroke();

         JDRMarker marker = stroke.getStartArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            if (marker.fillPaint instanceof JDRShading)
            {
               // shaded marker fill paint isn't implemented
               // yet, but add the code in case it is implemented
               // in future
               ((JDRShading)marker.fillPaint).reduceToGreyScale();
            }
            else
            {
               marker.fillPaint = marker.fillPaint.getJDRGray();
            }

            try
            {
               edit = new SetStartArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         marker = stroke.getMidArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            if (marker.fillPaint instanceof JDRShading)
            {
               ((JDRShading)marker.fillPaint).reduceToGreyScale();
            }
            else
            {
               marker.fillPaint = marker.fillPaint.getJDRGray();
            }

            try
            {
               edit = new SetMidArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         marker = stroke.getEndArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            if (marker.fillPaint instanceof JDRShading)
            {
               ((JDRShading)marker.fillPaint).reduceToGreyScale();
            }
            else
            {
               marker.fillPaint = marker.fillPaint.getJDRGray();
            }

            try
            {
               edit = new SetEndArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         return true;
      }
      else if (object.hasTextual())
      {
         JDRTextual text = object.getTextual();

         JDRPaint textPaint = text.getTextPaint();

         if (textPaint instanceof JDRShading)
         {
            textPaint = (JDRPaint)textPaint.clone();
            ((JDRShading)textPaint).reduceToGreyScale();
         }
         else if (!(textPaint instanceof JDRTransparent))
         {
            textPaint = textPaint.getJDRGray();
         }

         UndoableEdit edit;

         edit = new SetTextPaint(text, textPaint);
         ce.addEdit(edit);

         if (text.isOutline())
         {
            JDRPaint fillPaint = text.getFillPaint().removeTransparency();
            edit = new SetFillPaint(object, fillPaint);
            ce.addEdit(edit);
         }

         return true;
      }

      return false;
   }

   public void removeAlpha()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = removeAlpha(object, ce);
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   public boolean removeAlpha(JDRCompleteObject object, 
                               JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = removeAlpha(grp.get(i), ce) || flag;
         }

         return flag;
      }
      else if (object instanceof JDRShape && !object.hasTextual())
      {
         JDRShape path = (JDRShape)object;

         JDRPaint linePaint = path.getLinePaint().removeTransparency();

         JDRPaint fillPaint = path.getFillPaint().removeTransparency();

         UndoableEdit edit;

         edit = new SetFillPaint(path, fillPaint);
         ce.addEdit(edit);
         edit = new SetLinePaint(path, linePaint);
         ce.addEdit(edit);

         JDRBasicStroke stroke = (JDRBasicStroke)path.getStroke();

         JDRMarker marker = stroke.getStartArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            marker.fillPaint = marker.fillPaint.removeTransparency();

            try
            {
               edit = new SetStartArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         marker = stroke.getMidArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            marker.fillPaint = marker.fillPaint.removeTransparency();

            try
            {
               edit = new SetMidArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         marker = stroke.getEndArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            marker.fillPaint = marker.fillPaint.removeTransparency();

            try
            {
               edit = new SetEndArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         return true;
      }
      else if (object.hasTextual())
      {
         JDRTextual text = object.getTextual();

         JDRPaint textPaint = text.getTextPaint().removeTransparency();

         UndoableEdit edit;

         edit = new SetTextPaint(text, textPaint);
         ce.addEdit(edit);

         if (text.isOutline())
         {
            JDRPaint fillPaint = text.getFillPaint().removeTransparency();
            edit = new SetFillPaint(object, fillPaint);
            ce.addEdit(edit);
         }

         return true;
      }

      return false;
   }

   public void convertToCMYK()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = convertToCMYK(object, ce);
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   public boolean convertToCMYK(JDRCompleteObject object, 
                               JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = convertToCMYK(grp.get(i), ce) || flag;
         }

         return flag;
      }
      else if (object instanceof JDRShape && !object.hasTextual())
      {
         JDRShape path = (JDRShape)object;

         JDRPaint linePaint = path.getLinePaint();

         if (linePaint instanceof JDRShading)
         {
            linePaint = (JDRPaint)linePaint.clone();
            ((JDRShading)linePaint).convertToCMYK();
         }
         else if (!(linePaint instanceof JDRTransparent))
         {
            linePaint = linePaint.getJDRColorCMYK();
         }

         JDRPaint fillPaint = path.getFillPaint();

         if (fillPaint instanceof JDRShading)
         {
            fillPaint = (JDRPaint)fillPaint.clone();
            ((JDRShading)fillPaint).convertToCMYK();
         }
         else if (!(fillPaint instanceof JDRTransparent))
         {
            fillPaint = fillPaint.getJDRColorCMYK();
         }

         UndoableEdit edit;

         edit = new SetFillPaint(path, fillPaint);
         ce.addEdit(edit);
         edit = new SetLinePaint(path, linePaint);
         ce.addEdit(edit);

         JDRBasicStroke stroke = (JDRBasicStroke)path.getStroke();

         JDRMarker marker = stroke.getStartArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            if (marker.fillPaint instanceof JDRShading)
            {
               // shaded marker fill paint isn't implemented
               // yet, but add the code in case it is implemented
               // in future
               ((JDRShading)marker.fillPaint).convertToCMYK();
            }
            else
            {
               marker.fillPaint = marker.fillPaint.getJDRColorCMYK();
            }

            try
            {
               edit = new SetStartArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         marker = stroke.getMidArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            if (marker.fillPaint instanceof JDRShading)
            {
               ((JDRShading)marker.fillPaint).convertToCMYK();
            }
            else
            {
               marker.fillPaint = marker.fillPaint.getJDRColorCMYK();
            }

            try
            {
               edit = new SetMidArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         marker = stroke.getEndArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            if (marker.fillPaint instanceof JDRShading)
            {
               ((JDRShading)marker.fillPaint).convertToCMYK();
            }
            else
            {
               marker.fillPaint = marker.fillPaint.getJDRColorCMYK();
            }

            try
            {
               edit = new SetEndArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         return true;
      }
      else if (object.hasTextual())
      {
         JDRTextual text = object.getTextual();

         JDRPaint textPaint = text.getTextPaint();

         if (textPaint instanceof JDRShading)
         {
            textPaint = (JDRPaint)textPaint.clone();
            ((JDRShading)textPaint).convertToCMYK();
         }
         else if (!(textPaint instanceof JDRTransparent))
         {
            textPaint = textPaint.getJDRColorCMYK();
         }

         UndoableEdit edit;

         edit = new SetTextPaint(text, textPaint);
         ce.addEdit(edit);

         if (text.isOutline())
         {
            JDRPaint fillPaint = text.getFillPaint().removeTransparency();
            edit = new SetFillPaint(object, fillPaint);
            ce.addEdit(edit);
         }

         return true;
      }

      return false;
   }

   public void convertToHSB()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = convertToHSB(object, ce);
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   public boolean convertToHSB(JDRCompleteObject object, 
                               JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = convertToHSB(grp.get(i), ce) || flag;
         }

         return flag;
      }
      else if (object instanceof JDRShape && !object.hasTextual())
      {
         JDRShape path = (JDRShape)object;

         JDRPaint linePaint = path.getLinePaint();

         if (linePaint instanceof JDRShading)
         {
            linePaint = (JDRPaint)linePaint.clone();
            ((JDRShading)linePaint).convertToHSB();
         }
         else if (!(linePaint instanceof JDRTransparent))
         {
            linePaint = linePaint.getJDRColorHSB();
         }

         JDRPaint fillPaint = path.getFillPaint();

         if (fillPaint instanceof JDRShading)
         {
            fillPaint = (JDRPaint)fillPaint.clone();
            ((JDRShading)fillPaint).convertToHSB();
         }
         else if (!(fillPaint instanceof JDRTransparent))
         {
            fillPaint = fillPaint.getJDRColorHSB();
         }

         UndoableEdit edit;

         edit = new SetFillPaint(path, fillPaint);
         ce.addEdit(edit);
         edit = new SetLinePaint(path, linePaint);
         ce.addEdit(edit);

         JDRBasicStroke stroke = (JDRBasicStroke)path.getStroke();

         JDRMarker marker = stroke.getStartArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            if (marker.fillPaint instanceof JDRShading)
            {
               // shaded marker fill paint isn't implemented
               // yet, but add the code in case it is implemented
               // in future
               ((JDRShading)marker.fillPaint).convertToHSB();
            }
            else
            {
               marker.fillPaint = marker.fillPaint.getJDRColorHSB();
            }

            try
            {
               edit = new SetStartArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         marker = stroke.getMidArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            if (marker.fillPaint instanceof JDRShading)
            {
               ((JDRShading)marker.fillPaint).convertToHSB();
            }
            else
            {
               marker.fillPaint = marker.fillPaint.getJDRColorHSB();
            }

            try
            {
               edit = new SetMidArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         marker = stroke.getEndArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            if (marker.fillPaint instanceof JDRShading)
            {
               ((JDRShading)marker.fillPaint).convertToHSB();
            }
            else
            {
               marker.fillPaint = marker.fillPaint.getJDRColorHSB();
            }

            try
            {
               edit = new SetEndArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         return true;
      }
      else if (object.hasTextual())
      {
         JDRTextual text = object.getTextual();

         JDRPaint textPaint = text.getTextPaint();

         if (textPaint instanceof JDRShading)
         {
            textPaint = (JDRPaint)textPaint.clone();
            ((JDRShading)textPaint).convertToHSB();
         }
         else if (!(textPaint instanceof JDRTransparent))
         {
            textPaint = textPaint.getJDRColorHSB();
         }

         UndoableEdit edit;

         edit = new SetTextPaint(text, textPaint);
         ce.addEdit(edit);

         if (text.isOutline())
         {
            JDRPaint fillPaint = text.getFillPaint().removeTransparency();
            edit = new SetFillPaint(object, fillPaint);
            ce.addEdit(edit);
         }

         return true;
      }

      return false;
   }

   public void convertToRGB()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      boolean flag = false;
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            flag = convertToRGB(object, ce);
         }
      }

      ce.end();
      if (flag) frame_.postEdit(ce);
   }

   public boolean convertToRGB(JDRCompleteObject object, 
                               JDRCanvasCompoundEdit ce)
   {
      if (object instanceof JDRGroup)
      {
         boolean flag=false;
         JDRGroup grp = (JDRGroup)object;

         for (int i = 0, n = grp.size(); i < n; i++)
         {
            flag = convertToRGB(grp.get(i), ce) || flag;
         }

         return flag;
      }
      else if (object instanceof JDRShape && !object.hasTextual())
      {
         JDRShape path = (JDRShape)object;

         JDRPaint linePaint = path.getLinePaint();

         if (linePaint instanceof JDRShading)
         {
            linePaint = (JDRPaint)linePaint.clone();
            ((JDRShading)linePaint).convertToRGB();
         }
         else if (!(linePaint instanceof JDRTransparent))
         {
            linePaint = linePaint.getJDRColor();
         }

         JDRPaint fillPaint = path.getFillPaint();

         if (fillPaint instanceof JDRShading)
         {
            fillPaint = (JDRPaint)fillPaint.clone();
            ((JDRShading)fillPaint).convertToRGB();
         }
         else if (!(fillPaint instanceof JDRTransparent))
         {
            fillPaint = fillPaint.getJDRColor();
         }

         UndoableEdit edit;

         edit = new SetFillPaint(path, fillPaint);
         ce.addEdit(edit);
         edit = new SetLinePaint(path, linePaint);
         ce.addEdit(edit);

         JDRBasicStroke stroke = (JDRBasicStroke)path.getStroke();

         JDRMarker marker = stroke.getStartArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            if (marker.fillPaint instanceof JDRShading)
            {
               // shaded marker fill paint isn't implemented
               // yet, but add the code in case it is implemented
               // in future
               ((JDRShading)marker.fillPaint).convertToRGB();
            }
            else
            {
               marker.fillPaint = marker.fillPaint.getJDRColor();
            }

            try
            {
               edit = new SetStartArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         marker = stroke.getMidArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            if (marker.fillPaint instanceof JDRShading)
            {
               ((JDRShading)marker.fillPaint).convertToRGB();
            }
            else
            {
               marker.fillPaint = marker.fillPaint.getJDRColor();
            }

            try
            {
               edit = new SetMidArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         marker = stroke.getEndArrow();

         if (marker.fillPaint != null)
         {
            marker = (JDRMarker)marker.clone();

            if (marker.fillPaint instanceof JDRShading)
            {
               ((JDRShading)marker.fillPaint).convertToRGB();
            }
            else
            {
               marker.fillPaint = marker.fillPaint.getJDRColor();
            }

            try
            {
               edit = new SetEndArrow(path, marker);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(this, e);
            }
         }

         return true;
      }
      else if (object.hasTextual())
      {
         JDRTextual text = object.getTextual();

         JDRPaint textPaint = text.getTextPaint();

         if (textPaint instanceof JDRShading)
         {
            textPaint = (JDRPaint)textPaint.clone();
            ((JDRShading)textPaint).convertToRGB();
         }
         else if (!(textPaint instanceof JDRTransparent))
         {
            textPaint = textPaint.getJDRColor();
         }

         UndoableEdit edit;

         edit = new SetTextPaint(text, textPaint);
         ce.addEdit(edit);

         if (text.isOutline())
         {
            JDRPaint fillPaint = text.getFillPaint().removeTransparency();
            edit = new SetFillPaint(object, fillPaint);
            ce.addEdit(edit);
         }

         return true;
      }

      return false;
   }

   public void fade(double value)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit = null;

      for (int i = paths.size()-1; i >= 0; i--)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            edit = new FadeObject(object, value, i);
            ce.addEdit(edit);
         }
      }

      ce.end();
      frame_.postEdit(ce);
   }

   public JDRPaint getSelectedFillPaint()
   {
      JDRShape shape = getSelectedNonTextShape();

      if (shape != null)
      {
         return shape.getFillPaint();
      }

      return frame_.getCurrentFillPaint();
   }

   public JDRTextual getSelectedFont()
   {
      JDRTextual text = getSelectedTextual();

      if (text == null)
      {
         text = new JDRText(getCanvasGraphics());
      }

      return text;
   }

   public String getSelectedFontName()
   {
      return getSelectedFont().getFontFamily();
   }

   public int getSelectedFontShape()
   {
      return getSelectedFont().getFontShape();
   }

   public int getSelectedFontSeries()
   {
      return getSelectedFont().getFontSeries();
   }

   public JDRLength getSelectedFontSize()
   {
      return getSelectedFont().getFontSize();
   }

   public int getSelectedHalign()
   {
      return getSelectedTextual().getHAlign();
   }

   public int getSelectedValign()
   {
      return getSelectedTextual().getVAlign();
   }

   public JDRTextual getSelectedTextual()
   {
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               JDRTextual txt = ((JDRGroup)object).getTextual();

               if (txt != null)
               {
                  return txt;
               }
            }
            else if (object.hasTextual())
            {
               return object.getTextual();
            }
         }
      }

      return null; 
   }

   public JDRText getSelectedText()
   {
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               JDRText txt = ((JDRGroup)object).getText();

               if (txt != null)
               {
                  return txt;
               }
            }
            else if (object instanceof JDRText)
            {
               return (JDRText)object;
            }
         }
      }

      return null; 
   }

   public JDRShape getSelectedShape()
   {
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRShape)
            {
               return (JDRShape)object;
            }

            if (object instanceof JDRGroup)
            {
               JDRShape path = ((JDRGroup)object).getShape();

               if (path != null)
               {
                  return path;
               }
            }
         }
      }

      return null; 
   }

   public JDRPath getSelectedPath()
   {
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRPath)
            {
               return (JDRPath)object;
            }

            if (object instanceof JDRGroup)
            {
               JDRPath path = ((JDRGroup)object).getPath();

               if (path != null)
               {
                  return path;
               }
            }
         }
      }

      return null; 
   }

   public JDRShape getSelectedNonTextShape()
   {
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRShape
            && !(object.hasTextual()))
            {
               return (JDRShape)object;
            }

            if (object instanceof JDRGroup)
            {
               JDRShape shape = ((JDRGroup)object).getNonTextShape();

               if (shape != null)
               {
                  return shape;
               }
            }
         }
      }

      return null; 
   }

   public JDRBitmap getSelectedBitmap()
   {
      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRBitmap)
            {
               return (JDRBitmap)object;
            }

            if (object instanceof JDRGroup)
            {
               JDRBitmap bitmap = ((JDRGroup)object).getBitmap();

               if (bitmap != null)
               {
                  return bitmap;
               }
            }
         }
      }

      return null; 
   }

   public boolean isTextFieldVisible()
   {
      return textField.isVisible();
   }

   public String getSymbolText()
   {
      return textField.getText();
   }

   public int getSymbolCaretPosition()
   {
      return textField.getCaretPosition();
   }

   public void setSymbolCaretPosition(int position)
   {
      textField.setCaretPosition(position);
   }

   public void requestSymbolFocus()
   {
      textField.requestFocusInWindow();
   }

   public void setSelectedText(String text, int leftDelim, int rightDelim)
   {
      setSelectedText(text, "", leftDelim, rightDelim);
   }

   public void setSelectedText(String text, String ltxText, 
      int leftDelim, int rightDelim)
   {
      setSelectedText(text, ltxText, leftDelim, rightDelim, null);
   }

   public void setSelectedText(String text, String ltxText, 
      int leftDelim, int rightDelim, Vector<String> styNames)
   {
      JDRTextual object = getSelectedTextual();

      if (object != null)
      {
         if (text.isEmpty())
         {
             getResources().error(frame_,
                 getResources().getMessage("error.empty_string"));
         }
         else
         {
            if (styNames == null || styNames.isEmpty())
            {
               frame_.postEdit(new SetText(object, text, ltxText, 
               leftDelim, rightDelim));
            }
            else
            {
               JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
               ce.addEdit(new SetText(object, text, ltxText, 
                  leftDelim, rightDelim));

               ce.end();

               frame_.postEdit(ce);

               try
               {
                  addPackagesToPreamble(styNames);
               }
               catch (BadLocationException e)
               {
                  getResources().internalError(frame_, e);
               }
            }
         }
      }
   }

   public void setDocClass(String clsName)
   {
      String currentCls = getCanvasGraphics().getDocClass();

      if (currentCls == null && clsName == null)
      {
         return;
      }

      if (currentCls != null && clsName != null
       && currentCls.equals(clsName))
      {
         return;
      }

      frame_.postEdit(new SetDocClass(clsName));
   }

   public void setNormalSize(double normalsize)
   {
      frame_.postEdit(new SetNormalSize(normalsize));
   }

   private boolean setSelectedFont(JDRGroup group, JDRTextual text,
      JDRCanvasCompoundEdit ce)
   {
      boolean done = false;

      for (int i = 0, n = group.size(); i < n; i++)
      {
         JDRCompleteObject object = group.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               done = setSelectedFont(((JDRGroup)object), text, ce);
            }
            else if (object.hasTextual())
            {
               UndoableEdit edit = new SetFont(object.getTextual(),
                                       text.getFontFamily(),
                                       text.getFontSeries(),
                                       text.getFontShape(),
                                       text.getFontSize(),
                                       text.getLaTeXFamily(), 
                                       text.getLaTeXSize(), 
                                       text.getLaTeXSeries(),
                                       text.getLaTeXShape(),
                                       text.getHAlign(),
                                       text.getVAlign());

               ce.addEdit(edit);
               done = true;
            }
         }
      }

      return done;
   }

   public void setSelectedFont(JDRTextual text)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
         boolean done=false;

         done = setSelectedFont(paths, text, ce);

         ce.end();
         if (done) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // This shouldn't happen

         getResources().internalError(frame_, e);
      }
   }

   public boolean setSelectedFontFamily(JDRGroup group,
      String family, String latexFam, JDRCanvasCompoundEdit ce)
   {
      boolean done = false;

      for (int i = 0, n = group.size(); i < n; i++)
      {
         JDRCompleteObject object = group.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               done = setSelectedFontFamily(((JDRGroup)object), 
                  family, latexFam, ce);
            }
            else if (object.hasTextual())
            {
               UndoableEdit edit
                  = new SetFontFamily(object.getTextual(),
                                      family, latexFam);

               ce.addEdit(edit);
               done = true;
            }
         }
      }

      return done;
   }

   public void setSelectedFontFamily(String family, String latexFam)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      boolean done=false;

      done = setSelectedFontFamily(paths, family, latexFam, ce);

      ce.end();
      if (done) frame_.postEdit(ce);
   }

   public boolean setSelectedFontSize(JDRGroup group, JDRLength size,
      String latexSize, JDRCanvasCompoundEdit ce)
   {
      boolean done = false;

      for (int i = 0, n = group.size(); i < n; i++)
      {
         JDRCompleteObject object = group.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               done = setSelectedFontSize(((JDRGroup)object), size,
                         latexSize, ce);
            }
            else if (object.hasTextual())
            {
               UndoableEdit edit
                  = new SetFontSize(object.getTextual(),
                                    size, latexSize);

               ce.addEdit(edit);
               done = true;
            }
         }
      }

      return done;
   }

   public void setSelectedFontSize(JDRLength size, String latexSize)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
         boolean done=false;

         done = setSelectedFontSize(paths, size, latexSize, ce);

         ce.end();
         if (done) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // This shouldn't happen
         getResources().internalError(frame_, e);
      }
   }

   public boolean setSelectedFontShape(JDRGroup group, int shape,
      String latexShape, JDRCanvasCompoundEdit ce)
   {
      boolean done = false;

      for (int i = 0, n = group.size(); i < n; i++)
      {
         JDRCompleteObject object = group.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               done = setSelectedFontShape(((JDRGroup)object), shape,
                         latexShape, ce);
            }
            else if (object.hasTextual())
            {
               UndoableEdit edit
                  = new SetFontShape(object.getTextual(),
                                     shape, latexShape);

               ce.addEdit(edit);
               done = true;
            }
         }
      }

      return done;
   }

   public void setSelectedFontShape(int shape, String latexShape)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
         boolean done=false;

         done = setSelectedFontShape(paths, shape, latexShape, ce);

         ce.end();
         if (done) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // This shouldn't happen
         getResources().internalError(frame_, e);
      }
   }

   public boolean setSelectedFontSeries(JDRGroup group, int series,
      String latexSeries, JDRCanvasCompoundEdit ce)
   {
      boolean done = false;

      for (int i = 0, n = group.size(); i < n; i++)
      {
         JDRCompleteObject object = group.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               done = setSelectedFontSeries(((JDRGroup)object), series,
                         latexSeries, ce);
            }
            else if (object.hasTextual())
            {
               UndoableEdit edit
                  = new SetFontSeries(object.getTextual(),
                                       series, latexSeries);

               ce.addEdit(edit);
               done = true;
            }
         }
      }

      return done;
   }

   public void setSelectedFontSeries(int series, String latexSeries)
   {
      try
      {
         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
         boolean done=false;

         done = setSelectedFontSeries(paths, series, latexSeries, ce);

         ce.end();
         if (done) frame_.postEdit(ce);
      }
      catch (Throwable e)
      {
         // This shouldn't happen
         getResources().internalError(frame_, e);
      }
   }

   public boolean setSelectedTextTransform(
      JDRGroup group, double[] matrix, JDRCanvasCompoundEdit ce)
   {
      boolean done = false;

      for (int i = 0, n = group.size(); i < n; i++)
      {
         JDRCompleteObject object = group.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               done = setSelectedTextTransform(((JDRGroup)object), 
                  matrix, ce);
            }
            else if (object.hasTextual())
            {
               UndoableEdit edit
                  = new SetTextTransform(object.getTextual(), matrix);

               ce.addEdit(edit);
               done = true;
            }
         }
      }

      return done;
   }

   public void setSelectedTextTransform(double[] matrix)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      boolean done=false;

      done = setSelectedTextTransform(paths, matrix, ce);

      ce.end();
      if (done) frame_.postEdit(ce);
   }

   public void reverseSelectedPaths()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit = null;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected() && object instanceof JDRShape)
         {
            try
            {
               edit = new ReversePath((JDRShape)object,i);
               ce.addEdit(edit);
            }
            catch (Exception excp)
            {
               getResources().internalError(frame_, excp);
            }
         }
      }

      ce.end();
      if (edit != null) frame_.postEdit(ce);
   }

   public void convertToPolygon(JDRShape shape, JDRShape polygon)
   {
      try
      {
         UndoableEdit edit = new ConvertToPolygon(shape, polygon);

         frame_.postEdit(edit);
      }
      catch (Exception e)
      {
         getResources().internalError(this, e);
      }
   }

   public boolean updateLaTeXFontSize(JDRGroup group, 
      LaTeXFontBase latexFonts, JDRCanvasCompoundEdit ce)
   {
      boolean done = false;

      for (int i = 0, n = group.size(); i < n; i++)
      {
         JDRCompleteObject object = group.get(i);

         if (object instanceof JDRGroup)
         {
            done = updateLaTeXFontSize(((JDRGroup)object), 
                      latexFonts, ce);
         }
         else if (object instanceof JDRText)
         {
            JDRText t = (JDRText)object;

            UndoableEdit edit = new SetLaTeXFontSize(
               t, latexFonts.getLaTeXCmd(t.getFontSize()));

            ce.addEdit(edit);
            done = true;
         }
      }

      return done;
   }

   public void updateLaTeXFontSize(LaTeXFontBase latexFonts)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      boolean done=false;

      done = updateLaTeXFontSize(paths, latexFonts, ce);

      ce.end();

      if (done) frame_.postEdit(ce);
   }


   public void selectObjectAndScroll(JDRCompleteObject object)
   {
      UndoableEdit edit = new SelectObject(
         object, true, getResources().getMessage("undo.select"));
      frame_.postEdit(edit);

      scrollToObject(object);
   }

   public void selectObjectsAndScroll(JDRCompleteObject[] objects)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit = null;
      JDRCompleteObject object = null;

      for (JDRCompleteObject obj : objects)
      {
         if (object == null)
         {
            object = obj;
         }

         edit = new SelectObject(
            obj, true, getResources().getMessage("undo.select"));
      }

      ce.end();
      if (edit != null) frame_.postEdit(ce);

      if (object != null)
      {
         scrollToObject(object);
      }
   }

   public void scrollToObject(JDRCompleteObject object)
   {
      BBox box = object.getStorageBBox();

      scrollToStorageLocation(box.getMinX(), box.getMinY());
   }

   public void selectAll()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit = null;

      Vector<JDRCompleteObject> visible = getVisibleObjects();

      int n = (visible == null ? paths.size() : visible.size());

      for (int i = 0; i < n; i++)
      {
         edit = new SelectObject(
            (visible == null ? paths.get(i) : visible.get(i)),true,
            getResources().getMessage("undo.select_all"));
         ce.addEdit(edit);
      }

      ce.end();
      if (edit != null) frame_.postEdit(ce);
   }

   public void deleteSelection()
   {
      int n = paths.size();
      if (n == 0) return;

      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      for (int i = paths.size()-1; i >= 0; i--)
      {
         JDRCompleteObject object = paths.get(i);
         if (object.isSelected())
         {
            UndoableEdit edit = new RemoveObject(object, i);
            ce.addEdit(edit);
         }
      }

      ce.end();
      frame_.postEdit(ce);
   }

   /* JDRImage method used by transfer handler */
   public JDRGroup getSelection()
   {
      int n = paths.size();

      JDRGroup g;

      if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         g = new JDRGroup(getCanvasGraphics(), n);
      }
      else
      {
         g = new JDRGroup(getCanvasGraphics());
      }

      for (int i = 0; i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            g.add((JDRCompleteObject)object.clone());
         }
      }

      g.setDescription(getFrame().getFilename());

      return g;
   }

   /* JDRImage method used by transfer handler */
   @Override
   public void copySelection(JDRGroup grp)
   {
      copySelection(grp, true);
   }

   public void copySelection(JDRGroup grp, boolean shift)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      deselectAll();

      copySelection(ce, grp, shift);

      ce.end();
      if (ce.canUndo()) frame_.postEdit(ce);
   }

   public void copySelection(JDRCanvasCompoundEdit ce, JDRGroup grp,
    boolean shift)
   {
      String dupTag = null;

      if (grp.getDescription().equals(getFrame().getFilename()))
      {
         dupTag = " "+getResources().getMessage("label.duplicate");
      }

      JDRGrid grid = frame_.getGrid();

      CanvasGraphics cg = getCanvasGraphics();

      if (cg != grp.getCanvasGraphics())
      {
         grp.applyCanvasGraphics(cg);
      }

      Point2D offset = null;

      if (shift)
      {
         offset = grid.getMinorTicDistance();

         offset.setLocation(cg.bpToStorage(offset.getX()),
                            cg.bpToStorage(offset.getY()));

         BBox box = grp.getStorageBBox();

         if (box.getMinX() < offset.getX())
         {
            offset.setLocation(-offset.getX(), offset.getY());
         }

         if (box.getMinY() < offset.getY())
         {
            offset.setLocation(offset.getX(), -offset.getY());
         }
      }

      for (int i = 0, n = grp.size(); i < n; i++)
      {
         JDRCompleteObject object = (JDRCompleteObject)grp.get(i).clone();
         object.applyCanvasGraphics(cg);

         object.setSelected(true);

         if (shift)
         {
            object.translate(-offset.getX(),-offset.getY());
         }

         if (dupTag != null)
         {
            String desc = object.getDescription();

            if (desc != null && !desc.isEmpty())
            {
               object.setDescription(desc+dupTag);
            }

            FlowFrame flowframe = object.getFlowFrame();

            if (flowframe != null)
            {
               String label = flowframe.getLabel();
               flowframe.setLabel(label+dupTag);
            }
         }

         UndoableEdit edit = new AddObject(object, 
            getResources().getMessage("undo.paste"));
         ce.addEdit(edit);
      }
   }

   public void setImage(JDRGroup image)
   {
      paths = image;

      String preamble = image.getCanvasGraphics().getPreamble();
      String midPreamble = image.getCanvasGraphics().getMidPreamble();
      String endPreamble = image.getCanvasGraphics().getEndPreamble();
      String docEnv = image.getCanvasGraphics().getDocBody();
      String magicComments = image.getCanvasGraphics().getMagicComments();

      frame_.getLaTeXCodeEditor().setLaTeXCode(
        preamble, midPreamble, endPreamble, docEnv, magicComments);
   }

   public void updateBounds()
   {
      Graphics2D g = (Graphics2D)getGraphics();

      if (g != null)
      {
         g.setRenderingHints(frame_.getRenderingHints());
         CanvasGraphics cg = paths.getCanvasGraphics();
         cg.setGraphicsDevice(g);

         try
         {
            paths.updateBounds();
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g.dispose();
         }
      }
   }

   public int getCurrentTool()
   {
      return frame_.currentTool();
   }

   private JScrollBar getHorizontalScrollBar()
   {
      return frame_.scrollPane.getHorizontalScrollBar();
   }

   private JScrollBar getVerticalScrollBar()
   {
      return frame_.scrollPane.getVerticalScrollBar();
   }

   private void updateRulersAndCoords()
   {
      Point pt = getMousePosition(true);

      if (pt != null)
      {
         setStatusCoordsFromComponent(pt.x, pt.y);
         frame_.updateRulersFromComponent(pt.x, pt.y);
      }
   }

   private void unitScrollLeft()
   {
      JScrollBar scrollBar = getHorizontalScrollBar();

      if (scrollBar == null || !scrollBar.isVisible()) return;

      int increment = scrollBar.getUnitIncrement(-1);

      scrollBar.setValue(scrollBar.getValue()-increment);

      updateRulersAndCoords();
   }

   private void unitScrollRight()
   {
      JScrollBar scrollBar = getHorizontalScrollBar();

      if (scrollBar == null || !scrollBar.isVisible()) return;

      int increment = scrollBar.getUnitIncrement(1);

      scrollBar.setValue(scrollBar.getValue()+increment);

      updateRulersAndCoords();
   }

   private void unitScrollDown()
   {
      JScrollBar scrollBar = getVerticalScrollBar();

      if (scrollBar == null || !scrollBar.isVisible()) return;

      int increment = scrollBar.getUnitIncrement(1);

      scrollBar.setValue(scrollBar.getValue()+increment);

      updateRulersAndCoords();
   }

   private void unitScrollUp()
   {
      JScrollBar scrollBar = getVerticalScrollBar();

      if (scrollBar == null || !scrollBar.isVisible()) return;

      int increment = scrollBar.getUnitIncrement(-1);

      scrollBar.setValue(scrollBar.getValue()-increment);

      updateRulersAndCoords();
   }

   public void blockScrollDown()
   {
      JScrollBar scrollBar = getVerticalScrollBar();

      if (scrollBar == null || !scrollBar.isVisible()) return;

      int increment = scrollBar.getBlockIncrement(1);

      scrollBar.setValue(scrollBar.getValue()+increment);

      updateRulersAndCoords();
   }

   public void blockScrollUp()
   {
      JScrollBar scrollBar = getVerticalScrollBar();

      if (scrollBar == null || !scrollBar.isVisible()) return;

      int increment = scrollBar.getBlockIncrement(-1);

      scrollBar.setValue(scrollBar.getValue()-increment);

      updateRulersAndCoords();
   }

   public void blockScrollLeft()
   {
      JScrollBar scrollBar = getHorizontalScrollBar();

      if (scrollBar == null || !scrollBar.isVisible()) return;

      int increment = scrollBar.getBlockIncrement(-1);

      scrollBar.setValue(scrollBar.getValue()-increment);

      updateRulersAndCoords();
   }

   public void blockScrollRight()
   {
      JScrollBar scrollBar = getHorizontalScrollBar();

      if (scrollBar == null || !scrollBar.isVisible()) return;

      int increment = scrollBar.getBlockIncrement(1);

      scrollBar.setValue(scrollBar.getValue()+increment);

      updateRulersAndCoords();
   }

   public void scrollHomeUp()
   {
      JScrollBar scrollBar = getVerticalScrollBar();

      if (scrollBar == null || !scrollBar.isVisible()) return;

      scrollBar.setValue(0);

      updateRulersAndCoords();
   }

   public void scrollHomeLeft()
   {
      JScrollBar scrollBar = getHorizontalScrollBar();

      if (scrollBar == null || !scrollBar.isVisible()) return;

      scrollBar.setValue(0);

      updateRulersAndCoords();
   }

   public void scrollEndDown()
   {
      JScrollBar scrollBar = getVerticalScrollBar();

      if (scrollBar == null || !scrollBar.isVisible()) return;

      scrollBar.setValue(scrollBar.getMaximum());

      updateRulersAndCoords();
   }

   public void scrollEndRight()
   {
      JScrollBar scrollBar = getHorizontalScrollBar();

      if (scrollBar == null || !scrollBar.isVisible()) return;

      scrollBar.setValue(scrollBar.getMaximum());

      updateRulersAndCoords();
   }

   public Point2D scrollToStorageLocation(double x, double y)
   {
      CanvasGraphics cg = getCanvasGraphics();

      return scrollToComponentLocation(cg.storageToComponentX(x)+cg.getComponentOriginX(),
                                       cg.storageToComponentY(y)+cg.getComponentOriginY(),
                                       frame_.getComponentPaperWidth(),
                                       frame_.getComponentPaperHeight(),
                                       true);
   }

   public Point2D scrollToComponentLocation(double x, double y)
   {
      return scrollToComponentLocation(x, y,
        frame_.getComponentPaperWidth(), frame_.getComponentPaperHeight(),
        true);
   }

   // Returns component co-ords
   public Point2D scrollToComponentLocation(double x, double y,
     double paperW, double paperH, boolean checkBounds)
   {
      JScrollBar hScrollBar = getHorizontalScrollBar();
      JScrollBar vScrollBar = getVerticalScrollBar();

      if (checkBounds)
      {
         if (x < 0)
         {
            x = 0;
         }
         else if (x > paperW)
         {
            x = paperW;
         }

         if (y < 0)
         {
            y = 0;
         }
         else if (y > paperH)
         {
            y = paperH;
         }
      }

      Point2D location = new Point2D.Double(x, y);

      double ratioX = x/paperW;
      double ratioY = y/paperH;

      int maxWidth = hScrollBar.getMaximum();
      int maxHeight = vScrollBar.getMaximum();

      int hValue = (int)Math.round(maxWidth*ratioX);
      int vValue = (int)Math.round(maxHeight*ratioY);

      int hIncrement = hScrollBar.getBlockIncrement(1);
      int vIncrement = vScrollBar.getBlockIncrement(1);

      hValue -= hIncrement/2;
      vValue -= vIncrement/2;

      if (hValue < 0) hValue=0;
      if (vValue < 0) vValue=0;

      hScrollBar.setValue(hValue);
      vScrollBar.setValue(vValue);

      return location;
   }

   public void stateChanged(ChangeEvent e)
   {
      if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_MEMORY)
      {
         setBackgroundImage();
         repaint();
      }
   }

   public boolean getScrollableTracksViewportWidth() {return false;}
   public boolean getScrollableTracksViewportHeight(){return false;}

   public Dimension getPreferredScrollableViewportSize()
   {
      return getPreferredSize();
   }

   public Dimension getUnitIncrement()
   {
      JDRGrid grid = frame_.getGrid();
      Point2D minor = grid.getMinorTicDistance();
      Point2D major = grid.getMinorTicDistance();

      double incX = (minor.getX() > 0 ? minor.getX() : major.getX());
      double incY = (minor.getY() > 0 ? minor.getY() : major.getY());

      return new Dimension(
       (int)Math.ceil(getCanvasGraphics().bpToComponentX(incX)),
       (int)Math.ceil(getCanvasGraphics().bpToComponentY(incY)));
   }

   public int getScrollableUnitIncrement(Rectangle visibleRect,
                                         int orientation,
                                         int direction)
   {
      int currentPos = 0;

      Dimension unitIncrement = getUnitIncrement();

      int inc;

      if (orientation == SwingConstants.HORIZONTAL)
      {
         currentPos = visibleRect.x;
         inc = unitIncrement.width;
      }
      else
      {
         currentPos = visibleRect.y;
         inc = unitIncrement.height;
      }

      if (direction < 0)
      {
         int newPosition = currentPos - (currentPos/inc) * inc;

         return newPosition==0 ? inc : newPosition;
      }
      else
      {
         return ((currentPos/inc)+1)*inc - currentPos;
      }
   }

   public int getScrollableBlockIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction)
   {
      Dimension unitIncrement = getUnitIncrement();

      if (orientation == SwingConstants.HORIZONTAL)
         return (unitIncrement.width >= visibleRect.width) ?
           visibleRect.width :
           visibleRect.width - unitIncrement.width;
      else
         return (unitIncrement.height >= visibleRect.height) ?
           visibleRect.height :
           visibleRect.height - unitIncrement.height;
   }

   public void enableTools()
   {
      updateEditPathActions();
      updateGeneralActions(true);
      updateSelectActions();
      updateTextConstructionActions();
   }

   public JDRPoint getSelectedStoragePoint()
   {
      if (editedPath == null) return null;

      return editedPath.getSelectedControl();
   }

   public void setSelectedPoint(JDRLength x, JDRLength y)
   {
      JDRUnit unit = getCanvasGraphics().getStorageUnit();
      setSelectedStoragePoint(x.getValue(unit), y.getValue(unit));
   }

   protected void setSelectedStoragePoint(double x, double y)
   {
      JDRPoint selectedPoint = getSelectedStoragePoint();

      JDRPathSegment editedSegment = editedPath.getSelectedSegment();

      setStoragePoint(editedSegment, selectedPoint, x, y);
   }

   protected void setStoragePoint(JDRPathSegment editedSegment,
    JDRPoint selectedPoint, double x, double y)
   {
      if (selectedPoint != null && editedPath != null)
      {
         double oldx = selectedPoint.x;
         double oldy = selectedPoint.y;

         double incX = x - selectedPoint.x;
         double incY = y - selectedPoint.y;

         if (!(incX == 0.0 && incY == 0.0))
         {

            UndoableEdit edit = new MovePoint(editedPath,
                                              editedSegment,
                                              selectedPoint,
                                              x-selectedPoint.x,
                                              y-selectedPoint.y);
            frame_.postEdit(edit);
         }
      }
   }

   public void setControlPoint(JDRPathSegment segment, 
     JDRPoint point, JDRLength x, JDRLength y)
   {
      JDRUnit unit = getCanvasGraphics().getStorageUnit();
      setStoragePoint(segment, point, x.getValue(unit), y.getValue(unit));
   }

   public void setSymbolText(String str)
   {
      textField.setText(str);
      textField.requestFocusInWindow();
   }

   public Font getSymbolFont()
   {
      return symbolButtonFont;
   }

   public Font getSymbolButtonFont()
   {
      return symbolButtonFont;
   }

   public void updateTextFieldBounds()
   {
      textField.setFont(textFieldFont);
      textField.requestFocusInWindow();
      textField.repaint();
   }

   public void resetTextField()
   {
      Point2D p;
      JDRPaint currentTextPaint = frame_.getCurrentTextPaint();

      if (currentText == null)
      {
         p = textField.getPosition();
      }
      else
      {
         p = currentText.getStart().getPoint2D();
         currentText.setTextPaint(currentTextPaint);
      }

      setTextFieldFont(frame_.getCurrentFont());

      resetTextField(currentTextPaint, p);
   }

   public void setTextFieldFont(JDRFont f)
   {
      textFieldFont = f;
      symbolButtonFont = new Font(textFieldFont.getFamily(),
                                  textFieldFont.getJavaFontStyle(), 18);
   }

   public void resetTextField(JDRPaint foreground, Point2D location)
   {
      textField.setPosition(location.getX(), location.getY());
      textField.setTextPaint(foreground);
      updateTextFieldBounds();
      repaint();
      textField.requestFocusInWindow();
   }

   public void startTextAndPostEdit(Point2D currentPos)
   {
      UndoableEdit edit = startText(currentPos);
      if (edit != null) frame_.postEdit(edit);
   }

   public UndoableEdit startText(Point2D currentPos)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      if (currentText != null)
      {
         ce.addEdit(finishText());
      }

      UndoableEdit edit = new ShowTextField(currentPos);
      ce.addEdit(edit);
      ce.end();
      return ce;
   }

   public void abandonText()
   {
      textField.setVisible(false);
      updateTextConstructionActions();
      currentText = null;
   }

   public void finishTextAndPostEdit()
   {
      UndoableEdit edit = finishText();
      if (edit != null) frame_.postEdit(edit);
   }

   public UndoableEdit finishText()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      UndoableEdit edit = new HideTextField();

      if (currentText == null)
      {
         return edit;
      }

      if (textField.getText().isEmpty()) return edit;

      ce.addEdit(edit);

      addText(ce, textField.getText());

      ce.end();
      return ce;
   }

   private JDRText addText(JDRCanvasCompoundEdit ce, String text)
   {
      Graphics2D g2 = (Graphics2D)getGraphics();
      g2.setRenderingHints(frame_.getRenderingHints());

      CanvasGraphics cg = currentText.getCanvasGraphics();
      cg.setGraphicsDevice(g2);

      try
      {
         currentText.setFont(frame_.getCurrentFontFamily(),
            frame_.getCurrentFontSeries(),
            frame_.getCurrentFontShape(),
            frame_.getCurrentFontSize());

         currentText.setText(text);

         currentText.setLaTeXFont(frame_.getCurrentLaTeXFontFamily(),
                             frame_.getCurrentLaTeXFontSize(),
                             frame_.getCurrentLaTeXFontSeries(),
                             frame_.getCurrentLaTeXFontShape());

         currentText.setHAlign(frame_.getCurrentPGFHAlign());
         currentText.setVAlign(frame_.getCurrentPGFVAlign());
      }
      catch (Throwable e)
      {
         // This shouldn't happen
         getResources().internalError(frame_, e);
      }
      finally
      {
         cg.setGraphicsDevice(null);
         g2.dispose();
      }

      Vector<String> styNames = new Vector<String>();

      if (getCurrentTool() == ACTION_MATH)
      {
         currentText.setLaTeXText(
         "$"
         + getApplication().applyMathModeMappings(currentText.getText(), styNames)
         +"$");
      }
      else
      {
         currentText.setLaTeXText(
          getApplication().applyTextModeMappings(currentText.getText(), styNames));
      }

      try
      {
         UndoableEdit edit = new AddObject(currentText, 
            getResources().getMessage("undo.new_text"));
         ce.addEdit(edit);

         addPackagesToPreamble(styNames);
      }
      catch (Throwable e)
      {
         // This shouldn't happen
         getResources().internalError(this,e);
      }

      JDRText newText = currentText;

      currentText = null;
      frame_.setNewImageState(false);

      return newText;
   }

   public void finish()
   {
      if (currentPath != null)
      {
         finishPath();
      }
      else if (textField.isVisible() && currentText != null)
      {
         CanvasGraphics cg = getCanvasGraphics();

         JDRPoint dp = currentText.getStart();

         JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
         ce.addEdit(finishText());

         Point2D p = 
           new Point2D.Double(
                dp.getX(), 
                dp.getY() + cg.componentYToStorage(textField.getHeight()));


         ce.addEdit(startText(p));
         ce.end();
         frame_.postEdit(ce);
      }
   }

   public void abandonPath()
   {
      if (currentPath == null) return;

      currentPath    = null;
      currentSegment = null;
      anchor         = null;
      repaint();
      getApplication().updateActionButtons(false);
      frame_.refreshUndoRedo();
   }

   public void finishPath()
   {
      if (currentPath == null) return;

      int tool = frame_.currentTool();

      JDRPaint currentLinePaint = frame_.getCurrentLinePaint();
      JDRPaint currentFillPaint = frame_.getCurrentFillPaint();
      JDRBasicStroke currentStroke = 
         (JDRBasicStroke)frame_.getCurrentStroke().clone();

      CanvasGraphics cg = getCanvasGraphics();
      currentLinePaint.setCanvasGraphics(cg);
      currentFillPaint.setCanvasGraphics(cg);
      currentStroke.setCanvasGraphics(cg);

      frame_.refreshUndoRedo();

      if (anchor != null &&
          (tool == ACTION_RECTANGLE ||
           tool == ACTION_ELLIPSE))
      {
         currentPath = new JDRPath(cg,
                                currentLinePaint,
                                currentFillPaint,
                                currentStroke);
      }

      BBox box = null;

      if (currentPath != null)
      {
         box = currentPath.getStorageBBox();
      }

      double epsilon = cg.bpToStorage(1.002);

      if (tool != ACTION_RECTANGLE &&
          tool != ACTION_ELLIPSE)
      {
         if (box == null)
         {
            abandonPath();
            return;
         }

         if (box.getWidth() <= epsilon && box.getHeight() <= epsilon)
         {
            abandonPath();
            return;
         }
      }

      try
      {
         switch (tool)
         {
            case ACTION_SELECT :
               break;
            case ACTION_CLOSED_LINE :
               currentPath.close(JDRShape.CLOSE_LINE);
            case ACTION_OPEN_LINE :
               UndoableEdit edit = new AddObject(currentPath,
                  getResources().getMessage("undo.new_line"));
               frame_.postEdit(edit);
               break;
            case ACTION_CLOSED_CURVE :
               currentPath.close(JDRShape.CLOSE_CONT);
            case ACTION_OPEN_CURVE :
               edit = new AddObject(currentPath, 
                  getResources().getMessage("undo.new_curve"));
               frame_.postEdit(edit);
               break;
            case ACTION_RECTANGLE :
               if (Math.abs(anchor.getX()-mouse.getX()) <= epsilon
                && Math.abs(anchor.getY()-mouse.getY()) <= epsilon)
               {
                  abandonPath();
                  return;
               }

               currentPath = JDRPath.constructRectangle(
                  getCanvasGraphics(), anchor, mouse);

               ((JDRPath)currentPath).setStyle(currentLinePaint,
                                    currentFillPaint,
                                    currentStroke);
               edit = new AddObject(currentPath, 
                  getResources().getMessage("undo.new_rectangle"));
               frame_.postEdit(edit);
               break;
            case ACTION_ELLIPSE :
               double w = Math.abs(mouse.getX()-anchor.getX());
               double h = Math.abs(mouse.getY()-anchor.getY());

               if (w <= epsilon && h <= epsilon)
               {
                  abandonPath();
                  return;
               }

               currentPath = JDRPath.constructEllipse(
                 getCanvasGraphics(), anchor, w, h);
               ((JDRPath)currentPath).setStyle(currentLinePaint,
                                    currentFillPaint,
                                    currentStroke);
               edit = new AddObject(currentPath,
                  getResources().getMessage("undo.new_ellipse"));
               frame_.postEdit(edit);
               break;
         }
      }
      catch (Exception e)
      {
         getResources().internalError(this,e);
      }

      if (currentPath != null)
      {
         box = currentPath.getComponentControlBBox();
         repaint(box.getRectangle(), true);
      }

      if (currentSegment != null)
      {
         box = currentSegment.getComponentControlBBox();
         repaint(box.getRectangle(), true);
      }

      currentSegment = null;
      currentPath = null;
      anchor = null;
      getApplication().updateActionButtons(false);
   }

   public void addObject(JDRCompleteObject object, String undoText)
   {
      frame_.postEdit(new AddObject(object, undoText));
   }

   public void addObject(JDRCanvasCompoundEdit ce, JDRCompleteObject object,
     String undoText)
   {
      ce.addEdit(new AddObject(object, undoText));
   }

   public void drawPrinterMargins(Graphics2D g, double bpToCompXScale,
     double bpToCompYScale)
   {
      if (!frame_.showMargins()) return;

      Rectangle2D printableArea 
         = getCanvasGraphics().getImageableArea();

      if (printableArea == null)
      {
         return;
      }

      float compPaperWidth = (float)(bpToCompXScale*frame_.getBpPaperWidth());
      float compPaperHeight = (float)(bpToCompYScale*frame_.getBpPaperHeight());

      float marginMinX = (float)(bpToCompXScale*printableArea.getX()); 
      float marginMinY = (float)(bpToCompYScale*printableArea.getY()); 
      float marginMaxX = marginMinX
                       + (float)(bpToCompXScale*printableArea.getWidth());
      float marginMaxY = marginMinY
                       + (float)(bpToCompYScale*printableArea.getHeight());

      GeneralPath area = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

      area.moveTo(0f, 0f);
      area.lineTo(0f, compPaperHeight);
      area.lineTo(compPaperWidth,
                  compPaperHeight);
      area.lineTo(compPaperWidth, 0f);
      area.closePath();
      area.moveTo(marginMinX, marginMinY);
      area.lineTo(marginMinX, marginMaxY);
      area.lineTo(marginMaxX, marginMaxY);
      area.lineTo(marginMaxX, marginMinY);
      area.closePath();

      g.fill(area);
   }

   public void print()
   {
      // set paper size
      JDRPaper p = frame_.getPaper();

      PrintService service 
         = getApplication().getPrintService(p);

      if (service != null)
      {
         // obtain printer job
         PrinterJob printJob = PrinterJob.getPrinterJob();

         printJob.setPrintable(this);

         try
         {
            printJob.setPrintService(service);

            getApplication().doPrintJob(printJob);
         }
         catch (PrinterException pe)
         {
            getResources().error(frame_, new String[]
               {getResources().getMessage("error.printing"),
               pe.getMessage()});
         }
         catch (Exception e)
         {
            getResources().internalError(frame_,e);
         }
      }
      else
      {
         getResources().error(frame_,
            getResources().getMessage("error.printing.no_service"));
      }
   }

   public int print(Graphics g, PageFormat pageFormat, int pageIndex)
   {
      if (pageIndex > 0)
      {
         return Printable.NO_SUCH_PAGE;
      }
      else
      {
         RepaintManager currentManager 
            = RepaintManager.currentManager(this);
         currentManager.setDoubleBufferingEnabled(false);

         Graphics2D g2 = (Graphics2D)g;
         g2.setRenderingHints(getRenderingHints());

         for (int i = 0, n = paths.size(); i < n; i++)
         {
             paths.get(i).print(g2);
         }

         currentManager.setDoubleBufferingEnabled(true);
         return Printable.PAGE_EXISTS;
      }
   }

   public JDRFrame getFrame()
   {
      return frame_;
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return paths == null ? null : paths.getCanvasGraphics();
   }

   public double getStorageToComponentX()
   {
      return getCanvasGraphics().storageToComponentX(1.0);
   }

   public double getStorageToComponentY()
   {
      return getCanvasGraphics().storageToComponentY(1.0);
   }

   public boolean isObjectVisible(JDRCompleteObject object)
   {
      return isObjectVisible(object, displayPage);
   }

   public boolean isObjectVisible(JDRCompleteObject object, int page)
   {
       FlowFrame flowframe = object.getFlowFrame();

       if (flowframe != null)
       {
          switch (page)
          {
             case PAGES_ALL:
                return true;
             case PAGES_EVEN:
                return flowframe.isDefinedOnEvenPages();
             case PAGES_ODD:
                return flowframe.isDefinedOnOddPages();
             default :
                return flowframe.isDefinedOnPage(page);
          }
       }

      return true;
   }

   public int getNumberOfHiddenObjects()
   {
      if (paths == null)
      {
         return 0;
      }

      int numHidden=0;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (!isObjectVisible(object))
         {
            numHidden++;
         }
      }

      return numHidden;
   }

   public void setDisplayPage(int page)
   {
      if (paths == null)
      {
         return;
      }

      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (!isObjectVisible(object, page))
         {
            UndoableEdit edit = new SelectObject(
               object,false,
               getResources().getMessage("undo.deselect"));
            ce.addEdit(edit);
         }
      }

      UndoableEdit edit = new DisplayPageEdit(page);
      ce.addEdit(edit);

      ce.end();
      frame_.postEdit(ce);
   }

   public int getDisplayPage()
   {
      return displayPage;
   }

   public RenderingHints getRenderingHints()
   {
      return frame_.getRenderingHints();
   }

   public void repaint(Rectangle rect, boolean shift)
   {
      if (shift)
      {
         CanvasGraphics cg = getCanvasGraphics();
         repaint(0L, 
           (int)(rect.getX() + cg.getComponentOriginX()),
           (int)(rect.getY() + cg.getComponentOriginY()),
           rect.width, rect.height);
      }
      else
      {
         repaint(rect);
      }
   }

   public void paintComponent(Graphics g)
   {
      Graphics2D g2 = (Graphics2D)g;

      AffineTransform oldAf = g2.getTransform();
      Stroke oldStroke = g2.getStroke();

      super.paintComponent(g);

      if (paths == null)
      {
         return;
      }

      if (getFrame().isIoInProgress())
      {
         Dimension dim = getPreferredSize();

         int midY = dim.height/2;

         g2.setFont(getFont().deriveFont(Font.BOLD, 72f));
         g2.drawString(getResources().getMessage("message.io_in_progress"), 
            0, dim.height/2-2);
         g2.drawString(getResources().getMessage("message.please_wait"), 
            0, dim.height/2+74);

         return;
      }

      CanvasGraphics cg = getCanvasGraphics();
      cg.setGraphicsDevice(g2);
      cg.setResetTransform(oldAf);

      RenderingHints oldHints = g2.getRenderingHints();

      if (backgroundImage == null && 
          getCanvasGraphics().getOptimize() != CanvasGraphics.OPTIMIZE_NONE)
      {
         setBackgroundImage(true);
      }

      JDRGrid grid = cg.getGrid();
      JDRUnit gridUnit = grid.getMainUnit();
      JDRUnit storageUnit = cg.getStorageUnit();
      double offsetX = gridUnit.toUnit(cg.getOriginX(), storageUnit);
      double offsetY = gridUnit.toUnit(cg.getOriginY(), storageUnit);

      double bpToCompXScale = cg.bpToComponentX(1.0);
      double bpToCompYScale = cg.bpToComponentY(1.0);
      double storageToCompXScale = cg.storageToComponentX(1.0);
      double storageToCompYScale = cg.storageToComponentY(1.0);
      double compXToStorageScale = 1.0/storageToCompXScale;
      double compYToStorageScale = 1.0/storageToCompYScale;

      Rectangle rect = getBounds(null);

      g.getClipBounds(rect);

      Rectangle2D.Double clipBounds 
         = new Rectangle2D.Double(
               compXToStorageScale*rect.getX(),
               compYToStorageScale*rect.getY(),
               compXToStorageScale*rect.getWidth(),
               compYToStorageScale*rect.getHeight());

      FlowFrame typeblock = paths.getFlowFrame();

      if (backgroundImage != null)
      {
         g2.drawImage(backgroundImage,
                     (int)backgroundImageX,
                     (int)backgroundImageY, this);
         g2.setRenderingHints(frame_.getRenderingHints());
      }
      else
      {
         if (frame_.showGrid())
         {
            cg.getGrid().drawGrid();
         }

         g2.setPaint(marginColor);

         drawPrinterMargins(g2, bpToCompXScale, bpToCompYScale);

         g2.setPaint(typeblockColor);

         if (typeblock != null)
         {
            typeblock.draw(
               new BBox(cg, offsetX, offsetY,
                  frame_.getStoragePaperWidth()+offsetX,
                  frame_.getStoragePaperHeight()+offsetY));
         }

         g2.setRenderingHints(frame_.getRenderingHints());
      }

      Vector<BBox> selectedBBoxes = null;

      if ((backgroundImage == null)
        ||(backgroundImage != null 
            && editedPath == null && editedDistortion == null))
      {
         int n = paths.size();

         if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
         {
            selectedBBoxes = new Vector<BBox>(n);
         }
         else
         {
            selectedBBoxes = new Vector<BBox>();
         }

         g2.scale(storageToCompXScale, storageToCompYScale);

         AffineTransform oddAf = g2.getTransform();
         oddAf.translate(offsetX, offsetY);
         AffineTransform evenAf = oddAf;

         if (typeblock != null && cg.isEvenPage())
         {
            evenAf = (AffineTransform)oddAf.clone();
            evenAf.translate(typeblock.getEvenXShift(), 0.0);
         }

         for (int i = 0; i < n; i++)
         {
             JDRCompleteObject object = paths.get(i);

             if (isObjectVisible(object))
             {
                if (!object.isEdited())
                {
                   BBox box = object.getStorageBBox();
                   boolean isShowing;

                   if (getApplication().dragScaleEnabled())
                   {
                      JDRPoint topLeft = box.getTopLeft();
                      JDRPoint bottomRight = box.getBottomRight();

                      BBox extendedBox = box.add(topLeft.getStorageBBox());
                      extendedBox.encompass(bottomRight.getStorageBBox());

                      isShowing = extendedBox.intersects(clipBounds);
                   }
                   else
                   {
                      isShowing = box.intersects(clipBounds);
                   }

                   if (isShowing)
                   {
                      if (evenAf != oddAf && object.getFlowFrame() != null)
                      {
                         g2.setTransform(evenAf);
                      }
                      else
                      {
                         g2.setTransform(oddAf);
                      }

                      object.draw(false);

                      if (object.isSelected())
                      {
                         FlowFrame flowframe = object.getFlowFrame();

                         if (flowframe != null && cg.isEvenPage())
                         {
                            double xshift = flowframe.getEvenXShift();

                            if (typeblock != null)
                            {
                               xshift += typeblock.getEvenXShift();
                            }

                            box.translate(-xshift, 
                                          -flowframe.getEvenYShift());
                         }

                         selectedBBoxes.add(box);
                      }
                   }
                }
             }
         }
      }

      g2.setRenderingHints(oldHints);
      g2.setStroke(oldStroke);
      g2.setTransform(oldAf);
      g2.translate(offsetX * storageToCompXScale, offsetY * storageToCompYScale);

      if (selectedBBoxes != null)
      {
         // Draw the bounding boxes for the selected objects

         for (int i = 0, n=selectedBBoxes.size(); i < n; i++)
         {
            BBox box = selectedBBoxes.get(i);

            box.draw(getApplication().dragScaleEnabled() ? hotspotFlags : 0);
         }

         selectedBBoxes = null;
      }

      if (currentPath != null)
      {
         // Draw the path under construction

         BBox box = currentPath.getStorageControlBBox();

         if (box != null && box.intersects(clipBounds))
         {
            currentPath.drawDraft();
         }

         if (currentSegment != null) 
         {
            currentSegment.drawDraft(true);
         }
      }
      else if (editedPath != null)
      {
         BBox box = editedPath.getStorageControlBBox();

         if (box.intersects(clipBounds))
         {
            g2.setStroke(oldStroke);
            editedPath.drawDraft();
         }
      }
      else if (editedDistortion != null)
      {
         BBox box = editedDistortion.getStorageControlBBox();
         BBox bbox = editedDistortion.getDragBBox();

         box.merge(bbox);

         if (box.intersects(clipBounds))
         {
            bbox.draw((short)0);

            editedDistortion.drawControls(true);
         }
      }

      if (scanshape != null)
      {
         scanshape.draw(g2);
      }

      if (dragBBox != null)
      {
         g2.setXORMode(getBackground());

         dragBBox.draw();

         g2.setPaintMode();
      }

      g2.setTransform(oldAf);
   }

   public void deselectAll()
   {
      if (paths == null)
      {
         return;
      }

      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      boolean done=false;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         UndoableEdit edit = new SelectObject(
            paths.get(i), false,
            getResources().getMessage("undo.deselect_all"));
         ce.addEdit(edit);
         done=true;
      }

      ce.end();
      if (done) frame_.postEdit(ce);
   }

   public boolean deselectAll(JDRCanvasCompoundEdit ce)
   {
      boolean done=false;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         UndoableEdit edit = new SelectObject(
            paths.get(i), false,
            getResources().getMessage("undo.deselect_all"));
         ce.addEdit(edit);
         done=true;
      }

      return done;
   }

   public void drawRectangle(Graphics g, Point p1, Point p2)
   {
      int x      = (int)p1.x;
      int y      = (int)p1.y;
      int width  = (int)(p2.x-p1.x);
      int height = (int)(p2.y-p1.y);

      if (p1.x > p2.x)
      {
         width = -width;
         x = p2.x;
      }
      if (p1.y > p2.y)
      {
         height = -height;
         y = p2.y;
      }

      g.drawRect(x, y, width, height);
   }

   public void drawEllipse(Graphics g, Point p1, Point p2)
   {
      int x      = p1.x;
      int y      = p1.y;
      int width  = 2*Math.abs(p2.x-p1.x);
      int height = 2*Math.abs(p2.y-p1.y);

      g.drawOval(x-width/2, y-height/2, width, height);
   }

   public void setStatusCoordsFromComponent(double compX, double compY)
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRGrid grid = cg.getGrid();

      frame_.setCurrentPosition(grid.formatLocationFromCartesianBp(
         cg.componentXToBp(compX), cg.componentYToBp(compY)));
   }

   public void setStatusCoordsFromBp(double bpX, double bpY)
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRGrid grid = cg.getGrid();

      frame_.setCurrentPosition(
         grid.formatLocationFromCartesianBp(bpX, bpY));
   }

   public void setStatusCoordsFromStorage(double storageX, double storageY)
   {
      CanvasGraphics cg = getCanvasGraphics();

      double factor = cg.storageToBp(1.0);

      JDRGrid grid = cg.getGrid();

      frame_.setCurrentPosition(grid.formatLocationFromCartesianBp(
        factor*storageX, factor*storageY));
   }

   public void mouseMoved(MouseEvent evt)
   {
      Point2D currentPos = getNearestStorageTic(evt);
      moveToStorage(currentPos);

   }

   public void mousePressed(MouseEvent evt)
   {
      if (frame_.isEditingLaTeXCode())
      {
         requestFocusInWindow();
      }

      Point2D actual = new Point2D.Double();
      Point2D currentPos = getNearestStorageTic(evt, actual);
      mouseDown = true;
      int tool = frame_.currentTool();

      if (checkForPopupTrigger(evt)) return;

      if (evt.isAltDown()) return;

      if (tool == ACTION_SELECT)
      {
         if (paths == null) return;

         dragBBox = null;

         if (frame_.isObjectDistorting())
         {
            if (editedDistortion == null) return;

            if (cpedit == null)
               cpedit = new JDRCanvasCompoundEdit(this);

            if (evt.isControlDown())
            {
               UndoableEdit edit =
                new DeselectDistortionControl();

               frame_.postEdit(edit);
            }
            else
            {
               try
               {
                  UndoableEdit edit =
                     new SelectDistortionControl(currentPos);

                  frame_.postEdit(edit);

                  return;
               }
               catch (NullPointerException e)
               {
                  // no control selected
               }
            }

            // Finish distortion if outside bounding box

            if (dragScaleHotspot == BBox.HOTSPOT_NONE)
            {
               BBox box = editedDistortion.getUnderlyingObject()
                            .getStorageDistortionBounds();

               if (!box.contains(currentPos))
               {
                  setDistortState(false);
                  return;
               }
            }
         }
         else if (dragScaleHotspot != BBox.HOTSPOT_NONE)
         {
            cpedit = new JDRCanvasCompoundEdit(this);

            switch (dragScaleHotspot)
            {
               case BBox.HOTSPOT_S :
               case BBox.HOTSPOT_E :
               case BBox.HOTSPOT_SE :
                  dragScaleAnchor = 
                    dragScaleObject.getTopLeftHS();
               break;
               case BBox.HOTSPOT_NE :
               case BBox.HOTSPOT_NW :
                  dragScaleAnchor = 
                    dragScaleObject.getBottomLeftHS();
               break;
               case BBox.HOTSPOT_SW :
                  dragScaleAnchor =
                     dragScaleObject.getCentreHS();
               break;
            }

         }
         else if (frame_.isPathEdited())
         {
            if (editedPath == null) return;

            if (cpedit==null)
               cpedit = new JDRCanvasCompoundEdit(this);

            try
            {
               UndoableEdit edit;

               if (getApplication().getSettings().selectControlIgnoresLock)
               {
                  edit = new SelectControl(actual);
               }
               else
               {
                  edit = new SelectControl(currentPos);
               }

               frame_.postEdit(edit);
               repaint(editedPath.getComponentControlBBox().
                  getRectangle(), true);
            }
            catch (NullPointerException e)
            {
               // no control selected

               if (getApplication().getSettings().canvasClickExitsPathEdit)
               {
                  finishEditPath();
               }
            }
         }
         else if (editedDistortion == null && !paths.anySelected(currentPos))
         {
            boolean selected=false;
            JDRCompleteObject thisPath=null;
            selectedIndex = -1;

            FlowFrame typeblock = paths.getFlowFrame();

            for (int i = paths.size()-1; i >= 0; i--)
            {
               thisPath = paths.get(i);

               BBox bbox = thisPath.getStorageBBox();

               CanvasGraphics cg = getCanvasGraphics();

               double hoffset = 0.0;
               double voffset = 0.0;

               FlowFrame flowframe = thisPath.getFlowFrame();

               if (flowframe != null && cg.isEvenPage())
               {
                  hoffset = -flowframe.getEvenXShift();
                  voffset = -flowframe.getEvenYShift();

                  if (typeblock != null)
                  {
                     hoffset -= typeblock.getEvenXShift();
                  }
               }

               if (bbox.contains(currentPos.getX()+hoffset, 
                                 currentPos.getY()+voffset)
                 &&isObjectVisible(thisPath))
               {
                  selected = true;
                  selectedIndex = i;
                  break;
               }
            }

            anchor = (Point2D)currentPos.clone();

            if (selected && !evt.isShiftDown())
            {
               JDRCanvasCompoundEdit ce 
                  = new JDRCanvasCompoundEdit(this);

               if (!evt.isControlDown()) deselectAll(ce);
               if (!isObjectVisible(thisPath)) return;

               UndoableEdit edit = new SelectObject(
                  thisPath, true);
               ce.addEdit(edit);
               ce.end();
               frame_.postEdit(ce);
               cpedit = new JDRCanvasCompoundEdit(this);
            }
            else
            {
               dragBBox = new BBox(getCanvasGraphics(),
                  anchor.getX(), anchor.getY(),
                  anchor.getX(), anchor.getY());
               deselectAll();
               setCursor(
                  Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
         }
         else
         {
            cpedit = new JDRCanvasCompoundEdit(this);
         }
      }
   } 

   public void mouseReleased(MouseEvent evt)
   {
      if (frame_.isEditingLaTeXCode())
      {
         requestFocusInWindow();
      }

      Point2D currentPos = getNearestStorageTic(evt);
      mouseDown = false;
      int tool = frame_.currentTool();

      if (checkForPopupTrigger(evt)) return;

      if (evt.isMetaDown() || evt.isAltDown()) return;

      if (tool == ACTION_SELECT)
      {
         setToolCursor();

         dragBBox = null;

         if (dragScaleHotspot != BBox.HOTSPOT_NONE)
         {
            dragScaleHotspot = BBox.HOTSPOT_NONE;
            dragScaleObject = null;

            cpedit.end();
            frame_.postEdit(cpedit);
            cpedit = null;
            anchor=null;
         }
         else if (!frame_.isPathEdited() && !frame_.isObjectDistorting())
         {
            if (!paths.anySelected() && anchor != null)
            {
               // end of drag selection
               double minx = (anchor.getX() < mouse.getX() ?
                              anchor.getX() : mouse.getX());
               double miny = (anchor.getY() < mouse.getY() ?
                              anchor.getY() : mouse.getY());
               double maxx = (anchor.getX() > mouse.getX() ?
                              anchor.getX() : mouse.getX());
               double maxy = (anchor.getY() > mouse.getY() ?
                              anchor.getY() : mouse.getY());
               BBox box = new BBox(getCanvasGraphics(),minx,miny,maxx,maxy);

               Vector<JDRCompleteObject> grp;

               if (evt.isShiftDown())
               {
                  grp = paths.getAllInsideStorageBox(box);
               }
               else
               {
                  grp = paths.getAllIntersectsStorageBox(box);
               }

               repaint(box.getComponentRectangle(), true);

               int n=0;
               JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
               for (int i = 0, m = grp.size(); i < m; i++)
               {
                  JDRCompleteObject object = grp.get(i);
                  if (!isObjectVisible(object))
                  {
                     continue;
                  }

                  UndoableEdit edit = new SelectObject(
                     object, true);
                  ce.addEdit(edit);
                  n++;
               }
               ce.end();
               if (n > 0) frame_.postEdit(ce);
               anchor = null;
            }
            else if (cpedit != null)
            {
               // end of move
               cpedit.end();
               frame_.postEdit(cpedit);
               cpedit = null;
            }
         }
         else if (cpedit != null)
         {
            cpedit.end();
            frame_.postEdit(cpedit);
            cpedit = null;
         }
      }
   }

   public void mouseClicked(MouseEvent evt)
   {
      if (frame_.isIoInProgress())
      {
         return;
      }

      Point2D currentPos = getNearestStorageTic(evt);

      int tool = frame_.currentTool();

      setToolCursor();

      if (evt.isMetaDown() || evt.isAltDown()) return;

      if (frame_.isPathEdited())
      {
      }
      else if (evt.getClickCount() >= 2 && currentPath != null)
      {
         finishPath();
      }
      else if (dragScaleHotspot != BBox.HOTSPOT_NONE)
      {
         anchor.setLocation(currentPos.getX(),
                            currentPos.getY());
      }
      else if (tool == ACTION_SELECT)
      {
         if (evt.getClickCount() == 1 && evt.isShiftDown())
         {
            if (paths == null) return;
   
            for (int i = paths.size()-1; i >= 0; i--)
            {
               JDRCompleteObject object = paths.get(i);
               BBox bbox = object.getStorageBBox();

               if (object.isSelected()
                  && bbox.contains(currentPos)
                  && isObjectVisible(object))
               {
                  UndoableEdit edit = new SelectObject(
                     object, false,
                     getResources().getMessage("undo.deselect"));
                  frame_.postEdit(edit);
                  break;
               }
            }
         }
         else if (evt.getClickCount() >= 2)
         {
            if (paths == null) return;

            boolean selected=false;
            JDRCompleteObject thisPath=null;
            int index=-1;
            for (int i = selectedIndex-1; i >= 0; i--)
            {
               thisPath = paths.get(i);
               BBox bbox = thisPath.getStorageBBox();

               if (bbox.contains(currentPos)
                 && isObjectVisible(thisPath))
               {
                  index = i;
                  selected = true;
                  break;
               }
            }

            if (!selected)
            {
               for (int i = paths.size()-1; i > selectedIndex; i--)
               {
                  index = i;
                  thisPath = paths.get(i);
                  BBox bbox = thisPath.getStorageBBox();

                  if (bbox.contains(currentPos)
                    && isObjectVisible(thisPath))
                  {
                     selected = true;
                     break;
                  }
               }
            }

            if (selected)
            {
               if (thisPath.isSelected())
               {
                  // already selected
               }
               else
               {
                  JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
                  boolean done = false;

                  if (!evt.isControlDown())
                  {
                     done = deselectAll(ce);
                  }

                  if (isObjectVisible(thisPath))
                  {
                     UndoableEdit edit = new SelectObject(
                        thisPath, true,
                        getResources().getMessage("undo.select"));
                     ce.addEdit(edit);
                     done = true;
                  }

                  ce.end();
                  if (done) frame_.postEdit(ce);
               }
               repaint();
            }
            anchor = (Point2D)currentPos.clone();
            selectedIndex=index;
         }
      }
      else
      {
         doConstructMouseClick(currentPos);
      }
   }

   public void mouseEntered(MouseEvent evt)
   {
      Point2D currentPos = getNearestStorageTic(evt);

      mouse.setLocation(currentPos.getX(),currentPos.getY());

      setStatusCoordsFromStorage(mouse.getX(), mouse.getY());

      setToolCursor();
   }

   public void mouseExited(MouseEvent evt)
   {
   }

   public void moveToStorage(Point2D currentStoragePos)
   {
      int tool = frame_.currentTool();

      double x = currentStoragePos.getX();
      double y = currentStoragePos.getY();

      setStatusCoordsFromStorage(x, y);

      if (anchor != null)
      {
         if ((tool == ACTION_OPEN_LINE || 
             tool == ACTION_CLOSED_LINE) &&
             currentSegment != null)
         {
            currentSegment.setEnd(currentStoragePos);
            repaint();
         }
         else if ((tool == ACTION_OPEN_CURVE ||
                  tool == ACTION_CLOSED_CURVE) &&
                  currentSegment != null)
         {
            if (currentSegment instanceof JDRBezier)
            {
               JDRBezier seg;
               JDRPathSegment prev = currentPath.getLastSegment();

               if (currentPath.size() == 0 || 
                   !(prev instanceof JDRBezier))
               {
                  seg = new JDRBezier(getCanvasGraphics(), anchor, 
                     currentStoragePos);
               }
               else
               {
                  seg = JDRBezier.constructBezier((JDRSegment)prev,
                          currentStoragePos);

                  if (currentPath.size() > 1 && prev instanceof JDRBezier)
                  {
                     JDRPathSegment beforePrev = 
                        currentPath.get(currentPath.size()-2);

                     if (beforePrev instanceof JDRBezier)
                     {
                        JDRBezier.makeContinuous((JDRBezier)beforePrev,
                                              (JDRBezier)prev);
                     }
                  }
               }

               currentSegment = seg;
               repaint();
            }
            else
            {
               Graphics g = getGraphics();
               getCanvasGraphics().setGraphicsDevice((Graphics2D)g);

               try
               {
                  g.setXORMode(getBackground());
                  currentSegment.drawDraft(true);
                  currentSegment.setEnd(currentStoragePos);
                  currentSegment.drawDraft(true);
               }
               finally
               {
                  getCanvasGraphics().setGraphicsDevice(null);
                  g.dispose();
               }
            }
         }
         else if (tool == ACTION_RECTANGLE)
         {
            currentPath = JDRPath.constructRectangle(
               getCanvasGraphics(), anchor, new Point2D.Double(x,y));
            repaint();
         }
         else if (tool == ACTION_ELLIPSE)
         {
            double w = Math.abs(mouse.getX()-anchor.getX());
            double h = Math.abs(mouse.getY()-anchor.getY());
            currentPath = JDRPath.constructEllipse(
               getCanvasGraphics(), anchor, w, h);
            repaint();
         }
      }

      dragScaleHotspot=BBox.HOTSPOT_NONE;
      dragScaleObject = null;
      dragScaleIndex = -1;

      if (tool == ACTION_SELECT && editedPath == null
          && editedDistortion == null && getApplication().dragScaleEnabled())
      {
         for (int i = paths.size()-1; i >= 0; i--)
         {
            JDRCompleteObject object = paths.get(i);

            if (object.isSelected())
            {
               dragScaleHotspot = object.getHotspotFromStoragePoint(
                                     currentStoragePos);

               if (dragScaleHotspot != BBox.HOTSPOT_NONE)
               {
                  dragScaleObject = object;
                  dragScaleIndex = i;
                  break;
               }
            }
         }

         switch (dragScaleHotspot)
         {
            case BBox.HOTSPOT_NONE :
               setToolCursor();
            break;
            case BBox.HOTSPOT_E :
               setCursor(Cursor.getPredefinedCursor(
                  Cursor.E_RESIZE_CURSOR));
            break;
            case BBox.HOTSPOT_SE :
               setCursor(Cursor.getPredefinedCursor(
                  Cursor.SE_RESIZE_CURSOR));
            break;
            case BBox.HOTSPOT_S :
               setCursor(Cursor.getPredefinedCursor(
                  Cursor.S_RESIZE_CURSOR));
            break;
            case BBox.HOTSPOT_SW :
               setCursor(Cursor.getPredefinedCursor(
                  Cursor.HAND_CURSOR));
            break;
            case BBox.HOTSPOT_W :
               setCursor(Cursor.getPredefinedCursor(
                  Cursor.DEFAULT_CURSOR));
            break;
            case BBox.HOTSPOT_NE :
               setCursor(Cursor.getPredefinedCursor(
                  Cursor.N_RESIZE_CURSOR));
            break;
            case BBox.HOTSPOT_N :
               setCursor(Cursor.getPredefinedCursor(
                  Cursor.DEFAULT_CURSOR));
            break;
            case BBox.HOTSPOT_NW :
               setCursor(Cursor.getPredefinedCursor(
                  Cursor.E_RESIZE_CURSOR));
            break;
         }
      }

      mouse.setLocation(x, y);
   }

   public void mouseDragged(MouseEvent evt)
   {
      Point2D currentPos = getNearestStorageTic(evt);

      int tool = frame_.currentTool();

      if (evt.isAltDown() || evt.isMetaDown()) return;

      double x = currentPos.getX();
      double y = currentPos.getY();

      scrollRectToVisible(new Rectangle(evt.getX(),evt.getY(),1,1));

      if (tool == ACTION_SELECT)
      {
         Cursor currentCursor = getCursor();
         Cursor moveCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

         if (dragScaleObject != null && dragScaleAnchor != null)
         {
            if (currentCursor != moveCursor)
            {
               setCursor(moveCursor);
            }

            BBox bbox = dragScaleObject.getDragBBox();
            double minX = dragScaleAnchor.x;
            double minY = dragScaleAnchor.y;

            double mouseX = mouse.getX();
            double mouseY = mouse.getY();

            FlowFrame flowframe = dragScaleObject.getFlowFrame();
            FlowFrame typeblock = paths.getFlowFrame();

            if (flowframe != null
             && getCanvasGraphics().isEvenPage())
            {
               minX += flowframe.getEvenXShift();
               minY += flowframe.getEvenYShift();

               if (typeblock != null)
               {
                  minX += typeblock.getEvenXShift();
               }
            }

            double dx = x < minX ? mouseX-x : x-mouseX;
            double dy = y < minY ? mouseY-y : y-mouseY;

            switch (dragScaleHotspot)
            {
               case BBox.HOTSPOT_E :
               // scale horizontally
                  if (dx == 0) break;
                  double width = bbox.getWidth();
                  if (width == 0) return;
                  double newWidth = width + dx;
                  double factor = newWidth/width;

                  if (Math.abs(factor) >= 1e-6)
                  {
                     if ((x < minX && mouseX > minX)
                      || (x > minX && mouseX < minX))
                     {
                        // flipped
                        factor = -factor;
                     }

                     UndoableEdit edit = new ScaleX(dragScaleObject,
                        factor, dragScaleIndex, dragScaleAnchor);
                     cpedit.addEdit(edit);
                  }
                  else
                  {
                     x = mouseX;
                  }
               break;
               case BBox.HOTSPOT_S :
               // scale vertically
                  if (dy == 0) break;
                  double height = bbox.getHeight();
                  if (height == 0) return;
                  double newHeight = height + dy;
                  factor = newHeight/height;

                  if (Math.abs(factor) >= 1e-6)
                  {
                     if ((y < minY && mouseY > minY)
                      || (y > minY && mouseY < minY))
                     {
                        // flipped
                        factor = -factor;
                     }

                     UndoableEdit edit = new ScaleY(dragScaleObject,
                        factor, dragScaleIndex, dragScaleAnchor);
                     cpedit.addEdit(edit);
                  }
                  else
                  {
                     y = mouseY;
                  }
               break;
               case BBox.HOTSPOT_SE :
               // scale in both directions
                  if (dx == 0 && dy == 0) break;
                  height = bbox.getHeight();
                  width = bbox.getWidth();
                  if (height == 0 || width == 0) return;
                  newHeight = height + dy;
                  newWidth = width + dx;
                  double factorY = newHeight/height;
                  double factorX = newWidth/width;

                  if (Math.abs(factorY) >= 1e-6
                    && Math.abs(factorX) >= 1e-6)
                  {
                     if ((y < minY && mouseY > minY)
                      || (y > minY && mouseY < minY))
                     {
                        // flipped
                        factorY = -factorY;
                     }

                     if ((x < minX && mouseX > minX)
                      || (x > minX && mouseX < minX))
                     {
                        // flipped
                        factorX = -factorX;
                     }

                     UndoableEdit edit = new Scale(dragScaleObject,
                        factorX, factorY, dragScaleIndex,
                        dragScaleAnchor);
                     cpedit.addEdit(edit);
                  }
                  else
                  {
                     x = mouseX;
                     y = mouseY;
                  }
               break;

               case BBox.HOTSPOT_SW :
               // rotate
               // (ax,ay) is vector from centre to old position
               // (bx,by) is vector from centre to new position
               double ax = mouseX-dragScaleAnchor.x;
               double ay = mouseY-dragScaleAnchor.y;
               double bx = x - dragScaleAnchor.x;
               double by = y - dragScaleAnchor.y;

               // compute angle

               double norma = Math.sqrt(ax*ax+ay*ay);
               double normb = Math.sqrt(bx*bx+by*by);

               if (norma != 0 && normb != 0)
               {
                  double cosA = ax/norma;
                  double cosB = bx/normb;

                  double theta = Math.acos(cosA)-Math.acos(cosB);

                  if (by > 0) theta = -theta;

                  UndoableEdit edit = new Rotate(dragScaleObject,
                     theta, dragScaleIndex, dragScaleAnchor);
                  cpedit.addEdit(edit);
               }
               break; 
               case BBox.HOTSPOT_NW :
               // shear horizontally
               double sx = ((double)(x-mouseX))
                         / ((double)mouseY);

               if (sx != 0)
               {
                  UndoableEdit edit = new Shear(dragScaleObject,
                     sx, 0.0, dragScaleIndex, dragScaleAnchor);
                  cpedit.addEdit(edit);
               }
               break;
               case BBox.HOTSPOT_NE :
               // shear vertically
               double sy = ((double)(mouseY-y))
                         / ((double)mouseX);

               if (sy != 0)
               {
                  UndoableEdit edit = new Shear(dragScaleObject,
                     0.0, sy, dragScaleIndex, dragScaleAnchor);
                  cpedit.addEdit(edit);
               }
               break;
            }
         }
         else if (frame_.isObjectDistorting() &&
             editedDistortion != null
           && editedDistortion.getSelectedPoint() != null)
         {
            if (currentCursor != moveCursor)
            {
               setCursor(moveCursor);
            }

            UndoableEdit edit = new MoveDistortionControl(
               x-mouse.getX(), y-mouse.getY(), evt.isShiftDown());

            if (cpedit!=null) cpedit.addEdit(edit);
         }
         else if (frame_.isPathEdited())
         {
            if (currentCursor != moveCursor)
            {
               setCursor(moveCursor);
            }

            JDRPoint p = getSelectedStoragePoint();

            if (p != null && editedPath != null)
            {
               double incX = x-mouse.getX();
               double incY = y-mouse.getY();

               if (!(incX == 0.0 && incY == 0.0))
               {
                  UndoableEdit edit = new MovePoint(
                     editedPath,
                     editedPath.getSelectedSegment(),
                     p,
                     incX,
                     incY);

                  if (cpedit!=null) cpedit.addEdit(edit);
               }
            }
         }
         else
         {
            if (paths.anySelected())
            {
               if (currentCursor != moveCursor)
               {
                  setCursor(moveCursor);
               }

               dragBBox = null;
               double shift_left = mouse.getX()-x;
               double shift_up   = mouse.getY()-y;

               for (int i = 0, n=paths.size(); i < n; i++)
               {
                  JDRCompleteObject object = paths.get(i);

                  if (object.isSelected())
                  {
                     UndoableEdit edit = new MoveObject(
                        object, shift_left, shift_up);

                     if (cpedit != null) cpedit.addEdit(edit);
                  }
               }
            }
            else if (anchor != null && dragBBox != null)
            {
               double minx = Math.min(anchor.getX(), x);
               double miny = Math.min(anchor.getY(), y);
               double maxx = Math.max(anchor.getX(), x);
               double maxy = Math.max(anchor.getY(), y);

               repaint(dragBBox.getComponentRectangle(), true);

               dragBBox.reset(minx, miny, maxx, maxy);

               repaint(dragBBox.getComponentRectangle(), true);
            }
         }
      }

      mouse.setLocation(x, y);

      setStatusCoordsFromStorage(x, y);
   }

   public void gap()
   {
      if (currentSegment != null)
      {
         JDRSegment oldSegment = currentSegment;
         currentSegment = new JDRSegment(oldSegment.getStart(),
                                         oldSegment.getEnd());
       
         repaint();
      }
   }

   public void finishEditPath()
   {
      try
      {
         UndoableEdit edit = new EditPath(editedPath,false);
         frame_.postEdit(edit);
      }
      catch (Exception e)
      {
         getResources().internalError(frame_,
         getResources().getMessage("internal_error.finish_edit_path_failed"),
         e);
      }
   }

   public void editPath()
   {
      setToolCursor();
      JDRCompleteObject object = paths.getSelected();

      if (!(object instanceof JDRShape) || object == null)
      {
         getResources().internalError(frame_,
            getResources().getMessage("internal_error.no_path"));
      }
      else
      {
         UndoableEdit edit = new EditPath((JDRShape)object,
                                          !object.isEdited());
         frame_.postEdit(edit);
      }
   }

   public void resetTransform()
   {
      if (editedDistortion != null)
      {
         UndoableEdit edit = new DistortReset();
         frame_.postEdit(edit);
      }
      else
      {
         JDRCompleteObject object = paths.getSelected();

         if (object instanceof JDRTextual)
         {
            UndoableEdit edit = new TextReset((JDRTextual)object);
            frame_.postEdit(edit);
         }
         else if (object instanceof JDRBitmap)
         {
            UndoableEdit edit = new BitmapReset((JDRBitmap)object);
            frame_.postEdit(edit);
         }
         else
         {
            getResources().internalError(this, 
             new IllegalArgumentException("Class "+object.getClass().getName()
               +" doesn't have an associated transformation"));
         }
      }
   }

   public void setDistortState(boolean state)
   {
      if (state)
      {
         distortObject();
      }
      else
      {
         finishDistortObject();
      }

      setDistortObjectButton(isDistorting());
   }

   public boolean isDistorting()
   {
      return editedDistortion != null;
   }

   public void setDistortObjectButton(boolean flag)
   {
      CanvasAction action = getSelectGeneralAction("distort");

      if (action == null)
      {
         getResources().internalError(this, 
          new IllegalArgumentException("Can't find 'distort' action"));
      }
      else
      {
         action.setSelected(flag);
      }

      if (flag)
      {
         getApplication().setStatusInfo(
           getResources().getMessage("info.distort"), "sec:distort");
      }
      else
      {
         getApplication().setStatusInfo(
           getResources().getMessage("info.select"), "sec:selectobjects");
      }

      enableTools();
   }

   public void distortObject()
   {
      setToolCursor();

      int index = -1;
      JDRDistortable object = null;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject obj = paths.get(i);

         if (obj.isSelected() && obj instanceof JDRDistortable
           && obj.isDistortable())
         {
            object = (JDRDistortable)obj;
            index = i;
            break;
         }
      }

      if (object == null)
      {
         getResources().internalError(frame_,
           "null object can't be distorted");
      }
      else
      {
         UndoableEdit edit = new DistortObject(object);
         frame_.postEdit(edit);
      }
   }

   public void finishDistortObject()
   {
      try
      {
         UndoableEdit edit = new FinishDistortObject();
         frame_.postEdit(edit);
      }
      catch (Exception e)
      {
         getResources().internalError(frame_,
         "failed to finish distortion",
         e);
      }
   }

   public void setToolCursor()
   {
      setToolCursor(frame_.currentTool());
   }

   public void setToolCursor(int tool)
   {
      switch (tool)
      {
         case ACTION_SELECT :
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            break;
         case ACTION_TEXT :
         case ACTION_MATH :
            setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            break;
         default :
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }
   }

   public void setAction(int tool)
   {
      setToolCursor(tool);

      if (tool != ACTION_TEXT && tool != ACTION_MATH)
      {
         requestFocusInWindow();
      }

      int oldTool = frame_.currentTool();
      if (oldTool == tool) return;

      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      if (oldTool == ACTION_SELECT)
      {
         if (editedPath != null)
         {
            try
            {
               UndoableEdit edit = new EditPath(editedPath,false);
               ce.addEdit(edit);
            }
            catch (Throwable e)
            {
               // This shouldn't happen

               getResources().internalError(frame_, e);
            }
         }

         if (editedDistortion != null)
         {
            setDistortState(false);
         }
   
         deselectAll(ce);
      }

      UndoableEdit edit = new SetTool(tool);
      ce.addEdit(edit);

      ce.end();
      frame_.postEdit(ce);
   }

   private void finishEditingPath()
   {
      setToolCursor();

      if (editedPath != null)
      {
         editedPath.setEditMode(false);
      }

      getSelectPathAction("path.edit").setSelected(false);

      editedPath = null;
      enableTools();
   }

   public boolean isInEditMode()
   {
      return editedPath != null;
   }

   public void rotateSelectedPaths(JDRAngle angle)
   {
      rotateSelectedPaths(angle.toRadians());
   }

   public void rotateSelectedPaths(double angle)
   {
      if (angle == 0.0) return;

      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit = null;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            edit = new Rotate(object, angle, i);
            ce.addEdit(edit);
         }
      }

      ce.end();
      if (edit != null) frame_.postEdit(ce);
   }

   public void scaleSelectedPaths(double factor)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit = null;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            edit = new Scale(object, factor, i);
            ce.addEdit(edit);
         }
      }

      ce.end();
      if (edit != null) frame_.postEdit(ce);
   }

   public void scaleSelectedToTypeblock()
   {
      FlowFrame typeblock = paths.getFlowFrame();

      if (typeblock == null)
      {
         getResources().error(this, getResources().getMessage("error.no_typeblock"));
         return;
      }

      CanvasGraphics cg = getCanvasGraphics();

      double width = cg.getStoragePaperWidth() - typeblock.getLeft() 
                   - typeblock.getRight();
      double height = cg.getStoragePaperHeight() - typeblock.getTop()
                    - typeblock.getBottom();

      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit = null;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            double factorX, factorY;

            if (object instanceof JDRShape)
            {
                Rectangle2D bounds = 
                 ((JDRShape)object).getStorageStrokedArea().getBounds2D();
               factorX = width/bounds.getWidth();
               factorY = height/bounds.getHeight();
            }
            else
            {
               BBox box = object.getStorageBBox();

               factorX = width/box.getWidth();
               factorY = height/box.getHeight();
            }

            if (!Double.isInfinite(factorX)
             && !Double.isNaN(factorX)
             && !Double.isInfinite(factorY)
             && !Double.isNaN(factorY))
            {
               edit = new Scale(object, factorX, factorY, i);
               ce.addEdit(edit);
            }
         }
      }

      ce.end();
      if (edit != null) frame_.postEdit(ce);
   }

   public void scaleXSelectedPaths(double factor)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit=null;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            edit = new ScaleX(object, factor, i);
            ce.addEdit(edit);
         }
      }

      ce.end();
      if (edit != null) frame_.postEdit(ce);
   }

   public void scaleYSelectedPaths(double factor)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit = null;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            edit = new ScaleY(object, factor, i);
            ce.addEdit(edit);
         }
      }

      ce.end();
      if (edit != null) frame_.postEdit(ce);
   }

   public void shearSelectedPaths(double factorX, double factorY)
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit=null;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            edit = new Shear(object, factorX, factorY, i);
            ce.addEdit(edit);
         }
      }

      ce.end();
      if (edit != null) frame_.postEdit(ce);
   }

   public void moveUp()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      // make a list of indices

      Vector<Integer> list = new Vector<Integer>(paths.size());

      // omit top object as it can't move any higher

      for (int i = paths.size()-2; i >= 0; i--)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            // add index to list

            list.add(new Integer(i));
         }
      }

      if (list.size() == 0)
      {
         return;
      }

      for (int i = 0; i < list.size(); i++)
      {
          UndoableEdit edit = new MoveUp(list.get(i).intValue());
          ce.addEdit(edit);
      }

      ce.end();
      frame_.postEdit(ce);
   }

   public void moveDown()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      // make a list of indices

      Vector<Integer> list = new Vector<Integer>(paths.size());

      // Omit bottom item as it can't move any lower.

      for (int i = 1; i < paths.size(); i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            // add index to list

            list.add(new Integer(i));
         }
      }

      if (list.size() == 0)
      {
         return;
      }

      for (int i = 0; i < list.size(); i++)
      {
         UndoableEdit edit = new MoveDown(list.get(i).intValue());
         ce.addEdit(edit);
      }

      ce.end();
      frame_.postEdit(ce);
   }

   public void moveToFront()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      boolean done=false;

      for (int i = paths.size()-1; i >= 0; i--)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            UndoableEdit edit = new MoveToFront(object);
            ce.addEdit(edit);
            done = true;
         }
      }

      ce.end();
      if (done) frame_.postEdit(ce);
   }

   public void moveToBack()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      boolean done=false;

      for (int i = 0, n=paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            UndoableEdit edit = new MoveToBack(object);
            ce.addEdit(edit);
            done = true;
         }
      }

      ce.end();
      if (done) frame_.postEdit(ce);
   }

   public void splitText()
   {
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      boolean done=false;

      for (int i = 0, n=paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected() && object.hasTextual())
         {
            try
            {
               UndoableEdit edit = new SplitText(object);
               ce.addEdit(edit);
               done = true;
            }
            catch (Throwable e)
            {
               getResources().internalError(frame_, e);
            }
         }
      }

      ce.end();
      if (done) frame_.postEdit(ce);
   }

   public void convertToPath()
   {
      boolean done = false;
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      for (int i = 0, n=paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            try
            {
               if (object instanceof JDRText)
               {
                  UndoableEdit edit
                     = new ConvertTextToPath((JDRText)object);
                  ce.addEdit(edit);
                  done = true;
               }
               else if (object instanceof JDRTextPath)
               {
                  UndoableEdit edit
                     = new ConvertTextPathToPath((JDRTextPath)object);
                  ce.addEdit(edit);
                  done = true;
               }
               else if (object instanceof JDRShape)
               {
                  UndoableEdit edit 
                     = new ConvertOutlineToPath((JDRShape)object);
                  ce.addEdit(edit);
                  done = true;
               }
            }
            catch (EmptyGroupException e)
            {
               getResources().error(frame_,
                  getResources().getMessage("error.convert_to_path_failed"), e);
            }
            catch (Exception e)
            {
               getResources().internalError(frame_,
                getResources().getMessage("internal_error.convert_to_path")
                +"\n"+e.getMessage(),
                 e);
            }
         }
      }

      ce.end();
      if (done) frame_.postEdit(ce);
   }

   public void convertToFullPath()
   {
      boolean done = false;
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      for (int i = 0, n=paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            try
            {
               if (object instanceof JDRCompoundShape)
               {
                  UndoableEdit edit 
                     = new ConvertToFullPath((JDRCompoundShape)object);
                  ce.addEdit(edit);
                  done = true;
               }
            }
            catch (Exception e)
            {
               getResources().internalError(frame_, e);
            }
         }
      }

      ce.end();
      if (done) frame_.postEdit(ce);
   }

   public void convertToTextPath()
   {
      JDRShape path = null;
      JDRText text = null;

      for (int i = 0, n=paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRShape
           && !(object.hasTextual()))
            {
               path = (JDRShape)object;
            }
            else if (object instanceof JDRText)
            {
               text = (JDRText)object;
            }

            if (text != null && path != null)
            {
               break;
            }
         }
      }

      if (text == null || path == null)
      {
         getResources().internalError(frame_,
          getResources().getMessage(
            "internal_error.convert_to_textpath"));
      }
      else
      {
         UndoableEdit edit = new ConvertToTextPath(path, text);

         frame_.postEdit(edit);
      }
   }

   public void separate()
   {
      boolean done = false;
      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);

      for (int i = 0, n=paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRCompoundShape)
            {
               try
               {
                  UndoableEdit edit =
                    new Separate((JDRCompoundShape)object);
                  ce.addEdit(edit);

                  done = true;
               }
               catch (Throwable e)
               {
                  // This shouldn't happen
                  getResources().internalError(frame_, e);
               }
            }
         }
      }

      ce.end();
      if (done) frame_.postEdit(ce);
   }

   public void mergePaths()
   {
      int total = paths.size();

      Vector<JDRShape> list;

      if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         list = new Vector<JDRShape>(total);
      }
      else
      {
         list = new Vector<JDRShape>();
      }

      int n = 0;

      for (int i = 0; i < total; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected() && object instanceof JDRShape)
         {
            list.add((JDRShape)object);
            n++;
         }
      }

      if (n < 2) return;

      try
      {
         UndoableEdit edit = new MergePaths(list);
         frame_.postEdit(edit);
      }
      catch (Exception e)
      {
         getResources().internalError(frame_, e);
      }
   }

   public void xorPaths()
   {
      int total = paths.size();

      Vector<JDRShape> list;

      if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         list = new Vector<JDRShape>(total);
      }
      else
      {
         list = new Vector<JDRShape>();
      }

      int n = 0;

      for (int i = 0; i < total; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected() && object instanceof JDRShape)
         {
            list.add((JDRShape)object);
            n++;
         }
      }

      if (n < 2) return;

      try
      {
         UndoableEdit edit = new XORPaths(list);
         frame_.postEdit(edit);
      }
      catch (Throwable e)
      {
         // This shouldn't happen
         getResources().internalError(frame_, e);
      }
   }

   public void pathIntersect()
   {
      int total = paths.size();

      Vector<JDRShape> list;

      if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         list = new Vector<JDRShape>(total);
      }
      else
      {
         list = new Vector<JDRShape>();
      }

      int n = 0;

      for (int i = 0; i < total; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected() && object instanceof JDRShape)
         {
            list.add((JDRShape)object);
            n++;
         }
      }

      if (n < 2) return;

      try
      {
         UndoableEdit edit = new PathIntersect(list);
         frame_.postEdit(edit);
      }
      catch (Throwable e)
      {
         // This shouldn't happen
         getResources().internalError(frame_, e);
      }
   }

   public void subtractPaths()
   {
      int total = paths.size();

      Vector<JDRShape> list;

      if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         list = new Vector<JDRShape>(total);
      }
      else
      {
         list = new Vector<JDRShape>();
      }

      int n = 0;

      for (int i = 0; i < total; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected() && object instanceof JDRShape)
         {
            list.add((JDRShape)object);
            n++;
         }
      }

      if (n < 2) return;

      try
      {
         UndoableEdit edit = new SubtractPaths(list);
         frame_.postEdit(edit);
      }
      catch (Throwable e)
      {
         // This shouldn't happen
         getResources().internalError(frame_, e);
      }
   }

   public void pathUnion()
   {
      int total = paths.size();

      Vector<JDRShape> list;

      if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         list = new Vector<JDRShape>(total);
      }
      else
      {
         list = new Vector<JDRShape>();
      }

      int n = 0;

      for (int i = 0; i < total; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected() && object instanceof JDRShape)
         {
            list.add((JDRShape)object);
            n++;
         }
      }

      if (n < 2) return;

      try
      {
         UndoableEdit edit = new PathUnion(list);
         frame_.postEdit(edit);
      }
      catch (Throwable e)
      {
         // This shouldn't happen
         getResources().internalError(frame_, e);
      }
   }

   public void group()
   {
      int total = paths.size();

      Vector<JDRCompleteObject> list;

      if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         list = new Vector<JDRCompleteObject>(total);
      }
      else
      {
         list = new Vector<JDRCompleteObject>();
      }

      int n = 0;

      for (int i = 0; i < total; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            list.add(object);
            n++;
         }
      }

      if (n < 2) return;

      UndoableEdit edit = new GroupObjects(list);
      frame_.postEdit(edit);
   }

   public void ungroup()
   {
      int response=JOptionPane.NO_OPTION;

      JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(this);
      UndoableEdit edit = null;

      for (int i = paths.size()-1; i >= 0; i--)
      {
         JDRCompleteObject object = paths.get(i);
         if (object.isSelected() && object instanceof JDRGroup)
         {
            if (object.getFlowFrame() != null && response==JOptionPane.NO_OPTION)
            {
               response = getResources().confirm(frame_,
                  getResources().getMessage("flowframe.confirm.ungroup"),
                  getResources().getMessage("flowframe.confirm.ungroup.title"));

               if (response != JOptionPane.YES_OPTION) return;
            }

            edit = new UngroupObjects(i);
            ce.addEdit(edit);
         }
      }

      if (edit != null)
      {
         ce.end();
         frame_.postEdit(ce);
      }
   }

   /**
    * Gets the nearest tic position in storage unit from the
    * given mouse position.
    * x0 and y0 are in terms of the mouse position relative to the
    * top corner of the canvas. Returns point in storage units.
    * If actual is not null, the actual point in storage units is
    * stored in that.
   * @param x0 mouse x
   * @param y0 mouse y
   */
   public Point2D getNearestStorageTicFromComponent(double x0, double y0,
    Point2D actual)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (actual != null)
      {
         actual.setLocation(
           cg.componentXToStorage(x0)-cg.getStorageOriginX(), 
           cg.componentYToStorage(y0)-cg.getStorageOriginY());
      }

      if (!frame_.getGridLock())
      {
         if (actual == null)
         {
            return new Point2D.Double(
              cg.componentXToStorage(x0)-cg.getStorageOriginX(), 
              cg.componentYToStorage(y0)-cg.getStorageOriginY());
         }
         else
         {
            return actual;
         }
      }

      Point2D p = frame_.getGrid().getClosestBpTic(
         cg.componentXToBp(x0)-cg.getBpOriginX(), 
         cg.componentYToBp(y0)-cg.getBpOriginY());

      if (cg.getStorageUnitID() == JDRUnit.BP)
      {
         return p;
      }

      double factor = cg.bpToStorage(1.0);

      p.setLocation(factor*p.getX(), factor*p.getY());

      return p;
   }

   public Point2D getNearestStorageTicFromComponent(double x0, double y0)
   {
      return getNearestStorageTicFromComponent(x0, y0, null);
   }


   public Point2D getNearestStorageTic(MouseEvent evt)
   {
      return getNearestStorageTicFromComponent(evt.getX(), evt.getY());
   }

   public Point2D getNearestStorageTic(MouseEvent evt, Point2D actual)
   {
      return getNearestStorageTicFromComponent(evt.getX(), evt.getY(),
       actual);
   }

   public Point2D getNearestStorageTicFromBp(double bpX, double bpY)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (!frame_.getGridLock())
      {
         if (cg.getStorageUnitID() == JDRUnit.BP)
         {
            return new Point2D.Double(bpX, bpY);
         }

         double factor = cg.bpToStorage(1.0);

         return new Point2D.Double(factor*bpX, factor*bpY);
      }

      Point2D p = frame_.getGrid().getClosestBpTic(bpX, bpY);

      if (cg.getStorageUnitID() == JDRUnit.BP)
      {
         return p;
      }

      double factor = cg.bpToStorage(1.0);

      p.setLocation(factor*p.getX(), factor*p.getY());

      return p;
   }

   public Point2D getNearestStorageTicFromStorage(double storageX,
      double storageY)
   {
      if (!frame_.getGridLock())
      {
         return new Point2D.Double(storageX, storageY);
      }

      CanvasGraphics cg = getCanvasGraphics();

      if (cg.getStorageUnitID() == JDRUnit.BP)
      {
         return frame_.getGrid().getClosestBpTic(storageX, storageY);
      }

      Point2D p = cg.getGrid().getClosestTic(storageX, storageY);

      return p;
   }

   private void drawGrid()
   {
      frame_.getGrid().drawGrid();
   }

   public void removeBackgroundImage()
   {
      backgroundImage = null;
   }

   public void setBackgroundImage()
   {
      setBackgroundImage(false);
   }

   public void setBackgroundImage(boolean forceUpdate)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (paths == null || 
          cg.getOptimize() == CanvasGraphics.OPTIMIZE_NONE)
      {
         backgroundImage = null;
         return;
      }

      JDRGrid grid = cg.getGrid();
      JDRUnit gridUnit = grid.getMainUnit();
      JDRUnit storageUnit = cg.getStorageUnit();
      double offsetX = gridUnit.toUnit(cg.getOriginX(), storageUnit);
      double offsetY = gridUnit.toUnit(cg.getOriginY(), storageUnit);

      double bpToCompXScale = cg.bpToComponentX(1.0);
      double bpToCompYScale = cg.bpToComponentY(1.0);
      double storageToCompXScale = cg.storageToComponentX(1.0);
      double storageToCompYScale = cg.storageToComponentY(1.0);

      int compPaperWidth =
         (int)Math.ceil(frame_.getBpPaperWidth()*bpToCompXScale);
      int compPaperHeight = 
         (int)Math.ceil(frame_.getBpPaperHeight()*bpToCompYScale);

      FlowFrame typeblock = paths.getFlowFrame();

      if ((frame_.showGrid()
           || typeblock != null
           || editedPath != null
           || editedDistortion != null))
      {
         double originX = 0;
         double originY = 0;

         if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
         {
            try
            {
               backgroundImage = 
                  new BufferedImage(compPaperWidth, compPaperHeight,
                         BufferedImage.TYPE_INT_ARGB);
            }
            catch (OutOfMemoryError e)
            {
               backgroundImage = null;
               getResources().getMessageSystem().getPublisher().publishMessages(
                 MessageInfo.createWarning(
                   getResources().getMessage("warning.no_background_image")));
               getCanvasGraphics().setOptimize(CanvasGraphics.OPTIMIZE_NONE);
               return;
            }
         }
         else
         {
            Rectangle visibleRect = getVisibleRect();

            if (visibleRect.getWidth() == 0 ||
                visibleRect.getHeight() == 0)
            {
               backgroundImage = null;
               return;
            }

            if (backgroundImage != null && !forceUpdate)
            {
               Rectangle r = 
                  new Rectangle((int)(backgroundImageX/bpToCompXScale),
                                (int)(backgroundImageY/bpToCompYScale),
                                backgroundImage.getWidth(),
                                backgroundImage.getHeight());

               if (r.contains(visibleRect))
               {
                  return;
               }
            }

            double w = visibleRect.getWidth()/bpToCompXScale;
            double h = visibleRect.getHeight()/bpToCompYScale;

            backgroundImageX = visibleRect.getX()/bpToCompXScale;
            backgroundImageY = visibleRect.getY()/bpToCompYScale;

            if (backgroundImageX - w < 0)
            {
               if (backgroundImageX + w <= compPaperHeight)
               {
                  w = 2*w;
               }
            }
            else
            {
               backgroundImageX -= w;

               if (backgroundImageX + w > compPaperWidth)
               {
                  w = 2*w;
               }
               else
               {
                  w = 2*w;
               }
            }

            if (backgroundImageY - h < 0)
            {
               if (backgroundImageY + h <= compPaperWidth)
               {
                  h = 2*h;
               }
            }
            else
            {
               backgroundImageY -= h;

               if (backgroundImageY + h > compPaperHeight)
               {
                  h = 2*h;
               }
               else
               {
                  h = 2*h;
               }
            }

            int buffImageW = (int)Math.ceil(w*bpToCompXScale);
            int buffImageH = (int)Math.ceil(h*bpToCompYScale);

            try
            {
               if (backgroundImage == null ||
                   (backgroundImage.getWidth() != buffImageW &&
                   backgroundImage.getHeight() != buffImageH))
               {
                  backgroundImage = 
                     new BufferedImage(
                            buffImageW,
                            buffImageH,
                            BufferedImage.TYPE_INT_ARGB);
               }
               else
               {
                  for (int i=0, n=backgroundImage.getWidth();i < n;i++)
                  {
                     for (int j=0,m=backgroundImage.getHeight();j < m;j++)
                     {
                        backgroundImage.setRGB(i, j, 0);
                     }
                  }
               }
            }
            catch (OutOfMemoryError e)
            {
               backgroundImage = null;

               getResources().debugMessage(
                  "Not enough memory to create background image.");

               return;
            }

            originX = backgroundImageX/bpToCompXScale;
            originY = backgroundImageY/bpToCompYScale;
         }

         Graphics2D g = backgroundImage.createGraphics();

         cg.setGraphicsDevice(g);
         cg.setResetTransform(g.getTransform());

         try
         {
            RenderingHints renderHints = 
               new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_OFF);

            renderHints.add(new RenderingHints(
                              RenderingHints.KEY_RENDERING,
                              RenderingHints.VALUE_RENDER_SPEED));

            g.setRenderingHints(renderHints);
            BasicStroke stroke = new BasicStroke(1);
            g.setStroke(stroke);

            g.translate(offsetX*storageToCompXScale-originX,
                        offsetY*storageToCompYScale-originY);

            if (frame_.showGrid())
            {
               cg.getGrid().drawGrid();
            }

            g.setPaint(marginColor);

            drawPrinterMargins(g, bpToCompXScale, bpToCompYScale);

            g.setPaint(typeblockColor);

            if (typeblock != null)
            {
               typeblock.draw(
                  new BBox(cg, offsetX, offsetY,
                           frame_.getStoragePaperWidth()+offsetX,
                           frame_.getStoragePaperHeight()+offsetY));
            }

            if (editedPath != null || editedDistortion != null)
            {
               g.scale(cg.storageToComponentX(1.0),
                       cg.storageToComponentY(1.0));

               if (typeblock != null && cg.isEvenPage())
               {
                  g.translate(typeblock.getEvenXShift(), 0.0);
               }

               g.setRenderingHints(frame_.getRenderingHints());

               JDRCompleteObject object;
               int n = paths.size();

               for (int i = 0; i < n; i++)
               {
                   object = paths.get(i);

                   if (isObjectVisible(object))
                   {
                      if (object != editedPath && object != editedDistortion)
                      {
                         object.draw(false);
                      }
                   }
               }
            }
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g.dispose();
         }
      }
      else
      {
         backgroundImage = null;
      }
   }

   public void save(File file)
   {
      if (file.getName().toLowerCase().endsWith(".ajr"))
      {
         saveAJR(file, JDRAJR.CURRENT_VERSION, false);
      }
      else
      {
         save(file, JDRAJR.CURRENT_VERSION, false);
      }
   }

   public void save(File file, float jdrversion, boolean exitAfter)
   {
      if (currentText != null) finishTextAndPostEdit();

      if (editedDistortion != null) setDistortState(false);

      if (jdrversion < JDRAJR.CURRENT_VERSION)
      {
         if (getResources().confirm(frame_,
                getResources().getMessage("warning.save.jdr",
                   jdrversion),
                getResources().getMessage("warning.title"))
             != JOptionPane.YES_OPTION)
         {
            return;
         }
      }

      SaveJdr sj = new SaveJdr(frame_, file,
              jdrversion, paths, frame_.getSaveJDRsettings(), exitAfter);

      sj.execute();
   }

   public void saveAJR(File file, float ajrversion, boolean exitAfter)
   {
      if (currentText != null) finishTextAndPostEdit();

      if (editedDistortion != null) setDistortState(false);

      if (ajrversion < JDRAJR.CURRENT_VERSION)
      {
         if (getResources().confirm(frame_,
             getResources().getMessage("warning.save.ajr",
                ajrversion),
             getResources().getMessage("warning.title"))
             != JOptionPane.YES_OPTION)
         {
            return;
         }
      }

      SaveAjr sa = new SaveAjr(frame_, file,
            ajrversion, paths, frame_.getSaveJDRsettings(), exitAfter);

      sa.execute();
   }

   public void savePGF(File file, ExportSettings exportSettings)
   {
      if (currentText != null) finishTextAndPostEdit();

      (new SavePgf(frame_, file, paths, exportSettings)).execute();
   }

   public void saveFlowFrame(File file, ExportSettings exportSettings)
   {
      if (currentText != null) finishTextAndPostEdit();

      (new SaveFlf(frame_, file, paths, exportSettings)).execute();
   }

   public void savePNG(File file, ExportSettings exportSettings)
   {
      if (currentText != null) finishTextAndPostEdit();

      if (exportSettings.useExternalProcess)
      {
         (new SavePng(frame_, file, paths, exportSettings)).execute();
      }
      else
      {
         (new SaveNoProcessPng(frame_, file, paths, exportSettings)).execute();
      }
   }

   public void saveEPS(File file, ExportSettings exportSettings)
   {
      if (currentText != null) finishTextAndPostEdit();

      if (exportSettings.useExternalProcess)
      {
         (new SaveEps(frame_, file, paths, exportSettings)).execute();
      }
      else
      {
         (new SaveNoProcessEps(frame_, file, paths, exportSettings)).execute();
      }
   }

   public void savePDF(File file, ExportSettings exportSettings)
   {
      if (currentText != null) finishTextAndPostEdit();

      (new SavePdf(frame_, file, paths, exportSettings)).execute();
   }

   public void saveSVG(File file, ExportSettings exportSettings)
   {
      if (currentText != null) finishTextAndPostEdit();

      if (exportSettings.useExternalProcess)
      {
         (new SaveSvg(frame_, file, paths, exportSettings)).execute();
      }
      else
      {
         (new SaveNoProcessSvg(frame_, file, paths, exportSettings)).execute();
      }
   }

   public void updateTextAreaBounds()
   {
      Graphics2D g = (Graphics2D)getGraphics();

      if (g != null)
      {
         g.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = paths.getCanvasGraphics();
         cg.setGraphicsDevice(g);

         try
         {
            paths.updateBounds();
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g.dispose();
         }
      }
   }

   public void load(File file)
   {
      String lc = file.getName().toLowerCase();

      if (lc.endsWith(".jdr"))
      {
         loadJDR(file);
      }
      else if (lc.endsWith(".ajr"))
      {
         loadAJR(file);
      }
      else
      {
         try
         {
            if (AJR.isAJR(file))
            {
               loadAJR(file);
            }
            else
            {
               loadJDR(file);
            }
         }
         catch (FileNotFoundException e)
         {
            getResources().error(frame_, 
              getResources().getMessage("error.io.not_exists", 
              file.toString()));
         }
         catch (Exception e)
         {
            getResources().error(frame_, e);
         }

      }
   }

   public void loadJDR(File file)
   {
      (new LoadJdr(frame_, file)).execute();
   }

   public void loadAJR(File file)
   {
      (new LoadAjr(frame_, file)).execute();
   }

   public boolean canDiscard()
   {
      if (editedPath != null) finishEditPath();

      if (currentPath != null)
      {
         getResources().error(frame_,
            getResources().getMessage("error.finish_or_discard"));
         return false;
      }

      if (!frame_.isSaved())
      {
         switch (getApplication().getDiscardDialogBox().display(frame_))
         {
            case DiscardDialogBox.CANCEL: return false;
            case DiscardDialogBox.DISCARD: return true;
         }
      }

      return true;
   }

   public void discard()
   {
      paths = new JDRGroup(getCanvasGraphics());
      String filename = getResources().getMessage("label.untitled");
      count++;
      if (count > 1) filename += count;
      frame_.setDefaultName(filename);
      frame_.setNewImageState(true);
   }

   class SetDocClass extends AbstractUndoableEdit
   {
      private String oldCls, newCls;

      public SetDocClass(String cls)
      {
         oldCls = getCanvasGraphics().getDocClass();
         newCls = cls;

         frame_.markAsModified();

         getCanvasGraphics().setDocClass(newCls);
      }

      public void undo() throws CannotUndoException
      {
         getCanvasGraphics().setDocClass(oldCls);
      }

      public void redo() throws CannotRedoException
      {
         getCanvasGraphics().setDocClass(newCls);
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.set_doc_cls");
      }
   }

   class SetNormalSize extends AbstractUndoableEdit
   {
      private double oldSize, newSize;

      public SetNormalSize(double size)
      {
         oldSize = getCanvasGraphics().getLaTeXNormalSize();
         newSize = size;

         getCanvasGraphics().setLaTeXNormalSize(newSize);

         frame_.markAsModified();
      }

      public void undo() throws CannotUndoException
      {
         getCanvasGraphics().setLaTeXNormalSize(oldSize);
      }

      public void redo() throws CannotRedoException
      {
         getCanvasGraphics().setLaTeXNormalSize(newSize);
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.set_normalsize");
      }
   }

   class SetTool extends AbstractUndoableEdit
   {
      private int _newTool, _oldTool;

      public SetTool(int tool)
      {
         _oldTool = frame_.currentTool();
         _newTool = tool;

         setTool(_oldTool, _newTool);
      }

      public void undo() throws CannotUndoException
      {
         if (((_newTool == ACTION_TEXT) ^ (_newTool == ACTION_MATH))
             && currentText != null)
         {
            abandonText();
         }

         setTool(_newTool, _oldTool);
         getApplication().setTool(_oldTool);
      }

      public void redo() throws CannotRedoException
      {
         if (((_oldTool == ACTION_TEXT) ^ (_oldTool == ACTION_MATH))
          && currentText != null)
         {
            abandonText();
         }

         setTool(_oldTool, _newTool);
         getApplication().setTool(_newTool);
      }

      public void setTool(int oldTool, int newTool)
      {
         if ((oldTool == ACTION_TEXT && newTool == ACTION_MATH)
           ||(oldTool == ACTION_MATH && newTool == ACTION_TEXT))
         {
            getCanvasGraphics().setTool(newTool);
            return;
         }

         setToolCursor(newTool);
   
         if (oldTool == newTool) return;
   
         dragScaleHotspot=BBox.HOTSPOT_NONE;
         dragScaleIndex = -1;
         dragScaleObject = null;
         dragScaleAnchor=null;
   
         if (currentPath != null)
         {
            if (oldTool == ACTION_RECTANGLE || 
                oldTool == ACTION_ELLIPSE)
            {
               abandonPath();
            }
            else
            {
               finishPath();
            }
         }
         else if ((oldTool == ACTION_TEXT || oldTool == ACTION_MATH)
               && currentText != null)
         {
            finishTextAndPostEdit();

            if (newTool == ACTION_TEXT || newTool == ACTION_MATH)
            {
               textField.setVisible(true);
               updateTextConstructionActions();
            }
         }
   
         anchor = null;
   
         getCanvasGraphics().setTool(newTool);

         updateGeneralActions(false);
         getApplication().updateActionButtons(false);
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.set_tool");
      }
   }

   class SetTypeblock extends AbstractUndoableEdit
   {
      private FlowFrame oldTypeblock, newTypeblock;
      private String string=getResources().getMessage("undo.set_typeblock");

      public SetTypeblock(double left, double right,
                          double top, double bottom,
                          double evenHshift)
      {
         oldTypeblock = paths.getFlowFrame();
         CanvasGraphics cg = getCanvasGraphics();

         newTypeblock = new FlowFrame(cg, FlowFrame.TYPEBLOCK);

         newTypeblock.setLeft(left);
         newTypeblock.setRight(right);
         newTypeblock.setTop(top);
         newTypeblock.setBottom(bottom);
         newTypeblock.setEvenXShift(evenHshift);

         paths.setFlowFrame(newTypeblock);

         frame_.markAsModified();
         setBackgroundImage();
         repaint();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.setFlowFrame(oldTypeblock);
         frame_.markAsModified();
         setBackgroundImage();
         repaint();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.setFlowFrame(newTypeblock);
         frame_.markAsModified();
         setBackgroundImage();
         repaint();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return string;
      }
   }

   class SetFlowFrame extends CanvasUndoableEdit
   {
      private FlowFrame oldFrame, newFrame;
      private JDRCompleteObject object_;
      private String string=getResources().getMessage("undo.set_frame");

      public SetFlowFrame(JDRCompleteObject object, FlowFrame f,
                          String presentation)
      {
         super(getFrame());

         string = presentation;
         object_ = object;
         oldFrame = object_.getFlowFrame();
         newFrame = f;

         object_.setFlowFrame(newFrame);
         frame_.updateTitle();

         setRefreshBounds(object_);
      }

      public SetFlowFrame(JDRCompleteObject object, FlowFrame f)
      {
         this(object, f, getResources().getMessage("undo.set_frame"));
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         object_.setFlowFrame(oldFrame);
         frame_.updateTitle();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         object_.setFlowFrame(newFrame);
         frame_.updateTitle();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return string;
      }
   }

   class SetDescription extends CanvasUndoableEdit
   {
      private JDRCompleteObject object_;
      private String oldDescription, newDescription;

      public SetDescription(JDRCompleteObject object, String description)
      {
         super(getFrame());

         object_ = object;

         oldDescription = object.getDescription();
         newDescription = description;

         object_.setDescription(newDescription);
         markAsModified();
      }

      public void undo() throws CannotUndoException
      {
         object_.setDescription(oldDescription);
         markAsModified();
      }

      public void redo() throws CannotRedoException
      {
         object_.setDescription(newDescription);
         markAsModified();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.set_description");
      }
   }

   class SetTag extends CanvasUndoableEdit
   {
      private JDRCompleteObject object_;
      private String oldTag, newTag;

      public SetTag(JDRCompleteObject object, String tag)
      {
         super(getFrame());

         object_ = object;

         oldTag = object.getTag();
         newTag = tag;

         object_.setTag(newTag);
         markAsModified();
      }

      public void undo() throws CannotUndoException
      {
         object_.setTag(oldTag);
         markAsModified();
      }

      public void redo() throws CannotRedoException
      {
         object_.setTag(newTag);
         markAsModified();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.set_tag");
      }
   }

   class SetAbsolutePages extends CanvasUndoableEdit
   {
      private boolean oldSetting, newSetting;

      public SetAbsolutePages(boolean setting)
      {
         super(getFrame());

         CanvasGraphics cg = getCanvasGraphics();

         oldSetting = cg.useAbsolutePages();
         newSetting = setting;

         cg.setUseAbsolutePages(newSetting);
         getApplication().setUseAbsolutePages(newSetting);

         markAsModified();
      }

      public void undo() throws CannotUndoException
      {
         CanvasGraphics cg = getCanvasGraphics();
         cg.setUseAbsolutePages(oldSetting);
         markAsModified();
      }

      public void redo() throws CannotRedoException
      {
         CanvasGraphics cg = getCanvasGraphics();
         cg.setUseAbsolutePages(newSetting);
         markAsModified();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.set_absolute_pages");
      }
   }

   class AddObject extends CanvasUndoableEdit
   {
      private int index_;
      private JDRCompleteObject object_;
      private String string=getResources().getMessage("undo.new_object");

      public AddObject(JDRCompleteObject newObject)
      {
         super(getFrame());

         object_ = newObject;
         paths.add(newObject);
         index_ = paths.size()-1;
         enableTools();

         setRefreshBounds(newObject);
      }

      public AddObject(JDRCompleteObject newObject, String presentationString)
      {
         this(newObject);
         string = presentationString;
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.remove(index_);
         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         object_.reset();
         paths.add(index_, object_);
         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return string;
      }
   }

   class RemoveObject extends CanvasUndoableEdit
   {
      private int index_;
      private JDRCompleteObject object_;

      public RemoveObject(JDRCompleteObject object, int i)
      {
         super(getFrame());

         object_ = object;
         index_ = i;
         paths.remove(index_);
         enableTools();

         setRefreshBounds(object);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.add(index_, object_);
         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.remove(index_);
         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.delete");
      }
   }

   class SplitText extends CanvasUndoableEdit
   {
      private int index_;
      private JDRCompleteObject newObject_, orgObject_;

      public SplitText(JDRCompleteObject object)
        throws EmptyGroupException,InvalidShapeException
      {
         super(getFrame());

         orgObject_ = object;
         index_ = object.getIndex();

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());
         getCanvasGraphics().setGraphicsDevice(g2);

         JDRGroup group_ = null;

         try
         {
            if (object instanceof JDRCompoundShape)
            {
                group_ = ((JDRCompoundShape)object).splitText();
            }
            else
            {
                group_ = object.getTextual().splitText();
            }
         }
         finally
         {
            getCanvasGraphics().setGraphicsDevice(null);
            g2.dispose();
         }

         if (group_.size() == 1)
         {
            newObject_ = group_.get(0);
         }
         else
         {
            newObject_ = group_;
         }

         if (group_.size() == 0)
         {
            throw new EmptyGroupException(getResources().getMessageDictionary());
         }

         paths.set(index_, newObject_);

         enableTools();

         BBox box = getRefreshBounds(orgObject_);
         mergeRefreshBounds(newObject_, box);

         setRefreshBounds(box);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, orgObject_);
         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, newObject_);
         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.splittext");
      }
   }

   class ConvertTextToPath extends CanvasUndoableEdit
   {
      private int index_;
      private JDRText text_;
      private JDRCompleteObject object_;

      public ConvertTextToPath(JDRText text)
         throws InvalidPathException,EmptyGroupException
      {
         super(getFrame());

         text_  = text;
         index_ = text.getIndex();
         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());
         getCanvasGraphics().setGraphicsDevice(g2);

         try
         {
            JDRGroup group_ = text_.convertToPath();

            if (group_.size() == 1)
            {
               object_ = group_.get(0);
            }
            else
            {
               object_ = group_;
            }
         }
         finally
         {
            getCanvasGraphics().setGraphicsDevice(null);
            g2.dispose();
         }

         paths.set(index_, object_);
         enableTools();

         setRefreshBounds(text, object_);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, text_);
         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, object_);
         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_path");
      }
   }

   class ConvertOutlineToPath extends CanvasUndoableEdit
   {
      private int index_;
      private JDRShape oldObject_, object_;

      public ConvertOutlineToPath(JDRShape path)
         throws InvalidShapeException
      {
         super(getFrame());

         oldObject_ = path;
         index_ = path.getIndex();

         object_ = path.outlineToPath();
         paths.set(index_, object_);
         object_.setSelected(true);

         enableTools();

         setRefreshBounds(path, object_);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldObject_);
         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, object_);
         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_path");
      }
   }

   class ConvertToFullPath extends CanvasUndoableEdit
   {
      private int index_;
      private JDRCompleteObject oldObject_, object_;

      public ConvertToFullPath(JDRCompoundShape path)
        throws InvalidShapeException
      {
         super(getFrame());

         oldObject_ = (JDRCompleteObject)path;
         index_ = oldObject_.getIndex();

         object_ = path.getFullPath();
         paths.set(index_, object_);
         object_.setSelected(true);

         enableTools();

         setRefreshBounds(path, object_);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldObject_);
         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, object_);
         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_path");
      }
   }

   class ConvertTextPathToPath extends CanvasUndoableEdit
   {
      private int index_;
      private JDRTextPath textPath_;
      private JDRCompleteObject object_;

      public ConvertTextPathToPath(JDRTextPath textPath)
         throws InvalidShapeException,EmptyGroupException
      {
         super(getFrame());

         textPath_  = textPath;
         index_ = textPath.getIndex();
         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         getCanvasGraphics().setGraphicsDevice(g2);

         try
         {
            JDRGroup group = textPath.splitText();

            for (int j = 0; j < group.size(); j++)
            {
               JDRGroup grp 
                  = ((JDRText)group.get(j)).convertToPath();
               group.set(j, grp.get(0));
            }

            if (group.size() == 1)
            {
               object_ = group.get(0);
            }
            else
            {
               object_ = group;
            }
         }
         finally
         {
            getCanvasGraphics().setGraphicsDevice(null);
            g2.dispose();
         }

         paths.set(index_, object_);
         enableTools();

         setRefreshBounds(textPath_, object_);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, textPath_);

         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, object_);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_path");
      }
   }

   class RemovePattern extends CanvasUndoableEdit
   {
      private int index_;
      private JDRShape shape;
      private JDRPattern pattern;

      private BBox oldBox, newBox;

      public RemovePattern(JDRPattern path)
      {
         super(getFrame());

         index_ = path.getIndex();

         pattern = path;
         shape = pattern.getUnderlyingShape();

         paths.set(index_, shape);

         enableTools();

         setRefreshBounds(shape, pattern);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         paths.set(index_, (JDRCompleteObject)pattern);
         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         paths.set(index_, shape);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.pattern.remove");
      }
   }

   class ConvertToPattern extends CanvasUndoableEdit
   {
      private int index_;
      private JDRShape oldShape;
      private JDRPattern newShape;

      public ConvertToPattern(JDRShape path, JDRPattern pattern)
      {
         super(getFrame());

         index_ = path.getIndex();
         oldShape = path;

         newShape = (JDRPattern)pattern.clone();

         BBox box = oldShape.getStorageBBox();

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getStorageOriginX(), -cg.getStorageOriginY());

         newShape.setPatternAnchor(box.getMidX(), box.getMidY());

         newShape.setUnderlyingShape(oldShape);

         newShape.setDefaultPatternAdjust();

         paths.set(index_, (JDRCompleteObject)newShape);

         enableTools();

         if (getCanvasGraphics().getStorageUnitID() != JDRUnit.BP)
         {
            box.scale(getCanvasGraphics().storageToBp(1.0));
         }

         mergeRefreshBounds(newShape, box);

         setRefreshBounds(box);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         paths.set(index_, oldShape);

         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         paths.set(index_, (JDRCompleteObject)newShape);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_pattern");
      }
   }

   class UpdatePattern extends CanvasUndoableEdit
   {
      private JDRCompoundShape newShape, oldShape;

      private int index_;

      public UpdatePattern(int index, JDRPattern pattern)
      {
         super(getFrame());

         index_ = index;

         oldShape = (JDRCompoundShape)paths.get(index);

         if (!oldShape.hasPattern())
         {
            throw new ClassCastException("Not a pattern");
         }

         if (oldShape instanceof JDRPattern)
         {
            JDRPattern oldPattern = (JDRPattern)oldShape;

            pattern.setUnderlyingShape(oldPattern.getUnderlyingShape());
            pattern.setPatternAnchor(oldPattern.getPatternAnchor());

            if (oldPattern.getPatternAdjust() != null
             && pattern.getClass() == oldShape.getClass())
            {
               pattern.setPatternAdjust(oldPattern.getPatternAdjust());
            }
            else
            {
               pattern.setDefaultPatternAdjust();
            }

            newShape = pattern;
         }
         else
         {
            newShape = (JDRCompoundShape)oldShape.clone();
            setUnderPattern(newShape, pattern);
         }

         paths.set(index_, (JDRCompleteObject)newShape);

         enableTools();

         setRefreshBounds(oldShape, newShape);
      }

      private void setUnderPattern(JDRCompoundShape shape,
         JDRPattern pattern)
      {
         JDRShape underlyingShape = shape.getUnderlyingShape();

         if (underlyingShape instanceof JDRPattern)
         {
            JDRPattern oldPattern = (JDRPattern)underlyingShape;

            pattern.setUnderlyingShape(oldPattern.getUnderlyingShape());
            pattern.setPatternAnchor(oldPattern.getPatternAnchor());

            if (oldPattern.getPatternAdjust() != null
             && pattern.getClass() == oldPattern.getClass())
            {
               pattern.setPatternAdjust(oldPattern.getPatternAdjust());
            }
            else
            {
               pattern.setDefaultPatternAdjust();
            }

            shape.setUnderlyingShape(pattern);

            return;
         }

         setUnderPattern((JDRCompoundShape)underlyingShape, pattern);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         paths.set(index_, oldShape);

         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         paths.set(index_, newShape);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.edit_pattern");
      }
   }

   class ConvertToTextPath extends CanvasUndoableEdit
   {
      private int lowerIndex, higherIndex;
      private JDRCompleteObject lowerObject;
      private JDRCompleteObject higherObject;
      private JDRTextPath textPath;
      private JDRStroke oldStroke, newStroke;
      private JDRShape oldPath;
      private JDRPaint oldLinePaint, oldFillPaint, oldTextPaint;

      public ConvertToTextPath(JDRShape path, JDRText text)
      {
         super(getFrame());

         int pathIndex = path.getIndex();
         int textIndex = text.getIndex();

         if (pathIndex < textIndex)
         {
            lowerIndex = pathIndex;
            lowerObject = path;
            higherIndex = textIndex;
            higherObject = text;
         }
         else
         {
            lowerIndex = textIndex;
            lowerObject = text;
            higherIndex = pathIndex;
            higherObject = path;
         }

         oldStroke = path.getStroke();
         oldPath = path;
         oldLinePaint = path.getLinePaint();
         oldFillPaint = path.getFillPaint();
         oldTextPaint = text.getTextPaint();

         BBox box = getRefreshBounds(path);
         mergeRefreshBounds((JDRCompleteObject)text, box);

         textPath = new JDRTextPath(path, text);
         textPath.setSelected(true);

         newStroke = textPath.getStroke();

         paths.remove(higherIndex);
         paths.set(lowerIndex, textPath);

         enableTools();

         mergeRefreshBounds((JDRCompleteObject)textPath, box);

         setRefreshBounds(box);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         oldPath.setStroke(oldStroke);
         oldPath.setLinePaint(oldLinePaint);
         oldPath.setFillPaint(oldFillPaint);

         paths.add(higherIndex, higherObject);
         paths.set(lowerIndex, lowerObject);

         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         textPath.setStroke(newStroke);
         textPath.setTextPaint(oldTextPaint);

         paths.remove(higherIndex);
         paths.set(lowerIndex, textPath);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_textpath");
      }
   }

   class Separate extends CanvasUndoableEdit
   {
      private int index_;
      private JDRGroup group;
      private JDRCompoundShape original;

      public Separate(JDRCompoundShape shape)
        throws InvalidShapeException
      {
         super(getFrame());

         original = shape;
         index_ = shape.getIndex();

         Graphics2D g = (Graphics2D)getGraphics();

         try
         {
            getCanvasGraphics().setGraphicsDevice(g);
            group = shape.separate();
         }
         finally
         {
            getCanvasGraphics().setGraphicsDevice(null);
            g.dispose();
         }

         group.setSelected(true);

         paths.set(index_, group);

         enableTools();

         setRefreshBounds(shape, group);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         paths.set(index_, original);

         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         paths.set(index_, group);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}

      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.separate");
      }
   }

   class MergePaths extends CanvasUndoableEdit
   {
      private int index_;
      private int[] indexArray;
      private Vector<JDRShape> list_;
      private JDRShape newPath;
      private int n;

      public MergePaths(Vector<JDRShape> list) 
         throws InvalidShapeException
      {
         super(getFrame());

         list_  = list;
         n      = list.size();

         indexArray = new int[n];

         for (int i = 0; i < n; i++)
         {
            JDRShape object = list.get(i);

            indexArray[i] = object.getIndex();
         }

         Arrays.sort(indexArray);

         index_ = indexArray[0];

         newPath = paths.mergePaths(indexArray);

         enableTools();

         setRefreshBounds(newPath);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.remove(index_);

         for (int i = 0; i < n; i++)
         {
            paths.add(indexArray[i], list_.get(i));
         }

         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         for (int i = n-1; i >= 0; i--)
         {
            paths.remove(indexArray[i]);
         }

         paths.add(index_, newPath);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.merge_paths");
      }
   }

   class XORPaths extends CanvasUndoableEdit
   {
      private int index_;
      private int[] indexArray;
      private Vector<JDRShape> list_;
      private JDRShape newPath;
      private int n;

      public XORPaths(Vector<JDRShape> list) 
         throws InvalidShapeException
      {
         super(getFrame());

         list_  = list;
         n      = list.size();

         indexArray = new int[n];

         BBox box = null;

         for (int i = 0; i < n; i++)
         {
            JDRShape object = list.get(i);

            indexArray[i] = object.getIndex();

            if (box == null)
            {
               box = object.getBpBBox();
            }
            else
            {
               object.mergeBpBBox(box);
            }
         }

         Arrays.sort(indexArray);

         index_ = indexArray[0];

         newPath = list.get(0).exclusiveOr(list.get(1));

         for (int i = 2; i < n; i++)
         {
            newPath = newPath.exclusiveOr(list.get(i));
         }

         for (int i = n-1; i >= 0; i--)
         {
            paths.remove(indexArray[i]);
         }

         paths.add(index_, newPath);

         enableTools();

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         setRefreshBounds(box);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.remove(index_);

         for (int i = 0; i < n; i++)
         {
            paths.add(indexArray[i], list_.get(i));
         }

         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         for (int i = n-1; i >= 0; i--)
         {
            paths.remove(indexArray[i]);
         }

         paths.add(index_, newPath);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.xor_paths");
      }
   }

   class PathIntersect extends CanvasUndoableEdit
   {
      private int index_;
      private int[] indexArray;
      private Vector<JDRShape> list_;
      private JDRShape newPath;
      private int n;

      public PathIntersect(Vector<JDRShape> list) 
         throws InvalidShapeException
      {
         super(getFrame());

         list_  = list;
         n      = list.size();

         indexArray = new int[n];

         BBox box = null;

         for (int i = 0; i < n; i++)
         {
            JDRShape object = list.get(i);

            indexArray[i] = object.getIndex();

            if (box == null)
            {
               box = object.getBpBBox();
            }
            else
            {
               object.mergeBpBBox(box);
            }
         }

         Arrays.sort(indexArray);

         index_ = indexArray[0];

         newPath = list.get(0).intersect(list.get(1));

         for (int i = 2; i < n; i++)
         {
            newPath = newPath.intersect(list.get(i));
         }

         for (int i = n-1; i >= 0; i--)
         {
            paths.remove(indexArray[i]);
         }

         paths.add(index_, newPath);

         enableTools();

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         setRefreshBounds(box);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.remove(index_);

         for (int i = 0; i < n; i++)
         {
            paths.add(indexArray[i], list_.get(i));
         }

         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         for (int i = n-1; i >= 0; i--)
         {
            paths.remove(indexArray[i]);
         }

         paths.add(index_, newPath);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.path_intersect");
      }
   }

   class SubtractPaths extends CanvasUndoableEdit
   {
      private int index_;
      private int[] indexArray;
      private Vector<JDRShape> list_;
      private JDRShape newPath;
      private int n;

      public SubtractPaths(Vector<JDRShape> list) 
         throws InvalidShapeException
      {
         super(getFrame());

         list_  = list;
         n      = list.size();

         indexArray = new int[n];

         BBox box = null;

         for (int i = 0; i < n; i++)
         {
            JDRShape object = list.get(i);

            indexArray[i] = object.getIndex();

            if (box == null)
            {
               box = object.getBpBBox();
            }
            else
            {
               object.mergeBpBBox(box);
            }
         }

         Arrays.sort(indexArray);

         index_ = indexArray[0];

         newPath = list.get(0).subtract(list.get(1));

         for (int i = 2; i < n; i++)
         {
            newPath = newPath.subtract(list.get(i));
         }

         for (int i = n-1; i >= 0; i--)
         {
            paths.remove(indexArray[i]);
         }

         paths.add(index_, newPath);

         enableTools();

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         setRefreshBounds(box);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.remove(index_);

         for (int i = 0; i < n; i++)
         {
            paths.add(indexArray[i], list_.get(i));
         }

         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         for (int i = n-1; i >= 0; i--)
         {
            paths.remove(indexArray[i]);
         }

         paths.add(index_, newPath);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.subtract_paths");
      }
   }

   class PathUnion extends CanvasUndoableEdit
   {
      private int index_;
      private int[] indexArray;
      private Vector<JDRShape> list_;
      private JDRShape newPath;
      private int n;

      public PathUnion(Vector<JDRShape> list) 
         throws InvalidShapeException
      {
         super(getFrame());

         list_  = list;
         n      = list.size();

         indexArray = new int[n];

         for (int i = 0; i < n; i++)
         {
            JDRShape object = list.get(i);

            indexArray[i] = object.getIndex();
         }

         Arrays.sort(indexArray);

         index_ = indexArray[0];

         newPath = list.get(0).pathUnion(list.get(1));

         for (int i = 2; i < n; i++)
         {
            newPath = newPath.pathUnion(list.get(i));
         }

         for (int i = n-1; i >= 0; i--)
         {
            paths.remove(indexArray[i]);
         }

         paths.add(index_, newPath);

         enableTools();

         setRefreshBounds(newPath);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.remove(index_);

         for (int i = 0; i < n; i++)
         {
            paths.add(indexArray[i], list_.get(i));
         }

         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         for (int i = n-1; i >= 0; i--)
         {
            paths.remove(indexArray[i]);
         }

         paths.add(index_, newPath);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.path_union");
      }
   }

   class GroupObjects extends CanvasUndoableEdit
   {
      private int grpIndex_=0;
      private Vector<JDRCompleteObject> list_;
      private JDRGroup group_;
      private int[] indices;
      private int n;

      public GroupObjects(Vector<JDRCompleteObject> list)
      {
         super(getFrame());

         // list should be sorted before passed to here
         list_  = list;
         n      = list.size();

         indices = new int[n];

         for (int i = 0; i < n; i++)
         {
            indices[i] = list_.get(i).getIndex();
         }

// TODO remove is causing array index out of bounds. Need to check
// this
         Arrays.sort(indices);

         grpIndex_ = indices[0];

         group_ = new JDRGroup(getCanvasGraphics(), n);

         BBox box = null;

         for (int i = 0; i < n; i++)
         {
            JDRCompleteObject object = list_.get(i);
            group_.add(object);

            if (box == null)
            {
               box = object.getBpBBox();
            }
            else
            {
               object.mergeBpBBox(box);
            }
         }

         for (int i = n-1; i >= 0; i--)
         {
            paths.remove(indices[i]);
         }

         paths.add(grpIndex_, group_);
         group_.setSelected(true);

         enableTools();

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         setRefreshBounds(box);
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         paths.remove(grpIndex_);

         for (int i = 0; i < n; i++)
         {
            JDRCompleteObject object = (JDRCompleteObject)list_.get(i);
            paths.add(indices[i], object);
         }

         enableTools();

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         for (int i = n-1; i >= 0; i--)
         {
            paths.remove(indices[i]);
         }

         paths.add(grpIndex_, group_);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.group");
      }
   }

   class UngroupObjects extends CanvasUndoableEdit
   {
      private int grpIndex_;
      private Vector<JDRCompleteObject> list_;
      private JDRGroup group_;
      private int[] indices;
      private int n;

      public UngroupObjects(int index)
      {
         super(getFrame());

         grpIndex_ = index;

         group_ = (JDRGroup)paths.get(grpIndex_);

         n = group_.size();

         list_ = new Vector<JDRCompleteObject>(n);
         indices = new int[n];

         paths.remove(grpIndex_);

         BBox box = null;

         for (int i = 0; i < n; i++)
         {
            JDRCompleteObject object = group_.get(i);
            list_.add(i, object);
            indices[i] = grpIndex_+i;
            paths.add(indices[i], object);

            if (box == null)
            {
               box = object.getBpBBox();
            }
            else
            {
               object.mergeBpBBox(box);
            }
         }

         enableTools();

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         paths.remove(grpIndex_);
         for (int i = 0; i < n; i++)
         {
            JDRCompleteObject object = (JDRCompleteObject)list_.get(i);
            paths.add(indices[i], object);
         }

         enableTools();

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         for (int i = n-1; i >= 0; i--)
         {
            paths.remove(indices[i]);
         }
         paths.add(grpIndex_, group_);

         enableTools();

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.ungroup");
      }
   }

   class MoveToFront extends CanvasUndoableEdit
   {
      private JDRCompleteObject object_;
      private int oldIndex;

      public MoveToFront(JDRCompleteObject object)
      {
         super(getFrame());

         object_ = object;
         oldIndex = object_.getIndex();

         paths.moveToFront(object_);

         setRefreshBounds(object_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         paths.moveToFront(object_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.remove(object_);
         paths.add(oldIndex, object_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.move_to_front");
      }
   }

   class MoveToBack extends CanvasUndoableEdit
   {
      private JDRCompleteObject object_;
      private int oldIndex;

      public MoveToBack(JDRCompleteObject object)
      {
         super(getFrame());

         object_ = object;
         oldIndex = object_.getIndex();

         paths.moveToBack(object_);

         setRefreshBounds(object_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         paths.moveToBack(object_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.remove(object_);
         paths.add(oldIndex, object_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.move_to_back");
      }
   }

   class MoveUp extends CanvasUndoableEdit
   {
      private int oldIndex, newIndex;

      public MoveUp(int index)
      {
         super(getFrame());

         oldIndex = index;
         newIndex = oldIndex+1;

         setRefreshBounds(paths.get(oldIndex));

         paths.swap(oldIndex, newIndex);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         paths.swap(oldIndex, newIndex);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         paths.swap(oldIndex, newIndex);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.move_up", newIndex);
      }
   }

   class MoveDown extends CanvasUndoableEdit
   {
      private int oldIndex, newIndex;

      public MoveDown(int index)
      {
         super(getFrame());

         oldIndex = index;
         newIndex = oldIndex-1;

         setRefreshBounds(paths.get(oldIndex));

         paths.swap(oldIndex, newIndex);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         paths.swap(oldIndex, newIndex);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         paths.swap(oldIndex, newIndex);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.move_down", newIndex);
      }
   }

   class SelectDistortionControl extends CanvasUndoableEdit
   {
      private Point2D oldPt, newPt;
      private String presentation_
         = getResources().getMessage("undo.select_point");

      public SelectDistortionControl(Point2D currentPos)
         throws NullPointerException
      {
         super(getFrame());

         oldPt = editedDistortion.getSelectedPoint();
         editedDistortion.selectControl(currentPos.getX(), currentPos.getY());
         newPt = editedDistortion.getSelectedPoint();

         if (newPt == null)
         {
            throw new NullPointerException();
         }

         setRefreshBounds(editedDistortion);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         editedDistortion.setSelectedPoint(newPt);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         editedDistortion.setSelectedPoint(oldPt);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return presentation_;
      }
   }

   class DeselectDistortionControl extends CanvasUndoableEdit
   {
      private Point2D oldPt;
      private String presentation_
         = getResources().getMessage("undo.deselect");

      public DeselectDistortionControl()
      {
         super(getFrame());

         oldPt = editedDistortion.getSelectedPoint();
         editedDistortion.setSelectedPoint(null);

         setRefreshBounds(editedDistortion);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         editedDistortion.setSelectedPoint(null);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         editedDistortion.setSelectedPoint(oldPt);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return presentation_;
      }
   }

   class MoveDistortionControl extends CanvasUndoableEdit
   {
      private double oldX, oldY, newX, newY, oldNextX, oldNextY,
         newNextX, newNextY;
      private Point2D point, nextPoint;

      public MoveDistortionControl(double dx, double dy, boolean adjustNext)
      {
         super(getFrame());

         point = editedDistortion.getSelectedPoint();

         oldX = point.getX();
         oldY = point.getY();

         newX = point.getX()+dx;
         newY = point.getY()+dy;

         if (adjustNext)
         {
            int idx = (editedDistortion.getDistortionIndex(point)+1)
                    % editedDistortion.getNumDistortionPoints();

            nextPoint = editedDistortion.getDistortionPoint(idx);

            oldNextX = nextPoint.getX();
            oldNextY = nextPoint.getY();

            JDRLine line = new JDRLine(getCanvasGraphics(),
               oldX, oldY, oldNextX, oldNextY);
            line.rotate(
               new Point2D.Double(0.5*(oldNextX+oldX), 0.5*(oldNextY+oldY)),
               0.5*Math.PI);

            Point2D reflect = JDRPoint.getReflection(newX, newY, line);
            newNextX = reflect.getX();
            newNextY = reflect.getY();
         }
         else
         {
            nextPoint = null;
         }

         BBox box = editedDistortion.getBpControlBBox();
         box.merge(editedDistortion.getUnderlyingObject()
                     .getBpDistortionBounds());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         point.setLocation(newX, newY);

         if (nextPoint != null)
         {
            nextPoint.setLocation(newNextX, newNextY);
         }

         editedDistortion.updateDistortion();

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         point.setLocation(newX, newY);

         if (nextPoint != null)
         {
            nextPoint.setLocation(newNextX, newNextY);
         }

         editedDistortion.updateDistortion();

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         point.setLocation(oldX, oldY);

         if (nextPoint != null)
         {
            nextPoint.setLocation(oldNextX, oldNextY);
         }

         editedDistortion.updateDistortion();

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.move_point");
      }
   }

   class DistortReset extends CanvasUndoableEdit
   {
      private Point2D[] oldDistortion, newDistortion;

      public DistortReset()
      {
         super(getFrame());

         int n = editedDistortion.getNumDistortionPoints();
         oldDistortion = new Point2D[n];
         newDistortion = new Point2D[n];

         for (int i = 0; i < n; i++)
         {
            oldDistortion[i] =
               (Point2D)editedDistortion.getDistortionPoint(i).clone();
         }

         setRefreshBounds(editedDistortion);

         editedDistortion.resetDistortion();

         for (int i = 0; i < n; i++)
         {
            newDistortion[i] = editedDistortion.getDistortionPoint(i);
         }
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         editedDistortion.setDistortionBounds(newDistortion);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         editedDistortion.setDistortionBounds(oldDistortion);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.reset.distort");
      }
   }

   class DistortObject extends CanvasUndoableEdit
   {
      private JDRDistortable orgObject;
      private JDRDistortShape distObject;
      
      public DistortObject(JDRDistortable object)
      {
         super(getFrame());

         editedDistortionIndex = ((JDRCompleteObject)object).getIndex();

         orgObject = object;
         distObject = new JDRDistortShape(object);

         distObject.setEditMode(true);
         distObject.setSelected(true);
         paths.set(editedDistortionIndex, distObject);
         editedDistortion = distObject;

         setRefreshBounds((JDRCompleteObject)orgObject, 
            (JDRCompleteObject)distObject);
         setBackgroundImage();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         distObject.setEditMode(true);
         paths.set(editedDistortionIndex, (JDRCompleteObject)distObject);
         editedDistortion = distObject;
         setDistortObjectButton(true);

         setBackgroundImage();
         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         distObject.setEditMode(false);
         paths.set(editedDistortionIndex, (JDRCompleteObject)orgObject);
         editedDistortion = null;
         setDistortObjectButton(false);

         setBackgroundImage();
         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.distort");
      }
   }

   class FinishDistortObject extends CanvasUndoableEdit
   {
      private JDRDistortShape distObject;
      private JDRCompleteObject newObject;

      public FinishDistortObject()
      {
         super(getFrame());

         BBox box = editedDistortion.getBpControlBBox();
         box.merge(editedDistortion.getUnderlyingObject()
           .getBpDistortionBounds());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         distObject = editedDistortion;
         editedDistortion.setEditMode(false);
         editedDistortion = null;

         newObject = distObject.getDistortedObject();
         paths.set(editedDistortionIndex, newObject);

         setRefreshBounds(box);
         setBackgroundImage();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         editedDistortion.setEditMode(false);
         editedDistortion = null;
         paths.set(editedDistortionIndex, newObject);
         setDistortObjectButton(false);

         setBackgroundImage();
         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         editedDistortion = distObject;
         editedDistortion.setEditMode(true);
         setDistortObjectButton(true);
         paths.set(editedDistortionIndex, distObject);

         setBackgroundImage();
         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.distort_end");
      }
   }

   class ConvertToLine extends CanvasUndoableEdit
   {
      private JDRPathSegment oldSegment_, newSegment_;
      private int index_;
      private JDRShape path_;
      private JDRPoint oldPt_, newPt_;
      private boolean startAnchor, endAnchor;

      public ConvertToLine(JDRShape path, JDRPathSegment segment)
      throws InvalidPathException
      {
         super(getFrame());

         index_ = path.getIndex(segment);
         path_ = path;
         oldPt_ = getSelectedStoragePoint();

         oldSegment_ = segment;

         startAnchor = oldSegment_.getStart().isAnchored();
         endAnchor = oldSegment_.getEnd().isAnchored();

         newSegment_ = segment.convertToLine();

         path_.convertSegment(index_, newSegment_);

         newPt_ = oldPt_;

         if (oldSegment_ instanceof JDRBezier)
         {
            if (oldPt_ == ((JDRBezier)oldSegment_).getControl1())
            {
               newPt_ = oldSegment_.getStart();
            }
            else if (oldPt_ == ((JDRBezier)oldSegment_).getControl2())
            {
               newPt_ = oldSegment_.getEnd();
            }
         }

         editedPath.selectControl(newPt_);

         BBox box = oldSegment_.getBpControlBBox();
         box.merge(newSegment_.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         mergeRefreshBounds(path_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         try
         {
            path_.convertSegment(index_, newSegment_);
         }
         catch (InvalidPathException e)
         {
            getResources().debug(e);
            throw new CannotRedoException();
         }

         newSegment_.getStart().setAnchored(false);
         newSegment_.getEnd().setAnchored(false);

         editedPath.selectControl(newPt_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         try
         {
            path_.convertSegment(index_, oldSegment_);
         }
         catch (InvalidPathException e)
         {
            getResources().debug(e);
            throw new CannotUndoException();
         }

         oldSegment_.getStart().setAnchored(startAnchor);
         oldSegment_.getEnd().setAnchored(endAnchor);

         editedPath.selectControl(oldPt_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_line");
      }
   }

   class ConvertToSegment extends CanvasUndoableEdit
   {
      private JDRPathSegment oldSegment_, newSegment_;
      private int index_;
      private JDRShape path_;
      private JDRPoint oldPt_=null, newPt_=null;
      private boolean startAnchor, endAnchor;

      public ConvertToSegment(JDRShape path, JDRPathSegment segment)
      throws InvalidPathException
      {
         super(getFrame());

         index_ = path.getIndex(segment);
         path_ = path;
         oldPt_ = getSelectedStoragePoint();

         oldSegment_ = segment;

         startAnchor = oldSegment_.getStart().isAnchored();
         endAnchor = oldSegment_.getEnd().isAnchored();

         newSegment_ = segment.convertToSegment();

         path_.convertSegment(index_, newSegment_);

         newPt_ = oldPt_;

         if (oldSegment_ instanceof JDRBezier)
         {
            if (oldPt_ == ((JDRBezier)oldSegment_).getControl1())
            {
               newPt_ = oldSegment_.getStart();
            }
            else if (oldPt_ == ((JDRBezier)oldSegment_).getControl2())
            {
               newPt_ = oldSegment_.getEnd();
            }
         }

         editedPath.selectControl(newPt_);

         BBox box = oldSegment_.getBpControlBBox();
         box.merge(newSegment_.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         mergeRefreshBounds(path_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         try
         {
            path_.convertSegment(index_, newSegment_);
         }
         catch (InvalidPathException e)
         {
            getResources().debug(e);
            throw new CannotRedoException();
         }

         newSegment_.getStart().setAnchored(false);
         newSegment_.getEnd().setAnchored(false);

         editedPath.selectControl(newPt_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         try
         {
            path_.convertSegment(index_, oldSegment_);
         }
         catch (InvalidPathException e)
         {
            getResources().debug(e);
            throw new CannotUndoException();
         }

         oldSegment_.getStart().setAnchored(startAnchor);
         oldSegment_.getEnd().setAnchored(endAnchor);

         editedPath.selectControl(oldPt_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_move");
      }
   }

   class ConvertToClosingMove extends CanvasUndoableEdit
   {
      private JDRPathSegment oldSegment_, newSegment_;
      private int index_;
      private JDRShape path_;
      private JDRPoint oldPt_=null, newPt_=null;
      private boolean startAnchor, endAnchor;

      public ConvertToClosingMove(JDRShape path, JDRPathSegment segment)
      throws InvalidPathException
      {
         super(getFrame());

         index_ = path.getIndex(segment);
         path_ = path;
         oldPt_ = getSelectedStoragePoint();

         oldSegment_ = segment;

         startAnchor = oldSegment_.getStart().isAnchored();
         endAnchor = oldSegment_.getEnd().isAnchored();

         newSegment_ = new JDRClosingMove(
           segment.getStart(), segment.getEnd(),
           path.getBaseUnderlyingPath(), index_);

         path_.convertSegment(index_, newSegment_);

         newPt_ = oldPt_;

         if (oldSegment_ instanceof JDRBezier)
         {
            if (oldPt_ == ((JDRBezier)oldSegment_).getControl1())
            {
               newPt_ = oldSegment_.getStart();
            }
            else if (oldPt_ == ((JDRBezier)oldSegment_).getControl2())
            {
               newPt_ = oldSegment_.getEnd();
            }
         }

         editedPath.selectControl(newPt_);

         BBox box = oldSegment_.getBpControlBBox();
         box.merge(newSegment_.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         mergeRefreshBounds(path_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         try
         {
            path_.convertSegment(index_, newSegment_);
         }
         catch (InvalidPathException e)
         {
            getResources().debug(e);
            throw new CannotRedoException();
         }

         newSegment_.getStart().setAnchored(false);
         newSegment_.getEnd().setAnchored(false);

         editedPath.selectControl(newPt_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         try
         {
            path_.convertSegment(index_, oldSegment_);
         }
         catch (InvalidPathException e)
         {
            getResources().debug(e);
            throw new CannotUndoException();
         }

         oldSegment_.getStart().setAnchored(startAnchor);
         oldSegment_.getEnd().setAnchored(endAnchor);

         editedPath.selectControl(oldPt_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.close_sub_path");
      }
   }

   class ConvertToCurve extends CanvasUndoableEdit
   {
      private JDRPathSegment oldSegment_, newSegment_;
      private int index_;
      private JDRShape path_;
      private JDRPoint pt_=null;

      public ConvertToCurve(JDRShape path, JDRPathSegment segment)
      throws InvalidPathException
      {
         super(getFrame());

         index_ = path.getIndex(segment);
         path_ = path;

         pt_ = getSelectedStoragePoint();
         oldSegment_ = segment;
         newSegment_ = segment.convertToBezier();

         path_.convertSegment(index_, newSegment_);

         editedPath.selectControl(pt_);

         BBox box = oldSegment_.getBpControlBBox();
         box.merge(newSegment_.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         mergeRefreshBounds(path_, box);

         setRefreshBounds(box);
         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         try
         {
            path_.convertSegment(index_, newSegment_);
         }
         catch (InvalidPathException e)
         {
            getResources().debug(e);
            throw new CannotRedoException();
         }

         editedPath.selectControl(pt_);
         repaintRegion();
         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         try
         {
            path_.convertSegment(index_, oldSegment_);
         }
         catch (InvalidPathException e)
         {
            getResources().debug(e);
            throw new CannotUndoException();
         }

         editedPath.selectControl(pt_);
         repaintRegion();
         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_curve");
      }
   }

   class AppendSegment extends CanvasUndoableEdit
   {
      private JDRPath path_;
      private JDRSegment newSegment=null;
      private BBox bounds;

      public AppendSegment(JDRPath path, JDRSegment segment)
      throws InvalidPathException
      {
         super(getFrame());

         path_ = path;
         newSegment = segment;

         path_.add(newSegment);

         bounds = newSegment.getBpControlBBox();

         CanvasGraphics cg = getCanvasGraphics();
         bounds.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         setRefreshBounds(bounds);
         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         try
         {
            path_.add(newSegment);
         }
         catch (InvalidPathException e)
         {
            getResources().debug(e);
            throw new CannotRedoException();
         }

         bounds = newSegment.getBpControlBBox();
         repaintRegion();
         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         path_.removeLastSegment();

         repaintRegion();
         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.add_point");
      }
   }

   class AddPoint extends CanvasUndoableEdit
   {
      private int index_;
      private JDRShape path_, oldPath_;
      private JDRPoint oldPt=null, newPt=null;

      public AddPoint(JDRShape path)
      {
         super(getFrame());

         index_ = path.getIndex();
         path_ = (JDRShape)path.clone();
         oldPath_ = path;
         oldPt = getSelectedStoragePoint();
         int ctrIdx = editedPath.getControlIndex(oldPt);

         paths.set(index_, path_);
         editedPath.setEditMode(false);
         editedPath = path_;
         editedPath.selectControl(ctrIdx);

         newPt = path_.addPoint();

         editedPath.selectControl(newPt);

         setRefreshControlBounds(path_, oldPath_);
         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, path_);
         editedPath.setEditMode(false);
         editedPath = path_;
         editedPath.selectControl(newPt);

         repaintRegion();
         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldPath_);
         editedPath.setEditMode(false);
         editedPath = oldPath_;
         editedPath.selectControl(oldPt);

         repaintRegion();
         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.add_point");
      }
   }

   class DeletePoint extends CanvasUndoableEdit
   {
      private int index_;
      private JDRShape path_, oldPath_;
      private boolean pathRemoved_=false;
      private int oldPtIndex_, newPtIndex_;

      public DeletePoint(JDRShape path) throws InvalidPathException
      {
         super(getFrame());

         index_ = path.getIndex();
         oldPath_ = path;
         path = (JDRShape)path.clone();
         paths.set(index_, path);

         editedPath = path;

         JDRPoint newPt = editedPath.getSelectedSegment().getEnd();
         oldPtIndex_ = editedPath.getSelectedControlIndex();

         JDRPathSegment lastSegment = editedPath.getLastSegment();

         if (editedPath.getSelectedSegment() == lastSegment
           && getSelectedStoragePoint() != lastSegment.getStart())
         {
            newPt = editedPath.getSelectedSegment().getStart();
         }

         path.removeSelectedSegment();

         newPtIndex_ = editedPath.getSelectedControlIndex();

         if (newPtIndex_ == -1 || (path.size()==1 && path.isClosed()))
         {
            paths.remove(index_);
            pathRemoved_ = true;
            if (editedPath == path)
            {
               editedPath = null;
               finishEditingPath();
               enableTools();
            }
         }
         else
         {
            editedPath.selectControl(newPtIndex_);
            updateEditPathActions();
         }

         path_ = path;

         setRefreshControlBounds(oldPath_, path_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         oldPath_ = (JDRPath)paths.get(index_);

         if (pathRemoved_)
         {
            finishEditingPath();
            paths.remove(index_);
         }
         else
         {
            paths.set(index_, path_);
            editedPath = path_;
            path_.selectControl(newPtIndex_);
            updateEditPathActions();
         }

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         if (pathRemoved_)
         {
            paths.add(index_, oldPath_);
         }
         else
         {
            path_ = (JDRPath)paths.get(index_);
            paths.set(index_, oldPath_);
         }

         editedPath = oldPath_;
         editedPath.selectControl(oldPtIndex_);
         updateEditPathActions();
         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.delete_point");
      }
   }

   class BreakPath extends CanvasUndoableEdit
   {
      private int index_;
      private JDRShape oldPath, newPath1, newPath2;
      private int controlIndex;

      public BreakPath(JDRShape path)
         throws InvalidShapeException
      {
         super(getFrame());

         index_ = path.getIndex();
         oldPath = path;
         path = (JDRShape)oldPath.clone();
         controlIndex = editedPath.getSelectedControlIndex();
         editedPath = path;
         editedPath.selectControl(controlIndex);

         newPath2 = path.breakPath();

         if (newPath2.size() == 0)
         {
            editedPath = oldPath;
            throw new EmptyPathException(getResources().getMessageDictionary());
         }

         newPath1 = path;

         paths.set(index_, newPath1);
         paths.add(index_+1, newPath2);

         setBackgroundImage();

         setRefreshBounds(oldPath);
         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.remove(index_+1);
         paths.set(index_, oldPath);

         editedPath = oldPath;
         editedPath.selectControl(controlIndex);

         setBackgroundImage();

         repaintRegion();
         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, newPath1);
         paths.add(index_+1, newPath2);

         editedPath = newPath1;
         editedPath.selectControl(controlIndex);

         setBackgroundImage();

         repaintRegion();
         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.break");
      }
   }

   class OpenPath extends CanvasUndoableEdit
   {
      private int index_;
      private JDRShape path_, oldPath_;
      private int oldPtIdx, newPtIdx;

      public OpenPath(JDRShape path, boolean removeLast)
      throws InvalidPathException
      {
         super(getFrame());

         index_ = path.getIndex();
         oldPath_ = path;
         path_ = (JDRShape)oldPath_.clone();
         oldPtIdx = editedPath.getSelectedControlIndex();
         newPtIdx = oldPtIdx;

         path_.open(removeLast);
         paths.set(index_, path_);

         editedPath.setEditMode(false);
         editedPath = path_;

         if (editedPath.selectControl(newPtIdx) == null)
         {
            newPtIdx = 0;
            editedPath.selectControl(newPtIdx);
         }

         // The original path bounds should encompass the new path
         // bounds so only use the original path for the refresh
         // region.
         setRefreshControlBounds(path_);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, path_);
         editedPath.setEditMode(false);
         editedPath = path_;
         editedPath.selectControl(newPtIdx);

         repaintRegion();
         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldPath_);
         editedPath.setEditMode(false);
         editedPath = oldPath_;
         editedPath.selectControl(oldPtIdx);

         repaintRegion();
         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.open");
      }
   }

   class ClosePath extends CanvasUndoableEdit
   {
      private int index_;
      private JDRShape path_, oldPath_;
      private int oldPtIdx;

      public ClosePath(JDRShape path, int closeType)
         throws InvalidPathException
      {
         super(getFrame());

         index_ = path.getIndex();
         oldPath_ = path;
         path_ = (JDRShape)oldPath_.clone();
         oldPtIdx = editedPath.getSelectedControlIndex();

         path_.close(closeType);
         paths.set(index_, path_);

         editedPath.setEditMode(false);
         editedPath = path_;
         editedPath.selectControl(oldPtIdx);

         setRefreshControlBounds(path_, oldPath_);
         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, path_);
         editedPath.setEditMode(false);
         editedPath = path_;
         editedPath.selectControl(oldPtIdx);

         repaintRegion();
         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldPath_);
         editedPath.setEditMode(false);
         editedPath = oldPath_;
         editedPath.selectControl(oldPtIdx);

         repaintRegion();
         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.close");
      }
   }

   class MovePoint extends CanvasUndoableEdit
   {
      private JDRPoint dp_, p1;
      private JDRPathSegment segment_;
      private JDRShape path_;
      private double x_, y_, x1, y1;

      public MovePoint(JDRShape path, JDRPathSegment segment, JDRPoint dp, 
                       double storageX, double storageY)
      {
         super(getFrame());

         dp_ = dp;
         path_ = path;
         x_ = storageX;
         y_ = storageY;
         segment_ = segment;

         BBox box = path.getBpControlBBox();

         path_.translateControl(segment_, dp_, x_, y_);

         p1 = null;

         JDRPathSegment adjoining = null;

         if (segment_ instanceof JDRBezier)
         {
            JDRBezier bezier = (JDRBezier)segment_;
            JDRPoint p0 = null;

            if (dp_ == bezier.getControl1())
            {
               p0 = bezier.getStart();

               if (p0.isAnchored())
               {
                  int index = editedPath.getIndex(bezier);

                  if (index == 0)
                  {
                     p1 = editedPath.getLastControl() ;
                  }
                  else
                  {
                     adjoining = editedPath.get(index-1);

                     adjoining.mergeBpControlBBox(box);
                     p1 = adjoining.getControl(adjoining.controlCount()-1);
                  }
               }
            }
            else if (dp_ == bezier.getControl2())
            {
               p0 = bezier.getEnd();

               if (p0.isAnchored())
               {
                  int index = editedPath.getIndex(bezier);

                  if (index == editedPath.size()-1)
                  {
                     p1 = editedPath.getControl(1);
                  }
                  else
                  {
                     adjoining = editedPath.get(index+1);

                     adjoining.mergeBpControlBBox(box);
                     p1 = adjoining.getControl(1);
                  }
               }
            }

            if (p1 != null)
            {
               // Get the length of p0 - p1

               double diffX = p0.getX() - p1.getX();
               double diffY = p0.getY() - p1.getY();

               double length = Math.sqrt(diffX*diffX + diffY*diffY);

               if (length > 0)
               {
                  // Get the angle for dp - p0

                  double theta = Math.atan2(
                    p0.getY()-dp.getY(), p0.getX()-dp.getX());

                  // Get adjustments for p1

                  x1 = p0.x+length*Math.cos(theta) - p1.getX();
                  y1 = p0.y+length*Math.sin(theta) - p1.getY();
               }
               else
               {
                  p0 = null;
               }
            }
         }
         else if (segment_ instanceof JDRPartialBezier)
         {
            JDRPartialBezier bezier = (JDRPartialBezier)segment_;
            JDRPoint p0 = null;

            if (dp_ == bezier.getControl1())
            {
               p0 = bezier.getStart();

               if (p0.isAnchored())
               {
                  int index = editedPath.getIndex(bezier);

                  adjoining = editedPath.get(index-1);

                  adjoining.mergeBpControlBBox(box);
                  p1 = adjoining.getControl(adjoining.controlCount()-1);
               }
            }

            if (p1 != null)
            {
               // Get the length of p0 - p1

               double diffX = p0.getX() - p1.getX();
               double diffY = p0.getY() - p1.getY();

               double length = Math.sqrt(diffX*diffX + diffY*diffY);

               if (length > 0)
               {
                  // Get the angle for dp - p0

                  double theta = Math.atan2(
                    p0.getY()-dp.getY(), p0.getX()-dp.getX());

                  // Get adjustments for p1

                  x1 = p0.x+length*Math.cos(theta) - p1.getX();
                  y1 = p0.y+length*Math.sin(theta) - p1.getY();
               }
               else
               {
                  p0 = null;
               }
            }
         }

         if (segment_ != null)
         {
            segment_.mergeBpControlBBox(box);
         }

         if (adjoining != null)
         {
            adjoining.mergeBpControlBBox(box);
         }

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         mergeRefreshBounds(path_, box);

         if (p1 != null)
         {
            BBox pb = p1.getBpControlBBox();
            pb.translate(-cg.getBpOriginX(), -cg.getBpOriginY());
            box.merge(pb);

            path_.translateControl(segment_, p1, x1, y1);

            pb = p1.getBpControlBBox();
            pb.translate(-cg.getBpOriginX(), -cg.getBpOriginY());
            box.merge(pb);
         }

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         path_.translateControl(segment_, dp_, x_, y_);

         if (p1 != null)
         {
            path_.translateControl(segment_, p1, x1, y1);
         }

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         path_.translateControl(segment_, dp_, -x_, -y_);

         if (p1 != null)
         {
            path_.translateControl(segment_, p1, -x1, -y1);
         }

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.move_point");
      }
   }

   class SetSymmetryJoinAnchor extends CanvasUndoableEdit
   {
      private JDRSymmetricPath oldPath_, newPath_;

      private int index_;

      public SetSymmetryJoinAnchor(boolean anchor)
         throws InvalidClassException
      {
         super(getFrame());

         if (!(editedPath instanceof JDRSymmetricPath))
         {
            throw new InvalidClassException("Not a JDRSymmetricPath");
         }

         index_ = editedPath.getIndex();

         oldPath_ = (JDRSymmetricPath)editedPath;

         newPath_ = (JDRSymmetricPath)editedPath.clone();
         editedPath = newPath_;

         if (anchor)
         {
            newPath_.setJoin(null);
         }
         else
         {
            newPath_.setJoin(new JDRPartialSegment(
               oldPath_.getLastSegment().getEnd(),
               oldPath_.getSymmetry()));
         }

         paths.set(index_, newPath_);

         BBox box = oldPath_.getBpControlBBox();
         box.merge(newPath_.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         setRefreshBounds(box);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         paths.set(index_, newPath_);
         editedPath = newPath_;
         repaintRegion();

         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         paths.set(index_, oldPath_);
         editedPath = oldPath_;
         repaintRegion();

         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.symmetry.join_anchor");
      }
   }

   class SetSymmetryCloseAnchor extends CanvasUndoableEdit
   {
      private JDRSymmetricPath oldPath_, newPath_;
      private int index_;

      public SetSymmetryCloseAnchor(boolean anchor)
         throws InvalidClassException,
                EmptyPathException,
                IllFittingPathException
      {
         super(getFrame());

         if (!(editedPath instanceof JDRSymmetricPath))
         {
            throw new InvalidClassException("Not a JDRSymmetricPath");
         }

         oldPath_ = (JDRSymmetricPath)editedPath;
         index_ = editedPath.getIndex();

         newPath_ = (JDRSymmetricPath)editedPath.clone();

         JDRPathSegment newSegment;

         if (anchor)
         {
            newSegment = null;
         }
         else
         {
            newSegment = new JDRPartialLine(getCanvasGraphics());
         }

         newPath_.close(newSegment);

         paths.set(index_, newPath_);
         editedPath = newPath_;

         BBox box = oldPath_.getBpControlBBox();
         box.merge(newPath_.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         setRefreshBounds(box);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         paths.set(index_, newPath_);
         editedPath = newPath_;

         repaintRegion();

         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         paths.set(index_, oldPath_);
         editedPath = oldPath_;
         
         repaintRegion();

         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.symmetry.close_anchor");
      }
   }

   class JoinToMove extends CanvasUndoableEdit
   {
      private JDRSymmetricPath path_;
      private JDRPartialSegment oldJoin, newJoin;

      public JoinToMove(JDRSymmetricPath path)
      {
         super(getFrame());

         path_ = path;

         oldJoin = path_.getJoin();

         newJoin = new JDRPartialSegment(
            path_.getLastSegment().getEnd(),
            path_.getSymmetry());

         path_.setJoin(newJoin);

         BBox box = oldJoin.getBpControlBBox();
         box.merge(newJoin.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         mergeRefreshBounds(path_, box);

         setRefreshBounds(box);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         path_.setJoin(newJoin);

         repaintRegion();

         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         path_.setJoin(oldJoin);

         repaintRegion();

         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_move");
      }
   }

   class ClosingToMove extends CanvasUndoableEdit
   {
      private JDRSymmetricPath path_;
      private JDRPartialSegment oldSegment, newSegment;

      public ClosingToMove(JDRSymmetricPath path)
         throws InvalidPathException
      {
         super(getFrame());

         path_ = path;

         oldSegment = path_.getClosingSegment();

         newSegment = new JDRPartialSegment(getCanvasGraphics());

         path_.close(newSegment);

         BBox box = oldSegment.getBpControlBBox();
         box.merge(newSegment.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         mergeRefreshBounds(path_, box);

         setRefreshBounds(box);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         try
         {
            path_.close(newSegment);
         }
         catch (InvalidPathException e)
         {
            throw new CannotRedoException();
         }

         repaintRegion();

         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         try
         {
            path_.close(oldSegment);
         }
         catch (InvalidPathException e)
         {
            throw new CannotUndoException();
         }

         repaintRegion();

         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_move");
      }
   }

   class JoinToLine extends CanvasUndoableEdit
   {
      private JDRSymmetricPath path_;
      private JDRPartialSegment oldJoin, newJoin;

      public JoinToLine(JDRSymmetricPath path)
      {
         super(getFrame());

         path_ = path;

         oldJoin = path_.getJoin();

         newJoin = new JDRPartialLine(
            path_.getLastSegment().getEnd(),
            path_.getSymmetry());

         path_.setJoin(newJoin);

         BBox box = oldJoin.getBpControlBBox();
         box.merge(newJoin.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         setRefreshBounds(box);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         path_.setJoin(newJoin);

         repaintRegion();

         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         path_.setJoin(oldJoin);

         repaintRegion();

         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_line");
      }
   }

   class ClosingToLine extends CanvasUndoableEdit
   {
      private JDRSymmetricPath path_;
      private JDRPartialSegment oldSegment, newSegment;

      public ClosingToLine(JDRSymmetricPath path)
         throws InvalidPathException
      {
         super(getFrame());

         path_ = path;

         oldSegment = path_.getClosingSegment();

         newSegment = new JDRPartialLine(getCanvasGraphics());

         path_.close(newSegment);

         BBox box = oldSegment.getBpControlBBox();
         box.merge(newSegment.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         mergeRefreshBounds(path_, box);

         setRefreshBounds(box);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         try
         {
            path_.close(newSegment);
         }
         catch (InvalidPathException e)
         {
            throw new CannotRedoException();
         }

         repaintRegion();

         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         try
         {
            path_.close(oldSegment);
         }
         catch (InvalidPathException e)
         {
            throw new CannotUndoException();
         }

         repaintRegion();

         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_line");
      }
   }

   class JoinToCurve extends CanvasUndoableEdit
   {
      private JDRSymmetricPath path_;
      private JDRPartialSegment oldJoin, newJoin;

      public JoinToCurve(JDRSymmetricPath path)
      {
         super(getFrame());

         path_ = path;

         oldJoin = path_.getJoin();

         newJoin = new JDRPartialBezier(
            path_.getLastSegment().getEnd(),
            path_.getSymmetry());

         path_.setJoin(newJoin);

         newJoin.flatten();

         BBox box = oldJoin.getBpControlBBox();
         box.merge(newJoin.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         mergeRefreshBounds(path_, box);

         setRefreshBounds(box);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         path_.setJoin(newJoin);

         repaintRegion();

         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         path_.setJoin(oldJoin);

         repaintRegion();

         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_curve");
      }
   }

   class ClosingToCurve extends CanvasUndoableEdit
   {
      private JDRSymmetricPath path_;
      private JDRPartialSegment oldSegment, newSegment;

      public ClosingToCurve(JDRSymmetricPath path)
        throws InvalidPathException
      {
         super(getFrame());

         path_ = path;

         oldSegment = path_.getClosingSegment();

         newSegment = (JDRPartialSegment)new JDRPartialBezier(
            path_.getFirstControl(),
            path_.getSymmetry()).reverse();

         path_.close(newSegment);

         newSegment.flatten();

         BBox box = oldSegment.getBpControlBBox();
         box.merge(newSegment.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         mergeRefreshBounds(path_, box);

         setRefreshBounds(box);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         try
         {
            path_.close(newSegment);
         }
         catch (InvalidPathException e)
         {
            throw new CannotRedoException();
         }

         repaintRegion();

         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         try
         {
            path_.close(oldSegment);
         }
         catch (InvalidPathException e)
         {
            throw new CannotUndoException();
         }

         repaintRegion();

         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_curve");
      }
   }

   class AnchorControl extends CanvasUndoableEdit
   {
      private JDRPoint point;

      public AnchorControl(JDRPoint point)
      {
         super(getFrame());

         this.point = point;

         point.setAnchored(!point.isAnchored());
         BBox box = point.getBpControlBBox();

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         point.setAnchored(!point.isAnchored());

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         point.setAnchored(!point.isAnchored());

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.anchor_control");
      }
   }

   class MakeContinuous extends CanvasUndoableEdit
   {
      private JDRPathSegment segment_;
      private JDRShape path_;
      private double x_, y_, newX_, newY_;
      private boolean isStart=true;

      public MakeContinuous(boolean isEqui) throws InvalidClassException
      {
         super(getFrame());

         if (!(editedPath.getSelectedSegment() instanceof JDRBezier)
           && !(editedPath.getSelectedSegment() instanceof JDRPartialBezier))
         {
            throw new InvalidClassException("Not a Bezier curve");
         }

         path_ = editedPath;

         segment_ = editedPath.getSelectedSegment();

         BBox box = editedPath.getBpControlBBox();

         if (segment_ instanceof JDRPartialBezier)
         {
            isStart = true;

            x_ = ((JDRPartialBezier)segment_).getControl1().getX();
            y_ = ((JDRPartialBezier)segment_).getControl1().getY();
         }
         else
         {
            isStart = true;

            if (path_.getSelectedControl()
                   == ((JDRBezier)segment_).getControl2())
            {
               isStart = false;
            }

            x_ = isStart
               ? ((JDRBezier)segment_).getControl1().x
               : ((JDRBezier)segment_).getControl2().x;

            y_ = isStart
               ? ((JDRBezier)segment_).getControl1().y
               : ((JDRBezier)segment_).getControl2().y;
         }

         path_.makeContinuous(isStart, isEqui);

         if (segment_ instanceof JDRPartialBezier)
         {
            newX_ = ((JDRPartialBezier)segment_).getControl1().getX();
            newY_ = ((JDRPartialBezier)segment_).getControl1().getY();
         }
         else
         {
            newX_ = isStart
                  ? ((JDRBezier)segment_).getControl1().x
                  : ((JDRBezier)segment_).getControl2().x;
            newY_ = isStart
                  ? ((JDRBezier)segment_).getControl1().y
                  : ((JDRBezier)segment_).getControl2().y;
         }

         box.merge(editedPath.getBpControlBBox());

         CanvasGraphics cg = getCanvasGraphics();
         box.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         if (isStart)
         {
            if (segment_ instanceof JDRPartialBezier)
            {
               ((JDRPartialBezier)segment_).setControl(newX_, newY_);
            }
            else
            {
               ((JDRBezier)segment_).setControl1(newX_, newY_);
            }
         }
         else
         {
            ((JDRBezier)segment_).setControl2(newX_, newY_);
         }

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         if (isStart)
         {
            if (segment_ instanceof JDRPartialBezier)
            {
               ((JDRPartialBezier)segment_).setControl(x_, y_);
            }
            else
            {
               ((JDRBezier)segment_).setControl1(x_, y_);
            }
         }
         else
         {
            ((JDRBezier)segment_).setControl2(x_, y_);
         }

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.continuous");
      }
   }

   class SelectControl extends CanvasUndoableEdit
   {
      private int oldIndex_, newIndex_;
      private String presentation_
         = getResources().getMessage("undo.select_point");

      public SelectControl(Point2D currentPos)
         throws NullPointerException
      {
         super(getFrame());

         CanvasGraphics cg = getCanvasGraphics();

         double hoffset = 0.0;
         double voffset = 0.0;

         FlowFrame flowframe = editedPath.getFlowFrame();
         FlowFrame typeblock = paths.getFlowFrame();

         Point2D pt;

         if (flowframe != null && cg.isEvenPage())
         {
            hoffset = -flowframe.getEvenXShift();
            voffset = -flowframe.getEvenYShift();

            if (typeblock != null)
            {
               hoffset -= typeblock.getEvenXShift();
            }

            pt = new Point2D.Double(
            currentPos.getX()+hoffset, currentPos.getY()+voffset);
         }
         else
         {
            pt = new Point2D.Double(currentPos.getX(), currentPos.getY());
         }

         oldIndex_ = editedPath.getSelectedControlIndex();

         BBox box = null;

         if (oldIndex_ != -1)
         {
            box = editedPath.getSelectedControl().getBpControlBBox();
         }

         newIndex_ = editedPath.getControlIndex(pt);

         if (newIndex_ == -1)
         {
            throw new NullPointerException();
         }

         editedPath.selectControl(newIndex_);

         if (box == null)
         {
            box = editedPath.getSelectedControl().getBpControlBBox();
         }
         else
         {
            box.merge(editedPath.getSelectedControl().getBpControlBBox());
         }

         box.translate(cg.storageToBp(hoffset)-cg.getBpOriginX(), -cg.getBpOriginY());

         setRefreshBounds(box);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         editedPath.selectControl(newIndex_);

         repaintRegion();

         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         editedPath.selectControl(oldIndex_);

         repaintRegion();

         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return presentation_;
      }
   }

   class SelectNextControl extends CanvasUndoableEdit
   {
      private int oldIndex, newIndex;
      private String presentation_
         = getResources().getMessage("undo.select_point");

      public SelectNextControl()
         throws NullPointerException
      {
         super(getFrame());

         oldIndex = editedPath.getSelectedControlIndex();
         FlowFrame flowframe = editedPath.getFlowFrame();

         BBox box = null;
         JDRPathSegment seg = null;

         if (oldIndex != -1)
         {
            box = getControlRefreshBounds(editedPath.getSelectedControl(),
               flowframe);

            seg = editedPath.getSelectedSegment();

            if (seg != null)
            {
               box.merge(getControlRefreshBounds(seg, flowframe));
            }
         }

         JDRPoint newPt = editedPath.selectNextControl();

         if (newPt == null)
         {
            throw new NullPointerException();
         }

         newIndex = editedPath.getSelectedControlIndex();

         if (box == null)
         {
            box = getControlRefreshBounds(newPt, flowframe);
         }
         else
         {
            box.merge(getControlRefreshBounds(newPt, flowframe));
         }

         JDRPathSegment seg2 = editedPath.getSelectedSegment();

         if (seg != seg2 && seg2 != null)
         {
            BBox segbox = seg2.getBpControlBBox();

            CanvasGraphics cg = getCanvasGraphics();
            segbox.translate(-cg.getBpOriginX(), -cg.getBpOriginY());

            box.merge(segbox);
         }

         setRefreshBounds(box);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         editedPath.selectControl(newIndex);

         repaintRegion();

         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         editedPath.selectControl(oldIndex);

         repaintRegion();

         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return presentation_;
      }
   }

   class SelectPrevControl extends CanvasUndoableEdit
   {
      private int oldIndex, newIndex;
      private String presentation_
         = getResources().getMessage("undo.select_point");

      public SelectPrevControl()
         throws NullPointerException
      {
         super(getFrame());

         oldIndex = editedPath.getSelectedControlIndex();
         FlowFrame flowframe = editedPath.getFlowFrame();

         BBox box = null;

         JDRPathSegment seg = null;

         if (oldIndex != -1)
         {
            box = getControlRefreshBounds(editedPath.getSelectedControl(),
               flowframe);

            seg = editedPath.getSelectedSegment();

            if (seg != null)
            {
               box.merge(getControlRefreshBounds(seg, flowframe));
            }
         }

         JDRPoint newPt = editedPath.selectPreviousControl();

         if (newPt == null)
         {
            throw new NullPointerException();
         }

         newIndex = editedPath.getSelectedControlIndex();

         if (box == null)
         {
            box = getControlRefreshBounds(newPt, flowframe);
         }
         else
         {
            box.merge(getControlRefreshBounds(newPt, flowframe));
         }

         JDRPathSegment seg2 = editedPath.getSelectedSegment();

         if (seg != seg2 && seg2 != null)
         {
            box.merge(getControlRefreshBounds(seg2, flowframe));
         }

         setRefreshBounds(box);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         editedPath.selectControl(newIndex);

         repaintRegion();

         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         editedPath.selectControl(oldIndex);

         repaintRegion();

         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return presentation_;
      }
   }

   class SelectObject extends CanvasUndoableEdit
   {
      private JDRCompleteObject object_;
      private boolean oldselected_, selected_;
      private String string_=getResources().getMessage("undo.select");

      public SelectObject(JDRCompleteObject object, boolean selected)
      {
         super(getFrame());

         object_ = object;
         oldselected_ = object.isSelected();
         selected_ = selected;
         object_.setSelected(selected_);
         enableTools();

         setRefreshBounds(object, false);
         getApplication().updateSelectActionButtons();
      }

      public SelectObject(JDRCompleteObject object, boolean selected,
                        String presentationString)
      {
         this(object, selected);
         string_ = presentationString;
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         if (frame_.currentTool() != ACTION_SELECT)
         {
            frame_.setTool(ACTION_SELECT);
         }

         object_.setSelected(selected_);
         getApplication().updateSelectActionButtons();

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         if (frame_.currentTool() != ACTION_SELECT)
         {
            frame_.setTool(ACTION_SELECT);
         }
         
         object_.setSelected(oldselected_);
         getApplication().updateSelectActionButtons();

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return string_;
      }
   }

   class MoveObject extends CanvasUndoableEdit
   {
      private JDRCompleteObject object_;
      private double x_, y_;

      public MoveObject(JDRCompleteObject object, double x, double y)
      {
         super(getFrame());

         object_ = object;
         x_ = x;
         y_ = y;

         BBox box = getRefreshBounds(object);

         object_.translate(-x_, -y_);

         mergeRefreshBounds(object, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         object_.translate(-x_, -y_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         object_.translate(x_, y_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.move");
      }
   }

   // Set symmetry
   class SetSymmetricPath extends CanvasUndoableEdit
   {
      private JDRShape oldPath_, newPath_;
      private int index_;

      public SetSymmetricPath(JDRShape path, boolean addSymmetry)
      throws InvalidShapeException
      {
         super(getFrame());

         oldPath_ = path;
         index_ = path.getIndex();

         if (addSymmetry)
         {
            // Converting regular shape to a symmetric path.
            // If the specified path is already symmetric,
            // something's gone wrong.
            if (path.hasSymmetricPath())
            {
               throw new InvalidShapeException
                 ("Path is already symmetric");
            }

            // If the path is a pattern, then add symmetry to the
            // underlying path

            if (path instanceof JDRPattern)
            {
               newPath_ = (JDRCompoundShape)path.clone();

               ((JDRPattern)newPath_).setUnderlyingShape(
                  JDRSymmetricPath.createFrom(
                         ((JDRPattern)path).getUnderlyingShape()));
            }
            else
            {
               newPath_ = JDRSymmetricPath.createFrom(path);
            }
         }
         else
         {
            // Is this shape a symmetric path, or is the underlying
            // shape symmetric?

            if (!path.hasSymmetricPath())
            {
               throw new InvalidShapeException("Not a symmetric path");
            }

            newPath_ = ((JDRCompoundShape)path).removeSymmetry();
         }

         newPath_.setSelected(true);
         editedPath = newPath_;

         paths.set(index_, newPath_);

         setRefreshControlBounds(oldPath_, newPath_);

         updateEditPathActions();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         paths.set(index_, newPath_);
         editedPath = newPath_;

         repaintRegion();

         updateEditPathActions();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         paths.set(index_, oldPath_);
         editedPath = oldPath_;

         repaintRegion();

         updateEditPathActions();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.symmetry");
      }
   }

   // edit bitmap properties
   class SetBitmapProperties extends CanvasUndoableEdit
   {
      private JDRBitmap object_;
      private String filename_, latexfilename_;
      private String oldFilename_, oldLatexfilename_;
      private String oldCommand_, command_;
      private double[] oldmatrix, newmatrix;

      public SetBitmapProperties(JDRBitmap bitmap, String newfilename,
                        String newlatexfilename, String command,
                        double[] matrix)
      {
         super(getFrame());

         object_ = bitmap;
         oldFilename_      = bitmap.getFilename();
         oldLatexfilename_ = bitmap.getLaTeXLinkName();
         filename_         = newfilename;
         latexfilename_    = newlatexfilename;
         oldCommand_       = bitmap.getLaTeXCommand();
         command_          = command;
         newmatrix         = matrix;

         oldmatrix = new double[6];
         bitmap.getTransformation(oldmatrix);

         BBox box = getRefreshBounds(object_);

         object_.setProperties(filename_, latexfilename_);
         object_.setLaTeXCommand(command_);
         object_.setTransformation(newmatrix);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         object_.setProperties(filename_, latexfilename_);
         object_.setLaTeXCommand(command_);
         object_.setTransformation(newmatrix);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         object_.setProperties(oldFilename_, oldLatexfilename_);
         object_.setLaTeXCommand(oldCommand_);
         object_.setTransformation(oldmatrix);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.bitmap_properties");
      }
   }

   class BitmapReset extends CanvasUndoableEdit
   {
      private JDRBitmap object_;
      private double[] oldmatrix, newmatrix;

      public BitmapReset(JDRBitmap bitmap)
      {
         super(getFrame());

         object_ = bitmap;
         oldmatrix = new double[6];
         newmatrix = new double[6];

         bitmap.getTransformation(oldmatrix);

         BBox box = getRefreshBounds(object_);

         object_.reset();
         bitmap.getTransformation(newmatrix);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         object_.setTransformation(newmatrix);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         object_.setTransformation(oldmatrix);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.reset.bitmap");
      }
   }

   class TextReset extends CanvasUndoableEdit
   {
      private JDRTextual object_;
      private double[] oldmatrix, newmatrix;

      public TextReset(JDRTextual textual)
      {
         super(getFrame());

         object_ = textual;
         oldmatrix = new double[6];
         newmatrix = new double[6];

         textual.getTransformation(oldmatrix);

         BBox box = getRefreshBounds(object_);

         object_.reset();
         textual.getTransformation(newmatrix);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         object_.setTransformation(newmatrix);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         object_.setTransformation(oldmatrix);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.reset.text");
      }
   }

   class Scale extends CanvasUndoableEdit
   {
      private JDRCompleteObject object_, oldobject_;
      private int index_;

      public Scale(JDRCompleteObject object, double factor, int index)
      {
         super(getFrame());

         index_ = index;
         oldobject_ = object;
         object_ = (JDRCompleteObject)object.clone();

         object_.scale(factor);
         paths.set(index_, object_);

         setRefreshBounds(oldobject_, object_);
      }

      public Scale(JDRCompleteObject object, double factorX,
        double factorY, int index)
      {
         super(getFrame());

         index_ = index;
         oldobject_ = object;
         object_ = (JDRCompleteObject)object.clone();

         object_.scale(factorX, factorY);
         paths.set(index_, object_);

         setRefreshBounds(oldobject_, object_);
      }

      public Scale(JDRCompleteObject object, double factorX, double factorY,
                   int index, JDRPoint p)
      {
         super(getFrame());

         oldobject_ = object;
         object_ = (JDRCompleteObject)object.clone();
         index_ = index;

         object_.scale(p, factorX, factorY);
         paths.set(index_, object_);

         if (dragScaleObject == oldobject_) dragScaleObject = object_;

         setRefreshBounds(oldobject_, object_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         paths.set(index_, object_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         paths.set(index_, oldobject_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.scale");
      }
   }

   class ScaleX extends CanvasUndoableEdit
   {
      private JDRCompleteObject object_, oldobject_;
      private int index_;

      public ScaleX(JDRCompleteObject object, double factor, int index)
      {
         super(getFrame());

         oldobject_ = object;
         object_ = (JDRCompleteObject)object.clone();
         index_ = index;

         object_.scaleX(factor);
         paths.set(index_, object_);

         setRefreshBounds(oldobject_, object_);
      }

      public ScaleX(JDRCompleteObject object, double factor, int index,
                    JDRPoint p)
      {
         super(getFrame());

         oldobject_ = object;
         object_ = (JDRCompleteObject)object.clone();
         index_ = index;

         object_.scaleX(p, factor);
         paths.set(index_, object_);

         if (dragScaleObject == oldobject_) dragScaleObject = object_;

         setRefreshBounds(oldobject_, object_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, object_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldobject_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.scale_x");
      }
   }

   class ScaleY extends CanvasUndoableEdit
   {
      private JDRCompleteObject object_, oldobject_;
      private int index_;

      public ScaleY(JDRCompleteObject object, double factor, int index)
      {
         super(getFrame());

         oldobject_ = object;
         object_ = (JDRCompleteObject)object.clone();
         index_ = index;

         object_.scaleY(factor);
         paths.set(index_, object_);

         setRefreshBounds(oldobject_, object_);
      }

      public ScaleY(JDRCompleteObject object, double factor, int index,
                    JDRPoint p)
      {
         super(getFrame());

         oldobject_ = object;
         object_ = (JDRCompleteObject)object.clone();
         index_ = index;

         object_.scaleY(p, factor);

         if (dragScaleObject == oldobject_) dragScaleObject = object_;

         paths.set(index_, object_);

         setRefreshBounds(oldobject_, object_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, object_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldobject_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.scale_y");
      }
   }

   class Shear extends CanvasUndoableEdit
   {
      private JDRCompleteObject object_, oldobject_;
      private int index_;

      public Shear(JDRCompleteObject object, double factorX, double factorY,
         int index)
      {
         super(getFrame());

         oldobject_ = object;
         object_ = (JDRCompleteObject)object.clone();
         index_ = index;

         object_.shear(factorX, factorY);
         paths.set(index_, object_);

         if (dragScaleObject == oldobject_) dragScaleObject = object_;

         setRefreshBounds(oldobject_, object_);
      }

      public Shear(JDRCompleteObject object, double factorX, double factorY,
         int index, JDRPoint p)
      {
         super(getFrame());

         oldobject_ = object;
         object_ = (JDRCompleteObject)object.clone();
         index_ = index;

         object_.shear(p, factorX, factorY);
         paths.set(index_, object_);

         if (dragScaleObject == oldobject_) dragScaleObject = object_;

         setRefreshBounds(oldobject_, object_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, object_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldobject_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.shear");
      }
   }

   class Rotate extends CanvasUndoableEdit
   {
      private JDRCompleteObject object_, oldobject_;
      private int index_;

      public Rotate(JDRCompleteObject object, double angle, int index)
      {
         super(getFrame());

         oldobject_ = object;
         object_ = (JDRCompleteObject)object.clone();
         index_ = index;

         object_.rotate(angle);
         paths.set(index_, object_);

         setRefreshBounds(oldobject_, object_);
      }

      public Rotate(JDRCompleteObject object, double angle, 
                   int index, JDRPoint p)
      {
         super(getFrame());

         oldobject_ = object;
         object_ = (JDRCompleteObject)object.clone();
         index_ = index;

         object_.rotate(p, angle);
         paths.set(index_, object_);

         if (dragScaleObject == oldobject_) dragScaleObject = object_;

         setRefreshBounds(oldobject_, object_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, object_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldobject_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.rotate");
      }
   }

   class ConvertToPolygon extends CanvasUndoableEdit
   {
      private JDRShape object_, oldobject_;
      private int index_;

      public ConvertToPolygon(JDRShape oldShape, JDRShape polygon)
         throws InvalidShapeException
      {
         super(getFrame());

         oldobject_ = oldShape;
         index_ = ((JDRCompleteObject)oldShape).getIndex();

         object_ = polygon;

         paths.set(index_, object_);

         // Bounds may be different if the shape has markers.
         setRefreshBounds(oldobject_, object_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, object_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldobject_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.convert_to_polygon");
      }
   }

   class ReversePath extends CanvasUndoableEdit
   {
      private JDRShape object_, oldobject_;
      private int index_;

      public ReversePath(JDRShape object, int index)
         throws InvalidShapeException
      {
         super(getFrame());

         oldobject_ = object;
         object_ = object.reverse();
         index_ = index;

         paths.set(index_, object_);

         // Bounds may be different if the shape has markers.
         setRefreshBounds(oldobject_, object_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, object_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldobject_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.reverse");
      }
   }

   class AlignGroup extends CanvasUndoableEdit
   {
      private JDRGroup object_, oldobject_;
      private int index_, align_;
      private String string_=getResources().getMessage("undo.justify");

      public static final int LEFT=0, CENTRE=1, RIGHT=2,
                              TOP=3, MIDDLE=4, BOTTOM=5;

      public AlignGroup(JDRGroup object, int index, int align)
      {
         super(getFrame());

         oldobject_ = object;
         object_ = (JDRGroup)object.clone();
         index_ = index;
         align_ = align;

         switch (align_)
         {
            case LEFT :
               object_.leftAlign();
               string_ = getResources().getMessage("undo.justify.left");

               if (getApplication().isAutoAnchorEnabled())
               {
                  for (int i = 0; i < object_.size(); i++)
                  {
                     JDRCompleteObject obj = object_.get(i);

                     if (obj instanceof JDRText)
                     {
                        JDRText text = (JDRText)obj;

                        text.setHAlign(JDRText.PGF_HALIGN_LEFT);
                     }
                  }
               }
            break;
            case CENTRE :
               object_.centreAlign();
               string_ = getResources().getMessage("undo.justify.centre");

               if (getApplication().isAutoAnchorEnabled())
               {
                  for (int i = 0; i < object_.size(); i++)
                  {
                     JDRCompleteObject obj = object_.get(i);

                     if (obj instanceof JDRText)
                     {
                        JDRText text = (JDRText)obj;

                        text.setHAlign(JDRText.PGF_HALIGN_CENTRE);
                     }
                  }
               }
            break;
            case RIGHT :
               object_.rightAlign();
               string_ = getResources().getMessage("undo.justify.right");

               if (getApplication().isAutoAnchorEnabled())
               {
                  for (int i = 0; i < object_.size(); i++)
                  {
                     JDRCompleteObject obj = object_.get(i);

                     if (obj instanceof JDRText)
                     {
                        JDRText text = (JDRText)obj;

                        text.setHAlign(JDRText.PGF_HALIGN_RIGHT);
                     }
                  }
               }
            break;
            case TOP :
               object_.topAlign();
               string_ = getResources().getMessage("undo.justify.top");

               if (getApplication().isAutoAnchorEnabled())
               {
                  for (int i = 0; i < object_.size(); i++)
                  {
                     JDRCompleteObject obj = object_.get(i);

                     if (obj instanceof JDRText)
                     {
                        JDRText text = (JDRText)obj;

                        text.setVAlign(JDRText.PGF_VALIGN_TOP);
                     }
                  }
               }
            break;
            case MIDDLE :
               object_.middleAlign();
               string_ = getResources().getMessage("undo.justify.middle");

               if (getApplication().isAutoAnchorEnabled())
               {
                  for (int i = 0; i < object_.size(); i++)
                  {
                     JDRCompleteObject obj = object_.get(i);

                     if (obj instanceof JDRText)
                     {
                        JDRText text = (JDRText)obj;

                        text.setVAlign(JDRText.PGF_VALIGN_CENTRE);
                     }
                  }
               }
            break;
            case BOTTOM :
               object_.bottomAlign();
               string_ = getResources().getMessage("undo.justify.bottom");

               if (getApplication().isAutoAnchorEnabled())
               {
                  for (int i = 0; i < object_.size(); i++)
                  {
                     JDRCompleteObject obj = object_.get(i);

                     if (obj instanceof JDRText)
                     {
                        JDRText text = (JDRText)obj;

                        text.setVAlign(JDRText.PGF_VALIGN_BOTTOM);
                     }
                  }
               }
            break;
         }

         paths.set(index_, object_);

         // The bounds of the aligned objects will be smaller
         // than the original group bounds, so just set the
         // refresh bounds to the region taken up by the original
         // object.
         setRefreshBounds(object);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, object_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldobject_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return string_;
      }
   }

   class SetText extends CanvasUndoableEdit
   {
      private JDRTextual object_;
      private String oldtext_, newtext_;
      private String oldltxtext_, newltxtext_;
      private boolean textChanged;
      private int oldLeftDelim, oldRightDelim, newLeftDelim, newRightDelim;

      public SetText(JDRTextual object, String newtext,
         String newlatexText, int leftDelim, int rightDelim)
      {
         super(getFrame());

         object_     = object;
         oldtext_    = object.getText();
         newtext_    = newtext;
         oldltxtext_ = object.getLaTeXText();
         newltxtext_ = newlatexText;

         if (object instanceof JDRTextPath)
         {
            JDRTextPath textPath = (JDRTextPath)object;
            JDRTextPathStroke stroke = (JDRTextPathStroke)textPath.getStroke();

            oldLeftDelim = stroke.getLeftDelim();
            oldRightDelim = stroke.getRightDelim();

            newLeftDelim = leftDelim;
            newRightDelim = rightDelim;

            stroke.setLeftDelim(newLeftDelim);
            stroke.setRightDelim(newRightDelim);
         }

         BBox box = getRefreshBounds(object);

         textChanged = (!oldtext_.equals(newtext_));

         if (textChanged)
         {
            Graphics2D g = (Graphics2D)getGraphics();
            g.setRenderingHints(frame_.getRenderingHints());

            getCanvasGraphics().setGraphicsDevice(g);

            try
            {
               object_.setText(newtext_);
            }
            finally
            {
               getCanvasGraphics().setGraphicsDevice(null);
               g.dispose();
            }

            mergeRefreshBounds(object, box);

            setRefreshBounds(box);
         }
         else
         {
            frame_.markAsModified();
         }

         object_.setLaTeXText(newltxtext_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         if (textChanged)
         {
            Graphics2D g = (Graphics2D)getGraphics();
            g.setRenderingHints(frame_.getRenderingHints());

            getCanvasGraphics().setGraphicsDevice(g);

            try
            {
               object_.setText(newtext_);
            }
            finally
            {
               getCanvasGraphics().setGraphicsDevice(null);
               g.dispose();
            }

            repaintRegion();
         }
         else
         {
            frame_.markAsModified();
         }

         object_.setLaTeXText(newltxtext_);

         if (object_ instanceof JDRTextPath)
         {
            JDRTextPath textPath = (JDRTextPath)object_;
            JDRTextPathStroke stroke = (JDRTextPathStroke)textPath.getStroke();

            stroke.setLeftDelim(newLeftDelim);
            stroke.setRightDelim(newRightDelim);
         }
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         if (textChanged)
         {
            Graphics2D g = (Graphics2D)getGraphics();
            g.setRenderingHints(frame_.getRenderingHints());
            getCanvasGraphics().setGraphicsDevice(g);

            try
            {
               object_.setText(oldtext_);
            }
            finally
            {
               getCanvasGraphics().setGraphicsDevice(null);
               g.dispose();
            }

            repaintRegion();
         }
         else
         {
            frame_.markAsModified();
         }

         object_.setLaTeXText(oldltxtext_);

         if (object_ instanceof JDRTextPath)
         {
            JDRTextPath textPath = (JDRTextPath)object_;
            JDRTextPathStroke stroke = (JDRTextPathStroke)textPath.getStroke();

            stroke.setLeftDelim(oldLeftDelim);
            stroke.setRightDelim(oldRightDelim);
         }
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.edit_text");
      }
   }

   class FadeObject extends CanvasUndoableEdit
   {
      private JDRCompleteObject oldObject_, newObject_;
      private int index_;

      public FadeObject(JDRCompleteObject object, double value, int index)
      {
         super(getFrame());

         index_ = index;
         oldObject_ = object;
         newObject_ = (JDRCompleteObject)object.clone();

         newObject_.fade(value);
         paths.set(index_, newObject_);

         setRefreshBounds(object);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         paths.set(index_, newObject_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         paths.set(index_, oldObject_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.colour");
      }
   }

   class SetLinePaint extends CanvasUndoableEdit
   {
      private JDRShape object_;
      private JDRPaint oldpaint_, newpaint_;

      public SetLinePaint(JDRShape object, JDRPaint paint)
      {
         super(getFrame());

         object_ = object;
         oldpaint_ = object.getLinePaint();
         newpaint_ = paint;

         object_.setLinePaint(newpaint_);

         setRefreshBounds(object_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         object_.setLinePaint(newpaint_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         object_.setLinePaint(oldpaint_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.line_colour");
      }
   }

   class SetTextPaint extends CanvasUndoableEdit
   {
      private JDRTextual object_;
      private JDRPaint oldpaint_, newpaint_;

      public SetTextPaint(JDRTextual object, JDRPaint paint)
      {
         super(getFrame());

         object_ = object;
         oldpaint_ = object.getTextPaint();
         newpaint_ = paint;

         object_.setTextPaint(newpaint_);

         setRefreshBounds(object_);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         object_.setTextPaint(newpaint_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         object_.setTextPaint(oldpaint_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.colour");
      }
   }

   class SetFillPaint extends CanvasUndoableEdit
   {
      private JDRCompleteObject object_;
      private JDRPaint oldpaint_, newpaint_;

      public SetFillPaint(JDRCompleteObject object, JDRPaint paint)
      {
         super(getFrame());

         object_ = object;

         if (object_ instanceof JDRShape)
         {
            oldpaint_ = ((JDRShape)object).getFillPaint();
         }
         else
         {
            oldpaint_ = ((JDRTextual)object).getFillPaint();
         }

         newpaint_ = paint;

         setFillPaint(newpaint_);

         setRefreshBounds(object_);
      }

      private void setFillPaint(JDRPaint paint)
      {
         if (object_ instanceof JDRShape)
         {
            ((JDRShape)object_).setFillPaint(paint);
         }
         else
         {
            ((JDRTextual)object_).setFillPaint(paint);
         }
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         setFillPaint(newpaint_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         setFillPaint(oldpaint_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.fill_colour");
      }
   }

   class SetLineStyle extends CanvasUndoableEdit
   {
      private JDRShape object_;
      private JDRStroke oldstroke_, newstroke_;

      public SetLineStyle(JDRShape object, JDRStroke stroke)
      {
         super(getFrame());

         object_ = object;
         oldstroke_ = object.getStroke();
         newstroke_ = stroke;

         BBox box = getRefreshBounds(object_);

         object_.setStroke(newstroke_);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         object_.setStroke(newstroke_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         object_.setStroke(oldstroke_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.line_style");
      }
   }

   class SetLineWidth extends CanvasUndoableEdit
   {
      private JDRShape object_;
      private JDRLength oldStyle_, newStyle_;

      public SetLineWidth(JDRShape object, JDRLength style)
      {
         super(getFrame());

         object_ = object;
         oldStyle_ = ((JDRBasicStroke)object.getStroke()).getPenWidth();
         newStyle_ = (JDRLength)style.clone();

         BBox box = getRefreshBounds(object);

         ((JDRBasicStroke)object_.getStroke()).setPenWidth(style);

         mergeRefreshBounds(object, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setPenWidth(newStyle_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setPenWidth(oldStyle_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.line_style");
      }
   }

   class SetDashPattern extends CanvasUndoableEdit
   {
      private JDRShape object_;
      private DashPattern oldStyle_, newStyle_;

      public SetDashPattern(JDRShape object, DashPattern style)
      {
         super(getFrame());

         object_ = object;
         oldStyle_ = ((JDRBasicStroke)object.getStroke()).getDashPattern();
         newStyle_ = style;

         BBox box = getRefreshBounds(object_);

         ((JDRBasicStroke)object_.getStroke()).setDashPattern(style);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setDashPattern(newStyle_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setDashPattern(oldStyle_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.line_style");
      }
   }

   class SetCapStyle extends CanvasUndoableEdit
   {
      private JDRShape object_;
      private int oldStyle_, newStyle_;

      public SetCapStyle(JDRShape object, int style)
      {
         super(getFrame());

         object_ = object;
         oldStyle_ = ((JDRBasicStroke)object.getStroke()).getCapStyle();
         newStyle_ = style;

         BBox box = getRefreshBounds(object_);

         ((JDRBasicStroke)object_.getStroke()).setCapStyle(style);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setCapStyle(newStyle_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setCapStyle(oldStyle_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.line_style");
      }
   }

   class SetJoinStyle extends CanvasUndoableEdit
   {
      private JDRShape object_;
      private int oldStyle_, newStyle_;

      public SetJoinStyle(JDRShape object, int style)
      {
         super(getFrame());

         object_ = object;
         oldStyle_ = ((JDRBasicStroke)object.getStroke()).getJoinStyle();
         newStyle_ = style;

         BBox box = getRefreshBounds(object_);

         ((JDRBasicStroke)object_.getStroke()).setJoinStyle(style);

         mergeRefreshBounds(object_, box);
         setRefreshBounds(box);

         repaintRegion();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setJoinStyle(newStyle_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setJoinStyle(oldStyle_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.line_style");
      }
   }

   class SetMitreLimit extends CanvasUndoableEdit
   {
      private JDRShape object_;
      private double oldLimit_, newLimit_;

      public SetMitreLimit(JDRShape object, double limit)
      {
         super(getFrame());

         object_ = object;
         oldLimit_ = ((JDRBasicStroke)object.getStroke()).getMitreLimit();
         newLimit_ = limit;

         BBox box = getRefreshBounds(object_);

         ((JDRBasicStroke)object_.getStroke()).setMitreLimit(limit);

         mergeRefreshBounds(object_, box);
         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setMitreLimit(newLimit_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setMitreLimit(oldLimit_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.line_style");
      }
   }

   class SetStartArrow extends CanvasUndoableEdit
   {
      private JDRBasicStroke stroke;
      private JDRMarker oldMarker_, newMarker_;

      public SetStartArrow(JDRShape object, JDRMarker marker)
      {
         super(getFrame());

         stroke = (JDRBasicStroke)object.getStroke();

         oldMarker_     = stroke.getStartArrow();
         newMarker_     = (JDRMarker)marker.clone();

         BBox box = getRefreshBounds(object);

         stroke.setStartArrow(newMarker_);

         mergeRefreshBounds(object, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         stroke.setStartArrow(newMarker_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         stroke.setStartArrow(oldMarker_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.line_style");
      }
   }

   class SetMidArrow extends CanvasUndoableEdit
   {
      private JDRShape object_;
      private JDRMarker oldMarker_, newMarker_;

      public SetMidArrow(JDRShape object, JDRMarker marker)
      {
         super(getFrame());

         object_ = object;
         JDRBasicStroke stroke = (JDRBasicStroke)object.getStroke();

         oldMarker_     = stroke.getMidArrow();
         newMarker_     = (JDRMarker)marker.clone();

         BBox box = getRefreshBounds(object_);

         ((JDRBasicStroke)object_.getStroke()).setMidArrow(newMarker_);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setMidArrow(newMarker_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setMidArrow(oldMarker_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.line_style");
      }
   }

   class SetEndArrow extends CanvasUndoableEdit
   {
      private JDRShape object_;
      private JDRMarker oldMarker_, newMarker_;

      public SetEndArrow(JDRShape object, JDRMarker marker)
      {
         super(getFrame());

         object_ = object;
         JDRBasicStroke stroke = (JDRBasicStroke)object.getStroke();

         oldMarker_      = stroke.getEndArrow();
         newMarker_      = (JDRMarker)marker.clone();

         BBox box = getRefreshBounds(object_);

         ((JDRBasicStroke)object_.getStroke()).setEndArrow(newMarker_);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setEndArrow(newMarker_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setEndArrow(oldMarker_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.line_style");
      }
   }

   class SetMarkers extends CanvasUndoableEdit
   {
      private JDRShape object_;
      private JDRMarker newStartMarker_, newMidMarker_, newEndMarker_;
      private JDRMarker oldStartMarker_, oldMidMarker_, oldEndMarker_;

      public SetMarkers(JDRShape object, JDRMarker marker)
      {
         super(getFrame());

         object_ = object;
         JDRBasicStroke stroke = (JDRBasicStroke)object.getStroke();

         oldStartMarker_ = stroke.getStartArrow();
         oldMidMarker_   = stroke.getMidArrow();
         oldEndMarker_   = stroke.getEndArrow();

         newStartMarker_      = (JDRMarker)marker.clone();
         newMidMarker_      = (JDRMarker)marker.clone();
         newEndMarker_      = (JDRMarker)marker.clone();

         BBox box = getRefreshBounds(object_);

         ((JDRBasicStroke)object_.getStroke()).setStartArrow(newStartMarker_);
         ((JDRBasicStroke)object_.getStroke()).setMidArrow(newMidMarker_);
         ((JDRBasicStroke)object_.getStroke()).setEndArrow(newEndMarker_);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setStartArrow(newStartMarker_);
         ((JDRBasicStroke)object_.getStroke()).setMidArrow(newMidMarker_);
         ((JDRBasicStroke)object_.getStroke()).setEndArrow(newEndMarker_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setStartArrow(oldStartMarker_);
         ((JDRBasicStroke)object_.getStroke()).setMidArrow(oldMidMarker_);
         ((JDRBasicStroke)object_.getStroke()).setEndArrow(oldEndMarker_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.line_style");
      }
   }

   class SetWindingRule extends CanvasUndoableEdit
   {
      private JDRShape object_;
      private int oldStyle_, newStyle_;

      public SetWindingRule(JDRShape object, int style)
      {
         super(getFrame());

         object_ = object;
         oldStyle_ = ((JDRBasicStroke)object.getStroke()).getWindingRule();
         newStyle_ = style;

         BBox box = getRefreshBounds(object_);

         ((JDRBasicStroke)object_.getStroke()).setWindingRule(style);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setWindingRule(newStyle_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         ((JDRBasicStroke)object_.getStroke()).setWindingRule(oldStyle_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.line_style");
      }
   }

   class SetHAlign extends CanvasUndoableEdit
   {
      private JDRTextual object_;
      private int oldAlign_, newAlign_;

      public SetHAlign(JDRTextual object, int align)
      {
         super(getFrame());

         object_ = object;
         oldAlign_ = object.getHAlign();
         newAlign_ = align;

         BBox box = getRefreshBounds(object_);

         object_.setHAlign(align);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         object_.setHAlign(newAlign_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         object_.setHAlign(oldAlign_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.halign");
      }
   }

   class SetVAlign extends CanvasUndoableEdit
   {
      private JDRTextual object_;
      private int oldAlign_, newAlign_;

      public SetVAlign(JDRTextual object, int align)
      {
         super(getFrame());

         object_ = object;
         oldAlign_ = object.getVAlign();
         newAlign_ = align;

         BBox box = getRefreshBounds(object_);

         object_.setVAlign(align);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         object_.setVAlign(newAlign_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         object_.setVAlign(oldAlign_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.halign");
      }
   }

   class SetAnchor extends CanvasUndoableEdit
   {
      private JDRTextual object_;
      private int oldHAlign_, newHAlign_;
      private int oldVAlign_, newVAlign_;

      public SetAnchor(JDRTextual object, int halign, int valign)
      {
         super(getFrame());

         object_ = object;
         oldHAlign_ = object.getHAlign();
         newHAlign_ = halign;
         oldVAlign_ = object.getVAlign();
         newVAlign_ = valign;

         BBox box = getRefreshBounds(object_);

         object_.setHAlign(halign);
         object_.setVAlign(valign);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         object_.setHAlign(newHAlign_);
         object_.setVAlign(newVAlign_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         object_.setHAlign(oldHAlign_);
         object_.setVAlign(oldVAlign_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.anchor");
      }
   }

   class SetTextOutlineMode extends CanvasUndoableEdit
   {
      private JDRTextual text_;
      private boolean oldMode_, newMode_;

      public SetTextOutlineMode(JDRTextual object, boolean mode)
      {
         super(getFrame());

         text_ = object;
         oldMode_ = text_.isOutline();
         newMode_ = mode;

         if (oldMode_)
         {
            setRefreshBounds(text_);
         }

         setOutlineMode(newMode_);

         if (!oldMode_)
         {
            setRefreshBounds(text_);
         }
      }

      private void setOutlineMode(boolean mode)
      {
         Graphics2D g = (Graphics2D)getGraphics();
         g.setRenderingHints(frame_.getRenderingHints());

         getCanvasGraphics().setGraphicsDevice(g);

         try
         {
            text_.setOutlineMode(mode);
         }
         finally
         {
            getCanvasGraphics().setGraphicsDevice(null);
            g.dispose();
         }
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         setOutlineMode(newMode_);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         setOutlineMode(oldMode_);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.text.outline");
      }
   }

   class SetTextTransform extends CanvasUndoableEdit
   {
      private JDRTextual object_;
      private double[] oldmatrix, newmatrix;

      public SetTextTransform(JDRTextual object, double[] matrix)
      {
         super(getFrame());

         object_ = object;

         oldmatrix = object.getTransformation(null);
         newmatrix = matrix;

         BBox box = getRefreshBounds(object_);

         object_.setTransformation(matrix);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         object_.setTransformation(newmatrix);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         object_.setTransformation(oldmatrix);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.set_transform");
      }
   }

   class SetFont extends CanvasUndoableEdit
   {
      private JDRTextual object_;
      private String oldfamily, newfamily;
      private int oldseries, oldshape, oldvalign, oldhalign;
      private int newseries, newshape, newvalign, newhalign;
      private String oldlatexfamily, oldlatexsize, oldlatexseries,
                     oldlatexshape;
      private String newlatexfamily, newlatexsize, newlatexseries,
                     newlatexshape;

      private JDRLength oldsize, newsize;

      public SetFont(JDRTextual object, String family, int series,
                     int shape, JDRLength size, String latexfamily,
                     String latexsize, String latexseries,
                     String latexshape, int halign, int valign)
      {
         super(getFrame());

         object_ = object;

         oldfamily = object_.getFontFamily();
         oldseries = object_.getFontSeries();
         oldshape  = object_.getFontShape();
         oldsize   = object_.getFontSize();

         oldlatexfamily = object_.getLaTeXFamily();
         oldlatexsize   = object_.getLaTeXSize();
         oldlatexseries = object_.getLaTeXSeries();
         oldlatexshape  = object_.getLaTeXShape();
         oldhalign = object_.getHAlign();
         oldvalign = object_.getVAlign();

         newfamily = family;
         newseries = series;
         newsize   = size;
         newshape  = shape;

         newlatexfamily = latexfamily;
         newlatexsize   = latexsize;
         newlatexseries = latexseries;
         newlatexshape  = latexshape;
         newhalign = halign;
         newvalign = valign;

         BBox box = getRefreshBounds(object_);

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = ((JDRObject)object_).getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFont(newfamily,
                         newseries,
                         newshape,
                         newsize);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXFont(newlatexfamily, 
                          newlatexsize, 
                          newlatexseries,
                          newlatexshape);

         object_.setHAlign(newhalign);
         object_.setVAlign(newvalign);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = ((JDRObject)object_).getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFont(newfamily,
                            newseries,
                            newshape,
                            newsize);

            object_.setHAlign(newhalign);
            object_.setVAlign(newvalign);

         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXFont(newlatexfamily, 
                          newlatexsize, 
                          newlatexseries,
                          newlatexshape);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = ((JDRObject)object_).getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFont(oldfamily,
                            oldseries,
                            oldshape,
                            oldsize);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setHAlign(oldhalign);
         object_.setVAlign(oldvalign);

         object_.setLaTeXFont(oldlatexfamily, 
                          oldlatexsize, 
                          oldlatexseries,
                          oldlatexshape);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.font");
      }
   }

   class SetFontFamily extends CanvasUndoableEdit
   {
      private JDRTextual object_;
      private String oldfamily, newfamily;
      private String oldlatexfamily;
      private String newlatexfamily;

      public SetFontFamily(JDRTextual object, String family, 
                     String latexfamily)
      {
         super(getFrame());

         object_ = object;

         oldfamily = object_.getFontFamily();

         oldlatexfamily = object_.getLaTeXFamily();

         newfamily = family;

         newlatexfamily = latexfamily;

         BBox box = getRefreshBounds(object_);

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = object_.getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFontFamily(newfamily);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXFamily(newlatexfamily);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = object_.getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFontFamily(newfamily);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXFamily(newlatexfamily);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = object_.getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFontFamily(oldfamily);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXFamily(oldlatexfamily);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.font");
      }
   }

   class SetFontShape extends CanvasUndoableEdit
   {
      private JDRTextual object_;
      private int oldShape, newShape;
      private String oldlatexshape;
      private String newlatexshape;

      public SetFontShape(JDRTextual object, int shape, 
                     String latexshape)
      {
         super(getFrame());

         object_ = object;

         oldShape = object_.getFontShape();

         oldlatexshape = object_.getLaTeXShape();

         newShape = shape;

         newlatexshape = latexshape;

         BBox box = getRefreshBounds(object_);

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = object_.getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFontShape(newShape);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXShape(newlatexshape);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = object_.getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFontShape(newShape);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXShape(newlatexshape);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = object_.getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFontShape(oldShape);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXShape(oldlatexshape);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.font");
      }
   }

   class SetFontSeries extends CanvasUndoableEdit
   {
      private JDRTextual object_;
      private int oldSeries, newSeries;
      private String oldlatexseries;
      private String newlatexseries;

      public SetFontSeries(JDRTextual object, int series, 
                     String latexseries)
      {
         super(getFrame());

         object_ = object;

         oldSeries = object_.getFontSeries();

         oldlatexseries = object_.getLaTeXSeries();

         newSeries = series;

         newlatexseries = latexseries;

         BBox box = getRefreshBounds(object_);

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = object_.getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFontSeries(newSeries);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXSeries(newlatexseries);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = object_.getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFontSeries(newSeries);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXSeries(newlatexseries);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = object_.getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFontSeries(oldSeries);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXSeries(oldlatexseries);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.font");
      }
   }

   class SetFontSize extends CanvasUndoableEdit
   {
      private JDRTextual object_;
      private JDRLength oldSize, newSize;
      private String oldlatexsize;
      private String newlatexsize;

      public SetFontSize(JDRTextual object, JDRLength size, 
                     String latexsize)
      {
         super(getFrame());

         object_ = object;

         oldSize = object_.getFontSize();

         oldlatexsize = object_.getLaTeXSize();

         newSize = size;

         newlatexsize = latexsize;

         BBox box = getRefreshBounds(object_);

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = object_.getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFontSize(newSize);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXSize(newlatexsize);

         mergeRefreshBounds(object_, box);

         setRefreshBounds(box);
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());
         CanvasGraphics cg = object_.getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFontSize(newSize);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXSize(newlatexsize);

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         Graphics2D g2 = (Graphics2D)getGraphics();
         g2.setRenderingHints(frame_.getRenderingHints());

         CanvasGraphics cg = object_.getCanvasGraphics();
         cg.setGraphicsDevice(g2);

         try
         {
            object_.setFontSize(oldSize);
         }
         finally
         {
            cg.setGraphicsDevice(null);
            g2.dispose();
         }

         object_.setLaTeXSize(oldlatexsize);

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.font");
      }
   }

   class SetLaTeXFontSize extends AbstractUndoableEdit
   {
      private JDRTextual object_;
      private String oldlatexsize;
      private String newlatexsize;

      public SetLaTeXFontSize(JDRTextual object, String latexsize)
      {
         object_ = object;

         oldlatexsize = object_.getLaTeXSize();

         newlatexsize = latexsize;

         object_.setLaTeXSize(newlatexsize);

         frame_.markAsModified();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         object_.setLaTeXSize(newlatexsize);

         frame_.markAsModified();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         object_.setLaTeXSize(oldlatexsize);

         frame_.markAsModified();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.latexsize");
      }
   }

   class SetFontHAlign extends AbstractUndoableEdit
   {
      private JDRTextual object_;
      private int oldhalign;
      private int newhalign;

      public SetFontHAlign(JDRTextual object, int halign)
      {
         object_ = object;

         oldhalign = object_.getHAlign();
         newhalign = halign;

         object_.setHAlign(newhalign);

         frame_.markAsModified();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         object_.setHAlign(newhalign);

         frame_.markAsModified();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         object_.setHAlign(oldhalign);

         frame_.markAsModified();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.font");
      }
   }

   class SetFontVAlign extends AbstractUndoableEdit
   {
      private JDRTextual object_;
      private int oldvalign;
      private int newvalign;

      public SetFontVAlign(JDRTextual object, int valign)
      {
         object_ = object;

         oldvalign = object_.getVAlign();
         newvalign = valign;

         object_.setVAlign(newvalign);

         frame_.markAsModified();
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();

         object_.setVAlign(newvalign);

         frame_.markAsModified();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();

         object_.setVAlign(oldvalign);

         frame_.markAsModified();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.font");
      }
   }

   class EditPath extends CanvasUndoableEdit
   {
      private JDRShape path_;
      private boolean edited_;
      private int index_;

      CanvasAction editPathAction;

      public EditPath(JDRShape object, boolean flag)
      {
         super(getFrame());

         editPathAction = getSelectPathAction("path.edit");

         object.setEditMode(flag);
         edited_ = object.isEdited();
         path_ = object;

         index_ = 0;

         if (!object.isEdited())
         {
            index_ = editedPath.getSelectedControlIndex();
            finishEditingPath();
         }
         else
         {
            editedPath = path_;
            editedPath.selectControl(index_);
            editPathAction.setSelected(true);
            enableTools();
         }

         setBackgroundImage();

         setRefreshControlBounds(path_);
         updateStatus(flag);
      }

      private void updateStatus(boolean state)
      {
         if (state)
         {
            getApplication().setStatusInfo(
               getResources().getMessage("info.edit_path"), "sec:editpath");
         }
         else
         {
            getApplication().setStatusInfo(
               getResources().getMessage("info.select"), "sec:selectobjects");
         }
      }

      public void redo() throws CannotRedoException
      {
         frame_.selectThisFrame();
         path_.setEditMode(edited_);

         if (!path_.isEdited())
         {
            index_ = editedPath.getSelectedControlIndex();
            finishEditingPath();
            updateStatus(false);
         }
         else
         {
            editedPath = path_;
            editedPath.selectControl(index_);
            updateStatus(true);
            editPathAction.setSelected(true);
            enableTools();
         }

         setBackgroundImage();

         repaintRegion();
      }

      public void undo() throws CannotUndoException
      {
         frame_.selectThisFrame();
         path_.setEditMode(!edited_);

         if (!path_.isEdited())
         {
            index_ = editedPath.getSelectedControlIndex();
            finishEditingPath();
            updateStatus(false);
         }
         else
         {
            editedPath = path_;
            editedPath.selectControl(index_);
            enableTools();
            updateStatus(true);
            editPathAction.setSelected(true);
         }

         setBackgroundImage();

         repaintRegion();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return edited_ ? 
                getResources().getMessage("undo.edit.start") :
                getResources().getMessage("undo.edit.finish");
      }
   }

   class DisplayPageEdit extends AbstractUndoableEdit
   {
      private int _oldPage, _newPage;
      private String string_
         = getResources().getMessage("undo.displaypage");

      public DisplayPageEdit(int page)
      {
         _oldPage = displayPage;
         _newPage = page;
         displayPage = page;
         frame_.updateTitle();
         frame_.getApplication().updateTitle();

         updateEven();
         setBackgroundImage(true);
         repaint();
      }

      public void redo() throws CannotRedoException
      {
         displayPage = _newPage;
         frame_.updateTitle();
         frame_.getApplication().updateTitle();
         updateEven();

         setBackgroundImage(true);
         repaint();
      }

      public void undo() throws CannotUndoException
      {
         displayPage = _oldPage;
         frame_.updateTitle();
         frame_.getApplication().updateTitle();
         updateEven();

         setBackgroundImage(true);
         repaint();
      }

      private void updateEven()
      {
         getCanvasGraphics().setIsEvenPage(
          displayPage != PAGES_NONE && displayPage%2 == 0);
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return string_;
      }
   }

   class ShowTextField extends AbstractUndoableEdit
   {
      private String string_
         = getResources().getMessage("undo.start_new_text");
      private JDRText textarea_;
      private Point2D location_;
      private String text_ = "";
      private JDRFont font_, oldFont_;
      private JDRPaint foreground_;

      public ShowTextField(Point2D currentPos)
      {
         Point2D textpos = (Point2D)currentPos.clone();
         currentText = new JDRText(getCanvasGraphics(), textpos);

         textarea_ = (JDRText)currentText.clone();
         location_ = (Point2D)currentPos.clone();
         resetTextField();
         font_ = textFieldFont;
         oldFont_ = textFieldFont;
         foreground_ = textField.getTextPaint();

         text_ = textField.getText();
         textField.setNoUndoText("");
         textField.setPositionY(location_.getY());
         textField.setVisible(true);
         updateTextConstructionActions();
         textField.requestFocusInWindow();
         frame_.setNewImageState(false);
      }

      public void redo() throws CannotRedoException
      {
         currentText = textarea_;
         textField.setNoUndoText("");
         oldFont_ = textFieldFont;
         setTextFieldFont(font_);
         resetTextField(foreground_, location_);
         textField.setVisible(true);
         updateTextConstructionActions();
         textField.requestFocusInWindow();
      }

      public void undo() throws CannotUndoException
      {
         font_ = textFieldFont;
         textField.setNoUndoText(text_);
         setTextFieldFont(oldFont_);
         textField.setVisible(false);
         updateTextConstructionActions();
         currentText = null;
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return string_;
      }
   }

   class HideTextField extends AbstractUndoableEdit
   {
      private String string_
         = getResources().getMessage("undo.finish_new_text");
      private Point location_;
      private JDRText textarea_ = null;
      private JDRFont font_, oldFont_;
      private JDRPaint foreground_;

      public HideTextField()
      {
         font_ = textFieldFont;
         oldFont_ = textFieldFont;
         foreground_ = textField.getTextPaint();

         if (currentText != null)
         {
            currentText.setTextPaint(foreground_);
            textarea_ = (JDRText)currentText.clone();
            location_ = textarea_.getStart().getPoint();
         }
         else
         {
            location_ = (Point)textField.getPosition().clone();
         }

         textField.setVisible(false);
         updateTextConstructionActions();
      }

      public void redo() throws CannotRedoException
      {
         font_ = textFieldFont;
         setTextFieldFont(oldFont_);
         textField.setVisible(false);
         updateTextConstructionActions();
      }

      public void undo() throws CannotUndoException
      {
         currentText = textarea_;
         oldFont_ = textFieldFont;
         setTextFieldFont(font_);
         resetTextField(foreground_, location_);
         textField.setVisible(true);
         updateTextConstructionActions();
         textField.requestFocusInWindow();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return string_;
      }
   }

   class SetStorageUnit extends AbstractUndoableEdit
   {
      private JDRGroup oldImage, newImage;
      private byte orgUnitId, newUnitId;

      public SetStorageUnit(byte unitId)
      {
         orgUnitId = (byte)getCanvasGraphics().getStorageUnitID();
         newUnitId = unitId;

         oldImage = paths;
         newImage = (JDRGroup)paths.clone();

         CanvasGraphics newCanvasGraphics = 
            (CanvasGraphics)paths.getCanvasGraphics().clone();

         newCanvasGraphics.setStorageUnit(unitId);

         Cursor oldCursor = getApplication().getCursor();

         try
         {
            getApplication().setCursor(
               Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            newImage.applyCanvasGraphics(newCanvasGraphics);
            getApplication().setStatusStorageUnit(unitId);
         }
         finally
         {
            getApplication().setCursor(oldCursor);
         }

         paths = newImage;
         markAsModified();
      }

      public void redo() throws CannotRedoException
      {
         paths = newImage;

         getApplication().getDefaultCanvasGraphics().setStorageUnit(newUnitId);
         getApplication().setStatusStorageUnit(newUnitId);
      }

      public void undo() throws CannotUndoException
      {
         paths = oldImage;

         getApplication().getDefaultCanvasGraphics().setStorageUnit(orgUnitId);
         getApplication().setStatusStorageUnit(orgUnitId);
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return getResources().getMessage("undo.set_storage_unit");
      }
   }

   public void printInfo(PrintWriter out) throws IOException
   {
      out.println(paths.info());

      out.println("anchor: "+anchor);
      out.println("selected index:"+selectedIndex);
      out.println("drag scale object:"+dragScaleObject);
      out.println("drag scale anchor:"+dragScaleAnchor);
      out.println("drag scale hotspot:"+dragScaleHotspot);
      out.println("drag scale index:"+dragScaleIndex);
      out.println("scanshape:"+scanshape);
      out.println("background image:"+backgroundImage);
      out.println(movePtDialog);
   }

   /**
    * Gets information on selected objects.
    * @return string containing information about selected objects
    */
   public String getSelectedInfo()
   {
      if (paths == null)
      {
         return "no objects available";
      }

      String str = "Image size: "+paths.size()+"\n";

      boolean done = false;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);

         if (object.isSelected())
         {
            str += "Object index: "+i+"\n";
            str += object.info()+"\n\n";
            done = true;
         }
      }

      if (!done)
      {
         str += "No objects selected\n";
      }

      return str;
   }

   public void discardImage()
   {
      if (paths == null)
      {
         // Already been discarded
         return;
      }

      currentPath=null;
      editedPath = null;
      currentSegment=null;
      editedDistortion = null;
      editedDistortionIndex=-1;
      cpedit=null;
      anchor=null;
      editedSegmentDash=null;
      scanshape=null;
      count = 0;
      selectedIndex=-1;
      dragScaleIndex=-1;
      dragScaleHotspot=BBox.HOTSPOT_NONE;
      dragScaleAnchor=null;
      paths = null;

      if (selectPathActionList != null)
      {
         for (CanvasSelectAction action : selectPathActionList)
         {
            action.removeAllButtons();
         }
      }

      if (selectTextActionList != null)
      {
         for (CanvasSelectAction action : selectTextActionList)
         {
            action.removeAllButtons();
         }
      }

      if (selectGroupActionList != null)
      {
         for (CanvasSelectAction action : selectGroupActionList)
         {
            action.removeAllButtons();
         }
      }

      if (selectBitmapActionList != null)
      {
         for (CanvasSelectAction action : selectBitmapActionList)
         {
            action.removeAllButtons();
         }
      }

      if (selectGeneralActionList != null)
      {
         for (CanvasSelectAction action : selectGeneralActionList)
         {
            action.removeAllButtons();
         }
      }

      selectPathActionList=null;
      selectTextActionList=null;
      selectGroupActionList=null;
      selectBitmapActionList=null;
      selectGeneralActionList=null;

      generalActionList = null;
      textConstructionActionList = null;
      editPathActionList = null;
      appActionList = null;
   }

   private Point2D mouse;
   private JDRCanvasCompoundEdit cpedit=null;
   private JDRPath currentPath=null;
   private JDRShape editedPath=null;
   private JDRSegment currentSegment=null;
   private JDRDistortShape editedDistortion = null;
   private int editedDistortionIndex=-1;
   private Point2D anchor;
   private BBox dragBBox=null;
   private volatile JDRGroup paths;
   private int selectedIndex=-1;
   private JDRCompleteObject dragScaleObject=null;
   private JDRPoint dragScaleAnchor=null;
   private int dragScaleHotspot=BBox.HOTSPOT_NONE;
   private int dragScaleIndex = -1;
   private boolean mouseDown=false;

   private BasicStroke editedSegmentDash 
      = new BasicStroke(1.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        1.0f,new float[]{4.0f, 4.0f},0.0f);

   private static Color currentSegmentColor      = Color.gray;
   private static Color currentPathColor         = Color.gray;
   private static Color currentSegmentPointColor = Color.red;
   private static Color currentPathPointColor    = Color.red;
   private static Color typeblockColor           = Color.lightGray;
   private static Color marginColor              = new Color(230,230,230,100);

   private CanvasTextField textField;
   private JDRFont textFieldFont;
   private Font symbolButtonFont;
   protected JDRText currentText;

   private MovePointDialog movePtDialog;

   private JPopupMenu editPathPopupMenu, texteditPathPopupMenu,
                      distortPopupMenu;
   private JPopupMenu selectPopupMenu, selectTextPopupMenu,
                      selectPathPopupMenu, noneSelectedPopupMenu,
                      selectBitmapPopupMenu, selectTextPathPopupMenu;

   private Parshape scanshape;

   private static int count=0;

   private JDRFrame frame_;

   private JMenuItem copyText, cutText;

   private volatile BufferedImage backgroundImage=null;
   private double backgroundImageX=0, backgroundImageY=0;

   private Vector<CanvasAction> generalActionList;
   private Vector<CanvasAction> textConstructionActionList;
   private Vector<CanvasSelectAction> selectPathActionList;
   private Vector<CanvasSelectAction> selectTextActionList;
   private Vector<CanvasSelectAction> selectGroupActionList;
   private Vector<CanvasSelectAction> selectBitmapActionList;
   private Vector<CanvasSelectAction> selectGeneralActionList;
   private Vector<CanvasAction> editPathActionList;

   private Vector<FlowframTkAction> appActionList;

   // for flowframes
   public static final int 
      PAGES_NONE=0, PAGES_ALL=-1, PAGES_EVEN=-2, PAGES_ODD=-3; 
   private int displayPage=PAGES_ALL;

   // hotspot flags (for when shown)
   private static final short hotspotFlags = BBox.SOUTH
                                           | BBox.SOUTH_EAST
                                           | BBox.EAST
                                           | BBox.NORTH_EAST
                                           | BBox.NORTH_WEST
                                           | BBox.SOUTH_WEST;


   public static final Pattern STY_PATTERN =
     Pattern.compile("(\\\\(?:usepackage|RequirePackage))\\s*(?:\\[([^\\]]*)\\])?\\s*\\{([\\w\\-]+)\\}");
}

class CanvasTextField extends JTextField
   implements CaretListener,DocumentListener,
      MouseListener
{
   public CanvasTextField(JDRCanvas c, UndoableEditListener editList)
   {
      super(64);

      canvas = c;
      background = new Color(255,255,0,40);
      foreground = new JDRColor(c.getCanvasGraphics());

      setOpaque(false);
      setHorizontalAlignment(JTextField.LEADING);

      setBorder(null);
      setMargin(new Insets(0,0,0,0));

      addCaretListener(this);
      getDocument().addDocumentListener(this);
      addMouseListener(this);

      el = editList;
      getDocument().addUndoableEditListener(el);

      updateBounds();

      Action action = new AbstractAction()
      {
         public void actionPerformed(ActionEvent evt)
         {
            moveLeft();
         }
      };

      getInputMap(JComponent.WHEN_FOCUSED).put(
         getResources().getAccelerator("action.cursor_left"),
         "moveLeft"
         );

      getActionMap().put("moveLeft", action);

      action = new AbstractAction()
      {
         public void actionPerformed(ActionEvent evt)
         {
            moveRight();
         }
      };

      getInputMap(JComponent.WHEN_FOCUSED).put(
         getResources().getAccelerator("action.cursor_right"),
         "moveRight"
         );

      getActionMap().put("moveRight", action);

      action = new AbstractAction()
      {
         public void actionPerformed(ActionEvent evt)
         {
            shiftLeft();
         }
      };

      getInputMap(JComponent.WHEN_FOCUSED).put(
         getResources().getAccelerator("action.cursor_word_left"),
         "shiftLeft"
         );

      getActionMap().put("shiftLeft", action);

      action = new AbstractAction()
      {
         public void actionPerformed(ActionEvent evt)
         {
            shiftRight();
         }
      };

      getInputMap(JComponent.WHEN_FOCUSED).put(
         getResources().getAccelerator("action.cursor_word_right"),
         "shiftRight"
         );

      getActionMap().put("shiftRight", action);

      getInputMap(JComponent.WHEN_FOCUSED).put(
         getResources().getAccelerator("menu.textarea.insert_symbol"),
         "insert_symbol"
         );

      action = new AbstractAction()
      {
         public void actionPerformed(ActionEvent evt)
         {
            canvas.showSymbolSelector();
         }
      };

      getActionMap().put("insert_symbol", action);
   }

   public void setNoUndoText(String string)
   {
      if (el != null)
      {
         getDocument().removeUndoableEditListener(el);
         setText(string);
         getDocument().addUndoableEditListener(el);
      }
      else
      {
         setText(string);
      }
   }

   public void mouseClicked(MouseEvent e)
   {
   }

   public void mouseEntered(MouseEvent e)
   {
   }

   public void mouseExited(MouseEvent e)
   {
   }

   public void mousePressed(MouseEvent e)
   {
      repaint();
   }

   public void mouseReleased(MouseEvent e)
   {
      repaint();
   }

   public void moveLeft()
   {
      String selection = getSelectedText();

      int pos = getCaretPosition()-1;

      if (pos < 0)
      {
         pos = 0;
      }

      setCaretPosition(pos);

      if (selection != null)
      {
         repaint();
      }
   }

   public void moveRight()
   {
      String selection = getSelectedText();

      int n = getText().length();

      if (n == 0) return;

      int pos = getCaretPosition()+1;

      if (pos > n)
      {
         pos = n;
      }

      setCaretPosition(pos);

      if (selection != null)
      {
         repaint();
      }
   }

   public void shiftLeft()
   {
      int pos = getCaretPosition()-1;

      if (pos < 0)
      {
         return;
      }

      moveCaretPosition(pos);
   }

   public void shiftRight()
   {
      int n = getText().length();

      if (n == 0) return;

      int pos = getCaretPosition()+1;

      if (pos > n)
      {
         return;
      }

      moveCaretPosition(pos);
   }

   public void caretUpdate(CaretEvent e)
   {
      int dot = e.getDot();
      int mark = e.getMark();

      try
      {
         Rectangle caretArea = modelToView(dot);
         Rectangle bounds = getBounds();

         CanvasGraphics cg = canvas.getCanvasGraphics();

         double x = bounds.getX()+caretArea.getX();
         double y = bounds.getY()+caretArea.getY();
         double w = caretArea.getWidth();
         double h = bounds.getHeight();

         bounds.setBounds((int)Math.round(x), (int)Math.round(y), 
                          (int)Math.ceil(w), (int)Math.ceil(h));

         Rectangle canvasVisible = canvas.getVisibleRect();

         double minX = canvasVisible.getX();
         double minY = canvasVisible.getY();
         double maxX = minX+canvasVisible.getWidth();
         double maxY = minY+canvasVisible.getHeight();

         if (!canvasVisible.contains(bounds))
         {
            if (minY < y && y+h <= maxY)
            {
               if (maxX < x
                && x < maxX+canvasVisible.getWidth())
               {
                  canvas.blockScrollRight();
               }
               else if (minX-canvasVisible.getWidth() < x
                     && x+w < minX)
               {
                  canvas.blockScrollLeft();
               }
               else
               {
                  canvas.scrollToComponentLocation(x, y);
               }
            }
            else if (minX < x && x+w <= maxX)
            {
               if (maxY <= y+h && y < maxY+canvasVisible.getHeight())
               {
                  canvas.blockScrollDown();
               }
               else if (minY-canvasVisible.getHeight() < y
                     && y < minY)
               {
                  canvas.blockScrollUp();
               }
               else
               {
                  canvas.scrollToComponentLocation(x, y);
               }
            }
            else
            {
               canvas.scrollToComponentLocation(x, y);
            }
         }
      }
      catch (BadLocationException excp)
      {
         getResources().debugMessage(excp);
      }
      catch (NullPointerException excp)
      {
         getResources().debugMessage(excp);
      }

      repaint(getSelectedArea());
   }

   public void changedUpdate(DocumentEvent e)
   {
      canvas.markAsModified();
      updateBounds();
   }

   public void insertUpdate(DocumentEvent e)
   {
      canvas.markAsModified();
      updateBounds();
   }

   public void removeUpdate(DocumentEvent e)
   {
      canvas.markAsModified();
      updateBounds();
   }

   public void setPosition(double px, double py)
   {
      pos.setLocation(px, py);
      updateBounds();
   }

   public void setPositionY(double py)
   {
      setPosition(pos.getX(), py);
   }

   public Point2D getPosition()
   {
      return pos;
   }

   public void setFont(JDRFont f)
   {
      jdrFont = f;

      CanvasGraphics cg = canvas.getCanvasGraphics();

      // Need to average out the x and y scaling.

      double scale = 0.5*(cg.bpToComponentX(1.0) + cg.bpToComponentY(1.0));

      setFont(new Font(jdrFont.getFamily(), jdrFont.getJavaFontStyle(),
             (int)Math.round(scale*jdrFont.getSize().getValue(JDRUnit.bp))));

      updateBounds();
   }

   public void updateBounds()
   {
      Dimension dim = getSize();
   
      if (dim == null || canvas == null) return;
   
      Graphics2D g2 = null;

      try
      {
         g2 = (Graphics2D)getGraphics();
   
         if (g2 == null) return;
   
         g2.setRenderingHints(canvas.getRenderingHints());

         Font font = getFont();

         FontRenderContext frc = g2.getFontRenderContext();
         FontMetrics fm = g2.getFontMetrics(font);
   
         double width = dim.width;
   
         try
         {
            Caret caret = getCaret();
            Rectangle caretArea = modelToView(caret.getDot());
            width = caretArea.getWidth();
         }
         catch (BadLocationException ble)
         {
         }
         catch (NullPointerException npe)
         {
         }
   
         String text = getText();
         int n = text.length();
   
         text += widestChar;
   
         if (!text.isEmpty())
         {
            if (Character.isSpaceChar(text.charAt(0)))
            {
               text = widestChar+text.substring(1);
            }
   
            TextLayout layout = new TextLayout(text, font, frc);
            Rectangle2D bounds = layout.getBounds();
            width += bounds.getWidth();
         }

         maxAscent = fm.getMaxAscent();
         maxDescent = fm.getMaxDescent();

         CanvasGraphics cg = canvas.getCanvasGraphics();

         double x = pos.getX() + cg.getStorageOriginX();
         double y = pos.getY() + cg.getStorageOriginY();

         setBounds((int)Math.round(cg.storageToComponentX(x)),
                   (int)Math.round(cg.storageToComponentY(y)
                                   -maxAscent),
                   (int)Math.ceil(width),
                   (int)Math.ceil((maxAscent+maxDescent)));
      }
      finally
      {
         if (g2 != null)
         {
            g2.dispose();
         }
      }
   }

   protected void paintComponent(Graphics g)
   {
      Graphics2D g2 = (Graphics2D)g;

      Rectangle bounds = getVisibleRect();
      Paint oldPaint = g2.getPaint();
      AffineTransform oldAf = g2.getTransform();

      CanvasGraphics cg = canvas.getCanvasGraphics();

      g2.setPaint(background);
      g2.fill(bounds);

      TextLayout layout = null;

      if (!getText().equals(""))
      {
         RenderingHints oldHints = g2.getRenderingHints();

         g2.setRenderingHints(canvas.getRenderingHints());

         Font font = getFont();

         FontMetrics fm = g2.getFontMetrics(font);
         maxDescent = fm.getMaxDescent();

         FontRenderContext frc = g2.getFontRenderContext();
         layout = new TextLayout(getText(), font, frc);
         BBox box = new BBox(cg, layout.getBounds());

         maxAscent = fm.getMaxAscent();

         g2.translate(0, maxAscent);

         g2.setPaint(foreground.getPaint(box));

         layout.draw(g2, 0, 0);
         g2.translate(0, -maxAscent);

         g2.setTransform(oldAf);

         g2.setRenderingHints(oldHints);
      }

      Caret caret = getCaret();

      g.setColor(getCaretColor());

      caret.paint(g);

      if (layout != null)
      {
         int start = getSelectionStart();
         int end   = getSelectionEnd();

         if (start != end)
         {
            g2.setXORMode(foreground.getColor());

            g2.setColor(getSelectedTextColor());

            g2.fill(getSelectedArea());

            g2.setPaintMode();
         }
      }

      g2.setPaint(oldPaint);
   }

   public Rectangle getSelectedArea()
   {
      int start = getSelectionStart();
      int end   = getSelectionEnd();

      int startX = 0;

      int endX = 0;

      try
      {
         Rectangle startView = modelToView(start);

         startX = startView.x;
      }
      catch (BadLocationException e)
      {
      }

      try
      {
         Rectangle endView = modelToView(end);

         endX = endView.x+endView.width;
      }
      catch (BadLocationException e)
      {
      }

      Rectangle area 
         = new Rectangle(startX, 0, endX-startX, getHeight());

      return area;
   }

   public Color getForeground()
   {
      return null;
   }

   public void setTextPaint(JDRPaint paint)
   {
      foreground = paint;
   }

   public JDRPaint getTextPaint()
   {
      return foreground;
   }

   public int getMaxAscent()
   {
      return maxAscent;
   }

   public int getMaxDescent()
   {
      return maxDescent;
   }

   public JDRResources getResources()
   {
      return canvas.getResources();
   }

   private Point2D pos = new Point2D.Double(0,0);

   private JDRFont jdrFont;

   private int maxAscent=0, maxDescent=0;

   private JDRCanvas canvas;
   private Paint background;
   private JDRPaint foreground;

   private UndoableEditListener el=null;

   // I initially used char, but using a String allows
   // extra flexibility.
   public static String widestChar="M";
}

