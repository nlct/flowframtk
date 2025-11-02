// File          : ArrowStar.java
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
 * Star marker. The basic star marker shape looks like:
 * <img src="../images/starMarker.png" alt="[star shaped]">
 * (the origin is at the centre of the shape.)
 * This marker's shape does not depend on the associated path's
 * line width. Instead it depends on the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowStar extends JDRMarker
{
   /**
    * Creates star marker of given size.
    * The marker may be repeated and/or reversed. Since the 
    * marker is symmetrical, the reversed setting only has an
    * effect if the marker is repeated.
    */
   public ArrowStar(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength markerSize)
   {
      super(penwidth, repeat, isReversed, markerSize);

      type = ARROW_STAR;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"star":
           "arrow-"+repeated+"star";
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      path.moveTo((float)-markerSize, 0.0f);
      path.lineTo((float)markerSize, 0.0f);
      path.moveTo(0.0f, (float)markerSize);
      path.lineTo(0.0f, (float)-markerSize);
      // as above but rotated 45 degress
      double a = markerSize*0.707107;
      path.moveTo((float)-a, (float)-a);
      path.lineTo((float)a, (float)a);
      path.moveTo((float)a, (float)-a);
      path.lineTo((float)-a, (float)a);

      BasicStroke stroke = new BasicStroke((float)storageUnit.fromBp(2.0));

      Shape shape = stroke.createStrokedShape(path);

      return new GeneralPath(shape);
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

      double a = size.getValue(JDRUnit.bp)*0.707107;

      return "\\pgfsetdash{}{0pt}\\pgfsetlinewidth{2bp}"
           + "\\pgfpathqmoveto{-"+sizefmt+"bp}{0bp}"
           + "\\pgfpathqlineto{"+sizefmt+"bp}{0bp}"
           + "\\pgfpathqmoveto{0bp}{"+sizefmt+"bp}"
           + "\\pgfpathqlineto{0bp}{"+sizefmt+"bp}"
           + "\\pgfpathqmoveto{"+PGF.format(-a)+"bp}{"
           + PGF.format(-a)+"bp}"
           + "\\pgfpathqlineto{"+PGF.format(a)+"bp}{"
           + PGF.format(a)+"bp}"
           + "\\pgfpathqmoveto{"+PGF.format(a)+"bp}{"
           + PGF.format(-a)+"bp}"
           + "\\pgfpathqlineto{"+PGF.format(-a)+"bp}{"
           + PGF.format(a)+"bp}"
           + "\\pgfusepathqstroke";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowStar(penWidth, repeated,
                                reversed, (JDRLength)size.clone());
      makeOtherEqual(marker);

      return marker;
   }
}
