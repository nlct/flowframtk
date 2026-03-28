// File          : JDRSymmetricPath.java
// Creation Date : 25th July 2010
// Author        : Nicola L. C. Talbot
//               http://www.dickimaw-books.com/

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
 *  Class representing a symmetric path.
 *  @author Nicola L C Talbot
 */

public class JDRSymmetricPath extends JDRCompoundShape
{
   /**
    * Creates a symmetric path from the given path. This creates a
    * symmetric path from the given path. If endAnchor is true, the
    * final point in the path is anchored to the line of symmetry,
    * so the complete path will have an even number of segments. If
    * endAnchor is false, the final point isn't anchored to the line
    * of symmetry, so the complete path will have an odd number of
    * segments.
    * @param path the path
    * @param endAnchor if true anchor end point to the line of symmetry
    * @param line line of symmetry
    */
    public JDRSymmetricPath(JDRShape path, boolean endAnchor, JDRLine line)
    throws InvalidPathException
    {
       super(path.getCanvasGraphics());

       initIterators();

       path_ = path;

       selected = path_.isSelected();

       setSymmetry(line);

       join = (endAnchor ? null 
            : new JDRPartialSegment(path_.getLastControl(), line_));

       if (path_.isClosed())
       {
          path_.open(false);
          close(CLOSE_MERGE_ENDS);
       }

       super.setEditMode(path_.isEdited());
    }

    protected JDRSymmetricPath(JDRShape path, JDRPartialSegment joinSegment, JDRLine line)
    throws InvalidPathException
    {
       super(path.getCanvasGraphics());

       initIterators();

       path_ = path;

       selected = path_.isSelected();

       line_ = line;

       join = joinSegment;

       if (join != null)
       {
          join.setSymmetryLine(line_);
          join.setStart(path_.getLastControl());
       }

       if (path_.isClosed())
       {
          path_.open(false);
          close(CLOSE_MERGE_ENDS);
       }

       super.setEditMode(path_.isEdited());
    }

    private JDRSymmetricPath(JDRShape path, JDRPartialSegment joinSegment, 
      JDRLine line, JDRPartialSegment closingSeg, 
      boolean isClosed, boolean isSingle)
    {
       super(path.getCanvasGraphics());

       initIterators();

       path_ = path;

       selected = path_.isSelected();

       line_ = line;

       join = joinSegment;

       if (join != null)
       {
          join.setSymmetryLine(line_);
          join.setStart(path_.getLastSegment().getEnd());
       }

       closingSegment = closingSeg;

       if (closingSegment != null)
       {
          closingSegment.setSymmetryLine(line_);
       }

       closed = isClosed;

       this.isSingle = isSingle;

       super.setEditMode(path_.isEdited());
    }

    /**
     * Creates an empty path with the given line of symmetry and
     * path attributes.
     * @param line the line of symmetry
     * @param capacity initial path capacity
     * @param linePaint the path outline paint
     * @param fillPaint the fill paint
     * @param stroke the path stroke
     */
    public JDRSymmetricPath(JDRLine line,
       int capacity, JDRPaint linePaint, JDRPaint fillPaint,
       JDRStroke stroke)
    {
       super(line.getCanvasGraphics());

       initIterators();

       path_ = new JDRPath(capacity, linePaint, fillPaint, stroke);

       setSymmetry(line);

       join = null;
    }

    /**
     * Creates an empty path with a line of symmetry passing through
     * (0,1) and (0,-1).
     */
    public JDRSymmetricPath(CanvasGraphics cg)
    {
       super(cg);

       initIterators();

       path_ = new JDRPath(cg);
       join  = null;
       line_ = new JDRLine(new JDRSymmetryLinePoint(cg, 0.0, 1.0),
                           new JDRSymmetryLinePoint(cg, 0.0, -1.0));
    }

    public JDRSymmetricPath(int capacity, JDRPaint lineColor,
                            JDRPaint fillColor, JDRStroke s)
    {
       super(lineColor.getCanvasGraphics());

       initIterators();

       path_ = new JDRPath(capacity, lineColor, fillColor, s);
       join = null;
    }

    /**
     * Create a copy. 
     */ 
    public JDRSymmetricPath(JDRSymmetricPath symPath)
    {
       super(symPath);
       initIterators();

       isSingle = symPath.isSingle;
       closed = symPath.closed;
       path_ = (JDRShape)symPath.path_.clone();
       selected = symPath.selected;
       super.setEditMode(symPath.isEdited());

       line_ = new JDRLine(symPath.line_);

       if (symPath.join != null)
       {
          join = (JDRPartialSegment)symPath.join.clone();
          join.setStart(path_.getLastSegment().getEnd());
          join.setSymmetryLine(line_);
       }

       if (symPath.closingSegment != null)
       {
          closingSegment = (JDRPartialSegment)symPath.closingSegment.clone();
          closingSegment.setEnd(path_.getFirstControl());
          closingSegment.setSymmetryLine(line_);
       }

       int pointIndex = symPath.getSelectedControlIndex();

       if (pointIndex > -1)
       {
          selectControl(pointIndex);
       }
    }

    public static JDRSymmetricPath createFrom(JDRShape path)
    throws InvalidPathException
    {
       CanvasGraphics cg = path.getCanvasGraphics();

       JDRSymmetricPath symPath = new JDRSymmetricPath(cg);

       symPath.path_ = path;

       symPath.isSingle = !(path instanceof JDRTextPath);

       symPath.selected = path.isSelected();

       symPath.join = null;

       // Get the last point

       JDRPoint p = symPath.path_.getLastControl();

       // Construct vertical line through this point

       BBox box = path.getStorageBBox();

       symPath.line_ = new JDRLine(
          new JDRSymmetryLinePoint(cg, p.x, box.getMinY()),
          new JDRSymmetryLinePoint(cg, p.x, box.getMaxY()));

       if (symPath.path_.isClosed())
       {
          symPath.path_.open(false);

          try
          {
             symPath.close(CLOSE_MERGE_ENDS);
          }
          catch (EmptyPathException e)
          {
              throw new IllegalArgumentException(
                 "Can't make a symmetric shape out from an empty path", e);
          }
       }

       symPath.setEditMode(path.isEdited());

       return symPath;
    }


    protected void initIterators()
    {
       iterator = new JDRSymmetricPathIterator(this);
       pointIterator = new JDRPointIterator(this);
    }

    public JDRPathIterator getIterator()
    {
       iterator.reset();
       return iterator;
    }

    public JDRPointIterator getPointIterator()
    {
       pointIterator.reset();
       return pointIterator;
    }

    public JDRPathSegment getFirstSegment()
    {
       return path_.getFirstSegment();
    }

    public JDRPathSegment getLastSegment()
    {
       return line_;
    }

    public JDRPoint getFirstControl()
    {
       return path_.getFirstControl();
    }

    public JDRPoint getLastControl()
    {
       return line_.getEnd();
    }

    public JDRShape getUnderlyingShape()
    {
       return path_;
    }

    public void setUnderlyingShape(JDRShape shape)
    {
       path_ = shape;

       if (join == null)
       {
          moveToLine(path_.getLastSegment().getEnd());
       }
       else
       {
          join.setStart(path_.getLastSegment().getEnd());
       }

       if (isClosed())
       {
          if (closingSegment == null)
          {
             moveToLine(path_.getFirstControl());
          }
          else
          {
             closingSegment.setEnd(path_.getFirstControl());
          }
       }
    }

