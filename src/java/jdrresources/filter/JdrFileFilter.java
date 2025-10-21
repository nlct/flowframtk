// File          : JdrFileFilter.java
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
 * Filter for JDR files. Recognised image extension: jdr.
 */
public class JdrFileFilter extends AbstractJDRFileFilter
{
   /**
    * Creates a JDR file filter with default description.
    * The default description is "JDR Image Files".
    */
   public JdrFileFilter()
   {
      this("JDR Image Files");
   }

   /**
    * Creates a JDR file filter with default description and given
    * JDR version number.
    * The default description is "JDR Image Files".
    */
   public JdrFileFilter(float versionNumber)
   {
      super("JDR Image Files", versionNumber);
   }

   /**
    * Creates a JDR file filter with given description.
    */
   public JdrFileFilter(String description)
   {
      super(description, JDRAJR.CURRENT_VERSION);
   }

   /**
    * Creates a JDR file filter with given description and JDR
    * version  number.
    */
   public JdrFileFilter(String description, float versionNumber)
   {
      super(description, versionNumber);
   }

   /**
    * Determines whether given file is accepted by this filter.
    */
   @Override
   public boolean accept(File f)
   {
      if (f.isDirectory()) return true;

      String name = f.getName().toLowerCase();

      return name.endsWith(".jdr");
   }

   @Override
   public String getDefaultExtension()
   {
      return "jdr";
   }
}
