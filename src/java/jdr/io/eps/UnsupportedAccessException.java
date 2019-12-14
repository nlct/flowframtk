// File          : UnsupportedAccessException.java
// Date          : 22 May 2008
// Last Modified : 22 May 2008
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

package com.dickimawbooks.jdr.io.eps;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Exception thrown when an unsupported file access string is encountered.
 * @author Nicola L C Talbot
 */
public class UnsupportedAccessException extends InvalidFormatException
{
   /**
    * Initialises exception with the message "Unsupported file access "
    * followed by the invalid access string.
    * @param access unsupported access string
    */
   public UnsupportedAccessException(String access)
   {
      super("Unsupported file access '"+access+"'");

      access_ = access;
   }

   /**
    * Initialises exception with the message "Unsupported file access "
    * followed by the invalid access string.
    * @param access unsupported access string
    * @param line the line number
    */
   public UnsupportedAccessException(String access, int line)
   {
      super("Unsupported file access '"+access+"'", line);

      access_ = access;
   }

   public String getAccess()
   {
      return access_;
   }

   private String access_;
}
