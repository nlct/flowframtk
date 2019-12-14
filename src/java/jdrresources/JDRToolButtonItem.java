// File          : JDRToolButtonItem.java
// Description   : Toolbar buttons and menu item
// Creation Date : 19th May 2011
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
package com.dickimawbooks.jdrresources;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

/**
 * Application button and menu item.
 * @author Nicola L C Talbot
 */
public class JDRToolButtonItem extends JRadioButtonMenuItem 
   implements ActionListener
{
   public JDRToolButtonItem(JDRResources resources,
      String parentID, String action, ActionListener listener,
      ToolButtonGroup group, JComponent buttonParent, JMenu menu)
   {
      this(resources, parentID, action, listener, false, 
           group, buttonParent, menu);
   }

   public JDRToolButtonItem(JDRResources resources,
      String parentID, String action, 
      ActionListener listener,
      boolean selected, ToolButtonGroup group, 
      JComponent buttonParent, JMenu menu)
   {
      super(resources.getString(
            parentID==null?action : parentID+"."+action),
            selected);

      String menuID = (parentID==null?action : parentID+"."+action);
      String tooltipText = resources.getString("tooltip."+action, null);

      KeyStroke keyStroke = resources.getAccelerator(menuID);

      setMnemonic(resources.getChar(menuID+".mnemonic"));
      setAccelerator(keyStroke);
      addActionListener(this);
      setToolTipText(tooltipText);
      setActionCommand(action);

      button = resources.createToolButton(
         resources.getString("label."+action, getText()),
         action, this, keyStroke,
         null, selected, tooltipText);

      button.setAlignmentX(Component.CENTER_ALIGNMENT);
      button.setAlignmentY(Component.CENTER_ALIGNMENT);

      buttonParent.add(button);

      menu.add(this);
      group.add(this);

      mainListener = listener;
   }

   public JDRToolButtonItem(JDRResources resources,
      String menuID, String name, 
      KeyStroke keyStroke, ActionListener listener,
      String tooltipText, boolean selected, ToolButtonGroup group,
      JComponent buttonParent, JMenu menu)
   {
      super(resources.getString(menuID), selected);

      button = resources.createToolButton(
         resources.getString("label."+name, getText()), name, this, keyStroke,
         null, selected, tooltipText);

      button.setAlignmentX(Component.CENTER_ALIGNMENT);
      button.setAlignmentY(Component.CENTER_ALIGNMENT);

      setMnemonic(resources.getChar(menuID+".mnemonic"));

      if (keyStroke != null)
      {
         setAccelerator(keyStroke);
      }

      if (tooltipText != null)
      {
         setToolTipText(tooltipText);
      }

      addActionListener(this);
      setActionCommand(name);

      buttonParent.add(button);

      menu.add(this);
      group.add(this);

      mainListener = listener;
   }

   public JDRToolButtonItem(JDRResources resources,
     String menuID, String name, 
      KeyStroke keyStroke, ActionListener listener,
      String tooltipText, 
      ToolButtonGroup group, JComponent buttonParent, JMenu menu)
   {
      this(resources, menuID, name, keyStroke, listener, tooltipText,
         false, group, buttonParent, menu);
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

   public JDRToolButton getButton()
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

   private JDRToolButton button;
   private ActionListener mainListener=null;
}
