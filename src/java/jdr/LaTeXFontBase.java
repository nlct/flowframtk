// File          : LaTeXFontBase.java
// Creation Date : 8th February 2006
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
package com.dickimawbooks.jdr;

import java.lang.Math;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class containing information required to determine LaTeX's 
 * relative font sizes. This deals with conversion
 * between points and LaTeX's relative font size changing
 * declarations (such as <code>\large</code>), as well as determining
 * the value of <code>\baselineskip</code>. The normal font size
 * is required to determine all the relative information.
 * Note that some classes may define <code>\Huge</code> to be the
 * same as <code>\huge</code>.
 */
public class LaTeXFontBase
{
   /**
    * Creates a new font base with 10pt normal size. If you use this
    * constructor, you must ensure that you use a document class that
    * sets <code>\normalsize</code> to use 10pt fonts.
    * @see #LaTeXFontBase(double)
    */
   public LaTeXFontBase(JDRMessageDictionary msgSys)
   {
      this(msgSys, 10);
   }

   /**
    * Creates a new font base with given normal size.
    * The normal size is the font size given by LaTeX's
    * <code>\normalsize</code>. This is set via class options such
    * as 12pt. 
    * You must make sure that you use a document class which sets
    * <code>\normalsize</code> to use fonts in the given value.
    * @param normalsize the normal font size in pt
    * @see #LaTeXFontBase()
    * @see #setNormalSize(double)
    */
   public LaTeXFontBase(JDRMessageDictionary msgSys, double normalsize)
   {
      this.messageSystem = msgSys;
      baselineskip = new double[NUM_SIZES];
      fontsize     = new double[NUM_SIZES];
      setNormalSize(normalsize);
   }