   @Override
   public void pathChanged()
   {
      fullPath_ = null;
      super.pathChanged();
   }

   protected JDRShape ensureFullPath()
   {
      if (fullPath_ == null)
      {
         try
         {
            fullPath_ = getFullPath();
         }
         catch (InvalidShapeException e)
         {
            return path_;
         }
      }

      return fullPath_;
   }

   public void add(JDRSegment s) throws InvalidPathException
   {
      path_.add(s);
      pathChanged();
   }

   @Override
   public void insert(int index, JDRSegment segment)
      throws ArrayIndexOutOfBoundsException,
        NullPointerException,
        ClosingMoveException
   {
      path_.insert(index, segment);
      pathChanged();
   }

    public JDRShape getFullPath()
      throws InvalidShapeException
    {
       return (JDRShape)getFullObject();
    }

    public JDRCompleteObject getFullObject()
      throws InvalidShapeException
    {
       int n = path_.size();

       JDRShape shape = (JDRShape)path_.getFullPath();

       // add in joining segment

       if (join != null)
       {
          shape.add(join.getFullSegment());
       }

       // add in reflected half

       for (int i = n-1; i >= 0; i--)
       {
          shape.add((JDRSegment)getReflected(i).reverse());
       }

       if (closingSegment != null)
       {
          shape.close(closingSegment.getFullSegment());
       }

       shape.description = description;
       shape.tag = tag;

       return shape;
    }

    public Object clone()
    {
       return new JDRSymmetricPath(this);
    }

    public void makeEqual(JDRObject object)
    {
       super.makeEqual(object);

       JDRSymmetricPath symPath = (JDRSymmetricPath)object;

       fullPath_ = null;
       path_.makeEqual(symPath.path_);

       line_.makeEqual(symPath.line_);

       JDRPartialSegment newJoin;

       if (join == null)
       {
          newJoin = (symPath.join == null ? null
                   : (JDRPartialSegment)symPath.join.clone());
       }
       else if (symPath.join == null)
       {
          newJoin = null;
       }
       else
       {
          newJoin = (JDRPartialSegment)symPath.join.clone();
       }

       setJoin(newJoin);

       if (!symPath.isClosed())
       {
          open();
          return;
       }

       if (symPath.closingSegment != null)
       {
          try
          {
             close((JDRPathSegment)symPath.closingSegment.clone());
          }
          catch (InvalidPathException e)
          {
          }
       }
       else
       {
          try
          {
             close(CLOSE_MERGE_ENDS);
          }
          catch (EmptyPathException e)
          {
          }
       }
    }

    /**
     * Moves the given point to the nearest point on the line of
     * symmetry.
     */
    protected void moveToLine(JDRPoint p)
    {
       p.moveToLine(line_);
       pathChanged();
    }

    public void translateControl(JDRPathSegment segment, JDRPoint p, 
      double x, double y)
    {
       path_.translateControl(segment, p, x, y);

       // Is this control point anchored to the line of symmetry?
       // Or is the control point defining the line of symmetry?

       if (join == null)
       {
         JDRPoint lastPt = path_.getLastControl();

         if (line_ == segment)
         {
            p = lastPt;
         }

         if (p == lastPt)
         {
            // Move it to the nearest point on the line

            moveToLine(p);
         }
       }

       if (isClosed())
       {
          if (closingSegment == null)
          {
             JDRPoint firstPt = path_.getFirstControl();

             if (line_ == segment)
             {
                p = firstPt;
             }

             if (p == firstPt)
             {
                moveToLine(p);
             }
          }
          else if (segment == closingSegment &&
                   p == closingSegment.getStart())
          {
             Point2D reflected = p.getReflection(line_);

             path_.getFirstSegment().setStart(reflected);
          }
          else if (p == path_.getFirstControl())
          {
             Point2D reflected = p.getReflection(line_);

             closingSegment.setStart(reflected);
          }
          else if (segment == line_)
          {
             closingSegment.setEnd(getFirstSegment().getStart());
          }
       }

       pathChanged();
    }

   public void setCapacity(int capacity)
      throws IllegalArgumentException
   {
      path_.setCapacity(capacity);
   }

   public int getCapacity()
   {
      return path_.getCapacity();
   }

   public JDRPoint addPoint()
   {
      JDRPoint p = null;
      JDRPathSegment selectedSegment = getSelectedSegment();

      if (selectedSegment instanceof JDRPartialSegment)
      {
         JDRSegment seg = ((JDRPartialSegment)selectedSegment).getFullSegment();

         int currentPointIndex = getSelectedControlIndex();

         try
         {
            if (selectedSegment == join)
            {
               seg.split();
               path_.add(seg);
               join = null;
            }
            else if (selectedSegment == closingSegment)
            {
               currentPointIndex = 0;

               seg = (JDRSegment)seg.split();

               path_.insert(0, seg);

               closingSegment = null;
            }
            else
            {
               return null;
            }
         }
         catch (InvalidPathException e)
         {
            // shouldn't happen as segment should only be a move, line or curve
            return null;
         }

         p = path_.selectControl(currentPointIndex);
      }
      else
      {
         p = path_.addPoint();
      }

      pathChanged();
      return p;
   }

   public void makeContinuous(boolean atStart, boolean equiDistant)
   {
      int n = path_.size();

      JDRPathSegment selectedSegment = getSelectedSegment();
      JDRPoint selectedControl = getSelectedControl();
      int selectedSegmentIndex = getSelectedIndex();

      if (selectedSegmentIndex == 0 && isClosed() && atStart)
      {
         if (!(selectedSegment instanceof JDRBezier))
         {
            return;
         }

         JDRBezier curve = (JDRBezier)selectedSegment;

         Point2D dP;

         if (closingSegment == null)
         {
            // Make gradient perpendicular to line of symmetry

            dP = line_.getdP();

            dP.setLocation(dP.getY(), -dP.getX());
         }
         else
         {
            dP = closingSegment.getdP1();
         }

         curve.setStartGradient(dP);

         return;
      }

      if (selectedSegmentIndex < n-1)
      {
         path_.makeContinuous(atStart, equiDistant);
         return;
      }

      if (selectedSegment == null)
      {
         return;
      }

      if (selectedSegmentIndex == n-1)
      {
         if (!(selectedSegment instanceof JDRBezier))
         {
            // Not a Bezier curve
            return;
         }

         JDRBezier curve = (JDRBezier)selectedSegment;

         if (selectedControl != curve.getControl2())
         {
            path_.makeContinuous(atStart, equiDistant);
            return;
         }

         // The selected point is the second curvature control
         // on the last segment of the underlying path

         if (join == null)
         {
            // No joining segment, so make the gradient
            // perpendicular to the line of symmetry

            Point2D dP = line_.getdP();

            dP.setLocation(dP.getY(), -dP.getX());

            curve.setEndGradient(dP);

            return;
         }

         Point2D gradient = join.getdP0();

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

         return;
      }

      if (!(selectedSegment instanceof JDRPartialBezier))
      {
         return;
      }

      JDRPartialBezier curve = (JDRPartialBezier)selectedSegment;

      if (curve == join)
      {
         JDRSegment segment = (JDRSegment)path_.getLastSegment();

         Point2D gradient = segment.getdP1();

         if (equiDistant)
         {
            curve.setGradient(gradient);
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

               curve.setGradient(factor*gradient.getX(),
                                 factor*gradient.getY());
            }
         }

         return;
      }

