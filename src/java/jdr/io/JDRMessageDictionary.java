// File          : JDRMessageDictionary.java
// Purpose       : Interface for looking up messages strings
// Creation Date : 2015-10-04
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

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
package com.dickimawbooks.jdr.io;

import java.io.*;

import com.dickimawbooks.jdr.*;

public interface JDRMessageDictionary
{
   /**
    * Gets localised text using the format provided by label.
    * If no localisation is defined for the given label, the
    * fallback format (suitable with MessageFormat) will be used, if
    * not null.
    * @param label identifies the localisation message format
    * @param fallbackFormat the fallback format to use if the label
    * isn't defined (maybe null for no fallback)
    * @param params the message format parameters
    */
   public String getMessageWithFallback(String label,
       String fallbackFormat, Object... params);

   public String getApplicationName();
   public String getApplicationVersion();

   @Deprecated
   public default String getString(String tag, String defValue)
   {
      return getMessageWithFallback(tag, defValue);
   }

   @Deprecated
   public default String getMessageWithAlt(String altFormat, String tag,
     Object... values)
   {
      return getMessageWithFallback(tag, altFormat, values);
   }
}
