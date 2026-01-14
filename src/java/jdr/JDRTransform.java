// File          : JDRTransform.java
// Creation Date : 1st February 2006
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

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Transformation class used by {@link JDRText}. This keeps track
 * of the original bounding box and the anchor (see 
 * <a href="#fig1">Figure 1</a>) in addition to the
 * transformation. The transformation is stored as an
 * AffineTransform.
 * <p>
 * <center>
 * <table width=60%>
 * <tr align=center><td colspan=2><a name="fig1"></a>
 * <img src="images/textanchor.png"
    alt="[image illustrating bounding box and anchor]"></td></tr>
 * <tr><th valign=top>Figure&nbsp;1:</th>
 * <td>Bounding box and anchor for a text area. The anchor is
 * at the left most edge of the bounding box along the baseline.
 * The baseline of the original bounding box is always considered 
 * to be at y=0.</td>
 * </table>
 * </center>
 */
public class JDRTransform implements Cloneable,Serializable
{
   /**
    * Creates new transformation with zero dimensioned bounding
    * box and identity transformation.
    */
   public JDRTransform(CanvasGraphics cg)
   {
      canvasGraphics = cg;
      originalBounds = new Rectangle2D.Double();
      affineTransform = new AffineTransform();
   }

   public JDRTransform()
   {
      originalBounds = new Rectangle2D.Double();
      affineTransform = new AffineTransform();
   }

   /**
    * Creates new transformation with zero dimensioned bounding
    * box and the given transformation.
    * @param matrix transformation matrix in flat form
    */
   public JDRTransform(CanvasGraphics cg, double[] matrix)
   {
      canvasGraphics = cg;
      originalBounds = new Rectangle2D.Double();
      affineTransform = new AffineTransform(matrix);
   }

   /**
    * Creates new transformation with given original bounding
    * box and identity transformation.
    * @param orgBounds the original bounding box
    */
   public JDRTransform(CanvasGraphics cg, Rectangle2D orgBounds)
   {
      canvasGraphics = cg;

      originalBounds = new Rectangle2D.Double(
           orgBounds.getX(), orgBounds.getY(),
           orgBounds.getWidth(), orgBounds.getHeight());

      affineTransform = new AffineTransform();
      transformChanged();
   }

   /**
    * Creates new transformation with given original bounding
    * box and identity transformation.
    * @param orgBounds the original bounding box
    */
   public JDRTransform(BBox orgBounds)
   {
      canvasGraphics = orgBounds.getCanvasGraphics();

      originalBounds = new Rectangle2D.Double(
           orgBounds.getMinX(), orgBounds.getMinY(),
           orgBounds.getWidth(), orgBounds.getHeight());

      affineTransform = new AffineTransform();
      transformChanged();
   }

   /**
    * Create a copy. 
    */ 
   public JDRTransform(JDRTransform trans)
   {
      canvasGraphics = trans.canvasGraphics;

      affineTransform = new AffineTransform(trans.affineTransform);

      originalBounds = new Rectangle2D.Double(
         trans.originalBounds.getX(),
         trans.originalBounds.getY(),
         trans.originalBounds.getWidth(),
         trans.originalBounds.getHeight());

      transformChanged();
   }

   /**
    * Resets the scaling and shear elements of the transformation
    * matrix to the identity matrix.
    * Only resets the first 4 elements of the flat
    * matrix used to store the transformation.
    */
   public void reset()
   {
      double tx = affineTransform.getTranslateX();
      double ty = affineTransform.getTranslateY();

      affineTransform.setToTranslation(tx, ty);
      transformChanged();
   }

   /**
    * Updates the original (untransformed) bounds.
    * @param orgBounds the new untransformed bounds
    */
   public void updateOriginalBounds(Rectangle2D orgBounds)
   {
      originalBounds.setRect(orgBounds);
      transformChanged();
   }

