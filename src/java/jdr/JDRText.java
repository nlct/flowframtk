// File          : JDRText.java
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
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.util.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a single line text area.
 * A text area has the following attributes:
 * <ul>
 * <li> The text that should appear in the text area (no line breaks
 * allowed).
 * <li> The alternative text to be used when exporting to a LaTeX
 * file.
 * <li> The Java font to use to display on screen or when converting
 * to PNG.
 * <li> The LaTeX font changing commands used when exporting to
 * a LaTeX file.
 * <li> The vertical and horizontal alignment settings to be passed
 * to <code>\pgftext</code> when exporting to a LaTeX file.
 * <li> The transformation matrix to apply to the text.
 * </ul>
 */

public class JDRText extends JDRCompleteObject
   implements JDRTextual,JDRConstants
{
   /**
    * Creates an empty text area at the origin.
    * The font is set to SansSerif family,
    * medium weight, upright shape and 10bp.
    * @see #JDRText(Point2D)
    */
   public JDRText(CanvasGraphics cg)
   {
      super(cg);

      init("SansSerif",
           JDRFont.SERIES_MEDIUM,
           JDRFont.SHAPE_UPRIGHT,
           new JDRLength(cg, 10, JDRUnit.bp));
   }

   /**
    * Creates an empty text area at the given location.
    * The font is set to SansSerif family,
    * medium weight, upright shape and 10bp.
    * @param p the location of the new text area
    * @see #JDRText(CanvasGraphics)
    */
   public JDRText(CanvasGraphics cg, Point2D p)
   {
      super(cg);

      init("SansSerif",
           JDRFont.SERIES_MEDIUM,
           JDRFont.SHAPE_UPRIGHT,
           new JDRLength(cg, 10, JDRUnit.bp));
      setPosition(p.getX(),p.getY());
   }

   /**
    * Creates a new text area.
    * The font is set to SansSerif family,
    * medium weight, upright shape and 10bp.
    * The graphics device is needed to set up the bounds correctly.
    * @param g the graphics device
    * @param p the location of the new text area
    * @param str the text string to appear in the text area
    */
   public JDRText(CanvasGraphics cg, Point2D p, String str)
   {
      super(cg);

      init("SansSerif",
           JDRFont.SERIES_MEDIUM,
           JDRFont.SHAPE_UPRIGHT,
           new JDRLength(cg, 10, JDRUnit.bp),str);
      setPosition(p.getX(),p.getY());
   }

   /**
    * Creates a new text area.
    * The font is set to SansSerif family,
    * medium weight, upright shape and 10bp.
    * The graphics device is needed to set up the bounds correctly.
    * @param cg the graphics info
    * @param str the text string to appear in the text area
    */
   public JDRText(CanvasGraphics cg, String str)
   {
      super(cg);

      init("SansSerif",
           JDRFont.SERIES_MEDIUM,
           JDRFont.SHAPE_UPRIGHT, new JDRLength(cg, 10, JDRUnit.bp),str);
   }

   /**
    * Creates a new text area at the origin.
    * The graphics device is needed to set up the bounds correctly.
    * @param g the graphics device
    * @param family the Java font family name
    * @param series the font series, which must be one
    * of: {@link JDRFont#SERIES_MEDIUM} or {@link JDRFont#SERIES_BOLD}
    * @param shape the font shape, which must be one
    * of:  {@link JDRFont#SHAPE_UPRIGHT}, {@link JDRFont#SHAPE_EM},
    * {@link JDRFont#SHAPE_ITALIC}, {@link JDRFont#SHAPE_SLANTED} or
    * {@link JDRFont#SHAPE_SC}
    * @param size the font size which can't
    * be negative
    * @param str the text string to appear in the text area
    */
   public JDRText(CanvasGraphics cg, String family,
               int series, int shape, JDRLength size, String str)
   {
      super(cg);
      init(family, series, shape,size,str);
      setPosition(0, 0);
   }

   /**
    * Creates a new text area at the origin.
    * @param cg the graphics info
    * @param javaFont the font 
    * @param str the text string to appear in the text area
   */
   public JDRText(CanvasGraphics cg, Font javaFont, String str)
   {
      super(cg);
      String family = javaFont.getName();
      int series = (javaFont.isBold() ?
                    JDRFont.SERIES_BOLD :
                    JDRFont.SERIES_MEDIUM);
      int shape = (javaFont.isItalic() ?
                   JDRFont.SHAPE_EM :
                   JDRFont.SHAPE_UPRIGHT);

      init(family, series, shape, 
           new JDRLength(cg.getMessageSystem(), javaFont.getSize(), JDRUnit.bp),
           str);
      setPosition(0, 0);
   }

   /**
    * Creates a new text area at the given location.
    * The graphics device is needed to set up the bounds correctly.
    * @param g the graphics device
    * @param p the location of the new text area
    * @param family the Java font family name
    * @param series the font series, which must be one 
    * of: {@link JDRFont#SERIES_MEDIUM} or {@link JDRFont#SERIES_BOLD}
    * @param shape the font shape, which must be one
    * of:  {@link JDRFont#SHAPE_UPRIGHT}, {@link JDRFont#SHAPE_EM},
    * {@link JDRFont#SHAPE_ITALIC}, {@link JDRFont#SHAPE_SLANTED} or
    * {@link JDRFont#SHAPE_SC}
    * @param size the font size which can't
    * be negative
    * @param str the text string to appear in the text area
    */
   public JDRText(CanvasGraphics cg, Point2D p, String family,
               int series, int shape, JDRLength size, String str)
   {
      super(cg);
      init(family, series, shape,size,str);
      setPosition(p.getX(),p.getY());
   }

   /**
    * Creates a new text area with the given transformation.
    * (The text area bounds should already be set by the
    * transformation.)
    * @param trans the transformation to apply to this text area
    * @param family the Java font family name
    * @param series the font series, which must be one
    * of: {@link JDRFont#SERIES_MEDIUM} or {@link JDRFont#SERIES_BOLD}
    * @param shape the font shape, which must be one
    * of:  {@link JDRFont#SHAPE_UPRIGHT}, {@link JDRFont#SHAPE_EM},
    * {@link JDRFont#SHAPE_ITALIC}, {@link JDRFont#SHAPE_SLANTED} or
    * {@link JDRFont#SHAPE_SC}
    * @param size the font size which can't
    * be negative
    * @param str the text string to appear in the text area
    */
   public JDRText(JDRTransform trans, String family,
      int series, int shape, JDRLength size, String str)
   {
      super(trans.getCanvasGraphics());
      init(family, series, shape,size,str, (JDRTransform)trans.clone());
      setFont(family, series, shape, size);
   }

   public JDRText(CanvasGraphics cg, JDRFont jdrFont, String str)
   {
      this(jdrFont, str, new JDRTransform(cg));
   }

   public JDRText(JDRFont jdrFont, String str, JDRTransform transform)
   {
      super(transform.getCanvasGraphics());
      init(jdrFont, str, transform);
   }

   public JDRText(CanvasGraphics cg, Point2D p, JDRFont jdrFont, String str)
   {
      this(jdrFont, str, new JDRTransform(cg));
      setPosition(p.getX(),p.getY());
   }

   /**
    * Creates a copy.
    */ 
   public JDRText(JDRText textObj)
   {
      super(textObj);

      text = textObj.text;
      font = textObj.font;
      latexText = textObj.latexText;
      pgfValign = textObj.pgfValign;
      pgfHalign = textObj.pgfHalign;
      isOutline = textObj.isOutline;

      jdrFont = new JDRFont(textObj.jdrFont);
      jdrtransform = new JDRTransform(textObj.jdrtransform);
      latexFont = new LaTeXFont(textObj.latexFont);

      if (textObj.textPaint != null)
      {
         textPaint = (JDRPaint)textObj.textPaint.clone();
      }

      if (textObj.fillPaint != null)
      {
         fillPaint = (JDRPaint)textObj.fillPaint.clone();
      }
   }

   private void init(String family,
                    int series,
                    int shape,
                    JDRLength size)
   {
      init(family,series,shape,size,"",
           new JDRTransform(getCanvasGraphics()));
   }

   private void init(String family,
                    int series,
                    int shape,
                    JDRLength size, String str)
   {
      init(family, series, shape, size, str,
           new JDRTransform(getCanvasGraphics()));
   }

   private void init(String family,
                    int series,
                    int shape,
                    JDRLength size,
                    String str, JDRTransform trans)
   {
      jdrFont = new JDRFont(size.getMessageSystem());
      jdrtransform = trans;

      text = str.replaceAll("[\t\r\n]", " ");

      latexFont   = new LaTeXFont();
      latexText   = text;

      pgfValign = PGF_VALIGN_BASE;
      pgfHalign = PGF_HALIGN_LEFT;

      setFont(family, series, shape, size);

      setTextPaint(new JDRColor(getCanvasGraphics(), 0,0,0));
   }

   private void init(JDRFont jdrFont, String str, JDRTransform trans)
   {
      this.jdrFont = jdrFont;
      jdrtransform = trans;

      text = str.replaceAll("[\t\r\n]", " ");

      latexFont   = new LaTeXFont();
      latexText   = text;

      pgfValign = PGF_VALIGN_BASE;
      pgfHalign = PGF_HALIGN_LEFT;

      font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());
      updateBounds();

      setTextPaint(new JDRColor(getCanvasGraphics(), 0,0,0));
   }

   public void setTextPaint(JDRPaint paint)
   {
      textPaint = paint;
   }

   public JDRPaint getTextPaint()
   {
      return textPaint;
   }

   public void fade(double value)
   {
      textPaint.fade(value);
   }

   /**
    * Resets the transformation.
    */
   public void reset()
   {
      jdrtransform.reset();
   }

   /**
    * Gets the width of this text area.
    * @return width of this text area
    */
   public double getWidth()
   {
      updateBounds();
      return getStorageBBox().getWidth();
   }

   /**
    * Updates the bounding box of this text area.
    */
   public void updateBounds()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();
    
      if (g2 == null)
      {
         return;
      }

      double bpToStorage = cg.bpToStorage(1.0);

      if (text.equals(""))
      {
         FontMetrics fm = g2.getFontMetrics(font);
         double h = fm.getHeight()*bpToStorage;
         double d = fm.getDescent()*bpToStorage;

         jdrtransform.updateOriginalBounds(new BBox(cg, 0,d-h,0,d));
      }
      else
      {
         String str = text;

         if (str.startsWith(" "))
         {
            str = "_"+str.substring(1);
         }

         if (str.endsWith(" "))
         {
            str = str.substring(0, str.length()-1)+"_";
         }

         FontRenderContext frc = g2.getFontRenderContext();
         TextLayout layout = new TextLayout(str, font, frc);

         AffineTransform af = AffineTransform.getScaleInstance(
            bpToStorage, bpToStorage);
         Shape outline = layout.getOutline(af);

         if (isOutline)
         {
            outline = getOutlineStroke(cg).createStrokedShape(outline);
         }

         Rectangle2D bounds = outline.getBounds2D();

         bounds.setRect(bounds.getX(), bounds.getY(),
                        bounds.getWidth()+bpToStorage,
                        bounds.getHeight());

         jdrtransform.updateOriginalBounds(bounds);
      }
   }

   /**
    * Sets the font for this text area.
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
                       int shape, JDRLength size)
   {
      jdrFont.setFamily(name);
      jdrFont.setWeight(series);
      jdrFont.setShape(shape);
      jdrFont.setSize(size);
      font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());
      updateBounds();
   }

   /**
    * Sets the font family for this text area.
    * @param name the Java font family name
    */
   public void setFontFamily(String name)
   {
      jdrFont.setFamily(name);
      font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());
      updateBounds();
   }

   /**
    * Sets the font series for this text area.
    * @param series the font series, which must be one
    * of: {@link JDRFont#SERIES_MEDIUM} or {@link JDRFont#SERIES_BOLD}
    */
   public void setFontSeries(int series)
   {
      jdrFont.setWeight(series);
      font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());
      updateBounds();
   }

   /**
    * Sets the font shape for this text area.
    * @param shape the font shape, which must be one
    * of:  {@link JDRFont#SHAPE_UPRIGHT}, {@link JDRFont#SHAPE_EM},
    * {@link JDRFont#SHAPE_ITALIC}, {@link JDRFont#SHAPE_SLANTED} or
    * {@link JDRFont#SHAPE_SC}
    */
   public void setFontShape(int shape)
   {
      jdrFont.setShape(shape);
      font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());
      updateBounds();
   }

   /**
    * Sets the font for this text area.
    * @param size the font size
    */
   public void setFontSize(JDRLength size)
   {
      jdrFont.setSize(size);
      font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());
      updateBounds();
   }

   /**
    * Gets the name of the Java font family used by this text area.
    * @return font family name
    */
   public String getFontFamily()
   {
      return jdrFont.getFamily();
   }

   /**
    * Gets the font series for this text area.
    * @return font series
    */
   public int getFontSeries()
   {
      return jdrFont.getWeight();
   }

   /**
    * Gets the font shape for this text area.
    * @return font shape
    */
   public int getFontShape()
   {
      return jdrFont.getShape();
   }

   /**
    * Gets the Java font weight (required by {@link Font}).
    * @return {@link Font} weight
    */
   private int getFontWeight()
   {
      int weight = 0;
      weight += (getFontSeries() == JDRFont.SERIES_MEDIUM?
                 Font.PLAIN : Font.BOLD);
      weight += (getFontShape() == JDRFont.SHAPE_UPRIGHT?
                 0 : Font.ITALIC);

      return weight;
   }

   /**
    * Gets the font size for this text field.
    * @return font size
    */
   public JDRLength getFontSize()
   {
      return jdrFont.getSize();
   }

   /**
    * Gets the Java font used to display this text area.
    * @return font
    */
   public Font getFont()
   {
      return font;
   }

   /**
    * Gets the JDR font associated with this text area.
    * @return font
    */
   public JDRFont getJDRFont()
   {
      return jdrFont;
   }

   /**
    * Splits this text area into a group of text areas each 
    * consisting of a single character from this text area.
    * @return group containing new text areas
    */
   public JDRGroup splitText() 
     throws InvalidShapeException
   {
      CanvasGraphics cg = getCanvasGraphics();

      CanvasGraphics bpCg = new CanvasGraphics();

      Graphics2D g2 = cg.getGraphics();
      FontRenderContext frc = g2.getFontRenderContext();

      JDRGroup group = new JDRGroup(bpCg);

      int n = text.length();

      JDRTransform trans = (JDRTransform)jdrtransform.clone();
      trans.applyCanvasGraphics(bpCg);

      for (int i = 0; i < n;)
      {
         int codePoint = text.codePointAt(i);
         i += Character.charCount(codePoint);

         if (Character.isWhitespace(codePoint))
         {
            continue;
         }

         FontMetrics fm = g2.getFontMetrics(font);
      
         Rectangle2D rect = fm.getStringBounds(text, 0, i, g2);
         int cw = fm.charWidth(codePoint);

         Point2D p = new Point2D.Double(rect.getWidth()-cw,0);

         StringBuilder builder = new StringBuilder(2);
         builder.appendCodePoint(codePoint);

         JDRText newText = new JDRText(bpCg, p, builder.toString());

         newText.setTextPaint(getTextPaint());
         newText.jdrFont.makeEqual(jdrFont);
         newText.font = getFont();
         newText.latexFont.makeEqual(latexFont);
         newText.pgfValign = pgfValign;
         newText.pgfHalign = pgfHalign;
         newText.transform(trans);
         newText.updateBounds();

         group.add(newText);
      }

      group.applyCanvasGraphics(cg);
      group.setSelected(isSelected());

      return group;
   }

   /**
    * Gets the outline of this text area.
    * @param frc the font render context
    * @return shape describing the outline of this text area
    */
   public Shape getOutline(FontRenderContext frc)
   {
      TextLayout tl = new TextLayout(text, font, frc);

      return tl.getOutline(jdrtransform.copyAffineTransform());
   }

   /**
    * Converts this text area into a group containing paths
    * that approximate the outline of the text.
    * @return group containing new paths
    */
   public JDRGroup convertToPath() 
      throws InvalidPathException,EmptyGroupException
   {
      return convertToPath(new JDRBasicStroke(getCanvasGraphics()));
   }

   public JDRGroup convertToPath(JDRStroke stroke) 
      throws InvalidPathException,EmptyGroupException
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();
      FontRenderContext frc = g2.getFontRenderContext();

      BBox bbox = getStorageBBox();

      CanvasGraphics bpCG =
       (cg.getStorageUnitID() == JDRUnit.BP ? cg : new CanvasGraphics());

      JDRGroup group = new JDRGroup(cg);

      if (description.isEmpty())
      {
         group.description = getText();
      }
      else
      {
         group.description = description;
      }

      int n = text.length();

      JDRPaint pathStrokePaint;
      JDRPaint pathFillPaint;

      if (isOutline)
      {
         pathStrokePaint = getTextPaint();
         pathFillPaint = getFillPaint();

         if (pathFillPaint == null)
         {
            pathFillPaint = new JDRTransparent(cg);
         }
      }
      else
      {
         pathStrokePaint = new JDRTransparent(cg);
         pathFillPaint = getTextPaint();
      }

      for (int i = 0; i < n; )
      {
         int cp = text.codePointAt(i);
         i += Character.charCount(cp);

         if (cp == ' ') continue;

         String charStr = new String(Character.toChars(cp));

         FontMetrics fm = g2.getFontMetrics(font);
      
         Rectangle2D rect = fm.getStringBounds(text, 0, i, g2);
         int cw = fm.charWidth(cp);

         AffineTransform af = AffineTransform.getTranslateInstance(
                             rect.getWidth() - cw, 0);

         TextLayout tl = new TextLayout(charStr, font, frc);
         Shape outline = tl.getOutline(af);

         PathIterator pi = outline.getPathIterator(null);

         JDRPath path = JDRPath.getPath(bpCG, pi);
         path.setLinePaint(pathStrokePaint);
         path.setFillPaint(pathFillPaint);
         path.setStroke((JDRStroke)stroke.clone());

         if (bpCG != cg)
         {
            path.applyCanvasGraphics(cg);
         }

         path.description = charStr;

         group.add(path);
      }

      if (group.size() == 0)
      {
         throw new EmptyGroupException(getCanvasGraphics());
      }

      group.setSelected(isSelected());

      jdrtransform.transform(group);
      
