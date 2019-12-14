// File          : TeXLookup.java
// Purpose       : Associate command with a package
// Creation Date : 2014-05-10
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

package com.dickimawbooks.flowframtk;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

import com.dickimawbooks.jdrresources.*;

public class TeXLookup
{
   public TeXLookup(String command, String styName)
   {
      this.command = command;
      this.styName = (styName == null || styName.isEmpty() ? "none" : styName);
   }

   public String getCommand()
   {
      return command;
   }

   public String getStyName()
   {
      return styName;
   }

   public String toString()
   {
      return styName.equals("none") ? command :
        command+" "+styName;
   }

   private String command, styName;
}
