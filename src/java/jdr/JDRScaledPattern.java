// File          : JDRScaledPattern.java
// Creation Date : 9th April 2011
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
 *  Class representing a scaled pattern. This has an underlying
 *  shape that gets replicated by scaling relative to a given point.
 *  The overall shape is governed by the following parameters:
 *  <ul>
 *  <li> X scale factor.
 *  <li> Y scale factor.
 *  <li> Anchor.
 *  <li> Number of replicas.
 *  <li> Mode.
 *  </ul>
 *  The mode determines whether the underlying path and replicas are
 *  drawn in one go (single mode) or whether they are drawn independently 
 *  of each other (multi mode).
 *  @author Nicola L C Talbot
 */

public class JDRScaledPattern extends JDRPattern
{
   /**
    * Creates a scaled pattern from the given path.
    * @param path the path
    * @param point the point of rotation
    * @param adjust the adjustment control
    * @param scaleX X scale factor
    * @param scaleY Y scale factor
    * @param singleMode true if single path mode
    * @param replicas the number of times the underlying path is
    * replicated
    */
    public JDRScaledPattern(JDRShape path, JDRPoint point,
       JDRPoint adjust,
       double scaleX, double scaleY, int replicas, boolean singleMode)
    {
       this(path == null ? point.getCanvasGraphics() : 
            path.getCanvasGraphics(),
            path, point, adjust, scaleX, scaleY, replicas, singleMode);
    }

    public JDRScaledPattern(CanvasGraphics cg, JDRShape path, JDRPoint point,
       JDRPoint adjust,
       double scaleX, double scaleY, int replicas, boolean singleMode)
    {
       this(cg, path, point, adjust, scaleX, scaleY, replicas, singleMode,
            true);
    }

   /**
    * Creates a scaled pattern from the given path.
    * @param path the path
    * @param point the point of rotation
    * @param adjust the adjustment control
    * @param scaleX X scale factor
    * @param scaleY Y scale factor
    * @param singleMode true if single path mode
    * @param showOriginalShape true if original path is to be drawn
    * @param replicas the number of times the underlying path is
    * replicated
    */
    public JDRScaledPattern(JDRShape path, JDRPoint point,
       JDRPoint adjust,
       double scaleX, double scaleY, int replicas, boolean singleMode,
       boolean showOriginalShape)
    {
       this(path == null ? point.getCanvasGraphics() : 
            path.getCanvasGraphics(), path, point,
            adjust, scaleX, scaleY, replicas, singleMode,
            showOriginalShape);
    }

    public JDRScaledPattern(CanvasGraphics cg, JDRShape path, 
       JDRPoint point, JDRPoint adjust,
       double scaleX, double scaleY, int replicas, boolean singleMode,
       boolean showOriginalShape)
    {
       super(cg);

       setUnderlyingShape(path);
       setPatternAnchor(point);
       setPatternAdjust(adjust);
       setScale(scaleX, scaleY);
       setNumReplicas(replicas);
       setSinglePath(singleMode);
       setShowOriginal(showOriginalShape);

       initIterators();

    }

    public JDRScaledPattern(JDRShape path, JDRPoint point,
       double scaleX, double scaleY, int replicas, boolean singleMode)
    {
       this(path == null ? point.getCanvasGraphics() : 
             path.getCanvasGraphics(),
            path, point, null, scaleX, scaleY, replicas, singleMode, true);
    }

    public JDRScaledPattern(CanvasGraphics cg, JDRShape path, JDRPoint point,
       double scaleX, double scaleY, int replicas, boolean singleMode)
    {
       this(cg, path, point, null, scaleX, scaleY, replicas, singleMode,
            true);
    }

    public JDRScaledPattern(JDRShape path, JDRPoint point,
       double scaleX, double scaleY, int replicas, boolean singleMode,
       boolean showOriginalShape)
    {
       this(path == null ? point.getCanvasGraphics() :
            path.getCanvasGraphics(),
            path, point, null, scaleX, scaleY, replicas, singleMode,
            showOriginalShape);
    }

    public JDRScaledPattern(CanvasGraphics cg, JDRShape path, JDRPoint point,
       double scaleX, double scaleY, int replicas, boolean singleMode,
       boolean showOriginalShape)
    {
       this(cg, path, point, null, scaleX, scaleY, replicas, singleMode,
            showOriginalShape);
    }

    /**
     * Creates an empty path with one replica, scale factors = 2
     * and point at the origin.
     */
    public JDRScaledPattern(CanvasGraphics cg)
    {
       this(cg, 
            new JDRPath(cg), // underlying shape point,
            new JDRPatternAnchorPoint(cg), // anchor
            new JDRPatternAdjustPoint(cg), // adjust
            2, // scale X
            2, // scale y
            1, // replicas
            true, // single mode
            true // show original
            );
    }

    public JDRScaledPattern(int capacity, JDRPaint lineColor,
                            JDRPaint fillColor, JDRStroke s)
    {
       this(lineColor.getCanvasGraphics(), 
            new JDRPath(capacity, lineColor, fillColor, s), // underlying shape point,
            new JDRPatternAnchorPoint(lineColor.getCanvasGraphics()), // anchor
            new JDRPatternAdjustPoint(lineColor.getCanvasGraphics()), // adjust
            2, // scale X
            2, // scale y
            1, // replicas
            true, // single mode
            true // show original
            );
    }

   public boolean hasAdjust()
   {
      return true;
   }

    public JDRPattern createTemplate()
    {
       return new JDRScaledPattern(getCanvasGraphics(), null, null, null,
          getScaleX(), getScaleY(), getNumReplicas(), isSinglePath(),
          showOriginal());
    }

