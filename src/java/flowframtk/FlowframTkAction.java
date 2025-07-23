// File          : FlowframTkAction.java
// Description   : Actions for FlowframTk
// Date          : 2014-04-26
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2014-2025 Nicola L.C. Talbot

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

import java.util.Arrays;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;

public class FlowframTkAction extends AbstractAction implements JDRConstants
{
   public FlowframTkAction(FlowframTk application,
      String actionCommand,
      FlowframTkActionListener listener)
   {
      this(application, actionCommand, listener, 
           TOOL_FLAG_ANY, EDIT_FLAG_ANY,
           CONSTRUCTION_FLAG_ANY,
           SELECT_FLAG_ANY, SELECTION_IGNORE_COUNT);
   }

   public FlowframTkAction(FlowframTk application,
      String actionCommand,
      FlowframTkActionListener listener,
      String keyStrokeId)
   {
      this(application, actionCommand, listener, keyStrokeId,
           TOOL_FLAG_ANY, EDIT_FLAG_ANY,
           CONSTRUCTION_FLAG_ANY,
           SELECT_FLAG_ANY, SELECTION_IGNORE_COUNT);
   }

   public FlowframTkAction(FlowframTk application,
     String actionCommand,
     FlowframTkActionListener listener,
     int validToolsFlag,
     byte validEditFlag, 
     byte validConstructionFlag, 
     int validSelectionFlag,
     int[] validSelectionNumbers)
   {
      this(application, actionCommand, listener, null, validToolsFlag,
        validEditFlag, validConstructionFlag, validSelectionFlag,
        validSelectionNumbers);
   }

   public FlowframTkAction(FlowframTk application,
     String actionCommand,
     FlowframTkActionListener listener,
     String keyStrokeId,
     int validToolsFlag,
     byte validEditFlag, 
     byte validConstructionFlag, 
     int validSelectionFlag,
     int[] validSelectionNumbers)
   {
      super();
      this.application = application;
      this.listener = listener;
      this.validToolFlag = validToolsFlag;
      this.validEditFlag = validEditFlag;
      this.validConstructionFlag = validConstructionFlag;
      this.validSelectionFlag = validSelectionFlag;
      this.validSelectionNumbers = validSelectionNumbers;

      if (keyStrokeId != null)
      {
         this.accelerator = getResources().getAccelerator(keyStrokeId);
      }

      setActionCommand(actionCommand);
   }

