// File          : InvalidMappingException.java
// Creation Date : 2014-03-29
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

package com.dickimawbooks.jdr.exceptions;

import java.io.File;

import com.dickimawbooks.jdr.CanvasGraphics;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;

/**
 * Exception thrown when a mapping is invalid.
 * @author Nicola L C Talbot
 */
public class InvalidMappingException extends InvalidFormatException
{
   public InvalidMappingException(File file, String name, 
      String found, int lineNum, JDRMessageDictionary msgDict)
   {
      super(msgDict.getMessageWithFallback(
       "error.io.invalid_map",
       "Invalid mapping ''{0}={1}''",
        name, found), lineNum);
      init(file, name, found);
   }

   public InvalidMappingException(File file, String name, 
      String found, int lineNum, JDRMessageDictionary msgDict, Throwable cause)
   {
      super(msgDict.getMessageWithFallback(
       "error.io.invalid_map",
       "Invalid mapping ''{0}={1}''",
        name, found), lineNum, cause);
      init(file, name, found);
   }

   public InvalidMappingException(File file, String name, String found,
      int lineNum, CanvasGraphics cg)
   {
      this(file, name, found, lineNum, cg.getMessageDictionary());
   }

   public InvalidMappingException(File file, String name, 
      String found, int lineNum, CanvasGraphics cg, Throwable cause)
   {
      this(file, name, found, lineNum, cg.getMessageDictionary(), cause);
   }

   private void init(File file, String name, String found)
   {
      this.file = file;
      invalidType = name;
      this.found = found;
      setIdentifier(file.getName());
   }

   /**
    * Gets the invalid type.
    * @return invalid type
    */
   public String getInvalidType()
   {
      return invalidType;
   }

   public String getFound()
   {
      return found;
   }

   /**
    * Gets the file in which the invalid mapping was found.
    * @return file
    */
   public File getFile()
   {
      return file;
   }

   private String invalidType, found;
   private File file;

   public static final String KEYVAL = "key-value";
   public static final String VALUE = "value";
}
