// File          : JDRPointIterator.java
// Date          : 18th August 2010
// Last Modified : 18th August 2010
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
 * Iterates over all control points in a shape.
 * @author Nicola L C Talbot
 */

public class JDRPointIterator implements Iterator<JDRPoint>,Serializable
{
   public JDRPointIterator(JDRShape path)
   {
      path_ = path;
      reset();
   }

   protected JDRPointIterator()
   {
      path_ = null;
      reset();
   }

   /**
    * Returns true if path has more points.
    */
   public boolean hasNext()
   {
      // Is the path empty?

      if (path_.isEmpty()) return false;

      JDRPoint lastControl = path_.getLastControl();

      return (point != lastControl);
   }

   /**
    * Returns next point in the path.
    */
   public JDRPoint next() throws NoSuchElementException
   {
      if (pointIndex == -1)
      {
         point = path_.getFirstControl();

         if (point == null)
         {
            throw new NoSuchElementException();
         }

         pointIndex = 0;
         index = 0;
         segmentIndex = 0;

         return point;
      }

      JDRPathSegment currentSegment = path_.get(segmentIndex);
      JDRPathSegment lastSegment = path_.getLastSegment();

      int count = currentSegment.controlCount();

      if (path_.segmentHasEnd(currentSegment))
      {
         count++;
      }

      // Are we on the last segment?

      if (currentSegment == lastSegment)
      {
         // Are we on the last point?

         JDRPoint lastPoint = path_.getLastControl();

         if (point == lastPoint)
         {
            // Can't go any further

            throw new NoSuchElementException();
         }

         index++;

         if (path_.segmentHasEnd(currentSegment))
         {
            // Should we move to the end point
            // of this segment?

            if (index > count)
            {
               pointIndex++;
               point = currentSegment.getEnd();

               return point;
            }
         }
      }
      else
      {
         // move to the next point on this segment

         index++;

         // Have we gone off the last point of this segment?

         if (index >= count)
         {

            segmentIndex++;
            index = 0;

            try
            {
               currentSegment = path_.get(segmentIndex);
            }
            catch (IndexOutOfBoundsException e)
            {
               throw new NoSuchElementException(e.getMessage());
            }

            count = currentSegment.controlCount();

            if (path_.segmentHasEnd(currentSegment))
            {
               count++;
            }
         }
      }

      pointIndex++;

      if (index == currentSegment.controlCount())
      {
         point = currentSegment.getEnd();
      }
      else
      {
         try
         {
            point = currentSegment.getControl(index);
         }
         catch (IndexOutOfBoundsException e)
         {
            throw new NoSuchElementException(e.getMessage());
         }
      }

      return point;
   }

   public int getCurrentPointIndex()
   {
      return pointIndex;
   }

   public int getCurrentSegmentIndex()
   {
      return segmentIndex;
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
    * Resets the iterator.
    */
   public void reset()
   {
      point = null;
      index = -1;
      pointIndex = -1;
      segmentIndex = 0;
   }

   public void set(int currentPointIndex, int currentSegmentIndex,
      JDRPoint currentPoint)
      throws ArrayIndexOutOfBoundsException,NoSuchElementException
   {
      pointIndex = currentPointIndex;
      segmentIndex = currentSegmentIndex;
      point = currentPoint;

      JDRPathSegment segment = path_.get(currentSegmentIndex);

      if (path_.segmentHasEnd(segment) && point == segment.getEnd())
      {
         index = segment.controlCount();
      }
      else
      {
         index = segment.getControlIndex(point);
      }
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
    * Index of current point along current segment.
    */
   protected int index=-1;

   /**
    * Index of current point.
    */
   protected int pointIndex=-1;

   /**
    * Current point.
    */
   protected JDRPoint point=null;

   /**
    * Index of current segment.
    */
   protected int segmentIndex=0;
   
}
