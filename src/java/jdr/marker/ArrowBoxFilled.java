// File          : ArrowBoxFilled.java
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
 * Filled box marker. The basic filled box marker shape looks like:
 * <img src="../images/boxFilledMarker.png" alt="[filled square]">
 * (the origin is at the centre of the shape.)
 * This marker's shape does not depend on the associated path's
 * line width. Instead it depends on the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowBoxFilled extends JDRMarker
{
   /**
    * Creates filled box marker of given size.
    * The marker may be repeated and/or reversed. Since the box
    * marker is symmetrical, the reversed setting only has an
    * effect if the marker is repeated.
    */
   public ArrowBoxFilled(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength markerSize)
   {
      super(penwidth, repeat, isReversed, markerSize);

      type = ARROW_BOXFILLED;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"boxfilled":
           "arrow-"+repeated+"boxfilled";
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      double width = 2*markerSize;

      GeneralPath path = new GeneralPath(
         new Rectangle2D.Float((float)-markerSize, (float)-markerSize,
                             (float)width, (float)width));

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
      String width = size.getUnit().tex(2*size.getValue());

      return "\\pgfsetdash{}{0pt}\\pgfsetlinewidth{2bp}"
           + "\\pgfpathrectangle{\\pgfpoint{-"+PGF.length(size)+"}{-"
           + PGF.length(size) + "}}{\\pgfpoint{"
           + width+"}{" + width + "}}"
           + "\\pgfusepathqfillstroke";
   }

   public Object clone()
   {
      JDRMarker marker =  new ArrowBoxFilled(penWidth, repeated,
                                     reversed, (JDRLength)size.clone());
      makeOtherEqual(marker);

      return marker;
   }
}
