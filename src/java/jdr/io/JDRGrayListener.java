// File          : JDRGrayListener.java
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
 * Loader listener for grey paint.
 * @author Nicola L C Talbot
 */

public class JDRGrayListener implements JDRPaintLoaderListener
{
   public char getId(float version)
   {
      return 'Y';
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
      float version = jdr.getVersion();

      if (version < 1.4f)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.UNSUPPORTED_VERSION,
            paint.getClass().getName()+" ("+version+")", jdr);
      }

      JDRGray c = (JDRGray)paint;

      jdr.writeFloat((float)c.getGray());
      jdr.writeFloat((float)c.getAlpha());
   }

   public JDRPaint read(JDRAJR jdr)
      throws InvalidFormatException
   {
      double g = jdr.readFloat(
         InvalidFormatException.GRAY, 0f, 1f, true, true);

      double a = jdr.readFloat(
         InvalidFormatException.ALPHA, 0f, 1f, true, true);

      return new JDRGray(jdr.getCanvasGraphics(), g,a);
   }

   public int getConfigId()
   {
      return 6;
   }

   /**
    * @param paint the paint to save (must be {@link JDRGray}).
    */
   public String getConfigString(JDRPaint paint)
   {
      JDRGray c = (JDRGray)paint;

      return "" + c.getGray() 
           +"," + c.getAlpha();
   }

   public JDRPaint parseConfig(CanvasGraphics cg, String specs)
      throws InvalidFormatException
   {
      String split[] = specs.split(",", 3);

      if (split.length < 2)
      {
         throw new InvalidValueException(
            InvalidFormatException.GRAY, specs, cg);
      }     

      double grey  = 0;
      double alpha = 0;

      try
      {
         grey = Double.parseDouble(split[0]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
           InvalidFormatException.GRAY, split[0], cg, e);
      }

      if (grey < 0.0 || grey > 1.0)
      {
         throw new InvalidValueException(
            InvalidFormatException.GRAY, grey, cg);
      }

      try
      {
         alpha = Double.parseDouble(split[1]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.ALPHA, split[1], cg, e);
      }

      if (alpha < 0.0 || alpha > 1.0)
      {
         throw new InvalidValueException(
            InvalidFormatException.ALPHA, alpha, cg);
      }

      if (split.length == 3)
      {
         remainder = split[2];
      }
      else
      {
         remainder = "";
      }

      return new JDRGray(cg, grey, alpha);
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
