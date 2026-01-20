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
      this(null, null, null);
   }

   public TeX(Path basePath, Writer out, ExportSettings exportSettings)
   {
      this.basePath = basePath;
      setWriter(out);
      this.exportSettings = exportSettings;
      this.objectArgs = new Vector<String>();
   }

   public boolean isFlowframTkStyUsed()
   {
      return exportSettings.useFlowframTkSty;
   }

   public void writeOutlineDef()
     throws IOException
   {
      writeOutlineDef(false);
   }

   public void writeOutlineDef(boolean comment)
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
      println(comment, "\\newcommand*{\\jdroutline}[3]{%");
      println(comment, "  \\GenericWarning{}{text outline can't be implemented}#3%");
      println(comment, "}%");
      println(comment, "\\ifpdf");
      println(comment, " \\let\\jdrorgmod\\mod");
      println(comment, " \\InputIfFileExists{pdf-trans}%");
      println(comment, " {%");
      println(comment, "   \\renewcommand*{\\jdroutline}[3]{%");
      println(comment, "     {\\def\\mod{\\expandtwonumexprafter \\modulo}%");
      println(comment, "     \\setbox\\@tempboxa\\hbox{##3}%");
      println(comment, "     \\boxgs{##1}{}\\copy\\@tempboxa");
      println(comment, "     }%");
      println(comment, "   }%");
      println(comment, " }{}");
      println(comment, " \\let\\mod\\jdrorgmod");
      println(comment, "\\else");
      println(comment, " \\IfFileExists{pst-char.sty}%");
      println(comment, " {%");
      println(comment, "   \\usepackage{pst-char}");
      println(comment, "   \\renewcommand*{\\jdroutline}[3]{%");
      println(comment, "     \\begin{pspicture}(0,0)");
      println(comment, "     \\pscharpath[##2]{##3}");
      println(comment, "     \\end{pspicture}");
      println(comment, "   }");
      println(comment, " }{}");
      println(comment, "\\fi");
   }

   public void writeDocInfo(String title)
     throws IOException
   {
      if (isUsePdfInfoEnabled())
      {
         if (isFlowframTkStyUsed())
         {
            println("\\flowframtkimageinfo{");

            if (title != null && !title.isEmpty())
            {
               print(" title={");

               StringBuilder builder = new StringBuilder();

               for (int i = 0; i < title.length(); )
               {
                  int cp = title.codePointAt(i);
                  i += Character.charCount(cp);

                  switch (cp)
                  {
                     case '~':
                       builder.append(
                       "\\flowframtkimgtitlechar{\\textasciitilde}{\\176}");
                     break;
                     case '^':
                       builder.append(
                       "\\flowframtkimgtitlechar{\\textasciicircum}{\\136}");
                     break;
                     case '\\':
                       builder.append(
                       "\\flowframtkimgtitlechar{\\textbackslash}{\\\\}");
                     break;
                     case '$':
                     case '%':
                     case '&':
                     case '{':
                     case '}':
                     case '_':
                     case '#':
                       builder.append(String.format(
                       "\\flowframtkimgtitlechar{\\%c}{\\%03o}", (char)cp, cp));
                     break;
                     case '(':
                     case ')':
                       builder.append(String.format(
                       "\\flowframtkimgtitlechar{%c}{\\%c}", (char)cp, cp));
                     break;
                     default:
                       if (cp < 0x7F)
                       {
                          builder.appendCodePoint(cp);
                       }
                       else
                       {
                          builder.append("\\flowframtkimgtitlechar{");
                          builder.appendCodePoint(cp);
                          builder.append("}{");
                          builder.append(Integer.toOctalString(cp));
                          builder.append("}");
                       }
                  }
               }

               print(builder.toString());

               println("},");
            }

            Calendar now = Calendar.getInstance();
            int timezone = now.get(Calendar.ZONE_OFFSET)
                         + now.get(Calendar.DST_OFFSET);

            int tzmins = timezone/60000;
            int tzhours = tzmins/60;

            tzmins = tzmins % 60;

            print(" creationdate={D:");

            if (timezone == 0)
            {
               print(
                String.format(Locale.ROOT, "%d%02d%02d%02d%02d%02dZ",
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH),
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        now.get(Calendar.SECOND)));
            }
            else
            {
               print(
                String.format(Locale.ROOT, "%d%02d%02d%02d%02d%02d%+03d'%02d'",
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH),
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        now.get(Calendar.SECOND),
                        tzhours, tzmins));
            }

            println("}");

            println("}");
         }
         else
         {
            println("\\ifpdf");
            writePdfInfo(title);
            println("\\fi");
         }
      }
   }

   public void writePdfInfo(String title)
     throws IOException
   {
      println("\\pdfinfo{");

      int n = (title == null ? 0 : title.length());

      if (n > 0)
      {
         StringBuilder builder = new StringBuilder();

         for (int i = 0; i < n; )
         {
            int cp = title.codePointAt(i);
            i += Character.charCount(cp);

            if (cp == '(')
            {
               builder.append("\\(");
            }
            else if (cp == ')')
            {
               builder.append("\\)");
            }
            else if (cp == '\\')
            {
               builder.append("\\string\\\\");
            }
            else
            {
               builder.appendCodePoint(cp);
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

   protected void clearObjectArgList()
   {
      objectArgs.clear();
   }

   public int writeStartObject(JDRCompleteObject obj)
   throws IOException
   {
      String description = obj.getDescription();

      if (!description.isEmpty())
      {
         comment(description);
      }

      int objectId = objectArgs.size();

      if (exportSettings.objectMarkup != ExportSettings.ObjectMarkup.NONE)
      {
         CanvasGraphics cg = obj.getCanvasGraphics();

         if (storagePaperHeight == 0.0)
         {
            storagePaperHeight = cg.bpToStorage(cg.getPaperHeight());
         }

         BBox bbox = obj.getStorageBBox();

         double x1 = bbox.getMinX();
         double y1 = storagePaperHeight - bbox.getMaxY();
         double x2 = bbox.getMaxX();
         double y2 = storagePaperHeight - bbox.getMinY();


         String args = String.format(Locale.ROOT,
          "{%d}{%s}{%s}{%s}{%s}{%s}{%s}",
          objectId, obj.getClass().getSimpleName(),
          TeXMappings.replaceSpecialChars(description),
          TeXMappings.replaceSpecialChars(obj.getTag()),
          point(cg, x1, y1),
          length(cg, x2-x1),
          length(cg, y2-y1));

         objectArgs.add(args);

         switch (exportSettings.objectMarkup)
         {
            case PAIRED:
             print("\\flowframtkstartobject");
             print(args);
             println("%");
            break;
            case ENCAP:
             print("\\flowframtkencapobject");
             print(args);
             println("{%");
            break;
        }
      }

      return objectId;
   }

   public void writeEndObject(int idx, JDRCompleteObject obj)
   throws IOException
   {
      String description = obj.getDescription();

      if (isFlowframTkStyUsed()
        && exportSettings.objectMarkup != ExportSettings.ObjectMarkup.NONE)
      {
         switch (exportSettings.objectMarkup)
         {
            case PAIRED:
               print("\\flowframtkendobject");
               print(objectArgs.get(idx));
               println("%");
            break;
            case ENCAP:
               println("}%");
            break;
         }
      }
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
      return quickPath(cg, shape, String.format("%n"));
   }

   public static String quickPath(CanvasGraphics cg, Shape shape, String sep)
   {
      StringBuilder buffer = new StringBuilder();

      PathIterator pi = shape.getPathIterator(null);

      double[] coords = new double[6];

      for (; !pi.isDone(); pi.next())
      {
         switch (pi.currentSegment(coords))
         {
            case PathIterator.SEG_CLOSE:
              buffer.append("\\pgfclosepath");
              buffer.append(sep);
            break;
            case PathIterator.SEG_MOVETO:
              buffer.append("\\pgfpathqmoveto{");
              buffer.append(length(cg, coords[0]));
              buffer.append("}{");
              buffer.append(length(cg, coords[1]));
              buffer.append("}");
              buffer.append(sep);
            break;
            case PathIterator.SEG_LINETO:
              buffer.append("\\pgfpathqlineto{");
              buffer.append(length(cg, coords[0]));
              buffer.append("}{");
              buffer.append(length(cg, coords[1]));
              buffer.append("}");
              buffer.append(sep);
            break;
            case PathIterator.SEG_QUADTO:
              // no quick form? (but unlikely to occur)
              buffer.append("\\pgfpathquadraticcurveto{");
              buffer.append(point(cg, coords[0], coords[1]));
              buffer.append("}{");
              buffer.append(point(cg, coords[2], coords[3]));
              buffer.append("}");
              buffer.append(sep);
            break;
            case PathIterator.SEG_CUBICTO:
              buffer.append("\\pgfpathqcurveto{");
              buffer.append(length(cg, coords[0]));
              buffer.append("}{");
              buffer.append(length(cg, coords[1]));
              buffer.append("}{");
              buffer.append(length(cg, coords[2]));
              buffer.append("}{");
              buffer.append(length(cg, coords[3]));
              buffer.append("}{");
              buffer.append(length(cg, coords[4]));
              buffer.append("}{");
              buffer.append(length(cg, coords[5]));
              buffer.append("}");
              buffer.append(sep);
            break;
         }
      }

      return buffer.toString();
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
              println("\\pgfclosepath");
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
              print("\\pgfpathquadraticcurveto{");
              print(point(cg, coords[0], coords[1]));
              print("}{");
              print(point(cg, coords[2], coords[3]));
              println("}");
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
      return relativize(new File(filename));
   }

   public Path relativize(File file)
   {
      return relativize(file.toPath());
   }

   public Path relativize(Path path)
   {
      if (basePath == null || !path.isAbsolute())
      {
         return path;
      }

      try
      {
         return basePath.relativize(path).normalize();
      }
      catch (IllegalArgumentException e)
      {
         return path;
      }
   }

   public void setWriter(Writer writer)
   {
      this.writer = writer;
   }

   public Writer getWriter()
   {
      return writer;
   }

   public void format(String fmtStr, Object... args)
     throws IOException
   {
      print(String.format(Locale.ROOT, fmtStr, args));
   }

   public void print(String string)
     throws IOException
   {
      writer.write(string);
   }

   public void println(String string)
     throws IOException
   {
      print(string);
      print(System.getProperty("line.separator", "\n"));
   }

   public void println(boolean comment, String string)
     throws IOException
   {
      if (comment)
      {
         comment(string);
      }
      else
      {
         println(string);
      }
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

   protected void checkForRequiredSupport(JDRGroup group)
   {
      if (supportOutline && supportTextPath)
      {
         return;
      }

      for (int i = 0; i < group.size(); i++)
      {
         JDRCompleteObject object = group.get(i);

         if (object instanceof JDRGroup)
         {
            checkForRequiredSupport((JDRGroup)object);
         }
         else if (object instanceof JDRText)
         {
            if (((JDRText)object).isOutline())
            {
               supportOutline = true;
            }
         }
         else if (object.hasShape())
         {
            checkShapeForRequiredSupport((JDRShape)object);
         }

         if (supportOutline && supportTextPath)
         {
            return;
         }
      }
   }

   protected void checkShapeForRequiredSupport(JDRShape shape)
   {
      if (shape instanceof JDRCompoundShape)
      {
         checkShapeForRequiredSupport(((JDRCompoundShape)shape).getUnderlyingShape());
      }
      else if (shape instanceof JDRTextPath)
      {
         supportTextPath = true;
         // No support for text path outlines so don't bother checking
      }
   }

   protected void writeUsePackageFlowframTk(boolean usepackage)
     throws IOException
   {
      if (usepackage)
      {
         print("\\usepackage");

         if (supportOutline && supportTextPath)
         {
            print("[outline,textpath]");
         }
         else if (supportOutline)
         {
            print("[outline]");
         }
         else if (supportTextPath)
         {
            print("[textpath]");
         }
      }
      else
      {
         if (supportOutline)
         {
            print("\\PassOptionsToPackage{outline}{");
            print(FLOWFRAME_STY);
            println("}");
         }

         if (supportTextPath)
         {
            print("\\PassOptionsToPackage{textpath}{");
            print(FLOWFRAME_STY);
            println("}");
         }

         print("\\RequirePackage");
      }

      print("{");
      print(FLOWFRAME_STY);
      println("}");
   }

   public void writePreambleCommands(JDRGroup image, boolean inDoc)
     throws IOException
   {
      writePreambleCommands(image, inDoc, false);
   }

   public void writePreambleCommands(JDRGroup image, boolean inDoc, boolean comment)
     throws IOException
   {
      if (isFlowframTkStyUsed())
      {
         checkForRequiredSupport(image);

         if (inDoc)
         {
            writeUsePackageFlowframTk(true);
         }
         else
         {
            writeUsePackageFlowframTk(false);
         }
      }
      else
      {
         if (inDoc)
         {
            println("\\usepackage{pgf}");
            println("\\usepackage{ifpdf}");
         }
         else
         {
            println("\\RequiredPackage{pgf}");
            println("\\RequiredPackage{ifpdf}");
         }

         println("\\usepgflibrary{decorations.text}");
      }

      CanvasGraphics cg = image.getCanvasGraphics();

      if (cg.hasPreamble())
      {
         String preamble = cg.getPreamble();

         if (inDoc)
         {
            preamble = preamble.replaceAll("\\\\RequirePackage\\b", "\\\\usepackage");
         }
         else
         {
            preamble = preamble.replaceAll("\\\\usepackage\\b", "\\\\RequirePackage");
         }

         println(preamble);
      }

      if (!isFlowframTkStyUsed())
      {
         println("\\makeatletter");
         writeOutlineDef(comment);
         println("\\makeatother");
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

   public void writeCommentHeaderBlock() throws IOException
   {
      if (exportSettings.includeBoilerPlateBlock)
      {
         JDRMessageDictionary dict = exportSettings.getMessageDictionary();

         comment(dict.getMessageWithFallback("tex.comment.created_by",
            "Created by {0} version {1}",
            dict.getApplicationName(), dict.getApplicationVersion()));

         if (exportSettings.writeDateComment)
         {
            writeCreationDate();
         }

         if (exportSettings.currentFile != null && exportSettings.writeSrcFilename)
         {
            comment(exportSettings.currentFile.toString());
         }
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

   public void printNormalFontSizeOption(CanvasGraphics cg, int normalsize, String docClass)
   throws IOException
   {
      if (docClass.equals("a0poster"))
      {
         if (normalsize != 25)
         {
            JDRMessage msgSys = cg.getMessageSystem();
            MessageInfoPublisher publisher = msgSys.getPublisher();

            publisher.publishMessages(MessageInfo.createWarning(
             msgSys.getMessageWithFallback(
              "warning.unsupported_cls_opt",
              "Class ''{0}'' doesn''t support option ''{1}''",
              docClass, normalsize+"pt")));
         }
      }
      else if (docClass.equals("scrbook") || docClass.equals("scrreport")
        || docClass.equals("scrartcl") || docClass.equals("scrreprt"))
      {
         print("fontsize="+normalsize+"pt");
      }
      else
      {
         print(""+normalsize+"pt");
      }
   }
   public boolean isConvertBitmapToEpsEnabled()
   {
      return exportSettings.bitmapsToEps;
   }

   public boolean isUsePdfInfoEnabled()
   {
      return exportSettings.usePdfInfo;
   }

   public ExportSettings.TextPathOutline getTextPathExportOutlineSetting()
   {
      return exportSettings.textPathOutline;
   }

   public ExportSettings.TextualShading getTextualExportShadingSetting()
   {
      return exportSettings.textualShading;
   }

   public ExportSettings.ObjectMarkup getObjectMarkup()
   {
      return exportSettings.objectMarkup;
   }

   public ExportSettings getExportSettings()
   {
      return exportSettings;
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

   protected Vector<String> objectArgs;

   protected boolean supportOutline=false, supportTextPath=false;

   protected double storagePaperHeight = 0;

   ExportSettings exportSettings;

   public static final String FLOWFRAME_STY = "flowframtkutils";
}
