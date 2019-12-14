// File          : RulerPanel.java
// Description   : Provides ruler panel for canvas
// Creation Date : 5th June 2008
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
package com.dickimawbooks.flowframtk;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;

/**
 * Provides ruler for canvas.
 */
public class RulerPanel extends JPanel
   implements SwingConstants,MouseMotionListener
{
   public RulerPanel(int dir, int w, int h, JDRCanvas canvas)
   {
      direction = dir;
      canvas_ = canvas;

      setPreferredSize(new Dimension(w, h));

      setBorder(BorderFactory.createEtchedBorder());
      addMouseMotionListener(this);

      oldP = new Point2D.Double(-MARKER_WIDTH, -MARKER_HEIGHT);

      Font f = canvas.getApplication().getSettings().getRulerFont();

      if (f == null)
      {
         canvas.getApplication().getSettings().setRulerFont(getFont());
      }
      else
      {
         setFont(f);
      }
   }

   public void updateFromBp(double x, double y)
   {
      repaintMarker(oldP.getX(), oldP.getY());
      oldP.setLocation(x, y);
      repaintMarker(oldP.getX(), oldP.getY());
   }

   public void updateFromStorage(double x, double y)
   {
      double factor = canvas_.getCanvasGraphics().storageToBp(1.0);
      updateFromBp(factor*x, factor*y);
   }

   public void updateFromComponent(double x, double y)
   {
      CanvasGraphics cg = canvas_.getCanvasGraphics();
      updateFromBp(cg.componentXToBp(x), cg.componentYToBp(y));
   }

   public void updateFromComponent(MouseEvent evt)
   {
      Point2D currentPos = canvas_.getNearestStorageTic(evt);

      updateFromStorage(currentPos.getX(), currentPos.getY());
   }

   public void mouseMoved(MouseEvent e)
   {
      updateFromComponent(e);
   }

   public void mouseEntered(MouseEvent e)
   {
      updateFromComponent(e);
   }

   public void mouseDragged(MouseEvent e)
   {
      updateFromComponent(e);
   }

   private void paintHorizontalComponent(Graphics2D g)
   {
      Dimension d = getPreferredSize();

      CanvasGraphics cg = canvas_.getCanvasGraphics();

      JDRGrid grid = canvas_.getGrid();

      Point2D majorBp = grid.getMajorTicDistance();
      Point2D minorBp = grid.getMinorTicDistance();

      int subDivisions = grid.getSubDivisions();

      int minorTicLength = (d.height*4)/10;
      int majorTicLength = (d.height*8)/10;

      Point2D originBp = new Point2D.Double();

      grid.toCartesianBp(new Point2D.Double(), originBp);

      JDRUnit unit = grid.getMainUnit();

      double scaleX = cg.bpToComponentX(1.0);

      Rectangle clip = g.getClipBounds();

      double minX = 0;
      double maxX = d.getWidth();

      if (clip != null)
      {
         minX = clip.getX();
         maxX = minX + clip.getWidth();
      }

      minX /= scaleX;
      maxX /= scaleX;

      int minIdx = (int)Math.floor((minX-originBp.getX())/majorBp.getX());

      minX = minIdx * majorBp.getX() + originBp.getX();

      maxX = Math.ceil((maxX-originBp.getX())/majorBp.getX())
           * majorBp.getX() + originBp.getX();

      double minorCompInc = minorBp.getX()*scaleX;

      for (double x = minX; x <= maxX; x += majorBp.getX())
      {
         double compX = Math.round(x*scaleX);

         g.drawLine((int)compX, 0, (int)compX, majorTicLength);

         g.drawString(format(unit.fromBp(minIdx*majorBp.getX())),
                       (int)compX+2, majorTicLength);

         minIdx++;

         for (int i = 1; i < subDivisions; i++)
         {
            compX += minorCompInc;
            g.drawLine((int)compX, 0, (int)compX, minorTicLength);
         }
      }

      // Marker

      double markerX = scaleX*oldP.getX();

      int markerMin = getMarkerMin(markerX);
      int markerMax = getMarkerMax(markerX);

      // Does it need drawing?

      if (markerMax >= minX*scaleX && markerMin <= maxX*scaleX)
      {
         drawHorizontalMarker(g, markerX, markerMin, markerMax); 
      }
   }

   private void paintVerticalComponent(Graphics2D g)
   {
      Dimension d = getPreferredSize();

      CanvasGraphics cg = canvas_.getCanvasGraphics();

      JDRGrid grid = canvas_.getGrid();

      Point2D majorBp = grid.getMajorTicDistance();
      Point2D minorBp = grid.getMinorTicDistance();

      int subDivisions = grid.getSubDivisions();

      int minorTicLength = (d.width*4)/10;
      int majorTicLength = (d.width*8)/10;

      Point2D originBp = new Point2D.Double();

      grid.toCartesianBp(new Point2D.Double(), originBp);

      JDRUnit unit = grid.getMainUnit();

      double scaleY = cg.bpToComponentY(1.0);

      Rectangle clip = g.getClipBounds();

      double minY = 0;
      double maxY = d.getHeight();

      if (clip != null)
      {
         minY = clip.getY();
         maxY = minY + clip.getHeight();
      }

      minY /= scaleY;
      maxY /= scaleY;

      int minIdx = (int)Math.floor((minY-originBp.getY())/majorBp.getY());

      minY = minIdx * majorBp.getY() + originBp.getY();

      maxY = Math.ceil((maxY-originBp.getY())/majorBp.getY())
           * majorBp.getY() + originBp.getY();

      double minorCompInc = minorBp.getY()*scaleY;

      FontRenderContext frc = g.getFontRenderContext();
      Font font = g.getFont();

      for (double y = minY; y <= maxY; y += majorBp.getY())
      {
         double compY = Math.round(y*scaleY);

         g.drawLine(0, (int)compY, majorTicLength, (int)compY);

         TextLayout layout = new TextLayout(
            format(unit.fromBp(minIdx*majorBp.getY())), font, frc);

         Rectangle2D bounds = layout.getBounds();

         double w = bounds.getWidth();

         layout.draw(g, (float)(d.getWidth()-bounds.getX()-w-4),
                        (float)(compY+2-bounds.getY()));

         minIdx++;

         for (int i = 1; i < subDivisions; i++)
         {
            compY += minorCompInc;
            g.drawLine(0, (int)compY, minorTicLength, (int)compY);
         }
      }

      // Marker

      double markerY = scaleY*oldP.getY();

      int markerMin = getMarkerMin(markerY);
      int markerMax = getMarkerMax(markerY);

      // Does it need drawing?

      if (markerMax >= minY*scaleY && markerMin <= maxY*scaleY)
      {
         drawVerticalMarker(g, markerY, markerMin, markerMax); 
      }
   }

   public void paintComponent(Graphics g)
   {
      Graphics2D g2 = (Graphics2D)g;

      AffineTransform orgAf = g2.getTransform();

      super.paintComponent(g);

      g2.setRenderingHints(canvas_.getFrame().getRenderingHints());

      if (direction == HORIZONTAL)
      {
         paintHorizontalComponent(g2);
      }
      else
      {
         paintVerticalComponent(g2);
      }

      g2.setTransform(orgAf);
   }

   private int getMarkerMin(double x)
   {
      return (int)Math.round(x-MARKER_WIDTH/2);
   }

   private int getMarkerMin(int x)
   {
      return x-MARKER_WIDTH/2;
   }

   private int getMarkerMax(double x)
   {
      return (int)Math.round(x+MARKER_WIDTH/2);
   }

   private int getMarkerMax(int x)
   {
      return x+MARKER_WIDTH/2;
   }

   private void drawHorizontalMarker(Graphics2D g, double x)
   {
      drawHorizontalMarker(g, (int)Math.round(x), 
         getMarkerMin(x),
         getMarkerMax(x));
   }

   private void drawHorizontalMarker(Graphics2D g, double x,
     int min, int max)
   {
      drawHorizontalMarker(g, (int)Math.round(x), 
         getMarkerMin(x),
         getMarkerMax(x));
   }

   private void drawHorizontalMarker(Graphics2D g, int x,
     int min, int max)
   {
      Polygon p = new Polygon();
      p.addPoint(min,0);
      p.addPoint(max,0);
      p.addPoint(x, MARKER_HEIGHT);
      g.fillPolygon(p);
   }

   private void drawVerticalMarker(Graphics2D g, double y)
   {
      drawVerticalMarker(g, (int)Math.round(y), 
         getMarkerMin(y),
         getMarkerMax(y));
   }

   private void drawVerticalMarker(Graphics2D g, double y,
     int min, int max)
   {
      drawVerticalMarker(g, (int)Math.round(y), 
         getMarkerMin(y),
         getMarkerMax(y));
   }

   private void drawVerticalMarker(Graphics2D g, int y,
     int min, int max)
   {
      Polygon p = new Polygon();
      p.addPoint(0, min);
      p.addPoint(0, max);
      p.addPoint(MARKER_HEIGHT, y);
      g.fillPolygon(p);
   }

   protected void drawMarker(Graphics g, double x, double y)
   {
      if (direction == SwingConstants.HORIZONTAL)
      {
         drawHorizontalMarker((Graphics2D)g, x);
      }
      else
      {
         drawVerticalMarker((Graphics2D)g, y);
      }
   }

   protected void repaintMarker(double x, double y)
   {
      Rectangle rect = null;

      CanvasGraphics cg = canvas_.getCanvasGraphics();

      Dimension d = getPreferredSize();

      Point2D majorDist = cg.getGrid().getMajorTicDistance();

      if (direction == SwingConstants.HORIZONTAL)
      {
         // Extend to the previous and following major tics to ensure the 
         // labels get repainted.

         int idx = (int)Math.floor(x/majorDist.getX());

         double closestMajorTic = 
            cg.bpToComponentX(majorDist.getX()*idx);

         int markerX = (int)Math.round(cg.bpToComponentX(x));

         int min = (int)Math.min(getMarkerMin(markerX), closestMajorTic);

         closestMajorTic = 
            cg.bpToComponentX(majorDist.getX()*(idx+2));

         int max = (int)Math.max(getMarkerMax(markerX), closestMajorTic);

         if (max > 0 && min < d.width)
         {
            rect = new Rectangle(min-1, 0,
                                 max-min+2, d.height);
         }
      }
      else
      {
         // Extend to the previous and following major tics to ensure the 
         // labels get repainted.

         int idx = (int)Math.floor(y/majorDist.getY());

         double closestMajorTic = 
            cg.bpToComponentY(majorDist.getY()*idx);

         int markerY = (int)Math.round(cg.bpToComponentY(y));

         int min = (int)Math.min(getMarkerMin(markerY), closestMajorTic);

         closestMajorTic = 
            cg.bpToComponentY(majorDist.getY()*(idx+2));

         int markerMax = (int)Math.max(getMarkerMax(markerY), closestMajorTic);

         if (markerMax > 0 && min < d.height)
         {
            rect = new Rectangle(0, min-1,
                                 d.width, markerMax-min+2);
         }
      }

      if (rect != null)
      {
         repaint(rect);
      }
   }

   protected String format(double number)
   {
      return canvas_.getApplication().getRulerFormat().format(number);
   }

   private int direction;
   public final static int MARKER_WIDTH=10; 
   public final static int MARKER_HEIGHT=10; 

   private Point2D oldP;
   private JDRCanvas canvas_;
}