   /**
    * Updates the original (untransformed) bounds.
    * @param orgBounds the new untransformed bounds
    */
   public void updateOriginalBounds(BBox orgBounds)
   {
      originalBounds.setRect(orgBounds.getMinX(), orgBounds.getMinY(),
         orgBounds.getWidth(), orgBounds.getHeight());
      transformChanged();
   }

   /**
    * Sets the transformation matrix. (Elements are copied
    * from <code>matrix</code>.)
    * @param matrix the new transformation values
    */
   public void setTransformation(double[] matrix)
   {
      setTransformation(matrix[0], matrix[1], matrix[2], matrix[3],
       matrix[4], matrix[5]);
   }

   public void setTransformation(double m00, double m10, double m01, double m11,
      double m02, double m12)
   {
      affineTransform.setTransform(m00, m10, m01, m11, m02, m12);
      transformChanged();
   }

   @Deprecated
   public AffineTransform getAffineTransform()
   {
      return copyAffineTransform();
   }

   /**
    * Gets a copy of the affine transformation.
    * @return a new affine transform with the same matrix as this
    */
   public AffineTransform copyAffineTransform()
   {
      return new AffineTransform(affineTransform);
   }

   /**
    * Gets the transformation matrix. On exit, <code>matrix</code>
    * contains the transformation matrix.
    * @param matrix 6 element array used to store the flat matrix
    */
   public void getTransformation(double[] matrix)
   {
      affineTransform.getMatrix(matrix);
      transformChanged();
   }

   /**
    * Makes this transformation the same as another transformation.
    * @param t the other transformation
    */
   public void makeEqual(JDRTransform t)
   {
      affineTransform.setTransform(t.affineTransform);
      originalBounds.setRect(t.originalBounds);
      canvasGraphics = t.canvasGraphics;

      if (t.transformedBounds == null)
      {
         transformedBounds = null;
      }
      else
      {
         transformedBounds = new Rectangle2D.Double();
         transformedBounds.setRect(t.transformedBounds);
      }
   }

   /**
    * Gets the top left corner of the transformed bounding box.
    * @return top left corner
    */
   public JDRPoint getTopLeft()
   {
      if (transformedBounds == null)
      {
         transformChanged();
      }

      return new JDRPoint(canvasGraphics,
        transformedBounds.getX(), transformedBounds.getY());
   }

   /**
    * Gets the transformed anchor.
    * @return transformed anchor
    * @see #getOriginalAnchor()
    */
   public JDRPoint getAnchor()
   {
      Point2D p = new Point2D.Double(originalBounds.getX(), 0);

      return new JDRPoint(getCanvasGraphics(), affineTransform.transform(p, p));
   }

   /**
    * Gets original anchor.
    * @return original anchor
    * @see #getAnchor()
    */
   public JDRPoint getOriginalAnchor()
   {
      return new JDRPoint(getCanvasGraphics(), originalBounds.getX(), 0);
   }


   /**
    * Gets the top left corner of the original bounding box.
    * @return top left corner of original bounding box
    */
   public JDRPoint getOriginalTopLeft()
   {
      return new JDRPoint(getCanvasGraphics(), 
       originalBounds.getX(), originalBounds.getY());
   }

   /**
    * Gets the centre of the transformed bounding box.
    * @return centre of the transformed bounding box
    */
   public JDRPoint getCentre()
   {
      if (transformedBounds == null)
      {
         transformChanged();
      }

      return new JDRPoint(canvasGraphics,
         transformedBounds.getX()+0.5*transformedBounds.getWidth(),
         transformedBounds.getY()+0.5*transformedBounds.getHeight());
   }

   /**
    * Gets a copy of this transformation.
    * @return copy of this transformation
    */
   public Object clone()
   {
      JDRTransform transform = new JDRTransform();
      transform.makeEqual(this);

      return transform;
   }

   /**
    * Gets the original bounding box.
    * @return original bounding box
    */
   public BBox getOriginalBBox()
   {
      return new BBox(canvasGraphics, originalBounds);
   }

