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
 * Triangle cap marker with both length and width offset so that its
 * base is before the vertex with a minor protuberance.
 * This marker's shape does depend on the associated path's
 * line width.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowOffsetTriangle2 extends JDRMarker
{
   /**
    * Creates triangle marker for a path with the given pen width.
    * The marker may be repeated and/or reversed.
    */
   public ArrowOffsetTriangle2(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowLength, JDRLength arrowWidth)
   {
      super(penwidth, repeat, isReversed, arrowLength, arrowWidth);

      if (arrowWidth == null)
      {
         arrowWidth = (JDRLength)arrowLength.clone();
      }

      type = ARROW_OFFSET_TRIANGLE2;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"offsettriangle2cap-"+size+"-"+width+"-"+penWidth:
           "arrow-"+repeated+"offsettriangle2cap-"+size+"-"+width+"-"+penWidth;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double penW = penWidth.getValue(storageUnit);
      double halfPenWidth = 0.5 * penW;

      double markerLength = size.getValue(storageUnit)+halfPenWidth;

      double w = width.getValue(storageUnit) + penW;
      double halfWidth = 0.5*w;

      double protrusion = markerLength * penW / w;
      double offset = markerLength - protrusion;

      GeneralPath path = new GeneralPath();

      path.moveTo((float)-offset, (float)-halfWidth);
      path.lineTo((float)protrusion, 0.0f);
      path.lineTo((float)-offset, (float)halfWidth);
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
      JDRMarker marker = new ArrowOffsetTriangle2(penWidth, repeated,
                                         reversed, (JDRLength)size.clone(),
                                         (JDRLength)width.clone());
      makeOtherEqual(marker);

      return marker;
   }
}
