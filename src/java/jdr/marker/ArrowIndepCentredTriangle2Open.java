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
 * Triangle cap marker with both length and width but independent of
 * line width and centred on the vertex.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowIndepCentredTriangle2Open extends JDRMarker
{
   /**
    * Creates triangle marker for a path with the given pen width.
    * The marker may be repeated and/or reversed.
    */
   public ArrowIndepCentredTriangle2Open(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowLength, JDRLength arrowWidth)
   {
      super(penwidth, repeat, isReversed, arrowLength, arrowWidth);

      if (arrowWidth == null)
      {
         arrowWidth = (JDRLength)arrowLength.clone();
      }

      type = ARROW_INDEP_CENTRED_TRIANGLE2_OPEN;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"indepcentredtriangle2open-"+size+"-"+width:
           "arrow-"+repeated+"indepcentredtriangle2open-"+size+"-"+width;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerLength = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double halfWidth = 0.5*width.getValue(storageUnit);
      double halfLength = 0.5*markerLength;

      double penW = Math.min(storageUnit.fromBp(1.0),
        0.1 * (halfWidth + halfLength));

      path.moveTo((float)-halfLength, (float)-halfWidth);
      path.lineTo((float)halfLength, 0.0f);
      path.lineTo((float)-halfLength, (float)halfWidth);
      path.closePath();

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
      JDRMarker marker = new ArrowIndepCentredTriangle2Open(penWidth, repeated,
                                         reversed, (JDRLength)size.clone(),
                                         (JDRLength)width.clone());
      makeOtherEqual(marker);

      return marker;
   }
}
