// File          : EPSInteger.java
// Purpose       : class representing an EPS integer
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
 * Class representing an EPS integer.
 * @author Nicola L C Talbot
 */
public class EPSInteger
   implements EPSObject,EPSNumber,EPSRelational,EPSLogical
{
   /**
    * Initialises with the given value.
    * @param value the value representing this object
    */
   public EPSInteger(int value)
   {
      value_ = value;
   }

   /**
    * Initialises with the value acquired by parsing the given string.
    * @param s the string to be parsed
    */
   public EPSInteger(String s)
      throws NumberFormatException
   {
      value_ = Integer.parseInt(s);
   }

   public boolean equals(Object object)
   {
      if (object instanceof EPSNumber)
      {
         EPSNumber number = (EPSNumber)object;

         if (number instanceof EPSInteger)
         {
            return (intValue() == number.intValue());
         }
         else if (number instanceof EPSLong)
         {
            return (longValue() == number.longValue());
         }
         else
         {
            return (doubleValue() == number.doubleValue());
         }
      }

      return false;
   }

   public EPSLogical and(EPSLogical object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSNumber)
      {
          EPSNumber number = (EPSNumber)object;

          if (number instanceof EPSDouble)
          {
             throw new InvalidEPSObjectException("(and) invalid type");
          }

          return new EPSInteger(value_ & intValue());
      }

      throw new InvalidEPSObjectException("(and) invalid type");
   }

   public EPSLogical or(EPSLogical object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSNumber)
      {
          EPSNumber number = (EPSNumber)object;

          if (number instanceof EPSDouble)
          {
             throw new InvalidEPSObjectException("(or) invalid type");
          }

          return new EPSInteger(value_ | intValue());
      }

      throw new InvalidEPSObjectException("(or) invalid type");
   }

   public EPSLogical xor(EPSLogical object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSNumber)
      {
          EPSNumber number = (EPSNumber)object;

          if (number instanceof EPSDouble)
          {
             throw new InvalidEPSObjectException("(xor) invalid type");
          }

          return new EPSInteger(value_ ^ intValue());
      }

      throw new InvalidEPSObjectException("(xor) invalid type");
   }

   public EPSLogical not()
   {
      return new EPSInteger(~value_);
   }

   public EPSNumber bitshift(EPSNumber number)
      throws InvalidEPSObjectException
   {
       if (number instanceof EPSDouble)
       {
          throw new InvalidEPSObjectException(
             "(bitshift) invalid type");
       }

       return new EPSInteger(value_ << number.intValue());
   }

   public boolean ge(EPSRelational object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSNumber)
      {
         EPSNumber number = (EPSNumber)object;

         if (number instanceof EPSInteger)
         {
            return (intValue() >= number.intValue());
         }
         else if (number instanceof EPSLong)
         {
            return (longValue() >= number.longValue());
         }
         else
         {
            return (doubleValue() >= number.doubleValue());
         }
      }

      throw new InvalidEPSObjectException("(ge) invalid type");
   }

   public boolean gt(EPSRelational object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSNumber)
      {
         EPSNumber number = (EPSNumber)object;

         if (number instanceof EPSInteger)
         {
            return (intValue() > number.intValue());
         }
         else if (number instanceof EPSLong)
         {
            return (longValue() > number.longValue());
         }
         else
         {
            return (doubleValue() > number.doubleValue());
         }
      }

      throw new InvalidEPSObjectException("(gt) invalid type");
   }

   public boolean le(EPSRelational object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSNumber)
      {
         EPSNumber number = (EPSNumber)object;

         if (number instanceof EPSInteger)
         {
            return (intValue() <= number.intValue());
         }
         else if (number instanceof EPSLong)
         {
            return (longValue() <= number.longValue());
         }
         else
         {
            return (doubleValue() <= number.doubleValue());
         }
      }

      throw new InvalidEPSObjectException("(le) invalid type");
   }

   public boolean lt(EPSRelational object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSNumber)
      {
         EPSNumber number = (EPSNumber)object;

         if (number instanceof EPSInteger)
         {
            return (intValue() < number.intValue());
         }
         else if (number instanceof EPSLong)
         {
            return (longValue() < number.longValue());
         }
         else
         {
            return (doubleValue() < number.doubleValue());
         }
      }

      throw new InvalidEPSObjectException("(lt) invalid type");
   }

   public void set(int value)
   {
      value_ = value;
   }

   public void set(long value)
   {
      value_ = (int)value;
   }

   public void set(double value)
   {
      value_ = (int)value;
   }

   public EPSNumber add(EPSNumber number)
   {
      if (number instanceof EPSDouble 
       || number instanceof EPSLong)
      {
         return number.add(this);
      }

      return new EPSInteger(value_ + number.intValue());
   }

   public EPSNumber neg()
   {
      return new EPSInteger(-value_);
   }

   public EPSNumber abs()
   {
      return new EPSInteger(Math.abs(value_));
   }

   public EPSNumber ceiling()
   {
      return this;
   }

   public EPSNumber floor()
   {
      return this;
   }

   public EPSNumber round()
   {
      return this;
   }

   public EPSNumber truncate()
   {
      return this;
   }

   public EPSNumber sub(EPSNumber number)
   {
      if (number instanceof EPSDouble)
      {
         return new EPSDouble(value_ - number.doubleValue());
      }
      else if (number instanceof EPSLong)
      {
         return new EPSLong(value_ - number.longValue());
      }

      return new EPSInteger(value_ - number.intValue());
   }

   public EPSNumber mul(EPSNumber number)
   {
      if (number instanceof EPSDouble 
       || number instanceof EPSLong)
      {
         return number.mul(this);
      }

      return new EPSInteger(value_ * number.intValue());
   }

   public EPSDouble div(EPSNumber number)
   {
      return new EPSDouble(doubleValue()/number.doubleValue());
   }

   public EPSInteger idiv(EPSNumber number)
   {
      return new EPSInteger(value_ / number.intValue());
   }

   public EPSNumber mod(EPSNumber number)
   {
      return new EPSInteger(value_ % number.intValue());
   }

   public int intValue()
   {
      return value_;
   }

   public long longValue()
   {
      return (long)value_;
   }

   public double doubleValue()
   {
      return (double)value_;
   }

   public float floatValue()
   {
      return (float)value_;
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
      return new EPSName("integertype");
   }

   public Object clone()
   {
      return new EPSInteger(value_);
   }

   private int value_;
}
