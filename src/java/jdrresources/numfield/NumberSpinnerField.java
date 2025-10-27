// File          : NumberSpinnerField.java
// Description   : Provide spinner that implements NumberComponent
// Creation Date : 2020-08-10
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2020 Nicola L.C. Talbot

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

import javax.swing.*;
import javax.swing.event.*;

/**
 * Number spinner that implements NumberComponent.
 * @author Nicola L C Talbot
 */
public class NumberSpinnerField extends JSpinner implements NumberComponent
{
   /**
    * Initialise with given value.
    * @param defval the initial value of this text field
    */
   public NumberSpinnerField(double defVal, double min, double max, double step)
   {
      super(new SpinnerNumberModel(defVal, min, max, step));
   }

   public NumberSpinnerField(float defVal, float min, float max, float step)
   {
      super(new SpinnerNumberModel(defVal, min, max, step));
   }

   public NumberSpinnerField(byte defVal, byte min, byte max, byte step)
   {
      super(new SpinnerNumberModel(defVal, min, max, step));
   }

   public NumberSpinnerField(int defVal, int min, int max, int step)
   {
      super(new SpinnerNumberModel(defVal, min, max, step));
   }

   public NumberSpinnerField(long defVal, long min, long max, long step)
   {
      super(new SpinnerNumberModel(defVal, min, max, step));
   }

   public NumberSpinnerField(Number defVal, Comparable min, Comparable max,
      Number step)
   {
      super(new SpinnerNumberModel(defVal, min, max, step));

      JSpinner.NumberEditor editor = (JSpinner.NumberEditor)getEditor();
      JFormattedTextField field = editor.getTextField();
      int cols = field.getColumns();

      if (cols < 3)
      {
         field.setColumns(3);
      }
   }

   public NumberSpinnerField()
   {
      this(Double.valueOf(0.0), null, null, 1.0);
   }

   public NumberSpinnerField(int defVal)
   {
      this(Integer.valueOf(defVal), null, null, 1);
   }

   public NumberSpinnerField(double defVal)
   {
      this(Double.valueOf(defVal), null, null, 1.0);
   }

   public static NumberSpinnerField createNonNegativeDoubleField()
   {
      return new NumberSpinnerField(
       Double.valueOf(0.0), Double.valueOf(0.0), null, Double.valueOf(1.0));
   }

   public static NumberSpinnerField createNonNegativeDoubleField(double defVal)
   {
      return new NumberSpinnerField(
       Double.valueOf(defVal), Double.valueOf(0.0), null, Double.valueOf(1.0));
   }

   public static NumberSpinnerField createNonNegativeIntField()
   {
      return new NumberSpinnerField(
       Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1));
   }

   public static NumberSpinnerField createPositiveIntField()
   {
      return createPositiveIntField(1);
   }

   public static NumberSpinnerField createPositiveIntField(int defValue)
   {
      return new NumberSpinnerField(
       Integer.valueOf(defValue), Integer.valueOf(1), null, Integer.valueOf(1));
   }

   public void setColumns(int columns)
   {
      JSpinner.DefaultEditor editor = (JSpinner.NumberEditor)getEditor();
      JFormattedTextField field = editor.getTextField();
      field.setColumns(columns);
   }

   public void setNumber(Number number)
   {
      setValue(number);
   }

   public void setValue(double val)
   {
      setNumber(Double.valueOf(val));
   }

   public void setValue(float val)
   {
      setValue((double)val);
   }

   public void setValue(byte val)
   {
      setValue((int)val);
   }

   public void setValue(int val)
   {
      setNumber(Integer.valueOf(val));
   }

   public void setValue(long val)
   {
      setNumber(Long.valueOf(val));
   }

   public void setValue(short val)
   {
      setNumber(Short.valueOf(val));
   }

   public Number getNumber()
   {
      return (Number)getValue();
   }

   public double getDouble()
   {
      return getNumber().doubleValue();
   }

   public float getFloat()
   {
      return getNumber().floatValue();
   }

   public byte getByte()
   {
      return getNumber().byteValue();
   }

   public int getInt()
   {
      return getNumber().intValue();
   }

   public long getLong()
   {
      return getNumber().longValue();
   }

   public short getShort()
   {
      return getNumber().shortValue();
   }

   public JTextField getTextField()
   {
      return ((DefaultEditor)getEditor()).getTextField();
   }

   public JComponent getComponent()
   {
      return this;
   }
}

