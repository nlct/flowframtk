// File          : PercentageZoomValue.java
// Description   : Percentage object
// Date          : 2015-09-24
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

import java.text.NumberFormat;
import java.text.ParseException;

import com.dickimawbooks.jdrresources.JDRResources;

public class PercentageZoomValue extends ZoomValue
{
   public PercentageZoomValue(double value)
   {
      super(ZOOM_USER_ID);
      this.value = value;
   }

   public double getValue()
   {
      return value;
   }

   public static PercentageZoomValue parse(String string)
    throws ParseException
   {
      return new PercentageZoomValue(FORMAT.parse(string).doubleValue());
   }

   public String toString()
   {
      return FORMAT.format(value);
   }

   public boolean equals(Object obj)
   {
      if (!(obj instanceof PercentageZoomValue))
      {
         return false;
      }

      return ((PercentageZoomValue)obj).value == value;
   }

   private double value;

   public static final NumberFormat FORMAT = NumberFormat.getPercentInstance();
}
