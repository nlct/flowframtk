// File          : ArrowHookDown.java
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
 * Hook down marker. The basic hook down marker shape looks like:
 * <img src="../images/hookDownMarker.png" alt="[ hook down shape]">
 * (the origin is at the top left of the shape.)
 * This marker's shape depends on the associated path's
 * line width as well as the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowHookDown extends JDRMarker
{
   /**
    * Creates hook down marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowHookDown(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowSize)
   {
      super(penwidth, repeat, isReversed, arrowSize);

      type = ARROW_HOOK_DOWN;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"hookdown-"+size+"-"+penWidth:
           "arrow-"+repeated+"hookdown-"+size+"-"+penWidth;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      double halfSize = 0.5*markerSize;

      GeneralPath path = new GeneralPath();

      path.moveTo(0.0f, 0.0f);
      path.curveTo((float)halfSize, (float)(0.2*markerSize),
                   (float)halfSize, (float)(0.8*markerSize),
                   0.0f, (float)markerSize);

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
    * Not implemented (returns empty string).
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "";
   }

   public boolean hasXAxisSymmetry()
   {
      return false;
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowHookDown(penWidth, repeated,
                                 reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }
}
