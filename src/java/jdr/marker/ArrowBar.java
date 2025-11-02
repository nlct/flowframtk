// File          : ArrowBar.java
// Date          : 1st February 2006
// Last Modified : 30 July 2007
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
 * Bar marker. The basic bar marker shape looks like:
 * <img src="../images/barMarker.png" alt="[ | shape]">
 * This corresponds to the PGF <code>\pgfarrowbar</code> arrow style.
 * This marker's shape only depends on the associated path's
 * line width.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowBar extends JDRMarker
{
   /**
    * Creates bar marker for a path with the given pen width.
    * The marker may be repeated and/or reversed. Since the bar
    * marker is symmetrical, the reversed setting only has an
    * effect if the marker is repeated.
    */
   public ArrowBar(JDRLength penwidth, int repeat,
                      boolean isReversed)
   {
      super(penwidth, repeat, isReversed);

      type = ARROW_BAR;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"bar":
           "arrow-"+repeated+"bar";
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double width = penWidth.getValue(storageUnit);

      double halfHeight = 5*width;

      GeneralPath path = new GeneralPath();

      path.moveTo(0.0f, (float)-halfHeight);
      path.lineTo(0.0f, (float)halfHeight);

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
      return "\\pgfarrowbar";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowBar(penWidth, repeated,
                                         reversed);
      makeOtherEqual(marker);

      return marker;
   }
}
