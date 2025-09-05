// File          : JDRPattern.java
// Creation Date : 13th Sept 2010
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

import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdr.exceptions.*;

public abstract class JDRPattern extends JDRCompoundShape
{
   public JDRPattern(CanvasGraphics cg)
   {
      super(cg);
      initIterators();
   }

   public JDRPattern(JDRPattern pattern)
   {
      super(pattern);

      if (pattern.adjust_ != null)
      {
         adjust_ = (JDRPoint)pattern.adjust_.clone();
      }

      path_ = (JDRShape)pattern.path_.clone();
      point_ = (JDRPoint)pattern.point_.clone();

      showoriginal_ = pattern.showoriginal_;
      singlemode_ = pattern.singlemode_;
      replicas_ = pattern.replicas_;

      initIterators();
   }

   protected void initIterators()
   {
      iterator = new JDRPatternIterator(this);
      pointIterator = new JDRPatternPointIterator(this);
   }

   public boolean hasPattern()
   {
      return true;
   }

   public JDRPattern getPattern()
   {
      return this;
   }

   public int getObjectFlag()
   {
      return super.getObjectFlag() | SELECT_FLAG_PATTERN;
   }

   public void setSelected(boolean flag)
   {
      if (path_ != null)
      {
         path_.setSelected(flag);
      }
   }

   public boolean isSelected()
   {
      return path_ == null ? false : path_.isSelected();
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
      return path_.getLastSegment();
   }

   public JDRPoint getFirstControl()
   {
      return path_.getFirstControl();
   }

   public JDRPoint getLastControl()
   {
      return point_;
   }

   public JDRShape getUnderlyingShape()
   {
      return path_;
   }

   public void setUnderlyingShape(JDRShape shape)
   {
      path_ = shape;
   }

   public void add(JDRSegment s) throws InvalidPathException
   {
      path_.add(s);
   }

   /**
    * Returns number of replicas. (Doesn't include original shape.)
    * @see #setNumReplicas(int)
    */
   public int getNumReplicas()
   {
      return replicas_;
   }

