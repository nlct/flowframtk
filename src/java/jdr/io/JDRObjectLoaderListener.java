// File          : JDRObjectLoaderListener.java
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
 * JDRObject loader listener interface.
 * @author Nicola L C Talbot
 * @see JDRObjectLoader
 */

public interface JDRObjectLoaderListener
{
   /**
    * Gets the character identifying the associated object type
    * in JDR/AJR format.
    * @param version the JDR/AJR version number
    * @return JDR/AJR object id
    */
   public char getId(float version);

   /**
    * Gets the object for the given version number. 
    * Returns the given object if valid for the given version number
    * otherwise returns the closest equivalent object that's valid
    * for the given version.
    */ 
   public JDRObject getObject(JDRAJR jdr, JDRObject object, float version);


   /**
    * Writes the specified object in JDR/AJR format. (Doesn't include
    * the object ID returned by {@link #getId(float)} or
    * flowframe or description data which are 
    * dealt with by the object loader.)
    * @param object the object to save
    * @throws IOException if I/O error occurs
    */
   public void write(JDRAJR jdr, JDRObject object)
     throws IOException;

   /**
    * Reads object specified in JDR/AJR format. (Doesn't include the
    * object ID returned by {@link #getId(float)} or flowframe
    * or description data which are dealt
    * with by the object loader.)
    * @throws InvalidFormatException if there is something wrong
    * with the object format
    */
   public JDRObject read(JDRAJR jdr)
     throws InvalidFormatException;

}
