// File          : FLF.java
// Purpose       : functions to save JDRGroup as a LaTeX2e package
//                 or class that loads the flowfram package
// Creation Date : 27th February 2006
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
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Functions to save JDRGroup as a LaTeX2e package or class
 * that loads the flowfram package. Only objects that have 
 * {@link JDRCompleteObject#flowframe} set will be saved. Note that
 * this library can't load LaTeX packages or classes, but can only create them.
 * The typeblock for the document should be specified by the
 * flowframe information for the entire image.
 * @author Nicola L C Talbot
 */
public class FLF extends TeX
{
   public FLF(File baseFile, Writer out)
   {
      this(baseFile.toPath(), out);
   }

   public FLF(Path basePath, Writer out)
   {
      super(basePath, out);
   }

   public FLF(File baseFile, Writer out, boolean useFlowframTkSty)
   {
      this(baseFile.toPath(), out, useFlowframTkSty);
   }

   public FLF(Path basePath, Writer out, boolean useFlowframTkSty)
   {
      super(basePath, out, useFlowframTkSty);

      thumbtabObjects = new HashMap<String,JDRCompleteObject>();
   }

   /**
    * Exports flowframe information.
    * The typeblock for the document should be specified by the
    * flowframe information for the image
    * (<code>group.flowframe</code>).
    * @param group the image containing flow frame information
    * @param styName the package or class file name (including
    * extension)
    * @param useHPaddingShapepar use \Shapepar instead of \shapepar
    * @throws IOException if I/O error occurs
    * @throws MissingTypeBlockException if the typeblock is missing
    * (i.e. <code>group.flowframe==null</code>)
    * @throws InvalidShapeException if any of the frames use
    * <code>\parshape</code> or <code>\shapepar</code> but the
    * parameters can't be computed from the frame's shape
    * @throws MissplacedTypeBlockException if any of the objects
    * within the image contain typeblock flow frame data (only
    * <code>group.flowframe</code> should have the type
    * {@link FlowFrame#TYPEBLOCK})
    */
   public void save(JDRGroup group, String styName,  
     boolean useHPaddingShapepar)
   throws IOException,
          MissingTypeBlockException,
          InvalidShapeException,
          MissplacedTypeBlockException
   {
      CanvasGraphics cg = group.getCanvasGraphics();
      JDRMessage msgSys = cg.getMessageSystem();
      MessageInfoPublisher publisher = msgSys.getPublisher();
      this.useHPaddingShapepar = useHPaddingShapepar;

      this.group = group;
      JDRUnit unit = cg.getStorageUnit();
      int objectId = 0;

      FlowFrame typeblock = group.getFlowFrame();

      if (typeblock == null)
      {
         throw new MissingTypeBlockException(cg);
      }

      BBox box = group.getStorageBBox();

      println("\\NeedsTeXFormat{LaTeX2e}");

      int idx = styName.lastIndexOf(".");

      String basename ;

      if (idx > 0)
      {
         basename = styName.substring(0, idx);
      }
      else
      {
         basename = styName;
      }

      boolean isCls = styName.endsWith(".cls");

      String docClass = null;

      int normalsize = (int)cg.getLaTeXNormalSize();

      if (isCls)
      {
         if (cg.hasDocClass())
         {
            docClass = cg.getDocClass();
         }
         else
         {
            if (normalsize >= 10 || normalsize <= 12)
            {
               docClass = "article";
            }
            else if (normalsize == 25)
            {
               docClass = "a0poster";
            }
            else
            {
               docClass = "extarticle";
            }
         }
      }

      println("\\Provides"+(isCls ? "Class" : "Package")+"{"
                  +basename+"}");

      writePreambleCommands(group, false);

      if (useFlowframTkSty)
      {
         println("\\FlowFramTkDeclareKeys{"+basename+"}");

         if (docClass != null)
         {
            println("\\DeclareUnknownKeyHandler["+basename+"]{");
            println("  \\IfValueTF{#2}");
            println("  {\\PassOptionsToClass{#1=#2}{"+docClass+"}}");
            println("  {\\PassOptionsToClass{#1}{"+docClass+"}}");
            println("}");
         }

         if (cg.useAbsolutePages())
         {
            println("\\FlowFramTkSetKey{pages=absolute}");
         }
         else
         {
            println("\\FlowFramTkSetKey{pages=relative}");
         }

         println("\\ProcessKeyOptions["+basename+"]");
      }
      else
      {
         println("\\DeclareOption{draft}{\\PassOptionsToPackage{draft}{flowfram}}");
         println("\\DeclareOption{final}{\\PassOptionsToPackage{final}{flowfram}}");
         println("\\DeclareOption{rotate}{\\PassOptionsToPackage{rotate}{flowfram}}");
         println("\\DeclareOption{norotate}{\\PassOptionsToPackage{norotate}{flowfram}}");
         println("\\DeclareOption{ttbtitle}{\\PassOptionsToPackage{ttbtitle}{flowfram}}");
         println("\\DeclareOption{ttbnotitle}{\\PassOptionsToPackage{ttbnotitle}{flowfram}}");
         println("\\DeclareOption{ttbnum}{\\PassOptionsToPackage{ttbnum}{flowfram}}");
         println("\\DeclareOption{ttbnonum}{\\PassOptionsToPackage{ttbnonum}{flowfram}}");
         println("\\DeclareOption{color}{\\PassOptionsToPackage{color}{flowfram}}");
         println("\\DeclareOption{nocolor}{\\PassOptionsToPackage{nocolor}{flowfram}}");

         if (docClass != null)
         {
           println("\\DeclareOption*{\\PassOptionsToClass{\\CurrentOption}{"+docClass+"}}");
         }

         println("\\ProcessOptions");

         if (cg.useAbsolutePages())
         {
            println("\\PassOptionsToPackage{pages=absolute}{flowfram}");
         }
         else
         {
            println("\\PassOptionsToPackage{pages=relative}{flowfram}");
         }
      }

      if (docClass != null)
      {
         if (docClass.equals("a0poster"))
         {
            println("\\LoadClass{a0poster}");
         }
         else
         {
            println("\\LoadClass[" +normalsize+"pt]{"+docClass+"}");
         }
      }

      println("\\RequirePackage["+cg.getPaper().tex(cg)+"]{geometry}");

      double bpToStorage = cg.bpToStorage(1.0);

      double left = typeblock.getLeft();
      double right = typeblock.getRight();
      double top = typeblock.getTop();
      double bottom = typeblock.getBottom();
      double width = bpToStorage*cg.getPaperWidth()-(left+right);
      double height = bpToStorage*cg.getPaperHeight()-(top+bottom);

      typeblockRect = new Rectangle2D.Double(left,top,width,height);

      baselineskip = cg.getStorageBaselineskip(LaTeXFontBase.NORMALSIZE);

      typeblock.tex(this, group, typeblockRect, baselineskip, false);

      println("\\RequirePackage{flowfram}");

      if (cg.hasMidPreamble())
      {
         String midPreamble = cg.getMidPreamble();

         midPreamble = midPreamble.replaceAll("\\\\usepackage\\b", "\\\\RequirePackage");

         println(midPreamble);
      }

      publisher.publishMessages(MessageInfo.createIndeterminate(false));
      publisher.publishMessages(MessageInfo.createMaxProgress(group.size()+1));

      resetThumbtabs();

      for (int i = 0; i < group.size(); i++)
      {
         publisher.publishMessages(MessageInfo.createIncProgress());

         JDRCompleteObject object = group.get(i);
         FlowFrame flowframe = object.getFlowFrame();

         if (flowframe != null && flowframe.getType() == FlowFrame.TYPEBLOCK)
         {
            throw new MissplacedTypeBlockException(cg);
         }

         object.saveFlowframe(this, typeblockRect, baselineskip, 
            useHPaddingShapepar);
      }

      if (useFlowframTkSty)
      {
         printFlowframTkStyHeaderFooter();
      }
      else
      {
         printHeaderFooter();
      }

      printThumbtabs();

      publisher.publishMessages(MessageInfo.createIncProgress());

      if (cg.hasEndPreamble())
      {
         String endPreamble = cg.getEndPreamble();

         endPreamble = endPreamble.replaceAll("\\\\usepackage\\b", "\\\\RequirePackage");

         println(endPreamble);
      }

      println("\\endinput");
   }

   public void saveCompleteDoc(JDRGroup group, String extraPreamble,
     boolean useHPaddingShapepar)
   throws IOException,
          MissingTypeBlockException,
          InvalidShapeException,
          MissplacedTypeBlockException
   {
      CanvasGraphics cg = group.getCanvasGraphics();
      JDRMessage msgSys = cg.getMessageSystem();
      MessageInfoPublisher publisher = msgSys.getPublisher();
      this.useHPaddingShapepar = useHPaddingShapepar;

      this.group = group;
      JDRUnit unit = cg.getStorageUnit();
      int objectId = 0;

      FlowFrame typeblock = group.getFlowFrame();

      if (typeblock == null)
      {
         throw new MissingTypeBlockException(cg);
      }

      BBox box = group.getStorageBBox();

      String docClass = null;

      int normalsize = (int)cg.getLaTeXNormalSize();

      if (cg.hasDocClass())
      {
         docClass = cg.getDocClass();
      }
      else
      {
         if (normalsize >= 10 || normalsize <= 12)
         {
            docClass = "article";
         }
         else if (normalsize == 25)
         {
            docClass = "a0poster";
         }
         else
         {
            docClass = "extarticle";
         }
      }

      if (docClass != null)
      {
         if (docClass.equals("a0poster"))
         {
            println("\\documentclass{a0poster}");
         }
         else
         {
            println("\\documentclass[" +normalsize+"pt]{"+docClass+"}");
         }
      }

      if (extraPreamble != null && !extraPreamble.isEmpty())
      {
         println(extraPreamble);
      }

      writePreambleCommands(group, true);

      println("\\usepackage["+cg.getPaper().tex(cg)+"]{geometry}");

      print("\\usepackage[");

      if (cg.useAbsolutePages())
      {
         println("pages=absolute");
      }
      else
      {
         println("pages=relative");
      }

      println("]{flowfram}");

      double bpToStorage = cg.bpToStorage(1.0);

      double left = typeblock.getLeft();
      double right = typeblock.getRight();
      double top = typeblock.getTop();
      double bottom = typeblock.getBottom();
      double width = bpToStorage*cg.getPaperWidth()-(left+right);
      double height = bpToStorage*cg.getPaperHeight()-(top+bottom);

      typeblockRect = new Rectangle2D.Double(left,top,width,height);

      baselineskip = cg.getStorageBaselineskip(LaTeXFontBase.NORMALSIZE);

      typeblock.tex(this, group, typeblockRect, baselineskip, false);

      if (cg.hasMidPreamble())
      {
         String midPreamble = cg.getMidPreamble();

         midPreamble = midPreamble.replaceAll("\\\\RequirePackage\\b", "\\\\usepackage");

         println(midPreamble);
      }

      // Since user may prefer \hypersetup to \pdfinfo,
      // the image information has been moved after the
      // mid-preamble to allow hyperref to be loaded after flowfram

      writeDocInfo(group.getDescription());

      minPage = 1;
      numFlows = 0;

      println("\\makeatletter");

      publisher.publishMessages(MessageInfo.createIndeterminate(false));
      publisher.publishMessages(MessageInfo.createMaxProgress(group.size()+1));

      resetThumbtabs();

      for (int i = 0; i < group.size(); i++)
      {
         publisher.publishMessages(MessageInfo.createIncProgress());

         writeAndUpdateMinPage(group.get(i), typeblockRect,
            baselineskip, useHPaddingShapepar);
      }

      printHeaderFooter();

      printThumbtabs();

      println("\\makeatother");

      publisher.publishMessages(MessageInfo.createIncProgress());

      if (cg.hasEndPreamble())
      {
         String endPreamble = cg.getEndPreamble();

         println(endPreamble);
      }

      if (numFlows == 0)
      {
         println("\\onecolumn");
      }

      println("\\begin{document}");

      String docBody = cg.getDocBody();

      if (docBody == null || docBody.isEmpty())
      {
         for (int i = 0; i < minPage; i++)
         {
            if (i > 0)
            {
               println("\\clearpage");
            }

            println("\\null");
         }
      }
      else
      {
         println(docBody);
      }

      println("\\end{document}");
   }

   protected void printHeaderFooter() throws IOException
   {
      CanvasGraphics cg = group.getCanvasGraphics();
      String list = "";
      String headerlist = "";
      String footerlist = "";

      String psheadingsoddhead = "{\\jdrheadingfmt\\rightmark}\\hfill\\thepage";
      String psheadingsevenhead = "\\thepage\\hfill{\\jdrheadingfmt\\leftmark}";

      String psflowframtkoddhead = "\\flowframtkoddheaderfmt{\\rightmark}";
      String psflowframtkevenhead = "\\flowframtkevenheaderfmt{\\leftmark}";

      String headerlabel = cg.getHeaderLabel();

      FlowFrame header = group.getFlowFrame(FlowFrame.DYNAMIC, headerlabel);

      String evenheaderlabel = cg.getEvenHeaderLabel();
      FlowFrame evenheader = group.getFlowFrame(
            FlowFrame.DYNAMIC, evenheaderlabel);

      if (header != null)
      {
         println("\\renewcommand{\\@dothehead}{}%");
         println("\\renewcommand{\\@dodynamicthehead}{%");

         String contents = header.getContents();

         if (contents != null)
         {
            psheadingsoddhead = contents
                              + "{{\\jdrheadingfmt\\rightmark}\\hfill\\thepage}";
            psflowframtkoddhead = "\\flowframtkoddheaderfmt{"
                                 + contents + "\\rightmark}";
         }

         if (evenheader == null)
         {
            println(" \\setdynamiccontents*{"+headerlabel+"}{\\@thehead}%");
            list = headerlabel;

            if (contents != null)
            {
               psheadingsevenhead = contents
                              + "{\\thepage\\hfill{\\jdrheadingfmt\\leftmark}}";
               psflowframtkevenhead = "\\flowframtkevenheaderfmt{"
                                    + contents + "\\leftmark}";
            }
         }
         else
         {
            println(" \\setdynamiccontents*{"+headerlabel+"}{\\@oddhead}%");
            println(" \\setdynamiccontents*{"+evenheaderlabel+"}{\\@evenhead}%");
            list = headerlabel+","+evenheaderlabel;

            contents = evenheader.getContents();

            if (contents != null)
            {
               psheadingsevenhead = contents
                              + "{\\thepage\\hfill{\\jdrheadingfmt\\leftmark}}";
               psflowframtkevenhead = "\\flowframtkevenheaderfmt{"
                                     + contents + "\\leftmark}";
            }
         }

         println("}%");
      }
      else if (evenheader != null)
      {
         list = evenheaderlabel;
         println("\\renewcommand{\\@dothehead}{}%");
         println("\\renewcommand{\\@dodynamicthehead}{%");
         println(" \\setdynamiccontents*{"+evenheaderlabel+"}{\\@evenhead}%");
         println("}%");

         String contents = evenheader.getContents();

         if (contents != null)
         {
            psheadingsevenhead = contents
                           + "{\\thepage\\hfil{\\jdrheadingfmt\\leftmark}}";

            psflowframtkevenhead = "\\flowframtkevenheaderfmt{"
                                  + contents + "\\leftmark}";
         }
      }

      headerlist = list;

      String psplainoddfoot = "\\flowframtkoddfooterfmt{\\thepage}";
      String psplainevenfoot = "\\flowframtkevenfooterfmt{\\thepage}";

      String footerlabel = cg.getFooterLabel();

      FlowFrame footer = group.getFlowFrame(FlowFrame.DYNAMIC, footerlabel);

      String evenfooterlabel = cg.getEvenFooterLabel();
      FlowFrame evenfooter = group.getFlowFrame(
            FlowFrame.DYNAMIC, evenfooterlabel);

      if (footer != null)
      {
         if (!list.isEmpty()) list = list+",";

         println("\\renewcommand{\\@dothefoot}{}%");
         println("\\renewcommand{\\@dodynamicthefoot}{%");

         String contents = footer.getContents();

         if (contents != null)
         {
            psplainoddfoot = "\\flowframtkoddfooterfmt{" + contents+"\\thepage}";
         }

         if (evenfooter == null)
         {
            list += footerlabel;

            footerlist = footerlabel;

            println(" \\setdynamiccontents*{"+footerlabel+"}{\\@thefoot}%");

            if (contents != null)
            {
               psplainevenfoot = "\\flowframtkevenfooterfmt{" + contents+"\\thepage}";
            }

         }
         else
         {
            footerlist = footerlabel+","+evenfooterlabel;

            list += footerlist;

            println(" \\setdynamiccontents*{"+footerlabel+"}{\\@oddfoot}%");
            println(" \\setdynamiccontents*{"+evenfooterlabel+"}{\\@evenfoot}%");

            contents = evenfooter.getContents();

            if (contents != null)
            {
               psplainevenfoot = "\\flowframtkevenfooterfmt{"
                               + contents+"\\thepage}";
            }
         }

         println("}%");
      }
      else if (evenfooter != null)
      {
         if (list.isEmpty())
         {
            list = evenfooterlabel;
         }
         else
         {
            list += ","+evenfooterlabel;
         }

         footerlist = evenfooterlabel;

         println("\\renewcommand{\\@dothefoot}{}%");
         println("\\renewcommand{\\@dodynamicthefoot}{%");
         println(" \\setdynamiccontents*{"+evenfooterlabel+"}{\\@evenfoot}%");
         println("}%");

         String contents = evenfooter.getContents();

         if (contents != null)
         {
            psplainevenfoot = "\\flowframtkevenfooterfmt{"
                            + contents+"\\thepage}";
         }
      }

      if (!list.isEmpty())
      {
         println("\\renewcommand*{\\thispagestyle}[1]{%");
         println("  \\@ifundefined{ps@#1}\\undefinedpagestyle");
         println("  {%");
         println("    \\global\\@specialpagetrue");
         println("    \\gdef\\@specialstyle {#1}%");
         println("    \\@ifundefined{thisps@extra@#1}%");
         println("     {\\@nameuse{thisps@extra@other}}%");
         println("     {\\@nameuse{thisps@extra@#1}}%");
         println("  }%");
         println("}");

         println("\\newcommand*{\\thisps@extra@other}{%");
         println(" \\setdynamicframe*{"+list+"}{hidethis=false}%");
         println("}");
         println("\\newcommand*{\\thisps@extra@empty}{%");
         println(" \\setdynamicframe*{"+list+"}{hidethis=true}%");
         println("}");

         println("\\newcommand*{\\thisps@extra@plain}{%");

         if (!headerlist.isEmpty())
         {
            println("  \\setdynamicframe*{"+headerlist+"}{hidethis=true}%");
         }

         if (!footerlist.isEmpty())
         {
            println("  \\setdynamicframe*{"+footerlist+"}{hidethis=false}%");
         }

         println("}");

         println("  \\newcommand*{\\thisps@extra@headings}{%");

         if (!footerlist.isEmpty())
         {
            println("    \\setdynamicframe*{"+footerlist+"}{hidethis=true}%");
         }

         if (!headerlist.isEmpty())
         {
            println("    \\setdynamicframe*{"+headerlist+"}{hidethis=false}%");
         }

         println("  }%");

         println("  \\newcommand*{\\thisps@extra@myheadings}{%");

         if (!footerlist.isEmpty())
         {
            println("    \\setdynamicframe*{"+footerlist+"}{hidethis=true}%");
         }

         if (!headerlist.isEmpty())
         {
            println("    \\setdynamicframe*{"+headerlist+"}{hidethis=false}%");
         }

         println("  }%");


         println("\\renewcommand*{\\pagestyle}[1]{%");
         println("  \\@ifundefined{ps@#1}\\undefinedpagestyle");
         println("  {%");
         println("    \\@nameuse{ps@#1}%");
         println("    \\@ifundefined{ps@extra@#1}%");
         println("     {\\@nameuse{ps@extra@other}}%");
         println("     {\\@nameuse{ps@extra@#1}}%");
         println("  }%");
         println("}");

         println("\\newcommand*{\\ps@extra@other}{%");
         println(" \\setdynamicframe*{"+list+"}{hide=false}%");
         println("}");
         println("\\newcommand*{\\ps@extra@empty}{%");
         println(" \\setdynamicframe*{"+list+"}{hide=true}%");
         println("}");

         // Redefine plain style

         println("\\renewcommand*{\\ps@plain}{%");
         println("  \\let\\@mkboth\\@gobbletwo");
         println("  \\let\\@oddhead\\@empty");
         println("  \\let\\@evenhead\\@empty");
         println("  \\def\\@oddfoot{"+psplainoddfoot+"}%");
         println("  \\def\\@evenfoot{"+psplainevenfoot+"}%");
         println("}");

         println("\\newcommand*{\\ps@extra@plain}{%");

         if (!headerlist.isEmpty())
         {
            println("  \\setdynamicframe*{"+headerlist+"}{hide=true}%");
         }

         if (!footerlist.isEmpty())
         {
            println("  \\setdynamicframe*{"+footerlist+"}{hide=false}%");
         }

         println("}");

         println("\\providecommand*{\\jdrheadingfmt}{\\slshape}");
         println("\\providecommand*{\\jdrheadingcase}{\\MakeUppercase}");
         println("\\providecommand*{\\flowframtkoddheaderfmt}[1]{\\reset@font\\hfill#1}");
         println("\\providecommand*{\\flowframtkevenheaderfmt}[1]{\\reset@font#1\\hfill}");
         println("\\providecommand*{\\flowframtkoddfooterfmt}[1]{\\reset@font\\hfil#1\\hfil}");
         println("\\providecommand*{\\flowframtkevenfooterfmt}[1]{\\reset@font\\hfil#1\\hfil}");

         println("\\@ifundefined{chapter}");
         println("{%");

         // Redefine headings style (no chapters)

         println("  \\renewcommand*{\\ps@headings}{%");
         println("    \\let\\@oddfoot\\@empty");
         println("    \\let\\@evenfoot\\@empty");
         println("    \\def\\@oddhead{"+psheadingsoddhead+"}%");

         if (evenheader == null)
         {
            println("    \\let\\@evenhead\\@oddhead");
         }
         else
         {
            println("    \\def\\@evenhead{"+psheadingsevenhead+"}%");
         }

         println("    \\let\\@mkboth\\markboth");
         println("    \\def\\sectionmark##1{%");
         println("      \\markright{\\jdrheadingcase{\\ifnum\\c@secnumdepth >\\m@ne\\thesection\\quad\\fi ##1}}}%");

         println("  }%");

         println("  \\newcommand*{\\ps@extra@headings}{%");

         if (!footerlist.isEmpty())
         {
            println("    \\setdynamicframe*{"+footerlist+"}{hide=true}%");
         }

         if (!headerlist.isEmpty())
         {
            println("    \\setdynamicframe*{"+headerlist+"}{hide=false}%");
         }

         println("  }%");

         // Redefine myheadings style (no chapters)

         println("  \\renewcommand*{\\ps@myheadings}{%");
         println("    \\let\\@oddfoot\\@empty");
         println("    \\let\\@evenfoot\\@empty");
         println("    \\def\\@oddhead{"+psheadingsoddhead+"}%");
         println("    \\def\\@evenhead{"+psheadingsevenhead+"}%");

         println("    \\let\\@mkboth\\@gobbletwo");
         println("    \\let\\sectionmark\\@gobble");
         println("    \\let\\subsectionmark\\@gobble");

         println("  }%");

         println("  \\newcommand*{\\ps@extra@myheadings}{%");

         if (!footerlist.isEmpty())
         {
            println("    \\setdynamicframe*{"+footerlist+"}{hide=true}%");
         }

         if (!headerlist.isEmpty())
         {
            println("    \\setdynamicframe*{"+headerlist+"}{hide=false}%");
         }

         println("  }%");


         // Define flowframtk style (no chapters)

         println("  \\newcommand*{\\ps@flowframtk}{%");

         println("    \\def\\@oddfoot{"+psplainoddfoot+"}%");
         println("    \\def\\@evenfoot{"+psplainevenfoot+"}%");
         println("    \\def\\@oddhead{"+psflowframtkoddhead+"}%");
         println("    \\def\\@evenhead{"+psflowframtkevenhead+"}%");

         println("    \\let\\@mkboth\\markboth");
         println("    \\def\\sectionmark##1{%");
         println("      \\markboth{\\jdrheadingcase{\\ifnum\\c@secnumdepth >\\m@ne\\thesection\\quad\\fi ##1}}{\\jdrheadingcase{\\ifnum\\c@secnumdepth >\\m@ne\\thesection\\quad\\fi ##1}}}%");
         println("    \\def\\subsectionmark##1{%");
         println("      \\markright{\\jdrheadingcase{\\ifnum\\c@secnumdepth >\\tw@\\thesubsection\\quad\\fi ##1}}}%");

         println("  }%");


         println("}%"); // end of true part of \@ifundefined
         println("{%");

         // Redefine headings style (chapters)

         println("  \\renewcommand*{\\ps@headings}{%");
         println("    \\let\\@oddfoot\\@empty");
         println("    \\let\\@evenfoot\\@empty");
         println("    \\def\\@oddhead{"+psheadingsoddhead+"}%");
         println("    \\def\\@evenhead{"+psheadingsevenhead+"}%");
         println("    \\let\\@mkboth\\markboth");
         println("    \\def\\chaptermark##1{%");
         println("      \\markboth");
         println("      {\\jdrheadingcase{%");
         println("        \\ifnum \\c@secnumdepth >\\m@ne");
         println("          \\if@mainmatter");
         println("            \\@chapapp\\ \\thechapter. \\ ");
         println("          \\fi");
         println("        \\fi ##1%");
         println("      }}%");
         println("      {}%");
         println("    }%");
         println("    \\def\\sectionmark##1{%");
         println("      \\markright{\\jdrheadingcase{\\ifnum\\c@secnumdepth >\\z@\\thesection. \\ \\fi ##1}}}%");
         println("  }%");

         println("  \\newcommand*{\\ps@extra@headings}{%");

         if (!footerlist.isEmpty())
         {
            println("    \\setdynamicframe*{"+footerlist+"}{hide=true}%");
         }

         if (!headerlist.isEmpty())
         {
            println("    \\setdynamicframe*{"+headerlist+"}{hide=false}%");
         }

         println("  }%");

         // Redefine myheadings style (chapters)

         println("  \\renewcommand*{\\ps@myheadings}{%");
         println("    \\let\\@oddfoot\\@empty");
         println("    \\let\\@evenfoot\\@empty");
         println("    \\def\\@oddhead{"+psheadingsoddhead+"}%");
         println("    \\def\\@evenhead{"+psheadingsevenhead+"}%");
         println("    \\let\\@mkboth\\@gobbletwo");
         println("    \\let\\chaptermark\\@gobble");
         println("    \\let\\sectionmark\\@gobble");
         println("  }%");

         println("  \\newcommand*{\\ps@extra@myheadings}{%");

         if (!footerlist.isEmpty())
         {
            println("    \\setdynamicframe*{"+footerlist+"}{hide=true}%");
         }

         if (!headerlist.isEmpty())
         {
            println("    \\setdynamicframe*{"+headerlist+"}{hide=false}%");
         }

         println("  }%");

         // Define flowframtk style (chapters)

         println("  \\newcommand*{\\ps@flowframtk}{%");

         println("    \\def\\@oddfoot{"+psplainoddfoot+"}%");
         println("    \\def\\@evenfoot{"+psplainevenfoot+"}%");
         println("    \\def\\@oddhead{"+psflowframtkoddhead+"}%");
         println("    \\def\\@evenhead{"+psflowframtkevenhead+"}%");
         println("    \\let\\@mkboth\\markboth");
         println("    \\def\\chaptermark##1{%");
         println("      \\markboth");
         println("      {\\jdrheadingcase{%");
         println("        \\ifnum \\c@secnumdepth >\\m@ne");
         println("          \\if@mainmatter");
         println("            \\@chapapp\\ \\thechapter. \\ ");
         println("          \\fi");
         println("        \\fi ##1%");
         println("      }}%");
         println("      {}%");
         println("    }%");
         println("    \\def\\sectionmark##1{%");
         println("      \\markright{\\jdrheadingcase{\\ifnum\\c@secnumdepth >\\z@\\thesection. \\ \\fi ##1}}}%");
         println("  }%");

         println("}");

         if (headerlist.isEmpty() || footerlist.isEmpty())
         {
            print("\\@ifundefined{chapter}");
            println("{\\pagestyle{plain}}{\\pagestyle{headings}}");
         }
         else
         {
            println("\\pagestyle{flowframtk}");
         }
      }
   }

   protected void printFlowframTkStyHeaderFooter() throws IOException
   {
      CanvasGraphics cg = group.getCanvasGraphics();
      String list = "";
      String headerlist = "";
      String footerlist = "";

      String psheadingsoddhead = "{\\jdrheadingfmt\\rightmark}\\hfill\\thepage";
      String psheadingsevenhead = "\\thepage\\hfill{\\jdrheadingfmt\\leftmark}";

      String psflowframtkoddhead = "\\flowframtkoddheaderfmt{\\rightmark}";
      String psflowframtkevenhead = "\\flowframtkevenheaderfmt{\\leftmark}";

      String headerlabel = cg.getHeaderLabel();

      FlowFrame header = group.getFlowFrame(FlowFrame.DYNAMIC, headerlabel);

      String evenheaderlabel = cg.getEvenHeaderLabel();
      FlowFrame evenheader = group.getFlowFrame(
            FlowFrame.DYNAMIC, evenheaderlabel);

      if (header != null)
      {
         String contents = header.getContents();

         if (contents != null)
         {
            psheadingsoddhead = contents
                              + "{{\\jdrheadingfmt\\rightmark}\\hfill\\thepage}";
            psflowframtkoddhead = "\\flowframtkoddheaderfmt{"
                                 + contents + "\\rightmark}";
         }

         if (evenheader == null)
         {
            print("\\flowframtkSetDynamicOddHead{");
            print(headerlabel);
            println("}");

            list = headerlabel;

            if (contents != null)
            {
               psheadingsevenhead = contents
                              + "{\\thepage\\hfill{\\jdrheadingfmt\\leftmark}}";
               psflowframtkevenhead = "\\flowframtkevenheaderfmt{"
                                    + contents + "\\leftmark}";
            }
         }
         else
         {
            print("\\flowframtkSetDynamicOddEvenHead{");
            print(headerlabel);
            print("}{");
            print(evenheaderlabel);
            println("}");

            list = headerlabel+","+evenheaderlabel;

            contents = evenheader.getContents();

            if (contents != null)
            {
               psheadingsevenhead = contents
                              + "{\\thepage\\hfill{\\jdrheadingfmt\\leftmark}}";
               psflowframtkevenhead = "\\flowframtkevenheaderfmt{"
                                     + contents + "\\leftmark}";
            }
         }

      }
      else if (evenheader != null)
      {
         list = evenheaderlabel;
         print("\\flowframtkSetDynamicEvenHead{");
         print(evenheaderlabel);
         println("}");

         String contents = evenheader.getContents();

         if (contents != null)
         {
            psheadingsevenhead = contents
                           + "{\\thepage\\hfil{\\jdrheadingfmt\\leftmark}}";

            psflowframtkevenhead = "\\flowframtkevenheaderfmt{"
                                  + contents + "\\leftmark}";
         }
      }

      headerlist = list;

      String psplainoddfoot = "\\flowframtkoddfooterfmt{\\thepage}";
      String psplainevenfoot = "\\flowframtkevenfooterfmt{\\thepage}";

      String footerlabel = cg.getFooterLabel();

      FlowFrame footer = group.getFlowFrame(FlowFrame.DYNAMIC, footerlabel);

      String evenfooterlabel = cg.getEvenFooterLabel();
      FlowFrame evenfooter = group.getFlowFrame(
            FlowFrame.DYNAMIC, evenfooterlabel);

      if (footer != null)
      {
         if (!list.isEmpty()) list = list+",";

         String contents = footer.getContents();

         if (contents != null)
         {
            psplainoddfoot = "\\flowframtkoddfooterfmt{" + contents+"\\thepage}";
         }

         if (evenfooter == null)
         {
            list += footerlabel;

            footerlist = footerlabel;

            print("\\flowframtkSetDynamicOddFoot{");
            print(footerlabel);
            println("}");

            if (contents != null)
            {
               psplainevenfoot = "\\flowframtkevenfooterfmt{" + contents+"\\thepage}";
            }

         }
         else
         {
            footerlist = footerlabel+","+evenfooterlabel;

            list += footerlist;

            print("\\flowframtkSetDynamicOddFoot{");
            print(footerlabel);
            print("}{");
            print(evenfooterlabel);
            println("}");

            contents = evenfooter.getContents();

            if (contents != null)
            {
               psplainevenfoot = "\\flowframtkevenfooterfmt{"
                               + contents+"\\thepage}";
            }
         }
      }
      else if (evenfooter != null)
      {
         if (list.isEmpty())
         {
            list = evenfooterlabel;
         }
         else
         {
            list += ","+evenfooterlabel;
         }

         footerlist = evenfooterlabel;

         print("\\flowframtkSetDynamicEvenFoot{");
         print(evenfooterlabel);
         println("}");

         String contents = evenfooter.getContents();

         if (contents != null)
         {
            psplainevenfoot = "\\flowframtkevenfooterfmt{"
                            + contents+"\\thepage}";
         }
      }

      if (!list.isEmpty())
      {
         print("\\flowframtkSetExtraPageStyles{");
         print(list);
         print("}{");
         print(headerlist);
         print("}{");
         print(footerlist);
         print("}{");
         print(psplainoddfoot);
         print("}{");
         print(psplainevenfoot);
         println("}");

         if (evenheader == null)
         {
            print("\\flowframtkSetExtraOddHeadings{");
            print(headerlist);
            print("}{");
            print(footerlist);
            print("}{");
            print(psheadingsoddhead);
            println("}");
         }
         else
         {
            print("\\flowframtkSetExtraOddEvenHeadings{");
            print(headerlist);
            print("}{");
            print(footerlist);
            print("}{");
            print(psheadingsoddhead);
            print("}{");
            print(psheadingsevenhead);
            println("}");
         }

         if (headerlist.isEmpty() || footerlist.isEmpty())
         {
            println("\\flowframtkSetDefaultPageStyle");
         }
         else
         {
            println("\\pagestyle{flowframtk}");
         }
      }
   }

   protected String getDescription(JDRCompleteObject object)
   {
      JDRMessage msgSys = group.getCanvasGraphics().getMessageSystem();

      String description = object.getDescription();

      if (description == null)
      {
         Object[] info = object.getDescriptionInfo();

         description = msgSys.getMessageWithFallback(
          object.getClass().getCanonicalName(),
          object.getClass().getSimpleName());

         if (info.length > 0)
         {
            description += " " + info[0].toString();
         }
      }

      return description;
   }

   protected void writeAndUpdateMinPage(JDRCompleteObject object,
    Rectangle2D typeblockRect, double baselineskip,
      boolean useHPaddingShapepar)
    throws IOException,
     MissplacedTypeBlockException,
     InvalidShapeException
   {
      CanvasGraphics cg = group.getCanvasGraphics();
      JDRMessage msgSys = cg.getMessageSystem();
      MessageInfoPublisher publisher = msgSys.getPublisher();

      FlowFrame flowframe = object.getFlowFrame();

      if (flowframe == null)
      {
         if (object instanceof JDRGroup)
         {
            JDRGroup group = (JDRGroup)object;

            for (int i = 0; i < group.size(); i++)
            {
               writeAndUpdateMinPage(group.get(i), typeblockRect,
                  baselineskip, useHPaddingShapepar);
            }
         }
         else
         {
            String description = getDescription(object);

            publisher.publishMessages(
              MessageInfo.createVerbose(1,
                msgSys.getMessageWithFallback("message.omitting_object_no_flowframe",
                  "Omitting object ''{0}'' (no flow frame data set).",
                description),
                true));
         }
      }
      else
      {
         int type = flowframe.getType();

         if (type == FlowFrame.TYPEBLOCK)
         {
            throw new MissplacedTypeBlockException(cg);
         }

         if (type == FlowFrame.FLOW)
         {
            numFlows++;
         }

         String pages = flowframe.getPages();

         if (pages.equals("even"))
         {
            minPage = Math.max(2, minPage);
         }
         else if (!(pages.equals("all") || pages.equals("odd") || pages.equals("none")))
         {
            for (int j = pages.length()-2; j >= 0; j--)
            {
               char c = pages.charAt(j);
               int n = 0;

               try
               {
                  switch (c)
                  {
                     case '>':
                       n = Integer.parseInt(pages.substring(j+1).trim())+1;
                     break;
                     case '<':
                       n = Integer.parseInt(pages.substring(j+1).trim())-1;
                     break;
                     case '-':
                     case ',':
                       n = Integer.parseInt(pages.substring(j+1).trim());
                     break;
                     default:
                       if (!(Character.isDigit(c) || Character.isWhitespace(c)))
                       {
                          break;
                       }
                       else if (j == 0)
                       {
                          n = Integer.parseInt(pages.trim());
                       }
                  }
               }
               catch (NumberFormatException e)
               {
               }

               if (n > 0)
               {
                  minPage = Math.max(n, minPage);
                  break;
               }
            }
         }

         flowframe.tex(this, object, typeblockRect, baselineskip, 
            useHPaddingShapepar);
      }
   }

   public void foundThumbtab(JDRObject obj, double height)
   {
      hasThumbtabs = true;
      numThumbtabs++;
      sumThumbtabHeight += height;

      if (obj instanceof JDRCompleteObject)
      {
         JDRCompleteObject completeObject = (JDRCompleteObject)obj;

         FlowFrame ff = completeObject.getFlowFrame();

         thumbtabObjects.put(ff.getLabel(), completeObject);
      }
   }

   protected void resetThumbtabs()
   {
      hasThumbtabs = false;
      numThumbtabs = 0;
      sumThumbtabHeight = 0.0;
      thumbtabObjects.clear();
   }

   protected void printThumbtabs() throws IOException,InvalidShapeException
   {
      CanvasGraphics cg = group.getCanvasGraphics();

      if (hasThumbtabs)
      {
         for (int i = 1; i <= numThumbtabs; i++)
         {
            String ttlabel = "thumbtab"+i;
            JDRCompleteObject tt = thumbtabObjects.get(ttlabel);

            if (tt != null)
            {
               // Has an index thumbtab also been created?

               String ittlabel = "thumbtabindex"+i;
               JDRCompleteObject itt = thumbtabObjects.get(ittlabel);
               FlowFrame ff = tt.getFlowFrame();

               if (itt == null)
               {
                  // Not defined so replicate thumbtab

                  ff.tex(this, tt, typeblockRect, "", baselineskip,
                   useHPaddingShapepar, "none", ittlabel, ttlabel);
               }
            }

            ttlabel = "eventhumbtab"+i;
            tt = thumbtabObjects.get(ttlabel);

            if (tt != null)
            {
               // Has an even index thumbtab also been created?

               String ittlabel = "eventhumbtabindex"+i;
               JDRCompleteObject itt = thumbtabObjects.get(ittlabel);
               FlowFrame ff = tt.getFlowFrame();

               if (itt == null)
               {
                  // Not defined so replicate thumbtab

                  ff.tex(this, tt, typeblockRect, "", baselineskip,
                   useHPaddingShapepar, "none", ittlabel, ttlabel);
               }
            }
         }

         if (useFlowframTkSty)
         {
            println("\\makethumbtabs{"+length(cg, sumThumbtabHeight/numThumbtabs)+"}");
            println("\\enablethumbtabs");
         }
         else
         {
            cg.warning("warning.bespoke_thumbtabs_unsupported",
             "Currently installed version of flowfram.sty too old to support bespoke thumbtabs (at least v2.0 required).");
         }
      }
   }

   JDRGroup group;
   int minPage = 1;
   int numFlows = 0;

   boolean hasThumbtabs = false;
   int numThumbtabs = 0;
   double sumThumbtabHeight=0;
   HashMap<String,JDRCompleteObject> thumbtabObjects;

   Rectangle2D typeblockRect;
   double baselineskip;
   boolean useHPaddingShapepar;
}
