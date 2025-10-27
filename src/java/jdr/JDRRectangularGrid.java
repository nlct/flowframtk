// File          : JDRRectangularGrid.java
// Description   : Represents a rectangular grid.
// Author        : Nicola L.C. Talbot
// Creation Date : 17th August 2010
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
 * Class representing a rectangular grid. (Left-handed Cartesian.)
 * The origin is the top left corner of the page.
 * @author Nicola L C Talbot
 */
public class JDRRectangularGrid extends JDRGrid
{
   /**
    * Initialises using the default settings. The defaults are:
    * bp units, major interval width of 100 units and 10
    * sub-divisions per major interval.
    */
   public JDRRectangularGrid(CanvasGraphics cg)
   {
      super(cg, GRID_RECTANGULAR);
      set(JDRUnit.bp, 100.0, 10);
   }

   /**
    * Initialises using the given settings.
    * @param gridUnit the grid units
    * @param majorDiv the width of the major interval in terms of
    * the grid unit
    * @param subDiv the number of sub-divisions per major interval
    */
   public JDRRectangularGrid(CanvasGraphics cg,
       JDRUnit gridUnit, double majorDiv, int subDiv)
   {
      super(cg, GRID_RECTANGULAR);
      set(gridUnit, majorDiv, subDiv);
   }

