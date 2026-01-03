// File          : JDRFont.java
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
import java.awt.Font;

import com.dickimawbooks.texjavahelplib.HelpFontSettings;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing font styles. This is based on the way LaTeX
 * deals with fonts. That is, the font style consists of the
 * following attributes:
 *<ul>
 *<li>Family : the name identifying the font family. 
 * This may be a generic name like "Serif" or a name such as
 * "Lucida Bright".
 * <p>
 * <li> Series : the font series or weight. This may be either bold
 * or medium.
 * <p>
 * <li> Shape : the font shape. This may be one of: upright,
 * emphasized, italic, slanted or small caps.
 * <p>
 * <li> Size: the font size.
 *</ul>
 * The LaTeX equivalents (<code>&#092;rmfamily</code> etc) are stored in 
 * {@link LaTeXFont} and the font size conversion (to declarations
 * such as <code>&#092;large</code>) is implemented by 
 * {@link LaTeXFontBase}.
 * @see LaTeXFont
 * @see LaTeXFontBase
 */
public class JDRFont implements Cloneable,Serializable
{
   /**
    * Creates a new font style.
    * @param fontFamily the font family (e.g. "Serif")
    * @param fontWeight the font weight (may be either
    * {@link #SERIES_MEDIUM} or {@link #SERIES_BOLD})
    * @param fontShape the font shape (may be one of:
    * {@link #SHAPE_UPRIGHT}, {@link #SHAPE_EM}, {@link #SHAPE_ITALIC},
    * {@link #SHAPE_SLANTED} or {@link #SHAPE_SC})
    * @param fontSize the font size
    */
   public JDRFont(String fontFamily, int fontWeight, int fontShape, JDRLength fontSize)
   {
      setSize(fontSize);
      setFamily(fontFamily);
      setWeight(fontWeight);
      setShape(fontShape);
   }

   /**
    * Creates default font style. (SanSerif family, medium weight,
    * upright shape and 10pt size.)
    */
   public JDRFont(JDRMessageDictionary msgSys)
   {
      family = "SansSerif";
      weight = 0;
      shape  = 0;
      size   = new JDRLength(msgSys, 10, JDRUnit.pt);
   }

   /**
    * Create a copy. 
    */ 
   public JDRFont(JDRFont font)
   {
      family = font.family;
      weight = font.weight;
      shape = font.shape;
      size = new JDRLength(font.size);
   }

   /**
    * Gets this style's font family.
    * @return the name of the font family for this style
    */
   public String getFamily()
   {
      return family;
   }

   /**
    * Gets this style's font weight.
    * @return the font weight for this style
    */
   public int getWeight()
   {
      return weight;
   }

   /**
    * Gets this style's font shape.
    * @return the font shape for this style
    */
   public int getShape()
   {
      return shape;
   }

   public int getJavaFontStyle()
   {
      int style = (weight == SERIES_MEDIUM ?
                Font.PLAIN : Font.BOLD);

      if (shape == SHAPE_ITALIC
       || shape == SHAPE_EM
       || shape == SHAPE_SLANTED)
      {
         style += Font.ITALIC;
      }

      return style;
   }

   /**
    * Gets this style's font size.
    * @return the font size for this style
    */
   public JDRLength getSize()
   {
      return size;
   }

   public double getSize(JDRUnit unit)
   {
      return size.getValue(unit);
   }

   public int getBpSize()
   {
      int bpSize = (int)Math.floor(getSize(JDRUnit.bp));

      return bpSize == 0 ? 1 : bpSize;
   }

   /**
    * Sets the font family for this style.
    * @param fontFamily the font family name
    */
   public void setFamily(String fontFamily)
   {
      family = fontFamily;
   }

   /**
    * Sets the font weight for this style.
    * @param fontWeight the font weight which must be one of:
    * {@link #SERIES_MEDIUM} or {@link #SERIES_BOLD}
    */
   public void setWeight(int fontWeight)
   {
      if (fontWeight < 0 || fontWeight > MAX_SERIES_ID)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.FONT_WEIGHT, fontWeight,
           getMessageSystem());
      }

      weight = fontWeight;
   }

   /**
    * Sets the font shape for this style.
    * @param fontShape the font shape which must be one of:
    * {@link #SHAPE_UPRIGHT}, {@link #SHAPE_EM}, {@link #SHAPE_ITALIC},
    * {@link #SHAPE_SLANTED} or {@link #SHAPE_SC}
    */
   public void setShape(int fontShape)
   {
      if (fontShape < 0 || fontShape > MAX_SHAPE_ID)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.FONT_SHAPE, fontShape,
           getMessageSystem());
      }

      shape = fontShape;
   }

   /**
    * Sets the font size for this style.
    * @param fontSize the font size
    */
   public void setSize(JDRLength fontSize)
   {
      if (fontSize.getValue() < 0)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.FONT_SIZE, fontSize.toString(),
           fontSize.getMessageSystem());
      }

      size = fontSize;
   }

   /**
    * Saves this font style in JDR format.
    * @see #read(JDRAJR jdr)
    */
   public void save(JDRAJR jdr)
      throws IOException
   {
      float version = jdr.getVersion();

      jdr.writeString(family);

      if (version > 1.6f)
      {
         jdr.writeByte((byte)shape);
      }
      else
      {
         jdr.writeByte((byte)
         (shape==SHAPE_UPRIGHT || shape==SHAPE_SC ? SHAPE_UPRIGHT : SHAPE_EM));
      }

      jdr.writeByte((byte)weight);

      if (version < 1.8f)
      {
         jdr.writeInt((int)size.getValue(JDRUnit.bp));
      }
      else
      {
         jdr.writeLength(size);
      }
   }

   /**
    * Reads a font style in JDR format.
    * @throws InvalidFormatException if the input stream is not
    * in the required format
    * @return the font style read from the input stream
    * @see #save(JDRAJR)
    */
   public static JDRFont read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      JDRFont font = new JDRFont(jdr.getMessageSystem());

      font.setFamily(jdr.readString(
         InvalidFormatException.FONT_FAMILY));

      font.setShape((int)jdr.readByte(
         InvalidFormatException.FONT_SHAPE, 0, MAX_SHAPE_ID, true, true));

      font.setWeight((int)jdr.readByte(
         InvalidFormatException.FONT_WEIGHT, 0, MAX_SERIES_ID, true, true));

      if (version < 1.8f)
      {
         font.setSize(new JDRLength(jdr.getMessageSystem(),
            jdr.readIntGe(InvalidFormatException.FONT_SIZE, 0), JDRUnit.bp));
      }
      else
      {
         font.setSize(jdr.readNonNegLength(
            InvalidFormatException.FONT_SIZE));
      }

      return font;
   }

   /**
    * Gets this font style in SVG format.
    * @return SVG tags specifying this font style
    */
   public String svg()
   {
      String cssfamily = HelpFontSettings.getFontCssName(family);

      return "font-family=\""+cssfamily+"\""
       + " font-weight=\""
       + (weight == SERIES_MEDIUM ? "normal" : "bold")
       + "\""
       + " font-style=\""
       + (shape == SHAPE_UPRIGHT ? "normal" : "italic")
       + "\""
       + " font-size=\"" + SVG.length(size) + "\"";
   }

   /**
    * Not yet implemented.
    * @return empty string
    */
   public String saveEPS()
   {
      // not yet implemented

      return "";
   }

   /**
    * Gets a copy of this style.
    * @return a copy of this font style
    */
   public Object clone()
   {
      return new JDRFont(this);
   }

   /**
    * Sets this font style to be the same as another font style.
    * @param font the other font style
    */
   public void makeEqual(JDRFont font)
   {
      family = font.family;
      weight = font.weight;
      shape  = font.shape;
      size.makeEqual(font.size);
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (!(obj instanceof JDRFont)) return false;

      JDRFont f = (JDRFont)obj;
      if (!family.equals(f.family)) return false;
      if (weight != f.weight) return false;
      if (shape != f.shape) return false;
      if (!size.equals(f.size)) return false;

      return true;
   }

   public String info()
   {
      return "family="+family+",weight="+weight+",shape="+shape
      +",size="+size;
   }

   public JDRMessageDictionary getMessageSystem()
   {
      return size.getMessageSystem();
   }

   public String toString()
   {
      return String.format("%s[family=%s,weight=%s,shape=%s,size=%s]", getClass().getSimpleName(),
         family, weight, shape, size);
   }

   /**
    * Indicates medium font weight.
    */
   public static final int SERIES_MEDIUM=0;
   /**
    * Indicates bold font weight.
    */
   public static final int SERIES_BOLD=1;

   public static final int MAX_SERIES_ID = SERIES_BOLD;

   /**
    * Indicates upright font shape.
    */
   public static final int SHAPE_UPRIGHT = 0;
   /**
    * Indicates emphasized font shape.
    */
   public static final int SHAPE_EM      = 1;
   /**
    * Indicates italic font shape.
    */
   public static final int SHAPE_ITALIC  = 2;
   /**
    * Indicates slanted font shape.
    */
   public static final int SHAPE_SLANTED = 3;
   /**
    * Indicates small caps font shape.
    */
   public static final int SHAPE_SC      = 4;

   public static final int MAX_SHAPE_ID = SHAPE_SC;

   private volatile String family="SansSerif";
   private volatile int weight=0, shape=0;

   private volatile JDRLength size;
}