    /**
     * Makes the scale factors for this pattern the same as the
     * scale factors for the given pattern. (Which must also be
     * an instance of JDRScaledPattern.)
     */
    public void makeParametersEqual(JDRPattern object)
    {
       JDRScaledPattern pattern = (JDRScaledPattern)object;

       setScaleX(pattern.getScaleX());
       setScaleY(pattern.getScaleY());
    }

   public void getReplicaTransform(double[] matrix, int replicaIndex)
   {
      JDRPoint anchorPt = getPatternAnchor();

      if (replicaIndex == 1)
      {
         matrix[0] = getScaleX();
         matrix[1] = 0.0;
         matrix[2] = 0.0;
         matrix[3] = getScaleY();
         matrix[4] = anchorPt.getX() * (1.0 - getScaleX());
         matrix[5] = anchorPt.getY() * (1.0 - getScaleY());

         return;
      }

      double repScaleX = replicaIndex * getScaleX();
      double repScaleY = replicaIndex * getScaleY();

      double shiftX = 0.0;
      double shiftY = 0.0;

      JDRPoint adjustPt = getPatternAdjust();
      JDRShape path = getUnderlyingShape();

      if (adjustPt != null && path != null)
      {
         JDRPoint pt = path.getFirstControl();

         shiftX = adjustPt.getX()
                - getScaleX()*pt.getX()
                + anchorPt.getX()*(getScaleX() - 1);
         shiftY = adjustPt.getY()
                - getScaleY()*pt.getY()
                + anchorPt.getY()*(getScaleY() - 1);
      }

      int i = replicaIndex - 1;

      matrix[0] = repScaleX;
      matrix[1] = 0.0;
      matrix[2] = 0.0;
      matrix[3] = repScaleY;
      matrix[4] = anchorPt.getX() * (1.0 - repScaleX) + shiftX*i;
      matrix[5] = anchorPt.getY() * (1.0 - repScaleY) + shiftY*i;
   }

   public void translateParams(double x, double y)
   {
      super.translateAnchor(x, y);

      super.translateAdjust(x, y);
   }

   /**
    * Translate offset point but constrain it to the line of
    * scaling.
    */
   public void translateAdjust(double x, double y)
   {
      JDRPoint adjustPt = getPatternAdjust();

      if (adjustPt != null)
      {
         adjustPt.translate(x, y);

         JDRShape path = getUnderlyingShape();

         if (path != null)
         {
            JDRPoint pt = path.getFirstControl();

            JDRPoint anchorPt = getPatternAnchor();

            adjustPt.moveToLine(anchorPt.getX(), anchorPt.getY(),
               getScaleX()*pt.getX() + anchorPt.getX()*(1-getScaleX()),
               getScaleY()*pt.getY() + anchorPt.getY()*(1-getScaleY()));
         }
      }
   }

   public void translateAnchor(double x, double y)
   {
      getPatternAnchor().translate(x, y);

      translateAdjust(-x, -y);
   }


   public JDRObjectLoaderListener getListener()
   {
      return shapeListener;
   }

   public String toString()
   {
      int n = size();

      String str = "ScaledPattern: point="+getPatternAnchor()
                 +", scalex: " +getScaleX()
                 +", scalex: " +getScaleY()
                 + ", replicas: "+getNumReplicas()
                 + ", size="+n+", segments=[";

      for (int i = 0; i < n; i++)
      {
         str += get(i)+",";
      }

      str += "]";

      return str;
    }


   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "ScaledPattern:"+eol;

      str += "point of rotation: "+getPatternAnchor().info()+eol;

      str += "x scale factor: "+getScaleX()+eol;

      str += "y scale factor: "+getScaleY()+eol;

      str += "replicas: "+getNumReplicas();

      str += "Underlying shape:"+getUnderlyingShape().info();

      return str;
   }

   public double getScaleX()
   {
      return scalex_;
   }

   public double getScaleY()
   {
      return scaley_;
   }

   public void setScaleX(double scaleX)
     throws JdrIllegalArgumentException
   {
      if (scaleX == 0.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.PATTERN_SCALE_X, scaleX,
            getCanvasGraphics());
      }

      scalex_ = scaleX;
   }

   public void setScaleY(double scaleY)
     throws JdrIllegalArgumentException
   {
      if (scaleY == 0.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.PATTERN_SCALE_Y, scaleY,
            getCanvasGraphics());
      }

      scaley_ = scaleY;
   }

   public void setScale(double scaleX, double scaleY)
     throws JdrIllegalArgumentException
   {
      if (scaleX == 0.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.PATTERN_SCALE_X, scaleX,
            getCanvasGraphics());
      }

      if (scaleY == 0.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.PATTERN_SCALE_Y, scaleY,
            getCanvasGraphics());
      }

      scalex_ = scaleX;
      scaley_ = scaleY;
   }

   public Object[] getDescriptionInfo()
   {
      return new Object[] {getNumReplicas(), getScaleX(), getScaleY()};
   }

   public Object clone()
   {
      JDRScaledPattern pattern = new JDRScaledPattern(getCanvasGraphics());
      pattern.makeEqual(this);

      return pattern;
   }

   public void makeEqual(JDRGraphicObject object)
   {
      super.makeEqual(object);

      JDRScaledPattern pattern = (JDRScaledPattern)object;

      setScaleX(pattern.getScaleX());
      setScaleY(pattern.getScaleY());
   }

   private double scalex_=1.0, scaley_=1.0;

   private static JDRPathListener shapeListener = new JDRScaledPatternListener();

}

