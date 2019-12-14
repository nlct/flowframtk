// File          : EditPathAction.java
// Description   : Action for path editing
// Date          : 2012-05-01
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006 Nicola L.C. Talbot

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

import java.util.Vector;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;

public class EditPathAction extends CanvasAction
{
   public EditPathAction(JDRCanvas canvas, FlowframTkAction action)
   {
      super(canvas, action);
      this.validSegmentFlag = SEGMENT_FLAG_ANY;
      this.validControlFlag = CONTROL_FLAG_ANY;
      this.validSegmentListener = null;
      this.validControlListener = null;
   }

   public EditPathAction(JDRCanvas canvas,
     String actionCommand,
     KeyStroke keyStroke,
     FlowframTkActionListener listener,
     int validToolsFlag,
     byte validEditFlag,
     byte validConstructionFlag,
     int validSelectionFlag,
     int[] validSelectionNumbers,
     int validSegmentFlag,
     int validControlFlag)
   {
      super(canvas, actionCommand, keyStroke, listener,
       validToolsFlag, validEditFlag, validConstructionFlag,
       validSelectionFlag, validSelectionNumbers);

      this.validSegmentFlag = validSegmentFlag;
      this.validControlFlag = validControlFlag;
      this.validSegmentListener = null;
      this.validControlListener = null;
   }

   public static JMenuItem createMenuItem(JDRCanvas canvas,
     String parentId,
     String name,
     FlowframTkActionListener listener)
   {
      return createMenuItem(canvas, parentId, name, listener,
        SELECT_FLAG_SHAPE, SEGMENT_FLAG_ANY);
   }

   public static JMenuItem createMenuItem(JDRCanvas canvas,
     String parentId,
     String name,
     FlowframTkActionListener listener,
     int validSelectFlag, int validSegmentFlag)
   {
      return createMenuItem(canvas, parentId, name, listener,
         validSelectFlag, validSegmentFlag, CONTROL_FLAG_ANY);
   }

   public static JMenuItem createMenuItem(JDRCanvas canvas,
     String parentId,
     String name,
     FlowframTkActionListener listener,
     int validSelectFlag, int validSegmentFlag,
     ValidSegmentListener segmentListener)
   {
      return createMenuItem(canvas, parentId, name, listener,
         validSelectFlag, SELECTION_IGNORE_COUNT,
         validSegmentFlag, CONTROL_FLAG_ANY,
         segmentListener, null);
   }

   public static JMenuItem createMenuItem(JDRCanvas canvas,
     String parentId,
     String name,
     FlowframTkActionListener listener,
     int validSelectFlag, int validSegmentFlag,
     int validControlFlag,
     ValidSegmentListener segmentListener)
   {
      return createMenuItem(canvas, parentId, name, listener,
         validSelectFlag, SELECTION_IGNORE_COUNT,
         validSegmentFlag, validControlFlag,
         segmentListener, null);
   }

   public static JMenuItem createMenuItem(JDRCanvas canvas,
     String parentId,
     String name,
     FlowframTkActionListener listener,
     int validSelectFlag, int[] selection, int validSegmentFlag)
   {
      return createMenuItem(canvas, parentId, name, listener,
         validSelectFlag, selection, validSegmentFlag, CONTROL_FLAG_ANY);
   }

   public static JMenuItem createMenuItem(JDRCanvas canvas,
     String parentId,
     String name,
     FlowframTkActionListener listener,
     int validSelectFlag, int validSegmentFlag,
     int validControlFlag)
   {
      return createMenuItem(canvas, parentId, name, listener,
        validSelectFlag, SELECTION_IGNORE_COUNT,
        validSegmentFlag, validControlFlag);
   }

   public static JMenuItem createMenuItem(JDRCanvas canvas,
     String parentId,
     String name,
     FlowframTkActionListener listener,
     int validSelectFlag, int validSegmentFlag,
     int validControlFlag,
     ValidControlListener controlListener)
   {
      return createMenuItem(canvas, parentId, name, listener,
        validSelectFlag, SELECTION_IGNORE_COUNT,
        validSegmentFlag, validControlFlag,
        null, controlListener);
   }

   public static JMenuItem createMenuItem(JDRCanvas canvas,
     String parentId,
     String name,
     FlowframTkActionListener listener,
     int validSelectFlag, int[] selection, int validSegmentFlag,
     int validControlFlag)
   {
      return createMenuItem(canvas, parentId, name, listener,
         validSelectFlag, selection, validSegmentFlag,
         validControlFlag, null, null);
   }

   public static JMenuItem createMenuItem(JDRCanvas canvas,
     String parentId,
     String name,
     FlowframTkActionListener listener,
     int validSelectFlag, int[] selection, int validSegmentFlag,
     int validControlFlag,
     ValidSegmentListener segmentListener,
     ValidControlListener controlListener)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      EditPathAction action = new EditPathAction(canvas,
         name, canvas.getResources().getAccelerator(menuId),
         listener, TOOL_FLAG_SELECT, EDIT_FLAG_PATH,
         CONSTRUCTION_FLAG_NONE, validSelectFlag,
         selection, validSegmentFlag,
         validControlFlag);

