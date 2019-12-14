// File          : JDRPathIterator.java
// Date          : 1st August 2010
// Last Modified : 21st August 2010
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

package com.dickimawbooks.jdr;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing an iterator for JDRPath objects.
 * @author Nicola L C Talbot
 */

public class JDRPathIterator implements Iterator<JDRPathSegment>,Serializable
{
   public JDRPathIterator(JDRShape path)
   {
      path_ = path;
      index = 0;
   }

   protected JDRPathIterator()
   {
      path_ = null;
      index = 0;
   }

   public int pathSize()
   {
      return path_.size();
   }

   /**
    * Returns true if path has more segments.
    */
   public boolean hasNext()
   {
      return (index < pathSize());
   }

   /**
    * Returns true if the path has a previous segment.
    */
   public boolean hasPrev()
   {
      return index > 0;
   }

   /**
    * Returns next segment in the path.
    */
   public JDRPathSegment next()
   throws NoSuchElementException
   {
      int size = pathSize();

      if (index >= size)
      {
         throw new NoSuchElementException();
      }

      JDRPathSegment segment = path_.get(index);

      if (path_.getStroke() instanceof JDRBasicStroke)
      {
         JDRBasicStroke stroke = (JDRBasicStroke)path_.getStroke();

         if (index == 0)
         {
            segment.setStartMarker(stroke.getStartArrow());
         }
         else
         {
            segment.setStartMarker(null);
         }

         if (index == size-1)
         {
            segment.setEndMarker(stroke.getEndArrow());
         }
         else
         {
            segment.setEndMarker(stroke.getMidArrow());
         }
      }

      index++;

      return segment;
   }

   /**
    * Returns previous segment in the path.
    */
   public JDRPathSegment prev()
   throws NoSuchElementException
   {
      index--;

      if (index < 0)
      {
         throw new NoSuchElementException();
      }

      JDRPathSegment segment = path_.get(index);

      if (path_.getStroke() instanceof JDRBasicStroke)
      {
         JDRBasicStroke stroke = (JDRBasicStroke)path_.getStroke();

         if (index == 0)
         {
            segment.setStartMarker(stroke.getStartArrow());
         }
         else
         {
            segment.setStartMarker(null);
         }

         if (index == pathSize()-1)
         {
            segment.setEndMarker(stroke.getEndArrow());
         }
         else
         {
            segment.setEndMarker(stroke.getMidArrow());
         }
      }

      return segment;

   }

   /**
    * Unsupported.
    */
   public void remove()
     throws UnsupportedOperationException,
            IllegalStateException
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Resets the iterator to the starting segment.
    */
   public void reset()
   {
      index = 0;
   }

   /**
    * Resets the iterator.
    * @param toStart if true reset to the starting segment otherwise
    * reset to the last segment
    */
   public void reset(boolean toStart)
   {
      index = (toStart ? 0 : pathSize());
   }

   /**
    * Gets the underlying path.
    */
   public JDRShape getPath()
   {
      return path_;
   }

   /**
    * Path this is iterating over.
    */
   protected JDRShape path_;

   /**
    * Index of current segment.
    */
   protected int index=0;
}
