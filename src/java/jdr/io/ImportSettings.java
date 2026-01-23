/*
    Copyright (C) 2025 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or 
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

import java.io.File;
import java.util.Arrays;

public class ImportSettings
{
   public ImportSettings(JDRMessageDictionary dictionary)
   {
      this.dictionary = dictionary;
   }

   public static enum Type
   {
      SVG, ACORN_DRAW, EPS;
   }

   public static enum Markers
   {
      IGNORE, ADD_SHAPES, MARKER;
   }

   public static enum Paper
   {
      CURRENT, CUSTOM, PREDEFINED;
   }

   public void copyFrom(ImportSettings other)
   {
      type = other.type;
      extractBitmaps = other.extractBitmaps;
      bitmapDir = other.bitmapDir;
      bitmapNamePrefix = other.bitmapNamePrefix;
      useMappings = other.useMappings;
      parseMaths = other.parseMaths;
      markers = other.markers;
      paper = other.paper;

      if (other.mathsCssClasses == null)
      {
         mathsCssClasses = other.mathsCssClasses;
      }
      else if (mathsCssClasses == null
            || mathsCssClasses.length != other.mathsCssClasses.length)
      {
         mathsCssClasses = Arrays.copyOf(
           other.mathsCssClasses, other.mathsCssClasses.length);
      }
      else
      {
         for (int i = 0; i < mathsCssClasses.length; i++)
         {
            mathsCssClasses[i] = other.mathsCssClasses[i];
         }
      }
   }

   public boolean hasMathsCssClasses()
   {
      return (mathsCssClasses != null && mathsCssClasses.length > 0);
   }

   public JDRMessageDictionary getMessageDictionary()
   {
      return dictionary;
   }


   public Type type = Type.SVG;
   public File currentFile = null;
   public boolean extractBitmaps = false;
   public File bitmapDir = null;
   public String bitmapNamePrefix = "";
   public boolean useMappings = true;
   public boolean parseMaths = true;
   public String[] mathsCssClasses = null;
   public Markers markers = Markers.MARKER;
   public Paper paper = Paper.CURRENT;
   JDRMessageDictionary dictionary;
}
