// File          : JDRColorListener.java
// Creation Date : 29th February 2008
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

package com.dickimawbooks.jdr.io;

import java.io.*;
import java.util.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Loader listener for RGB paint.
 * @author Nicola L C Talbot
 */

public class JDRColorListener implements JDRPaintLoaderListener
{
   public char getId(float version)
   {
      return 'R';
   }

   public JDRPaint getPaint(JDRAJR jdr, JDRPaint paint, float version)
   {
      return paint;
   }

   public void write(JDRAJR jdr, JDRPaint paint)
      throws IOException
   {
      JDRColor c = (JDRColor)paint;

      jdr.writeFloat((float)c.getRed());
      jdr.writeFloat((float)c.getGreen());
      jdr.writeFloat((float)c.getBlue());
      jdr.writeFloat((float)c.getAlpha());
   }

   public JDRPaint read(JDRAJR jdr)
      throws InvalidFormatException
   {
      double r = jdr.readFloat(
         InvalidFormatException.RED, 0f, 1f, true, true);

      double g = jdr.readFloat(
         InvalidFormatException.GREEN, 0f, 1f, true, true);

      double b = jdr.readFloat(
         InvalidFormatException.BLUE, 0f, 1f, true, true);

      double a = jdr.readFloat(
         InvalidFormatException.ALPHA, 0f, 1f, true, true);

      return new JDRColor(jdr.getCanvasGraphics(), r,g,b,a);
   }

   public int getConfigId()
   {
      return 1;
   }

   /**
    * @param paint the paint to save (must be {@link JDRColor}).
    */
   public String getConfigString(JDRPaint paint)
   {
      JDRColor c = (JDRColor)paint;

      return "" + c.getRed() 
           +"," + c.getGreen()
           +"," + c.getBlue()
           +"," + c.getAlpha();
   }

   public JDRPaint parseConfig(CanvasGraphics cg, String specs)
      throws InvalidFormatException
   {
      String split[] = specs.split(",",5);

      if (split.length < 4)
      {
         throw new InvalidValueException(
           InvalidFormatException.RGB, specs, cg);
      }     

      double red   = 0;
      double green = 0;
      double blue  = 0;
      double alpha = 0;

      try
      {
         red = Double.parseDouble(split[0]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
           InvalidFormatException.RED, split[0], cg, e);
      }

      if (red < 0 || red > 1)
      {
         throw new InvalidValueException(
            InvalidFormatException.RED, red, cg);
      }

      try
      {
         green = Double.parseDouble(split[1]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
           InvalidFormatException.GREEN, split[1], cg, e);
      }

      if (green < 0 || green > 1)
      {
         throw new InvalidValueException(
            InvalidFormatException.GREEN, green, cg);
      }

      try
      {
         blue = Double.parseDouble(split[2]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.BLUE, split[2], cg, e);
      }

      if (blue < 0 || blue > 1)
      {
         throw new InvalidValueException(
            InvalidFormatException.BLUE, blue, cg);
      }

      try
      {
         alpha = Double.parseDouble(split[3]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.ALPHA, split[3], cg, e);
      }

      if (alpha < 0 || alpha > 1)
      {
         throw new InvalidValueException(
            InvalidFormatException.ALPHA, alpha, cg);
      }

      if (split.length == 5)
      {
         remainder = split[4];
      }
      else
      {
         remainder = "";
      }

      return new JDRColor(cg, red, green, blue, alpha);
   }

   /**
    * Gets the remainder of the specs String after it has been
    * parsed by {@link #parseConfig(CanvasGraphics,String)}.
    */
   public String getConfigRemainder()
   {
      return remainder;
   }

   private String remainder="";
}
