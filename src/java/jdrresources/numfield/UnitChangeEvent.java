// File          : UnitChangeEvent.java
// Description   : Event for unit changes
// Date          : 2020-09-02
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2020 Nicola L.C. Talbot

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

import java.util.EventObject;

import com.dickimawbooks.jdr.JDRUnit;

public class UnitChangeEvent extends EventObject
{
   public UnitChangeEvent(Object source, JDRUnit oldUnit, JDRUnit newUnit)
   {
      super(source);
      this.oldUnit = oldUnit;
      this.newUnit = newUnit;
   }

   public JDRUnit getOldUnit()
   {
      return oldUnit;
   }

   public JDRUnit getNewUnit()
   {
      return newUnit;
   }

   private JDRUnit oldUnit, newUnit;
}
