// File          : ArrowTriangle.java
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
 * Right facing triangle marker.
 * The basic right facing triangle marker shape looks like:
 * <img src="../images/triangleMarker.png" alt="[filled right facing triangle]">
 * (the origin is to the right of the centre of the shape.)
 * This corresponds to the PGF <code>\pgfarrowtriangle</code>
 * arrow style.
 * This marker's shape depends on the associated path's
 * line width as well as the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowTriangle extends JDRMarker
{
   /**
    * Creates right facing triangle marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowTriangle(JDRLength penwidth, int repeat,
                       boolean isReversed, JDRLength arrowSize)
   {
      super(penwidth, repeat, isReversed, arrowSize);

      type = ARROW_TRIANGLE;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"triangle-"+size+"-"+penWidth:
           "arrow-"+repeated+"triangle-"+size+"-"+penWidth;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);
      double width = 0.7*penWidth.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double xa = markerSize + width;
      double ya = 0.577*markerSize + width;

      path.moveTo((float)-xa, (float)-ya);
      path.lineTo((float)(markerSize*penWidth.getValue(storageUnit)*0.65), 0.0f);
      path.lineTo((float)-xa, (float)ya);
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
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "\\pgfarrowtriangle{"+PGF.length(size)+"}";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowTriangle(penWidth, repeated,
                                    reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }
}