   /**
    * Gets the transformed bounding box.
    * @return transformed bounding box
    */
   public BBox getBBox()
   {
      if (transformedBounds == null)
      {
         transformChanged();
      }

      return new BBox(canvasGraphics, transformedBounds);
   }

   /**
    * Apply this transformation to the given object.
    * Calls {@link JDRObject#transform(AffineTransform)}.
    * @param object the object to transform
    */
   public void transform(JDRObject object)
   {
      object.transform(affineTransform);
      transformChanged();
   }

   /**
    * Concatenates this transformation with another transformation.
    * @param trans the transformation to apply
    * @see #concat(JDRTransform)
    */
   public void concat(AffineTransform trans)
   {
      affineTransform.concatenate(trans);
      transformChanged();
   }

   /**
    * Concatenates this transformation with another transformation.
    * @param trans the transformation to apply
    * @see #concat(AffineTransform)
    */
   public void concat(JDRTransform trans)
   {
      concat(trans.affineTransform);
   }

   /**
    * Concatenates this transformation with another transformation.
    * @param matrix the transformation matrix to apply
    * @see #concat(AffineTransform)
    * @see #concat(JDRTransform)
    */
   public void concat(double[] matrix)
   {
      concat(new AffineTransform(matrix));
   }

   /**
    * Preconcatenate this transformation with another transformation.
    * @param trans the other transformation
    */
   public void preConcatenate(AffineTransform trans)
   {
      affineTransform.preConcatenate(trans);
      transformChanged();
   }

   /**
    * Sets the position. This sets the last two elements of the
    * transformation matrix.
    * @param x the x position
    * @param y the y position
    */
   public void setPosition(double x, double y)
   {
      double tx = affineTransform.getTranslateX();
      double ty = affineTransform.getTranslateY();

      affineTransform.translate(x-tx, y-ty);
      transformChanged();
   }

   /**
    * Applies the given translation. This increments the last two
    * elements of the transformation by the specified amount.
    * @param x the x shift
    * @param y the y shift
    */
   public void translate(double x, double y)
   {
      double scaleX = affineTransform.getScaleX();
      double scaleY = affineTransform.getScaleY();
      double shearX = affineTransform.getShearX();
      double shearY = affineTransform.getShearY();
      double tx = affineTransform.getTranslateX();
      double ty = affineTransform.getTranslateY();

      affineTransform.setTransform(scaleX, shearY, shearX, scaleY, tx+x, ty+y);
      transformChanged();
   }

   /**
    * Applies a rotation about the centre of the transformed bounding
    * box by the given angle.
    * @param angle the angle of rotation
    */
   public void rotate(double angle)
   {
      JDRPoint p = getCentre();

      rotate(p.getPoint(), angle);
   }

   /**
    * Applies a rotation about the given point by the given angle.
    * @param p the point about which to rotate
    * @param angle the angle of rotation (radians)
    */
   public void rotate(Point2D p, double angle)
   {
      affineTransform.preConcatenate(
        AffineTransform.getRotateInstance(angle, p.getX(), p.getY()));
      transformChanged();
   }

   /**
    * Applies a scaling relative to the top left corner of the
    * transformed bounding box.
    * @param factorX horizontal scaling factor
    * @param factorY vertical scaling factor
    */
   public void scale(double factorX, double factorY)
   {
      if (transformedBounds == null)
      {
         transformChanged();
      }

      double x = transformedBounds.getMinX();
      double y = transformedBounds.getMinY();

      double scaleX = affineTransform.getScaleX() * factorX;
      double scaleY = affineTransform.getScaleY() * factorY;
      double shearX = affineTransform.getShearX() * factorX;
      double shearY = affineTransform.getShearY() * factorY;
      double tx = (affineTransform.getTranslateX() - x) * factorX + x;
      double ty = (affineTransform.getTranslateY() - y) * factorY + y;

      affineTransform.setTransform(scaleX, shearY, shearX, scaleY, tx, ty);
      transformChanged();
   }

