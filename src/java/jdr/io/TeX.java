// File          : TeX.java
// Purpose       : functions related to TeX
// Creation Date : 2014-04-18
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
package com.dickimawbooks.jdr.io;

import java.io.*;
import java.util.Locale;
import java.nio.file.Path;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.text.*;
import com.dickimawbooks.jdr.*;

/**
 * Functions to TeX.
 * @author Nicola L C Talbot
 */

public class TeX
{
   protected TeX()
   {
      this(null, null);
   }

   public TeX(Path basePath, Writer out)
   {
      this.basePath = basePath;
      setWriter(out);
   }

   public void writeOutlineDef()
     throws IOException
   {
/*
  Example syntax:
  \jdroutline
    {2 Tr 0 1 0 rg 1 0 0 RG}
    {fillstyle=solid,linecolor=green,fillcolor=red}
    {\bfseries\Huge ABC}
 */

/*
 * pdf-trans defines \mod which is likely to conflict with other
 * packages so patch it.
 */
      println("\\newcommand*{\\jdroutline}[3]{%");
      println("  \\GenericWarning{}{text outline can't be implemented}#3%");
      println("}%");
      println("\\ifpdf");
      println(" \\let\\jdrorgmod\\mod");
      println(" \\InputIfFileExists{pdf-trans}%");
      println(" {%");
      println("   \\renewcommand*{\\jdroutline}[3]{%");
      println("     {\\def\\mod{\\expandtwonumexprafter \\modulo}%");
      println("     \\setbox\\@tempboxa\\hbox{##3}%");
      println("     \\boxgs{##1}{}\\copy\\@tempboxa");
      println("     }%");
      println("   }%");
      println(" }{}");
      println(" \\let\\mod\\jdrorgmod");
      println("\\else");
      println(" \\IfFileExists{pst-char.sty}%");
      println(" {%");
      println("   \\usepackage{pst-char}");
      println("   \\renewcommand*{\\jdroutline}[3]{%");
      println("     \\begin{pspicture}(0,0)");
      println("     \\pscharpath[##2]{##3}");
      println("     \\end{pspicture}");
      println("   }");
      println(" }{}");
      println("\\fi");
   }

