// File          : CanvasAction.java
// Description   : Action for path editing etc
// Date          : 2012-03-15
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2012-2025 Nicola L.C. Talbot

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

public class CanvasAction extends FlowframTkAction
{
   public CanvasAction(JDRCanvas canvas, FlowframTkAction action)
   {
      super(action.getApplication(), action.getActionCommand(),
            action.getListener());

      this.canvas = canvas;
      this.parent = action;

      setValidToolFlag(action.getValidToolFlag());
      setValidEditFlag(action.getValidEditFlag());
      setValidConstructionFlag(action.getValidConstructionFlag());
      setValidSelection(action.getValidSelectionFlag(),
         action.getValidSelectionNumbers());
      setActionCommand(action.getActionCommand());
      setRequiresNonEmptyImage(action.requiresNonEmptyImage());
      setRequiresCanvas(true);
      setValidDuringIO(action.validDuringIO());
      setRequiresTypeblock(action.requiresTypeblock());

      setAccelerator(action.getAccelerator());
   }

   public CanvasAction(JDRCanvas canvas,
     String actionCommand,
     KeyStroke keyStroke,
     FlowframTkActionListener listener,
     int validToolsFlag,
     byte validEditFlag,
     byte validConstructionFlag,
     int validSelectionFlag,
     int[] validSelectionNumbers)
   {
      super(canvas.getApplication(), actionCommand,
        listener, validToolsFlag, validEditFlag,
        validConstructionFlag, validSelectionFlag,
        validSelectionNumbers);

      this.canvas = canvas;

      setAccelerator(keyStroke);
   }

   public JDRCanvas getCanvas()
   {
      return canvas;
   }

   public JDRFrame getFrame()
   {
      return canvas == null ? null : canvas.getFrame();
   }

   public void setAccelerator(KeyStroke keyStroke)
   {
      String name = getActionCommand();

      if (keyStroke != null && name != null)
      {
         super.setAccelerator(keyStroke);

         if (canvas != null)
         {
            JDRFrame frame = canvas.getFrame();

            frame.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStroke,
               name);
            frame.getActionMap().put(name, this);

         }
      }
   }

   public static JMenuItem createMenuItem(JDRCanvas canvas,
     String parentId,
     String name,
     FlowframTkActionListener listener,
     int validToolsFlag,
     byte validEditFlag,
     byte validConstructionFlag,
     int validSelectionFlag,
     int[] validSelectionNumbers)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      CanvasAction action = new CanvasAction(canvas,
         name, canvas.getResources().getAccelerator(menuId),
         listener, validToolsFlag, validEditFlag,
         validConstructionFlag, validSelectionFlag,
         validSelectionNumbers);

      canvas.addCanvasAction(action);

      return action.createMenuItem(menuId, "tooltip."+name);
   }

   public FlowframTkAction getParent()
   {
      return parent;
   }

   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);

      if (parent != null)
      {
         parent.setEnabled(enabled);
      }
   }

   public void setSelected(boolean selected)
   {
      super.setSelected(selected);

      if (parent != null)
      {
         parent.setSelected(selected);
      }
   }

   private FlowframTkAction parent;

   private JDRCanvas canvas;

}
