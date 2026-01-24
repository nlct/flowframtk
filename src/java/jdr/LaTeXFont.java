// File          : LaTeXFont.java
// Creation Date : 12 January 2007
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
import java.util.*;
import java.util.regex.Pattern;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing LaTeX font styles. The font style consists of the
 * following attributes:
 *<ul>
 *<li>Family : the LaTeX declaration that sets the font family. For
 * example: 
 * <code>&#092;rmfamily</code>, <code>&#092;sffamily</code> or 
 * <code>&#092;ttfamily</code>. The actual font used in the LaTeX document
 * will depend on which fonts have been loaded. By default this will
 * be Donald Knuth's computer modern fonts, but may be changed
 * by loading another package, such as mathptmx. Therefore the
 * text viewed in FlowframTk will not look exactly the same as it
 * will look when exported to a LaTeX document, and in some cases
 * may be significantly different.
 * <p>
 * <li> Series : the LaTeX declaration that sets the font series.
 * For example: <code>&#092;bfseries</code> or <code>&#092;mdseries</code>.
 * <p>
 * <li> Shape : the LaTeX declaration that sets the font shape.
 * For example: <code>&#092;itshape</code>, <code>&#092;upshape</code>, 
 * <code>&#092;slshape</code>, <code>&#092;em</code> or <code>&#092;scshape</code>.
 * <p>
 * <li> Size: the LaTeX declaration that sets the font size.
 * For example: <code>&#092;large</code>.
 *</ul>
 * The conversion from a font size in points to one of the LaTeX
 * font size declarations (such as <code>&#092;large</code>) is
 * implemented by {@link LaTeXFontBase}.
 * @see JDRFont
 * @see LaTeXFontBase
 */
public class LaTeXFont implements Cloneable,Serializable
{
   /**
    * Creates a new LaTeX font style.
    * @param fontFamily the LaTeX declaration to set the font
    * family (e.g. "&#092;sffamily")
    * @param fontWeight the LaTeX declaration to set the font
    * weight (e.g. "&#092;bfseries")
    * @param fontShape the LaTeX declaration to set the font
    * shape (e.g. "&#092;em")
    * @param fontSize the LaTeX declaration to set the font size
    * (e.g. "&#092;large"
    * @see #LaTeXFont()
    */
   public LaTeXFont(String fontFamily, String fontWeight,
      String fontShape, String fontSize)
   {
      setFamily(fontFamily);
      setWeight(fontWeight);
      setShape(fontShape);
      setSize(fontSize);
   }

   /**
    * Creates default LaTeX font style. The font family is set to
    * "&#092;rmfamily", the weight is set to "&#092;mdseries",
    * the shape is set to "&#092;upshape" and the font size is
    * set to "&#092;normalsize".
    * @see #LaTeXFont(String,String,String,String)
    */
   public LaTeXFont()
   {
      family = "\\rmfamily";
      weight = "\\mdseries";
      shape  = "\\upshape";
      size   = "\\normalsize";
   }

   /**
    * Create a copy. 
    */ 
   public LaTeXFont(LaTeXFont font)
   {
      family = font.family;
      weight = font.weight;
      shape = font.shape;
      size = font.size;
   }

   public static LaTeXFont createFor(LaTeXFontBase lfb, JDRFont jdrFont)
   {
      LaTeXFont lf = new LaTeXFont();

      lf.family = fromJavaFamily(jdrFont.getFamily());
      lf.size = lfb.getLaTeXCmd(jdrFont.getSize());

      switch (jdrFont.getShape())
      {
         case JDRFont.SHAPE_UPRIGHT:
           lf.shape = "\\upshape";
         break;
         case JDRFont.SHAPE_EM:
           lf.shape = "\\em";
         break;
         case JDRFont.SHAPE_ITALIC:
           lf.shape = "\\itshape";
         break;
         case JDRFont.SHAPE_SLANTED:
           lf.shape = "\\slshape";
         break;
         case JDRFont.SHAPE_SC:
           lf.shape = "\\scshape";
         break;
      }

      switch (jdrFont.getWeight())
      {
         case JDRFont.SERIES_MEDIUM:
            lf.weight = "\\mdseries";
         break;
         case JDRFont.SERIES_BOLD:
            lf.weight = "\\bfseries";
         break;
      }

      return lf;
   }

   /**
    * Gets the font family.
    * @return the LaTeX declaration that sets the font family
    */
   public String getFamily()
   {
      return family;
   }

   /**
    * Gets the font weight.
    * @return the LaTeX declaration that sets the font weight
    */
   public String getWeight()
   {
      return weight;
   }

   /**
    * Gets the font shape.
    * @return the LaTeX declaration that sets the font shape
    */
   public String getShape()
   {
      return shape;
   }

   /**
    * Gets the font size.
    * @return the LaTeX declaration that sets the font size
    */
   public String getSize()
   {
      return size;
   }

   /**
    * Sets the font family.
    * @param fontFamily the LaTeX declaration to set the font
    * family (e.g. "&#092;sffamily")
    */
   public void setFamily(String fontFamily)
   {
      family = fontFamily;
   }

   /**
    * Sets the font weight.
    * @param fontWeight the LaTeX declaration to set the font
    * weight (e.g. "&#092;bfseries")
    */
   public void setWeight(String fontWeight)
   {
      weight = fontWeight;
   }

   /**
    * Sets the font shape.
    * @param fontShape the LaTeX declaration to set the font
    * shape (e.g. "&#092;em")
    */
   public void setShape(String fontShape)
   {
      shape = fontShape;
   }

   /**
    * Sets the font size.
    * @param fontSize the LaTeX declaration to set the font size
    * (e.g. "&#092;large"
    */
   public void setSize(String fontSize)
   {
      size = fontSize;
   }

   /**
    * Saves this LaTeX font style in JDR format.
    * @throws IOException if I/O error occurs
    * @see #read(JDRAJR)
    */
   public void save(JDRAJR jdr)
      throws IOException
   {
      jdr.writeString(family);
      jdr.writeString(weight);
      jdr.writeString(shape);
      jdr.writeString(size);
   }

   /**
    * Reads a LaTeX font style stored in JDR format.
    * @return the font style read from the input stream
    * @throws InvalidFormatException if data is not stored in
    * the correct format
    * @see #save(JDRAJR jdr)
    */
   public static LaTeXFont read(JDRAJR jdr)
      throws InvalidFormatException
   {
      LaTeXFont font = new LaTeXFont();

      font.setFamily(jdr.readString(
        InvalidFormatException.LATEX_FONT_FAMILY));

      font.setWeight(jdr.readString(
        InvalidFormatException.LATEX_FONT_WEIGHT));

      font.setShape(jdr.readString(
        InvalidFormatException.LATEX_FONT_SHAPE));

      font.setSize(jdr.readString(
        InvalidFormatException.LATEX_FONT_SIZE));

      return font;
   }

   /**
    * Gets the LaTeX declarations that set this font style.
    * This just returns a concatenation of the 
    * family, weight, shape and size. Spaces are inserted
    * if any of the parameters end with a command name (backslash
    * followed by one or more alphabetical characters).
    * @return the LaTeX declarations that set this font style
    */
   public String tex()
   {
      return tex(family) + tex(weight) + tex(shape) + tex(size);
   }

   public static String tex(String cmd)
   {
      if (ENDS_CONTROL_SEQUENCE_NAME.matcher(cmd).matches())
      {
         return cmd + " ";
      }
      else
      {
         return cmd;
      }
   }

   /**
    * Gets a copy of this font style.
    * @return a copy of this font style
    */
   public Object clone()
   {
      return new LaTeXFont(this);
   }

   /**
    * Sets this font style to be the same as another font style.
    * @param lf the other font style
    */
   public void makeEqual(LaTeXFont lf)
   {
      family = lf.family;
      weight = lf.weight;
      shape  = lf.shape;
      size   = lf.size;
   }

   /**
    * Determines if this object is the same as another object.
    * @param obj the other object with which this object should be
    * compared
    * @return true if this object is considered to be the same as the
    * other object
    */
   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (!(obj instanceof LaTeXFont)) return false;

      LaTeXFont f = (LaTeXFont)obj;

      if (!family.equals(f.family)) return false;
      if (!weight.equals(f.weight)) return false;
      if (!shape.equals(f.shape)) return false;
      if (!size.equals(f.size)) return false;

      return true;
   }

   /**
    * Loads PostScript to LaTeX mappings from given file.
    * Each line in the file must be of the form:<br>
    * <em>PostScript name</em>=<em>LaTeX family</em>,<em>LaTeX series</em>,<em>LaTeX shape</em><br>
    * Blank lines and lines starting with # are ignored.
    * The PostScript name may contain regular expressions to indicate a match. For
    * example:
<pre>
.*obliquebold=,\bfseries,\slshape
</pre>
    * @param file the file from which to load the mappings
    * @throws IOException if I/O error occurs
    * @throws InvalidFormatException if data in file is not in 
    * correct format
    */
   public static void loadPostScriptMappings(JDRMessageDictionary msgDict, 
      File file)
      throws IOException,InvalidFormatException
   {
      BufferedReader in = new BufferedReader(new FileReader(file));

      psMapping = new HashMap<String,LaTeXFont>();

      String line = in.readLine();
      int lineNum = 1;

      while (line != null)
      {
         if (line.equals("") || line.startsWith("#"))
         {
            continue;
         }

         String[] split = line.split("=");

         if (split.length != 2)
         {
            throw new InvalidMappingException(
               file, InvalidMappingException.KEYVAL, line, lineNum, msgDict);
         }

         String key = split[0];

         split = split[1].split(",");

         if (split.length != 3)
         {
            throw new InvalidMappingException(
               file, InvalidMappingException.VALUE, line, lineNum, msgDict);
         }

         LaTeXFont font = new LaTeXFont(split[0], split[1], split[2],
             "\\normalsize");

         psMapping.put(key, font);

         line = in.readLine();
         lineNum++;
      }

      in.close();
   }

   /**
    * Loads Java to LaTeX mappings from given file.
    * Each line in the file must be of the form:<br>
    * <em>Java name</em>=<em>LaTeX family</em><br>
    * Blank lines and lines starting with # are ignored.
    * @param file the file from which to load the mappings
    * @throws IOException if I/O error occurs
    * @throws InvalidFormatException if data in file is not in 
    * correct format
    */
   public static void loadJavaMappings(JDRMessageDictionary msgDict, File file)
      throws IOException,InvalidFormatException
   {
      BufferedReader in = new BufferedReader(new FileReader(file));

      javaMapping = new HashMap<String,String>();

      String line = in.readLine();
      int lineNum = 1;

      while (line != null)
      {
         if (line.equals("") || line.startsWith("#"))
         {
            continue;
         }

         String[] split = line.split("=");

         if (split.length != 2)
         {
            throw new InvalidMappingException(
               file, InvalidMappingException.KEYVAL, line, lineNum, msgDict);
         }

         javaMapping.put(split[0], split[1]);

         line = in.readLine();
         lineNum++;
      }

      in.close();
   }

   /**
    * Convert named Java font to LaTeX font using Java
    * to LaTeX font mapping. If {@link #javaMapping} is 
    * <code>null</code> or if the given font is not found in 
    * {@link #javaMapping}, the font is obtained from
    * {@link #defaultFromJavaFamily(String)}.
    * @param javaName the name of the Java font (may also be a regular expression)
    * @return nearest equivalent LaTeX font family declaration
    * @see #javaMapping
    */
   public static String fromJavaFamily(String javaName)
   {
      if (javaMapping == null)
      {
         return defaultFromJavaFamily(javaName);
      }

      String name = javaMapping.get(javaName);

      if (name != null)
      {
         return name;
      }

      Set<String> keySet = javaMapping.keySet();

      for (Iterator<String> i=keySet.iterator(); i.hasNext();)
      {
         String key = i.next();

         if (javaName.matches(key))
         {
            return javaMapping.get(key);
         }
      }

      return defaultFromJavaFamily(javaName);
   }

   /**
    * Default mapping from Java font name to LaTeX family
    * declaration. The mapping is as follows:
    * <ul>
    * <li> If the Java name contains "typewriter" or "mono"
    * or "courier", then returns "&#092;ttfamily";
    * <li> if the Java name contains "sans", then returns
    * "&#092;sffamily";
    * <li> otherwise returns "&#092;rmfamily".
    * </ul>
    * @return nearest equivalent LaTeX font family declaration
    */
   public static String defaultFromJavaFamily(String javaName)
   {
      String name = javaName.toLowerCase();

      if (name.equals("typewriter")
            || name.indexOf("mono") != -1
            || name.indexOf("courier") != -1)
      {
         return "\\ttfamily";
      }
      else if (name.indexOf("sans") != -1)
      {
         return "\\sffamily";
      }
      else
      {
         return "\\rmfamily";
      }
   }

   /**
    * Convert named PostScript font to LaTeX font using PostScript
    * to LaTeX font mapping. If {@link #psMapping} is 
    * <code>null</code> or if the given font is not found in 
    * {@link #psMapping}, the font is obtained from
    * {@link #defaultFromPostScript(String)}.
    * @param psName the name of the PostScript font
    * @return nearest equivalent LaTeX declarations (font size
    * is always "&#092;normalsize")
    * @see #psMapping
    */
   public static LaTeXFont fromPostScript(String psName)
   {
      if (psMapping == null)
      {
         return defaultFromPostScript(psName);
      }

      LaTeXFont font = psMapping.get(psName);

      if (font != null)
      {
         return font;
      }

      Set<String> keySet = psMapping.keySet();

      for (Iterator<String> i=keySet.iterator(); i.hasNext();)
      {
         String key = i.next();

         if (psName.matches(key))
         {
            return psMapping.get(key);
         }
      }

      return defaultFromPostScript(psName);
   }

   /**
    * Default PostScript to LaTeX font mapping. The mapping is
    * as follows:
    * <ul>
    * <li>If PostScript name contains "bold", the series command is
    * set to "&#092;bfseries", otherwise it is set to
    * "&#092;mdseries".
    * <li>If PostScript name contains "italic", the shape command is
    * set to "&#092;itshape"; if it contains "oblique", the shape
    * command is set to "&#092;slshape"; otherwise the shape 
    * command is set to "&#092;upshape".
    * <li>If PostScript name contains "times", the family command is
    * set to "&#092;rmfamily"; if it contains "helvetica", the
    * family command is set to "&#092;sffamily"; if it contains
    * "courier", the family command is set to "&#092;ttfamily"; if
    * it contains "mono", the family command is set to 
    * "&#092;ttfamily"; if it contains "sans", the family command
    * is set to "&#092;sffamily"; otherwise the family command is
    * set to "&#092;rmfamily".
    * </ul>
    * @return nearest equivalent LaTeX declarations (font size
    * is always "&#092;normalsize")
    */
   public static LaTeXFont defaultFromPostScript(String psName)
   {
      String string = psName.toLowerCase();

      String seriesCmd = "\\mdseries";

      if (string.matches(".*bold.*"))
      {
         seriesCmd = "\\bfseries";
      }

      String shapeCmd = "\\upshape";

      if (string.matches(".*italic.*"))
      {
         shapeCmd = "\\itshape";
      }
      else if (string.matches(".*oblique.*"))
      {
         shapeCmd = "\\slshape";
      }

      string = string.replaceAll(
         "(oblique)|(bold)|(italic)","");
      string = string.replaceFirst("[\\.]", " ");
      string = string.replaceFirst("^/", "");
      string = string.replaceFirst(" *$", "");

      String familyCmd = "\\rmfamily";

      if (string.matches("times"))
      {
         familyCmd = "\\rmfamily";
      }
      else if (string.matches("helvetica"))
      {
         familyCmd = "\\sffamily";
      }
      else if (string.matches("courier"))
      {
         familyCmd = "\\ttfamily";
      }
      else if (string.matches("mono"))
      {
         familyCmd = "\\ttfamily";
      }
      else if (string.matches("sans"))
      {
         familyCmd = "\\sffamily";
      }

      return new LaTeXFont(familyCmd, seriesCmd, shapeCmd,
         "\\normalsize");
   }

   public String info(JDRMessageDictionary msgSys)
   {
      return msgSys.getMessageWithFallback(
       "objectinfo.textual.latex_font",
       "LaTeX font family: {0}, weight: {1}, shape: {2}, size: {3}",
       family, weight, shape, size);
   }

   private String family="\\rmfamily";
   private String weight="\\mdseries";
   private String shape="\\upshape";
   private String size="\\normalsize";

   /**
    * Map converting Java font name to LaTeX font declarations.
    * If this is null, default mapping used.
    */
   public static HashMap<String,String> javaMapping=null;

   /**
    * Map converting PostScript font name to LaTeX font declarations.
    * If this is null, default mapping used.
    */
   public static HashMap<String,LaTeXFont> psMapping=null;

   public static final Pattern ENDS_CONTROL_SEQUENCE_NAME 
     = Pattern.compile(".*\\\\[a-zA-Z]+");
}

