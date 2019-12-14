// File          : EPSNumber.java
// Purpose       : interface representing an EPS number
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
 * Interface representing an EPS number.
 * @author Nicola L C Talbot
 */
public interface EPSNumber extends EPSObject
{
   /**
    * Sets the value represented by this object to the given value.
    * @param value the value to which this object must be set
    */
   public void set(int value);
   /**
    * Sets the value represented by this object to the given value.
    * (Truncation may occur if this number can only represent
    * <code>int</code> values.)
    * @param value the value to which this object must be set
    */
   public void set(long value);
   /**
    * Sets the value represented by this object to the given value.
    * (Truncation may occur if this number can't represent a
    * double precision number.)
    * @param value the value to which this object must be set
    */
   public void set(double value);
   /**
    * Gets this number as an <code>int</code>.
    * @return this number as an <code>int</code>
    */
   public int intValue() throws NumberFormatException;
   /**
    * Gets this number as a <code>double</code>.
    * @return this number as a <code>double</code>
    */
   public double doubleValue() throws NumberFormatException;
   /**
    * Gets this number as a <code>long</code>.
    * @return this number as a <code>long</code>
    */
   public long longValue() throws NumberFormatException;

   /**
    * Gets this number as a <code>float</code>.
    * @return this number as a <code>float</code>
    */
   public float floatValue() throws NumberFormatException;

  /**
    * Returns the sum of this and the given number.
    * @param number the number to add to this number
    * @return the sum of this and the given number
    */

   public EPSNumber add(EPSNumber number);
   /**
    * Returns this number less the given number.
    * @param number the number to subtract from this number
    * @return this number less the given number
    */

   public EPSNumber sub(EPSNumber number);

   /**
    * Returns the negative of this number.
    * @return the negative of this number
    */
   public EPSNumber neg(); 

   /**
    * Returns the produce of this and the given number.
    * @param number the number to multiply to this number
    * @return the product of this and the given number
    */
   public EPSNumber mul(EPSNumber number); 

   /**
    * Returns this number divided by the given number.
    * @param number the divisor
    * @return this number divided by the given number
    */
   public EPSDouble div(EPSNumber number);

   /**
    * Returns this number divided by the given number using
    * integer division.
    * @param number the divisor
    * @return this number divided by the given number using
    * integer division
    */
   public EPSInteger idiv(EPSNumber number);

   /**
    * Returns this number modulo the given number.
    * @param number the number with which to modulo this number
    * @return this number modulo the given number
    */
   public EPSNumber mod(EPSNumber number);

   /**
    * Returns the absolute value of this number.
    * @return the absolute value of this number
    */
   public EPSNumber abs();

   /**
    * Returns the smallest (closest to negative infinity) number
    * that is greater than or equal to this number and is
    * a mathematical integer.
    * @return the smallest (closest to negative infinity) number
    * that is greater than or equal to this number and is
    * a mathematical integer
    */
   public EPSNumber ceiling();

   /**
    * Returns the largest (closest to positive infinity) number
    * that is less than or equal to this number and is
    * a mathematical integer.
    * @return the largest (closest to positive infinity) number
    * that is less than or equal to this number and is
    * a mathematical integer
    */
   public EPSNumber floor();

   /**
    * Returns the closest number that is a mathematical
    * integer to this number.
    * @return the closest number that is a mathematical
    * integer to this number
    */
   public EPSNumber round();

   /**
    * Returns the value of this number with the decimal part chopped off.
    * @return the value of this number with the decimal part chopped off
    */
   public EPSNumber truncate();

   /**
    * Bitshift operation.
    * @param number
    * @return this number bitshift the other number
    * @throws InvalidEPSObjectException if either of the numbers
    * aren't {@link EPSInteger} or {@link EPSLong}.
    */
   public EPSNumber bitshift(EPSNumber number)
      throws InvalidEPSObjectException;
}
