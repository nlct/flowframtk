// File          : PdfFileFilter.java
// Creation Date : 2014-05-08
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
package com.dickimawbooks.jdrresources.filter;

import java.io.*;

import com.dickimawbooks.jdr.io.*;

/**
 * Filter for PDF files. Only recognises
 * the extension <code>pdf</code>.
 */

public class PdfFileFilter extends javax.swing.filechooser.FileFilter
implements JDRFileFilterInterface
{
   /**
    * Creates a PDF file filter with given description.
    */
   public PdfFileFilter(String description)
   {
      super();

      description_ = description;
   }

   /**
    * Determines whether given file is accepted by this filter.
    */
   public boolean accept(File f)
   {
      return f.getName().toLowerCase().endsWith(".pdf")
            || f.isDirectory();
   }

   /**
    * Gets the description of this filter.
    */
   public String getDescription()
   {
      return description_;
   }

   public String getDefaultExtension()
   {
      return "pdf";
   }

   public void setVersion(float versionNumber)
   {
      version = versionNumber;
   }

   public float getVersion()
   {
      return version;
   }

   private float version = 1.0f;
   private String description_;
}