   public void registerAction(JComponent comp, int condition)
   {
      if (accelerator != null)
      {
         comp.getInputMap(condition).put(accelerator, getActionCommand());
         comp.getActionMap().put(getActionCommand(), this);
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      if (listener == null || !isValid())
      {
         return;
      }

      listener.doAction(this, evt);

      if (requiresCanvas())
      {
         JDRFrame frame = getFrame();

         if (frame != null)
         {
            frame.requestFocusInWindow();
         }
      }
   }

   public JMenuItem createMenuItem(String menuId, String tooltipId)
   {
      JMenuItem item = new JMenuItem(
         getResources().getMessage(menuId),
         getResources().getCodePoint(menuId+".mnemonic"));

      if (tooltipId != null)
      {
         item.setToolTipText(getResources().getMessageIfExists(tooltipId));
      }

      KeyStroke keyStroke = getAccelerator();

      if (keyStroke == null)
      {
         keyStroke = getResources().getAccelerator(menuId);
         setAccelerator(keyStroke);
      }

      if (keyStroke != null)
      {
         item.setAccelerator(keyStroke);
      }

      item.addActionListener(this);
      item.setActionCommand(getActionCommand());

      setActionButton(item);

      return item;
   }

   public JMenu createMenu(String menuId, String tooltipId)
   {
      JMenu menu = getResources().createAppMenu(menuId);

      if (tooltipId != null)
      {
         menu.setToolTipText(getResources().getMessageIfExists(tooltipId));
      }

      KeyStroke keyStroke = getAccelerator();

      if (keyStroke == null)
      {
         keyStroke = getResources().getAccelerator(menuId);
         setAccelerator(keyStroke);
      }

      if (keyStroke != null)
      {
         menu.setAccelerator(keyStroke);
      }

      menu.addActionListener(this);
      menu.setActionCommand(getActionCommand());

      setActionButton(menu);

      return menu;
   }

   public JCheckBoxMenuItem createCheckBoxMenuItem(String menuId, 
      boolean selected, String tooltipId)
   {
      JCheckBoxMenuItem item = new JCheckBoxMenuItem(
         getResources().getMessage(menuId), selected);

      item.setMnemonic(getResources().getCodePoint(menuId+".mnemonic"));

      if (tooltipId != null)
      {
         item.setToolTipText(getResources().getMessageIfExists(tooltipId));
      }

      KeyStroke keyStroke = getAccelerator();

      if (keyStroke == null)
      {
         keyStroke = getResources().getAccelerator(menuId);
         setAccelerator(keyStroke);
      }

      if (keyStroke != null)
      {
         item.setAccelerator(keyStroke);
      }

      item.addActionListener(this);
      item.setActionCommand(getActionCommand());

      setActionButton(item);

      return item;
   }

   public JRadioButtonMenuItem createRadioButtonMenuItem(String menuId, 
      ButtonGroup bg, boolean selected, String tooltipId)
   {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem(
         getResources().getMessage(menuId), selected);

      item.setMnemonic(getResources().getCodePoint(menuId+".mnemonic"));
      bg.add(item);

      if (tooltipId != null)
      {
         item.setToolTipText(getResources().getMessageIfExists(tooltipId));
      }

      KeyStroke keyStroke = getAccelerator();

      if (keyStroke == null)
      {
         keyStroke = getResources().getAccelerator(menuId);
         setAccelerator(keyStroke);
      }

      if (keyStroke != null)
      {
         item.setAccelerator(keyStroke);
      }

      item.addActionListener(this);
      item.setActionCommand(getActionCommand());

      setActionButton(item);

      return item;
   }

   public JDRButtonItem createButtonItem(String name,
      String action, String keystrokeId, String tooltipId,
      JComponent comp, JMenu menu)
   {
      KeyStroke keyStroke = getAccelerator();

      if (keyStroke == null && keystrokeId != null)
      {
         keyStroke = getResources().getAccelerator(keystrokeId);
         setAccelerator(keyStroke);
      }

      JDRButtonItem button = new JDRButtonItem(getResources(), name, action,
         keyStroke, this, 
         tooltipId == null ? null : getResources().getMessageIfExists(tooltipId),
         comp, menu);

      setActionButton(button);
      button.setActionCommand(getActionCommand());

      return button;
   }

   public JDRButton createButton(String name, String keystrokeId, 
      String tooltipId)
   {
      KeyStroke keyStroke = getAccelerator();

      if (keyStroke == null && keystrokeId != null)
      {
         keyStroke = getResources().getAccelerator(keystrokeId);
         setAccelerator(keyStroke);
      }

      JDRButton button = getResources().createAppButton(
         name, this, keyStroke,
         tooltipId == null ? null : getResources().getMessageIfExists(tooltipId));

      setActionButton(button);
      button.setActionCommand(getActionCommand());

      return button;
   }

   public JDRToggleButton createToggleButton(String name, String keystrokeId, 
      String tooltipId)
   {
      KeyStroke keyStroke = getAccelerator();

      if (keyStroke == null && keystrokeId != null)
      {
         keyStroke = getResources().getAccelerator(keystrokeId);
         setAccelerator(keyStroke);
      }

      JDRToggleButton button = getResources().createToggleButton(
         getResources().getMessage("button."+name), name, this, keyStroke,
         tooltipId == null ? null : getResources().getMessageIfExists(tooltipId));

      setActionButton(button);
      button.setActionCommand(getActionCommand());

      return button;
   }

   public JDRToggleButtonItem createToggleButtonItem(String menuId, 
      String name, String keystrokeId, 
      String tooltipId, boolean selected, JComponent buttonParent,
      JMenu menu)
   {
      KeyStroke keyStroke = getAccelerator();

      if (keyStroke == null && keystrokeId != null)
      {
         keyStroke = getResources().getAccelerator(keystrokeId);
         setAccelerator(keyStroke);
      }

      JDRToggleButtonItem button = new JDRToggleButtonItem(getResources(),
          menuId, name, keyStroke, this,
          getResources().getMessageIfExists(tooltipId),
          selected, buttonParent, menu);

      setActionButton(button);

      return button;
   }

   public JDRToolButton createToolButton(String name, String keystrokeId, 
      String tooltipId, ButtonGroup bg, boolean selected)
   {
      KeyStroke keyStroke = getAccelerator();

      if (keyStroke == null && keystrokeId != null)
      {
         keyStroke = getResources().getAccelerator(keystrokeId);
         setAccelerator(keyStroke);
      }

      JDRToolButton button = getResources().createToolButton(
         getResources().getMessage("tools."+name), name,
         this, keyStroke, bg, selected,
         tooltipId == null ? null : getResources().getMessageIfExists(tooltipId));

      setActionButton(button);
      button.setActionCommand(getActionCommand());

      return button;
   }

   public JDRToolButtonItem createToolButtonItem(
      String parentId, String name, String keystrokeId, 
      String tooltipId, ToolButtonGroup buttonGroup, 
      JComponent comp, JMenu menu)
   {
      KeyStroke keyStroke = getAccelerator();

      if (keyStroke == null && keystrokeId != null)
      {
         keyStroke = getResources().getAccelerator(keystrokeId);
         setAccelerator(keyStroke);
      }

      JDRToolButtonItem button = new JDRToolButtonItem(
        getResources(), parentId, name, this, buttonGroup, comp, menu);

      setActionButton(button);
      button.setActionCommand(getActionCommand());

      return button;
   }

   public void setActionButton(AbstractButton button)
   {
      actionButton = button;
      actionButton.setEnabled(isValid());
   }

   public AbstractButton getActionButton()
   {
      return actionButton;
   }

   public void setEnabled(boolean enable)
   {
      super.setEnabled(enable);

      if (actionButton != null)
      {
         actionButton.setEnabled(enable);
      }
   }

   public void setSelected(boolean selected)
   {
      if (actionButton != null)
      {
         actionButton.setSelected(selected);
      }
   }

   public boolean isSelected()
   {
      return (actionButton == null ? false : actionButton.isSelected());
   }

   public String getActionCommand()
   {
      return actionCommand;
   }

   public void setActionCommand(String name)
   {
      actionCommand = name;
   }

   public FlowframTkActionListener getListener()
   {
      return listener;
   }

   public void setListener(FlowframTkActionListener listener)
   {
      this.listener = listener;
   }

   public KeyStroke getAccelerator()
   {
      return accelerator;
   }

   public void setAccelerator(KeyStroke keyStroke)
   {
      accelerator = keyStroke;
   }

   public void setValidToolFlag(int validToolsFlag)
   {
      this.validToolFlag = validToolsFlag;
   }

   public int getValidToolFlag()
   {
      return validToolFlag;
   }

   public void setValidEditFlag(byte validEditFlag)
   {
      this.validEditFlag = validEditFlag;
   }

   public byte getValidEditFlag()
   {
      return validEditFlag;
   }

   public void setValidConstructionFlag(byte validConstructionFlag)
   {
      this.validConstructionFlag = validConstructionFlag;
   }

   public byte getValidConstructionFlag()
   {
      return validConstructionFlag;
   }

   public void setValidSelection(int validSelectionFlag,
      int[] validSelectionNumbers)
   {
      this.validSelectionFlag = validSelectionFlag;
      this.validSelectionNumbers = validSelectionNumbers;
   }

   public int getValidSelectionFlag()
   {
      return validSelectionFlag;
   }

   public int[] getValidSelectionNumbers()
   {
      return validSelectionNumbers;
   }

   public FlowframTk getApplication()
   {
      return application;
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   public JDRFrame getFrame()
   {
      JDRCanvas canvas = getCanvas();

      return canvas == null ? application.getSelectedFrame()
          : canvas.getFrame();
   }

   public JDRCanvas getCanvas()
   {
      JDRFrame frame = application.getSelectedFrame();

      if (frame == null) return null;

      return frame.getCanvas();
   }

   public void updateEnabled(boolean ioInProgress, int currentTool,
     byte currentEditFlag, byte currentConstructionFlag,
     JDRSelection selection)
   {
      boolean enable = isValid(ioInProgress, currentTool,
        currentEditFlag, currentConstructionFlag,
        selection);

      setEnabled(enable);
   }

   public int getCurrentTool()
   {
      return application.getCurrentTool();
   }

   public boolean isIoInProgress()
   {
      JDRCanvas canvas = getCanvas();

      if (canvas == null)
      {
         return false;
      }

      return canvas.getFrame().isIoInProgress();
   }

   public boolean requiresCanvas()
   {
      return requiresCanvas;
   }

   public void setRequiresCanvas(boolean flag)
   {
      requiresCanvas = flag;
   }

   public boolean requiresNonEmptyImage()
   {
      return requiresNonEmptyImage;
   }

   public void setRequiresNonEmptyImage(boolean flag)
   {
      requiresNonEmptyImage = flag;
   }

   public boolean validDuringIO()
   {
      return validDuringIO;
   }

   public void setValidDuringIO(boolean flag)
   {
      validDuringIO = flag;
   }

   public byte getCurrentEditFlag()
   {
      JDRCanvas canvas = getCanvas();

      if (canvas == null)
      {
         return EDIT_FLAG_NONE;
      }

      return canvas.getEditFlag();
   }

   public byte getCurrentConstructionFlag()
   {
      JDRCanvas canvas = getCanvas();

      if (canvas == null)
      {
         return CONSTRUCTION_FLAG_NONE;
      }

      return canvas.getConstructionFlag();
   }

   public JDRSelection getCurrentSelection()
   {
      JDRCanvas canvas = getCanvas();

      if (canvas == null)
      {
         return null;
      }

      return canvas.getSelectionFlags();
   }

   public boolean isValid()
   {
      return isValid(isIoInProgress(), getCurrentTool(),
         getCurrentEditFlag(), getCurrentConstructionFlag(),
         validSelectionFlag == SELECT_FLAG_ANY ? null : getCurrentSelection());
   }

   public boolean isValid(boolean ioInProgress, int currentTool,
     byte currentEditFlag, byte currentConstructionFlag)
   {
      return isValid(ioInProgress, currentTool, currentEditFlag,
         currentConstructionFlag, null);
   }

   public boolean isValid(boolean ioInProgress, int currentTool,
     byte currentEditFlag, byte currentConstructionFlag,
     JDRSelection currentSelection)
   {
      JDRCanvas canvas = getCanvas();

      if (requiresCanvas() && canvas == null)
      {
         return false;
      }

      if (!validDuringIO() && ioInProgress)
      {
         return false;
      }

      if (requiresNonEmptyImage())
      {
         if (canvas == null)
         {
            return false;
         }

         JDRGroup image = canvas.getAllPaths();

         if (image == null || image.size() == 0)
         {
            return false;
         }
      }

      if (currentTool != -1 // current tool is known
      && ((1 << currentTool) & validToolFlag) == 0)
      {
         return false;
      }


      if ((currentConstructionFlag & validConstructionFlag) == 0)
      {
         return false;
      }

      if (currentTool == ACTION_SELECT)
      {
         int currentSelectionFlag;
         int[] selections;

         if (currentSelection == null)
         {
            currentSelectionFlag = SELECT_FLAG_NONE;
            selections = SELECTION_IGNORE_COUNT;
         }
         else
         {
            currentSelectionFlag = currentSelection.getSelectionFlag();
            selections = currentSelection.getSelectionCount();
         }

         if ((currentEditFlag & validEditFlag) == 0)
         {
            return false;
         }

         if ((currentSelectionFlag & validSelectionFlag) == 0)
         {
            return false;
         }

         if (selections == SELECTION_IGNORE_COUNT)
         {
            // Haven't been provided with any information about the
            // number of selections

            return true;
         }

         if (validSelectionNumbers == SELECTION_IGNORE_COUNT)
         {
            // This action doesn't care how many objects of a
            // particular type have been selected.

            return true;
         }

         for (int i = 0; i < validSelectionNumbers.length; i++)
         {
            if (validSelectionNumbers[i] == -1)
            {
               // This action doesn't care how many of this type of
               // object have been selected.

               continue;
            }

            if ((currentSelectionFlag & (1 << i)) == 0)
            {
               // This object type hasn't been selected.

               if (validSelectionNumbers[i] != 0)
               {
                  return false;
               }
            }
            else if (validSelectionNumbers[i] < -1)
            {
               // Minimum number required

               if (selections[i] < -validSelectionNumbers[i])
               {
                  return false;
               }
            }
            else if (validSelectionNumbers[i] != selections[i])
            {
               return false;
            }
         }
      }

      return true;
   }

   public static JDRButtonItem createButtonItem(FlowframTk application,
      String parentId, String name, JComponent comp, JMenu menu,
      FlowframTkActionListener listener)
   {
      return createButtonItem(application, parentId, name,
        "tooltip."+name, comp, menu, listener);
   }

   public static JDRButtonItem createButtonItem(FlowframTk application,
      String parentId, String name, String tooltipId,
      JComponent comp, JMenu menu,
      FlowframTkActionListener listener)
   {
      return createButtonItem(application, parentId, name,
        tooltipId, comp, menu,
        parentId == null ? name : parentId+"."+name,
        listener);
   }

   public static JDRButtonItem createButtonItem(FlowframTk application,
      String parentId, String name, String tooltipId,
      JComponent comp, JMenu menu, String keystrokeId,
      FlowframTkActionListener listener)
   {
      return createButtonItem(application, parentId, name,
        tooltipId, comp, menu, keystrokeId,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
        SELECTION_IGNORE_COUNT,
        false, true,
        listener);
   }

   public static JDRButtonItem createButtonItem(FlowframTk application,
      String parentId, String name, String tooltipId,
      JComponent comp, JMenu menu, String keystrokeId,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createButtonItem(application, parentId, name,
        tooltipId, comp, menu, keystrokeId,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
        SELECTION_IGNORE_COUNT,
        requiresCanvas, validDuringIO,
        listener);
   }

   public static JDRButtonItem createButtonItem(FlowframTk application,
      String parentId, String name, JComponent comp, JMenu menu,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createButtonItem(application, parentId, name,
        "tooltip."+name, comp, menu,
        parentId == null ? name : parentId+"."+name,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
        SELECTION_IGNORE_COUNT,
        requiresCanvas, validDuringIO,
        listener);
   }

   public static JDRButtonItem createButtonItem(FlowframTk application,
      String parentId, String name, String tooltipId,
      JComponent comp, JMenu menu,
      String keystrokeId, int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createButtonItem(application, parentId, name,
         tooltipId, comp, menu, keystrokeId, validToolsFlag,
         validEditFlag, CONSTRUCTION_FLAG_ANY,
         validSelectionFlag, validSelectionNumbers,
         requiresCanvas, validDuringIO, false, listener);
   }

   public static JDRButtonItem createButtonItem(FlowframTk application,
      String parentId, String name,
      JComponent comp, JMenu menu,
      int validToolsFlag,
      byte validEditFlag, 
      int validSelectionFlag, int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO, 
      FlowframTkActionListener listener)
   {
      return createButtonItem(application, parentId, name,
        comp, menu, validToolsFlag, validEditFlag,
        CONSTRUCTION_FLAG_ANY, validSelectionFlag,
        validSelectionNumbers, requiresCanvas, validDuringIO,
        false, listener);
   }

   public static JDRButtonItem createButtonItem(FlowframTk application,
      String parentId, String name,
      JComponent comp, JMenu menu,
      int validToolsFlag,
      byte validEditFlag, byte validConstructionFlag,
      int validSelectionFlag, int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO, 
      boolean requiresNonEmptyImage,
      FlowframTkActionListener listener)
   {
      return createButtonItem(application, parentId, name,
         "tooltip."+name, comp, menu, 
         parentId == null ? name : parentId+"."+name,
         validToolsFlag, validEditFlag, validConstructionFlag,
         validSelectionFlag, validSelectionNumbers,
         requiresCanvas, validDuringIO, requiresNonEmptyImage,
         listener);
   }

   public static JDRButtonItem createButtonItem(FlowframTk application,
      String parentId, String name, String tooltipId,
      JComponent comp, JMenu menu,
      String keystrokeId, int validToolsFlag,
      byte validEditFlag, byte validConstructionFlag,
      int validSelectionFlag, int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO, 
      boolean requiresNonEmptyImage,
      FlowframTkActionListener listener)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      FlowframTkAction action = new FlowframTkAction(application,
         name, listener, keystrokeId, validToolsFlag,
         validEditFlag, validConstructionFlag, validSelectionFlag,
         validSelectionNumbers);

      action.setRequiresCanvas(requiresCanvas);
      action.setValidDuringIO(validDuringIO);
      action.setRequiresNonEmptyImage(requiresNonEmptyImage);

      application.addAppAction(action);

      return action.createButtonItem(menuId,
         name, keystrokeId, tooltipId, comp, menu);
   }

   public static JDRToggleButtonItem createToggleButtonItem(FlowframTk application,
      String parentId, String name, boolean selected,
      JComponent comp, JMenu menu,
      int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createToggleButtonItem(application, parentId, name,
        selected, "tooltip."+name, comp, menu,
        parentId == null ? name : parentId+"."+name,
        validToolsFlag, validEditFlag, validSelectionFlag,
        validSelectionNumbers, requiresCanvas, validDuringIO,
        listener);
   }

   public static JDRToggleButtonItem createToggleButtonItem(FlowframTk application,
      String parentId, String name, boolean selected, String tooltipId,
      JComponent comp, JMenu menu,
      String keystrokeId, int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createToggleButtonItem(application, parentId, name,
        selected, tooltipId, comp, menu, keystrokeId,
        validToolsFlag, validEditFlag, CONSTRUCTION_FLAG_ANY,
        validSelectionFlag, validSelectionNumbers,
        requiresCanvas, validDuringIO, false, listener);
   }

   public static JDRToggleButtonItem createToggleButtonItem(FlowframTk application,
      String parentId, String name, boolean selected, String tooltipId,
      JComponent comp, JMenu menu,
      String keystrokeId, int validToolsFlag,
      byte validEditFlag, byte validConstructionFlag,
      int validSelectionFlag, int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO, 
      boolean requiresNonEmptyImage,
      FlowframTkActionListener listener)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      FlowframTkAction action = new FlowframTkAction(application,
         name, listener, keystrokeId, validToolsFlag,
         validEditFlag, validConstructionFlag, 
         validSelectionFlag, validSelectionNumbers);

      action.setRequiresCanvas(requiresCanvas);
      action.setValidDuringIO(validDuringIO);
      action.setRequiresNonEmptyImage(requiresNonEmptyImage);

      application.addAppAction(action);

      return action.createToggleButtonItem(menuId,
         name, keystrokeId, tooltipId, selected, comp, menu);
   }

