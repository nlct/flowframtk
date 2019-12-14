// File          : JDRSymmetricPathIterator.java
// Date          : 1st August 2010
// Last Modified : 1st September 2010
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
 * Class representing an iterator for JDRSymmetricPath objects.
 * @author Nicola L C Talbot
 */

public class JDRSymmetricPathIterator extends JDRPathIterator 
   implements Serializable
{
   public JDRSymmetricPathIterator(JDRSymmetricPath path)
   {
      super(path);
   }

   /**
    * Returns true if path has more segments.
    */
   public boolean hasNext()
   {
      return (index < ((JDRSymmetricPath)path_).getTotalPathSegments());
   }

   /**
    * Returns next segment in the path. Doesn't include the line of
    * symmetry
    */
   public JDRPathSegment next()
   throws NoSuchElementException
   {
      JDRSymmetricPath symPath = (JDRSymmetricPath)path_;

      int size = symPath.getUnderlyingShape().size();
      int totalSegments = symPath.getTotalPathSegments();

      if (index >= totalSegments)
      {
         throw new NoSuchElementException();
      }

      JDRPathSegment segment;

      if (index < size)
      {
         segment = path_.get(index);
      }
      else if (index == size && !symPath.isAnchored())
      {
         segment = symPath.getJoin();
      }
      else if (index == totalSegments-1 && 
              symPath.getClosingSegment() != null)
      {
         segment = symPath.getClosingSegment();
      }
      else
      {
         segment = symPath.getReflected(totalSegments-index
                    -(symPath.getClosingSegment()==null?1:2))
                 .reverse();
      }

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

         if (index == totalSegments-1)
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

}
