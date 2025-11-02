// File          : ArrowTriangleDownFilled.java
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
 * Filled downward facing triangle marker.
 * The basic filled downward facing triangle marker shape looks like:
 * <img src="../images/triangleDownFilledMarker.png" alt="[filled downward facing triangle]">
 * (the origin is at the centre of the shape.)
 * This marker's shape does not depend on the associated path's
 * line width. Instead it depends on the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowTriangleDownFilled extends JDRMarker
{
   /**
    * Creates filled downward facing triangle marker of given size.
    * The marker may be repeated and/or reversed. Since the 
    * marker is symmetrical, the reversed setting only has an
    * effect if the marker is repeated.
    */
   public ArrowTriangleDownFilled(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength markerSize)
   {
      super(penwidth, repeat, isReversed, markerSize);

      type = ARROW_TRIANGLE_DOWN_FILLED;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"triangledownfilled":
           "arrow-"+repeated+"triangledownfilled";
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double length = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double x = length*0.866025;
      double y = length*0.5;

      path.moveTo(0.0f, (float)length);
      path.lineTo((float)-x, (float)-y);
      path.lineTo((float)x, (float)-y);
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
    * @deprecated
    */
   protected String pgfarrow()
   {
      double markerSize = size.getValue(JDRUnit.bp);

      double x = markerSize*0.866025;
      double y = markerSize*0.5;

      return "\\pgfsetdash{}{0pt}\\pgfsetlinewidth{2bp}"
           + "\\pgfpathqmoveto{0bp}{"+PGF.format(-markerSize)+"bp}"
           + "\\pgfpathqlineto{"+PGF.format(-x)+"bp}{"
           + PGF.format(y)+"bp}"
           + "\\pgfpathqlineto{"+PGF.format(x)+"bp}{"
           + PGF.format(y)+"bp}"
           + "\\pgfclosepath"
           + "\\pgfusepathqfillstroke";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowTriangleDownFilled(penWidth, repeated,
                                         reversed, (JDRLength)size.clone());
      makeOtherEqual(marker);

      return marker;
   }

   public boolean hasXAxisSymmetry()
   {
      return false;
   }
}