   public static JMenuItem createMenuItem(FlowframTk application,
      String parentId, String name, JMenu menu,
      FlowframTkActionListener listener)
   {
      return createMenuItem(application, parentId, name,
        "tooltip."+name, menu, listener);
   }

   public static JMenuItem createMenuItem(FlowframTk application,
      String parentId, String name, String tooltipId, JMenu menu,
      FlowframTkActionListener listener)
   {
      return createMenuItem(application, parentId, name,
        tooltipId, menu, 
        parentId == null ? name : parentId+"."+name,
        listener);
   }

   public static JMenuItem createMenuItem(FlowframTk application,
      String parentId, String name, String tooltipId, JMenu menu,
      String keystrokeId,
      FlowframTkActionListener listener)
   {
      return createMenuItem(application, parentId, name,
        tooltipId, menu, keystrokeId,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY, 
        SELECTION_IGNORE_COUNT,
        false, true,
        listener);
   }

   public static JMenuItem createMenuItem(FlowframTk application,
      String parentId, String name, String tooltipId, JMenu menu,
      String keystrokeId,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createMenuItem(application, parentId, name,
        tooltipId, menu, keystrokeId,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
        SELECTION_IGNORE_COUNT,
        requiresCanvas, validDuringIO,
        listener);
   }

