// File          : JDRRectangularGrid.java
// Description   : Represents an isometric grid.
// Author        : Nicola L.C. Talbot
// Creation Date : 2014-06-06
//              http://www.dickimaw-books.com/

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
package com.dickimawbooks.jdr;

import java.awt.*;
import java.awt.geom.*;
import java.text.*;

import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.*;

/**
 * Class representing an isometric grid. (Left-handed Cartesian.)
 * The origin is the top left corner of the page.
 * @author Nicola L C Talbot
 */
public class JDRIsoGrid extends JDRGrid
{
   /**
    * Initialises using the default settings. The defaults are:
    * bp units, major interval width of 100 units and 10
    * sub-divisions per major interval.
    */
   public JDRIsoGrid(CanvasGraphics cg)
   {
      super(cg, GRID_ISO);
      set(JDRUnit.bp, 100.0, 10);
   }

   /**
    * Initialises using the given settings.
    * @param gridUnit the grid units
    * @param majorDiv the width of the major interval in terms of
    * the grid unit
    * @param subDiv the number of sub-divisions per major interval
    */
   public JDRIsoGrid(CanvasGraphics cg,
      JDRUnit gridUnit, double majorDiv, int subDiv)
   {
      super(cg, GRID_ISO);
      set(gridUnit, majorDiv, subDiv);
   }

   public Object clone()
   {
      return new JDRIsoGrid(getCanvasGraphics(),
         unit, majorDivisions, subDivisions);
   }

   /**
    * Change the grid settings settings.
    * @param gridUnit the grid units
    * @param majorDiv the width of the major interval in terms of
    * the grid unit
    * @param subDiv the number of sub-divisions per major interval
    */
   public void set(JDRUnit gridUnit, double majorDiv, int subDiv)
   {
      setUnit(gridUnit);
      setMajorInterval(majorDiv);
      setSubDivisions(subDiv);
   }

   /**
    * Sets the grid unit.
    * @param gridUnit the grid units
    */
   public void setUnit(JDRUnit gridUnit)
   {
      unit = gridUnit;
   }

