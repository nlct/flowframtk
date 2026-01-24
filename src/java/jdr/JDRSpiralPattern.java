// File          : JDRSpiralPattern.java
// Creation Date : 9th Sept 2010
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
 *  Class representing a spiral pattern. This has an underlying
 *  shape that gets replicated by spiralling around a given point. The
 *  overall shape is governed by the following parameters:
 *  <ul>
 *  <li> Point of rotation.
 *  <li> Angle increment.
 *  <li> Distance parameter
 *  <li> Number of replicas.
 *  <li> Mode.
 *  </ul>
 *  The mode determines whether the underlying path and replicas are
 *  drawn in one go (single mode) or whether they are drawn independently 
 *  of each other (multi mode).
 *  @author Nicola L C Talbot
 */

public class JDRSpiralPattern extends JDRPattern
{
   /**
    * Creates a spiral pattern from the given path.
    * @param path the path
    * @param point the point of rotation
    * @param adjust adjust control
    * @param angle the angle increment
    * @param distance the distance parameter of Archimedean spiral
    * @param singleMode true if single path mode
    * @param replicas the number of times the underlying path is
    * replicated
    */
    public JDRSpiralPattern(JDRShape path, JDRPoint point, JDRPoint adjust,
       JDRAngle angle, double distance,
       int replicas, boolean singleMode)
    {
       this(path == null ? point.getCanvasGraphics() : path.getCanvasGraphics(),
            path, point, adjust, angle, distance, replicas, singleMode, true);
    }

    public JDRSpiralPattern(CanvasGraphics cg,
       JDRShape path, JDRPoint point, JDRPoint adjust,
       JDRAngle angle, double distance,
       int replicas, boolean singleMode)
    {
       this(cg, path, point, adjust, angle, distance, replicas,
            singleMode, true);
    }

   /**
    * Creates a spiral pattern from the given path.
    * @param path the path
    * @param point the point of rotation
    * @param adjust adjust control
    * @param angle the angle increment
    * @param distance the distance parameter of Archimedean spiral
    * @param singleMode true if single path mode
    * @param showOriginalShape true if original path is to be drawn
    * @param replicas the number of times the underlying path is
    * replicated
    */
    public JDRSpiralPattern(JDRShape path, JDRPoint point, JDRPoint adjust,
       JDRAngle angle, double distance,
       int replicas, boolean singleMode, boolean showOriginalShape)
    {
       this(path == null ? point.getCanvasGraphics() : path.getCanvasGraphics(),
            path, point, adjust, angle, distance, replicas,
            singleMode, showOriginalShape);
    }

    public JDRSpiralPattern(CanvasGraphics cg,
       JDRShape path, JDRPoint point, JDRPoint adjust,
       JDRAngle angle, double distance,
       int replicas, boolean singleMode, boolean showOriginalShape)
    {
       super(cg);

       path_ = path;
       angle_ = new JDRAngle(cg);

       setPatternAnchor(point);
       setPatternAdjust(adjust);
       setRotationAngle(angle);
       setDistance(distance);
       setNumReplicas(replicas);
       setSinglePath(singleMode);
       setShowOriginal(showOriginalShape);

       initIterators();
    }

    public JDRSpiralPattern(JDRShape path, JDRPoint point,
       JDRAngle angle, double distance,
       int replicas, boolean singleMode)
    {
       this(path == null ? point.getCanvasGraphics() : path.getCanvasGraphics(),
            path, point, null, angle, distance, replicas, singleMode, true);
    }

    public JDRSpiralPattern(CanvasGraphics cg, JDRShape path, JDRPoint point,
       JDRAngle angle, double distance,
       int replicas, boolean singleMode)
    {
       this(cg, path, point, null, angle, distance, replicas,
            singleMode, true);

       setDefaultPatternAdjust();
    }

    /**
     * Creates an empty path with one replica, angle of PI radians
     * and point at the origin.
     */
    public JDRSpiralPattern(CanvasGraphics cg)
    {
       this(cg, 
            new JDRPath(cg), // underlying shape
            new JDRPatternAnchorPoint(cg),// anchor
            new JDRPatternAdjustPoint(cg),// adjust
            new JDRAngle(cg, Math.PI*0.25, JDRAngle.RADIAN), // angle
            cg.bpToStorage(1.0),// distance
            1,// replicas
            true,// single mode
            true// show original 
            );
    }

    public JDRSpiralPattern(int capacity, JDRPaint lineColor,
                            JDRPaint fillColor, JDRStroke s)
    {
       this(lineColor.getCanvasGraphics(), 
            new JDRPath(capacity, lineColor, fillColor, s), // underlying shape
            new JDRPatternAnchorPoint(lineColor.getCanvasGraphics()),// anchor
            new JDRPatternAdjustPoint(lineColor.getCanvasGraphics()),// adjust
            new JDRAngle(lineColor.getCanvasGraphics(), Math.PI*0.25, JDRAngle.RADIAN), // angle
            lineColor.getCanvasGraphics().bpToStorage(1.0),// distance
            4,// replicas
            true,// single mode
            true// show original 
            );
    }

