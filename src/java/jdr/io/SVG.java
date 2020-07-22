// File          : SVG.java
// Purpose       : functions to save JDRGroup as SVG file
// Creation Date : 1st February 2006
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
import java.nio.file.Path;

import java.awt.Shape;
import java.awt.geom.*;
import java.util.Enumeration;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.svg.*;

/**
 * Contains single method to write image to SVG file. This needs
 * more work as I haven't been able to test it for a while.
 * @author Nicola L C Talbot
 */

public class SVG
{
   protected SVG(Path basePath, PrintWriter out)
   {
      this.basePath = basePath;
      setWriter(out);
   }

   /**
    * Saves image to SVG file.
    * @param image image to save
    * @param title image title
    * @param out output stream
    * @throws IOException if I/O error occurs
    */
   public static void save(JDRGroup image, String title, PrintWriter out)
      throws IOException
   {
      SVG svg = new SVG(null, out);

      CanvasGraphics cg = image.getCanvasGraphics();

      svg.setCanvasGraphics(cg);

      BBox box = image.getStorageBBox();

      double storagePaperHeight = cg.bpToStorage(cg.getPaperHeight());

      // transformation to convert from left handed
      // co-ordinate system to right-hand co-ordinate system

      svg.setTransform(new AffineTransform(
         1, 0, 0, -1, 0, storagePaperHeight));


      out.println("<?xml version=\"1.0\" standalone=\"no\"?>");
      out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"");
      out.println("         \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
      out.println("<svg width=\""+svg.length(box.getWidth())+"\" height=\""
         + svg.length(box.getHeight()) + "\"");
      out.println("     viewBox=\""
         + svg.length(box.getMinX()) + " " + svg.length(box.getMinY()) + " "
         + svg.length(box.getMaxX()) + " " + svg.length(box.getMaxY())+"\"");
//      out.println("     transform=\"scale(1.25,1.25)\"");
      out.println("     version=\"1.1\"");
      out.println("     xmlns=\"http://www.w3.org/2000/svg\"");
      out.println("     xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
      out.println("   <title>"+title+"</title>");

      String desc = image.getDescription();

      if (!desc.equals(""))
      {
         out.println("   <desc>"+desc+"</desc>");
      }

      out.println("   <defs>");

      JDRBasicStroke.svgDefs(svg, image);
      JDRGradient.svgDefs(svg, image);
      JDRRadial.svgDefs(svg, image);

      out.println("   </defs>");

      for (int i = 0; i < image.size(); i++)
      {
         image.get(i).saveSVG(svg);
      }
      out.println("</svg>");
   }

   public static JDRGroup load(CanvasGraphics cg, File file)
     throws SAXException,IOException
   {
      XMLReader xr = XMLReaderFactory.createXMLReader();

      JDRGroup group = new JDRGroup(cg);

      SVGHandler handler = new SVGHandler(group);

      xr.setContentHandler(handler);
      xr.setErrorHandler(handler);

      JDRMessage msgSys = cg.getMessageSystem();

      FileReader r = new FileReader(file);

      msgSys.getPublisher().publishMessages(
       MessageInfo.createMessage(
          msgSys.getMessageWithAlt("Loading ''{0}''",
          "info.loading",         
          file.getAbsolutePath())));

      xr.parse(new InputSource(r));

      return group;
   }

   public AffineTransform getTransform()
   {
      return affineTransform;
   }

   public void setTransform(AffineTransform af)
   {
      affineTransform = af;
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

   public Path relativize(String filename)
   {
      Path path = (new File(filename)).toPath();

      if (basePath == null || !path.isAbsolute())
      {
         return path;
      }

      return basePath.relativize(path).normalize();
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
             + af.getScaleY()+","+af.getTranslateX()+","
             + af.getTranslateY()+")\"";
   }

   public String transform(double[] matrix)
   {
      return "transform=\"matrix("+matrix[0]+","
             + matrix[1]+","+matrix[2]+","
             + matrix[3]+","+matrix[4]+","
             + matrix[5]+")\"";
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

      if (affineTransform != null)
      {
         xt = affineTransform.getScaleX()*x
            + affineTransform.getShearX()*y
            + affineTransform.getTranslateX();
         yt = affineTransform.getShearY()*x
            + affineTransform.getScaleY()*y
            + affineTransform.getTranslateY();
      }

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
      PathIterator it = shape.getPathIterator(null);
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
   private AffineTransform affineTransform;
   private PrintWriter writer;
   private Path basePath;
}
