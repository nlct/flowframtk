// File          : InvalidEPSObjectException.java
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
 * Exception thrown when an invalid EPS object is encountered.
 * @author Nicola L C Talbot
 */
public class InvalidEPSObjectException extends InvalidFormatException
{
   /**
    * Initialises exception with the message "Invalid Object".
    */
   public InvalidEPSObjectException()
   {
      super("Invalid Object");
   }

   /**
    * Initialises exception with the message "Invalid Object"
    * and given line number.
    * @param line the line number
    */
   public InvalidEPSObjectException(int line)
   {
      super("Invalid Object", line);
   }

   /**
    * Initialises exception with the given message.
    * @param message the error message
    */
   public InvalidEPSObjectException(String message)
   {
      super(message);
   }

   /**
    * Initialises exception with the given message and line
    * number.
    * @param message the error message
    * @param line the line number
    */
   public InvalidEPSObjectException(String message, int line)
   {
      super(message, line);
   }
}
