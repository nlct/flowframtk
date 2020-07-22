// File          : JDRDistortable.java
// Date          : 2012-06-15
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2012 Nicola L.C. Talbot

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

import java.io.*;
import java.awt.Shape;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import com.dickimawbooks.jdr.exceptions.*;

public interface JDRDistortable extends JDRGraphicObject
{
   public BBox getStorageDistortionBounds();

   public BBox getBpDistortionBounds();

   public BBox getComponentDistortionBounds();

   public void distort(JDRDistortable original,
      Shape[] area,
      AffineTransform[] trans);

   public JDRTextual getTextual();
   public boolean hasTextual();
   public boolean hasShape();
   public JDRSymmetricPath getSymmetricPath();
   public boolean hasSymmetricPath();
   public boolean hasPattern();
   public Object[] getDescriptionInfo();
   public JDRPattern getPattern();
   public Object clone();

   public int getHotspotFromStoragePoint(Point2D p);
   public int getHotspotFromBpPoint(Point2D p);
   public int getHotspotFromComponentPoint(Point2D p);

   public JDRPoint getTopRightHS();
   public JDRPoint getBottomRightHS();
   public JDRPoint getBottomLeftHS();
   public JDRPoint getTopLeftHS();
   public JDRPoint getCentreHS();
   public BBox getDragBBox();

   public JDRPoint getControlFromStoragePoint(
     double storagePointX, double storagePointY, boolean endPoint);

}
