// File          : ArrowBrace.java
// Creation Date : 28th April 2008
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
 * Brace marker. The basic marker shape looks like:
 * <img src="../images/braceMarker.png" alt="[ } shape]">
 * (the origin is at the right edge of the shape.)
 * This marker's shape depends on the associated path's
 * line width.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowBrace extends JDRMarker
{
   /**
    * Creates brace bracket marker.
    * The marker may be repeated and/or reversed.
    */
   public ArrowBrace(JDRLength penwidth, int repeat,
                      boolean isReversed)
   {
      super(penwidth, repeat, isReversed);

      type = ARROW_BRACE;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"brace":
           "arrow-"+repeated+"brace";
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double width = penWidth.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double dx = 4.0*width;
      double dy = 5.0*width;
      double c1x = 0.2125*dx;
      double c1y = 1.02*dy;
      double c2x = -1.2125*dx;
      double c2y = 0.08*dy;

      path.moveTo((float)-dx, (float)-dy);
      path.curveTo((float)c1x, (float)-c1y,
                   (float)c2x, (float)-c2y,
                   0.0f, 0.0f);
      path.curveTo((float)c2x, (float)c2y,
                   (float)c1x, (float)c1y,
                   (float)-dx, (float)dy);

      BasicStroke stroke = new BasicStroke((float)width,
         BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

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
    * Not implemented (returns empty string).
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowBrace(penWidth, repeated,
                                  reversed);
      makeEqual(marker);

      return marker;
   }
}
