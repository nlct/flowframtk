// File          : JDRCompoundShape.java
// Creation Date : 6th April 2011
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

import com.dickimawbooks.jdr.exceptions.*;

/**
 * An object that has an underlying shape combined with either
 * another object or transformation parameters.
 */

public abstract class JDRCompoundShape extends JDRShape
{
   public JDRCompoundShape(CanvasGraphics cg)
   {
      super(cg);
   }

   public JDRCompoundShape(JDRCompoundShape shape)
   {
      super(shape);
   }

   public abstract JDRShape getUnderlyingShape();

   public abstract void setUnderlyingShape(JDRShape shape);

   @Override
   public JDRPath getBaseUnderlyingPath()
   {
      JDRShape shape = getUnderlyingShape();

      if (shape instanceof JDRPath)
      {
         return (JDRPath)shape;
      }
      else
      {
         return shape.getBaseUnderlyingPath();
      }
   }

   public boolean isDistortable()
   {
      return getUnderlyingShape().isDistortable();
   }

   public boolean isPolygon()
   {
      return getUnderlyingShape().isPolygon();
   }

   public BBox getStorageDistortionBounds()
   {
      return getUnderlyingShape().getStorageDistortionBounds();
   }

   public void close(int closeType)
      throws InvalidPathException
   {
      getUnderlyingShape().close(closeType);
   }

   public boolean hasShape()
   {
      if (this instanceof JDRShape)
      {
         return true;
      }

      return getUnderlyingShape().hasShape();
   }

   public int getObjectFlag()
   {
      return super.getObjectFlag()
           | SELECT_FLAG_COMPOUND
           | (getUnderlyingShape().getObjectFlag()
              & ~SELECT_FLAG_PATH);
   }

   public boolean hasPattern()
   {
      if (this instanceof JDRPattern)
      {
         return true;
      }

      return getUnderlyingShape().hasPattern();
   }

   public JDRPattern getPattern()
   {
      if (this instanceof JDRPattern)
      {
         return (JDRPattern)this;
      }

      return getUnderlyingShape().getPattern();
   }

   /**
    * Gets non-symmetric version of this shape or this shape if no
    * symmetry.
    */
   public JDRShape removeSymmetry()
   {
      if (hasSymmetricPath())
      {
         if (this instanceof JDRSymmetricPath)
         {
            return getUnderlyingShape();
         }

         // If this isn't a symmetric path, then the underlying
         // shape is a compound object that has symmetry

         JDRCompoundShape shape = (JDRCompoundShape)clone();

         shape.setUnderlyingShape(
           ((JDRCompoundShape)getUnderlyingShape()).removeSymmetry());

         return shape;
      }
      else
      {
         return this;
      }
   }

   /**
    * Split this object into its constituent components.
    * @return the group containing the components of this object
    */
   public abstract JDRGroup separate()
     throws InvalidShapeException;

   /**
    * Gets the total number of segments defining this shape.
    * Includes segments such as the line of symmetry.
    */
   public abstract int getTotalPathSegments();

   /**
    * Shear the parameters associated with this compound shape. For
    * example, anchor control or line of symmetry.
    */
   public abstract void shearParams(
      Point2D p, double factorX, double factorY);

   public void shear(Point2D p, double factorX, double factorY)
   {
      getUnderlyingShape().shear(p, factorX, factorY);
      shearParams(p, factorX, factorY);
   }

   /**
    * Scale the parameters associated with this compound shape. For
    * example, anchor control or line of symmetry.
    */
   public abstract void scaleParams(Point2D p, double factorX, double factorY);

   public void scale(Point2D p,
      double factorX, double factorY)
   {
      getUnderlyingShape().scale(p, factorX, factorY);
      scaleParams(p, factorX, factorY);
   }

   /**
    * Rotate the parameters associated with this compound shape. For
    * example, anchor control or line of symmetry.
    */
   public abstract void rotateParams(Point2D p, double angle);

   public void rotate(Point2D p, double angle)
   {
      getUnderlyingShape().rotate(p, angle);
      rotateParams(p, angle);
   }

   /**
    * Translate the parameters associated with this compound shape. For
    * example, anchor control or line of symmetry.
    */
   public abstract void translateParams(double shiftX, double shiftY);

   public void translate(double shiftX, double shiftY)
   {
      getUnderlyingShape().translate(shiftX, shiftY);
      translateParams(shiftX, shiftY);
   }

   public abstract void transformParams(double[] matrix);

   public void transform(double[] matrix)
   {
      getUnderlyingShape().transform(matrix);
      transformParams(matrix);
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      super.setCanvasGraphics(cg);

      JDRShape shape = getUnderlyingShape();

      if (shape != null)
      {
         shape.setCanvasGraphics(cg);
      }
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      getUnderlyingShape().applyCanvasGraphics(cg);
      super.applyCanvasGraphics(cg);
   }

   public int getSelectedSegmentFlag()
   {
      return getUnderlyingShape().getSelectedSegmentFlag();
   }

   public abstract JDRCompleteObject getFullObject()
     throws InvalidShapeException;

   public abstract JDRGroup splitText() throws InvalidShapeException;
}
