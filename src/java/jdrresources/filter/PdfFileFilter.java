// File          : PdfFileFilter.java
// Creation Date : 2014-05-08
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2014-2025 Nicola L.C. Talbot

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
 * Filter for PDF files. Only recognises
 * the extension <code>pdf</code>.
 */

public class PdfFileFilter extends AbstractJDRFileFilter
{
   /**
    * Creates a PDF file filter with given description.
    */
   public PdfFileFilter(String description)
   {
      super(description);
   }

   /**
    * Determines whether given file is accepted by this filter.
    */
   @Override
   public boolean accept(File f)
   {
      return f.getName().toLowerCase().endsWith(".pdf")
            || f.isDirectory();
   }

   @Override
   public boolean supportsImportType(ImportSettings.Type type)
   {
      return false;
   }

   @Override
   public boolean supportsExportType(ExportSettings.Type type)
   {
      switch (type)
      {
         case IMAGE_PDF:
         case FLF_PDF:
         return true;
      }

      return false;
   }

   @Override
   public String getDefaultExtension()
   {
      return "pdf";
   }
}
