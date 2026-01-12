// File          : SVG.java
// Purpose       : functions to save JDRGroup as SVG file
// Creation Date : 1st February 2006
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2026 Nicola L.C. Talbot

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
import java.nio.file.Path;

import java.awt.Shape;
import java.awt.geom.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.svg.*;

/**
 * Contains single method to write image to SVG file. This needs
 * more work as I haven't been able to test it for a while.
 * @author Nicola L C Talbot
 */

public class SVG
{
   protected SVG(ImportSettings importSettings)
   {
      this.importSettings = importSettings;
   }

   protected SVG(Path basePath, Writer out, ExportSettings exportSettings)
   {
      this.exportSettings = exportSettings;
      this.basePath = basePath;
      setWriter(out);
   }

   /**
    * Saves image to SVG file.
    * @param image image to save
    * @param title image title
    * @param out output stream
    * @param exportSettings export settings
    * @throws IOException if I/O error occurs
    */
   public static void save(JDRGroup image, String title, Writer out,
       ExportSettings exportSettings)
      throws IOException
   {
      SVG svg = new SVG(null, out, exportSettings);

      svg.save(image, title);
   }

   protected void save(JDRGroup image, String title)
      throws IOException
   {
      CanvasGraphics cg = image.getCanvasGraphics();
      JDRMessage msgSys = cg.getMessageSystem();
      MessageInfoPublisher publisher = msgSys.getPublisher();

      boolean indeter = (image.size() <= 1);

      publisher.publishMessages(MessageInfo.createIndeterminate(indeter));

      if (!indeter)
      {
         publisher.publishMessages(MessageInfo.createMaxProgress(image.size()));
      }

      setCanvasGraphics(cg);

      double storagePaperHeight = cg.getStoragePaperHeight();

      double imageWidth, imageHeight, minX, minY, maxX, maxY;

      switch (exportSettings.bounds)
      {
         case PAPER:
           imageWidth = cg.getStoragePaperWidth();
           imageHeight = storagePaperHeight;
           minX = 0;
           minY = 0;
           maxX = imageWidth;
           maxY = imageHeight;
         break;
         case TYPEBLOCK:
           FlowFrame typeblock = image.getFlowFrame();

           if (typeblock != null)
           {
              minX = typeblock.getLeft();
              minY = typeblock.getTop();
              maxX = cg.getStoragePaperWidth() - typeblock.getRight();
              maxY = storagePaperHeight - typeblock.getBottom();

              imageWidth = maxX - minX;
              imageHeight = maxY - minY;

              break;
           }

         case IMAGE:
           BBox box = image.getStorageBBox();
           imageWidth = box.getWidth();
           imageHeight = box.getHeight();
           minX = box.getMinX();
           minY = box.getMinY();
           maxX = box.getMaxX();
           maxY = box.getMaxY();
         break;
         default:
           throw new AssertionError(exportSettings.bounds);
      }

      println("<?xml version=\"1.0\" standalone=\"no\"?>");
      println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"");
      println("         \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
      println("<svg width=\""+length(imageWidth)+"\" height=\""
         + length(imageHeight) + "\"");
      println("     viewBox=\""
         + length(minX) + " " + length(minY) + " "
         + length(maxX) + " " + length(maxY)+"\"");
      println("     version=\"1.1\"");
      println("     xmlns=\"http://www.w3.org/2000/svg\"");
      println("     xmlns:xlink=\"http://www.w3.org/1999/xlink\">");

      if (title != null && !title.isEmpty())
      {
         println("   <title>"+encodeContent(title)+"</title>");
      }

      String desc = image.getDescription();

      if (desc != null && !desc.isEmpty())
      {
         println("   <title>"+encodeContent(desc)+"</title>");
      }

      referenceIDs = null;

      println("   <defs>");
      image.writeSVGdefs(this);
      println("   </defs>");

      for (int i = 0; i < image.size(); i++)
      {
         publisher.publishMessages(MessageInfo.createIncProgress());

         image.get(i).saveSVG(this);
      }

      println("</svg>");
   }