   public Object clone()
   {
      return new JDRRectangularGrid(getCanvasGraphics(),
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
      double major = unit.toBp(majorDivisions);

      return new Point2D.Double(major, major);
   }

   public Point2D getMinorTicDistance()
   {
      Point2D p = new Point2D.Double(0, 0);

      if (subDivisions > 0)
      {
         double distance = unit.toBp(majorDivisions)/subDivisions;

         p.setLocation(distance, distance);
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

   // x, y and return values in bp
   public Point2D getClosestBpTic(double x, double y)
   {
      JDRPaper paper = getCanvasGraphics().getPaper();

      double major = unit.toBp(majorDivisions);

      double maxX = paper.getWidth();
      double maxY = paper.getHeight();

      int n = (int)Math.floor(x/major);
      int m = (int)Math.floor(y/major);

      if (subDivisions == 0)
      {
         double px1 = n*major;
         double py1 = m*major;

         double dx = px1-x;
         double dy = py1-y;

         double distance1 = dx*dx + dy*dy;

         double px2 = px1 + major;
         double py2 = py1;

         dx = px2-x;
         dy = py2-y;

         double distance2 = dx*dx + dy*dy;

         double px3 = px1;
         double py3 = py1+major;

         dx = px3-x;
         dy = py3-y;

         double distance3 = dx*dx + dy*dy;

         double px4 = px2;
         double py4 = py3;

         dx = px4-x;
         dy = py4-y;

         double distance4 = dx*dx + dy*dy;

         if (distance1 <= distance2
          && distance1 <= distance3 
          && distance1 <= distance4)
         {
            return new Point2D.Double(px1, py1);
         }

         if (distance2 <= distance1
          && distance2 <= distance3
          && distance2 <= distance4)
         {
            return new Point2D.Double(px2, py2);
         }

         if (distance3 <= distance1
          && distance3 <= distance2
          && distance3 <= distance4)
         {
            return new Point2D.Double(px3, py3);
         }

         return new Point2D.Double(px4, py4);
      }

      double minor = major/subDivisions;

      double X = n*major;
      double Y = m*major;

      double x1 = x - X;
      double y1 = y - Y;

      int n1 = (int)Math.floor(x1/minor);
      int m1 = (int)Math.floor(y1/minor);

      double px1 = n1*minor;
      double py1 = m1*minor;

      double dx = px1-x1;
      double dy = py1-y1;

      double distance1 = dx*dx + dy*dy;

      double px2 = px1 + minor;
      double py2 = py1;

      dx = px2-x1;
      dy = py2-y1;

      double distance2 = dx*dx + dy*dy;

      double px3 = px1;
      double py3 = py1+minor;

      dx = px3-x1;
      dy = py3-y1;

      double distance3 = dx*dx + dy*dy;

      double px4 = px2;
      double py4 = py3;

      dx = px4-x1;
      dy = py4-y1;

      double distance4 = dx*dx + dy*dy;

      if (distance1 <= distance2
       && distance1 <= distance3 
       && distance1 <= distance4)
      {
         return new Point2D.Double(X+px1, Y+py1);
      }

      if (distance2 <= distance1
       && distance2 <= distance3
       && distance2 <= distance4)
      {
         return new Point2D.Double(X+px2, Y+py2);
      }

      if (distance3 <= distance1
       && distance3 <= distance2
       && distance3 <= distance4)
      {
         return new Point2D.Double(X+px3, Y+py3);
      }

      return new Point2D.Double(X+px4, Y+py4);
   }

   public Point2D getClosestTic(double x, double y)
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRUnit storageUnit = cg.getStorageUnit();
      JDRPaper paper = cg.getPaper();

      double major = unit.toUnit(majorDivisions, storageUnit);

      double maxX = storageUnit.fromBp(paper.getWidth());
      double maxY = storageUnit.fromBp(paper.getHeight());

      int n = (int)Math.floor(x/major);
      int m = (int)Math.floor(y/major);

      if (subDivisions == 0)
      {
         double px1 = n*major;
         double py1 = m*major;

         double dx = px1-x;
         double dy = py1-y;

         double distance1 = dx*dx + dy*dy;

         double px2 = px1 + major;
         double py2 = py1;

         dx = px2-x;
         dy = py2-y;

         double distance2 = dx*dx + dy*dy;

         double px3 = px1;
         double py3 = py1+major;

         dx = px3-x;
         dy = py3-y;

         double distance3 = dx*dx + dy*dy;

         double px4 = px2;
         double py4 = py3;

         dx = px4-x;
         dy = py4-y;

         double distance4 = dx*dx + dy*dy;

         if (distance1 <= distance2
          && distance1 <= distance3 
          && distance1 <= distance4)
         {
            return new Point2D.Double(px1, py1);
         }

         if (distance2 <= distance1
          && distance2 <= distance3
          && distance2 <= distance4)
         {
            return new Point2D.Double(px2, py2);
         }

         if (distance3 <= distance1
          && distance3 <= distance2
          && distance3 <= distance4)
         {
            return new Point2D.Double(px3, py3);
         }

         return new Point2D.Double(px4, py4);
      }

      double minor = major/subDivisions;

      double X = n*major;
      double Y = m*major;

      double x1 = x - X;
      double y1 = y - Y;

      int n1 = (int)Math.floor(x1/minor);
      int m1 = (int)Math.floor(y1/minor);

      double px1 = n1*minor;
      double py1 = m1*minor;

      double dx = px1-x1;
      double dy = py1-y1;

      double distance1 = dx*dx + dy*dy;

      double px2 = px1 + minor;
      double py2 = py1;

      dx = px2-x1;
      dy = py2-y1;

      double distance2 = dx*dx + dy*dy;

      double px3 = px1;
      double py3 = py1+minor;

      dx = px3-x1;
      dy = py3-y1;

      double distance3 = dx*dx + dy*dy;

      double px4 = px2;
      double py4 = py3;

      dx = px4-x1;
      dy = py4-y1;

      double distance4 = dx*dx + dy*dy;

      if (distance1 <= distance2
       && distance1 <= distance3 
       && distance1 <= distance4)
      {
         return new Point2D.Double(X+px1, Y+py1);
      }

      if (distance2 <= distance1
       && distance2 <= distance3
       && distance2 <= distance4)
      {
         return new Point2D.Double(X+px2, Y+py2);
      }

      if (distance3 <= distance1
       && distance3 <= distance2
       && distance3 <= distance4)
      {
         return new Point2D.Double(X+px3, Y+py3);
      }

      return new Point2D.Double(X+px4, Y+py4);
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

      Point2D majorBp = getMajorTicDistance();
      Point2D minorBp = getMinorTicDistance();

      double majorCompX = bpToCompXFactor*majorBp.getX();
      double majorCompY = bpToCompYFactor*majorBp.getY();

      if (majorCompX == 0 || majorCompY == 0)
      {
         return;
      }

      double minorCompX = bpToCompXFactor*minorBp.getX();
      double minorCompY = bpToCompYFactor*minorBp.getY();

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

      double compOffsetX = cg.getOriginX() * bpToCompXFactor;
      double compOffsetY = cg.getOriginY() * bpToCompYFactor;

      compMinX -= compOffsetX;
      compMinY -= compOffsetY;

      compMaxX -= compOffsetX;
      compMaxY -= compOffsetY;

      compMinX -= HALF_MAJOR_TIC;
      compMinY -= HALF_MAJOR_TIC;
      compMaxX += HALF_MAJOR_TIC;
      compMaxY += HALF_MAJOR_TIC;

      g2.setColor(minorGridColor);
      g2.drawLine(compMinX, 0, compMaxX, 0);
      g2.drawLine(0, compMinY, 0, compMaxY);

      int currentMajorXidx, currentMajorYidx;
      int currentMinorXidx, currentMinorYidx;
      double currentCompX, currentCompY;

      int initialMajorYidx, initialMinorYidx;
      double initialCurrentCompY;

      // Is the origin in range?

      if (compMinX > 0 || compMinY > 0)
      {
         // Origin is outside clip bounds. Find the nearest tic.

         try
         {
            currentMajorXidx = compMinX / (int)majorCompX;
            currentMinorXidx = (compMinX % (int)majorCompX) / (int)minorCompX;

            currentCompX = majorCompX * currentMajorXidx
                         + minorCompX * currentMinorXidx;

            initialMajorYidx = compMinY / (int)majorCompY;
            initialMinorYidx = (compMinY % (int)majorCompY) / (int)minorCompY;

            initialCurrentCompY = majorCompY * initialMajorYidx
                        + minorCompY * initialMinorYidx;
         }
         catch (ArithmeticException e)
         {
            currentMajorXidx = 0;
            currentMinorXidx = 0;
            currentCompX = 0;

            initialMajorYidx = 0;
            initialMinorYidx = 0;
            initialCurrentCompY = 0;
         }
      }
      else
      {
         currentMajorXidx = 0;
         currentMinorXidx = 0;
         currentCompX = 0;

         initialMajorYidx = 0;
         initialMinorYidx = 0;
         initialCurrentCompY = 0;
      }

      while (currentCompX < compMaxX)
      {
         currentMajorYidx = initialMajorYidx;
         currentMinorYidx = initialMinorYidx;
         currentCompY = initialCurrentCompY;

         while (currentCompY < compMaxY)
         {
            if (currentMinorXidx == 0 && currentMinorYidx == 0)
            {
               drawMajorTic(g2, currentCompX, currentCompY,
                 cg.getComponent());
            }
            else
            {
               drawMinorTic(g2, currentCompX, currentCompY,
                 cg.getComponent());
            }

            currentMinorYidx++;

            if (currentMinorYidx >= subDivisions)
            {
               currentMinorYidx = 0;
               currentMajorYidx++;
            }

            currentCompY += minorCompY;
         }

         currentMinorXidx++;

         if (currentMinorXidx >= subDivisions)
         {
            currentMinorXidx = 0;
            currentMajorXidx++;
         }

         currentCompX += minorCompX;
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
      JDRRectangularGrid rectGrid = (JDRRectangularGrid)grid;
      majorDivisions = rectGrid.majorDivisions;
      subDivisions = rectGrid.subDivisions;
      unit = rectGrid.unit;
   }

   public JDRRectangularGrid getRectangularGrid()
   {
      return this;
   }

   private static JDRRectangularGridListener listener = new JDRRectangularGridListener();

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
