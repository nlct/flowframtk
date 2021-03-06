// File          : ArrowDiamondOpen.java
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
 * Outline of diamond marker. The basic marker shape looks like:
 * <img src="../images/openDiamondMarker.png" alt="[outline of diamond]">
 * (the origin is off the centre of the shape.)
 * This is an outline of {@link ArrowDiamond} marker.
 * This marker's shape depends on the associated path's
 * line width.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowDiamondOpen extends JDRMarker
{
   /**
    * Creates outline of diamond marker.
    * The marker may be repeated and/or reversed.
    */
   public ArrowDiamondOpen(JDRLength penwidth, int repeat,
                       boolean isReversed)
   {
      super(penwidth, repeat, isReversed);

      type = ARROW_DIAMOND_OPEN;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"diamondopen-"+penWidth:
           "arrow-"+repeated+"diamondopen-"+penWidth;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double width = penWidth.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double a = 1.2*width;

      double midX = -5.0*a+a;
      double midY = 3.0*a;

      path.moveTo((float)(2.0*a), 0.0f);
      path.lineTo((float)midX, (float)midY);
      path.lineTo((float)(-11.0*a+a), 0.0f);
      path.lineTo((float)midX, (float)-midY);
      path.closePath();

      BasicStroke stroke = new BasicStroke((float)width);

      return new GeneralPath(stroke.createStrokedShape(path));
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
      JDRMarker marker = new ArrowDiamondOpen(penWidth, repeated,
                                   reversed);
      makeEqual(marker);

      return marker;
   }
}
