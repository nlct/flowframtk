// File          : JDRObject.java
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
import java.util.Vector;
import java.util.NoSuchElementException;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.lang.Math;
import java.text.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;

import javax.swing.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a JDR object.
 * @author Nicola L C Talbot
 */
public abstract class JDRObject implements Serializable,JDRGraphicObject
{
   /**
    * Creates a new object.
    */
   private JDRObject()
   {
   }

   /**
    * Creates a new copy of the given object.
    */
   protected JDRObject(JDRObject obj)
   {
      this();
      selected = obj.selected;
      canvasGraphics = obj.canvasGraphics;
   }

   protected JDRObject(CanvasGraphics cg)
   {
      setCanvasGraphics(cg);
   }

   /**
    * Sets whether this object is selected.
    * @param flag indicates whether this object is selected
    * @see #isSelected()
    */
   public void setSelected(boolean flag)
   {
      selected = flag;
   }

   /**
    * Determines whether this object is selected.
    * @return true if this object is selected otherwise false
    * @see #setSelected(boolean)
    */
   public boolean isSelected()
   {
      return selected;
   }

   /**
    * Gets this object's bounding box in terms of storage units.
    * Returns <code>null</code> if this object has no size.
    * @return this object's bounding box or <code>null</code> if
    * this object has no size
    * @see #getStorageControlBBox()
    */
   public abstract BBox getStorageBBox();

   /**
    * Gets this object's bounding box in bp units.
    * Returns <code>null</code> if this object has no size.
    * @return this object's bounding box or <code>null</code> if
    * this object has no size
    * @see #getStorageControlBBox()
    */
   public BBox getBpBBox()
   {
      CanvasGraphics cg = getCanvasGraphics();

      BBox box = getStorageBBox();

      JDRUnit unit = cg.getStorageUnit();

      if (unit.getID() != JDRUnit.BP)
      {
         double factor = unit.toBp(1.0);

         box.reset(box.getMinX()*factor,
                   box.getMinY()*factor,
                   box.getMaxX()*factor,
                   box.getMaxY()*factor);
      }

      return box;
   }

   /**
    * Gets this object's bounding box in component units.
    * Returns <code>null</code> if this object has no size.
    * @return this object's bounding box or <code>null</code> if
    * this object has no size
    * @see #getComponentControlBBox()
    */
   public BBox getComponentBBox()
   {
      CanvasGraphics cg = getCanvasGraphics();

      double scaleX = cg.storageToComponentX(1.0);
      double scaleY = cg.storageToComponentY(1.0);

      BBox box = getStorageBBox();

      box.reset(box.getMinX()*scaleX,
                box.getMinY()*scaleY,
                box.getMaxX()*scaleX,
                box.getMaxY()*scaleY);

      return box;
   }

   /**
    * Merges the given bounding box (in storage units) with the bounding box for this
    * object.
    * @param box the given bounding box
    */
   public void mergeStorageBBox(BBox box)
   {
      BBox thisBox = getStorageBBox();

      if (thisBox != null) box.merge(thisBox);
   }

   /**
    * Merges the given bounding box (in bp units) with the bounding box for this
    * object.
    * @param box the given bounding box
    */
   public void mergeBpBBox(BBox box)
   {
      BBox thisBox = getBpBBox();

      if (thisBox != null) box.merge(thisBox);
   }

   /**
    * Merges the given bounding box (in component units) with the bounding box for this
    * object.
    * @param box the given bounding box
    */
   public void mergeComponentBBox(BBox box)
   {
      BBox thisBox = getComponentBBox();

      if (thisBox != null) box.merge(thisBox);
   }

   /**
    * Merges the given bounding box (in storage units) with the bounding box for this
    * object.
    * @param box the given bounding box
    */
   public void mergeStorageControlBBox(BBox box)
   {
      BBox thisBox = getStorageControlBBox();

      if (thisBox != null) box.merge(thisBox);
   }

   /**
    * Merges the given bounding box (in bp units) with the bounding box for this
    * object.
    * @param box the given bounding box
    */
   public void mergeBpControlBBox(BBox box)
   {
      BBox thisBox = getBpControlBBox();

      if (thisBox != null) box.merge(thisBox);
   }

