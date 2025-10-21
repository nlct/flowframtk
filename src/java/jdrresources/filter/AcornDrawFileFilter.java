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
package com.dickimawbooks.jdrresources.filter;

import java.io.*;

import com.dickimawbooks.jdr.io.*;

/**
 * Filter for Acorn Draw files. Since RISC OS doesn't recognise file
 * extensions, this uses the NFS style <code>,aff</code>
 * (NB comma not dot). 
 */

public class AcornDrawFileFilter extends AbstractJDRFileFilter
{
   /**
    * Creates an Acorn Draw file filter with given description.
    */
   public AcornDrawFileFilter(String description)
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

      return name.endsWith(",aff")
          || f.isDirectory();
   }

   @Override
   public String getDefaultExtension()
   {
      return "aff";
   }

   @Override
   public String getExtensionSeparator() { return ","; }
}
