// File          : StyFileFilter.java
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
package com.dickimawbooks.jdrresources.filter;

import java.io.*;

import com.dickimawbooks.jdr.io.*;

/**
 * Filter for LaTeX packages. Recognised extension: sty.
 */
public class StyFileFilter extends javax.swing.filechooser.FileFilter
implements JDRFileFilterInterface
{
   /**
    * Creates a LaTeX style file filter with default description.
    * The default description is "LaTeX Packages".
    */
   public StyFileFilter()
   {
      this("LaTeX Packages");
   }

   /**
    * Creates a LaTeX Style file filter with given description.
    */
   public StyFileFilter(String description)
   {
      super();

      description_ = description;
   }

   /**
    * Determines whether given file is accepted by this filter.
    */
   public boolean accept(File f)
   {
      if (f.isDirectory()) return true;

      String name = f.getName().toLowerCase();

      return name.endsWith(".sty");
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
      return "sty";
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
