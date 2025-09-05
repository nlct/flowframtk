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
      boolean isClosed, boolean markerSym)
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

       markerSymmetry = markerSym;

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

       markerSymmetry = symPath.markerSymmetry;
       closed = symPath.closed;
       path_ = (JDRShape)symPath.path_.clone();

       if (symPath.join != null)
       {
          join = (JDRPartialSegment)symPath.join.clone();
       }

       line_ = new JDRLine(symPath.line_);

       if (symPath.closingSegment != null)
       {
          closingSegment = (JDRPartialSegment)symPath.closingSegment.clone();
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

    public void add(JDRSegment s) throws InvalidPathException
    {
       path_.add(s);
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

       return shape;
    }

    public Object clone()
    {
       return new JDRSymmetricPath(this);
/*
       JDRSymmetricPath symPath = new JDRSymmetricPath(
            (JDRShape)path_.clone(),  
            join == null ? null : (JDRPartialSegment)join.clone(),
            (JDRLine)line_.clone(),
            closingSegment == null ? null : (JDRPartialSegment)closingSegment.clone(),
            closed, markerSymmetry);

        int segIndex = symPath.getSelectedIndex();
        JDRPathSegment selectedSeg = symPath.getSelectedSegment();

        int controlIndex = symPath.getSelectedControlIndex();
        JDRPoint selectedControl = symPath.getSelectedControl();

        if (controlIndex > -1 && selectedControl == null)
        {
           selectedControl = selectControl(controlIndex);
        }

        if (segIndex > -1 && selectedSeg == null)
        {
           JDRPathSegment seg = getSelectedSegment();

           if (seg == line_)
           {
              selectedSeg = symPath.line_;
              selectedSeg.setSelected(true);
           }
           else if (seg == join)
           {
              selectedSeg = symPath.join;
              selectedSeg.setSelected(true);
           }
           else if (seg == closingSegment)
           {
              selectedSeg = symPath.closingSegment;
              selectedSeg.setSelected(true);
           }

           symPath.setSelectedElements(segIndex, controlIndex,
             selectedSeg, selectedControl);
        }

        return symPath;
*/
    }

    public void makeEqual(JDRObject object)
    {
       super.makeEqual(object);

       JDRSymmetricPath symPath = (JDRSymmetricPath)object;

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
      return path_.addPoint();
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

      if (selectedSegmentIndex < path_.size())
      {
         return remove(selectedSegmentIndex);
      }

      return null;
   }

   public JDRPathSegment remove(JDRPathSegment segment)
   throws InvalidPathException
   {
      for (int i = 0, n = path_.size(); i < n; i++)
      {
         if (get(i) == segment)
         {
            return remove(i);
         }
      }

      return null;
   }

   public JDRSegment removeSegment(int index)
     throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return (JDRSegment)path_.remove(index);
   }

   public JDRPathSegment setSegment(int index, JDRPathSegment segment)
     throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      int n = path_.size();

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

       if (showPath())
       {
          super.draw(parentFrame);
       }
       else
       {
          CanvasGraphics cg = getCanvasGraphics();

          Graphics2D g2 = cg.getGraphics();

          if (g2 == null) return;

          AffineTransform oldAf = g2.getTransform();

          path_.draw(parentFrame);

          AffineTransform af = line_.getReflectionTransform(null);

          af.preConcatenate(oldAf);

          g2.setTransform(af);

          path_.draw(parentFrame);

          g2.setTransform(oldAf);
       }
    }

   public JDRGroup splitText()
     throws InvalidShapeException
   {
      if (showPath())
      {
         return getFullPath().getTextual().splitText();
      }

      JDRGroup group = path_.getTextual().splitText();

      AffineTransform af = line_.getReflectionTransform(null);
      double[] matrix = new double[6];
      af.getMatrix(matrix);

      for (int i = 0, n = group.size(); i < n; i++)
      {
         JDRCompleteObject object = (JDRCompleteObject)group.get(i).clone();

         object.transform(matrix);

         group.add(object);
      }

      return group;
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
    }

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

      return newPath;
    }

   public Shape getStorageStrokedPath()
   {
      Shape shape;

      if (showPath())
      {
         shape = getStroke().getStorageStrokedPath(this);
      }
      else
      {
         shape = path_.getStorageStrokedPath();

         AffineTransform af = line_.getReflectionTransform(null);

         Path2D reflectedShape = new Path2D.Double(shape);

         reflectedShape.transform(af);

         reflectedShape.append(shape, false);

         shape = reflectedShape;
      }

      return shape;
   }

   public Area getStorageStrokedArea()
   {
      Area shape;

      if (showPath())
      {
         shape = getStroke().getStorageStrokedArea(this);
      }
      else
      {
         shape = path_.getStorageStrokedArea();

         AffineTransform af = line_.getReflectionTransform(null);

         Area reflectedShape = new Area(shape);

         reflectedShape.transform(af);

         shape.add(reflectedShape);
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
   }

   public void fade(double value)
   {
      path_.fade(value);
   }

   public JDRPaint getLinePaint()
   {
      return path_.getLinePaint();
   }

   public void setLinePaint(JDRPaint paint)
   {
      path_.setLinePaint(paint);
   }

   // May be null if underlying path is a text-path
   public JDRPaint getFillPaint()
   {
      return path_.getFillPaint();
   }

   public void setFillPaint(JDRPaint paint)
   {
      path_.setFillPaint(paint);
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
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();

      JDRGroup group = new JDRGroup(cg);

      JDRShape shape = getUnderlyingShape();

      if (shape instanceof JDRTextPath)
      {
         return ((JDRTextPath)shape).separate();
      }

      group.add(shape);

      JDRPath path;

      if (join != null)
      {
         path = new JDRPath(cg);
         path.setLinePaint(getLinePaint());
         path.setFillPaint(getFillPaint());
         path.setStroke(getStroke() instanceof JDRBasicStroke ?
            (JDRStroke)getStroke().clone() : new JDRBasicStroke(cg));

         if (join.isGap())
         {
            path.add(new JDRLine(join.getStart(), join.getEnd()));
         }
         else
         {
            path.add(join.getFullSegment());
         }

         group.add(path);
      }

      JDRShape reflectedShape = shape.reflection(line_);

      group.add(reflectedShape);

      if (closingSegment != null)
      {
         path = new JDRPath(cg);
         path.setLinePaint(getLinePaint());
         path.setFillPaint(getFillPaint());
         path.setStroke(getStroke() instanceof JDRBasicStroke ?
            (JDRStroke)getStroke().clone() : new JDRBasicStroke(cg));

         path.add(closingSegment.getFullSegment());

         group.add(path);
      }

      path = new JDRPath(cg);
      path.setLinePaint(getLinePaint());
      path.setFillPaint(getFillPaint());
      path.setStroke(new JDRBasicStroke(cg));
      path.add(line_);

      group.add(path);

      return group;
   }

   /**
    * Gets this path as a Path2D.
    * @return this path as a Path2D
    */
   public Path2D getGeneralPath()
   {
      if (isEmpty()) return null;

      JDRStroke stroke = getStroke();

      Path2D reflectedPath = new Path2D.Double();

      Path2D path = new Path2D.Double();

      if (stroke != null)
      {
         path.setWindingRule(stroke.getWindingRule());
         reflectedPath.setWindingRule(stroke.getWindingRule());
      }

      boolean closeSubPath = isClosed() && 
               ((join != null && join.isGap()) || 
                (closingSegment != null && closingSegment.isGap()));

      int n = path_.size();

      JDRPathSegment segment = path_.getFirstSegment();

      path.moveTo(segment.getStart().x, segment.getStart().y);
      segment.appendToGeneralPath(path);

      segment = getReflected(n-1).reverse();

      reflectedPath.moveTo(segment.getStart().x, segment.getStart().y);
      segment.appendToGeneralPath(reflectedPath);

      boolean done = false;

      boolean appendWithLine = true;

      for (int i = 1; i < n; i++)
      {
         segment = path_.get(i);
         segment.appendToGeneralPath(path);

         appendWithLine = !segment.isGap();

         segment = getReflected(n-i-1).reverse();

         if (segment.isGap() && closeSubPath && !done)
         {
            reflectedPath.closePath();
            done = true;
         }

         segment.appendToGeneralPath(reflectedPath);
      }

      if (closeSubPath)
      {
         path.closePath();

         JDRPoint p = path_.getLastSegment().getEnd();
         path.moveTo(p.getX(), p.getY());
      }

      if (join != null)
      {
         join.appendToGeneralPath(path);

         appendWithLine = !join.isGap();
      }

      if (closeSubPath && !done)
      {
         reflectedPath.closePath();
      }

      path.append(reflectedPath, appendWithLine);

      if (closingSegment != null)
      {
         closingSegment.appendToGeneralPath(path);
      }

      if (isClosed() && !closeSubPath)
      {
         path.closePath();
      }

      return path;
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
        closed, markerSymmetry);
   }

   public JDRShape reverse() throws InvalidShapeException
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRShape newShape = path_.reverse();

      JDRLine newSymLine = line_.reverse();

      JDRPartialSegment newJoin = null;
      JDRPartialSegment newClosing = null;

      if (join != null)
      {
         newClosing = (JDRPartialSegment)join.reverse();
      }

      if (closingSegment != null)
      {
         newJoin = (JDRPartialSegment)closingSegment.reverse();
      }

      return new JDRSymmetricPath(newShape, newJoin, newSymLine, newClosing,
        closed, markerSymmetry);
   }

   public JDRShape intersect(JDRShape shape)
      throws InvalidShapeException
   {
      return getFullPath().intersect(shape);
   }

   public JDRShape pathUnion(JDRShape shape)
      throws InvalidShapeException
   {
      return getFullPath().pathUnion(shape);
   }

   public JDRShape exclusiveOr(JDRShape shape)
      throws InvalidShapeException
   {
      return getFullPath().exclusiveOr(shape);
   }

   public JDRShape subtract(JDRShape shape)
      throws InvalidShapeException
   {
      return getFullPath().subtract(shape);
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
                 + ", marker symmetry="+markerSymmetry
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
    }

    public JDRPartialSegment getJoin()
    {
       return join;
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
    }

    /**
     * Gets the line of symmetry.
     * @return line of symmetry
     */ 
    public JDRLine getSymmetry()
    {
       return line_;
    }

    /**
     * Sets the marker symmetry flag. If true, the markers are
     * reflected with the path, otherwise the markers follow the
     * entire path.
     * @param flag if true, markers are reflected with the path
     */
    public void setMarkerSymmetry(boolean flag)
    {
       markerSymmetry = flag;
    }

    /**
     * Gets the marker symmetry.
     * @return true if the markers are reflected with the path,
     * otherwise the markers follow the entire path.
     */
    public boolean hasMarkerSymmetry()
    {
       return markerSymmetry;
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
       JDRPaint paint = getFillPaint();

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
      if (isEmpty()) return;

      JDRPathSegment segment = get(0);

      svg.print("   <path "+ attr+" d=\"M ");

      segment.getStart().saveSVG(svg);
      segment.saveSVG(svg);

      JDRPathIterator pi = getIterator();

      while (pi.hasNext())
      {
         segment = pi.next();
         segment.saveSVG(svg);
      }

      if (isClosed()) svg.print("z");

      svg.println("\"");
      svg.println("      "+getLinePaint().svgLine());
      svg.println("      "+getFillPaint().svgFill());

      if (getStroke() instanceof JDRBasicStroke)
      {
         svg.println("      "
            +((JDRBasicStroke)getStroke()).svg(getLinePaint()));
      }

      svg.println("   />");
   } 

   public void savePgf(TeX tex)
    throws IOException
   {
      BBox pathBBox = getStorageBBox();

      if (pathBBox == null)
      {
         return;
      }

      tex.println("\\begin{pgfscope}");

      JDRStroke stroke = getStroke();

      JDRPaint linePaint = getLinePaint();

      if (stroke instanceof JDRBasicStroke)
      {
         JDRPaint paint = getFillPaint();

         ((JDRBasicStroke)getStroke()).savePgf(tex);

         if (linePaint instanceof JDRShading)
         {
            String msg = getCanvasGraphics().warning(
               "warning.pgf-no-stroke-shading",
               "stroke shading paint can't be exported to pgf");

            tex.comment(msg);
         }

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

            if (getStroke() instanceof JDRBasicStroke)
            {
               tex.println(((JDRBasicStroke)getStroke()).windingRule
                          == Path2D.WIND_EVEN_ODD ? 
                      "\\pgfseteorule" :
                      "\\pgfsetnonzerorule");
            }

            if (paint instanceof JDRGradient
             || paint instanceof JDRRadial)
            {
               tex.println(linePaint.pgfstrokecolor(pathBBox));

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
      else
      {
         // Text path

         JDRTextPathStroke textPathStroke = (JDRTextPathStroke)stroke;

         textPathStroke.savePgf(tex, linePaint, this);

         AffineTransform af = line_.getReflectionTransform(null);

         tex.comment("Reflection");
         tex.println("\\pgflowlevel{"
                    +tex.transform(getCanvasGraphics(), af)+"}");

         textPathStroke.savePgf(tex, linePaint, this);
      }

      tex.println("\\end{pgfscope}");

      pgfMarkers(tex, pathBBox);
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

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "SymmetricPath:"+eol;

      str += "line of symmetry: "+line_.info()+eol;

      str += "anchored: "+isAnchored()+eol;

      if (join != null)
      {
         str += "join segment: "+join.info();
      }

      if (closingSegment != null)
      {
         str += "closing segment: "+closingSegment.info();
      }

      str += "Underlying shape:"+path_.info();

      return str;
   }

   protected void setSelectedElements(int segmentIndex, int controlIndex,
      JDRPathSegment segment, JDRPoint control)
   {
      path_.setSelectedElements(segmentIndex, controlIndex, segment, control);
   }

   public Object[] getDescriptionInfo()
   {
      return getUnderlyingShape().getDescriptionInfo();
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
            closed, markerSymmetry);
   }

   private boolean markerSymmetry=true;

   private boolean closed=false;

   private JDRShape path_;
   private JDRPartialSegment join;
   private JDRLine line_;
   private JDRPartialSegment closingSegment=null;

   private static JDRPathListener symmetricPathListener = new JDRSymmetricPathListener();

   protected JDRSymmetricPathIterator iterator;
   protected JDRPointIterator pointIterator;

}

