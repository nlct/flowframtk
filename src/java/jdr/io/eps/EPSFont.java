// File          : EPSFont.java
// Purpose       : class representing an EPS font
// Date          : 1st February 2006
// Last Modified : 28 July 2007
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

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
package com.dickimawbooks.jdr.io.eps;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.font.*;
import java.util.*;
import java.util.regex.*;
import java.text.DateFormat;
import java.math.*;
import javax.imageio.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing an EPS font.
 * @author Nicola L C Talbot
 */
public class EPSFont implements EPSDictionaryInterface
{
   /**
    * Initialises font information using the given PostScript
    * font name.
    * This attempts to find the closest matching font to the
    * given PostScript font, but there are no guarantees that
    * it will produce a good match.
    * @param psName the PostScript font name
    * @param fontBase the LaTeX font base
    */
   public EPSFont(EPS eps, String psName)
   {
      init(eps, psName, new AffineTransform());
   }

   /**
    * Initialises font information using the given PostScript
    * font name and the given transformation.
    * This attempts to find the closest matching font to the
    * given PostScript font, but there are no guarantees that
    * it will produce a good match.
    * @param eps
    * @param psName the PostScript font name
    * @param trans the transformation to apply to the font
    */
   public EPSFont(EPS eps, String psName,
       AffineTransform trans)
   {
      init(eps, psName, trans);
   }

   private EPSFont()
   {
      dict = new Hashtable<String,EPSObject>();
   }


   private void init(EPS eps, String psName,
      AffineTransform trans)
   {
      this.eps = eps;
      postscriptName = psName;
      latexFontBase = eps.getLaTeXFontBase();
      series = JDRFont.SERIES_MEDIUM;
      shape  = JDRFont.SHAPE_UPRIGHT;
      family = new String(psName);
      String string = psName.toLowerCase();

      if (string.matches(".*bold.*"))
      {
         series = JDRFont.SERIES_BOLD;
      }

      if (string.matches(".*italic.*"))
      {
         shape = JDRFont.SHAPE_ITALIC;
      }
      else if (string.matches(".*oblique.*"))
      {
         shape = JDRFont.SHAPE_SLANTED;
      }

      family = family.replaceAll("([oO]blique)|([bB]old)|([iI]talic)","");
      family = family.replaceFirst("[\\.]", " ");
      family = family.replaceFirst("^/", "");
      family = family.replaceFirst(" *$", "");

      if (family.startsWith("Times"))
      {
         family = "Serif";
      }
      else if (family.startsWith("Helvetica"))
      {
         family = "SansSerif";
      }
      else if (family.startsWith("Courier"))
      {
         family = "Monospaced";
      }

      double[] array = new double[6];
      trans.getMatrix(array);

      array[0] *= 0.001;
      array[3] *= 0.001;

      transformationMatrix = new EPSArray(array);

      dict = new Hashtable<String,EPSObject>();

      dict.put("FontMatrix", transformationMatrix);

      // assume all fonts are type 1

      dict.put("FontType", new EPSInteger(1));
   }

   /**
    * Scales this font by the given scaling factor.
    * @param factor the scaling factor
    */
   public void scaleFont(double factor)
   {
      scale(factor, factor);
   }

   /**
    * Sets this font's transformation matrix.
    * @param matrix new transformation matrix
    */
   public void setTransform(double[] matrix)
   {
      for (int i = 0; i < 6; i++)
      {
         try
         {
            ((EPSNumber)transformationMatrix.get(i)).set(matrix[i]);
         }
         catch (NoReadAccessException e)
         {
         }
      }
   }

   public void setTransform(AffineTransform af)
   {
      double[] matrix = new double[6];
      af.getMatrix(matrix);

      setTransform(matrix);
   }

   public AffineTransform getAffineTransform()
   {
      try
      {
         return new AffineTransform(transformationMatrix.getMatrix());
      }
      catch (NotMatrixException e)
      {
      }

      // this shouldn't happen
      return new AffineTransform();
   }

   /**
    * Concatenates given matrix with the font's current transformation
    * matrix.
    * @param matrix the matrix with which to concatenate this
    * font's transformation matrix
    */
   public void transform(double[] matrix)
   {
      AffineTransform af = getAffineTransform();

      af.concatenate(new AffineTransform(matrix));

      setTransform(af);
   }

   /**
    * Translates this font's transformation matrix by the given
    * x and y displacement.
    * @param x the x displacement
    * @param y the y displacement
    */
   public void translate(double x, double y)
   {
      AffineTransform af = getAffineTransform();

      af.translate(x, y);

      setTransform(af);
   }

   /**
    * Scales this font's transformation matrix by the given 
    * scaling factors.
    * @param sx the x scale factor
    * @param sy the y scale factor
    */
   public void scale(double sx, double sy)
   {
      AffineTransform af = getAffineTransform();

      af.scale(sx, sy);

      setTransform(af);
   }

   /**
    * Rotates this font's transformation matrix by the given
    * angle.
    * @param angle the angle of rotation (in Radians)
    */
   public void rotate(double angle)
   {
      AffineTransform af = getAffineTransform();

      af.rotate(angle);

      setTransform(af);
   }

   /**
    * Concatenates this font's transformation matrix with the
    * given transform.
    * @param trans the transform with which to concatenate this
    * font's transformation matrix
    */
   public void concatenate(AffineTransform trans)
   {
      AffineTransform af = getAffineTransform();

      af.concatenate(trans);

      setTransform(af);
   }

