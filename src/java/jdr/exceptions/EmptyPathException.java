// File          : EmptyPathException.java
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
import com.dickimawbooks.jdr.io.JDRMessageDictionary;

/**
 * Exception thrown when non-empty path is required.
 */
public class EmptyPathException extends InvalidPathException
{
   public EmptyPathException(CanvasGraphics cg)
   {
      this(cg.getMessageDictionary());
   }

   public EmptyPathException(JDRMessageDictionary msgSys)
   {
      super(msgSys.getString("internal_error.empty_path", "Empty Path"));
   }
}
