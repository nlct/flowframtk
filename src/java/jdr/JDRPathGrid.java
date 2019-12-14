// File          : JDRPathGrid.java
// Description   : Represents a "grid" that follows a path.
// Author        : Nicola L.C. Talbot
// Creation Date : 21st April 2017
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
 * Class representing a grid that follows a path.
 * @author Nicola L C Talbot
 */
public class JDRPathGrid extends JDRGrid
{
   /**
    * Initialises using the default settings.
    * The path is set to the page boundary.
    */
   public JDRPathGrid(CanvasGraphics cg)
   {
      super(cg, GRID_PATH);
      set(JDRUnit.bp, 100.0, 10);

      JDRPaper paper = cg.getPaper();

      path = new Rectangle2D.Double(0.0, 0.0,
       paper.getWidth(), paper.getHeight());
   }

   /**
    * Initialises using the given settings.
    * @param gridUnit the grid units
    * @param majorDiv the width of the major interval in terms of
    * the grid unit
    * @param subDiv the number of sub-divisions per major interval
    * @param shape the path to use as a grid (co-ordinates must
    * match gridUnit)
    */
   public JDRPathGrid(CanvasGraphics cg,
      JDRUnit gridUnit, double majorDiv, int subDiv, Shape shape)
   {
      super(cg, GRID_PATH);
      set(gridUnit, majorDiv, subDiv);

      if (shape == null)
      {
         JDRPaper paper = cg.getPaper();

         path = new Rectangle2D.Double(0.0, 0.0,
          paper.getWidth(), paper.getHeight());
      }
      else
      {
         setShape(shape);
      }
   }

   /**
    * Initialises using the given settings.
    * @param gridUnit the grid units
    * @param majorDiv the width of the major interval in terms of
    * the grid unit
    * @param subDiv the number of sub-divisions per major interval
    * @param shape the path to use as a grid
    * @param shapeUnit the units used by the shape
    */
   public JDRPathGrid(CanvasGraphics cg,
      JDRUnit gridUnit, double majorDiv, int subDiv,
      Shape shape, JDRUnit shapeUnit)
   {
      super(cg, GRID_PATH);
      set(gridUnit, majorDiv, subDiv);
      setShape(shape, shapeUnit);
   }

   /**
    * Initialises using the given settings.
    * @param gridUnit the grid units
    * @param majorDiv the width of the major interval in terms of
    * the grid unit
    * @param subDiv the number of sub-divisions per major interval
    * @param shape the path to use as a grid
    */
   public JDRPathGrid(CanvasGraphics cg,
      JDRUnit gridUnit, double majorDiv, int subDiv, JDRShape shape)
   {
      super(cg, GRID_PATH);
      set(gridUnit, majorDiv, subDiv);
      setShape(shape);
   }

   public JDRPathGrid(JDRPathGrid grid)
   {
      super(grid.getCanvasGraphics(), grid.getID());
      unit = grid.unit;
      majorDivisions = grid.majorDivisions;
      subDivisions = grid.subDivisions;

      // No need to clone path as it's never edited. A new Shape
      // object has to be created if the path changes.
      path = grid.path;
   }

   public Object clone()
   {
      return new JDRPathGrid(this);
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
      JDRUnit storageUnit = unit;

      unit = gridUnit;

      // Update the path co-ordinates if path has been set

      if (path != null && !storageUnit.equals(unit))
      {
         double factor = storageUnit.toUnit(1.0, unit);

         AffineTransform af = new AffineTransform();
         af.scale(factor, factor);

         path = new GeneralPath(af.createTransformedShape(path));
      }
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
      if (unit.equals(JDRUnit.bp))
      {
         return getClosestTic(x, y);
      }

      Point2D p = getClosestTic(unit.fromBp(x), unit.fromBp(y));

      p.setLocation(unit.toBp(p.getX()), unit.toBp(p.getY()));

      return p;
   }

