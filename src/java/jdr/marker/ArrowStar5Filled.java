// File          : ArrowStar5Filled.java
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
 * Filled 5-pointed star marker. The basic marker shape looks like:
 * <img src="../images/star5FilledMarker.png" alt="[filled 5 pointed star]">
 * (the origin is at the centre of the shape.)
 * This marker's shape does not depend on the associated path's
 * line width. Instead it depends on the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowStar5Filled extends JDRMarker
{
   /**
    * Creates filled 5 pointed star marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowStar5Filled(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength markerSize)
   {
      super(penwidth, repeat, isReversed, markerSize);

      type = ARROW_STAR5_FILLED;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"star5filled-"+size:
           "arrow-"+repeated+"star5filled-"+size;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double length = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double x0 = length*ArrowPentagonOpen.sinAngle;
      double y0 = length*ArrowPentagonOpen.cosAngle;
      double x1 = length*ArrowPentagonOpen.sinHalfAngle;
      double y1 = length*ArrowPentagonOpen.cosHalfAngle;

      double halfx0 = 0.5*x0;
      double halfy0 = 0.5*y0;
      double halfx1 = 0.5*x1;
      double halfy1 = 0.5*y1;

      path.moveTo(0.0f, -(float)length);
      path.lineTo(-(float)halfx1, -(float)halfy1);
      path.lineTo(-(float)x0, -(float)y0);
      path.lineTo(-(float)halfx0, (float)halfy0);
      path.lineTo(-(float)x1, (float)y1);
      path.lineTo(0.0f, (float)(length*0.5));
      path.lineTo((float)x1, (float)y1);
      path.lineTo((float)halfx0, (float)halfy0);
      path.lineTo((float)x0, -(float)y0);
      path.lineTo((float)halfx1, -(float)halfy1);
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
      JDRMarker marker = new ArrowStar5Filled(penWidth, repeated,
                                     reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }

   public boolean hasXAxisSymmetry()
   {
      return false;
   }
}
