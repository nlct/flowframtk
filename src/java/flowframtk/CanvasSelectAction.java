// File          : CanvasSelectAction.java
// Description   : Action for selection-related actions
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

public class CanvasSelectAction extends CanvasAction
{
   public CanvasSelectAction(JDRCanvas canvas, FlowframTkAction action)
   {
      super(canvas, action);

      actionButtonList = new Vector<AbstractButton>();
   }

   public CanvasSelectAction(JDRCanvas canvas,
     String actionCommand,
     KeyStroke keyStroke,
     FlowframTkActionListener listener,
     int validToolsFlag,
     byte validEditFlag,
     byte validConstructionFlag,
     int validSelectionFlag,
     int[] validSelectionNumbers)
   {
      super(canvas, actionCommand, keyStroke,
        listener, validToolsFlag, validEditFlag,
        validConstructionFlag, validSelectionFlag,
        validSelectionNumbers);

      actionButtonList = new Vector<AbstractButton>();
   }

   public void setActionButton(AbstractButton button)
   {
      if (getActionButton() == null)
      {
         super.setActionButton(button);
      }

      actionButtonList.add(button);
   }

   public void removeAllButtons()
   {
      for (AbstractButton button : actionButtonList)
      {
         button.removeActionListener(this);
      }

      actionButtonList.clear();
   }

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource();

      if (source instanceof AbstractButton)
      {
         setSelected(((AbstractButton)source).isSelected());
      }

      FlowframTkActionListener listener = getListener();

      if (listener == null) return;

      if (!isValid())
      {
         return;
      }

      listener.doAction(this, evt);
   }

   public static JMenuItem createMenuItem(JDRCanvas canvas,
     String parentId,
     String name,
     FlowframTkActionListener listener,
     byte validEditFlag,
     byte validConstructionFlag,
     int validSelectionFlag,
     int[] validSelectionNumbers)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      CanvasSelectAction action = new CanvasSelectAction(canvas,
         name, canvas.getResources().getAccelerator(menuId),
         listener, TOOL_FLAG_SELECT, validEditFlag,
         validConstructionFlag, validSelectionFlag,
         validSelectionNumbers);

      canvas.addCanvasAction(action);

      return action.createMenuItem(menuId, "tooltip."+name);
   }

   public void setSelected(boolean selected)
   {
      super.setSelected(selected);

      for (AbstractButton button : actionButtonList)
      {
         button.setSelected(selected);
      }
   }

   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);

      for (AbstractButton button : actionButtonList)
      {
         button.setEnabled(enabled);
      }
   }

   private JDRCanvas canvas;

   private Vector<AbstractButton> actionButtonList;
}
