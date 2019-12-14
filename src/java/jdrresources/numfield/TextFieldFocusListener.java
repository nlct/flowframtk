// File          : TextFieldFocusListener.java
// Description   : Focus listener that selects all text in the text
//                 field on gaining the focus.
// Creation Date : 2014-06-05
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

import java.text.DecimalFormat;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

/**
 * Focus listener that selects all text in the text
 * field on gaining the focus.
 * @author Nicola L C Talbot
 */

public class TextFieldFocusListener implements FocusListener
{
   public TextFieldFocusListener(JTextField field)
   {
      textField = field;
   }

   public void focusGained(FocusEvent e)
   {
      textField.setCaretPosition(textField.getText().length());
      textField.moveCaretPosition(0);
   }

   public void focusLost(FocusEvent e)
   {
      int end = textField.getSelectionEnd();
      textField.setSelectionStart(end);
      textField.setSelectionEnd(end);
   }

   private JTextField textField;
}
