// File          : ArrowEquilateralFilled.java
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

package com.dickimawbooks.jdr.marker;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Filled equilateral triangle marker marker.
 *  The basic marker shape looks like:
 * <img src="../images/equilateralFilledMarker.png" alt="[filled equilateral triangle]">
 * (the origin is along the left edge of the shape.)
 * This marker's shape does not depend on the associated path's
 * line width. Instead it depends on the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowEquilateralFilled extends JDRMarker
{
   /**
    * Creates filled equilateral triangle marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowEquilateralFilled(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength markerSize)
   {
      super(penwidth, repeat, isReversed, markerSize);

      type = ARROW_EQUILATERAL_FILLED;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"equilateralfilled":
           "arrow-"+repeated+"equilateralfilled";
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      double y = markerSize*0.5;
      double x = markerSize*0.866025404;

      GeneralPath path = new GeneralPath();

      path.moveTo(0.0f, (float)y);
      path.lineTo((float)x, 0.0f);
      path.lineTo(0.0f, (float)-y);
      path.closePath();

      return path;
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
    * Returns empty string.
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowEquilateralFilled(penWidth, repeated,
                                     reversed, (JDRLength)size.clone());
      makeOtherEqual(marker);

      return marker;
   }
}
