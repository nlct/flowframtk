// File          : SamplePathPanel.java
// Description   : Panel for displaying sample paths
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
import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Panel for displaying sample paths.
 * @author Nicola L C Talbot
 */

public class SamplePathPanel extends JPanel implements SamplePanel
{
   public SamplePathPanel(JDRSelector chooserPanel)
   {
      this(chooserPanel, true);
   }

   public SamplePathPanel(JDRSelector chooserPanel, boolean vertical)
   {
      super();
      setBackground(Color.white);

      cg = new CanvasGraphics(chooserPanel.getResources().getMessageSystem());

      BevelBorder border = new BevelBorder(BevelBorder.LOWERED,
         new Color(154,154,154), Color.darkGray);

      setBorder(border);

      panel      = chooserPanel;
      path = new JDRPath[MAX_PATHS];

      int xoffset=vertical?0:100;
      int yoffset=vertical?60:0;

      int x=0;
      int y=vertical?-120:-140;

      try
      {
         path[0] = new JDRPath(cg);
         path[0].add(new JDRLine(cg, new Point(20+x, 200+y),
                              new Point(60+x, 150+y)));
         path[0].add(new JDRLine(cg, new Point(60+x, 150+y),
                              new Point(80+x, 180+y)));

         x += xoffset;
         y += yoffset;
   
         path[1] = new JDRPath(cg);
         path[1].add(new JDRLine(cg, new Point(20+x, 200+y),
                              new Point(40+x, 160+y)));
         path[1].add(new JDRLine(cg, new Point(40+x, 160+y),
                              new Point(70+x, 220+y)));
         path[1].add(new JDRLine(cg, new Point(70+x, 220+y),
                              new Point(80+x, 180+y)));
         path[1].close();

         x += xoffset;
         y += yoffset;
   
         path[2] = new JDRPath(cg);
         path[2].add(new JDRBezier(cg, new Point(20+x, 200+y), 
                                new Point(50+x, 170+y),
                                new Point(50+x, 230+y),
                                new Point(80+x, 200+y)));
   
         x += xoffset;
         y += yoffset;
   
         path[3] = new JDRPath(cg);
         path[3].add(new JDRBezier(cg, new Point(20+x, 200+y), 
                                new Point(20+x, 180+y),
                                new Point(50+x, 180+y),
                                new Point(50+x, 200+y)));
         path[3].add(new JDRBezier(cg, new Point(50+x, 200+y), 
                                new Point(50+x, 220+y),
                                new Point(80+x, 220+y),
                                new Point(80+x, 200+y)));
         path[3].add(new JDRBezier(cg, new Point(80+x, 200+y), 
                                new Point(80+x, 180+y),
                                new Point(50+x, 180+y),
                                new Point(50+x, 200+y)));
         path[3].close(new JDRBezier(cg, new Point(50+x, 200+y),
                                  new Point(50+x, 220+y),
                                  new Point(20+x, 220+y),
                                  new Point(20+x, 100+y)));
   
         x += xoffset;
         y += yoffset;
   
         path[4] = new JDRPath(cg);
         path[4].add(new JDRLine(cg, new Point(20+x, 180+y),
                              new Point(80+x, 180+y)));
         path[4].add(new JDRLine(cg, new Point(80+x, 180+y),
                              new Point(80+x, 220+y)));
         path[4].add(new JDRLine(cg, new Point(80+x, 220+y),
                              new Point(20+x, 220+y)));
         path[4].close();
   
         x += xoffset;
         y += yoffset;
   
         path[5] = new JDRPath(cg);
         path[5].add(new JDRLine(cg, new Point(20+x, 180+y),
                              new Point(80+x, 180+y)));
         path[5].add(new JDRLine(cg, new Point(80+x, 180+y),
                              new Point(80+x, 220+y)));
         path[5].add(new JDRLine(cg, new Point(80+x, 220+y),
                              new Point(20+x, 220+y)));
         path[5].add(new JDRLine(cg, new Point(20+x, 220+y),
                              new Point(20+x, 180+y)));
         path[5].add(new JDRSegment(cg, new Point(20+x, 180+y),
                                 new Point(30+x, 190+y)));
         path[5].add(new JDRLine(cg, new Point(30+x, 190+y),
                              new Point(45+x, 190+y)));
         path[5].add(new JDRLine(cg, new Point(45+x, 190+y),
                              new Point(45+x, 210+y)));
         path[5].add(new JDRLine(cg, new Point(45+x, 210+y),
                              new Point(30+x, 210+y)));
         path[5].add(new JDRLine(cg, new Point(30+x, 210+y),
                              new Point(30+x, 190+y)));
         path[5].add(new JDRSegment(cg, new Point(30+x, 190+y),
                                 new Point(55+x, 190+y)));
         path[5].add(new JDRLine(cg, new Point(55+x, 190+y),
                              new Point(55+x, 210+y)));
         path[5].add(new JDRLine(cg, new Point(55+x, 210+y),
                              new Point(70+x, 210+y)));
         path[5].add(new JDRLine(cg, new Point(70+x, 210+y),
                              new Point(70+x, 190+y)));
         path[5].add(new JDRLine(cg, new Point(70+x, 190+y),
                              new Point(55+x, 190+y)));

         x += xoffset;
         y += yoffset;
   
         path[6] = new JDRPath(cg);

         if (vertical)
         {
            path[6].add(new JDRLine(cg, new Point(20+x, 200+y),
               new Point(80+x, 200+y)));
         }
         else
         {
            path[6].add(new JDRLine(cg, new Point(20+x, 180+y),
               new Point(20+x, 220+y)));
         }
      }
      catch (InvalidPathException e)
      {
         // This shouldn't happen
         getResources().internalError(this, e);
      }

      Dimension dim = getPreferredSize();

      if (vertical)
      {
         dim.width = Math.max(dim.width, 150);
         dim.height = Math.max(dim.height, 400);
      }
      else
      {
         dim.height = Math.max(dim.height, 150);
         dim.width = Math.max(dim.width, 650);
      }

      setPreferredSize(dim);
   }