   public void writePdfInfo(String title)
     throws IOException
   {
      println("\\pdfinfo{");

      int n = (title == null ? 0 : title.length());

      if (n > 0)
      {
         StringBuilder builder = new StringBuilder();

         for (int i = 0; i < n; i++)
         {
            char c = title.charAt(i);

            if (c == '(')
            {
               builder.append("\\(");
            }
            else if (c == ')')
            {
               builder.append("\\)");
            }
            else if (c == '\\')
            {
               builder.append("\\string\\\\");
            }
            else
            {
               builder.append(c);
            }
         }

         println(" /Title ("+builder.toString()+")");
      }

      Calendar now = Calendar.getInstance();
      int timezone = now.get(Calendar.ZONE_OFFSET)
                   + now.get(Calendar.DST_OFFSET);

      int tzmins = timezone/60000;
      int tzhours = tzmins/60;

      tzmins = tzmins % 60;

      if (timezone == 0)
      {
         println(" /CreationDate (D:"+
          String.format(Locale.ROOT, "%d%02d%02d%02d%02d%02dZ",
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH),
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        now.get(Calendar.SECOND))
          +")");
      }
      else
      {
         println(" /CreationDate (D:"+
          String.format(Locale.ROOT, "%d%02d%02d%02d%02d%02d%+03d'%02d'",
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH),
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        now.get(Calendar.SECOND),
                        tzhours, tzmins)
          +")");
      }
      println("}");
   }

   public AffineTransform getTransform()
   {
      return affineTransform;
   }

   public void setTransform(AffineTransform af)
   {
      affineTransform = af;
   }

   /**
    * Formats a value in decimal notation. (TeX can't handle 
    * exponents.)
    * @param value value to be formatted
    * @return formatted value
    */
   public static String format(double value)
   {
      return df.format(value);
   }

   public static String length(CanvasGraphics cg, double length)
   {
      return cg.getStorageUnit().tex(length);
   }

   public static String length(JDRLength length)
   {
      return length.getUnit().tex(length.getValue());
   }

   /**
    * Writes the transformed point in pgf format. This passes
    * the transformed co-ordinates to {@link #point(double,double)}.
    * That is, the transformation is done by Java not by pgf
    * package.
    * @param cg graphics information
    * @param af transform to apply before printing
    * @param x x co-ordinate
    * @param y y co-ordinate
    * @return string containing pgf command to specify the 
    * transformed point
    * @see #point(CanvasGraphics,double,double)
    */
   public static String point(CanvasGraphics cg, AffineTransform af, double x, double y)
   {
      Point2D.Double p = new Point2D.Double(x, y);
      af.transform(p, p);

      return point(cg, p.getX(), p.getY());
   }

   /**
    * Gets the given co-ordinates in pgf format using the given unit.
    * @param cg the graphics information
    * @param x x co-ordinate
    * @param y y co-ordinate
    * @return string containing pgf command to specify the point
    * @see #point(CanvasGraphics,AffineTransform,double,double)
    */
   public static String point(CanvasGraphics cg, double x, double y)
   {
      return new String("\\pgfpoint{"
         +length(cg, x) +"}{" +length(cg, y)+"}");
   }

   /**
    * Gets the pgf commands to set the transformation matrix.
    * @param matrix transformation matrix in flat format
    * @return string containing pgf commands to set the transformation
    * matrix
    * @see #transform(CanvasGraphics,AffineTransform)
    */
   public static String transform(CanvasGraphics cg, double[] matrix)
   {
      return "\\pgftransformcm{"
           + format(matrix[0])+"}{"
           + format(matrix[1])+"}{"
           + format(matrix[2])+"}{"
           + format(matrix[3])+"}{"
           + point(cg, matrix[4], matrix[5])+"}";
   }

   /**
    * Gets the pgf commands to set the transformation matrix.
    * @param af transformation
    * @return string containing pgf commands to set the transformation
    * matrix
    * @see #transform(CanvasGraphics cg, double[])
    */
   public static String transform(CanvasGraphics cg, AffineTransform af)
   {
      double[] matrix = new double[6];
      af.getMatrix(matrix);
      return transform(cg, matrix);
   }

   public static String quickPath(CanvasGraphics cg, Shape shape)
   {
      String str = "";

      PathIterator pi = shape.getPathIterator(null);

      double[] coords = new double[6];

      for (; !pi.isDone(); pi.next())
      {
         switch (pi.currentSegment(coords))
         {
            case PathIterator.SEG_CLOSE:
            break;
            case PathIterator.SEG_MOVETO:
              str += "\\pgfpathqmoveto{"
               + length(cg, coords[0])+"}{"
               + length(cg, coords[1])+"}";
            break;
            case PathIterator.SEG_LINETO:
              str += "\\pgfpathqlineto{"
               + length(cg, coords[0])+"}{"
               + length(cg, coords[1])+"}";
            break;
            case PathIterator.SEG_QUADTO:
            break;
            case PathIterator.SEG_CUBICTO:
              str += "\\pgfpathqcurveto{"
               + length(cg, coords[0])+"}{"
               + length(cg, coords[1])+"}{"
               + length(cg, coords[2])+"}{"
               + length(cg, coords[3])+"}{"
               + length(cg, coords[4])+"}{"
               + length(cg, coords[5])+"}";
            break;
         }
      }

      return str;
   }

   public void printQuickPath(CanvasGraphics cg, Shape shape)
     throws IOException
   {
      PathIterator pi = shape.getPathIterator(null);

      double[] coords = new double[6];

      for (; !pi.isDone(); pi.next())
      {
         switch (pi.currentSegment(coords))
         {
            case PathIterator.SEG_CLOSE:
            break;
            case PathIterator.SEG_MOVETO:
              println("\\pgfpathqmoveto{"
               + length(cg, coords[0])+"}{"
               + length(cg, coords[1])+"}");
            break;
            case PathIterator.SEG_LINETO:
              println("\\pgfpathqlineto{"
               + length(cg, coords[0])+"}{"
               + length(cg, coords[1])+"}");
            break;
            case PathIterator.SEG_QUADTO:
              println("\\pgfpathqcurveto{"
               + length(cg, coords[0])+"}{"
               + length(cg, coords[1])+"}{"
               + length(cg, coords[0])+"}{"
               + length(cg, coords[1])+"}{"
               + length(cg, coords[2])+"}{"
               + length(cg, coords[3])+"}");
            break;
            case PathIterator.SEG_CUBICTO:
              println("\\pgfpathqcurveto{"
               + length(cg, coords[0])+"}{"
               + length(cg, coords[1])+"}{"
               + length(cg, coords[2])+"}{"
               + length(cg, coords[3])+"}{"
               + length(cg, coords[4])+"}{"
               + length(cg, coords[5])+"}");
            break;
         }
      }
   }

   public Path relativize(String filename)
   {
      Path path = (new File(filename)).toPath();

      if (basePath == null || !path.isAbsolute())
      {
         return path;
      }

      return basePath.relativize(path).normalize();
   }

   public void setWriter(Writer writer)
   {
      this.writer = writer;
   }

   public Writer getWriter()
   {
      return writer;
   }

   public void print(String string)
     throws IOException
   {
      writer.write(string);
   }

   public void println(String string)
     throws IOException
   {
      print(string+System.getProperty("line.separator", "\n"));
   }

   public void println()
     throws IOException
   {
      print(System.getProperty("line.separator", "\n"));
   }

   public void comment(String string)
     throws IOException
   {
      println("% "+string);
   }

   public void writePreambleCommands(JDRGroup image)
     throws IOException
   {
      println("\\usepackage{pgf}");
      println("\\usepgflibrary{decorations.text}");

      CanvasGraphics cg = image.getCanvasGraphics();

      if (cg.hasPreamble())
      {
         println(cg.getPreamble());
      }
   }

   public void writeMidPreambleCommands(JDRGroup image)
     throws IOException
   {
      CanvasGraphics cg = image.getCanvasGraphics();

      if (cg.hasMidPreamble())
      {
         println(cg.getMidPreamble());
      }
   }

   public void writeEndPreambleCommands(JDRGroup image)
     throws IOException
   {
      CanvasGraphics cg = image.getCanvasGraphics();

      if (cg.hasEndPreamble())
      {
         println(cg.getEndPreamble());
      }
   }

   public void writeCreationDate()
     throws IOException
   {
      writeCreationDate(new Date());
   }

   public void writeCreationDate(Date date)
     throws IOException
   {
      comment(dateFormat.format(date));
   }

   public boolean isConvertBitmapToEpsEnabled()
   {
      return convertBitmapToEps;
   }

   public void setConvertBitmapToEpsEnabled(boolean enabled)
   {
      convertBitmapToEps = enabled;
   }

   public boolean isUsePdfInfoEnabled()
   {
      return usePdfInfo;
   }

   public void setUsePdfInfoEnabled(boolean enabled)
   {
      usePdfInfo = enabled;
   }

   public void setTextPathExportOutlineSetting(int flag)
   {
      switch (flag)
      {
         case TEXTPATH_EXPORT_OUTLINE_TO_PATH:
         case TEXTPATH_EXPORT_OUTLINE_IGNORE:
            textPathExportOutlineSetting = flag;
         break;
         default:
            throw new IllegalArgumentException(
               "Invalid textpath export outline setting "+flag);
      }
   }

   public int getTextPathExportOutlineSetting()
   {
      return textPathExportOutlineSetting;
   }

   public void setTextualExportShadingSetting(int flag)
   {
      switch (flag)
      {
         case TEXTUAL_EXPORT_SHADING_AVERAGE:
         case TEXTUAL_EXPORT_SHADING_START:
         case TEXTUAL_EXPORT_SHADING_END:
         case TEXTUAL_EXPORT_SHADING_TO_PATH:
            textualExportShadingSetting = flag;
         break;
         default:
            throw new IllegalArgumentException(
               "Invalid textual export shading setting "+flag);
      }
   }

   public int getTextualExportShadingSetting()
   {
      return textualExportShadingSetting;
   }

   /**
    * Number format to use to print decimal numbers in LaTeX file.
    * @see #format(double)
    */
   public final static NumberFormat df
      = new DecimalFormat("#####0.0#####",
        new DecimalFormatSymbols(Locale.ENGLISH));

   /**
    * Gets date format. (The creation date is written to the 
    * LaTeX file.)
    */
   protected final static DateFormat dateFormat
      = DateFormat.getDateTimeInstance();

   private Path basePath;

   protected AffineTransform affineTransform;

   protected Writer writer;

   protected boolean convertBitmapToEps = false;

   protected boolean usePdfInfo = false;

   public static final int TEXTPATH_EXPORT_OUTLINE_TO_PATH=0;
   public static final int TEXTPATH_EXPORT_OUTLINE_IGNORE=1;

   public static final int TEXTPATH_EXPORT_OUTLINE_MAX_INDEX=1;

   private volatile int textPathExportOutlineSetting 
      = TEXTPATH_EXPORT_OUTLINE_TO_PATH;

   public static final int TEXTUAL_EXPORT_SHADING_AVERAGE = 0;
   public static final int TEXTUAL_EXPORT_SHADING_START = 1;
   public static final int TEXTUAL_EXPORT_SHADING_END = 2;
   public static final int TEXTUAL_EXPORT_SHADING_TO_PATH = 3;

   public static final int TEXTUAL_EXPORT_SHADING_MAX_INDEX = 3;

   private volatile int textualExportShadingSetting 
      = TEXTUAL_EXPORT_SHADING_AVERAGE;

}