   /**
    * Sets the number of replicas. (Doesn't include original shape.)
    * @see #getNumReplicas()
    */
   public void setNumReplicas(int n)
   {
      if (n < 1)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.PATTERN_REPLICAS, n,
            getCanvasGraphics());
      }

      replicas_ = n;
   }

   /**
    * Gets the transformation matrix for the given replica.
    * On exit, the matrix will contain the flat matrix that will
    * transform the underlying shape to the specified replica
    * @param matrix flat matrix in which to store the transformation
    * @param replicaIndex the index for the required replica
    */
   public abstract void getReplicaTransform(double[] matrix, int replicaIndex);

   public JDRGroup separate()
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRGroup group = new JDRGroup(cg);

      group.add((JDRShape)path_.clone());

      double[] matrix = new double[6];

      for (int i = 1; i <= replicas_; i++)
      {
         JDRShape shape = (JDRShape)path_.clone();

         getReplicaTransform(matrix, i);

         shape.transform(matrix);

         group.add(shape);
      }

      return group;
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

   public BBox getStorageControlBBox()
   {
      BBox bbox = path_.getStorageControlBBox();

      if (bbox == null) return null;

      Path2D p = getReplicasGeneralPath();

      bbox.merge(p.getBounds2D());

      point_.mergeStorageBBox(bbox);

      if (adjust_ != null)
      {
         adjust_.mergeStorageBBox(bbox);
      }

      return bbox;
   }

   public void mergeStorageControlBBox(BBox box)
   {
      path_.mergeStorageControlBBox(box);

      Path2D p = getReplicasGeneralPath();

      box.merge(p.getBounds2D());

      point_.mergeStorageBBox(box);

      if (adjust_ != null)
      {
         adjust_.mergeStorageBBox(box);
      }
   }


   public void convertSegment(int idx, JDRPathSegment segment)
   throws InvalidPathException
   {
      path_.convertSegment(idx, segment);
   }

   public void shearAnchor(double factorX, double factorY)
   {
      point_.shear(factorX, factorY);

      shearAdjust(factorX, factorY);
   }

   public void shearAdjust(double factorX, double factorY)
   {
      if (adjust_ != null)
      {
         adjust_.shear(factorX, factorY);
      }
   }

   public void shearAnchor(Point2D p, double factorX, double factorY)
   {
      point_.shear(p, factorX, factorY);

      shearAdjust(p, factorX, factorY);
   }

   public void shearAdjust(Point2D p, double factorX, double factorY)
   {
      if (adjust_ != null)
      {
         adjust_.shear(p, factorX, factorY);
      }
   }

   public void shearParams(Point2D p, double factorX, double factorY)
   {
      shearAnchor(p, factorX, factorY);
   }

   public void scaleAnchor(double factorX, double factorY)
   {
      point_.scale(factorX, factorY);

      scaleAdjust(factorX, factorY);
   }

   public void scaleAdjust(double factorX, double factorY)
   {
      if (adjust_ != null)
      {
         adjust_.scale(factorX, factorY);
      }
   }

   public void scaleAnchor(Point2D p, double factorX, double factorY)
   {
      point_.scale(p, factorX, factorY);

      scaleAdjust(p, factorX, factorY);
   }

   public void scaleAdjust(Point2D p, double factorX, double factorY)
   {
      if (adjust_ != null)
      {
         adjust_.scale(p, factorX, factorY);
      }
   }

   public void scaleParams(Point2D p, double factorX, double factorY)
   {
      scaleAnchor(p, factorX, factorY);
   }

   public void rotateAnchor(double rotAngle)
   {
      point_.rotate(rotAngle);

      rotateAdjust(rotAngle);
   }

   public void rotateAdjust(double rotAngle)
   {
      if (adjust_ != null)
      {
         adjust_.rotate(rotAngle);
      }
   }

   public void rotateAnchor(Point2D p, double rotAngle)
   {
      point_.rotate(p, rotAngle);

      rotateAdjust(p, rotAngle);
   }

   public void rotateAdjust(Point2D p, double rotAngle)
   {
      if (adjust_ != null)
      {
         adjust_.rotate(p, rotAngle);
      }
   }

   public void rotateParams(Point2D p, double rotAngle)
   {
      rotateAnchor(p, rotAngle);
   }

   public void translateAnchor(double x, double y)
   {
      point_.translate(x, y);
   }

   public void translateAdjust(double x, double y)
   {
      if (adjust_ != null)
      {
         adjust_.translate(x, y);
      }
   }

   public void translateParams(double x, double y)
   {
      translateAnchor(x, y);
      translateAdjust(x, y);
   }

   public void translateControl(JDRPathSegment segment, JDRPoint p, 
     double x, double y)
   {
      if (p == point_)
      {
         translateAnchor(x, y);
      }
      else if (p == adjust_)
      {
         translateAdjust(x, y);
      }
      else
      {
         path_.translateControl(segment, p, x, y);
      }
   }

   public void transformAnchor(double[] matrix)
   {
      point_.transform(matrix);

      transformAdjust(matrix);
   }

   public void transformAdjust(double[] matrix)
   {
      if (adjust_ != null)
      {
         adjust_.transform(matrix);
      }
   }

   public void transformParams(double[] matrix)
   {
      transformAnchor(matrix);
   }

   public boolean isEmpty()
   {
      return size() == 0;
   }

   public boolean isClosed()
   {
      return path_.isClosed();
   }

   public void open()
   throws InvalidPathException
   {
      path_.open();
   }

   public void open(boolean removeLastSegment)
   throws InvalidPathException
   {
      path_.open(removeLastSegment);
   }

   public void close(JDRPathSegment segment)
     throws InvalidPathException,IllFittingPathException
   {
      path_.close(segment);
   }

   public Shape getStorageStrokedPath()
   {
      return getStorageStrokedPath(showoriginal_);
   }

   public Shape getStorageStrokedPath(boolean addOriginal)
   {
      if (singlemode_)
      {
         return super.getStorageStrokedPath();
      }

      Shape shape = path_.getStorageStrokedPath();

      double[] matrix = new double[6];

      AffineTransform af = new AffineTransform();

      Path2D fullPath = null;

      if (addOriginal)
      {
         fullPath = new Path2D.Double(shape);
      }

      for (int i = 1; i <= replicas_; i++)
      {
         Path2D subPath = new Path2D.Double(shape);

         getReplicaTransform(matrix, i);

         af.setTransform(matrix[0], matrix[1], matrix[2],
                         matrix[3], matrix[4], matrix[5]);

         subPath.transform(af);

         if (fullPath == null)
         {
            fullPath = subPath;
         }
         else
         {
            fullPath.append(subPath, false);
         }
      }

      return fullPath;
   }

   public Area getStorageStrokedArea()
   {
      return getStorageStrokedArea(showoriginal_);
   }

   public Area getStorageStrokedArea(boolean addOriginal)
   {
      if (singlemode_)
      {
         return super.getStorageStrokedArea();
      }

      Area shape;

      shape = path_.getStorageStrokedArea();

      double[] matrix = new double[6];

      AffineTransform af = new AffineTransform();

      Area fullArea = null;

      if (addOriginal)
      {
         fullArea = new Area(shape);
      }

      for (int i = 1; i <= replicas_; i++)
      {
         Area subPath = new Area(shape);

         getReplicaTransform(matrix, i);

         af.setTransform(matrix[0], matrix[1], matrix[2],
                         matrix[3], matrix[4], matrix[5]);

         subPath.transform(af);

         if (fullArea == null)
         {
            fullArea = subPath;
         }
         else
         {
            fullArea.add(subPath);
         }
      }

      return fullArea;
   }

   public Shape getBpStrokedPath()
   {
      return getBpStrokedPath(showoriginal_);
   }

   public Shape getBpStrokedPath(boolean addOriginal)
   {
      if (singlemode_)
      {
         return super.getBpStrokedPath();
      }

      Shape shape = path_.getBpStrokedPath();

      double[] matrix = new double[6];

      AffineTransform af = new AffineTransform();

      Path2D fullPath = null;

      if (addOriginal)
      {
         fullPath = new Path2D.Double(shape);
      }

      for (int i = 1; i <= replicas_; i++)
      {
         Path2D subPath = new Path2D.Double(shape);

         getReplicaTransform(matrix, i);

         af.setTransform(matrix[0], matrix[1], matrix[2],
                         matrix[3], matrix[4], matrix[5]);

         subPath.transform(af);

         if (fullPath == null)
         {
            fullPath = subPath;
         }
         else
         {
            fullPath.append(subPath, false);
         }
      }

      return fullPath;
   }

   public Area getBpStrokedArea()
   {
      return getBpStrokedArea(showoriginal_);
   }

   public Area getBpStrokedArea(boolean addOriginal)
   {
      if (singlemode_)
      {
         return super.getBpStrokedArea();
      }

      Area shape;

      shape = path_.getBpStrokedArea();

      double[] matrix = new double[6];

      AffineTransform af = new AffineTransform();

      Area fullArea = null;

      if (addOriginal)
      {
         fullArea = new Area(shape);
      }

      for (int i = 1; i <= replicas_; i++)
      {
         Area subPath = new Area(shape);

         getReplicaTransform(matrix, i);

         af.setTransform(matrix[0], matrix[1], matrix[2],
                         matrix[3], matrix[4], matrix[5]);

         subPath.transform(af);

         if (fullArea == null)
         {
            fullArea = subPath;
         }
         else
         {
            fullArea.add(subPath);
         }
      }

      return fullArea;
   }

   public Shape getComponentStrokedPath()
   {
      return getComponentStrokedPath(showoriginal_);
   }

   public Shape getComponentStrokedPath(boolean addOriginal)
   {
      if (singlemode_)
      {
         return super.getComponentStrokedPath();
      }

      Shape shape = path_.getComponentStrokedPath();

      double[] matrix = new double[6];

      AffineTransform af = new AffineTransform();

      Path2D fullPath = null;

      if (addOriginal)
      {
         fullPath = new Path2D.Double(shape);
      }

      for (int i = 1; i <= replicas_; i++)
      {
         Path2D subPath = new Path2D.Double(shape);

         getReplicaTransform(matrix, i);

         af.setTransform(matrix[0], matrix[1], matrix[2],
                         matrix[3], matrix[4], matrix[5]);

         subPath.transform(af);

         if (fullPath == null)
         {
            fullPath = subPath;
         }
         else
         {
            fullPath.append(subPath, false);
         }
      }

      return fullPath;
   }

   public Area getComponentStrokedArea()
   {
      return getComponentStrokedArea(showoriginal_);
   }

   public Area getComponentStrokedArea(boolean addOriginal)
   {
      if (singlemode_)
      {
         return super.getComponentStrokedArea();
      }

      Area shape;

      shape = path_.getComponentStrokedArea();

      double[] matrix = new double[6];

      AffineTransform af = new AffineTransform();

      Area fullArea = null;

      if (addOriginal)
      {
         fullArea = new Area(shape);
      }

      for (int i = 1; i <= replicas_; i++)
      {
         Area subPath = new Area(shape);

         getReplicaTransform(matrix, i);

         af.setTransform(matrix[0], matrix[1], matrix[2],
                         matrix[3], matrix[4], matrix[5]);

         subPath.transform(af);

         if (fullArea == null)
         {
            fullArea = subPath;
         }
         else
         {
            fullArea.add(subPath);
         }
      }

      return fullArea;
   }

   public JDRStroke getStroke()
   {
      return path_.getStroke();
   }

   public void setStroke(JDRStroke stroke)
   {
      path_.setStroke(stroke);
   }

   public JDRPaint getLinePaint()
   {
      return path_.getLinePaint();
   }

   public void setLinePaint(JDRPaint paint)
   {
      path_.setLinePaint(paint);
   }

   public JDRPaint getFillPaint()
   {
      return path_.getFillPaint();
   }

   public void setFillPaint(JDRPaint paint)
   {
      path_.setFillPaint(paint);
   }

   public Path2D getReplicasGeneralPath()
   {
      if (isEmpty()) return null;

      Path2D path = path_.getGeneralPath();

      Path2D replicaPath = null;

      double[] matrix = new double[6];

      AffineTransform af = new AffineTransform();

      for (int i = 1; i <= replicas_; i++)
      {
         Path2D subPath = (Path2D)path.clone();

         getReplicaTransform(matrix, i);

         af.setTransform(matrix[0], matrix[1], matrix[2],
                         matrix[3], matrix[4], matrix[5]);

         subPath.transform(af);

         if (replicaPath == null)
         {
            replicaPath = subPath;
         }
         else
         {
            replicaPath.append(subPath, false);
         }
      }

      return replicaPath;
   }

   public Path2D getGeneralPath()
   {
      return getGeneralPath(showoriginal_);
   }

   public Path2D getGeneralPath(boolean addOriginalPath)
   {
      if (isEmpty()) return null;

      Path2D shape = path_.getGeneralPath();

      Path2D fullPath = null;

      if (addOriginalPath)
      {
         fullPath = (Path2D)shape.clone();
      }

      double[] matrix = new double[6];

      AffineTransform af = new AffineTransform();

      for (int i = 1; i <= replicas_; i++)
      {
         Path2D subPath = (Path2D)shape.clone();

         getReplicaTransform(matrix, i);

         af.setTransform(matrix[0], matrix[1], matrix[2],
                         matrix[3], matrix[4], matrix[5]);

         subPath.transform(af);

         if (fullPath == null)
         {
            fullPath = subPath;
         }
         else
         {
            fullPath.append(subPath, false);
         }
      }

      return fullPath;
   }

   public void makeEqual(JDRObject object)
   {
      super.makeEqual(object);

      JDRPattern pattern = (JDRPattern)object;

      if (pattern.path_ == null)
      {
         path_ = null;
      }
      else if (path_ == null ||
         !path_.getClass().equals(pattern.path_.getClass()))
      {
         path_ = (JDRShape)pattern.path_.clone();
      }
      else
      {
         path_.makeEqual(pattern.path_);
      }

      if (pattern.point_ == null)
      {
         point_ = null;
      }
      else if (point_ == null)
      {
         point_ = (JDRPatternAnchorPoint)pattern.point_.clone();
      }
      else
      {
         point_.makeEqual(pattern.point_);
      }

      replicas_ = pattern.replicas_;

      singlemode_ = pattern.singlemode_;
      showoriginal_ = pattern.showoriginal_;

      if (pattern.adjust_ == null)
      {
         adjust_ = null;
      }
      else if (adjust_ == null)
      {
         adjust_ = (JDRPatternAdjustPoint)pattern.adjust_.clone();
      }
      else
      {
         adjust_.makeEqual(pattern.adjust_);
      }

      makeParametersEqual(pattern);
   }

    public Object clone()
    {
       JDRPattern p = createTemplate();

       if (path_ != null)
       {
          p.path_ = (JDRShape)path_.clone();
       }

       if (point_ != null)
       {
          p.point_ = (JDRPatternAnchorPoint)point_.clone();
       }

       if (adjust_ != null)
       {
          p.adjust_ = (JDRPatternAdjustPoint)adjust_.clone();
       }

       p.singlemode_ = singlemode_;
       p.showoriginal_ = showoriginal_;

       return p;
    }

   /**
    * Creates a pattern using this as a template. The resulting
    * pattern has null for the underlying shape and anchor point, 
    * but all the other parameters as the same as this.
    */
   public abstract JDRPattern createTemplate();

   /**
    * Makes pattern parameters for this the same as those for the
    * given pattern. (Doesn't include general pattern parameters:
    * anchor point, single mode, replicas or underlying shape)
    */
   public abstract void makeParametersEqual(JDRPattern pattern);

   public JDRShape reverse()
       throws InvalidShapeException
   {
      JDRShape shape = path_.reverse();

      JDRPattern pattern = createTemplate();

      pattern.path_  = shape;
      pattern.point_ = (JDRPatternAnchorPoint)point_.clone();

      if (adjust_ != null)
      {
         pattern.adjust_ = (JDRPatternAdjustPoint)adjust_.clone();
      }

      return pattern;
   }

   public JDRShape toPolygon(double flatness)
     throws InvalidShapeException
   {
      JDRShape shape = path_.toPolygon(flatness);

      JDRPattern pattern = createTemplate();

      pattern.path_  = shape;
      pattern.point_ = (JDRPatternAnchorPoint)point_.clone();

      if (adjust_ != null)
      {
         pattern.adjust_ = (JDRPatternAdjustPoint)adjust_.clone();
      }

      return pattern;
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
      return path_.hasSymmetricPath();
   }

   public JDRSymmetricPath getSymmetricPath()
   {
      return path_.getSymmetricPath();
   }

   /**
    * Gets the anchor point. The anchor is the point of origin for
    * the pattern transformation.
    * @see #setPatternAnchor(JDRPoint)
    */
   public JDRPoint getPatternAnchor()
   {
      return point_;
   }

   /**
    * Gets the offset point. For some types of patterns this is
    * always null.
    */
   public JDRPoint getPatternAdjust()
   {
      return adjust_;
   }


   /**
    * Sets the anchor point.
    * @see #setPatternAnchor(JDRPoint)
    */
   public void setPatternAnchor(JDRPoint point)
   {
      if (point == null || point instanceof JDRPatternAnchorPoint)
      {
         point_ = point;
      }
      else
      {
         point_ = new JDRPatternAnchorPoint(getCanvasGraphics(), 
                  point.x, point.y);
      }
   }

   /**
    * Sets the anchor point.
    * @see #setPatternAnchor(JDRPoint)
    */
   public void setPatternAnchor(double x, double y)
   {
      if (point_ == null)
      {
         point_ = new JDRPatternAnchorPoint(getCanvasGraphics(), x, y);
      }
      else
      {
         point_.x = x;
         point_.y = y;
      }
   }

   /**
    * Sets the offset point.
    * @see #setPatternAdjust(JDRPoint)
    */
   public void setPatternAdjust(JDRPoint point)
   {
      if (point == null || point instanceof JDRPatternAdjustPoint)
      {
         adjust_ = point;
      }
      else
      {
         adjust_ = new JDRPatternAdjustPoint(getCanvasGraphics(),
                   point.x, point.y);
      }
   }

   /**
    * Sets the offset point.
    * @see #setPatternAdjust(JDRPoint)
    */
   public void setPatternAdjust(double x, double y)
   {
      if (adjust_ == null)
      {
         adjust_ = new JDRPatternAdjustPoint(getCanvasGraphics(),x, y);
      }
      else
      {
         adjust_.x = x;
         adjust_.y = y;
      }
   }

   public boolean hasAdjust()
   {
      return false;
   }

   /**
    * Sets the default offset if this pattern has an offset.
    */
   public void setDefaultPatternAdjust()
   {
      adjust_ = null;

      if (!hasAdjust())
      {
         return;
      }

      if (point_ == null || path_ == null)
      {
         return;
      }

      JDRPoint firstPt = path_.getFirstControl();

      double[] matrix = new double[6];

      getReplicaTransform(matrix, 1);

      adjust_ = new JDRPatternAdjustPoint
               (getCanvasGraphics(),
                 matrix[0]*firstPt.x + matrix[2]*firstPt.y + matrix[4],
                 matrix[1]*firstPt.x + matrix[3]*firstPt.y + matrix[5]
               );
   }


   /**
    * Sets single path mode. If true, the pattern will be drawn as a
    * single path with moves between each replica. If false, the
    * original pattern and the replicas will be drawn independently
    * of each other.
    */
   public void setSinglePath(boolean singleMode)
   {
      singlemode_ = singleMode;
   }

   /**
    * Gets single path mode state.
    * @see #setSinglePath(boolean)
    */
   public boolean isSinglePath()
   {
      return singlemode_;
   }

   /**
    * Sets whether to show the original path when drawing. If false, 
    * only the replicas are drawn.
    */
   public void setShowOriginal(boolean showOriginal)
   {
      showoriginal_ = showOriginal;
   }

   public boolean showOriginal()
   {
      return showoriginal_;
   }

   public JDRGroup splitText()
     throws InvalidShapeException
   {
      if (singlemode_)
      {
         return getFullPath().getTextual().splitText();
      }

      JDRGroup org;

      if (path_ instanceof JDRCompoundShape)
      {
         org = ((JDRCompoundShape)path_).splitText();
      }
      else
      {
         // this shouldn't happen
         org = path_.getTextual().splitText();
      }

      JDRGroup group = new JDRGroup(getCanvasGraphics(), org.size());
      if (showoriginal_)
      {
         for (int i = 0; i < org.size(); i++)
         {
            group.add(org.get(i));
         }
      }

      double[] matrix = new double[6];

      // replicas

      for (int i = 1; i <= replicas_; i++)
      {
         getReplicaTransform(matrix, i);

         JDRGroup g = (JDRGroup)org.clone();
         g.transform(matrix);
         group.add(g);
      }

      group.setSelected(isSelected());
      return group;
   }

   public void draw(FlowFrame parentFrame)
   {
      if (parentFrame == null)
      {
         parentFrame = flowframe;
      }

      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();

      if (g2 == null) return;

      if (singlemode_)
      {
         super.draw(parentFrame);
         return;
      }

      // multipath mode

      // Draw original

      if (showoriginal_)
      {
         path_.draw(parentFrame);
      }

      AffineTransform oldAf = g2.getTransform();

      double[] matrix = new double[6];

      AffineTransform af = new AffineTransform();

      // Draw replicas

      for (int i = 1; i <= replicas_; i++)
      {
         g2.setTransform(oldAf);

         getReplicaTransform(matrix, i);

         af.setTransform(matrix[0], matrix[1], matrix[2],
                         matrix[3], matrix[4], matrix[5]);

         g2.transform(af);
            
         path_.draw(parentFrame);
      }

      g2.setTransform(oldAf);
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

      AffineTransform orgTransform = g2.getTransform();

      if (doShift)
      {
         g2.translate(cg.storageToComponentX(xshift), 
            cg.storageToComponentY(parentFrame.getEvenYShift()));
      }

      double[] matrix = new double[6];
      AffineTransform af = new AffineTransform();

      double storageToCompX = cg.storageToComponentX(1.0);
      double storageToCompY = cg.storageToComponentY(1.0);

      Path2D orgPath = path_.getGeneralPath();

      // draw replicas first so they don't obscure control points

      g2.setPaint(draftColor);

      for (int i = 1; i <= replicas_; i++)
      {
         getReplicaTransform(matrix, i);

         af.setTransform(storageToCompX*matrix[0], storageToCompY*matrix[1],
                         storageToCompX*matrix[2], storageToCompY*matrix[3],
                         storageToCompX*matrix[4], storageToCompY*matrix[5]);

         Shape p = af.createTransformedShape(orgPath);

         g2.draw(p);
      }

      g2.setPaint(draftColor);

      af.setToIdentity();
      af.scale(storageToCompX, storageToCompY);
      g2.draw(af.createTransformedShape(orgPath));

      JDRPathSegment segment = getSelectedSegment();

      if (segment != null)
      {
         segment.drawSelectedNoControls();
      }

      for (int i = 0, n = path_.size(); i < n; i++)
      {
         segment = get(i);
         segment.drawControls(segmentHasEnd(segment));
      }

      point_.draw();

      if (adjust_ != null)
      {
         Stroke oldStroke = g2.getStroke();

         g2.setPaint(adjust_.isSelected() ? 
                     adjust_.getSelectedPaint() :
                     adjust_.getUnselectedPaint());

         g2.setStroke(JDRSegment.guideStroke);

         cg.drawMagLine(point_.x, point_.y,
                     adjust_.x, adjust_.y);

         g2.setStroke(oldStroke);

         adjust_.draw();
      }

      g2.setTransform(orgTransform);
   }

   public void print(Graphics2D g2)
   {
      if (singlemode_)
      {
         super.print(g2);
         return;
      }

      // multipath mode

      // Draw original

      if (showoriginal_)
      {
         path_.print(g2);
      }

      AffineTransform oldAf = g2.getTransform();

      double[] matrix = new double[6];

      AffineTransform af = new AffineTransform();

      // Draw replicas

      for (int i = 1; i <= replicas_; i++)
      {
         g2.setTransform(oldAf);

         getReplicaTransform(matrix, i);

         af.setTransform(matrix[0], matrix[1], matrix[2],
                         matrix[3], matrix[4], matrix[5]);

         g2.transform(af);
         
         path_.print(g2);
      }

      g2.setTransform(oldAf);
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
      path_.makeContinuous(atStart, equiDistant);
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

   public JDRPoint selectPreviousControl()
   {
      int totalSegments = path_.size();

      int selectedIndex = getSelectedIndex();

      JDRPoint control = getSelectedControl();

      if (control == point_)
      {
         if (adjust_ != null)
         {
            selectControl(adjust_, getSelectedControlIndex()-1,
                          totalSegments);

            return adjust_;
         }
         else
         {
            selectControl(path_.getFirstControl(), 0, 0);

            return path_.selectPreviousControl();
         }
      }

      if (adjust_ != null && control == adjust_)
      {
         selectControl(path_.getFirstControl(), 0, 0);

         return path_.selectPreviousControl();
      }

      return super.selectPreviousControl();
   }

   protected void setSelectedElements(int segmentIndex, int controlIndex,
    JDRPathSegment segment, JDRPoint control)
   {
      path_.setSelectedElements(segmentIndex, controlIndex, segment, control);
   }

   public void selectControl(JDRPoint p, int pointIndex, int segmentIndex)
   {
      if (p == point_)
      {
         stopEditing();

         setSelectedElements(segmentIndex, pointIndex, null, point_);
         point_.setSelected(true);

         if (adjust_ != null)
         {
            adjust_.setSelected(false);
         }
      }
      else if (adjust_ != null && p == adjust_)
      {
         stopEditing();

         setSelectedElements(segmentIndex, pointIndex, null, adjust_);
         adjust_.setSelected(true);
         point_.setSelected(false);
      }
      else
      {
         point_.setSelected(false);

         if (adjust_ != null)
         {
            adjust_.setSelected(false);
         }

         path_.selectControl(p, pointIndex, segmentIndex);
      }

      editMode = true;
      selected = true;
   }

   public int getSelectedControlIndex()
   {
      return path_.getSelectedControlIndex();
   }

   public JDRPathSegment removeSelectedSegment()
   throws InvalidPathException
   {
      return path_.removeSelectedSegment();
   }

   public JDRPathSegment remove(JDRPathSegment segment)
   throws InvalidPathException
   {
      return path_.remove(segment);
   }

   public JDRSegment removeSegment(int index)
     throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return (JDRSegment)path_.remove(index);
   }

   public JDRPathSegment setSegment(int index, JDRPathSegment segment)
     throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return path_.setSegment(index, segment);
   }

   public JDRPathSegment remove(int i) throws InvalidPathException
   {
      return path_.remove(i);
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

   public boolean segmentHasEnd(JDRPathSegment segment)
   {
      return path_.segmentHasEnd(segment);
   }

   public int size()
   {
      return path_.size();
   }

   public int getTotalPathSegments()
   {
      // Include gap between replicas

      return (replicas_+1)*path_.size() + replicas_;
   }

   public JDRPathSegment get(int index)
     throws ArrayIndexOutOfBoundsException
   {
      int totalSegments = getTotalPathSegments();

      if (index >= totalSegments)
      {
         throw new ArrayIndexOutOfBoundsException(index);
      }

      if (index < path_.size())
      {
         return path_.get(index);
      }

      int size = path_.size();

      JDRPathSegment segment = null;

      int subIndex = index%(size+1);

      int replicaIndex = index/(size+1);

      double[] matrix = new double[6];

      if (subIndex == size)
      {
         JDRPathSegment lastSegment = path_.getLastSegment();
         JDRPathSegment nextSegment = path_.getFirstSegment();

         JDRPoint startPt = lastSegment.getEnd();

         if (!(lastSegment instanceof JDRPartialSegment))
         {
            startPt = (JDRPoint)startPt.clone();
         }

         if (replicaIndex > 0)
         {
            getReplicaTransform(matrix, replicaIndex);
            startPt.transform(matrix);
         }

         JDRPoint endPt = (JDRPoint)nextSegment.getStart().clone();

         getReplicaTransform(matrix, replicaIndex+1);

         endPt.transform(matrix);

         segment = new JDRSegment(startPt, endPt);
      }
      else
      {
         segment = (JDRPathSegment) path_.get(subIndex).clone();

         getReplicaTransform(matrix, replicaIndex);

         segment.transform(matrix);
      }

      return segment;
   }

   public int getIndex(JDRPathSegment segment)
   {
      return path_.getIndex(segment);
   }

   public int getLastIndex(JDRPathSegment segment)
   {
      return path_.getLastIndex(segment);
   }

   public JDRShape breakPath()
      throws InvalidShapeException
   {
      // break this path

      JDRShape path = path_.breakPath();

      JDRPattern pattern = createTemplate();

      pattern.path_ = path;

      if (point_ != null)
      {
         pattern.point_ = (JDRPatternAnchorPoint)point_.clone();
      }

      if (adjust_ != null)
      {
         pattern.adjust_ = (JDRPatternAdjustPoint)adjust_.clone();
      }

      return pattern;
   }

    public JDRShape getFullPath()
      throws InvalidShapeException
    {
       if (singlemode_)
       {
          CanvasGraphics cg = getCanvasGraphics();

          JDRGroup group = new JDRGroup(cg);

          JDRShape full = path_.getFullPath();

          group.add(full);

          double[] matrix = new double[6];

          for (int i = 1; i <= replicas_; i++)
          {
             JDRShape shape = (JDRShape)full.getFullPath();

             getReplicaTransform(matrix, i);

             shape.transform(matrix);

             group.add(shape);
          }

          return group.mergePaths(null);
       }

       JDRShape shape = null;

       JDRPoint endPt = null;

       Path2D underlyingShape = path_.getGeneralPath();
       Path2D fullPath = null;

       if (showoriginal_)
       {
          fullPath = underlyingShape;
       }

       double[] matrix = new double[6];

       AffineTransform af = new AffineTransform();

       for (int i = 1; i <= replicas_; i++)
       {
          getReplicaTransform(matrix, i);
          af.setTransform(matrix[0], matrix[1], matrix[2],
                          matrix[3], matrix[4], matrix[5]);

          if (fullPath == null)
          {
             fullPath = new Path2D.Double(underlyingShape, af);
          }
          else
          {
             fullPath.append(af.createTransformedShape(underlyingShape), false);
          }
       }

       JDRPath newPath = JDRPath.getPath(getCanvasGraphics(), 
             fullPath.getPathIterator(null));

       JDRStroke stroke = path_.getStroke();

       if (stroke instanceof JDRBasicStroke)
       {
          newPath.setStroke(stroke);
          newPath.setFillPaint(path_.getFillPaint());
          newPath.setLinePaint(path_.getLinePaint());

          return newPath;
       }

       JDRTextPathStroke tpStroke = (JDRTextPathStroke)stroke;

       newPath.setLinePaint(path_.getLinePaint());

       return new JDRTextPath(newPath, tpStroke);
    }

   public JDRCompleteObject getFullObject()
     throws InvalidShapeException
   {
      if (singlemode_)
      {
         return getFullPath();
      }

      JDRGroup group = new JDRGroup(getCanvasGraphics(), replicas_+1);

      JDRShape shape = path_.getFullPath();

      if (showoriginal_)
      {
         group.add(shape);
      }

      double[] matrix = new double[6];

      for (int i = 1; i <= replicas_; i++)
      {
         JDRShape rep = (JDRShape)shape.clone();

         getReplicaTransform(matrix, i);

         rep.transform(matrix);

         group.add(rep);
      }

      return group;
   }

   public int psLevel()
   {
      return path_.psLevel();
   }

   public void saveEPS(PrintWriter out) throws IOException
   {
      if (singlemode_)
      {
         try
         {
            getFullPath().saveEPS(out);
         }
         catch (InvalidShapeException e)
         {
            getCanvasGraphics().getMessageSystem().getPublisher().publishMessages(
               MessageInfo.createWarning(e));
         }
      }
      else
      {
         // multipath mode

         // Do original

         if (showoriginal_)
         {
            path_.saveEPS(out);
         }

         double[] matrix = new double[6];

         for (int i = 1; i <= replicas_; i++)
         {
            JDRShape rep = (JDRShape)path_.clone();

            getReplicaTransform(matrix, i);

            rep.transform(matrix);

            rep.saveEPS(out);
         }
      }
   }

   public void saveSVG(SVG svg, String attr)
      throws IOException
   {
      if (singlemode_)
      {
         try
         {
            getFullPath().saveSVG(svg, attr);
         }
         catch (InvalidShapeException e)
         {
            getCanvasGraphics().getMessageSystem().getPublisher().publishMessages(
               MessageInfo.createWarning(e));
         }
      }
      else
      {
         // multipath mode

         // Do original

         if (showoriginal_)
         {
            path_.saveSVG(svg, attr);
         }

         double[] matrix = new double[6];

         for (int i = 1; i <= replicas_; i++)
         {
            JDRShape rep = (JDRShape)path_.clone();

            getReplicaTransform(matrix, i);

            rep.transform(matrix);

            rep.saveSVG(svg, attr);
         }
      }
   } 

   public void savePgf(TeX tex)
     throws IOException
   {
      if (singlemode_)
      {
         try
         {
            getFullPath().savePgf(tex);
         }
         catch (InvalidShapeException e)
         {
            getCanvasGraphics().getMessageSystem().getPublisher().publishMessages(
               MessageInfo.createWarning(e));
            return;
         }
      }
      else
      {
         // multipath mode

         // Do original

         tex.comment("pattern (" + replicas_ 
                    + " " + (replicas_ == 1 ? "replica" : "replicas")
                    + ")");

         if (showoriginal_)
         {
            tex.comment("Original");
            path_.savePgf(tex);
         }

         double[] matrix = new double[6];

         for (int i = 1; i <= replicas_; i++)
         {
            JDRShape rep = (JDRShape)path_.clone();

            getReplicaTransform(matrix, i);

            rep.transform(matrix);

            tex.comment("replica " + i);

            rep.savePgf(tex);
         }
      }
   }

   public void fade(double value)
   {
      path_.fade(value);
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      super.setCanvasGraphics(cg);

      if (point_ != null)
      {
         point_.setCanvasGraphics(cg);
      }

      if (adjust_ != null)
      {
         adjust_.setCanvasGraphics(cg);
      }
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      point_.applyCanvasGraphics(cg);

      if (adjust_ != null)
      {
         adjust_.applyCanvasGraphics(cg);
      }

      super.applyCanvasGraphics(cg);
   }

   public JDRShape outlineToPath() throws InvalidShapeException
   {
      JDRPattern p = createTemplate();

      if (path_ != null)
      {
         p.path_ = path_.outlineToPath();
      }

      if (point_ != null)
      {
         p.point_ = (JDRPatternAnchorPoint)point_.clone();
      }

      if (adjust_ != null)
      {
         p.adjust_ = (JDRPatternAdjustPoint)adjust_.clone();
      }

      p.singlemode_ = singlemode_;
      p.showoriginal_ = showoriginal_;

      return p;
   }

   protected JDRPatternIterator iterator;
   protected JDRPatternPointIterator pointIterator;

   protected int replicas_;
   protected boolean singlemode_;
   protected boolean showoriginal_ = true;

   protected JDRShape path_;
   protected JDRPoint point_;

   // Some patterns also require an offset point

   protected JDRPoint adjust_ = null;

}
