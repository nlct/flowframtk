// File          : JDRPartialBezierLoaderListener.java
// Creation Date : 18th August 2010
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

package com.dickimawbooks.jdr.io;

import java.io.*;
import java.util.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Loader listener for partial B&eacute;zier segments.
 * @author Nicola L C Talbot
 */

public class JDRPartialBezierLoaderListener extends JDRSegmentLoaderListener
{
   /**
    * Gets the character identifying the partial B&eacute;zier type
    * in JDR/AJR format.
    * @param version the JDR/AJR version number
    * @return segment id
    */
   public char getId(float version)
   {
      return 'b';
   }

   /**
    * Reads partial B&eacute;zier specified in JDR/AJR format. (Doesn't include the
    * segment ID returned by {@link #getId(float)} which is dealt
    * with by the segment loader.)
    * @throws InvalidFormatException if there is something wrong
    * with the segment format
    * @return the line segment read from the input stream
    */
   public JDRObject read(JDRAJR jdr)
      throws InvalidFormatException
   {
      double cx = jdr.readDouble(InvalidFormatException.PARTIAL_BEZIER_CX);
      double cy = jdr.readDouble(InvalidFormatException.PARTIAL_BEZIER_CY);

      CanvasGraphics cg = jdr.getCanvasGraphics();

      return new JDRPartialBezier(cg, null, new JDRPoint(cg, cx, cy), null);
   }

   public void write(JDRAJR jdr, JDRObject segment)
      throws IOException
   {
      JDRPartialBezier curve = (JDRPartialBezier)segment;

      double factor = (jdr.getVersion() < 1.8f ?
                       jdr.getCanvasGraphics().storageToBp(1.0) : 1.0);

      JDRPoint c = curve.getControl1();

      jdr.writeDouble(factor*c.getX());
      jdr.writeDouble(factor*c.getY());
   }

}
