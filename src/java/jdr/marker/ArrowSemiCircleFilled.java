// File          : ArrowSemiCircleFilled.java
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
 * Filled semicircle marker. The basic marker shape looks like:
 * <img src="../images/semiCircleFilledMarker.png" alt="[filled semicircle]">
 * This marker's shape does not depend on the associated path's
 * line width. Instead it depends on the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowSemiCircleFilled extends JDRMarker
{
   /**
    * Creates filled semicircle marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowSemiCircleFilled(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength markerSize)
   {
      super(penwidth, repeat, isReversed, markerSize);

      type = ARROW_SEMICIRCLE_FILLED;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"semicirclefilled-"+size:
           "arrow-"+repeated+"semicirclefilled-"+size;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double radius = size.getValue(storageUnit);

      double diameter = 2.0*radius;

      Shape path = new Arc2D.Double(-radius, -radius, diameter,
         diameter, -90.0, 180.0, Arc2D.CHORD);

      return new GeneralPath(path);
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
      JDRMarker marker = new ArrowSemiCircleFilled(penWidth, repeated,
                                   reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }
}
