// File          : JDRTextual.java
// Creation Date : 8th July 2009
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

import com.dickimawbooks.jdr.exceptions.*;

public interface JDRTextual
{
   /**
    * Sets the horizontal alignment.
    * @param align the horizontal alignment
    */
   public void setHAlign(int align);

   /**
    * Sets the vertical alignment.
    * @param align the vertical alignment
    */
   public void setVAlign(int align);

   /**
    * Sets the horizontal and vertical alignment.
    * @param halign the horizontal alignment
    * @param valign the vertical alignment
    */
   public void setAlign(int halign, int valign);

   /**
    * Gets the horizontal alignment.
    * @return the horizontal alignment
    */
   public int getHAlign();

   /**
    * Gets the vertical alignment.
    * @return the vertical alignment
    */
   public int getVAlign();

   /**
    * Sets the text and LaTeX alternative text.
    * @param text the text
    * @param latexString the LaTeX alternative text
    */
   public void setText(String text, String latexString);

   /**
    * Sets the text.
    * @param text the text
    */
   public void setText(String text);

   /**
    * Sets the LaTeX alternative text.
    * @param latexText the LaTeX alternative text
    */
   public void setLaTeXText(String latexText);

   /**
    * Gets the text.
    * @return the text
    */
   public String getText();

   /**
    * Gets the LaTeX alternative text.
    * @return the LaTeX alternative text
    */
   public String getLaTeXText();

   /**
    * Sets the font.
    * @param name the Java font family name
    * @param series the font series, which must be one 
    * of: {@link JDRFont#SERIES_MEDIUM} or {@link JDRFont#SERIES_BOLD}
    * @param shape the font shape, which must be one
    * of:  {@link JDRFont#SHAPE_UPRIGHT}, {@link JDRFont#SHAPE_EM},
    * {@link JDRFont#SHAPE_ITALIC}, {@link JDRFont#SHAPE_SLANTED} or
    * {@link JDRFont#SHAPE_SC}
    * @param size the font size which can't
    * be negative
    */
   public void setFont(String name, int series,
      int shape, JDRLength size);

   /**
    * Sets the font family.
    * @param name the Java font family name
    */
   public void setFontFamily(String name);

   /**
    * Sets the font series.
    * @param series the font series, which must be one 
    * of: {@link JDRFont#SERIES_MEDIUM} or {@link JDRFont#SERIES_BOLD}
    */
   public void setFontSeries(int series);

   /**
    * Sets the font shape.
    * @param g graphics device
    * @param shape the font shape, which must be one
    * of:  {@link JDRFont#SHAPE_UPRIGHT}, {@link JDRFont#SHAPE_EM},
    * {@link JDRFont#SHAPE_ITALIC}, {@link JDRFont#SHAPE_SLANTED} or
    * {@link JDRFont#SHAPE_SC}
    */
   public void setFontShape(int shape);

   /**
    * Sets the font size.
    * @param size the font size
    */
   public void setFontSize(JDRLength size);

   /**
    * Gets the name of the Java font family.
    * @return font family name
    */
   public String getFontFamily();

   /**
    * Gets the font series.
    * @return font series
    */
   public int getFontSeries();

   /**
    * Gets the font shape.
    * @return the font shape
    */
   public int getFontShape();

   /**
    * Gets the font size.
    * @return the font size
    */
   public JDRLength getFontSize();

   /**
    * Gets the Java font used to display this text.
    * @return font
    */
   public Font getFont();

   /**
    * Gets the JDR font associated with this text.
    * @return font
    */
   public JDRFont getJDRFont();

   /**
    * Sets the LaTeX font family declaration.
    * @param family LaTeX font family declaration
    * @see LaTeXFont#setFamily(String)
    */
   public void setLaTeXFamily(String family);

   /**
    * Sets the LaTeX font size declaration.
    * @param size LaTeX font size declaration
    * @see LaTeXFont#setSize(String)
    */
   public void setLaTeXSize(String size);

   /**
    * Sets the LaTeX font series declaration.
    * @param series LaTeX font series declaration
    */
   public void setLaTeXSeries(String series);

   /**
    * Sets the LaTeX font shape declaration.
    * @param shape LaTeX font shape declaration
    */
   public void setLaTeXShape(String shape);

   /**
    * Sets the LaTeX font declarations for this text area.
    * @param family LaTeX font family declaration
    * @param shape LaTeX font shape declaration
    * @param size LaTeX font size declaration
    * @param series LaTeX font series declaration
    * @see #setLaTeXFont(LaTeXFont)
    */
   public void setLaTeXFont(String family, String size,
      String series, String shape);

   public String getLaTeXFamily();

   public String getLaTeXSeries();

   public String getLaTeXShape();

   public String getLaTeXSize();

   public void setTextPaint(JDRPaint paint);

   public JDRPaint getTextPaint();

   public BBox getStorageBBox();

   public BBox getBpBBox();

   //public BBox getExtent();

   /**
    * Splits this into a group of text areas each
    * consisting of a single character from this text.
    * @return group containing new text 
    */

   public JDRGroup splitText() throws InvalidShapeException;

   /**
    * Sets this text area's LaTeX font.
    * @param ltxFont LaTeX font
    * @see #setLaTeXFont(String,String,String,String)
    */
   public void setLaTeXFont(LaTeXFont ltxFont);

   public double[] getTransformation(double[] mtx);

   public void setTransformation(double[] mtx);

   public void reset();

   public CanvasGraphics getCanvasGraphics();

   public void setCanvasGraphics(CanvasGraphics cg);

   public boolean isOutline();

   public void setOutlineMode(boolean enable);

   public void setOutlineFillPaint(JDRPaint paint);

   public JDRPaint getOutlineFillPaint();

   @Deprecated
   public abstract void setFillPaint(JDRPaint paint);
   @Deprecated
   public abstract JDRPaint getFillPaint();
}
