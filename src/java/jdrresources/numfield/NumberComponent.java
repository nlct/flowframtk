// File          : NumberComponent.java
// Description   : Interface for components that only allow numerical values
// Creation Date : 2020-08-09
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeListener;

/**
 * Interface for components that only allows numbers.
 * @author Nicola L C Talbot
 */
public interface NumberComponent
{
   public void setNumber(Number val);

   public void setValue(double val);
   public void setValue(float val);
   public void setValue(byte val);
   public void setValue(int val);
   public void setValue(short val);
   public void setValue(long val);

   public double getDouble();
   public float getFloat();
   public byte getByte();
   public int getInt();
   public short getShort();

   public Number getNumber();

   public void addChangeListener(ChangeListener listener);

   public JTextField getTextField();

   public JComponent getComponent();
}

