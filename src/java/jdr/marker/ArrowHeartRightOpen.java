// File          : ArrowHeartRightOpen.java
// Creation Date : 13th May 2008
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
 * Right facing open heart marker.
 * The basic marker shape looks like:
 * <img src="../images/heartrightOpenMarker.png" alt="[open right facing heart]">
 * This marker's shape depends on the marker size.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowHeartRightOpen extends JDRMarker
{
   /**
    * Creates right facing open heart shaped marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowHeartRightOpen(JDRLength penwidth, int repeat,
                       boolean isReversed, JDRLength arrowSize)
   {
      super(penwidth, repeat, isReversed, arrowSize);

      type = ARROW_HEART_RIGHT_OPEN;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"heartrightopen-"+size:
           "arrow-"+repeated+"heartrightopen-"+size;
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      double scale = 0.01*markerSize;

      path.moveTo(0.0f, 0.0f);
      path.curveTo((float)(-scale*18), (float)(scale*6),
                   (float)(-scale*27), (float)(scale*17),
                   (float)(-scale*28), (float)(scale*31.5));
      path.curveTo((float)(-scale*29), (float)(scale*46.5),
                   (float)(-scale*15), (float)(scale*62),
                   (float)(-scale*11), (float)(scale*57));
      path.curveTo((float)(scale*37), (float)(scale*52),
                   (float)(scale*65), (float)(scale*12),
                   (float)(scale*104), 0.0f);
      path.curveTo((float)(scale*65), (float)(-scale*12),
                   (float)(scale*37), (float)(-scale*52),
                   (float)(scale*11), (float)(-scale*57));
      path.curveTo((float)(-scale*15), (float)(-scale*62),
                   (float)(-scale*29), (float)(-scale*46.5),
                   (float)(-scale*28), (float)(-scale*31.5));
      path.curveTo((float)(-scale*27), (float)(-scale*17),
                   (float)(-scale*18), (float)(-scale*6),
                   0.0f, 0.0f);
      path.closePath();

      BasicStroke stroke = new BasicStroke((float)storageUnit.fromBp(1.0));

      Shape shape = stroke.createStrokedShape(path);

      if (shape instanceof GeneralPath)
      {
         return (GeneralPath)shape;
      }

      return new GeneralPath(shape);
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
      JDRMarker marker = new ArrowHeartRightOpen(penWidth, repeated,
                                    reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }
}
