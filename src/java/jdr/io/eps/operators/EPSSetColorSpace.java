// File          : EPSSetColorSpace.java
// Purpose       : class representing setcolorspace operator
// Creation Date : 1st June 2008
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
 * Object representing setcolorspace operator.
 * @author Nicola L C Talbot
 */
public class EPSSetColorSpace extends EPSOperator
{
   public EPSSetColorSpace()
   {
      super("setcolorspace");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      GraphicsState currentGraphicsState
         = eps.getCurrentGraphicsState();

      CanvasGraphics cg = eps.getCanvasGraphics();

      EPSObject object = stack.popObject();

      int n = 1;
      String name;
      EPSArray array = null;

      if (object instanceof EPSName)
      {
         name = ((EPSName)object).toString();
      }
      else
      {
         array = (EPSArray)object;

         if (!(array.get(0) instanceof EPSName))
         {
            throw new InvalidFormatException(
               "(setcolorspace) color space missing",
               eps.getLineNum());
         }

         name = ((EPSName)array.get(0)).toString();
         n = array.size();
      }

      if (name.equals("/DeviceRGB"))
      {
         if (n == 1)
         {
            currentGraphicsState.setPaint(new JDRColor(cg));
         }
         else if (n == 4)
         {
            double red = array.getDouble(1);
            double green = array.getDouble(2);
            double blue = array.getDouble(3);

            if (blue > 1)
            {
               blue = 1;
            }
            else if (blue < 0)
            {
               blue = 0;
            }

            if (green > 1)
            {
               green = 1;
            }
            else if (green < 0)
            {
               green = 0;
            }

            if (red > 1)
            {
               red = 1;
            }
            else if (red < 0)
            {
               red = 0;
            }

            currentGraphicsState.setPaint(
               new JDRColor(cg, red, green, blue));
         }
         else
         {
            throw new InvalidFormatException(
               "invalid number of parameters to setcolorspace array",
               eps.getLineNum());
         }
      }
      else if (name.equals("/DeviceCMYK"))
      {
         if (n == 1)
         {
            currentGraphicsState.setPaint(new JDRColorCMYK(cg));
         }
         else if (n == 5)
         {
            double cyan = array.getDouble(1);
            double magenta = array.getDouble(2);
            double yellow = array.getDouble(3);
            double black = array.getDouble(4);

            if (black > 1)
            {
               black = 1;
            }
            else if (black < 0)
            {
               black = 0;
            }

            if (yellow > 1)
            {
               yellow = 1;
            }
            else if (yellow < 0)
            {
               yellow = 0;
            }

            if (magenta > 1)
            {
               magenta = 1;
            }
            else if (magenta < 0)
            {
               magenta = 0;
            }

            if (cyan > 1)
            {
               cyan = 1;
            }
            else if (cyan < 0)
            {
               cyan = 0;
            }

            currentGraphicsState.setPaint(
               new JDRColorCMYK(cg, cyan, magenta, yellow, black));
         }
         else
         {
            throw new InvalidFormatException(
               "invalid number of parameters to setcolorspace array",
               eps.getLineNum());
         }
      }
      else if (name.equals("/DeviceGray"))
      {
         if (n == 1)
         {
            currentGraphicsState.setPaint(new JDRGray(cg));
         }
         else if (n == 2)
         {
            double gray = array.getDouble(1);

            if (gray > 1)
            {
               gray = 1;
            }
            else if (gray < 0)
            {
               gray = 0;
            }

            currentGraphicsState.setPaint(new JDRGray(cg, gray));
         }
         else
         {
            throw new InvalidFormatException(
               "invalid number of parameters to setcolorspace array",
               eps.getLineNum());
         }
      }
      else if (name.equals("/DeviceHSB"))
      {
         if (n == 1)
         {
            currentGraphicsState.setPaint(new JDRColorHSB(cg));
         }
         else if (n == 4)
         {
            double hue = array.getDouble(1);
            double saturation = array.getDouble(2);
            double brightness = array.getDouble(3);

            if (hue >= 1 || hue < 0)
            {
               hue = 0.0;
            }

            if (saturation > 1)
            {
               saturation = 1;
            }
            else if (saturation < 0)
            {
               saturation = 0;
            }

            if (brightness > 1)
            {
               brightness = 1;
            }
            else if (brightness < 0)
            {
               brightness = 0;
            }

            currentGraphicsState.setPaint(
               new JDRColorHSB(cg, hue*360, saturation, brightness));
         }
      }
      else
      {
         throw new InvalidFormatException("color space "
            +name+" not implemented", eps.getLineNum());
      }
   }

}
