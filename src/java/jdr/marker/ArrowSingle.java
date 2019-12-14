// File          : ArrowSingle.java
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
 * LaTeX style marker. The basic LaTeX style marker shape looks like:
 * <img src="../images/singleMarker.png" alt="[LaTeX arrow shape]">
 * This corresponds to the PGF <code>\pgfarrowsingle</code> arrow
 * (hence the name ArrowSingle).
 * This marker's shape depends on the associated path's
 * line width.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowSingle extends JDRMarker
{
   /**
    * Creates LaTeX style marker corresponding to <code>\pgfarrowsingle</code>.
    * The marker may be repeated and/or reversed.
    */
   public ArrowSingle(JDRLength penwidth, int repeat,
                      boolean isReversed)
   {
      super(penwidth, repeat, isReversed);

      type = ARROW_SINGLE;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"single":
           "arrow-"+repeated+"single";

   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double width = penWidth.getValue(storageUnit);

      GeneralPath path = new GeneralPath();

      path.moveTo((float)(2.0*width), 0.0f);
      path.lineTo((float)(-6.0*width), (float)(4.0*width));
      path.lineTo((float)(-3.0*width), 0.0f);
      path.lineTo((float)(-6.0*width), (float)(-4.0*width));
      path.closePath();

      return path;
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
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "\\pgfarrowsingle";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowSingle(penWidth, repeated,
                                  reversed);
      makeEqual(marker);

      return marker;
   }
}
