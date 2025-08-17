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

package com.dickimawbooks.jdr.io;

import java.io.*;
import java.util.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Loader listener for closing moves.
 * @author Nicola L C Talbot
 */

public class JDRClosingMoveLoaderListener extends JDRSegmentLoaderListener
{
   /**
    * Gets the character identifying the associated segment type
    * in JDR/AJR format.
    * @param version the JDR/AJR version number
    * @return segment id
    */
   public char getId(float version)
   {
      return 'Z';
   }

   public JDRObject getObject(JDRAJR jdr, JDRObject segment, float version)
   {
      if (version < 2.1f)
      {
         return ((JDRClosingMove)segment).convertToNonClosingMove();
      }
      else
      {
         return segment;
      }
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
      double x1 = jdr.readDouble(
            InvalidFormatException.SEGMENT_X1);
      double y1 = jdr.readDouble(
            InvalidFormatException.SEGMENT_Y1);

      return new JDRClosingMove(jdr.getCanvasGraphics(), x, y, x1, y1);
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