   /**
    * Merges the given bounding box (in component units) with the bounding box for this
    * object.
    * @param box the given bounding box
    */
   public void mergeComponentControlBBox(BBox box)
   {
      BBox thisBox = getComponentControlBBox();

      if (thisBox != null) box.merge(thisBox);
   }

   /**
    * Gets this object's bounding box including control points (in
    * storage units).
    * @return this object's bounding box including control points
    * or <code>null</code> if this object has no size
    * @see #getStorageBBox()
    */
   public BBox getStorageControlBBox()
   {
      return getStorageBBox();
   }

   /**
    * Gets this object's bounding box including control points (in
    * bp units).
    * @return this object's bounding box including control points
    * or <code>null</code> if this object has no size
    * @see #getBpBBox()
    */
   public BBox getBpControlBBox()
   {
      CanvasGraphics cg = getCanvasGraphics();

      BBox box = getStorageControlBBox();

      JDRUnit unit = cg.getStorageUnit();

      if (unit.getID() != JDRUnit.BP)
      {
         double factor = unit.toBp(1.0);

         box.reset(box.getMinX()*factor,
                   box.getMinY()*factor,
                   box.getMaxX()*factor,
                   box.getMaxY()*factor);
      }

      return box;
   }

   /**
    * Gets this object's bounding box including control points (in
    * component units).
    * @return this object's bounding box including control points
    * or <code>null</code> if this object has no size
    * @see #getComponentBBox()
    */
   public BBox getComponentControlBBox()
   {
      CanvasGraphics cg = getCanvasGraphics();

      double scaleX = cg.storageToComponentX(1.0);
      double scaleY = cg.storageToComponentY(1.0);

      BBox box = getStorageControlBBox();

      box.reset(box.getMinX()*scaleX,
                box.getMinY()*scaleY,
                box.getMaxX()*scaleX,
                box.getMaxY()*scaleY);

      return box;
   }

   /**
    * Shifts this object.
    * @param p the x and y shift
    * @see #translate(double,double)
    */
   public void translate(Point p)
   {
      translate(p.x, p.y);
   }

   /**
    * Shifts this object.
    * @param p the x and y shift
    * @see #translate(double,double)
    */
   public void translate(JDRPoint p)
   {
      translate(p.x, p.y);
   }

   /**
    * Shifts this object.
    * @param p the x and y shift
    * @see #translate(double,double)
    */
   public void translate(Point2D p)
   {
      translate(p.getX(), p.getY());
   }

   /**
    * Scales this object.
    * @param factor the x and y scale factor
    * @see #scale(double,double)
    */
   public void scale(double factor)
   {
      scale(factor, factor);
   }

   /**
    * Scales this object relative to the given point.
    * @param p the scaling origin
    * @param factorX the x scale factor
    * @param factorY the y scale factor
    * @see #scale(double,double)
    */
   public void scale(Point2D p, double factorX, double factorY)
   {
      translate(new Point2D.Double(-p.getX(), -p.getY()));
      scale(factorX, factorY);
      translate(p);
   }

   /**
    * Scales this object relative to the given point.
    * @param p the scaling origin
    * @param factor the x and y scale factor
    * @see #scale(Point2D,double,double)
    * @see #scale(double,double)
    */
   public void scale(Point2D p, double factor)
   {
      scale(p, factor, factor);
   }

   /**
    * Scales this object relative to the given point.
    * @param p the scaling origin
    * @param factor the x and y scale factor
    * @see #scale(JDRPoint,double,double)
    * @see #scale(double,double)
    */
   public void scale(JDRPoint p, double factor)
   {
      scale(p.getPoint2D(), factor);
   }

   /**
    * Scales this object relative to the given point.
    * @param p the scaling origin
    * @param factorX the x scale factor
    * @param factorY the y scale factor
    * @see #scale(double,double)
    */
   public void scale(JDRPoint p, double factorX, double factorY)
   {
      scale(p.getPoint2D(), factorX, factorY);
   }

