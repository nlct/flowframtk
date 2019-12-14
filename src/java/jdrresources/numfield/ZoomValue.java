// File          : ZoomValue.java
// Description   : Zoom setting
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

public class ZoomValue
{
   public ZoomValue(String action)
   {
      this.action = action;
   }

   public String getActionCommand()
   {
      return action;
   }

   private String action;

   public static final String ZOOM_PAGE_WIDTH_ID="zoom.width";
   public static final String ZOOM_PAGE_HEIGHT_ID="zoom.height";
   public static final String ZOOM_PAGE_ID="zoom.page";
   public static final String ZOOM_USER_ID="zoom.user";
   public static final String ZOOM_INVALID_ID="zoom.invalid";
}
