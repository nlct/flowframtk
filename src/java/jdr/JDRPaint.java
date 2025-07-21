// File          : JDRPaint.java
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
import java.lang.Math;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.*;

import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.*;

/**
 * Abstract paint class for JDR objects.
 * @author Nicola L C Talbot
 */
public abstract class JDRPaint implements Serializable
{
   public JDRPaint()
   {
   }

   public JDRPaint(CanvasGraphics cg)
   {
      setCanvasGraphics(cg);
   }

   /**
    * Gets the closest matching <code>java.awt.Color</code>.
    * @return closest matching Color.
    */
   public abstract Color getColor();

   /**
    * Gets the <code>java.awt.Paint</code> equivalent.
    * @param box the bounding box of the object to which this
    * paint should be applied (required for gradient style paint)
    * @return the closest matching Paint
    */
   public abstract Paint getPaint(BBox box);

   public abstract JDRPaint average(JDRPaint paint);

   /**
    * Gets the closest matching {@link JDRColor}. Note that the
    * transformations between colour spaces is not exact.
    * @return the closest matching JDRColor.
    */
   public abstract JDRColor getJDRColor();

   /**
    * Gets the closest matching {@link JDRColorCMYK}. Note that the
    * transformations between colour spaces is not exact.
    * @return the closest matching JDRColorCMYK.
    */
   public abstract JDRColorCMYK getJDRColorCMYK();

   /**
    * Gets the closest matching {@link JDRColorHSB}. Note that the
    * transformations between colour spaces is not exact.
    * @return the closest matching JDRColorHSB.
    */
   public abstract JDRColorHSB getJDRColorHSB();

   /**
    * Gets the closest matching {@link JDRGray}. Note that the
    * transformations between colour spaces is not exact.
    * @return the closest matching JDRGray.
    */
   public abstract JDRGray getJDRGray();

   /**
    * Fades alpha channel.
    * @param value alpha value is scaled by
    */
   public void fade(double value)
   {
      double alpha = getAlpha();

      if (value < 0)
      {
         throw new JdrIllegalArgumentException
           (JdrIllegalArgumentException.FADE, value, getCanvasGraphics());
      }

      alpha *= value;

      if (alpha > 1.0)
      {
         alpha = 1.0;
      }

      setAlpha(alpha);
   }

   /**
    * Gets ID associated with this colour.
    * @return label uniquely identifying this colour
    */
   public abstract String getID();

   /**
    * Gets PGF commands identifying this colour.
    * @param box the bounding box of the object to which this
    * colour applies (maybe null)
    * @return string containing PGF colour command
    */
   public String pgf(BBox box)
   {
      return "\\color["+pgfmodel()+"]{"+pgfspecs()+"}";
   }

   public abstract String pgfmodel();

   public abstract String pgfspecs();

   /**
    * Gets PGF commands identifying this fill colour.
    * @param box the bounding box of the object to which this
    * colour applies (may be null)
    * @return string containing PGF colour command
    */
   public String pgffillcolor(BBox box)
   {
      String str = "";

      double alpha = getAlpha();

      if (alpha < 1.0)
      {
         str =  "\\pgfsetfillopacity{"+PGF.format(alpha)+"}";
      }

      str += "\\definecolor{fillpaint}{"+pgfmodel()+"}{" + pgfspecs() + "}"
           + "\\pgfsetfillcolor{fillpaint}";

      return str;
   }

   /**
    * Gets PGF commands identifying this stroke colour.
    * @param box the bounding box of the object to which this
    * colour applies (may be null)
    * @return string containing PGF colour command
    */
   public String pgfstrokecolor(BBox box)
   {
      String str = "";

      double alpha = getAlpha();

      if (alpha < 1.0)
      {
         str = "\\pgfsetstrokeopacity{"+PGF.format(alpha)+"}";
      }

      str += "\\definecolor{strokepaint}{"+pgfmodel()+"}{" + pgfspecs() + "}"
           + "\\pgfsetstrokecolor{strokepaint}";

      return str;
   }

   /**
    * Writes the required EPS commands to set this colour.
    * @param out the output stream
    * @param box the bounding box of the object to which this colour 
    * applies (may be null)
    * @throws IOException if I/O error occurs
    */
   public abstract void saveEPS(PrintWriter out, BBox box)
      throws IOException;

   /**
    * Returns the EPS level supported by this colour.
    * @return the PostScript level that supports this colour
    */
   public abstract int psLevel();

   /**
    * Gets this paint's transparency.
    * @return the transparency
    */
   public abstract double getAlpha();

   /**
    * Sets this paint's transparency
    */
   public abstract void setAlpha(double alpha);

   /**
    * Gets the SVG commands to identify this paint. This
    * doesn't include the opacity.
    * @return string containing SVG commands identifying this 
    * paint
    */
   public abstract String svg();

   /**
    * Gets the SVG commands to identify this fill paint.
    * This includes <code>fill</code> and <code>fill-opacity</code>
    * tags.
    * @return string containing SVG commands identifying this 
    * fill paint
    */
   public abstract String svgFill();

   /**
    * Gets the SVG commands to identify this line paint.
    * This includes <code>stroke</code> and <code>stroke-opacity</code>
    * tags.
    * @return string containing SVG commands identifying this 
    * line paint
    */
   public abstract String svgLine();

   /**
    * Gets the paint loader listener associated with this paint.
    */
   public abstract JDRPaintLoaderListener getListener();

   /**
    * Removes transparency. If alpha value is less than 0.5, returns
    * JDRTransparent otherwise the alpha value is set to 1.
    *  @return JDRTransparent or a copy of this with the alpha value
    *  set to 1.
    */ 
   public JDRPaint removeTransparency()
   {
      double alpha = getAlpha();

      if (alpha < 0.5)
      {
         return new JDRTransparent(getCanvasGraphics());
      }

      JDRPaint p = (JDRPaint)clone();

      p.setAlpha(1.0);

      return p;
   }

   /**
    * Gets a copy of this paint.
    * @return a copy of this paint
    */
   public abstract Object clone();

   public void makeEqual(JDRPaint paint)
   {
      setCanvasGraphics(paint.getCanvasGraphics());
   }

   /**
    * Gets string representation of this paint.
    * @return string representation of this paint
    */
   public abstract String toString();

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      canvasGraphics = cg;
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      setCanvasGraphics(cg);
   }

   public String getName()
   {
      String name = getClass().getName();

      if (canvasGraphics == null)
      {
         return name;
      }

      return canvasGraphics.getMessageWithFallback("class."+name, name);
   }

   public abstract String getPdfStrokeSpecs();

   public abstract String getPdfFillSpecs();

   private CanvasGraphics canvasGraphics;
}
