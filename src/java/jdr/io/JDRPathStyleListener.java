// File          : JDRPathStyleListener.java
// Creation Date : 29th August 2010
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
 * Path style listener interface.
 * @author Nicola L C Talbot
 * @see JDRPathStyleLoader
 */

public interface JDRPathStyleListener
{
   /**
    * Gets the number identifying the associated path style type
    * in JDR/AJR format.
    * @param version the JDR/AJR version number
    * @return path style id
    */
   public byte getId(float version);

   public JDRShape getShape(JDRAJR jdr, JDRShape shape, float version);

   /**
    * Writes the path style of the specified shape in JDR/AJR format.
    * (Doesn't include
    * the path style ID returned by {@link #getId(float)} which
    * is dealt with by the path style loader.)
    * @param shape the shape whose path style needs to be saved
    * @throws IOException if I/O error occurs
    */
   public void write(JDRAJR jdr, JDRShape shape) throws IOException;

   /**
    * Reads path style specified in JDR/AJR format. (Doesn't include the
    * path style ID returned by {@link #getId(float)} which is dealt
    * with by the path style loader.)
    * @param shape the shape whose path style needs to be set
    * @throws InvalidFormatException if there is something wrong
    * with the object format
    */
   public void read(JDRAJR jdr, JDRShape shape) throws InvalidFormatException;

}
