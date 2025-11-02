// File          : ArrowInvertFastCap.java
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
 * Combination of inverted triangle cap and inverted chevron marker.
 * The basic marker shape looks like:
 * <img src="../images/invertFastCapMarker.png" alt="[inverted fast shape]">
 * (for a path with pen width = 10 PostScript points).
 * This marker's shape depends on the associated path's
 * line width as well as the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowInvertFastCap extends JDRMarker
{
   /**
    * Creates marker for a path with the given pen width.
    * The marker may be repeated and/or reversed.
    */
   public ArrowInvertFastCap(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowSize)
   {
      super(penwidth, repeat, isReversed, arrowSize);

      type = ARROW_INVERT_FAST_CAP;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"invertfastcap-"+size+"-"+penWidth:
           "arrow-"+repeated+"invertfastcap-"+size+"-"+penWidth;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double halfW = 0.5*penWidth.getValue(storageUnit);
      double halfWplusSize = halfW+markerSize;

      double doubleSize = 2.0*markerSize;
      double doubleSizePlusHalfW = doubleSize + halfW;

      path.moveTo(0.0f, (float)-halfW);
      path.lineTo((float)halfW, (float)-halfW);
      path.lineTo(0.0f, 0.0f);
      path.lineTo((float)halfW, (float)halfW);
      path.lineTo(0.0f, (float)halfW);
      path.closePath();

      path.moveTo((float)halfWplusSize, (float)-halfW);
      path.lineTo((float)markerSize, 0.0f);
      path.lineTo((float)halfWplusSize, (float)halfW);
      path.lineTo((float)(doubleSizePlusHalfW), (float)halfW);
      path.lineTo((float)(doubleSize), 0.0f);
      path.lineTo((float)(doubleSizePlusHalfW), (float)-halfW);
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
      JDRMarker marker = new ArrowInvertFastCap(penWidth, repeated,
                                         reversed, (JDRLength)size.clone());
      makeOtherEqual(marker);

      return marker;
   }
}
