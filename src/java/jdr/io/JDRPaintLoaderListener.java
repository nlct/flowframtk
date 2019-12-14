// File          : JDRPaintLoaderListener.java
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
 * JDRPaint loader listener interface.
 * @author Nicola L C Talbot
 * @see JDRPaintLoader
 */

public interface JDRPaintLoaderListener
{
   /**
    * Gets the character identifying the associated paint type
    * in JDR/AJR format.
    * @param version the JDR/AJR version number
    * @return paint id
    */
   public char getId(float version);

   public JDRPaint getPaint(JDRAJR jdr, JDRPaint paint, float version);

   /**
    * Writes the specified paint in JDR/AJR format. (Doesn't include
    * the paint ID returned by {@link #getId(float)} which is 
    * dealt with by the paint loader.)
    * @param paint the paint to save
    * @throws IOException if I/O error occurs
    */
   public void write(JDRAJR jdr, JDRPaint paint)
      throws IOException;

   /**
    * Reads paint specified in JDR/AJR format. (Doesn't include the
    * paint ID returned by {@link #getId(float)} which is dealt
    * with by the paint loader.)
    * @throws InvalidFormatException if there is something wrong
    * with the paint format
    */
   public JDRPaint read(JDRAJR jdr)
      throws InvalidFormatException;

   /**
    * Gets configuration ID associated with the paint type.
    * This is used to identify paint in FlowframTk's configuration
    * file.
    * @return configuration id
    */
   public int getConfigId();

   /**
    * Gets configuration file representation of the specified
    * paint. This does not include the paint ID which is 
    * specified by {@link #getConfigId()}.
    * @param paint the specified paint
    */
   public String getConfigString(JDRPaint paint);

   /**
    * Parses configuration file paint specification.
    * This does not include the paint ID which is 
    * specified by {@link #getConfigId()}.
    * @param paintspecs the paint specification
    * @throws InvalidFormatException if there is something wrong
    * with the paint format
    * @return the paint described by <code>paintspecs</code>
    */
   public JDRPaint parseConfig(CanvasGraphics cg, String paintspecs)
      throws InvalidFormatException;

   /**
    * Gets the remainder of the specs String after it has been
    * parsed by {@link #parseConfig(CanvasGraphics,String)}.
    */
   public String getConfigRemainder();
}
