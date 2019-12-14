// File          : ArrowCircleOpen.java
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
 * Outline of circle marker. The basic marker shape looks like:
 * <img src="../images/openCircleMarker.png" alt="[outline of circle]">
 * (the origin is off the centre of the shape.)
 * This is an outline of {@link ArrowCircle} marker.
 * This marker's shape depends on the associated path's
 * line width as well as the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowCircleOpen extends JDRMarker
{
   /**
    * Creates outline of circle marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowCircleOpen(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowSize)
   {
      super(penwidth, repeat, isReversed, arrowSize);

      type = ARROW_CIRCLE_OPEN;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"circleopen-"+size+"-"+penWidth:
           "arrow-"+repeated+"circleopen-"+size+"-"+penWidth;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      double width = penWidth.getValue(storageUnit);

      double radius = markerSize + 0.6*width;

      // centre of circle
      double x = 0.5*width - markerSize;
      double y = 0.0;

      float diameter = (float)(2.0*radius);

      Ellipse2D circle = new Ellipse2D.Float(
         (float)(x-radius), (float)(y-radius),
         diameter, diameter);

      BasicStroke stroke = new BasicStroke((float)width);

      return new GeneralPath(stroke.createStrokedShape(circle));
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
      return "\\pgfarrowcircle{"+PGF.format(size.getValue(JDRUnit.bp))+"bp}";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowCircleOpen(penWidth, repeated,
                                  reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }
}