   /**
    * Scales this object relative to the given point.
    * @param p the scaling origin
    * @param factor the x and y scale factor
    * @see #scale(double,double)
    */
   public void scale(Point p, double factor)
   {
      scale(new Point2D.Double(p.x, p.y),factor);
   }

   /**
    * Scales this object horizontally.
    * @param factor the x scale factor
    * @see #scale(double,double)
    */
   public void scaleX(double factor)
   {
      scale(factor,1.0);
   }

   /**
    * Scales this object horizontally relative to the given point.
    * @param p the scaling origin
    * @param factor the x scale factor
    * @see #scaleX(double)
    */
   public void scaleX(Point2D p, double factor)
   {
      scale(p, factor, 1.0);
   }

   /**
    * Scales this object horizontally relative to the given point.
    * @param p the scaling origin
    * @param factor the x scale factor
    * @see #scaleX(double)
    */
   public void scaleX(JDRPoint p, double factor)
   {
      scaleX(p.getPoint2D(),factor);
   }

   /**
    * Scales this object horizontally relative to the given point.
    * @param p the scaling origin
    * @param factor the x scale factor
    * @see #scaleX(double)
    */
   public void scaleX(Point p, double factor)
   {
      scaleX(new Point2D.Double(p.x,p.y),factor);
   }

   /**
    * Scales this object vertically.
    * @param factor the y scale factor
    * @see #scale(double,double)
    */
   public void scaleY(double factor)
   {
      scale(1.0,factor);
   }

   /**
    * Scales this object vertically relative to the given point.
    * @param p the scaling origin
    * @param factor the y scale factor
    * @see #scaleY(double)
    */
   public void scaleY(JDRPoint p, double factor)
   {
      scaleY(p.getPoint2D(),factor);
   }

   /**
    * Scales this object vertically relative to the given point.
    * @param p the scaling origin
    * @param factor the y scale factor
    * @see #scaleY(double)
    */
   public void scaleY(Point p, double factor)
   {
      scaleY(new Point2D.Double(p.x,p.y),factor);
   }

   /**
    * Scales this object vertically relative to the given point.
    * @param p the scaling origin
    * @param factor the y scale factor
    * @see #scaleY(double)
    */
   public void scaleY(Point2D p, double factor)
   {
      scale(p, 1.0, factor);
   }

   /**
    * Shears this object.
    * @param factor the x and y shear factor
    * @see #shear(double,double)
    */
   public void shear(double factor)
   {
      shear(factor, factor);
   }

   /**
    * Shears this object relative to the given point.
    * @param p the shearing origin
    * @param factor the x and y shear factor
    * @see #shear(Point2D,double,double)
    */
   public void shear(Point2D p, double factor)
   {
      translate(new Point2D.Double(-p.getX(), -p.getY()));
      shear(factor);
      translate(p);
   }

   /**
    * Shears this object relative to the given point.
    * @param p the shearing origin
    * @param factorX the x shear factor
    * @param factorY the y shear factor
    * @see #shear(double,double)
    */
   public void shear(Point2D p, double factorX, double factorY)
   {
      translate(new Point2D.Double(-p.getX(), -p.getY()));
      shear(factorX, factorY);
      translate(p);
   }

   /**
    * Shears this object relative to the given point.
    * @param p the shearing origin
    * @param factorX the x shear factor
    * @param factorY the y shear factor
    * @see #shear(double,double)
    */
   public void shear(JDRPoint p, double factorX, double factorY)
   {
      shear(p.getPoint2D(), factorX, factorY);
   }

   /**
    * Shears this object relative to the given point.
    * @param p the shearing origin
    * @param factor the x and y shear factor
    * @see #shear(JDRPoint,double,double)
    */
   public void shear(JDRPoint p, double factor)
   {
      shear(p.getPoint2D(),factor);
   }

   /**
    * Shears this object relative to the given point.
    * @param p the shearing origin
    * @param factor the x and y shear factor
    * @see #shear(JDRPoint,double,double)
    */
   public void shear(Point p, double factor)
   {
      shear(new Point2D.Double(p.x, p.y),factor);
   }

   /**
    * Shears this object horizontally.
    * @param factor the x shear factor
    * @see #shear(double,double)
    * @see #shearY(double)
    */
   public void shearX(double factor)
   {
      shear(factor, 0.0);
   }

