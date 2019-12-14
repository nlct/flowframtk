// File          : JDRTschicholdGrid.java
// Description   : Represents a Tschichold grid.
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
 * Class representing a Tschichold grid. (Left-handed Cartesian.)
 * The origin is the top left corner of the page.
 * @author Nicola L C Talbot
 */
public class JDRTschicholdGrid extends JDRGrid
{
   /**
    * Initialises using the default settings. The defaults are:
    * bp units, major interval width of 100 units and 10
    * sub-divisions per major interval.
    */
   public JDRTschicholdGrid(CanvasGraphics cg)
   {
      super(cg, GRID_TSCHICHOLD);
      set(JDRUnit.bp, 100.0, 10);
   }

   /**
    * Initialises using the given settings.
    * @param gridUnit the grid units
    * @param majorDiv the width of the major interval in terms of
    * the grid unit
    * @param subDiv the number of sub-divisions per major interval
    */
   public JDRTschicholdGrid(CanvasGraphics cg,
      JDRUnit gridUnit, double majorDiv, int subDiv)
   {
      super(cg, GRID_TSCHICHOLD);
      set(gridUnit, majorDiv, subDiv);
   }

   public Object clone()
   {
      return new JDRTschicholdGrid(getCanvasGraphics(),
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
      CanvasGraphics cg = getCanvasGraphics();

      double paperWidth = cg.getPaperWidth();
      double paperHeight = cg.getPaperHeight();

      double halfPaperHeight = 0.5*paperHeight;

      double thirdPaperHeight = paperHeight/3.0;
      double thirdPaperWidth = paperWidth/3.0;
      double twoThirdPaperWidth = 2.0*thirdPaperWidth;

      double sixthPaperHeight = paperHeight/6.0;

      double ninthPaperWidth = paperWidth/9.0;
      double ninthPaperHeight = paperHeight/9.0;

      double eightNinthPaperWidth = 8.0*ninthPaperWidth;

      double sevenNinthPaperWidth = 7.0*ninthPaperWidth;
      double sevenNinthPaperHeight = 7.0*ninthPaperHeight;

      double nearestX;
      double nearestY;

      if (cg.isEvenPage())
      {
         double twoNinthPaperWidth = 2.0*ninthPaperWidth;

         double px = paperWidth;
         double py = 0.0;

         double dx = x-px;
         double dy = y-py;

         nearestX = px;
         nearestY = py;

         double minDiff = dx*dx + dy*dy;

         px = 0.0;
         py = paperHeight;

         dx = x-px;
         dy = y-py;

         double diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = paperWidth;
         py = halfPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = 0;
         py = 0;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = twoThirdPaperWidth;
         py = 0.0;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = twoThirdPaperWidth;
         py = thirdPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = paperWidth;
         py = sixthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = eightNinthPaperWidth;
         py = ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = eightNinthPaperWidth;
         py = 4.0*ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = eightNinthPaperWidth;
         py = 5.0*ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = twoThirdPaperWidth;
         py = ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = twoNinthPaperWidth;
         py = ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = twoNinthPaperWidth;
         py = sevenNinthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = 4.0*ninthPaperWidth;
         py = sevenNinthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }
      }
      else
      {
         double px = 0;
         double py = 0;

         double dx = x-px;
         double dy = y-py;

         nearestX = px;
         nearestY = py;

         double minDiff = dx*dx + dy*dy;

         px = paperWidth;
         py = paperHeight;

         dx = x-px;
         dy = y-py;

         double diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = 0.0;
         py = halfPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = paperWidth;
         py = 0;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = thirdPaperWidth;
         py = 0;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = thirdPaperWidth;
         py = thirdPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = 0;
         py = sixthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = ninthPaperWidth;
         py = ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = ninthPaperWidth;
         py = 4.0*ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = ninthPaperWidth;
         py = 5.0*ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = sevenNinthPaperWidth;
         py = ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = thirdPaperWidth;
         py = ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = sevenNinthPaperWidth;
         py = sevenNinthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = 5.0*ninthPaperWidth;
         py = sevenNinthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }
      }

