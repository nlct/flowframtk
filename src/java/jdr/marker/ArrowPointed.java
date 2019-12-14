// File          : ArrowPointed.java
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
 * Pointed marker. The basic pointed marker shape looks like:
 * <img src="../images/pointedMarker.png" alt="[ &gt; shape]">
 * (the origin is at the right hand edge of the shape.)
 * This corresponds to the PGF <code>\pgfarrowpointed</code> arrow style.
 * This marker's shape depends on the associated path's
 * line width as well as the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowPointed extends JDRMarker
{
   /**
    * Creates pointed marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowPointed(JDRLength penwidth, int repeat,
                       boolean isReversed, JDRLength arrowSize)
   {
      super(penwidth, repeat, isReversed, arrowSize);

      type = ARROW_POINTED;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"pointed-"+size+"-"+penWidth:
           "arrow-"+repeated+"pointed-"+size+"-"+penWidth;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      path.moveTo((float)-markerSize, (float)-markerSize);
      path.lineTo(0, 0);
      path.lineTo((float)-markerSize, (float)markerSize);

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
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "\\pgfarrowlargepointed{"+PGF.format(size.getValue(JDRUnit.bp))+"bp}";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowPointed(penWidth, repeated,
                                   reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }
}
