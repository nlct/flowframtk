// File          : JDRGradientListener.java
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
 * Loader listener for gradient paint.
 * @author Nicola L C Talbot
 */

public class JDRGradientListener implements JDRPaintLoaderListener
{
   public char getId(float version)
   {
      return 'G';
   }

   public JDRPaint getPaint(JDRAJR jdr, JDRPaint paint, float version)
   {
      return paint;
   }

   public void write(JDRAJR jdr, JDRPaint paint)
      throws IOException
   {
      JDRGradient c = (JDRGradient)paint;
      JDRPaint startColor = c.getStartColor();
      JDRPaint endColor = c.getEndColor();

      JDRPaintLoader loader = jdr.getPaintLoader();
      loader.save(jdr, startColor);
      loader.save(jdr, endColor);

      jdr.writeInt(c.getDirection());
   }

   public JDRPaint read(JDRAJR jdr)
      throws InvalidFormatException
   {
      JDRPaintLoader loader = jdr.getPaintLoader();
      JDRPaint startPaint = loader.load(jdr);

      if (startPaint instanceof JDRShading)
      {
         throw new InvalidValueException(
          InvalidFormatException.GRADIENT_START, 
          startPaint.getClass().getName(), jdr);
      }

      JDRPaint endPaint = loader.load(jdr);

      if (endPaint instanceof JDRShading)
      {
         throw new InvalidValueException(
          InvalidFormatException.GRADIENT_END, 
          endPaint.getClass().getName(), jdr);
      }

      int direction = jdr.readInt(InvalidFormatException.GRADIENT_DIRECTION);

      return new JDRGradient(direction, startPaint, endPaint);
   }

   public int getConfigId()
   {
      return 3;
   }

   /**
    * @param paint the paint to save (must be {@link JDRGradient}).
    */
   public String getConfigString(JDRPaint paint)
   {
      JDRGradient c = (JDRGradient)paint;
      JDRPaint startColor = c.getStartColor();
      JDRPaint endColor = c.getEndColor();

      JDRPaintLoader loader = JDR.getPaintLoader();

      String specs = loader.getConfigString(startColor)
                   + ","
                   + loader.getConfigString(endColor)
                   + ","
                   + c.getDirection();

      return specs;
   }

   public JDRPaint parseConfig(CanvasGraphics cg, String specs)
      throws InvalidFormatException
   {
      JDRPaintLoader loader = JDR.getPaintLoader();
      JDRPaint startPaint = loader.parseConfig(cg, specs);

      if (startPaint instanceof JDRShading)
      {
         throw new InvalidValueException(
          InvalidFormatException.GRADIENT_START, 
          startPaint.getClass().getName(), cg);
      }

      specs = loader.getConfigRemainder();

      JDRPaint endPaint = loader.parseConfig(cg, specs);

      if (endPaint instanceof JDRShading)
      {
         throw new InvalidValueException(
          InvalidFormatException.GRADIENT_END, 
          endPaint.getClass().getName(), cg);
      }

      specs = loader.getConfigRemainder();

      String[] split = specs.split(",", 2);

      int direction = 0;

      try
      {
         direction = Integer.parseInt(split[0]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.GRADIENT_DIRECTION, split[0], cg, e);
      }

      if (split.length == 1)
      {
         remainder = "";
      }
      else
      {
         remainder = split[1];
      }

      return new JDRGradient(direction, startPaint, endPaint);
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
