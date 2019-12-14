// File          : JDRPatternPointIterator.java
// Creation Date : 10th Sept 2010
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
 * Iterates over all control points in a rotational pattern shape.
 * @author Nicola L C Talbot
 */

public class JDRPatternPointIterator extends JDRPointIterator
{
   public JDRPatternPointIterator(JDRShape path)
   {
      path_ = path;
      reset();
   }

   protected JDRPatternPointIterator()
   {
      path_ = null;
      reset();
   }

   public JDRPoint next() throws NoSuchElementException
   {
      JDRPattern pattern = (JDRPattern)path_;

      if (pointIndex == -1 || segmentIndex == -1)
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

      JDRPoint anchor = pattern.getPatternAnchor();

      if (point == anchor)
      {
         throw new NoSuchElementException();
      }

      if (pattern.hasAdjust() && point == pattern.getPatternAdjust())
      {
         point = anchor;  

         pointIndex++;
         index = 1;

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
         // Are we on the last point of the underlying shape?

         JDRPoint lastPoint 
            = pattern.getUnderlyingShape().getLastControl();

         if (point == lastPoint)
         {
            JDRPoint adjust = pattern.getPatternAdjust();

            pointIndex++;
            segmentIndex++;

            if (adjust != null)
            {
               // move to the adjust point

               index = 0;
               point = adjust;
            }
            else
            {
               // move to the point of rotation

               index = 1;
               point = anchor;
            }

            return point;
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

   public void set(int currentPointIndex, int currentSegmentIndex,
      JDRPoint currentPoint)
      throws ArrayIndexOutOfBoundsException,NoSuchElementException
   {
      pointIndex = currentPointIndex;
      segmentIndex = currentSegmentIndex;
      point = currentPoint;

      JDRPattern pattern = (JDRPattern)path_;

      JDRPoint adjust = pattern.getPatternAdjust();

      if (currentPoint == pattern.getPatternAnchor())
      {
         index = adjust == null ? 0 : 1;
         return;
      }

      if (currentPoint == adjust)
      {
         index = 0;
         return;
      }

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
}