   /**
    * Sets the normal size for this font base. This rounds
    * <code>normalsize</code> to the nearest known LaTeX setting as
    * follows:
    * <ul>
    * <li> if <code>normalsize</code> &gt;= 24 assume 25pt. (This
    * setting is available with the a0poster class.)
    * <li> if 24 &gt; <code>normalsize</code> &gt;= 19 assume 20pt.
    * (This setting is available with the extsizes classes.)
    * <li> if 19 &gt; <code>normalsize</code> &gt;= 16 assume 17pt.
    * (This setting is available with the extsizes classes.)
    * <li> if 17 &gt; <code>normalsize</code> &gt;= 13 assume 14pt.
    * (This setting is available with the extsizes classes.)
    * <li> if <code>normalsize</code> = 12 assume 12pt.
    * (This setting is available with all standard classes.)
    * <li> if <code>normalsize</code> = 11 assume 11pt.
    * (This setting is available with all standard classes.)
    * <li> if <code>normalsize</code> = 10 assume 10pt.
    * (This setting is available with all standard classes.)
    * <li> if <code>normalsize</code> = 9 assume 9pt.
    * (This setting is available with the extsizes classes.)
    * <li> if <code>normalsize</code> &lt; 9 assume 8pt.
    * (This setting is available with the extsizes classes.)
    * </ul>
    * @param normalsize the new normal size setting (specified in pt)
    */
   public void setNormalSize(double normalsize)
   {
      if (normalsize < 1)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.SETTING_NORMALSIZE, normalsize,
           getMessageSystem());
      }

      if (normalsize >= 24)
      {
         //assume 25pt (a0poster)
         setFontSize(TINY, 12.0f, 14.0f);
         setFontSize(SCRIPTSIZE,14.4f,18.0f);
         setFontSize(FOOTNOTESIZE,17.28f,22.0f);
         setFontSize(SMALL,20.74f,25.0f);
         setFontSize(NORMALSIZE,24.88f,30.0f);
         setFontSize(LARGE,29.86f,37.0f);
         setFontSize(XLARGE,35.83f,45.0f);
         setFontSize(XXLARGE,43.0f,54.0f);
         setFontSize(HUGE,51.6f,64.0f);
         setFontSize(XHUGE,61.92f,77.0f);
         setFontSize(VERYHUGE,74.3f,93.0f);
         setFontSize(XVERYHUGE,89.16f,112.0f);
         setFontSize(XXVERYHUGE,107.0f,134.0f);
      }
      else if (normalsize >= 19)
      {
         // assume 20pt
         setFontSize(TINY, 10.0f, 11.0f);
         setFontSize(SCRIPTSIZE, 12.0f, 14.0f);
         setFontSize(FOOTNOTESIZE, 14.0f, 17.0f);
         setFontSize(SMALL, 17.0f, 22.0f);
         setFontSize(NORMALSIZE, 20.0f, 25.0f);
         setFontSize(LARGE, 25.0f, 30.0f);
         setFontSize(XLARGE, 29.86f, 35.0f);
         setFontSize(XXLARGE, 35.83f, 41.0f);
         setFontSize(HUGE, 42.99f, 52.0f);
         setFontSize(XHUGE, 51.59f, 63.0f);
         setFontSize(VERYHUGE, 51.59f, 63.0f);
         setFontSize(XVERYHUGE, 51.59f, 63.0f);
         setFontSize(XXVERYHUGE, 51.59f, 63.0f);
      }
      else if (normalsize >= 16)
      {
         // assume 17pt
         setFontSize(TINY, 8.0f, 9.0f);
         setFontSize(SCRIPTSIZE, 10.0f, 11.0f);
         setFontSize(FOOTNOTESIZE, 12.0f, 14.0f);
         setFontSize(SMALL, 14.0f, 17.0f);
         setFontSize(NORMALSIZE, 17.0f, 22.0f);
         setFontSize(LARGE, 20.0f, 25.0f);
         setFontSize(XLARGE, 25.0f, 30.0f);
         setFontSize(XXLARGE, 29.86f, 35.0f);
         setFontSize(HUGE, 35.83f, 41.0f);
         setFontSize(XHUGE, 42.99f, 52.0f);
         setFontSize(VERYHUGE, 42.99f, 52.0f);
         setFontSize(XVERYHUGE, 42.99f, 52.0f);
         setFontSize(XXVERYHUGE, 42.99f, 52.0f);
      }
      else if (normalsize >= 13)
      {
         // assume 14pt
         setFontSize(TINY, 6.0f, 7.0f);
         setFontSize(SCRIPTSIZE, 8.0f, 9.5f);
         setFontSize(FOOTNOTESIZE, 10.0f, 12.0f);
         setFontSize(SMALL, 12.0f, 14.0f);
         setFontSize(NORMALSIZE, 14.0f, 17.0f);
         setFontSize(LARGE, 17.0f, 22.0f);
         setFontSize(XLARGE, 20.0f, 25.0f);
         setFontSize(XXLARGE, 25.0f, 30.0f);
         setFontSize(HUGE, 29.86f, 35.0f);
         setFontSize(XHUGE, 35.83f, 40.0f);
         setFontSize(VERYHUGE, 35.83f, 40.0f);
         setFontSize(XVERYHUGE, 35.83f, 40.0f);
         setFontSize(XXVERYHUGE, 35.83f, 40.0f);
      }
      else if (normalsize >= 12)
      {
         setFontSize(TINY, 6.0f, 7.0f);
         setFontSize(SCRIPTSIZE, 8.0f, 9.5f);
         setFontSize(FOOTNOTESIZE, 10.0f, 12.0f);
         setFontSize(SMALL, 11.0f, 13.6f);
         setFontSize(NORMALSIZE, 12.0f, 14.5f);
         setFontSize(LARGE, 14.0f, 18.0f);
         setFontSize(XLARGE, 17.0f, 22.0f);
         setFontSize(XXLARGE, 20.0f, 25.0f);
         setFontSize(HUGE, 25.0f, 30.0f);
         setFontSize(XHUGE, 25.0f, 30.0f);
         setFontSize(VERYHUGE, 25.0f, 30.0f);
         setFontSize(XVERYHUGE, 25.0f, 30.0f);
         setFontSize(XXVERYHUGE, 25.0f, 30.0f);
      }
      else if (normalsize >= 11)
      {
         setFontSize(TINY, 6.0f, 7.0f);
         setFontSize(SCRIPTSIZE, 8.0f, 9.5f);
         setFontSize(FOOTNOTESIZE, 9.0f, 11.0f);
         setFontSize(SMALL, 10.0f, 12.0f);
         setFontSize(NORMALSIZE, 11.0f, 13.6f);
         setFontSize(LARGE, 12.0f, 14.0f);
         setFontSize(XLARGE, 14.0f, 18.0f);
         setFontSize(XXLARGE, 17.0f, 22.0f);
         setFontSize(HUGE, 20.0f, 25.0f);
         setFontSize(XHUGE, 25.0f, 30.0f);
         setFontSize(VERYHUGE, 25.0f, 30.0f);
         setFontSize(XVERYHUGE, 25.0f, 30.0f);
         setFontSize(XXVERYHUGE, 25.0f, 30.0f);
      }
      else if (normalsize >= 10)
      {
         setFontSize(TINY, 5.0f, 6.0f);
         setFontSize(SCRIPTSIZE, 7.0f, 8.0f);
         setFontSize(FOOTNOTESIZE, 8.0f, 9.5f);
         setFontSize(SMALL, 9.0f, 11.0f);
         setFontSize(NORMALSIZE, 10.0f, 12.0f);
         setFontSize(LARGE, 12.0f, 14.0f);
         setFontSize(XLARGE, 14.0f, 18.0f);
         setFontSize(XXLARGE, 17.0f, 22.0f);
         setFontSize(HUGE, 20.0f, 25.0f);
         setFontSize(XHUGE, 25.0f, 30.0f);
         setFontSize(VERYHUGE, 25.0f, 30.0f);
         setFontSize(XVERYHUGE, 25.0f, 30.0f);
         setFontSize(XXVERYHUGE, 25.0f, 30.0f);
      }
      else if (normalsize >= 9)
      {
         setFontSize(TINY, 5.0f, 6.0f);
         setFontSize(SCRIPTSIZE, 6.0f, 7.0f);
         setFontSize(FOOTNOTESIZE, 7.0f, 8.0f);
         setFontSize(SMALL, 8.0f, 9.0f);
         setFontSize(NORMALSIZE, 9.0f, 11.0f);
         setFontSize(LARGE, 10.0f, 12.0f);
         setFontSize(XLARGE, 11.0f, 13.0f);
         setFontSize(XXLARGE, 12.0f, 14.0f);
         setFontSize(HUGE, 14.0f, 18.0f);
         setFontSize(XHUGE, 17.0f, 22.0f);
         setFontSize(VERYHUGE, 17.0f, 22.0f);
         setFontSize(XVERYHUGE, 17.0f, 22.0f);
         setFontSize(XXVERYHUGE, 17.0f, 22.0f);
      }
      else
      {
         // assume 8pt
         setFontSize(TINY, 5.0f, 6.0f);
         setFontSize(SCRIPTSIZE, 5.0f, 6.0f);
         setFontSize(FOOTNOTESIZE, 6.0f, 7.0f);
         setFontSize(SMALL, 7.0f, 8.0f);
         setFontSize(NORMALSIZE, 8.0f, 9.5f);
         setFontSize(LARGE, 10.0f, 11.0f);
         setFontSize(XLARGE, 11.0f, 12.0f);
         setFontSize(XXLARGE, 12.0f, 14.0f);
         setFontSize(HUGE, 14.0f, 18.0f);
         setFontSize(XHUGE, 17.0f, 22.0f);
         setFontSize(VERYHUGE, 17.0f, 22.0f);
         setFontSize(XVERYHUGE, 17.0f, 22.0f);
         setFontSize(XXVERYHUGE, 17.0f, 22.0f);
      }
   }

   private void setFontSize(int index,double size,double skip)
   {
      fontsize[index]     = size;
      baselineskip[index] = skip;
   }

   /**
    * Gets the LaTeX relative size index.
    * @param fontHeight
    * @return closest matching LaTeX relative size index 
    * (e.g. {@link #NORMALSIZE}) or -1 if the given size is too far
    * from the nearest matching size declaration
    * @see #getLaTeXCmd(JDRLength)
    */
   public int getLaTeXSize(JDRLength fontHeight)
   {
      double size = fontHeight.getValue(JDRUnit.pt);

      // find the index of the fontsize closest to size
      int min_idx=0;
      double min = Math.abs(fontsize[0]-size);

      for (int i = 1; i < NUM_SIZES; i++)
      {
         double distance = Math.abs(fontsize[i]-size);
         if (distance < min)
         {
            min = distance;
            min_idx = i;
         }
      }

      if (min > max_deviation) return -1;

      return min_idx;
   }

   /**
    * Gets the LaTeX declaration for the closest matching font size.
    * If {@link #getLaTeXSize(double)} returns -1, this method
    * returns the size in terms of <code>\fontsize</code>, but 
    * remember that non-standard font sizes will need to use 
    * scalable fonts (such as PostScript fonts). Note that if the
    * normal size &gt;= 24pt, the declarations <code>\veryHuge</code>,
    * <code>\VeryHuge</code> or <code>\VERYHuge</code> may be 
    * returned. These commands are defined by the a0poster class, but
    * are in general not defined in other classes.
    * @param bpsize font size in terms of PostScript points
    * @return string containing closest matching LaTeX font size
    * declaration
    * @see #getLaTeXSize(double)
    */
   public String getLaTeXCmd(JDRLength fontHeight)
   {
      // find the LaTeX declaration for the fontsize closest to size

      int idx = getLaTeXSize(fontHeight);

      double normalsize = getNormalSize();

      switch (idx)
      {
         case TINY : return "\\tiny";
         case SCRIPTSIZE : return "\\scriptsize";
         case FOOTNOTESIZE : return "\\footnotesize";
         case SMALL : return "\\small";
         case NORMALSIZE : return "\\normalsize";
         case LARGE : return "\\large";
         case XLARGE : return "\\Large";
         case XXLARGE : return "\\LARGE";
         case HUGE : return "\\huge";
         case XHUGE : return "\\Huge";
         case VERYHUGE :
            if (normalsize >= 24) return "\\veryHuge";
         case XVERYHUGE :
            if (normalsize >= 24) return "\\VeryHuge";
         case XXVERYHUGE :
            if (normalsize >= 24) return "\\VERYHuge";
      }

      String texHeight = PGF.length(fontHeight);

      return "\\fontsize{"+texHeight+"}{" +texHeight+"}\\selectfont";
   }

   /**
    * Gets the value of <code>\baselineskip</code> for the given
    * font size index.
    * @param i the font size index (for example {@link #NORMALSIZE})
    * @return the value of <code>\baselineskip</code> in TeX points
    */
   public double getBaselineskip(int i)
   {
      return baselineskip[i];
   }

   /**
    * Gets the font size for the given index.
    * @param i the font size index (for example {@link #NORMALSIZE})
    * @return the font size in TeX points
    * @see #getNormalSize()
    */
   public double getFontSize(int i)
   {
      return fontsize[i];
   }

   public double getFontSize(JDRUnit unit, int i)
   {
      return unit.fromPt(getFontSize(i));
   }

   /**
    * Gets the normal font size. This is equivalent to
    * <code>getFontSize(LaTeXFontBase.NORMALSIZE)</code>.
    * @return the normal font size int TeX points
    * @see #getFontSize(int)
    */
   public double getNormalSize()
   {
      return getFontSize(NORMALSIZE);
   }

   public double getNormalSize(JDRUnit unit)
   {
      return unit.fromPt(getNormalSize());
   }

   /**
    * Determines if this object is the same as another object.
    * @param obj the object with which to compare this object
    * @return true if this object is the same as the other object
    */
   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (!(obj instanceof LaTeXFontBase)) return false;

      LaTeXFontBase fb = (LaTeXFontBase)obj;

      for (int i = 0; i < NUM_SIZES; i++)
      {
         if (baselineskip[i] != fb.baselineskip[i]) return false;
         if (fontsize[i] != fb.fontsize[i]) return false;
      }

      return true;
   }

   public String toString()
   {
      String str = "LaTeXFontBase[normalsize=";

      str += getNormalSize();

      str += ",maxDeviation="+max_deviation+"]";

      return str;
   }

   public Object clone()
   {
      return new LaTeXFontBase(getMessageSystem(), getNormalSize());
   }

   public void makeEqual(LaTeXFontBase latexFonts)
   {
      for (int i = 0; i < NUM_SIZES; i++)
      {
         baselineskip[i] = latexFonts.baselineskip[i];
         fontsize[i] = latexFonts.fontsize[i];
      }
   }

   public JDRMessageDictionary getMessageSystem()
   {
      return messageSystem;
   }

   public void setMessageSystem(JDRMessageDictionary msgSys)
   {
      if (msgSys == null)
      {
         throw new NullPointerException();
      }

      this.messageSystem = msgSys;
   }

   private double[] baselineskip;
   private double[] fontsize;

   private JDRMessageDictionary messageSystem;

   // the maximum deviation from the lowest or highest size command
   private static double max_deviation=5.0;

   /**
    * Font size index equivalent to <code>\tiny</code>.
    */
   public static final int TINY=0;
   /**
    * Font size index equivalent to <code>\scriptsize</code>.
    */
   public static final int SCRIPTSIZE=1;
   /**
    * Font size index equivalent to <code>\footnotesize</code>.
    */
   public static final int FOOTNOTESIZE=2;
   /**
    * Font size index equivalent to <code>\small</code>.
    */
   public static final int SMALL=3;
   /**
    * Font size index equivalent to <code>\normalsize</code>.
    */
   public static final int NORMALSIZE=4;
   /**
    * Font size index equivalent to <code>\large</code>.
    */
   public static final int LARGE=5;
   /**
    * Font size index equivalent to <code>\Large</code>.
    */
   public static final int XLARGE=6;
   /**
    * Font size index equivalent to <code>\LARGE</code>.
    */
   public static final int XXLARGE=7;
   /**
    * Font size index equivalent to <code>\huge</code>.
    */
   public static final int HUGE=8;
   /**
    * Font size index equivalent to <code>\Huge</code>.
    */
   public static final int XHUGE=9;
   /**
    * Font size index equivalent to <code>\veryHuge</code>.
      (Defined in a0poster class.)
    */
   public static final int VERYHUGE=10;
   /**
    * Font size index equivalent to <code>\VeryHuge</code>.
      (Defined in a0poster class.)
    */
   public static final int XVERYHUGE=11;
   /**
    * Font size index equivalent to <code>\VERYHuge</code>.
      (Defined in a0poster class.)
    */
   public static final int XXVERYHUGE=12;
   /**
    * Total number of font size indices.
    */
   public static final int NUM_SIZES=13;
}