   public static JDRGroup load(CanvasGraphics cg, File file,
      ImportSettings importSettings,
      TeXMappings textModeMappings, TeXMappings mathModeMappings)
     throws SAXException,IOException
   {
      JDRMessage msgSys = cg.getMessageSystem();

      FileReader r = new FileReader(file);

      msgSys.getPublisher().publishMessages(
       MessageInfo.createMessage(
          msgSys.getMessageWithFallback(
          "info.loading",         
          "Loading ''{0}''",
          file.getAbsolutePath())));

      return load(cg, r, importSettings, textModeMappings, mathModeMappings);
   }

   public static JDRGroup load(CanvasGraphics cg, Reader reader,
      ImportSettings importSettings, TeXMappings textModeMappings,
      TeXMappings mathModeMappings)
     throws SAXException,IOException
   {
      if (importSettings == null)
      {
         importSettings = new ImportSettings(cg.getMessageDictionary());
         importSettings.type = ImportSettings.Type.SVG;
      }

      SVG svg = new SVG(importSettings);
      svg.canvasGraphics = cg;

      if (textModeMappings != null)
      {
         svg.setTextModeMappings(textModeMappings);
      }

      if (mathModeMappings != null)
      {
         svg.setMathModeMappings(mathModeMappings);
      }

      return svg.load(reader);
   }

   protected JDRGroup load(Reader reader) throws SAXException,IOException
   {
      XMLReader xr = XMLReaderFactory.createXMLReader();

      JDRGroup group = new JDRGroup(canvasGraphics);

      styNames = new Vector<String>();

      SVGHandler handler = new SVGHandler(this, group);

      xr.setContentHandler(handler);
      xr.setErrorHandler(handler);

      JDRMessage msgSys = canvasGraphics.getMessageSystem();

      xr.parse(new InputSource(reader));

      if (!styNames.isEmpty())
      {
         String preamble = canvasGraphics.getPreamble();

         StringBuilder buffer = new StringBuilder(
           preamble.length()+styNames.firstElement().length()+12);

         buffer.append(preamble);

         for (String sty : styNames)
         {
            if (sty.startsWith("["))
            {
               int idx = sty.indexOf("]");

               buffer.append(String.format("\\usepackage%s{%s}%n",
                 sty.substring(0, idx+1), sty.substring(idx+1)));
            }
            else
            {
               buffer.append(String.format("\\usepackage{%s}%n", sty));
            }
         }

         canvasGraphics.setPreamble(buffer.toString());
      }

      return group;
   }

   public static String encodeContent(String text)
   {
      return encodeContent(text, false);
   }

   public static String encodeContent(String text, boolean encodeQuotes)
   {
      return TeXJavaHelpLib.encodeHTML(text, encodeQuotes);
   }

   public static String encodeAttributeValue(String text)
   {
      return encodeAttributeValue(text, false);
   }

   public static String encodeAttributeValue(String text, boolean url)
   {
      return TeXJavaHelpLib.encodeAttributeValue(text, url);
   }

   public void setWriter(Writer writer)
   {
      this.writer = writer;
   }

