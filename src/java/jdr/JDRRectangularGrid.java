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

   /**
    * Initialises using the given settings.
    * @param gridUnit the grid units
    * @param majorDivX the width of the major x interval in terms of
    * the grid unit
    * @param majorDivY the width of the major y interval in terms of
    * the grid unit
    * @param subDivX the number of sub-divisions per major x interval
    * @param subDivY the number of sub-divisions per major y interval
    */
   public JDRRectangularGrid(CanvasGraphics cg,
       JDRUnit gridUnit, double majorDivX, double majorDivY,
       int subDivX, int subDivY)
   {
      super(cg, GRID_RECTANGULAR);
      set(gridUnit, majorDivX, majorDivY, subDivX, subDivY);
   }

   public Object clone()
   {
      return new JDRRectangularGrid(getCanvasGraphics(),
         unit, majorDivisionsX, majorDivisionsY,
         subDivisionsX, subDivisionsY);
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
    * Change the grid settings settings.
    * @param gridUnit the grid units
    * @param majorDivX the width of the major x interval in terms of
    * the grid unit
    * @param majorDivY the width of the major y interval in terms of
    * the grid unit
    * @param subDivX the number of sub-divisions per major x interval
    * @param subDivY the number of sub-divisions per major y interval
    */
   public void set(JDRUnit gridUnit, double majorDivX, double majorDivY,
      int subDivX, int subDivY)
   {
      setUnit(gridUnit);
      setMajorInterval(majorDivX, majorDivY);
      setSubDivisions(subDivX, subDivY);
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

      majorDivisionsX = majorDiv;
      majorDivisionsY = majorDiv;
   }

   public void setMajorInterval(double majorDivX, double majorDivY)
   {
      if (majorDivX <= 0.0)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.GRID_MAJOR, majorDivX,
           getCanvasGraphics());
      }

      if (majorDivY <= 0.0)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.GRID_MAJOR, majorDivY,
           getCanvasGraphics());
      }

      majorDivisionsX = majorDivX;
      majorDivisionsY = majorDivY;
   }

   /**
    * Sets the number of sub divisions within the major x and y interval.
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

      subDivisionsX = subDiv;
      subDivisionsY = subDiv;
   }

   /**
    * Sets the number of sub divisions within the major x and y intervals.
    * @param subDivX the number of x sub divisions, which must be &gt;=0.
    * @param subDivY the number of y sub divisions, which must be &gt;=0.
    */
   public void setSubDivisions(int subDivX, int subDivY)
   {
      if (subDivX < 0)
      {
         throw new JdrIllegalArgumentException(
          JdrIllegalArgumentException.GRID_SUBDIVISIONS, subDivX,
          getCanvasGraphics());
      }

      if (subDivY < 0)
      {
         throw new JdrIllegalArgumentException(
          JdrIllegalArgumentException.GRID_SUBDIVISIONS, subDivY,
          getCanvasGraphics());
      }

      subDivisionsX = subDivX;
      subDivisionsY = subDivY;
   }

   /**
    * Sets the number of sub divisions within the major x interval.
    * @param subDivX the number of x sub divisions, which must be &gt;=0.
    */
   public void setSubDivisionsX(int subDivX)
   {
      if (subDivX < 0)
      {
         throw new JdrIllegalArgumentException(
          JdrIllegalArgumentException.GRID_SUBDIVISIONS, subDivX,
          getCanvasGraphics());
      }

      subDivisionsX = subDivX;
   }

   /**
    * Sets the number of sub divisions within the major y interval.
    * @param subDivX the number of y sub divisions, which must be &gt;=0.
    */
   public void setSubDivisionsY(int subDivY)
   {
      if (subDivY < 0)
      {
         throw new JdrIllegalArgumentException(
          JdrIllegalArgumentException.GRID_SUBDIVISIONS, subDivY,
          getCanvasGraphics());
      }

      subDivisionsY = subDivY;
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
      return getMajorXInterval();
   }

   /**
    * Gets the width of the major x intervals.
    * @return the width of the major x intervals in terms of the
    * grid's unit.
    */
   public double getMajorXInterval()
   {
      return majorDivisionsX;
   }

   /**
    * Gets the width of the major y intervals.
    * @return the width of the major y intervals in terms of the
    * grid's unit.
    */
   public double getMajorYInterval()
   {
      return majorDivisionsY;
   }

   @Override
   public int getSubDivisions()
   {
      return subDivisionsX;
   }

   /**
    * Gets the number of sub divisions within a major x interval.
    * @return the number of x sub divisions.
    */
   @Override
   public int getSubDivisionsX()
   {
      return subDivisionsX;
   }

   /**
    * Gets the number of sub divisions within a major y interval.
    * @return the number of y sub divisions.
    */
   @Override
   public int getSubDivisionsY()
   {
      return subDivisionsY;
   }

   @Override
   public Point2D getMajorTicDistance()
   {
      double majorX = unit.toBp(majorDivisionsX);
      double majorY = unit.toBp(majorDivisionsY);

      return new Point2D.Double(majorX, majorY);
   }

   @Override
   public Point2D getMinorTicDistance()
   {
      double x = 0;
      double y = 0;

      if (subDivisionsX > 0)
      {
         x = unit.toBp(majorDivisionsX)/subDivisionsX;
      }

      if (subDivisionsY > 0)
      {
         y = unit.toBp(majorDivisionsY)/subDivisionsY;
      }

      return new Point2D.Double(x, y);
   }

   @Override
   public Point2D fromCartesianBp(double x, double y)
   {
      return new Point2D.Double(unit.fromBp(x), unit.fromBp(y));
   }

   @Override
   public void fromCartesianBp(Point2D cartesianPoint, Point2D target)
   {
      target.setLocation(unit.fromBp(cartesianPoint.getX()),
                         unit.fromBp(cartesianPoint.getY()));
   }

   @Override
   public void toCartesianBp(Point2D original, Point2D target)
   {
      target.setLocation(unit.toBp(original.getX()),
                         unit.toBp(original.getY()));
   }

   // x, y and return values in bp
   @Override
   public Point2D getClosestBpTic(double x, double y)
   {
      JDRPaper paper = getCanvasGraphics().getPaper();

      double majorX = unit.toBp(majorDivisionsX);
      double majorY = unit.toBp(majorDivisionsY);

      double maxX = paper.getWidth();
      double maxY = paper.getHeight();

      int nx = (int)Math.floor(x/majorX);
      int ny = (int)Math.floor(y/majorY);

      int subDivX = Math.max(1, subDivisionsX);
      int subDivY = Math.max(1, subDivisionsY);

      double X = nx * majorX;
      double Y = ny * majorY;

      double x1 = x - X;
      double y1 = y - Y;

      double minorX = majorX/subDivX;
      int nx1 = (int)Math.floor(x1/minorX);
      double px1 = nx1 * minorX;

      double minorY = majorY/subDivY;
      int ny1 = (int)Math.floor(y1/minorY);
      double py1 = ny1 * minorY;

      double dx = px1-x1;
      double dy = py1-y1;

      double distance1 = dx*dx + dy*dy;

      double px2 = px1 + minorX;
      double py2 = py1;

      dx = px2-x1;
      dy = py2-y1;

      double distance2 = dx*dx + dy*dy;

      double px3 = px1;
      double py3 = py1+minorY;

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

   @Override
   public Point2D getClosestTic(double x, double y)
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRUnit storageUnit = cg.getStorageUnit();
      JDRPaper paper = cg.getPaper();

      double majorX = unit.toUnit(majorDivisionsX, storageUnit);
      double majorY = unit.toUnit(majorDivisionsY, storageUnit);

      double maxX = storageUnit.fromBp(paper.getWidth());
      double maxY = storageUnit.fromBp(paper.getHeight());

      int nx = (int)Math.floor(x/majorX);
      int ny = (int)Math.floor(y/majorY);

      int subDivX = Math.max(1, subDivisionsX);
      int subDivY = Math.max(1, subDivisionsY);

      double X = nx * majorX;
      double Y = ny * majorY;

      double x1 = x - X;
      double y1 = y - Y;

      double minorX = majorX/subDivX;
      int nx1 = (int)Math.floor(x1/minorX);

      double minorY = majorY / subDivY;
      int ny1 = (int)Math.floor(y1 / minorY);

      double px1 = nx1 * minorX;
      double py1 = ny1 * minorY;

      double dx = px1-x1;
      double dy = py1-y1;

      double distance1 = dx*dx + dy*dy;

      double px2 = px1 + minorX;
      double py2 = py1;

      dx = px2-x1;
      dy = py2-y1;

      double distance2 = dx*dx + dy*dy;

      double px3 = px1;
      double py3 = py1+minorY;

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

   @Override
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

      // Is the origin in range?

      if (compMinX == 0)
      {
         currentMajorXidx = 0;
         currentMinorXidx = 0;
         currentCompX = 0;
      }
      else
      {
         try
         {
            currentMajorXidx = Math.abs(compMinX) / (int)majorCompX;
            currentMinorXidx = (Math.abs(compMinX) % (int)majorCompX) / (int)minorCompX;

            currentCompX = majorCompX * currentMajorXidx
                         + minorCompX * currentMinorXidx;

            if (compMinX < 0)
            {
               currentCompX = -currentCompX;
               currentMajorXidx = -currentMajorXidx;
               currentMinorXidx = (subDivisionsX - currentMinorXidx) % subDivisionsX;
            }
         }
         catch (ArithmeticException e)
         {
            currentMajorXidx = 0;
            currentMinorXidx = 0;
            currentCompX = 0;
         }
      }

      if (compMinY == 0)
      {
         initialMajorYidx = 0;
         initialMinorYidx = 0;
         initialCurrentCompY = 0;
      }
      else
      {
         try
         {
            initialMajorYidx = Math.abs(compMinY) / (int)majorCompY;
            initialMinorYidx = (Math.abs(compMinY) % (int)majorCompY) / (int)minorCompY;

            initialCurrentCompY = majorCompY * initialMajorYidx
                        + minorCompY * initialMinorYidx;

            if (compMinY < 0)
            {
               initialCurrentCompY = -initialCurrentCompY;
               initialMajorYidx = -initialMajorYidx;
               initialMinorYidx = (subDivisionsY - initialMinorYidx) % subDivisionsY;
            }
         }
         catch (ArithmeticException e)
         {
            initialMajorYidx = 0;
            initialMinorYidx = 0;
            initialCurrentCompY = 0;
         }
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

            if (currentMinorYidx >= subDivisionsY)
            {
               currentMinorYidx = 0;
               currentMajorYidx++;
            }

            currentCompY += minorCompY;
         }

         currentMinorXidx++;

         if (currentMinorXidx >= subDivisionsX)
         {
            currentMinorXidx = 0;
            currentMajorXidx++;
         }

         currentCompX += minorCompX;
      }

   }

   @Override
   public String getUnitLabel()
   {
      return unit.getLabel();
   }

   @Override
   public JDRUnit getMainUnit()
   {
      return unit;
   }

   @Override
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

   @Override
   public JDRGridLoaderListener getListener()
   {
      return listener;
   }

   @Override
   public void makeEqual(JDRGrid grid)
   {
      JDRRectangularGrid rectGrid = (JDRRectangularGrid)grid;
      majorDivisionsX = rectGrid.majorDivisionsX;
      majorDivisionsY = rectGrid.majorDivisionsY;
      subDivisionsX = rectGrid.subDivisionsX;
      subDivisionsY = rectGrid.subDivisionsY;
      unit = rectGrid.unit;
   }

   @Override
   public JDRRectangularGrid getRectangularGrid()
   {
      return this;
   }

   private static JDRRectangularGridListener listener = new JDRRectangularGridListener();

   @Override
   public String toString()
   {
      return String.format(
       "%s[majorX=%f,majorY=%f,subdivisionsX=%d,subdivisionsY=%d,unit=%s]",
        getClass().getSimpleName(), 
        majorDivisionsX, majorDivisionsY,
        subDivisionsX, subDivisionsY, unit
      );
   }

   /**
    * Stores the distance between the major tick marks in terms of
    * the unit given by {@link #unit}.
    */
   private double majorDivisionsX, majorDivisionsY;

   /**
    * Stores the number of subdivisions within a major grid
    * interval.
    */
   private int subDivisionsX, subDivisionsY;

   private JDRUnit unit;
}