/*
      BBox grpBox = group.getBBox();
      double shiftx = bbox.getMinX()-grpBox.getMinX();
      double shifty = bbox.getMinY()-grpBox.getMinY();
      group.translate(-shiftx, -shifty);
*/

      return group;
   }

   /**
    * Preconcatenates a transformation with this text areas
    * transformation.
    * @param trans affine transformation to preconcatenate
    */
   public void preConcatenate(AffineTransform trans)
   {
      jdrtransform.preConcatenate(trans);
   }

   /**
    * Transforms this text area by concatenation a transformation
    * with this text area's transformation matrix.
    */
   @Override
   public void transform(double[] matrix)
   {
      jdrtransform.concat(matrix);
   }

   /**
    * Transforms this text area by concatenation a transformation
    * with this text area's transformation matrix.
    * @param jdrt transformation to apply
    */
   public void transform(JDRTransform jdrt)
   {
      jdrtransform.concat(jdrt);
   }

   /**
    * Transforms this text area by concatenation a transformation
    * with this text area's transformation matrix.
    * @param af transformation to apply
    */
   @Override
   public void transform(AffineTransform af)
   {
      jdrtransform.concat(af);
   }

   public void rotate(double angle)
   {
      jdrtransform.rotate(angle);
   }

   public void rotate(Point2D p, double angle)
   {
      jdrtransform.rotate(p, angle);
   }

   public void scaleX(double factor)
   {
      scale(factor, 1.0);
   }

   public void scaleX(Point2D p, double factor)
   {
      scale(p,factor,1.0);
   }

   public void scaleY(double factor)
   {
      scale(1.0, factor);
   }

   public void scaleY(Point2D p, double factor)
   {
      scale(p,1.0,factor);
   }

   public void scale(double factorX, double factorY)
   {
      jdrtransform.scale(factorX, factorY);
   }

   public void scale(Point2D p, double factorX, double factorY)
   {
      jdrtransform.scale(p, factorX, factorY);
   }

   public void shearX(double factor)
   {
      shear(factor, 0.0);
   }

   public void shearX(Point2D p, double factor)
   {
      shear(p,factor,0.0);
   }

   public void shearY(double factor)
   {
      shear(0.0, factor);
   }

   public void shearY(Point2D p, double factor)
   {
      shear(p,0.0,factor);
   }

   public void shear(Point2D p, double factor)
   {
      shear(p,factor,factor);
   }

   public void shear(double factor)
   {
      shear(factor, factor);
   }

   public void shear(double factorX, double factorY)
   {
      jdrtransform.shear(factorX, factorY);
   }

   public void shear(Point2D p, double factorX, double factorY)
   {
      jdrtransform.shear(p, factorX, factorY);
   }

   public void translate(double x, double y)
   {
      jdrtransform.translate(x, y);
   }

   /**
    * Sets this text area's position.
    * @param x the x co-ordinate
    * @param y the y co-ordinate
    */
   public void setPosition(double x, double y)
   {
      jdrtransform.setPosition(x, y);
   }

   public BBox getStorageBBox()
   {
      return jdrtransform.getBBox();
   }

   /**
    * Gets the starting position of this text area.
    * This is given by {@link JDRTransform#getAnchor()}.
    * @return starting position
    */
   public JDRPoint getStart()
   {
      return jdrtransform.getAnchor();
   }

   /**
    * Gets the centre point.
    * This is given by {@link JDRTransform#getCentre()}
    * @return the centre of the text area
    */
   public JDRPoint getCentre()
   {
      return jdrtransform.getCentre();
   }

   /**
    * Sets the text to appear in this text area. The LaTeX alternative
    * is set to the same text.
    * @param str the new text
    * @see #setText(Graphics,String,String)
    */
   public void setText(String str)
   {
      text = str.replaceAll("[\t\r\n]", " ");
      latexText = text;

      updateBounds();
   }

   /**
    * Sets the text to appear in this text area and the alternative
    * LaTeX text.
    * @param str the new text
    * @param latexStr the alternative LaTeX text
    * @see #setText(String)
    */
   public void setText(String str, String latexStr)
   {
      text = str.replaceAll("[\t\r\n]", " ");

      if (latexStr == null)
      {
         latexText = text;
      }
      else
      {
         latexText = latexStr.replaceAll("[\t\r\n]", " ");
      }
   }

   /**
    * Gets the text to appear in this text area.
    * @return the text to appear in this text area
    */
   public String getText()
   {
      return text;
   }

   /**
    * Sets the alternative LaTeX text.
    * @param str the alternative LaTeX text
    * @see #setText(String,String)
    */
   public void setLaTeXText(String str)
   {
      latexText = str.replaceAll("[\t\r\n]", " ");
   }

   /**
    * Gets the alternative LaTeX text.
    * @return alternative LaTeX text
    */
   public String getLaTeXText()
   {
      return latexText;
   }

   public void draw(FlowFrame parentFrame)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();

      if (g2 == null) return;

      FontRenderContext frc = g2.getFontRenderContext();
      TextLayout layout = new TextLayout(text, font, frc);

      /*
      * The font size is in bp (integer) so we need to scale it
      * to the storage unit.
      */

      double bpToStorage = cg.bpToStorage(1.0);

      AffineTransform af = jdrtransform.copyAffineTransform();

      if (parentFrame == null)
      {
         parentFrame = flowframe;
      }

      if (parentFrame != null && cg.isEvenPage())
      {
         af.translate(parentFrame.getEvenXShift(), 
                      parentFrame.getEvenYShift());
      }

      af.scale(bpToStorage, bpToStorage);

      Shape outline = layout.getOutline(af);

      BBox box = null;

      if (getTextPaint() instanceof JDRShading)
      {
         Rectangle2D bounds = outline.getBounds();

         box = new BBox(cg, bounds);
      }

      if (isOutline)
      {
         if (fillPaint != null && !(fillPaint instanceof JDRTransparent))
         {
            g2.setPaint(fillPaint.getPaint(box));
            g2.fill(outline);
         }

         g2.setPaint(getTextPaint().getPaint(box));

         Stroke oldStroke = g2.getStroke();

         g2.setStroke(getOutlineStroke(cg));

         g2.draw(outline);

         g2.setStroke(oldStroke);
      }
      else
      {
         g2.setPaint(getTextPaint().getPaint(box));

         g2.fill(outline);
      }
   }

   public void print(Graphics2D g2)
   {
      CanvasGraphics cg = getCanvasGraphics();

      AffineTransform oldAf = g2.getTransform();
      double storageToBp = cg.storageToBp(1.0);
      g2.scale(storageToBp, storageToBp);

      FontRenderContext frc = g2.getFontRenderContext();
      TextLayout layout = new TextLayout(text, font, frc);

      double bpToStorage = cg.bpToStorage(1.0);

      AffineTransform af = jdrtransform.copyAffineTransform();
      af.scale(bpToStorage, bpToStorage);

      Shape outline = layout.getOutline(af);

      BBox box = null;

      if (getTextPaint() instanceof JDRShading)
      {
         Rectangle2D bounds = outline.getBounds();

         box = new BBox(cg, bounds);
      }

      g2.setPaint(getTextPaint().getPaint(box));

      if (isOutline)
      {
         Stroke oldStroke = g2.getStroke();
         g2.setStroke(getOutlineStroke(cg));
         g2.draw(outline);

         g2.setStroke(oldStroke);
      }
      else
      {
         g2.fill(outline);
      }

      g2.setTransform(oldAf);
   }

   public void drawControls(boolean endPoint)
   {
   }

   /**
    * Draws text area with the PGF anchors.
    * @param anchorPaint the anchor paint
    */
   public void drawWithAnchors(Paint anchorPaint)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();

      AffineTransform af = jdrtransform.copyAffineTransform();

      AffineTransform oldAf = g2.getTransform();

      g2.setTransform(new AffineTransform());

      FontRenderContext frc = g2.getFontRenderContext();
      TextLayout layout = new TextLayout(text, font, frc);

      af.preConcatenate(oldAf);
      g2.setTransform(af);

      if (getTextPaint() instanceof JDRShading)
      {
         Rectangle2D bounds = layout.getBounds();

         BBox box = new BBox(cg, bounds);

         g2.setPaint(getTextPaint().getPaint(box));
      }
      else
      {
         g2.setPaint(getTextPaint().getColor());
      }

      layout.draw(g2, 0.0f, 0.0f);

      g2.setPaint(anchorPaint);

      Point2D p = getPGFAnchor();

      double radius = 1;

      g2.fill(new Ellipse2D.Double(p.getX()-radius, p.getY()-radius,
              2*radius, 2*radius));

      g2.setTransform(oldAf);
   }

   public void fill()
   {
   }

   public Object clone()
   {
      JDRText dt = new JDRText(jdrtransform,
                            getFontFamily(), getFontSeries(),
                            getFontShape(), (JDRLength)getFontSize().clone(), text);

      dt.makeEqual(this);

      return dt;
   }

   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;

      if (!(obj instanceof JDRText)) return false;

      JDRText textObj = (JDRText)obj;

      if (!getTextPaint().equals(textObj.getTextPaint())) return false;

      if (!jdrFont.equals(textObj.jdrFont)) return false;

      if (!text.equals(textObj.getText())) return false;

      if (latexText == null || textObj.latexText == null)
      {
         if (latexText != textObj.latexText) return false;
      }
      else
      {
         if (!latexText.equals(textObj.latexText)) return false;
      }

      if (!jdrtransform.equals(textObj.jdrtransform)) return false;

      if (!latexFont.equals(textObj.latexFont)) return false;

      if (pgfValign != textObj.pgfValign) return false;

      if (pgfHalign != textObj.pgfHalign) return false;

      if (isOutline != textObj.isOutline) return false;

      if (fillPaint == textObj.fillPaint) return true;

      if ((fillPaint == null && textObj.fillPaint != null)
       || !fillPaint.equals(textObj.fillPaint))
      {
         return false;
      }

      return true;
   }

   /**
    * Makes this text area equivalent to another text area.
    * @param t the other text area
    */
   public void makeEqual(JDRText t)
   {
      super.makeEqual(t);

      setTextPaint((JDRPaint)t.getTextPaint().clone());

      text = t.getText();
      jdrFont.makeEqual(t.jdrFont);
      font = t.getFont();
      jdrtransform.makeEqual(t.getTransform());
      latexFont.makeEqual(t.latexFont);
      pgfValign = t.pgfValign;
      pgfHalign = t.pgfHalign;
      latexText = t.latexText;
      isOutline = t.isOutline;

      fillPaint = (t.fillPaint == null ? null : 
                  (JDRPaint)t.fillPaint.clone());
   }

   /**
    * Gets the transformation to apply to this text area.
    * @return text area's transformation
    */
   public JDRTransform getTransform()
   {
      return jdrtransform;
   }

   /**
    * Gets this text area's transformation matrix.
    * @param matrix flat matrix in which to store transformation
    * or null if new array should be created
    * @return transformation matrix
    */
   public double[] getTransformation(double[] matrix)
   {
      if (matrix == null)
      {
         matrix = new double[6];
      }

      jdrtransform.getTransformation(matrix);

      return matrix;
   }

   /**
    * Sets this text area's transformation matrix.
    * @param matrix flat matrix storing transformation
    */
   public void setTransformation(double[] matrix)
   {
      jdrtransform.setTransformation(matrix);
   }

   /**
    * Sets the LaTeX font family declaration for this text area.
    * @param family LaTeX font family declaration
    * @see LaTeXFont#setFamily(String)
    */
   public void setLaTeXFamily(String family)
   {
      latexFont.setFamily(family);
   }

   /**
    * Sets the LaTeX font size declaration for this text area.
    * @param size LaTeX font size declaration
    * @see LaTeXFont#setSize(String)
    */
   public void setLaTeXSize(String size)
   {
      latexFont.setSize(size);
   }

   /**
    * Sets the LaTeX font series declaration for this text area.
    * @param series LaTeX font series declaration
    * @see LaTeXFont#setWeight(String)
    */
   public void setLaTeXSeries(String series)
   {
      latexFont.setWeight(series);
   }

   /**
    * Sets the LaTeX font shape declaration for this text area.
    * @param shape LaTeX font shape declaration
    * @see LaTeXFont#setShape(String)
    */
   public void setLaTeXShape(String shape)
   {
      latexFont.setShape(shape);
   }

   /**
    * Sets the LaTeX font declarations for this text area.
    * @param family LaTeX font family declaration
    * @param shape LaTeX font shape declaration
    * @param size LaTeX font size declaration
    * @param series LaTeX font series declaration
    * @see #setLaTeXFont(LaTeXFont)
    */
   public void setLaTeXFont(String family, String size, String series,
                        String shape)
   {
      latexFont.setFamily(family);
      latexFont.setSize(size);
      latexFont.setWeight(series);
      latexFont.setShape(shape);
   }

   /**
    * Sets this text area's LaTeX font.
    * @param ltxFont LaTeX font
    * @see #setLaTeXFont(String,String,String,String)
    */
   public void setLaTeXFont(LaTeXFont ltxFont)
   {
      latexFont = ltxFont;
   }

   /**
    * Sets the vertical alignment for <code>\pgftext</code>.
    * @param valign the vertical alignment, which must be one of:
    * {@link #PGF_VALIGN_TOP}, {@link #PGF_VALIGN_CENTRE},
    * {@link #PGF_VALIGN_BASE} or {@link #PGF_VALIGN_BOTTOM}
    */
   public void setVAlign(int valign)
   {
      if (valign < 0 || valign > 3)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.VALIGN, valign, getCanvasGraphics());
      }

      pgfValign = valign;
   }

   /**
    * Sets the horizontal alignment for <code>\pgftext</code>.
    * @param halign the horizontal alignment, which must be one of:
    * {@link #PGF_HALIGN_LEFT}, {@link #PGF_HALIGN_CENTRE} or
    * {@link #PGF_HALIGN_RIGHT}
    */
   public void setHAlign(int halign)
   {
      if (halign < 0 || halign > 2)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.HALIGN, halign, getCanvasGraphics());
      }

      pgfHalign = halign;
   }

   public void setAlign(int halign, int valign)
   {
      setHAlign(halign);
      setVAlign(valign);
   }

   /**
    * Gets the horizontal alignment for <code>\pgftext</code>.
    * @return horizontal alignment
    */
   public int getHAlign()
   {
      return pgfHalign;
   }

   /**
    * Gets the vertical alignment for <code>\pgftext</code>.
    * @return vertical alignment
    */
   public int getVAlign()
   {
      return pgfValign;
   }

   /**
    * Gets the LaTeX font family declaration for this text area.
    * @return LaTeX font family declaration
    * @see LaTeXFont#getFamily()
    */
   public String getLaTeXFamily()
   {
      return latexFont.getFamily();
   }

   /**
    * Gets the LaTeX font series declaration for this text area.
    * @return LaTeX font series declaration
    * @see LaTeXFont#getWeight()
    */
   public String getLaTeXSeries()
   {
      return latexFont.getWeight();
   }

   /**
    * Gets the LaTeX font shape declaration for this text area.
    * @return LaTeX font shape declaration
    * @see LaTeXFont#getShape()
    */
   public String getLaTeXShape()
   {
      return latexFont.getShape();
   }

   /**
    * Gets the LaTeX font size declaration for this text area.
    * @return LaTeX font size declaration
    * @see LaTeXFont#getSize()
    */
   public String getLaTeXSize()
   {
      return latexFont.getSize();
   }

   /**
    * Gets the LaTeX font associated with this text area.
    * @return LaTeX font for this text area
    */
   public LaTeXFont getLaTeXFont()
   {
      return latexFont;
   }

   private Point2D getPGFAnchor()
   {
      double x = 0;
      double y = 0;

      BBox box = jdrtransform.getOriginalBBox();

      switch (pgfValign)
      {
         case PGF_VALIGN_TOP:
            y = box.getMinY();
         break;
         case PGF_VALIGN_CENTRE:
            y = box.getMidY();
         break;
         case PGF_VALIGN_BASE:
            y = 0;
         break;
         case PGF_VALIGN_BOTTOM:
            y = box.getMaxY();
         break;
      }

      switch (pgfHalign)
      {
         case PGF_HALIGN_LEFT:
            x = 0;
         break;
         case PGF_HALIGN_CENTRE:
            x = box.getMidX();
         break;
         case PGF_HALIGN_RIGHT:
            x = box.getMinX()+box.getMaxX();
         break;
      }

      return new Point2D.Double(x, y);
   }

   public void savePgf(TeX tex)
    throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      String valign="";
      String halign="";

      double x = 0;
      double y = 0;

      BBox box = jdrtransform.getOriginalBBox();

      switch (pgfValign)
      {
         case PGF_VALIGN_TOP:
            valign= "top";
            y = box.getMaxY()-box.getMinY();
         break;
         case PGF_VALIGN_CENTRE:
            valign= "center";
            y = box.getMidY()-box.getMinY();
         break;
         case PGF_VALIGN_BASE:
            valign= "base";
            y = box.getMaxY();
         break;
         case PGF_VALIGN_BOTTOM:
            valign= "bottom";
            y = 0;
         break;
      }

      switch (pgfHalign)
      {
         case PGF_HALIGN_LEFT:
            halign= "left";
            x = 0;
         break;
         case PGF_HALIGN_CENTRE:
            halign= "center";
            x = box.getMidX();
         break;
         case PGF_HALIGN_RIGHT:
            halign= "right";
            x = box.getMinX()+box.getMaxX();
         break;
      }

      ExportSettings exportSettings = tex.getExportSettings();

      JDRPaint p = getTextPaint();
      JDRPaint fill = fillPaint;

      String str;

      if (p instanceof JDRTransparent)
      {
         str = "\\pgftext["+halign+","+valign+"]{"
              + latexFont.tex() 
              + "\\phantom{"
              + (latexText == null || latexText.isEmpty() ? text : latexText)
              + "}}";
      }
      else
      {
         if (p instanceof JDRShading)
         {
            String shadingSetting = exportSettings.textualShading.toString().toLowerCase();

            String msg = cg.getMessageWithFallback(
               "warning.pgf-no-text-shading",
               "Text shading paint can't be exported to pgf: using export setting {0}",
               cg.getMessageDictionary().getMessageWithFallback(
                "export.textualshading."+shadingSetting,
                shadingSetting));

            cg.getMessageSystem().getPublisher().publishMessages(
               MessageInfo.createMessage(msg));

            tex.comment(msg);

            JDRShading shading = (JDRShading)p;

            switch (exportSettings.textualShading)
            {
               case AVERAGE:
                  p = shading.getStartColor().average(shading.getEndColor());
               break;
               case START:
                  p = shading.getStartColor();
               break;
               case END:
                  p = shading.getEndColor();
               break;
               case TO_PATH:
                  try
                  {
                     JDRGroup g = convertToPath();
                     g.mergePaths(null).savePgf(tex);
                  }
                  catch (Exception e)
                  {
                     cg.getMessageSystem().getPublisher().publishMessages(
                        MessageInfo.createWarning(e));
                  }

                  return;
            }
         }

         String exportText =
           (latexText == null || latexText.isEmpty() ? text : latexText);

         if (isOutline())
         {
            if (fill instanceof JDRShading)
            {
               String shadingSetting = exportSettings.textualShading.toString().toLowerCase();

               String msg = cg.getMessageWithFallback(
                  "warning.pgf-no-text-shading",
                  "Text shading paint can''t be exported to pgf: using export setting {0}",
                  cg.getMessageDictionary().getMessageWithFallback(
                   "export.textualshading."+shadingSetting,
                   shadingSetting));

               cg.getMessageSystem().getPublisher().publishMessages(
                  MessageInfo.createMessage(msg));

               tex.comment(msg);

               JDRShading shading = (JDRShading)fill;

               switch (exportSettings.textualShading)
               {
                  case AVERAGE:
                     fill = shading.getStartColor().average(shading.getEndColor());
                  break;
                  case START:
                     fill = shading.getStartColor();
                  break;
                  case END:
                     fill = shading.getEndColor();
                  break;
                  case TO_PATH:
                     try
                     {
                        JDRGroup g = convertToPath();
                        g.mergePaths(null).savePgf(tex);
                     }
                     catch (Exception e)
                     {
                        cg.getMessageSystem().getPublisher().publishMessages(
                           MessageInfo.createWarning(e));
                     }

                   return;
               }
            }

            String colDefs = String.format(
                             "\\definecolor{strokepaint}{%s}{%s}", 
                             p.pgfmodel(), p.pgfspecs());
            String pdf = p.getPdfStrokeSpecs();
            String ps = "linecolor=strokepaint";

            int tr = 1;

            if (fill != null && !(fill instanceof JDRTransparent))
            {
               colDefs += String.format(
                             "%%%n\\definecolor{fillpaint}{%s}{%s}", 
                             fill.pgfmodel(), fill.pgfspecs());
               tr = 2;
               pdf += " "+fill.getPdfFillSpecs();
               ps += ",fillcolor=fillpaint,fillstyle=solid";
            }

            exportText = String.format(
                         "%s%%%n\\jdroutline{%d Tr %s}{%s}{%s%%%n%s}",
                         colDefs, 
                         tr, pdf, ps,
                         latexFont.tex(), exportText
                         );
         }
         else
         {
            exportText = latexFont.tex() + p.pgf(box) + exportText;
         }

         str = "\\pgftext["+halign+","+valign+"]{" + exportText + "}";
      }

      jdrtransform.savePgf(tex, x, y, str);
   }

   public void saveEPS(PrintWriter out)
      throws IOException
   {
      JDRPaint paint = getTextPaint();

      if (paint instanceof JDRTransparent)
      {
         return;
      }

      out.println("gsave");
      String psname = getFont().getPSName();

      // substitute generic names
      if (psname.equals("SansSerif.plain"))
      {
         psname = "Helvetica";
      }
      else if (psname.equals("SansSerif.bold"))
      {
         psname = "Helvetica-Bold";
      }
      else if (psname.equals("SansSerif.italic"))
      {
         psname = "Helvetica-Oblique";
      }
      else if (psname.equals("SansSerif.bolditalic"))
      {
         psname = "Helvetica-BoldOblique";
      }
      else if (psname.equals("Serif.plain"))
      {
         psname = "Times-Roman";
      }
      else if (psname.equals("Serif.bold"))
      {
         psname = "Times-Bold";
      }
      else if (psname.equals("Serif.italic"))
      {
         psname = "Times-Italic";
      }
      else if (psname.equals("Serif.bolditalic"))
      {
         psname = "Times-BoldItalic";
      }
      else if (psname.equals("Monospaced.plain"))
      {
         psname = "Courier";
      }
      else if (psname.equals("Monospaced.bold"))
      {
         psname = "Courier-Bold";
      }
      else if (psname.equals("Monospaced.italic"))
      {
         psname = "Courier-Oblique";
      }
      else if (psname.equals("Monospaced.bolditalic"))
      {
         psname = "Courier-BoldOblique";
      }

      out.println("/"+psname+" findfont");
      out.println(""+jdrFont.getBpSize()+" scalefont");
      out.println("setfont");
      jdrtransform.saveEPS(out);
      out.println("0 0 moveto");

      String psString = "";

      char[] charArray = text.toCharArray();

      for (int i = 0; i < charArray.length; i++)
      {
         if (charArray[i] == '\\')
         {
            psString += "\\\\";
         }
         else if (charArray[i] == '(')
         {
            psString += "\\(";
         }
         else if (charArray[i] == ')')
         {
            psString += "\\)";
         }
         else
         {
            psString += charArray[i];
         }
      }

      out.println("("+psString+")");

      if ((paint instanceof JDRGradient)
        || (paint instanceof JDRRadial))
      {
         out.println("true charpath");

         // need original bounding box
         BBox box = jdrtransform.getOriginalBBox();
         double storageToBp = getCanvasGraphics().storageToBp(1.0);

         // flip
         double minY = -box.getMaxY()*storageToBp;
         double maxY = -box.getMinY()*storageToBp;
         double minX = box.getMinX()*storageToBp;
         double maxX = box.getMaxX()*storageToBp;
         box.reset(minX, minY, maxX, maxY);

         paint.saveEPS(out, box);

         out.println("clip shfill");
      }
      else
      {
         paint.saveEPS(out, null);
         out.println("show");
      }

      out.println("grestore");
   } 

   public void saveSVG(SVG svg, String attr)
      throws IOException
   {
      ExportSettings exportSettings = svg.getExportSettings();

       if (isOutline()
           && (exportSettings.textAreaOutline ==
               ExportSettings.TextAreaOutline.TO_PATH))
      {
         JDRShape shape = null;

         try
         {
            JDRGroup group = convertToPath();
            shape = group.mergePaths(null);
            shape.setDescription(text);
         }
         catch (Exception e)
         {
            getCanvasGraphics().getMessageSystem().getPublisher().publishMessages(
               MessageInfo.createWarning(e));
            shape = null;
         }

         if (shape != null)
         {
            shape.saveSVG(svg, attr);
            return;
         }
      }

      svg.println("   <text "+attr+" x=\"0\" y=\"0\" ");
      svg.println("   "+getTextPaint().svgFill());
      svg.println("       "+jdrFont.svg()
                  +" "+jdrtransform.svg(svg)+">");

      if (description != null && !description.isEmpty())
      {
         svg.print("<title>");
         svg.print(svg.encodeContent(description));
         svg.println("</title>");
      }

      svg.println(svg.encodeContent(text));

      svg.println("   </text>");
   }

   @Override
   public void writeSVGdefs(SVG svg) throws IOException
   {
      JDRPaint paint = getTextPaint();
   
      paint.writeSVGdefs(svg);

      if (isOutline && getFillPaint() != null)
      {
         getFillPaint().writeSVGdefs(svg);
      }
   }

   /**
    * Gets string representation of this text area.
    * @return string representation of this text area
    */
   public String toString()
   {
      return "JDRText:"+text+"@"+jdrtransform;
   }

   public JDRObjectLoaderListener getListener()
   {
      return textListener;
   }

   /**
    * If the contents of this text area contain TeX special 
    * characters, set the LaTeX equivalent with special characters 
    * replaced with control sequences.
    */
   public void escapeTeXChars()
   {
      boolean replaced=false;

      int n = text.length();
      StringBuilder buffer = new StringBuilder(n);

      for (int i = 0; i < n; )
      {
         int cp = text.codePointAt(i);
         i += Character.charCount(cp);

         switch (cp)
         {
            case '\\' :
               buffer.append("\\textbackslash{}");
               replaced = true;
            break;
            case '^' :
               buffer.append("\\textasciicircum{}");
               replaced = true;
            break;
            case '~' :
               buffer.append("\\textasciitilde{}");
               replaced = true;
            break;
            case '$' :
            case '&' :
            case '#' :
            case '%' :
            case '_' :
            case '{' :
            case '}' :
               buffer.append("\\");
               buffer.appendCodePoint(cp);
               replaced = true;
            break;
            default :
               buffer.appendCodePoint(cp);
         }
      }

      if (replaced)
      {
         latexText = buffer.toString();
      }
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "Text:"+eol;
      str += "contents: "+text+eol;
      str += "LaTeX equivalent: "+latexText+eol;
      str += "Paint: "+getTextPaint()+eol;
      str += "font: "+jdrFont.info()+eol;
      str += "LaTeX font: "+latexFont.info()+eol;
      str += "start: "+getStart()+eol;
      str += "pgfHalign: " +pgfHalign+eol;
      str += "pgfValign: " +pgfValign+eol;
      str += "pgfanchor: "+getPGFAnchor()+eol;
      str += "outline: "+isOutline()+eol;
      str += "transformation: "+jdrtransform.info()+eol;
      str += "original bounds: "+jdrtransform.getOriginalBBox().info()+eol;

      return str+super.info();
   }

   public JDRTextual getTextual()
   {
      return this;
   }

   public boolean hasTextual()
   {
      return true;
   }

   public boolean hasShape()
   {
      return false;
   }

   public boolean hasSymmetricPath()
   {
      return false;
   }

   public JDRSymmetricPath getSymmetricPath()
   {
      return null;
   }

   public boolean hasPattern()
   {
      return false;
   }

   public JDRPattern getPattern()
   {
      return null;
   }

   public int getObjectFlag()
   {
      int flag = super.getObjectFlag() | SELECT_FLAG_TEXT
        | SELECT_FLAG_TEXTUAL;

      if (isOutline)
      {
         flag = flag | SELECT_FLAG_OUTLINE;
      }

      return flag;
   }

   public Object[] getDescriptionInfo()
   {
      return new Object[] {getText(),
         latexText == null || latexText.isEmpty() ? text : latexText};
   }

   public JDRPoint getControlFromStoragePoint(double x, double y, boolean endPoint)
   {
      return null;
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      setCanvasGraphics(cg);
      textPaint.applyCanvasGraphics(cg);
      jdrtransform.applyCanvasGraphics(cg);
   }

   public void setOutlineMode(boolean enable)
   {
      isOutline = enable;
      updateBounds();
   }

   public boolean isOutline()
   {
      return isOutline;
   }

   /*
    * Fill paint is only used in outline mode.
    */ 
   public void setFillPaint(JDRPaint paint)
   {
      fillPaint = paint;
   }

   public JDRPaint getFillPaint()
   {
      return fillPaint;
   }

   public static Stroke getOutlineStroke(CanvasGraphics cg)
   {
      return getOutlineStroke(cg.getStorageUnit());
   }

   public static Stroke getOutlineStroke(JDRUnit unit)
   {
      switch (unit.getID())
      {
         case JDRUnit.BP:
           return BP_OUTLINE_STROKE;
         case JDRUnit.IN:
           return IN_OUTLINE_STROKE;
         case JDRUnit.MM:
           return MM_OUTLINE_STROKE;
         case JDRUnit.CM:
           return CM_OUTLINE_STROKE;
         case JDRUnit.PT:
           return PT_OUTLINE_STROKE;
         default:
           return new BasicStroke((float)unit.fromBp(1.0), 
             BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
      }
   }

   private volatile String text;
   protected volatile JDRFont jdrFont;
   protected volatile Font font;

   private volatile JDRTransform jdrtransform;

   // LaTeX stuff

   /**
    * Associated LaTeX font.
    */
   public volatile LaTeXFont latexFont = new LaTeXFont();
   /**
    * Alternative LaTeX text.
    */
   public String latexText;

   /**
    * Vertical alignment for <code>\pgftext</code>.
    */
   protected int pgfValign;
   /**
    * Horizontal alignment for <code>\pgftext</code>.
    */
   protected int pgfHalign;

   /**
    * <code>\pgftext</code> horizontal alignment.
    */
   public static final int PGF_HALIGN_LEFT=0,
                           PGF_HALIGN_CENTRE=1,
                           PGF_HALIGN_RIGHT=2;
   /**
    * <code>\pgftext</code> vertical alignment.
    */
   public static final int PGF_VALIGN_TOP=0,
                           PGF_VALIGN_CENTRE=1,
                           PGF_VALIGN_BASE=2,
                           PGF_VALIGN_BOTTOM=3;

   private static JDRTextListener textListener = new JDRTextListener();

   private volatile JDRPaint textPaint;

   private volatile JDRPaint fillPaint = null;

   private volatile boolean isOutline = false;

   protected static final Stroke BP_OUTLINE_STROKE = 
      new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

   protected static final Stroke IN_OUTLINE_STROKE = 
      new BasicStroke((float)JDRUnit.in.fromBp(1.0),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

   protected static final Stroke MM_OUTLINE_STROKE = 
      new BasicStroke((float)JDRUnit.mm.fromBp(1.0),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

   protected static final Stroke CM_OUTLINE_STROKE = 
      new BasicStroke((float)JDRUnit.cm.fromBp(1.0),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

   protected static final Stroke PT_OUTLINE_STROKE = 
      new BasicStroke((float)JDRUnit.pt.fromBp(1.0),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
}
