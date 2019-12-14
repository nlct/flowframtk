// File          : EPSDictMark.java
// Purpose       : class representing an EPS dictionary mark
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
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.font.*;
import java.util.*;
import java.util.regex.*;
import java.text.DateFormat;
import java.math.*;
import javax.imageio.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing an EPS dictionary mark.
 * @author Nicola L C Talbot
 */
public class EPSDictMark implements EPSObject
{
   /**
    * Returns a string representation of this object
    * @return a string representation of this object
    */
   public String toString()
   {
      return "<<";
   }

   /**
    * Returns <code>true</code> if given object is also an
    * <code>EPSDictMark</code>.
    */
   public boolean equals(Object object)
   {
      return (object instanceof EPSDictMark);
   }

   public EPSName pstype()
   {
      return new EPSName("marktype");
   }

   public Object clone()
   {
      return new EPSDictMark();
   }
}