   public Writer getWriter()
   {
      return writer;
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      canvasGraphics = cg;
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   public ExportSettings getExportSettings()
   {
      return exportSettings;
   }

   public ImportSettings getImportSettings()
   {
      return importSettings;
   }

   public void setTextModeMappings(TeXMappings texMappings)
   {
      this.textModeMappings = texMappings;

      importSettings.useMappings = (texMappings != null);
   }

   public TeXMappings getTextModeMappings()
   {
      return textModeMappings;
   }

   public void setMathModeMappings(TeXMappings texMappings)
   {
      this.mathModeMappings = texMappings;
   }

   public TeXMappings getMathModeMappings()
   {
      return mathModeMappings;
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

   public boolean addReferenceID(String id)
   {
      if (referenceIDs == null)
      {
         referenceIDs = new Vector<String>();
      }

      if (referenceIDs.contains(id))
      {
         return false;
      }
      else
      {
         referenceIDs.add(id);
         return true;
      }
   }

   public void print(Object text)
      throws IOException
   {
      writer.write(text.toString());
   }

   public void println(Object text)
      throws IOException
   {
      if (writer instanceof PrintWriter)
      {
         ((PrintWriter)writer).println(text);
      }
      else
      {
         writer.write(String.format("%s%n", text));
         writer.flush();
      }
   }

   public void println()
      throws IOException
   {
      if (writer instanceof PrintWriter)
      {
         ((PrintWriter)writer).println();
      }
      else
      {
         writer.write(String.format("%n"));
         writer.flush();
      }
   }

   public String transform(AffineTransform af)
   {
      return "transform=\"matrix("+af.getScaleX()+","
             + af.getShearY()+","+af.getShearX()+","
             + af.getScaleY()+","+length(af.getTranslateX())+","
             + length(af.getTranslateY())+")\"";
   }

   public String transform(double[] matrix)
   {
      return "transform=\"matrix("+matrix[0]+","
             + matrix[1]+","+matrix[2]+","
             + matrix[3]+","+length(matrix[4])+","
             + length(matrix[5])+")\"";
   }

   public static String length(JDRLength length)
   {
      return length.svg();
   }

   public String length(double length)
   {
      JDRUnit unit = getCanvasGraphics().getStorageUnit();

      return unit.svg(length);
   }

   public void savePoint(double x, double y)
     throws IOException
   {
      JDRUnit unit = getCanvasGraphics().getStorageUnit();

      double xt = x;
      double yt = y;

      print(unit.svg(xt)+" "+unit.svg(yt)+" ");
   }

   public void savePoint(Point2D p)
     throws IOException
   {
      savePoint(p.getX(), p.getY());
   }

   public void saveStoragePathData(Shape shape)
     throws IOException
   {
      saveStoragePathData(shape.getPathIterator(null));
   }

   public void saveStoragePathData(PathIterator it)
     throws IOException
   {
      double[] coords = new double[6];

      for (; !it.isDone(); it.next())
      {
         switch (it.currentSegment(coords))
         {
            case PathIterator.SEG_CLOSE:
               print("Z ");
            break;
            case PathIterator.SEG_CUBICTO:
               print("C ");
               savePoint(coords[0], coords[1]);
               savePoint(coords[2], coords[3]);
               savePoint(coords[4], coords[5]);
               println();
            break;
            case PathIterator.SEG_MOVETO:
               print("M ");
               savePoint(coords[0], coords[1]);
               println();
            break;
            case PathIterator.SEG_LINETO:
               print("L ");
               savePoint(coords[0], coords[1]);
               println();
            break;
            case PathIterator.SEG_QUADTO:
               print("Q ");
               savePoint(coords[0], coords[1]);
               savePoint(coords[2], coords[3]);
               println();
            break;
         }
      }
   }

   public void setLaTeXText(JDRText jdrText)
   {
      String text = jdrText.getText();
      String latexText = null;

      if (importSettings.useMappings)
      {
         if (mathModeMappings != null
              && text.length() > 1 && text.startsWith("$") && text.endsWith("$"))
         {
            text = text.substring(1, text.length()-1);

            latexText = "$" + mathModeMappings.applyMappings(
              text, styNames) + "$";

            jdrText.setText(text);
         }
         else if (textModeMappings != null)
         {
            latexText = textModeMappings.applyMappings(text, styNames);
         }
         else
         {
            jdrText.escapeTeXChars();
         }
      }

      if (latexText != null && !latexText.equals(text))
      {
         jdrText.setLaTeXText(latexText);
      }

   }

   private CanvasGraphics canvasGraphics;
   private Writer writer;
   private Path basePath;
   private ImportSettings importSettings;
   private ExportSettings exportSettings;

   Vector<String> styNames;
   TeXMappings textModeMappings=null;
   TeXMappings mathModeMappings=null;

   private Vector<String> referenceIDs;
}
