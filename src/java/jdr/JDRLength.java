//File          : JDRLength.java
//Description   : Class representing a length
//Author        : Nicola L.C. Talbot
//Creation Date : 2014-04-01
//              http://www.dickimaw-books.com/

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
package com.dickimawbooks.jdr;

import java.io.IOException;
import java.io.Serializable;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.dickimawbooks.jdr.io.JDRAJR;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;
import com.dickimawbooks.jdr.exceptions.*;

public class JDRLength implements Serializable,Cloneable
{
   public JDRLength(CanvasGraphics cg)
   {
      this(cg.getMessageDictionary(), 0.0, JDRUnit.bp);
   }

   public JDRLength(CanvasGraphics cg, double value, JDRUnit unit)
   {
      this(cg.getMessageDictionary(), value, unit);
   }

   public JDRLength(JDRMessageDictionary msgSys)
   {
      this(msgSys, 0.0, JDRUnit.bp);
   }

   public JDRLength(JDRMessageDictionary msgSys, double value, JDRUnit unit)
   {
      this.messageSystem = msgSys;
      this.value = value;
      this.unit = unit;
   }

   public JDRLength(JDRLength length)
   {
      messageSystem = length.messageSystem;
      value = length.value;
      unit = length.unit;
   }

   public double getValue()
   {
      return value;
   }

   public JDRUnit getUnit()
   {
      return unit;
   }

   /**
    * Gets this length in terms of another unit.
    */
   public double getValue(JDRUnit otherUnit)
   {
      if (unit.getID() == otherUnit.getID())
      {
         return value;
      }

      return unit.toUnit(value, otherUnit);
   }

   public void setValue(double newValue)
   {
      value = newValue;
   }

   public void setValue(double newValue, JDRUnit newUnit)
   {
      this.value = newValue;
      this.unit = newUnit;
   }

   public void changeUnit(JDRUnit newUnit)
   {
      if (unit.getID() == newUnit.getID())
      {
         return;
      }

      value = unit.toUnit(value, newUnit);
      unit = newUnit;
   }

   public void makeEqual(JDRLength length)
   {
      messageSystem = length.messageSystem;
      value = length.value;
      unit = length.unit;
   }

   public Object clone()
   {
      return new JDRLength(getMessageSystem(), value, unit);
   }

   public String toString()
   {
      return "" + value + unit.getLabel();
   }

   public void scale(double factor)
   {
      value *= factor;
   }

   public void add(JDRLength otherLength)
   {
      add(otherLength.getValue(), otherLength.getUnit());
   }

   public void add(double otherValue, JDRUnit otherUnit)
   {
      if (unit.getID() == otherUnit.getID())
      {
         value += otherValue;
         return;
      }

      value += unit.fromUnit(otherValue, otherUnit);
   }

   public void subtract(JDRLength otherLength)
   {
      subtract(otherLength.getValue(), otherLength.getUnit());
   }

   public void subtract(double otherValue, JDRUnit otherUnit)
   {
      if (unit.getID() == otherUnit.getID())
      {
         value -= otherValue;
         return;
      }

      value -= unit.fromUnit(otherValue, otherUnit);
   }

   public static JDRLength parse(JDRMessageDictionary msgSys, String text)
     throws InvalidValueException
   {
      Matcher m = PATTERN_LENGTH.matcher(text);

      if (!m.matches())
      {
         throw new InvalidValueException(
            InvalidFormatException.LENGTH, text, msgSys);
      }

      double value;

      try
      {
         value = Double.parseDouble(m.group(1));
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
            InvalidFormatException.LENGTH, text, msgSys, e);
      }

      String unitLabel = m.group(2);

      JDRUnit unit = JDRUnit.getUnit(unitLabel);

      if (unit == null)
      {
         throw new InvalidValueException(
            InvalidFormatException.UNIT_NAME, unitLabel, msgSys);
      }

      return new JDRLength(msgSys, value, unit);
   }

   public boolean equals(Object o)
   {
      if (this == o) return true;

      if (o == null) return false;

      if (!(o instanceof JDRLength)) return false;

      JDRLength length = (JDRLength)o;

      if (unit.getID() == length.unit.getID())
      {
         return value == length.value;
      }

      return value == length.getValue(unit);
   }

   public String svg()
   {
      return unit.svg(value);
   }

   public void setMessageSystem(JDRMessageDictionary msgSys)
   {
      this.messageSystem = msgSys;
   }

   public JDRMessageDictionary getMessageSystem()
   {
      return messageSystem;
   }

   private JDRUnit unit;
   private double value;

   private JDRMessageDictionary messageSystem;

   public static final Pattern PATTERN_LENGTH
     = Pattern.compile("\\s*([+\\-]?\\d*\\.?\\d+(?:[Ee][+\\-]*)?\\d*)\\s*([a-z]{2})\\s*");
}
