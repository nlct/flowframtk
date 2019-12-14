// File          : JDRRadialGrid.java
// Description   : Represents a radial grid.
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
import java.awt.image.ImageObserver;
import java.awt.geom.*;
import java.text.*;

import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.*;

/**
 * Class representing a radial grid. The origin is on the centre of
 * the page.
 * @author Nicola L C Talbot
 */
public class JDRRadialGrid extends JDRGrid
{
   /**
    * Initialises using the default settings. The defaults are:
    * bp units, major interval width of 100 units, 10
    * sub-divisions per major interval and 8 spokes.
    */
   public JDRRadialGrid(CanvasGraphics cg)
   {
      super(cg, GRID_RADIAL);
      set(JDRUnit.bp, 100.0, 10, 8);
   }

   /**
    * Initialises using the given settings.
    * @param gridUnit the grid units
    * @param majorDiv the width of the major interval in terms of
    * the grid unit (must be greater than 0)
    * @param subDiv the number of sub-divisions per major interval
    * (can't be negative)
    * @param gridSpokes the number of spokes in the grid (can't be
    * negative)
    */
   public JDRRadialGrid(CanvasGraphics cg,
     JDRUnit gridUnit, double majorDiv, int subDiv, int gridSpokes)
   {
      super(cg, GRID_RADIAL);
      set(gridUnit, majorDiv, subDiv, gridSpokes);
   }

   public Object clone()
   {
      return new JDRRadialGrid(getCanvasGraphics(),
         unit, majorDivisions, subDivisions, spokes);
   }

   /**
    * Change the grid settings settings.
    * @param gridUnit the grid units
    * @param majorDiv the width of the major interval in terms of
    * the grid unit (must be greater than 0)
    * @param subDiv the number of sub-divisions per major interval
    * (can't be negative)
    * @param gridSpokes the number of spokes in the grid (can't be
    * negative)
    */
   public void set(JDRUnit gridUnit, double majorDiv, int subDiv,
     int gridSpokes)
   {
      setUnit(gridUnit);
      setMajorInterval(majorDiv);
      setSubDivisions(subDiv);
      setSpokes(gridSpokes);
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
    * grid's unit (must be greater than 0)
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
    * @param subDiv the number of sub divisions (can't be negative)
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
    * Sets the number of spokes.
    * @param gridSpokes the number of spokes in the grid (can't be
    * negative)
    */
   public void setSpokes(int gridSpokes)
   {
      if (gridSpokes < 0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.GRID_SPOKES, gridSpokes, 
            getCanvasGraphics());
      }

      spokes = gridSpokes;
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

   /**
    * Gets the number of spokes.
    * @return the number of spokes
    */
   public int getSpokes()
   {
      return spokes;
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
         double minor = unit.toBp(majorDivisions)/subDivisions;

         p.setLocation(minor, minor);
      }

      return p;
   }

   public Point2D fromCartesianBp(double bpX, double bpY)
   {
      JDRPaper paper = getCanvasGraphics().getPaper();

      JDRRadialPoint p = new JDRRadialPoint(
         getCanvasGraphics().getMessageSystem());

      p.setLocation(
         unit.fromBp(bpX-0.5*paper.getWidth()),
         unit.fromBp(bpY-0.5*paper.getHeight()));

      return p;
   }

   public Point2D fromCartesian(double storageX, double storageY)
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRPaper paper = cg.getPaper();
      JDRUnit storageUnit = cg.getStorageUnit();

      JDRRadialPoint p = new JDRRadialPoint(cg.getMessageSystem());

      p.setLocation(
         unit.fromUnit(storageX-storageUnit.fromBp(0.5*paper.getWidth()), storageUnit),
         unit.fromUnit(storageY-storageUnit.fromBp(0.5*paper.getHeight()), storageUnit));

      return p;
   }

   public void fromCartesianBp(Point2D cartesianPoint, Point2D target)
   {
      JDRPaper paper = getCanvasGraphics().getPaper();

      target.setLocation(
         unit.fromBp(cartesianPoint.getX()-0.5*paper.getWidth()),
         unit.fromBp(cartesianPoint.getY()-0.5*paper.getHeight()));
   }