   /**
    * Shears this object horizontally relative to the given point.
    * @param p the shearing origin
    * @param factor the x shear factor
    * @see #shearX(double)
    * @see #shear(Point2D,double,double)
    */
   public void shearX(Point2D p, double factor)
   {
      translate(new Point2D.Double(-p.getX(), -p.getY()));
      shearX(factor);
      translate(p);
   }

   /**
    * Shears this object horizontally relative to the given point.
    * @param p the shearing origin
    * @param factor the x shear factor
    * @see #shearX(double)
    * @see #shear(Point2D,double,double)
    */
   public void shearX(JDRPoint p, double factor)
   {
      shearX(p.getPoint2D(),factor);
   }

   /**
    * Shears this object horizontally relative to the given point.
    * @param p the shearing origin
    * @param factor the x shear factor
    * @see #shearX(double)
    * @see #shear(Point2D,double,double)
    */
   public void shearX(Point p, double factor)
   {
      shearX(new Point2D.Double(p.x,p.y),factor);
   }

   /**
    * Shears this object vertically.
    * @param factor the y shear factor
    * @see #shear(double,double)
    * @see #shearX(double)
    */
   public void shearY(double factor)
   {
      shear(0.0, factor);
   }

   /**
    * Shears this object vertically relative to the given point.
    * @param p the shearing origin
    * @param factor the y shear factor
    * @see #shearY(double)
    * @see #shear(double,double)
    * @see #shear(JDRPoint,double,double)
    */
   public void shearY(JDRPoint p, double factor)
   {
      shearY(p.getPoint2D(),factor);
   }

   /**
    * Shears this object vertically relative to the given point.
    * @param p the shearing origin
    * @param factor the y shear factor
    * @see #shearY(double)
    * @see #shear(double,double)
    * @see #shear(Point2D,double,double)
    */
   public void shearY(Point p, double factor)
   {
      shearY(new Point2D.Double(p.x,p.y),factor);
   }

   /**
    * Shears this object vertically relative to the given point.
    * @param p the shearing origin
    * @param factor the y shear factor
    * @see #shearY(double)
    * @see #shear(double,double)
    * @see #shear(Point2D,double,double)
    */
   public void shearY(Point2D p, double factor)
   {
      translate(new Point2D.Double(-p.getX(), -p.getY()));
      shearY(factor);
      translate(p);
   }

   /**
    * Rotates this object. Subclasses need to
    * override this method.
    * @param angle the angle of rotation in radians
    */
   public abstract void rotate(double angle);

   public void rotate(JDRAngle angle)
   {
      rotate(angle.toRadians());
   }

   /**
    * Rotates this object about the given point.
    * @param p the point of rotation
    * @param angle the angle of rotation in radians
    * @see #rotate(double)
    */
   public abstract void rotate(Point2D p, double angle);

   public void rotate(Point2D p, JDRAngle angle)
   {
      rotate(p, angle.toRadians());
   }

   /**
    * Rotates this object about the given point.
    * @param p the point of rotation
    * @param angle the angle of rotation
    * @see #rotate(double)
    */
   public void rotate(JDRPoint p, double angle)
   {
      rotate(p.getPoint2D(), angle);
   }

   public void rotate(JDRPoint p, JDRAngle angle)
   {
      rotate(p, angle.toRadians());
   }

   /**
    * Rotates this object about the given point.
    * @param p the point of rotation
    * @param angle the angle of rotation
    * @see #rotate(double)
    */
   public void rotate(Point p, double angle)
   {
      rotate(new Point2D.Double(p.x,p.y), angle);
   }

   public void rotate(Point p, JDRAngle angle)
   {
      rotate(p, angle.toRadians());
   }

   /**
    * Draws the control points associated with this object. 
    * The canvas graphics must have the Graphics2D component set
    * before call this method.
    * @param endPoint indicates whether the end control point should
    * be drawn
    */
   public abstract void drawControls(boolean endPoint);

