// File          : BBox.java
// Creation Date : 1st February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

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

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Bounding box information.
 * Stores the bounding box information for a given {@link JDRObject}.
 * A bounding box has eight "hotspots" given by a {@link JDRPoint}.
 * These regions are shown in red in <a href="#fig1">Figure 1</a>.
 * <p>
 * <center>
 * <table width=60%>
 * <tr align=center><td colspan=2>
 * <img src="images/bbox.png" alt="[image of box with red square regions at the 8 compass points]"></td></tr>
 * <tr><th valign=top>Figure&nbsp;1</th>
 * <td>A bounding box has eight "hotspot" regions (shown in red)</td>
 * </table>
 * </center>
 * FlowframTk only uses six of these eight hotspots.
 *
 * @version 0.3.1b 13 March 2008
 * @author Nicola L C Talbot
 */
public class BBox implements Cloneable,Serializable
{
   /**
    * Construct bounding box from two opposing points.
    * @param p1 first point
    * @param p2 diagonally opposite point
    */
   public BBox(CanvasGraphics cg, Point p1, Point p2)
   {
      minX_ = p1.x < p2.x ? p1.x : p2.x;
      minY_ = p1.y < p2.y ? p1.y : p2.y;
      maxX_ = p1.x < p2.x ? p2.x : p1.x; 
      maxY_ = p1.y < p2.y ? p2.y : p1.y;

      initHotspots(cg);
   }

   /**
    * Construct bounding box from two opposing points.
    * @param p1 first point
    * @param p2 diagonally opposite point
    */
   public BBox(CanvasGraphics cg, Point2D p1, Point2D p2)
   {
      minX_ = p1.getX() < p2.getX() ?  p1.getX() : p2.getX();
      minY_ = p1.getY() < p2.getY() ?  p1.getY() : p2.getY();
      maxX_ = p1.getX() < p2.getX() ?  p2.getX() : p1.getX(); 
      maxY_ = p1.getY() < p2.getY() ?  p2.getY() : p1.getY();

      initHotspots(cg);
   }

   /**
    * Construct bounding box from two opposing points.
    * @param p1 first point
    * @param p2 diagonally opposite point
    */
   public BBox(JDRPoint p1, JDRPoint p2)
   {
      minX_ = p1.x < p2.x ? p1.x : p2.x;
      minY_ = p1.y < p2.y ? p1.y : p2.y;
      maxX_ = p1.x < p2.x ? p2.x : p1.x; 
      maxY_ = p1.y < p2.y ? p2.y : p1.y;

      initHotspots(p1.getCanvasGraphics());
   }

   /**
    * Construct bounding box from minimum and maximum values.
    * @param minX minimum horizontal extent
    * @param minY minimum vertical extent
    * @param maxX maximum horizontal extent
    * @param maxY maximum vertical extent
    */
   public BBox(CanvasGraphics cg, int minX, int minY, int maxX, int maxY)
   {
      minX_ = minX;
      minY_ = minY;
      maxX_ = maxX;
      maxY_ = maxY;

      initHotspots(cg);
   }

   /**
    * Construct bounding box from minimum and maximum values.
    * @param minX minimum horizontal extent
    * @param minY minimum vertical extent
    * @param maxX maximum horizontal extent
    * @param maxY maximum vertical extent
    */
   public BBox(CanvasGraphics cg, 
     double minX, double minY, double maxX, double maxY)
   {
      minX_ = minX;
      minY_ = minY;
      maxX_ = maxX;
      maxY_ = maxY;

      initHotspots(cg);
   }

   /**
    * Construct bounding box from given rectangle.
    * @param r rectangle describing bounding box extent
    */
   public BBox(CanvasGraphics cg, Rectangle2D r)
   {
      this(cg, r.getX(), r.getY(),
           r.getX()+r.getWidth(), r.getY()+r.getHeight());
   }

   public BBox(CanvasGraphics cg)
   {
      this(cg, 0.0, 0.0, 0.0, 0.0);
   }

   private BBox()
   {
   }

