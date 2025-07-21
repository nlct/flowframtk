// File          : InvalidImageFormatException.java
// Creation Date : 12th June 2008
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
 * Exception thrown when a image's format is unrecognised.
 * @author Nicola L C Talbot
 */
public class InvalidImageFormatException extends InvalidFormatException
{
   /**
    * Initialises indicating the invalid image.
    * @param filename invalid image's filename
    */
   public InvalidImageFormatException(String filename, CanvasGraphics cg)
   {
      super(cg.getMessageWithFallback("error.invalid_imageformat",
              "Invalid image format"), (JDRAJR)null);
      setIdentifier(filename);
      invalidFilename = filename;
   }

   public InvalidImageFormatException(String filename, 
      CanvasGraphics cg, Throwable cause)
   {
      super(cg.getMessageWithFallback("error.invalid_imageformat",
              "Invalid image format"), null, cause);
      setIdentifier(filename);
      invalidFilename = filename;
   }

   /**
    * Initialises indicating the invalid image and the line number
    * where the error occurred.
    * @param filename invalid image's filename
    */
   public InvalidImageFormatException(String filename, JDRAJR jdr)
   {
      super(jdr.getMessageSystem().getMessageWithFallback("error.invalid_imageformat",
              "Invalid image format"), jdr);
      setIdentifier(filename);
      invalidFilename = filename;
   }

   public InvalidImageFormatException(String filename, JDRAJR jdr,
      Throwable cause)
   {
      super(jdr.getMessageSystem().getMessageWithFallback("error.invalid_imageformat",
              "Invalid image format"), jdr, cause);
      setIdentifier(filename);
      invalidFilename = filename;
   }

   /**
    * Gets the invalid filename.
    * @return invalid filename
    */
   public String getInvalidFilename()
   {
      return invalidFilename;
   }

   private String invalidFilename;
}