   public void toCartesianBp(Point2D original, Point2D target)
   {
      JDRPaper paper = getCanvasGraphics().getPaper();

      // Shift the origin to the top left corner

      target.setLocation(
         unit.toBp(original.getX())+0.5*paper.getWidth(),
         unit.toBp(original.getY())+0.5*paper.getHeight());
   }

   public void toCartesian(Point2D original, Point2D target)
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRPaper paper = cg.getPaper();
      JDRUnit storageUnit = cg.getStorageUnit();

      // Shift the origin to the top left corner

      target.setLocation(
         unit.toUnit(original.getX(), storageUnit)
           +storageUnit.fromBp(0.5*paper.getWidth()),
         unit.toUnit(original.getY(), storageUnit)
           +storageUnit.fromBp(0.5*paper.getHeight()));
   }

   public void drawGrid()
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRPaper paper = cg.getPaper();
      Graphics2D g = cg.getGraphics();

      if (g == null)
      {
         return;
      }

      AffineTransform oldAf = g.getTransform();

      double scaleX = cg.bpToComponentX(1.0);
      double scaleY = cg.bpToComponentY(1.0);

      double major = unit.toBp(getMajorInterval());
      double minor = getSubDivisions();

      if (minor > 0)
      {
         minor = major/minor;
      }

      double maxRadius;

      double halfW = 0.5*paper.getWidth();
      double halfH = 0.5*paper.getHeight();

      g.translate(scaleX*halfW, scaleY*halfH);

      Rectangle clip = g.getClipBounds();

      double minX, minY, maxX, maxY;

      if (clip == null)
      {
         minX = -halfW;
         minY = -halfH;
         maxX = halfW;
         maxY = halfH;
      }
      else
      {
         minX = cg.componentXToBp(clip.getX());
         minY = cg.componentYToBp(clip.getY());
         maxX = minX + cg.componentXToBp(clip.getWidth());
         maxY = minY + cg.componentYToBp(clip.getHeight());
      }

      minX -= HALF_MAJOR_TIC;
      minY -= HALF_MAJOR_TIC;
      maxX += HALF_MAJOR_TIC;
      maxY += HALF_MAJOR_TIC;

      ImageObserver obs = cg.getComponent();

      if (minY <= 0 && maxY >= 0 && minX <= 0 && maxX >= 0)
      {
         drawMajorTic(g, 0, 0, obs);

         drawAllSpokes(g, scaleX, scaleY, minX, minY, maxX, maxY, 
          major, minor, obs);
      }
      else if (minY >= 0 && minX >= 0)
      {
         drawRegion1Spokes(g, scaleX, scaleY, minX, minY, maxX, maxY,
          major, minor, obs);
      }
      else if (minY >= 0 && maxX < 0)
      {
         drawRegion2Spokes(g, scaleX, scaleY, minX, minY, maxX, maxY,
          major, minor, obs);
      }
      else if (maxY <= 0 && maxX <= 0)
      {
         drawRegion3Spokes(g, scaleX, scaleY, minX, minY, maxX, maxY,
          major, minor, obs);
      }
      else if (maxY <= 0 && minX > 0)
      {
         drawRegion4Spokes(g, scaleX, scaleY, minX, minY, maxX, maxY,
          major, minor, obs);
      }
      else if (minY > 0 && maxX > 0 && minX < 0)
      {
         drawRegion1and2Spokes(g, scaleX, scaleY, minX, minY, maxX, maxY,
          major, minor, obs);
      }
      else if (minX < 0 && maxY > 0 && minY < 0)
      {
         drawRegion2and3Spokes(g, scaleX, scaleY, minX, minY, maxX, maxY,
          major, minor, obs);
      }
      else if (maxY < 0 && minX < 0 && maxX > 0)
      {
         drawRegion3and4Spokes(g, scaleX, scaleY, minX, minY, maxX, maxY,
          major, minor, obs);
      }
      else if (minX > 0 && minY < 0 && maxY > 0)
      {
         drawRegion4and1Spokes(g, scaleX, scaleY, minX, minY, maxX, maxY,
          major, minor, obs);
      }
      else
      {
         // this shouldn't happen

         drawAllSpokes(g, scaleX, scaleY, minX, minY, maxX, maxY, 
          major, minor, obs);
      }

      g.setTransform(oldAf);

   }

   private void drawRegion1Spokes(Graphics2D g, double scaleX, double scaleY,
     double minX, double minY, double maxX, double maxY, 
     double major, double minor, ImageObserver obs)
   {
      JDRAngle spokeAngle = getSpokeAngle();

      double radSpokeAngle = spokeAngle.toRadians();

      double minAngle = 0;

      if (minY > 0)
      {
         minAngle = Math.atan2(minY, maxX);

         int n = (int)Math.floor(minAngle/radSpokeAngle);

         minAngle = n*radSpokeAngle;
      }

      double maxAngle = HALF_PI;

      if (minX > 0)
      {
         maxAngle = Math.atan2(maxY, minX);

         int n = (int)Math.ceil(maxAngle/radSpokeAngle);

         maxAngle = n*radSpokeAngle;
      }

      double minRadius = Math.sqrt(minX*minX + minY*minY);

      minRadius = Math.floor(minRadius/major)*major;

      double maxRadius = Math.sqrt(maxX*maxX + maxY*maxY);

      maxRadius = Math.ceil(maxRadius/major)*major;

      forEachSpoke(g, minAngle, maxAngle, minRadius, maxRadius, spokeAngle,
         major, minor, scaleX, scaleY, obs);
   }

   private void drawRegion2Spokes(Graphics2D g, double scaleX, double scaleY,
     double minX, double minY, double maxX, double maxY, 
     double major, double minor, ImageObserver obs)
   {
      JDRAngle spokeAngle = getSpokeAngle();

      double radSpokeAngle = spokeAngle.toRadians();

      double minAngle = HALF_PI;

      if (maxX < 0)
      {
         minAngle = Math.atan2(maxY, maxX);

         int n = (int)Math.floor(minAngle/radSpokeAngle);

         minAngle = n*radSpokeAngle;
      }

      double maxAngle = Math.PI;

      if (minY > 0)
      {
         maxAngle = Math.atan2(minY, minX);

         int n = (int)Math.ceil(maxAngle/radSpokeAngle);

         maxAngle = n*radSpokeAngle;
      }

      double minRadius = Math.sqrt(maxX*maxX + minY*minY);

      minRadius = Math.floor(minRadius/major)*major;

      double maxRadius = Math.sqrt(minX*minX + maxY*maxY);

      maxRadius = Math.ceil(maxRadius/major)*major;

      forEachSpoke(g, minAngle, maxAngle, minRadius, maxRadius, spokeAngle,
         major, minor, scaleX, scaleY, obs);
   }

   private void drawRegion3Spokes(Graphics2D g, double scaleX, double scaleY,
     double minX, double minY, double maxX, double maxY, 
     double major, double minor, ImageObserver obs)
   {
      JDRAngle spokeAngle = getSpokeAngle();

      double radSpokeAngle = spokeAngle.toRadians();

      double minAngle = Math.PI;

      if (maxY < 0)
      {
         minAngle = Math.atan2(maxY, minX);

         int n = (int)Math.floor(minAngle/radSpokeAngle);

         minAngle = n*radSpokeAngle;
      }

      double maxAngle = ONE_HALF_PI;

      if (maxX < 0)
      {
         maxAngle = Math.atan2(minY, maxX);

         int n = (int)Math.ceil(maxAngle/radSpokeAngle);

         maxAngle = n*radSpokeAngle;
      }

      double minRadius = Math.sqrt(maxX*maxX + maxY*maxY);

      minRadius = Math.floor(minRadius/major)*major;

      double maxRadius = Math.sqrt(minX*minX + minY*minY);

      maxRadius = Math.ceil(maxRadius/major)*major;

      forEachSpoke(g, minAngle, maxAngle, minRadius, maxRadius, spokeAngle,
         major, minor, scaleX, scaleY, obs);
   }

   private void drawRegion4Spokes(Graphics2D g, double scaleX, double scaleY,
     double minX, double minY, double maxX, double maxY, 
     double major, double minor, ImageObserver obs)
   {
      JDRAngle spokeAngle = getSpokeAngle();

      double radSpokeAngle = spokeAngle.toRadians();

      double minAngle = ONE_HALF_PI;

      if (minX > 0)
      {
         minAngle = Math.atan2(minY, minX);

         int n = (int)Math.floor(minAngle/radSpokeAngle);

         minAngle = n*radSpokeAngle;
      }

      double maxAngle = TWO_PI;

      if (maxY < 0)
      {
         maxAngle = Math.atan2(maxY, maxX);

         int n = (int)Math.ceil(maxAngle/radSpokeAngle);

         maxAngle = n*radSpokeAngle;
      }

      double minRadius = Math.sqrt(minX*minX + maxY*maxY);

      minRadius = Math.floor(minRadius/major)*major;

      double maxRadius = Math.sqrt(maxX*maxX + minY*minY);

      maxRadius = Math.ceil(maxRadius/major)*major;

      forEachSpoke(g, minAngle, maxAngle, minRadius, maxRadius, spokeAngle,
         major, minor, scaleX, scaleY, obs);
   }

   private void drawRegion1and2Spokes(Graphics2D g, double scaleX, double scaleY,
     double minX, double minY, double maxX, double maxY, 
     double major, double minor, ImageObserver obs)
   {
      JDRAngle spokeAngle = getSpokeAngle();

      double radSpokeAngle = spokeAngle.toRadians();

      double minAngle = Math.atan2(minY, maxX);

      int n = (int)Math.floor(minAngle/radSpokeAngle);

      minAngle = n*radSpokeAngle;

      double maxAngle = Math.atan2(minY, minX);

      n = (int)Math.ceil(maxAngle/radSpokeAngle);

      maxAngle = n*radSpokeAngle;

      double minRadius = Math.abs(minY);

      minRadius = Math.floor(minRadius/major)*major;

      double maxYsq = maxY*maxY;

      double maxRadius = Math.sqrt(Math.max(
                         maxX*maxX+maxYsq,
                         minX*minX+maxYsq));

      maxRadius = Math.ceil(maxRadius/major)*major;

      forEachSpoke(g, minAngle, maxAngle, minRadius, maxRadius, spokeAngle,
         major, minor, scaleX, scaleY, obs);
   }

   private void drawRegion2and3Spokes(Graphics2D g, double scaleX, double scaleY,
     double minX, double minY, double maxX, double maxY, 
     double major, double minor, ImageObserver obs)
   {
      JDRAngle spokeAngle = getSpokeAngle();

      double radSpokeAngle = spokeAngle.toRadians();

      double minAngle = Math.atan2(maxY, maxX);

      int n = (int)Math.floor(minAngle/radSpokeAngle);

      minAngle = n*radSpokeAngle;

      double maxAngle = Math.atan2(minY, maxX);

      n = (int)Math.ceil(maxAngle/radSpokeAngle);

      maxAngle = n*radSpokeAngle;

      double minRadius = Math.abs(maxX);

      minRadius = Math.floor(minRadius/major)*major;

      double minXsq = minX*minX;

      double maxRadius = Math.sqrt(Math.max(
                         maxY*maxY+minXsq,
                         minY*minY+minXsq));

      maxRadius = Math.ceil(maxRadius/major)*major;

      forEachSpoke(g, minAngle, maxAngle, minRadius, maxRadius, spokeAngle,
         major, minor, scaleX, scaleY, obs);
   }

   private void drawRegion3and4Spokes(Graphics2D g, double scaleX, double scaleY,
     double minX, double minY, double maxX, double maxY, 
     double major, double minor, ImageObserver obs)
   {
      JDRAngle spokeAngle = getSpokeAngle();

      double radSpokeAngle = spokeAngle.toRadians();

      double minAngle = Math.atan2(maxY, minX);

      int n = (int)Math.floor(minAngle/radSpokeAngle);

      minAngle = n*radSpokeAngle;

      double maxAngle = Math.atan2(maxY, maxX);

      n = (int)Math.ceil(maxAngle/radSpokeAngle);

      maxAngle = n*radSpokeAngle;

      double minRadius = Math.abs(maxY);

      minRadius = Math.floor(minRadius/major)*major;

      double minYsq = minY*minY;

      double maxRadius = Math.sqrt(Math.max(
                         maxX*maxX+minYsq,
                         minX*minX+minYsq));

      maxRadius = Math.ceil(maxRadius/major)*major;

      forEachSpoke(g, minAngle, maxAngle, minRadius, maxRadius, spokeAngle,
         major, minor, scaleX, scaleY, obs);
   }

   private void drawRegion4and1Spokes(Graphics2D g, double scaleX, double scaleY,
     double minX, double minY, double maxX, double maxY, 
     double major, double minor, ImageObserver obs)
   {
      JDRAngle spokeAngle = getSpokeAngle();

      double radSpokeAngle = spokeAngle.toRadians();

      double minAngle = Math.atan2(minY, minX);

      int n = (int)Math.floor(minAngle/radSpokeAngle);

      minAngle = n*radSpokeAngle;

      double maxAngle = Math.atan2(maxY, minX);

      n = (int)Math.ceil(maxAngle/radSpokeAngle);

      maxAngle = n*radSpokeAngle;

      double minRadius = Math.abs(minX);

      minRadius = Math.floor(minRadius/major)*major;

      double maxXsq = maxX*maxX;

      double maxRadius = Math.sqrt(Math.max(
                         maxY*maxY+maxXsq,
                         minY*minY+maxXsq));

      maxRadius = Math.ceil(maxRadius/major)*major;

      forEachSpoke(g, minAngle, TWO_PI, minRadius, maxRadius, spokeAngle,
         major, minor, scaleX, scaleY, obs);

      forEachSpoke(g, 0, maxAngle, minRadius, maxRadius, spokeAngle,
         major, minor, scaleX, scaleY, obs);
   }

   private void forEachSpoke(Graphics2D g, double minAngle, double maxAngle,
     double minRadius, double maxRadius, JDRAngle spokeAngle,
     double major, double minor, double scaleX, double scaleY,
     ImageObserver obs)
   {
      JDRMessageDictionary dict = getCanvasGraphics().getMessageDictionary();

      JDRAngle angle = new JDRAngle(dict, minAngle, JDRAngle.RADIAN);
      double radAngle = minAngle;

      JDRRadialPoint rpt   = new JDRRadialPoint(dict);
      Point2D.Double point = new Point2D.Double();

      for (; radAngle <= maxAngle; angle.add(spokeAngle))
      {
         radAngle = angle.toRadians();

         rpt.setParameters(0.0, angle);

         for (double radius = minRadius; radius <= maxRadius;
              radius += major)
         {
            rpt.setRadius(radius);

            if (subDivisions == 0)
            {
               drawMajorTic(g, rpt.getX()*scaleX,
                               rpt.getY()*scaleY,
                            obs);
            }
            else
            {
               if (radius > 0.0)
               {
                  drawMajorTic(g, rpt.getX()*scaleX,
                                  rpt.getY()*scaleY,
                               obs);
               }

               for (int i = 1; i < subDivisions; i++)
               {
                  rpt.setRadius(radius+i*minor);

                  drawMinorTic(g, (int)(rpt.getX()*scaleX),
                                  (int)(rpt.getY()*scaleY),
                               obs);
               }
            }
         }
      }
   }

   private void drawAllSpokes(Graphics2D g, double scaleX, double scaleY,
     double minX, double minY, double maxX, double maxY, 
     double major, double minor, ImageObserver obs)
   {
      JDRMessage msgSys = getCanvasGraphics().getMessageSystem();

      JDRAngle spokeAngle = getSpokeAngle();

      JDRRadialPoint rpt = new JDRRadialPoint(msgSys);
      Point2D.Double point = new Point2D.Double();

      JDRAngle angle = new JDRAngle(msgSys, 0.0, JDRAngle.RADIAN);
      double radAngle = 0.0;

      double maxRadius = Math.sqrt(Math.max(
        maxX*maxX + maxY*maxY,
        minX*minX + minY*minY));

      for (; radAngle < TWO_PI; angle.add(spokeAngle))
      {
         radAngle = angle.toRadians();

         rpt.setParameters(0.0, angle);

         for (double radius = 0.0; radius <= maxRadius;
              radius += major)
         {
            rpt.setRadius(radius);

            if (subDivisions == 0)
            {
               drawMajorTic(g, rpt.getX()*scaleX,
                               rpt.getY()*scaleY,
                            obs);
            }
            else
            {
               if (radius > 0.0)
               {
                  drawMajorTic(g, rpt.getX()*scaleX,
                                  rpt.getY()*scaleY,
                               obs);
               }

               for (int i = 1; i < subDivisions; i++)
               {
                  rpt.setRadius(radius+i*minor);

                  drawMinorTic(g, (int)(rpt.getX()*scaleX),
                                  (int)(rpt.getY()*scaleY),
                               obs);
               }
            }
         }
      }
   }

   /**
    * Gets the angle between spokes.
    * @return the angle between spokes.
    */
   public JDRAngle getSpokeAngle()
   {
      return new JDRAngle(getCanvasGraphics().getMessageSystem(), 
         2.0*Math.PI/spokes, JDRAngle.RADIAN);
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
      JDRRadialPoint rpt = (JDRRadialPoint)fromCartesianBp(bpX, bpY);

      DecimalFormat f = new DecimalFormat(
        unit.getID() == JDRUnit.BP ? "0" : "0.00");

      return f.format(rpt.getRadius())+unit.getLabel()+","
            +f.format(rpt.getAngle().toDegrees())+"°";
   }

   public Point2D getClosestBpTic(double x, double y)
   {
      // Convert the original point to radial co-ordinates

      JDRRadialPoint point = (JDRRadialPoint)fromCartesianBp(x,y);

      // Determine the closest spoke

      JDRAngle angle = getSpokeAngle();

      int n = (int)Math.round(point.getAngle().toRadians()/angle.toRadians());

      // Closest spoke is the nth spoke

      angle.scale(n);
      point.setAngle(angle);

      // Now find the closest tic mark along this slope

      if (subDivisions > 0)
      {
         n = (int)Math.floor(point.getRadius()/majorDivisions);

         double R = n*majorDivisions;

         // lies between n and n+1 major tick mark along this slope

         double r0 = point.getRadius() - R;

         // Compute the length of each sub-division

         double length = majorDivisions/subDivisions;

         int n0 = (int)Math.round(r0/length);

         // point is closest to the n0 minor tick along this
         // interval.

         point.setRadius(R + n0*length);
      }
      else
      {
         n = (int)Math.round(point.getRadius()/majorDivisions);

         point.setRadius(n*majorDivisions);
      }

      // Get this point in Cartesian co-ordinates

      Point2D target = new Point2D.Double();

      toCartesianBp(point, target);

      return target;
   }

   public Point2D getClosestTic(double x, double y)
   {
      // Convert the original point to radial co-ordinates

      JDRRadialPoint point = (JDRRadialPoint)fromCartesian(x,y);

      // Determine the closest spoke

      JDRAngle angle = getSpokeAngle();

      int n = (int)Math.round(point.getAngle().toRadians()/angle.toRadians());

      // Closest spoke is the nth spoke

      angle.scale(n);
      point.setAngle(angle);

      // Now find the closest tic mark along this slope

      if (subDivisions > 0)
      {
         n = (int)Math.floor(point.getRadius()/majorDivisions);

         double R = n*majorDivisions;

         // lies between n and n+1 major tick mark along this slope

         double r0 = point.getRadius() - R;

         // Compute the length of each sub-division

         double length = majorDivisions/subDivisions;

         int n0 = (int)Math.round(r0/length);

         // point is closest to the n0 minor tick along this
         // interval.

         point.setRadius(R + n0*length);
      }
      else
      {
         n = (int)Math.round(point.getRadius()/majorDivisions);

         point.setRadius(n*majorDivisions);
      }

      // Get this point in Cartesian co-ordinates

      Point2D target = new Point2D.Double();

      toCartesian(point, target);

      return target;
   }

   public JDRGridLoaderListener getListener()
   {
      return listener;
   }

   public void makeEqual(JDRGrid grid)
   {
      setCanvasGraphics(grid.getCanvasGraphics());
      JDRRadialGrid radGrid = (JDRRadialGrid)grid;
      majorDivisions = radGrid.majorDivisions;
      subDivisions = radGrid.subDivisions;
      unit = radGrid.unit;
      spokes = radGrid.spokes;
   }

   public JDRRectangularGrid getRectangularGrid()
   {
      return new JDRRectangularGrid(getCanvasGraphics(),
         unit, majorDivisions, subDivisions);
   }

   private static JDRRadialGridListener listener = new JDRRadialGridListener();

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

   /**
    * Stores the number of spokes.
    */
   private int spokes;

   /**
    * The unit used by the grid.
    */
   private JDRUnit unit;

   public static double HALF_PI = 0.5*Math.PI;
   public static double ONE_HALF_PI = 1.5*Math.PI;
   public static double TWO_PI  = 2*Math.PI;

}
