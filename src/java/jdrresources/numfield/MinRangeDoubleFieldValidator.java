// File          : MinRangeDoubleFieldValidator.java
// Description   : Double number validator that has a minimum value
// Creation Date : 2014-04-01
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
package com.dickimawbooks.jdrresources.numfield;

public class MinRangeDoubleFieldValidator extends NumberFieldValidator
{
   public MinRangeDoubleFieldValidator(double minValue)
   {
      super();
      this.minValue = minValue;
   }

   public boolean isValid(Number value)
   {
      return isValid(value.doubleValue());
   }

   public boolean isValid(double value)
   {
      return value >= minValue;
   }

   public boolean isValid(float value)
   {
      return isValid((double)value);
   }

   public boolean isValid(byte value)
   {
      return isValid((double)value);
   }

   public boolean isValid(int value)
   {
      return isValid((double)value);
   }

   public boolean isValid(short value)
   {
      return isValid((double)value);
   }

   public boolean isValid(long value)
   {
      return isValid((double)value);
   }

   public boolean allowsNegative()
   {
      return minValue < 0;
   }

   private double minValue;
}
