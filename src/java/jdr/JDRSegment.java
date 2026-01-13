// File          : JDRSegment.java
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
import java.util.*;

import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a segment of a path. Paths are made up of
 * a list of sequential segments to make it easier to edit the
 * path. The JDRSegment class represents a move. It has a start
 * and end control point, but no line is drawn between them.
 * Subclasses change how the start and end points are joined.
 * @author Nicola L C Talbot
 * @see JDRPath
 */
public class JDRSegment extends JDRObject
   implements JDRPathSegment,JDRConstants
{
   /**
    * Creates a new segment with start and end points at the 
    * origin.
    */
   public JDRSegment(CanvasGraphics cg)
   {
      super(cg);
      init(new JDRPoint(cg, 0,0), new JDRPoint(cg, 0,0));
   }

   /**
    * Creates a copy of a segment. 
    */ 
   public JDRSegment(JDRSegment segment)
   {
      super(segment);

      start = (JDRPoint)segment.start.clone();
      end   = (JDRPoint)segment.end.clone();

      setStartMarker(segment.getStartMarker());
      setEndMarker(segment.getEndMarker());
   }

   /**
    * Creates a new segment with given start and end locations.
    * @param p0x the x co-ordinate of the starting point
    * @param p0y the y co-ordinate of the starting point
    * @param p1x the x co-ordinate of the end point
    * @param p1y the y co-ordinate of the end point
    */
   public JDRSegment(CanvasGraphics cg, double p0x, double p0y, double p1x, double p1y)
   {
      super(cg);
      init(p0x, p0y, p1x, p1y);
   }

   /**
    * Creates a new segment with given start and end locations.
    * @param p0 the starting point
    * @param p1 the end point
    */
   public JDRSegment(CanvasGraphics cg, Point p0, Point p1)
   {
      super(cg);
      init(p0, p1);
   }

   /**
    * Creates a new segment with given start and end locations.
    * @param p0 the starting point
    * @param p1 the end point
    */
   public JDRSegment(CanvasGraphics cg, Point2D p0, Point2D p1)
   {
      super(cg);
      init(p0, p1);
   }

   /**
    * Creates a new segment with given start and end locations.
    * @param p0 the starting point
    * @param p1 the end point
    */
   public JDRSegment(JDRPoint p0, JDRPoint p1)
   {
      super(p0.getCanvasGraphics());
      init(p0, p1);
   }

   private void init(double p0x, double p0y, double p1x, double p1y)
   {
      CanvasGraphics cg = getCanvasGraphics();

      start = new JDRPoint(cg, p0x, p0y);
      end   = new JDRPoint(cg, p1x, p1y);
   }

   private void init(Point p0, Point p1)
   {
      CanvasGraphics cg = getCanvasGraphics();

      start = new JDRPoint(cg, p0);
      end   = new JDRPoint(cg, p1);
   }

   private void init(Point2D p0, Point2D p1)
   {
      CanvasGraphics cg = getCanvasGraphics();

      start = new JDRPoint(cg, p0);
      end   = new JDRPoint(cg, p1);
   }

   private void init(JDRPoint p0, JDRPoint p1)
   {
      start = p0;
      end   = p1;
   }

   /**
    * Creates a new segment that is the reverse of this one.
    * That is, the new segment will start at the end point of
    * this one and end at the start point of this one.
    * @return segment that is the reverse of this one
    */
   public JDRSegment reverse()
   {
      JDRSegment segment = new JDRSegment(this);

      segment.start.x = end.x;
      segment.start.y = end.y;
      segment.end.x   = start.x;
      segment.end.y   = start.y;

      return segment;
   }

   /**
    * Rotates this segment about the origin.
    * @param angle the angle of rotation (in radians)
    */
   public void rotate(double angle)
   {
      double cosTheta = (double)Math.cos(angle);
      double sinTheta = (double)Math.sin(angle);

      double old_x = start.x;
      double old_y = start.y;

      start.x = old_x*cosTheta - old_y*sinTheta;
      start.y = old_x*sinTheta + old_y*cosTheta;

      old_x = end.x;
      old_y = end.y;

      end.x = old_x*cosTheta - old_y*sinTheta;
      end.y = old_x*sinTheta + old_y*cosTheta;
   }

   /**
    * Rotates segment about a given point. The end point will only 
    * be rotated if endPoint is true.
    * @param p point about which to rotate
    * @param angle the angle of rotation (radians)
    * @param endPoint if true rotate end point as well
    */
   public void rotate(Point2D p, double angle, boolean endPoint)
   {
      start.rotate(p, angle);
      if (endPoint) end.rotate(p, angle);
   }

   public void rotate(Point2D p, double angle)
   {
      rotate(p, angle, true);
   }

   /**
    * Scales this segment.
    * @param factorX the horizontal scaling factor
    * @param factorY the vertical scaling factor
    */
   public void scale(double factorX, double factorY)
   {
      start.x = factorX*start.x;
      start.y = factorY*start.y;
      end.x = factorX*end.x;
      end.y = factorY*end.y;
   }

   /**
    * Scales this segment horizontally.
    * @param factor the horizontal scaling factor
    */
   public void scaleX(double factor)
   {
      start.x = factor*start.x;
      end.x = factor*end.x;
   }

   /**
    * Scales this segment vertically.
    * @param factor the vertical scaling factor
    */
   public void scaleY(double factor)
   {
      start.y = factor*start.y;
      end.y = factor*end.y;
   }

   /**
    * Scales segment about a given point. The end point will only 
    * be scaled if endPoint is true.
    * @param endPoint if true scale end point as well
    */
   public void scale(Point2D p, double factorX, double factorY,
                     boolean endPoint)
   {
      start.scale(p, factorX, factorY);
      if (endPoint) end.scale(p, factorX, factorY);
   }

   /**
    * Shears this segment.
    * @param factorX the x shear factor
    * @param factorY the y shear factor
    */
   public void shear(double factorX, double factorY)
   {
      start.shear(factorX, factorY);
      end.shear(factorX, factorY);
   }

   /**
    * Shears segment about a given point. The end point will only 
    * be sheared if endPoint is true.
    * @param endPoint if true shear end point as well
    */
   public void shear(Point2D p, double factorX, double factorY,
                     boolean endPoint)
   {
      start.shear(p, factorX, factorY);
      if (endPoint) end.shear(p, factorX, factorY);
   }

   /**
    * Translates this segment.
    * @param x the x shift
    * @param y the y shift
    * @see #translate(double,double,boolean)
    */
   public void translate(double x, double y)
   {
      start.translate(x,y);
      end.translate(x,y);
   }

   /**
    * Translate segment, optionally omitting the end point.
    * @param x the x shift
    * @param y the y shift
    * @param endPoint indicates whether to translate the end point
    * (translates end point if true)
    */
   public void translate(double x, double y, boolean endPoint)
   {
      start.translate(x,y);
      if (endPoint) end.translate(x,y);
   }

   /**
    * Transforms this segment.
    * @param matrix transformation matrix in flat form
    */
   public void transform(double[] matrix)
   {
      start.transform(matrix);
      end.transform(matrix);
   }

   /**
    * Transforms this segment.
    * @param af affine transformation
    */
   public void transform(AffineTransform af)
   {
      start.transform(af);
      end.transform(af);
   }

   public JDRPathSegment getReflection(JDRLine line)
   {
      Point2D p1 = start.getReflection(line);
      Point2D p2 = end.getReflection(line);

      return new JDRSegment(getCanvasGraphics(), p1, p2);
   }

   public BBox getReflectedBBox(JDRLine line)
   {
      Point2D p = end.getReflection(line);

      double minX = p.getX();
      double minY = p.getY();
      double maxX = p.getX();
      double maxY = p.getY();

      for (int i = 0, n = controlCount(); i < n; i++)
      {
         p = getControl(i).getReflection(line);

         if (p.getX() < minX)
         {
            minX = p.getX();
         }
         else if (p.getX() > maxX)
         {
            maxX = p.getX();
         }

         if (p.getY() < minY)
         {
            minY = p.getY();
         }
         else if (p.getY() > maxY)
         {
            maxY = p.getY();
         }
      }

      p = null;

      return new BBox(getCanvasGraphics(), minX, minY, maxX, maxY);
   }

   public void mergeReflectedBBox(JDRLine line, BBox box)
   {
      double minX = box.getMinX();
      double minY = box.getMinY();
      double maxX = box.getMaxX();
      double maxY = box.getMaxY();

      Point2D p = end.getReflection(line);

      if (p.getX() < minX)
      {
         minX = p.getX();
      }
      else if (p.getX() > maxX)
      {
         maxX = p.getX();
      }

      if (p.getY() < minY)
      {
         minY = p.getY();
      }
      else if (p.getY() > maxY)
      {
         maxY = p.getY();
      }

      for (int i = 0, n = controlCount(); i < n; i++)
      {
         p = getControl(i).getReflection(line);

         if (p.getX() < minX)
         {
            minX = p.getX();
         }
         else if (p.getX() > maxX)
         {
            maxX = p.getX();
         }

         if (p.getY() < minY)
         {
            minY = p.getY();
         }
         else if (p.getY() > maxY)
         {
            maxY = p.getY();
         }
      }

      box.reset(minX, minY, maxX, maxY);
   }

   /**
    * Gets this segment's bounding box.
    */
   public BBox getStorageBBox()
   {
      double minX = end.x;
      double minY = end.y;
      double maxX = end.x;
      double maxY = end.y;

      for (int i = 0, n = controlCount(); i < n; i++)
      {
         JDRPoint p = getControl(i);

         if (p.x < minX)
         {
            minX = p.x;
         }
         else if (p.x > maxX)
         {
            maxX = p.x;
         }

         if (p.y < minY)
         {
            minY = p.y;
         }
         else if (p.y > maxY)
         {
            maxY = p.y;
         }
      }

      return new BBox(getCanvasGraphics(), minX, minY, maxX, maxY);
   }

   public void mergeStorageBBox(BBox box)
   {
      double minX = end.x;
      double minY = end.y;
      double maxX = end.x;
      double maxY = end.y;

      for (int i = 0, n = controlCount(); i < n; i++)
      {
         JDRPoint p = getControl(i);

         if (p.x < minX)
         {
            minX = p.x;
         }
         else if (p.x > maxX)
         {
            maxX = p.x;
         }

         if (p.y < minY)
         {
            minY = p.y;
         }
         else if (p.y > maxY)
         {
            maxY = p.y;
         }
      }

      box.merge(minX, minY, maxX, maxY);
   }

   /**
    * Gets this segment's bounding box including control points.
    */
   public BBox getStorageControlBBox()
   {
      BBox box = end.getStorageControlBBox();

      for (int i = 0, n=controlCount(); i < n; i++)
      {
         JDRPoint p = getControl(i);
         p.mergeStorageControlBBox(box);
      }

      return box;
   }

   public void mergeStorageControlBBox(BBox box)
   {
      end.mergeStorageControlBBox(box);

      for (int i = 0, n=controlCount(); i < n; i++)
      {
         JDRPoint p = getControl(i);
         p.mergeStorageControlBBox(box);
      }
   }

   /**
    * Gets the gradient vector for this segment at t.
    * @param t the parameter used to describe this segment
    * @return gradient vector
    * @see #getdP0()
    * @see #getdP1()
    */
   public Point2D getdP(double t)
   {
      return getdP();
   }

   /**
    * Gets the gradient vector for this segment.
    * @return gradient vector
    */
   public Point2D getdP()
   {
      return JDRLine.getGradient(start.x, start.y, end.x, end.y);
   }

   public void flatten()
   {
   }

   /**
    * Gets the gradient vector for this segment at t=0.
    * @return gradient vector at t=0
    */
   public Point2D getdP0()
   {
      return getdP();
   }

   /**
    * Gets the gradient vector for this segment at t=1.
    * @return gradient vector at t=1
    */
   public Point2D getdP1()
   {
      return getdP();
   }

   /**
    * Gets the position vector for this segment at t.
    * Note that getP(0) always returns the starting co-ordinates
    * and getP(1) always returns the end co-ordinates.
    * @param t the parameter used to describe this segment
    * @return position vector
    */
   public Point2D getP(double t)
   {
      return new Point2D.Double(start.x+(end.x-start.x)*t,
                                start.y+(end.y-start.y)*t);
   }

   /**
    * Gets the y co-ordinate of the line at the given value of x.
    * Returns infinity if the line is vertical.
    */
   public double getY(double x)
   {
      double x1 = getStartX();
      double x2 = getEndX();
      double y1 = getStartY();
      double y2 = getEndY();

      double result = y1 + (x - x1)*(y2-y1)/(x2-x1);

      if (Double.isNaN(result))
      {
         // start and end points coincide
         return y1;
      }

      return result;
   }

   /**
    * Gets the x co-ordinate of the line at the given value of y.
    * Returns infinity if the line is horizontal.
    */
   public double getX(double y)
   {
      double x1 = getStartX();
      double x2 = getEndX();
      double y1 = getStartY();
      double y2 = getEndY();

      double result = x1 + (y-y1)*(x2-x1)/(y2-y1);

      if (Double.isNaN(result))
      {
         // start and end points coincide

         return x1;
      }

      return result;
   }

   /**
    * Draws the control points defining this segment.
    * @param endPoint true if the end point should be draw
    */
   public void drawControls(boolean endPoint)
   {
      start.draw();
      if (endPoint) end.draw();
   }

   public void draw()
   {
   }

   public void draw(FlowFrame parentFrame)
   {
      draw();
   }

   public void print(Graphics2D g2)
   {
   }

   public void drawSelectedNoControls()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Stroke oldStroke = cg.getStroke();
      Paint oldPaint = cg.getPaint();
      cg.setPaint(start.getSelectedPaint());
      cg.setStroke(guideStroke);

      cg.drawMagLine(start.x, start.y, end.x, end.y);

      cg.setStroke(oldStroke);
      cg.setPaint(oldPaint);
   }

   public void drawDraft(boolean drawEnd)
   {
      if (isSelected())
      {
         drawSelectedNoControls();
      }

      drawControls(drawEnd);
   }

   public JDRPathSegment split()
   {
      JDRPoint midPt = new JDRPoint(getCanvasGraphics(), getP(0.5));

      JDRSegment newSegment = new JDRSegment(midPt, end);

      end = midPt;

      return newSegment;
   }

   /**
    * Gets a copy of this segment.
    * @return a copy of this segment
    */
   public Object clone()
   {
      return new JDRSegment(this);
   }

   /**
    * Makes this segment the same as another segment.
    * @param seg the other segment
    */
   public void makeEqual(JDRSegment seg)
   {
      super.makeEqual(seg);
      start.makeEqual(seg.start);
      end.makeEqual(seg.end);

      setStartMarker(seg.startMarker);
      setEndMarker(seg.endMarker);
   }

   /**
    * Returns true if this object is the same as another object.
    * @param obj the other object
    */
   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;

      if (!(obj instanceof JDRSegment)) return false;

      JDRSegment s = (JDRSegment)obj;

      return (start.equals(s.start) && end.equals(s.end));
   }

   /**
    * Gets this segment's start control point.
    * @return the start control point
    */
   public JDRPoint getStart()
   {
      return start;
   }

   public double getStartX()
   {
      return start.x;
   }

   public double getStartY()
   {
      return start.y;
   }

   public double getEndX()
   {
      return end.x;
   }

   public double getEndY()
   {
      return end.y;
   }

   /**
    * Gets this segment's end control point.
    * @return the end control point
    */
   public JDRPoint getEnd()
   {
      return end;
   }

   /**
    * Sets this segment's start point. This sets the control point to
    * the argument rather than making a copy of it.
    * @param p the new start point
    */
   public void setStart(JDRPoint p)
   {
      start = p;
   }

   public void setStart(Point2D p)
   {
      start.x = p.getX();
      start.y = p.getY();
   }

   /**
    * Sets this segment's end point. This sets the control point to
    * the argument rather than making a copy of it.
    * @param p the new end point
    */
   public void setEnd(JDRPoint p)
   {
      end = p;
   }

   /**
    * Sets this segment's start point.
    * @param newX the new starting x co-ordinate
    * @param newY the new starting y co-ordinate
    */
   public void setStart(double newX, double newY)
   {
      start.x = newX;
      start.y = newY;
   }

   /**
    * Sets this segment's end point.
    * @param newX the new end x co-ordinate
    * @param newY the new end y co-ordinate
    */
   public void setEnd(double newX, double newY)
   {
      end.x = newX;
      end.y = newY;
   }

   public void setEnd(Point2D storagePoint)
   {
      setEnd(storagePoint.getX(), storagePoint.getY());
   }

   /**
    * Sets whether this segment's control points are being edited.
    * @param flag true if this segment's control points are being
    * edited
    */
   public void setEditedControls(boolean flag)
   {
      start.selected = flag;
      end.selected = flag;
   }

   /**
    * Gets the control point that contains the given point.  
    * If none of the control points
    * contain the given point, null is returned.
    * @param storagePoint the point under investigation
    * @param endPoint if true, check the end control point, otherwise
    * omit the end point in the search
    * @return the control point that contains the given point
    * or null if none of the control points contain the given point
    */
   public JDRPoint getControlFromStoragePoint(
     double storagePointX, double storagePointY, 
     boolean endPoint)
   {
      if (start.containsStoragePoint(storagePointX, storagePointY))
      {
         return start;
      }

      if (endPoint && end.containsStoragePoint(storagePointX, storagePointY))
      {
         return end;
      }

      return null;
   }

   public JDRPoint getControlFromStoragePoint(Point p, boolean endPoint)
   {
      return getControlFromStoragePoint(p.getX(), p.getY(), endPoint);
   }

   /**
    * Sets whether this segment is being edited.
    * @param flag true if this segment is being edited
    */
   public void setSelected(boolean flag)
   {
      selected = flag;
      if (flag == false)
      {
         start.selected = false;
         end.selected = false;
      }
   }

   /**
    * Gets the current edited control point.
    * @return the current edited control point or null if no control
    * points are being edited
    */
   public JDRPoint getEditedControl()
   {
      if (start.selected) return start;
      if (end.selected) return end;
      return null;
   }

   /**
    * Converts this to a line.
    * @return line formed from the start and end points of this
    */
   public JDRPathSegment convertToLine()
   {
      return new JDRLine(start, end);
   }

   /**
    * Converts this to a move.
    * @return segment formed from the start and end points of this
    */
   public JDRPathSegment convertToSegment()
   {
      return new JDRSegment(start, end);
   }

   /**
    * Converts this to a B&eacute;zier curve. The curvature control
    * points are set to form a line.
    * @return B&acute;zier formed from the start and end points of this
    */
   public JDRPathSegment convertToBezier()
   {
      JDRBezier curve = new JDRBezier(start, end);
      return curve;
   }

   public void saveSVG(SVG svg, String attr)
      throws IOException
   {
      svg.print("M ");
      end.saveSVG(svg, attr);
   }

   public void saveEPS(PrintWriter out) throws IOException
   {
      end.saveEPS(out);
      out.println("moveto");
   }

   public void savePgf(TeX tex)
     throws IOException
   {
      tex.print("\\pgfpathmoveto{");
      end.savePgf(tex);
      tex.println("}");
   }

   /**
    * Appends this segment to the given path. The start
    * point is not included.
    * @param path the path on which to append this segment.
    * This has changed from GeneralPath to Path2D.
    */
   public void appendToGeneralPath(Path2D path)
   {
      path.moveTo(end.x, end.y);
   }

   /**
    * Appends the reflection of this segment to the given path.
    * @param path the path on which to append the reflection of this segment
    * @param line the line of symmetry
    */
   public void appendReflectionToGeneralPath(Path2D path, JDRLine line)
   {
      Point2D p = start.getReflection(line);

      path.moveTo(p.getX(), p.getY());
   }

   /**
    * Gets string representation of this object.
    * @return string representation of this object
    */
   public String toString()
   {
      return "JDRSegment:("+start.x+","+start.y+")("+end.x+","+end.y+"),startMarker="+startMarker+",endMarker="+endMarker;
   }

   public JDRObjectLoaderListener getListener()
   {
      return listener;
   }

   public String info()
   {
      return "segment["+start.info()+","+end.info()+"]";
   }

   public String getDetails()
   {
      JDRMessageDictionary msg = getCanvasGraphics().getMessageDictionary();

      String type = msg.getMessageWithFallback("class."+getClass().getCanonicalName(),
       getClass().getSimpleName());

      Point2D dp = getdP();

      return msg.getMessageWithFallback(
        "segmentinfo.details.line",
        "Segment type: {0}; P(0)=({1},{2}), P(1)=({3},{4}); P'(t) = ({5},{6}).",
        type, start.x, start.y, end.x, end.y, dp.getX(), dp.getY()
       );
   }

   public JDRMarker getStartMarker()
   {
      return startMarker;
   }

   public JDRMarker getEndMarker()
   {
      return endMarker;
   }

   public void setStartMarker(JDRMarker marker)
   {
      startMarker = marker;
   }

   public void setEndMarker(JDRMarker marker)
   {
      endMarker = marker;
   }

   /**
    * The number of control points excluding the end point.
    */
   public int controlCount()
   {
      return 1;
   }

   /**
    * Gets the control with the given index excluding the end point.
    */
   public JDRPoint getControl(int index)
      throws IndexOutOfBoundsException
   {
      if (index == 0) return start;

      throw new IndexOutOfBoundsException("No control point at index "+index);
   }

   /**
    * Gets the index for the given control point excluding the end
    * point.
    */
   public int getControlIndex(JDRPoint storagePt)
      throws NoSuchElementException
   {
      if (storagePt == start) return 0;

      throw new NoSuchElementException();
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      super.setCanvasGraphics(cg);

      for (int i = 0, n = controlCount(); i < n; i++)
      {
         JDRPoint p = getControl(i);

         if (p != null)
         {
            p.setCanvasGraphics(cg);
         }
      }

      JDRPoint p = getEnd();

      if (p != null)
      {
         p.setCanvasGraphics(cg);
      }
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      for (int i = 0, n = controlCount(); i < n; i++)
      {
         getControl(i).applyCanvasGraphics(cg);
      }

      getEnd().applyCanvasGraphics(cg);

      super.setCanvasGraphics(cg);
   }

   public void fade(double value)
   {
   }

   public boolean isGap() {return true;}

   public boolean isCurve() {return false;}

   public int getSegmentFlag()
   {
      return SEGMENT_FLAG_MOVE;
   }

   protected JDRMarker startMarker=null, endMarker=null;

   /**
    * The starting point.
    */
   protected JDRPoint start;
   /**
    * The end point.
    */
   protected JDRPoint end;

   /**
    * Stroke used for drawing guides.
    */
   public static Stroke guideStroke = new BasicStroke(1.0f,
      BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,10.0f,
      new float[] {1.0f, 5.0f}, 0.0f);

   private static JDRSegmentLoaderListener listener
      = new JDRSegmentLoaderListener();
}