   public static JMenuItem createMenuItem(FlowframTk application,
      String parentId, String name, JMenu menu,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createMenuItem(application, parentId, name,
        "tooltip."+name, menu,
        parentId == null ? name : parentId+"."+name,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY, 
        SELECTION_IGNORE_COUNT,
        requiresCanvas, validDuringIO,
        listener);
   }

   public static JMenuItem createMenuItem(FlowframTk application,
      String parentId, String name, JMenu menu, int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createMenuItem(application, parentId, name,
        "tooltip."+name, menu,
        parentId == null ? name : parentId+"."+name,
        validToolsFlag, validEditFlag, validSelectionFlag, 
        validSelectionNumbers,
        requiresCanvas, validDuringIO,
        listener);
   }

   public static JMenuItem createMenuItem(FlowframTk application,
      String parentId, String name, 
      JMenu menu, int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      boolean requiresNonEmptyImage,
      FlowframTkActionListener listener)
   {
      return createMenuItem(application, parentId, name,
        "tooltip."+name, menu,
        parentId == null ? name : parentId+"."+name,
        validToolsFlag, validEditFlag, 
        CONSTRUCTION_FLAG_ANY,
        validSelectionFlag,
        validSelectionNumbers, requiresCanvas, validDuringIO,
        requiresNonEmptyImage, listener);
   }

