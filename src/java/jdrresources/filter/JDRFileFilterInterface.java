// File          : JDRFileFilterInterface.java
// Creation Date : 24th March 2008
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

import java.io.File;

/**
 * Filter interface.
 */
public interface JDRFileFilterInterface
{
   public boolean accept(File file);

   /**
    * Gets default file extension for this filter (not including
    * dot).
    */
   public String getDefaultExtension();

   /**
    * Gets the version number associated with this file filter
    */
  public float getVersion();

  /**
   * Sets the version number associated with this file filter
   */
  public void setVersion(float versionNumber);
}
