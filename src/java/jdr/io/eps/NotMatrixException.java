// File          : NotMatrixException.java
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
 * Exception thrown when a matrix is expected
 * but not found.
 * @author Nicola L C Talbot
 */
public class NotMatrixException extends InvalidFormatException
{
   /**
    * Initialises exception with the message "Not a matrix".
    */
   public NotMatrixException()
   {
      super("Not a matrix");
   }

   /**
    * Initialises exception with the message "Not a matrix"
    * and line number.
    * @param line the line number
    */
   public NotMatrixException(int line)
   {
      super("Not a matrix", line);
   }

   /**
    * Initialises exception with the given message.
    * @param message the error message
    */
   public NotMatrixException(String message)
   {
      super(message);
   }

   /**
    * Initialises exception with the given message and line number.
    * @param message the error message
    * @param line the line number
    */
   public NotMatrixException(String message, int line)
   {
      super(message, line);
   }
}
