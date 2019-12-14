// File          : JDRColorCMYKListener.java
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
 * Loader listener for CMYK paint.
 * @author Nicola L C Talbot
 */

public class JDRColorCMYKListener implements JDRPaintLoaderListener
{
   public char getId(float version)
   {
      return 'C';
   }

   public JDRPaint getPaint(JDRAJR jdr, JDRPaint paint, float version)
   {
      return paint;
   }

   /**
    * Writes CMYK paint in JDR format.
    * @param paint the paint to save (must be {@link JDRColorCMYK}).
    */
   public void write(JDRAJR jdr, JDRPaint paint)
      throws IOException
   {
      JDRColorCMYK c = (JDRColorCMYK)paint;

      jdr.writeFloat((float)c.getCyan());
      jdr.writeFloat((float)c.getMagenta());
      jdr.writeFloat((float)c.getYellow());
      jdr.writeFloat((float)c.getKey());
      jdr.writeFloat((float)c.getAlpha());
   }

   public JDRPaint read(JDRAJR jdr)
      throws InvalidFormatException
   {
      double c = jdr.readFloat(
         InvalidFormatException.CYAN, 0f, 1f, true, true);

      double m = jdr.readFloat(
         InvalidFormatException.MAGENTA, 0f, 1f, true, true);

      double y = jdr.readFloat(
         InvalidFormatException.YELLOW, 0f, 1f, true, true);

      double k = jdr.readFloat(
         InvalidFormatException.BLACK, 0f, 1f, true, true);

      double a = jdr.readFloat(
         InvalidFormatException.ALPHA, 0f, 1f, true, true);

      return new JDRColorCMYK(jdr.getCanvasGraphics(), c,m,y,k,a);
   }

   public int getConfigId()
   {
      return 2;
   }

   /**
    * @param paint the paint to save (must be {@link JDRColorCMYK}).
    */
   public String getConfigString(JDRPaint paint)
   {
      JDRColorCMYK c = (JDRColorCMYK)paint;

      return "" + c.getCyan() 
           +"," + c.getMagenta()
           +"," + c.getYellow()
           +"," + c.getKey()
           +"," + c.getAlpha();
   }

   public JDRPaint parseConfig(CanvasGraphics cg, String specs)
   throws InvalidFormatException
   {
      String split[] = specs.split(",",6);

      if (split.length < 5)
      {
         throw new InvalidValueException(
            InvalidFormatException.CMYK, specs, cg);
      }     

      double cyan = 0;
      double magenta = 0;
      double yellow  = 0;
      double black   = 0;
      double alpha   = 0;

      try
      {
         cyan = Double.parseDouble(split[0]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.CYAN, split[0], cg, e);
      }

      if (cyan < 0.0 || cyan > 1.0)
      {
         throw new InvalidValueException(
           InvalidFormatException.CYAN, cyan, cg);
      }

      try
      {
         magenta = Double.parseDouble(split[1]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.MAGENTA, split[1], cg, e);
      }

      if (magenta < 0.0 || magenta > 1.0)
      {
         throw new InvalidValueException(
           InvalidFormatException.MAGENTA, magenta, cg);
      }

      try
      {
         yellow = Double.parseDouble(split[2]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.YELLOW, split[2], cg, e);
      }

      if (yellow < 0.0 || yellow > 1.0)
      {
         throw new InvalidValueException(
            InvalidFormatException.YELLOW, yellow, cg);
      }

      try
      {
         black = Double.parseDouble(split[3]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.BLACK, split[3], cg, e);
      }

      if (black < 0.0 || black > 1.0)
      {
         throw new InvalidValueException(
            InvalidFormatException.BLACK, black, cg);
      }

      try
      {
         alpha = Double.parseDouble(split[4]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.ALPHA, split[4], cg, e);
      }

      if (alpha < 0.0 || alpha > 1.0)
      {
         throw new InvalidValueException(
            InvalidFormatException.ALPHA, alpha, cg);
      }

      if (split.length == 6)
      {
         remainder = split[5];
      }
      else
      {
         remainder = "";
      }

      return new JDRColorCMYK(cg,
         cyan, yellow, magenta, black, alpha);
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