   public static JMenuItem createMenuItem(FlowframTk application,
      String parentId, String name, String tooltipId,
      JMenu menu,
      String keystrokeId, int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createMenuItem(application, parentId, name, tooltipId,
        menu, keystrokeId, validToolsFlag, validEditFlag,
        CONSTRUCTION_FLAG_ANY, validSelectionFlag,
        validSelectionNumbers, requiresCanvas, validDuringIO, false,
        listener);
   }

   public static JMenuItem createMenuItem(FlowframTk application,
      String parentId, String name, JMenu menu,
      int validToolsFlag,
      byte validEditFlag, byte validConstructionFlag,
      int validSelectionFlag, int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      boolean requiresNonEmptyImage,
      FlowframTkActionListener listener)
   {
      return createMenuItem(application, parentId, name, 
        "tooltip."+name, menu, 
        parentId==null?name:parentId+"."+name,
        validToolsFlag, validEditFlag,
        validConstructionFlag, validSelectionFlag,
        validSelectionNumbers, requiresCanvas, validDuringIO, 
        requiresNonEmptyImage, listener);
   }

   public static JMenuItem createMenuItem(FlowframTk application,
      String parentId, String name, String tooltipId,
      JMenu menu,
      String keystrokeId, int validToolsFlag,
      byte validEditFlag, byte validConstructionFlag,
      int validSelectionFlag, int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO, 
      boolean requiresNonEmptyImage,
      FlowframTkActionListener listener)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      FlowframTkAction action = new FlowframTkAction(application,
         name, listener, keystrokeId, validToolsFlag,
         validEditFlag, validConstructionFlag, 
         validSelectionFlag, validSelectionNumbers);

      action.setRequiresCanvas(requiresCanvas);
      action.setValidDuringIO(validDuringIO);
      action.setRequiresNonEmptyImage(requiresNonEmptyImage);

      application.addAppAction(action);

      JMenuItem item = action.createMenuItem(menuId, tooltipId);

      menu.add(item);