    public boolean hasAdjust()
    {
       return true;
    }

    public JDRPattern createTemplate()
    {
       return new JDRSpiralPattern(getCanvasGraphics(),
          null, null, null,
          getRotationAngle(),
          getDistance(),
          getNumReplicas(),
          isSinglePath(),
          showOriginal());
    }

    /**
     * Makes the angle of rotation for this pattern the same as the
     * angle of rotation for the given pattern. (Which must also be
     * an instance of JDRSpiralPattern.)
     */
    public void makeParametersEqual(JDRPattern object)
    {
       JDRSpiralPattern pattern = (JDRSpiralPattern)object;

       setRotationAngle(pattern.getRotationAngle());
       setDistance(pattern.getDistance());
    }

   public void getReplicaTransform(double[] matrix, int replicaIndex)
   {
      double dx = 0;
      double dy = 0;

      double a = 0.0;

      double repAngle = replicaIndex*getRotationAngle().toRadians();

      JDRPoint anchorPt = getPatternAnchor();
      JDRPoint adjustPt = getPatternAdjust();

      if (adjustPt != null)
      {
         dx = anchorPt.getX() - adjustPt.getX();
         dy = anchorPt.getY() - adjustPt.getY();

         a = Math.sqrt(dx*dx + dy*dy);

         repAngle += Math.atan2(dy, dx);
      }

      double cosAngle = Math.cos(repAngle);
      double sinAngle = Math.sin(repAngle);

      double b = getDistance() / (2.0*Math.PI);

      double radius = a + b * repAngle;

      matrix[0] = cosAngle;
      matrix[1] = sinAngle;
      matrix[2] = -sinAngle;
      matrix[3] = cosAngle;
      matrix[4] = cosAngle*(radius-anchorPt.getX())
                + anchorPt.getY()*sinAngle+anchorPt.getX();
      matrix[5] = sinAngle*(radius-anchorPt.getX())
                + anchorPt.getY()*(1-cosAngle);
   }


   public JDRObjectLoaderListener getListener()
   {
      return shapeListener;
   }

   public String toString()
   {
      String str = "SpiralPattern: point="+point_+", angle: " +angle_
                 + ", distance: " + distance_
                 + ", replicas: "+replicas_
                 + ", size="+size()+", segments=[";

      for (int i = 0; i < size(); i++)
      {
         str += get(i)+",";
      }

      str += "]";

      return str;
    }

   @Override
   public String info(String prefix)
   {
      JDRMessage msgSys = getCanvasGraphics().getMessageSystem();
      String eol = String.format("%n");

      StringBuilder builder = new StringBuilder();

      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
       "objectinfo.spiral_pattern", "Spiral pattern:"));

      builder.append(eol);
      builder.append(prefix);

      JDRPoint adjustPt = getPatternAdjust();

      if (adjustPt == null)
      {
         builder.append(msgSys.getMessageWithFallback(
           "objectinfo.spiral_pattern.adjust_control", "Adjustment control: {0}",
            "null"));
      }
      else
      {
         builder.append(msgSys.getMessageWithFallback(
           "objectinfo.spiral_pattern.adjust_control", "Adjustment control: {0}",
            adjustPt.info()));
      }

      builder.append(eol);
      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
        "objectinfo.spiral_pattern.angle", "Angle: {0}",
         angle_ == null ? "null" : angle_.info())
      );

      builder.append(eol);
      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
        "objectinfo.spiral_pattern.distance", "Distance: {0}",
         getDistance())
      );

      builder.append(eol);
      builder.append(super.info(prefix));

      return builder.toString();
   }

   public JDRAngle getRotationAngle()
   {
      return angle_;
   }

   public void setRotationAngle(JDRAngle angle)
   {
      angle_.makeEqual(angle);
   }

   public void setDistance(double distance)
   {
      distance_ = distance;
   }

   public double getDistance()
   {
      return distance_;
   }

   public Object[] getDescriptionInfo()
   {
      return new Object[] {getNumReplicas(), getRotationAngle(), getDistance()};
   }

   public Object clone()
   {
      JDRSpiralPattern pattern = new JDRSpiralPattern(getCanvasGraphics());
      pattern.makeEqual(this);

      return pattern;
   }

   public void makeEqual(JDRGraphicObject object)
   {
      super.makeEqual(object);

      JDRSpiralPattern pattern = (JDRSpiralPattern)object;

      setRotationAngle(pattern.getRotationAngle());
      setDistance(pattern.getDistance());
   }

   private JDRAngle angle_;

   private double distance_;

   private static JDRPathListener shapeListener = new JDRSpiralPatternListener();

}

