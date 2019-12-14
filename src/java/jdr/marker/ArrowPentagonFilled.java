// File          : ArrowPentagonFilled.java
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
 * Filled pentagon marker. The basic filled pentagon marker shape looks like:
 * <img src="../images/pentFilledMarker.png" alt="[filled pentagon]">
 * (the origin is at the centre of the shape.)
 * This marker's shape does not depend on the associated path's
 * line width. Instead it depends on the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowPentagonFilled extends JDRMarker
{
   /**
    * Creates filled pentagon marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowPentagonFilled(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength markerSize)
   {
      super(penwidth, repeat, isReversed, markerSize);

      type = ARROW_PENTAGON_FILLED;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"pentfilled":
           "arrow-"+repeated+"pentfilled";
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double length = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      float x0 = (float)(length*ArrowPentagonOpen.sinAngle);
      float y0 = (float)(length*ArrowPentagonOpen.cosAngle);
      float x1 = (float)(length*ArrowPentagonOpen.sinHalfAngle);
      float y1 = (float)(length*ArrowPentagonOpen.cosHalfAngle);

      path.moveTo(0.0f, -(float)length);
      path.lineTo(-x0, -y0);
      path.lineTo(-x1, y1);
      path.lineTo(x1, y1);
      path.lineTo(x0, -y0);
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
    * Not implemented (returns empty string).
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowPentagonFilled(penWidth, repeated,
                                     reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }

   public boolean hasXAxisSymmetry()
   {
      return false;
   }
}
