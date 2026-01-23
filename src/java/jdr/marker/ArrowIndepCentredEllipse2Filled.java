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
 * Ellipse cap marker with both length and width but independent of
 * line width and centred on the vertex.
 * See {@link JDRMarker} for a description of markers.
 *
 */
public class ArrowIndepCentredEllipse2Filled extends JDRMarker
{
   /**
    * Creates ellipse marker for a path with the given pen width.
    * The marker may be repeated and/or reversed.
    */
   public ArrowIndepCentredEllipse2Filled(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowLength, JDRLength arrowWidth)
   {
      super(penwidth, repeat, isReversed, arrowLength, arrowWidth);

      if (arrowWidth == null)
      {
         arrowWidth = (JDRLength)arrowLength.clone();
      }

      type = ARROW_INDEP_CENTRED_ELLIPSE2_FILLED;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"indepcentredellipse2filled-"+size+"-"+width:
           "arrow-"+repeated+"indepcentredellipse2filled-"+size+"-"+width;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerLength = size.getValue(storageUnit);
      double markerWidth = width.getValue(storageUnit);

      Ellipse2D ellipse = new Ellipse2D.Double(
       -0.5*markerLength, -0.5*markerWidth, markerLength, markerWidth);

      return new GeneralPath(ellipse);
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
      JDRMarker marker = new ArrowIndepCentredEllipse2Filled(penWidth, repeated,
                                         reversed, (JDRLength)size.clone(),
                                         (JDRLength)width.clone());
      makeOtherEqual(marker);

      return marker;
   }
}
