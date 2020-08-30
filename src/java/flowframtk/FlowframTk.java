// File          : FlowframTk.java
// Purpose       : Main GUI for FlowframTk
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

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

import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;  

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.awt.image.*;
import java.awt.font.*;
import java.awt.dnd.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.undo.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.TransferHandler;
import javax.swing.plaf.basic.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.imageio.*;
import javax.help.*;  

import java.beans.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.filter.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.dialog.*;

public class FlowframTk extends JFrame
   implements InternalFrameListener,
              UndoableEditListener,
              SymbolSelectorListener,
              DropTargetListener,
              JDRApp,JDRConstants
{
   // set up GUI
   public FlowframTk(FlowframTkInvoker invoker, boolean showWelcome)

   {
      super(invoker.getName());

      this.invoker = invoker;

      try
      {
         init();
      }
      catch (Throwable e)
      {
         getResources().internalError(null, e);
      }

      if (showWelcome)
      {
         invoker.showWelcome();
      }

      setVisible(true);

      try
      {
         getCurrentFrame().setSelected(true);
      }
      catch (PropertyVetoException e)
      {
         debugMessage(e);
      }
   }

   private void init()
   {
      FlowframTkSettings appSettings = invoker.getSettings();

      JDRResources resources = invoker.getResources();

      setIconImage(resources.getSmallAppIcon().getImage());

      // set up the desktop

      theDesktop = new JDesktopPane();
      theDesktop.setBackground(Color.lightGray);
      getContentPane().add(theDesktop);

      theDesktop.setDropTarget(new DropTarget(theDesktop, 
       DnDConstants.ACTION_COPY, this, true));

      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

      addWindowListener(new WindowAdapter()
         {
            public void windowActivated(WindowEvent evt)
            {
               JDRFrame frame =
                  (JDRFrame)theDesktop.getSelectedFrame();

               if (frame != null) frame.requestFocusInWindow();
            }

            public void windowClosing(WindowEvent evt)
            {
               quit();
            }
         });

      // set dimensions and location

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension d = tk.getScreenSize();
      int screenHeight = d.height;
      int screenWidth  = d.width;
      int width  = 3*screenHeight/4;
      int height = screenWidth/2;
      setSize(width,height);
      setLocation((screenWidth-width)/2, (screenHeight-height)/2);

      // set up available font families

      GraphicsEnvironment env = 
         GraphicsEnvironment.getLocalGraphicsEnvironment();
      availableFontFamilies = env.getAvailableFontFamilyNames();

      // set up annotation font

      try
      {
         JDRCompleteObject.annoteFont = new Font(
            resources.getString("font.annote.name"), 0,
            resources.getInt("font.annote.size"));
      }
      catch (NumberFormatException e)
      {
         resources.internalError(this,
            resources.getMessage("internal_error.integer_key",
               "font.annote.size"));
      }

      if (!invoker.isPrintDisabled())
      {
         // set up print request attribute set
         printRequestAttributeSet = new HashPrintRequestAttributeSet();

         getPrintService(appSettings.getPaper());
      }
      else
      {
         printRequestAttributeSet = null;
      }

      // initialise help set

      initializeHelp(this);

      // load list of recent files

      loadRecentFiles();

      // Initialise action lists

      generalActionList = new Vector<FlowframTkAction>(100);
      toolActionList = new Vector<FlowframTkAction>(100);
      selectActionList = new Vector<FlowframTkAction>(100);

      // Initialise application selector dialog

      appSelector = new JDRAppSelector(this);

      Container contentPane = getContentPane();

      // create and add status bar

      statusBar = new StatusBar(this);
      contentPane.add(statusBar, "South");

      // create and add horizontal tool bar

      Box toolBar = Box.createHorizontalBox();

      hSlidingBar 
         = new SlidingToolBar(resources, toolBar, SlidingToolBar.HORIZONTAL);

      contentPane.add(hSlidingBar, "North");

      // create and add vertical tool bar

      Box sidePanel = Box.createVerticalBox();

      vSlidingBar = 
         new SlidingToolBar(resources, sidePanel, SlidingToolBar.VERTICAL);

      contentPane.add(vSlidingBar, appSettings.getVerticalToolBarLocation());

      invoker.setStartupInfo(resources.getString("message.init_menus"));

      invoker.setStartupDeterminate(185);

      // create menu bar, menu and menu item

      JMenuBar mbar = new JMenuBar();
      setJMenuBar(mbar);

      // File Menu

      fileM = resources.createAppMenu("file");
      mbar.add(fileM);

      incStartupProgress(fileM);

      // New

      newButtonItem = FlowframTkAction.createButtonItem(this,
         "file", "new", toolBar, fileM,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               addFrame(invoker.getSettings().getCanvasGraphics());
            }
         });

      incStartupProgress(fileM, newButtonItem);

      hSlidingBar.setUnitIncrement( 
         newButtonItem.getButtonPreferredWidth());

      // Open

      openButtonItem = FlowframTkAction.createButtonItem(this,
         "file", "open", toolBar, fileM,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               openImage();
            }
         });


      incStartupProgress(fileM, openButtonItem);

      // open dialog box

      openjdrFC = new JDRFileChooser(resources, false);

      openjdrFC.setCurrentDirectory(new File(appSettings.startDir));

      jdrAjrFileFilter = new JdrAjrFileFilter(
         resources.getString("filter.jdrajr"));

      openjdrFC.addChoosableFileFilter(jdrAjrFileFilter);
      openjdrFC.setFileFilter(jdrAjrFileFilter);

      // save as dialog box

      savejdrFC = new JDRFileChooser(resources);

      savejdrFC.setCurrentDirectory(new File(appSettings.startDir));
      savejdrFC.setAcceptAllFileFilterUsed(false);

      jdrFileFilter = new JdrFileFilter(
         resources.getString("filter.jdr"));
      savejdrFC.addChoosableFileFilter(jdrFileFilter);

      ajrFileFilter = new AjrFileFilter(
         resources.getString("filter.ajr"));
      savejdrFC.addChoosableFileFilter(ajrFileFilter);

      int version = (int)Math.round(10.0*(JDRAJR.CURRENT_VERSION-0.1f))-10;

      int numOldVersions = version+1;

      oldJdrFileFilter = new JdrFileFilter[numOldVersions];
      oldAjrFileFilter = new AjrFileFilter[numOldVersions];

      for (int i = numOldVersions-1; i >= 0; i--)
      {
         float thisVersion = (float)(i*0.1+1.0);

         oldJdrFileFilter[i] = new JdrFileFilter(
            resources.getMessage("filter.version.jdr", thisVersion), 
            thisVersion);
         savejdrFC.addChoosableFileFilter(oldJdrFileFilter[i]);

         oldAjrFileFilter[i] = new AjrFileFilter(
           resources.getMessage("filter.version.ajr", thisVersion), 
           thisVersion);
         savejdrFC.addChoosableFileFilter(oldAjrFileFilter[i]);
      }

      savejdrFC.setFileFilter(jdrFileFilter);

      // Recent Files

      recentM = resources.createAppMenu("file.recent");
      fileM.add(recentM);

      recentM.addMenuListener(new MenuListener()
         {
            public void menuSelected(MenuEvent evt)
            {
               setRecentFiles();
            }
            public void menuDeselected(MenuEvent evt)
            {
            }
            public void menuCanceled(MenuEvent evt)
            {
            }
         });

      incStartupProgress(fileM, recentM);

      // Image Description

      imageDescriptionItem = FlowframTkAction.createMenuItem(this,
         "file", "image_description", fileM, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayImageDescriptionDialog(
                  action.getFrame().getAllPaths());
            }
         }
      );

      imageDescriptionDialog
         = new DescriptionDialogBox(this, "imagedescription");


      incStartupProgress(fileM, imageDescriptionItem);

      // Save

      saveButtonItem = FlowframTkAction.createButtonItem(this,
         "file", "save", toolBar, fileM,
         TOOL_FLAG_ANY, EDIT_FLAG_NONE,
         FlowframTkAction.CONSTRUCTION_FLAG_NONE_OR_TEXT,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT,
         true, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               saveImage();
            }
         });

      incStartupProgress(fileM, saveButtonItem);

      // Save As

      saveAsItem = FlowframTkAction.createMenuItem(this,
         "file", "save_as", fileM,
         TOOL_FLAG_ANY, EDIT_FLAG_NONE,
         FlowframTkAction.CONSTRUCTION_FLAG_NONE_OR_TEXT,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT,
         true, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               saveAs();
            }
         });

      incStartupProgress(fileM, saveAsItem);

      // Export

      exportItem = FlowframTkAction.createMenuItem(this,
         "file", "export", fileM, 
         TOOL_FLAG_ANY, EDIT_FLAG_NONE,
         FlowframTkAction.CONSTRUCTION_FLAG_NONE_OR_TEXT,
         SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT,
         true, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               exportImage();
            }
         });

      incStartupProgress(fileM, exportItem);

      exportPngDialog = new ExportPngDialog(this);

      exportFC = new JDRFileChooser(resources);

      exportFC.setDialogTitle(
         resources.getString("export.title"));

      exportFC.setCurrentDirectory(new File(appSettings.startDir));
      exportFC.setAcceptAllFileFilterUsed(false);

      pgfFileFilter = new TeXFileFilter(
         resources.getString("filter.pgf"));
      exportFC.addChoosableFileFilter(pgfFileFilter);

      clsFileFilter = new ClsFileFilter(
         resources.getString("filter.cls"));
      exportFC.addChoosableFileFilter(clsFileFilter);

      styFileFilter = new StyFileFilter(
         resources.getString("filter.sty"));
      exportFC.addChoosableFileFilter(styFileFilter);

      pgfDocFileFilter = new TeXFileFilter(
         resources.getString("filter.pgfdoc"));
      exportFC.addChoosableFileFilter(pgfDocFileFilter);

      pgfEncapDocFileFilter = new TeXFileFilter(
         resources.getString("filter.pgfencapdoc"));
      exportFC.addChoosableFileFilter(pgfEncapDocFileFilter);

      pngFileFilter = new PngFileFilter(
         resources.getString("filter.png"));
      exportFC.addChoosableFileFilter(pngFileFilter);

      epsFileFilter = new EpsFileFilter(
         resources.getString("filter.eps"));
      exportFC.addChoosableFileFilter(epsFileFilter);

      pdfFileFilter = new PdfFileFilter(
         resources.getString("filter.pdf"));
      exportFC.addChoosableFileFilter(pdfFileFilter);

      svgFileFilter = new SvgFileFilter(
         resources.getString("filter.svg"));
      exportFC.addChoosableFileFilter(svgFileFilter);

      exportFC.setFileFilter(pgfFileFilter);

      texFC = new JDRFileChooser(resources);

      texFC.setCurrentDirectory(new File(appSettings.startDir));
      texFC.setAcceptAllFileFilterUsed(false);
      texFileFilter = new TeXFileFilter(
         resources.getString("filter.tex"));
      texFC.addChoosableFileFilter(texFileFilter);

      // Import

      // not fully implemented

      if (invoker.isExperimentalMode() || resources.debugMode)
      {
         importItem = FlowframTkAction.createMenuItem(this,
           "file", "import", fileM,
            new FlowframTkActionListener()
            {
               public void doAction(FlowframTkAction action, ActionEvent evt)
               {
                  importImage();
               }
            });

         incStartupProgress(fileM, importItem);

         importFC = new JDRFileChooser(resources, false);
         importFC.setDialogTitle(
            resources.getString("import.title"));

         importFC.setCurrentDirectory(
            new File(appSettings.startDir));
         importFC.setAcceptAllFileFilterUsed(false);

         importFC.addChoosableFileFilter(epsFileFilter);

      }

      // Printer Page Setup dialog

      pageDialogItem = FlowframTkAction.createMenuItem(this,
         "file", "page", fileM, true, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               pageDialog(action.getFrame());
            }
         });

      incStartupProgress(fileM, pageDialogItem);

      // Print

      printItem = FlowframTkAction.createMenuItem(this,
        "file", "print", fileM, true, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().print();
            }
         });


      incStartupProgress(fileM, printItem);

      if (invoker.isPrintDisabled())
      {
         printItem.setEnabled(false);
      }

      fileM.addSeparator();

      JMenuItem showMessagesItem = FlowframTkAction.createMenuItem(this,
         "file", "messages", fileM,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               getMessageSystem().displayMessages();
            }
         });

      fileM.addSeparator();

      incStartupProgress(fileM, showMessagesItem);

      // Close

      closeItem = FlowframTkAction.createMenuItem(this,
        "file", "close", fileM, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               close();
            }
         });

      incStartupProgress(fileM, closeItem);

      discardDB = new DiscardDialogBox(this);


      // Quit ^Q

      quitItem = FlowframTkAction.createMenuItem(this,
        "file", "quit", fileM, false, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               quit();
            }
         });

      incStartupProgress(fileM, quitItem);

      // Edit Menu

      editM = FlowframTkAction.createMenu(this, "edit",
         TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      mbar.add(editM);

      incStartupProgress(editM);

      //initialise undo/redo system

      undoManager = new UndoManager();
      undoSupport = new UndoableEditSupport();
      undoSupport.addUndoableEditListener(this);

      // Undo

      undoItem = FlowframTkAction.createMenuItem(this,
         "edit", "undo", editM,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               Cursor oldCursor = getCursor();
               setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
               if (undoManager.canUndo())
               {
                  undoManager.undo();
               }

               refreshUndoRedo();
               setCursor(oldCursor);
            }
         });

      incStartupProgress(editM, undoItem);

      // Redo

      redoItem = FlowframTkAction.createMenuItem(this,
        "edit", "redo", editM,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               Cursor oldCursor = getCursor();
               setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
               if (undoManager.canRedo())
               {
                  undoManager.redo();
               }

               refreshUndoRedo();
               setCursor(oldCursor);
            }
         });

      incStartupProgress(editM, redoItem);

      refreshUndoRedo();

      editM.addSeparator();

      // Select All

      selectAllButtonItem = FlowframTkAction.createButtonItem(this,
        "edit", "select_all",
        toolBar, editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, 
         FlowframTkAction.CONSTRUCTION_FLAG_NONE,
         SELECT_FLAG_ANY, FlowframTkAction.SELECTION_IGNORE_COUNT,
         true, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().selectAll();
            }
         });

      incStartupProgress(editM, selectAllButtonItem);

      // Deselect All

      deselectAllItem = FlowframTkAction.createMenuItem(this,
         "edit", "deselect_all", editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_IGNORE_COUNT,
          true, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().deselectAll();
            }
         });

      incStartupProgress(editM, deselectAllItem);

      // Move By

      moveByItem = FlowframTkAction.createMenuItem(this,
         "edit", "moveby", editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_IGNORE_COUNT,
          true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               showMoveByDialog();
            }
         });

      incStartupProgress(editM, moveByItem);

      moveByDialog = new MoveByDialogBox(this);


      // Cut

      cutButtonItem = FlowframTkAction.createButtonItem(this,
         "edit", "cut", toolBar, editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_IGNORE_COUNT,
         true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().cutSelectedPaths();
            }
         });


      incStartupProgress(editM, cutButtonItem);

      // Copy

      copyButtonItem = FlowframTkAction.createButtonItem(this,
         "edit", "copy", toolBar, editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_IGNORE_COUNT,
         true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().copySelectedPaths();
            }
         });

      incStartupProgress(editM, copyButtonItem);

      // Paste

      pasteButtonItem = FlowframTkAction.createButtonItem(this,
         "edit", "paste", toolBar, editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_ANY,
        FlowframTkAction.SELECTION_IGNORE_COUNT,
         true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().pasteSelectedPaths();
            }
         });

      incStartupProgress(editM, pasteButtonItem);

      // Object Description

      objectDescriptionItem = FlowframTkAction.createMenuItem(this, 
        "edit", "description", editM,
        TOOL_FLAG_SELECT, EDIT_FLAG_NONE, 
        SELECT_FLAG_OBJECT,
        FlowframTkAction.SELECTION_SINGLE_OBJECT,
        true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayObjectDescriptionDialog(
                  action.getFrame().getSelectedObject());
            }
         }
        );

      objectDescriptionDialog
         = new DescriptionDialogBox(this, "objectdescription");


      editM.addSeparator();

      incStartupProgress(editM, objectDescriptionItem);

      // JDRPath submenu

      pathM = FlowframTkAction.createMenu(this, 
         null, "edit.path", editM,
         TOOL_FLAG_SELECT, 
         EDIT_FLAG_NONE_OR_PATH,
         SELECT_FLAG_SHAPE | SELECT_FLAG_NON_TEXTUAL_SHAPE, 
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      incStartupProgress(editM, pathM);

      // Edit JDRPath

      editPathButtonItem = FlowframTkAction.createToggleButtonItem(this,
         "edit", "path.edit", false, "tooltip.edit_path", toolBar, pathM,
         "edit.path.edit",
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE_OR_PATH,
         SELECT_FLAG_SHAPE, 
        FlowframTkAction.SELECTION_SINGLE_SHAPE_NO_GROUP,
        true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               JDRCanvas canvas = action.getCanvas();

               if (canvas.isInEditMode())
               {
                  canvas.finishEditPath();
               }
               else
               {
                  canvas.editPath();
               }
            }
         });

      incStartupProgress(editM, pathM, editPathButtonItem);

      // JDRLine colour

      linePaintItem = FlowframTkAction.createMenuItem(this,
         "edit.path", "line_colour", pathM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayLinePaintChooser();
            }
         });

      linePaintChooserBox = new LinePaintSelector(this);

      incStartupProgress(editM, pathM, linePaintItem);

      // Edit Line Style sub menu

      lineStyleM = FlowframTkAction.createMenu(this, 
         "edit", "path.style", pathM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      incStartupProgress(editM, pathM, lineStyleM);

      // all styles

      lineStyleItem = FlowframTkAction.createMenuItem(this,
         "edit", "path.style.all", lineStyleM,
          TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
          SELECT_FLAG_NON_TEXTUAL_SHAPE,
          FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               lineStyleChooserBox.initialise();
            }
         });

      lineStyleChooserBox = new LineStyleSelector(this);

      incStartupProgress(editM, pathM, lineStyleM, lineStyleItem);

      // all markers

      allMarkersItem = FlowframTkAction.createMenuItem(this,
         "edit.path.style", "all_markers", lineStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayAllMarkerChooser();
            }
         });

      allMarkersChooserBox = new ArrowStyleSelector(this,
         ArrowStylePanel.ALL);

      lineStyleM.addSeparator();

      incStartupProgress(editM, pathM, lineStyleM, allMarkersItem);

      // line width

      lineWidthItem = FlowframTkAction.createMenuItem(this,
         "edit.path.style", "linewidth", lineStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayLineWidthChooser();
            }
         });

      lineWidthChooserBox = new LineWidthSelector(this);

      incStartupProgress(editM, pathM, lineStyleM, lineWidthItem);

      // dash pattern

      dashItem = FlowframTkAction.createMenuItem(this,
         "edit.path.style", "dashpattern", lineStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayDashPatternChooser();
            }
         });

      dashPatternChooserBox = new DashPatternSelector(this);

      incStartupProgress(editM, pathM, lineStyleM, dashItem);

      // Cap style sub menu

      JMenu capM = FlowframTkAction.createMenu(this,
         "edit.path.style", "capstyle", lineStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      capM.addMenuListener(new MenuListener()
         {
            public void menuSelected(MenuEvent evt)
            {
               JDRFrame frame = getCurrentFrame();
               if (frame==null) return;

               JDRBasicStroke stroke = frame.getSelectedStroke();

               if (stroke != null)
               {
                  switch (stroke.getCapStyle())
                  {
                     case BasicStroke.CAP_BUTT:
                        capButtItem.setSelected(true);
                     break;
                     case BasicStroke.CAP_ROUND:
                        capRoundItem.setSelected(true);
                     break;
                     case BasicStroke.CAP_SQUARE:
                        capSquareItem.setSelected(true);
                     break;
                  }
               }
            }
            public void menuDeselected(MenuEvent evt)
            {
            }
            public void menuCanceled(MenuEvent evt)
            {
            }
         });

      ButtonGroup capGroup = new ButtonGroup();

      incStartupProgress(editM, pathM, lineStyleM, capM);

      // butt cap

      capButtItem = FlowframTkAction.createRadioButtonMenuItem(this,
         "edit.path.style", "capstyle.butt", capM, capGroup,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().setSelectedCapStyle(BasicStroke.CAP_BUTT);
            }
         });

      incStartupProgress(editM, pathM, lineStyleM, capM, capButtItem);

      // round cap

      capRoundItem = FlowframTkAction.createRadioButtonMenuItem(this,
         "edit.path.style", "capstyle.round", capM, capGroup,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().setSelectedCapStyle(BasicStroke.CAP_ROUND);
            }
         });

      incStartupProgress(editM, pathM, lineStyleM, capM, capRoundItem);

      // square cap

      capSquareItem = FlowframTkAction.createRadioButtonMenuItem(this,
         "edit.path.style", "capstyle.square", capM, capGroup,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().setSelectedCapStyle(BasicStroke.CAP_SQUARE);
            }
         });

      incStartupProgress(editM, pathM, lineStyleM, capM, capSquareItem);

      // Join Style

      joinItem = FlowframTkAction.createMenuItem(this,
         "edit.path.style", "joinstyle", lineStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayJoinStyleChooser();
            }
         }
         );

      joinStyleChooserBox = new JoinStyleSelector(this);

      incStartupProgress(editM, pathM, lineStyleM, joinItem);

      // start arrow style

      startArrowItem = FlowframTkAction.createMenuItem(this,
        "edit", "path.style.startarrow", lineStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayStartMarkerChooser();
            }
         }
        );

      startArrowChooserBox = new ArrowStyleSelector(this,
         ArrowStylePanel.START);

      incStartupProgress(editM, pathM, lineStyleM, startArrowItem);

      // mid arrow style

      midArrowItem = FlowframTkAction.createMenuItem(this,
        "edit", "path.style.midarrow", lineStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayMidMarkerChooser();
            }
         }
        );

      midArrowChooserBox = new ArrowStyleSelector(this,
         ArrowStylePanel.MID);

      incStartupProgress(editM, pathM, lineStyleM, midArrowItem);

      // end arrow style

      endArrowItem = FlowframTkAction.createMenuItem(this,
        "edit", "path.style.endarrow", lineStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayEndMarkerChooser();
            }
         }
        );

      endArrowChooserBox = new ArrowStyleSelector(this,
         ArrowStylePanel.END);

      incStartupProgress(editM, pathM, lineStyleM, endArrowItem);

      // winding rule sub menu

      JMenu windingM = FlowframTkAction.createMenu(this, 
        "edit.path.style", "windingrule", lineStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      ButtonGroup windingGroup = new ButtonGroup();

      windingM.addMenuListener(new MenuListener()
         {
            public void menuSelected(MenuEvent evt)
            {
               JDRFrame frame = getCurrentFrame();
               if (frame==null) return;

               JDRStroke stroke = frame.getSelectedStroke();

               if (stroke != null)
               {
                  switch (stroke.getWindingRule())
                  {
                     case GeneralPath.WIND_EVEN_ODD:
                        windingEvenOddItem.setSelected(true);
                     break;
                     case GeneralPath.WIND_NON_ZERO:
                        windingNonZeroItem.setSelected(true);
                     break;
                  }
               }
            }
            public void menuDeselected(MenuEvent evt)
            {
            }
            public void menuCanceled(MenuEvent evt)
            {
            }
         });

      incStartupProgress(editM, pathM, lineStyleM, windingM);

      // even-odd

      windingEvenOddItem = FlowframTkAction.createRadioButtonMenuItem(this,
         "edit.path.style", "windingrule.evenodd", windingM,
         windingGroup,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().setSelectedWindingRule(
                  GeneralPath.WIND_EVEN_ODD);
            }
         });

      incStartupProgress(editM, pathM, lineStyleM, windingM, 
         windingEvenOddItem);

      // non zero

      windingNonZeroItem = FlowframTkAction.createRadioButtonMenuItem(this,
         "edit.path.style", "windingrule.nonzero", windingM,
         windingGroup,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().setSelectedWindingRule(
                  GeneralPath.WIND_NON_ZERO);
            }
         });

      incStartupProgress(editM, pathM, lineStyleM, windingM,
         windingNonZeroItem);

      // JDRText submenu

      textM = FlowframTkAction.createMenu(this,
         null, "edit.text", editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      incStartupProgress(editM, textM);

      // Edit JDRText

      editTextItem = FlowframTkAction.createMenuItem(this,
         "edit", "text.edit", textM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_SINGLE_TEXTUAL, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayEditTextDialog();
            }
         });

      // Character selector

      if (appSettings.getUnicodeRanges() == null)
      {
         appSettings.setDefaultUnicodeRanges();
      }

      characterSelector = new CharacterSelector(this, getUnicodeRanges());

      editTextBox = new TextSelector(this);

      incStartupProgress(editM, textM, editTextItem);

      // JDRText colour

      textPaintItem = FlowframTkAction.createMenuItem(this,
         "edit", "text.colour", textM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayTextPaintChooser();
            }
         });

      textPaintChooserBox = new TextPaintSelector(this);

      incStartupProgress(editM, textM, textPaintItem);

      JCheckBoxMenuItem textOutlineItem = 
         FlowframTkAction.createToggleMenuItem(this,
         "edit", "text.outline", textM, false,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         FlowframTkAction.CONSTRUCTION_FLAG_NONE,
         SELECT_FLAG_TEXTUAL, FlowframTkAction.SELECTION_IGNORE_COUNT, 
         true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               Object source = evt.getSource();

               if (source instanceof AbstractButton)
               {
                  action.setSelected(((AbstractButton)source).isSelected());
               }

               action.getCanvas().setSelectedTextOutlineMode(action.isSelected());
            }
         });

      incStartupProgress(editM, textM, textOutlineItem);

      // JDRText transformation matrix

      textMatrixItem = FlowframTkAction.createMenuItem(this,
         "edit", "text.matrix", textM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               textMatrixDialog.initialise();
            }
         });

      textMatrixDialog = new SetTransformDialogBox(this);

      incStartupProgress(editM, textM, textMatrixItem);

      // Change Font

      fontStyleM = FlowframTkAction.createMenu(this,
         "edit.text", "font", textM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      incStartupProgress(editM, textM, fontStyleM);

      // All font styles

      textStyleItem = FlowframTkAction.createMenuItem(this,
         "edit.text", "font.all_styles", fontStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayAllFontStylesChooser();
            }
         });

      textStyleChooserBox = new FontSelector(this);

      fontStyleM.addSeparator();

      incStartupProgress(editM, textM, fontStyleM, textStyleItem);

      // font family

      fontFamilyItem = FlowframTkAction.createMenuItem(this,
         "edit.text", "font.family", fontStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayFontFamilyChooser();
            }
         });

      fontFamilyChooserBox = new FontFamilySelector(this);

      incStartupProgress(editM, textM, fontStyleM, fontFamilyItem);

      // font size

      fontSizeItem = FlowframTkAction.createMenuItem(this,
         "edit.text", "font.size", fontStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayFontSizeChooser();
            }
         });

      fontSizeChooserBox = new FontSizeSelector(this);

      incStartupProgress(editM, textM, fontStyleM, fontSizeItem);

      // font shape

      fontShapeItem = FlowframTkAction.createMenuItem(this,
         "edit.text", "font.shape", fontStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayFontShapeChooser();
            }
         });

      fontShapeChooserBox = new FontShapeSelector(this);

      incStartupProgress(editM, textM, fontStyleM, fontShapeItem);

      // font series

      fontSeriesItem = FlowframTkAction.createMenuItem(this,
         "edit.text", "font.series", fontStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayFontSeriesChooser();
            }
         });

      fontSeriesChooserBox = new FontSeriesSelector(this);

      incStartupProgress(editM, textM, fontStyleM, fontSeriesItem);

      // text anchor

      JMenu fontAnchorM = FlowframTkAction.createMenu(this,
         "edit.text", "font.anchor", fontStyleM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      incStartupProgress(editM, textM, fontStyleM, fontAnchorM);

      // both

      fontAnchorItem = FlowframTkAction.createMenuItem(this,
         "edit.text", "font.anchor.both", fontAnchorM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayFontAnchorChooser();
            }
         });

      fontAnchorChooserBox = new FontAnchorSelector(this);

      incStartupProgress(editM, textM, fontStyleM, fontAnchorM, fontAnchorItem);

      // horizontal anchor setting

      fontHAnchorItem = FlowframTkAction.createMenuItem(this, 
         "edit.text", "font.anchor.horizontal", fontAnchorM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayFontHAnchorChooser();
            }
         });

      fontHAnchorChooserBox = new FontHAnchorSelector(this);

      incStartupProgress(editM, textM, fontStyleM, fontAnchorM, 
         fontHAnchorItem);

      // vertical anchor setting

      fontVAnchorItem = FlowframTkAction.createMenuItem(this,
         "edit.text", "font.anchor.vertical", fontAnchorM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayFontVAnchorChooser();
            }
         });

      fontVAnchorChooserBox = new FontVAnchorSelector(this);

      incStartupProgress(editM, textM, fontStyleM, fontAnchorM,
         fontVAnchorItem);

      // Fill colour

      fillPaintItem = FlowframTkAction.createMenuItem(this,
         "edit", "fill_colour", editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_NON_TEXTUAL_SHAPE | SELECT_FLAG_OUTLINE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayFillPaintChooser();
            }
         });


      fillPaintChooserBox = new FillPaintSelector(this);

      incStartupProgress(editM, pathM, fillPaintItem);


      // Adjust colour sub menu

      adjustColM = FlowframTkAction.createMenu(this,
         "edit", "adjustcol", editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE | SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      // Reduce to grey scale

      reduceToGreyItem = FlowframTkAction.createMenuItem(this,
         "edit", "adjustcol.togrey", adjustColM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE | SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().reduceToGrey();
            }
         });

      incStartupProgress(editM, adjustColM, reduceToGreyItem);

      // Convert to CMYK

      convertToCMYKItem = FlowframTkAction.createMenuItem(this,
         "edit", "adjustcol.cmyk", adjustColM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE | SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().convertToCMYK();
            }
         });

      incStartupProgress(editM, adjustColM, convertToCMYKItem);

      // Convert to RGB

      convertToRGBItem = FlowframTkAction.createMenuItem(this,
         "edit", "adjustcol.rgb", adjustColM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE | SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().convertToRGB();
            }
         });

      incStartupProgress(editM, adjustColM, convertToRGBItem);

      // Convert to HSB

      convertToHSBItem = FlowframTkAction.createMenuItem(this,
         "edit", "adjustcol.hsb", adjustColM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE | SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().convertToHSB();
            }
         });

      incStartupProgress(editM, adjustColM, convertToHSBItem);

      // Fade

      fadeItem = FlowframTkAction.createMenuItem(this,
         "edit", "adjustcol.fade", adjustColM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE | SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               fadeDialog.display();
            }
         });

      fadeDialog = new FadeDialogBox(this);

      incStartupProgress(editM, adjustColM, fadeItem);

      // Remove alpha

      removeAlphaItem = FlowframTkAction.createMenuItem(this,
         "edit", "adjustcol.removetrans", adjustColM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE | SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().removeAlpha();
            }
         });

      incStartupProgress(editM, adjustColM, removeAlphaItem);

      editM.addSeparator();

      // Move To Front

      moveToFrontButtonItem = FlowframTkAction.createButtonItem(this,
         "edit", "front", toolBar, editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().moveDrawObjectToFront();
            }
         });

      incStartupProgress(editM, moveToFrontButtonItem);

      // Move To Back

      moveToBackButtonItem = FlowframTkAction.createButtonItem(this,
        "edit", "back", toolBar, editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().moveDrawObjectToBack();
            }
         });

      incStartupProgress(editM, moveToBackButtonItem);

      // Move Up

      moveUpItem = FlowframTkAction.createMenuItem(this,
         "edit", "moveup", editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().moveDrawObjectUp();
            }
         });

      incStartupProgress(editM, moveUpItem);

      // Move Down

      moveDownItem = FlowframTkAction.createMenuItem(this,
         "edit", "movedown", editM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().moveDrawObjectDown();
            }
         });

      incStartupProgress(editM, moveDownItem);

      // Transform Menu

      transformM = resources.createAppMenu("transform");
      mbar.add(transformM);

      incStartupProgress(transformM);

      // Rotate

      rotateButtonItem = FlowframTkAction.createButtonItem(this,
         "transform", "rotate", toolBar, transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               rotateDB.display();
            }
         });

      rotateDB = new RotateDialogBox(this);

      incStartupProgress(transformM, rotateButtonItem);

      // Scale

      scaleButtonItem = FlowframTkAction.createButtonItem(this,
         "transform", "scale", toolBar, transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               scaleDB.display();
            }
         });

      scaleDB = new ScaleDialogBox(this);

      incStartupProgress(transformM, scaleButtonItem);

      // Shear

      shearButtonItem = FlowframTkAction.createButtonItem(this,
         "transform", "shear", toolBar, transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               shearDB.display();
            }
         });

      shearDB = new ShearDialogBox(this);

      incStartupProgress(transformM, shearButtonItem);

      // Reset transformation matrix

      JMenuItem resetItem = FlowframTkAction.createMenuItem(this,
         "transform", "reset", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE_OR_DISTORT,
         SELECT_FLAG_BITMAP | SELECT_FLAG_TEXTUAL | SELECT_FLAG_DISTORTED,
         FlowframTkAction.SELECTION_SINGLE_OBJECT_NO_GROUP, 
         true, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getCanvas().resetTransform();
            }
         });


      // Toggle Distortion

      distortButtonItem = FlowframTkAction.createToggleButtonItem(this,
         "transform", "distort", false, toolBar, transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE_OR_DISTORT,
         SELECT_FLAG_PATH | SELECT_FLAG_DISTORTED,
         FlowframTkAction.SELECTION_SINGLE_OBJECT_NO_GROUP, 
         true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getCanvas().setDistortState(action.isSelected());

            }
         });

      incStartupProgress(transformM, distortButtonItem);

      transformM.addSeparator();

      // Reverse path

      reverseItem = FlowframTkAction.createMenuItem(this,
         "transform", "reverse", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().reverseSelectedPaths();
            }
         });

      incStartupProgress(transformM, reverseItem);

      // Merge Paths

      mergePathsItem = FlowframTkAction.createMenuItem(this,
         "transform", "merge", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE,
         FlowframTkAction.SELECTION_AT_LEAST_TWO_SHAPES_NO_OTHER, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().mergePaths();
            }
         });

      incStartupProgress(transformM, mergePathsItem);

      // Path Union

      pathUnionItem = FlowframTkAction.createMenuItem(this,
         "transform", "union", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE,
         FlowframTkAction.SELECTION_AT_LEAST_TWO_SHAPES_NO_OTHER, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().pathUnion();
            }
         });

      incStartupProgress(transformM, pathUnionItem);

      // XOR Paths

      xorPathsItem = FlowframTkAction.createMenuItem(this,
         "transform", "xor", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE,
         FlowframTkAction.SELECTION_AT_LEAST_TWO_SHAPES_NO_OTHER, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().xorPaths();
            }
         });

      incStartupProgress(transformM, xorPathsItem);

      // Path Intersection

      intersectPathsItem = FlowframTkAction.createMenuItem(this,
         "transform", "intersect", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE,
         FlowframTkAction.SELECTION_AT_LEAST_TWO_SHAPES_NO_OTHER, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().pathIntersect();
            }
         });

      incStartupProgress(transformM, intersectPathsItem);

      // Subtract Paths

      subtractPathsItem = FlowframTkAction.createMenuItem(this,
         "transform", "subtract", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE,
         FlowframTkAction.SELECTION_AT_LEAST_TWO_SHAPES_NO_OTHER, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().subtractPaths();
            }
         });

      incStartupProgress(transformM, subtractPathsItem);

      // Convert to polygon

      convertToPolygonDialog = new ConvertToPolygonDialog(this);

      convertToPolygonItem = FlowframTkAction.createMenuItem(this,
         "transform", "convert_to_polygon", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE,
         FlowframTkAction.SELECTION_SINGLE_OBJECT_NO_GROUP, true, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               convertToPolygonDialog.display(action.getFrame());
            }
         });

      incStartupProgress(transformM, subtractPathsItem);

      transformM.addSeparator();

      patternM = FlowframTkAction.createMenu(this,
         "transform", "pattern", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE,
         FlowframTkAction.SELECTION_SINGLE_SHAPE_NO_GROUP, true, false);

      incStartupProgress(transformM, patternM);

      transformM.addSeparator();

      // Set Pattern

      setPatternButtonItem = FlowframTkAction.createButtonItem(this,
         "transform", "pattern.set", toolBar, patternM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE,
         FlowframTkAction.SELECTION_SINGLE_NON_PATTERN_SHAPE_NO_GROUP, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               patternBox.display();  
            }
         });

      patternBox = new PatternDialogBox(this);

      incStartupProgress(transformM, patternM);

      // Edit Pattern

      editPatternItem = FlowframTkAction.createMenuItem(this,
         "transform", "pattern.edit", patternM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_PATTERN,
         FlowframTkAction.SELECTION_SINGLE_PATTERN_NO_GROUP, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               patternBox.display(action.getFrame().getSelectedPatternIndex());
            }
         });

      incStartupProgress(transformM, patternM, editPatternItem);

      // Remove Pattern

      removePatternItem = FlowframTkAction.createMenuItem(this,
         "transform", "pattern.remove", patternM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_PATTERN,
         FlowframTkAction.SELECTION_SINGLE_PATTERN_NO_GROUP, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().removePattern();
            }
         });

      incStartupProgress(transformM, patternM, removePatternItem);

      // Convert to JDRPath

      convertToPathItem = FlowframTkAction.createMenuItem(this,
         "transform", "convert", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_SHAPE | SELECT_FLAG_TEXT,
         FlowframTkAction.SELECTION_ONLY_SHAPES_OR_TEXT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().convertToPath();
            }
         });

      incStartupProgress(transformM, convertToPathItem);

      // Convert pattern or symmetric path to full path

      convertToFullPathItem = FlowframTkAction.createMenuItem(this,
         "transform", "convert_to_full", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_COMPOUND,
         FlowframTkAction.SELECTION_ONLY_COMPOUND_SHAPES, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().convertToFullPath();
            }
         });

      incStartupProgress(transformM, convertToFullPathItem);

      // split text

      splitTextItem = FlowframTkAction.createMenuItem(this,
         "transform", "split", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXTUAL,
         FlowframTkAction.SELECTION_ONLY_TEXTUAL, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().splitText();
            }
         });

      incStartupProgress(transformM, splitTextItem);

      // Convert to JDRTextPath

      convertToTextPathButtonItem = FlowframTkAction.createButtonItem(this,
         "transform", "textpath", toolBar, transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_TEXT | SELECT_FLAG_NON_TEXTUAL_SHAPE,
         FlowframTkAction.SELECTION_ONE_NON_TEXTUAL_SHAPE_AND_ONE_TEXT_ONLY,
         true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().convertToTextPath();
            }
         });

      incStartupProgress(transformM, convertToTextPathButtonItem);

      // Separate compound shape

      separateItem = FlowframTkAction.createMenuItem(this,
         "transform", "separate", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_COMPOUND,
         FlowframTkAction.SELECTION_ONLY_COMPOUND_SHAPES, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().separate();
            }
         });

      incStartupProgress(transformM, separateItem);

      transformM.addSeparator();

      // Group

      groupButtonItem = FlowframTkAction.createButtonItem(this,
         "transform", "group", toolBar, transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_AT_LEAST_TWO_OBJECTS, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().group();
            }
         });

      incStartupProgress(transformM, groupButtonItem);

      // Ungroup

      ungroupButtonItem = FlowframTkAction.createButtonItem(this,
        "transform", "ungroup", toolBar, transformM,
        TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
        SELECT_FLAG_GROUP,
        FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().ungroup();
            }
         });

      incStartupProgress(transformM, ungroupButtonItem);

      // Justify Sub Menu

      justifyM = FlowframTkAction.createMenu(this,
         "transform", "justify", transformM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_GROUP,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      incStartupProgress(transformM, justifyM);

      // Left Align

      leftAlignItem = FlowframTkAction.createMenuItem(this,
         "transform", "justify.left", justifyM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_GROUP,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().leftAlign();
            }
         });

      incStartupProgress(transformM, justifyM, leftAlignItem);

      // Centre Align

      centreAlignItem = FlowframTkAction.createMenuItem(this,
         "transform", "justify.centre", justifyM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_GROUP,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().centreAlign();
            }
         });

      incStartupProgress(transformM, justifyM, centreAlignItem);

      // Right Align

      rightAlignItem = FlowframTkAction.createMenuItem(this,
         "transform", "justify.right", justifyM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_GROUP,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().rightAlign();
            }
         });

      justifyM.addSeparator();

      incStartupProgress(transformM, justifyM, rightAlignItem);

      // Top Align

      topAlignItem = FlowframTkAction.createMenuItem(this,
         "transform", "justify.top", justifyM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_GROUP,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().topAlign();
            }
         });

      incStartupProgress(transformM, justifyM, topAlignItem);

      // Middle Align

      middleAlignItem = FlowframTkAction.createMenuItem(this,
         "transform", "justify.middle", justifyM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_GROUP,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().middleAlign();
            }
         });

      incStartupProgress(transformM, justifyM, middleAlignItem);

      // Bottom Align

      bottomAlignItem = FlowframTkAction.createMenuItem(this,
         "transform", "justify.bottom", justifyM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         SELECT_FLAG_GROUP,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().bottomAlign();
            }
         });

      incStartupProgress(transformM, justifyM, bottomAlignItem);

      // Tools menu

      toolsM = FlowframTkAction.createMenu(this, "tools",
         TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      mbar.add(toolsM);

      toolButtonGroup = new ToolButtonGroup();

      incStartupProgress(toolsM);

      int currentTool = getSettings().getTool();

      // Select

      selectButtonItem = ToolAction.createToolButtonItem(this,
         "tools", currentTool==ACTION_SELECT, sidePanel, toolsM, 
         toolButtonGroup, ACTION_SELECT);

      incStartupProgress(toolsM, selectButtonItem);

      vSlidingBar.setUnitIncrement(
         selectButtonItem.getButtonPreferredHeight());

      // Open line

      openLineButtonItem = ToolAction.createToolButtonItem(this,
         "tools", currentTool==ACTION_OPEN_LINE, sidePanel, toolsM, 
         toolButtonGroup, ACTION_OPEN_LINE);

      incStartupProgress(toolsM, openLineButtonItem);

      // Closed Line

      closedLineButtonItem = ToolAction.createToolButtonItem(this,
         "tools", currentTool==ACTION_CLOSED_LINE, 
          sidePanel, toolsM, toolButtonGroup,
         ACTION_CLOSED_LINE);

      incStartupProgress(toolsM, closedLineButtonItem);

      // Open curve

      openCurveButtonItem = ToolAction.createToolButtonItem(this,
         "tools", currentTool==ACTION_OPEN_CURVE, 
         sidePanel, toolsM, toolButtonGroup,
         ACTION_OPEN_CURVE);

      incStartupProgress(toolsM, openCurveButtonItem);

      // Closed curve

      closedCurveButtonItem = ToolAction.createToolButtonItem(this,
         "tools", currentTool==ACTION_CLOSED_CURVE, 
         sidePanel, toolsM, toolButtonGroup,
         ACTION_CLOSED_CURVE);

      incStartupProgress(toolsM, closedCurveButtonItem);

      // Rectangle

      rectangleButtonItem = ToolAction.createToolButtonItem(this,
         "tools", currentTool==ACTION_RECTANGLE, 
         sidePanel, toolsM, toolButtonGroup,
         ACTION_RECTANGLE);

      incStartupProgress(toolsM, rectangleButtonItem);

      // Ellipse

      ellipseButtonItem = ToolAction.createToolButtonItem(this,
         "tools", currentTool==ACTION_ELLIPSE,
         sidePanel, toolsM, toolButtonGroup,
         ACTION_ELLIPSE);

      incStartupProgress(toolsM, ellipseButtonItem);

      // Text

      textButtonItem = ToolAction.createToolButtonItem(this,
         "tools", currentTool==ACTION_TEXT, 
         sidePanel, toolsM, toolButtonGroup,
         ACTION_TEXT);

      incStartupProgress(toolsM, textButtonItem);

      // Maths

      JDRToolButtonItem mathButtonItem = ToolAction.createToolButtonItem(this,
         "tools", currentTool==ACTION_MATH, 
         sidePanel, toolsM, toolButtonGroup,
         ACTION_MATH);

      incStartupProgress(toolsM, mathButtonItem);

      toolsM.addSeparator();

      // Gap

      gapButtonItem = FlowframTkAction.createButtonItem(this,
         "tools", "gap", sidePanel, toolsM,
         TOOL_FLAG_ANY_PATHS,
         EDIT_FLAG_NONE,
         FlowframTkAction.CONSTRUCTION_FLAG_NON_GEOMETRIC,
         SELECT_FLAG_NONE, FlowframTkAction.SELECTION_IGNORE_COUNT,
         true, false, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().gap();
            }
         });

      incStartupProgress(toolsM, gapButtonItem);

      // Abandon

      abandonButtonItem = FlowframTkAction.createButtonItem(this,
         "tools", "abandon", sidePanel, toolsM,
         TOOL_FLAG_ANY_PATHS,
         EDIT_FLAG_NONE,
         FlowframTkAction.CONSTRUCTION_FLAG_PATH,
         SELECT_FLAG_NONE, FlowframTkAction.SELECTION_IGNORE_COUNT,
         true, false, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().abandonPath();
            }
         });

      incStartupProgress(toolsM, abandonButtonItem);

      // Finish

      finishButtonItem = FlowframTkAction.createButtonItem(this,
         "tools", "finish", sidePanel, toolsM,
         TOOL_FLAG_ANY_PATHS | TOOL_FLAG_ANY_TEXT,
         EDIT_FLAG_NONE,
         FlowframTkAction.CONSTRUCTION_FLAG_PATH_OR_TEXT,
         SELECT_FLAG_NONE, FlowframTkAction.SELECTION_IGNORE_COUNT,
         true, false, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getCanvas().finish();
            }
         });

      incStartupProgress(toolsM, finishButtonItem);

      Dimension prefSize = sidePanel.getPreferredSize();
      prefSize.width += 4;
      sidePanel.setPreferredSize(prefSize);

      // navigate menu

      navigateM = FlowframTkAction.createMenu(this, "navigate",
         TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      mbar.add(navigateM);

      incStartupProgress(navigateM);

      // go to co-ordinate

      gotoItem = FlowframTkAction.createMenuItem(this,
         null, "navigate.goto", navigateM, true, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayGoToDialog();
            }
         });

      gotoDialog = new GoToDialogBox(this);

      incStartupProgress(navigateM, gotoItem);

      // select next object

      nextItem = FlowframTkAction.createMenuItem(this,
         null, "navigate.select", navigateM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().getCanvas().selectNextObject();
            }
         });

      incStartupProgress(navigateM, nextItem);

      // add next object to selection

      addNextItem = FlowframTkAction.createMenuItem(this,
         null, "navigate.add_next", navigateM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().getCanvas().addNextObject();
            }
         });

      incStartupProgress(navigateM, addNextItem);

      // deselect lowest object and add next object to selection

      skipItem = FlowframTkAction.createMenuItem(this,
         null, "navigate.skip", navigateM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().getCanvas().skipObject();
            }
         });

      incStartupProgress(navigateM, skipItem);

      // scroll to selected objects.

      findSelectedItem = FlowframTkAction.createMenuItem(this,
         null, "navigate.find", navigateM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().getCanvas().findSelectedObjects();
            }
         });

      incStartupProgress(navigateM, findSelectedItem);

      // find object with given description

      findByDescriptionItem = FlowframTkAction.createMenuItem(this,
         null, "navigate.description", navigateM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayFindByDescriptionDialog();
            }
         });

      findByDescDialog = new FindByDescriptionDialogBox(this);

      incStartupProgress(navigateM, findByDescriptionItem);

      // find object with given description and add to current 
      // selection

      addByDescriptionItem = FlowframTkAction.createMenuItem(this,
         null, "navigate.add_description", navigateM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               findByDescDialog.display(false);
            }
         });

      navigateM.add(addByDescriptionItem);

      incStartupProgress(navigateM, addByDescriptionItem);

      // Bitmap menu

      bitmapM = FlowframTkAction.createMenu(this, "bitmap",
         TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      mbar.add(bitmapM);

      incStartupProgress(bitmapM);

      // Insert Bitmap

      insertBitmapItem = FlowframTkAction.createMenuItem(this,
         null, "bitmap.insert", bitmapM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayInsertBitmapChooser(action.getFrame());
            }
         });

      bitmapFC = new JFileChooser();
      bitmapFC.setCurrentDirectory(new File(appSettings.startDir));
      bitmapFC.setAcceptAllFileFilterUsed(false);

      String[] bitmapExt = ImageIO.getReaderFileSuffixes();

      String validExt = "";

      for (int i = 0; i < bitmapExt.length; i++)
      {
         if (validExt.isEmpty())
         {
            validExt = "*."+bitmapExt[i];
         }
         else
         {
            validExt = validExt + ", *."+bitmapExt[i];
         }
      }

      bitmapFileFilter = new BitmapFileFilter(
         resources.getMessage("filter.bitmap", validExt),
         bitmapExt);
      bitmapFC.addChoosableFileFilter(bitmapFileFilter);

      imagePreviewPanel = new ImagePreview(resources, bitmapFC);
      bitmapFC.setAccessory(imagePreviewPanel);


      imagePreviewPanel.setPreviewSelected(getPreviewBitmaps());

      imagePreviewPanel.addPreviewBoxActionListener(
         new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               setPreviewBitmaps(
                  imagePreviewPanel.isPreviewSelected());
            }
         });

      incStartupProgress(bitmapM, insertBitmapItem);

      // Refresh

      refreshItem = FlowframTkAction.createMenuItem(this,
        null, "bitmap.refresh", bitmapM, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().refresh();
            }
         });

      incStartupProgress(bitmapM, refreshItem);

      // Properties

      bitmapPropItem = FlowframTkAction.createMenuItem(this,
         null, "bitmap.properties", bitmapM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_BITMAP,
         FlowframTkAction.SELECTION_SINGLE_OBJECT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayBitmapPropertiesChooser();
            }
         });

      bitmapPropChooserBox = new BitmapProperties(this, bitmapFC);

      incStartupProgress(bitmapM, bitmapPropItem);

      // Vectorize

      vectorizeItem = FlowframTkAction.createMenuItem(this,
         "bitmap", "vectorize", bitmapM,
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_BITMAP,
         FlowframTkAction.SELECTION_SINGLE_BITMAP, true, false,
      new FlowframTkActionListener()
      {
         public void doAction(FlowframTkAction action, ActionEvent evt)
         {
            displayVectorizeBitmapDialog();
         }
      });

      incStartupProgress(bitmapM, vectorizeItem);

      vectorizeBitmapDialog = new VectorizeBitmapDialog(this);

      // TeX/LaTeX

      texM = FlowframTkAction.createMenu(this, "tex",
         TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      mbar.add(texM);

      incStartupProgress(texM);

      // Set preamble

      JMenuItem preambleItem = FlowframTkAction.createMenuItem(this,
         "tex", "set_preamble", texM,
         TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               JDRFrame frame = action.getFrame();

               if (frame != null)
               {
                  frame.displayPreambleEditor();
               }
            }
         });

      texEditorDialog = new TeXEditorDialog(this);

      incStartupProgress(texM, preambleItem);

      // Parshape

      TeXAction parshapeAction = 
         new TeXAction(this, TeXAction.PARSHAPE);

      parshapeItem = parshapeAction.createMenuItem("tex.parshape", null);

      texM.add(parshapeItem);

      incStartupProgress(texM, parshapeItem);

      // Shapepar

      TeXAction shapeparAction = 
         new TeXAction(this, TeXAction.SHAPEPAR);

      shapeparItem = shapeparAction.createMenuItem("tex.shapepar", null);

      texM.add(shapeparItem);

      incStartupProgress(texM, shapeparItem);

      // Flow Frame Menu

      JMenu flowframeM = resources.createAppMenu("tex.flowframe");
      texM.add(flowframeM);

      incStartupProgress(texM, flowframeM);

      // Clear All

      clearAllItem = FlowframTkAction.createMenuItem(this, 
         "tex", "flowframe.clear", flowframeM,
         true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().unsetAllFlowFrames();
            }
         });

      incStartupProgress(texM, flowframeM, clearAllItem);

      // Set Typeblock

      setTypeblockItem = FlowframTkAction.createMenuItem(this,
        "tex", "flowframe.set_typeblock", flowframeM,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
        FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               JDRFrame frame = action.getFrame();

               FlowFrame typeblock = frame.getTypeblock();

               setTypeblockSelector.display(typeblock, 
                  frame.getUnit());
            }
         });

      setTypeblockSelector = new FLFSetTypeblock(this);

      incStartupProgress(texM, flowframeM, setTypeblockItem);

      // Set Frame

      setFrameItem = FlowframTkAction.createMenuItem(this,
        "tex", "flowframe.set_frame", flowframeM,
        TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_OBJECT,
        FlowframTkAction.SELECTION_SINGLE_OBJECT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               JDRFrame frame = action.getFrame();

               FlowFrame typeblock = frame.getTypeblock();

               if (typeblock == null)
               {
                  invoker.getResources().error(frame, 
                    invoker.getResources().getString("error.no_typeblock"));
                  return;
               }

               flfSelector.display();
            }
         });

      flfSelector = new FLFSelector(this);

      incStartupProgress(texM, flowframeM, setFrameItem);

      // Scale to fit typeblock

      JMenuItem setTypeblockItem = FlowframTkAction.createMenuItem(this,
        "tex", "flowframe.scale_to_typeblock", flowframeM,
        TOOL_FLAG_SELECT, EDIT_FLAG_NONE, SELECT_FLAG_ANY_OBJECT,
        FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               JDRCanvas canvas = action.getCanvas();

               canvas.scaleSelectedToTypeblock();
            }
         });

      // Display pages

      displayPageDialog = new DisplayPageDialog(this);

      displayPageItem = FlowframTkAction.createMenuItem(this,
        "tex", "flowframe.display_page", flowframeM,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
        FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayPageDialog.display();
            }
         });

      incStartupProgress(texM, flowframeM, displayPageItem);

      // Settings

      settingsM = resources.createAppMenu("settings");
      mbar.add(settingsM);

      incStartupProgress(settingsM);

      // Styles

      stylesItem = FlowframTkAction.createMenuItem(this,
         "settings", "styles", settingsM,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               styleChooserBox.display();
            }
         });

      styleChooserBox = new StyleSelector(this);

      // set style settings

      styleChooserBox.set(appSettings);

      incStartupProgress(settingsM, stylesItem);

      hSlidingBar.setVisible(appSettings.showToolBar);  
      vSlidingBar.setVisible(appSettings.showToolBar);  

      // Show both rulers

      showRulersItem = FlowframTkAction.createToggleMenuItem(this,
         null, "settings.rulers", settingsM, true,
         true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().showRulers(
                  action.isSelected());
            }
         });

      incStartupProgress(settingsM, showRulersItem);

      // Grid Sub Menu

      gridM = FlowframTkAction.createMenu(this,
         "settings", "grid", settingsM,
         TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      incStartupProgress(settingsM, gridM);

      // Show Grid

      showGridButtonItem = FlowframTkAction.createToggleButtonItem(this,
        "settings", "grid.show", true, toolBar, gridM,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
        FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               setGridDisplay(!action.getFrame().showGrid());
            }
         });

      incStartupProgress(settingsM, gridM, showGridButtonItem);

      // Lock

      lockGridButtonItem = FlowframTkAction.createToggleButtonItem(this,
         "settings", "grid.lock", false, toolBar, gridM,
         TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               setLockGrid(!action.getFrame().getGridLock());
            }
         });

      incStartupProgress(settingsM, gridM, lockGridButtonItem);

      // Grid Settings

      gridSettingsItem = FlowframTkAction.createMenuItem(this,
         "settings", "grid.settings", gridM, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               displayGridSettings();
            }
         });

      gridSettingsChooserBox = new GridSettings(this);

      incStartupProgress(settingsM, gridM, gridSettingsItem);

      // Zoom sub menu

      zoomM = FlowframTkAction.createMenu(this, 
        "settings", "zoom", settingsM,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
        FlowframTkAction.SELECTION_IGNORE_COUNT, true, true);

      incStartupProgress(settingsM, zoomM);

      ButtonGroup zoomGroup = new ButtonGroup();
      
      // Fit Width

      zoomWidthItem = FlowframTkAction.createMenuItem(this,
        "settings", "zoom.width", zoomM, true, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               JDRFrame frame = action.getFrame();
               frame.zoomWidth();
               zoomSettingsChooserBox.setRelativeValue(
                  ZoomValue.ZOOM_PAGE_WIDTH_ID,
                  frame.getMagnification());
               zoomSettingsItem.setSelected(true);
            }
         });

      incStartupProgress(settingsM, zoomM, zoomWidthItem);

      // Fit Height

      zoomHeightItem = FlowframTkAction.createMenuItem(this,
         "settings", "zoom.height", zoomM, true, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               JDRFrame frame = action.getFrame();
               frame.zoomHeight();
               zoomSettingsChooserBox.setRelativeValue(
                  ZoomValue.ZOOM_PAGE_HEIGHT_ID,
                  frame.getMagnification());
               zoomSettingsItem.setSelected(true);
            }
         });

      incStartupProgress(settingsM, zoomM, zoomHeightItem);

      // Fit Page

      zoomPageItem = FlowframTkAction.createMenuItem(this,
         "settings", "zoom.page", zoomM, true, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               JDRFrame frame = action.getFrame();
               frame.zoomPage();
               zoomSettingsChooserBox.setRelativeValue(
                  ZoomValue.ZOOM_PAGE_ID,
                  frame.getMagnification());
               zoomSettingsItem.setSelected(true);
            }
         });

      zoomM.addSeparator();

      incStartupProgress(settingsM, zoomM, zoomPageItem);

      // User defined zoom

      zoomSettingsItem = FlowframTkAction.createRadioButtonMenuItem(this,
        "settings", "zoom.user", zoomM, zoomGroup,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
        FlowframTkAction.SELECTION_IGNORE_COUNT, true, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               zoomSettingsChooserBox.display();
            }
         });

      zoomSettingsChooserBox = new ZoomSettings(this, this);

      incStartupProgress(settingsM, zoomM, zoomSettingsItem);

      // 25% Magnification

      zoom25Item = ZoomAction.createRadioMenuItem(this, 
         "settings", "zoom.25", zoomM, zoomGroup, 0.25);

      incStartupProgress(settingsM, zoomM, zoom25Item);

      // 50% Magnification

      zoom50Item = ZoomAction.createRadioMenuItem(this, 
         "settings", "zoom.50", zoomM, zoomGroup, 0.5);

      incStartupProgress(settingsM, zoomM, zoom50Item);

      // 75% Magnification

      zoom75Item = ZoomAction.createRadioMenuItem(this, 
         "settings", "zoom.75", zoomM, zoomGroup, 0.75);

      incStartupProgress(settingsM, zoomM, zoom75Item);

      // 100% Magnification

      zoom100Item = ZoomAction.createRadioMenuItem(this, 
         "settings", "zoom.100", zoomM, zoomGroup, 1.0);

      incStartupProgress(settingsM, zoomM, zoom100Item);

      // 200% Magnification

      zoom200Item = ZoomAction.createRadioMenuItem(this, 
         "settings", "zoom.200", zoomM, zoomGroup, 2.0);

      incStartupProgress(settingsM, zoomM, zoom200Item);

      // 400% Magnification

      zoom400Item = ZoomAction.createRadioMenuItem(this, 
         "settings", "zoom.400", zoomM, zoomGroup, 4.0);

      incStartupProgress(settingsM, zoomM, zoom400Item);

      // 800% Magnification

      zoom800Item = ZoomAction.createRadioMenuItem(this, 
         "settings", "zoom.800", zoomM, zoomGroup, 8.0);

      incStartupProgress(settingsM, zoomM, zoom800Item);

      // Paper size menu

      paperM = FlowframTkAction.createMenu(this,
        "settings", "paper", settingsM,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
        FlowframTkAction.SELECTION_IGNORE_COUNT, true, false);

      incStartupProgress(settingsM, paperM);

      // Show printer margins

      showPrinterMarginsItem = FlowframTkAction.createToggleMenuItem(
         this, "settings", "paper.margins", null,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().setMargins();
            }
         });

      if (invoker.isPrintDisabled())
      {
         showPrinterMarginsItem.setEnabled(false);
      }
      else
      {
         paperM.add(showPrinterMarginsItem);
      }

      incStartupProgress(settingsM, paperM, showPrinterMarginsItem);

      ButtonGroup orientationGroup = new ButtonGroup();

      // Portrait

      portraitItem = FlowframTkAction.createRadioButtonMenuItem(this,
         "settings", "paper.portrait", paperM, orientationGroup,
         TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().setOrientation(true);
            }
         });

      incStartupProgress(settingsM, paperM, portraitItem);

      // Landscape

      landscapeItem = FlowframTkAction.createRadioButtonMenuItem(this,
         "settings", "paper.landscape", paperM, orientationGroup,
         TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               action.getFrame().setOrientation(false);
            }
         });

      incStartupProgress(settingsM, paperM, landscapeItem);

      paperM.addSeparator();

      ButtonGroup paperGroup = new ButtonGroup();

      // A5 paper

      paperA5Item = PaperAction.createRadioMenuItem(this,
        "settings", "paper.A5", paperM, paperGroup,
        JDRPaper.A5, JDRPaper.A5R);

      incStartupProgress(settingsM, paperM, paperA5Item);

      // A4 paper

      paperA4Item = PaperAction.createRadioMenuItem(this,
        "settings", "paper.A4", paperM, paperGroup,
        JDRPaper.A4, JDRPaper.A4R);

      incStartupProgress(settingsM, paperM, paperA4Item);

      // A3 paper

      paperA3Item = PaperAction.createRadioMenuItem(this,
        "settings", "paper.A3", paperM, paperGroup,
        JDRPaper.A3, JDRPaper.A3R);

      incStartupProgress(settingsM, paperM, paperA3Item);

      // A2 paper

      paperA2Item = PaperAction.createRadioMenuItem(this,
        "settings", "paper.A2", paperM, paperGroup,
        JDRPaper.A2, JDRPaper.A2R);

      incStartupProgress(settingsM, paperM, paperA2Item);

      // A1 paper

      paperA1Item = PaperAction.createRadioMenuItem(this,
        "settings", "paper.A1", paperM, paperGroup,
        JDRPaper.A1, JDRPaper.A1R);

      incStartupProgress(settingsM, paperM, paperA1Item);

      // A0 paper

      paperA0Item = PaperAction.createRadioMenuItem(this,
        "settings", "paper.A0", paperM, paperGroup,
        JDRPaper.A0, JDRPaper.A0R);

      incStartupProgress(settingsM, paperM, paperA0Item);

      // Letter paper

      paperLetterItem = PaperAction.createRadioMenuItem(this,
        "settings", "paper.letter", paperM, paperGroup,
        JDRPaper.LETTER, JDRPaper.LETTERR);

      incStartupProgress(settingsM, paperM, paperLetterItem);

      // Legal paper

      paperLegalItem = PaperAction.createRadioMenuItem(this,
        "settings", "paper.legal", paperM, paperGroup,
        JDRPaper.LEGAL, JDRPaper.LEGALR);

      incStartupProgress(settingsM, paperM, paperLegalItem);

      // Executive paper

      paperExecutiveItem = PaperAction.createRadioMenuItem(this,
        "settings", "paper.executive", paperM, paperGroup,
        JDRPaper.EXECUTIVE, JDRPaper.EXECUTIVER);

      incStartupProgress(settingsM, paperM, paperExecutiveItem);

      // other paper

      paperOtherItem = FlowframTkAction.createRadioButtonMenuItem(this,
         "settings", "paper.other", paperM, paperGroup,
         TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
         FlowframTkAction.SELECTION_IGNORE_COUNT, true, false,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               paperDialog.initialise(isPortrait());
            }
         });

      paperDialog = new PaperDialogBox(this, getSettings().getPaper());

      incStartupProgress(settingsM, paperM, paperOtherItem);

      // configure settings

      configSettingsItem = FlowframTkAction.createMenuItem(this,
         null, "settings.config", settingsM,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               configDialog.display();
            }
         });

      configDialog = new ConfigSettingsDialog(this, appSelector);

      incStartupProgress(settingsM, configSettingsItem);

      // configure TeX/LaTeX settings

      JMenuItem configTeXSettingsItem = FlowframTkAction.createMenuItem(this,
         "settings", "configtex", settingsM,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               configTeXDialog.display(action.getFrame());
            }
         });

      configTeXDialog = new ConfigTeXSettingsDialog(this);

      incStartupProgress(settingsM, configTeXSettingsItem);

      // configure UI

      JMenuItem configUISettingsItem = FlowframTkAction.createMenuItem(this,
         null, "settings.configui", settingsM,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               configUIDialog.display();
            }
         });

      configUIDialog = new ConfigUISettingsDialog(this);

      incStartupProgress(settingsM, configSettingsItem);

      // Window menu

      windowM = FlowframTkAction.createMenu(this,
        "window", TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
        FlowframTkAction.SELECTION_IGNORE_COUNT, false, true);
      mbar.add(windowM);

      windowButtonGroup = new ButtonGroup();
      windowM.addSeparator();

      incStartupProgress(windowM);

      // Tile frames

      tileItem = FlowframTkAction.createMenuItem(this, "window", "tile",
         windowM, true, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               tileFrames();
            }
         });

      incStartupProgress(windowM, tileItem);

      // Arrange frames vertically

      verticallyItem = FlowframTkAction.createMenuItem(this, 
        "window", "vertically", windowM, true, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               tileFramesVertically();
            }
         });

      incStartupProgress(windowM, verticallyItem);

      // Arrange frames horizontally

      horizontallyItem = FlowframTkAction.createMenuItem(this,
         "window", "horizontally", windowM, true, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               tileFramesHorizontally();
            }
         });

      incStartupProgress(windowM, horizontallyItem);

      // minimize all frames

      minimizeItem = FlowframTkAction.createMenuItem(this,
        "window", "minimize", windowM, true, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               minimizeAll();
            }
         });

      incStartupProgress(windowM, minimizeItem);

      // maximize all frames

      maximizeItem = FlowframTkAction.createMenuItem(this,
         "window", "maximize", windowM, false, true,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               maximizeAll();
            }
         });

      incStartupProgress(windowM, maximizeItem);

      // Help menu

      JMenu helpM = resources.createAppMenu("help");

      mbar.add(helpM);

      incStartupProgress(helpM);

      // manual

      JMenuItem item = addHelpItem(helpM);

      toolBar.add(resources.createMainHelpButton());

      incStartupProgress(helpM, item);

      // Licence dialog

      licenceDialog = new LicenceDialog(resources, this);

      licenceItem = FlowframTkAction.createMenuItem(this,
        "help", "licence", helpM,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               licenceDialog.setVisible(true);
            }
         });

      incStartupProgress(helpM, licenceItem);

      // About dialog

      aboutDialog = new AboutDialog(resources, this, invoker.getName(),
         invoker.getVersion());

      aboutItem = FlowframTkAction.createMenuItem(this,
         "help", "about", helpM,
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               aboutDialog.setVisible(true);
            }
         });

      incStartupProgress(helpM, aboutItem);

      FlowframTkAction objectInfo = new FlowframTkAction(this,
        "objectinfo", 
        new FlowframTkActionListener()
        {
           public void doAction(FlowframTkAction action, ActionEvent evt)
           {
              debugMessage("fetching object info");
              objectInfo();
           }
        },
         "debug.objectinfo"
        );

      addAppAction(objectInfo);

      FlowframTkAction writeLog = new FlowframTkAction(this,
        "writelog", 
        new FlowframTkActionListener()
        {
           public void doAction(FlowframTkAction action, ActionEvent evt)
           {
              getMessageSystem().messageln(
                  invoker.getResources().getString("debug.writelog"));
              debugMessage("writing log");
              writeLog();
           }
        },
         "debug.writelog");

      addAppAction(writeLog);

      FlowframTkAction dumpAll = new FlowframTkAction(this,
         "dumpall",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               getMessageSystem().messageln(
                  invoker.getResources().getString("debug.dumpall"));
               debugMessage("Dumping all");
               dumpAll();
            }
         },
         "debug.dumpall");
 
      addAppAction(dumpAll);

      FlowframTkAction revalidate = new FlowframTkAction(this,
         "revalidate",
         new FlowframTkActionListener()
         {
            public void doAction(FlowframTkAction action, ActionEvent evt)
            {
               JDRFrame frame = getCurrentFrame();

               if (frame != null)
               {
                  debugMessage("Revalidating " +frame.getTitle());
                  frame.revalidateImage(true);
               }
               else
               {
                  debugMessage("No frame to revalidate");
               }
            }
         },
         "debug.revalidate");
 
      addAppAction(revalidate);

      // debug menu

      if (resources.debugMode)
      {
         JMenu debugM = resources.createAppMenu("debug");

         mbar.add(debugM);

         incStartupProgress(debugM);

         JMenuItem infoItem = objectInfo.createMenuItem("debug.objectinfo",
            null);
         debugM.add(infoItem);

         incStartupProgress(debugM, infoItem);

         JMenuItem writeLogItem = writeLog.createMenuItem("debug.writelog",
            null);
         debugM.add(writeLogItem);

         incStartupProgress(debugM, writeLogItem);

         JMenuItem dumpAllItem = dumpAll.createMenuItem("debug.dumpall",
            null);
         debugM.add(dumpAllItem);

         incStartupProgress(debugM, dumpAllItem);

         JMenuItem revalidateItem = revalidate.createMenuItem(
            "debug.revalidate", null);
         debugM.add(revalidateItem);

         incStartupProgress(debugM, revalidateItem);
      }
      else
      {
         objectInfo.registerAction(theDesktop, 
           JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
         writeLog.registerAction(theDesktop, 
           JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
         dumpAll.registerAction(theDesktop, 
           JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
         revalidate.registerAction(theDesktop, 
           JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      }

      // set the browse utility for bitmaps

      appSettings.setBrowseUtil(new BrowseUtil(
         resources.getString("browse.label"),
         resources.getString("browse.not_found"),
         resources.getString("browse.invalid_format"),
         resources.getString("browse.cant_refresh"),
         resources.getString("browse.title"),
         resources.getString("browse.invalid_title"),
         resources.getString("browse.discard")));
      appSettings.setBitmapChooser(bitmapFC);

      exportToEpsSettings = new ExportToEpsSettings(this, appSelector);
      exportToSvgSettings = new ExportToSvgSettings(this, appSelector);
      exportToPdfSettings = new ExportToPdfSettings(this, appSelector);

      segmentInfoDialog = new SegmentInfoDialog(this);

      invoker.setStartupInfo(resources.getString("message.init_desktop"));
      invoker.setStartupIndeterminate();

      Vector<String> filenames = invoker.getFilenames();

      // add initial frame
      if (filenames.size() == 0) 
      {
         addFrame(appSettings.getCanvasGraphics());
      }
      else
      {
         for (String filename : filenames)
         {
            addFrame(appSettings.getCanvasGraphics(), new File(filename));
         }
      }

      discardAllEdits();
   }

   public FlowframTkInvoker getInvoker()
   {
      return invoker;
   }

   public JDRResources getResources()
   {
      return invoker.getResources();
   }

   private void incStartupProgress(AbstractButton item)
   {
      invoker.incStartupProgress(item.getText());
   }

   private void incStartupProgress(AbstractButton item1, AbstractButton item2)
   {
      invoker.incStartupProgress(item1.getText()+"->"+item2.getText());
   }

   private void incStartupProgress(AbstractButton item1,
       AbstractButton item2, AbstractButton item3)
   {
      invoker.incStartupProgress(item1.getText()+"->"+item2.getText()
        +"->"+item3.getText());
   }

   private void incStartupProgress(AbstractButton item1,
       AbstractButton item2, AbstractButton item3,
       AbstractButton item4)
   {
      invoker.incStartupProgress(item1.getText()+"->"+item2.getText()
        +"->"+item3.getText()+"->"+item4.getText());
   }

   private void incStartupProgress(AbstractButton item1,
       AbstractButton item2, AbstractButton item3,
       AbstractButton item4, AbstractButton item5)
   {
      invoker.incStartupProgress(item1.getText()+"->"+item2.getText()
        +"->"+item3.getText()+"->"+item4.getText()
        +"->"+item5.getText());
   }

   public void addAppAction(FlowframTkAction action)
   {

      int validToolFlag = action.getValidToolFlag();

      if (validToolFlag == TOOL_FLAG_ANY)
      {
         generalActionList.add(action);
      }
      else if (validToolFlag == TOOL_FLAG_SELECT
           && action.getValidSelectionFlag() != SELECT_FLAG_ANY)
      {
         selectActionList.add(action);
      }
      else
      {
         toolActionList.add(action);
      }
   }

   public FlowframTkAction getAppAction(String actionName)
   {
      for (FlowframTkAction action : generalActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      for (FlowframTkAction action : toolActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      for (FlowframTkAction action : selectActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      return null;
   }

   public FlowframTkAction getGeneralAction(String actionName)
   {
      for (FlowframTkAction action : generalActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      return null;
   }

   public FlowframTkAction getToolAction(String actionName)
   {
      for (FlowframTkAction action : toolActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      return null;
   }

   public FlowframTkAction getSelectAction(String actionName)
   {
      for (FlowframTkAction action : selectActionList)
      {
         if (actionName.equals(action.getActionCommand()))
         {
            return action;
         }
      }

      return null;
   }

   public void debugMessage(String message)
   {
      getResources().debugMessage(message);
   }

   public void debugMessage(Throwable e)
   {
      getResources().debugMessage(e);
   }

   public FlowframTkSettings getSettings()
   {
      return invoker.getSettings();
   }

   public boolean useAbsolutePages()
   {
      return getSettings().useAbsolutePages();
   }

   public void setUseAbsolutePages(boolean flag)
   {
      getSettings().setUseAbsolutePages(flag);
   }

   public boolean usePdfInfo()
   {
      return getSettings().usePdfInfo();
   }

   public void setUsePdfInfoEnabled(boolean enabled)
   {
      getSettings().setUsePdfInfoEnabled(enabled);
   }

   public void setVerticalToolBarLocation(String location)
   {
      getSettings().setVerticalToolBarLocation(location);
   }

   public String getVerticalToolBarLocation()
   {
      return getSettings().getVerticalToolBarLocation();
   }

   public void setTextPathExportOutlineSetting(int flag)
   {
      getSettings().setTextPathExportOutlineSetting(flag);
   }

   public int getTextPathExportOutlineSetting()
   {
      return getSettings().getTextPathExportOutlineSetting();
   }

   public void setTextualExportShadingSetting(int flag)
   {
      getSettings().setTextualExportShadingSetting(flag);
   }

   public int getTextualExportShadingSetting()
   {
      return getSettings().getTextualExportShadingSetting();
   }

   public String displayTeXEditorDialog(String text)
   {
      return texEditorDialog.display(text);
   }

   public int getTeXEditorWidth()
   {
      return getSettings().getTeXEditorWidth();
   }

   public void setTeXEditorWidth(int width)
   {
      getSettings().setTeXEditorWidth(width);
   }

   public int getTeXEditorHeight()
   {
      return getSettings().getTeXEditorHeight();
   }

   public void setTeXEditorHeight(int height)
   {
      getSettings().setTeXEditorHeight(height);
   }

   public Font getTeXEditorFont()
   {
      return getSettings().getTeXEditorFont();
   }

   public void setTeXEditorFont(String name, int size)
   {
      getSettings().setTeXEditorFont(name, size);
   }

   public boolean isSyntaxHighlightingOn()
   {
      return getSettings().isSyntaxHighlightingOn();
   }

   public void setSyntaxHighlighting(boolean enabled)
   {
      getSettings().setSyntaxHighlighting(enabled);
   }

   public Color getCommentHighlight()
   {
      return getSettings().getCommentHighlight();
   }

   public void setCommentHighlight(Color col)
   {
      getSettings().setCommentHighlight(col);
   }

   public Color getControlSequenceHighlight()
   {
      return getSettings().getControlSequenceHighlight();
   }

   public void setControlSequenceHighlight(Color col)
   {
      getSettings().setControlSequenceHighlight(col);
   }


   public void updateTeXEditorStyles()
   {
      texEditorDialog.updateStyles(getSettings());

      JInternalFrame[] frames = theDesktop.getAllFrames();

      for (int i = 0, n=frames.length; i < n; i++)
      {
         JDRFrame f = (JDRFrame)frames[i];

         f.updateEditorStyles(getSettings());
      }
   }

   public void setCanvasSplit(int orientation, boolean isCanvasFirst)
   {
      getSettings().setCanvasSplit(orientation);
      getSettings().setCanvasFirst(isCanvasFirst);
   }

   public boolean useHPaddingShapepar()
   {
      return getSettings().useHPaddingShapepar();
   }

   public void setHPaddingShapepar(boolean flag)
   {
      getSettings().setHPaddingShapepar(flag);
   }

   public boolean useRelativeFontDeclarations()
   {
      return getSettings().useRelativeFontDeclarations();
   }

   public void setRelativeFontDeclarations(boolean flag)
   {
      getSettings().setRelativeFontDeclarations(flag);
   }

   public long getMaxProcessTime()
   {
      return getSettings().getMaxProcessTime();
   }

   public void setMaxProcessTime(long millisecs)
   {
      getSettings().setMaxProcessTime(millisecs);
   }

   public void pageDialog(JDRFrame frame)
   {
      if (invoker.isPrintDisabled())
      {
         getResources().error(this,
            getResources().getString("error.printing.no_service"));

         return;
      }

      PrinterJob printJob = PrinterJob.getPrinterJob();

      Rectangle2D rect = frame.getCanvasGraphics().getImageableArea();

      MediaPrintableArea mpa = new MediaPrintableArea(
          (float)(rect.getX()/72), (float)(rect.getY()/72),
          (float)(rect.getWidth()/72), (float)(rect.getHeight()/72),
          MediaPrintableArea.INCH);

      if (printJob != null)
      {
         printRequestAttributeSet.add(mpa);

         PageFormat pf
            = printJob.pageDialog(printRequestAttributeSet);

         if (pf != null)
         {
            frame.setMargins(pf, true);
         }
      }
      else
      {
          getResources().error(this,
             getResources().getString("error.printing.no_service"));
      }
   }

   public PrintService getPrintService(JDRPaper p)
   {
      if (printRequestAttributeSet == null)
      {
         return null;
      }

      // set paper size
      printRequestAttributeSet.add(p.isPortrait() ?
               OrientationRequested.PORTRAIT :
               OrientationRequested.LANDSCAPE);

      MediaSizeName mediaSizeName = p.getMediaSizeName();

      if (mediaSizeName != null)
      {
         printRequestAttributeSet.add(mediaSizeName);
      }

      // set paper margins

      printRequestAttributeSet.add(p.getMediaPrintableArea());

      // find print services
      PrintService[] services = PrinterJob.lookupPrintServices();

      if (services.length > 0)
      {
         return services[0];
      }

      return null;
   }

   public void doPrintJob(PrinterJob printJob)
      throws PrinterException
   {
      if (invoker.isPrintDisabled())
      {
          getResources().error(this,
             getResources().getString("error.printing.no_service"));

          return;
      }

      if (printJob.printDialog(printRequestAttributeSet))
      {
         printJob.print(printRequestAttributeSet);
      }
   }

   public void displayObjectDescriptionDialog(JDRCompleteObject object)
   {
      if (object != null)
      {
         objectDescriptionDialog.initialise(object);
      }
   }

   public void displayImageDescriptionDialog(JDRCompleteObject object)
   {
      if (object != null)
      {
         imageDescriptionDialog.initialise(object);
      }
   }

   public void displayFindByDescriptionDialog()
   {
      findByDescDialog.display(true);
   }

   public void displayEditTextDialog()
   {
      editTextBox.display();
   }

   public void displayInsertBitmapChooser(JDRFrame frame)
   {
      int result = bitmapFC.showOpenDialog(this);

      if (result == JFileChooser.APPROVE_OPTION)
      {
         String filename = bitmapFC.getSelectedFile().getAbsolutePath();

         frame.insertBitmap(filename);
      }
   }

   public void displayBitmapPropertiesChooser()
   {
      bitmapPropChooserBox.initialise();
   }

   public void displayVectorizeBitmapDialog()
   {
      vectorizeBitmapDialog.display();
   }

   public void repaintVectorizeBitmapDialog()
   {
      if (vectorizeBitmapDialog != null && vectorizeBitmapDialog.isVisible())
      {
         vectorizeBitmapDialog.repaint();
      }
   }

   public boolean isVectorizeBitmapInProgress()
   {
      return vectorizeBitmapDialog.getBitmap() != null;
   }

   public boolean isVectorizeBitmapInProgress(JDRFrame frame)
   {
      return vectorizeBitmapDialog.getBitmap() != null
          && (frame == null || vectorizeBitmapDialog.getBitmapFrame() == frame);
   }

   public boolean closeVectorizeBitmap(JDRFrame frame)
   {
      if (frame == null)
      {
         frame = vectorizeBitmapDialog.getBitmapFrame();
      }

      if (isVectorizeBitmapInProgress(frame))
      {
         if (getResources().confirm(this,
          getResources().getMessage(
            "message.vectorize_in_progress.confirm_close",
            frame.getFilename(), vectorizeBitmapDialog.getBitmap().getName()),
            JOptionPane.YES_NO_OPTION)
            != JOptionPane.YES_OPTION)
         {
            vectorizeBitmapDialog.redisplay();

            return false;
         }

         vectorizeBitmapDialog.cancel(false);
      }

      return true;
   }

   public void displaySegmentInfoDialog(JDRFrame frame,
      JDRShape shape, JDRPathSegment segment)
   {
      segmentInfoDialog.display(frame, shape, segment);
   }

   public void displayTextPaintChooser()
   {
      textPaintChooserBox.initialise();
   }

   public void displayAllFontStylesChooser()
   {
      textStyleChooserBox.initialise();
   }

   public void displayFontFamilyChooser()
   {
      fontFamilyChooserBox.initialise();
   }

   public void displayFontSizeChooser()
   {
      fontSizeChooserBox.initialise();
   }

   public void displayFontShapeChooser()
   {
      fontShapeChooserBox.initialise();
   }

   public void displayFontSeriesChooser()
   {
      fontSeriesChooserBox.initialise();
   }

   public void displayFontAnchorChooser()
   {
      fontAnchorChooserBox.initialise();
   }

   public void displayFontHAnchorChooser()
   {
      fontHAnchorChooserBox.initialise();
   }

   public void displayFontVAnchorChooser()
   {
      fontVAnchorChooserBox.initialise();
   }

   public void displayLinePaintChooser()
   {
      linePaintChooserBox.initialise();
   }

   public void displayFillPaintChooser()
   {
      fillPaintChooserBox.initialise();
   }

   public void displayLineWidthChooser()
   {
      lineWidthChooserBox.initialise();
   }

   public void displayDashPatternChooser()
   {
      dashPatternChooserBox.initialise();
   }

   public void displayJoinStyleChooser()
   {
      joinStyleChooserBox.initialise();
   }

   public void displayStartMarkerChooser()
   {
      startArrowChooserBox.initialise();
   }

   public void displayMidMarkerChooser()
   {
      midArrowChooserBox.initialise();
   }

   public void displayEndMarkerChooser()
   {
      endArrowChooserBox.initialise();
   }

   public void displayAllMarkerChooser()
   {
      allMarkersChooserBox.initialise();
   }

   public void internalFrameClosing(InternalFrameEvent e)
   {
   }

   public void internalFrameClosed(InternalFrameEvent e)
   {
   }

   public void internalFrameOpened(InternalFrameEvent e)
   {
   }

   public void internalFrameIconified(InternalFrameEvent e)
   {
      if (!iconifyFrameUndoRedo)
      {
         UndoableEdit edit = new IconifyFrame(e.getInternalFrame(),
            true);
         postEdit(edit);
      }
   }
 
   public void internalFrameDeiconified(InternalFrameEvent e)
   {
      if (!iconifyFrameUndoRedo)
      {
         UndoableEdit edit = new IconifyFrame(e.getInternalFrame(),
            false);
         postEdit(edit);
      }
   }

   public void internalFrameActivated(InternalFrameEvent e)
   {
      if (!activateFrameUndoRedo)
      {
         UndoableEdit edit = new ActivateFrame(e.getInternalFrame(),
            true);
         postEdit(edit);
      }
   }

   public void internalFrameDeactivated(InternalFrameEvent e)
   {
      setCurrentFile(null);

      if (!activateFrameUndoRedo)
      {
         UndoableEdit edit = new ActivateFrame(e.getInternalFrame(),
            false);
         postEdit(edit);
      }
   }

   class ActivateFrame extends AbstractUndoableEdit
   {
      private JInternalFrame frame_;
      private boolean activate_;

      public ActivateFrame(JInternalFrame frame, boolean activate)
      {
         frame_ = frame;
         activate_ = activate;
      }

      public void redo() throws CannotRedoException
      {
         if (frame_ == null)
         {
            throw new CannotRedoException();
         }

         activateFrameUndoRedo=true;

         try
         {
            frame_.setSelected(activate_);
         }
         catch (java.beans.PropertyVetoException e)
         {
            throw new CannotRedoException();
         }

         activateFrameUndoRedo=false;
      }

      public void undo() throws CannotUndoException
      {
         if (frame_ == null)
         {
            throw new CannotUndoException();
         }

         activateFrameUndoRedo=true;

         try
         {
            frame_.setSelected(!activate_);
         }
         catch (java.beans.PropertyVetoException e)
         {
            throw new CannotUndoException();
         }

         activateFrameUndoRedo=false;
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return activate_ 
                ? getResources().getString("undo.select_child_window")
                : getResources().getString("undo.deselect_child_window");
      }
   }

   class IconifyFrame extends AbstractUndoableEdit
   {
      private boolean iconify_;
      private JInternalFrame frame_;

      public IconifyFrame(JInternalFrame frame, boolean iconify)
      {
         frame_ = frame;
         iconify_ = iconify;
      }

      public void redo() throws CannotRedoException
      {
         iconifyFrameUndoRedo=true;

         try
         {
            frame_.setIcon(iconify_);
         }
         catch (java.beans.PropertyVetoException e)
         {
            throw new CannotRedoException();
         }

         iconifyFrameUndoRedo=false;
      }

      public void undo() throws CannotUndoException
      {
         iconifyFrameUndoRedo=true;

         try
         {
            frame_.setIcon(!iconify_);
         }
         catch (java.beans.PropertyVetoException e)
         {
            throw new CannotUndoException();
         }

         iconifyFrameUndoRedo=false;
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return iconify_ 
                ? getResources().getString("undo.iconify_child_window")
                : getResources().getString("undo.deiconify_child_window");
      }
   }

   public boolean getPreviewBitmaps()
   {
      return getSettings().previewBitmaps;
   }

   public void setPreviewBitmaps(boolean selected)
   {
      getSettings().previewBitmaps = selected;
   }

   public void setRelativeBitmaps(boolean flag)
   {
      getSettings().setRelativeBitmaps(flag);
   }

   public boolean useRelativeBitmaps()
   {
      return getSettings().useRelativeBitmaps();
   }

   public void setDefaultBitmapCommand(String cmdName)
   {
      getSettings().setDefaultBitmapCommand(cmdName);
   }

   public String getDefaultBitmapCommand()
   {
      return getSettings().getDefaultBitmapCommand();
   }

   public int[][] getUnicodeRanges()
   {
      return getSettings().getUnicodeRanges();
   }

   public void setUnicodeRanges(int[][] ranges)
   {
      getSettings().setUnicodeRanges(ranges);
   }

   public String getDictId()
   {
      return getSettings().getDictId();
   }

   public String getHelpId()
   {
      return getSettings().getHelpId();
   }

   public void setDictId(String value)
   {
      getSettings().setDictId(value);
   }

   public void setHelpId(String value)
   {
      getSettings().setHelpId(value);
   }

   public CharacterSelector getCharacterSelector()
   {
      return characterSelector;
   }

   public void setSymbolText(String text)
   {
      getCurrentFrame().getCanvas().setSymbolText(text);
   }

   public Font getSymbolFont()
   {
      return getCurrentFrame().getCanvas().getSymbolFont();
   }

   public String getSymbolText()
   {
      return getCurrentFrame().getCanvas().getSymbolText();
   }

   public Font getSymbolButtonFont()
   {
      return getCurrentFrame().getCanvas().getSymbolButtonFont();
   }

   public int getSymbolCaretPosition()
   {
      return getCurrentFrame().getCanvas().getSymbolCaretPosition();
   }

   public void setSymbolCaretPosition(int position)
   {
      getCurrentFrame().getCanvas().setSymbolCaretPosition(position);
   }

   public void requestSymbolFocus()
   {
      getCurrentFrame().getCanvas().requestSymbolFocus();
   }

   public String getLaTeXApp()
   {
      return getSettings().getLaTeXApp();
   }

   public void setLaTeXApp(String path)
   {
      getSettings().setLaTeXApp(path);
   }

   public String getPdfLaTeXApp()
   {
      return getSettings().getPdfLaTeXApp();
   }

   public void setPdfLaTeXApp(String path)
   {
      getSettings().setPdfLaTeXApp(path);
   }

   public String getDvipsApp()
   {
      return invoker.getSettings().getDvipsApp();
   }

   public void setDvipsApp(String path)
   {
      invoker.getSettings().setDvipsApp(path);
   }

   public String getDvisvgmApp()
   {
      return invoker.getSettings().getDvisvgmApp();
   }

   public void setDvisvgmApp(String path)
   {
      invoker.getSettings().setDvisvgmApp(path);
   }

   public String getLibgs()
   {
      return invoker.getSettings().getLibgs();
   }

   public void setLibgs(String libgs)
   {
      invoker.getSettings().setLibgs(libgs);
   }

   public String requestLaTeXApp()
   {
      String app = getLaTeXApp();

      if (app != null && !app.isEmpty())
      {
         return app;
      }

      File path = null;

      path = requestApp("latex");

      if (path != null)
      {
         setLaTeXApp(path.getAbsolutePath());
      }

      return getLaTeXApp();
   }

   public String requestDvipsApp()
   {
      String app = getDvipsApp();

      if (app != null && !app.isEmpty())
      {
         return app;
      }

      File path = null;

      path = requestApp("dvips");

      if (path != null)
      {
         setDvipsApp(path.getAbsolutePath());
      }

      return getDvipsApp();
   }

   public String requestDvisvgmApp()
   {
      String app = getDvisvgmApp();

      if (app != null && !app.isEmpty())
      {
         return app;
      }

      File path = null;

      path = requestApp("dvisvgm");

      if (path != null)
      {
         setDvisvgmApp(path.getAbsolutePath());
      }

      return getDvisvgmApp();
   }

   public File requestApp(String appName)
   {
      File path = appSelector.fetchApplicationPath(appName,
         getResources().getMessage("appselect.query.location", appName));

      if (!path.exists())
      {
         getResources().error(
            getResources().getMessage("error.cant_find_app", appName));
         return null;
      }

      return path;
   }

   public boolean isRobotEnabled()
   {
      return (getSettings().robot != null);
   }

   public void setToolBarsVisible(boolean flag)
   {
      hSlidingBar.setVisible(flag);
      vSlidingBar.setVisible(flag);
      getSettings().showToolBar=flag;
   }

   public boolean hasToolBars()
   {
     return hSlidingBar.isVisible();
   }

   public void setStatusBarVisible(boolean flag)
   {
      statusBar.setVisible(flag);
      getSettings().showStatus=flag;
   }

   public boolean hasStatusBar()
   {
      return statusBar.isVisible();
   }

   public boolean isStatusZoomVisible()
   {
      return statusBar.isZoomVisible();
   }

   public boolean isStatusPositionVisible()
   {
      return statusBar.isPositionVisible();
   }

   public boolean isStatusModifiedVisible()
   {
      return statusBar.isModifiedVisible();
   }

   public boolean isStatusLockVisible()
   {
      return statusBar.isLockVisible();
   }

   public boolean isStatusUnitVisible()
   {
      return statusBar.isUnitVisible();
   }

   public boolean isStatusInfoVisible()
   {
      return statusBar.isInfoVisible();
   }

   public boolean isStatusHelpVisible()
   {
      return statusBar.isHelpVisible();
   }

   public int getHRulerHeight()
   {
      return getSettings().getHRulerHeight();
   }

   public int getVRulerWidth()
   {
      return getSettings().getVRulerWidth();
   }

   public void setHRulerHeight(int height)
   {
      if (height == 0)
      {
         throw new IllegalArgumentException(
           getResources().getMessage("error.invalid_ruler_height", height));
      }

      getSettings().setHRulerHeight(height);
   }

   public void setRulerConf(Font rulerFont,
      int sideWidth, int topHeight,
      String pattern, Locale locale)
      throws IllegalArgumentException
   {
      getSettings().setRulerFormat(pattern, locale);
      getSettings().setRulerFont(rulerFont);
      setVRulerWidth(sideWidth);
      setHRulerHeight(topHeight);

      JInternalFrame[] frames = theDesktop.getAllFrames();

      if (frames != null)
      {
         for (int i = 0; i < frames.length; i++)
         {
            ((JDRFrame)frames[i]).setRulerConf(rulerFont, sideWidth, topHeight);
         }
      }
   }

   public void setVRulerWidth(int width)
      throws IllegalArgumentException
   {
      if (width == 0)
      {
         throw new IllegalArgumentException(
           getResources().getMessage("error.invalid_ruler_width", width));
      }

      getSettings().setVRulerWidth(width);
   }

   public DecimalFormat getRulerFormat()
   {
      return getSettings().getRulerFormat();
   }

   public Locale getRulerLocale()
   {
      return getSettings().getRulerLocale();
   }

   public Font getRulerFont()
   {
      Font f = getSettings().getRulerFont();

      if (f == null)
      {
         JInternalFrame comp = 
          (theDesktop == null ? null : theDesktop.getSelectedFrame());

         if (comp == null)
         {
            f = getFont();
         }
         else
         {
            f = ((JDRFrame)comp).getRulerFont();
         }
      }

      return f == null ? Font.decode("Dialog") : f;
   }

   public Font getStatusFont()
   {
      return getSettings().getStatusFont();
   }

   public void setStatusFont(Font f)
   {
      getSettings().setStatusFont(f);
   }

   public int getStatusHeight()
   {
      return getSettings().getStatusHeight();
   }

   public void setStatusHeight(int height)
   {
      getSettings().setStatusHeight(height);
   }

   public int getStatusPositionWidth()
   {
      return getSettings().getStatusPositionWidth();
   }

   public void setStatusPositionWidth(int width)
   {
      getSettings().setStatusPositionWidth(width);
   }

   public int getStatusUnitWidth()
   {
      return getSettings().getStatusUnitWidth();
   }

   public void setStatusUnitWidth(int width)
   {
      getSettings().setStatusUnitWidth(width);
   }

   public int getStatusModifiedWidth()
   {
      return getSettings().getStatusModifiedWidth();
   }

   public void setStatusModifiedWidth(int width)
   {
      getSettings().setStatusModifiedWidth(width);
   }

   public boolean isLaTeXFontUpdateEnabled()
   {
      return getSettings().updateLaTeXFonts;
   }

   public void setLaTeXFontUpdate(boolean update)
   {
      getSettings().updateLaTeXFonts = update;
   }

   public void setAutoAnchor(boolean update)
   {
      getSettings().autoUpdateAnchors = update;
   }

   public boolean isAutoAnchorEnabled()
   {
      return getSettings().autoUpdateAnchors;
   }

   public void setAutoEscapeSpChars(boolean enabled)
   {
      getSettings().autoEscapeSpChars = enabled;
   }

   public boolean isAutoEscapeSpCharsEnabled()
   {
      return getSettings().autoEscapeSpChars;
   }

   public String applyTextModeMappings(String original, Vector<String> styNames)
   {
      return getSettings().applyTextModeMappings(original, styNames);
   }

   public void setAutoEscapeMathChars(boolean enabled)
   {
      getSettings().autoEscapeMathChars = enabled;
   }

   public boolean isAutoEscapeMathCharsEnabled()
   {
      return getSettings().autoEscapeMathChars;
   }

   public String applyMathModeMappings(String original, Vector<String> styNames)
   {
      return getSettings().applyMathModeMappings(original, styNames);
   }

   public TextModeMappings getTextModeMappings()
   {
      return getSettings().getTextModeMappings();
   }

   public MathModeMappings getMathModeMappings()
   {
      return getSettings().getMathModeMappings();
   }

   public File getConfigPreambleFile()
   {
      return invoker.getConfigPreambleFile();
   }

   public String getConfigPreamble()
     throws IOException
   {
      File file = getConfigPreambleFile();

      if (file == null || !file.exists())
      {
         return null;
      }

      FileReader in = null;

      try
      {
         in = new FileReader(file);

         StringBuilder builder = new StringBuilder();

         int code;

         while ((code = in.read()) != -1)
         {
            builder.appendCodePoint(code);
         }

         return builder.toString();
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
   }

   public CanvasGraphics getDefaultCanvasGraphics()
   {
      return getSettings().getCanvasGraphics();
   }

   public void moveMouse(int screenX, int screenY)
   {
      if (getSettings().robot != null)
      {
         getSettings().robot.mouseMove(screenX, screenY);
      }
   }

   public String[] getFontFamilies()
   {
      return availableFontFamilies;
   }

   public int initSettings()
   {
      return getSettings().initSettings;
   }

   public void setInitSettings(int i)
   {
      getSettings().initSettings=i;

      if (getSettings().initSettings==FlowframTkSettings.INIT_USER)
      {
         try
         {
            invoker.saveUserSettings();
         }
         catch (IOException e)
         {
            getResources().error(this, e);
         }
      }
   }

   public int getSaveJDRsettings()
   {
      return getSettings().getSaveSettings();
   }

   public int useJDRsettings()
   {
      return getSettings().getUseSettingsOnLoad();
   }

   public boolean warnOnOldJdr()
   {
      return getSettings().warnOnOldJdr;
   }

   public void setJDRsettings(int saveJDRSettings, 
                              int useJDRSettings,
                              boolean warnOnOld)
   {
      getSettings().setSaveSettings(saveJDRSettings);
      getSettings().setUseSettingsOnLoad(useJDRSettings);
      getSettings().warnOnOldJdr = warnOnOld;
   }

   public void setStorageUnit(byte unitId)
   {
      getSettings().getCanvasGraphics().setStorageUnit(unitId);

      JDRFrame frame = getCurrentFrame();

      if (frame != null)
      {
         frame.setStorageUnit(unitId);
      }
   }

   public int getStartDirType()
   {
      return getSettings().startDirType;
   }

   public String getStartDirectory()
   {
      return getSettings().startDir;
   }

   public void setStartDirectory(int startDirType, String dir)
   {
      getSettings().startDirType = startDirType;

      switch (startDirType)
      {
         case FlowframTkSettings.STARTDIR_CWD :
            getSettings().startDir = ".";
         break;
         case FlowframTkSettings.STARTDIR_NAMED :
            getSettings().startDir = dir;
         break;
      }
   }

   public void setCurrentFile(File file)
   {
      setCurrentFile(file, false);
   }

   public void setCurrentFile(File file, boolean modified)
   {
      setModified(modified);

      currentFile = file;

      if (file == null)
      {
         return;
      }

      File parent = file.getParentFile();

      if (parent != null)
      {
         savejdrFC.setCurrentDirectory(parent);
         openjdrFC.setCurrentDirectory(parent);
      }
   }

   public File getCurrentDirectory()
   {
      return savejdrFC.getCurrentDirectory();
   }

   public void setStatusStorageUnit(JDRUnit unit)
   {
      statusBar.setStorageUnit(unit);
   }

   public void setStatusStorageUnit(byte id)
   {
      statusBar.setStorageUnit(id);
   }

   public void setModified(boolean modified)
   {
      statusBar.setModified(modified);

      updateTitle();
   }

   public void updateTitle()
   {
      JDRFrame frame = getCurrentFrame();

      if (frame == null)
      {
         setTitle(invoker.getName());
      }
      else
      {
         setTitle(invoker.getName()+" - "+frame.getTitle());
      }
   }

   public void displayGridSettings()
   {
      gridSettingsChooserBox.display();
   }

   public double getCurrentMagnification()
   {
      JDRFrame frame = getCurrentFrame();

      if (frame == null)
      {
         return getSettings().getCanvasGraphics().getMagnification();
      }

      return frame.getMagnification();
   }

   public void setCurrentMagnification(double factor)
   {
      if (factor <= 0)
      {
         return;
      }

      JDRFrame frame = getCurrentFrame();

      if (frame != null)
      {
         frame.setMagnification(factor);
      }
   }

   public void updateZoom(double factor)
   {
      switch ((int)Math.round(factor*100.0))
      {
         case 25: zoom25Item.setSelected(true); break;
         case 75: zoom75Item.setSelected(true); break;
         case 100: zoom100Item.setSelected(true); break;
         case 200: zoom200Item.setSelected(true); break;
         case 400: zoom400Item.setSelected(true); break;
         case 800: zoom800Item.setSelected(true); break;
         default: zoomSettingsItem.setSelected(true);
      }

      if (zoomSettingsChooserBox.getMag() != factor)
      {
         zoomSettingsChooserBox.setMag(factor);
      }

      statusBar.updateZoom(factor);
   }

   public void showZoomChooser()
   {
      JDRFrame frame = getCurrentFrame();

      if (frame != null)
      {
         zoomSettingsChooserBox.display();
      }
   }

   public double zoomAction(ZoomValue zoomValue)
   {
      JDRFrame frame = getCurrentFrame();

      if (frame == null)
      {
         return -1.0;
      }

      if (zoomValue instanceof PercentageZoomValue)
      {
         double mag = ((PercentageZoomValue)zoomValue).getValue();
         frame.setMagnification(mag);

         return mag;
      }

      String action = zoomValue.getActionCommand();

      if (action.equals(ZoomValue.ZOOM_PAGE_WIDTH_ID))
      {
         frame.zoomWidth();
         zoomWidthItem.setSelected(true);
      }
      else if (action.equals(ZoomValue.ZOOM_PAGE_HEIGHT_ID))
      {
         frame.zoomHeight();
         zoomHeightItem.setSelected(true);
      }
      else if (action.equals(ZoomValue.ZOOM_PAGE_ID))
      {
         frame.zoomPage();
         zoomPageItem.setSelected(true);
      }

      return frame.getMagnification();
   }

   /**
    * Gets the magnification specified by the zoom settings.
    */
   public double getMagnification()
   {
      return zoomSettingsChooserBox.getMag();
   }

   public void displayGoToDialog()
   {
      gotoDialog.display();
   }

   public boolean showMargins()
   {
      return showPrinterMarginsItem.isSelected();
   }

   public boolean dragScaleEnabled()
   {
      return getSettings().enableDragScale;
   }

   public void setDragScale(boolean isEnabled)
   {
      getSettings().enableDragScale = isEnabled;
   }

   public void setCurrentPosition(String position)
   {
      statusBar.setPosition(position);
   }

   public boolean isPathEdited()
   {
      return editPathButtonItem.isSelected();
   }

   public boolean isObjectDistorting()
   {
      return distortButtonItem.isSelected();
   }

   public void setTool(int tool)
   {
      JDRFrame frame = getSelectedFrame();

      int prevTool = (frame == null ? -1 : 
        frame.getCanvasGraphics().getTool());

      ToolAction.setTool(this, toolButtonGroup, tool);

      if (frame != null && prevTool != tool)
      {
         frame.setAction(tool);
      }

      getDefaultCanvasGraphics().setTool(tool);

      updateToolActionButtons(tool == ACTION_SELECT);
   }

   public StatusBar getStatusBar()
   {
      return statusBar;
   }

   public String getStatusInfo()
   {
      return statusBar.getInfo();
   }

   public void setStatusInfo(String info)
   {
      statusBar.setInfo(info);
   }

   public void setStatusInfo(String info, String helpId)
   {
      statusBar.setInfo(info, helpId);
   }

   public void updateStatus(int n)
   {
      statusBar.noFramesSelected(n);
   }

   public void updateStatus()
   {
      JDRFrame frame = getSelectedFrame();

      if (frame == null)
      {
         JInternalFrame[] f = theDesktop.getAllFrames();

         updateStatus(f.length);
      }
      else
      {
         updateZoom(frame.getMagnification());
         setStatusStorageUnit(frame.getCanvasGraphics().getStorageUnit());
      }
   }

   public void setPaperSize(JDRPaper paper)
   {
      if (paper == JDRPaper.A0)
      {
         paperA0Item.setSelected(true);
         portraitItem.setSelected(true);
      }
      else if (paper == JDRPaper.A1)
      {
         paperA1Item.setSelected(true);
         portraitItem.setSelected(true);
      }
      else if (paper == JDRPaper.A2)
      {
         paperA2Item.setSelected(true);
         portraitItem.setSelected(true);
      }
      else if (paper == JDRPaper.A3)
      {
         paperA3Item.setSelected(true);
         portraitItem.setSelected(true);
      }
      else if (paper == JDRPaper.A4)
      {
         paperA4Item.setSelected(true);
         portraitItem.setSelected(true);
      }
      else if (paper == JDRPaper.A5)
      {
         paperA5Item.setSelected(true);
         portraitItem.setSelected(true);
      }
      else if (paper == JDRPaper.LETTER)
      {
         paperLetterItem.setSelected(true);
         portraitItem.setSelected(true);
      }
      else if (paper == JDRPaper.LEGAL)
      {
         paperLegalItem.setSelected(true);
         portraitItem.setSelected(true);
      }
      else if (paper == JDRPaper.EXECUTIVE)
      {
         paperExecutiveItem.setSelected(true);
         portraitItem.setSelected(true);
      }
      else if (paper == JDRPaper.A0R)
      {
         paperA0Item.setSelected(true);
         landscapeItem.setSelected(true);
      }
      else if (paper == JDRPaper.A1R)
      {
         paperA1Item.setSelected(true);
         landscapeItem.setSelected(true);
      }
      else if (paper == JDRPaper.A2R)
      {
         paperA2Item.setSelected(true);
         landscapeItem.setSelected(true);
      }
      else if (paper == JDRPaper.A3R)
      {
         paperA3Item.setSelected(true);
         landscapeItem.setSelected(true);
      }
      else if (paper == JDRPaper.A4R)
      {
         paperA4Item.setSelected(true);
         landscapeItem.setSelected(true);
      }
      else if (paper == JDRPaper.A5R)
      {
         paperA5Item.setSelected(true);
         landscapeItem.setSelected(true);
      }
      else if (paper == JDRPaper.LETTERR)
      {
         paperLetterItem.setSelected(true);
         landscapeItem.setSelected(true);
      }
      else if (paper == JDRPaper.LEGALR)
      {
         paperLegalItem.setSelected(true);
         landscapeItem.setSelected(true);
      }
      else if (paper == JDRPaper.EXECUTIVER)
      {
         paperExecutiveItem.setSelected(true);
         landscapeItem.setSelected(true);
      }
      else
      {
         paperOtherItem.setSelected(true);
         paperDialog.setPaper(paper, 
            getCurrentCanvasGraphics().getStorageUnit());

         if (paper.isPortrait())
         {
            portraitItem.setSelected(true);
         }
         else
         {
            landscapeItem.setSelected(true);
         }
      }

      getPrintService(paper);
      getDefaultCanvasGraphics().setPaper(paper);
   }

   public void showMoveByDialog()
   {
      if (moveByItem.isEnabled())
      {
         moveByDialog.initialise();
      }
   }

   public void setRulers(boolean flag)
   {
      showRulersItem.setSelected(flag);
      getDefaultCanvasGraphics().setShowRulers(flag);
   }

   public void setShowGridButtonItemState(boolean selected)
   {
      showGridButtonItem.setSelected(selected);
      getDefaultCanvasGraphics().setDisplayGrid(selected);
   }

   public void setGridDisplay(boolean flag)
   {
      JDRFrame frame = (JDRFrame)theDesktop.getSelectedFrame();

      setShowGridButtonItemState(flag);

      if (frame != null) frame.showGrid(flag);
   }

   public boolean isGridDisplayed()
   {
      return showGridButtonItem.isSelected();
   }

   public void setLockGrid(boolean flag)
   {
      lockGridButtonItem.setSelected(flag);
      statusBar.setLock(flag);
      getDefaultCanvasGraphics().setGridLock(flag);

      JDRFrame frame = getCurrentFrame();
      if (frame != null)
      {
         CanvasGraphics cg = frame.getCanvasGraphics();

         if (cg != null)
         {
            cg.setGridLock(flag);
         }
      }
   }

   public void applySettings(CanvasGraphics cg)
   {
      setRulers(cg.showRulers());
      setGridDisplay(cg.isGridDisplayed());
      setTool(cg.getTool());
      setPaperSize(cg.getPaper());
   }

   public void setPointSize(JDRLength pointSize, boolean isScaleEnabled)
   {
      getSettings().getCanvasGraphics().setPointSize(pointSize, isScaleEnabled);

      JDRFrame frame = getCurrentFrame();

      if (frame != null)
      {
         frame.getCanvasGraphics().setPointSize(pointSize, isScaleEnabled);
      }
   }

   public boolean getAntiAlias()
   {
      return getSettings().isAntiAliasOn();
   }

   public boolean getRenderQuality()
   {
      return getSettings().isRenderQualityOn();
   }

   public RenderingHints getRenderingHints()
   {
      return getSettings().getRenderingHints();
   }

   public void setRendering(boolean antialias, boolean renderquality)
   {
      getSettings().setRendering(antialias, renderquality);
   }

   public boolean isPortrait()
   {
      return portraitItem.isSelected();
   }

   public JDRPaper getCurrentPaper()
   {
      boolean isPortrait = isPortrait();

      if (paperA0Item.isSelected())
      {
         return isPortrait ? JDRPaper.A0 : JDRPaper.A0R;
      }
      else if (paperA1Item.isSelected())
      {
         return isPortrait ? JDRPaper.A1 : JDRPaper.A1R;
      }
      else if (paperA2Item.isSelected())
      {
         return isPortrait ? JDRPaper.A2 : JDRPaper.A2R;
      }
      else if (paperA3Item.isSelected())
      {
         return isPortrait ? JDRPaper.A3 : JDRPaper.A3R;
      }
      else if (paperA4Item.isSelected())
      {
         return isPortrait ? JDRPaper.A4 : JDRPaper.A4R;
      }
      else if (paperA5Item.isSelected())
      {
         return isPortrait ? JDRPaper.A5 : JDRPaper.A5R;
      }
      else if (paperLetterItem.isSelected())
      {
         return isPortrait ? JDRPaper.LETTER : JDRPaper.LETTERR;
      }
      else if (paperLegalItem.isSelected())
      {
         return isPortrait ? JDRPaper.LEGAL : JDRPaper.LEGALR;
      }
      else if (paperExecutiveItem.isSelected())
      {
         return isPortrait ? JDRPaper.EXECUTIVE : JDRPaper.EXECUTIVER;
      }

      return paperDialog.getPaper();
   }

   public void setCurrentSettings(JDRPaint linePaint,
      JDRPaint fillPaint, JDRPaint textPaint, JDRBasicStroke stroke,
      String fontFamilyName, JDRLength fontSize, int fontSeries,
      int fontShape, String latexFamily, String latexFontSize,
      String latexSeries, String latexFontShape, int pgfHalign,
      int pgfValign)
   {
      UndoableEdit edit = new SetCurrentSettings(linePaint,
         fillPaint, textPaint, stroke, fontFamilyName, fontSize,
         fontSeries, fontShape, latexFamily, latexFontSize,
         latexSeries, latexFontShape, pgfHalign, pgfValign);

      postEdit(edit);
   }

   class SetCurrentSettings extends AbstractUndoableEdit
   {
      private String string_=getResources().getString("undo.set_current_settings");
      private JDRPaint oldLinePaint_, linePaint_, oldFillPaint_,
         fillPaint_, oldTextPaint_, textPaint_;
      private JDRBasicStroke oldStroke_, stroke_;
      private String oldFamilyName_, familyName_, oldLatexFamily_,
        latexFamily_, oldLatexSize_, latexSize_, oldLatexSeries_,
        latexSeries_, oldLatexShape_, latexShape_;
      private int oldSeries_, series_, oldShape_,
         shape_, oldHalign_, hAlign_, oldValign_, vAlign_;

      private JDRLength oldSize_, size_;

      public SetCurrentSettings(JDRPaint linePaint,
         JDRPaint fillPaint, JDRPaint textPaint, JDRBasicStroke stroke,
         String fontFamilyName, JDRLength fontSize, int fontSeries,
         int fontShape, String latexFamily, String latexFontSize,
         String latexFontSeries, String latexFontShape, int pgfHalign,
         int pgfValign)
      {
         oldLinePaint_ = getSettings().getLinePaint();
         linePaint_    = linePaint;
         oldFillPaint_ = getSettings().getFillPaint();
         fillPaint_    = fillPaint;
         oldTextPaint_ = getSettings().getTextPaint();
         textPaint_    = textPaint;
         oldStroke_    = getSettings().getStroke();
         stroke_       = stroke;
         oldFamilyName_ = getSettings().getFontFamily();
         familyName_    = fontFamilyName;
         oldSize_       = getSettings().getFontSize();
         size_          = fontSize;
         oldShape_      = getSettings().getFontShape();
         shape_         = fontShape;
         oldSeries_     = getSettings().getFontSeries();
         series_        = fontSeries;
         oldHalign_     = getSettings().pgfHalign;
         hAlign_        = pgfHalign;
         oldValign_     = getSettings().pgfValign;
         vAlign_        = pgfValign;
         oldLatexFamily_ = getSettings().getLaTeXFontFamily();
         latexFamily_    = latexFamily;
         oldLatexShape_  = getSettings().getLaTeXFontShape();
         latexShape_     = latexFontShape;
         oldLatexSize_   = getSettings().getLaTeXFontSize();
         latexSize_      = latexFontSize;
         oldLatexSeries_ = getSettings().getLaTeXFontSeries();
         latexSeries_    = latexFontSeries;

         getSettings().setLinePaint(linePaint_);
         getSettings().setFillPaint(fillPaint_);
         getSettings().setTextPaint(textPaint_);
         getSettings().setStroke(stroke_);
         getSettings().setFontFamily(familyName_);
         getSettings().setFontSize(size_);
         getSettings().setFontSeries(series_);
         getSettings().setFontShape(shape_);
         getSettings().pgfHalign  = hAlign_;
         getSettings().pgfValign  = vAlign_;
         getSettings().setLaTeXFontFamily(latexFamily_);
         getSettings().setLaTeXFontSeries(latexSeries_);
         getSettings().setLaTeXFontSize(latexSize_);
         getSettings().setLaTeXFontShape(latexShape_);
         JDRFrame frame = (JDRFrame)theDesktop.getSelectedFrame();
         if (frame!=null) frame.resetTextField();
      }

      public void redo() throws CannotRedoException
      {
         getSettings().setLinePaint(linePaint_);
         getSettings().setFillPaint(fillPaint_);
         getSettings().setTextPaint(textPaint_);
         getSettings().setStroke(stroke_);
         getSettings().setFontFamily(familyName_);
         getSettings().setFontSize(size_);
         getSettings().setFontSeries(series_);
         getSettings().setFontShape(shape_);
         getSettings().pgfHalign  = hAlign_;
         getSettings().pgfValign  = vAlign_;
         getSettings().setLaTeXFontFamily(latexFamily_);
         getSettings().setLaTeXFontSeries(latexSeries_);
         getSettings().setLaTeXFontSize(latexSize_);
         getSettings().setLaTeXFontShape(latexShape_);
         JDRFrame frame = (JDRFrame)theDesktop.getSelectedFrame();
         if (frame!=null) frame.resetTextField();
      }

      public void undo() throws CannotUndoException
      {
         getSettings().setLinePaint(oldLinePaint_);
         getSettings().setFillPaint(oldFillPaint_);
         getSettings().setTextPaint(oldTextPaint_);
         getSettings().setStroke(oldStroke_);
         getSettings().setFontFamily(oldFamilyName_);
         getSettings().setFontSize(oldSize_);
         getSettings().setFontSeries(oldSeries_);
         getSettings().setFontShape(oldShape_);
         getSettings().pgfHalign  = oldHalign_;
         getSettings().pgfValign  = oldValign_;
         getSettings().setLaTeXFontFamily(oldLatexFamily_);
         getSettings().setLaTeXFontSeries(oldLatexSeries_);
         getSettings().setLaTeXFontSize(oldLatexSize_);
         getSettings().setLaTeXFontShape(oldLatexShape_);
         JDRFrame frame = (JDRFrame)theDesktop.getSelectedFrame();
         if (frame!=null) frame.resetTextField();
      }

      public boolean canUndo() {return true;}
      public boolean canRedo() {return true;}

      public String getPresentationName()
      {
         return string_;
      }
   }

   public JDRBasicStroke getCurrentStroke()
   {
      return getSettings().getStroke();
   }

   public JDRPaint getCurrentLinePaint()
   {
      return getSettings().getLinePaint();
   }

   public JDRPaint getCurrentFillPaint()
   {
      return getSettings().getFillPaint();
   }

   public JDRPaint getCurrentTextPaint()
   {
      return getSettings().getTextPaint();
   }

   public String getCurrentFontFamily()
   {
      return getSettings().fontFamily;
   }

   public String getCurrentLaTeXFontFamily()
   {
      return getSettings().getLaTeXFontFamily();
   }

   public JDRLength getCurrentFontSize()
   {
      return getSettings().getFontSize();
   }

   public int getCurrentFontSeries()
   {
      return getSettings().getFontSeries();
   }

   public int getCurrentFontShape()
   {
      return getSettings().getFontShape();
   }

   public int getCurrentPGFHAlign()
   {
      return getSettings().pgfHalign;
   }

   public int getCurrentPGFVAlign()
   {
      return getSettings().pgfValign;
   }

   public String getCurrentLaTeXFontSeries()
   {
      return getSettings().getLaTeXFontSeries();
   }

   public LaTeXFontBase getCurrentLaTeXFontBase()
   {
      JDRFrame frame = getCurrentFrame();

      if (frame != null)
      {
         return frame.getLaTeXFontBase();
      }

      return null;
   }

   public String getCurrentLaTeXFontSize()
   {
      JDRFrame frame = getCurrentFrame();

      if (frame != null)
      {
         return frame.getLaTeXFontBase().getLaTeXCmd(
            getCurrentFontSize());
      }

      return getSettings().getLaTeXFontSize();
   }

   public String getCurrentLaTeXFontShape()
   {
      return getSettings().latexFontShape;
   }

   public int getCurrentFontWeight()
   {
      int fontSeries = getCurrentFontSeries();
      int fontShape  = getCurrentFontShape();

      int weight = 0;
      weight += (fontSeries == JDRFont.SERIES_MEDIUM?
                 Font.PLAIN : Font.BOLD);
      weight += (fontShape == JDRFont.SHAPE_UPRIGHT?
                 0 : Font.ITALIC);

      return weight;
   }

   private Font requestNewFontFamily()
   {
      JComboBox<String> box = new JComboBox<String>(availableFontFamilies);

      JOptionPane.showMessageDialog(this,
         new Object[]{
            getResources().getString("error.unknown_font")+": "
               +getCurrentFontFamily(),
            getResources().getString("error.unknown_font_select"),
            box},
            getResources().getString("error.unknown_font"),
            JOptionPane.ERROR_MESSAGE
         );

      getSettings().setFontFamily((String)box.getSelectedItem());
      getSettings().setFontShape(JDRFont.SHAPE_UPRIGHT);
      getSettings().setFontSeries(JDRFont.SERIES_MEDIUM);

      styleChooserBox.setFontName(getSettings().fontFamily);

      return new Font(getCurrentFontFamily(),
         getCurrentFontWeight(), 
         (int)getCurrentFontSize().getValue(JDRUnit.bp));
   }

   public JDRFont getCurrentFont()
   {
      return new JDRFont(getCurrentFontFamily(),
            getCurrentFontSeries(), 
            getCurrentFontShape(), 
            getCurrentFontSize());
   }

   public int getCurrentTool()
   {
      return getDefaultCanvasGraphics().getTool();
/*

      if (selectButtonItem.isSelected())
      {
         return ACTION_SELECT;
      }
      else if (openLineButtonItem.isSelected())
      {
         return ACTION_OPEN_LINE;
      }
      else if (closedLineButtonItem.isSelected())
      {
         return ACTION_CLOSED_LINE;
      }
      else if (openCurveButtonItem.isSelected())
      {
         return ACTION_OPEN_CURVE;
      }
      else if (closedCurveButtonItem.isSelected())
      {
         return ACTION_CLOSED_CURVE;
      }
      else if (rectangleButtonItem.isSelected())
      {
         return ACTION_RECTANGLE;
      }
      else if (ellipseButtonItem.isSelected())
      {
         return ACTION_ELLIPSE;
      }

      return ACTION_TEXT;
*/
   }

   public void saveString(String str)
   {
      int result = texFC.showSaveDialog(this);

      if (result == JFileChooser.APPROVE_OPTION)
      {
         File file = texFC.getSelectedFile();

         String filename = file.getAbsolutePath();

         // does the file already exist?
         if (file.exists())
         {
            int selection = getResources().confirm(this,
               new String[] {filename,
               getResources().getString("warning.file_exists")},
               getResources().getString("warning.title"),
               JOptionPane.YES_NO_OPTION,
               JOptionPane.WARNING_MESSAGE);

               if (selection == JOptionPane.NO_OPTION) return;
         }

         // save string to file

         PrintWriter out;

         try
         {
            out = new PrintWriter(new FileWriter(filename));
         }
         catch (IOException e)
         {
            getResources().error(this, new String[]
            {getResources().getMessage("error.io.open",
               filename),
            e.getMessage()});

            return;
         }
         catch (Exception e)
         {
            getResources().error(this, e);
            return;
         }

         out.println(str);

         out.close();
      }
   }

   public void undoableEditHappened(UndoableEditEvent evt)
   {
      addUndoableEdit(evt.getEdit());
   }

   public void addUndoableEdit(UndoableEdit edit)
   {
      undoManager.addEdit(edit);
      refreshUndoRedo();
   }

   private class UndoAction extends AbstractAction
   {
      public void actionPerformed(ActionEvent evt)
      {
         undoManager.undo();
         refreshUndoRedo();
      }
   }

   private class RedoAction extends AbstractAction
   {
      public void actionPerformed(ActionEvent evt)
      {
         undoManager.redo();
         refreshUndoRedo();
      }
   }

   public void refreshUndoRedo()
   {
      editM.setEnabled(true);
      undoItem.setText(undoManager.getUndoPresentationName());
      undoItem.setEnabled(undoManager.canUndo());

      redoItem.setText(undoManager.getRedoPresentationName());
      redoItem.setEnabled(undoManager.canRedo());
   }

   public void postEdit(UndoableEdit edit)
   {
      if (edit != null && edit.canUndo())
      {
         undoSupport.postEdit(edit);
      }
   }

   public void discardAllEdits()
   {
      undoManager.discardAllEdits();
      refreshUndoRedo();
   }

   public void disableUndoRedo()
   {
      undoItem.setEnabled(false);
      redoItem.setEnabled(false);
   }

   public void updateActionButtons()
   {
      updateActionButtons(false);
   }

   public void updateActionButtons(boolean useSelectionFlags)
   {
      JDRFrame frame = getSelectedFrame();

      boolean ioInProgress;
      int currentTool;
      byte currentEditFlag;
      byte currentConstructionFlag;

      if (frame != null)
      {
         ioInProgress = frame.isIoInProgress();
         currentTool = frame.currentTool();
         currentEditFlag = frame.getEditFlag();
         currentConstructionFlag = frame.getConstructionFlag();
      }
      else
      {
         ioInProgress = false;
         currentTool = -1;
         currentEditFlag = EDIT_FLAG_NONE;
         currentConstructionFlag = FlowframTkAction.CONSTRUCTION_FLAG_NONE;
      }

      JDRSelection selection = null;

      if (useSelectionFlags && frame != null)
      {
         selection = frame.getSelectionFlags();
      }

      for (FlowframTkAction action : generalActionList)
      {
         action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);
      }

      for (FlowframTkAction action : toolActionList)
      {
         action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);
      }

      for (FlowframTkAction action : selectActionList)
      {
         action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);
      }
   }

   public void updateToolActionButtons(boolean useSelectionFlags)
   {
      JDRFrame frame = getSelectedFrame();

      boolean ioInProgress;
      int currentTool;
      byte currentEditFlag;
      byte currentConstructionFlag;

      if (frame != null)
      {
         ioInProgress = frame.isIoInProgress();
         currentTool = frame.currentTool();
         currentEditFlag = frame.getEditFlag();
         currentConstructionFlag = frame.getConstructionFlag();
      }
      else
      {
         ioInProgress = false;
         currentTool = -1;
         currentEditFlag = EDIT_FLAG_NONE;
         currentConstructionFlag = FlowframTkAction.CONSTRUCTION_FLAG_NONE;
      }

      JDRSelection selection = null;

      if (useSelectionFlags && frame != null)
      {
         selection = frame.getSelectionFlags();
      }

      for (FlowframTkAction action : toolActionList)
      {
         action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);
      }
   }

   public void updateSelectActionButtons()
   {
      JDRFrame frame = getSelectedFrame();

      boolean ioInProgress;
      int currentTool;
      byte currentEditFlag;
      byte currentConstructionFlag;

      if (frame != null)
      {
         ioInProgress = frame.isIoInProgress();
         currentTool = frame.currentTool();
         currentEditFlag = frame.getEditFlag();
         currentConstructionFlag = frame.getConstructionFlag();
      }
      else
      {
         ioInProgress = false;
         currentTool = -1;
         currentEditFlag = EDIT_FLAG_NONE;
         currentConstructionFlag = FlowframTkAction.CONSTRUCTION_FLAG_NONE;
      }

      JDRSelection selection = null;

      if (frame != null)
      {
         selection = frame.getSelectionFlags();
      }

      for (FlowframTkAction action : selectActionList)
      {
         action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);
      }
   }

   public void updateGeneralActionButtons(boolean useSelectionFlags)
   {
      JDRFrame frame = getSelectedFrame();

      boolean ioInProgress;
      int currentTool;
      byte currentEditFlag;
      byte currentConstructionFlag;

      if (frame != null)
      {
         ioInProgress = frame.isIoInProgress();
         currentTool = frame.currentTool();
         currentEditFlag = frame.getEditFlag();
         currentConstructionFlag = frame.getConstructionFlag();
      }
      else
      {
         ioInProgress = false;
         currentTool = -1;
         currentEditFlag = EDIT_FLAG_NONE;
         currentConstructionFlag = FlowframTkAction.CONSTRUCTION_FLAG_NONE;
      }

      JDRSelection selection = null;

      if (useSelectionFlags && frame != null)
      {
         selection = frame.getSelectionFlags();
      }

      for (FlowframTkAction action : generalActionList)
      {
         action.updateEnabled(ioInProgress, currentTool, currentEditFlag,
            currentConstructionFlag, selection);
      }
   }

   public JDRFrame getSelectedFrame()
   {
      if (theDesktop==null) return null;
      return (JDRFrame)theDesktop.getSelectedFrame();
   }

   // returns currently selected frame or first frame in the list
   public JDRFrame getCurrentFrame()
   {
      if (theDesktop==null) return null;
      JDRFrame frame = getSelectedFrame();

      if (frame == null)
      {
         JInternalFrame[] f = theDesktop.getAllFrames();
         if (f.length > 0)
         {
            frame = (JDRFrame)f[0];
         }
      }

      return frame;
   }

   public void updateAllFrames()
   {
      JInternalFrame[] frames = theDesktop.getAllFrames();

      for (int i = 0; i < frames.length; i++)
      {
         ((JDRFrame)frames[i]).forceUpdate();
      }
   }

   public void tileFrames()
   {
      JInternalFrame[] frames = theDesktop.getAllFrames();

      Dimension dim = theDesktop.getSize();

      int n = frames.length;

      if (n == 0) return;

      if (n == 1)
      {
         try
         {
            frames[0].setMaximum(true);
            frames[0].setIcon(false);
         }
         catch (PropertyVetoException ignore)
         {
         }

         return;
      }

      int cols = (int)Math.ceil(Math.sqrt(n));

      int rows = (int)Math.ceil((double)n/(double)cols);

      dim.width  /= cols;
      dim.height /= rows;

      for (int i = 0, j=0, x=0, y=0; i < n; i++)
      {
         try
         {
            frames[i].setMaximum(false);
            frames[i].setIcon(false);
         }
         catch (PropertyVetoException ignore)
         {
         }
         frames[i].reshape(x, y, dim.width, dim.height);

         y += dim.height;
         j++;
         if (j >= rows)
         {
            y = 0;
            x += dim.width;
            j=0;
         }
      }
   }

   public void tileFramesHorizontally()
   {
      JInternalFrame[] frames = theDesktop.getAllFrames();

      Dimension dim = theDesktop.getSize();

      int n = frames.length;

      if (n == 0) return;

      if (n == 1)
      {
         try
         {
            frames[0].setMaximum(true);
            frames[0].setIcon(false);
         }
         catch (PropertyVetoException ignore)
         {
         }

         return;
      }

      dim.height = (int)Math.ceil((double)dim.height/(double)n);

      for (int i = 0, x=0, y=0; i < n; i++)
      {
         try
         {
            frames[i].setMaximum(false);
            frames[i].setIcon(false);
         }
         catch (PropertyVetoException ignore)
         {
         }
         frames[i].reshape(x, y, dim.width, dim.height);

         y += dim.height;
      }
   }

   public void tileFramesVertically()
   {
      JInternalFrame[] frames = theDesktop.getAllFrames();

      Dimension dim = theDesktop.getSize();

      int n = frames.length;

      if (n == 0) return;

      if (n == 1)
      {
         try
         {
            frames[0].setMaximum(true);
            frames[0].setIcon(false);
         }
         catch (PropertyVetoException ignore)
         {
         }

         return;
      }

      dim.width = (int)Math.ceil((double)dim.width/(double)n);

      for (int i = 0, x=0, y=0; i < n; i++)
      {
         try
         {
            frames[i].setMaximum(false);
            frames[i].setIcon(false);
         }
         catch (PropertyVetoException ignore)
         {
         }
         frames[i].reshape(x, y, dim.width, dim.height);

         x += dim.width;
      }
   }

   public void minimizeAll()
   {
      JInternalFrame[] frames = theDesktop.getAllFrames();

      for (int i = 0, n=frames.length; i < n; i++)
      {
         try
         {
            frames[i].setIcon(true);
         }
         catch (PropertyVetoException ignore)
         {
         }
      }
   }

   public void maximizeAll()
   {
      JInternalFrame[] frames = theDesktop.getAllFrames();

      for (int i = 0, n=frames.length; i < n; i++)
      {
         try
         {
            frames[i].setMaximum(true);
            if (frames[i].isIcon()) frames[i].setIcon(false);
         }
         catch (PropertyVetoException ignore)
         {
         }
      }
   }

   public CanvasGraphics getCurrentCanvasGraphics()
   {
      JDRFrame currentFrame = (JDRFrame)theDesktop.getSelectedFrame();

      CanvasGraphics cg;

      if (currentFrame == null)
      {
         cg = getSettings().getCanvasGraphics();
      }
      else
      {
         cg = currentFrame.getCanvasGraphics();

         if (cg == null)
         {
            cg = getSettings().getCanvasGraphics();
         }
         else
         {
            cg = (CanvasGraphics)cg.clone();
         }
      }

      try
      {
         cg.setDisplayGrid(showGridButtonItem.isSelected());
         cg.setGridLock(lockGridButtonItem.isSelected());
         cg.setShowRulers(showRulersItem.isSelected());
         cg.setPaper(getCurrentPaper());
         getSettings().setTool(getCurrentTool());
      }
      catch (JdrIllegalArgumentException e)
      {
         getResources().internalError(this, e);
      }

      return cg;
   }

   public synchronized JDRFrame addFrame()
   {
      return addFrame(getCurrentCanvasGraphics());
   }

   public synchronized JDRFrame addFrame(File file)
   {
      return addFrame(getCurrentCanvasGraphics(), file);
   }

   public synchronized JDRFrame addFrame(CanvasGraphics cg)
   {
      return addFrame(cg, null);
   }

   public synchronized JDRFrame addFrame(CanvasGraphics cg, File file)
   {
      JDRFrame frame = new JDRFrame(file, (CanvasGraphics)cg.clone(), 
         windowM, windowButtonGroup, this);

      frame.addInternalFrameListener(this);

      // attach internal frame to desktop and show it
      theDesktop.add(frame);

      try
      {
         frame.setMaximum(true);
      }
      catch (PropertyVetoException ignore)
      {
      }

      frame.setVisible(true);
      updateWindowMenu();
      frame.updateTextAreaBounds();

      statusBar.enableStatus();

      return frame;
   }

   public void removeFrame(JInternalFrame frame)
   {
      theDesktop.remove(frame);
      frame.dispose();

      JInternalFrame[] frames = theDesktop.getAllFrames();
      if (frames.length > 0)
      {
         try
         {
            frames[0].setSelected(true);
         }
         catch (PropertyVetoException ignore)
         {
         }
      }
      else
      {
         theDesktop.setSelectedFrame(null);
         updateStatus(0);
      }
   }

   public boolean isJdrAjrExtension(File file)
   {
      return !file.isDirectory() && jdrAjrFileFilter.accept(file);
   }

   public boolean isJdrExtension(File file)
   {
      return !file.isDirectory() && jdrFileFilter.accept(file);
   }

   public boolean isAjrExtension(File file)
   {
      return !file.isDirectory() && ajrFileFilter.accept(file);
   }

   public boolean isBitmapExtension(File file)
   {
      return !file.isDirectory() && bitmapFileFilter.accept(file);
   }

   public void dragEnter(DropTargetDragEvent dtde)
   {
   }

   public void dragExit(DropTargetEvent dte)
   {
   }

   public void dragOver(DropTargetDragEvent dtde)
   {
   }

   public void drop(DropTargetDropEvent dtde)
   {
      Transferable transferable = dtde.getTransferable();

      if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
      {
         return;
      }

      dtde.acceptDrop(DnDConstants.ACTION_COPY);

      try
      {
         ArrayList list = (ArrayList)transferable.getTransferData(
            DataFlavor.javaFileListFlavor);

         int n = 0;

         for (Object object : list)
         {
            File file = (File)object;

            if (isJdrAjrExtension(file))
            {
               addFrame(getDefaultCanvasGraphics(), file);
               n++;
            }
            else if (isBitmapExtension(file))
            {
               JDRFrame frame = addFrame(getDefaultCanvasGraphics());
               frame.setTool(JDRConstants.ACTION_SELECT);
               frame.insertBitmap(file);
               n++;
            }
            else
            {
               getMessageSystem().warning(getResources().getMessage(
                  "warning.unknown_drop", file.toString()));
               getMessageSystem().displayMessages();
            }
         }

         if (n > 0)
         {
            tileFrames();
         }

         dtde.dropComplete(true);
      }
      catch (UnsupportedFlavorException e)
      {
         getResources().debugMessage(e);
         dtde.rejectDrop();
      }
      catch (IOException e)
      {
         getResources().error(this, e);
         dtde.rejectDrop();
      }
   }

   public void dropActionChanged(DropTargetDragEvent dtde)
   {
   }

   public void openImage()
   {
      JDRFrame currentFrame = getSelectedFrame();

      int result = openjdrFC.showOpenDialog(this);

      if (result == JFileChooser.APPROVE_OPTION)
      {
         if (currentFrame == null)
         {
            addFrame((CanvasGraphics)getSettings().getCanvasGraphics().clone(),
               openjdrFC.getSelectedFile());
         }
         else if (currentFrame.isNewImage())
         {
            if (currentFrame.isIcon())
            {
               currentFrame.selectThisFrame();
            }

            currentFrame.load(openjdrFC.getSelectedFile());
         }
         else
         {
            addFrame((CanvasGraphics)getSettings().getCanvasGraphics().clone(),
                     openjdrFC.getSelectedFile());
         }
      }
   }

   public void importImage()
   {
      int result = importFC.showOpenDialog(this);

      if (result == JFileChooser.APPROVE_OPTION)
      {
         JDRFrame currentFrame = getSelectedFrame();

         if (currentFrame == null)
         {
            currentFrame = addFrame((CanvasGraphics)getSettings().getCanvasGraphics().clone());
         }
         else if (currentFrame.isNewImage())
         {
            if (currentFrame.isIcon())
            {
               currentFrame.selectThisFrame();
            }
         }
         else
         {
            currentFrame = addFrame((CanvasGraphics)getSettings().getCanvasGraphics().clone());
         }

         File file = new File(
            importFC.getSelectedFile().getAbsolutePath());

         (new LoadEps(currentFrame, file)).execute();
      }
   }

   public boolean saveImage()
   {
      JDRFrame frame = getCurrentFrame();
      if (frame == null) return false;

      return saveImage(frame);
   }

   public boolean saveImage(JDRFrame frame)
   {
      return saveImage(frame, false);
   }

   public boolean saveImage(JDRFrame frame, boolean exitAfter)
   {
      if (frame.isIcon())
      {
         frame.selectThisFrame();
      }

      if (frame.hasFileName())
      {
         String filenameLC = frame.getFilename().toLowerCase();

         if (filenameLC.endsWith(".ajr"))
         {
            frame.saveAJR(exitAfter);
         }
         else
         {
            frame.save(exitAfter);
         }
      }
      else
      {
         return promptAndSave(frame, exitAfter);
      }

      return true;
   }

   public boolean promptAndSave(JDRFrame frame)
   {
      return promptAndSave(frame, false);
   }

   public boolean promptAndSave(JDRFrame frame, boolean exitAfter)
   {
      savejdrFC.setSelectedFile(frame.hasFileName() ? frame.getFile() :
        new File(frame.getFilename()));

      int result = savejdrFC.showSaveDialog(frame);

      if (result == JFileChooser.APPROVE_OPTION)
      { 
         saveAs(frame, exitAfter);

         return true;
      }

      return false;
   }

   public void saveAs(JDRFrame frame, boolean exitAfter)
   {
      File file = savejdrFC.getSelectedFile();

      JDRFileFilterInterface filter 
         = (JDRFileFilterInterface)savejdrFC.getFileFilter();

      String filenameLC = file.getName().toLowerCase();

      int index = filenameLC.lastIndexOf('.');

      String extension;

      if (index == -1)
      {
         extension = "";
      }
      else
      {
         extension = filenameLC.substring(index);
      }

      // does the file already exist?

      if (file.exists())
      {
         int selection = getResources().confirm(frame,
            new String[]
            {file.toString(),
            getResources().getString("warning.file_exists")},
            getResources().getString("warning.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

            if (selection != JOptionPane.YES_OPTION) return;
      }

      float version = filter.getVersion();

      if (extension.equals(".jdr"))
      {
         frame.save(file, version, exitAfter);
      }
      else if (extension.equals(".ajr"))
      {
         frame.saveAJR(file, version, exitAfter);
      }
      else if (filter instanceof JdrFileFilter
             || filter instanceof JdrAjrFileFilter)
      {
         frame.save(file, version, exitAfter);
      }
      else if (filter instanceof AjrFileFilter)
      {
         frame.save(file, version, exitAfter);
      }
      else
      {
         getResources().internalError("Unknown file filter");
      }
   }

   public void saveAs()
   {
      JDRFrame frame = getCurrentFrame();
      if (frame == null) return;

      if (frame.isIcon())
      {
         frame.selectThisFrame();
      }

      savejdrFC.setSelectedFile(frame.hasFileName() ? frame.getFile() :
        new File(frame.getFilename()));

      int result = savejdrFC.showSaveDialog(frame);

      if (result == JFileChooser.APPROVE_OPTION)
      {
         saveAs(frame, false);
      }
   }

   public void exportImage()
   {
      JDRFrame frame = getCurrentFrame();
      if (frame == null) return;

      if (frame.isIcon())
      {
         frame.selectThisFrame();
      }

      File file = exportFC.getSelectedFile();

      if (file == null && !frame.getFilename().isEmpty())
      {
         file = new File(frame.getFilename());

         String name = file.getName();

         int i = name.lastIndexOf(".jdr");

         if (i == -1) i = name.lastIndexOf(".ajr");

         if (i != -1)
         {
            name = name.substring(0, i);
         }

         File currentDir = exportFC.getCurrentDirectory();

         exportFC.setSelectedFile(new File(currentDir, name));
      }

      int result = exportFC.showSaveDialog(frame);

      if (result == JFileChooser.APPROVE_OPTION)
      {
         file = exportFC.getSelectedFile();
         String filename = file.getAbsolutePath();

         FileFilter filter = exportFC.getFileFilter();

         // does the file already exist?
         if (file.exists())
         {
            int selection = getResources().confirm(frame,
               new String[]
               {filename,
               getResources().getString("warning.file_exists")},
               getResources().getString("warning.title"),
               JOptionPane.YES_NO_OPTION,
               JOptionPane.WARNING_MESSAGE);

               if (selection != JOptionPane.YES_OPTION) return;
         }

         if (filter == pgfFileFilter)
         {         
            frame.savePGF(file);
         }
         else if (filter == pgfDocFileFilter)
         {         
            frame.savePGFDoc(file, false);
         }
         else if (filter == pgfEncapDocFileFilter)
         {         
            frame.savePGFDoc(file, true);
         }
         else if (filter == pngFileFilter)
         {         
            if (exportPngDialog.display())
            {
               frame.savePNG(file);
            }
         }
         else if (filter == epsFileFilter)
         {         
            String latexApp = getLaTeXApp();
            String dvipsApp = getDvipsApp();

            if ((latexApp == null || latexApp.isEmpty())
             || (dvipsApp == null || dvipsApp.isEmpty()))
            {
               if (!exportToEpsSettings.display())
               {
                  return;
               }

               latexApp = getLaTeXApp();
               dvipsApp = getDvipsApp();
            }

            frame.saveEPS(file, latexApp, dvipsApp);
         }
         else if (filter == pdfFileFilter)
         {         
            String pdflatexApp = getPdfLaTeXApp();

            if (pdflatexApp == null || pdflatexApp.isEmpty())
            {
               if (!exportToPdfSettings.display())
               {
                  return;
               }

               pdflatexApp = getPdfLaTeXApp();
            }

            frame.savePdf(file, pdflatexApp);
         }
         else if (filter == svgFileFilter)
         {
            String latexApp = getLaTeXApp();
            String dvisvgmApp = getDvisvgmApp();
            String libGs = getLibgs();

            if ((latexApp == null || latexApp.isEmpty())
             || (dvisvgmApp == null || dvisvgmApp.isEmpty()))
            {
               if (!exportToSvgSettings.display())
               {
                  return;
               }

               latexApp = getLaTeXApp();
               dvisvgmApp = getDvisvgmApp();
               libGs = getLibgs();
            }

            frame.saveSVG(file, latexApp, dvisvgmApp, libGs);
         }
         else
         {
            frame.saveFlowFrame(file);
         }
      }
   }

   public void close()
   {
      JDRFrame frame = (JDRFrame)theDesktop.getSelectedFrame();

      if (frame.canDiscard())
      {
         frame.discard();
         frame = null;
      }
   }

   public DiscardDialogBox getDiscardDialogBox()
   {
      return discardDB;
   }

   public void updateWindowMenu()
   {
      JInternalFrame[] frames = theDesktop.getAllFrames();

/*
 * The GTKLookAndFeel has a bug in getAllFrames
 * (http://bugs.java.com/view_bug.do?bug_id=8057184) that duplicates
 * each frame returned, so this will throw the numbering out.
 */

      try
      {
         LookAndFeel lookandfeel = UIManager.getLookAndFeel();

         if (lookandfeel != null && lookandfeel.getClass().getName().equals(
               "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"))
         {
            Vector<JDRFrame> processed = new Vector<JDRFrame>(frames.length);
            int idx = 0;

            for (int i = 0; i < frames.length; i++)
            {
               JDRFrame f = (JDRFrame)frames[i];

               if (processed.contains(f)) continue;

               idx++;
               String id = new String((idx < 10 ? " ":"")+idx);
               f.menuItem.setText(id+" "+f.getTitle());
               f.menuItem.setMnemonic(id.charAt(1));
               processed.add(f);
            }

            updateActionButtons(false);
            return;
         }
      }
      catch (Throwable e)
      {
         getResources().debugMessage(e);
      }

      for (int i = 0; i < frames.length; i++)
      {
         JDRFrame f = (JDRFrame)frames[i];

         String id = new String((i < 9 ? " ":"")+(i+1));
         f.menuItem.setText(id+" "+f.getTitle());
         f.menuItem.setMnemonic(id.charAt(1));
      }

      updateActionButtons(false);
   }

   public void quit()
   {
      if (!closeVectorizeBitmap(null))
      {
         return;
      }

      try
      {
         invoker.saveResources(recentFiles);
      }
      catch (Exception e)
      {
         getResources().error(this, e);
      }

      JInternalFrame[] frames = theDesktop.getAllFrames();

      Vector<JDRFrame> unsaved = new Vector<JDRFrame>(frames.length);

      for (int i = 0, n=frames.length; i < n; i++)
      {
         JDRFrame frame = (JDRFrame)frames[i];

         if (frame.isIoInProgress())
         {
            if (getResources().confirm(this,
             getResources().getMessage(
               "message.io_in_progress.confirm_quit",
               frame.getFilename()))
               != JOptionPane.YES_OPTION)
            {
               return;
            }
         }
         else if (frame.isModified())
         {
            unsaved.add(frame);
         }
      }

      if (unsaved.size() > 0)
      {
         if (discardDB.display(unsaved, true) == DiscardDialogBox.CANCEL)
         {
            return;
         }
      }
      else
      {
         System.exit(0);
      }
   }

   public void addRecentFile(File file)
   {
      recentFiles.remove(file);
      recentFiles.add(0, file);
   }

   public void setRecentFiles()
   {
      if (recentFiles == null) return;

      if (recentM.getMenuComponentCount() > 0)
      {
         recentM.removeAll();
      }

      for (int i = 0, n = recentFiles.size(); i < n && i < 10; i++)
      {
         File file = recentFiles.get(i);
         String num = new String(i==9?"10":" "+(i+1));
         JMenuItem item = new JMenuItem(num+" "+file);
         item.setMnemonic(num.charAt(1));
         item.addActionListener(new LoadAction(this, file, recentFiles));
         recentM.add(item);
      }
   }

   private void loadRecentFiles()
   {
      try
      {
         recentFiles = invoker.loadRecentFiles();
      }
      catch (IOException e)
      {
         debugMessage(e);
         recentFiles = new Vector<File>();
      }
      catch (Exception e)
      {
         getResources().error(null,e);
      }
   }

   public JMenuItem addHelpItem(JMenu helpM)
   {
      return getResources().addHelpItem(helpM, invoker.getName());
   }

   public void initializeHelp(JFrame parent)
   {
      getResources().initialiseHelp(parent, invoker.getName().toLowerCase());
   }

   public void enableHelpOnButton(AbstractButton comp, String id)
   {
      getResources().enableHelpOnButton(comp, id);
   }

   public void writeLog()
   {
      File logFile = invoker.getLogFile();

      if (logFile == null)
      {
         getResources().error(this, "no log file");
         return;
      }

      try
      {
         PrintWriter out = new PrintWriter(new FileWriter(logFile));

         out.println("flowframtk v"+invoker.getVersion());

         DateFormat dateFormat = DateFormat.getDateTimeInstance();

         out.println("Log file created "
            +dateFormat.format(new Date()));

         out.println("main window:");
         out.println(this);

         out.println();
         out.println("desktop:");
         out.println(theDesktop);

         JInternalFrame[] allFrames = theDesktop.getAllFrames();

         out.println();
         out.println(allFrames.length+" internal frame(s)");
         out.println();

         for (int i = 0; i < allFrames.length; i++)
         {
            out.println("Frame "+i);

            JDRFrame f = (JDRFrame)allFrames[i];

            f.printInfo(out);

            out.println();
         }

         out.println(linePaintChooserBox.info());
         out.println(fillPaintChooserBox.info());
         out.println(lineStyleChooserBox.info());
         out.println(lineWidthChooserBox.info());
         out.println(dashPatternChooserBox.info());
         out.println(joinStyleChooserBox.info());
         out.println(startArrowChooserBox.info());
         out.println(midArrowChooserBox.info());
         out.println(endArrowChooserBox.info());
         out.println(allMarkersChooserBox.info());
         out.println(editTextBox.info());
         out.println(textPaintChooserBox.info());
         out.println(textStyleChooserBox.info());
         out.println(fontFamilyChooserBox.info());
         out.println(fontSizeChooserBox.info());
         out.println(fontShapeChooserBox.info());
         out.println(fontSeriesChooserBox.info());
         out.println(fontAnchorChooserBox.info());
         out.println(fontHAnchorChooserBox.info());
         out.println(fontVAnchorChooserBox.info());
         out.println(styleChooserBox.info());
         out.println(configDialog.info());
         out.println(configTeXDialog.info());
         out.println(rotateDB.info());
         out.println(scaleDB.info());
         out.println(shearDB.info());
         out.println(flfSelector.info());
         out.println(gotoDialog.info());

         out.close();

         JOptionPane.showMessageDialog(null,
         "Information written to '"+logFile.getAbsolutePath()+"'",
         "Write Log",
         JOptionPane.INFORMATION_MESSAGE);
      }
      catch (IOException e)
      {
         getResources().error(this, e);
      }
   }

   public void objectInfo()
   {
      JDRFrame frame = (JDRFrame)theDesktop.getSelectedFrame();

      String message;

      if (frame == null)
      {
         message = "Can't access any data";
      }
      else
      {
         message = frame.getSelectedInfo();
      }

      JTextArea textArea = new JTextArea(message);
      textArea.setEditable(false);

      JScrollPane sp = new JScrollPane(textArea);

      sp.setPreferredSize(new Dimension(500,400));

      JOptionPane.showMessageDialog(null, sp, "Object Info",
         JOptionPane.PLAIN_MESSAGE);
   }

   public void dumpAll()
   {
      String usersettings = invoker.getConfigDirName();

      if (usersettings == null)
      {
         getResources().error(null, 
            "Can't dump all images - no configuration directory");
         return;
      }

      JInternalFrame[] allFrames = theDesktop.getAllFrames();

      if (allFrames.length == 0)
      {
         return;
      }

      Calendar cal = new GregorianCalendar();

      int month = cal.get(Calendar.MONTH)+1;
      int day = cal.get(Calendar.DAY_OF_MONTH);
      int hour = cal.get(Calendar.HOUR_OF_DAY);
      int minute = cal.get(Calendar.MINUTE);
      int second = cal.get(Calendar.SECOND);

      String dir = 
         cal.get(Calendar.YEAR)
         + (month < 10 ? ("0"+month) : ""+month)
         + (day < 10 ? ("0"+day) : ""+day)
         + (hour < 10 ? ("0"+hour) : ""+hour)
         + (minute < 10 ? ("0"+minute) : ""+minute)
         + (second < 10 ? ("0"+second) : ""+second);

      File parent = new File(usersettings, dir);

      if (!parent.mkdir())
      {
         getResources().error(null, "Unable to create directory '"
          +parent.getAbsolutePath()+"'");

         return;
      }

      for (int i = 0; i < allFrames.length; i++)
      {
         JDRFrame frame = (JDRFrame)allFrames[i];

         File file = new File(parent, "image"+(i+1)+".jdr");

         frame.save(file, JDRAJR.CURRENT_VERSION);
      }
   }

   public void showMessageFrame()
   {
      getMessageSystem().displayMessages();
   }

   public void showMessageFrame(String text)
   {
      getMessageSystem().displayMessages();
      getMessageSystem().getPublisher().publishMessages(
        MessageInfo.createMessage(text));
      setStatusInfo(text);
   }

   public void addMessage(String text)
   {
      getMessageSystem().getPublisher().publishMessages(
        MessageInfo.createMessage(text));
   }

   public void addMessage(Exception e)
   {
      getMessageSystem().getPublisher().publishMessages(
        MessageInfo.createMessage(e.getMessage()));
   }

   public void hideMessageFrame()
   {
      if (!getMessageSystem().warningFlagged())
      {
         getMessageSystem().hideMessages();
      }
   }

   public JDRGuiMessage getMessageSystem()
   {
      return (JDRGuiMessage)invoker.getSettings().getMessageSystem();
   }

   public FlowframTkInvoker invoker;

   private Vector<File> recentFiles=null;

   // panels

   private StatusBar statusBar;

   // dialog boxes

   private AboutDialog aboutDialog;
   private LicenceDialog licenceDialog;
   private LinePaintSelector linePaintChooserBox;
   private FillPaintSelector fillPaintChooserBox;
   private LineStyleSelector lineStyleChooserBox;
   private LineWidthSelector lineWidthChooserBox;
   private DashPatternSelector dashPatternChooserBox;
   private JoinStyleSelector joinStyleChooserBox;
   private ArrowStyleSelector startArrowChooserBox;
   private ArrowStyleSelector midArrowChooserBox;
   private ArrowStyleSelector endArrowChooserBox;
   private ArrowStyleSelector allMarkersChooserBox;
   private TextSelector editTextBox;
   private TextPaintSelector textPaintChooserBox;
   private FontSelector textStyleChooserBox;
   private FontFamilySelector fontFamilyChooserBox;
   private FontSizeSelector fontSizeChooserBox;
   private FontShapeSelector fontShapeChooserBox;
   private FontSeriesSelector fontSeriesChooserBox;
   private FontAnchorSelector fontAnchorChooserBox;
   private FontHAnchorSelector fontHAnchorChooserBox;
   private FontVAnchorSelector fontVAnchorChooserBox;
   private BitmapProperties bitmapPropChooserBox;
   private StyleSelector styleChooserBox;
   private GridSettings gridSettingsChooserBox;
   private ZoomSettings zoomSettingsChooserBox;
   private ConfigSettingsDialog configDialog;
   private ConfigTeXSettingsDialog configTeXDialog;
   private ConfigUISettingsDialog configUIDialog;
   private RotateDialogBox rotateDB;
   private ScaleDialogBox scaleDB;
   private ShearDialogBox shearDB;
   private FLFSelector flfSelector;
   private FLFSetTypeblock setTypeblockSelector;
   private DiscardDialogBox discardDB;
   private GoToDialogBox gotoDialog;
   private MoveByDialogBox moveByDialog;
   private DisplayPageDialog displayPageDialog;
   private DescriptionDialogBox imageDescriptionDialog;
   private DescriptionDialogBox objectDescriptionDialog;
   private FindByDescriptionDialogBox findByDescDialog;
   private PaperDialogBox paperDialog;
   private SetTransformDialogBox textMatrixDialog;
   private PatternDialogBox patternBox;
   private VectorizeBitmapDialog vectorizeBitmapDialog;
   private ConvertToPolygonDialog convertToPolygonDialog;
   private FadeDialogBox fadeDialog;
   private TeXEditorDialog texEditorDialog;
   private CharacterSelector characterSelector;
   private ExportPngDialog exportPngDialog;
   private SegmentInfoDialog segmentInfoDialog;

   // file choosers
   private JFileChooser savejdrFC, openjdrFC,
      exportFC, bitmapFC, texFC, importFC;

   // thumbnail image panel
   private ImagePreview imagePreviewPanel;

   private JDRAppSelector appSelector;
   private ExportToEpsSettings exportToEpsSettings;
   private ExportToSvgSettings exportToSvgSettings;
   private ExportToPdfSettings exportToPdfSettings;

   // file filters

   private JdrAjrFileFilter jdrAjrFileFilter;
   private JdrFileFilter jdrFileFilter;
   private JdrFileFilter[] oldJdrFileFilter;
   private AjrFileFilter ajrFileFilter;
   private AjrFileFilter[] oldAjrFileFilter;
   private TeXFileFilter pgfFileFilter;
   private TeXFileFilter pgfDocFileFilter;
   private TeXFileFilter pgfEncapDocFileFilter;
   private StyFileFilter styFileFilter;
   private ClsFileFilter clsFileFilter;
   private PngFileFilter pngFileFilter;
   private EpsFileFilter epsFileFilter;
   private TeXFileFilter texFileFilter;
   private SvgFileFilter svgFileFilter;
   private PdfFileFilter pdfFileFilter;
   private BitmapFileFilter bitmapFileFilter;

   // undo/redo stuff
   private UndoManager undoManager;
   public UndoableEditSupport undoSupport;
   private JMenuItem undoItem, redoItem;

   // button and menu items

   private JDRButtonItem newButtonItem, openButtonItem, saveButtonItem,
      selectAllButtonItem, cutButtonItem, copyButtonItem, pasteButtonItem,
      moveToFrontButtonItem, moveToBackButtonItem, rotateButtonItem,
      scaleButtonItem, shearButtonItem, groupButtonItem,
      ungroupButtonItem, gapButtonItem, abandonButtonItem,
      finishButtonItem, setPatternButtonItem, convertToTextPathButtonItem;

   private JDRToggleButtonItem editPathButtonItem, showGridButtonItem,
      lockGridButtonItem, distortButtonItem;

   private JDRToolButtonItem selectButtonItem, openLineButtonItem,
      closedLineButtonItem, openCurveButtonItem, closedCurveButtonItem,
      rectangleButtonItem, ellipseButtonItem, textButtonItem;

   // menus
   private JMenuItem saveAsItem,
                     exportItem, importItem, printItem, 
                     pageDialogItem,
                     closeItem, quitItem,
                     deselectAllItem, linePaintItem, fillPaintItem,
                     lineStyleItem, lineWidthItem, dashItem,
                     startArrowItem, midArrowItem, endArrowItem,
                     allMarkersItem, editTextItem, textPaintItem,
                     textStyleItem, fontFamilyItem, fontSizeItem,
                     fontShapeItem, fontSeriesItem,
                     textMatrixItem, fontAnchorItem,
                     fontVAnchorItem, fontHAnchorItem,
                     reverseItem,
                     convertToPathItem, mergePathsItem,
                     convertToFullPathItem,
                     convertToPolygonItem,
                     removePatternItem,
                     editPatternItem,
                     separateItem,
                     xorPathsItem, intersectPathsItem,
                     subtractPathsItem, pathUnionItem, 
                     leftAlignItem, centreAlignItem,
                     rightAlignItem, topAlignItem, middleAlignItem,
                     bottomAlignItem, splitTextItem, 
                     insertBitmapItem,
                     refreshItem, bitmapPropItem, parshapeItem,
                     shapeparItem, clearAllItem, setFrameItem,
                     setTypeblockItem, stylesItem, joinItem,
                     gridSettingsItem,
                     configSettingsItem, licenceItem, aboutItem,
                     tileItem, verticallyItem, horizontallyItem,
                     minimizeItem, maximizeItem, gotoItem, nextItem,
                     addNextItem, skipItem, moveByItem,
                     findSelectedItem, displayPageItem,
                     imageDescriptionItem, objectDescriptionItem,
                     findByDescriptionItem, addByDescriptionItem,
                     zoomWidthItem, zoomHeightItem, zoomPageItem,
                     reduceToGreyItem, removeAlphaItem, convertToCMYKItem,
                     convertToRGBItem, convertToHSBItem, vectorizeItem, fadeItem,
                     moveUpItem, moveDownItem;

   private JRadioButtonMenuItem 
      zoomSettingsItem, zoom25Item,
      zoom50Item, zoom75Item, zoom100Item, zoom200Item, zoom400Item,
      zoom800Item, portraitItem, landscapeItem, paperA5Item,
      paperA4Item, paperA3Item, paperA2Item, paperA1Item, paperA0Item,
      paperLegalItem, paperLetterItem, paperExecutiveItem,
      paperOtherItem, capButtItem, capRoundItem, capSquareItem,
      windingEvenOddItem, windingNonZeroItem;

   private JCheckBoxMenuItem showRulersItem,
      showStatusBarItem, 
      showPrinterMarginsItem;

   private JMenu fileM, editM, pathM, textM, transformM, justifyM,
                 toolsM, bitmapM, texM, settingsM, windowM, recentM,
                 gridM, zoomM, paperM, navigateM, fontStyleM,
                 lineStyleM, patternM, adjustColM;

   // Selection dependent actions
   private Vector<FlowframTkAction> selectActionList;

   // Tool dependent actions
   private Vector<FlowframTkAction> toolActionList;

   // Non-tool dependent actions
   private Vector<FlowframTkAction> generalActionList;

   private ToolButtonGroup toolButtonGroup;

   private SlidingToolBar hSlidingBar, vSlidingBar; 

   // Clipboard
   public final Clipboard clipboard = getToolkit().getSystemClipboard();


   // desktop
   private JDesktopPane theDesktop;
   private ButtonGroup windowButtonGroup;
   private File currentFile = null;
   private boolean activateFrameUndoRedo=false;
   private boolean iconifyFrameUndoRedo=false;

   // available fonts
   private String[] availableFontFamilies;

   // printer settings
   private HashPrintRequestAttributeSet printRequestAttributeSet;

   public JDRGuiMessage messageSystem;

   public static final int MAX_RERUN = 6;

}

