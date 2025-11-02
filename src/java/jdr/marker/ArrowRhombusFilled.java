// File          : ArrowRhombusFilled.java
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
 * Filled rhombus marker. The basic filled rhombus marker shape looks like:
 * <img src="../images/rhombusFilledMarker.png" alt="[filled rhombus]">
 * (the origin is at the centre of the shape.)
 * This marker's shape does not depend on the associated path's
 * line width. Instead it depends on the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowRhombusFilled extends JDRMarker
{
   /**
    * Creates filled rhombus marker of given size.
    * The marker may be repeated and/or reversed. Since the 
    * marker is symmetrical, the reversed setting only has an
    * effect if the marker is repeated.
    */
   public ArrowRhombusFilled(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength markerSize)
   {
      super(penwidth, repeat, isReversed, markerSize);

      type = ARROW_RHOMBUS_FILLED;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"rhombusfilled":
           "arrow-"+repeated+"rhombusfilled";
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double length = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      path.moveTo(0.0f, (float)length);
      path.lineTo((float)-length, 0.0f);
      path.lineTo(0.0f, (float)-length);
      path.lineTo((float)length, 0.0f);
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
      String sizefmt = PGF.format(size.getValue(JDRUnit.bp));

      return "\\pgfsetdash{}{0pt}\\pgfsetlinewidth{2bp}"
           + "\\pgfpathqmoveto{0bp}{"+sizefmt+"bp}"
           + "\\pgfpathqlineto{-"+sizefmt+"bp}{0bp}"
           + "\\pgfpathqlineto{0bp}{-"+sizefmt+"bp}"
           + "\\pgfpathqlineto{"+sizefmt+"bp}{0bp}"
           + "\\pgfclosepath"
           + "\\pgfusepathqfillstroke";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowRhombusFilled(penWidth, repeated,
                                         reversed, (JDRLength)size.clone());
      makeOtherEqual(marker);

      return marker;
   }
}