   /**
    * Returns a copy of this font.
    * @return copy of this font
    */
   public Object clone()
   {
      EPSFont font = new EPSFont();

      font.makeEqual(this);

      return font;
   }

   public void makeEqual(EPSObject object)
   {
      EPSFont font = (EPSFont)object;
      postscriptName = font.postscriptName;
      latexFontBase = font.latexFontBase;
      family = font.family;
      series = font.series;
      shape = font.shape;
      eps = font.eps;

      for (Enumeration<String> en=font.dict.keys();
           en.hasMoreElements();)
      {
         String key = en.nextElement();

         EPSObject value = (EPSObject)font.dict.get(key).clone();

         dict.put(key, value);

         if (key.equals("FontMatrix"))
         {
            transformationMatrix = (EPSArray)value;
         }
      }
   }

   /**
    * Gets this font as a <code>java.awt.Font</code> object.
    * @return this font as a <code>java.awt.Font</code> object
    */
   public Font getFont()
   {
      int weight = 0;
      weight += (series == JDRFont.SERIES_MEDIUM?
                 Font.PLAIN : Font.BOLD);
      weight += (shape == JDRFont.SHAPE_UPRIGHT?
                 0 : Font.ITALIC);

      Font font = new Font(family, weight, 1000);

      AffineTransform af = getAffineTransform();

      return font.deriveFont(af);
   }

   /**
    * Creates JDRText from this font.
    * @param cg canvas graphics info
    * @param text the contents of the text area
    * @param ctm current transformation matrix
    * @param currentPoint the current point (untransformed)
    * @param displacement the text area displacement is placed in
    * here on return if not null
    * @return text area
    */
   public JDRText getJDRText(CanvasGraphics cg, String text,
      AffineTransform ctm, Point2D currentPoint,
      Point2D displacement)
   {
      Graphics2D g = cg.getGraphics();

      JDRText textarea = null;

      AffineTransform af = getAffineTransform();

      af.preConcatenate(new AffineTransform(1000,0,0,1000,0,0));

      JDRLength size = new JDRLength(cg, 
         Math.round(0.5*(Math.abs(af.getScaleX()) + Math.abs(af.getScaleY()))), 
         JDRUnit.bp);

      if (size.getValue() == 0)
      {
         size.setValue(Math.round(latexFontBase.getNormalSize()));
      }

      textarea = new JDRText(cg, family, series, shape, 
         size, text);

      LaTeXFont latexFont = LaTeXFont.fromPostScript(postscriptName);

      textarea.setLaTeXFamily(latexFont.getFamily());
      textarea.setLaTeXSeries(latexFont.getWeight());
      textarea.setLaTeXShape(latexFont.getShape());
      textarea.setLaTeXSize(latexFontBase.getLaTeXCmd(size));

      JDRPoint start = textarea.getStart();

      GlyphVector gv = textarea.getFont().createGlyphVector(
         g.getFontRenderContext(), textarea.getText());

      Rectangle2D bounds = gv.getLogicalBounds();

      Point2D dp = new Point2D.Double(
         bounds.getWidth(), 0);

      AffineTransform af2 = new AffineTransform();

      double bpSize = size.getValue(JDRUnit.bp);

      af2.concatenate(ctm);
      af2.translate(currentPoint.getX(), currentPoint.getY());
      af2.concatenate(af);
      af2.scale(1.0/bpSize, -1.0/bpSize);

      textarea.transform(af2);

      eps.setLaTeXText(textarea);

      if (displacement != null)
      {
         af2.deltaTransform(dp, displacement);
      }

      return textarea;
   }

   public void copy(EPSObject object)
      throws InvalidEPSObjectException,
             NoWriteAccessException
   {
      if (!(object instanceof EPSFont))
      {
         throw new InvalidEPSObjectException("(copy) not a font");
      }

      EPSFont font = (EPSFont)object;

      series = font.series;
      shape  = font.shape;
      transformationMatrix     = font.transformationMatrix;
      family = font.family;
      dict = font.dict;
   }

   public int length()
   {
      return dict.size();
   }

   public EPSObject get(EPSObject index)
   {
      return get(index.toString());
   }

   public EPSObject get(String index)
   {
      String key = index.toString();

      if (key.startsWith("/"))
      {
         key = key.substring(1);
      }

      return dict.get(key);
   }

   public void put(EPSObject index, EPSObject value)
   {
      put(index.toString(), value);
   }

   public void put(String index, EPSObject value)
   {
      String key = index;

      if (key.startsWith("/"))
      {
         key = key.substring(1);
      }

      if (key.equals("FontMatrix"))
      {
         transformationMatrix = (EPSArray)value;
      }

      dict.put(key, value);
   }

   public void forall(EPSStack stack, EPSProc proc)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      for (Enumeration<String> en=dict.keys(); en.hasMoreElements();)
      {
         String key = en.nextElement();
         stack.add(new EPSName("/"+key));
         stack.add(dict.get(key));
         stack.execObject(proc);

         if (stack.getExitStatus()) break;
      }
   }

   public EPSName pstype()
   {
      return new EPSName("fonttype");
   }

   public String toString()
   {
      return "EPSFont[family="+family+",series="+series+",shape="
         +shape+",af="+transformationMatrix+",normalsize="
         +latexFontBase.getNormalSize()+",psfont="+postscriptName
         +"]";
   }

   private int series, shape;
   protected String family;
   private String postscriptName;

   private EPS eps;
   private EPSArray transformationMatrix;

   protected LaTeXFontBase latexFontBase;

   private Hashtable<String,EPSObject> dict
      = new Hashtable<String,EPSObject>();
}
