// File          : ArrowClubCap.java
// Creation Date : 2012-03-11
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
 * Club cap marker. The basic marker shape looks like:
 * <img src="../images/clubCapMarker.png" alt="[club shape]">
 * (for a path with pen width = 10 PostScript points).
 * This marker's shape depends on the associated path's
 * line width as well as the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowClubCap extends JDRMarker
{
   /**
    * Creates club marker for a path with the given pen width.
    * The marker may be repeated and/or reversed.
    */
   public ArrowClubCap(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowSize)
   {
      super(penwidth, repeat, isReversed, arrowSize);

      type = ARROW_CLUB_CAP;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"clubcap-"+size+"-"+penWidth:
           "arrow-"+repeated+"clubcap-"+size+"-"+penWidth;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      double width = penWidth.getValue(storageUnit);

      double radius = 0.15*width*markerSize;

      double diameter = 2*radius;

      double x = radius*(HALF_ROOT_3-1);

      Shape shape = new Ellipse2D.Double(x,
         -radius, diameter, diameter);

      AffineTransform af = AffineTransform.getTranslateInstance(-x, 0);

      Area area = new Area(af.createTransformedShape(shape));

      af = AffineTransform.getRotateInstance(-HALF_PI);

      area.add(new Area(af.createTransformedShape(shape)));

      af = AffineTransform.getRotateInstance(HALF_PI);

      area.add(new Area(af.createTransformedShape(shape)));

      af = AffineTransform.getTranslateInstance(x, 0);

      return new GeneralPath(af.createTransformedShape(area));
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
      JDRMarker marker = new ArrowClubCap(penWidth, repeated,
                                         reversed, (JDRLength)size.clone());
      makeOtherEqual(marker);

      return marker;
   }

}
