// File          : ClsFileFilter.java
// Creation Date : 1st February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2025 Nicola L.C. Talbot

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
package com.dickimawbooks.jdrresources.filter;

import java.io.*;

import com.dickimawbooks.jdr.io.*;

/**
 * Filter for LaTeX classes. Recognised extension: cls.
 */
public class ClsFileFilter extends AbstractJDRFileFilter
{
   /**
    * Creates a LaTeX class file filter with default description.
    * The default description is "LaTeX Classes".
    */
   public ClsFileFilter()
   {
      this("LaTeX Classes");
   }

   /**
    * Creates a LaTeX class file filter with given description.
    */
   public ClsFileFilter(String description)
   {
      super(description);
   }

   /**
    * Determines whether given file is accepted by this filter.
    */
   @Override
   public boolean accept(File f)
   {
      if (f.isDirectory()) return true;

      String name = f.getName().toLowerCase();

      return name.endsWith(".cls");
   }

   @Override
   public boolean supportsImportType(ImportSettings.Type type)
   {
      return false;
   }

   @Override
   public boolean supportsExportType(ExportSettings.Type type)
   {
      return type == ExportSettings.Type.CLS;
   }

   @Override
   public String getDefaultExtension()
   {
      return "cls";
   }
}
