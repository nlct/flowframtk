// File          : JDRShape.java
// Creation Date : 18th August 2010
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

import com.dickimawbooks.jdr.marker.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a shape, such as a path, symmetric path or
 * text-path.
 * @author Nicola L C Talbot
 */

public abstract class JDRShape extends JDRCompleteObject
  implements JDRDistortable
{
   public JDRShape(CanvasGraphics cg)
   {
      super(cg);
   }

   public JDRShape(JDRShape shape)
   {
      super(shape);
   }

   /**
    * Gets the path iterator for this shape.
    * @return the path iterator for this shape
    */
   public abstract JDRPathIterator getIterator();

   public abstract JDRPointIterator getPointIterator();

   /**
    * Creates a new shape that is the reverse of this shape.
    * @return the reverse of this shape
    * @throws InvalidPathException if this shape can't be reversed
    */
   public abstract JDRShape reverse() throws InvalidShapeException;

   public abstract JDRPath getBaseUnderlyingPath();

   /**
    * Creates a new shape that is a reflection of this shape.
    * @return the reflection of this shape
    */
   public JDRShape reflection(JDRLine symmetryLine)
     throws InvalidPathException
   {
      JDRShape reflection = (JDRShape)clone();

      JDRPointIterator pi = getPointIterator();

      while (pi.hasNext())
      {
         JDRPoint point = pi.next();
         point.reflect2D(symmetryLine);
      }

      return reflection;
   }
   
   /**
    * Returns a shape created from applying an exclusive OR to
    * this shape and another shape.
    * @param shape the other shape
    * @return new shape that is this shape XOR the other shape
    */
   public abstract JDRShape exclusiveOr(JDRShape shape)
      throws InvalidShapeException;

   /**
    * Returns a shape created from applying union of
    * this shape and another shape.
    * @param shape the other shape
    * @return new shape that is this shape AND the shaper path
    */
   public abstract JDRShape pathUnion(JDRShape shape)
      throws InvalidShapeException;
   
   /**
    * Returns a new shape that is the intersection of this shape
    * and another shape.
    * @param shape the other shape
    * @return a new shape that is the intersection of this shape
    * and another shape
    */
   public abstract JDRShape intersect(JDRShape shape)
      throws InvalidShapeException;

   /**
    * Returns a new shape that is this shape less another shape.
    * @param shape the other shape
    * @return a new shape that is this shape less another shape
    */
   public abstract JDRShape subtract(JDRShape shape)
      throws InvalidShapeException;

   /**
    * Creates a new shape from the stroked outline of this shape.
    * @return the shape following this shape's stroked outline
    * @throws InvalidPathException if something is wrong with 
    * the shape outline
    */
   public JDRShape outlineToPath() throws InvalidShapeException
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

      JDRPath path = new JDRPath(cg, getCapacity());
      path.setLinePaint(new JDRTransparent(cg));
      path.setFillPaint((JDRPaint)getLinePaint().clone());
      boolean closeflag=false;
      JDRSegment postLastMoveSegment = null;

      path.description = description;

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

               if (postLastMoveSegment == null)
               {
                  postLastMoveSegment = segment;
               }
            break;
            case PathIterator.SEG_QUADTO :
               segment = JDRBezier.quadToCubic(cg, startpt.getX(),
                                            startpt.getY(), 
                                            coords[0],coords[1],
                                            coords[2],coords[3]);
               startpt = new Point2D.Double(coords[4], coords[4]);
               path.add(segment);

               if (postLastMoveSegment == null)
               {
                  postLastMoveSegment = segment;
               }
            break;
            case PathIterator.SEG_LINETO :
               segment = new JDRLine(cg, startpt.getX(),
                                  startpt.getY(),
                                  coords[0], coords[1]);
               startpt = new Point2D.Double(coords[0], coords[1]);
               path.add(segment);

               if (postLastMoveSegment == null)
               {
                  postLastMoveSegment = segment;
               }
            break;
            case PathIterator.SEG_MOVETO :

               if (closeflag && postLastMoveSegment != null)
               {
                  segment = new JDRClosingMove(
                                 startpt.getX(), startpt.getY(),
                                 coords[0], coords[1],
                                 path, path.size(), postLastMoveSegment);
               }
               else
               {
                  segment = new JDRSegment(cg, startpt.getX(),
                                     startpt.getY(),
                                     coords[0], coords[1]);
               }

               startpt = new Point2D.Double(coords[0], coords[1]);
               path.add(segment);
               postLastMoveSegment = null;
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
    * Breaks shape at the currently selected segment and returns the
    * left over part as a new shape.
    * @return left over shape
    * @throws InvalidPathException if the path can't be broken
    */
   public abstract JDRShape breakPath() throws InvalidShapeException;

   /**
    * Returns the size of this shape. Typically the number of
    * segments that define the path.
    * @return the size of this shape
    */
   public abstract int size();

   /**
    * Determines if this shape is empty.
    * @return true if this shape is empty
    */
   public boolean isEmpty()
   {
      return size() == 0;
   }

   /**
    * Returns the current capacity of this shape.
    * @return current capacity of this shape
    */
   public abstract int getCapacity();

   /**
    * Sets the current capacity of this shape.
    * @param capacity the new capacity
    * @throws IllegalArgumentException if the new capacity is
    * invalid
    */
   public abstract void setCapacity(int capacity)
      throws IllegalArgumentException;

   /**
    * Opens this shape.
    * @see #open(boolean)
    */
   public abstract void open() throws InvalidPathException;

   /**
    * Opens this shape, optionally removing the final segment.
    * @param removeLastSegment true if the last segment should be
    * removed
    * @see #open()
    */
   public abstract void open(boolean removeLastSegment)
    throws InvalidPathException;

   public void close() throws InvalidPathException
   {
      close(CLOSE_LINE);
   }

   /**
    * Closes this.
    * @param closeType may be one of {@link #CLOSE_LINE},
    * {@link #CLOSE_CONT} or {@link #CLOSE_MERGE_ENDS}
    * @throws EmptyPathException if this path is empty
    */
   public abstract void close(int closeType) throws InvalidPathException;

   /**
    * Closes this path with the given segment. The segment's start
    * and end points must fit the gap between the original opened 
    * path's end and start points.
    * @param segment the segment to use to close the path
    * @throws InvalidPathException if this path is empty
    * or if the segment doesn't fit
    */
   public abstract void close(JDRPathSegment segment)
      throws InvalidPathException;

   /**
    * Returns true if this shape is closed.
    * @return true if this shape is closed
    */
   public abstract boolean isClosed();

   public abstract boolean hasClosedSubPaths();

   /**
    * Determines whether the given segment has an end point that
    * doesn't coincide with the starting point of another segment.
    */
   public abstract boolean segmentHasEnd(JDRPathSegment segment);

   /**
    * Gets the index of the given segment or -1 if the segment
    * is not a part of this shape. The search starts from the start
    * of the shape.
    * @param segment the segment to find
    * @return the index of the segment or -1 if it is not a part
    * of this shape
    * @see #getLastIndex(JDRPathSegment)
    */
   public abstract int getIndex(JDRPathSegment segment);

   /**
    * Gets the index of the given segment or -1 if the segment
    * is not a part of this shape. The search starts from the end
    * of the shape.
    * @param segment the segment to find
    * @return the index of the segment or -1 if it is not a part
    * of this shape
    * @see #getIndex(JDRPathSegment)
    */
   public abstract int getLastIndex(JDRPathSegment segment);

   /**
    * Gets the segment at the specified index.
    * @param index the index
    * @throws ArrayIndexOutOfBoundsException if the index is out
    * of range (<code>index &lt; 0 || index &gt;= size()</code>)
    */
   public abstract JDRPathSegment get(int index)
      throws ArrayIndexOutOfBoundsException;

   /**
    * Gets the last segment in this shape.
    * @return the last segment in this shape or null if this shape
    * is empty
    */
   public abstract JDRPathSegment getLastSegment();

   /**
    * Gets the first segment in this shape.
    * @return the first segment in this shape or null if this shape
    * is empty
    */
   public abstract JDRPathSegment getFirstSegment();

   /**
    * Gets the first control point in the path.
    * @return the first control point or null if this shape is empty
    */
   public JDRPoint getFirstControl()
   {
      JDRPathSegment segment = getFirstSegment();

      if (segment == null)
      {
         return null;
      }

      return segment.getStart();
   }

   /**
    * Gets the last control point in the path.
    * @return the last control point in this shape or null if this
    * shape is empty
    */

   public JDRPoint getLastControl()
   {
      JDRPathSegment segment = getLastSegment();

      if (segment == null)
      {
         return null;
      }

      if (segmentHasEnd(segment))
      {
         return segment.getEnd();
      }

      try
      {
         return segment.getControl(segment.controlCount()-1);
      }
      catch (IndexOutOfBoundsException e)
      {
      }

      return segment.getStart();
   }

   /**
    * Gets the index of the given control point.
    * @param storagePt the point under investigation
    * @return the index of the first control point in this shape that
    * contains the given point, or -1 if none found
    */
   public int getControlIndex(JDRPoint storagePt)
   {
      JDRPointIterator pi = getPointIterator();

      for (int i = 0; pi.hasNext(); i++)
      {
         JDRPoint thisPoint = pi.next();

         if (thisPoint == storagePt) return i;
      }

      return -1;
   }

   /**
    * Switches off edit mode and deselects currently selected
    * control point and segment.
    */
   protected abstract void stopEditing();

   /**
    * Gets the index of the control point that contains the
    * given point.
    * @param storagePt the point under investigation
    * @return the index of the first control point in this shape that
    * contains the given point, or -1 if none found
    * @see #getControlIndex(JDRPoint)
    */
   public int getControlIndex(Point2D storagePt)
   {
      JDRPointIterator pi = getPointIterator();

      for (int i = 0; pi.hasNext(); i++)
      {
         JDRPoint thisPoint = pi.next();

         if (thisPoint.containsStoragePoint(storagePt)) return i;
      }

      return -1;
   }

   /**
    * Gets the index of the currently selected control point.
    * @return the index of the currently selected control point,
    * or -1 if none selected
    */
   public abstract int getSelectedControlIndex();

   public JDRPoint getControlFromStoragePoint(
      double storagePointX, double storagePointY, boolean endPoint)
   {
      JDRPathIterator pi = getIterator();

      while (pi.hasNext())
      {
         JDRPathSegment segment = pi.next();

         for (int i = 0, n = segment.controlCount(); i < n; i++)
         {
            JDRPoint point = segment.getControl(i);

            if (point.containsStoragePoint(point))
            {
               return point;
            }
         }

         if (endPoint)
         {
            JDRPoint point = segment.getEnd();

            if (point.containsStoragePoint(point))
            {
               return point;
            }
         }
      }

      return null;
   }

   /**
    * Gets the control point with the given index.
    * @param idx the index of the required control point
    * @return the control point with the given index or null if the
    * index is out of range
    */
   public JDRPoint getControl(int idx)
   {
      JDRPointIterator pi = getPointIterator();

      for (int i = 0; pi.hasNext(); i++)
      {
         JDRPoint point = pi.next();

         if (i == idx)
         {
            return point;
         }
      }

      return null;
   }

   /**
    * Selects the control point with the given index.
    * @param idx the index of the required control point
    * @return the control point with the given index or null if the
    * index is out of range
    */
   public JDRPoint selectControl(int idx)
   {
      JDRPointIterator pi = getPointIterator();

      for (int i = 0; pi.hasNext(); i++)
      {
         JDRPoint point = pi.next();

         if (i == idx)
         {
            selectControl(point, i, pi.getCurrentSegmentIndex());

            return point;
         }
      }

      return null;
   }

   /**
    * Selects the control point that contains the given point (in
    * storage units).
    * @param storagePt the point under investigation
    * @return the first control point in this path that contains
    * the given point or null if none found
    */
   public JDRPoint selectControl(Point2D storagePt)
   {
      return selectControl(storagePt.getX(), storagePt.getY());
   }

   public JDRPoint selectControl(double storagePtX, double storagePtY)
   {
      JDRPointIterator pi = getPointIterator();

      for (int i = 0; pi.hasNext(); i++)
      {
         JDRPoint thisPoint = pi.next();

         if (thisPoint.containsStoragePoint(storagePtX, storagePtY))
         {
            selectControl(thisPoint, i, pi.getCurrentSegmentIndex());

            return thisPoint;
         }
      }

      return null;
   }

   /**
    * Selects the control point that contains the given point.
    * @param storagePt the point under investigation
    * @return the first control point in this shape that contains
    * the given point or null if none found
    */
   public JDRPoint selectControl(JDRPoint storagePt)
   {
      JDRPointIterator pi = getPointIterator();

      for (int i = 0; pi.hasNext(); i++)
      {
         JDRPoint thisPoint = pi.next();

         if (thisPoint.equals(storagePt))
         {
            selectControl(thisPoint, i, pi.getCurrentSegmentIndex());

            return thisPoint;
         }
      }

      return null;
   }

   public JDRPoint selectPreviousControl()
   {
      int pointIndex=getSelectedControlIndex();
      int segmentIndex=getSelectedIndex();
      JDRPoint point = getSelectedControl();

      if (pointIndex == 0)
      {
         JDRPointIterator pi = getPointIterator();

         while (pi.hasNext())
         {
            point = pi.next();
         }

         pointIndex = pi.getCurrentPointIndex();
         segmentIndex = pi.getCurrentSegmentIndex();

         selectControl(point, pointIndex, segmentIndex);

         return point;
      }

      JDRPathSegment segment = get(segmentIndex);

      int index;

      if (segmentHasEnd(segment) && point == segment.getEnd())
      {
         index = segment.controlCount()-1;
      }
      else
      {
         index = segment.getControlIndex(point)-1;
      }

      if (index < 0)
      {
         segmentIndex--;
         segment = get(segmentIndex);
         index = segmentHasEnd(segment) ? segment.controlCount() 
                                        : segment.controlCount()-1;
      }

      point = (index == segment.controlCount()
            ? segment.getEnd()
            : segment.getControl(index));

      pointIndex--;

      selectControl(point, pointIndex, segmentIndex);

      return point;
   }

   /**
    * Selects the control following the currently selected control point
    * and sets that as the new selected control point.
    * @return the next edited control point
    */
   public JDRPoint selectNextControl()
   {
      JDRPointIterator pi = getPointIterator();

      int pointIndex=getSelectedControlIndex();
      int segmentIndex=getSelectedIndex();
      JDRPoint point = getSelectedControl();

      try
      {
         pi.set(pointIndex, segmentIndex, point);
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         pointIndex = 0;
         segmentIndex = 0;
         point = null;

         pi.reset();
      }
      catch (NoSuchElementException e)
      {
         pointIndex = 0;
         segmentIndex = 0;
         point = null;

         pi.reset();
      }

      try
      {
         point = pi.next();

         pointIndex = pi.getCurrentPointIndex();
         segmentIndex = pi.getCurrentSegmentIndex();
      }
      catch (NoSuchElementException e)
      {
         point = getFirstControl();

         pointIndex   = 0;
         segmentIndex = 0;
      }

      selectControl(point, pointIndex, segmentIndex);

      return point;
   }

   /**
    * Sets the new currently edited control point.
    * @param storagePt the new point to select
    * @param pointIndex the index of the control point
    * @param segmentIndex the index of the segment containing the
    * control point
    */
   protected abstract void selectControl(JDRPoint storagePt, int pointIndex,
      int segmentIndex);

   /**
    * Gets the currently selected segment.
    * @return currently selected segment or null if no segments
    * are selected
    */
   public abstract JDRPathSegment getSelectedSegment();

   /**
    * Gets the currently selected control.
    * @return currently selected control or null if no control points
    * are selected
    */
   public abstract JDRPoint getSelectedControl();

   /**
    * Gets the index of the currently selected segment.
    * @return index of currently selected segment or -1 if no 
    * segments are selected
    */
   public abstract int getSelectedIndex();

   public boolean hasMarkers()
   {
      JDRStroke stroke = getStroke();

      return (stroke instanceof JDRBasicStroke)
               && ((JDRBasicStroke)stroke).hasMarkers();
   }

   /**
    * Gets the bounding box in storage units.
    * This will be the bounding box of the stroked path unless the line
    * paint is transparent and there are no markers,
    * in which case it will be the bounding
    * box of the general path.
    * @return bounding box in storage units
    */
   public BBox getStorageBBox()
   {
      if (isEmpty()) return null;

      Rectangle2D bounds;

      if (getLinePaint() instanceof JDRTransparent && !hasMarkers())
      {
         bounds = getGeneralPath().getBounds2D();
      }
      else
      {
         bounds = getStorageStrokedPath().getBounds2D();
      }

      if (bounds.getWidth() == 0 && bounds.getHeight() == 0)
      {
         BBox box = null;

         for (int i = 0, n = size(); i < n; i++)
         {
            JDRPathSegment segment = get(i);

            if (box == null)
            {
               box = segment.getStorageBBox();
            }
            else
            {
               segment.mergeStorageControlBBox(box);
            }
         }

         return box;
      }

      double minX = bounds.getX();
      double minY = bounds.getY();
      double maxX = minX+bounds.getWidth();
      double maxY = minY+bounds.getHeight();

      return new BBox(getCanvasGraphics(), minX, minY, maxX, maxY);
   }

   public void mergeStorageBBox(BBox box)
   {
      if (isEmpty()) return;

      Rectangle2D bounds = getStorageStrokedPath().getBounds2D();

      if (bounds.getWidth() == 0 && bounds.getHeight() == 0)
      {
         for (int i = 0, n = size(); i < n; i++)
         {
            JDRPathSegment segment = get(i);

            segment.mergeStorageControlBBox(box);
         }

         return;
      }

      double minX = bounds.getX();
      double minY = bounds.getY();
      double maxX = minX+bounds.getWidth();
      double maxY = minY+bounds.getHeight();

      box.merge(minX, minY, maxX, maxY);
   }

   /**
    * Gets the bounding box encompassing all the control points
    * defining this shape.
    */
   public BBox getStorageControlBBox()
   {
      if (isEmpty()) return null;

      BBox bbox = null;

      for (int i = 0, n = size(); i < n; i++)
      {
         JDRPathSegment segment = get(i);

         if (bbox == null)
         {
            bbox = segment.getStorageControlBBox();
         }
         else
         {
            segment.mergeStorageControlBBox(bbox);
         }
      }

      return bbox;
   }

   public void mergeStorageControlBBox(BBox bbox)
   {
      for (int i = 0, n = size(); i < n; i++)
      {
         JDRPathSegment segment = get(i);

         segment.mergeStorageControlBBox(bbox);
      }
   }

   /**
    * Gets the stroked path. (Markers are included in the
    * shape.)
    * @return the shape defining this path when it is stroked (in
    * storage units)
    * @see JDRStroke#getStorageStrokedPath(JDRShape)
    * @see #getStorageStrokedArea()
    */
   public Shape getStorageStrokedPath()
   {
      return getStroke().getStorageStrokedPath(this);
   }

   public Shape getBpStrokedPath()
   {
      return getStroke().getBpStrokedPath(this);
   }

   /**
    * Gets the stroked path applying storage to component
    * transformation. (Markers are included in the
    * shape.)
    * @return the shape defining this path when it is stroked
    * @see JDRStroke#getStorageStrokedPath(JDRShape)
    * @see #getStorageStrokedArea()
    */
   public Shape getComponentStrokedPath()
   {
      return getStroke().getComponentStrokedPath(this);
   }

   /**
    * Gets the stroked area. (Markers are included in the
    * area.)
    * @return the area defining this path when it is stroked
    * @see JDRStroke#getStorageStrokedArea(JDRShape)
    * @see #getStorageStrokedPath()
    */
   public Area getStorageStrokedArea()
   {
      return getStroke().getStorageStrokedArea(this);
   }

   /**
    * Gets the stroked area applying storage to component
    * transformation. (Markers are included in the
    * area.)
    * @return the area defining this path when it is stroked
    * @see JDRStroke#getStorageStrokedArea(JDRShape)
    * @see #getStorageStrokedPath()
    */
   public Area getBpStrokedArea()
   {
      return getStroke().getBpStrokedArea(this);
   }

   /**
    * Gets the stroked area applying storage to component
    * transformation. (Markers are included in the
    * area.)
    * @return the area defining this path when it is stroked
    * @see JDRStroke#getStorageStrokedArea(JDRShape)
    * @see #getStorageStrokedPath()
    */
   public Area getComponentStrokedArea()
   {
      return getStroke().getComponentStrokedArea(this);
   }

   /**
    * Gets this shape as a Path2D. This has changed from GeneralPath
    * to Path2D to allow for double-precision.
    * @return this shape as a Path2D (in storage units)
    */
   public abstract Path2D getGeneralPath();

   /**
    * Gets this shape as a Path2D applying storage to component
    * transformation.
    * @return this shape as a Path2D
    */
   public Path2D getComponentGeneralPath()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Path2D path = getGeneralPath();

      double scaleX = cg.storageToComponentX(1.0);
      double scaleY = cg.storageToComponentY(1.0);

      AffineTransform af = new AffineTransform();
      af.scale(scaleX, scaleY);

      return new Path2D.Double(path, af);
   }

   /**
    * Gets this shape as a Path2D applying storage to bp
    * transformation.
    * @return this shape as a Path2D
    */
   public Path2D getBpGeneralPath()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Path2D path = getGeneralPath();

      JDRUnit unit = cg.getStorageUnit();

      if (unit.getID() == JDRUnit.BP)
      {
         return path;
      }

      double factor = unit.toBp(1.0);

      AffineTransform af = new AffineTransform();
      af.scale(factor, factor);

      return new Path2D.Double(path, af);
   }

   /**
    * Sets the segment at the given index.
    * @param segment the replacement segment
    * @param index the index
    * @return the old segment at the given index
    */
   public abstract JDRPathSegment setSegment(int index, JDRPathSegment segment)
     throws ArrayIndexOutOfBoundsException,InvalidPathException;

   /**
    * Appends the given segment, enlarging the
    * capacity if necessary.
    * @param s the segment to append to this shape
    */
   public abstract void add(JDRSegment s)
   throws InvalidPathException;

   /**
    * Adds a new point midway along the currently edited segment.
    * @return the newly added point or null if this shape is not
    * being edited
    */
   public abstract JDRPoint addPoint();

   /**
    * Makes the currently edited segment continuous along its start
    * or end point. Does nothing if the path is not being edited
    * or if the edited segment is not an instance of {@link JDRBezier}.
    * @param atStart if segment should be made continuous at the
    * start
    * @param equiDistant if new gradient should have the same length
    * as the gradient on the other side of the join
    */
   public abstract void makeContinuous(boolean atStart, boolean equiDistant);

   /**
    * Replaces the segment at the given index with a new segment.
    * Control points are adjusted as appropriate.
    * @param idx the index at which to substitute the new segment
    * @param newSegment the new segment
    */
   public abstract void convertSegment(int idx, JDRPathSegment newSegment)
   throws InvalidPathException;

   /**
    * Removes the segment at the given index and adjusts surrounding
    * control points as appropriate.
    * @param i the index of the segment to remove
    * @return the removed segment
    */
   public abstract JDRPathSegment remove(int i)
     throws ArrayIndexOutOfBoundsException,InvalidPathException;

   /**
    * Removes the given segment from the segment list.
    */
   public abstract JDRSegment removeSegment(int index)
      throws ArrayIndexOutOfBoundsException,InvalidPathException;

   /**
    * Removes the given segment and adjusts surrounding
    * control points as appropriate.
    * @param segment the segment to remove
    * @return the removed segment or null if not found
    */
   public abstract JDRPathSegment remove(JDRPathSegment segment)
    throws InvalidPathException;

   /**
    * Removes the currently selected segment.
    * @return the removed segment
    */
   public abstract JDRPathSegment removeSelectedSegment()
    throws InvalidPathException;

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
   public abstract void translateControl(
      JDRPathSegment segment, JDRPoint p, 
      double x, double y);

   @Override
   public void transform(double[] matrix)
   {
      JDRPointIterator pi = getPointIterator();

      while (pi.hasNext())
      {
         JDRPoint point = pi.next();

         point.transform(matrix);
      }
   }

   @Override
   public void transform(AffineTransform af)
   {
      JDRPointIterator pi = getPointIterator();

      while (pi.hasNext())
      {
         JDRPoint point = pi.next();

         point.transform(af);
      }
   }

   @Override
   public void translate(double x, double y)
   {
      JDRPointIterator pi = getPointIterator();

      while (pi.hasNext())
      {
         JDRPoint point = pi.next();

         point.translate(x, y);
      }
   }

   public void scale(Point2D p, double factorX, double factorY)
   {
      JDRPointIterator pi = getPointIterator();

      while (pi.hasNext())
      {
         JDRPoint point = pi.next();

         point.scale(p, factorX, factorY);
      }
   }

   public void shear(Point2D p, double factorX, double factorY)
   {
      JDRPointIterator pi = getPointIterator();

      while (pi.hasNext())
      {
         JDRPoint point = pi.next();

         point.shear(p, factorX, factorY);
      }
   }

   public void rotate(Point2D p, double angle)
   {
      JDRPointIterator pi = getPointIterator();

      while (pi.hasNext())
      {
         JDRPoint point = pi.next();

         point.rotate(p, angle);
      }
   }

   public void drawControls(boolean endPoint)
   {
      JDRPointIterator pi = getPointIterator();

      while (pi.hasNext())
      {
         JDRPoint point = pi.next();

         point.draw();
      }
   }

   public void draw(FlowFrame parentFrame)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (parentFrame == null)
      {
         parentFrame = flowframe;
      }

      boolean doShift = (parentFrame != null && cg.isEvenPage());

      if (doShift)
      {
         doShift = (parentFrame.getEvenXShift() != 0.0
                 || parentFrame.getEvenYShift() != 0.0);
      }

      BBox box = null;

      JDRPaint linePaint = getLinePaint();
      JDRPaint fillPaint = getFillPaint();

      if (fillPaint instanceof JDRShading
        ||linePaint instanceof JDRShading)
      {
         box = getStorageBBox();

         if (doShift)
         {
            box.translate(parentFrame.getEvenXShift(), 
                          parentFrame.getEvenYShift());
         }
      }

      Shape path = getGeneralPath();

      if (doShift)
      {
         AffineTransform shiftAf = AffineTransform.getTranslateInstance(
           parentFrame.getEvenXShift(), parentFrame.getEvenYShift());
         path = shiftAf.createTransformedShape(path);
      }

      if (!(fillPaint instanceof JDRTransparent) && fillPaint != null)
      {
         cg.setPaint(fillPaint.getPaint(box));
         cg.fill(path);
      }

      if (linePaint instanceof JDRTransparent)
      {
         // Just draw any markers, if they have a paint independent
         // of the shape

         if (getStroke() instanceof JDRBasicStroke)
         {
            ((JDRBasicStroke)getStroke()).drawMarkers(this);
         }
      }
      else
      {
         cg.setPaint(linePaint.getPaint(box));

         getStroke().drawStoragePath(this, path);
      }
   }

   public void print(Graphics2D g2)
   {
      BBox box = null;

      JDRPaint linePaint = getLinePaint();
      JDRPaint fillPaint = getFillPaint();

      if (fillPaint instanceof JDRShading
        ||linePaint instanceof JDRShading)
      {
         box = getBpBBox();
      }

      Path2D path = getBpGeneralPath();

      if (!(fillPaint instanceof JDRTransparent) && fillPaint != null)
      {
         g2.setPaint(fillPaint.getPaint(box));
         g2.fill(path);
      }

      if (linePaint instanceof JDRTransparent)
      {
         // Just draw any markers, if they have a paint independent
         // of the shape

         if (getStroke() instanceof JDRBasicStroke)
         {
            ((JDRBasicStroke)getStroke()).printMarkers(g2, this);
         }
      }
      else
      {
         g2.setPaint(linePaint.getPaint(box));

         getStroke().printPath(g2, this, path);
      }
   }

   public void saveEPS(PrintWriter out)
     throws IOException
   {
      BBox box = null;

      JDRPaint linePaint = getLinePaint();
      JDRPaint fillPaint = getFillPaint();

      if (fillPaint instanceof JDRShading
        ||linePaint instanceof JDRShading)
      {
         box = getBpBBox();
      }

      Path2D path = getBpGeneralPath();

      if (!(fillPaint instanceof JDRTransparent) && fillPaint != null)
      {
         EPS.fillPath(path, fillPaint, out);
      }

      if (linePaint instanceof JDRTransparent)
      {
         // Just draw any markers, if they have a paint independent
         // of the shape

         if (getStroke() instanceof JDRBasicStroke)
         {
            if (box == null)
            {
               box = getBpBBox();
            }

            ((JDRBasicStroke)getStroke()).epsMarkers(this, box, out);
         }
      }
      else
      {
         getStroke().saveEPS(this, out);
      }
   }

   /**
    * Draws path in draft mode.
    * @param cg graphics information
    */
   public void drawDraft(FlowFrame parentFrame)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (parentFrame == null)
      {
         parentFrame = flowframe;
      }

      boolean doShift = (parentFrame != null && cg.isEvenPage());
      double xshift = 0.0;

      if (doShift)
      {
         xshift = parentFrame.getEvenXShift();

         FlowFrame typeblock = getTypeblock();

         if (typeblock != null)
         {
            xshift += typeblock.getEvenXShift();
         }

         doShift = (xshift != 0.0
                 || parentFrame.getEvenYShift() != 0.0);
      }

      Path2D p = getComponentGeneralPath();

      Graphics2D g2 = cg.getGraphics();

      AffineTransform af = g2.getTransform();

      if (doShift)
      {
         g2.translate(cg.storageToComponentX(xshift), 
            cg.storageToComponentY(parentFrame.getEvenYShift()));
      }

      cg.setPaint(draftColor);

      cg.draw(p);

      JDRPathSegment segment = getSelectedSegment();

      if (segment != null)
      {
         segment.drawSelectedNoControls();
      }

      for (int i = 0, n = size(); i < n; i++)
      {
         segment = get(i);
         segment.drawControls(segmentHasEnd(segment));
      }

      if (doShift)
      {
         g2.setTransform(af);
      }
   }

   public void fill()
   {
      if (isEmpty()) return;

      CanvasGraphics cg = getCanvasGraphics();

      Path2D path = getGeneralPath();
      cg.fill(path);
   }

   public void savePgfPath(TeX tex)
    throws IOException
   {
      JDRPathSegment segment = get(0);

      tex.print("\\pgfpathmoveto{");
      segment.getStart().savePgf(tex);
      tex.println("}");

      JDRPathIterator pi = getIterator();

      while (pi.hasNext())
      {
         segment = pi.next();

         segment.savePgf(tex);
      }

      if (isClosed())
      {
         tex.println("\\pgfclosepath");
      }
   }

   /**
    * Gets this shape as a complete path. (In storage units.)
    */
   public abstract JDRShape getFullPath()
      throws InvalidShapeException;

   /**
    * Determines whether the path that defines this shape should be
    * drawn in non-draft mode.
    * @return true if the path defining this shape should be drawn
    * in non-draft mode.
    */
   public abstract boolean showPath();

   public abstract void setLinePaint(JDRPaint paint);
   public abstract JDRPaint getLinePaint();
   public abstract void setFillPaint(JDRPaint paint);
   public abstract JDRPaint getFillPaint();
   public abstract void setStroke(JDRStroke stroke);
   public abstract JDRStroke getStroke();

   /** Makes this shape have the same attributes as other.
    * Sets the line and fill paint, the stroke and common attributes
    * such as description, flowframe data and edit mode. This checks
    * for null in the event that it's used while a shape is under
    * construction and the attributes haven't yet been set.
    */
   public void setAttributes(JDRShape other)
   {
      JDRPaint paint = other.getLinePaint();
      setLinePaint(paint == null ? null : (JDRPaint)paint.clone());
      paint = other.getFillPaint();
      setFillPaint(paint == null ? null : (JDRPaint)paint.clone());
      JDRStroke stroke = other.getStroke();
      setStroke(stroke == null ? null : (JDRStroke)stroke.clone());

      super.makeEqual(other);
   }

   /**
    * Gets the path style listener for this shape.
    */
   public JDRPathStyleListener getPathStyleListener()
   {
      return getStroke().getPathStyleListener();
   }

   protected abstract void setSelectedElements(int segmentIndex, int controlIndex,
      JDRPathSegment segment, JDRPoint control);

   /**
    * Prints the segments of a Shape (provided for debugging
    * purposes only).
    */

   public static void printPath(Shape path)
   {
      printPath(path.getPathIterator(null));
   }

   public static void printPath(PathIterator pi)
   {
      double[] coords = new double[6];

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_CLOSE:
              System.out.println("Close sub-path");
            break;
            case PathIterator.SEG_CUBICTO:
              System.out.println("Cubic to: "
              + "("+coords[0]+","+coords[1]+") "
              + "("+coords[2]+","+coords[3]+") "
              + "("+coords[4]+","+coords[5]+")");
            break;
            case PathIterator.SEG_LINETO:
              System.out.println("Line to: "
              + "("+coords[0]+","+coords[1]+")");
            break;
            case PathIterator.SEG_MOVETO:
              System.out.println("Move to: "
              + "("+coords[0]+","+coords[1]+")");
            break;
            case PathIterator.SEG_QUADTO:
              System.out.println("Quad to: "
              + "("+coords[0]+","+coords[1]+") "
              + "("+coords[2]+","+coords[3]+")");
            break;
         }

         pi.next();
      }

      int rule = pi.getWindingRule();
      System.out.println("Winding rule: "+(rule==PathIterator.WIND_EVEN_ODD? "Even-Odd" : "Non-Zero"));
   }

   public int svg(StringBuilder builder)
   {
      return svg(builder, getGeneralPath());
   }

   public static int svg(StringBuilder builder, Shape shape)
   {
      return svg(builder, shape.getPathIterator(null), String.format("%n"));
   }

   public static int svg(StringBuilder builder, Shape shape, String sep)
   {
      return svg(builder, shape.getPathIterator(null), sep);
   }

   public static int svg(StringBuilder builder, PathIterator pi, String sep)
   {
      int n = 0;

      double[] coords = new double[6];

      while (!pi.isDone())
      {
         n++;
         int type = pi.currentSegment(coords);

         if (builder.length() > 0)
         {
            builder.append(sep);
         }

         switch (type)
         {
            case PathIterator.SEG_MOVETO:
              builder.append(String.format("M %f %f", coords[0], coords[1]));
            break;
            case PathIterator.SEG_LINETO:
              builder.append(String.format("L %f %f", coords[0], coords[1]));
            break;
            case PathIterator.SEG_QUADTO:
              builder.append(String.format("Q %f %f %f %f",
                coords[0], coords[1], coords[2], coords[3]));
            break;
            case PathIterator.SEG_CUBICTO:
              builder.append(String.format("C %f %f %f %f %f %f",
                 coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]));
            break;
            case PathIterator.SEG_CLOSE:
              builder.append("Z");
            break;
         }

         pi.next();
      }

      return n;
   }

   @Override
   public void writeSVGdefs(SVG svg) throws IOException
   {
      getStroke().writeSVGdefs(svg, this);
   }

   public double computeArea()
   {
      return computeArea(getGeneralPath());
   }

   public double computeArea(double flatness)
   {
      return computeArea(getGeneralPath(), flatness);
   }

   public static double computeArea(Shape shape)
   {
      return computeArea(shape, 0.0);
   }

   // estimated - only for simple paths (won't work for shapes with
   // holes)
   public static double computeArea(Shape shape, double flatness)
   {
      Area polyArea;

      if (shape instanceof Area && ((Area)shape).isPolygonal())
      {
         polyArea = (Area)shape;
      }
      else
      {
         Path2D polygon = new Path2D.Double();
         polygon.append(shape.getPathIterator(null, flatness), false);
         polyArea = new Area(polygon);
      }

      PathIterator pi = polyArea.getPathIterator(null);

      double area = 0.0;
      double sum = 0.0;
      double prevX = 0.0;
      double prevY = 0.0;
      double startX = 0.0;
      double startY = 0.0;

      double[] coords = new double[6];

      for (; !pi.isDone(); pi.next())
      {
         int type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_CLOSE:
               sum += prevX * startY - prevY * startX;
               area += Math.abs(sum/2.0);
               sum = 0.0;
            break;
            case PathIterator.SEG_LINETO:
               sum += prevX * coords[1] - prevY * coords[0];
               prevX = coords[0];
               prevY = coords[1];
            break;
            case PathIterator.SEG_QUADTO:
               // estimate (shouldn't happen)
               sum += prevX * coords[3] - prevY * coords[2];
               prevX = coords[2];
               prevY = coords[3];
            break;
            case PathIterator.SEG_CUBICTO:
               // estimate (shouldn't happen)
               sum += prevX * coords[5] - prevY * coords[4];
               prevX = coords[4];
               prevY = coords[5];
            break;
            case PathIterator.SEG_MOVETO:
               prevX = coords[0];
               prevY = coords[1];
               startX = prevX;
               startY = prevY;
            break;
         }
      }

      return Math.abs(area);
   }

   public static double computeArea(Point2D[] pts)
   {
      return computeArea(pts, 0, pts.length-1);
   }

   public static double computeArea(Point2D[] pts, int startOffset, int endOffset)
   {
      double sum = 0.0;
      Point2D prev = pts[startOffset];

      if (startOffset > endOffset)
      {
         for (int i = startOffset; i < pts.length; i++)
         {
            sum += prev.getX() * pts[i].getY() - prev.getY() * pts[i].getX();

            prev = pts[i];
         }

         for (int i = 0; i <= endOffset; i++)
         {
            sum += prev.getX() * pts[i].getY() - prev.getY() * pts[i].getX();

            prev = pts[i];
         }
      }
      else
      {
         for (int i = startOffset; i <= endOffset; i++)
         {
            sum += prev.getX() * pts[i].getY() - prev.getY() * pts[i].getX();

            prev = pts[i];
         }
      }

      sum += prev.getX() * pts[startOffset].getY()
           - prev.getY() * pts[startOffset].getX();

      return Math.abs(sum)/2;
   }

   public static double computeArea(Vector<Point2D> pts)
   {
      return computeArea(pts, 0, pts.size()-1);
   }

   public static double computeArea(Vector<Point2D> pts, int startOffset, int endOffset)
   {
      double sum = 0.0;
      Point2D p0 = pts.get(startOffset);
      Point2D prev = p0;

      if (startOffset > endOffset)
      {
         int n = pts.size();

         for (int i = startOffset; i < n; i++)
         {
            Point2D p = pts.get(i);

            sum += prev.getX() * p.getY() - prev.getY() * p.getX();

            prev = p;
         }

         for (int i = 0; i <= endOffset; i++)
         {
            Point2D p = pts.get(i);

            sum += prev.getX() * p.getY() - prev.getY() * p.getX();

            prev = p;
         }
      }
      else
      {
         for (int i = startOffset; i <= endOffset; i++)
         {
            Point2D p = pts.get(i);

            sum += prev.getX() * p.getY() - prev.getY() * p.getX();

            prev = p;
         }
      }

      sum += prev.getX() * p0.getY() - prev.getY() * p0.getX();

      return Math.abs(sum)/2;
   }

   public double computePerimeter()
   {
      return computePerimeter(getGeneralPath());
   }

   public static double computePerimeter(Shape shape)
   {
      return computePerimeter(shape, 0.0);
   }

   public static double computePerimeter(Shape shape, double flatness)
   {
      return computePerimeter(shape.getPathIterator(null, flatness));
   }

   public static double computePerimeter(PathIterator pi)
   {
      double length = 0.0;
      double prevX = 0.0;
      double prevY = 0.0;
      double startX = 0.0;
      double startY = 0.0;

      double[] coords = new double[6];

      for (; !pi.isDone(); pi.next())
      {
         int type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_CLOSE:
               length += Point2D.distance(prevX, prevY, startX, startY);
            break;
            case PathIterator.SEG_MOVETO:
               startX = coords[0];
               startY = coords[1];
               prevX = startX;
               prevY = startY;
            break;
            case PathIterator.SEG_LINETO:
               length += Point2D.distance(prevX, prevY, coords[0], coords[1]);
               prevX = coords[0];
               prevY = coords[1];
            break;
            case PathIterator.SEG_QUADTO:
               length += JDRBezier.getEstimatedLength(prevX, prevY,
                 (2*coords[0]+prevX)/3, (2*coords[1]+prevY)/3, 
                 (coords[2]+2*coords[0])/3, (coords[3]+2*coords[1])/3, 
                 coords[2], coords[3]);
               prevX = coords[2];
               prevY = coords[3];
            break;
            case PathIterator.SEG_CUBICTO:
               length += JDRBezier.getEstimatedLength(prevX, prevY,
                 coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
               prevX = coords[4];
               prevY = coords[5];
            break;
         }
      }

      return length;
   }

   public Object[] getDescriptionInfo()
   {
      return new Object[] {size()};
   }

   public boolean hasShape()
   {
      return true;
   }

   public boolean isDistortable()
   {
      return true;
   }

   /**
    * Returns true if this path is a polygon. That is, it only
    * consists of lines.
    * @return true if this path is a polygon
    */
   public abstract boolean isPolygon();

   public abstract JDRShape toPolygon(double flatness)
    throws InvalidShapeException;

   public abstract BBox getStorageDistortionBounds();

   public BBox getBpDistortionBounds()
   {
      CanvasGraphics cg = getCanvasGraphics();

      BBox box = getStorageDistortionBounds();

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

   public BBox getComponentDistortionBounds()
   {
      CanvasGraphics cg = getCanvasGraphics();

      BBox box = getStorageDistortionBounds();

      double scaleX = cg.storageToComponentX(1.0);
      double scaleY = cg.storageToComponentY(1.0);

      box.reset(box.getMinX()*scaleX,
                box.getMinY()*scaleY,
                box.getMaxX()*scaleX,
                box.getMaxY()*scaleY);

      return box;

   }

   public void distort(JDRDistortable original,
      Shape[] area,
      AffineTransform[] trans)
   {
      JDRPointIterator orgPi = ((JDRShape)original).getPointIterator();
      JDRPointIterator pi = getPointIterator();

      Point2D src = new Point2D.Double();
      Point2D dst = new Point2D.Double();

      while (pi.hasNext())
      {
         JDRPoint orgPoint = orgPi.next();
         JDRPoint point = pi.next();

         point.set(orgPoint.getX(), orgPoint.getY());

         src.setLocation(point.getX(), point.getY());
         dst.setLocation(point.getX(), point.getY());

         for (int i = 0; i < area.length; i++)
         {
            if (area[i].contains(src))
            {
               trans[i].transform(src, dst);
               break;
            }
         }

         point.x = dst.getX();
         point.y = dst.getY();
      }
   }

   public abstract Object clone();

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      super.applyCanvasGraphics(cg);

      JDRStroke stroke = getStroke();

      if (stroke != null)
      {
         stroke.applyCanvasGraphics(cg);
      }

      JDRPaint linePaint = getLinePaint();

      if (linePaint != null)
      {
         linePaint.applyCanvasGraphics(cg);
      }

      JDRPaint fillPaint = getFillPaint();

      if (fillPaint != null)
      {
         fillPaint.applyCanvasGraphics(cg);
      }

      setCanvasGraphics(cg);
   }

   public int getObjectFlag()
   {
      int flag = super.getObjectFlag()
        | SELECT_FLAG_SHAPE
        | SELECT_FLAG_DISTORTABLE;

      if (isClosed())
      {
         flag = (flag | SELECT_FLAG_CLOSED);
      }
      else
      {
         flag = (flag | SELECT_FLAG_OPEN);
      }

      if (hasClosedSubPaths())
      {
         flag = (flag | SELECT_FLAG_CLOSED_SUB_PATHS);
      }
      else
      {
         flag = (flag | SELECT_FLAG_NO_CLOSED_SUB_PATHS);
      }

      return flag;
   }

   public int getSelectedSegmentFlag()
   {
      JDRPathSegment seg = getSelectedSegment();

      if (seg == null) return SEGMENT_FLAG_NONE;

      int flag = seg.getSegmentFlag();

      boolean isFirst = (seg == getFirstSegment());
      boolean isLast = (seg == getLastSegment());

      if (isFirst)
      {
         flag = (flag | SEGMENT_FLAG_FIRST);
      }

      if (isLast)
      {
         flag = (flag | SEGMENT_FLAG_LAST);
      }

      if (!isFirst && !isLast)
      {
         flag = (flag | SEGMENT_FLAG_MID);
      }

      return flag;
   }

   public int getSelectedControlFlag()
   {
      JDRPoint pt = getSelectedControl();

      if (pt == null)
      {
         return CONTROL_FLAG_NONE;
      }

      int flag = pt.getControlFlag();

      JDRPathSegment seg = getSelectedSegment();

      if (seg == null)
      {
         return flag;
      }

      boolean isStart = (seg.getStart() == pt);
      boolean isEnd = (!(seg instanceof JDRPartialSegment)
                      && seg.getEnd() == pt);

      if (isStart)
      {
         flag = (flag | CONTROL_FLAG_START);

         if ((seg instanceof JDRBezier || seg instanceof JDRPartialBezier)
         && (isClosed() || seg != getFirstSegment()))
         {
            int idx = getSelectedIndex();

            if ((idx > 0 && (get(idx-1) instanceof JDRBezier))
             || (isClosed() && (getLastSegment() instanceof JDRBezier)))
            {
               flag = (flag | CONTROL_FLAG_CAN_ANCHOR);
            }
         }
      }
      else if (isEnd)
      {
         flag = (flag | CONTROL_FLAG_END);
      }
      else if (seg instanceof JDRBezier)
      {
         flag = (flag | CONTROL_FLAG_CURVATURE);
         boolean isOpen = !isClosed();

         if (isOpen && seg == getFirstSegment())
         {
            if (pt != seg.getControl(1))
            {
               flag = (flag
                     | CONTROL_FLAG_CAN_MAKE_JOIN_CONTINUOUS);
            }
         }
         else if (isOpen && seg == getLastSegment())
         {
            if (pt != seg.getControl(2))
            {
               flag = (flag
                    | CONTROL_FLAG_CAN_MAKE_JOIN_CONTINUOUS);
            }
         }
         else
         {
            flag = (flag
                 | CONTROL_FLAG_CAN_MAKE_JOIN_CONTINUOUS);
         }
      }
      else if (seg instanceof JDRPartialBezier)
      {
         flag = (flag | CONTROL_FLAG_CURVATURE);

         if (isClosed() || seg != getLastSegment())
         {
            flag = (flag
                 | CONTROL_FLAG_CAN_MAKE_JOIN_CONTINUOUS);
         }
      }

      return flag;
   }


   public static final int CLOSE_LINE = 0;
   public static final int CLOSE_CONT = 1;
   public static final int CLOSE_MERGE_ENDS = 2;
}
