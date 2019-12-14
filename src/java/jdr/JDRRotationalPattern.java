// File          : JDRRotationalPattern.java
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
 *  Class representing a rotational pattern. This has an underlying
 *  shape that gets replicated by rotating around a given point. The
 *  overall shape is governed by the following parameters:
 *  <ul>
 *  <li> Point of rotation.
 *  <li> Angle of rotation.
 *  <li> Number of replicas.
 *  <li> Mode.
 *  </ul>
 *  The mode determines whether the underlying path and replicas are
 *  drawn in one go (single mode) or whether they are drawn independently 
 *  of each other (multi mode).
 *  @author Nicola L C Talbot
 */

public class JDRRotationalPattern extends JDRPattern
{
   /**
    * Creates a rotational pattern from the given path.
    * @param path the path
    * @param point the point of rotation
    * @param angle the angle of rotation (Radians)
    * @param singleMode true if single path mode
    * @param replicas the number of times the underlying path is
    * replicated
    */
    public JDRRotationalPattern(CanvasGraphics cg, JDRShape path, 
       JDRPoint point, JDRAngle angle, int replicas, boolean singleMode)
    {
       this(cg, path, point, angle, replicas, singleMode, true);
    }

   /**
    * Creates a rotational pattern from the given path.
    * @param path the path
    * @param point the point of rotation
    * @param angle the angle of rotation (Radians)
    * @param singleMode true if single path mode
    * @param showOriginalShape true if original should be drawn
    * @param replicas the number of times the underlying path is
    * replicated
    */
    public JDRRotationalPattern(CanvasGraphics cg,
       JDRShape path, JDRPoint point, JDRAngle angle, 
       int replicas, boolean singleMode, boolean showOriginalShape)
    {
       super(cg);

       angle_ = new JDRAngle(cg);

       setUnderlyingShape(path);
       setPatternAnchor(point);
       setRotationAngle(angle);
       setNumReplicas(replicas);
       setSinglePath(singleMode);
       setShowOriginal(showOriginalShape);

       initIterators();
    }

    /**
     * Creates an empty path with one replica, angle of PI radians
     * and point at the origin.
     */
    public JDRRotationalPattern(CanvasGraphics cg)
    {
       this(cg,
            new JDRPath(cg), // underlying shape
            new JDRPatternAnchorPoint(cg), // anchor
            new JDRAngle(cg, Math.PI, JDRAngle.RADIAN), // angle
            1, // replicas
            true, // single mode
            true // show original
            );
    }

    public JDRRotationalPattern(int capacity, JDRPaint lineColor,
                            JDRPaint fillColor, JDRStroke s)
    {
       this(lineColor.getCanvasGraphics(),
            new JDRPath(capacity, lineColor, fillColor, s), // underlying shape
            new JDRPatternAnchorPoint(lineColor.getCanvasGraphics()), // anchor
            new JDRAngle(lineColor.getCanvasGraphics(),Math.PI*0.5, JDRAngle.RADIAN), // angle
            4, // replicas
            true, // single mode
            true // show original
            );
    }

    public JDRPattern createTemplate()
    {
       return new JDRRotationalPattern(getCanvasGraphics(),
          null, null,
          getRotationAngle(),
          getNumReplicas(),
          isSinglePath(),
          showOriginal());
    }

    /**
     * Makes the angle of rotation for this pattern the same as the
     * angle of rotation for the given pattern. (Which must also be
     * an instance of JDRRotationalPattern.)
     */
    public void makeParametersEqual(JDRPattern object)
    {
       JDRRotationalPattern pattern = (JDRRotationalPattern)object;

       setRotationAngle(pattern.getRotationAngle());
    }

   public void getReplicaTransform(double[] matrix, int replicaIndex)
   {
      double repAngle = replicaIndex*getRotationAngle().toRadians();

      double cosAngle = Math.cos(repAngle);
      double sinAngle = Math.sin(repAngle);

      JDRPoint anchorPt = getPatternAnchor();

      matrix[0] = cosAngle;
      matrix[1] = sinAngle;
      matrix[2] = -sinAngle;
      matrix[3] = cosAngle;
      matrix[4] = anchorPt.getX()
                - anchorPt.getX() * cosAngle
                + anchorPt.getY() * sinAngle;
      matrix[5] = anchorPt.getY()
                - anchorPt.getX() * sinAngle
                - anchorPt.getY() * cosAngle;
   }


   public JDRObjectLoaderListener getListener()
   {
      return shapeListener;
   }

   public String toString()
   {
      String str = "RotationalPattern: point="
                 +getPatternAnchor()+", angle: " +getRotationAngle()
                 + ", replicas: "+getNumReplicas()
                 + ", size="+size()+", segments=[";

      for (int i = 0; i < size(); i++)
      {
         str += get(i)+",";
      }

      str += "]";

      return str;
    }


   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "RotationalPattern:"+eol;

      str += "point of rotation: "+getPatternAnchor().info()+eol;

      str += "angle of rotation: "+getRotationAngle()+eol;

      str += "replicas: "+getNumReplicas();

      str += "Underlying shape:"+getUnderlyingShape().info();

      return str;
   }

   public JDRAngle getRotationAngle()
   {
      return angle_;
   }

   public void setRotationAngle(JDRAngle angle)
   {
      angle_.makeEqual(angle);
   }

   public String[] getDescriptionInfo()
   {
      return new String[] {""+getNumReplicas(), ""+getRotationAngle()};
   }

   private JDRAngle angle_;

   private static JDRPathListener shapeListener = new JDRRotationalPatternListener();

}

