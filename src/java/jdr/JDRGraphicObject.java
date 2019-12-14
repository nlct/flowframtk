// File          : JDRGraphicObject.java
// Creation Date : 2012-06-15
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
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.io.TeX;
import com.dickimawbooks.jdr.io.SVG;
import com.dickimawbooks.jdr.exceptions.*;

public interface JDRGraphicObject extends Cloneable
{
   public String info();
   public void setSelected(boolean flag);
   public boolean isSelected();

   /**
    * Rotates this object about the given point.
    * @param p the point of rotation
    * @param angle the angle of rotation 
    */ 
   public void rotate(Point2D p, double angle);

   /**
    * Scales this object.
    * @param factorX the x scale factor 
    * @param factorY the y scale factor 
    */
 
   public void scale(double factorX, double factorY);

   /**
    * Scales this object relative to the given point.
    * @param p the scaling origin
    * @param factorX the x scale factor 
    * @param factorY the y scale factor 
    */
 
   public void scale(Point2D p, double factorX, double factorY);

   /**
    * Shears this object.
    * @param factorX the x shear factor 
    * @param factorY the y shear factor 
    *
    */ 
   public void shear(double factorX, double factorY);

   /**
    * Shears this object relative to the given point.
    * @param p the shearing origin
    * @param factorX the x shear factor 
    * @param factorY the y shear factor 
    *
    */ 
   public void shear(Point2D p, double factorX, double factorY);

   /**
    * Shifts this object. Subclasses need to override this method.
    * @param x the x shift
    * @param y the y shift
    */ 
   public void translate(double x, double y);

  /**
   * Transforms this object.
   * @param matrix the affine transformation matrix (stored in 
   * flat format)
   */
   public void transform(double[] matrix);

   public void fade(double value);

   /**
    * Print the PGF commands for this object.
    */
 
   public void savePgf(TeX tex) throws IOException;

   /**
    * Saves this object in SVG format.
    * @param out the output stream 
    * @param cg the graphics information (typically it should just
    * be the storage unit that's needed)
    * @throws IOException if I/O error occurs 
    */ 
   public void saveSVG(SVG svg, String attr)
      throws IOException;

   /**
    * Saves this object in EPS format.
    * @param out the output stream
    * @param cg the graphics information (typically it should just
    * be the storage unit that's needed)
    * @throws IOException if I/O error occurs 
    */ 
   public void saveEPS(PrintWriter out)
      throws IOException;

   /**
    * Returns the PostScript level supported by this object. 
    */ 
   public int psLevel();

   /**
    * Draws this object in non-draft mode.
    * @param cg the graphics information 
    */ 
   public void draw(FlowFrame parentFrame);

   public void print(Graphics2D g2);

   /**
    *  Makes this object identical to the other object
    *  @param object the other object
    */ 
   public void makeEqual(JDRGraphicObject object);

   /**
    * Determines if this object is the same as another object.
    * @param o the other object
    * @return true if this object is equal to the other object
    */ 
   public boolean equals(Object o);

   public BBox getStorageBBox();
   public BBox getStorageControlBBox();

   public void mergeStorageControlBBox(BBox box);
   public void mergeStorageBBox(BBox box);

   public CanvasGraphics getCanvasGraphics();
   public void setCanvasGraphics(CanvasGraphics cg);

}
