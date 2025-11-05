/*
    Copyright (C) 2025 Nicola L.C. Talbot

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
 * Triangle cap marker with both length and width and dependent on the
 * line width.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowDepTriangle2 extends JDRMarker
{
   /**
    * Creates triangle marker for a path with the given pen width.
    * The marker may be repeated and/or reversed.
    */
   public ArrowDepTriangle2(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowLength, JDRLength arrowWidth)
   {
      super(penwidth, repeat, isReversed, arrowLength, arrowWidth);

      if (arrowWidth == null)
      {
         arrowWidth = (JDRLength)arrowLength.clone();
      }

      type = ARROW_DEP_TRIANGLE2;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"deptriangle2-"+size+"-"+width+"-"+penWidth:
           "arrow-"+repeated+"deptriangle2-"+size+"-"+width+"-"+penWidth;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double halfPenWidth = 0.5*penWidth.getValue(storageUnit);

      double markerLength = size.getValue(storageUnit)+halfPenWidth;

      GeneralPath path = new GeneralPath();

      double halfWidth = 0.5*width.getValue(storageUnit)+halfPenWidth;

      path.moveTo(0.0f, (float)-halfWidth);
      path.lineTo((float)markerLength, 0.0f);
      path.lineTo(0.0f, (float)halfWidth);
      path.closePath();

      return path;
   }

   @Override
   public boolean supportsWidth()
   {
      return true;
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
      JDRMarker marker = new ArrowDepTriangle2(penWidth, repeated,
                                         reversed, (JDRLength)size.clone(),
                                         (JDRLength)width.clone());
      makeOtherEqual(marker);

      return marker;
   }
}
