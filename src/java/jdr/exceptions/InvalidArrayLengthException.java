// File          : InvalidArrayLengthException.java
// Creation Date : 2014-03-26
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

import com.dickimawbooks.jdr.io.JDRAJR;
import com.dickimawbooks.jdr.CanvasGraphics;

/**
 * Exception thrown when an array length is invalid.
 * @author Nicola L C Talbot
 */
public class InvalidArrayLengthException extends InvalidFormatException
{
   public InvalidArrayLengthException(int length, CanvasGraphics cg)
   {
      super(cg.getStringWithValues(
        "error.io.invalid_array_length",
        new String[] {String.format("%d", length)},
        String.format("Invalid array length %d", length)), (JDRAJR)null);
      invalidArrayLength = length;
      setIdentifier(null);
   }

   public InvalidArrayLengthException(int length, JDRAJR jdr)
   {
      this(null, length, jdr);
   }

   public InvalidArrayLengthException(String name, int length, CanvasGraphics cg)
   {
      super(cg.getStringWithValues(
        "error.io.invalid_array_length",
        new String[] {String.format("%d", length)},
        String.format("Invalid array length %d", length)), (JDRAJR)null);
      invalidArrayLength = length;
      setIdentifier(name);
   }

   public InvalidArrayLengthException(String name, int length, 
      JDRAJR jdr)
   {
      super(jdr.getCanvasGraphics().getStringWithValues(
        "error.io.invalid_array_length",
        new String[] {String.format("%d", length)},
        String.format("Invalid array length %d", length)), jdr);
      invalidArrayLength = length;
      setIdentifier(name);
   }

   public InvalidArrayLengthException(String name, int length, 
     JDRAJR jdr, Throwable cause)
   {
      super(jdr.getCanvasGraphics().getStringWithValues(
        "error.io.invalid_array_length",
        new String[] {String.format("%d", length)},
        String.format("Invalid array length %d", length)), jdr, cause);
      invalidArrayLength = length;
      setIdentifier(name);
   }

   /**
    * Gets the invalid length.
    * @return invalid length
    */
   public int getInvalidArrayLength()
   {
      return invalidArrayLength;
   }

   private int invalidArrayLength;
}
