// File          : JDRTextPathStroke.java
// Creation Date : 10 July 2009
// Author        : Nicola L.C. Talbot
//                 (with bits adapted from Jerry Huxtable's TextStroke.java)

package com.dickimawbooks.jdr;

import java.io.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing stroke made of text.
 * This code was adapted from Jerry Huxtable's example TextStroke.java
 * code (available from http://www.jhlabs.com/java/java2d/strokes/
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0)
 */

public class JDRTextPathStroke implements JDRStroke
{
   /**
     * Creates a new stroke using given text and font.
     * @param text the text to follow the required path
     * @param font the font
    */
   public JDRTextPathStroke(CanvasGraphics cg, String text, Font font)
   {
      setCanvasGraphics(cg);
      this.text = text;
      latexText = null;

      halign = LEFT;
      valign = BASE;

      jdrFont = new JDRFont(font.getName(), 
        font.isBold() ?  JDRFont.SERIES_BOLD : JDRFont.SERIES_MEDIUM,
        font.isItalic() ? JDRFont.SHAPE_EM : JDRFont.SHAPE_UPRIGHT,
        new JDRLength(cg.getMessageSystem(), font.getSize(), JDRUnit.bp));


      this.font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());

      matrix = new double[6];

      reset();
   }

   /**
    * Creates a new stroke using the given text attributes.
    * (The transformation matrix isn't used.)
    * @param jdrtext the text attributes
    */
   public JDRTextPathStroke(JDRText jdrtext)
   {
      setCanvasGraphics(jdrtext.getCanvasGraphics());
      text = jdrtext.getText();
      setLaTeXText(jdrtext.getLaTeXText());

      jdrFont = (JDRFont)jdrtext.getJDRFont().clone();
      font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());
      latexFont = (LaTeXFont)jdrtext.latexFont.clone();
      halign = jdrtext.pgfHalign;
      valign = jdrtext.pgfValign;

      matrix = jdrtext.getTransformation(null);

      matrix[4]=0;
      matrix[5]=0;
   }

   public JDRTextPathStroke(CanvasGraphics cg)
   {
      this(cg, "", "", new JDRFont(cg.getMessageSystem()),
           JDRText.PGF_HALIGN_LEFT, 
           JDRText.PGF_VALIGN_BASE, new LaTeXFont());
   }

   private JDRTextPathStroke()
   {
   }

   protected JDRTextPathStroke(CanvasGraphics cg,
      String text, String ltxtext, 
      JDRFont jdrfont, int hAlign, int vAlign, LaTeXFont ltxfont)
   {
      setCanvasGraphics(cg);
      this.text = text;
      setLaTeXText(ltxtext);
      jdrFont = jdrfont;
      latexFont = ltxfont;
      halign = hAlign;
      valign = vAlign;

      font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());

      matrix = new double[6];

      matrix[0] = 1;
      matrix[1] = 0;
      matrix[2] = 0;
      matrix[3] = 1;
      matrix[4] = 0;
      matrix[5] = 0;
   }

   public Object clone()
   {
      JDRTextPathStroke stroke = new JDRTextPathStroke(getCanvasGraphics(),
          text, font);

      stroke.latexText = latexText;
      stroke.latexFont = (LaTeXFont)latexFont.clone();
      stroke.halign = halign;
      stroke.valign = valign;

      for (int i = 0; i < 6; i++)
      {
         stroke.matrix[i] = matrix[i];
      }

      stroke.setLeftDelim(getLeftDelim());
      stroke.setRightDelim(getRightDelim());

      stroke.svgID = svgID;

      return stroke;
   }

   /**
    * Gets the text attributes.
    * @return the text attributes
    */
   public JDRText getJDRText()
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRText jdrtext = new JDRText(cg);

      jdrtext.jdrFont = (JDRFont)jdrFont.clone();
      jdrtext.font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());
      jdrtext.latexFont = (LaTeXFont)latexFont.clone();

      jdrtext.setText(text, latexText);

      jdrtext.pgfHalign = halign;
      jdrtext.pgfValign = valign;

      jdrtext.setTransformation(matrix);

      return jdrtext;
   }

   public void setFont(String name, int series, int shape, JDRLength size)
   {
      setFontFamily(name);
      setFontSeries(series);
      setFontShape(shape);
      setFontSize(size);
   }

   /**
    * Sets the font family.
    * @param name the Java font family name
    */
   public void setFontFamily(String name)
   {
      jdrFont.setFamily(name);
      font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());
   }

   /**
    * Sets the font series.
    * @param series the font series, which must be one
    * of: {@link JDRFont#SERIES_MEDIUM} or {@link JDRFont#SERIES_BOLD}
    */
   public void setFontSeries(int series)
   {
      jdrFont.setWeight(series);
      font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());
   }

   /**
    * Sets the font shape.
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
   }

   /**
    * Sets the font size.
    * @param size the font size. Can't be
    * negative.
    */
   public void setFontSize(JDRLength size)
   {
      jdrFont.setSize(size);
      font = new Font(jdrFont.getFamily(), getFontWeight(),
         jdrFont.getBpSize());
   }

   /**
    * Gets the Java font family name.
    * @return font family name
    */
   public String getFontFamily()
   {
      return jdrFont.getFamily();
   }

   /**
    * Gets the font series.
    * @return font series
    */
   public int getFontSeries()
   {
      return jdrFont.getWeight();
   }

   /**
    * Gets the font shape.
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
    * Gets the font size.
    * @return font size 
    */
   public JDRLength getFontSize()
   {
      return jdrFont.getSize();
   }

   /**
    * Gets the Java font.
    * @return font
    */
   public Font getFont()
   {
      return font;
   }

   /**
    * Gets the JDR font.
    * @return font
    */
   public JDRFont getJDRFont()
   {
      return jdrFont;
   }

   /**
    * Gets the text.
    * @return the text
    */
   public String getText()
   {
      return text;
   }

   /**
    * Gets the alternative LaTeX text.
    * @return the alternative LaTeX text
    */
   public String getLaTeXText()
   {
      return latexText;
   }

   public void setLaTeXFont(LaTeXFont ltxFont)
   {
      latexFont = ltxFont;
   }

   public LaTeXFont getLaTeXFont()
   {
      return latexFont;
   }

   public void setLaTeXFont(String family, String size, 
      String series, String shape)
   {
      latexFont.setFamily(family);
      latexFont.setWeight(series);
      latexFont.setShape(shape);
      latexFont.setSize(size);
   }

   public void setLaTeXFamily(String name)
   {
      latexFont.setFamily(name);
   }

   public void setLaTeXSeries(String series)
   {
      latexFont.setWeight(series);
   }

   public void setLaTeXShape(String shape)
   {
      latexFont.setShape(shape);
   }

   public void setLaTeXSize(String size)
   {
      latexFont.setSize(size);
   }

   public String getLaTeXFamily()
   {
      return latexFont.getFamily();
   }

   public String getLaTeXSeries()
   {
      return latexFont.getWeight();
   }

   public String getLaTeXSize()
   {
      return latexFont.getSize();
   }

   public String getLaTeXShape()
   {
      return latexFont.getShape();
   }

   /**
    * Sets the text.
    * @param newText the new text
    */
   public void setText(String newText)
   {
      text = newText.replaceAll("[\t\r\n]", " ");
      setLaTeXText(text);
   }

   /**
    * Sets the text and LaTeX alternative text.
    * @param newText the text
    * @param newLaTeXText the alternative text
    */
   public void setText(String newText, String newLaTeXText)
   {
      text = newText.replaceAll("[\t\r\n]", " ");
      setLaTeXText(newLaTeXText);
   }

   /**
    * Sets LaTeX alternative text.
    * @param newLaTeXText the alternative text
    */
   public void setLaTeXText(String newLaTeXText)
   {
      latexText = (newLaTeXText == null ? newLaTeXText : 
                   (newLaTeXText.isEmpty() ? null :
                   newLaTeXText.replaceAll("[\t\r\n]", " ")));
   }

   /**
    * Sets the horizontal alignment
    * @param hAlign the horizontal alignment, which must be one of:
    * {@link #LEFT}, {@link #CENTER} or {@link #RIGHT}
    * @see #getHAlign()
    * @see #setVAlign(int)
    */
   public void setHAlign(int hAlign)
   {
      if (hAlign < 0 || hAlign > 2)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.HALIGN, hAlign,
           getCanvasGraphics());
      }

      halign = hAlign;
   }

   /**
    * Gets the horizontal alignment
    * @return the horizontal alignment
    * @see #setHAlign(int)
    * @see #getVAlign()
    */
   public int getHAlign()
   {
      return halign;
   }

   /**
    * Sets the vertical alignment
    * @param vAlign the vertical alignment, which must be one of:
    * {@link #TOP}, {@link #MIDDLE}, {@link #BASE} or 
    * {@link #BOTTOM}
    * @see #getVAlign()
    * @see #setHAlign(int)
    */
   public void setVAlign(int vAlign)
   {
      if (vAlign < 0 || vAlign > 3)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.VALIGN, vAlign,
           getCanvasGraphics());
      }

      valign = vAlign;
   }

   /**
    * Gets the vertical alignment
    * @return the vertical alignment
    * @see #setVAlign(int)
    * @see #getHAlign()
    */
   public int getVAlign()
   {
      return valign;
   }

   public void saveEPS(JDRShape path, PrintWriter out)
   {
   }

   public static JDRTextPathStroke read(JDRAJR jdr)
     throws InvalidFormatException
   {
      float version = jdr.getVersion();

      // font specs
      JDRFont jf = JDRFont.read(jdr);

      // transformation matrix

      double[] mtx = jdr.readTransform(
         InvalidFormatException.TEXT_PATH_TRANSFORM);

      // LaTeX font specs

      LaTeXFont ltxfont;

      String ltxText = null;
      int hAlign = LEFT;
      int vAlign = RIGHT;

      int delimL = (int)'|';
      int delimR = (int)'|';

      if (jdr.readBoolean(InvalidFormatException.TEXT_PATH_LATEX_FLAG))
      {
         ltxfont = LaTeXFont.read(jdr);

         hAlign = (int)jdr.readByte(
            InvalidFormatException.TEXT_PATH_HALIGN);

         if (hAlign < 0 || hAlign > RIGHT)
         {
            throw new InvalidValueException(
              InvalidFormatException.TEXT_PATH_HALIGN, hAlign, jdr);
         }

         vAlign = (int)jdr.readByte(
            InvalidFormatException.TEXT_PATH_VALIGN);

         if (vAlign < 0 || vAlign > BOTTOM)
         {
            throw new InvalidValueException(
               InvalidFormatException.TEXT_PATH_VALIGN, vAlign, jdr);
         }

         ltxText = jdr.readString(
            InvalidFormatException.TEXT_PATH_LATEX_TEXT);

         if (version >= 2.1f)
         {
            delimL = jdr.readInt(
               InvalidFormatException.TEXT_PATH_DELIM_L);
            delimR = jdr.readInt(
               InvalidFormatException.TEXT_PATH_DELIM_R);
         }
         else if (version >= 1.8f)
         {
            delimL = jdr.readChar(
               InvalidFormatException.TEXT_PATH_DELIM_L);
            delimR = jdr.readChar(
               InvalidFormatException.TEXT_PATH_DELIM_R);
         }
      }
      else
      {
         ltxfont = new LaTeXFont();
      }

      // text

      String string = jdr.readString(
         InvalidFormatException.TEXT_PATH_TEXT);

      JDRTextPathStroke tps = new JDRTextPathStroke(
         jdr.getCanvasGraphics(), string, ltxText, jf, 
         hAlign, vAlign, ltxfont);

      tps.setLeftDelim(delimL);
      tps.setRightDelim(delimR);

      tps.setTransformation(mtx);

      return tps;
   }

   public void save(JDRAJR jdr)
      throws IOException
   {
      float version = jdr.getVersion();

      // font specs
      jdrFont.save(jdr);

      // transformation matrix

      jdr.writeTransform(matrix);

      // LaTeX stuff
      jdr.writeBoolean(true);
      latexFont.save(jdr);

      jdr.writeByte((byte)halign);
      jdr.writeByte((byte)valign);

      jdr.writeString(text.equals(latexText) ? null : latexText);

      if (version >= 2.1f)
      {
         jdr.writeInt(getLeftDelim());
         jdr.writeInt(getRightDelim());
      }
      else if (version >= 1.8f)
      {
         jdr.writeChar((char)getLeftDelim());
         jdr.writeChar((char)getRightDelim());
      }
      else if (getLeftDelim() != '|'
             &&getRightDelim() != '|')
      {
         jdr.warningWithFallback(
           "warning.save_unsupported_text_path_delim",
           "Text path format delimiters not supported by JDR/AJR version {0}",
            version);
      }

      // text

      jdr.writeString(text);
   }

   public String getID()
   {
      if (svgID == null)
      {
         int id = ++max_id;

         svgID = "textpathstroke-"+id;
      }

      return svgID;
   }

   @Override
   public void writeSVGdefs(SVG svg, JDRShape shape) throws IOException
   {
      String id = getID();

      if (svg.addReferenceID(id))
      {
         svg.println("<path id=\""+id+"\" fill=\"transparent\" d=\"");
         svg.saveStoragePathData(shape.getGeneralPath());
         svg.println("\"/>");
      }
   }

   public Area getStorageStrokedArea(JDRShape path)
   {
      return new Area(getStorageStrokedPath(path));
   }

   public Shape getStorageStrokedPath(JDRShape path)
   {
      return createStrokedShape(path.getGeneralPath(),
        getCanvasGraphics().getStorageUnit());
   }

   public Area getComponentStrokedArea(JDRShape path)
   {
      return new Area(getComponentStrokedPath(path));
   }

   public Shape getComponentStrokedPath(JDRShape path)
   {
      Shape shape = createStrokedShape(path.getBpGeneralPath(), JDRUnit.bp);

      double factorX = getCanvasGraphics().bpToComponentX(1.0);
      double factorY = getCanvasGraphics().bpToComponentY(1.0);

      AffineTransform af = AffineTransform.getScaleInstance(factorX, factorY);

      return af.createTransformedShape(shape);
   }

   public Area getBpStrokedArea(JDRShape path)
   {
      return new Area(getBpStrokedPath(path));
   }

   public Shape getBpStrokedPath(JDRShape path)
   {
      return createStrokedShape(path.getBpGeneralPath(), JDRUnit.bp);
   }

   public void drawStoragePath(JDRShape path)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Path2D genPath = path.getGeneralPath();

      cg.draw(genPath);
      cg.fill(genPath);
   }

   public void drawStoragePath(JDRShape shape, Shape path)
   {
      CanvasGraphics cg = getCanvasGraphics();

      cg.fill(createStrokedShape(path, cg.getStorageUnit()));
   }

   public void printPath(Graphics2D g2, JDRShape shape, Shape bpGeneralPath)
   {
      g2.fill(createStrokedShape(bpGeneralPath, JDRUnit.bp));
   }

   public void setWindingRule(int rule)
   {
   }

   public int getWindingRule()
   {
      return Path2D.WIND_NON_ZERO;
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "TextPathStroke:"+eol;
      str += "text: "+text+eol;
      str += "LaTeX equivalent: "+latexText+eol;
      str += "font: "+jdrFont.info()+eol;
      str += "LaTeX font: "+latexFont.info()+eol;
      str += "HAlign: "+halign+eol;
      str += "VAlign: "+valign+eol;

      str += "Matrix: [";

      for (int i = 0; i < 6; i++)
      {
         if (i != 0) str += ",";

         str += matrix[i];
      }

      str += "]";

      return str;
   }

   /**
    * Creates outline of text along the path.
    * This code was adapted from Jerry Huxtable's TextStroke.java
    * code.
    * @param shape the shape along which the text should go
    * @return outline of text along the given shape
    */
   public Shape createStrokedShape(Shape shape, JDRUnit unit)
   {
      FontRenderContext frc = new FontRenderContext(null, true, true);

      Font transformedFont = font.deriveFont(
         new AffineTransform(matrix));
      GlyphVector glyphVector
         = transformedFont.createGlyphVector(frc, text);

      TextLayout layout = new TextLayout(text, transformedFont, frc);

      double storageToBp = canvasGraphics.storageToBp(1.0);

      double unitToBp = unit.toBp(1.0);

      // Convert to bp to perform calculations since font size is in
      // PostScript points

      double descent = (double)layout.getDescent();
      double ascent = (double)layout.getAscent();

      Rectangle2D bounds = layout.getBounds();

      double xoffset = (double)(storageToBp*matrix[4]);
      double yoffset = (double)(storageToBp*matrix[5]);

      switch (valign)
      {
         case TOP:
            yoffset = -ascent;
         break;

         case MIDDLE:
            yoffset = descent-(double)bounds.getHeight()*0.5f;
         break;

         case BOTTOM:
           yoffset = descent;
         break;
      }

      AffineTransform unitToBpAf 
        = AffineTransform.getScaleInstance(unitToBp, unitToBp);

      PathIterator it = new FlatteningPathIterator(
        shape.getPathIterator(unitToBpAf), FLATNESS);

      if (halign != LEFT)
      {
         double pathLength = measurePathLength(it);

         it = new FlatteningPathIterator(
            shape.getPathIterator(unitToBpAf), FLATNESS);

         if (halign == CENTER)
         {
            xoffset = (double)(pathLength-bounds.getWidth())*0.5f;
         }
         else
         {
            xoffset = (double)(pathLength-bounds.getWidth());
         }
      }

      Path2D result = new Path2D.Double();

      double points[] = new double[6];
      double moveX = 0;
      double moveY = 0;
      double lastX = 0;
      double lastY = 0;
      double thisX = 0;
      double thisY = 0;

      int type = 0;
      boolean first = false;
      double next = 0;
      int currentChar = 0;
      int length = glyphVector.getNumGlyphs();

      if (length == 0) return result;

      double nextAdvance = 0;

      while (currentChar < length && !it.isDone())
      {
         type = it.currentSegment(points);

         switch (type)
         {
            case PathIterator.SEG_MOVETO:
               moveX = lastX = points[0];
               moveY = lastY = points[1];
               result.moveTo(moveX, moveY);
               first = true;
               nextAdvance = glyphVector.getGlyphMetrics(currentChar)
                 .getAdvance() * 0.5f;
               next = nextAdvance+xoffset;
            break;

            case PathIterator.SEG_CLOSE:
               points[0] = moveX;
               points[1] = moveY;
            case PathIterator.SEG_LINETO:
               thisX = points[0];
               thisY = points[1];
               double dx = thisX-lastX;
               double dy = thisY-lastY;
               double distance = Math.sqrt(dx*dx+dy*dy);

               if (distance >= next)
               {
                  double r = 1.0/distance;
                  double angle = Math.atan2(dy,dx);

                  while (currentChar < length && distance >= next)
                  {
                     Shape glyph = glyphVector.getGlyphOutline(currentChar);
                     Point2D p = glyphVector.getGlyphPosition(currentChar);
                     double px = p.getX();
                     double py = p.getY()+yoffset;
                     double x = lastX + next*dx*r;
                     double y = lastY + next*dy*r;

                     double advance = nextAdvance;
                     nextAdvance = currentChar < length-1 ?
                        glyphVector.getGlyphMetrics(currentChar+1).getAdvance() * 0.5f :
                        0.0f;

                     af.setToTranslation(x, y);
                     af.rotate(angle);
                     af.translate(-px-advance, -py);

                     result.append(af.createTransformedShape(glyph),false);

                     currentChar++;

                     next += advance+nextAdvance;
                  }
               }

               next -= distance;
               first = false;
               lastX = thisX;
               lastY = thisY;
            break;
         }

         it.next();
      }

      if (unit.getID() != JDRUnit.BP)
      {
         double bpToUnit = unit.fromBp(1.0);

         return AffineTransform.getScaleInstance(bpToUnit, bpToUnit)
           .createTransformedShape(result);
      }

      return result;
   }

   /**
    * Measure the approximate length of the given shape.
    * This code was adapted from Jerry Huxtable's TextStroke.java
    * code.
   */
   public static double measurePathLength(Shape shape)
   {
       return measurePathLength(new FlatteningPathIterator(
        shape.getPathIterator(null), FLATNESS));
   }

   public static double measurePathLength(PathIterator it)
   {
      double points[] = new double[6];
      double moveX = 0;
      double moveY = 0;
      double lastX = 0;
      double lastY = 0;
      double thisX = 0;
      double thisY = 0;

      int type = 0;
      double total = 0;

      while (!it.isDone())
      {
         type = it.currentSegment(points);

         switch (type)
         {
            case PathIterator.SEG_MOVETO:
               moveX = lastX = points[0];
               moveY = lastY = points[1];
            break;

            case PathIterator.SEG_CLOSE:
               points[0] = moveX;
               points[1] = moveY;
            case PathIterator.SEG_LINETO:
               thisX = points[0];
               thisY = points[1];
               double dx = thisX-lastX;
               double dy = thisY-lastY;
               total += Math.sqrt(dx*dx+dy*dy);
               lastX = thisX;
               lastY = thisY;
            break;
         }

         it.next();
      }

      return total;
   }

   public JDRGroup split(JDRTextPath textPath)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Shape shape = textPath.getBpGeneralPath();

      JDRGroup group = new JDRGroup(cg);

      if (textPath.description.isEmpty())
      {
         group.description = text;
      }
      else
      {
         group.description = textPath.description;
      }

      double[] mtx = new double[6];

      FontRenderContext frc = new FontRenderContext(null, true, true);

      Font transformedFont = getFont().deriveFont(
         new AffineTransform(matrix));
      GlyphVector glyphVector
         = transformedFont.createGlyphVector(frc, text);

      TextLayout layout = new TextLayout(text, transformedFont, frc);

      double descent = layout.getDescent();
      double ascent = layout.getAscent();
      Rectangle2D bounds = layout.getBounds();

      double yoffset = (double)matrix[5];
      double xoffset = (double)matrix[4];

      switch (valign)
      {
         case TOP:
            yoffset = -ascent;
         break;

         case MIDDLE:
            yoffset = descent-(double)bounds.getHeight()*0.5f;
         break;

         case BOTTOM:
           yoffset = descent;
         break;
      }

      if (halign != LEFT)
      {
         double pathLength = measurePathLength(shape);

         if (halign == CENTER)
         {
            xoffset = (double)(pathLength-bounds.getWidth())*0.5f;
         }
         else
         {
            xoffset = (double)(pathLength-bounds.getWidth());
         }
      }

      PathIterator it = new FlatteningPathIterator(
        shape.getPathIterator(null), FLATNESS);


      double points[] = new double[6];
      double moveX = 0;
      double moveY = 0;
      double lastX = 0;
      double lastY = 0;
      double thisX = 0;
      double thisY = 0;

      int type = 0;
      boolean first = false;
      double next = 0;
      int currentChar = 0;
      int length = glyphVector.getNumGlyphs();

      if (length == 0) return group;

      double nextAdvance = 0;

      CanvasGraphics bpCG = new CanvasGraphics();
      StringBuilder builder = new StringBuilder();

      while (currentChar < length && !it.isDone())
      {
         type = it.currentSegment(points);

         switch (type)
         {
            case PathIterator.SEG_MOVETO:
               moveX = lastX = points[0];
               moveY = lastY = points[1];
               first = true;
               nextAdvance = glyphVector.getGlyphMetrics(currentChar)
                 .getAdvance() * 0.5f;
               next = nextAdvance+xoffset;
            break;

            case PathIterator.SEG_CLOSE:
               points[0] = moveX;
               points[1] = moveY;
            case PathIterator.SEG_LINETO:
               thisX = points[0];
               thisY = points[1];
               double dx = thisX-lastX;
               double dy = thisY-lastY;
               double distance = Math.sqrt(dx*dx+dy*dy);

               if (distance >= next)
               {
                  double r = 1.0/distance;
                  double angle = Math.atan2(dy,dx);

                  while (currentChar < length && distance >= next)
                  {
                     Shape glyph = glyphVector.getGlyphOutline(currentChar);
                     Point2D p = glyphVector.getGlyphPosition(currentChar);
                     double x = lastX + next*dx*r;
                     double y = lastY + next*dy*r;

                     double advance = nextAdvance;
                     nextAdvance = currentChar < length-1 ?
                        glyphVector.getGlyphMetrics(currentChar+1).getAdvance() * 0.5f :
                        0.0f;

                     int thisChar = text.codePointAt(currentChar);
                     builder.appendCodePoint(thisChar);

                     if (!Character.isWhitespace(thisChar)
                      && glyphVector.getGlyphMetrics(currentChar)
                                    .isStandard())
                     {
                        JDRText textArea
                           = new JDRText(bpCG, builder.toString());

                        af.setToIdentity();
                        af.translate(x, y-yoffset);
                        af.rotate(angle);
                        af.translate(-advance, 0);
                        af.concatenate(
                           new AffineTransform(matrix[0], matrix[1],
                              matrix[2], matrix[3], 0, 0));

                        af.getMatrix(mtx);
                        textArea.setTransformation(mtx);

                        textArea.setLaTeXFamily(getLaTeXFamily());
                        textArea.setLaTeXSize(getLaTeXSize());
                        textArea.setLaTeXSeries(getLaTeXSeries());
                        textArea.setLaTeXShape(getLaTeXShape());
                        textArea.setTextPaint(textPath.getLinePaint());

                        textArea.setFont(getFontFamily(),
                                         getFontSeries(),
                                         getFontShape(),
                                         getFontSize());
                        textArea.setVAlign(getVAlign());
                        textArea.setHAlign(getHAlign());

                        textArea.applyCanvasGraphics(cg);
                        textArea.setOutlineMode(textPath.isOutline());

                        group.add(textArea);
                     }

                     currentChar += Character.charCount(thisChar);
                     builder.setLength(0);

                     next += advance+nextAdvance;
                  }
               }

               next -= distance;
               first = false;
               lastX = thisX;
               lastY = thisY;
            break;
         }

         it.next();
      }

      group.setSelected(textPath.isSelected());
      return group;
   }

   public double[] getTransformation(double[] mtx)
   {
      if (mtx == null)
      {
         mtx = new double[6];
      }

      for (int i = 0; i < 6; i++)
      {
         mtx[i] = matrix[i];
      }

      return mtx;
   }

   public void setTransformation(double[] mtx)
   {
      matrix = mtx;
   }

   public void reset()
   {
      for (int i = 0; i < matrix.length; i++)
      {
         matrix[i] = (i == 0 || i == 3 ? 1.0 : 0.0);
      }
   }

   public JDRPathStyleListener getPathStyleListener()
   {
      return pathStyleListener;
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
         double scale = oldUnit.toUnit(1.0, newUnit);

         // only the translation elements should be adjusted
         matrix[4] *= scale;
         matrix[5] *= scale;
      }

      setCanvasGraphics(cg);
   }

   public void savePgf(TeX tex, JDRPaint paint, JDRShape path)
     throws IOException
   {
      tex.println(getLaTeXFont().tex());

      String latexText = getLaTeXText();

      if (latexText == null || latexText.isEmpty())
      {
         latexText = getText();
      }

      String align = null;

      switch (getHAlign())
      {
         case LEFT:
           align = "left";
         break;
         case CENTER:
           align = "center";
         break;
         case RIGHT:
           align = "right";
         break;
      }

      if (align != null)
      {
         tex.println("\\pgfset{decoration/text align="+align+"}");
      }

      int leftDelim = getLeftDelim();
      int rightDelim = getRightDelim();

      String leftDelimStr;

      if (leftDelim == 0)
      {
         if (rightDelim == 0)
         {
            leftDelimStr = "|";
         }
         else
         {
            leftDelimStr = new String(Character.toChars(rightDelim));
         }
      }
      else
      {
         leftDelimStr = new String(Character.toChars(leftDelim));
      }

      String rightDelimStr;

      if (rightDelim == 0)
      {
         rightDelimStr = leftDelimStr;
      }
      else
      {
         rightDelimStr = new String(Character.toChars(rightDelim));
      }

      tex.println("\\pgfset{decoration/text format delimiters={"
        +leftDelimStr+"}{"+rightDelimStr+"}}");

      tex.println("\\pgfset{decoration/text={"+leftDelimStr

           + paint.pgf(null)+rightDelimStr

           + latexText+"}}");

      tex.print("\\pgfdecoratepath{text along path}{");

      path.savePgfPath(tex);

      tex.println("}");

   }

   public void setLeftDelim(int delim)
   {
      delimL = delim;
   }

   public void setRightDelim(int delim)
   {
      delimR = delim;
   }

   public int getLeftDelim()
   {
      return delimL;
   }

   public int getRightDelim()
   {
      return delimR;
   }

   public String toString()
   {
      return String.format("%s[text=%s,font=%s,jdrfont=%s,halign=%d,valign=%d]", getClass().getSimpleName(),
       text, font, jdrFont, halign, valign);
   }

   private CanvasGraphics canvasGraphics;

   private String text;
   private Font font;
   private JDRFont jdrFont;
   private int halign, valign;

   /**
    * Associated LaTeX font
    */
   public LaTeXFont latexFont = new LaTeXFont();

   /**
    * Associated LaTeX text
    */
   public String latexText;

   /**
    * pgf text path decoration delimiters
    */
   private int delimL = (int)'|';
   private int delimR = (int)'|';

   private AffineTransform af = new AffineTransform();

   /**
    * Horizontal alignment. (Before transformation)
    */
   public static final int LEFT=0, CENTER=1, RIGHT=2;

   /**
    * Vertical alignment. (Before transformation)
    */
   public static final int TOP=0, MIDDLE=1, BASE=2, BOTTOM=3;

   private static final float FLATNESS = 1.0f;

   private double[] matrix;

   private String svgID = null;

   private static int max_id=0;

   private static JDRPathStyleListener pathStyleListener
      = new JDRTextPathStyleListener();
}
