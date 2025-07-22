// File          : CharacterButton.java
// Description   : Button representing symbol or character
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
import java.awt.geom.*;
import java.awt.font.*;

import javax.swing.*;

/**
 * Button representing symbol or character.
 * @see ArrowStyleSelector
 * @author Nicola L C Talbot
 */

public class CharacterButton extends JButton
{
   public CharacterButton(int code, SymbolSelectorListener symList)
   {
      super();

      codePoint = code;
      characterString = new String(new int[]{code}, 0, 1);

      hexString = String.format("%05X", code);
      setPreferredSize(new Dimension(WIDTH,HEIGHT));

      listener = symList;

      String name = Character.getName(code);

      if (name == null)
      {
         setEnabled(false);
      }
      else
      {
         setToolTipText("U+"+hexString+" "+name);
      }
   }

   public void paintComponent(Graphics g)
   {
      if (!isEnabled())
      {
         super.paintComponent(g);
         return;
      }

      Graphics2D g2 = (Graphics2D)g;
      Paint oldPaint = g2.getPaint();
      Font oldFont = g2.getFont();

      Font font = listener.getSymbolButtonFont();

      if (font == null)
      {
         font = oldFont;
      }
      else
      {
         g2.setFont(font);
      }

      RenderingHints oldHints = g2.getRenderingHints();
      RenderingHints newHints = listener.getRenderingHints();

      if (newHints != null)
      {
         g2.setRenderingHints(newHints);
      }

      Rectangle bounds = getBounds();
      int w = (int)bounds.getWidth();
      int h = (int)bounds.getHeight();

      g2.setPaint(Color.white);
      g2.fillRect(0, 0, w, h);

      g2.setPaint(Color.black);
      FontRenderContext frc = g2.getFontRenderContext();
      FontMetrics fm = g2.getFontMetrics(font);

      TextLayout layout = new TextLayout(characterString,
         font, frc);

      Rectangle2D layoutBounds = layout.getBounds();

      double x = 0.5*bounds.getWidth()
               - 0.5*layoutBounds.getWidth();

      double y = bounds.getHeight() - fm.getMaxDescent() - 4;

      layout.draw(g2, (float)x, (float)y);

      if (hasFocus())
      {
         g2.setPaint(Color.gray);
         g2.drawRect(2, 2, w-4, h-4);
      }

      g2.setPaint(oldPaint);
      if (newHints != null) g2.setRenderingHints(oldHints);
   }

   public int getCodePoint()
   {
      return codePoint;
   }

   public String getHexString()
   {
      return hexString;
   }

   public static int getMaxHeight()
   {
      return HEIGHT;
   }

   public String getString()
   {
      return characterString;
   }

   private int codePoint;

   private String characterString, hexString;

   private SymbolSelectorListener listener;

   public static final int WIDTH=40, HEIGHT=40;
}