   public Point2D getClosestTic(double x, double y)
   {
      CanvasGraphics cg = getCanvasGraphics();

      double nearestX = x;
      double nearestY = y;

      double min = Double.MAX_VALUE;

      PathIterator pi = path.getPathIterator(null);
      double[] coords = new double[6];

      double diff;

      Point2D current = null;

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         // Check major tick marks first.

         switch (type)
         {
            case PathIterator.SEG_CUBICTO:

               diff = Math.abs(coords[4] - x) + Math.abs(coords[5] - y);

               if (diff <= min)
               {
                  nearestX = coords[4];
                  nearestY = coords[5];

                  min = diff;
               }

            // fall through
            case PathIterator.SEG_QUADTO:

               diff = Math.abs(coords[2] - x) + Math.abs(coords[3] - y);

               if (diff <= min)
               {
                  nearestX = coords[2];
                  nearestY = coords[3];

                  min = diff;
               }

            // fall through
            case PathIterator.SEG_MOVETO:
            case PathIterator.SEG_LINETO:

               diff = Math.abs(coords[0] - x) + Math.abs(coords[1] - y);

               if (diff <= min)
               {
                  nearestX = coords[0];
                  nearestY = coords[1];

                  min = diff;
               }

            break;
         }

         // Now check minor tick marks if subDivisions > 1

         if (subDivisions > 1)
         {
            switch (type)
            {
               case PathIterator.SEG_MOVETO:
               case PathIterator.SEG_LINETO:

                  if (current == null)
                  {
                     current = new Point2D.Double(coords[0], coords[1]);
                  }
                  else
                  {
                     for (int i = 0, n = subDivisions-1; i < n; i++)
                     {
                        double t = (double)(i+1)/(double)subDivisions;
                        double px = current.getX()+(coords[0]-current.getX())*t;
                        double py = current.getY()+(coords[1]-current.getY())*t;

                        diff = Math.abs(px - x) + Math.abs(py - y);

                        if (diff <= min)
                        {
                           nearestX = px;
                           nearestY = py;

                           min = diff;
                        }
                     }

                     current.setLocation(coords[0], coords[1]);
                  }

               break;

               case PathIterator.SEG_QUADTO:

                  for (int i = 0, n = subDivisions-1; i < n; i++)
                  {
                     double t = (double)(i+1)/(double)subDivisions;

                     double px = quadratic(t, current.getX(), coords[0],
                        coords[2]);
                     double py = quadratic(t, current.getY(), coords[1],
                        coords[3]);

                     diff = Math.abs(px - x) + Math.abs(py - y);

                     if (diff <= min)
                     {
                        nearestX = px;
                        nearestY = py;

                        min = diff;
                     }
                  }

                  current.setLocation(coords[2], coords[3]);

               break;
               case PathIterator.SEG_CUBICTO:

                  for (int i = 0, n = subDivisions-1; i < n; i++)
                  {
                     double t = (double)(i+1)/(double)subDivisions;

                     double px = cubic(t, current.getX(), coords[0], coords[2],
                       coords[4]);
                     double py = cubic(t, current.getY(), coords[1], coords[3],
                       coords[5]);

                     diff = Math.abs(px - x) + Math.abs(py - y);

                     if (diff <= min)
                     {
                        nearestX = px;
                        nearestY = py;

                        min = diff;
                     }
                  }

                  current.setLocation(coords[4], coords[5]);

               break;
            }
         }

         pi.next();
      }

