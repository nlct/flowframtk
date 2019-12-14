// File          : EPSSetColor.java
// Purpose       : class representing setcolor operator
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
 * Object representing setcolor operator.
 * @author Nicola L C Talbot
 */
public class EPSSetColor extends EPSOperator
{
   public EPSSetColor()
   {
      super("setcolor");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      GraphicsState currentGraphicsState
         = eps.getCurrentGraphicsState();

      JDRPaint paint = currentGraphicsState.getPaint();

      CanvasGraphics cg = eps.getCanvasGraphics();

      if (paint instanceof JDRColor)
      {
         double blue  = stack.popDouble();
         double green = stack.popDouble();
         double red   = stack.popDouble();

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
            new JDRColor(cg,red, green, blue));
      }
      else if (paint instanceof JDRColorCMYK)
      {
         double black   = stack.popDouble();
         double yellow  = stack.popDouble();
         double magenta = stack.popDouble();
         double cyan    = stack.popDouble();

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
            new JDRColorCMYK(cg,cyan, magenta, yellow, black));
      }
      else if (paint instanceof JDRColorHSB)
      {
         double brightness = stack.popDouble();
         double saturation = stack.popDouble();
         double hue        = stack.popDouble();

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
            new JDRColorHSB(cg,hue*360, saturation, brightness));
      }
      else if (paint instanceof JDRGray)
      {
         double gray = stack.popDouble();

         if (gray > 1)
         {
            gray = 1;
         }
         else if (gray < 0)
         {
            gray = 0;
         }

         currentGraphicsState.setPaint(new JDRGray(cg,gray));
      }
   }

}
