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

package com.dickimawbooks.jdrconverter;

import java.io.File;

public enum FileFormatType
{
  JDR(true, false, false, false), AJR(true, true, false, false),
  EPS(true, true, true, false), SVG(true, true, true, false), 
  PNG(false, false, false, false), // TODO
  TEX(false, true, false, false),
  CLS(false, true, false, false),
  STY(false, true, false, false),
  PDF(false, false, true, true);

  private FileFormatType(final boolean inputSupported, final boolean isTextFile,
   final boolean canTeXToolsCreate, final boolean requiresTeXTools)
  {
     this.inputSupported = inputSupported;
     this.isTextFile = isTextFile;
     this.canTeXToolsCreate = canTeXToolsCreate;
     this.requiresTeXTools = requiresTeXTools;
  }

  public boolean isInputSupported()
  {
     return inputSupported;
  }

  public boolean isTextFile()
  {
     return isTextFile;
  }

  public boolean canTeXToolsCreate()
  {
     return canTeXToolsCreate;
  }

  public boolean requiresTeXTools()
  {
     return requiresTeXTools;
  }

  public static FileFormatType getFormat(File file)
  {
      FileFormatType type = null;

      // guess from file extension

      String name = file.getName();
      int idx = name.lastIndexOf(".");

      if (idx > 0)
      {
         String ext = name.substring(idx+1).toUpperCase();

         type = valueOf(ext);
      }

      return type;
  }

  private final boolean inputSupported;
  private final boolean isTextFile;
  private final boolean canTeXToolsCreate;
  private final boolean requiresTeXTools;
}