   /**
    * Initialise the hotspots.
    */
   private void initHotspots(CanvasGraphics cg)
   {
      double midX = getMidX();
      double midY = getMidY();

      hotspotS  = new JDRPoint(cg, midX, maxY_);
      hotspotSE = new JDRPoint(cg, maxX_, maxY_);
      hotspotE  = new JDRPoint(cg, maxX_, midY);
      hotspotNE = new JDRPoint(cg, maxX_, minY_);
      hotspotN  = new JDRPoint(cg, midX, minY_);
      hotspotNW = new JDRPoint(cg, minX_, minY_);
      hotspotW  = new JDRPoint(cg, minX_, midY);
      hotspotSW = new JDRPoint(cg, minX_, maxY_);
   }

   /**
    * Reset the hotspots. (Required when bounding box changes size.
    */
   private void resetHotspots()
   {
      double midX = getMidX();
      double midY = getMidY();

      hotspotS.set(midX, maxY_);
      hotspotSE.set(maxX_, maxY_);
      hotspotE.set(maxX_, midY);
      hotspotNE.set(maxX_, minY_);
      hotspotN.set(midX, minY_);
      hotspotNW.set(minX_, minY_);
      hotspotW.set(minX_, midY);
      hotspotSW.set(minX_, maxY_);
   }

   /**
    * Determines if this bounding box intersects the given 
    * rectangle.
    * (Rectangle should be in the same units as this.)
    * @param rect the rectangle
    * @return true if this bounding box intersects the given
    * rectangle
    */
   public boolean intersects(Rectangle rect)
   {
      if (rect == null) return false;

      return intersects(rect.getX(), rect.getY(),
        rect.getWidth(), rect.getHeight());
   }

   /**
    * Determines if this bounding box intersects the given 
    * rectangle.
    * (Rectangle should be in the same units as this.)
    * @param rect the rectangle
    * @return true if this bounding box intersects the given
    * rectangle
    */
   public boolean intersects(Rectangle2D rect)
   {
      if (rect == null) return false;

      return intersects(rect.getX(), rect.getY(),
        rect.getWidth(), rect.getHeight());
   }

   public boolean intersects(BBox box)
   {
      if (box == null) return false;

      if (contains(box.minX_, box.minY_)
       || contains(box.minX_, box.maxY_)
       || contains(box.maxX_, box.maxY_)
       || contains(box.maxX_, box.minY_))
      {
         return true;
      }

      if (box.contains(minX_, minY_)
       || box.contains(minX_, maxY_)
       || box.contains(maxX_, maxY_)
       || box.contains(maxX_, minY_))
      {
         return true;
      }

      return false;
   }

   public boolean intersects(double px, double py,
     double width, double height)
   {
      double x0 = px;
      double y0 = px;
      double x1 = x0 + width;
      double y1 = y0 + height;

      if (minX_ >= x0 && minX_ <= x1
       && minY_ >= y0 && minY_ <= y1)
      {
         return true;
      }

      if (minX_ >= x0 && minX_ <= x1
       && maxY_ >= y0 && maxY_ <= y1)
      {
         return true;
      }

      if (maxX_ >= x0 && maxX_ <= x1
       && minY_ >= y0 && minY_ <= y1)
      {
         return true;
      }

      if (maxX_ >= x0 && maxX_ <= x1
       && maxY_ >= y0 && maxY_ <= y1)
      {
         return true;
      }

      return true;
   }

   /**
    * Determines if given point is inside this bounding box.
    * (Point should be in same units as this.)
    * @return <code>true</code> if point inside this bounding box, 
    * otherwise <code>false</code>
    * @param p point in question
    */
   public boolean contains(Point p)
   {
      return contains(p.getX(), p.getY());
   }

   /**
    * Determines if given point is inside this bounding box.
    * (Point should be in same units as this.)
    * @return <code>true</code> if point inside this bounding box, 
    * otherwise <code>false</code>
    * @param p point in question
    */
   public boolean contains(Point2D p)
   {
      return contains(p.getX(), p.getY());
   }

   public boolean contains(double px, double py)
   {
      if (px >= minX_ && px <= maxX_
       && py >= minY_ && py <= maxY_)
      {
         return true;
      }

      return false;
   }

