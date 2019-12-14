// File          : NumberField.java
// Description   : Provide text fields that only allow numerical values
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * Text field that only allows numbers.
 * @author Nicola L C Talbot
 */
public class NumberField extends JTextField
{
   /**
    * Initialise with given value.
    * @param defval the initial value of this text field
    */
   public NumberField(double defval, NumberFieldValidator validator)
   {
      this(""+defval, validator);
   }

   public NumberField(float defval, NumberFieldValidator validator)
   {
      this(""+defval, validator);
   }

   public NumberField(byte defval, NumberFieldValidator validator)
   {
      this(""+defval, validator);
   }

   public NumberField(int defval, NumberFieldValidator validator)
   {
      this(""+defval, validator);
   }

   public NumberField(long defval, NumberFieldValidator validator)
   {
      this(""+defval, validator);
   }

   private NumberField(String text, NumberFieldValidator validator)
   {
      super(new NumberDocument(validator), text, 3);
      setHorizontalAlignment(JTextField.RIGHT);
      this.validator = validator;
      addFocusListener(new TextFieldFocusListener(this));
   }

   protected Document createDefaultModel()
   {
      return new NumberDocument(validator);
   }

   public void setValue(double val)
   {
      if (validator.isValid(val))
      {
         super.setText(""+val);
         setCaretPosition(0);
      }
   }

   public void setValue(float val)
   {
      if (validator.isValid(val))
      {
         super.setText(""+val);
         setCaretPosition(0);
      }
   }

   public void setValue(byte val)
   {
      if (validator.isValid(val))
      {
         super.setText(""+val);
      }
   }

   public void setValue(int val)
   {
      if (validator.isValid(val))
      {
         super.setText(""+val);
      }
   }

   public void setValue(long val)
   {
      if (validator.isValid(val))
      {
         super.setText(""+val);
      }
   }

   public double getDouble()
   {
      return validator.getDouble(getText());
   }

   public float getFloat()
   {
      return validator.getFloat(getText());
   }

   public byte getByte()
   {
      return validator.getByte(getText());
   }

   public int getInt()
   {
      return validator.getInt(getText());
   }

   public long getLong()
   {
      return validator.getLong(getText());
   }

   private NumberFieldValidator validator;
}

