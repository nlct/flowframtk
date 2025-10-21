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

public class ImportSettings
{
   public ImportSettings(JDRMessageDictionary dictionary)
   {
      this.dictionary = dictionary;
   }

   public static enum Type
   {
      ACORN_DRAW, EPS, SVG;
   }

   public void copyFrom(ImportSettings other)
   {
      type = other.type;
      extractBitmaps = other.extractBitmaps;
      bitmapDir = other.bitmapDir;
      bitmapNamePrefix = other.bitmapNamePrefix;
      useMappings = other.useMappings;
   }

   public JDRMessageDictionary getMessageDictionary()
   {
      return dictionary;
   }


   public Type type = Type.ACORN_DRAW;
   public File currentFile = null;
   public boolean extractBitmaps = false;
   public File bitmapDir = null;
   public String bitmapNamePrefix = null;
   public boolean useMappings = true;
   JDRMessageDictionary dictionary;
}