   /**
    * Gets the height of this bounding box.
    * @return the height of this bounding box
    */
   public double getHeight()
   {
      return Math.abs(maxY_-minY_);
   }

   /**
    * Gets the width of this bounding box.
    * @return the width of this bounding box
    */
   public double getWidth()
   {
      return Math.abs(maxX_-minX_);
   }

   /**
    * Gets the minimum horizontal extent of this bounding box.
    * @return the minimum horizontal extent
    */
   public double getMinX()
   {
      return minX_;
   }

   /**
    * Gets the minimum vertical extent of this bounding box.
    * @return the minimum vertical extent
    */
   public double getMinY()
   {
      return minY_;
   }

   /**
    * Gets the maximum horizontal extent of this bounding box.
    * @return the maximum horizontal extent
    */
   public double getMaxX()
   {
      return maxX_;
   }

   /**
    * Gets the maximum vertical extent of this bounding box.
    * @return the maximum vertical extent
    */
   public double getMaxY()
   {
      return maxY_;
   }

   /**
    * Gets the horizontal mid point of this bounding box.
    * @return the horizontal mid point
    */
   public double getMidX()
   {
      return minX_+0.5*getWidth();
   }

   /**
    * Gets the vertical mid point of this bounding box.
    * @return the horizontal mid point
    */
   public double getMidY()
   {
      return minY_+0.5*getHeight();
   }

   /**
    * Translates this bounding box.
    * @param x horizontal shift
    * @param y vertical shift
    */
   public void translate(double x, double y)
   {
      minX_ -= x;
      maxX_ -= x;
      minY_ -= y;
      maxY_ -= y;

      resetHotspots();
   }

   /**
    * Resets the extent of this bounding box.
    * @param minX minimum horizontal extent
    * @param minY minimum vertical extent
    * @param maxX maximum horizontal extent
    * @param maxY maximum vertical extent
    */
   public void reset(double minX, double minY, double maxX, double maxY)
   {
      minX_ = minX;
      minY_ = minY;
      maxX_ = maxX;
      maxY_ = maxY;

      resetHotspots();
   }

   /**
    * Enlarges this box so that it encompasses another bounding
    * box.
    * @param bbox the other bounding box
    * @see #add(BBox)
    */
   public void encompass(BBox bbox)
   {
      double minX = bbox.getMinX();
      double minY = bbox.getMinY();
      double maxX = bbox.getMaxX();
      double maxY = bbox.getMaxY();

      double thisminX = getMinX();
      double thisminY = getMinY();
      double thismaxX = getMaxX();
      double thismaxY = getMaxY();

      if (minX > thisminX) minX = thisminX;
      if (minY > thisminY) minY = thisminY;
      if (maxX < thismaxX) maxX = thismaxX;
      if (maxY < thismaxY) maxY = thismaxY;

      minX_ = minX;
      minY_ = minY;
      maxX_ = maxX;
      maxY_ = maxY;

      resetHotspots();
   }

   /**
    * Gets the smallest bounding box that encompasses both this
    * bounding box and another bounding box.
    * @param bbox the other bounding box
    * @return bounding box encompassing both this and the other 
    * bounding box
    */
   public BBox add(BBox bbox)
   {
      double minX = bbox.getMinX();
      double minY = bbox.getMinY();
      double maxX = bbox.getMaxX();
      double maxY = bbox.getMaxY();

      double thisminX = getMinX();
      double thisminY = getMinY();
      double thismaxX = getMaxX();
      double thismaxY = getMaxY();

      if (minX > thisminX) minX = thisminX;
      if (minY > thisminY) minY = thisminY;
      if (maxX < thismaxX) maxX = thismaxX;
      if (maxY < thismaxY) maxY = thismaxY;

      return new BBox(getCanvasGraphics(), minX, minY, maxX, maxY);
   }

