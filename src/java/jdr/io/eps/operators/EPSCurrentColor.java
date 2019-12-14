// File          : EPSCurrentColor.java
// Purpose       : class representing currentcolor operator
// Date          : 1st June 2008
// Last Modified : 1st June 2008
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

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
package com.dickimawbooks.jdr.io.eps.operators;

import java.io.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.eps.*;
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Object representing currentcolor operator.
 * @author Nicola L C Talbot
 */
public class EPSCurrentColor extends EPSOperator
{
   public EPSCurrentColor()
   {
      super("currentcolor");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      JDRPaint paint = eps.getCurrentGraphicsState().getPaint();

      if (paint instanceof JDRColor)
      {
         JDRColor c = (JDRColor)paint;

         stack.pushDouble(c.getRed());
         stack.pushDouble(c.getGreen());
         stack.pushDouble(c.getBlue());
      }
      else if (paint instanceof JDRColorCMYK)
      {
         JDRColorCMYK c = (JDRColorCMYK)paint;

         stack.pushDouble(c.getCyan());
         stack.pushDouble(c.getMagenta());
         stack.pushDouble(c.getYellow());
         stack.pushDouble(c.getKey());
      }
      else if (paint instanceof JDRColorHSB)
      {
         JDRColorHSB c = (JDRColorHSB)paint;

         stack.pushDouble(c.getHue()/360.0);
         stack.pushDouble(c.getSaturation());
         stack.pushDouble(c.getBrightness());
      }
      else if (paint instanceof JDRGray)
      {
         JDRGray c = (JDRGray)paint;

         stack.pushDouble(c.getGray());
      }
   }
}
