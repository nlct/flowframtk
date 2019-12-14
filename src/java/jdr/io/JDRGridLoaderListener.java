// File          : JDRGridLoaderListener.java
// Creation Date : 17th August 2010
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
 * JDRGrid loader listener interface.
 * @author Nicola L C Talbot
 * @see JDRGridLoader
 */

public interface JDRGridLoaderListener
{
   /**
    * Gets the number identifying the associated grid type
    * in JDR/AJR format.
    * @param version the JDR/AJR version number
    * @return object id
    */
   public byte getId(float version);

   public JDRGrid getGrid(JDRAJR jdr, JDRGrid grid, float version);

   /**
    * Writes the specified grid in JDR/AJR format. (Doesn't include
    * the grid ID returned by {@link #getId(float)} which is
    * dealt with by the grid loader.)
    * @param grid the grid to save
    * @throws IOException if I/O error occurs
    */
   public void write(JDRAJR jdr, JDRGrid grid) throws IOException;

   /**
    * Reads grid specified in JDR/AJR format. (Doesn't include the
    * grid ID returned by {@link #getId(float)} which is dealt
    * with by the grid loader.)
    * @throws InvalidFormatException if there is something wrong
    * with the format
    */
   public JDRGrid read(JDRAJR jdr) throws InvalidFormatException;
}
