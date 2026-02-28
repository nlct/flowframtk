/*
    Copyright (C) 2026 Nicola L.C. Talbot

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

package com.dickimawbooks.jdr;

public enum AnchorY
{
   TOP(0), MIDDLE(1), BASE(2), BOTTOM(3);

   AnchorY(int id)
   {
      this.id = id;
   }

   public int getId()
   {
      return id;
   }

   public static AnchorY valueOf(int anchorId)
   {
      for (AnchorY anchorY : values())
      {
         if (anchorY.id == anchorId) return anchorY;
      }

      throw new IllegalArgumentException("Invalid AnchorY ID "+anchorId);
   }

   final int id;
}
