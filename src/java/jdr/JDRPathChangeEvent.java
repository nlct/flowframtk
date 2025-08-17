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

package com.dickimawbooks.jdr;

public class JDRPathChangeEvent
{
   public JDRPathChangeEvent(JDRPath path,
     JDRPathChangeEvent.Type type)
   {
      this(path, -1, type);
   }

   public JDRPathChangeEvent(JDRPath path, int segmentIndex,
     JDRPathChangeEvent.Type type)
   {
      this(path, segmentIndex, type, null, null);
   }

   public JDRPathChangeEvent(JDRPath path, int segmentIndex,
     JDRPathChangeEvent.Type type, JDRPathSegment oldSegment,
     JDRPathSegment newSegment)
   {
      if (path == null)
      {
         throw new NullPointerException();
      }

      this.path = path;
      this.segmentIndex = segmentIndex;
      this.type = type;
      this.oldSegment = oldSegment;
      this.newSegment = newSegment;

      switch (type)
      {
         case SEGMENT_ADDED:
         case SEGMENT_INSERTED:
         case SEGMENT_REMOVED:
         case SEGMENT_CHANGED:

          if (segmentIndex < 0 || segmentIndex > path.size())
          {
             throw new ArrayIndexOutOfBoundsException(
               "Invalid segment index "+segmentIndex
               +" for change type "+type);
          }
      }
   }

   public JDRPath getPath()
   {
      return path;
   }

   public int getIndex()
   {
      return segmentIndex;
   }

   public JDRPathSegment getOldSegment()
   {
      return oldSegment;
   }

   public JDRPathSegment getNewSegment()
   {
      return newSegment;
   }

   public JDRPathChangeEvent.Type getChangeType()
   {
      return type;
   }

   public boolean isConsumed()
   {
      return isConsumed;
   }

   public void consume()
   {
      isConsumed = true;
   }

   JDRPath path;
   int segmentIndex; // may be -1 if not applicable
   JDRPathChangeEvent.Type type;
   boolean isConsumed = false;
   JDRPathSegment oldSegment, newSegment;// may be null if not applicable

   public static enum Type
   {
      SEGMENT_ADDED,
      SEGMENT_INSERTED,
      SEGMENT_REMOVED,
      SEGMENT_CHANGED,
      CONTROLS_ADJUSTED,
      PATH_CHANGED,
      PATH_OPENED,
      PATH_CLOSED
   }
}
