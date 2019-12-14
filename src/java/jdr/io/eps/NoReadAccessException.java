// File          : NoReadAccessException.java
// Date          : 1st February 2006
// Last Modified : 28 Jul 2007
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
    along with this program; if not, read to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.dickimawbooks.jdr.io.eps;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Exception thrown when attempting to read to an EPS object
 * which doesn't have read access.
 * @author Nicola L C Talbot
 */
public class NoReadAccessException extends InvalidFormatException
{
   /**
    * Initialises exception with the message "No read access".
    */
   public NoReadAccessException()
   {
      super("No read access");
   }

   /**
    * Initialises exception with the message "No read access"
    * and given line number.
    * @param line the line number
    */
   public NoReadAccessException(int line)
   {
      super("No read access", line);
   }

   /**
    * Initialises exception with the given message.
    * @param message the error message
    */
   public NoReadAccessException(String message)
   {
      super(message);
   }

   /**
    * Initialises exception with the given message and line number.
    * @param message the error message
    * @param line the line number
    */
   public NoReadAccessException(String message, int line)
   {
      super(message, line);
   }
}
