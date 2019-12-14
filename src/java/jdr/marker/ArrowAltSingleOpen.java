// File          : ArrowAltSingleOpen.java
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
 * Outline of LaTeX style marker.
 * The basic marker shape looks like:
 * <img src="../images/altSingleOpenMarker.png" alt="[outline of LaTeX arrow shape]">
 * This is an outline of {@link ArrowAltSingle}.
 * This marker's shape depends on the associated path's
 * line width as well as the given marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowAltSingleOpen extends JDRMarker
{
   /**
    * Creates outline of LaTeX style marker .
    * The marker may be repeated and/or reversed.
    */
   public ArrowAltSingleOpen(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength arrowSize)
   {
      super(penwidth, repeat, isReversed, arrowSize);

      type = ARROW_ALT_SINGLE_OPEN;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"altsingleopen-"+size+"-"+penWidth:
           "arrow-"+repeated+"altsingleopen-"+size+"-"+penWidth;

   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double y = 2*markerSize/3;

      path.moveTo((float)(2.0*penWidth.getValue(storageUnit)), 0.0f);
      path.lineTo((float)(-markerSize), (float)y);
      path.lineTo((float)(-0.5*markerSize), 0.0f);
      path.lineTo((float)(-markerSize), (float)(-y));
      path.closePath();

      BasicStroke stroke = new BasicStroke(1.0f);

      return new GeneralPath(stroke.createStrokedShape(path));
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
      JDRMarker marker = new ArrowAltSingleOpen(penWidth, repeated,
                                  reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }
}
