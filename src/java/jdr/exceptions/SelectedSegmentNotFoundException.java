// File          : SelectedSegmentNotFoundException.java
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
 * Exception thrown when the selected segment can't be found in the
 * path that is being edited.
 * @author Nicola L C Talbot
 */
public class SelectedSegmentNotFoundException extends InvalidPathException
{
   public SelectedSegmentNotFoundException(CanvasGraphics cg)
   {
      super(cg.getString("internal_error.cant_find_selected_segment",
         "Can't find selected segment"));
   }
}
