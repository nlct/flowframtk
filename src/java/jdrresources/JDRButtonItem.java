// File        : JDRButtonItem.java
// Description : Toolbar buttons and menu item
// Date        : 19th May 2011
// Author      : Nicola L.C. Talbot
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
public class JDRButtonItem extends JMenuItem
{
   public JDRButtonItem(JDRResources resources, String parentId, String action, 
      ActionListener listener, JComponent buttonParent, JComponent menu)
   {
      super(resources.getString(parentId == null ? action : parentId+"."+action));

      String menuID = (parentId == null ? action : parentId+"."+action);

      KeyStroke keyStroke = resources.getAccelerator(menuID);

      String tooltipText = resources.getString("tooltip."+action, null);

      button = resources.createAppButton(
         resources.getString("label."+menuID, getText()), 
         menuID, listener, keyStroke, tooltipText);

      button.setAlignmentX(Component.CENTER_ALIGNMENT);
      button.setAlignmentY(Component.CENTER_ALIGNMENT);

      buttonParent.add(button);

      addActionListener(listener);
      setActionCommand(action);
      setAccelerator(keyStroke);
      setMnemonic(resources.getCodePoint(menuID+".mnemonic"));
      setToolTipText(tooltipText);

      menu.add(this);
   }

   public JDRButtonItem(JDRResources resources, String menuID, String name, 
      KeyStroke keyStroke, ActionListener listener,
      String tooltipText, JComponent buttonParent, JComponent menu)
   {
      super(resources.getString(menuID));
      button = resources.createAppButton(
         resources.getString("label."+name, getText()), name, listener, keyStroke,
         tooltipText);

      button.setAlignmentX(Component.CENTER_ALIGNMENT);
      button.setAlignmentY(Component.CENTER_ALIGNMENT);

      buttonParent.add(button);

      setMnemonic(resources.getCodePoint(menuID+".mnemonic"));
      setAccelerator(keyStroke);
      setActionCommand(name);
      setToolTipText(tooltipText);
      addActionListener(listener);
      menu.add(this);
   }

   public void setEnabled(boolean flag)
   {
      button.setEnabled(flag);
      super.setEnabled(flag);
   }

   public void setSelected(boolean flag)
   {
      button.setSelected(flag);
      super.setSelected(flag);
   }

   public JDRButton getButton()
   {
      return button;
   }

   public int getButtonWidth()
   {
      return button.getWidth();
   }

   public int getButtonHeight()
   {
      return button.getHeight();
   }

   public int getButtonPreferredWidth()
   {
      return button.getPreferredSize().width;
   }

   public int getButtonPreferredHeight()
   {
      return button.getPreferredSize().height;
   }

   private JDRButton button;
}
