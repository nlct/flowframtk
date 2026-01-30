// File          : JDRRadialListener.java
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
 * Loader listener for radial paint.
 * @author Nicola L C Talbot
 */

public class JDRRadialListener implements JDRPaintLoaderListener
{
   /**
    * Gets the character identifying {@link JDRRadial} paint in
    * JDR/AJR format. JDR/AJR versions
    * before 1.3 did not support radial paint, so if version 
    * &lt; 1.3, returns {@link JDRGradient} ID.
    * @return the {@link JDRRadial} paint ID ('D') if
    * version &gt;= 1.3,
    * otherwise the {@link JDRGradient} paint ID ('G')
    */
   public char getId(float version)
   {
      return 'D';
   }

   public JDRPaint getPaint(JDRAJR jdr, JDRPaint paint, float version)
   {
      // convert to gradient paint if version < 1.3

      if (version < 1.3f)
      {
         return ((JDRRadial)paint).getJDRGradient();
      }

      return paint;
   }

   /**
    * Writes specified radial paint in JDR format. JDR versions
    * before 1.3 did not support radial paint, so if version 
    * &lt; 1.3, the nearest gradient equivalent is saved instead.
    * @param paint the paint to save (must be of type 
    * {@link JDRRadial})
    */
   public void write(JDRAJR jdr, JDRPaint paint)
   throws IOException
   {
      float version = jdr.getVersion();

      if (version < 1.3f)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.UNSUPPORTED_VERSION,
            paint.getClass().getName()+" ("+version+")", jdr);
      }

      JDRPaintLoader loader = jdr.getPaintLoader();

      JDRRadial c = (JDRRadial)paint;
      JDRPaint startColor = c.getStartColor();
      JDRPaint endColor = c.getEndColor();

      loader.save(jdr, startColor);

      if (version < 2.2f)
      {
         if (c.hasMidColor())
         {
            jdr.warningWithFallback("warning.save_unsupported_radial_mid_paint",
             "Radial mid-paint not supported in JDR/AJR version {0}",
             version);
         }
      }
      else
      {
         if (c.hasMidColor())
         {
            JDRPaint midPaint = c.getMidColor();

            jdr.writeBoolean(true);
            loader.save(jdr, midPaint);
         }
         else
         {
            jdr.writeBoolean(false);
         }
      }

      loader.save(jdr, endColor);

      jdr.writeInt(c.getStartLocation());
   }

   /**
    * Reads radial paint specified in JDR format. JDR versions
    * prior to 1.3 did not support radial paint, so if 
    * version &lt; 1.3, returns {@link JDRGradient} paint 
    * otherwise returns {@link JDRRadial} paint.
    */
   public JDRPaint read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      JDRPaintLoader loader = jdr.getPaintLoader();
      JDRPaint startPaint = loader.load(jdr);

      if (startPaint instanceof JDRShading)
      {
         throw new InvalidValueException(
            InvalidFormatException.RADIAL_GRADIENT_START,
            startPaint.getClass().getName(), jdr);
      }

      JDRPaint midPaint = null;

      if (version >= 2.2f)
      {
         if (jdr.readBoolean())
         {
            midPaint = loader.load(jdr);
         }
      }

      JDRPaint endPaint = loader.load(jdr);

      if (endPaint instanceof JDRShading)
      {
         throw new InvalidValueException(
            InvalidFormatException.RADIAL_GRADIENT_END,
            endPaint.getClass().getName(), jdr);
      }

      int direction = jdr.readInt(
         InvalidFormatException.RADIAL_GRADIENT_LOCATION);

      if (version < 1.3f)
      {
         return new JDRGradient(direction, startPaint, endPaint);
      }
      else
      {
         return new JDRRadial(direction, startPaint, midPaint, endPaint);
      }
   }

   public int getConfigId()
   {
      return 4;
   }

   /**
    * @param paint the paint to save (must be {@link JDRRadial}).
    */
   public String getConfigString(JDRPaint paint)
   {
      JDRRadial c = (JDRRadial)paint;
      JDRPaint startColor = c.getStartColor();
      JDRPaint endColor = c.getEndColor();

      JDRPaintLoader loader = JDR.getPaintLoader();

      StringBuilder builder = new StringBuilder();

      builder.append(loader.getConfigString(startColor));
      builder.append(',');

      if (c.hasMidColor())
      {
         builder.append('[');
         builder.append(loader.getConfigString(c.getMidColor()));
         builder.append(']');
      }

      builder.append(loader.getConfigString(endColor));
      builder.append(',');
      builder.append(c.getStartLocation());

      return builder.toString();
   }

   public JDRPaint parseConfig(CanvasGraphics cg, String specs)
     throws InvalidFormatException
   {
      JDRPaintLoader loader = JDR.getPaintLoader();
      JDRPaint startPaint = loader.parseConfig(cg, specs);
      JDRPaint midPaint = null;

      specs = loader.getConfigRemainder();

      if (specs.startsWith("["))
      {
         int idx = specs.indexOf(']');

         if (idx > -1)
         {
            String midSpecs = specs.substring(1, idx);
            specs = specs.substring(idx+1);

            midPaint = loader.parseConfig(cg, midSpecs);
         }
      }

      JDRPaint endPaint = loader.parseConfig(cg, specs);

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
            InvalidFormatException.RADIAL_GRADIENT_LOCATION, direction, cg);
      }

      if (split.length == 1)
      {
         remainder = "";
      }
      else
      {
         remainder = split[1];
      }

      return new JDRRadial(direction, startPaint, midPaint, endPaint);
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
