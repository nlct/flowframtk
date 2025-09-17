// File          : PGF.java
// Purpose       : functions to save JDRGroup as a pgfpicture environment
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
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.text.*;
import com.dickimawbooks.jdr.*;

/**
 * Functions to save JDRGroup as a pgfpicture environment for use
 * with the pgf package. Note that this library can only export
 * to LaTeX files and can't load them.
 * @author Nicola L C Talbot
 */

public class PGF extends TeX
{
   public PGF(Path basePath, Writer out)
   {
      super(basePath, out);
   }

   public PGF(File baseFile, Writer out)
   {
      this(baseFile == null ? null : baseFile.toPath(), out, new ExportSettings());
   }

   public PGF(Path basePath, Writer out, ExportSettings exportSettings)
   {
      super(basePath, out, exportSettings);
   }

   public PGF(File baseFile, Writer out, ExportSettings exportSettings)
   {
      this(baseFile == null ? null : baseFile.toPath(), out, exportSettings);
   }

   /**
    * Saves the image as a pgfpicture environment.
    * @param allObjects all the objects that constitute the image
    * @throws IOException if I/O error occurs
    */
   public void save(JDRGroup allObjects)
      throws IOException
   {
      boolean useTypeblockAsBoundingBox = 
        (exportSettings.bounds == ExportSettings.Bounds.TYPEBLOCK);

      CanvasGraphics cg = allObjects.getCanvasGraphics();
      JDRMessage msgSys = cg.getMessageSystem();
      MessageInfoPublisher publisher = msgSys.getPublisher();

      boolean indeter = (allObjects.size() <= 1);

      publisher.publishMessages(MessageInfo.createIndeterminate(indeter));

      if (!indeter)
      {
         publisher.publishMessages(MessageInfo.createMaxProgress(allObjects.size()));
      }

      double storagePaperHeight = cg.bpToStorage(cg.getPaperHeight());

      // transformation to convert from left handed
      // co-ordinate system to right-hand co-ordinate system

      affineTransform = new AffineTransform(
         1, 0, 0, -1, 0, storagePaperHeight);

      clearObjectArgList();

      println("\\begin{pgfpicture}");

      int objectId = writeStartObject(allObjects);

      FlowFrame flowframe = allObjects.getFlowFrame();

      if (useTypeblockAsBoundingBox && flowframe != null)
      {
         double storagePaperWidth = cg.bpToStorage(cg.getPaperWidth());

         println(String.format("\\pgfpathmoveto{%s}",
                 point(cg, flowframe.getLeft(), 
                           storagePaperHeight-flowframe.getTop())));
         println(String.format("\\pgfpathlineto{%s}",
                 point(cg, storagePaperWidth-flowframe.getRight(),
                           storagePaperHeight-flowframe.getBottom())));
      }
      else
      {
         BBox box = allObjects.getStorageBBox();

         println(String.format("\\pgfpathmoveto{%s}",
                 point(cg, box.getMinX(), storagePaperHeight-box.getMaxY())));
         println(String.format("\\pgfpathlineto{%s}",
                 point(cg, box.getMaxX(), storagePaperHeight-box.getMinY())));
      }

      println("\\pgfusepath{use as bounding box}");

      for (int i = 0; i < allObjects.size(); i++)
      {
         publisher.publishMessages(MessageInfo.createIncProgress());

         JDRCompleteObject obj = allObjects.get(i);
         int idx = writeStartObject(obj);
         obj.savePgf(this);
         writeEndObject(idx, obj);
      }

      writeEndObject(objectId, allObjects);
      println("\\end{pgfpicture}");
   }

   /**
    * Saves the image as a pgfpicture environment in a complete
    * document.
    * @param allObjects all the objects that constitute the image
    * @throws IOException if I/O error occurs
    */
   public void saveDoc(JDRGroup allObjects)
      throws IOException
   {
      saveDoc(allObjects, null);
   }

