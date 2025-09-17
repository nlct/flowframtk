/*
    Copyright (C) 2006 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or 
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

import java.util.Vector;

public class ExportSettings
{
   public static enum Type
   {
      PGF, IMAGE_DOC, FLF_DOC, CLS, STY, PDF, EPS, SVG, PNG;
   }

   public static enum Bounds
   {
      PAPER, IMAGE, TYPEBLOCK;
   }

   public static enum ObjectMarkup
   {
      NONE, PAIRED, ENCAP;
   }

   public static enum TextualShading
   {
      AVERAGE, START, END, TO_PATH;
   }

   public static enum TextPathOutline
   {
      TO_PATH, IGNORE;
   }

   public void copyFrom(ExportSettings other)
   {
      type = other.type;
      bounds = other.bounds;
      objectMarkup = other.objectMarkup;
      textualShading = other.textualShading;
      textPathOutline = other.textPathOutline;
      dviLaTeXApp = other.dviLaTeXApp;
      dviLaTeXOptions = other.dviLaTeXOptions;
      pdfLaTeXApp = other.pdfLaTeXApp;
      pdfLaTeXOptions = other.pdfLaTeXOptions;
      dvipsApp = other.dvipsApp;
      dvipsOptions = other.dvipsOptions;
      dvisvgmApp = other.dvisvgmApp;
      dvisvgmOptions = other.dvisvgmOptions;
      libgs = other.libgs;
      useExternalProcess = other.useExternalProcess;
      timeout = other.timeout;
      pngUseAlpha = other.pngUseAlpha;
      useFlowframTkSty = other.useFlowframTkSty;
      usePdfInfo = other.usePdfInfo;
      bitmapsToEps = other.bitmapsToEps;
      shapeparUseHpadding = other.shapeparUseHpadding;
      docClass = other.docClass;
   }

   public String[] getDviLaTeXCmd(String basename)
   {
      String[] latexCmd = new String[dviLaTeXOptions.length+1];
      latexCmd[0] = dviLaTeXApp;

      for (int i = 0; i < dviLaTeXOptions.length; i++)
      {
         latexCmd[i+1] = dviLaTeXOptions[i];
      }
   
      return getCmdList(latexCmd, basename, basename+".tex", basename+".dvi");
   }
   
   public String[] getPdfLaTeXCmd(String basename)
   {
      String[] latexCmd = new String[pdfLaTeXOptions.length+1];
      latexCmd[0] = pdfLaTeXApp;

      for (int i = 0; i < pdfLaTeXOptions.length; i++)
      {
         latexCmd[i+1] = pdfLaTeXOptions[i];
      }
   
      return getCmdList(latexCmd, basename, basename+".tex", basename+".pdf");
   }
   
   public String[] getDviPsCmd(String basename)
   {
      return getDviPsCmd(basename, basename+".dvi", basename+".eps");
   }

   public String[] getDviPsCmd(String basename, String dviFile, String epsFile)
   {
      String[] dvipsCmd = new String[dvipsOptions.length+1];
      dvipsCmd[0] = dvipsApp;

      for (int i = 0; i < dvipsOptions.length; i++)
      {
         dvipsCmd[i+1] = dvipsOptions[i];
      }

      return getCmdList(dvipsCmd, basename, dviFile, epsFile);
   }

   public String[] getDviSvgmCmd(String basename)
   {
      return getDviSvgmCmd(basename, basename+".dvi", basename+".svg");
   }

   public String[] getDviSvgmCmd(String basename, String dviFile, String svgFile)
   {
      String libGsPath = getLibGsPath();

      String[] opts = dvisvgmOptions;

      if (libGsPath == null && opts.length > 1)
      {
         Vector<String> list = new Vector<String>(opts.length);

         for (int i = 0; i < opts.length; i++)
         {
            if (opts[i].equals("--libgs"))
            {//skip this and next
               i++;
            }
            else if (opts[i].startsWith("--libgs="))
            {//skip
            }
            else
            {
               list.add(opts[i]);
            }
         }

         if (list.size() < opts.length)
         {
            opts = new String[list.size()];
            opts = list.toArray(opts);
         }
      }

      String[] dvisvgmCmd = new String[opts.length+1];
      dvisvgmCmd[0] = dvisvgmApp;

      for (int i = 0; i < opts.length; i++)
      {
         dvisvgmCmd[i+1] = opts[i];
      }

      return getCmdList(dvisvgmCmd, basename, dviFile, svgFile);
   }

   public String[] getCmdList(String[] list, String basename,
     String inFileName, String outFileName)
   {
      String[] cmdList = new String[list.length];

      for (int i = 0; i < list.length; i++)
      {
         if (list[i].equals("$outputfile"))
         {
            cmdList[i] = outFileName;
         }
         else if (list[i].equals("$inputfile"))
         {
            cmdList[i] = inFileName;
         }
         else if (list[i].equals("$basename"))
         {
            cmdList[i] = basename;
         }
         else if (list[i].equals("$libgs"))
         {
            cmdList[i] = getLibGsPath();

            if (cmdList[i] == null)
            {
               // shouldn't happen as already checked
               cmdList[i] = "";
            }
         }
         else if (list[i].equals("--libgs=$libgs"))
         {
            cmdList[i] = getLibGsPath();

            if (cmdList[i] == null)
            {
              // shouldn't happen as already checked
               cmdList[i] = "--libgs=";
            }
         }
         else
         {
            cmdList[i] = list[i];
         }
      }

      return cmdList;
   }

   private String getLibGsPath()
   {
      if (libgs == null)
      {
         return System.getenv("LIBGS");
      }
      else
      {
         return libgs;
      }
   }


   public Type type = Type.PGF;
   public Bounds bounds = Bounds.IMAGE;
   public ObjectMarkup objectMarkup = ObjectMarkup.NONE;
   public TextualShading textualShading = TextualShading.TO_PATH;
   public TextPathOutline textPathOutline = TextPathOutline.TO_PATH;

   public String dviLaTeXApp = "latex";
   public String[] dviLaTeXOptions = new String[] { "$basename" };

   public String pdfLaTeXApp = "pdflatex";
   public String[] pdfLaTeXOptions = new String[] { "$basename" };

   public String dvipsApp = "dvips";
   public String[] dvipsOptions
      = new String[] { "-E", "-o", "$outputfile", "$inputfile" };

   public String dvisvgmApp = "dvisvgm";
   public String[] dvisvgmOptions
      = new String[] { "--libgs", "$libgs", "-o", "$outputfile", "$inputfile" };

   public String libgs = null;
   public boolean useExternalProcess = false;
   public long timeout = 300000L;
   public boolean pngUseAlpha=false;
   public boolean useFlowframTkSty = false;
   public boolean usePdfInfo = false;
   public boolean bitmapsToEps = false;

   public boolean shapeparUseHpadding = true; // use \Shapepar instead of \shapepar

   public String docClass = null;
}
