// File          : InvalidDimensionException.java
// Creation Date : 1st February 2006
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

import com.dickimawbooks.jdr.CanvasGraphics;

/**
 * Exception thrown when an invalid dimension is given.
 * @author Nicola L C Talbot
 */
public class InvalidDimensionException extends InvalidFormatException
{
   /**
    * Initialises with invalid dimension.
    * @param dimension string containing invalid dimension
    */
   public InvalidDimensionException(String dimension, CanvasGraphics cg)
   {
      super(cg.getStringWithValues(
            "error.id.unit", new String[] {dimension},
             "Invalid dimension "+dimension));
      invalidDimension = dimension;
   }

   /**
    * Initialises with invalid dimension and line number on which
    * the error occurs.
    * @param dimension string containing invalid dimension
    */
   public InvalidDimensionException(String dimension, int line, CanvasGraphics cg)
   {
      super(cg.getStringWithValues(line, 
            "error.id.unit", new String[] {dimension},
             "Invalid dimension "+dimension), line);
      invalidDimension = dimension;
   }

   /**
    * Gets the invalid dimension.
    * @return invalid dimension
    */
   public String getInvalidDimension()
   {
      return invalidDimension;
   }

   private String invalidDimension;
}
