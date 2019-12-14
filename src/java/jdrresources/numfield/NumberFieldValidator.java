// File          : NumberFieldValidator.java
// Description   : Validator for numerical fields
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

public class NumberFieldValidator
{
   public NumberFieldValidator()
   {
   }

   public boolean isValid(int value)
   {
      return true;
   }

   public boolean isValid(long value)
   {
      return true;
   }

   public boolean isValid(byte value)
   {
      return true;
   }

   public boolean isValid(float value)
   {
      return true;
   }

   public boolean isValid(double value)
   {
      return true;
   }

   public int getDefaultInt()
   {
      return 0;
   }

   public long getDefaultLong()
   {
      return 0L;
   }

   public byte getDefaultByte()
   {
      return (byte)0;
   }

   public float getDefaultFloat()
   {
      return 0f;
   }

   public double getDefaultDouble()
   {
      return 0;
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
         double value = Double.parseDouble(text);

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

   public double getDouble(String text)
   {
      double value = getDefaultDouble();

      try
      {
         value = Double.parseDouble(text);
      }
      catch (NumberFormatException e)
      {
      }
      catch (NullPointerException e)
      {
      }

      return isValid(value) ? value : getDefaultDouble();
   }

   public float getFloat(String text)
   {
      float value = getDefaultFloat();

      try
      {
         value = Float.parseFloat(text);
      }
      catch (NumberFormatException e)
      {
      }
      catch (NullPointerException e)
      {
      }

      return isValid(value) ? value : getDefaultFloat();
   }

   public byte getByte(String text)
   {
      byte value = getDefaultByte();

      try
      {
         value = Byte.parseByte(text);
      }
      catch (NumberFormatException e)
      {
      }
      catch (NullPointerException e)
      {
      }

      return isValid(value) ? value : getDefaultByte();
   }

   public int getInt(String text)
   {
      int value = getDefaultInt();

      try
      {
         value = Integer.parseInt(text);
      }
      catch (NumberFormatException e)
      {
      }
      catch (NullPointerException e)
      {
      }

      return isValid(value) ? value : getDefaultInt();
   }

   public long getLong(String text)
   {
      long value = getDefaultLong();

      try
      {
         value = Long.parseLong(text);
      }
      catch (NumberFormatException e)
      {
      }
      catch (NullPointerException e)
      {
      }

      return isValid(value) ? value : getDefaultLong();
   }

}
