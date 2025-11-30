// File          : JDRPath.java
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
import java.util.Locale;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import com.dickimawbooks.jdr.marker.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing paths. A path may contain 
 * cubic B&eacute;zier segments {@link JDRBezier},
 * lines {@link JDRLine} or moves (gaps) {@link JDRSegment}.
 * The segments that make up the path are stored in an array
 * to make it more convenient to access individual segments
 * for editing purposes. The default initial storage capacity is
 * given by {@link #getInitCapacity()}, but expands as the array 
 * is filled to capacity. The expansion is determined by 
 * {@link CanvasGraphics#getOptimize()}: if the value returned is 
 * {@link CanvasGraphics#OPTIMIZE_SPEED} the capacity is doubled, other
 * the capacity is incremented by 5.
 * @author Nicola L C Talbot
 */

public class JDRPath extends JDRShape
{
   /**
    * Creates an empty path with default initial capacity.
    */
   public JDRPath(CanvasGraphics cg)
   {
      super(cg);
      init();
   }

   /**
    * Creates an empty path with default initial capacity and
    * the given line colour. The path has no fill colour.
    * @param lineColor the line colour of the path
    */
   public JDRPath(JDRPaint lineColor)
   {
      this(lineColor.getCanvasGraphics());
      setLinePaint(lineColor);
   }

   /**
    * Creates an empty path with default initial capacity and
    * the given line and fill colour.
    * @param lineColor the line colour of the path
    * @param fillColor the fill colour of the path
    */
   public JDRPath(JDRPaint lineColor, JDRPaint fillColor)
   {
      this(lineColor);
      setFillPaint(fillColor);
   }

   /**
    * Creates an empty path with default initial capacity and
    * the given line colour, fill colour and stroke.
    * @param lineColor the line colour of the path
    * @param fillColor the fill colour of the path
    * @param s the stroke
    */
   public JDRPath(JDRPaint lineColor, JDRPaint fillColor, JDRStroke s)
   {
      this(lineColor, fillColor);
      setStroke(s);
   }

   public JDRPath(CanvasGraphics cg, 
     JDRPaint lineColor, JDRPaint fillColor, JDRStroke s)
   {
      this(cg);
      lineColor.setCanvasGraphics(cg);
      fillColor.setCanvasGraphics(cg);
      s.setCanvasGraphics(cg);

      setLinePaint(lineColor);
      setFillPaint(fillColor);
      setStroke(s);
   }

   /**
    * Creates an empty path with default initial capacity and
    * the given line colour, fill colour and stroke
    * styles.
    * @param lineColor the line colour of the path
    * @param fillColor the fill colour of the path
    * @param thickness the pen width (can't be negative)
    * @param cap the end cap, which must be one
    * of {@link BasicStroke#CAP_BUTT}, {@link BasicStroke#CAP_ROUND}
    * or {@link BasicStroke#CAP_SQUARE}
    * @param join the join style, which must be one
    * of {@link BasicStroke#JOIN_MITER}, {@link BasicStroke#JOIN_ROUND}
    * or {@link BasicStroke#JOIN_BEVEL}
    * @param mitre the mitre limit (can't be less than one)
    * @param dp the dash pattern
    * @see #JDRPath(JDRPaint,JDRPaint,JDRStroke)
    */
   public JDRPath(JDRPaint lineColor, JDRPaint fillColor,
               JDRLength thickness, int cap, int join, double mitre,
               DashPattern dp)
   {
      this(lineColor.getCanvasGraphics());
      setStyle(lineColor, fillColor, thickness, cap, join, mitre, dp);
   }

   /**
    * Creates an empty path with given initial capacity.
    * @param capacity the initial storage capacity
    */
   public JDRPath(CanvasGraphics cg, int capacity)
   {
      super(cg);
      init(capacity);
   }

   /**
    * Creates an empty path with given initial capacity and
    * the given line colour. The path has no fill colour.
    * @param capacity the initial storage capacity
    * @param lineColor the line colour of the path
    */
   public JDRPath(int capacity, JDRPaint lineColor)
   {
      super(lineColor.getCanvasGraphics());
      init(capacity);
      setLinePaint(lineColor);
   }

   /**
    * Creates an empty path with given initial capacity and
    * the given line and fill colour.
    * @param capacity the initial storage capacity
    * @param lineColor the line colour of the path
    * @param fillColor the fill colour of the path
    */
   public JDRPath(int capacity, JDRPaint lineColor, JDRPaint fillColor)
   {
      super(lineColor.getCanvasGraphics());
      init(capacity);
      setLinePaint(lineColor);
      setFillPaint(fillColor);
   }

   /**
    * Creates an empty path with given initial capacity and
    * the given line colour, fill colour and stroke.
    * @param capacity the initial storage capacity
    * @param lineColor the line colour of the path
    * @param fillColor the fill colour of the path
    * @param s the stroke
    */
   public JDRPath(int capacity, JDRPaint lineColor,
                  JDRPaint fillColor, JDRStroke s)
   {
      super(lineColor.getCanvasGraphics());
      init(capacity);
      setLinePaint(lineColor);
      setFillPaint(fillColor);
      setStroke(s);
   }

   /**
    * Creates an empty path with given initial capacity and
    * the given line colour, fill colour and stroke
    * styles.
    * @param capacity the initial storage capacity
    * @param lineColor the line colour of the path
    * @param fillColor the fill colour of the path
    * @param thickness the pen width (which can't be negative)
    * @param cap the end cap, which must be one
    * of {@link BasicStroke#CAP_BUTT}, {@link BasicStroke#CAP_ROUND}
    * or {@link BasicStroke#CAP_SQUARE}
    * @param join the join style, which must be one
    * of {@link BasicStroke#JOIN_MITER}, {@link BasicStroke#JOIN_ROUND}
    * or {@link BasicStroke#JOIN_BEVEL}
    * @param mitre the mitre limit, which can't be less than 1
    * @param dp the dash pattern
    * @see #JDRPath(int,JDRPaint,JDRPaint,JDRStroke)
    */
   public JDRPath(int capacity, JDRPaint lineColor, JDRPaint fillColor,
               JDRLength thickness, int cap, int join, double mitre,
               DashPattern dp)
   {
      this(lineColor.getCanvasGraphics(), capacity);

      setStyle(lineColor, fillColor, thickness, cap, join, mitre, dp);
   }

   public static JDRPath fromShape(JDRShape shape)
   {
      JDRPath path = new JDRPath(shape.getLinePaint(), shape.getFillPaint());

      if (shape.getStroke() instanceof JDRBasicStroke)
      {
         path.setStroke(shape.getStroke());
      }
      else
      {
         path.setStroke(new JDRBasicStroke(shape.getCanvasGraphics()));
      }

      JDRPathIterator pi = shape.getIterator();

      while (pi.hasNext())
      {
         JDRPathSegment segment = pi.next();

         if (segment instanceof JDRPartialSegment)
         {
            segment = ((JDRPartialSegment)segment).getFullSegment();
         }

         try
         {
            path.add((JDRSegment)segment);
         }
         catch (ClosingMoveException e)
         {
            path.segmentList_[e.getSegmentIndex()]
              = e.getSegment().convertToNonClosingMove();
         }
         catch (InvalidPathException e)
         {
            path.getCanvasGraphics().getMessageSystem().postMessage(
              MessageInfo.createInternalError(e));
         }
      }

      if (shape.isClosed())
      {
         try
         {
            path.close();
         }
         catch (InvalidPathException e)
         {
            shape.getCanvasGraphics().getMessageSystem().postMessage(
              MessageInfo.createInternalError(e));
         }
      }

      return path;
   }

   private void init()
   {
      init(getInitCapacity());
   }

   private void init(int capacity)
   {
      capacity_ = capacity;
      size_ = 0;
      segmentList_ = new JDRSegment[capacity];
      closed = false;

      initIterators();
   }

   protected void initIterators()
   {
      iterator = new JDRPathIterator(this);
      pointIterator = new JDRPointIterator(this);
   }

   @Override
   public JDRPathIterator getIterator()
   {
      iterator.reset();
      return iterator;
   }

   @Override
   public JDRPointIterator getPointIterator()
   {
      pointIterator.reset();
      return pointIterator;
   }

   /**
    * Sets this path's style.
    * @param c the draw colour
    * @param fill_color the fill colour
    * @param thickness the line width (can't be negative)
    * @param cap the end cap (as allowed by JDRBasicStroke)
    * @param join the join style (as allowed by JDRBasicStroke)
    * @param mitre_limit the mitre limit, which can't be less than
    * one
    * @param pattern the dash pattern
    */
   public void setStyle(JDRPaint c, JDRPaint fill_color, 
                    JDRLength thickness, int cap, int join,
                    double mitre_limit, DashPattern pattern)
   {
      setLinePaint(c);
      setFillPaint(fill_color);

      stroke = new JDRBasicStroke(getCanvasGraphics(),
        thickness,cap,join,mitre_limit,pattern);
   }

   /**
    * Sets this path's style.
    * @param c the draw colour
    * @param fill_color the fill colour
    * @param s the stroke
    */
   public void setStyle(JDRPaint c, JDRPaint fill_color, 
                    JDRStroke s)
   {
      setLinePaint(c);
      setFillPaint(fill_color);

      setStroke(s);
   }

   /**
    * Gets the line colour for this path.
    * @return the draw colour for this path
    * @see #setLinePaint(JDRPaint)
    * @see #getFillPaint()
    */
   @Override
   public JDRPaint getLinePaint()
   {
      return linePaint;
   }

   /**
    * Gets the fill colour for this path.
    * @return the fill colour for this path
    * @see #setFillPaint(JDRPaint)
    * @see #getLinePaint()
    */
   @Override
   public JDRPaint getFillPaint()
   {
      return fillPaint;
   }

   /**
    * Gets the stroke for this path.
    * @return the stroke for this path
    * @see #setStroke(JDRStroke)
    */
   @Override
   public JDRStroke getStroke()
   {
      return stroke;
   }

   /**
    * Sets the stroke for this path.
    * @param s the new stroke
    * @see #getStroke()
    */
   @Override
   public void setStroke(JDRStroke s)
   {
      stroke = s;

      if (stroke != null)
      {
         stroke.setCanvasGraphics(getCanvasGraphics());
      }
   }

   /**
    * Sets the line colour for this object.
    * @param paint the new line colour
    * @see #getLinePaint()
    */
   @Override
   public void setLinePaint(JDRPaint paint)
   {
      if (paint == null)
      {
         linePaint = new JDRTransparent(getCanvasGraphics());
      }
      else
      {
         linePaint = paint;
         linePaint.setCanvasGraphics(getCanvasGraphics());
      }
   }

   /**
    * Sets the fill colour for this object.
    * @param paint the new line colour
    * @see #getFillPaint()
    */
   public void setFillPaint(JDRPaint paint)
   {
      if (paint == null)
      {
         fillPaint = new JDRTransparent(getCanvasGraphics());
      }
      else
      {
         fillPaint = paint;
         fillPaint.setCanvasGraphics(getCanvasGraphics());
      }
   }

   /**
    * Creates a new path from the given path iterator.
    * Note that a JDRPath type path can't have sub paths so
    * it may not be an exact duplicate. Quadratic B&eacute;zier
    * curves are converted to cubic B&eacute;zier segments.
    * @param pi path iterator describing path
    * @throws MissingMoveException if the path iterator does not
    * start with a move
    * @throws EmptyPathException if the path iterator is does not
    * contain at least one segment (move/line/curve)
    * @return a path representing the
    * shape specified by the given path iterator
    */
   public static JDRPath getPath(CanvasGraphics cg, PathIterator pi)
   throws InvalidPathException
   {
      JDRPath path = null;

      double[] coords = new double[6];
      double oldX=0, oldY=0;
      double startX=0, startY=0;
      int moveto=0;
      boolean isClosed = false;

      JDRSegment lastPostMoveSeg = null;

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_MOVETO :
               if (path == null)
               {
                  oldX = coords[0];
                  oldY = coords[1];
                  path = new JDRPath(cg);
               }
               else
               {
                  JDRSegment segment;

                  if (isClosed && lastPostMoveSeg != null)
                  {
                     segment = new JDRClosingMove(
                                     oldX, oldY, coords[0],coords[1],
                                     path, path.size(), lastPostMoveSeg);
                  }
                  else
                  {
                     segment = new JDRSegment(cg, oldX, oldY,
                                                coords[0],coords[1]);
                  }

                  oldX = coords[0];
                  oldY = coords[1];
                  if (path == null)
                  {
                     throw new MissingMoveException(cg);
                  }
                  path.add(segment);
               }
               startX = coords[0];
               startY = coords[1];
               lastPostMoveSeg = null;
               moveto++;
               isClosed = false;
            break;
            case PathIterator.SEG_LINETO :
               JDRLine line = new JDRLine(cg, oldX, oldY, coords[0],coords[1]);
               oldX = coords[0];
               oldY = coords[1];
               if (path == null)
               {
                  throw new MissingMoveException(cg);
               }
               path.add(line);

               if (lastPostMoveSeg == null)
               {
                  lastPostMoveSeg = line;
               }
            break;
            case PathIterator.SEG_QUADTO :
               JDRBezier curve = JDRBezier.quadToCubic(cg, oldX, oldY, 
                                         coords[0],coords[1],
                                         coords[2],coords[3]);
               oldX = coords[2];
               oldY = coords[3];
               if (path == null)
               {
                  throw new MissingMoveException(cg);
               }
               path.add(curve);

               if (lastPostMoveSeg == null)
               {
                  lastPostMoveSeg = curve;
               }
            break;
            case PathIterator.SEG_CUBICTO :
               JDRBezier cubic = new JDRBezier(cg, oldX, oldY, 
                                         coords[0],coords[1],
                                         coords[2],coords[3],
                                         coords[4],coords[5]);
               oldX = coords[4];
               oldY = coords[5];
               if (path == null)
               {
                  throw new MissingMoveException(cg);
               }
               path.add(cubic);

               if (lastPostMoveSeg == null)
               {
                  lastPostMoveSeg = cubic;
               }
            break;
            case PathIterator.SEG_CLOSE :

               if (path == null)
               {
                  throw new MissingMoveException(cg);
               }

               isClosed = true;
            break;
         }

         pi.next();
      }

      if (path == null || path.isEmpty())
      {
         throw new EmptyPathException(cg);
      }

      if (isClosed && moveto == 1)
      {
         path.close(CLOSE_LINE);
      }

      JDRStroke stroke = path.getStroke();

      if (stroke instanceof JDRBasicStroke)
      {
         ((JDRBasicStroke)stroke).setWindingRule(pi.getWindingRule());
      }

      return path;
   }

   /**
    * Creates a new path or group of paths from the given path iterator.
    * If the original shape described by the path iterator has sub
    * paths, this method will return a group of paths where each
    * element is a sub path of the original. Quadratic B&eacute;zier
    * curves are converted to cubic B&eacute;zier segments.
    * @param pi path iterator describing path
    * @throws MissingMoveException if the path iterator does not
    * start with a move
    * @throws EmptyPathException if the path iterator is does not
    * contain at least one segment (move/line/curve)
    * @return either a path or a group of paths representing the
    * shape specified by the given path iterator
    */
   public static JDRCompleteObject getPaths(CanvasGraphics cg, PathIterator pi)
   throws InvalidPathException
   {
      JDRGroup group = null;
      JDRPath path = null;
      JDRPath prevPath = null;

      double[] coords = new double[6];
      double oldX=0, oldY=0;
      double startX=0, startY=0;


      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_MOVETO :
               if (path == null)
               {
                  if (prevPath != null && group == null)
                  {
                     group = new JDRGroup(cg);
                     group.add(prevPath);
                  }

                  oldX = coords[0];
                  oldY = coords[1];
                  path = new JDRPath(cg);
               }
               else
               {
                  JDRSegment segment = new JDRSegment(cg, oldX, oldY,
                                                coords[0],coords[1]);
                  oldX = coords[0];
                  oldY = coords[1];
                  if (path == null)
                  {
                     throw new MissingMoveException(cg);
                  }
                  path.add(segment);
               }
               startX = coords[0];
               startY = coords[1];
            break;
            case PathIterator.SEG_LINETO :
               JDRLine line = new JDRLine(cg, oldX, oldY, coords[0],coords[1]);
               oldX = coords[0];
               oldY = coords[1];
               path.add(line);
               if (path == null)
               {
                  throw new MissingMoveException(cg);
               }
            break;
            case PathIterator.SEG_QUADTO :
               JDRBezier curve = JDRBezier.quadToCubic(cg, oldX, oldY, 
                                         coords[0],coords[1],
                                         coords[2],coords[3]);
               oldX = coords[2];
               oldY = coords[3];
               path.add(curve);
               if (path == null)
               {
                  throw new MissingMoveException(cg);
               }
            break;
            case PathIterator.SEG_CUBICTO :
               JDRBezier cubic = new JDRBezier(cg, oldX, oldY, 
                                         coords[0],coords[1],
                                         coords[2],coords[3],
                                         coords[4],coords[5]);
               oldX = coords[4];
               oldY = coords[5];
               path.add(cubic);
               if (path == null)
               {
                  throw new MissingMoveException(cg);
               }
            break;
            case PathIterator.SEG_CLOSE :
               if (path == null || path.isEmpty())
               {
                  throw new EmptyPathException(cg);
               }

               JDRSegment firstSegment = (JDRSegment)path.getFirstSegment();
               JDRSegment lastSegment  = (JDRSegment)path.getLastSegment();
               lastSegment.end = firstSegment.start;
               path.closed = true;

               if (group != null)
               {
                  group.add(path);
               }

               prevPath = path;
               path = null;
         }

         pi.next();
      }

      if (group != null)
      {
         return group;
      }

      if (prevPath == null)
      {
         throw new EmptyPathException(cg);
      }

      return prevPath;
   }

   public void smoothLines(double tolerance)
   {
      smoothLines(0, tolerance);
   }

   public void smoothLines(int startIdx, double tolerance)
   {
      int n = size_;

      JDRPathSegment prevSegment = null;

      for (int i = 0; i < n; i++)
      {
         JDRPathSegment segment = get(i);

         if (!(segment instanceof JDRLine))
         {
            prevSegment = null;
            continue;
         }

         if (prevSegment == null)
         {
            prevSegment = segment;
            continue;
         }

         double x0 = prevSegment.getStartX();
         double y0 = prevSegment.getStartY();
         double x1 = segment.getStartX();
         double y1 = segment.getStartY();
         double x2 = segment.getEndX();
         double y2 = segment.getEndY();

         // compute angle between the two lines


         double angle = Math.atan2(y0-y1, x0-x1)
                      - Math.atan2(y1-y2, x1-x2);

         if (Math.abs(angle) < tolerance ||
             Math.abs(angle-Math.PI) < tolerance)
         {
            JDRLine line = new JDRLine(prevSegment.getStart(),
                                    segment.getEnd());

            // replace previous and current segments with new line

            try
            {
               removeSegmentFromList(i);
               setSegment(i-1, line);
            }
            catch (InvalidPathException e)
            {// shouldn't occur
               getCanvasGraphics().getMessageSystem().postMessage(
                 MessageInfo.createInternalError(e));
            }

            smoothLines(i-1, tolerance);

            return;
         }

         prevSegment = segment;
      }

      try
      {
         firePathChangeEvent(JDRPathChangeEvent.Type.PATH_CHANGED);
      }
      catch (ClosingMoveException e)
      {
         // Shouldn't occur as this method is just replacing one or
         // more curves with one or more lines.

         getCanvasGraphics().getMessageSystem().postMessage(
           MessageInfo.createInternalError(e));
      }
   }

   public static double computeArea(Shape shape, double flatness)
   {
      PathIterator pi = shape.getPathIterator(null, flatness);

      if (pi.isDone()) return 0;
      
      double area = 0;
      double[] coords = new double[6];

      pi.currentSegment(coords);
      pi.next();

      double x0 = coords[0];
      double y0 = coords[1];

      double orgX = x0;
      double orgY = y0;

      boolean isClosed = false;

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         // type should always be a line, move or close since it's been
         // flattened.

         switch (type)
         {
            case PathIterator.SEG_LINETO:
            case PathIterator.SEG_MOVETO:

               double x1 = coords[0];
               double y1 = coords[1];

               area += x0*y1 - x1*y0;

               x0 = x1;
               y0 = y1;

               break;

            case PathIterator.SEG_CLOSE:
               isClosed = true;
         }

         pi.next();
      }

      if (!isClosed)
      {
         area += x0*orgY - orgX*y0;
      }

      area *= 0.5;

      return area;
   }

   /**
    * Finds the approximate area of this path.
    */
   public double computeArea(double flatness)
   {
      if (size() == 0)
      {
         return 0;
      }

      return computeArea(getGeneralPath(), flatness);
   }

   /**
    * Creates a new path that is the reverse of this path.
    * @return the reverse of this path
    * @throws InvalidPathException if this path can't be reversed
    */
   @Override
   public JDRShape reverse() throws InvalidPathException
   {
      int n = size_;

      JDRPath path = new JDRPath(getCanvasGraphics(), n);

      path.setAttributes(this);

      for (int i = n-1; i >= 0; i--)
      {
         JDRSegment segment = ((JDRSegment)get(i)).reverse();

         if (i == 0 && closed)
         {
            path.close(segment);
         }
         else
         {
            try
            {
               path.add(segment);
            }
            catch (ClosingMoveException e)
            {
               segmentList_[e.getSegmentIndex()] =
                e.getSegment().convertToNonClosingMove();
            }
         }
      }

      return path;
   }

   /**
    * Returns a path created from applying an exclusive OR to
    * this path and another path.
    * @param path the other path
    * @return new path that is this path XOR the other path
    */
   @Override
   public JDRShape exclusiveOr(JDRShape path)
   throws InvalidPathException
   {
      Area area1 = new Area(getGeneralPath());
      Area area2 = new Area(path.getGeneralPath());

      area1.exclusiveOr(area2);

      JDRPath newpath = getPath(getCanvasGraphics(),
       area1.getPathIterator(null));

      newpath.setAttributes(this);

      return newpath;
   }

   /**
    * Returns a path created from applying union of
    * this path and another path.
    * @param path the other path
    * @return new path that is this path AND the other path
    */
   @Override
   public JDRShape pathUnion(JDRShape path)
   throws InvalidPathException
   {
      Area area1 = new Area(getGeneralPath());
      Area area2 = new Area(path.getGeneralPath());

      area1.add(area2);

      JDRPath newpath = getPath(getCanvasGraphics(),
         area1.getPathIterator(null));

      newpath.setAttributes(this);

      return newpath;
   }

   /**
    * Returns a new path that is the intersection of this path
    * and another path.
    * @param path the other path
    * @return a new path that is the intersection of this path
    * and another path
    */
   @Override
   public JDRShape intersect(JDRShape path)
   throws InvalidPathException
   {
      Area area1 = new Area(getGeneralPath());
      Area area2 = new Area(path.getGeneralPath());

      area1.intersect(area2);

      JDRPath newpath = getPath(getCanvasGraphics(),
         area1.getPathIterator(null));

      newpath.setAttributes(this);

      return newpath;
   }

   /**
    * Returns a new path that is this path less another path.
    * @param path the other path
    * @return a new path that is this path less another path
    */
   @Override
   public JDRShape subtract(JDRShape path)
      throws InvalidPathException
   {
      Area area1 = new Area(getGeneralPath());
      Area area2 = new Area(path.getGeneralPath());

      area1.subtract(area2);

      JDRPath newpath = getPath(getCanvasGraphics(),
          area1.getPathIterator(null));

      newpath.setAttributes(this);

      return newpath;
   }

   /**
    * Returns the number of segments in this path.
    * @return the number of segments in this path
    */
   public int size()
   {
      return size_;
   }

   /**
    * Returns the current capacity of this path.
    * @return current capacity of this path
    */
   @Override
   public int getCapacity()
   {
      return capacity_;
   }

   /**
    * Sets the current capacity of this path. The new capacity must
    * be at least equal to the size of this path.
    * @param capacity the new capacity
    * @throws IllegalArgumentException if the new capacity is less
    * than the size of the path
    */
   @Override
   public void setCapacity(int capacity)
      throws IllegalArgumentException
   {
      if (capacity < size_)
      {
         throw new IllegalArgumentException(
           "Can't set capacity to "+capacity+" for path of size "+size_);
      }

      enlargeList(capacity);
   }

   private void enlargeList()
   {
      if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         enlargeList(2*capacity_);
      }
      else
      {
         enlargeList(capacity_+5);
      }
   }

   private void enlargeList(int capacity)
   {
      capacity_ = capacity;

      JDRSegment[] list = new JDRSegment[capacity_];

      for (int i = 0; i < size_; i++)
      {
         list[i] = segmentList_[i];
      }

      segmentList_ = list;
   }

   private void addSegmentToList(JDRSegment s)
   throws NullPointerException,ClosingMoveException
   {
      if (s == null)
      {
         throw new NullPointerException(
            "Null segments may not be added to path");
      }

      if (capacity_ == size_)
      {
         enlargeList();
      }

      segmentList_[size_] = s;

      size_++;

      if (s instanceof JDRClosingMove)
      {
         numClosedSubPaths++;
      }

      firePathChangeEvent(size_-1, JDRPathChangeEvent.Type.SEGMENT_ADDED,
       null, s);
   }

   private void addSegmentToList(int index, JDRSegment s)
      throws ArrayIndexOutOfBoundsException,
        NullPointerException,
        ClosingMoveException
   {
      if (index < 0 || index > size_)
      {
         throw new ArrayIndexOutOfBoundsException(index);
      }

      if (s == null)
      {
         throw new NullPointerException(
            "Null segments may not be added to path");
      }

      if (capacity_ == size_)
      {
         enlargeList();
      }

      for (int i = size_; i > index; i--)
      {
         segmentList_[i] = segmentList_[i-1];
      }

      segmentList_[index] = s;

      size_++;

      if (s instanceof JDRClosingMove)
      {
         numClosedSubPaths++;
      }

      firePathChangeEvent(index, JDRPathChangeEvent.Type.SEGMENT_INSERTED,
        null, s);
   }

   protected JDRSegment removeSegmentFromList(JDRSegment segment)
   {
      int index = -1;

      for (int i = 0; i < size_; i++)
      {
         if (segmentList_[i] == segment)
         {
            index = i;
            break;
         }
      }

      if (index == -1) return null;

      return removeSegmentFromList(index);
   }

   protected JDRSegment removeSegmentFromList(int index)
   throws ArrayIndexOutOfBoundsException
   {
      if (index < 0 || index >= size_)
      {
         throw new ArrayIndexOutOfBoundsException(index);
      }

      JDRSegment segment = segmentList_[index];

      for (int i = index; i < size_-1; i++)
      {
         segmentList_[i] = segmentList_[i+1];
      }

      size_--;

      if (segment instanceof JDRClosingMove)
      {
         numClosedSubPaths--;
      }

      return segment;
   }

   public JDRSegment removeSegment(int index)
     throws ArrayIndexOutOfBoundsException,
      ClosingMoveException
   {
      JDRPathSegment segment = removeSegmentFromList(index);

      if (index > 0)
      {
         segmentList_[index].setStart(segmentList_[index-1].getEnd());
      }

      if (index < size()-2)
      {
         segmentList_[index+1].setStart(segmentList_[index].getEnd());
      }
      else if (index == size()-1 && isClosed())
      {
         segmentList_[index].setEnd(segmentList_[0].getStart());
      }

      firePathChangeEvent(index, JDRPathChangeEvent.Type.SEGMENT_REMOVED,
         segment, null);

      return (JDRSegment)segment;
   }

   public JDRSegment removeLastSegment()
   {
      // NB check not empty before calling this method
      int index = size()-1;

      JDRPathSegment segment = removeSegmentFromList(index);

      if (index > 0)
      {
         segmentList_[index].setStart(segmentList_[index-1].getEnd());
      }

      if (index < size()-2)
      {
         segmentList_[index+1].setStart(segmentList_[index].getEnd());
      }
      else if (index == size()-1 && isClosed())
      {
         segmentList_[index].setEnd(segmentList_[0].getStart());
      }

      try
      {
         firePathChangeEvent(index, JDRPathChangeEvent.Type.SEGMENT_REMOVED,
            segment, null);
      }
      catch (ClosingMoveException e)
      {// shouldn't happen
         getCanvasGraphics().getMessageSystem().postMessage(
           MessageInfo.createInternalError(e));
      }

      return (JDRSegment)segment;
   }

   @Override
   public JDRPathSegment setSegment(int index, JDRPathSegment segment)
   throws ArrayIndexOutOfBoundsException,NullPointerException,
    InvalidPathException
   {
      if (index < 0 || index >= size_)
      {
         throw new ArrayIndexOutOfBoundsException(index);
      }

      if (segment == null)
      {
         throw new NullPointerException(
            "Path can't contain null segments");
      }

      JDRSegment oldSegment = segmentList_[index];
      segmentList_[index] = (JDRSegment)segment;

      if (index > 0)
      {
         segment.setStart(segmentList_[index-1].getEnd());
      }

      if (index == size()-1 && isClosed())
      {
         segment.setEnd(segmentList_[0].getStart());
      }

      if (oldSegment instanceof JDRClosingMove)
      {
         numClosedSubPaths--;
      }

      if (segment instanceof JDRClosingMove)
      {
         numClosedSubPaths++;
      }

      firePathChangeEvent(index, JDRPathChangeEvent.Type.SEGMENT_CHANGED,
        oldSegment, segment);

      return oldSegment;
   }

   /**
    * Appends the given segment to this path, enlarging the path's
    * capacity if necessary.
    * @param s the segment to append to this path
    */
   @Override
   public void add(JDRSegment s) throws InvalidPathException
   {
      JDRPathSegment lastSegment = getLastSegment();

      if (lastSegment != null)
      {
         s.start = lastSegment.getEnd();
      }

      addSegmentToList(s);
   }

   /**
    * Adds a new point midway along the currently edited segment.
    * To be more precise, it replaces the edited segment with two 
    * segments that lie on the first and second halves of the edited 
    * segment, respectively.
    * @return the newly added point or null if this path is not
    * being edited
    */
   public JDRPoint addPoint()
   {
      // add a point midway along selectedSegment

      int currentSegmentIndex = getSelectedIndex();
      int currentPointIndex = getSelectedControlIndex();
      JDRPathSegment currentSegment = getSelectedSegment();

      if (currentSegment == null || currentPointIndex == -1
        || currentSegmentIndex == -1)
      {
         return null;
      }

      stopEditing();

      JDRPathSegment newSegment = currentSegment.split();

      try
      {
         addSegmentToList(currentSegmentIndex+1, (JDRSegment)newSegment);
      }
      catch (ClosingMoveException e)
      {
         // shouldn't occur as splitting a closing move will 
         // create a non-closing move

         getCanvasGraphics().getMessageSystem().postMessage(
           MessageInfo.createInternalError(e));
      }

      JDRPoint p = selectControl(currentPointIndex);

      return p;
   }

   /**
    * Makes the currently edited segment continuous along its start
    * or end point. Does nothing if the path is not being edited
    * or if the edited segment is not an instance of {@link JDRBezier}.
    * @param atStart if segment should be made continuous at the
    * start
    */
   @Override
   public void makeContinuous(boolean atStart, boolean equiDistant)
   {
      if (selectedSegment == null || !(selectedSegment instanceof JDRBezier))
      {
         return;
      }

      JDRBezier curve = (JDRBezier)selectedSegment;

      int idx = getIndex(curve);

      int adjIdx = -1;

      if (atStart)
      {
         adjIdx = idx-1;

         if (adjIdx == -1 && closed)
         {
            adjIdx = size_-1;
         }
      }
      else
      {
         adjIdx = idx+1;

         if (adjIdx == size_)
         {
            adjIdx = (closed ? 0 : -1);
         }
      }

      if (idx == -1) return;

      JDRSegment adjSegment = (JDRSegment)get(adjIdx);

      if (atStart)
      {
         Point2D gradient = adjSegment.getdP1();

         if (equiDistant)
         {
            curve.setStartGradient(gradient);
         }
         else
         {
            double length = Math.sqrt(gradient.getX()*gradient.getX()+
               gradient.getY()*gradient.getY());

            if (length > 0)
            {
               Point2D dp = curve.getdP0();

               double orgLength = Math.sqrt(dp.getX()*dp.getX()+
                                            dp.getY()*dp.getY());

               double factor = orgLength/length;

               curve.setStartGradient(factor*gradient.getX(),
                                      factor*gradient.getY());
            }
         }
      }
      else
      {
         Point2D gradient = adjSegment.getdP0();

         if (equiDistant)
         {
            curve.setEndGradient(gradient);
         }
         else
         {
            double length = Math.sqrt(gradient.getX()*gradient.getX()+
               gradient.getY()*gradient.getY());

            if (length > 0)
            {
               Point2D dp = curve.getdP1();

               double orgLength = Math.sqrt(dp.getX()*dp.getX()+
                  dp.getY()*dp.getY());

               double factor = orgLength/length;

               curve.setEndGradient(factor*gradient.getX(),
                                    factor*gradient.getY());
            }
         }
      }

      try
      {
         firePathChangeEvent(idx, JDRPathChangeEvent.Type.CONTROLS_ADJUSTED,
          curve, curve);
      }
      catch (ClosingMoveException e)
      {
         // Shouldn't occur as not replacing, adding or removing any
         // segments.

         getCanvasGraphics().getMessageSystem().postMessage(
           MessageInfo.createInternalError(e));
      }
   }

   /**
    * Gets the index of the given segment or -1 if the segment
    * is not a part of this path. The search starts from the start
    * of the path.
    * @param segment the segment to find
    * @return the index of the segment or -1 if it is not a part
    * of this path
    * @see #getLastIndex(JDRPathSegment)
    */
   @Override
   public int getIndex(JDRPathSegment segment)
   {
      for (int i = 0; i < size_; i++)
      {
         if (segmentList_[i] == segment)
         {
            return i;
         }
      }

      return -1;
   }

   @Override
   public boolean segmentHasEnd(JDRPathSegment segment)
   {
      return (!isClosed() && segment == getLastSegment());
   }

   /**
    * Gets the index of the given segment or -1 if the segment
    * is not a part of this path. The search starts from the end
    * of the path.
    * @param segment the segment to find
    * @return the index of the segment or -1 if it is not a part
    * of this path
    * @see #getIndex(JDRPathSegment)
    */
   @Override
   public int getLastIndex(JDRPathSegment segment)
   {
      for (int i = size_-1; i >= 0; i--)
      {
         if (segmentList_[i] == segment)
         {
            return i;
         }
      }

      return -1;
   }

   /**
    * Replaces the segment at the given index with a new segment.
    * Control points are adjusted as appropriate.
    * @param idx the index at which to substitute the new segment
    * @param newSegment the new segment
    */
   @Override
   public void convertSegment(int idx, JDRPathSegment newSegment)
   throws InvalidPathException
   {
      JDRPathSegment oldSegment = setSegment(idx, newSegment);

      if (selectedSegment == oldSegment)
      {
         selectedSegment = newSegment;
      }

      newSegment.getStart().setSelected(oldSegment.getStart().isSelected());
      newSegment.getEnd().setSelected(oldSegment.getEnd().isSelected());

      // Anchors must be removed if the new segment isn't a Bezier

      if (!(newSegment instanceof JDRBezier))
      {
         newSegment.getStart().setAnchored(false);
         newSegment.getEnd().setAnchored(false);
      }

      if (selectedControl == oldSegment.getStart())
      {
         selectedControl = newSegment.getStart();
      }
      else if (selectedControl == oldSegment.getEnd())
      {
         selectedControl = newSegment.getEnd();
      }

      if (oldSegment instanceof JDRBezier)
      {
         JDRBezier curve = (JDRBezier)oldSegment;

         if (curve.control1.selected)
         {
            curve.control1.setSelected(false);
            curve.getStart().setSelected(false);
            newSegment.getStart().setSelected(true);
         }
         else if (curve.control2.selected)
         {
            curve.control2.setSelected(false);
            curve.getEnd().setSelected(false);
            newSegment.getEnd().setSelected(true);
         }

         if (selectedControl == curve.control1)
         {
            selectedControl = newSegment.getStart();
         }
         else if (selectedControl == curve.control2)
         {
            selectedControl = newSegment.getEnd();
         }
      }

      int n = size_-1;

      if (idx != n)
      {
         JDRSegment nextSeg = (JDRSegment)get(idx+1);
         nextSeg.setStart(newSegment.getEnd());
      }
      else if (closed && idx == n)
      {
         JDRSegment nextSeg = (JDRSegment)get(0);
         nextSeg.setStart(newSegment.getEnd());
      }

      if (idx > 0)
      {
         newSegment.setStart(get(idx-1).getEnd());
      }
      else if (idx == 0 && closed)
      {
         newSegment.setStart(get(n).getEnd());
      }

      firePathChangeEvent(idx, JDRPathChangeEvent.Type.SEGMENT_CHANGED,
       oldSegment, newSegment);
   }

   /**
    * Removes the segment at the given index and adjusts surrounding
    * control points as appropriate.
    * @param i the index of the segment to remove
    * @return the removed segment
    */
   @Override
   public JDRSegment remove(int i) throws InvalidPathException
   {
      JDRSegment segment = (JDRSegment)get(i);
      JDRPoint dp = segment.getEnd();

      int index = getSelectedControlIndex();

      if (dp == selectedControl || 
         (segment instanceof JDRBezier
          && selectedControl == ((JDRBezier)segment).control2))
      {
         dp = segment.getStart();
      }

      if (i == 0)
      {
         if (closed)
         {
            JDRSegment prev = (JDRSegment)get(size_-1);
            prev.setEnd(dp);
         }
      }
      else
      {
         JDRSegment prev = (JDRSegment)get(i-1);
         prev.setEnd(dp);
      }

      JDRSegment oldSegment = removeSegmentFromList(i);

      stopEditing();

      dp = selectControl(index);

      if (dp == null)
      {
         selectControl(0);
      }

      firePathChangeEvent(i, JDRPathChangeEvent.Type.SEGMENT_REMOVED,
         oldSegment, null);

      return oldSegment;
   }

   /**
    * Removes the given segment and adjusts surrounding
    * control points as appropriate.
    * @param segment the segment to remove
    * @return the removed segment or null if not found
    */
   @Override
   public JDRPathSegment remove(JDRPathSegment segment)
   throws InvalidPathException
   {
      for (int i = 0; i < size_; i++)
      {
         if (get(i) == segment)
         {
            return remove(i);
         }
      }

      return null;
   }

   @Override
   public JDRPathSegment removeSelectedSegment()
   throws InvalidPathException
   {
      JDRPathSegment segment = null;

      if (selectedSegment != null)
      {
         segment = remove(selectedSegment);
      }

      return segment;
   }

   /**
    * @throws NoSegmentSelectedException if no segment has been
    * selected for editing
    * @throws SelectedSegmentNotFoundException if the edited 
    * segment can't be found
    */
   public JDRShape breakPath()
      throws InvalidPathException
   {
      if (selectedSegment == null)
      {
         throw new NoSegmentSelectedException(getCanvasGraphics());
      }

      JDRPath newPath = new JDRPath(size_,
        (JDRPaint)getLinePaint().clone(),
        (JDRPaint)getFillPaint().clone(),
        (JDRStroke)getStroke().clone());

      int index = -1;

      for (int i = 0; i < size_; i++)
      {
         JDRSegment segment = (JDRSegment)get(i);

         if (segment == selectedSegment)
         {
            index = i;
            continue;
         }

         if (index > -1)
         {
            try
            {
               newPath.add((JDRSegment)segment.clone());
            }
            catch (ClosingMoveException e)
            {
               newPath.segmentList_[e.getSegmentIndex()]
                 = e.getSegment().convertToNonClosingMove();
            }
         }
      }

      if (index == -1)
      {
         throw new SelectedSegmentNotFoundException(getCanvasGraphics());
      }

      if (closed) open(false);

      // chop remaining segments from this path
      size_ = index+1;

      firePathChangeEvent(JDRPathChangeEvent.Type.PATH_CHANGED);

      return newPath;
   }

   /**
    * Gets the segment at the specified index.
    * @param index the index
    * @throws ArrayIndexOutOfBoundsException if the index is out
    * of range (<code>index &lt; 0 || index &gt;= size()</code>)
    */
   @Override
   public JDRPathSegment get(int index)
      throws ArrayIndexOutOfBoundsException
   {
      if (index < 0 || index >= size_)
      {
         throw new ArrayIndexOutOfBoundsException(index);
      }

      return segmentList_[index];
   }

   /**
    * Gets the last segment in this path.
    * @return the last segment in this path or null if this path
    * is empty
    */
   @Override
   public JDRPathSegment getLastSegment()
   {
      if (size_ == 0) return null;

      return segmentList_[size_-1];
   }

   /**
    * Gets the first segment in this path.
    * @return the first segment in this path or null if this path
    * is empty
    */
   @Override
   public JDRPathSegment getFirstSegment()
   {
      if (size_ == 0) return null;

      return segmentList_[0];
   }

   /**
    * Opens this path, removing the final segment.
    * @see #open(boolean)
    */
   @Override
   public void open() throws InvalidPathException
   {
      open(true);
   }

   /**
    * Opens this path, optionally removing the final segment.
    * @param removeLastSegment true if the last segment should be
    * removed
    * @see #open()
    */
   @Override
   public void open(boolean removeLastSegment)
   throws InvalidPathException
   {
      if (size_ == 0) return;

      if (closed)
      {
         JDRPathSegment oldSegment = null;

         if (removeLastSegment)
         {
            size_--;

            oldSegment = segmentList_[size_];
         }
         else
         {
           JDRSegment lastSegment = (JDRSegment)getLastSegment();

           JDRPoint endPt = lastSegment.end;

           lastSegment.end = (JDRPoint)endPt.clone();

           lastSegment.end.setSelected(false);
         }

         closed = false;

         firePathChangeEvent(JDRPathChangeEvent.Type.PATH_OPENED);

         if (oldSegment != null)
         {
            firePathChangeEvent(size_, JDRPathChangeEvent.Type.SEGMENT_REMOVED,
              oldSegment, null);
         }
      }
   }

   /**
    * Close this path with a segment matching the previous
    * final segment.
    */
   public void closeMatch() throws InvalidPathException
   {
      if (isEmpty()) return;

      if (!closed)
      {
         JDRSegment first = (JDRSegment)getFirstSegment();
         JDRSegment last  = (JDRSegment)getLastSegment();

         if (last instanceof JDRLine)
         {
            JDRLine line = new JDRLine(last.end, first.start);
            add(line);
            line.end = first.start;
         }
         else
         {
            JDRBezier curve = JDRBezier.constructBezier(
               last, first.start.getPoint2D());
            add(curve);
            curve.end = first.start;
         }
         
         closed = true;

         firePathChangeEvent(JDRPathChangeEvent.Type.PATH_CLOSED);
      }
   }

   /**
    * Closes this path with the given segment. The segment's start
    * and end points must fit the gap between the original opened 
    * path's end and start points.
    * @param segment the segment to use to close the path
    * @throws InvalidPathException if this path is empty
    * or if the segment doesn't fit
    */
   @Override
   public void close(JDRPathSegment segment)
      throws InvalidPathException
   {
      if (isEmpty())
      {
         throw(new EmptyPathException(getCanvasGraphics()));
      }

      JDRSegment last = (JDRSegment)getLastSegment();
      JDRSegment first = (JDRSegment)getFirstSegment();

      JDRPoint endPt = segment.getEnd();
      JDRPoint startPt = segment.getStart();

      if (last.end.x != startPt.getX() &&
          last.end.y != startPt.getY() &&
          first.start.x != endPt.getX() &&
          first.start.y != endPt.getY())
      {
         throw(new IllFittingPathException(getCanvasGraphics()));
      }

      ((JDRSegment)segment).end = first.start;
      add((JDRSegment)segment);

      closed=true;

      firePathChangeEvent(JDRPathChangeEvent.Type.PATH_CLOSED);
   }

   @Override
   public void close(int closeType)
      throws InvalidPathException
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (size() == 0)
      {
         throw new EmptyPathException(cg);
      }

      JDRSegment firstSeg = (JDRSegment)getFirstSegment();
      JDRSegment lastSeg = (JDRSegment)getLastSegment();

      switch (closeType)
      {
         case CLOSE_LINE :
            JDRSegment seg = new JDRLine(cg, lastSeg.end.x, lastSeg.end.y,
              firstSeg.start.x, firstSeg.start.y);

            seg.end = firstSeg.start;
            add(seg);
            closed = true;
         break;
         case CLOSE_CONT :
            Point2D dp1 = lastSeg.getdP1();
            Point2D dp2 = firstSeg.getdP0();

            JDRBezier curve = new JDRBezier(cg, lastSeg.end.x, lastSeg.end.y,
               lastSeg.end.x, lastSeg.end.y,
               firstSeg.start.x, firstSeg.start.y,
               firstSeg.start.x, firstSeg.start.y);

            curve.setGradients(dp1, dp2);

            curve.end = firstSeg.start;
            add(curve);
            closed = true;
         break;
         case CLOSE_MERGE_ENDS :
            lastSeg.end = firstSeg.start;
            closed = true;
         break;
         default:
            throw new JdrIllegalArgumentException(
               JdrIllegalArgumentException.CLOSE_TYPE, closeType, cg);
      }

      firePathChangeEvent(JDRPathChangeEvent.Type.PATH_CLOSED);
   }

   /**
    * Returns true if this path is closed.
    * @return true if this path is closed
    */
   @Override
   public boolean isClosed()
   {
      return closed;
   }

   @Override
   public boolean hasClosedSubPaths()
   {
      return numClosedSubPaths > 0;
   }

   /**
    * Returns true if this path is a polygon. That is, it only
    * consists of lines.
    * @return true if this path is a polygon
    */
   @Override
   public boolean isPolygon()
   {
      for (int i = 0, n = size(); i < n; i++)
      {
         if (get(i).isCurve())
         {
            return false;
         }
      }

      return true;
   }

   /**
    * Gets this path as a Path2D.
    * @return this path as a Path2D
    */
   @Override
   public Path2D getGeneralPath()
   {
      if (isEmpty()) return null;

      JDRStroke stroke = getStroke();
      Path2D.Double path = new Path2D.Double();

      if (stroke != null)
      {
         path.setWindingRule(stroke.getWindingRule());
      }

      boolean closePath = closed;

      JDRPathIterator pi = getIterator();

      JDRPathSegment segment = pi.next();

      path.moveTo(segment.getStart().x,
                  segment.getStart().y);
      segment.appendToGeneralPath(path);

      while (pi.hasNext())
      {
         segment = pi.next();

         segment.appendToGeneralPath(path);

         if (closed && !pi.hasNext() && segment instanceof JDRClosingMove)
         {
            closePath = false;
         }
      }

      segment = null;

      if (closePath) path.closePath();

      return path;
   }

   @Override
   public JDRShape toPolygon(double flatness)
    throws InvalidPathException
   {
      Shape path = getGeneralPath();

      PathIterator pi = path.getPathIterator(null, flatness);

      JDRPath newPath = getPath(getCanvasGraphics(), pi);
      newPath.setAttributes(this);

      return newPath;
   }

   /**
    * Creates a new path from the stroked outline of this path.
    * @return the path following this path's stroked outline
    * @throws InvalidPathException if this path is empty
    * or something is wrong with a closing segment
    */
   @Override
   public JDRShape outlineToPath()
      throws InvalidPathException
   {
      CanvasGraphics cg = getCanvasGraphics();

      Shape shape = getStorageStrokedArea();

      PathIterator pi = shape.getPathIterator(null);

      double[] coords = new double[6];

      int type = pi.currentSegment(coords);

      double x = coords[0];
      double y = coords[1];

      Point2D startpt = new Point2D.Double(coords[0], coords[1]);

      pi.next();

      JDRPath path = new JDRPath(cg, capacity_);
      path.setLinePaint(new JDRTransparent(cg));
      path.setFillPaint((JDRPaint)getLinePaint().clone());
      path.setStroke(new JDRBasicStroke(cg));
      boolean closeflag=false;
      JDRSegment lastPostMoveSeg = null;

      while (!pi.isDone())
      {
         type = pi.currentSegment(coords);
         JDRSegment segment;

         switch (type)
         {
            case PathIterator.SEG_CUBICTO :
               segment = new JDRBezier(cg, startpt,
                             new Point2D.Double(coords[0],coords[1]),
                             new Point2D.Double(coords[2],coords[3]),
                             new Point2D.Double(coords[4],coords[5]));
               startpt = new Point2D.Double(coords[4], coords[4]);
               path.add(segment);

               if (lastPostMoveSeg == null)
               {
                  lastPostMoveSeg = segment;
               }
               closeflag = false;

            break;
            case PathIterator.SEG_QUADTO :
               segment = JDRBezier.quadToCubic(cg, startpt.getX(),
                                            startpt.getY(), 
                                            coords[0],coords[1],
                                            coords[2],coords[3]);
               startpt = new Point2D.Double(coords[4], coords[4]);
               path.add(segment);

               if (lastPostMoveSeg == null)
               {
                  lastPostMoveSeg = segment;
               }
               closeflag = false;

            break;
            case PathIterator.SEG_LINETO :
               segment = new JDRLine(cg, startpt.getX(),
                                  startpt.getY(),
                                  coords[0], coords[1]);
               startpt = new Point2D.Double(coords[0], coords[1]);
               path.add(segment);

               if (lastPostMoveSeg == null)
               {
                  lastPostMoveSeg = segment;
               }
               closeflag = false;
            break;
            case PathIterator.SEG_MOVETO :
               if (closeflag && lastPostMoveSeg != null)
               {
                  segment = new JDRClosingMove(startpt.getX(),
                              startpt.getY(), coords[0], coords[1],
                              this, path.size(), lastPostMoveSeg);
               }
               else
               {
                  segment = new JDRSegment(cg, startpt.getX(),
                                     startpt.getY(),
                                     coords[0], coords[1]);
               }

               startpt = new Point2D.Double(coords[0], coords[1]);
               path.add(segment);

               lastPostMoveSeg = null;
               closeflag = false;
            break;
            case PathIterator.SEG_CLOSE :
               closeflag = true;
            break;
         }
         pi.next();
      }

      if (closeflag)
      {
         path.close(
            new JDRSegment(cg, startpt.getX(), startpt.getY(), x, y));
      }

      return path;
   }

   /**
    * Creates parshape information from this path. This computes the
    * parameters required for TeX's <code>\parshape</code> command.
    * The parameters are computed by passing horizontal scan lines 
    * across the shape.
    * @param cg graphics device on which to draw scan lines (may be
    * null if no drawing required)
    * @param dy the vertical distance between scan lines
    * @param outline if true use the outline of the stroked path
    * as the shape to be defined (as given by
    * {@link #getStorageStrokedArea()}) otherwise use the unstroked path
    * shape (as given by {@link #getGeneralPath()})
    * @return parshape graphics information in the event that
    * an application needs to draw the shape and scan lines
    * @throws InvalidShapeException if the required shape can't
    * be implemented using <code>\parshape</code>
    */
   public Parshape parshape(double dy, boolean outline)
      throws InvalidShapeException
   {
      CanvasGraphics cg = getCanvasGraphics();
      JDRUnit unit = cg.getStorageUnit();

      Graphics2D g2 = cg.getGraphics();

      // Area.contains more reliable than GeneralPath.contains
      Area area = outline
                  ? getStorageStrokedArea()
                  : new Area(getGeneralPath());

      if (g2 != null)
      {
         g2.setColor(Color.gray);
         g2.draw(AffineTransform.getScaleInstance(
                     cg.storageToComponentX(1.0),
                     cg.storageToComponentY(1.0))
                 .createTransformedShape(area));
         g2.setColor(Color.lightGray);
      }
      Rectangle2D bounds = area.getBounds2D();

      double offsetx = bounds.getMinX();
      double offsety = bounds.getMinY();

      int width = (int)Math.ceil(bounds.getWidth());
      double height = bounds.getHeight();

      if (width == 0)
      {
         throw(new ShapeHasNoWidthException(getCanvasGraphics()));
      }

      if (height == 0)
      {
         throw(new ShapeHasNoHeightException(getCanvasGraphics()));
      }

      int scanlines = (int)Math.ceil(height/dy);
      int n = scanlines;

      StringBuffer parameters = new StringBuffer();

      Path2D scanlinePath = new Path2D.Double();

      for (int i = 0; i < n; i++)
      {
         int intersects=0;
         boolean prevInside = false;
         boolean inside=false;
         int offset=0;
         int length=0;

         double y = offsety + i*dy;

         if (g2 != null)
         {
            cg.drawMagLine((int)offsetx,(int)y,(int)offsetx+width,(int)y);
         }
         scanlinePath.moveTo(offsetx, y);
         scanlinePath.lineTo(offsetx+width, y);

         for (int j = -1; j <= width; j++)
         {
            double x = offsetx + j;

            inside = area.contains(x, y);

            if ((inside && !prevInside)
             || (!inside && prevInside))
            {
               intersects++;
            }

            if (intersects > 2)
            {
               throw new TooManyIntersectsException(getCanvasGraphics(), x, y);
            }

            if (inside && !prevInside)
            {
               offset = j;
            }

            if (!inside && prevInside)
            {
               length = j-offset;
            }

            prevInside = inside;
         }

         if (length != 0)
         {
            parameters.append(String.format("%n%s %s ", unit.tex(offset), 
                        unit.tex(length)));
         }
         else
         {
            scanlines--;
         }
      }

      return new Parshape(
        String.format(Locale.ROOT,
           "\\parshape=%d%s", scanlines, parameters.toString()),
           scanlinePath, area);
   }

   private class ScanLine
   {
      public ScanLine(double start, double end){x=start;x1=end;}
      public double x=0.0, x1=0.0;
   }

   private class Begin extends ScanLine
   {
      public Begin(double x0){super(x0,x0);}
   }

   private class End extends ScanLine
   {
      public End(double x){super(x,x);}
   }

   private class Join extends ScanLine 
   {
      public Join(){super(0,0);}
   }

   private class Split extends ScanLine
   {
      public Split(){super(0,0);}
   }

   /**
    * Creates shapepar information from this path. This computes the
    * parameters required for the <code>\shapepar</code> command
    * defined in Donald Arseneau's shapepar package.
    * The parameters are computed by passing horizontal scan lines 
    * across the shape.
    * @param cg graphics device on which to draw scan lines (may be
    * null if no drawing required)
    * @param dy the vertical distance between scan lines
    * @param outline if true use the outline of the stroked path
    * as the shape to be defined (as given by
    * {@link #getStorageStrokedArea()}) otherwise use the unstroked path
    * shape (as given by {@link #getGeneralPath()})
    * @throws InvalidShapeException if the required shape can't
    * be implemented using <code>\shapepar</code>
    */
   public Parshape shapepar(boolean hpadding, double dy, boolean outline)
      throws InvalidShapeException
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();
      String eol = System.getProperty("line.separator", "\n");

      Area shape = outline
                 ? getStorageStrokedArea()
                 : new Area(getGeneralPath());

      Rectangle2D bounds = shape.getBounds2D();

      if (g2 != null)
      {
         g2.setColor(Color.gray);
         g2.draw(AffineTransform.getScaleInstance(
                     cg.storageToComponentX(1.0),
                     cg.storageToComponentY(1.0))
                 .createTransformedShape(shape));
         g2.setColor(Color.lightGray);
      }

      double offsetx = bounds.getMinX();
      double offsety = bounds.getMinY();

      double width = bounds.getWidth();
      double hcenter = 0.5*width;
      double height = bounds.getHeight();

      if (width == 0)
      {
         throw(new ShapeHasNoWidthException(getCanvasGraphics()));
      }

      if (height == 0)
      {
         throw(new ShapeHasNoHeightException(getCanvasGraphics()));
      }

      String str = (hpadding ? "\\shapepar" : "\\Shapepar") +"[" 
        + PGF.format(cg.getStorageUnit().toPt(1.0)) + "pt]{{"
        + PGF.format(hcenter)+"}%"+eol;

      double eps = 1-Double.MIN_VALUE;
      boolean firstRow=true;

      // make a list of all the scan line y coords
      // scanlines are dy apart, but also include y coords of all
      // vertices in the shape
      Vector<Double> scanY = new Vector<Double>();
      for (double y = 0; y <= height; y += dy)
      {
         scanY.add(new Double(y));
      }

      PathIterator pi = shape.getPathIterator(null);
      double[] coords = new double[6];

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         double y = coords[1]-offsety;
         boolean skip = false;

         double y0 = ((Double)scanY.get(0)).doubleValue();

         for (int i =0, n=scanY.size(); i < n; i++)
         {
            double y1 = ((Double)scanY.get(i)).doubleValue();

            if (y == y1)
            {
               skip = true;
               break;
            }

            if (y > y0 && y < y1)
            {
               scanY.add(i, new Double(y));
               skip = true;
               break;
            }

            if (i == n-1 && y > y1)
            {
               scanY.add(new Double(y));
               skip = true;
               break;
            }

            y0 = y1;
         }

         if (!skip && type == PathIterator.SEG_CUBICTO)
         {
            y = coords[5]-offsety;
            y0 = ((Double)scanY.get(0)).doubleValue();

            for (int i =0, n=scanY.size(); i < n; i++)
            {
               double y1 = ((Double)scanY.get(i)).doubleValue();

               if (y == y1)
               {
                  break;
               }

               if (y > y0 && y < y1)
               {
                  scanY.add(i, new Double(y));
                  break;
               }

               y0 = y1;
            }

         }

         pi.next();
      }

      Path2D scanlinePath = new Path2D.Double();

      for (int k=0, m = scanY.size(); k < m; k++)
      {
         double y = ((Double)scanY.get(k)).doubleValue();

         Vector<ScanLine> scanlines = new Vector<ScanLine>();

         double offset = 0;
         boolean prevInside = false;
         boolean prevBelowInside=false;
         boolean prevAboveInside=false;
         boolean nextInside = false;

         Rectangle2D r;
         r = new Rectangle2D.Double(offsetx,y+offsety-0.5,eps,eps);
         boolean inside = shape.intersects(r);

         r = new Rectangle2D.Double(offsetx,y+offsety+0.5,eps,eps);
         boolean belowInside = shape.intersects(r);

         r = new Rectangle2D.Double(offsetx,y+offsety-1.5,eps,eps);
         boolean aboveInside = shape.intersects(r);

         double startsplit=-1;
         double startjoin=-1;

         if (g2 != null)
         {
            double ycoord = offsety+y;
            cg.drawMagLine(offsetx, ycoord,
                           offsetx+width,ycoord);
         }

         scanlinePath.moveTo(offsetx, offsety+y);
         scanlinePath.lineTo(offsetx+width, offsety+y);

         for (double x = 0; x <= width; x++)
         {
            r = new Rectangle2D.Double(x+offsetx+1,y+offsety-0.5,eps,eps);
            nextInside = shape.intersects(r);

            r=new Rectangle2D.Double(x+1+offsetx,y+offsety+0.5,eps,eps);
            boolean nextBelowInside = shape.intersects(r);

            r=new Rectangle2D.Double(x+1+offsetx,y+offsety-1.5,eps,eps);
            boolean nextAboveInside = shape.intersects(r);

            if (inside && !prevInside 
            && !prevAboveInside && !aboveInside)
            {
               offset = x;
               // is it a local maximum?
               boolean maximum = true;
               for (double x0=x+1; x0 <= width; x0++)
               {
                  r = new Rectangle2D.Double(x0+offsetx,
                                             y+offsety-0.5,
                                             eps,eps);
                  boolean tmpInside = shape.intersects(r);
                  r = new Rectangle2D.Double(x0+offsetx,
                                             y+offsety-1.5,
                                             eps,eps);
                  boolean tmpAboveInside = shape.intersects(r);

                  if (tmpAboveInside)
                  {
                     maximum = false;
                     break;
                  }

                  if (!tmpInside) break;
               }
               if (maximum) scanlines.add(new Begin(x));
            }
            else if (inside && !prevInside
               && !prevBelowInside && !belowInside)
            {
               offset = x;
               // is it a local minimum?
               boolean minimum = true;
               for (double x0=x+1; x0 <= width; x0++)
               {
                  r = new Rectangle2D.Double(x0+offsetx,
                                             y+offsety-0.5,
                                             eps,eps);
                  boolean tmpInside = shape.intersects(r);
                  r = new Rectangle2D.Double(x0+offsetx,
                                             y+offsety+0.5,
                                             eps,eps);
                  boolean tmpBelowInside = shape.intersects(r);

                  if (tmpBelowInside)
                  {
                     minimum = false;
                     break;
                  }

                  if (!tmpInside) break;
               }
               if (minimum) scanlines.add(new End(x));
            }
            else if (inside && prevInside && nextInside
                     && prevAboveInside && !aboveInside)
            {
               startjoin = x;
            }
            else if (inside && prevInside && nextInside
                   && prevBelowInside && !belowInside)
            {
               startsplit = x;
            }
            else if (inside && nextInside 
                    && nextAboveInside && !aboveInside 
                    && startjoin !=-1)
            {
               double midx = startjoin+0.5*(x-startjoin);
               ScanLine line = new ScanLine(offset, midx);
               scanlines.add(line);
               scanlines.add(new Join());
               offset = midx;
               startjoin = -1;
            }
            else if (inside && nextInside
                   && nextBelowInside && !belowInside
                   && startsplit != -1)
            {
               double midx = startsplit+0.5*(x-startsplit);
               ScanLine line = new ScanLine(offset, midx);
               scanlines.add(line);
               scanlines.add(new Split());
               offset = midx;
               startsplit = -1;
            }
            else if (!prevInside && inside)
            {
               offset = x;
            }
            else if (inside && !nextInside)
            {
               ScanLine line = new ScanLine(offset, x+1);
               scanlines.add(line);
            }

            prevInside = inside;
            inside = nextInside;
            prevBelowInside = belowInside;
            prevAboveInside = aboveInside;
            belowInside = nextBelowInside;
            aboveInside = nextAboveInside;
         }

         int n=scanlines.size();

         if (n > 0)
         {
            String thisRow = "";
            String nextRow = "";
            String prevRow = "";

            for (int i = 0; i < n; i++)
            {
               ScanLine object = scanlines.get(i);

               if (object instanceof Split)
               {
                  thisRow += "s";
               }
               else if (object instanceof Join)
               {
                  thisRow += "j";
               }
               else if (object instanceof Begin)
               {
                  prevRow += "b{"+PGF.format(((Begin)object).x)+"}";
               }
               else if (object instanceof End)
               {
                  nextRow += "e{"+PGF.format(((End)object).x)+"}";
               }
               else
               {
                  double len = object.x1-object.x;

                  thisRow += "t{"+PGF.format(object.x)+"}{"+PGF.format(len)+"}";
               }
            }

            if (!prevRow.equals(""))
               str += (firstRow?"":"\\\\")+"{"+PGF.format(y)+"}"+prevRow+"%"+eol;
            if (!thisRow.equals(""))
               str += "\\\\{"+PGF.format(y)+"}"+thisRow+"%"+eol;
            if (!nextRow.equals(""))
               str += "\\\\{"+PGF.format(y)+"}"+nextRow+"%"+eol;

            firstRow = false;
         }
      }

      str += "}";

      return new Parshape(str,scanlinePath,shape);
   }

   /**
    * Translate the given control point on the given segment.
    * Adjusts control point of neighbouring B&eacute;zier curve
    * if necessary.
    * @param segment the segment containing the control point
    * to move
    * @param p the control point to move
    * @param x the x shift to move the control point
    * @param y the y shift to move the control point
    */
   @Override
   public void translateControl(JDRPathSegment segment, JDRPoint p, 
                                double x, double y)
   {
      int segIdx = -1;

      if (segment instanceof JDRBezier)
      {
         if (p == ((JDRBezier)segment).control1 ||
             p == ((JDRBezier)segment).control2)
         {
            p.translate(x, y);
            return;
         }

         JDRSegment prev = null;
         JDRSegment next = null;
         JDRSegment seg  = null;

         for (int i = 0, n = size_-1; i < size_; i++)
         {
            prev = seg;
            seg = (JDRSegment)get(i);

            if (seg == segment)
            {
               segIdx = i;

               if (i < n)
               {
                  next = (JDRSegment)get(i+1);
               }

               if (i == 0 && closed)
               {
                  prev = (JDRSegment)get(n);
               }

               break;
            }
         }

         if (p == segment.getStart())
         {
            if (prev != null && prev instanceof JDRBezier)
            {
               ((JDRBezier)prev).control2.translate(x,y);
            }

            ((JDRBezier)segment).control1.translate(x,y);
         }
         else if (p == segment.getEnd())
         {
            if (next != null && next instanceof JDRBezier)
            {
               ((JDRBezier)next).control1.translate(x,y);
            }

            ((JDRBezier)segment).control2.translate(x,y);
         }
      }

      p.translate(x, y);

      try
      {
         firePathChangeEvent(segIdx, JDRPathChangeEvent.Type.CONTROLS_ADJUSTED,
          segment, segment);
      }
      catch (ClosingMoveException e)
      {
         // Shouldn't occur
         getCanvasGraphics().getMessageSystem().postMessage(
           MessageInfo.createInternalError(e));
      }
   }

   /**
    * Creates a new path defined a rectangle given two opposing
    * points.
    * @param p1 the first point
    * @param p2 the second point
    * @return path describing rectangle
    */
   public static JDRPath constructRectangle(CanvasGraphics cg,
      Point2D p1, Point2D p2)
   {
      return constructRectangle(cg, p1.getX(), p1.getY(), p2.getX(), p2.getY());
   }

   /**
    * Creates a new path defined a rectangle given two opposing
    * points.
    * @param p1x the x co-ordinate of the first point
    * @param p1y the y co-ordinate of the first point
    * @param p2x the x co-ordinate of the second point
    * @param p2y the y co-ordinate of the second point
    * @return path describing rectangle
    */
   public static JDRPath constructRectangle(CanvasGraphics cg,
      double p1x, double p1y, double p2x, double p2y)
   {
      JDRPath path = new JDRPath(cg);

      try
      {
         path.add(new JDRLine(cg, new Point2D.Double(p1x, p1y),
                           new Point2D.Double(p1x, p2y)));
         path.add(new JDRLine(cg, new Point2D.Double(p1x, p2y),
                           new Point2D.Double(p2x, p2y)));
         path.add(new JDRLine(cg, new Point2D.Double(p2x, p2y),
                           new Point2D.Double(p2x, p1y)));
         path.close();
      }
      catch (InvalidPathException e)
      {// shouldn't occur
         cg.getMessageSystem().postMessage(
           MessageInfo.createInternalError(e));
      }

      return path;
   }

   /**
    * Constructs a path composed of cubic B&eacute;zier segments
    * approximating an ellipse centred on the given point.
    * @param p the centre point
    * @param rx half the width of the ellipse
    * @param ry half the height of the ellipse
    */
   public static JDRPath constructEllipse(CanvasGraphics cg,
      Point2D p, double rx, double ry)
   {
      Ellipse2D ellipse = new Ellipse2D.Double(
         p.getX()-rx,p.getY()-ry,2*rx,2*ry);

      PathIterator pi = ellipse.getPathIterator(null);

      JDRPath path = new JDRPath(cg);

      double[] coords = new double[6];

      int i=0;

      Point2D start = new Point2D.Double(0,0);
      JDRBezier arc;

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         if (type == PathIterator.SEG_MOVETO)
         {
            start = new Point2D.Double(coords[0], coords[1]);
         }
         else if (type == PathIterator.SEG_CUBICTO)
         {
            arc = new JDRBezier(cg, start,
                             new Point2D.Double(coords[0],coords[1]),
                             new Point2D.Double(coords[2],coords[3]),
                             new Point2D.Double(coords[4],coords[5]));
            start = new Point2D.Double(coords[4], coords[4]);

            i++;
            try
            {
               if (i==4) path.close(arc);
               else path.add(arc);
            }
            catch (InvalidPathException e)
            {
               // shouldn't occur

               cg.getMessageSystem().postMessage(
                 MessageInfo.createInternalError(e));
            }
         }

         pi.next();
      }

      return path;
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (!(obj instanceof JDRPath)) return false;

      if (!super.equals(obj)) return false;

      JDRPath path = (JDRPath)obj;

      if (!getLinePaint().equals(path.getLinePaint())) return false;
      if (!getFillPaint().equals(path.getFillPaint())) return false;
      if (!getStroke().equals(path.getStroke())) return false;

      if (size_ != path.size_) return false;

      for (int i = 0; i < size_; i++)
      {
         if (get(i) != path.get(i)) return false;
      }

      return true;
   }

   public void makeEqual(JDRObject obj)
   {
      JDRPath path = (JDRPath)obj;
      super.makeEqual(path);

      setLinePaint((JDRPaint)path.getLinePaint().clone());

      if (path.getFillPaint() == null)
      {
         setFillPaint(new JDRTransparent(getCanvasGraphics()));
      }
      else
      {
         setFillPaint((JDRPaint)path.getFillPaint().clone());
      }

      setStroke((JDRStroke)path.getStroke().clone());

      if (path.capacity_ != capacity_)
      {
         capacity_ = path.size_;

         segmentList_ = new JDRSegment[capacity_];
      }

      size_ = 0;

      selectedSegmentIndex = path.selectedSegmentIndex;
      selectedControlIndex = path.selectedControlIndex;
      selectedSegment=null;
      selectedControl=null;

      for (int i = 0; i < path.size_; i++)
      {
         JDRSegment segment = (JDRSegment)path.get(i);
         JDRSegment newsegment = (JDRSegment)segment.clone();

         try
         {
            if (i == path.size_-1 && path.closed)
            {
               close(newsegment);
            }
            else
            {
               add(newsegment);
            }
         }
         catch (InvalidPathException e)
         {
            getCanvasGraphics().getMessageSystem().postMessage(
              MessageInfo.createInternalError(e));
         }

         if (segment == path.selectedSegment)
         {
            selectedSegment = newsegment;
            selectedSegment.setSelected(true);

            for (int j = 0, n = segment.controlCount(); j < n; j++)
            {
               JDRPoint p = segment.getControl(j);

               if (p == path.selectedControl)
               {
                  selectedControl = newsegment.getControl(j);
                  selectedControl.setSelected(true);
               }
            }

            if (selectedControl == null && path.closed)
            {
               JDRPoint p = segment.getEnd();

               if (p == path.selectedControl)
               {
                  selectedControl = newsegment.getEnd();
                  selectedControl.setSelected(true);
               }
            }
         }
      }

   }

   @Override
   public Object clone()
   {
      JDRPath path = new JDRPath(getCanvasGraphics(), capacity_);
      path.makeEqual(this);

      return path;
   }

   @Override
   public JDRPath getBaseUnderlyingPath()
   {
      return this;
   }

   /**
    * Returns a copy of this path.
    */
   @Override
   public JDRShape getFullPath()
      throws InvalidPathException
   {
      return (JDRPath)clone();
   }

   @Override
   protected void stopEditing()
   {
      if (selectedSegment != null)
      {
         selectedSegment.setSelected(false);
      }

      selectedSegment = null;
      selectedSegmentIndex = -1;
      selectedControl = null;
      selectedControlIndex   = -1;

      editMode = false;
   }

   public void setEditMode(boolean mode)
   {
      if (mode)
      {
         selectNextControl();
      }
      else
      {
         stopEditing();
      }
   }

   /**
    * Gets the index of the currently selected control point.
    * The first control point in the
    * path (i.e. the start point of the first segment) has index 0.
    * The point index is increment in the order: start, [control1,
    * control2], start, [control1, control2], ... Note that the
    * end points are not included except for the final end point
    * where the path is open. This is because the end point of one
    * segment is the same as the start point of the next segment,
    * except for the final point in an open path. In a closed path,
    * the end point of the final segment is the same as the start
    * point of the first segment.
    * @return the index of the currently selected control point,
    * or -1 if none selected
    */
   @Override
   public int getSelectedControlIndex()
   {
      return selectedControlIndex;
   }

   @Override
   public JDRPathSegment getSelectedSegment()
   {
      return selectedSegment;
   }

   @Override
   public JDRPoint getSelectedControl()
   {
      return selectedControl;
   }

   @Override
   public int getSelectedIndex()
   {
      return selectedSegmentIndex;
   }

   /** 
    * Prints just the path construction commands.
    */
   public void savePgf(TeX tex)
     throws IOException
   {
      BBox pathBBox = getStorageBBox();

      if (pathBBox == null)
      {
         return;
      }

      CanvasGraphics cg = getCanvasGraphics();

      JDRPaint linePaint = getLinePaint();

      JDRPaint paint = getFillPaint();

      JDRBasicStroke stroke = (JDRBasicStroke)getStroke();

      tex.println("\\begin{pgfscope}");

      if (linePaint instanceof JDRShading)
      {
         if (!(paint instanceof JDRTransparent))
         {
            stroke.savePgf(tex);

            tex.println(paint.pgffillcolor(pathBBox));
            tex.print(stroke.windingRule==Path2D.WIND_EVEN_ODD ? 
                   "\\pgfseteorule " :
                   "\\pgfsetnonzerorule ");

            if (paint instanceof JDRGradient
             || paint instanceof JDRRadial)
            {
               tex.println(linePaint.pgfstrokecolor(pathBBox));

               if (!(linePaint instanceof JDRTransparent))
               {
                  tex.println("\\pgfusepath{stroke}");
               }
            }
            else
            {
               tex.println("\\pgfusepath{fill}");
            }
         }

         try
         {
            outlineToPath().savePgf(tex);
         }
         catch (InvalidPathException e)
         {
            getCanvasGraphics().getMessageSystem().getPublisher().publishMessages(
               MessageInfo.createWarning(e));
         }

      }
      else
      {
         stroke.savePgf(tex);

         if (paint instanceof JDRTransparent)
         {
            savePgfPath(tex);

            if (!(linePaint instanceof JDRTransparent))
            {
               tex.println(linePaint.pgfstrokecolor(pathBBox));
               tex.println("\\pgfusepath{stroke}");
            }
         }
         else
         {
            savePgfPath(tex);

            tex.println(paint.pgffillcolor(pathBBox));
            tex.println(stroke.windingRule==Path2D.WIND_EVEN_ODD ? 
                   "\\pgfseteorule " :
                   "\\pgfsetnonzerorule ");

            if (paint instanceof JDRGradient
             || paint instanceof JDRRadial)
            {
               tex.print(linePaint.pgfstrokecolor(pathBBox));

               if (!(linePaint instanceof JDRTransparent))
               {
                  tex.println("\\pgfusepath{stroke}");
               }
            }
            else if (linePaint instanceof JDRTransparent)
            {
               tex.println("\\pgfusepath{fill}");
            }
            else
            {
               tex.println(linePaint.pgfstrokecolor(pathBBox));

               tex.println("\\pgfusepath{fill,stroke}");
            }
         }
      }

      tex.println("\\end{pgfscope}");

      pgfMarkers(tex, pathBBox);
   }

   public void pgfMarkers(TeX tex, BBox pathBBox)
     throws IOException
   {
      JDRBasicStroke stroke = (JDRBasicStroke)getStroke();

      if (stroke.getStartArrow().getType() == JDRMarker.ARROW_NONE
       && stroke.getEndArrow().getType() == JDRMarker.ARROW_NONE
       && stroke.getMidArrow().getType() == JDRMarker.ARROW_NONE)
      {
         return;
      }

      JDRPaint linePaint = getLinePaint();

      JDRPathIterator pi = getIterator();

      while (pi.hasNext())
      {
         JDRPathSegment segment = pi.next();

         JDRMarker marker = segment.getStartMarker();

         if (marker != null && marker.getType() != JDRMarker.ARROW_NONE)
         {
            marker.pgfShape(linePaint, pathBBox,
                      segment, true, tex);
         }

         marker = segment.getEndMarker();

         if (marker != null && marker.getType() != JDRMarker.ARROW_NONE)
         {
            marker.pgfShape(linePaint, pathBBox,
                      segment, false, tex);
         }
      }

   }

   public int psLevel()
   {
      int level = 1;

      level = Math.max(level, getFillPaint().psLevel());
      level = Math.max(level, getLinePaint().psLevel());

      return level;
   }

   public void saveSVG(SVG svg, String attr)
      throws IOException
   {
      if (isEmpty()) return;

      JDRPathSegment segment = get(0);

      svg.print("   <path "+attr+" d=\"M ");

      segment.getStart().saveSVG(svg);
      segment.saveSVG(svg);

      for (int i = 1; i < size_; i++)
      {
         segment = get(i);
         segment.saveSVG(svg);
      }

      if (closed) svg.print("z");

      svg.println("\"");
      svg.println("      "+getLinePaint().svgLine());
      svg.println("      "+getFillPaint().svgFill());

      JDRStroke s = getStroke();

      if (s != null && s instanceof JDRBasicStroke)
      {
         svg.println("      "+ ((JDRBasicStroke)s).svg(getLinePaint()));
      }

      svg.println("   />");
   } 

   /**
    * Gets string representation of this path.
    * @return string representation of this path
    */
   public String toString()
   {
      StringBuilder builder = new StringBuilder();

      builder.append("Path: size="+size_+", segments=[");

      for (int i = 0; i < size_; i++)
      {
         builder.append(get(i));
         builder.append(',');
      }

      builder.append("], closed="+isClosed());
      builder.append(", closed sub paths="+numClosedSubPaths);

      return builder.toString();
   }

   public JDRObjectLoaderListener getListener()
   {
      return pathListener;
   }

   /**
    * Returns the default initial capacity for new paths.
    * This method checks {@link CanvasGraphics#getOptimize()} and
    * will return either {@link #init_capacity_speed} (if
    * OPTIMIZE_SPEED) or
    * {@link #init_capacity_memory} (if OPTIMIZE_MEMORY or
    * OPTIMIZE_NONE)
    */
   public int getInitCapacity()
   {
      return ((getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)?
               init_capacity_speed
             : init_capacity_memory);
   }

   public String info()
   {
      StringBuilder builder = new StringBuilder(
       String.format("Path:%nsize: %d%n", size_));

      for (int i = 0; i < size_; i++)
      {
         builder.append(String.format("Segment %d:%n%s%n",
           i, segmentList_[i].info()));
      }

      builder.append(String.format("closed: %s%nline paint: %s%nfill paint: %s%ncapacity: %d%nedited segment: %s%nedited control: %s%nedited index: %s%nstroke: %s%narea: %f%n",
        isClosed(), getLinePaint(), getFillPaint(), capacity_, selectedSegment,
        selectedControl, selectedControlIndex, 
        stroke == null ? "null" : stroke.info(),
        computeArea(getGeneralPath())));

      builder.append(super.info());

      return builder.toString();
   }

   protected void setSegmentList(JDRSegment[] list, int size)
   {
      segmentList_ = list;
      size_ = size;
   }

   protected JDRSegment[] getSegmentList()
   {
      return segmentList_;
   }

   @Override
   protected void selectControl(JDRPoint p, int pointIndex, int segmentIndex)
   {
      stopEditing();

      selected = true;

      JDRPathSegment segment;

      try
      {
         segment = get(segmentIndex);
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         segment = null;
      }

      setSelectedElements(segmentIndex, pointIndex, segment, p);

      if (selectedControl != null) selectedControl.setSelected(true);
      if (selectedSegment != null) selectedSegment.setSelected(true);

      editMode = true;
   }

   @Override
   public JDRTextual getTextual()
   {
      return null;
   }

   @Override
   public boolean hasTextual()
   {
      return false;
   }

   @Override
   public boolean showPath()
   {
      return true;
   }

   @Override
   public boolean hasSymmetricPath()
   {
      return false;
   }

   @Override
   public JDRSymmetricPath getSymmetricPath()
   {
      return null;
   }

   @Override
   public boolean hasPattern()
   {
      return false;
   }

   @Override
   public JDRPattern getPattern()
   {
      return null;
   }

   @Override
   protected void setSelectedElements(
      int segmentIndex, int controlIndex,
      JDRPathSegment segment, JDRPoint control)
   {
      selectedSegmentIndex = segmentIndex;
      selectedSegment = segment;
      selectedControl = control;
      selectedControlIndex = controlIndex;
   }

   public void fade(double value)
   {
      getLinePaint().fade(value);
      getFillPaint().fade(value);
      ((JDRBasicStroke)getStroke()).fade(value);
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      for (int i = 0, m = size_-1; i < size_; i++)
      {
         segmentList_[i].applyCanvasGraphics(cg);
      }

      super.applyCanvasGraphics(cg);
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      super.setCanvasGraphics(cg);

      for (int i = 0; i < size_; i++)
      {
         segmentList_[i].setCanvasGraphics(cg);
      }

      if (fillPaint != null)
      {
         fillPaint.setCanvasGraphics(cg);
      }

      if (linePaint != null)
      {
         linePaint.setCanvasGraphics(cg);
      }

      if (stroke != null)
      {
         stroke.setCanvasGraphics(cg);
      }
   }

   @Override
   public BBox getStorageDistortionBounds()
   {
      return getStorageBBox();
   }

   public int getObjectFlag()
   {
      return super.getObjectFlag() | SELECT_FLAG_PATH
           | SELECT_FLAG_NON_TEXTUAL_SHAPE;
   }

   public void addPathChangeListener(JDRPathChangeListener listener)
   {
      if (pathChangeListeners == null)
      {
         pathChangeListeners = new Vector<JDRPathChangeListener>();
      }

      if (!pathChangeListeners.contains(listener))
      {
         pathChangeListeners.add(listener);
      }
   }

   public void removePathChangeListener(JDRPathChangeListener listener)
   {
      if (pathChangeListeners != null)
      {
         pathChangeListeners.remove(listener);
      }
   }

   protected void firePathChangeEvent(JDRPathChangeEvent.Type type)
   throws ClosingMoveException
   {
      firePathChangeEvent(-1, type);
   }

   protected void firePathChangeEvent(int index, JDRPathChangeEvent.Type type)
   throws ClosingMoveException
   {
      firePathChangeEvent(index, type, null, null);
   }

   protected void firePathChangeEvent(int index, JDRPathChangeEvent.Type type,
     JDRPathSegment oldSegment, JDRPathSegment newSegment)
   throws ClosingMoveException
   {
      if (pathChangeListeners != null)
      {
         JDRPathChangeEvent evt = new JDRPathChangeEvent(this, index, type,
           oldSegment, newSegment);

         for (int i = 0; i < pathChangeListeners.size(); i++)
         {
            pathChangeListeners.get(i).pathChanged(evt);

            if (evt.isConsumed())
            {
               break;
            }
         }
      }
   }

   private boolean closed;

   private int numClosedSubPaths = 0;

   protected int selectedSegmentIndex=-1;
   protected JDRPathSegment selectedSegment=null;
   protected JDRPoint selectedControl=null;
   protected int selectedControlIndex;

   private JDRSegment[] segmentList_;
   private int capacity_=10;
   private int size_ = 0;

   protected JDRPathIterator iterator;
   protected JDRPointIterator pointIterator;

   private static JDRPathListener pathListener = new JDRPathListener();

   /**
    * Initial capacity if {@link CanvasGraphics#getOptimize()} returns
    * {@link CanvasGraphics#OPTIMIZE_MEMORY} or {@link CanvasGraphics#OPTIMIZE_NONE}.
    */
   public static int init_capacity_memory=5;
   /**
    * Initial capacity if {@link CanvasGraphics#getOptimize()} returns
    * {@link CanvasGraphics#OPTIMIZE_SPEED}.
    */
   public static int init_capacity_speed=20;

   private JDRPaint fillPaint, linePaint;
   private JDRStroke stroke;

   private Vector<JDRPathChangeListener> pathChangeListeners;
}