   /**
    * Draws this object in draft mode. It assumes that the graphics state
    * has already been set to use the relevant draft colour. This
    * just calls {@link #draw()}
    * The canvas graphics must have the Graphics2D component set
    * before call this method.
    */
   public void drawDraft(FlowFrame parentFrame)
   {
      draw(parentFrame);
   }

   /**
    * Draws this object. Sets the draft paint if draft mode. Calls
    * {@link #drawDraft()} if draft mode on, otherwise calls
    * {@link #draw()}
    * The canvas graphics must have the Graphics2D component set
    * before call this method.
    * @param draft determines whether to use draft mode
    */
   public void draw(boolean draft, FlowFrame parentFrame)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (draft)
      {
         cg.setPaint(draftColor);
         drawDraft(parentFrame);
      }
      else
      {
         draw(parentFrame);
      }
   }

   /**
    * Returns the EPS level supported by this object. This
    * defaults to Level 1.
    * @return the PostScript level that supports this object
    */
   public int psLevel()
   {
      return 1;
   }

   public abstract void savePgf(TeX tex) throws IOException;

   public abstract void saveSVG(SVG svg, String attr)
      throws IOException;

   public void saveSVG(SVG svg)
      throws IOException
   {
      saveSVG(svg, "");
   }

   public abstract void saveEPS(PrintWriter out)
      throws IOException;

   public abstract void fade(double value);

   /**
    * Makes this object identical to the other object.
    * @param object the other object
    */
   public void makeEqual(JDRGraphicObject object)
   {
      selected = object.isSelected();
      setCanvasGraphics(object.getCanvasGraphics());
   }

   /**
    * Determines if this object is the same as another object.
    * @param o the other object
    * @return true if this object is equal to the other object
    */
   public boolean equals(Object o)
   {
      if (o == null)
      {
         return false;
      }

      if (this == o) return true;

      if (!(o instanceof JDRObject))
      {
         return false;
      }

      JDRObject jdrobj = (JDRObject)o;

      if (selected != jdrobj.selected) return false;

      return true;
   }

  /**
    * Gets the control point that contains the given point
    * (specified in storage units).
    * @param storagePoint the given point
    * @param endPoint if true include the end point in the search
    * otherwise don't check the end point.
    * @return the control point that contains the given point or
    * null if none of the control points contain that point
    */
   public abstract JDRPoint getControlFromStoragePoint(
      double storagePointX, double storagePointY, boolean endPoint);

  /**
    * Gets the control point that contains the given point
    * (specified in bp units).
    * @param bpPoint the given point
    * @param endPoint if true include the end point in the search
    * otherwise don't check the end point.
    * @return the control point that contains the given point or
    * null if none of the control points contain that point
    */
   public JDRPoint getControlFromBpPoint(
      Point bpPoint, boolean endPoint)
   {
      CanvasGraphics cg = getCanvasGraphics();

      return getControlFromStoragePoint(
       cg.bpToStorage(bpPoint.getX()),
       cg.bpToStorage(bpPoint.getY()),
       endPoint);
   }

  /**
    * Gets the control point that contains the given point
    * (specified in component units).
    * @param compPoint the given point
    * @param endPoint if true include the end point in the search
    * otherwise don't check the end point.
    * @return the control point that contains the given point or
    * null if none of the control points contain that point
    */
   public JDRPoint getControlFromComponentPoint(
      Point compPoint, boolean endPoint)
   {
      CanvasGraphics cg = getCanvasGraphics();

      return getControlFromStoragePoint(
       cg.componentXToStorage(compPoint.getX()),
       cg.componentYToStorage(compPoint.getY()),
       endPoint);
   }

   /**
    * Gets the object loader listener associated with this object.
    * May be null if there is no listener associated with this 
    * object.
    * @return the listener used to load and save this object
    * or <code>null</code> if no available listener
    */
   public JDRObjectLoaderListener getListener()
   {
      return null;
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      canvasGraphics = cg;
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

   public abstract void applyCanvasGraphics(CanvasGraphics cg);

   /**
    * The colour to draw objects in draft mode.
    */
   public static Paint draftColor = Color.lightGray;

   /**
    * Indicates whether this object is selected.
    */
   protected volatile boolean selected;

   protected volatile CanvasGraphics canvasGraphics;

}
