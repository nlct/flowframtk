// File          : ArrowPentagonOpen.java
// Creation Date : 27th April 2008
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
 * Open pentagon marker. The basic open pentagon marker shape looks like:
 * <img src="../images/pentOpenMarker.png" alt="[open pentagon]">
 * (the origin is at the centre of the shape.)
 * This marker's shape does not depend on the associated path's
 * line width. Instead it depends on the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowPentagonOpen extends JDRMarker
{
   /**
    * Creates open pentagon marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowPentagonOpen(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength markerSize)
   {
      super(penwidth, repeat, isReversed, markerSize);

      type = ARROW_PENTAGON_OPEN;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"pentopen":
           "arrow-"+repeated+"pentopen";
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double length = markerSize-storageUnit.fromBp(1);

      float x0 = (float)(length*sinAngle);
      float y0 = (float)(length*cosAngle);
      float x1 = (float)(length*sinHalfAngle);
      float y1 = (float)(length*cosHalfAngle);

      path.moveTo(0.0f, -(float)length);
      path.lineTo(-x0, -y0);
      path.lineTo(-x1, y1);
      path.lineTo(x1, y1);
      path.lineTo(x0, -y0);
      path.closePath();

      BasicStroke stroke = new BasicStroke((float)storageUnit.fromBp(2.0));

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
      JDRMarker marker = new ArrowPentagonOpen(penWidth, repeated,
                                   reversed, (JDRLength)size.clone());
      makeOtherEqual(marker);

      return marker;
   }

   public boolean hasXAxisSymmetry()
   {
      return false;
   }

   /**
    * cos(2*pi/5).
    */
   protected static final double cosAngle 
      = Math.cos(2*Math.PI/5);

   /**
    * sin(2*pi/5).
    */
   protected static final double sinAngle 
      = Math.sin(2*Math.PI/5);

   /**
    * cos(2*pi/10).
    */
   protected static final double cosHalfAngle 
      = Math.cos(2*Math.PI/10);

   /**
    * sin(2*pi/10).
    */
   protected static final double sinHalfAngle 
      = Math.sin(2*Math.PI/10);
}
