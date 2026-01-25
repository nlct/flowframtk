// File          : SampleTextPanel.java
// Description   : Panel for displaying sample text
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
import java.awt.font.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Panel for displaying sample text.
 * @author Nicola L C Talbot
 */

public class SampleTextPanel extends JPanel implements SamplePanel
{
   public SampleTextPanel(JDRSelector chooserPanel)
   {
      this(chooserPanel, false);
   }

   public SampleTextPanel(JDRSelector chooserPanel, boolean showAnchor)
   {
      super();
      setBackground(Color.white);

      this.showAnchor = showAnchor;

      cg = new CanvasGraphics();

      BevelBorder border = new BevelBorder(BevelBorder.LOWERED,
         new Color(154,154,154), Color.darkGray);

      setBorder(border);

      panel = chooserPanel;

      sampleText = new JDRText(cg);

      matrix = new double[]{1, 0, 0, 1, 0, 0};

      Dimension dim = getPreferredSize();

      dim.height = Math.max(dim.height, 100);

      setPreferredSize(dim);
   }

   public String getSampleText()
   {
      return panel.getSampleText();
   }

   public void paintComponent(Graphics g)
   {
      super.paintComponent(g);

      String text = sampleText.getText();

      if (text == null || text.length() == 0)
      {
         return;
      }

      Graphics2D g2 = (Graphics2D)g;

      cg.setGraphicsDevice(g2);

      updateStyle();

      Dimension dim = getSize();

      RenderingHints oldHints = g2.getRenderingHints();
      g2.setRenderingHints(panel.getRenderingHints());

      matrix[0] = 1;
      matrix[3] = 1;

      sampleText.setTransformation(matrix);

      BBox box = sampleText.getStorageBBox();

      // Shift so that it's centred

      sampleText.setPosition((0.5*dim.width-box.getMidX()),
                             (0.5*dim.height-box.getMidY()));

      if (showAnchor)
      {
         sampleText.drawWithAnchors(Color.red);
      }
      else
      {
         sampleText.draw();
      }

      g2.setRenderingHints(oldHints);

      cg.setGraphicsDevice(null);
   }

   public void updateSamples()
   {
      Graphics2D orgG = cg.getGraphics();

      Graphics2D g2 = (Graphics2D)getGraphics();

      cg.setGraphicsDevice(g2);
      updateStyle();
      cg.setGraphicsDevice(orgG);

      if (g2 != null)
      {
         g2.dispose();

         validate();
         repaint();
      }
   }

   protected void updateStyle()
   {
      sampleText.setText(getSampleText());

      JDRPaint paint = (JDRPaint)panel.getTextPaint().clone();
      paint.applyCanvasGraphics(cg);

      sampleText.setTextPaint(paint);

      if (panel.isTextOutline())
      {
         sampleText.setOutlineMode(true);
         paint = (JDRPaint)panel.getOutlineFillPaint().clone();
         paint.applyCanvasGraphics(cg);

         sampleText.setOutlineFillPaint(paint);
      }
      else
      {
         sampleText.setOutlineMode(false);
      }

      sampleText.setFontFamily(panel.getFontName());
      sampleText.setFontSize(panel.getFontSize());
      sampleText.setFontSeries(panel.getFontSeries());
      sampleText.setFontShape(panel.getFontShape());
      sampleText.setHAlign(panel.getHalign());
      sampleText.setVAlign(panel.getValign());
   }

   public BBox getBBox()
   {
      return sampleText.getStorageBBox();
   }

/*
   public Dimension getPreferredSize()
   {
      if (panel == null || sampleText == null)
      {
         return super.getPreferredSize();
      }

      BBox box = getBBox();

      double norm = JDRUnit.getNormalizingFactor();

      return new Dimension((int)Math.ceil(box.getWidth()*norm)+20,
         (int)Math.ceil(box.getHeight()*norm)+20);
   }
*/

   private JDRSelector panel;
   private JDRText sampleText;

   private double[] matrix;

   private boolean showAnchor = false;

   private CanvasGraphics cg;
}
