// File          : ArrowAltBrace.java
// Creation Date : 6th May 2008
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
 * Alternative brace marker. The basic marker shape looks like:
 * <img src="../images/braceMarker.png" alt="[ } shape]">
 * (the origin is at the right edge of the shape.)
 * This is like {@link ArrowBrace} but has an associated size.
 * This marker's shape depends on the associated path's
 * line width as well as the marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowAltBrace extends JDRMarker
{
   /**
    * Creates brace bracket marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowAltBrace(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowSize)
   {
      super(penwidth, repeat, isReversed, arrowSize);

      type = ARROW_ALT_BRACE;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"altbrace-"+size:
           "arrow-"+repeated+"altbrace-"+size;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double dx = 0.8*markerSize;
      double dy = markerSize;
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

      BasicStroke stroke = new BasicStroke((float)penWidth.getValue(storageUnit),
         BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

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
    * Not implemented (returns empty string).
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "";
   }

   public Object clone()
   {
      JDRMarker marker  = new ArrowAltBrace(penWidth, repeated,
                                  reversed, (JDRLength)size.clone());
      makeOtherEqual(marker);

      return marker;
   }
}
