// File          : Jdr2Tex.java
// Description   : command line jdr to tex converter
// Creation Date : 5th June 2008
// Author        : Nicola L C Talbot
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
package com.dickimawbooks.jdr2tex;

import java.io.*;
import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Command line JDR to TeX converter.
 * @author Nicola L C Talbot
 */
public class Jdr2Tex
{
   public static void appVersion()
   {
      System.err.println("jdr2tex 1.7");
      System.err.println("Copyright (C) 2007 Nicola L C Talbot");
      System.err.println("This is free software distributed under the GNU General Public License.");
      System.err.println("There is NO WARRANTY. See accompanying licence file for details.");
   }

   public static void syntax(int exitCode)
   {
      System.err.println("Syntax: jdr2tex [-version] [-doc] <jdr file> <tex file>");
      System.exit(exitCode);
   }

   public static void main(String[] args)
   {
      String jdrFile = null;
      String texFileName = null;

      boolean doc = false;
      boolean encapsulate = false;
      boolean convertBitmapToEps = false;
      boolean useTypeblockAsBoundingBox = false;

      JDRMessage messageSystem = new JDRDefaultMessage();
      CanvasGraphics canvasGraphics = new CanvasGraphics(messageSystem);

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-version"))
         {
            appVersion();
            System.exit(0);
         }
         else if (args[i].equals("-doc"))
         {
            doc = true;
         }
         else if (args[i].equals("-nodoc"))
         {
            doc = false;
         }
         else if (args[i].equals("-use_typeblock"))
         {
            useTypeblockAsBoundingBox = true;
         }
         else if (args[i].equals("-nouse_typeblock"))
         {
            useTypeblockAsBoundingBox = false;
         }
         else if (args[i].equals("-help"))
         {
            syntax(0);
         }
         else if (args[i].startsWith("-"))
         {
            System.err.println("Unknown switch '"+args[i]+"'");
            syntax(1);
         }
         else if (jdrFile == null)
         {
            jdrFile = args[i];
         }
         else if (texFileName == null)
         {
            texFileName = args[i];
         }
         else
         {
            System.err.println("Too many file names specified");
            syntax(1);
         }
      }

      if (jdrFile == null)
      {
         System.err.println("Missing input file");
         syntax(1);
      }
      else if (texFileName == null)
      {
         System.err.println("Missing output file");
         syntax(1);
      }

      DataInputStream in=null;

      try
      {
         in = new DataInputStream(new BufferedInputStream(new FileInputStream(jdrFile)));
      }
      catch (IOException e)
      {
         System.err.println("Unable to open "
            +e.getMessage());
         System.exit(1);
      }
      catch (Exception e)
      {
         System.err.println(e);
         System.exit(1);
      }

      String fileVersion = "";
      JDRGroup paths=null;

      JDR jdr = new JDR();

      try
      {
         paths = jdr.load(in, canvasGraphics);
      }
      catch (InvalidFormatException e)
      {
         System.err.println(e.getMessageWithIndex(messageSystem));

         Throwable cause = e.getCause();

         if (cause != null)
         {
            System.err.println(cause);
         }

         System.exit(1);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.exit(1);
      }

      try
      {
         in.close();
      }
      catch (Exception e)
      {
         System.err.println(e);
         System.exit(1);
      }


      PrintWriter out=null;

      File texFile = new File(texFileName);

      try
      {
         out = new PrintWriter(texFile);
      }
      catch (IOException e)
      {
         System.err.println("Unable to open "+e.getMessage());
         System.exit(1);
      }
      catch (Exception e)
      {
         System.err.println(e);
         System.exit(1);
      }

      try
      {
         PGF pgf = new PGF(texFile.getParentFile().toPath(), out);

         if (doc)
         {
            pgf.saveDoc(paths, null, encapsulate, convertBitmapToEps, useTypeblockAsBoundingBox);
         }
         else
         {
            pgf.save(paths, useTypeblockAsBoundingBox);
         }
      }
      catch (Exception e)
      {
         System.err.println(e);
         System.exit(1);
      }
      finally
      {
         try
         {
            out.close();
         }
         catch (Exception e)
         {
            System.err.println(e);
            System.exit(1);
         }
      }
   }
}