   /**
    * Sets the major interval width.
    * @param majorDiv the major interval width in terms of the
    * grid's unit, which must be greater than 0
    */
   public void setMajorInterval(double majorDiv)
   {
      if (majorDiv <= 0.0)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.GRID_MAJOR, majorDiv, 
           getCanvasGraphics());
      }

      majorDivisions = majorDiv;
   }

   /**
    * Sets the number of sub divisions within the major interval.
    * @param subDiv the number of sub divisions, which must be &gt;=0.
    */
   public void setSubDivisions(int subDiv)
   {
      if (subDiv < 0)
      {
         throw new JdrIllegalArgumentException(
          JdrIllegalArgumentException.GRID_SUBDIVISIONS, subDiv,
          getCanvasGraphics());
      }

      subDivisions = subDiv;
   }

   /**
    * Gets the grid unit.
    * @return the grid unit
    */
   public JDRUnit getUnit()
   {
      return unit;
   }

   /**
    * Gets the width of the major intervals.
    * @return the width of the major intervals in terms of the
    * grid's unit.
    */
   public double getMajorInterval()
   {
      return majorDivisions;
   }

   /**
    * Gets the number of sub divisions within a major interval.
    * @return the number of sub divisions.
    */
   public int getSubDivisions()
   {
      return subDivisions;
   }

   public Point2D getMajorTicDistance()
   {
      double length = unit.toBp(majorDivisions);

      return new Point2D.Double(JDRConstants.HALF_ROOT_3 * length, length);
   }

   public Point2D getMinorTicDistance()
   {
      Point2D p = new Point2D.Double(0, 0);

      if (subDivisions > 0)
      {
         double length = unit.toBp(majorDivisions)/subDivisions;

         p.setLocation(JDRConstants.HALF_ROOT_3 * length, length);
      }

      return p;
   }

   public Point2D fromCartesianBp(double x, double y)
   {
      return new Point2D.Double(unit.fromBp(x), unit.fromBp(y));
   }

   public void fromCartesianBp(Point2D cartesianPoint, Point2D target)
   {
      target.setLocation(unit.fromBp(cartesianPoint.getX()),
                         unit.fromBp(cartesianPoint.getY()));
   }

   public void toCartesianBp(Point2D original, Point2D target)
   {
      target.setLocation(unit.toBp(original.getX()),
                         unit.toBp(original.getY()));
   }

   @Override
   public Point2D getDefaultOffset()
   {
      Point2D p = getMinorTicDistance();
      return getClosestBpTic(p.getX(), p.getY(), p);
   }

   // x, y and return values in bp
   public Point2D getClosestBpTic(double x, double y)
   {
      return getClosestBpTic(x, y, null);
   }

   public Point2D getClosestBpTic(double x, double y, Point2D p)
   {
      double length = unit.toBp(majorDivisions);

      double majorX = JDRConstants.HALF_ROOT_3 * length;

      double halfLength = 0.5 * length;

      int xIdx = (int)Math.floor(x/majorX);

      double x0 = xIdx * majorX;

      boolean isOdd = ((xIdx%2)==1);

      int yIdx = (int)Math.floor(y/halfLength);

      double y0 = halfLength * yIdx;

      double x1 = x0 + majorX;
      double y1 = y0 + length;
      double yinc = y0 + halfLength;
      double ydec = y0 - halfLength;

      Path2D.Double rightTriangle = new Path2D.Double();
      Path2D.Double leftTriangle = new Path2D.Double();

      if (isOdd)
      {
         if ((yIdx%2) == 1)
         {
            rightTriangle.moveTo(x0, y0);
            rightTriangle.lineTo(x0, y1);
            rightTriangle.lineTo(x1, yinc);
            rightTriangle.closePath();

            leftTriangle.moveTo(x0, y0);
            leftTriangle.lineTo(x1, yinc);
            leftTriangle.lineTo(x1, ydec);
            leftTriangle.closePath();
         }
         else
         {
            rightTriangle.moveTo(x0, ydec);
            rightTriangle.lineTo(x0, yinc);
            rightTriangle.lineTo(x1, y0);
            rightTriangle.closePath();

            leftTriangle.moveTo(x0, yinc);
            leftTriangle.lineTo(x1, y1);
            leftTriangle.lineTo(x1, y0);
            leftTriangle.closePath();
         }
      }
      else
      {
         if ((yIdx%2) == 0)
         {
            rightTriangle.moveTo(x0, y0);
            rightTriangle.lineTo(x0, y1);
            rightTriangle.lineTo(x1, yinc);
            rightTriangle.closePath();

            leftTriangle.moveTo(x0, y0);
            leftTriangle.lineTo(x1, yinc);
            leftTriangle.lineTo(x1, ydec);
            leftTriangle.closePath();
         }
         else
         {
            rightTriangle.moveTo(x0, ydec);
            rightTriangle.lineTo(x0, yinc);
            rightTriangle.lineTo(x1, y0);
            rightTriangle.closePath();

            leftTriangle.moveTo(x0, yinc);
            leftTriangle.lineTo(x1, y1);
            leftTriangle.lineTo(x1, y0);
            leftTriangle.closePath();
         }
      }

      PathIterator pi;

      if (rightTriangle.contains(x, y))
      {
         pi = rightTriangle.getPathIterator(null);
      }
      else
      {
         pi = leftTriangle.getPathIterator(null);
      }


      double[] coords = new double[6];

      // First vertex

      pi.currentSegment(coords);

      double dx = x - coords[0];
      double dy = y - coords[1];

      double minDiff = dx*dx + dy*dy;

      double nearestX = coords[0];
      double nearestY = coords[1];

      // Second vertex

      pi.next();

      pi.currentSegment(coords);

      dx = x - coords[0];
      dy = y - coords[1];

      double diff = dx*dx + dy*dy;

      double secondNearestX, secondNearestY, secondMinDiff;

      if (diff < minDiff)
      {
         secondNearestX = nearestX;
         secondNearestY = nearestY;
         secondMinDiff = minDiff;

         nearestX = coords[0];
         nearestY = coords[1];
         minDiff = diff;
      }
      else
      {
         secondNearestX = coords[0];
         secondNearestY = coords[1];
         secondMinDiff = diff;
      }

      // Third vertex

      pi.next();

      pi.currentSegment(coords);

      dx = x - coords[0];
      dy = y - coords[1];

      diff = dx*dx + dy*dy;

      if (diff < minDiff)
      {
         secondNearestX = nearestX;
         secondNearestY = nearestY;
         secondMinDiff = minDiff;

         nearestX = coords[0];
         nearestY = coords[1];
      }
      else if (diff < secondMinDiff)
      {
         secondNearestX = coords[0];
         secondNearestY = coords[1];
         secondMinDiff = diff;
      }

      if (subDivisions > 0)
      {
         double minorLength = length/(double)subDivisions;

         double delta = nearestX - secondNearestX;

         double inv = 1.0/delta;

         if (Double.isInfinite(inv) || Double.isNaN(inv))
         {
            double lowerY, upperY;
            double offset = y - nearestY;

            if (nearestY < secondNearestY)
            {
               lowerY = nearestY + minorLength*Math.floor(offset/minorLength);
               upperY = lowerY + minorLength;
            }
            else
            {
               upperY = nearestY + minorLength*Math.floor(offset/minorLength);
               lowerY = upperY - minorLength;
            }

            if (y - lowerY < upperY - y)
            {
               nearestY = lowerY;
            }
            else
            {
               nearestY = upperY;
            }
         }
         else
         {
            int n = subDivisions;

            double ax, ay, bx, by;

            int min, max;

            if (nearestX < secondNearestX)
            {
               ax = nearestX;
               ay = nearestY;
               bx = secondNearestX;
               by = secondNearestY;
               min = 1;
               max = n/2;
            }
            else
            {
               ax = secondNearestX;
               ay = secondNearestY;
               bx = nearestX;
               by = nearestY;
               min = n/2;
               max = n-1;
            }

            for (int t = min; t <= max; t++)
            {
               double currentX = (ax*(n-t))/(double)n
                               + (bx*t)/(double)n;
               double currentY = (ay*(n-t))/(double)n
                               + (by*t)/(double)n;

               dx = currentX - x;
               dy = currentY - y;

               diff = dx*dx + dy*dy;

               if (diff < minDiff)
               {
                  nearestX = currentX;
                  nearestY = currentY;
                  minDiff = diff;
               }
            }
         }
      }

      if (p == null)
      {
         p = new Point2D.Double(nearestX, nearestY);
      }
      else
      {
         p.setLocation(nearestX, nearestY);
      }

      return p;
   }

   public Point2D getClosestTic(double x, double y)
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double length = unit.toUnit(majorDivisions, storageUnit);

      double majorX = JDRConstants.HALF_ROOT_3 * length;

      double halfLength = 0.5 * length;

      int xIdx = (int)Math.floor(x/majorX);

      double x0 = xIdx * majorX;

      boolean isOdd = ((xIdx%2)==1);

      int yIdx = (int)Math.floor(y/halfLength);

      double y0 = halfLength * yIdx;

      double x1 = x0 + majorX;
      double y1 = y0 + length;
      double yinc = y0 + halfLength;
      double ydec = y0 - halfLength;

      Path2D.Double rightTriangle = new Path2D.Double();
      Path2D.Double leftTriangle = new Path2D.Double();

      if (isOdd)
      {
         if ((yIdx%2) == 1)
         {
            rightTriangle.moveTo(x0, y0);
            rightTriangle.lineTo(x0, y1);
            rightTriangle.lineTo(x1, yinc);
            rightTriangle.closePath();

            leftTriangle.moveTo(x0, y0);
            leftTriangle.lineTo(x1, yinc);
            leftTriangle.lineTo(x1, ydec);
            leftTriangle.closePath();
         }
         else
         {
            rightTriangle.moveTo(x0, ydec);
            rightTriangle.lineTo(x0, yinc);
            rightTriangle.lineTo(x1, y0);
            rightTriangle.closePath();

            leftTriangle.moveTo(x0, yinc);
            leftTriangle.lineTo(x1, y1);
            leftTriangle.lineTo(x1, y0);
            leftTriangle.closePath();
         }
      }
      else
      {
         if ((yIdx%2) == 0)
         {
            rightTriangle.moveTo(x0, y0);
            rightTriangle.lineTo(x0, y1);
            rightTriangle.lineTo(x1, yinc);
            rightTriangle.closePath();

            leftTriangle.moveTo(x0, y0);
            leftTriangle.lineTo(x1, yinc);
            leftTriangle.lineTo(x1, ydec);
            leftTriangle.closePath();
         }
         else
         {
            rightTriangle.moveTo(x0, ydec);
            rightTriangle.lineTo(x0, yinc);
            rightTriangle.lineTo(x1, y0);
            rightTriangle.closePath();

            leftTriangle.moveTo(x0, yinc);
            leftTriangle.lineTo(x1, y1);
            leftTriangle.lineTo(x1, y0);
            leftTriangle.closePath();
         }
      }

      PathIterator pi;

      if (rightTriangle.contains(x, y))
      {
         pi = rightTriangle.getPathIterator(null);
      }
      else
      {
         pi = leftTriangle.getPathIterator(null);
      }


      double[] coords = new double[6];

      // First vertex

      pi.currentSegment(coords);

      double dx = x - coords[0];
      double dy = y - coords[1];

      double minDiff = dx*dx + dy*dy;

      double nearestX = coords[0];
      double nearestY = coords[1];

      // Second vertex

      pi.next();

      pi.currentSegment(coords);

      dx = x - coords[0];
      dy = y - coords[1];

      double diff = dx*dx + dy*dy;

      double secondNearestX, secondNearestY, secondMinDiff;

      if (diff < minDiff)
      {
         secondNearestX = nearestX;
         secondNearestY = nearestY;
         secondMinDiff = minDiff;

         nearestX = coords[0];
         nearestY = coords[1];
         minDiff = diff;
      }
      else
      {
         secondNearestX = coords[0];
         secondNearestY = coords[1];
         secondMinDiff = diff;
      }

      // Third vertex

      pi.next();

      pi.currentSegment(coords);

      dx = x - coords[0];
      dy = y - coords[1];

      diff = dx*dx + dy*dy;

      if (diff < minDiff)
      {
         secondNearestX = nearestX;
         secondNearestY = nearestY;
         secondMinDiff = minDiff;

         nearestX = coords[0];
         nearestY = coords[1];
      }
      else if (diff < secondMinDiff)
      {
         secondNearestX = coords[0];
         secondNearestY = coords[1];
         secondMinDiff = diff;
      }

      if (subDivisions > 0)
      {
         double minorLength = length/(double)subDivisions;

         double delta = nearestX - secondNearestX;

         double inv = 1.0/delta;

         if (Double.isInfinite(inv) || Double.isNaN(inv))
         {
            double lowerY, upperY;
            double offset = y - nearestY;

            if (nearestY < secondNearestY)
            {
               lowerY = nearestY + minorLength*Math.floor(offset/minorLength);
               upperY = lowerY + minorLength;
            }
            else
            {
               upperY = nearestY + minorLength*Math.floor(offset/minorLength);
               lowerY = upperY - minorLength;
            }

            if (y - lowerY < upperY - y)
            {
               nearestY = lowerY;
            }
            else
            {
               nearestY = upperY;
            }
         }
         else
         {
            int n = subDivisions;

            double ax, ay, bx, by;

            int min, max;

            if (nearestX < secondNearestX)
            {
               ax = nearestX;
               ay = nearestY;
               bx = secondNearestX;
               by = secondNearestY;
               min = 1;
               max = n/2;
            }
            else
            {
               ax = secondNearestX;
               ay = secondNearestY;
               bx = nearestX;
               by = nearestY;
               min = n/2;
               max = n-1;
            }

            for (int t = min; t <= max; t++)
            {
               double currentX = (ax*(n-t))/(double)n
                               + (bx*t)/(double)n;
               double currentY = (ay*(n-t))/(double)n
                               + (by*t)/(double)n;

               dx = currentX - x;
               dy = currentY - y;

               diff = dx*dx + dy*dy;

               if (diff < minDiff)
               {
                  nearestX = currentX;
                  nearestY = currentY;
                  minDiff = diff;
               }
            }
         }
      }

      return new Point2D.Double(nearestX, nearestY);
   }

   public void drawGrid()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();

      if (g2 == null)
      {
         return;
      }

      double bpToCompXFactor = cg.bpToComponentX(1.0);
      double bpToCompYFactor = cg.bpToComponentY(1.0);

      double compWidth = bpToCompXFactor * cg.getPaperWidth();
      double compHeight = bpToCompYFactor * cg.getPaperHeight();

      // Length of each side of the equilateral triangle:
      double length = unit.toBp(majorDivisions);

      double majorCompX = JDRConstants.HALF_ROOT_3 * length
                         * bpToCompXFactor;

      double majorCompY = bpToCompYFactor * length;

      double yCompOffset = 0.5 * majorCompY;

      double minorLength = length/subDivisions;

      double minorCompY = bpToCompYFactor*minorLength;

      double dx = minorCompY * JDRConstants.HALF_ROOT_3;
      double dy = minorCompY * 0.5;

      if (majorCompX == 0 || majorCompY == 0)
      {
         return;
      }

      Rectangle clip = g2.getClipBounds();

      int compMinX, compMinY, compMaxX, compMaxY;

      if (clip == null)
      {
         compMinX = 0;
         compMinY = 0;
         compMaxX = (int)compWidth;
         compMaxY = (int)compHeight;
      }
      else
      {
         compMinX = clip.x;
         compMinY = clip.y;
         compMaxX = clip.x + clip.width;
         compMaxY = clip.y + clip.height;
      }

      double compOffsetX = cg.getBpOriginX() * bpToCompXFactor;
      double compOffsetY = cg.getBpOriginY() * bpToCompYFactor;

      compMinX -= compOffsetX;
      compMinY -= compOffsetY;

      compMaxX -= compOffsetX;
      compMaxY -= compOffsetY;

      compMinX -= HALF_MAJOR_TIC;
      compMinY -= HALF_MAJOR_TIC;
      compMaxX += HALF_MAJOR_TIC;
      compMaxY += HALF_MAJOR_TIC;

      g2.setColor(axesGridColor);
      g2.drawLine(compMinX, 0, compMaxX, 0);
      g2.drawLine(0, compMinY, 0, compMaxY);

      int currentMajorXidx, currentMajorYidx;
      int currentMinorXidx, currentMinorYidx;
      double currentCompX, currentCompY;

      int initialMajorYidx, initialMinorYidx;
      double initialCurrentCompY;

      currentMinorXidx = 0;
      initialMinorYidx = 0;

      // Is the origin in range?

      if (compMinX == 0)
      {
         currentMajorXidx = 0;
         currentCompX = 0;
      }
      else
      {
         try
         {
            if (compMinX > 0)
            {
               currentMajorXidx = compMinX / (int)majorCompX;
            }
            else
            {
               currentMajorXidx = -(int)Math.ceil(-compMinX / majorCompX);
            }

            currentCompX = majorCompX * currentMajorXidx;
         }
         catch (ArithmeticException e)
         {
            currentMajorXidx = 0;
            currentCompX = 0;
         }
      }

      if (compMinY == 0)
      {
         initialMajorYidx = 0;
         initialCurrentCompY = 0;
      }
      else
      {
         try
         {
            if (compMinY > 0)
            {
               initialMajorYidx = compMinY / (int)majorCompY;
            }
            else
            {
               initialMajorYidx = -(int)Math.ceil(-compMinY / majorCompY);
            }

            initialCurrentCompY = majorCompY * initialMajorYidx;
         }
         catch (ArithmeticException e)
         {
            initialMajorYidx = 0;
            initialCurrentCompY = 0;
         }
      }

      int halfSubDivisions = subDivisions/2;

      while (currentCompX < compMaxX)
      {
         currentMajorYidx = initialMajorYidx;
         currentMinorYidx = initialMinorYidx;
         currentCompY = initialCurrentCompY;

         double y = 0.0;

         if ((Math.abs(currentMajorXidx) % 2) == 1)
         {
            y = yCompOffset;

            for (int i = 1; i < subDivisions; i++)
            {
               if (i <= halfSubDivisions)
               {
                  drawMinorTic(g2, currentCompX, currentCompY+y-(i*minorCompY));
               }

               drawMinorTic(g2, currentCompX+(i*dx), currentCompY+y-(i*dy));
            }
         }

         while (currentCompY < compMaxY)
         {
            drawMajorTic(g2, currentCompX, currentCompY+y);

            for (int i = 1; i < subDivisions; i++)
            {
               drawMinorTic(g2, currentCompX, currentCompY+y+(i*minorCompY));

               drawMinorTic(g2, currentCompX+(i*dx), currentCompY+y+(i*dy));

               drawMinorTic(g2, currentCompX+(i*dx), 
                 currentCompY+y+majorCompY-(i*dy));
            }

            currentCompY += majorCompY;
         }

         currentMajorXidx++;

         currentCompX += majorCompX;
      }
   }

   public String getUnitLabel()
   {
      return unit.getLabel();
   }

   public JDRUnit getMainUnit()
   {
      return unit;
   }

   public String formatLocationFromCartesianBp(double bpX, double bpY)
   {
      if (unit.getID() == JDRUnit.BP)
      {
          DecimalFormat f = new DecimalFormat("0");

          return f.format(bpX)+","+f.format(bpY)+unit.getLabel();
      }


      DecimalFormat f = new DecimalFormat("0.00");

      return f.format(unit.fromBp(bpX))+","
            +f.format(unit.fromBp(bpY))+" "
            +unit.getLabel();
   }

   public JDRGridLoaderListener getListener()
   {
      return listener;
   }

   public void makeEqual(JDRGrid grid)
   {
      setCanvasGraphics(grid.getCanvasGraphics());
      JDRIsoGrid rectGrid = (JDRIsoGrid)grid;
      majorDivisions = rectGrid.majorDivisions;
      subDivisions = rectGrid.subDivisions;
      unit = rectGrid.unit;
   }

   public JDRRectangularGrid getRectangularGrid()
   {
      return new JDRRectangularGrid(getCanvasGraphics(),
         unit, majorDivisions, subDivisions);
   }

   private static JDRIsoGridListener listener = new JDRIsoGridListener();
   /**
    * Stores the distance between the major tick marks in terms of
    * the unit given by {@link #unit}.
    */
   private double majorDivisions;

   /**
    * Stores the number of subdivisions within a major grid
    * interval.
    */
   private int subDivisions;

   private JDRUnit unit;
}