   /**
    * Applies a scaling relative to the given point.
    * @param p the point about which to scale
    * @param factorX horizontal scaling factor
    * @param factorY vertical scaling factor
    */
   public void scale(Point2D p, double factorX, double factorY)
   {
      double x = p.getX();
      double y = p.getY();

      double scaleX = affineTransform.getScaleX() * factorX;
      double scaleY = affineTransform.getScaleY() * factorY;
      double shearX = affineTransform.getShearX() * factorX;
      double shearY = affineTransform.getShearY() * factorY;
      double tx = (affineTransform.getTranslateX() - x) * factorX + x;
      double ty = (affineTransform.getTranslateY() - y) * factorY + y;

      affineTransform.setTransform(scaleX, shearY, shearX, scaleY, tx, ty);
      transformChanged();
   }

   /**
    * Applies a shear relative to the bottom left corner of the
    * transformed bounding box.
    * @param factorX horizontal shear factor
    * @param factorY vertical shear factor
    */
   public void shear(double factorX, double factorY)
   {
      if (transformedBounds == null)
      {
         transformChanged();
      }

      double x = transformedBounds.getMinX();
      double y = transformedBounds.getMaxY();

      double scaleX = affineTransform.getScaleX();
      double scaleY = affineTransform.getScaleY();
      double shearX = affineTransform.getShearX();
      double shearY = affineTransform.getShearY();
      double tx = affineTransform.getTranslateX() - x;
      double ty = affineTransform.getTranslateY() - y;

      affineTransform.setTransform(
        scaleX - factorX * shearY,
        shearY - factorY * scaleX,
        shearX - factorX * scaleY,
        scaleY - factorY * shearX,
        tx - factorX * ty + x,
        ty - factorY * tx + y
      );

      transformChanged();
   }

   /**
    * Applies a shear relative to the given point.
    * @param p the point about which to shear
    * @param factorX horizontal shear factor
    * @param factorY vertical shear factor
    */
   public void shear(Point2D p, double factorX, double factorY)
   {
      double x = p.getX();
      double y = p.getY();
      double scaleX = affineTransform.getScaleX();
      double scaleY = affineTransform.getScaleY();
      double shearX = affineTransform.getShearX();
      double shearY = affineTransform.getShearY();
      double tx = affineTransform.getTranslateX() - x;
      double ty = affineTransform.getTranslateY() - y;

      affineTransform.setTransform(
        scaleX - factorX * shearY,
        shearY - factorY * scaleX,
        shearX - factorX * scaleY,
        scaleY - factorY * shearX,
        tx - factorX * ty + x,
        ty - factorY * tx + y
      );

      transformChanged();
   }

   /**
    * Writes this transformation in EPS format. This method writes the
    * lines:
<pre>
[m0 m1 m2 m3 m4 m5] concat
1 -1 scale
</pre>
    * where m0, ..., m5 are the elements of the transformation matrix.
    * @param out the output stream
    */
   public void saveEPS(PrintWriter out)
   {
      double[] flatmatrix = new double[6];
      affineTransform.getMatrix(flatmatrix);

      if (getCanvasGraphics().getStorageUnitID() != JDRUnit.BP)
      {
         double storageToBp = getCanvasGraphics().storageToBp(1.0);
         out.println(""+storageToBp+" "+storageToBp + " scale");

         out.println("["+flatmatrix[0]+" "+(flatmatrix[1])+" "
                        +flatmatrix[2]+" "+(flatmatrix[3])+" "
                        +flatmatrix[4]+" "+(flatmatrix[5])
                     +"] concat");

         out.println("1 -1 scale");


         double bpToStorage = getCanvasGraphics().bpToStorage(1.0);
         out.println(""+bpToStorage+" "+bpToStorage + " scale");
      }
      else
      {
         out.println("["+flatmatrix[0]+" "+(flatmatrix[1])+" "
                        +flatmatrix[2]+" "+(flatmatrix[3])+" "
                        +flatmatrix[4]+" "+(flatmatrix[5])
                     +"] concat");

         out.println("1 -1 scale");
      }
   }

