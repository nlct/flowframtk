// File          : ArrowRound.java
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
 * Round bracket marker. The basic round bracket marker shape looks like:
 * <img src="../images/roundMarker.png" alt="[ ) shape]">
 * (the origin is on the right edge of the shape.)
 * This corresponds to the PGF <code>\pgfarrowround</code> arrow style.
 * This marker's shape depends on the associated path's
 * line width.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowRound extends JDRMarker
{
   /**
    * Creates round bracket marker.
    * The marker may be repeated and/or reversed.
    */
   public ArrowRound(JDRLength penwidth, int repeat,
                      boolean isReversed)
   {
      super(penwidth, repeat, isReversed);

      type = ARROW_ROUND;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"round":
           "arrow-"+repeated+"round";
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double width = penWidth.getValue(storageUnit);

      double minX = -2.0*width;
      double maxY = 5.0*width;
      double midiY = 3.0*width;

      GeneralPath path = new GeneralPath();

      path.moveTo((float)minX, (float)-maxY);
      path.curveTo((float)width, (float)-midiY,
                   (float)width, (float)midiY,
                   (float)minX, (float)maxY);

      BasicStroke stroke = new BasicStroke((float)width);
      Shape shape = stroke.createStrokedShape(path);

      return new GeneralPath(shape);
   }

   public boolean isResizable()
   {
      return false;
   }

   public boolean usesLineWidth()
   {
      return true;
   }

   /**
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "\\pgfarrowround";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowRound(penWidth, repeated,
                                 reversed);
      makeOtherEqual(marker);

      return marker;
   }
}