   /**
    * Sets this bounding box so that it is the smallest bounding box
    * that encompasses both the original version of this bounding
    * box and another bounding box. This is like add(BBox) but
    * doesn't make a new object.
    * @param bbox the other bounding box
    */
   public void merge(BBox bbox)
   {
      merge(bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
   }

   public void merge(double minX, double minY, double maxX, double maxY)
   {
      double thisminX = getMinX();
      double thisminY = getMinY();
      double thismaxX = getMaxX();
      double thismaxY = getMaxY();

      if (minX > thisminX) minX = thisminX;
      if (minY > thisminY) minY = thisminY;
      if (maxX < thismaxX) maxX = thismaxX;
      if (maxY < thismaxY) maxY = thismaxY;
      
      reset(minX, minY, maxX, maxY);
   }

   public void mergeStorageControl(Point2D p)
   {
      CanvasGraphics cg = getCanvasGraphics();

      DoubleDimension dim = cg.getStoragePointSize();
      double halfSizeX = dim.getWidth()*0.5;
      double halfSizeY = dim.getHeight()*0.5;

      merge(p.getX()-halfSizeX, p.getY()-halfSizeY,
            p.getX()+halfSizeX, p.getY()+halfSizeY);
   }

   /**
    * Sets this bounding box so that it is the smallest bounding box
    * that encompasses both this bounding
    * box and the given rectangle.
    * @param rect the given rectangle
    */
   public void merge(Rectangle2D rect)
   {
      double minX = rect.getX();
      double minY = rect.getY();
      double maxX = minX + rect.getWidth();
      double maxY = minY + rect.getHeight();

      merge(minX, minY, maxX, maxY);
   }

   /**
    * If the given point isn't contained in this bounding box, this
    * box is expanded to contain the point.
    * @param point the point that needs to be added to this bounding
    * box
    */
   public void merge(Point2D point)
   {
      merge(point.getX(), point.getY(),
            point.getX(), point.getY());
   }

   /**
    * Gets this bounding box as a rectangle, including area
    * taken up by hotspots.
    * @return rectangle encompassing this bounding box and its
    * hotspots
    */
   public Rectangle getStorageRectangle()
   {
      CanvasGraphics cg = getCanvasGraphics();

      DoubleDimension dim = cg.getStoragePointSize();

      double dx = 0.5*dim.getWidth()+1;
      double dy = 0.5*dim.getHeight()+1;

      return new Rectangle((int)Math.floor(getMinX()-dx),
                           (int)Math.floor(getMinY()-dy),
                           (int)Math.ceil(getWidth()+2*dx+2),
                           (int)Math.ceil(getHeight()+2*dy+2));
   }

   /**
    * Gets this bounding box as a rectangle, 
    * including hotspots, and scaled.
    * @param scale scaling factor
    * @return rectangle encompassing this bounding box and its
    * hotspots
    */
   public Rectangle getRectangle(double scale)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Dimension2D ptSize = cg.getComponentPointSize();

      double dx = 0.5*ptSize.getWidth()+1;
      double dy = 0.5*ptSize.getHeight()+1;

      return new Rectangle((int)Math.floor((getMinX()-dx-1)*scale),
                           (int)Math.floor((getMinY()-dy-1)*scale),
                           (int)Math.ceil((getWidth()+2*dx+2)*scale),
                           (int)Math.ceil((getHeight()+2*dy+2)*scale));
   }

   public Rectangle2D getRectangle2D(double scale)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Dimension2D ptSize = cg.getComponentPointSize();

      double dx = 0.5*ptSize.getWidth()+1;
      double dy = 0.5*ptSize.getHeight()+1;

