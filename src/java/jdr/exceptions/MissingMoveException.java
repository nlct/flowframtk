// File          : MissingMoveException.java
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

import com.dickimawbooks.jdr.JDRPath;
import com.dickimawbooks.jdr.CanvasGraphics;

/**
 * Exception thrown when a shape does not start 
 * with an initial starting point. Most graphics systems start a
 * path with an initial move, but note that a {@link JDRPath}'s 
 * starting point is obtained from the starting point of the first
 * segment.
 * @author Nicola L C Talbot
 */
public class MissingMoveException extends InvalidPathException
{
   public MissingMoveException(CanvasGraphics cg)
   {
      super(cg.getString("internal_error.missing_move", "Missing move"));
   }
}

