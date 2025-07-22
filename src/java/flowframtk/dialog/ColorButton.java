// File          : ColorButton.java
// Description   : Button representing a colour swatch
// Creation Date : 6th February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2025 Nicola L.C. Talbot

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
package com.dickimawbooks.flowframtk.dialog;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

/**
 * Button representing colour swatch.
 * @author Nicola L C Talbot
 */

public class ColorButton extends JButton
{
    public ColorButton(Color color)
    {
       super("");
       setBackground(color);
    }

    public Dimension getPreferredSize()
    {
       return dimension;
    }

    public Dimension getMaximumSize()
    {
       return dimension;
    }

    public Dimension getMinimumSize()
    {
       return dimension;
    }

    public Dimension getSize()
    {
       return dimension;
    }

    public void paintBorder(Graphics g)
    {
       Graphics2D g2 = (Graphics2D)g;

       // Not all the L&F styles will fill the background
       g2.setColor(getBackground());
       g2.fillRect(0, 0, getWidth(), getWidth());

       if (!isFocusOwner())
       {
          g2.setColor(borderColor);
          g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
       }
    }

    private static Color borderColor = new Color(160,160,160);
    private static Dimension dimension = new Dimension(30,30);
}
