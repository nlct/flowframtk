// File          : JDRGrid.java
// Description   : Represents a grid.
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
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.geom.*;

import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.*;

/**
 * Class representing a grid.
 * @author Nicola L C Talbot
 */
public abstract class JDRGrid implements Cloneable
{
   public JDRGrid(CanvasGraphics cg, byte id)
   {
      this.id = id;
      this.canvasGraphics = cg;
   }

   private JDRGrid()
   {
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      this.canvasGraphics = cg;
   }

   /**
    * Draws a minor tic mark. Note that this doesn't set the colour.
    * @param g the graphics device
    * @param x the x co-ordinate of the centre of the tic mark
    * @param y the y co-ordinate of the centre of the tic mark
    * @see #drawMajorTic(Graphics,int,int)
    */
   public void drawMinorTic(Graphics g, int x, int y, ImageObserver obs)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (minorTicImage != null)
      {
         g.drawImage(minorTicImage, x-HALF_MINOR_TIC, y-HALF_MINOR_TIC, obs);
      }
      else if (cg.getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         Graphics2D imgGraphics = null;

         try
         {
            minorTicImage = new BufferedImage(2*HALF_MINOR_TIC+1, 
               2*HALF_MINOR_TIC+1, BufferedImage.TYPE_INT_ARGB);

            imgGraphics = minorTicImage.createGraphics();

            RenderingHints renderHints =
               new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_ON);

            renderHints.add(new RenderingHints(
                              RenderingHints.KEY_RENDERING,
                              RenderingHints.VALUE_RENDER_QUALITY));

            imgGraphics.setRenderingHints(renderHints);

            drawMinorTic(imgGraphics, HALF_MINOR_TIC, HALF_MINOR_TIC);

            imgGraphics.dispose();
            imgGraphics = null;
            g.drawImage(minorTicImage, x-HALF_MINOR_TIC, y-HALF_MINOR_TIC, obs);
         }
         catch (OutOfMemoryError e)
         {
            minorTicImage = null;
            cg.setOptimize(CanvasGraphics.OPTIMIZE_NONE);
            drawMinorTic(g, x, y);
         }
         finally
         {
            if (imgGraphics != null)
            {
               imgGraphics.dispose();
            }
         }
      }
      else
      {
         drawMinorTic(g, x, y);
      }
   }

   public void drawMinorTic(Graphics g, int x, int y)
   {
      g.setColor(minorGridColor);
/*
 * Sometimes this throws a null pointer exception
 * from sun.java2d.pipe.LoopPipe.drawLine
 * Maybe related to
 * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7153339
 * If so, it should be fixed in Java 8, but check for null pointer
 * exception and ignore.
 */
      try
      {
         g.drawLine(x-HALF_MINOR_TIC, y, x+HALF_MINOR_TIC, y);
         g.drawLine(x, y-HALF_MINOR_TIC, x, y+HALF_MINOR_TIC);
      }
      catch (NullPointerException e)
      {
         canvasGraphics.debugMessage(e);
      }
   }

   public void drawMinorTic(Graphics g, double x, double y)
   {
      drawMinorTic(g, (int)Math.round(x), (int)Math.round(y));
   }

   public void drawMinorTic(Graphics g, double x, double y, ImageObserver obs)
   {
      drawMinorTic(g, (int)Math.round(x), (int)Math.round(y), obs);
   }

   /**
    * Draws a major tic mark. Note that this doesn't set the colour.
    * @param g the graphics device
    * @param x the x co-ordinate of the centre of the tic mark
    * @param y the y co-ordinate of the centre of the tic mark
    * @see #drawMinorTic(Graphics,int,int)
    */
   public void drawMajorTic(Graphics g, int x, int y)
   {
      g.setColor(majorGridColor);
// See above
      try
      {
         g.drawLine(x-HALF_MAJOR_TIC, y, x+HALF_MAJOR_TIC, y);
         g.drawLine(x, y-HALF_MAJOR_TIC, x, y+HALF_MAJOR_TIC);
      }
      catch (NullPointerException e)
      {
         canvasGraphics.debugMessage(e);
      }
   }

   public void drawMajorTic(Graphics g, int x, int y, ImageObserver obs)
   {
      if (majorTicImage != null)
      {
         g.drawImage(majorTicImage, x-HALF_MAJOR_TIC, y-HALF_MAJOR_TIC, obs);
      }
      else if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         Graphics2D imgGraphics = null;

         try
         {
            majorTicImage = new BufferedImage(2*HALF_MAJOR_TIC+1, 
               2*HALF_MAJOR_TIC+1, BufferedImage.TYPE_INT_ARGB);

            imgGraphics = majorTicImage.createGraphics();

            RenderingHints renderHints =
               new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_OFF);

            renderHints.add(new RenderingHints(
                              RenderingHints.KEY_RENDERING,
                              RenderingHints.VALUE_RENDER_SPEED));

            imgGraphics.setRenderingHints(renderHints);

            drawMajorTic(imgGraphics, HALF_MAJOR_TIC, HALF_MAJOR_TIC);

            imgGraphics.dispose();
            imgGraphics = null;
            g.drawImage(majorTicImage, x-HALF_MAJOR_TIC, y-HALF_MAJOR_TIC, obs);
         }
         catch (OutOfMemoryError e)
         {
            majorTicImage = null;
            getCanvasGraphics().setOptimize(CanvasGraphics.OPTIMIZE_NONE);
            drawMajorTic(g, x, y);
         }
         finally
         {
            if (imgGraphics != null)
            {
               imgGraphics.dispose();
            }
         }
      }
      else
      {
         drawMajorTic(g, x, y);
      }
   }

   public void drawMajorTic(Graphics g, double x, double y)
   {
      drawMajorTic(g, (int)Math.round(x), (int)Math.round(y));
   }

   public void drawMajorTic(Graphics g, double x, double y, ImageObserver obs)
   {
      drawMajorTic(g, (int)Math.round(x), (int)Math.round(y), obs);
   }

   /**
    * Draws the grid.
    */
   public abstract void drawGrid();

   /**
    * Converts a co-ordinate from this space to left-handed
    * Cartesian system.
    * @param original the original point in this space
    * @param target the target point (bp) in left-handed Cartesian space
    */
   public abstract void toCartesianBp(Point2D original,
      Point2D target);

   /**
    * Converts a co-ordinate from left-handed Cartesian space to
    * this space.
    * @param cartesianPoint the original point (bp)
    * @param target the target point in this space.
    */
   public abstract void fromCartesianBp(Point2D cartesianPoint,
      Point2D target);

   /**
    * Converts a co-ordinate from left-handed Cartesian space to
    * this space.
    * @param x the x co-ordinate of the original point (bp)
    * @param y the y co-ordinate of the original point (bp)
    */
   public abstract Point2D fromCartesianBp(double x,
      double y);

   /**
    * Gets the default shift in bp. This is used by copy and paste
    * to offset the pasted objects.
    */
   public Point2D getDefaultOffset()
   {
      return getMinorTicDistance();
   }

   /**
    * Gets the distance between minor intervals or 0 if no sub
    * divisions.
    * @return the x and y distance (in bp) between minor intervals or 0 if no sub
    * divisions
    */
   public abstract Point2D getMinorTicDistance();

   /**
    * Gets the distance between major intervals.
    * @return the x and y distance (in bp) between major intervals
    */
   public abstract Point2D getMajorTicDistance();

   /**
    * Gets the closest tick mark to the given bp point.
    * @param x the x co-ordinate of the point (bp)
    * @param y the y co-ordinate of the point (bp)
    * @return the closest tick mark (in bp) to the given point in
    * left-handed Cartesian space.
    */
   public abstract Point2D getClosestBpTic(double x, double y);

   /**
    * Gets the closest tick mark to the given point.
    * @param x the x co-ordinate of the point
    * @param y the y co-ordinate of the point
    * @return the closest tick mark (in the given unit) to the given point in
    * left-handed Cartesian space.
    */
   public abstract Point2D getClosestTic(double x, double y);

   /**
    * Gets the unit label associated with this grid.
    */
   public abstract String getUnitLabel();

   /**
    * Gets the principle unit associated with this grid.
    */
   public abstract JDRUnit getMainUnit();

   /**
    * Gets the number of sub divisions within the major interval.
    * If the grid supports different x and y sub-divisions then this
    * method should return the number of x sub-divisions.
    * @return the number of sub divisions.
    */
   public abstract int getSubDivisions();

   /**
    * Sets the number of sub divisions within the major interval.
    * If the grid supports different x and y sub-divisions then this
    * method should set both.
    * @param subDivisions the number of sub divisions.
    */
   public abstract void setSubDivisions(int subDivisions);

   /**
    * Sets the number of sub divisions within the major x interval.
    * If the grid doesn't support different x and y sub-divisions then this
    * method should just use setSubDivisions(int).
    * @param subDivisions the number of x sub divisions.
    */
   public void setSubDivisionsX(int subDivisions)
   {
      setSubDivisions(subDivisions);
   }

   /**
    * Sets the number of sub divisions within the major y interval.
    * If the grid doesn't support different x and y sub-divisions then this
    * method should do nothing.
    * @param subDivisions the number of y sub divisions.
    */
   public void setSubDivisionsY(int subDivisions)
   {
   }

   /**
    * Gets the number of sub divisions within the major x interval.
    * If the grid doesn't support different x and y sub-divisions then this
    * method should return getSubDivisions().
    * @return the number of sub divisions.
    */
   public int getSubDivisionsX()
   {
      return getSubDivisions();
   }

   /**
    * Gets the number of sub divisions within the major y interval.
    * If the grid doesn't support different x and y sub-divisions then this
    * method should return getSubDivisions().
    * @return the number of sub divisions.
    */
   public int getSubDivisionsY()
   {
      return getSubDivisions();
   }

   /**
    * Returns a formatted string representing the given point.
    */
   public abstract String formatLocationFromCartesianBp(double bpX, double bpY);

   public abstract Object clone();

   public abstract void makeEqual(JDRGrid grid);

   public byte getID()
   {
      return id;
   }

   public static JDRGrid getGrid(CanvasGraphics cg, byte gridId)
   {
      switch (gridId)
      {
         case GRID_RECTANGULAR: return new JDRRectangularGrid(cg);
         case GRID_RADIAL: return new JDRRadialGrid(cg);
         case GRID_ISO: return new JDRIsoGrid(cg);
         case GRID_TSCHICHOLD: return new JDRTschicholdGrid(cg);
      }

      throw new JdrIllegalArgumentException(
         JdrIllegalArgumentException.GRID_ID, gridId, cg);
   }

   public String getName()
   {
      String name = getClass().getName();

      if (canvasGraphics == null)
      {
         return name;
      }

      return canvasGraphics.getMessageWithFallback("class."+name, name);
   }

   public abstract JDRRectangularGrid getRectangularGrid();

   /**
    * Gets the listener for this grid.
    */
   public abstract JDRGridLoaderListener getListener();

   public static Color majorGridColor = Color.gray;
   public static Color minorGridColor = new Color(222,222,255);
   public static Color axesGridColor = Color.gray;

   public static final byte GRID_RECTANGULAR = 0;
   public static final byte GRID_RADIAL = 1;
   public static final byte GRID_ISO = 2;
   public static final byte GRID_TSCHICHOLD = 3;
   public static final byte GRID_PATH = 4;

   public static final int HALF_MINOR_TIC = 2;
   public static final int HALF_MAJOR_TIC = 3;

   private byte id;


   private static BufferedImage minorTicImage = null;
   private static BufferedImage majorTicImage = null;

   private CanvasGraphics canvasGraphics;
}
