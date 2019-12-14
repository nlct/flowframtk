// File          : SampleCharPanel.java
// Description   : Sample Panel for displaying character
// Date          : 2012-03-12
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
package com.dickimawbooks.flowframtk.dialog;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.image.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Sample Panel for displaying character.
 * @author Nicola L C Talbot
 */

public class SampleCharPanel extends JPanel
   implements SamplePanel
{
   public SampleCharPanel(UnicodePanel unicodePanel)
   {
      super();

      this.unicodePanel = unicodePanel;
   }

   public void paintComponent(Graphics g)
   {
      Graphics2D g2 = (Graphics2D)g;

      super.paintComponent(g);

      RenderingHints hints = unicodePanel.getSymbolRenderingHints();

      if (hints != null)
      {
         g2.setRenderingHints(hints);
      }

      Font oldFont = g2.getFont();

      Font font = unicodePanel.getSymbolFont();

      if (font == null)
      {
         font = oldFont;
      }
      else
      {
         g2.setFont(font);
      }

      double factor = 8;

      Rectangle bounds = getBounds();

      g2.setPaint(Color.black);

      FontRenderContext frc = g2.getFontRenderContext();
      FontMetrics fm = g2.getFontMetrics(font);

      TextLayout layout = new TextLayout(unicodePanel.getSymbol(),
         font, frc);

      AffineTransform af = new AffineTransform();
      af.scale(factor, factor);

      Shape outline = layout.getOutline(af);

      Rectangle2D outlineBounds = outline.getBounds2D();

      af.setToIdentity();

      af.translate(0.5*(bounds.getWidth()
                       -outlineBounds.getWidth())
                   -outlineBounds.getX(),
                   0.5*(bounds.getHeight()
                       -outlineBounds.getHeight())
                   -outlineBounds.getY());

      g2.fill(af.createTransformedShape(outline));

      g2.setFont(oldFont);
   }

   public void updateSamples()
   {
      repaint();
   }

   private UnicodePanel unicodePanel;
}