   /**
    * Gets this transformation in SVG format.
    * @return this transformation in SVG format
    */
   public String svg(SVG svg)
   {
      return svg.transform(affineTransform);
   }

   /**
    * Gets this transformation as PGF commands.
    * An offset may be applied (this is used by
    * {@link JDRText#pgf(TeX)} according to the
    * horizontal and vertical alignment settings).
    * @param xOffset the x offset
    * @param yOffset the y offset
    * @param objectPGF the PGF commands to which this transformation
    * should apply
    */
   public void savePgf(TeX tex, double xOffset, double yOffset, String objectPGF)
     throws IOException
   {
      AffineTransform texAf = tex.getTransform();

      CanvasGraphics cg = getCanvasGraphics();

      tex.println("\\begin{pgfscope}");

      BBox box = getOriginalBBox();
      double y = box.getMaxY();

      AffineTransform concat = new AffineTransform();
      concat.concatenate(texAf);
      concat.concatenate(affineTransform);
      concat.concatenate(new AffineTransform(1, 0, 0, -1, xOffset, y-yOffset));

      tex.println(tex.transform(cg, concat));
      tex.println(objectPGF);

      tex.println("\\end{pgfscope}");
   }

   /**
    * Saves this transformation in JDR/AJR format.
    * @throws IOException if I/O error occurs
    */
   public void save(JDRAJR jdr)
      throws IOException
   {
      jdr.writeTransform(affineTransform);
   }

   /**
    * Reads transformation in JDR format.
    * @throws InvalidFormatException if the format is invalid
    */
   public static double[] read(JDRAJR jdr, String identifier)
      throws InvalidFormatException
   {
      return jdr.readTransform(identifier);
   }

   /**
    * Gets a string representation of this transformation.
    * @return string representation of this transformation
    */
   public String toString()
   {
      return String.format("%s[transform=%s,originalBounds=%s]",
        getClass().getSimpleName(), affineTransform, originalBounds);
   }

   public String info()
   {
     return String.format("[[%f,%f,%f][%f,%f,%f]]",
        affineTransform.getScaleX(),
        affineTransform.getShearX(),
        affineTransform.getTranslateX(),
        affineTransform.getScaleY(),
        affineTransform.getShearY(),
        affineTransform.getTranslateY()
      );
   }

   private void transformChanged()
   {
      if (originalBounds != null && canvasGraphics != null)
      {
         Shape shape = affineTransform.createTransformedShape(originalBounds);
         transformedBounds = shape.getBounds2D();
      }
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      canvasGraphics = cg;
      transformChanged();
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      JDRUnit oldUnit = canvasGraphics.getStorageUnit();
      JDRUnit newUnit = cg.getStorageUnit();

      if (oldUnit.getID() != newUnit.getID())
      {
         double factor = oldUnit.toUnit(1.0, newUnit);

         double scaleX = affineTransform.getScaleX();//m00
         double scaleY = affineTransform.getScaleY();//m11

         double shearX = affineTransform.getShearX();//m01
         double shearY = affineTransform.getShearY();//m10

         double x = affineTransform.getTranslateX() * factor;
         double y = affineTransform.getTranslateY() * factor;

         affineTransform.setTransform(scaleX, shearY,
           shearX, scaleY, x, y);

         originalBounds.setRect(
            originalBounds.getX() * factor,
            originalBounds.getY() * factor,
            originalBounds.getWidth() * factor,
            originalBounds.getHeight() * factor);
      }

      setCanvasGraphics(cg);
   }

   private Rectangle2D originalBounds, transformedBounds;
   private AffineTransform affineTransform;

   private CanvasGraphics canvasGraphics;
}
