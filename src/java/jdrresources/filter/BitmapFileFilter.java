// File          : BitmapFileFilter.java
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
import javax.imageio.ImageIO;

import com.dickimawbooks.jdr.io.*;

/**
 * Filter for raster image files. Recognised image extensions:
 * tiff, tif, gif, jpeg, jpg, png.
 */
public class BitmapFileFilter extends javax.swing.filechooser.FileFilter
implements JDRFileFilterInterface
{
   /**
    * Creates a bitmap file filter with default description.
    * The default description is "Image Files".
    */
   public BitmapFileFilter()
   {
      this("Image Files", ImageIO.getReaderFileSuffixes());
   }

   /**
    * Creates a bitmap file filter with given description.
    */
   public BitmapFileFilter(String description, String[] extensions)
   {
      super();

      description_ = description;
      validExtensions = extensions;
   }

   /**
    * Determines whether given file is accepted by this filter.
    */
   public boolean accept(File f)
   {
      if (f.isDirectory()) return true;

      String name = f.getName().toLowerCase();

      int idx = name.lastIndexOf(".");

      if (idx == -1)
      {
         return false;
      }

      String ext = name.substring(idx+1);

      for (int i = 0; i < validExtensions.length; i++)
      {
         if (ext.equals(validExtensions[i]))
         {
            return true;
         }
      }

      return false;
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
      return "png";
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

   private String[] validExtensions;
}
