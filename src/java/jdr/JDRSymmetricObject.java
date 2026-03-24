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

public class JDRSymmetricObject extends JDRObjectReference
{
   public JDRSymmetricObject(JDRCompleteObject object)
    throws NullPointerException
   {
      super(object);

      symmetric = object.getSymmetricPath();

      if (symmetric == null)
      {
         throw new NullPointerException();
      }
   }

   public JDRSymmetricObject(JDRCompleteObject object, JDRSymmetricPath symmetric)
    throws NullPointerException
   {
      super(object);

      if (symmetric == null)
      {
         throw new NullPointerException();
      }

      this.symmetric = symmetric;
   }

   public JDRSymmetricPath getSymmetricPath()
   {
      return symmetric;
   }

   JDRSymmetricPath symmetric;
}
