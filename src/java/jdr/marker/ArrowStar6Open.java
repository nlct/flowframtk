// File          : ArrowStar6Open.java
// Creation Date : 9th July 2008
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
 * Open 6-pointed star marker. The basic marker shape looks like:
 * <img src="../images/star6OpenMarker.png" alt="[open 6 pointed star]">
 * (the origin is at the centre of the shape.)
 * This marker's shape does not depend on the associated path's
 * line width. Instead it depends on the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowStar6Open extends JDRMarker
{
   /**
    * Creates open 6 pointed star marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowStar6Open(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength markerSize)
   {
      super(penwidth, repeat, isReversed, markerSize);

      type = ARROW_STAR6_OPEN;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"star6open-"+size:
           "arrow-"+repeated+"star6open-"+size;
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

      double root3over2 = 0.866025404;

      double shortradius = 0.5*length;

      double x0 = length*root3over2;
      double y0 = length*0.5;

      double x1 = shortradius*0.5;
      double y1 = shortradius*root3over2;

      path.moveTo((float)shortradius, 0.0f);
      path.lineTo((float)x0, (float)y0);
      path.lineTo((float)x1, (float)y1);
      path.lineTo((float)0, (float)length);
      path.lineTo((float)-x1, (float)y1);
      path.lineTo((float)-x0, (float)y0);
      path.lineTo((float)-shortradius, 0.0f);
      path.lineTo((float)-x0, (float)-y0);
      path.lineTo((float)-x1, (float)-y1);
      path.lineTo((float)0, (float)-length);
      path.lineTo((float)x1, (float)-y1);
      path.lineTo((float)x0, (float)-y0);
      path.closePath();

      BasicStroke stroke = new BasicStroke((float)storageUnit.fromBp(2.0));

      return new GeneralPath(stroke.createStrokedShape(path));
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
      JDRMarker marker = new ArrowStar6Open(penWidth, repeated,
                                     reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }

   public boolean hasXAxisSymmetry()
   {
      return true;
   }
}
