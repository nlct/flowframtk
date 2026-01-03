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

   protected SVG(Path basePath, PrintWriter out, ExportSettings exportSettings)
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
   public static void save(JDRGroup image, String title, PrintWriter out,
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
      ImportSettings importSettings)
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

      return load(cg, r, importSettings);
   }

   public static JDRGroup load(CanvasGraphics cg, Reader reader,
      ImportSettings importSettings)
     throws SAXException,IOException
   {
      if (importSettings == null)
      {
         importSettings = new ImportSettings(cg.getMessageDictionary());
         importSettings.type = ImportSettings.Type.SVG;
      }

      XMLReader xr = XMLReaderFactory.createXMLReader();

      JDRGroup group = new JDRGroup(cg);

      SVG svg = new SVG(importSettings);
      svg.canvasGraphics = cg;

      SVGHandler handler = new SVGHandler(svg, group);

      xr.setContentHandler(handler);
      xr.setErrorHandler(handler);

      JDRMessage msgSys = cg.getMessageSystem();

      xr.parse(new InputSource(reader));

      return group;
   }

   public static String encodeContent(String text)
   {
      return TeXJavaHelpLib.encodeHTML(text, false);
   }

   public static String encodeAttributeValue(String text)
   {
      return TeXJavaHelpLib.encodeAttributeValue(text, false);
   }

   public void setWriter(PrintWriter writer)
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
      writer.print(text);
   }

   public void println(Object text)
      throws IOException
   {
      writer.println(text);
   }

   public void println()
      throws IOException
   {
      writer.println();
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

      writer.print(unit.svg(xt)+" "+unit.svg(yt)+" ");
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
               writer.print("Z ");
            break;
            case PathIterator.SEG_CUBICTO:
               writer.print("C ");
               savePoint(coords[0], coords[1]);
               savePoint(coords[2], coords[3]);
               savePoint(coords[4], coords[5]);
               writer.println();
            break;
            case PathIterator.SEG_MOVETO:
               writer.print("M ");
               savePoint(coords[0], coords[1]);
               writer.println();
            break;
            case PathIterator.SEG_LINETO:
               writer.print("L ");
               savePoint(coords[0], coords[1]);
               writer.println();
            break;
            case PathIterator.SEG_QUADTO:
               writer.print("Q ");
               savePoint(coords[0], coords[1]);
               savePoint(coords[2], coords[3]);
               writer.println();
            break;
         }
      }
   }

   private CanvasGraphics canvasGraphics;
   private PrintWriter writer;
   private Path basePath;
   private ImportSettings importSettings;
   private ExportSettings exportSettings;

   private Vector<String> referenceIDs;
}
