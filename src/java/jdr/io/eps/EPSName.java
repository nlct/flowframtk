// File          : EPSName.java
// Purpose       : class representing an EPS name
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
 * Class representing an EPS name.
 * @author Nicola L C Talbot
 */
public class EPSName implements EPSObject
{
   /**
    * Initialises with the given name.
    * @param name the value of this object
    */
   public EPSName(String name)
   {
      name_ = name;
   }

   /**
    * Gets the operator associated with this name or null if
    * none found.
    * @param stack the stack
    */
   public EPSOperator getOperator(EPSStack stack)
   {
      for (int i = stack.getDictStackSize()-1; i >= 0; i--)
      {
         EPSDict dict = stack.getDict(i);

         for (int j = 0; j < dict.size(); j++)
         {
            EPSObject entry = dict.get(i);

            if (entry instanceof EPSOperator)
            {
               EPSOperator op = (EPSOperator)entry;

               if (op.getName().equals(name_))
               {
                  return op;
               }
            }
         }
      }

      return null;
   }

   public boolean equals(Object object)
   {
      if (object instanceof EPSName)
      {
         return name_.equals(((EPSName)object).name_);
      }

      return false;
   }

   /**
    * Returns a string representation of this object
    * @return a string representation of this object
    */
   public String toString()
   {
      return name_;
   }

   public EPSName pstype()
   {
      return new EPSName("nametype");
   }

   public Object clone()
   {
      return new EPSName(name_);
   }

   private String name_;
}
