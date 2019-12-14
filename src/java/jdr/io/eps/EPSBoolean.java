// File          : EPSBoolean.java
// Purpose       : class representing an EPS boolean object
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
 * Class representing an EPS boolean object.
 * @author Nicola L C Talbot
 */
public class EPSBoolean implements EPSObject,EPSLogical
{
   /**
    * Initialises using the given boolean value.
    * @param value the boolean value associated with this object
    */
   public EPSBoolean(boolean value)
   {
      value_ = value;
   }

   /**
    * Initialises this where the value is parsed from the given 
    * string. (Uses
    * <code>java.lang.Boolean.parseBoolean(String)</code> to parse 
    * the value.)
    * @param s the string to be converted into a boolean
    */
   public EPSBoolean(String s)
   {
      value_ = Boolean.parseBoolean(s);
   }

   public boolean equals(Object object)
   {
      if (object instanceof EPSBoolean)
      {
         return (value_ == ((EPSBoolean)object).value_);
      }

      return false;
   }

   public EPSLogical and(EPSLogical object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSBoolean)
      {
          value_ = value_ && ((EPSBoolean)object).booleanValue();
          return this;
      }

      throw new InvalidEPSObjectException("(and) invalid type");
   }

   public EPSLogical or(EPSLogical object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSBoolean)
      {
          value_ = value_ || ((EPSBoolean)object).booleanValue();
          return this;
      }

      throw new InvalidEPSObjectException("(or) invalid type");
   }

   public EPSLogical xor(EPSLogical object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSBoolean)
      {
          boolean value2 = ((EPSBoolean)object).booleanValue();

          value_ = (value_ != value2);
          
          return this;
      }

      throw new InvalidEPSObjectException("(xor) invalid type");
   }

   public EPSLogical not()
   {
       value_ = !value_;
       return this;
   }

   /**
    * Gets the <code>boolean</code> value associated with this object.
    * @return the <code>boolean</code> value associated with this
    * object
    */
   public boolean booleanValue()
   {
      return value_;
   }

   /**
    * Returns a string representation of this object
    * @return a string representation of this object
    */
   public String toString()
   {
      return ""+value_;
   }

   public EPSName pstype()
   {
      return new EPSName("booleantype");
   }

   /**
    * Sets the boolean value.
    * @param newValue new value to assign to this object
    */
   public void set(boolean newValue)
   {
      value_ = newValue;
   }

   public Object clone()
   {
      return new EPSBoolean(value_);
   }

   private boolean value_;
}
