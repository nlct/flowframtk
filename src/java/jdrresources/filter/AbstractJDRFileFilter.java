/*
    Copyright (C) 2025 Nicola L.C. Talbot

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

import java.io.File;
import javax.swing.filechooser.FileFilter;

import com.dickimawbooks.jdr.io.ImportSettings;
import com.dickimawbooks.jdr.io.ExportSettings;

/**
 * Abstract filter.
 */
public abstract class AbstractJDRFileFilter
  extends javax.swing.filechooser.FileFilter
{
   public AbstractJDRFileFilter(String description)
   {
      this.description = description;
   }

   public AbstractJDRFileFilter(String description, float versionNumber)
   {
      this.description = description;
      this.versionNumber = versionNumber;
   }

   public abstract boolean accept(File file);

   public abstract boolean supportsImportType(ImportSettings.Type type);
   public abstract boolean supportsExportType(ExportSettings.Type type);

   @Override
   public String getDescription()
   {
      return description;
   }

   /**
    * Gets default file extension for this filter (not including
    * dot).
    */
   public abstract String getDefaultExtension();

   public String getExtensionSeparator() { return "."; }

   /**
    * Gets the version number associated with this file filter
    */
   public float getVersion()
   {
      return versionNumber;
   }

  /**
   * Sets the version number associated with this file filter
   */
   public void setVersion(float versionNumber)
   {
      this.versionNumber = versionNumber;
   }

   protected String description;
   protected float versionNumber;
}
