// File          : DirectionButton.java
// Description   : Button representing direction
// Date          : 6th February 2006
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

import javax.swing.*;

/**
 * Button representing direction.
 * @author Nicola L C Talbot
 */

public class DirectionButton extends JRadioButton
{
   public DirectionButton(Icon icon, Icon selectedIcon, int direction,
      boolean selected)
   {
      super(icon, selected);

      if (selectedIcon != null)
      {
         setSelectedIcon(selectedIcon);
      }

      direction_ = direction;
      setMargin(new Insets(0,0,0,0));
   }

   public DirectionButton(Icon icon, Icon selectedIcon, int direction)
   {
      this(icon, selectedIcon, direction, false);
   }

   public int getDirection()
   {
      return direction_;
   }

   public void paintBorder(Graphics g)
   {
      Graphics2D g2 = (Graphics2D)g;
      if (isFocusOwner())
      {
         g2.setColor(borderColor);
         g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
      }
   }

   private int direction_;
   private static Color borderColor = new Color(160,160,160);
}
