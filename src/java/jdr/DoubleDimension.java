// File          : DoubleDimension.java
// Creation Date : 2015-10-21
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

package com.dickimawbooks.jdr;

import java.awt.*;
import java.awt.geom.*;

public class DoubleDimension extends Dimension2D
{
   public DoubleDimension()
   {
      this(0, 0);
   }

   public DoubleDimension(double w, double h)
   {
      super();
      this.width = w;
      this.height = h;
   }

   public Object clone()
   {
      return new DoubleDimension(width, height);
   }

   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof Dimension2D)) return false;

      if (this == obj) return true;

      Dimension2D dim = (Dimension2D)obj;

      return (width == dim.getWidth()) && (height == dim.getHeight());
   }

   public double getHeight()
   {
      return height;
   }

   public double getWidth()
   {
      return width;
   }

   public void setWidth(double w)
   {
      this.width = w;
   }

   public void setHeight(double h)
   {
      this.height = h;
   }

   public void setSize(double w, double h)
   {
      this.width = w;
      this.height = h;
   }

   public void setSize(Dimension2D dim)
   {
      this.width = dim.getWidth();
      this.height = dim.getHeight();
   }

   public String toString()
   {
      return "DoubleDimension["+width+","+height+"]";
   }

   private double width, height;
}
