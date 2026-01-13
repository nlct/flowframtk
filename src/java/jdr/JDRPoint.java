// File          : JDRPoint.java
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

import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a control point. Despite being a point it has
 * an associated area surrounding it (given by {@link CanvasGraphics#pointSize})
 * that allows a GUI user to select the point approximately.
 * @author Nicola L C Talbot
 */
public class JDRPoint extends JDRObject implements JDRConstants
{
   /**
    * Creates a new control point at the origin.
    */
   public JDRPoint(CanvasGraphics cg)
   {
      super(cg);
      x = 0;
      y = 0;
   }

   /**
    * Creates a copy of another control point. 
    */ 
   public JDRPoint(JDRPoint p)
   {
      super(p);
      x = p.x;
      y = p.y;
      anchored = p.anchored;
   }

   /**
    * Creates a control point at the given location.
    * @param p the location of the new point
    */
   public JDRPoint(CanvasGraphics cg, Point p)
   {
      super(cg);
      x = p.x;
      y = p.y;
   }

   /**
    * Creates a control point at the given location.
    * @param p the location of the new point
    */
   public JDRPoint(CanvasGraphics cg, Point2D p)
   {
      super(cg);
      x = p.getX();
      y = p.getY();
   }

   /**
    * Creates a control point at the given location.
    * @param px the x co-ordinate
    * @param py the y co-ordinate
    */
   public JDRPoint(CanvasGraphics cg, double px, double py)
   {
      super(cg);
      x = px;
      y = py;
   }

   /**
    * Sets the location of this point.
    * @param newX the new x co-ordinate
    * @param newY the new y co-ordinate
    */
   public void set(double newX, double newY)
   {
      x = newX;
      y = newY;
   }

   public double getX()
   {
      return x;
   }

   public double getY()
   {
      return y;
   }

   /**
    * Gets the paint used to draw this point when it is selected.
    */
   public Color getSelectedPaint()
   {
      return selectColor;
   }

   /**
    * Sets the paint use to draw points of this class when selected.
    */
   public void setSelectedPaint(Color paint)
   {
      selectColor = paint;
   }

   /**
     Gets the paint used to draw this point when it isn't selected.
    */
   public Color getUnselectedPaint()
   {
      return controlColor;
   }

   /**
    * Sets the paint used to draw points of this class when not
    * selected.
    */
   public void setUnselectedPaint(Color paint)
   {
      controlColor = paint;
   }

   public void fade(double value)
   {
   }

   public void drawControls(boolean endPoint)
   {
   }

   public void print(Graphics2D g2)
   {
   }

   public void draw()
   {
      draw(selected ? getSelectedPaint() : getUnselectedPaint());
   }

   public void draw(FlowFrame parentFrame)
   {
      draw();
   }

   public void draw(Paint paint)
   {
      CanvasGraphics cg = getCanvasGraphics();

      draw(cg.getGraphics(), cg.getComponentPointSize(), paint,
       cg.storageToComponentX(1.0), cg.storageToComponentY(1.0));
   }

   public void draw(Graphics2D g2, Dimension2D ptSize, Paint paint,
    double storageToCompX, double storageToCompY)
   {
      Paint oldPaint = g2.getPaint();

      g2.setPaint(paint);

      double halfSizeX = 0.5*ptSize.getWidth();
      double halfSizeY = 0.5*ptSize.getHeight();

      double compX = storageToCompX*x;
      double compY = storageToCompY*y;

      Rectangle2D.Double rect = new Rectangle2D.Double(
         compX-halfSizeX, compY-halfSizeY, 
         ptSize.getWidth(), ptSize.getHeight());

      g2.draw(rect);

      g2.setPaint(innerColour);

      if (rect.width > 10)
      {
         rect.x += 2;
         rect.y += 2;
         rect.width -= 4;
         rect.height -= 4;
         g2.draw(rect);
      }
      else if (rect.width > 2)
      {
         rect.x++;
         rect.y++;
         rect.width -= 2;
         rect.height -= 2;
         g2.draw(rect);
      }

      if (anchored)
      {
         g2.setPaint(Color.black);
         anchorImage.draw(g2, (float)compX, (float)compY, 
            (float)ptSize.getHeight());
      }

      g2.setPaint(oldPaint);
   }

   public BBox getStorageBBox()
   {
      return new BBox(getCanvasGraphics(), x, y, x, y);
   }

   public BBox getStorageControlBBox()
   {
      CanvasGraphics cg = getCanvasGraphics();

      DoubleDimension dim = cg.getStoragePointSize();

      double halfSizeX = dim.getWidth()*0.5;
      double halfSizeY = dim.getHeight()*0.5;

      return new BBox(cg, 
                      x-halfSizeX,
                      y-halfSizeY,
                      x+halfSizeX,
                      y+halfSizeY);
   }

   public void mergeStorageControlBBox(BBox box)
   {
      CanvasGraphics cg = getCanvasGraphics();

      DoubleDimension size = cg.getStoragePointSize();
      double halfSizeX = size.getWidth()*0.5;
      double halfSizeY = size.getHeight()*0.5;

      double minX = x-halfSizeX;
      double minY = y-halfSizeY;
      double maxX = x+halfSizeX;
      double maxY = y+halfSizeY;

      box.merge(minX, minY, maxX, maxY);
   }

   public BBox getBpControlBBox()
   {
      CanvasGraphics cg = getCanvasGraphics();

      double storageToBp = cg.storageToBp(1.0);

      DoubleDimension size = cg.getStoragePointSize();

      double halfSizeX = storageToBp*size.getWidth()*0.5;
      double halfSizeY = storageToBp*size.getHeight()*0.5;

      double bpX = storageToBp*x;
      double bpY = storageToBp*y;

      return new BBox(cg, 
                      bpX-halfSizeX,
                      bpY-halfSizeY,
                      bpX+halfSizeX,
                      bpY+halfSizeY);
   }

   public boolean containsStoragePoint(JDRPoint p)
   {
      if (this == p) return true;

      return containsStoragePoint(p.x, p.y);
   }

   public boolean containsStoragePoint(Point2D p)
   {
      return containsStoragePoint(p.getX(), p.getY());
   }

   public boolean containsStoragePoint(
      double storageX, double storageY)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Dimension2D controlSize = cg.getStoragePointSize();
      double dx = controlSize.getWidth()*0.5;
      double dy = controlSize.getHeight()*0.5;

      double minX = x-dx;
      double maxX = x+dx;
      double minY = y-dy;
      double maxY = y+dy;

      return (minX <= storageX && storageX <= maxX
            && minY <= storageY && storageY <= maxY);
   }

   public boolean containsBpPoint(Point bpPoint)
   {
      return containsBpPoint(bpPoint.getX(), bpPoint.getY());
   }

   public boolean containsBpPoint(Point2D bpPoint)
   {
      return containsBpPoint(bpPoint.getX(), bpPoint.getY());
   }

   public boolean containsBpPoint( 
      double bpPointX, double bpPointY)
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRUnit unit = cg.getStorageUnit();

      if (unit.getID() == JDRUnit.BP)
      {
         return containsStoragePoint(bpPointX, bpPointY);
      }

      double factor = unit.fromBp(1.0);

      return containsStoragePoint(factor*bpPointX, factor*bpPointY);
   }

   public boolean containsComponentPoint(Point compPoint)
   {
      return containsComponentPoint(compPoint.getX(), compPoint.getY());
   }

   public boolean containsComponentPoint(Point2D compPoint)
   {
      return containsComponentPoint(compPoint.getX(), compPoint.getY());
   }

   public boolean containsComponentPoint(
      double compPointX, double compPointY)
   {
      CanvasGraphics cg = getCanvasGraphics();

      return containsStoragePoint( 
        cg.componentXToStorage(compPointX), 
        cg.componentYToStorage(compPointY));
   }

   public void transform(double[] matrix)
   {
      double newx = matrix[0]*x+matrix[2]*y+matrix[4];
      double newy = matrix[1]*x+matrix[3]*y+matrix[5];
      x = newx;
      y = newy;
   }

   public void transform(AffineTransform af)
   {
      Point2D p = new Point2D.Double(x, y);
      p = af.transform(p, p);

      x = p.getX();
      y = p.getY();
   }

   public void translate(double px, double py)
   {
      x += px;
      y += py;
   }

   /**
    * Moves this point to the nearest point on the given line.
    * @see #moveToLine(double,double,double,double)
    */
   public void moveToLine(JDRLine line)
   {
      moveToLine(line.start.x, line.start.y, line.end.x, line.end.y);
   }

   /**
    * Moves this point to the nearest point on the given line.
    * @param x0 x co-ordinate of first point defining line
    * @param y0 y co-ordinate of first point defining line
    * @param x1 x co-ordinate of second point defining line
    * @param y1 y co-ordinate of second point defining line
    * @see #moveToLine(JDRLine)
    */
   public void moveToLine(double x0, double y0,
                          double x1, double y1)
   {
      // Need to trap near vertical and near horizontal lines of
      // symmetry to prevent arithmetic errors

      double dx = x0 - x1;
      double dy = y0 - y1;

      double m = dy/dx;
      double minv = dx/dy;

      if (Math.abs(dx) < 1)
      {
         // Line is approximately vertical

         x = minv*(y - y0) + x0;
      }
      else if (Math.abs(dy) < 1)
      {
         // Line is approximately horizontal

         y = m*(x - x0) + y0;
      }
      else
      {
         double px = (x * minv + y + m*x0 - y0)
                   / (m + minv);

         double py = m*(px - x0) + y0;

         x = px;
         y = py;
      }
   }

   public void scale(double factorX, double factorY)
   {
      x = factorX*x;
      y = factorY*y;
   }

   public void scaleX(double factor)
   {
      x = factor*x;
   }

   public void scaleY(double factor)
   {
      y = factor*y;
   }

   public void shear(double factorX, double factorY)
   {
      double oldx = x;
      double oldy = y;

      // left handed co-ordinate system
      x = oldx-factorX*oldy;
      y = oldy-factorY*oldx;
   }

   public void shear(double factor)
   {
      shear(factor, factor);
   }

   public void shearX(double factor)
   {
      shear(factor,0.0);
   }

   public void shearY(double factor)
   {
      shear(0.0,factor);
   }

   // angle in radians
   public void rotate(double angle)
   {
      double cosTheta = Math.cos(angle);
      double sinTheta = Math.sin(angle);

      double old_x = x;
      double old_y = y;

      x = old_x*cosTheta - old_y*sinTheta;
      y = old_x*sinTheta + old_y*cosTheta;
   }

   public void rotate(Point2D p, double angle)
   {
      translate(-p.getX(), -p.getY());
      rotate(angle);
      translate(p.getX(), p.getY());
   }

   /**
    * Converts this point to a {@link Point2D} object.
    * @return this point as a {@link Point2D} object
    */
   public Point2D getPoint2D()
   {
      return new Point2D.Double(x,y);
   }

   /**
    * Gets a reflection of this point about the given line.
    * @param line the line of symmetry
    * @return reflected point
    */
   public Point2D getReflection(JDRLine line)
   {
      return getReflection(x, y, line);
   }

   /**
    * Reflects this point about the given line.
    * @param line the line of symmetry
    */
   public void reflect2D(JDRLine line)
   {
      Point2D p = getReflection(x, y, line);

      x = p.getX();
      y = p.getY();
   }

   public static Point2D getReflection(double px, double py, JDRLine line)
   {
      AffineTransform af = line.getReflectionTransform(null);

      return new Point2D.Double(
        af.getScaleX()*px + af.getShearX()*py + af.getTranslateX(),
        af.getShearY()*px + af.getScaleY()*py + af.getTranslateY());

   }

   /**
    * Converts this point to a {@link Point} object.
    * The co-ordinates are rounded to the nearest integer
    * using {@link Math#round(double)}.
    * @return this point as a {@link Point} object
    */
   public Point getPoint()
   {
      return new Point((int)Math.round(x),(int)Math.round(y));
   }

   public Object clone()
   {
      return new JDRPoint(this);
   }

   /**
    * Makes this point equal to another point.
    * @param p the other point
    */
   public void makeEqual(JDRPoint p)
   {
      super.makeEqual(p);
      x = p.x;
      y = p.y;
      anchored = p.anchored;
   }

   /**
    * Determines if this point equals the other object.
    * This calls {@link JDRObject#equals(Object)} in addition
    * to checking the co-ordinates.
    * @param obj the other object
    * @return true if this point is equivalent to the other object
    */
   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;

      if (!(obj instanceof JDRPoint)) return false;

      JDRPoint p = (JDRPoint)obj;

      return (x == p.x && y == p.y);
   }

   /**
    * Gets string representation of this point.
    * @return string representation of this point
    */
   public String toString()
   {
      return new String("JDRPoint("+x+","+y+")");
   }

   public void savePgf(TeX tex)
     throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      AffineTransform af = tex.getTransform();

      Point2D p = new Point2D.Double(x, y);

      if (af != null)
      {
         af.transform(p, p);
      }

      tex.print(tex.point(cg, p.getX(), p.getY()));
   }

   public void saveSVG(SVG svg, String attr)
      throws IOException
   {
      svg.savePoint(x, y);
   }

   public void saveEPS(PrintWriter out) throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      EPS.saveStoragePoint(cg, out, x, y);
   }

   public String info()
   {
      return "("+x+","+y+")";
   }

   public boolean isAnchored()
   {
      return anchored;
   }

   public void setAnchored(boolean isAnchorOn)
   {
      anchored = isAnchorOn;
   }

   public JDRPoint getControlFromStoragePoint(
      double storagePointX, double storagePointY, boolean endPoint)
   {
      BBox box = getStorageControlBBox();

      if (box.contains(x, y))
      {
         return this;
      }

      return null;
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      JDRUnit oldUnit = getCanvasGraphics().getStorageUnit();
      JDRUnit newUnit = cg.getStorageUnit();

      if (oldUnit.getID() != newUnit.getID())
      {
         double factor = oldUnit.toUnit(1.0, newUnit);
         x *= factor;
         y *= factor;
      }

      setCanvasGraphics(cg);
   }

   public int getControlFlag()
   {
      int flag = CONTROL_FLAG_REGULAR;

      if (isAnchored())
      {
         flag = (flag | CONTROL_FLAG_ANCHORED);
      }

      return flag;
   }

   /**
    * The x co-ordinate of this point.
    */
   public double x;
   /**
    * The y co-ordinate of this point.
    */
   public double y;

   /**
    * Has this point been anchored?
    */ 
   public boolean anchored=false;

   public static Color innerColour = new Color(192,192,192,127);

   /**
    * The colour to draw control points.
    */
   public static Color controlColor = new Color(255,200,0,200);

   /**
    * The colour to draw selected points.
    */
   public static Color selectColor = new Color(255,0,0,200);

   /**
    * Anchor image
    */

   public static JDRAnchor anchorImage = new JDRAnchor();
}
