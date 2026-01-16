// File          : EpsFileFilter.java
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
 * Filter for PostScript files. Only recognises
 * the extension <code>eps</code> or <code>ps</code>.
 */

public class EpsFileFilter extends AbstractJDRFileFilter
{
   /**
    * Creates an EPS file filter with given description.
    */
   public EpsFileFilter(String description)
   {
      super(description);
   }

   /**
    * Determines whether given file is accepted by this filter.
    */
   @Override
   public boolean accept(File f)
   {
      String name = f.getName().toLowerCase();

      return name.endsWith(".eps")
          || name.endsWith(".ps")
          || f.isDirectory();
   }

   @Override
   public boolean supportsImportType(ImportSettings.Type type)
   {
      return type == ImportSettings.Type.EPS;
   }

   @Override
   public boolean supportsExportType(ExportSettings.Type type)
   {
      return type == ExportSettings.Type.EPS;
   }

   @Override
   public String getDefaultExtension()
   {
      return "eps";
   }
}
