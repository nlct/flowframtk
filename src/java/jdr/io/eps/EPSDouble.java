// File          : EPSDouble.java
// Purpose       : class representing an EPS double
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
 * Class representing an EPS double.
 * @author Nicola L C Talbot
 */
public class EPSDouble implements EPSObject,EPSNumber,EPSRelational
{
   /**
    * Initialises with the given value.
    * @param value the value representing this object
    */
   public EPSDouble(double value)
   {
      value_ = value;
   }

   /**
    * Initialises with the value acquired by parsing the given string.
    * @param s the string to be parsed
    */
   public EPSDouble(String s)
      throws NumberFormatException
   {
      value_ = Double.parseDouble(s);
   }

   public boolean equals(Object object)
   {
      if (object instanceof EPSNumber)
      {
         EPSNumber number = (EPSNumber)object;

         return (doubleValue() == number.doubleValue());
      }

      return false;
   }

   public boolean ge(EPSRelational object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSNumber)
      {
         EPSNumber number = (EPSNumber)object;

         return (doubleValue() >= number.doubleValue());
      }

      throw new InvalidEPSObjectException("(ge) invalid type");
   }

   public boolean gt(EPSRelational object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSNumber)
      {
         EPSNumber number = (EPSNumber)object;

         return (doubleValue() > number.doubleValue());
      }

      throw new InvalidEPSObjectException("(gt) invalid type");
   }

   public boolean le(EPSRelational object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSNumber)
      {
         EPSNumber number = (EPSNumber)object;

         return (doubleValue() <= number.doubleValue());
      }

      throw new InvalidEPSObjectException("(le) invalid type");
   }

   public boolean lt(EPSRelational object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSNumber)
      {
         EPSNumber number = (EPSNumber)object;

         return (doubleValue() < number.doubleValue());
      }

      throw new InvalidEPSObjectException("(lt) invalid type");
   }

   public void set(int value)
   {
      value_ = (double)value;
   }

   public void set(long value)
   {
      value_ = (double)value;
   }

   public void set(double value)
   {
      value_ = value;
   }

   public EPSNumber add(EPSNumber number)
   {
      return new EPSDouble(value_ + number.doubleValue());
   }

   public EPSNumber mul(EPSNumber number)
   {
      return new EPSDouble(value_ * number.doubleValue());
   }

   public EPSNumber neg()
   {
      return new EPSDouble(-value_);
   }

   public EPSNumber abs()
   {
      return new EPSDouble(Math.abs(value_));
   }

   public EPSNumber ceiling()
   {
      return new EPSDouble(Math.ceil(value_));
   }

   public EPSNumber floor()
   {
      return new EPSDouble(Math.floor(value_));
   }

   public EPSNumber round()
   {
      return new EPSDouble(Math.round(value_));
   }

   public EPSNumber truncate()
   {
      return new EPSDouble((double)((int)value_));
   }

   public EPSNumber sub(EPSNumber number)
   {
      return new EPSDouble(value_ - number.doubleValue());
   }

   public EPSDouble div(EPSNumber number)
   {
      return new EPSDouble(value_ / number.doubleValue());
   }

   public EPSInteger idiv(EPSNumber number)
   {
      return new EPSInteger(intValue()/number.intValue());
   }

   public EPSNumber mod(EPSNumber number)
   {
      return new EPSInteger(intValue()%number.intValue());
   }

   public int intValue()
   {
      return (int)value_;
   }

   public long longValue()
   {
      return (long)value_;
   }

   public double doubleValue()
   {
      return value_;
   }

   public float floatValue()
   {
      return (float)value_;
   }

   public EPSNumber bitshift(EPSNumber number)
      throws InvalidEPSObjectException
   {
       throw new InvalidEPSObjectException("(bitshift) invalid type");
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
      return new EPSName("realtype");
   }

   public Object clone()
   {
      return new EPSDouble(value_);
   }

   private double value_;
}
