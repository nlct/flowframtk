// File          : ToolButtonGroup.java
// Description   : Group for tool buttons
// Date          : 2014-04-26
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

import java.util.Vector;
import javax.swing.ButtonGroup;

public class ToolButtonGroup extends Vector<JDRToolButtonItem>
{
   public ToolButtonGroup()
   {
      super();
      buttonGroup = new ButtonGroup();
      menuGroup = new ButtonGroup();
   }

   public boolean add(JDRToolButtonItem buttonItem)
   {
      buttonGroup.add(buttonItem.getButton());
      menuGroup.add(buttonItem);
      return super.add(buttonItem);
   }

   public JDRToolButtonItem getButtonItem(String commandName)
   {
      for (int i = 0, n = size(); i < n; i++)
      {
         JDRToolButtonItem item = get(i);

         if (commandName.equals(item.getActionCommand()))
         {
            return item;
         }
      }

      return null;
   }

   private ButtonGroup buttonGroup, menuGroup;
}
