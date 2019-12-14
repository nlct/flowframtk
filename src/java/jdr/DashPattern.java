// File          : DashPattern.java
// Creation Date : 1st February 2006
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

import java.io.*;

import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing dash pattern.
 * @author Nicola L C Talbot
 */
public class DashPattern implements Serializable,Cloneable
{
   /**
    * Initialises using default settings: solid line.
    */
   public DashPattern(CanvasGraphics cg)
   {
      this(cg, null, 0.0f);
   }

   /**
    * Initialises using the specified dash pattern with 0 offset.
    * A solid line is indicated by setting dashPattern to null.
    * @param dashPattern the dash pattern (alternating dash length
    * and gap length)
    * @see #DashPattern(CanvasGraphics)
    */
   public DashPattern(CanvasGraphics cg, float[] dashPattern)
   {
      this(cg, dashPattern, 0.0f);
   }

   /**
    * Initialises using the specified dash pattern and offset.
    * A solid line is indicated by setting <code>dashPattern</code> to 
    * <code>null</code>.
    * @param dashPattern the dash pattern (alternating dash length
    * and gap length)
    * @param dashOffset the offset
    * @see #DashPattern(CanvasGraphics)
    */
   public DashPattern(CanvasGraphics cg, float[] dashPattern, float dashOffset)
   {
      setCanvasGraphics(cg);
      pattern = dashPattern;
      offset = dashOffset;
   }

   /**
    * Returns a copy of this object.
    * @return a copy of this object
    */
   public Object clone()
   {
      if (pattern == null)
      {
         return new DashPattern(getCanvasGraphics(), null, offset);
      }

      float[] dashPattern = new float[pattern.length];

      for (int i=0; i < pattern.length; i++)
      {
         dashPattern[i]=pattern[i];
      }

      return new DashPattern(getCanvasGraphics(), dashPattern, offset);
   }

   /**
    * Saves this object in JDR/AJR format.
    */
   public void save(JDRAJR jdr)
      throws IOException
   {
      jdr.writeArray(pattern);

      int n = (pattern == null? 0 : pattern.length);

      if (n > 0) jdr.writeFloat(offset);
   }

   /**
    * Reads a dash pattern from a JDR/AJR input stream.
    * @throws InvalidFormatException if the data in the file is
    * not correctly formatted
    * @return the specified dash pattern
    */
   public static DashPattern read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float[] pat = jdr.readFloatArray(
         InvalidFormatException.DASH_PATTERN);

      if (pat == null)
      {
         return new DashPattern(jdr.getCanvasGraphics(), null, 0.0F);
      }

      float off = jdr.readFloat(
         InvalidFormatException.DASH_OFFSET);

      return new DashPattern(jdr.getCanvasGraphics(), pat, off);
   }

   /**
    * Converts this dash pattern to SVG format.
    * @return the equivalent SVG format representing this dash pattern
    */
   public String svg()
   {
      if (pattern == null) return "stroke-dasharray=\"none\"";

      JDRUnit unit = canvasGraphics.getStorageUnit();

      String str="stroke-dasharray=\""+unit.svg(pattern[0]);

      for (int i = 1, n=pattern.length; i < n; i++)
      {
         str += ","+unit.svg(pattern[i]);
      }

      str += "\" stroke-dashoffset=\"" +unit.svg(offset)+"\"";
      return str;
   }

   /**
    * Writes PostScript command to set this dash pattern.
    * @param out the output stream
    * @throws IOException if I/O error occurs
    */
   public void saveEPS(PrintWriter out)
      throws IOException
   {
      if (pattern == null)
      {
         out.println("[] 0 setdash");
      }
      else
      {
         JDRUnit unit = canvasGraphics.getStorageUnit();

         out.print("[");
         for (int i = 0; i < pattern.length; i++)
         {
            out.print(""+unit.toBp(pattern[i])+" ");
         }
         out.println("] "+unit.toBp(offset)+" setdash");
      }
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      canvasGraphics = cg;
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      JDRUnit oldUnit = canvasGraphics.getStorageUnit();
      JDRUnit newUnit = cg.getStorageUnit();

      if (oldUnit.getID() != newUnit.getID())
      {
         double factor = oldUnit.toUnit(1.0, newUnit);

         offset = (float)(offset*factor);

         if (pattern != null)
         {
            for (int i = 0; i < pattern.length; i++)
            {
               pattern[i] = (float)(factor*pattern[i]);
            }
         }
      }

      canvasGraphics = cg;
   }

   public float[] getStoragePattern()
   {
      return pattern;
   }

   public float getStorageOffset()
   {
      return offset;
   }

   public void setOffset(JDRLength offsetLength)
   {
      offset = (float)offsetLength.getValue(getCanvasGraphics().getStorageUnit());
   }

   public void setStorageOffset(float storageOffset)
   {
      offset = storageOffset;
   }

   public void setStoragePattern(float[] storagePattern)
   {
      pattern = storagePattern;
   }

   public float getBpOffset()
   {
      if (canvasGraphics.getStorageUnitID() == JDRUnit.BP)
      {
         return offset;
      }

      return canvasGraphics.storageToBp(offset);
   }

   public float[] getBpPattern()
   {
      if (canvasGraphics.getStorageUnitID() == JDRUnit.BP || pattern == null)
      {
         return pattern;
      }
      
      float[] bpPattern = new float[pattern.length];

      for (int i = 0; i < pattern.length; i++)
      {
         bpPattern[i] = canvasGraphics.storageToBp(pattern[i]);
      }

      return bpPattern;
   }

   public void savePgf(TeX tex)
     throws IOException
   {
      if (pattern == null) return;

      tex.print("\\pgfsetdash{");

      for (int i = 0; i < pattern.length; i++)
      {
         tex.print("{"+PGF.length(canvasGraphics, pattern[i])+"}");
      }

      tex.println("}{"+PGF.length(canvasGraphics, offset)+"}");
   }

   public String toString()
   {
      if (pattern == null)
      {
         return "solid";
      }

      String str = "pattern=[";

      for (int i = 0; i < pattern.length; i++)
      {
         str += (i==0 ? "" : ",")+pattern[i];
      }

      return str+"],offset="+offset;
   }

   /**
    * Stores the alternating dash length and gap length for this
    * pattern.
    */
   private volatile float[] pattern;
   /**
    * Stores the offset to the start of the first dash.
    */
   private volatile float offset;

   private volatile CanvasGraphics canvasGraphics;
}

