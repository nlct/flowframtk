// File          : ArrowAltBar.java
// Creation Date : 6th May 2008
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

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Alternative bar marker. The basic bar marker shape looks like:
 * <img src="../images/barMarker.png" alt="[ | shape]">
 * This is like {@link ArrowBar} but can be resized.
 * This marker's shape depends on the associated path's
 * line width as well as the marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowAltBar extends JDRMarker
{
   /**
    * Creates bar marker for a path with the given pen width
    * and given marker size.
    * The marker may be repeated and/or reversed. Since the bar
    * marker is symmetrical, the reversed setting only has an
    * effect if the marker is repeated.
    */
   public ArrowAltBar(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowSize)
   {
      super(penwidth, repeat, isReversed, arrowSize);

      type = ARROW_ALT_BAR;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"altbar-"+size:
           "arrow-"+repeated+"altbar-"+size;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      path.moveTo(0.0f, (float)(-markerSize));
      path.lineTo(0.0f, (float)(markerSize));

      BasicStroke stroke = new BasicStroke((float)penWidth.getValue(storageUnit));
      Shape shape = stroke.createStrokedShape(path);

      return new GeneralPath(shape);
   }

   public boolean isResizable()
   {
      return true;
   }

   public boolean usesLineWidth()
   {
      return true;
   }

   /**
    * Not implemented. Returns empty string.
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "";
   }

   public Object clone()
   {
      JDRMarker marker  = new ArrowAltBar(penWidth, repeated,
                                  reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }
}