   public void saveDoc(JDRGroup allObjects, String extraPreamble)
      throws IOException
   {
      boolean encapsulate =
        (exportSettings.bounds != ExportSettings.Bounds.PAPER);

      boolean useTypeblockAsBoundingBox = 
        (exportSettings.bounds == ExportSettings.Bounds.TYPEBLOCK);

      CanvasGraphics cg = allObjects.getCanvasGraphics();
      JDRMessage msgSys = cg.getMessageSystem();
      MessageInfoPublisher publisher = msgSys.getPublisher();

      boolean indeter = (allObjects.size() <= 1);

      publisher.publishMessages(MessageInfo.createIndeterminate(indeter));

      if (!indeter)
      {
         publisher.publishMessages(MessageInfo.createMaxProgress(allObjects.size()));
      }

      BBox box;

      double storagePaperHeight = cg.bpToStorage(cg.getPaperHeight());

      FlowFrame flowframe = allObjects.getFlowFrame();

      if (useTypeblockAsBoundingBox && flowframe != null)
      {
         double storagePaperWidth = cg.bpToStorage(cg.getPaperWidth());

         box = new BBox(cg, flowframe.getLeft(), flowframe.getTop(),
                        storagePaperWidth-flowframe.getRight(),
                        storagePaperHeight-flowframe.getBottom());
      }
      else
      {
         box = allObjects.getStorageBBox();
      }

      JDRUnit unit = cg.getStorageUnit();

      int normalsize = (int)cg.getLaTeXNormalSize();

      // transformation to convert from left handed
      // co-ordinate system to right-hand co-ordinate system

      affineTransform = new AffineTransform(
         1, 0, 0, -1, 0, storagePaperHeight);

      if (exportSettings.docClass != null)
      {
         print("\\documentclass[");

         printNormalFontSizeOption(cg, normalsize, exportSettings.docClass);

         print("]{");
         print(exportSettings.docClass);
         println("}");
      }
      else if (cg.hasDocClass())
      {
         print("\\documentclass[");

         printNormalFontSizeOption(cg, normalsize, cg.getDocClass());

         print("]{");
         print(cg.getDocClass());
         println("}");
      }
      else
      {
         if (normalsize >= 10 || normalsize <= 12)
         {
            println("\\documentclass["
              +normalsize+"pt]{article}");
         }
         else if (normalsize == 25)
         {
            println("\\documentclass{a0poster}");
         }
         else
         {
            println("\\documentclass["
              +normalsize+"pt]{extarticle}");
         }
      }

      println();

      if (extraPreamble != null && !extraPreamble.isEmpty())
      {
         println(extraPreamble);
      }

      writePreambleCommands(allObjects, true);

      println();

      double boxHeight = box.getHeight();

      double baselineskip = cg.getLaTeXFontBase()
        .getBaselineskip(LaTeXFontBase.NORMALSIZE);

      String height;

      if (unit.toPt(boxHeight) > baselineskip)
      {
         height = length(cg, boxHeight);
      }
      else
      {
         height = ""+Math.ceil(baselineskip)+"pt";
      }

      if (encapsulate)
      {
         println("\\usepackage[paperwidth="+length(cg, box.getWidth())
          +",paperheight="+height
          +",noheadfoot,nomarginpar,top=0pt,left=0pt,right=0pt,bottom=0pt"
          +"]{geometry}");
      }
      else
      {
         JDRPaper paper = cg.getPaper();

         println("\\usepackage["+paper.tex(cg)
          +",top="+length(cg, box.getMinY())
          +",left="+length(cg, box.getMinX())
          +",width="+length(cg, box.getWidth())
          +",height="+height
          +"]{geometry}");
      }

      writeMidPreambleCommands(allObjects);

      // Since user may prefer \hypersetup to \pdfinfo,
      // the image information has been moved after the
      // mid-preamble to allow hyperref to be loaded after flowfram

      writeDocInfo(allObjects.getDescription());

      println("\\pagestyle{empty}");
      println();

      if (!isFlowframTkStyUsed())
      {
         comment("This is just in case numerical rounding errors ");
         comment("cause the image to be marginally larger than the ");
         comment("typeblock, which can cause a spurious blank page:");
         println("\\newcommand{\\jdrimagebox}[1]{\\vbox to \\textheight{\\hbox to \\textwidth{#1}}}");

      }

      println();

      writeEndPreambleCommands(allObjects);

      println("\\begin{document}");

      if (!isFlowframTkStyUsed())
      {
        println("\\noindent");
      }

      clearObjectArgList();

      println("\\jdrimagebox{\\begin{pgfpicture}");

      int objectId = writeStartObject(allObjects);

      println(String.format("\\pgfpathmoveto{%s}",
              point(cg, box.getMinX(), storagePaperHeight-box.getMaxY())));
      println(String.format("\\pgfpathlineto{%s}",
              point(cg, box.getMaxX(), storagePaperHeight-box.getMinY())));

      println("\\pgfusepath{use as bounding box}");


      for (int i = 0; i < allObjects.size(); i++)
      {
         publisher.publishMessages(MessageInfo.createIncProgress());

         JDRCompleteObject obj = allObjects.get(i);
         int idx = writeStartObject(obj);
         obj.savePgf(this);
         writeEndObject(idx, obj);
      }

      writeEndObject(objectId, allObjects);

      println("\\end{pgfpicture}}");

      println("\\end{document}");
   }

}