      return new Point2D.Double(nearestX, nearestY);
   }

   private static double quadratic(double t, double p0, double p1, double p2)
   {
      double oneMinusT = 1.0-t;

      return oneMinusT*(oneMinusT*p0 + t*p1) + t*(oneMinusT*p1 + t*p2);
   }

   private static double cubic(double t, double p0, double p1, double p2,
     double p3)
   {
      double oneMinusT = 1.0-t;
      double oneMinusTsq = oneMinusT * oneMinusT;
      double tsq = t * t;

      return oneMinusTsq*oneMinusT*p0
           + 3*oneMinusTsq * t * p1
           + 3*oneMinusT * tsq * p2
           + tsq * t * p3;
   }

   public void drawGrid()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();

      if (g2 == null)
      {
         return;
      }

      double unitToBp = unit.toBp(1.0);

      double compXFactor = cg.bpToComponentY(unitToBp);
      double compYFactor = cg.bpToComponentY(unitToBp);

      PathIterator pi = path.getPathIterator(null);
      double[] coords = new double[6];

      Point2D current = null;

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_MOVETO:
            case PathIterator.SEG_LINETO:
               drawMajorTic(g2, (int)(compXFactor*coords[0]),
                (int)(compYFactor*coords[1]));

               if (current == null)
               {
                  current = new Point2D.Double(coords[0], coords[1]);
               }
               else
               {
                  for (int i = 0, n = subDivisions-1; i < n; i++)
                  {
                     double t = (double)(i+1)/(double)subDivisions;
                     double x = current.getX()+(coords[0]-current.getX())*t;
                     double y = current.getY()+(coords[1]-current.getY())*t;

                     drawMinorTic(g2,(int)(compXFactor*x),(int)(compXFactor*y));
                  }

                  current.setLocation(coords[0], coords[1]);
               }

            break;
            case PathIterator.SEG_QUADTO:
               drawMajorTic(g2, (int)(compXFactor*coords[0]),
                (int)(compYFactor*coords[1]));
               drawMajorTic(g2, (int)(compXFactor*coords[2]),
                (int)(compYFactor*coords[3]));

               for (int i = 0, n = subDivisions-1; i < n; i++)
               {
                  double t = (double)(i+1)/(double)subDivisions;

                  double x = quadratic(t, current.getX(), coords[0], coords[2]);
                  double y = quadratic(t, current.getY(), coords[1], coords[3]);

                  drawMinorTic(g2,(int)(compXFactor*x),(int)(compXFactor*y));
               }

               current.setLocation(coords[2], coords[3]);
            break;
            case PathIterator.SEG_CUBICTO:
               drawMajorTic(g2, (int)(compXFactor*coords[0]),
                (int)(compYFactor*coords[1]));
               drawMajorTic(g2, (int)(compXFactor*coords[2]),
                (int)(compYFactor*coords[3]));
               drawMajorTic(g2, (int)(compXFactor*coords[4]),
                (int)(compYFactor*coords[5]));

               for (int i = 0, n = subDivisions-1; i < n; i++)
               {
                  double t = (double)(i+1)/(double)subDivisions;

                  double x = cubic(t, current.getX(), coords[0], coords[2],
                    coords[4]);
                  double y = cubic(t, current.getY(), coords[1], coords[3],
                    coords[5]);

                  drawMinorTic(g2,(int)(compXFactor*x),(int)(compXFactor*y));
               }

               current.setLocation(coords[4], coords[5]);
            break;
         }

         pi.next();
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
      JDRPathGrid pathGrid = (JDRPathGrid)grid;
      majorDivisions = pathGrid.majorDivisions;
      subDivisions = pathGrid.subDivisions;
      unit = pathGrid.unit;
      path = pathGrid.path;
   }

   public JDRRectangularGrid getRectangularGrid()
   {
      return new JDRRectangularGrid(getCanvasGraphics(),
         unit, majorDivisions, subDivisions);
   }

   public Shape getShape()
   {
      return path;
   }

   /**
    * Sets the path that defines the grid.
    * @param shape the shape describing the path (must use the
    * same unit as the grid)
    */ 
   public void setShape(Shape shape)
   {
      path = shape;
   }

   /**
    * Sets the path that defines the grid.
    * @param shape the shape describing the path
    * @param shapeUnit the unit used by the shape
    */ 
   public void setShape(Shape shape, JDRUnit shapeUnit)
   {
      path = shape;

      if (!shapeUnit.equals(unit))
      {
         double factor = shapeUnit.toUnit(1.0, unit);

         AffineTransform af = new AffineTransform();
         af.scale(factor, factor);

         path = new GeneralPath(af.createTransformedShape(path));
      }
   }

   /**
    * Sets the path that defines the grid.
    * @param shape the shape describing the path (co-ordinates will
    * be converted to the same unit as the grid)
    */ 
   public void setShape(JDRShape shape)
   {
      CanvasGraphics cg = getCanvasGraphics();

      setShape(shape.getGeneralPath(), cg.getStorageUnit());
   }

   private static JDRPathGridListener listener = new JDRPathGridListener();

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

   /**
    * Path used by the grid. 
    */ 

   private Shape path;
}
