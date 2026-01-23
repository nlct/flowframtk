/*
    Copyright (C) 2026 Nicola L.C. Talbot

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
 * Diamond cap marker with both length and width but independent of
 * line width and centred on the vertex.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowIndepCentredDiamond2Open extends JDRMarker
{
   /**
    * Creates diamond marker for a path with the given pen width.
    * The marker may be repeated and/or reversed.
    */
   public ArrowIndepCentredDiamond2Open(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowLength, JDRLength arrowWidth)
   {
      super(penwidth, repeat, isReversed, arrowLength, arrowWidth);

      if (arrowWidth == null)
      {
         arrowWidth = (JDRLength)arrowLength.clone();
      }

      type = ARROW_INDEP_CENTRED_DIAMOND2_OPEN;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"indepcentreddiamond2open-"+size+"-"+width:
           "arrow-"+repeated+"indepcentreddiamond2open-"+size+"-"+width;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerLength = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      float halfWidth = 0.5f*(float)width.getValue(storageUnit);
      float halfLength = 0.5f*(float)markerLength;

      path.moveTo(-halfLength, 0f);
      path.lineTo(0f, -halfWidth);
      path.lineTo(halfLength, 0f);
      path.lineTo(0f, halfWidth);
      path.closePath();

      double penW = Math.min(storageUnit.fromBp(1.0),
        0.1 * (halfWidth + halfLength));

      BasicStroke stroke = new BasicStroke((float)penW);
      Shape shape = stroke.createStrokedShape(path);

      return new GeneralPath(shape);
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
      return false;
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
      JDRMarker marker = new ArrowIndepCentredDiamond2Open(penWidth, repeated,
                                         reversed, (JDRLength)size.clone(),
                                         (JDRLength)width.clone());
      makeOtherEqual(marker);

      return marker;
   }
}