      return new Rectangle2D.Double((getMinX()-dx-1)*scale,
                           (getMinY()-dy-1)*scale,
                           (getWidth()+2*dx+2)*scale,
                           (getHeight()+2*dy+2)*scale);
   }

   public Rectangle getComponentRectangle()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Dimension2D ptSize = cg.getComponentPointSize();

      double dx = 0.5*ptSize.getWidth()+1;
      double dy = 0.5*ptSize.getHeight()+1;

      double scaleX = cg.storageToComponentX(1.0);
      double scaleY = cg.storageToComponentY(1.0);

      return new Rectangle((int)Math.floor(scaleX*getMinX()-dx),
                           (int)Math.floor(scaleY*getMinY()-dy),
                           (int)Math.ceil(scaleX*getWidth()+2*dx),
                           (int)Math.ceil(scaleY*getHeight()+2*dy));
   }

   public Rectangle2D getComponentRectangle2D()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Dimension2D ptSize = cg.getComponentPointSize();

      double dx = 0.5*ptSize.getWidth()+1;
      double dy = 0.5*ptSize.getHeight()+1;

      double scaleX = cg.storageToComponentX(1.0);
      double scaleY = cg.storageToComponentY(1.0);

      return new Rectangle2D.Double(scaleX*getMinX()-dx,
                           scaleY*getMinY()-dy,
                           scaleX*getWidth()+2*dx,
                           scaleY*getHeight()+2*dy);
   }

   public Rectangle getRectangle()
   {
      return getRectangle(1.0);
   }

   /**
    * Draws this bounding box on given graphics object.
    * (The hotspots are not drawn.)
    */
   public void draw()
   {
      draw((short)0);
   }

   /**
    * Draws this bounding box on given graphics object
    * possibly including its hotspots. The hotspots to be
    * drawn are given in the bits of <code>hotspotFlags</code>.
    * For example, FlowframTk uses the value
    * <pre>
    * hotspotFlags = BBox.SOUTH
    *              | BBox.SOUTH_EAST
    *              | BBox.EAST
    *              | BBox.NORTH_EAST
    *              | BBox.NORTH_WEST
    *              | BBox.SOUTH_WEST
    * </pre>
    * when it is configured to show the hotspots, and uses 
    * <code>hotspotFlags=0</code> otherwise. The co-ordinates of the
    * bounding box are scaled by the given factor. Note that this
    * doesn't alter the graphics device transformation matrix.
    * @param hotspotFlags which hotspots should also be drawn
    */
   public void draw(short hotspotFlags)
   {
      CanvasGraphics cg = getCanvasGraphics();
      Graphics2D g2 = cg.getGraphics();

      if (g2 == null) return;

      Rectangle clip = g2.getClipBounds();

      Stroke oldS = cg.getStroke();
      Paint oldPaint = cg.getPaint();
      cg.setPaint(outlineColour);

      Rectangle rect = new Rectangle(
         (int)Math.floor(cg.storageToComponentX(minX_)),
         (int)Math.floor(cg.storageToComponentY(minY_)), 
         (int)Math.floor(cg.storageToComponentX(getWidth())),
         (int)Math.floor(cg.storageToComponentY(getHeight())));

      g2.draw(rect);

      float[] dashPattern = new float[2];

      dashPattern[0] = 6.0f;
      dashPattern[1] = 6.0f;
      cg.setPaint(dashColour);
      BasicStroke s = new BasicStroke(1.0f,
                          BasicStroke.CAP_BUTT,
                          BasicStroke.JOIN_MITER,
                          10.0f,dashPattern,5.0f);
      cg.setStroke(s);
      cg.draw(rect);

      cg.setStroke(oldS);

      if ((hotspotFlags & SOUTH)==SOUTH)
      {
         hotspotS.draw();
      }

      if ((hotspotFlags & SOUTH_EAST)==SOUTH_EAST)
      {
         hotspotSE.draw();
      }

      if ((hotspotFlags & EAST)==EAST)
      {
         hotspotE.draw();
      }

      if ((hotspotFlags & NORTH_EAST)==NORTH_EAST)
      {
         hotspotNE.draw();
      }

      if ((hotspotFlags & NORTH)==NORTH)
      {
         hotspotN.draw();
      }

      if ((hotspotFlags & NORTH_WEST)==NORTH_WEST)
      {
         hotspotNW.draw();
      }

      if ((hotspotFlags & WEST)==WEST)
      {
         hotspotW.draw();
      }

      if ((hotspotFlags & SOUTH_WEST)==SOUTH_WEST)
      {
         hotspotSW.draw();
      }

      cg.setPaint(oldPaint);
   }

   /**
    * Returns a copy of this object.
    * @return copy of this object
    */
   public Object clone()
   {
      return new BBox(getCanvasGraphics(), minX_, minY_, maxX_, maxY_);
   }

   /**
    * Makes this bounding box equal to the other.
    * @param bbox the other bounding box
    */
   public void makeEqual(BBox bbox)
   {
      minX_ = bbox.getMinX();
      minY_ = bbox.getMinY();
      maxX_ = bbox.getMaxX();
      maxY_ = bbox.getMaxY();

      resetHotspots();
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (!(obj instanceof BBox)) return false;

      BBox b = (BBox) obj;

      return (minX_ == b.minX_
           && minY_ == b.minY_
           && maxX_ == b.maxX_
           && maxY_ == b.maxY_);
   }

   /**
    * Gets the top left hotspot.
    * @return the top left hotspot
    */
   public JDRPoint getTopLeft()
   {
      return hotspotNW;
   }

   /**
    * Gets the bottom left hotspot.
    * @return the bottom left hotspot
    */
   public JDRPoint getBottomLeft()
   {
      return hotspotSW;
   }

   /**
    * Gets the bottom right hotspot.
    * @return the bottom right hotspot
    */
   public JDRPoint getBottomRight()
   {
      return hotspotSE;
   }

   /**
    * Gets the top right hotspot.
    * @return the top right hotspot
    */
   public JDRPoint getTopRight()
   {
      return hotspotNE;
   }

   /**
    * Gets the mid point of this bounding box.
    * @return mid point
    */
   public JDRPoint getCentre()
   {
      return new JDRPoint(getCanvasGraphics(), getMidX(), getMidY());
   }

   /**
    * Gets number identifying hotspot that contains the given point.
    * Uses {@link JDRPoint#contains(Point2D)} to determine whether
    * the given point is inside a hotspot. Possible values are:
    * <code>HOTSPOT_SE</code>, <code>HOTSPOT_SW</code>,
    * <code>HOTSPOT_NE</code>, <code>HOTSPOT_NW</code>,
    * <code>HOTSPOT_SW</code>, <code>HOTSPOT_E</code>,
    * <code>HOTSPOT_S</code> or <code>HOTSPOT_NONE</code>.
    * @return the identifier of the hotspot that contains given point
    * or <code>HOTSPOT_NONE</code> if none of this bounding box's
    * hotspots contain given point.
    * @param bpPoint point in question
    */
   public int getHotspotFromBpPoint(Point2D bpPoint)
   {
      if (hotspotSE.containsBpPoint(bpPoint))
      {
         return HOTSPOT_SE;
      }
      else if (hotspotSW.containsBpPoint(bpPoint))
      {
         return HOTSPOT_SW;
      }
      else if (hotspotNE.containsBpPoint(bpPoint))
      {
         return HOTSPOT_NE;
      }
      else if (hotspotNW.containsBpPoint(bpPoint))
      {
         return HOTSPOT_NW;
      }
      else if (hotspotNW.containsBpPoint(bpPoint))
      {
         return HOTSPOT_SW;
      }
      else if (hotspotE.containsBpPoint(bpPoint))
      {
         return HOTSPOT_E;
      }
      else if (hotspotS.containsBpPoint(bpPoint))
      {
         return HOTSPOT_S;
      }

      return HOTSPOT_NONE;
   }

   public int getHotspotFromStoragePoint(Point2D storagePoint)
   {
      if (hotspotSE.containsStoragePoint(storagePoint))
      {
         return HOTSPOT_SE;
      }
      else if (hotspotSW.containsStoragePoint(storagePoint))
      {
         return HOTSPOT_SW;
      }
      else if (hotspotNE.containsStoragePoint(storagePoint))
      {
         return HOTSPOT_NE;
      }
      else if (hotspotNW.containsStoragePoint(storagePoint))
      {
         return HOTSPOT_NW;
      }
      else if (hotspotNW.containsStoragePoint(storagePoint))
      {
         return HOTSPOT_SW;
      }
      else if (hotspotE.containsStoragePoint(storagePoint))
      {
         return HOTSPOT_E;
      }
      else if (hotspotS.containsStoragePoint(storagePoint))
      {
         return HOTSPOT_S;
      }

      return HOTSPOT_NONE;
   }

   public int getHotspotFromComponentPoint(Point2D compPoint)
   {
      if (hotspotSE.containsComponentPoint(compPoint))
      {
         return HOTSPOT_SE;
      }
      else if (hotspotSW.containsComponentPoint(compPoint))
      {
         return HOTSPOT_SW;
      }
      else if (hotspotNE.containsComponentPoint(compPoint))
      {
         return HOTSPOT_NE;
      }
      else if (hotspotNW.containsComponentPoint(compPoint))
      {
         return HOTSPOT_NW;
      }
      else if (hotspotNW.containsComponentPoint(compPoint))
      {
         return HOTSPOT_SW;
      }
      else if (hotspotE.containsComponentPoint(compPoint))
      {
         return HOTSPOT_E;
      }
      else if (hotspotS.containsComponentPoint(compPoint))
      {
         return HOTSPOT_S;
      }

      return HOTSPOT_NONE;
   }

   /**
    * Gets a string representation of this bounding box.
    * This is of the form BBox:[minX, minY, maxX, maxY].
    */
   public String toString()
   {
      return new String("BBox:["+minX_+" "+minY_+" "
                      +maxX_+" "+maxY_+"]");
   }

   public String info()
   {
      return new String("minX="+minX_+", minY="+minY_+", maxX="
                      +maxX_+", maxY="+maxY_);
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return hotspotS.getCanvasGraphics();
   }

   public void scale(double factor)
   {
      minX_ *= factor;
      minY_ *= factor;
      maxX_ *= factor;
      maxY_ *= factor;

      resetHotspots();
   }

   public void scale(double scaleX, double scaleY)
   {
      minX_ *= scaleX;
      minY_ *= scaleY;
      maxX_ *= scaleX;
      maxY_ *= scaleY;

      resetHotspots();
   }

   /**
    * This bounding box's extents.
    */
   private double minX_, minY_, maxX_, maxY_;

   /**
    * No hotspot. This value is returned by
    * {@link #getHotspotFromStoragePoint(Point2D)} etc if the given point does not 
    * lie in any of the hotspot regions.
    */
   public static final int HOTSPOT_NONE=0;
   /**
    * East hotspot. The point 
    * lies in the East (centre right) hotspot region.
    */
   public static final int HOTSPOT_E=1;
   /**
    * South-East hotspot. The point 
    * lies in the South-East (lower right) hotspot region.
    */
   public static final int HOTSPOT_SE=2;
   /**
    * South hotspot. The point 
    * lies in the South (lower middle) hotspot region.
    */
   public static final int HOTSPOT_S=3;
   /**
    * South-West hotspot. The point 
    * lies in the South-West (lower left) hotspot region.
    */
   public static final int HOTSPOT_SW=4;
   /**
    * West hotspot. The point 
    * lies in the West (centre left) hotspot region.
    */
   public static final int HOTSPOT_W=5;
   /**
    * North-West hotspot. The point 
    * lies in the North-West (upper left) hotspot region.
    */
   public static final int HOTSPOT_NW=6;
   /**
    * North hotspot. The point 
    * lies in the North (upper middle) hotspot region.
    */
   public static final int HOTSPOT_N=7;
   /**
    * North-East hotspot. The point 
    * lies in the North-East (upper right) hotspot region.
    */
   public static final int HOTSPOT_NE=8;

   /**
    * Hotspot region.
    */
   private JDRPoint hotspotS, hotspotSE, hotspotE,
                    hotspotNE, hotspotNW, hotspotSW,
                    hotspotN, hotspotW;

   /**
    * Hotspot flag.
    * @see #draw(short)
    */
   public static final short NORTH      = 0x80,
                             NORTH_EAST = 0x40,
                             EAST       = 0x20,
                             SOUTH_EAST = 0x10,
                             SOUTH      = 0x8,
                             SOUTH_WEST = 0x4,
                             WEST       = 0x2,
                             NORTH_WEST = 0x1;

//   private static Color dashColour = new Color(192,192,192,100);
   private static Color dashColour = Color.white;
   private static Color outlineColour = new Color(255,0,0,200);
}