      if (curve == closingSegment)
      {
         Point2D dP = getReflected(0).reverse().getdP1();

         curve.setGradient(dP);
      }

      pathChanged();
   }

   protected void stopEditing()
   {
      path_.stopEditing();

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

   public int getSelectedControlIndex()
   {
      return path_.getSelectedControlIndex();
   }

   public JDRPathSegment removeSelectedSegment()
   throws InvalidPathException
   {
      int selectedSegmentIndex = getSelectedIndex();
      JDRPathSegment segment = null;

      if (selectedSegmentIndex < path_.size())
      {
         segment = remove(selectedSegmentIndex);

         pathChanged();
      }

      return segment;
   }

   public JDRPathSegment remove(JDRPathSegment segment)
   throws InvalidPathException
   {
      JDRPathSegment removedSeg = null;

      for (int i = 0, n = path_.size(); i < n; i++)
      {
         if (get(i) == segment)
         {
            removedSeg = remove(i);
            pathChanged();
            break;
         }
      }

      return removedSeg;
   }

   public JDRSegment removeSegment(int index)
     throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      JDRSegment segment = (JDRSegment)path_.remove(index);
      pathChanged();
      return segment;
   }

   public JDRPathSegment setSegment(int index, JDRPathSegment segment)
     throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      int n = path_.size();

      pathChanged();

      if (index < n)
      {
         return path_.setSegment(index, segment);
      }

      int i = 0;

      if (join != null)
      {
         if (index == n)
         {
            JDRPathSegment oldJoin = join;
            setJoin((JDRPartialSegment)segment);

            return oldJoin;
         }

         i++;
      }

      if (closingSegment != null)
      {
         if (index == n+i)
         {
            JDRPathSegment oldSegment = closingSegment;
            closingSegment = (JDRPartialSegment)segment;

            closingSegment.setSymmetryLine(line_);

            return oldSegment;
         }
      }

      throw new ArrayIndexOutOfBoundsException(index);
   }

   public JDRSegment remove(int i) throws InvalidPathException
   {
      JDRSegment segment = (JDRSegment)path_.get(i);
      JDRPoint dp = segment.getEnd();

      JDRPoint selectedControl = getSelectedControl();

      int index = getSelectedControlIndex();

      if (dp == selectedControl ||
          (segment instanceof JDRBezier
           && selectedControl == ((JDRBezier)segment).control2))
      {
         dp = segment.getStart();
      }

      if (i == 0)
      {
         if (isClosed() && closingSegment != null)
         {
            closingSegment.setStart(dp);
         }
      }
      else
      {
         JDRSegment prev = (JDRSegment)path_.get(i-1);

         if (i == path_.size()-1 && isAnchored())
         {
            moveToLine(prev.getEnd());
         }
         else
         {
            prev.setEnd(dp);
         }
      }

      JDRSegment oldSegment = path_.removeSegment(i);

      stopEditing();

      dp = selectControl(index);

      if (dp == null)
      {
         selectControl(0);
      }

      pathChanged();

      return oldSegment;
   }

   public int getSelectedIndex()
   {
      return path_.getSelectedIndex();
   }

   public JDRPoint getSelectedControl()
   {
      return path_.getSelectedControl();
   }

   public JDRPathSegment getSelectedSegment()
   {
      return path_.getSelectedSegment();
   }

   public int getSelectedSegmentFlag()
   {
      JDRPathSegment seg = getSelectedSegment();

      if (seg == null) return SEGMENT_FLAG_NONE;

      if (seg == getSymmetry())
      {
         return SEGMENT_FLAG_SYMMETRY_LINE;
      }

      return super.getSelectedSegmentFlag();
   }

   public boolean segmentHasEnd(JDRPathSegment segment)
   {
      if (segment instanceof JDRPartialSegment)
      {
         return false;
      }

      if (segment == line_) return true;

      if (isAnchored() && segment == path_.getLastSegment())
      {
         return true;
      }

      return false;
   }

   public int size()
   {
      return getTotalBaseSegments();
   }

   /**
    * Gets total number of base segments including the join segment (if
    * any), the line of symmetry and the closing segment (if any).
    */

   public int getTotalBaseSegments()
   {
      int n = path_.size()+1;

      if (join != null) n++;

      if (closingSegment != null) n++;

      return n;
   }

   /**
    * Gets total number of segments that make up the path. Doesn't
    * include the line of symmetry, does include the join segment
    * (if non-null), the reflected segments and the closing segment 
    * (if non-null).
    */
   public int getTotalPathSegments()
   {
      int n = 2*path_.size();

      if (join != null) n++;

      if (closingSegment != null) n++;

      return n;
   }

   /**
    * Gets the segment given by index.
    */
   public JDRPathSegment get(int index)
     throws ArrayIndexOutOfBoundsException
   {
      int n = path_.size();

      if (index < n) return path_.get(index);

      int i = 0;

      if (join != null)
      {
         if (index == n) return join;
         i++;
      }

      if (closingSegment != null)
      {
         if (index == n+i) return closingSegment;
         i++;
      }

      if (index == n+i) return line_;

      throw new ArrayIndexOutOfBoundsException(index);
   }

   public int getIndex(JDRPathSegment segment)
   {
      int index = 0;

      for (int n = path_.size(); index < n; index++)
      {
         JDRPathSegment s = path_.get(index);

         if (s == segment) return index;
      }

      if (join != null)
      {
         if (join == segment) return index;
         index++;
      }

      if (closingSegment != null)
      {
         if (closingSegment == segment) return index;
         index++;
      }

      if (line_ == segment) return index;

      return -1;
   }

   public int getLastIndex(JDRPathSegment segment)
   {
      int n = path_.size();

      int i = 0;

      if (join != null)
      {
         if (segment == join) return n;
         i++;
      }

      if (closingSegment != null)
      {
         if (segment == closingSegment) return n+i;
         i++;
      }

      if (segment == line_)
      {
         return n+i;
      }

      return path_.getLastIndex(segment);
   }

    /**
     * Gets the segment that's the reflection of the given segment.
     */
    public JDRPathSegment getReflected(int index)
    {
       return get(index).getReflection(line_);
    }

   public void draw(FlowFrame parentFrame)
   {
      if (parentFrame == null)
      {
         parentFrame = flowframe;
      }

      if (isSingle())
      {
         ensureFullPath().draw(parentFrame);
      }
      else
      {
         CanvasGraphics cg = getCanvasGraphics();

         Graphics2D g2 = cg.getGraphics();

         if (g2 == null) return;

         BBox box = null;

         JDRPaint linePaint = getLinePaint();
         JDRPaint fillPaint = getShapeFillPaint();

         if (fillPaint instanceof JDRShading
           ||linePaint instanceof JDRShading)
         {
            box = getStorageBBox();

            if (parentFrame != null && cg.isEvenPage())
            {
               box.translate(parentFrame.getEvenXShift(),
                             parentFrame.getEvenYShift());
            }
         }

         Paint oldPaint = g2.getPaint();
         AffineTransform oldAf = g2.getTransform();

         path_.draw(parentFrame);

         JDRBasicStroke bs = null;

         if (join != null || closingSegment != null)
         {
            bs = getBasicStroke();
         }

         if (join != null && bs != null)
         {
            Shape joinShape = join.toShape();

            if (joinShape != null)
            {
               if (!(fillPaint instanceof JDRTransparent))
               {
                  g2.setPaint(fillPaint.getPaint(box));
                  g2.fill(joinShape);
               }

               if (!(linePaint instanceof JDRTransparent))
               {
                  g2.setPaint(linePaint.getPaint(box));

                  g2.fill(bs.createStrokedShape(joinShape,
                    getCanvasGraphics().getStorageUnit()));
               }
            }
         }

         AffineTransform af = line_.getReflectionTransform(null);

         af.preConcatenate(oldAf);

         g2.setTransform(af);

         path_.draw(parentFrame);

         g2.setTransform(oldAf);

         if (closingSegment != null)
         {
            Shape closingSegmentShape = closingSegment.toShape();

            if (closingSegmentShape != null)
            {
               if (!(fillPaint instanceof JDRTransparent))
               {
                  g2.setPaint(fillPaint.getPaint(box));
                  g2.fill(closingSegmentShape);
               }

               if (!(linePaint instanceof JDRTransparent))
               {
                  g2.setPaint(linePaint.getPaint(box));

                  g2.fill(bs.createStrokedShape(closingSegmentShape,
                    getCanvasGraphics().getStorageUnit()));
               }
            }
         }

         g2.setPaint(oldPaint);
      }
   }

   public void print(Graphics2D g2)
   {
      if (isSingle())
      {
         ensureFullPath().print(g2);
      }
      else
      {
         BBox box = null;

         JDRPaint linePaint = getLinePaint();
         JDRPaint fillPaint = getShapeFillPaint();

         if (fillPaint instanceof JDRShading
           ||linePaint instanceof JDRShading)
         {
            box = getStorageBBox();
         }

         Paint oldPaint = g2.getPaint();
         AffineTransform oldAf = g2.getTransform();

         path_.print(g2);

         JDRBasicStroke bs = null;

         if (join != null || closingSegment != null)
         {
            bs = getBasicStroke();
         }

         if (join != null && bs != null)
         {
            Shape joinShape = join.toShape();

            if (joinShape != null)
            {
               if (!(fillPaint instanceof JDRTransparent))
               {
                  g2.setPaint(fillPaint.getPaint(box));
                  g2.fill(joinShape);
               }

               if (!(linePaint instanceof JDRTransparent))
               {
                  g2.setPaint(linePaint.getPaint(box));

                  g2.fill(bs.createStrokedShape(joinShape,
                    getCanvasGraphics().getStorageUnit()));
               }
            }
         }

         AffineTransform af = line_.getReflectionTransform(null);

         af.preConcatenate(oldAf);

         g2.setTransform(af);

         path_.print(g2);

         g2.setTransform(oldAf);

         if (closingSegment != null)
         {
            Shape closingSegmentShape = closingSegment.toShape();

            if (closingSegmentShape != null)
            {
               if (!(fillPaint instanceof JDRTransparent))
               {
                  g2.setPaint(fillPaint.getPaint(box));
                  g2.fill(closingSegmentShape);
               }

               if (!(linePaint instanceof JDRTransparent))
               {
                  g2.setPaint(linePaint.getPaint(box));

                  g2.fill(bs.createStrokedShape(closingSegmentShape,
                    getCanvasGraphics().getStorageUnit()));
               }
            }
         }

         g2.setPaint(oldPaint);
      }
   }

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

      Graphics2D g2 = cg.getGraphics();

      AffineTransform orgAf = g2.getTransform();

      if (doShift)
      {
         g2.translate(cg.storageToComponentX(xshift), 
            cg.storageToComponentY(parentFrame.getEvenYShift()));
      }

      Paint oldPaint = g2.getPaint();

      g2.setPaint(draftColor);

      path_.drawDraft(parentFrame);

      if (join != null)
      {
         join.drawDraft(false);
      }

      g2.setPaint(draftColor);

      AffineTransform af = line_.getComponentReflectionTransform(null);

      Path2D p = path_.getComponentGeneralPath();

      p.transform(af);

      g2.draw(p);

      if (closingSegment != null)
      {
         closingSegment.drawDraft(false);
         closingSegment.start.draw();
      }

      line_.drawDraft(true);
      g2.setPaint(oldPaint);
      g2.setTransform(orgAf);
   }

   @Override
   public BBox getStorageBBox()
   {
      if (isSingle())
      {
         return ensureFullPath().getStorageBBox();
      }
      else
      {
         BBox box = super.getStorageBBox();
// TODO??
         return box;
      }
   }

   @Override
   public void mergeStorageBBox(BBox box)
   {
      if (isSingle())
      {
         ensureFullPath().mergeStorageBBox(box);
      }
      else
      {
         super.mergeStorageBBox(box);
// TODO??
      }
   }

    public BBox getStorageControlBBox()
    {
       BBox bbox = path_.getStorageControlBBox();

       if (bbox == null) return null;

       for (int i = 0; i < path_.size(); i++)
       {
          JDRSegment segment = (JDRSegment)path_.get(i);
          segment.mergeReflectedBBox(line_, bbox);
       }

       if (join != null)
       {
          join.mergeStorageControlBBox(bbox);
       }

       if (closingSegment != null)
       {
          closingSegment.mergeStorageControlBBox(bbox);
       }

       line_.mergeStorageControlBBox(bbox);

       return bbox;
    }

    public void mergeStorageControlBBox(BBox box)
    {
       path_.mergeStorageControlBBox(box);

       for (int i = 0; i < path_.size(); i++)
       {
          JDRSegment segment = (JDRSegment)path_.get(i);
          segment.mergeReflectedBBox(line_, box);
       }

       if (join != null)
       {
          join.mergeStorageControlBBox(box);
       }

       if (closingSegment != null)
       {
          closingSegment.mergeStorageControlBBox(box);
       }

       line_.mergeStorageControlBBox(box);
    }


   public void convertSegment(int idx, JDRPathSegment segment)
   throws InvalidPathException
   {
      JDRPathSegment orgSegment = get(idx);

      JDRPoint selectedControl = getSelectedControl();
      int selectedControlIndex = getSelectedControlIndex();

      if (idx < path_.size())
      {
         path_.convertSegment(idx, segment);

         if (join != null && orgSegment.getEnd() == join.getStart())
         {
            join.setStart(segment.getEnd());
         }

         return;
      }

      setSegment(idx, segment);

      try
      {
         int i = orgSegment.getControlIndex(selectedControl);

         selectedControl = segment.getStart();

         if (i > 0)
         {
            selectedControlIndex -= i;
         }

         setSelectedElements(idx, selectedControlIndex, segment, 
            selectedControl);
      }
      catch (NoSuchElementException e)
      {
      }

      pathChanged();
   }

   public void shearParams(Point2D p, double factorX, double factorY)
   {
      if (join != null)
      {
         join.shear(p, factorX, factorY);
      }

      line_.shear(p, factorX, factorY);

      if (closingSegment != null)
      {
         closingSegment.shear(p, factorX, factorY);

         closingSegment.setEnd(getFirstControl());
      }

      pathChanged();
   }

   public void scaleParams(Point2D p, double factorX, double factorY)
   {
      if (join != null)
      {
         join.scale(p, factorX, factorY);
      }

      line_.scale(p, factorX, factorY);

      if (closingSegment != null)
      {
         closingSegment.scale(p, factorX, factorY);

         closingSegment.setEnd(getFirstControl());
      }

      pathChanged();
   }

   public void rotateParams(Point2D p, double angle)
   {
      if (join != null)
      {
         join.rotate(p, angle);
      }

      line_.rotate(p, angle);

      if (closingSegment != null)
      {
         closingSegment.rotate(p, angle);

         closingSegment.setEnd(getFirstControl());
      }

      pathChanged();
   }

   public void translateParams(double shiftX, double shiftY)
   {
      if (join != null)
      {
         join.translate(shiftX, shiftY);
      }

      line_.translate(shiftX, shiftY);

      if (closingSegment != null)
      {
         closingSegment.translate(shiftX, shiftY);

         closingSegment.setEnd(getFirstControl());
      }

      pathChanged();
   }

   @Override
   public void transformParams(double[] matrix)
   {
      if (join != null)
      {
         join.transform(matrix);
      }

      line_.transform(matrix);

      if (closingSegment != null)
      {
         closingSegment.transform(matrix);

         closingSegment.setEnd(getFirstControl());
      }

      pathChanged();
   }

   @Override
   public void transformParams(AffineTransform af)
   {
      if (join != null)
      {
         join.transform(af);
      }

      line_.transform(af);

      if (closingSegment != null)
      {
         closingSegment.transform(af);

         closingSegment.setEnd(getFirstControl());
      }

      pathChanged();
   }

   public boolean isEmpty()
   {
      return size() == 0;
   }

   public boolean isClosed()
   {
      return closed;
   }

   public void open()
   {
      closed = false;
      closingSegment = null;

      pathChanged();
   }

   public void open(boolean removeLastSegment)
   {
      open();
   }

   public void close(int type)
     throws EmptyPathException
   {
      if (isEmpty())
      {
         throw new EmptyPathException(getCanvasGraphics());
      }

      JDRSegment firstSeg = (JDRSegment)path_.getFirstSegment();

      try
      {
         switch (type)
         {
            case CLOSE_LINE :
               close(new JDRPartialLine(getCanvasGraphics()));
            return;
            case CLOSE_CONT :
               JDRPartialBezier seg = new JDRPartialBezier(getCanvasGraphics());
               close(seg);

               JDRSegment lastSeg = ((JDRSegment)firstSeg.getReflection(line_)).reverse();

               Point2D dp = lastSeg.getdP1();

               seg.setGradient(dp);

            return;
            case CLOSE_MERGE_ENDS :
               moveToLine(firstSeg.getStart());
               closed = true;
               closingSegment = null;

               if (isEdited())
               {
                  selectControl(0);
               }
            return;
            default:
               throw new IllegalArgumentException("Invalid close path type "
                  + type);
         }
      }
      catch (IllFittingPathException e)
      {
      }
   }

    /**
     * Closes this symmetric path with the given segment, which must
     * be a partial segment. If the given segment is null, this
     * method just calls {@link #close(CLOSE_MERGE_ENDS)}
     * @param segment the closing segment (must be an instance of 
     * {@link JDRPartialSegment}).
     * @see #close(int)
     */
   public void close(JDRPathSegment segment)
     throws EmptyPathException,IllFittingPathException
   {
      if (segment == null)
      {
         close(CLOSE_MERGE_ENDS);
         return;
      }

      if (!(segment instanceof JDRPartialSegment))
      {
         throw new IllFittingPathException(getCanvasGraphics());
      }

      closed = true;

      closingSegment = (JDRPartialSegment)segment;

      closingSegment.setSymmetryLine(line_);

      closingSegment.setEnd(path_.getFirstSegment().getStart());

      if (isEdited())
      {
         selectControl(0);
      }

      pathChanged();
   }

   public JDRShape breakPath()
      throws InvalidShapeException
   {
      // Make new path have the same join as this path had before
      // it gets broken

      JDRLine line = (JDRLine)line_.clone();

      JDRPartialSegment joinSegment = null;

      if (getJoin() != null)
      {
         joinSegment = (JDRPartialSegment)getJoin().clone();
      }

      // break this path

      JDRShape path = path_.breakPath();

      // make the new path symmetric

      JDRSymmetricPath newPath = new JDRSymmetricPath(path,
         joinSegment,
         line);

      // make the join for this path a gap

      setJoin(new JDRPartialSegment(path_.getLastSegment().getEnd(), line_));

      pathChanged();

      return newPath;
   }

   public Shape getStorageStrokedPath()
   {
      Shape shape;

      if (isSingle())
      {
         shape = ensureFullPath().getStorageStrokedPath();
      }
      else
      {
         shape = path_.getStorageStrokedPath();

         AffineTransform af = line_.getReflectionTransform(null);

         Path2D reflectedShape = new Path2D.Double(shape);

         reflectedShape.transform(af);

         JDRBasicStroke bs = null;

         if (join != null || closingSegment != null)
         {
            bs = getBasicStroke();
         }

         if (join != null && bs != null)
         {
            Shape joinShape = join.toShape();

            if (joinShape != null)
            {
               reflectedShape.append(bs.createStrokedShape(joinShape,
                 getCanvasGraphics().getStorageUnit()), false);
            }
         }

         reflectedShape.append(shape, false);

         if (closingSegment != null && bs != null)
         {
            Shape closingSegmentShape = closingSegment.toShape();

            reflectedShape.append(bs.createStrokedShape(closingSegmentShape,
              getCanvasGraphics().getStorageUnit()), false);
         }

         shape = reflectedShape;
      }

      return shape;
   }

   public Area getStorageStrokedArea()
   {
      Area shape;

      if (isSingle())
      {
         shape = ensureFullPath().getStorageStrokedArea();
      }
      else
      {
         shape = path_.getStorageStrokedArea();

         AffineTransform af = line_.getReflectionTransform(null);

         Area reflectedShape = new Area(shape);

         reflectedShape.transform(af);

         shape.add(reflectedShape);

         JDRBasicStroke bs = null;

         if (join != null || closingSegment != null)
         {
            bs = getBasicStroke();
         }

         if (join != null && bs != null)
         {
            Shape joinShape = join.toShape();

            if (joinShape != null)
            {
               shape.add(new Area(bs.createStrokedShape(joinShape,
                 getCanvasGraphics().getStorageUnit())));
            }
         }

         if (closingSegment != null && bs != null)
         {
            Shape closingSegmentShape = closingSegment.toShape();

            shape.add(new Area(bs.createStrokedShape(closingSegmentShape,
              getCanvasGraphics().getStorageUnit())));
         }

      }

      return shape;
   }

   public JDRStroke getStroke()
   {
      return path_.getStroke();
   }

   public void setStroke(JDRStroke stroke)
   {
      path_.setStroke(stroke);
      pathChanged();
   }

   public void fade(double value)
   {
      path_.fade(value);
      pathChanged();
   }

   public JDRPaint getLinePaint()
   {
      return path_.getLinePaint();
   }

   public void setLinePaint(JDRPaint paint)
   {
      path_.setLinePaint(paint);
      pathChanged();
   }

   public JDRPaint getShapeFillPaint()
   {
      return path_.getShapeFillPaint();
   }

   @Deprecated
   public JDRPaint getFillPaint()
   {
      return path_.getFillPaint();
   }

   @Deprecated
   public void setFillPaint(JDRPaint paint)
   {
      path_.setFillPaint(paint);
      pathChanged();
   }

   public void setShapeFillPaint(JDRPaint paint)
   {
      path_.setShapeFillPaint(paint);
      pathChanged();
   }

   /**
    * Separates this symmetric path into a group containing its
    * constituent parts. The groups consists of: the underlying
    * shape, a path containing the join segment if it exists, a path
    * containing the line of symmetry, the reflected shape, and a
    * path containing the closing segment if it exists.
    * @return a group containing between 3 and 5 objects
    */
   public JDRGroup separate()
      throws InvalidShapeException
   {
      return separate(true);
   }

   public JDRGroup separate(boolean incSymmetryLine)
      throws InvalidShapeException
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();

      JDRBasicStroke bs = null;

      if (join != null || closingSegment != null)
      {
         bs = getBasicStroke();
      }
         
      JDRGroup group = new JDRGroup(cg);

      JDRShape shape = (JDRShape)getUnderlyingShape().clone();

      group.add(shape);

      if (join != null && bs != null)
      {
         try
         {
            JDRBasicStroke stroke = new JDRBasicStroke(bs);
            stroke.setStartArrow(JDRMarker.ARROW_NONE);
            stroke.setEndArrow(JDRMarker.ARROW_NONE);

            JDRPath path = new JDRPath(
              (JDRPaint)getLinePaint().clone(), 
              (JDRPaint)getShapeFillPaint().clone(), 
              stroke);

            path.add(joinToFullSegment());

            group.add(path);
         }
         catch (InvalidPathException e)
         {
            getCanvasGraphics().debugMessage(e);
         }
      }

      JDRShape reflectedShape = shape.reflection(line_);

      group.add(reflectedShape);

      if (closingSegment != null && bs != null)
      {
         try
         {
            JDRBasicStroke stroke = new JDRBasicStroke(bs);
            stroke.setStartArrow(JDRMarker.ARROW_NONE);
            stroke.setEndArrow(JDRMarker.ARROW_NONE);

            JDRPath path = new JDRPath(
              (JDRPaint)getLinePaint().clone(), 
              (JDRPaint)getShapeFillPaint().clone(),
              stroke);

            path.add(closingToFullSegment());

            group.add(path);
         }
         catch (InvalidPathException e)
         {
            getCanvasGraphics().debugMessage(e);
         }
      }

      if (incSymmetryLine)
      {
         try
         {
            JDRPath path = new JDRPath(
              (JDRPaint)getLinePaint().clone(), 
              (JDRPaint)getShapeFillPaint().clone(),
              new JDRBasicStroke(getCanvasGraphics()));

            path.add(new JDRLine(line_));
            group.add(path);
         }
         catch (InvalidPathException e)
         {
            getCanvasGraphics().debugMessage(e);
         }
      }

      group.setSelected(isSelected());

      group.setDescription(getDescription());
      group.setTag(getTag());

      return group;
   }

   public JDRGroup splitText(TextModeMappings textMappings,
     MathModeMappings mathMappings, Vector<String> styNames)
    throws InvalidShapeException
   {
      JDRTextual textual = getTextual();

      if (textual == null)
      {
         throw new InvalidShapeException(getCanvasGraphics().getMessageWithFallback(
           "error.invalid_text-path-shape", "Invalid text-path shape"));
      }

      JDRTextPath tp = (JDRTextPath)textual;
      JDRTextPathStroke tps = (JDRTextPathStroke)tp.getStroke();

      JDRGroup group;

      if (isSingle())
      {
         JDRShape shape = getFullPath();

         if (shape instanceof JDRCompoundShape)
         {
            group = ((JDRCompoundShape)shape).splitText(textMappings,
              mathMappings, styNames);
         }
         else
         {
            group = textual.splitText(textMappings,
              mathMappings, styNames);
         }
      }
      else
      {
         group = new JDRGroup(getCanvasGraphics());

         JDRGroup subGrp = ((JDRCompoundShape)path_).splitText(textMappings,
              mathMappings, styNames);

         if (subGrp.size() == 1)
         {
            group.add(subGrp.firstElement());
         }
         else
         {
            group.add(subGrp);
         }

         JDRBasicStroke bs = null;

         if (join != null || closingSegment != null)
         {
            bs = getBasicStroke();
         }
         
         if (join != null && bs != null)
         {
            try
            {
               JDRBasicStroke stroke = new JDRBasicStroke(bs);
               stroke.setStartArrow(JDRMarker.ARROW_NONE);
               stroke.setEndArrow(JDRMarker.ARROW_NONE);

               JDRPath path = new JDRPath(
                 (JDRPaint)getLinePaint().clone(), 
                 (JDRPaint)getShapeFillPaint().clone(), 
                 stroke);

               path.add(joinToFullSegment());

               group.add(path);
            }
            catch (InvalidPathException e)
            {
               getCanvasGraphics().debugMessage(e);
            }
         }

         AffineTransform af = line_.getReflectionTransform(null);

         subGrp = (JDRGroup)subGrp.clone();
         subGrp.transform(af);

         if (subGrp.size() == 1)
         {
            group.add(subGrp.firstElement());
         }
         else
         {
            group.add(subGrp);
         }

         if (closingSegment != null && bs != null)
         {
            try
            {
               JDRBasicStroke stroke = new JDRBasicStroke(bs);
               stroke.setStartArrow(JDRMarker.ARROW_NONE);
               stroke.setEndArrow(JDRMarker.ARROW_NONE);

               JDRPath path = new JDRPath(
                 (JDRPaint)getLinePaint().clone(), 
                 (JDRPaint)getShapeFillPaint().clone(),
                 stroke);

               path.add(closingToFullSegment());
               group.add(path);
            }
            catch (InvalidPathException e)
            {
               getCanvasGraphics().debugMessage(e);
            }
         }
      }

      group.setSelected(isSelected());

      group.setDescription(getDescription());
      group.setTag(getTag());

      return group;
   }

   /**
    * Gets this path as a Path2D.
    * @return this path as a Path2D
    */
   public Path2D getGeneralPath()
   {
      if (isEmpty()) return null;

      return ensureFullPath().getGeneralPath();
   }

   public JDRShape toPolygon(double flatness)
       throws InvalidShapeException
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRShape polygon = path_.toPolygon(flatness);

      JDRLine newSymLine = new JDRLine(line_);

      JDRPartialSegment newJoin = null;
      JDRPartialSegment newClosing = null;

      if (join != null && join.isCurve())
      {
         newJoin = new JDRPartialLine(cg);
      }

      if (closingSegment != null && closingSegment.isCurve())
      {
         newClosing = new JDRPartialLine(cg);
      }

      return new JDRSymmetricPath(polygon, newJoin, newSymLine, newClosing,
        closed, isSingle);
   }

   @Override
   public JDRShape reverse() throws InvalidShapeException
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRSymmetricPath symPath = new JDRSymmetricPath(this);

      AffineTransform af = line_.getReflectionTransform(null);

      symPath.path_.transform(af);

      if (join != null)
      {
         symPath.join = (JDRPartialSegment)join.reverse();
         symPath.join.setSymmetryLine(symPath.line_);
         symPath.join.setStart(symPath.path_.getLastSegment().getEnd());
      }

      if (closingSegment != null)
      {
         symPath.closingSegment = (JDRPartialSegment)closingSegment.reverse();
         symPath.closingSegment.setSymmetryLine(symPath.line_);
         symPath.closingSegment.setEnd(symPath.path_.getFirstControl());
      }

      return symPath;
   }

   @Override
   public JDRShape intersect(JDRShape shape)
      throws InvalidShapeException
   {
      return ensureFullPath().intersect(shape);
   }

   @Override
   public JDRShape pathUnion(JDRShape shape)
      throws InvalidShapeException
   {
      return ensureFullPath().pathUnion(shape);
   }

   @Override
   public JDRShape exclusiveOr(JDRShape shape)
      throws InvalidShapeException
   {
      return ensureFullPath().exclusiveOr(shape);
   }

   @Override
   public JDRShape subtract(JDRShape shape)
      throws InvalidShapeException
   {
      return ensureFullPath().subtract(shape);
   }

   @Override
   public JDRCompleteObject clip(Rectangle2D clipBounds)
      throws UnableToClipException
   {
      return ensureFullPath().clip(clipBounds);
   }

   public JDRObjectLoaderListener getListener()
   {
      return symmetricPathListener;
   }

   /**
    * Gets string representation of this symmetric path.
    * @return string representation of this symmetric path
    */
   public String toString()
   {
      String str = "SymmetricPath: join="+join+", symmetry: " +line_
                 + ", single="+isSingle
                 + ", size="+size()+", segments=[";

      for (int i = 0; i < size(); i++)
      {
         str += get(i)+",";
      }

      str += "]";

      return str;
    }

    /**
     * Gets if the end point is anchored to the line of symmetry.
     * @return true if end point anchored otherwise false
     */
    public boolean isAnchored()
    {
       return join == null;
    }

    /**
     * Sets the join.
     * @param segment the join segment (may be null)
     */
   public void setJoin(JDRPartialSegment segment)
   {
      if (join == segment) return;

      join = segment;

      if (join == null)
      {
         moveToLine(path_.getLastSegment().getEnd());

         if (isEdited())
         {
            selectControl(path_.getLastSegment().getEnd());
         }
      }
      else
      {
         join.setStart(path_.getLastSegment().getEnd());
         join.setSymmetryLine(line_);

         if (isEdited())
         {
            selectControl(join.getStart());
         }
      }

      pathChanged();
   }

    public JDRPartialSegment getJoin()
    {
       return join;
    }

    public JDRSegment joinToFullSegment()
    {
       if (join == null)
       {
          return null;
       }

       JDRSegment seg = join.getFullSegment();
       seg.setStart(new JDRPoint(seg.start));

       return seg;
    }

    /**
     * Gets the closing segment.
     * @return the closing segment or null if this path is open or if
     * this path is closed and the start point is anchored to the line 
     * of symmetry
     */
    public JDRPartialSegment getClosingSegment()
    {
       return closingSegment;
    }

    public JDRSegment closingToFullSegment()
    {
       if (closingSegment == null)
       {
          return null;
       }

       JDRSegment seg = closingSegment.getFullSegment();
       seg.setStart(new JDRPoint(seg.start));
       seg.setEnd(new JDRPoint(seg.end));

       return seg.reverse();
    }

   public boolean isPolygon()
   {
      if ((join != null && join.isCurve())
       || (closingSegment != null && closingSegment.isCurve()))
      {
         return false;
      }

      return getUnderlyingShape().isPolygon();
   }

    /**
     *  Checks if join is an instance of JDRPartialLine and is
     *  non-null.
     *  @return true if join not null and an instance of
     *  JDRPartialLine
     */
    public boolean isJoinLine()
    {
       if (join == null) return false;

       return (join instanceof JDRPartialLine);
    }

    /**
     *  Checks if the closing segment is an instance of JDRPartialLine 
     *  and is non-null.
     *  @return true if closing segment not null and an instance of
     *  JDRPartialLine
     */
    public boolean isClosingSegmentLine()
    {
       if (closingSegment == null) return false;

       return (closingSegment instanceof JDRPartialLine);
    }

    /**
     *  Checks if join is an instance of JDRPartialBezier and is
     *  non-null.
     *  @return true if join not null and an instance of
     *  JDRPartialBezier
     */
    public boolean isJoinBezier()
    {
       if (join == null) return false;

       return (join instanceof JDRPartialBezier);
    }

    /**
     *  Checks if the closing segment is an instance of
     *  JDRPartialBezier and is non-null.
     *  @return true if closing segment not null and an instance of
     *  JDRPartialBezier
     */
    public boolean isClosingSegmentBezier()
    {
       if (closingSegment == null) return false;

       return (closingSegment instanceof JDRPartialBezier);
    }

   /**
    * Sets the line of symmetry.
    * @param x0 x co-ordinate of first point
    * @param y0 y co-ordinate of first point
    * @param x1 x co-ordinate of second point
    * @param y1 y co-ordinate of second point
    */
   public void setSymmetry(double x0, double y0, double x1, double y1)
   {
     CanvasGraphics cg = getCanvasGraphics();

      if (line_ == null)
      {
         line_ = new JDRLine(new JDRSymmetryLinePoint(cg, x0, y0),
                             new JDRSymmetryLinePoint(cg, x1, y1));
      }
      else
      {
         line_.setStart(x0, y0);
         line_.setEnd(x1, y1);
      }

      if (join != null) join.setSymmetryLine(line_);

      pathChanged();
   }

   public void setSymmetry(JDRLine line)
   {
      CanvasGraphics cg = getCanvasGraphics();

      line_ = line;

      if (line == null)
      {
         return;
      }

      JDRPoint p = line_.getStart();

      if (!(p instanceof JDRSymmetryLinePoint))
      {
         line_.setStart(new JDRSymmetryLinePoint(cg, p.x, p.y));
      }

      p = line_.getEnd();

      if (!(p instanceof JDRSymmetryLinePoint))
      {
         line_.setEnd(new JDRSymmetryLinePoint(cg, p.x, p.y));
      }

      pathChanged();
   }

    /**
     * Gets the line of symmetry.
     * @return line of symmetry
     */ 
    public JDRLine getSymmetry()
    {
       return line_;
    }

    public void selectControl(JDRPoint p, int pointIndex, int segmentIndex)
    {
       stopEditing();

       selected = true;

/*
       selectedControl = p;
       selectedControlIndex = pointIndex;
       selectedSegmentIndex = segmentIndex;
*/

       if (segmentIndex < path_.size())
       {
          path_.selectControl(p, pointIndex, segmentIndex);
       }
       else
       {
          JDRPathSegment selectedSegment = get(segmentIndex);

          stopEditing();

          setSelectedElements(segmentIndex, pointIndex, selectedSegment, p);

          p.setSelected(true);
          selectedSegment.setSelected(true);
       }

       editMode = true;
    }

    public int psLevel()
    {
       return path_.psLevel();
    }

    public void saveEPS(PrintWriter out) throws IOException
    {
       JDRPaint paint = getShapeFillPaint();

       Path2D path = getGeneralPath();

       out.println("gsave");
       EPS.fillPath(path, paint, out);
       out.println("grestore");

       out.println("gsave");
       getStroke().saveEPS(this, out);
       out.println("grestore");
    }

   public void saveSVG(SVG svg, String attr)
      throws IOException
   {
      try
      {
         if (isSingle())
         {
            getFullPath().saveSVG(svg, attr);
         }
         else
         {
            separate(false).saveSVG(svg, attr);
         }
      }
      catch (InvalidShapeException e)
      {
         canvasGraphics.warning(e);
      }
   } 

   public void savePgf(TeX tex)
    throws IOException
   {
      try
      {
         if (isSingle())
         {
            getFullPath().savePgf(tex);
         }
         else
         {
            separate(false).savePgf(tex);
         }
      }
      catch (InvalidShapeException e)
      {
         canvasGraphics.warning(e);
      }
   }

   /** 
    * Gets just the path construction commands.
    */
   public void savePgfPath(TeX tex)
     throws IOException
   {
      String path = "";

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

   public void pgfMarkers(TeX tex, BBox pathBBox)
     throws IOException
   {
      if (getStroke() instanceof JDRBasicStroke)
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
   }

   public JDRTextual getTextual()
   {
      return path_.getTextual();
   }

   public boolean hasTextual()
   {
      return path_.hasTextual();
   }

   public boolean showPath()
   {
      return path_.showPath();
   }

   public boolean isSingle()
   {
      return isSingle;
   }

   public void setSingleMode(boolean on)
   {
      isSingle = on;
      pathChanged();
   }

   @Deprecated
   public boolean hasMarkerSymmetry()
   {
      return !isSingle();
   }

   @Deprecated
   public void setMarkerSymmetry(boolean on)
   {
      isSingle = !on;
   }

   public boolean hasSymmetricPath()
   {
      return true;
   }

   public JDRSymmetricPath getSymmetricPath()
   {
      return this;
   }

   public int getObjectFlag()
   {
      int flag = super.getObjectFlag() | SELECT_FLAG_SYMMETRIC;

      if (path_.hasTextual())
      {
         flag = (flag | SELECT_FLAG_SYMTEXTPATH);
      }

      if (isAnchored())
      {
         flag = (flag | SELECT_FLAG_SYMMETRIC_ANCHORED_JOIN);
      }

      if (isClosed())
      {
         if (getClosingSegment() == null)
         {
            flag = (flag | SELECT_FLAG_SYMMETRIC_ANCHORED_CLOSE);
         }

         flag = (flag & ~SELECT_FLAG_OPEN) | SELECT_FLAG_CLOSED;
      }
      else
      {
         flag = (flag & ~SELECT_FLAG_CLOSED) | SELECT_FLAG_OPEN;
      }

      return flag;
   }

   @Override
   public String info(String prefix)
   {
      JDRMessage msgSys = getCanvasGraphics().getMessageSystem();
      String eol = String.format("%n");

      StringBuilder builder = new StringBuilder();

      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
       "objectinfo.symmetric_shape", "Symmetric shape:"));

      builder.append(eol);
      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
       "objectinfo.symmetric_shape.line", "Symmetry: {0}", line_.info()));

      builder.append(eol);
      builder.append(prefix);

      if (isSingle())
      {
         builder.append(msgSys.getMessageWithFallback(
          "objectinfo.symmetric_shape.single_on", "Single"));
      }
      else
      {
         builder.append(msgSys.getMessageWithFallback(
          "objectinfo.symmetric_shape.single_off", "Not single"));
      }

      builder.append(eol);
      builder.append(prefix);

      if (isAnchored())
      {
         builder.append(msgSys.getMessageWithFallback(
          "objectinfo.symmetric_shape.anchor_on", "Anchored"));
      }
      else
      {
         builder.append(msgSys.getMessageWithFallback(
          "objectinfo.symmetric_shape.anchor_off", "Not anchored"));
      }

      builder.append(eol);
      builder.append(prefix);

      if (join == null)
      {
         builder.append(msgSys.getMessageWithFallback(
          "objectinfo.symmetric_shape.no_join", "No join"));
      }
      else
      {
         builder.append(msgSys.getMessageWithFallback(
          "objectinfo.symmetric_shape.join", "Join: {0}", join.info()));
      }

      builder.append(eol);
      builder.append(prefix);

      if (closingSegment == null)
      {
         builder.append(msgSys.getMessageWithFallback(
          "objectinfo.symmetric_shape.no_closing", "No closing segment"));
      }
      else
      {
         builder.append(msgSys.getMessageWithFallback(
          "objectinfo.symmetric_shape.closing", "Closing: {0}",
            closingSegment.info()));
      }

      builder.append(eol);
      builder.append(super.info(prefix));
      builder.append(eol);
      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
        "objectinfo.underlying", "Underlying object:")
      );

      builder.append(eol);
      builder.append(path_.info(prefix+prefix));

      return builder.toString();
   }

   protected void setSelectedElements(int segmentIndex, int controlIndex,
      JDRPathSegment segment, JDRPoint control)
   {
      path_.setSelectedElements(segmentIndex, controlIndex, segment, control);
   }

   public Object[] getDescriptionInfo()
   {
      return new Object[] {line_.getStart().info(), line_.getEnd().info()};
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      super.setCanvasGraphics(cg);

      if (line_ != null)
      {
         line_.setCanvasGraphics(cg);
      }

      if (join != null)
      {
         join.setCanvasGraphics(cg);
      }

      if (closingSegment != null)
      {
         closingSegment.setCanvasGraphics(cg);
      }
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      line_.applyCanvasGraphics(cg);

      if (join != null)
      {
         join.applyCanvasGraphics(cg);
      }

      if (closingSegment != null)
      {
         closingSegment.applyCanvasGraphics(cg);
      }

      super.applyCanvasGraphics(cg);
   }

   public JDRShape outlineToPath() throws InvalidShapeException
   {
       return new JDRSymmetricPath(
            path_.outlineToPath(),  
            join == null ? null : (JDRPartialSegment)join.clone(),
            (JDRLine)line_.clone(),
            closingSegment == null ? null : (JDRPartialSegment)closingSegment.clone(),
            closed, isSingle);
   }

   private boolean closed=false;
   private boolean isSingle=true;

   private JDRShape path_, fullPath_;
   private JDRPartialSegment join;
   private JDRLine line_;
   private JDRPartialSegment closingSegment=null;

   private static JDRPathListener symmetricPathListener = new JDRSymmetricPathListener();

   protected JDRSymmetricPathIterator iterator;
   protected JDRPointIterator pointIterator;

}

