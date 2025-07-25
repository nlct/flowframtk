// File          : MissingTypeBlockException.java
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
 * Exception thrown when the typeblock has not been defined when 
 * exporting to a LaTeX package based on the flowframe package.
 * @author Nicola L C Talbot
 */
public class MissingTypeBlockException extends InvalidFormatException
{
   /**
    * Initialises with the given message.
    * @param message error message
    */
   public MissingTypeBlockException(CanvasGraphics cg)
   {
      super(cg.getMessageWithFallback("error.no_typeblock", "Typeblock not set"));
   }
}
