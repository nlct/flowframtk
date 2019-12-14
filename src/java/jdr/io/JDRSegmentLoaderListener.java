// File          : JDRSegmentLoaderListener.java
// Creation Date : 29th February 2008
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
 * Loader listener for segments.
 * @author Nicola L C Talbot
 */

public class JDRSegmentLoaderListener implements JDRObjectLoaderListener
{
   /**
    * Gets the character identifying the associated segment type
    * in JDR/AJR format.
    * @param version the JDR/AJR version number
    * @return segment id
    */
   public char getId(float version)
   {
      return 'M';
   }

   public JDRObject getObject(JDRAJR jdr, JDRObject segment, float version)
   {
      return segment;
   }

   /**
    * Writes the specified segment in JDR format. (Doesn't include
    * the segment ID returned by {@link #getId(float)} which is 
    * dealt with by the segment loader.)
    * @param object the segment to save
    * @throws IOException if I/O error occurs
    */
   public void write(JDRAJR jdr, JDRObject object)
      throws IOException
   {
      float version = jdr.getVersion();

      double factor = (version < 1.8f ?
                       jdr.getCanvasGraphics().storageToBp(1.0) : 1.0);

      JDRPathSegment segment = (JDRPathSegment)object;

      if (version < 1.3f)
      {
         jdr.writeDouble(factor*segment.getStart().getX());
         jdr.writeDouble(factor*segment.getStart().getY());
      }

      jdr.writeDouble(factor*segment.getEnd().getX());
      jdr.writeDouble(factor*segment.getEnd().getY());
   }

   /**
    * Reads segment specified in JDR format. (Doesn't include the
    * segment ID returned by {@link #getId(float)} which is dealt
    * with by the segment loader.)
    * @param x x co-ordinate of current point
    * @param y y co-ordinate of current point
    * @throws InvalidFormatException if there is something wrong
    * with the segment format
    */
   public JDRObject read(JDRAJR jdr, double x, double y)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      double x0, x1, y0, y1;

      if (version < 1.3f)
      {
         x0 = jdr.readDouble(
            InvalidFormatException.SEGMENT_X0);
         y0 = jdr.readDouble(
            InvalidFormatException.SEGMENT_Y0);
      }
      else
      {
         x0 = x;
         y0 = y;
      }

      x1 = jdr.readDouble(
            InvalidFormatException.SEGMENT_X1);
      y1 = jdr.readDouble(
            InvalidFormatException.SEGMENT_Y1);

      return new JDRSegment(jdr.getCanvasGraphics(), x0, y0, x1, y1);
   }

   /**
    * Reads segment specified in JDR format. The current point is
    * set to (0,0).
    * @see #read(JDRAJR,double,double)
    */
   public JDRObject read(JDRAJR jdr)
      throws InvalidFormatException
   {
      return read(jdr, 0, 0);
   }

}
