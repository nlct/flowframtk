// File          : IntRangeField.java
// Description   : Provide text fields that only allows integers
//                 in a given range
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
 * Text field that only allows integers in a given range.
 * @author Nicola L C Talbot
 */
public class IntRangeField extends NumberField
{
   /**
    * Initialises with the given range and initial value.
    * @param defval the initial value
    * @param minValue the minimum allowed value (an empty text
    * field is associated with the minimum value)
    * @param maxValue the maximum allowed value
    */
   public IntRangeField(int defval, int minValue, int maxValue)
   {
      super(defval, new RangeIntFieldValidator(minValue, maxValue));
   }

}

