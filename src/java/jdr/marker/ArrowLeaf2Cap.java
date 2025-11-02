// File          : ArrowLeaf2Cap.java
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
 * Leaf2 cap marker. The basic marker shape looks like:
 * <img src="../images/leaf2CapMarker.png" alt="[leaf2 shape]">
 * (for a path with pen width = 10 PostScript points).
 * This marker's shape depends on the associated path's
 * line width as well as the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowLeaf2Cap extends JDRMarker
{
   /**
    * Creates leaf2 marker for a path with the given pen width.
    * The marker may be repeated and/or reversed.
    */
   public ArrowLeaf2Cap(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowSize)
   {
      super(penwidth, repeat, isReversed, arrowSize);

      type = ARROW_LEAF2_CAP;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"leaf2cap-"+size+"-"+penWidth:
           "arrow-"+repeated+"leaf2cap-"+size+"-"+penWidth;
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

      double x = radius*(1-ROOT_3);
      double y = 0.8*width;

      double midX = x + radius;

      GeneralPath path = new GeneralPath();

      path.moveTo(x, 0);

      double c1 = y*ONE_OVER_ROOT_3;
      double c2 = midX-c1;

      path.curveTo(x, -c1, c2, -y, midX, -y);

      double len = 1.5*diameter;

      double endX = midX + len;

      double c3x = midX + radius*1.720465053;
      double c4x = endX - 0.6*radius;

      path.curveTo(c3x, -y,
                   c4x, 0,
                   endX, 0);

      path.curveTo(c4x, 0,
                   c3x, y,
                   midX, y);

      path.curveTo(c2, y, x, c1, x, 0);

      path.closePath();

      AffineTransform af = AffineTransform.getTranslateInstance(-x, 0);

      Shape shape = af.createTransformedShape(path);

      double angle = 0.5*Math.PI;

      af = AffineTransform.getRotateInstance(-angle);

      Area area = new Area(af.createTransformedShape(shape));

      af = AffineTransform.getRotateInstance(angle);

      area.add(new Area(af.createTransformedShape(shape)));

      return new GeneralPath(area);
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
      JDRMarker marker = new ArrowLeaf2Cap(penWidth, repeated,
                                         reversed, (JDRLength)size.clone());
      makeOtherEqual(marker);

      return marker;
   }

}