      return new Point2D.Double(nearestX, nearestY);
   }

   public Point2D getClosestTic(double x, double y)
   {
      CanvasGraphics cg = getCanvasGraphics();

      double paperWidth = cg.getStoragePaperWidth();
      double paperHeight = cg.getStoragePaperHeight();

      double halfPaperHeight = 0.5*paperHeight;

      double thirdPaperHeight = paperHeight/3.0;
      double thirdPaperWidth = paperWidth/3.0;
      double twoThirdPaperWidth = 2.0*thirdPaperWidth;

      double sixthPaperHeight = paperHeight/6.0;

      double ninthPaperWidth = paperWidth/9.0;
      double ninthPaperHeight = paperHeight/9.0;

      double eightNinthPaperWidth = 8.0*ninthPaperWidth;

      double sevenNinthPaperWidth = 7.0*ninthPaperWidth;
      double sevenNinthPaperHeight = 7.0*ninthPaperHeight;

      double nearestX;
      double nearestY;

      if (cg.isEvenPage())
      {
         double twoNinthPaperWidth = 2.0*ninthPaperWidth;

         double px = paperWidth;
         double py = 0.0;

         double dx = x-px;
         double dy = y-py;

         nearestX = px;
         nearestY = py;

         double minDiff = dx*dx + dy*dy;

         px = 0.0;
         py = paperHeight;

         dx = x-px;
         dy = y-py;

         double diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = paperWidth;
         py = halfPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = 0;
         py = 0;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = twoThirdPaperWidth;
         py = 0.0;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = twoThirdPaperWidth;
         py = thirdPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = paperWidth;
         py = sixthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = eightNinthPaperWidth;
         py = ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = eightNinthPaperWidth;
         py = 4.0*ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = eightNinthPaperWidth;
         py = 5.0*ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = twoThirdPaperWidth;
         py = ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = twoNinthPaperWidth;
         py = ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = twoNinthPaperWidth;
         py = sevenNinthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = 4.0*ninthPaperWidth;
         py = sevenNinthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }
      }
      else
      {
         double px = 0;
         double py = 0;

         double dx = x-px;
         double dy = y-py;

         nearestX = px;
         nearestY = py;

         double minDiff = dx*dx + dy*dy;

         px = paperWidth;
         py = paperHeight;

         dx = x-px;
         dy = y-py;

         double diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = 0.0;
         py = halfPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = paperWidth;
         py = 0;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = thirdPaperWidth;
         py = 0;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = thirdPaperWidth;
         py = thirdPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = 0;
         py = sixthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = ninthPaperWidth;
         py = ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = ninthPaperWidth;
         py = 4.0*ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = ninthPaperWidth;
         py = 5.0*ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = sevenNinthPaperWidth;
         py = ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = thirdPaperWidth;
         py = ninthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = sevenNinthPaperWidth;
         py = sevenNinthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
         }

         px = 5.0*ninthPaperWidth;
         py = sevenNinthPaperHeight;

         dx = x-px;
         dy = y-py;

         diff = dx*dx + dy*dy;

         if (diff < minDiff)
         {
            nearestX = px;
            nearestY = py;
            minDiff = diff;
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

      double compPaperWidth = bpToCompXFactor * cg.getPaperWidth();
      double compPaperHeight = bpToCompYFactor * cg.getPaperHeight();

      double halfCompPaperHeight = 0.5*compPaperHeight;

      double thirdCompPaperHeight = compPaperHeight/3.0;
      double thirdCompPaperWidth = compPaperWidth/3.0;
      double twoThirdCompPaperWidth = 2.0*thirdCompPaperWidth;

      double sixthCompPaperHeight = compPaperHeight/6.0;

      double ninthCompPaperWidth = compPaperWidth/9.0;
      double ninthCompPaperHeight = compPaperHeight/9.0;

      double eightNinthCompPaperWidth = 8.0*ninthCompPaperWidth;

      double sevenNinthCompPaperWidth = 7.0*ninthCompPaperWidth;
      double sevenNinthCompPaperHeight = 7.0*ninthCompPaperHeight;

      g2.setColor(minorGridColor);

      if (cg.isEvenPage())
      {
         g2.drawLine((int)compPaperWidth, 0, 0, (int)compPaperHeight);
         g2.drawLine(0, 0, (int)compPaperWidth, (int)halfCompPaperHeight);
         g2.drawLine(0, (int)compPaperHeight, 
                    (int)compPaperWidth, (int)halfCompPaperHeight);

         g2.drawLine((int)twoThirdCompPaperWidth, 0, 
                     (int)twoThirdCompPaperWidth, (int)thirdCompPaperHeight);

         g2.drawLine((int)twoThirdCompPaperWidth, 0, 
                     (int)compPaperWidth, (int)sixthCompPaperHeight);

         double twoNinthCompPaperWidth = 2.0*ninthCompPaperWidth;

         drawMajorTic(g2, compPaperWidth, 0.0, cg.getComponent());
         drawMajorTic(g2, 0.0, compPaperHeight, cg.getComponent());
         drawMajorTic(g2, compPaperWidth, halfCompPaperHeight, 
            cg.getComponent());
         drawMajorTic(g2, 0, 0, cg.getComponent());
         drawMajorTic(g2, twoThirdCompPaperWidth, 0.0, cg.getComponent());
         drawMajorTic(g2, twoThirdCompPaperWidth, thirdCompPaperHeight, 
            cg.getComponent());
         drawMajorTic(g2, compPaperWidth, sixthCompPaperHeight, 
            cg.getComponent());
         drawMajorTic(g2, eightNinthCompPaperWidth, ninthCompPaperHeight, 
            cg.getComponent());
         drawMajorTic(g2, eightNinthCompPaperWidth, 4.0*ninthCompPaperHeight, 
            cg.getComponent());
         drawMajorTic(g2, eightNinthCompPaperWidth, 5.0*ninthCompPaperHeight, 
            cg.getComponent());
         drawMajorTic(g2, twoThirdCompPaperWidth, ninthCompPaperHeight, 
            cg.getComponent());
         drawMajorTic(g2, twoNinthCompPaperWidth, ninthCompPaperHeight, 
            cg.getComponent());
         drawMajorTic(g2, twoNinthCompPaperWidth, sevenNinthCompPaperHeight, 
            cg.getComponent());
         drawMajorTic(g2, 4.0*ninthCompPaperWidth, sevenNinthCompPaperHeight, 
            cg.getComponent());
      }
      else
      {
         g2.drawLine(0,0, (int)compPaperWidth, (int)compPaperHeight);
         g2.drawLine(0, (int)halfCompPaperHeight, 
           (int)compPaperWidth, (int)compPaperHeight);
         g2.drawLine(0, (int)halfCompPaperHeight, (int)compPaperWidth, 0);

         g2.drawLine((int)thirdCompPaperWidth, 0, 
                     (int)thirdCompPaperWidth, (int)thirdCompPaperHeight);

         g2.drawLine((int)thirdCompPaperWidth, 0, 
                     0, (int)sixthCompPaperHeight);

         drawMajorTic(g2, 0, 0, cg.getComponent());
         drawMajorTic(g2, compPaperWidth, compPaperHeight, cg.getComponent());
         drawMajorTic(g2, 0.0, halfCompPaperHeight, cg.getComponent());
         drawMajorTic(g2, compPaperWidth, 0.0, cg.getComponent());
         drawMajorTic(g2, thirdCompPaperWidth, 0.0, cg.getComponent());
         drawMajorTic(g2, thirdCompPaperWidth, thirdCompPaperHeight, 
          cg.getComponent());
         drawMajorTic(g2, 0.0, sixthCompPaperHeight, cg.getComponent());

         drawMajorTic(g2, ninthCompPaperWidth, ninthCompPaperHeight,
           cg.getComponent());

         drawMajorTic(g2, ninthCompPaperWidth, 4.0*ninthCompPaperHeight,
           cg.getComponent());

         drawMajorTic(g2, ninthCompPaperWidth, 5.0*ninthCompPaperHeight,
           cg.getComponent());

         drawMajorTic(g2, sevenNinthCompPaperWidth, ninthCompPaperHeight,
           cg.getComponent());

         drawMajorTic(g2, thirdCompPaperWidth, ninthCompPaperHeight,
           cg.getComponent());

         drawMajorTic(g2, sevenNinthCompPaperWidth, sevenNinthCompPaperHeight,
           cg.getComponent());

         drawMajorTic(g2, 5.0*ninthCompPaperWidth, sevenNinthCompPaperHeight,
           cg.getComponent());
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
      JDRTschicholdGrid rectGrid = (JDRTschicholdGrid)grid;
      majorDivisions = rectGrid.majorDivisions;
      subDivisions = rectGrid.subDivisions;
      unit = rectGrid.unit;
   }

   public JDRRectangularGrid getRectangularGrid()
   {
      return new JDRRectangularGrid(getCanvasGraphics(),
         unit, majorDivisions, subDivisions);
   }

   private static JDRTschicholdGridListener listener = new JDRTschicholdGridListener();

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
