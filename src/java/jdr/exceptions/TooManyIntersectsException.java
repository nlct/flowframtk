// File          : TooManyIntersectsException.java
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
 * Exception thrown when scan line encounters too many intersects.
 * @author Nicola L C Talbot
 * @see JDRPath#parshape(Graphics2D,double,boolean)
 */
public class TooManyIntersectsException extends InvalidShapeException
{
   /**
    * Initialises indicating the co-ordinates where the error was
    * encountered.
    * @param x x co-ordinate
    * @param y y co-ordinate (height of scan line)
    */
   public TooManyIntersectsException(CanvasGraphics cg, double x, double y)
   {
      super(cg.getMessageWithAlt("Shape has too many intersects: x={0} y={1}",
            "error.shape.too_many_intersects", x, y));

      x_ = x;
      y_ = y;
   }

   /**
    * Gets the x co-ordinate where the error occurred.
    * @return x co-ordinate
    */
   public double getX()
   {
      return x_;
   }

   /**
    * Gets the y co-ordinate where the error occurred.
    * @return y co-ordinate
    */
   public double getY()
   {
      return y_;
   }

   private double x_, y_;
}
