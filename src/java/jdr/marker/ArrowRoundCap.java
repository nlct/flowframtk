// File          : ArrowRoundCap.java
// Creation Date : 29th April 2008
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
 * Round marker. The basic marker shape looks like:
 * <img src="../images/roundCapMarker.png" alt="[round shape]">
 * (for a path with pen width = 10 PostScript points).
 * This marker's shape depends on the associated path's
 * line width as well as the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowRoundCap extends JDRMarker
{
   /**
    * Creates round marker for a path with the given pen width.
    * The marker may be repeated and/or reversed.
    */
   public ArrowRoundCap(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowSize)
   {
      super(penwidth, repeat, isReversed, arrowSize);

      type = ARROW_ROUND_CAP;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"roundcap-"+size+"-"+penWidth:
           "arrow-"+repeated+"roundcap-"+size+"-"+penWidth;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);
      double width = penWidth.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double halfWidth = 0.5*width;
      double y = 0.4*width;
      double x = 1.35*markerSize;

      path.moveTo(0.0f, (float)-halfWidth);
      path.curveTo((float)x, (float)-y,
                   (float)x, (float)y,
                   0.0f, (float)halfWidth);
      path.closePath();

      return path;
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
    * Not implemented (returns empty string).
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowRoundCap(penWidth, repeated,
                                         reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }
}
