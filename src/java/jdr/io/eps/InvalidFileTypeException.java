// File          : InvalidFileTypeException.java
// Date          : 1st February 2006
// Last Modified : 13th January 2007
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

package com.dickimawbooks.jdr.io.eps;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

// error.invalid_object
/**
 * Exception thrown when an invalid file identifier is encountered.
 * @author Nicola L C Talbot
 */
public class InvalidFileTypeException extends InvalidFormatException
{
   /**
    * Initialises exception with the message "Invalid file type "
    * followed by the invalid type.
    * @param type invalid file type
    */
   public InvalidFileTypeException(int type)
   {
      super("Invalid file type "+type);

      id = type;
   }

   /**
    * Initialises exception with the message "Invalid file type "
    * followed by the invalid type.
    * @param type invalid file type
    * @param line the line number
    */
   public InvalidFileTypeException(int type, int line)
   {
      super("Invalid file type "+type, line);

      id = type;
   }

   public int getID()
   {
      return id;
   }

   private int id;
}
