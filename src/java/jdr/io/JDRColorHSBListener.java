// File          : JDRColorHSBListener.java
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
 * Loader listener for HSB paint.
 * @author Nicola L C Talbot
 */

public class JDRColorHSBListener implements JDRPaintLoaderListener
{
   public char getId(float version)
   {
      return 'S';
   }

   public JDRPaint getPaint(JDRAJR jdr, JDRPaint paint, float version)
   {
      if (version < 1.4f)
      {
         return paint.getJDRColor();
      }

      return paint;
   }

   public void write(JDRAJR jdr, JDRPaint paint)
      throws IOException
   {
      JDRColorHSB c = (JDRColorHSB)paint;

      float version = jdr.getVersion();

      if (version < 1.4f)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.UNSUPPORTED_VERSION,
            paint.getClass().getName()+" ("+version+")", jdr);
      }

      jdr.writeFloat((float)c.getHue());
      jdr.writeFloat((float)c.getSaturation());
      jdr.writeFloat((float)c.getBrightness());
      jdr.writeFloat((float)c.getAlpha());
   }

   public JDRPaint read(JDRAJR jdr)
      throws InvalidFormatException
   {
      double h = jdr.readFloat(
         InvalidFormatException.HUE, 0f, 360f, true, false);

      double s = jdr.readFloat(
         InvalidFormatException.SATURATION, 0f, 1f, true, true);

      double b = jdr.readFloat(
         InvalidFormatException.BRIGHTNESS, 0f, 1f, true, true);

      double a = jdr.readFloat(
         InvalidFormatException.ALPHA, 0f, 1f, true, true);

      return new JDRColorHSB(jdr.getCanvasGraphics(), h,s,b,a);
   }

   public int getConfigId()
   {
      return 5;
   }

   /**
    * @param paint the paint to save (must be {@link JDRColorHSB}).
    */
   public String getConfigString(JDRPaint paint)
   {
      JDRColorHSB c = (JDRColorHSB)paint;

      return "" + c.getHue() 
           +"," + c.getSaturation()
           +"," + c.getBrightness()
           +"," + c.getAlpha();
   }

   public JDRPaint parseConfig(CanvasGraphics cg, String specs)
      throws InvalidFormatException
   {
      String split[] = specs.split(",",5);

      if (split.length < 4)
      {
         throw new InvalidValueException(
           InvalidFormatException.HSB, specs, cg);
      }     

      double hue = 0;
      double saturation = 0;
      double brightness  = 0;
      double alpha   = 0;

      try
      {
         hue = Double.parseDouble(split[0]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.HUE, split[0], cg, e);
      }

      if (hue < 0.0 || hue >= 360)
      {
         throw new InvalidValueException(
            InvalidFormatException.HUE, hue, cg);
      }

      try
      {
         saturation = Double.parseDouble(split[1]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.SATURATION, split[1], cg, e);
      }

      if (saturation < 0.0 || saturation > 1.0)
      {
         throw new InvalidValueException(
            InvalidFormatException.SATURATION, saturation, cg);
      }

      try
      {
         brightness = Double.parseDouble(split[2]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.BRIGHTNESS, split[2], cg, e);
      }

      if (brightness < 0.0 || brightness > 1.0)
      {
         throw new InvalidValueException(
            InvalidFormatException.BRIGHTNESS, brightness, cg);
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

      if (alpha < 0.0 || alpha > 1.0)
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

      return new JDRColorHSB(cg, hue, saturation, brightness, alpha);
   }

   /**
    * Gets the remainder of the specs String after it has been
    * parsed by {@link #parseConfig(String)}.
    */
   public String getConfigRemainder()
   {
      return remainder;
   }

   private String remainder="";
}