      return item;
   }

   public static JMenu createMenu(FlowframTk application,
      String name, 
      int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO)
   {
      return createMenu(application, (String)null, name,
       "tooltip."+name, (JMenu)null,
       validToolsFlag, validEditFlag, validSelectionFlag,
       validSelectionNumbers, requiresCanvas, validDuringIO, null);
   }

   public static JMenu createMenu(FlowframTk application,
      String parentId, String name,
      int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO)
   {
      return createMenu(application, parentId, name, 
       "tooltip."+name, (JMenu)null,
       validToolsFlag, validEditFlag, validSelectionFlag,
       validSelectionNumbers, requiresCanvas, validDuringIO, null);
   }

   public static JMenu createMenu(FlowframTk application,
      String parentId, String name, JMenu parentMenu,
      int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO)
   {
      return createMenu(application, parentId, name, 
       "tooltip."+name, parentMenu,
       validToolsFlag, validEditFlag, validSelectionFlag,
       validSelectionNumbers, requiresCanvas, validDuringIO, null);
   }

   public static JMenu createMenu(FlowframTk application,
      String name, JMenu parentMenu,
      int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO)
   {
      return createMenu(application, null, name, 
       "tooltip."+name, parentMenu,
       validToolsFlag, validEditFlag, validSelectionFlag,
       validSelectionNumbers, requiresCanvas, validDuringIO, null);
   }

   public static JMenu createMenu(FlowframTk application,
      String parentId, String name, String tooltipId,
      int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO)
   {
      return createMenu(application, parentId, name, tooltipId,
       null, validToolsFlag, validEditFlag, validSelectionFlag,
       validSelectionNumbers, requiresCanvas, validDuringIO, null);
   }

   public static JMenu createMenu(FlowframTk application,
      String parentId, String name, String tooltipId,
      JMenu parentMenu,
      int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO)
   {
      return createMenu(application, parentId, name, tooltipId,
       parentMenu, validToolsFlag, validEditFlag, validSelectionFlag,
       validSelectionNumbers, requiresCanvas, validDuringIO, null);
   }

   public static JMenu createMenu(FlowframTk application,
      String parentId, String name, String tooltipId,
      JMenu parentMenu,
      int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createMenu(application, parentId, name, tooltipId,
        parentMenu, validToolsFlag, validEditFlag,
        CONSTRUCTION_FLAG_ANY, validSelectionFlag,
        validSelectionNumbers, requiresCanvas, validDuringIO,
        listener);
   }

   public static JMenu createMenu(FlowframTk application,
      String parentId, String name, String tooltipId,
      JMenu parentMenu,
      int validToolsFlag,
      byte validEditFlag, byte validConstructionFlag,
      int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      FlowframTkAction action = new FlowframTkAction(application,
         name, listener, null, validToolsFlag,
         validEditFlag, validConstructionFlag, validSelectionFlag,
         validSelectionNumbers);

      action.setRequiresCanvas(requiresCanvas);
      action.setValidDuringIO(validDuringIO);

      application.addAppAction(action);

      JMenu menu = action.createMenu(menuId, tooltipId);

      if (parentMenu != null)
      {
         parentMenu.add(menu);
      }

      return menu;
   }

   public static JRadioButtonMenuItem createRadioButtonMenuItem(FlowframTk application,
      String parentId, String name, 
      JMenu menu, ButtonGroup buttonGroup,
      int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createRadioButtonMenuItem(application, parentId, name,
       menu, buttonGroup, false,
        
       validToolsFlag, validEditFlag, validSelectionFlag,
       validSelectionNumbers, requiresCanvas, validDuringIO,
       listener);
   }

   public static JRadioButtonMenuItem createRadioButtonMenuItem(FlowframTk application,
      String parentId, String name, 
      JMenu menu, ButtonGroup buttonGroup, boolean selected,
      int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createRadioButtonMenuItem(application, parentId, name,
       "tooltip."+name, menu, buttonGroup, selected,
       parentId == null ? name : parentId+"."+name, 
       validToolsFlag, validEditFlag, validSelectionFlag,
       validSelectionNumbers, requiresCanvas, validDuringIO,
       listener);
   }

   public static JRadioButtonMenuItem createRadioButtonMenuItem(FlowframTk application,
      String parentId, String name, String tooltipId,
      JMenu menu, ButtonGroup buttonGroup, boolean selected,
      String keystrokeId, int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createRadioButtonMenuItem(application, parentId, name,
        tooltipId, menu, buttonGroup, selected,
        keystrokeId, validToolsFlag, validEditFlag,
        CONSTRUCTION_FLAG_ANY, validSelectionFlag,
        validSelectionNumbers, requiresCanvas, validDuringIO,
        listener);
   }

   public static JRadioButtonMenuItem createRadioButtonMenuItem(FlowframTk application,
      String parentId, String name, String tooltipId,
      JMenu menu, ButtonGroup buttonGroup, boolean selected,
      String keystrokeId, int validToolsFlag,
      byte validEditFlag, byte validConstructionFlag,
      int validSelectionFlag, int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      FlowframTkAction action = new FlowframTkAction(application,
         name, listener, keystrokeId, validToolsFlag,
         validEditFlag, validConstructionFlag, 
         validSelectionFlag, validSelectionNumbers);

      action.setRequiresCanvas(requiresCanvas);
      action.setValidDuringIO(validDuringIO);

      application.addAppAction(action);

      JRadioButtonMenuItem item = action.createRadioButtonMenuItem(
         menuId, buttonGroup, selected, tooltipId);

      menu.add(item);

      return item;
   }

   public static JCheckBoxMenuItem createToggleMenuItem(FlowframTk application,
      String parentId, String name, JMenu menu,
      FlowframTkActionListener listener)
   {
      return createToggleMenuItem(application, parentId, name,
        "tooltip."+name, menu, false,
        parentId == null ? name : parentId+"."+name,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, CONSTRUCTION_FLAG_ANY,
        SELECT_FLAG_ANY, SELECTION_IGNORE_COUNT,
        false, true, listener);
   }

   public static JCheckBoxMenuItem createToggleMenuItem(FlowframTk application,
      String parentId, String name, JMenu menu, boolean selected,
      FlowframTkActionListener listener)
   {
      return createToggleMenuItem(application, parentId, name,
        "tooltip."+name, menu, selected,
        parentId == null ? name : parentId+"."+name,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, CONSTRUCTION_FLAG_ANY,
        SELECT_FLAG_ANY, SELECTION_IGNORE_COUNT,
        false, true, listener);
   }

   public static JCheckBoxMenuItem createToggleMenuItem(FlowframTk application,
      String parentId, String name, 
      JMenu menu, boolean selected,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createToggleMenuItem(application, parentId, name,
         "tooltip."+name, menu, selected,
         parentId == null ? name : parentId+"."+name,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, CONSTRUCTION_FLAG_ANY,
        SELECT_FLAG_ANY, SELECTION_IGNORE_COUNT,
        requiresCanvas, validDuringIO, listener);
   }

   public static JCheckBoxMenuItem createToggleMenuItem(FlowframTk application,
      String parentId, String name, 
      JMenu menu, boolean selected, int validToolsFlag,
      byte validEditFlag, byte validConstructionFlag,
      int validSelectionFlag, int[] validSelectionNumbers,

      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createToggleMenuItem(application, parentId, name,
         "tooltip."+name, menu, selected,
         parentId == null ? name : parentId+"."+name,
        validToolsFlag, validEditFlag, validConstructionFlag,
        validSelectionFlag, validSelectionNumbers,
        requiresCanvas, validDuringIO, listener);
   }

   public static JCheckBoxMenuItem createToggleMenuItem(FlowframTk application,
      String parentId, String name, String tooltipId,
      JMenu menu, boolean selected,
      String keystrokeId, int validToolsFlag,
      byte validEditFlag, byte validConstructionFlag,
      int validSelectionFlag, int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      FlowframTkAction action = new FlowframTkAction(application,
         name, listener, keystrokeId, validToolsFlag,
         validEditFlag, validConstructionFlag, 
         validSelectionFlag, validSelectionNumbers);

      action.setRequiresCanvas(requiresCanvas);
      action.setValidDuringIO(validDuringIO);

      application.addAppAction(action);

      JCheckBoxMenuItem item = action.createCheckBoxMenuItem(
         menuId, selected, tooltipId);

      if (menu != null)
      {
         menu.add(item);
      }

      return item;
   }

   public static JDRToolButtonItem createToolButtonItem(FlowframTk application,
      String parentId, String name,
      ToolButtonGroup buttonGroup,
      JComponent comp, JMenu menu, 
      boolean canvasRequired, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createToolButtonItem(application, parentId,
        name, "tooltip."+name, buttonGroup,
        comp, menu, parentId==null ? name : parentId+"."+name,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, SELECT_FLAG_ANY,
        SELECTION_IGNORE_COUNT, canvasRequired, validDuringIO, 
        listener);
   }

   public static JDRToolButtonItem createToolButtonItem(FlowframTk application,
      String parentId, String name,
      ToolButtonGroup buttonGroup,
      JComponent comp, JMenu menu, 
      byte validConstructionFlag,
      boolean canvasRequired, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createToolButtonItem(application, parentId,
        name, "tooltip."+name, buttonGroup,
        comp, menu, parentId==null ? name : parentId+"."+name,
        TOOL_FLAG_ANY, EDIT_FLAG_ANY, validConstructionFlag, 
        SELECT_FLAG_ANY, SELECTION_IGNORE_COUNT, 
        canvasRequired, validDuringIO, 
        listener);
   }

   public static JDRToolButtonItem createToolButtonItem(FlowframTk application,
      String parentId, String name, String tooltipId,
      ToolButtonGroup buttonGroup,
      JComponent comp, JMenu menu, String keystrokeId, 
      int validToolsFlag,
      byte validEditFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      return createToolButtonItem(application, parentId, name, tooltipId,
        buttonGroup, comp, menu, keystrokeId,
        validToolsFlag, validEditFlag, CONSTRUCTION_FLAG_ANY,
        validSelectionFlag, validSelectionNumbers,
        requiresCanvas, validDuringIO,
        listener);
   }

   public static JDRToolButtonItem createToolButtonItem(FlowframTk application,
      String parentId, String name, String tooltipId,
      ToolButtonGroup buttonGroup,
      JComponent comp, JMenu menu, String keystrokeId, 
      int validToolsFlag,
      byte validEditFlag, byte validConstructionFlag, int validSelectionFlag,
      int[] validSelectionNumbers,
      boolean requiresCanvas, boolean validDuringIO,
      FlowframTkActionListener listener)
   {
      FlowframTkAction action = new FlowframTkAction(application,
         name, listener, keystrokeId, validToolsFlag,
         validEditFlag, validConstructionFlag, validSelectionFlag,
         validSelectionNumbers);

      action.setRequiresCanvas(requiresCanvas);
      action.setValidDuringIO(validDuringIO);

      application.addAppAction(action);

      return action.createToolButtonItem(parentId, name,
         keystrokeId, tooltipId, buttonGroup,
         comp, menu);
   }

   public static int[] createNumberArray(int index, int value)
   {
      return createNumberArray(null, index, value);
   }

   public static int[] createNumberArray(int[] numArray, int index, int value)
   {
      int[] array;

      if (numArray == null)
      {
         array = new int[OBJECT_MAX_INDEX+1];

         for (int i = 0; i < array.length; i++)
         {
            array[i] = -1;
         }
      }
      else
      {
         array = Arrays.copyOf(numArray, numArray.length);
      }

      array[index] = value;

      return array;
   }

   public static int[] createNumberArray(int index1, int value1,
      int index2, int value2)
   {
      return createNumberArray(null,
         index1, value1,
         index2, value2);
   }

   public static int[] createNumberArray(int[] numArray, 
      int index1, int value1,
      int index2, int value2)
   {
      int[] array;

      if (numArray == null)
      {
         array = new int[OBJECT_MAX_INDEX+1];

         for (int i = 0; i < array.length; i++)
         {
            array[i] = -1;
         }
      }
      else
      {
         array = Arrays.copyOf(numArray, numArray.length);
      }

      array[index1] = value1;
      array[index2] = value2;

      return array;
   }

   public static int[] createNumberArray(int index1, int value1,
      int index2, int value2, int index3, int value3)
   {
      return createNumberArray(null,
         index1, value1,
         index2, value2,
         index3, value3);
   }

   public static int[] createNumberArray(int[] numArray, 
      int index1, int value1,
      int index2, int value2,
      int index3, int value3)
   {
      int[] array;

      if (numArray == null)
      {
         array = new int[OBJECT_MAX_INDEX+1];

         for (int i = 0; i < array.length; i++)
         {
            array[i] = -1;
         }
      }
      else
      {
         array = Arrays.copyOf(numArray, numArray.length);
      }

      array[index1] = value1;
      array[index2] = value2;
      array[index3] = value3;

      return array;
   }

   public static int[] createNumberArray(int index1, int value1,
      int index2, int value2, int index3, int value3,
      int index4, int value4)
   {
      return createNumberArray(null,
         index1, value1,
         index2, value2,
         index3, value3,
         index4, value4);
   }

   public static int[] createNumberArray(int[] numArray, 
      int index1, int value1,
      int index2, int value2,
      int index3, int value3,
      int index4, int value4)
   {
      int[] array;

      if (numArray == null)
      {
         array = new int[OBJECT_MAX_INDEX+1];

         for (int i = 0; i < array.length; i++)
         {
            array[i] = -1;
         }
      }
      else
      {
         array = Arrays.copyOf(numArray, numArray.length);
      }

      array[index1] = value1;
      array[index2] = value2;
      array[index3] = value3;
      array[index4] = value4;

      return array;
   }

   public static final int[] SELECTION_IGNORE_COUNT = null;

   public static final int[] SELECTION_NONE
     = createNumberArray(OBJECT_COMPLETE, 0);

   public static final int[] SELECTION_SINGLE_OBJECT
     = createNumberArray(OBJECT_COMPLETE, 1);

   public static final int[] SELECTION_AT_LEAST_TWO_OBJECTS
     = createNumberArray(OBJECT_COMPLETE, -2);

   public static final int[] SELECTION_SINGLE_OBJECT_NO_GROUP
     = createNumberArray
       (
          OBJECT_COMPLETE, 1,
          OBJECT_GROUP, 0
       );

   public static final int[] SELECTION_SINGLE_SHAPE_NO_GROUP
     = createNumberArray
       (
          OBJECT_COMPLETE, 1,
          OBJECT_SHAPE, 1,
          OBJECT_GROUP, 0
       );

   public static final int[] SELECTION_SINGLE_NON_PATTERN_SHAPE_NO_GROUP
     = createNumberArray(SELECTION_SINGLE_SHAPE_NO_GROUP,
         OBJECT_PATTERN, 0);

   public static final int[] SELECTION_SINGLE_PATTERN_NO_GROUP
     = createNumberArray
       (
          OBJECT_COMPLETE, 1,
          OBJECT_PATTERN, 1,
          OBJECT_GROUP, 0
       );

   public static final int[] SELECTION_SINGLE_TEXTUAL
     = createNumberArray
      (
         OBJECT_COMPLETE, 1,
         OBJECT_TEXTUAL, 1
      );

   public static final int[] SELECTION_SINGLE_BITMAP
     = createNumberArray
      (
         OBJECT_COMPLETE, 1,
         OBJECT_BITMAP, 1
      );

   public static final int[] SELECTION_ONLY_COMPOUND_SHAPES
     = createNumberArray
       (
          OBJECT_PATH, 0,
          OBJECT_TEXT, 0,
          OBJECT_BITMAP, 0,
          OBJECT_GROUP, 0
       );

   public static final int[] SELECTION_ONE_NON_TEXTUAL_SHAPE_AND_ONE_TEXT_ONLY
     = createNumberArray
       (
          OBJECT_COMPLETE, 2,
          OBJECT_TEXT, 1,
          OBJECT_NON_TEXTUAL_SHAPE, 1
       );

   public static final int[] SELECTION_NO_TEXT
     = createNumberArray(OBJECT_TEXTUAL, 0);

   public static final int[] SELECTION_ONLY_TEXTUAL
     = createNumberArray
       (
         OBJECT_NON_TEXTUAL_SHAPE, 0,
         OBJECT_GROUP, 0,
         OBJECT_BITMAP, 0,
         OBJECT_DISTORTED, 0
       );

   public static final int[] SELECTION_ONLY_SHAPES_OR_TEXT
     = createNumberArray
       (
         OBJECT_GROUP, 0,
         OBJECT_BITMAP, 0,
         OBJECT_DISTORTED, 0
       );

   public static final int[] SELECTION_ONLY_SHAPES
     = createNumberArray
       (
         OBJECT_GROUP, 0,
         OBJECT_BITMAP, 0,
         OBJECT_TEXT, 0,
         OBJECT_DISTORTED, 0
       );

   public static final int[] SELECTION_AT_LEAST_TWO_SHAPES_NO_OTHER
     = createNumberArray
       (
          SELECTION_ONLY_SHAPES,
          OBJECT_SHAPE, -2
       );

   public static final int[] SELECTION_SINGLE_CLOSED_SHAPE
     = createNumberArray
      (
         OBJECT_COMPLETE, 1,
         OBJECT_SHAPE, 1,
         OBJECT_CLOSED, 1
      );

   public static final int[] SELECTION_SINGLE_OPEN_SHAPE
     = createNumberArray
      (
         OBJECT_COMPLETE, 1,
         OBJECT_SHAPE, 1,
         OBJECT_OPEN, 1
      );

   public static final int[] SELECTION_NO_SYMMETRIC_SHAPES
     = createNumberArray
       (
          OBJECT_SYMMETRIC, 0,
          OBJECT_SYMTEXTPATH, 0
       );

   public static final byte CONSTRUCTION_FLAG_NONE = (byte)1;
   public static final byte CONSTRUCTION_FLAG_NON_GEOMETRIC = (byte)2;
   public static final byte CONSTRUCTION_FLAG_GEOMETRIC = (byte)4;
   public static final byte CONSTRUCTION_FLAG_TEXT = (byte)8;

   public static final byte CONSTRUCTION_FLAG_PATH =
     CONSTRUCTION_FLAG_NON_GEOMETRIC
   | CONSTRUCTION_FLAG_GEOMETRIC;

   public static final byte CONSTRUCTION_FLAG_PATH_OR_TEXT =
     CONSTRUCTION_FLAG_PATH
   | CONSTRUCTION_FLAG_TEXT;

   public static final byte CONSTRUCTION_FLAG_ANY =
     CONSTRUCTION_FLAG_PATH
   | CONSTRUCTION_FLAG_TEXT
   | CONSTRUCTION_FLAG_NONE;

   public static final byte CONSTRUCTION_FLAG_NONE_OR_TEXT =
     CONSTRUCTION_FLAG_NONE
   | CONSTRUCTION_FLAG_TEXT;


   private boolean requiresCanvas = false;

   private boolean validDuringIO = false;

   private boolean requiresNonEmptyImage = false;

   private int validToolFlag = TOOL_FLAG_ANY;

   private byte validEditFlag = EDIT_FLAG_ANY;

   private byte validConstructionFlag = CONSTRUCTION_FLAG_ANY;

   private int validSelectionFlag = SELECT_FLAG_ANY;

   private int[] validSelectionNumbers = SELECTION_IGNORE_COUNT;

   private KeyStroke accelerator;

   private FlowframTk application;

   private FlowframTkActionListener listener;

   private AbstractButton actionButton;

   private String actionCommand;
}
