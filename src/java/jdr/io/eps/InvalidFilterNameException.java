// File          : InvalidFilterNameException.java
// Date          : 25 May 2008
// Last Modified : 25 May 2008
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
 * Exception thrown when an invalid filter name is encountered.
 * @author Nicola L C Talbot
 */
public class InvalidFilterNameException extends InvalidFormatException
{
   /**
    * Initialises exception with the message "Unknown filter name "
    * followed by the invalid name.
    * @param name invalid filter name
    */
   public InvalidFilterNameException(String name)
   {
      super("Unknown filter name "+name);

      id = name;
   }

   /**
    * Initialises exception with the message "Unknown filter name "
    * followed by the invalid name.
    * @param name invalid filter name
    * @param line the line number
    */
   public InvalidFilterNameException(String name, int line)
   {
      super("Unknown filter name "+name, line);

      id = name;
   }

   public String getID()
   {
      return id;
   }

   private String id;
}
