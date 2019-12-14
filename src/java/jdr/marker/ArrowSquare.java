// File          : ArrowSquare.java
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
 * Square bracket marker. The basic square bracket marker shape looks like:
 * <img src="../images/squareMarker.png" alt="[ ] shape]">
 * (the origin is at the right edge of the shape.)
 * This marker corresponds to the PGF <code>\pgfarrowsquare</code>
 * arrow style.
 * This marker's shape depends on the associated path's
 * line width.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowSquare extends JDRMarker
{
   /**
    * Creates square bracket marker.
    * The marker may be repeated and/or reversed.
    */
   public ArrowSquare(JDRLength penwidth, int repeat,
                      boolean isReversed)
   {
      super(penwidth, repeat, isReversed);

      type = ARROW_SQUARE;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"square":
           "arrow-"+repeated+"square";
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double width = penWidth.getValue(storageUnit);

      double x = -3.0*width;
      double y = 5.0*width;

      GeneralPath path = new GeneralPath();

      path.moveTo((float)x, (float)-y);
      path.lineTo(0.0f, (float)-y);
      path.lineTo(0.0f, (float)y);
      path.lineTo((float)x, (float)y);

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
      return "\\pgfarrowsquare";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowSquare(penWidth, repeated,
                                  reversed);
      makeEqual(marker);

      return marker;
   }
}
