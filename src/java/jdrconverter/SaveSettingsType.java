/*
    Copyright (C) 2025 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.dickimawbooks.jdrconverter;

public enum SaveSettingsType
{
   NONE("none", 0), ALL("all", 1), PAPER_ONLY("paper-only", 2),
   MATCH_INPUT("match-input", 3);

   private SaveSettingsType(final String tag, final int id)
   {
      this.tag = tag;
      this.id = id;
   }

   public static SaveSettingsType getFromTag(String typeTag)
   {
      for (SaveSettingsType type : values())
      {
         if (type.tag.equals(typeTag))
         {
            return type;
         }
      }

      return null;
   }

   public static SaveSettingsType valueOf(int typeId)
   {
      for (SaveSettingsType type : values())
      {
         if (type.id == typeId)
         {
            return type;
         }
      }

      return null;
   }

   public String getTag()
   {
      return tag;
   }

   public int getId()
   {
      return id;
   }

   private final String tag;
   private final int id;
}
