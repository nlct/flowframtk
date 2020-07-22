// File          : InvalidValueException.java
// Creation Date : 2014-03-26
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

package com.dickimawbooks.jdr.exceptions;

import com.dickimawbooks.jdr.io.JDRAJR;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;
import com.dickimawbooks.jdr.CanvasGraphics;

/**
 * Exception thrown when a value is invalid.
 * @author Nicola L C Talbot
 */
public class InvalidValueException extends InvalidFormatException
{
   public InvalidValueException(String tag, int value, JDRAJR jdr)
   {
      this(tag, new Integer(value), jdr);
   }

   public InvalidValueException(String tag, double value, JDRAJR jdr)
   {
      this(tag, new Double(value), jdr);
   }

   public InvalidValueException(String tag, float value, JDRAJR jdr)
   {
      this(tag, new Float(value), jdr);
   }

   public InvalidValueException(String tag, byte value, JDRAJR jdr)
   {
      this(tag, new Byte(value), jdr);
   }

   public InvalidValueException(String tag, char value, JDRAJR jdr)
   {
      this(tag, new Character(value), jdr);
   }

   public InvalidValueException(String tag, int value, JDRAJR jdr, Throwable cause)
   {
      this(tag, new Integer(value), jdr, cause);
   }

   public InvalidValueException(String tag, double value, JDRAJR jdr, Throwable cause)
   {
      this(tag, new Double(value), jdr, cause);
   }

   public InvalidValueException(String tag, float value, JDRAJR jdr, Throwable cause)
   {
      this(tag, new Float(value), jdr, cause);
   }

   public InvalidValueException(String tag, byte value, JDRAJR jdr, Throwable cause)
   {
      this(tag, new Byte(value), jdr, cause);
   }

   public InvalidValueException(String tag, char value, JDRAJR jdr, Throwable cause)
   {
      this(tag, new Character(value), jdr, cause);
   }

   public InvalidValueException(String tag, Object value, CanvasGraphics cg)
   {
      this(tag, value, cg.getMessageDictionary());
   }

   public InvalidValueException(String tag, Object value, JDRMessageDictionary msgSys)
   {
      super(msgSys.getString("error.invalid_"+tag,
            String.format("Invalid %s", tag)));
      setTag(tag);
      setInvalidValue(value);
      setIdentifier(value);
   }

   public InvalidValueException(String tag, Object value, JDRAJR jdr)
   {
      super(jdr.getMessageSystem().getString(
            "error.invalid_"+tag,
            String.format("Invalid %s", tag)), jdr);
      setTag(tag);
      setInvalidValue(value);
      setIdentifier(value);
   }

   public InvalidValueException(String tag, Object value, CanvasGraphics cg, 
     Throwable cause)
   {
      this(tag, value, cg.getMessageDictionary(), cause);
   }

   public InvalidValueException(String tag, Object value, JDRMessageDictionary msgSys, 
     Throwable cause)
   {
      super(msgSys.getString("error.invalid_"+tag,
            String.format("Invalid %s", tag)), cause);
      setTag(tag);
      setInvalidValue(value);
      setIdentifier(value);
   }

   public InvalidValueException(String tag, Object value, 
     JDRAJR jdr, Throwable cause)
   {
      super(jdr.getMessageSystem().getString(
            "error.invalid_"+tag,
            String.format("Invalid %s", tag)), jdr, cause);
      setTag(tag);
      setInvalidValue(value);
      setIdentifier(value);
   }

   public InvalidValueException(String tag, JDRAJR jdr, Throwable cause)
   {
      super(jdr.getMessageSystem().getString(
            "error.invalid_"+tag,
            String.format("Invalid %s", tag)), jdr, cause);
      setTag(tag);
   }

   /**
    * Gets the invalid value.
    * @return invalid value
    */
   public Object getInvalidValue()
   {
      return invalidValue;
   }

   public void setInvalidValue(Object value)
   {
      invalidValue = value;
   }

   public void setInvalidValue(double value)
   {
      setInvalidValue(new Double(value));
   }

   public void setInvalidValue(float value)
   {
      setInvalidValue(new Float(value));
   }

   public void setInvalidValue(int value)
   {
      setInvalidValue(new Integer(value));
   }

   public void setInvalidValue(byte value)
   {
      setInvalidValue(new Byte(value));
   }

   public void setInvalidValue(char value)
   {
      setInvalidValue(new Character(value));
   }

   public void setTag(String tag)
   {
      this.tag = tag;
   }

   public String getTag()
   {
      return tag;
   }

   public String getMessageWithIdentifier(JDRMessageDictionary msgSys)
   {
      String msg = super.getMessageWithIdentifier(msgSys);

      if (invalidValue == null)
      {
         return msg;
      }

      String val = invalidValue.toString();

      if (msgSys == null)
      {
         return String.format("%s found '%s'", msg, val);
      }

      return msgSys.getMessageWithAlt("{0} found ''{1}''", 
        "error.with_found", msg, val);
   }

   private Object invalidValue;

   private String tag;
}
