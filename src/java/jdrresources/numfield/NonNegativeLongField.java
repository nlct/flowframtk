// File          : NonNegativeLongField.java
// Description   : Provide text fields that only allow non-negative long integers
// Creation Date : 2014-05-09
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
 * Text field that only allows non-negative long integers.
 * @author Nicola L C Talbot
 */
public class NonNegativeLongField extends NumberField
{
   /**
    * Initialise with given value.
    * @param defval the initial value of this text field
    */
   public NonNegativeLongField(long defval)
   {
      super(defval, new MinRangeLongFieldValidator(0));
   }

}