   public void paintComponent(Graphics g)
   {
      super.paintComponent(g);

      Graphics2D g2 = (Graphics2D)g;

      cg.setGraphicsDevice(g2);

      BBox box = getBBox();

      if (box == null)
      {
         return;
      }

      Dimension dim = getSize();

      Stroke oldStroke = g2.getStroke();

      RenderingHints oldHints = g2.getRenderingHints();
      g2.setRenderingHints(panel.getRenderingHints());

      AffineTransform oldAf = g2.getTransform();

      // Shift so that the paths are centred

      g2.translate(0.5*dim.width-box.getMidX(),
                   0.5*dim.height-box.getMidY());

      for (int i = 0; i < MAX_PATHS; i++)
      {
         path[i].draw(false);
      }

      g2.setRenderingHints(oldHints);
      g2.setStroke(oldStroke);
      g2.setTransform(oldAf);
      cg.setGraphicsDevice(null);

   }

   public BBox getBBox()
   {
      if (panel == null || path == null || path[0] == null
          || panel.getStroke() == null)
      {
         return null;
      }

      BBox box = null;

      for (int i = 0; i < MAX_PATHS; i++)
      {
         if (path[i].getStroke() == null)
         {
            path[i].setStroke(panel.getStroke());
         }

         if (box == null)
         {
            box = path[i].getStorageBBox();
         }
         else
         {
            path[i].mergeStorageBBox(box);
         }

      }

      return box;
   }

   public void updateSamples()
   {
      if (panel == null)
      {
         return;
      }

      JDRPaint lineColour = (JDRPaint)panel.getLinePaint().clone();
      JDRPaint fillColour = (JDRPaint)panel.getFillPaint().clone();
      JDRStroke stroke    = (JDRStroke)panel.getStroke().clone();

      lineColour.applyCanvasGraphics(cg);
      fillColour.applyCanvasGraphics(cg);
      stroke.applyCanvasGraphics(cg);

      for (int i = 0; i < MAX_PATHS; i++)
      {
         path[i].setLinePaint(lineColour);
         path[i].setFillPaint(fillColour);
         path[i].setStroke(stroke);
      }

      validate();
      repaint();
   }

/*
   public Dimension getPreferredSize()
   {
      if (path == null || panel == null)
      {
         return super.getPreferredSize();
      }

      BBox box = getBBox();

      int w = 150;
      int h = Integer.MAX_VALUE;

      if (box != null)
      {
         w = (int)Math.ceil(box.getWidth())+10;
         h = (int)Math.ceil(box.getHeight())+10;
      }

      return new Dimension(w , h);
   }
*/

   public JDRResources getResources()
   {
      return panel.getResources();
   }

   private JDRSelector panel;
   private JDRPath[] path;
   private final static int MAX_PATHS=7;

   private CanvasGraphics cg;
}
