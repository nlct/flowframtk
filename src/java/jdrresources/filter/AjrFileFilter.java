// File          : AjrFileFilter.java
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
 * Filter for AJR files. Recognised image extension: ajr.
 */
public class AjrFileFilter extends javax.swing.filechooser.FileFilter
   implements JDRFileFilterInterface
{
   /**
    * Creates an AJR file filter with default description.
    * The default description is "AJR Image Files".
    */
   public AjrFileFilter()
   {
      this("AJR Image Files");
   }

   /**
    * Creates an AJR file filter with default description and given
    * AJR version number.
    * The default description is "AJR Image Files".
    */
   public AjrFileFilter(float versionNumber)
   {
      this("AJR Image Files", versionNumber);
   }

   /**
    * Creates an AJR file filter with given description.
    */
   public AjrFileFilter(String description)
   {
      super();

      description_ = description;
   }

   /**
    * Creates an AJR file filter with given description and given
    * AJR version number.
    */
   public AjrFileFilter(String description, float versionNumber)
   {
      super();

      description_ = description;
      version = versionNumber;
   }

   /**
    * Determines whether given file is accepted by this filter.
    */
   public boolean accept(File f)
   {
      if (f.isDirectory()) return true;

      String name = f.getName().toLowerCase();

      return name.endsWith(".ajr");
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
      return "ajr";
   }

   public float getVersion()
   {
      return version;
   }

   public void setVersion(float versionNumber)
   {
      version = versionNumber;
   }

   private float version = JDRAJR.CURRENT_VERSION;
   private String description_;
}
