// File          : JDRToggleButtonItem.java
// Description   : Toolbar buttons and menu item
// Creation Date : 19th May 2011
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
package com.dickimawbooks.jdrresources;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

/**
 * Application button and menu item.
 * @author Nicola L C Talbot
 */
public class JDRToggleButtonItem extends JCheckBoxMenuItem
   implements ActionListener
{
   public JDRToggleButtonItem(JDRResources resources,
      String parentID, String action, 
      ActionListener listener, boolean selected,
      JComponent buttonParent, JMenu menu)
   {
      super(resources.getString(
             parentID == null ? action : parentID+"."+action),
            selected);

      String menuID = (parentID == null ? action : parentID+"."+action);

      KeyStroke keyStroke = resources.getAccelerator(menuID);

      String tooltipText = resources.getString("tooltip."+action, null);

      setMnemonic(resources.getChar(menuID+".mnemonic"));
      setToolTipText(tooltipText);
      setAccelerator(keyStroke);
      addActionListener(this);
      setActionCommand(action);

      button = resources.createToggleButton(
         resources.getString("label."+action, getText()),
         action, this, keyStroke, tooltipText);

      button.setAlignmentX(Component.CENTER_ALIGNMENT);
      button.setAlignmentY(Component.CENTER_ALIGNMENT);

      button.setSelected(selected);

      buttonParent.add(button);

      menu.add(this);

      mainListener = listener;
   }

   public JDRToggleButtonItem(JDRResources resources, 
      String menuID, String name, 
      KeyStroke keyStroke, ActionListener listener,
      String tooltipText, boolean selected,
      JComponent buttonParent, JMenu menu)
   {
      super(resources.getString(menuID), selected);

      setMnemonic(resources.getChar(menuID+".mnemonic"));
      setToolTipText(tooltipText);
      setAccelerator(keyStroke);
      addActionListener(this);
      setActionCommand(name);

      button = resources.createToggleButton(
         resources.getString("label."+name, getText()),
         name, this, keyStroke, tooltipText);

      button.setAlignmentX(Component.CENTER_ALIGNMENT);
      button.setAlignmentY(Component.CENTER_ALIGNMENT);

      button.setSelected(selected);

      buttonParent.add(button);

      menu.add(this);

      mainListener = listener;
   }

   public JDRToggleButtonItem(JDRResources resources, 
      String menuID, String name, 
      KeyStroke keyStroke, ActionListener listener,
      String tooltipText, JComponent buttonParent, JMenu menu)
   {
      this(resources, menuID, name, keyStroke, listener, tooltipText,
         false, buttonParent, menu);
   }

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource();

      if (source == button)
      {
         setSelected(button.isSelected());
      }
      else
      {
         button.setSelected(isSelected());
      }

      if (mainListener != null)
      {
         mainListener.actionPerformed(evt);
      }
   }

   public void setEnabled(boolean flag)
   {
      button.setEnabled(flag);
      super.setEnabled(flag);
   }

   public void setSelected(boolean flag)
   {
      if (button != null)
      {
         button.setSelected(flag);
      }

      super.setSelected(flag);
   }

   public JDRToggleButton getButton()
   {
      return button;
   }

   private JDRToggleButton button;
   private ActionListener mainListener=null;
}
