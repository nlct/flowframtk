// File          : TextFieldSBarListener.java
// Description   : Listener for IntRangeField where a JScrollBar
//                 needs updating when the contents of the text
//                 field changes
// Date          : 6th July 2009
// Last Modified : 6th July 2009
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
 * Listener for IntRangeField where a scroll bar
 * needs repainting when the contents of the text
 * field changes
 * @author Nicola L C Talbot
 */
public class TextFieldSBarListener implements DocumentListener
{
   /**
    * Initialise with the given component.
    * @param intField the integer field associated with this listener
    * @param scrollBar the scroll bar that needs updating whenever 
    * the document contents change 
    */
   public TextFieldSBarListener(IntRangeField intField,
     JScrollBar scrollBar)
   {
      field = intField;
      sbar = scrollBar;
   }

   @Override
   public void insertUpdate(DocumentEvent e)
   {
      if (sbar != null)
      {
         sbar.setValue(field.getInt());
      }
   }

   @Override
   public void removeUpdate(DocumentEvent e)
   {
      if (sbar != null)
      {
         sbar.setValue(field.getInt());
      }
   }

   @Override
   public void changedUpdate(DocumentEvent e)
   {
      if (sbar != null)
      {
         sbar.setValue(field.getInt());
      }
   }

   private JScrollBar sbar;
   private IntRangeField field;
}