      action.setValidSegmentListener(segmentListener);
      action.setValidControlListener(controlListener);

      action.getCanvas().addCanvasAction(action);

      return action.createMenuItem(menuId, "tooltip."+name);
   }

   public static JCheckBoxMenuItem createCheckBoxMenuItem(JDRCanvas canvas,
     String parentId,
     String name, boolean selected,
     FlowframTkActionListener listener,
     int validSelectFlag, int validSegmentFlag)
   {
      return createCheckBoxMenuItem(canvas, parentId, name,
        selected, listener, validSelectFlag,
        SELECTION_IGNORE_COUNT, validSegmentFlag);
   }

   public static JCheckBoxMenuItem createCheckBoxMenuItem(JDRCanvas canvas,
     String parentId,
     String name, boolean selected,
     FlowframTkActionListener listener,
     int validSelectFlag, int[] selection, int validSegmentFlag)
   {
      return createCheckBoxMenuItem(canvas, parentId, name,
        selected, listener, validSelectFlag, selection,
        validSegmentFlag, CONTROL_FLAG_ANY);
   }

   public static JCheckBoxMenuItem createCheckBoxMenuItem(JDRCanvas canvas,
     String parentId,
     String name, boolean selected,
     FlowframTkActionListener listener,
     int validSelectFlag, int[] selection, int validSegmentFlag,
     int validControlFlag)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      EditPathAction action = new EditPathAction(canvas,
         name, canvas.getResources().getAccelerator(menuId),
         listener, TOOL_FLAG_SELECT, EDIT_FLAG_PATH,
         CONSTRUCTION_FLAG_NONE, validSelectFlag,
         selection, validSegmentFlag, validControlFlag);

      action.getCanvas().addCanvasAction(action);

      return action.createCheckBoxMenuItem(menuId, selected,
         "tooltip."+name);
   }

   public static JMenu createMenu(JDRCanvas canvas,
     String parentId,
     String name,
     int validSelectFlag, int validSegmentFlag)
   {
      return createMenu(canvas, parentId, name, validSelectFlag,
        validSegmentFlag, CONTROL_FLAG_ANY);
   }

   public static JMenu createMenu(JDRCanvas canvas,
     String parentId,
     String name,
     int validSelectFlag, int validSegmentFlag, int validControlFlag)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      EditPathAction action = new EditPathAction(canvas,
         name, canvas.getResources().getAccelerator(menuId),
         null, TOOL_FLAG_SELECT, EDIT_FLAG_PATH,
         CONSTRUCTION_FLAG_NONE, validSelectFlag,
         SELECTION_IGNORE_COUNT, validSegmentFlag, 
         validControlFlag);

      action.getCanvas().addCanvasAction(action);

      return action.createMenu(menuId, "tooltip."+name);
   }

   public boolean isValidSegment(JDRSelection currentSelection,
      int currentSegmentFlag)
   {
      if ((currentSegmentFlag & validSegmentFlag) == 0)
      {
         return false;
      }

      if (validSegmentListener != null)
      {
         if (!validSegmentListener.isValid(currentSelection,
                currentSegmentFlag))
         {
            return false;
         }
      }

      return true;
   }

   public boolean isValidControl(JDRSelection currentSelection,
      int currentSegmentFlag,
      int currentControlFlag)
   {
      if ((currentControlFlag & validControlFlag) == 0)
      {
         return false;
      }

      if (validControlListener != null)
      {
         if (!validControlListener.isValid(currentSelection,
                currentSegmentFlag,
                currentControlFlag))
         {
            return false;
         }
      }

      return true;
   }

   public boolean isValid(boolean ioInProgress, int currentTool,
     byte currentEditFlag, byte currentConstructionFlag,
     JDRSelection currentSelection)
   {
      JDRShape editedShape = getCanvas().getEditedPath();

      if (editedShape == null)
      {
         return false;
      }

      int currentSegmentFlag = editedShape.getSelectedSegmentFlag();

      if (!isValidSegment(currentSelection, currentSegmentFlag))
      {
         return false;
      }

      int currentControlFlag = editedShape.getSelectedControlFlag();

      if (!isValidControl(currentSelection, currentSegmentFlag,
              currentControlFlag))
      {
         return false;
      }

      return super.isValid(ioInProgress, currentTool,
        currentEditFlag, currentConstructionFlag,
        currentSelection);
   }

   public void setValidSegmentListener(ValidSegmentListener listener)
   {
      validSegmentListener = listener;
   }

   public void setValidControlListener(ValidControlListener listener)
   {
      validControlListener = listener;
   }

   private int validSegmentFlag = SEGMENT_FLAG_ANY;
   private int validControlFlag = CONTROL_FLAG_ANY;

   private ValidSegmentListener validSegmentListener;
   private ValidControlListener validControlListener;
}
