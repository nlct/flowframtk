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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    throws NumberFormatException
   {
      Matcher m = PATTERN.matcher(string);

      if (!m.matches())
      {
         throw new NumberFormatException(string);
      }

      double num = Double.parseDouble(m.group(1));

      if (m.groupCount() == 1)
      {
         return new PercentageZoomValue(num);
      }

      return new PercentageZoomValue(num/100.0);
   }

   public String toString()
   {
      return String.format("%d%%", Math.round(value*100.0));
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

   public static final Pattern PATTERN = Pattern.compile("\\s*(\\d*\\.?\\d+)\\s*(%)?\\s*");
}
