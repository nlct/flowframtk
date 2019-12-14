// File          : Eps2Jdr.java
// Description   : command line eps to jdr converter
// Creation Date : 5 March 2007
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
package com.dickimawbooks.eps2jdr;

import java.io.*;
import java.net.URISyntaxException;
import java.awt.geom.NoninvertibleTransformException;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

import com.dickimawbooks.jdrresources.*;

/**
 * EPS to JDR command line converter.
 * v1.1: added -normalsize switch
*/
public class Eps2Jdr
{
   /**
    * Prints version information to STDERR.
    */
   public static void appVersion()
   {
      System.err.println("eps2jdr 1.7");
      System.err.println("Copyright (C) 2007 Nicola L C Talbot");
      System.err.println("This is free software distributed under the GNU General Public License.");
      System.err.println("There is NO WARRANTY. See accompanying licence file for details.");
   }

   /**
    * Prints syntax to STDERR and exits.
    */
   public static void syntax()
   {
         System.err.println("Syntax: eps2jdr [options] <eps file> <jdr file>");
         System.err.println("Options:");
         System.err.println("-v<n>\tSave as JDR version <n>");
         System.err.println("-bitmap <basename>\tExtracted bitmaps to be saved as <basename><n>.png\n");
         System.err.println("-normalsize <n>\tSet normal LaTeX font size to <n>\n");
         System.err.println("-verbose\tVerbose output");
         System.err.println("-version\tPrint current version and exit");
         System.exit(0);
   }

   public static void syntaxError(String message)
   {
      System.err.println(message);
      System.err.println("Use '-help' for help");

      System.exit(1);
   }

   public static void main(String[] args)
   {
      try
      {
         eps2jdr(args);
      }
      catch (InvalidFormatException e)
      {
         System.err.println(e.getMessage());

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
   }

   private static void eps2jdr(String[] args)
     throws IOException,InvalidFormatException,URISyntaxException,
            NoninvertibleTransformException
   {
      String epsFile = null;
      String jdrFile = null;
      String bitmapBase = null;
      int verbose = 0;
      float version = JDRAJR.CURRENT_VERSION;
      int normalsize=10;

      JDRResources resources = new JDRResources();

      JDRDefaultMessage messageSystem = new JDRDefaultMessage();
      resources.setMessageSystem(messageSystem);

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-version"))
         {
            appVersion();
            System.exit(0);
         }
         else if (args[i].equals("-verbose"))
         {
            verbose = 1;
         }
         else if (args[i].startsWith("-v"))
         {
            try
            {
               version = Float.parseFloat(args[i].substring(2));
            }
            catch (NumberFormatException e)
            {
               syntaxError("Unknown switch '"+args[i]+"'");
            }
         }
         else if (args[i].equals("-help"))
         {
            syntax();
         }
         else if (args[i].equals("-bitmap"))
         {
            i++;

            if (i >= args.length)
            {
               syntaxError("missing -bitmap basename");
            }

            bitmapBase = args[i]; 
         }
         else if (args[i].equals("-normalsize"))
         {
            i++;

            if (i >= args.length)
            {
               syntaxError("missing -normalsize value");
            }
            try
            {
               normalsize = Integer.parseInt(args[i]); 
            }
            catch (NumberFormatException e)
            {
               System.err.println(
                  "Integer expected for -normalsize, '"+args[i]+"' found");
               System.exit(1);
            }
         }
         else if (args[i].startsWith("-"))
         {
            syntaxError("Unknown switch '"+args[i]+"'");
         }
         else if (epsFile == null)
         {
            epsFile = args[i];
         }
         else if (jdrFile == null)
         {
            jdrFile = args[i];
         }
         else
         {
            syntaxError("Too many file names specified");
         }
      }

      CanvasGraphics canvasGraphics 
         = new CanvasGraphics(messageSystem);

      messageSystem.setVerbosity(verbose);

      if (epsFile == null)
      {
         syntaxError("Missing input file");
      }
      else if (jdrFile == null)
      {
         syntaxError("Missing output file");
      }

      if (bitmapBase == null)
      {
         int idx = jdrFile.lastIndexOf(".jdr");

         if (idx == -1)
         {
            bitmapBase = jdrFile;
         }
         else
         {
            bitmapBase = jdrFile.substring(0, idx);
         }
      }

      String userconfigdir = resources.getUserConfigDirName();

      if (userconfigdir != null)
      {
         // load PostScript font mappings

         String psmappings=null;

         File file = new File(userconfigdir, "psfontmap");

         try
         {
            psmappings = file.getCanonicalPath();

            if (psmappings != null && file.exists()
                && file.isFile())
            {
               try
               {
                  LaTeXFont.loadPostScriptMappings(
                     messageSystem, file);
               }
               catch (InvalidFormatException e)
               {
                  messageSystem.error(e);
               }
            }
         }
         catch (IOException e)
         {
            messageSystem.error("'"+psmappings+"': " + e.getMessage());
         }
      }

      messageSystem.messageln("Reading '"+epsFile+"'");

      JDRGroup paths=null;

      canvasGraphics.getLaTeXFontBase().setNormalSize(normalsize);

      BufferedReader in = null;

      try
      {
         in = new BufferedReader(new FileReader(epsFile));

         paths = EPS.load(canvasGraphics, in, bitmapBase);
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }

      if (paths == null || paths.size() == 0)
      {
         System.err.println("no image created: nothing to save");
         System.exit(1);
      }

      BBox box = paths.getBpBBox();
      double width = box.getWidth();
      double height = box.getHeight();

      JDRPaper paper = JDRPaper.getClosestEnclosingPredefinedPaper(
         width, height, version);

      if (paper == null)
      {
         paper = new JDRPaper(messageSystem, width, height);
      }

      DataOutputStream out=null;

      try
      {
         out = new DataOutputStream(new FileOutputStream(jdrFile));
      }
      catch (IOException e)
      {
         System.err.println("Unable to open '"+jdrFile
            +"' "+e.getMessage());
         System.exit(1);
      }
      catch (Exception e)
      {
         System.err.println(e);
         System.exit(1);
      }

      messageSystem.messageln("");
      messageSystem.messageln("writing '"+jdrFile+"'");

      try
      {
         JDR jdr = new JDR();

         jdr.save(paths, out, version, JDR.ALL_SETTINGS);
      }
      catch (Exception e)
      {
         //System.err.println(e);
         e.printStackTrace();
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

         messageSystem.messageln("");
      }
   }
}
