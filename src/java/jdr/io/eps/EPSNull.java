// File          : EPSNull.java
// Purpose       : class representing null EPS object
// Date          : 1st February 2006
// Last Modified : 28 July 2007
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
package com.dickimawbooks.jdr.io.eps;

import java.io.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing an EPS null.
 * @author Nicola L C Talbot
 */
public class EPSNull implements EPSObject
{
   public String toString()
   {
      return "null";
   }

   /**
    * Returns <code>true</code> if given object is also an instance 
    * of this class otherwise returns <code>false</code>.
    * @return <code>true</code> if given object is also an instance 
    * of this class otherwise returns <code>false</code>
    */
   public boolean equals(Object object)
   {
      return (object instanceof EPSNull);
   }

   public EPSName pstype()
   {
      return new EPSName("nulltype");
   }

   public Object clone()
   {
      return new EPSNull();
   }
}
