// File          : Parshape.java
// Date          : 1st February 2006
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

import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Stores <code>\parshape</code> and <code>\shapepar</code> 
 * graphics and settings. This class is just used to store
 * the information obtained when computing the parameters
 * for <code>\parshape</code> and <code>\shapepar</code>.
 * @author Nicola L C Talbot
 * @see JDRPath#parshape(double,boolean)
 * @see JDRPath#shapepar(double,boolean)
 */

public class Parshape
{
   /**
    * Creates a new parshape object.
    * @param str string containing TeX commands to set this
    * paragraph shape
    * @param shape the scan lines used to obtain the required
    * parameters
    * @param outLine the original shape used to obtain the
    * required parameters
    */
   public Parshape(String str, Shape shape, Shape outLine)
   {
      string    = str;
      scanlines = shape;
      outline   = outLine;
   }

   /**
    * Draws the outline and scan lines. The outline is drawn in
    * grey, and the scan lines are drawn in light grey.
    * @param g graphics device
    */
   public void draw(Graphics g)
   {
      Graphics2D g2 = (Graphics2D) g;

      g2.setColor(Color.gray);
      g2.draw(outline);
      g2.setColor(Color.lightGray);
      g2.draw(scanlines);
   }

   /**
    * String containing TeX commands to set this paragraph shape.
    */
   public String string;
   /**
    * Scan lines used to obtain the required parameters.
    */
   public Shape scanlines;
   /**
    * Outline used to obtain the required parameters.
    */
   public Shape outline;
}
