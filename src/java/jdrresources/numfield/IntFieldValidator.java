// File          : IntFieldValidator.java
// Description   : Integer number field
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

public class IntFieldValidator extends NumberFieldValidator
{
   public IntFieldValidator()
   {
      super();
   }

   public boolean isValid(Number value)
   {
      if (value instanceof Integer || value instanceof Byte
           || value instanceof Short)
      {
         return isValid(value.intValue());
      }

      if (value instanceof Long)
      {
         return isValid(value.longValue());
      }

      return false;
   }

   public boolean isValid(int value)
   {
      return true;
   }

   public boolean isValid(byte value)
   {
      return isValid((int)value);
   }

   public boolean isValid(short value)
   {
      return isValid((int)value);
   }

   public boolean isValid(long value)
   {
      return (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE)
         && isValid((int)value);
   }

   public boolean isValid(float value)
   {
      return false;
   }

   public boolean isValid(double value)
   {
      return false;
   }

   public boolean allowsNegative()
   {
      return true;
   }

   public boolean isValid(String text)
   {
      if (text.equals("-"))
      {
         if (!allowsNegative()) return false;

         text = "0";
      }

      try
      {
         int value = Integer.parseInt(text);

         return isValid(value);
      }
      catch (NumberFormatException e)
      {
         return false;
      }
      catch (NullPointerException e)
      {
         return false;
      }
   }

}
