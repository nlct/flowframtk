/*
    Copyright (C) 2025 Nicola L.C. Talbot

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
import com.dickimawbooks.jdr.JDRClosingMove;
import com.dickimawbooks.jdr.JDRPath;

public class ClosingMoveException extends InvalidPathException
{
   public ClosingMoveException(JDRPath path, JDRClosingMove segment, int segmentIndex)
   {
      this(path.getCanvasGraphics(), path, segment, segmentIndex);
   }

   public ClosingMoveException(CanvasGraphics cg,
     JDRPath path, JDRClosingMove segment, int segmentIndex)
   {
      super(cg.getMessageWithFallback("error.close_subpath",
            "Unable to close sub path"));
      this.path = path;
      this.segment = segment;
      this.segmentIndex = segmentIndex;
   }

   public ClosingMoveException(JDRPath path, JDRClosingMove segment, int segmentIndex,
     Throwable cause)
   {
      this(path.getCanvasGraphics(), path, segment, segmentIndex, cause);
   }

   public ClosingMoveException(CanvasGraphics cg,
     JDRPath path, JDRClosingMove segment, int segmentIndex, Throwable cause)
   {
      super(cg.getMessageWithFallback("error.close+subpath",
            "Unable to close sub path"), cause);
      this.path = path;
      this.segment = segment;
      this.segmentIndex = segmentIndex;
   }

   public JDRPath getPath()
   {
      return path;
   }

   public JDRClosingMove getSegment()
   {
      return segment;
   }

   public int getSegmentIndex()
   {
      return segmentIndex;
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return path.getCanvasGraphics();
   }

   JDRPath path;
   JDRClosingMove segment;
   int segmentIndex;
}
