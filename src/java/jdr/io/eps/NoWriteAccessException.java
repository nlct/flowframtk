// File          : NoWriteAccessException.java
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
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.dickimawbooks.jdr.io.eps;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Exception thrown when attempting to write to an EPS object
 * which doesn't have write access.
 * @author Nicola L C Talbot
 */
public class NoWriteAccessException extends InvalidFormatException
{
   /**
    * Initialises exception with the message "No write access".
    */
   public NoWriteAccessException()
   {
      super("No write access");
   }

   /**
    * Initialises exception with the message "No write access"
    * and line number.
    */
   public NoWriteAccessException(int line)
   {
      super("No write access", line);
   }

   /**
    * Initialises exception with the given message.
    * @param message the error message
    */
   public NoWriteAccessException(String message)
   {
      super(message);
   }

   /**
    * Initialises exception with the given message and line
    * number.
    * @param message the error message
    * @param line the line number
    */
   public NoWriteAccessException(String message, int line)
   {
      super(message, line);
   }
}
