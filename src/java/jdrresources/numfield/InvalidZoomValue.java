// File          : InvalidZoomValue.java
// Description   : Invalid Zoom setting
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

public class InvalidZoomValue extends ZoomValue
{
   public InvalidZoomValue(String text, String msg)
   {
      super(ZOOM_INVALID_ID);
      this.text = text;
      this.msg = msg;
   }

   public String toString()
   {
      return text;
   }

   public boolean equals(Object obj)
   {
      if (!(obj instanceof InvalidZoomValue))
      {
         return false;
      }

      return ((InvalidZoomValue)obj).toString().equals(toString());
   }

   public String getMessage()
   {
      return msg;
   }

   private String text, msg;
}
