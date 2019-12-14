// File          : HexField.java
// Description   : Provide text fields that only allows positive
//                 hexadecimal numbers
// Creation Date : 6th February 2006
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
 * Text field that only allows positive hexadecimal numbers.
 * @author Nicola L C Talbot
 */
public class HexField extends NumberField
{
   public HexField(int defval)
   {
      super(defval, new HexFieldValidator());
   }

   public int getInt()
   {
      String text = getText().trim();

      if (text.isEmpty()) return 0;

      return Integer.parseInt(text, 16);
   }

   public byte getByte()
   {
      String text = getText().trim();

      if (text.isEmpty()) return (byte)0;

      return Byte.parseByte(text, 16);
   }

   public long getLong()
   {
      String text = getText().trim();

      if (text.isEmpty()) return 0L;

      return Long.parseLong(text, 16);
   }
}
