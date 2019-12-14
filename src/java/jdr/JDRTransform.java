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
 * transformation. The transformation is stored as a flatmatrix
 * [m0, m1, m2, m3, m4, m5] where the transformation of (x,y) to
 * (x',y') is given by:
 * <p>
 * <center>
      x' = m0*x + m2*y + m4;<br>
      y' = m1*x + m3*y + m5;
 * </center>
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
      this(new BBox(cg, 0,0,0,0));
   }

   public JDRTransform()
   {
      this(new BBox(null, 0,0,0,0));
   }

   /**
    * Creates new transformation with zero dimensioned bounding
    * box and the given transformation.
    * @param matrix transformation matrix in flat form
    */
   public JDRTransform(CanvasGraphics cg, double[] matrix)
   {
      this(new BBox(cg, 0,0,0,0));

      setTransformation(matrix);
   }

   /**
    * Creates new transformation with given original bounding
    * box and identity transformation.
    * @param orgBounds the original bounding box
    */
   public JDRTransform(CanvasGraphics cg, Rectangle2D orgBounds)
   {
      this(new BBox(cg, orgBounds));
   }

   /**
    * Creates new transformation with given original bounding
    * box and identity transformation.
    * @param orgBounds the original bounding box
    */
   public JDRTransform(BBox orgBounds)
   {
      setCanvasGraphics(orgBounds.getCanvasGraphics());

      originalBounds = new Rectangle2D.Double(
           orgBounds.getMinX(), orgBounds.getMinY(),
           orgBounds.getWidth(), orgBounds.getHeight());

      flatmatrix = new double[6];

      flatmatrix[4] = 0.0;
      flatmatrix[5] = 0.0;

      reset();
   }

   /**
    * Create a copy. 
    */ 
   public JDRTransform(JDRTransform trans)
   {
      canvasGraphics = trans.canvasGraphics;
      flatmatrix = new double[trans.flatmatrix.length];

      for (int i = 0; i < trans.flatmatrix.length; i++)
      {
         flatmatrix[i] = trans.flatmatrix[i];
      }

      originalBounds = new Rectangle2D.Double(
         trans.originalBounds.getX(),
         trans.originalBounds.getY(),
         trans.originalBounds.getWidth(),
         trans.originalBounds.getHeight());
   }

   /**
    * Resets the scaling and shear elements of the transformation
    * matrix to the identity matrix.
    * Only resets the first 4 elements of the flat
    * matrix used to store the transformation.
    */
   public void reset()
   {
      flatmatrix[0] = 1.0;
      flatmatrix[1] = 0.0;
      flatmatrix[2] = 0.0;
      flatmatrix[3] = 1.0;
   }

   /**
    * Updates the original (untransformed) bounds.
    * @param orgBounds the new untransformed bounds
    */
   public void updateOriginalBounds(Rectangle2D orgBounds)
   {
      originalBounds.setRect(orgBounds);
   }

   /**
    * Updates the original (untransformed) bounds.
    * @param orgBounds the new untransformed bounds
    */
   public void updateOriginalBounds(BBox orgBounds)
   {
      originalBounds.setRect(orgBounds.getMinX(), orgBounds.getMinY(),
         orgBounds.getWidth(), orgBounds.getHeight());
   }

   /**
    * Sets the transformation matrix. (Elements are copied
    * from <code>matrix</code>.)
    * @param matrix the new transformation values
    */
   public void setTransformation(double[] matrix)
   {
      for (int i = 0; i < 6; i++)
      {
         flatmatrix[i] = matrix[i];
      }
   }

   /**
    * Gets the affine transformation defined by this transformation's
    * matrix.
    * @return affine transform defined by this transformation's flat
    * matrix
    */
   public AffineTransform getAffineTransform()
   {
      return new AffineTransform(flatmatrix);
   }

   /**
    * Gets the transformation matrix. On exit, <code>matrix</code>
    * contains the transformation matrix.
    * @param matrix 6 element array used to store the flat matrix
    */
   public void getTransformation(double[] matrix)
   {
      for (int i = 0; i < 6; i++) matrix[i] = flatmatrix[i];
   }

   /**
    * Makes this transformation the same as another transformation.
    * @param t the other transformation
    */
   public void makeEqual(JDRTransform t)
   {
      double[] matrix = new double[6];
      t.getTransformation(matrix);
      for (int i = 0; i < 6; i++) flatmatrix[i] = matrix[i];
      updateOriginalBounds(t.getOriginalBBox());
      setCanvasGraphics(t.getCanvasGraphics());
   }

   /**
    * Gets the top left corner of the transformed bounding box.
    * @return top left corner
    */
   public JDRPoint getTopLeft()
   {
      BBox box = getBBox();
      return new JDRPoint(getCanvasGraphics(), box.getMinX(), box.getMinY());
   }

   /**
    * Gets the transformed anchor.
    * @return transformed anchor
    * @see #getOriginalAnchor()
    */
   public JDRPoint getAnchor()
   {
      double anchorX = originalBounds.getX();
      double anchorY = 0;

      double m0 = flatmatrix[0];
      double m1 = flatmatrix[1];
      double m2 = flatmatrix[2];
      double m3 = flatmatrix[3];
      double m4 = flatmatrix[4];
      double m5 = flatmatrix[5];

      double x = m0*anchorX+m2*anchorY+m4;
      double y = m1*anchorX+m3*anchorY+m5;

      return new JDRPoint(getCanvasGraphics(), x, y);
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
      BBox box = getBBox();
      return new JDRPoint(getCanvasGraphics(), box.getMinX()+0.5*box.getWidth(),
                           box.getMinY()+0.5*box.getHeight());
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
      return new BBox(getCanvasGraphics(), originalBounds);
   }

   /**
    * Gets the transformed bounding box.
    * @return transformed bounding box
    */
   public BBox getBBox()
   {
      AffineTransform af = new AffineTransform(flatmatrix);

      return new BBox(getCanvasGraphics(), 
        af.createTransformedShape(originalBounds).getBounds2D());
   }

   /**
    * Apply this transformation to the given object.
    * Calls {@link JDRObject#transform(double[])}.
    * @param object the object to transform
    */
   public void transform(JDRObject object)
   {
      object.transform(flatmatrix);
   }

   /**
    * Concatenates this transformation with another transformation.
    * @param trans the transformation to apply
    * @see #concat(JDRTransform)
    * @see #concat(double[])
    */
   public void concat(AffineTransform trans)
   {
      AffineTransform af = new AffineTransform(flatmatrix);
      af.concatenate(trans);
      af.getMatrix(flatmatrix);
   }

   /**
    * Concatenates this transformation with another transformation.
    * @param trans the transformation to apply
    * @see #concat(AffineTransform)
    * @see #concat(double[])
    */
   public void concat(JDRTransform trans)
   {
      concat(trans.flatmatrix);
   }

   /**
    * Concatenates this transformation with another transformation.
    * @param matrix the transformation matrix to apply
    * @see #concat(AffineTransform)
    * @see #concat(JDRTransform)
    */
   public void concat(double[] matrix)
   {
      double a0 = flatmatrix[0];
      double a1 = flatmatrix[1];
      double a2 = flatmatrix[2];
      double a3 = flatmatrix[3];
      double a4 = flatmatrix[4];
      double a5 = flatmatrix[5];

      flatmatrix[0] = matrix[0]*a0+matrix[2]*a1;
      flatmatrix[1] = matrix[1]*a0+matrix[3]*a1;
      flatmatrix[2] = matrix[0]*a2+matrix[2]*a3;
      flatmatrix[3] = matrix[1]*a2+matrix[3]*a3;
      flatmatrix[4] = matrix[0]*a4+matrix[2]*a5+matrix[4];
      flatmatrix[5] = matrix[1]*a4+matrix[3]*a5+matrix[5];
   }

   /**
    * Preconcatenate this transformation with another transformation.
    * @param trans the other transformation
    */
   public void preConcatenate(AffineTransform trans)
   {
      AffineTransform af = new AffineTransform(flatmatrix);
      af.preConcatenate(trans);
      af.getMatrix(flatmatrix);
   }

   /**
    * Sets the position. This sets the last two elements of the
    * transformation matrix.
    * @param x the value to set flatmatrix[4]
    * @param y the value to set flatmatrix[5]
    */
   public void setPosition(double x, double y)
   {
      flatmatrix[4] = x;
      flatmatrix[5] = y;
   }

   /**
    * Applies the given translation. This increments the last two
    * elements of the transformation by the specified amount.
    * @param x the amount to increment flatmatrix[4]
    * @param y the amount to increment flatmatrix[5]
    */
   public void translate(double x, double y)
   {
      flatmatrix[4] += x;
      flatmatrix[5] += y;
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
    * @param angle the angle of rotation
    */
   public void rotate(Point2D p, double angle)
   {
      translate(-p.getX(), -p.getY());

      double[] matrix = new double[6];
      double cosTheta = Math.cos(-angle);
      double sinTheta = Math.sin(-angle);

      matrix[0] = flatmatrix[0]*cosTheta+flatmatrix[1]*sinTheta;
      matrix[1] = flatmatrix[1]*cosTheta-flatmatrix[0]*sinTheta;
      matrix[2] = flatmatrix[2]*cosTheta+flatmatrix[3]*sinTheta;
      matrix[3] = flatmatrix[3]*cosTheta-flatmatrix[2]*sinTheta;
      matrix[4] = flatmatrix[4]*cosTheta+flatmatrix[5]*sinTheta;
      matrix[5] = flatmatrix[5]*cosTheta-flatmatrix[4]*sinTheta;

      setTransformation(matrix);

      translate(p.getX(), p.getY());
   }

   /**
    * Applies a scaling relative to the top left corner of the
    * transformed bounding box.
    * @param factorX horizontal scaling factor
    * @param factorY vertical scaling factor
    */
   public void scale(double factorX, double factorY)
   {
      BBox box = getBBox();
      Point2D p = new Point2D.Double(box.getMinX(),box.getMinY());
      scale(p, factorX, factorY);
   }

   /**
    * Applies a scaling relative to the given point.
    * @param p the point about which to scale
    * @param factorX horizontal scaling factor
    * @param factorY vertical scaling factor
    */
   public void scale(Point2D p, double factorX, double factorY)
   {
      translate(-p.getX(), -p.getY());

      flatmatrix[0] *= factorX;
      flatmatrix[1] *= factorY;
      flatmatrix[2] *= factorX;
      flatmatrix[3] *= factorY;
      flatmatrix[4] *= factorX;
      flatmatrix[5] *= factorY;

      translate(p.getX(), p.getY());
   }

   /**
    * Applies a shear relative to the bottom left corner of the
    * transformed bounding box.
    * @param factorX horizontal shear factor
    * @param factorY vertical shear factor
    */
   public void shear(double factorX, double factorY)
   {
      BBox box = getBBox();
      Point2D p = new Point2D.Double(box.getMinX(),box.getMaxY());
      shear(p, factorX, factorY);
   }

   /**
    * Applies a shear relative to the given point.
    * @param p the point about which to shear
    * @param factorX horizontal shear factor
    * @param factorY vertical shear factor
    */
   public void shear(Point2D p, double factorX, double factorY)
   {
      translate(-p.getX(), -p.getY());

      double m0 = flatmatrix[0];
      double m1 = flatmatrix[1];
      double m2 = flatmatrix[2];
      double m3 = flatmatrix[3];
      double m4 = flatmatrix[4];
      double m5 = flatmatrix[5];

      flatmatrix[0] = m0-factorX*m1;
      flatmatrix[1] = m1-factorY*m0;
      flatmatrix[2] = m2-factorX*m3;
      flatmatrix[3] = m3-factorY*m2;
      flatmatrix[4] = m4-factorX*m5;
      flatmatrix[5] = m5-factorY*m4;

      translate(p.getX(), p.getY());
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
      AffineTransform af = svg.getTransform();

      CanvasGraphics cg = getCanvasGraphics();

      BBox box = getOriginalBBox();
      double y = box.getMaxY();

      AffineTransform concat = new AffineTransform();
      concat.concatenate(af);
      concat.concatenate(getAffineTransform());
      concat.concatenate(new AffineTransform(1, 0, 0, -1, 0, y));

      return svg.transform(concat);
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
      AffineTransform af = tex.getTransform();

      CanvasGraphics cg = getCanvasGraphics();

      tex.println("\\begin{pgfscope}");

      BBox box = getOriginalBBox();
      double y = box.getMaxY();

      AffineTransform concat = new AffineTransform();
      concat.concatenate(af);
      concat.concatenate(getAffineTransform());
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
      jdr.writeTransform(flatmatrix);
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
      String str = "JDRTransform:";

      for (int i = 0; i < 6; i++) str+= " "+flatmatrix[i];

      return str;
   }

   public String info()
   {
      return "["
        + "["+flatmatrix[0]+","+flatmatrix[2]+","+flatmatrix[4]+"]"
        + "["+flatmatrix[1]+","+flatmatrix[3]+","+flatmatrix[5]+"]"
        + "]";
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      canvasGraphics = cg;
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

         flatmatrix[4] *= factor;
         flatmatrix[5] *= factor;
      }

      setCanvasGraphics(cg);
   }

   private volatile Rectangle2D originalBounds;
   private volatile double[] flatmatrix;

   private volatile CanvasGraphics canvasGraphics;
}
