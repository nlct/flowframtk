// File          : CanOnlyMergePathsException.java
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
 * Exception thrown when there is an attempt to merge an object
 * other than a {@link JDRPath}.
 * @author Nicola L C Talbot
 */
public class CanOnlyMergePathsException extends InvalidPathException
{
   public CanOnlyMergePathsException(CanvasGraphics cg)
   {
      super(cg.getMessageWithFallback("internal_error.can_only_merge_paths",
          "can only merge paths"));
   }
}
