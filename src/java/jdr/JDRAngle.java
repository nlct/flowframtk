//File          : JDRAngle.java
//Description   : Class representing an angle
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

import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.NumberFormat;
import java.text.ParseException;

import com.dickimawbooks.jdr.io.JDRAJR;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;
import com.dickimawbooks.jdr.exceptions.*;

public class JDRAngle implements Serializable,Cloneable
{
   public JDRAngle(CanvasGraphics cg)
   {
      this(cg.getMessageDictionary(), 0.0, RADIAN);
   }

   public JDRAngle(JDRMessageDictionary msgDict)
   {
      this(msgDict, 0.0, RADIAN);
   }

   public JDRAngle(CanvasGraphics cg, double value, byte unitId)
   {
      this(cg.getMessageDictionary(), value, unitId);
   }

   public JDRAngle(JDRMessageDictionary msgDict, double value, byte unitId)
   {
      this.messageDict = msgDict;
      this.value = value;

      if (unitId == RADIAN || unitId == DEGREE)
      {
         this.unit = unitId;
      }
      else
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.ANGLE_ID, unitId, msgDict);
      }
   }

   public Object clone()
   {
      return new JDRAngle(getMessageDictionary(), value, unit);
   }

   public String toString()
   {
      return String.format(Locale.ROOT, "%f %s", value, getLabel());
   }

   public String getLabel()
   {
      return unit == RADIAN ? "rad" : "deg";
   }

   public double getValue()
   {
      return value;
   }

   public double getValue(int otherUnit)
   {
      if (otherUnit == unit)
      {
         return value;
      }

      if (otherUnit == RADIAN)
      {
         return toRadians();
      }

      return toDegrees();
   }

   public byte getUnitId()
   {
      return unit;
   }

   public double toRadians()
   {
      return unit == RADIAN ? value : Math.toRadians(value);
   }

   public double toDegrees()
   {
      return unit == DEGREE ? value : Math.toDegrees(value);
   }

   public void fromRadians(double radianValue)
   {
      if (unit == RADIAN)
      {
         value = radianValue;
         return;
      }

      value = Math.toDegrees(radianValue);
   }

   public String svg()
   {
      return String.format(Locale.ROOT,
        "%f%s", value, (unit == DEGREE ? "deg" : "rad"));
   }

   public void makeEqual(JDRAngle angle)
   {
      messageDict = angle.messageDict;
      value = angle.value;
      unit = angle.unit;
   }

   public boolean equals(JDRAngle angle)
   {
      if (unit == angle.unit)
      {
         return value == angle.value;
      }

      return (toRadians() == angle.toRadians());
   }

   public void scale(double factor)
   {
      value *= factor;
   }

   public void add(JDRAngle otherAngle)
   {
      if (unit == otherAngle.unit)
      {
         value += otherAngle.value;
         return;
      }

      if (unit == RADIAN)
      {
         value += otherAngle.toRadians();
      }
      else
      {
         value += otherAngle.toDegrees();
      }
   }

   public void convertTo(byte otherUnitId)
   {
      if (unit == otherUnitId) return;

      if (otherUnitId == RADIAN)
      {
         value = Math.toRadians(value);
      }
      else
      {
         value = Math.toDegrees(value);
      }

      unit = otherUnitId;
   }

   public static JDRAngle read(JDRAJR jdr, String identifier,
     boolean oldUseFloat)
     throws InvalidFormatException
   {
      float version = jdr.getVersion();

      if (version < 1.8f)
      {
         return new JDRAngle(jdr.getMessageDictionary(),
           oldUseFloat ? jdr.readFloat(identifier) : jdr.readDouble(), RADIAN);
      }
      else
      {
         return jdr.readAngle(identifier);
      }
   }

   public void save(JDRAJR jdr, boolean oldUseFloat)
      throws IOException
   {
      if (jdr.getVersion() < 1.8f)
      {
         if (oldUseFloat)
         {
            jdr.writeFloat((float)toRadians());
         }
         else
         {
            jdr.writeDouble(toRadians());
         }
      }
      else
      {
         jdr.writeAngle(this);
      }
   }

   public static JDRAngle parse(JDRMessageDictionary msgDict, String text)
     throws InvalidValueException
   {
      Matcher m = PATTERN_ANGLE.matcher(text);

      if (!m.matches())
      {
         throw new InvalidValueException(
            InvalidFormatException.ANGLE, text, msgDict);
      }

      String valueText = m.group(1);
      double value;

      try
      {
         value = Double.parseDouble(valueText);
      }
      catch (NumberFormatException e)
      {
         // allow for earlier versions that might've used a
         // locale-sensitive format

         try
         {
            value = NumberFormat.getInstance().parse(valueText).doubleValue();
         }
         catch (ParseException pe)
         {
            throw new InvalidValueException(
               InvalidFormatException.ANGLE_VALUE, valueText, msgDict, pe);
         }
      }

      String unitLabel = m.group(2);

      byte unitId;

      if (unitLabel.equals("rad"))
      {
         unitId = RADIAN;
      }
      else if (unitLabel.equals("deg"))
      {
         unitId = DEGREE;
      }
      else
      {
         throw new InvalidValueException(
            InvalidFormatException.ANGLE_ID, unitLabel, msgDict);
      }

      return new JDRAngle(msgDict, value, unitId);
   }

   public void setMessageSystem(JDRMessageDictionary msgDict)
   {
      this.messageDict = msgDict;
   }

   public JDRMessageDictionary getMessageDictionary()
   {
      return messageDict;
   }

   private byte unit;
   private double value;

   private JDRMessageDictionary messageDict;

   public static final byte RADIAN = 0;
   public static final byte DEGREE = 1;

   public static final Pattern PATTERN_ANGLE
     = Pattern.compile("\\s*([+\\-]?\\d*[\\.,]?\\d+(?:[Ee][+\\-])?\\d*)\\s*([a-z]{3})\\s*");
}
