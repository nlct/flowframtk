// File          : ArrowSnowflake.java
// Creation Date : 9th May 2008
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

package com.dickimawbooks.jdr.marker;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Snowflake marker. The basic marker shape looks like:
 * <img src="../images/snowflakeMarker.png" alt="[snowflake]">
 * (the origin is at the centre of the shape.)
 * This marker's shape does not depend on the associated path's
 * line width. Instead it depends on the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowSnowflake extends JDRMarker
{
   /**
    * Creates snowflake marker of given size.
    * The marker may be repeated and/or reversed. 
    */
   public ArrowSnowflake(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength markerSize)
   {
      super(penwidth, repeat, isReversed, markerSize);

      type = ARROW_SNOWFLAKE;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"snowflake-"+size:
           "arrow-"+repeated+"snowflake-"+size;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double segAngle = Math.PI/3;
      double cosSegAngle = 0.5;
      double sinSegAngle = 0.866025404;

      float x = (float)(markerSize*cosSegAngle);
      float y = (float)(markerSize*sinSegAngle);

      path.moveTo((float)markerSize, 0.0f);
      path.lineTo(-(float)markerSize, 0.0f);

      path.moveTo(x, y);
      path.lineTo(-x, -y);

      path.moveTo(-x, y);
      path.lineTo(x, -y);

      double alpha = 0.6;
      double beta = 1.0-alpha;

      double theta = Math.atan((beta*sinSegAngle)/(alpha+beta*cosSegAngle));

      double r1 = alpha*markerSize;
      double r2 = markerSize*Math.sqrt(1-alpha*beta);

      double p0x = r2*Math.cos(theta);
      double p0y = r2*Math.sin(theta);

      double p1x = r2*Math.cos(segAngle-theta);
      double p1y = r2*Math.sin(segAngle-theta);

      double p2x = r1*cosSegAngle;
      double p2y = r1*sinSegAngle;

      double p3x = r2*Math.cos(segAngle+theta);
      double p3y = r2*Math.sin(segAngle+theta);

      path.moveTo((float)p0x, (float)-p0y);
      path.lineTo((float)r1, 0.0f);
      path.lineTo((float)p0x, (float)p0y);

      path.moveTo((float)p1x, (float)p1y);
      path.lineTo((float)p2x, (float)p2y);
      path.lineTo((float)p3x, (float)p3y);

      path.moveTo((float)-p3x, (float)p3y);
      path.lineTo((float)-p2x, (float)p2y);
      path.lineTo((float)-p1x, (float)p1y);

      path.moveTo((float)-p0x, (float)p0y);
      path.lineTo((float)-r1, 0.0f);
      path.lineTo((float)-p0x, (float)-p0y);

      path.moveTo((float)-p1x, (float)-p1y);
      path.lineTo((float)-p2x, (float)-p2y);
      path.lineTo((float)-p3x, (float)-p3y);

      path.moveTo((float)p3x, (float)-p3y);
      path.lineTo((float)p2x, (float)-p2y);
      path.lineTo((float)p1x, (float)-p1y);

      BasicStroke stroke = new BasicStroke((float)storageUnit.fromBp(0.5),
         BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

      Shape shape = stroke.createStrokedShape(path);

      return new GeneralPath(shape);
   }

   public boolean isResizable()
   {
      return true;
   }

   public boolean usesLineWidth()
   {
      return false;
   }

   /**
    * Not implemented (returns empty string).
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowSnowflake(penWidth, repeated,
                                   reversed, (JDRLength)size.clone());
      makeOtherEqual(marker);

      return marker;
   }
}
