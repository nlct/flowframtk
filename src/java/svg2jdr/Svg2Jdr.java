// File          : Svg2Jdr.java
// Description   : command line svg to jdr converter
// Creation Date : 22 May 2012
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
package com.dickimawbooks.svg2jdr;

import java.io.*;
import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.svg.*;

/**
 * Command line SVG to JDR converter.
 * @author Nicola L C Talbot
 */
public class Svg2Jdr
{
   /**
    * Prints version information to STDERR.
    */
   public static void appVersion()
   {
      System.err.println("svg2jdr 1.7");
      System.err.println("Copyright (C) 2007 Nicola L C Talbot");
      System.err.println("This is free software distributed under the GNU General Public License.");
      System.err.println("There is NO WARRANTY. See accompanying licence file for details.");
   }

   /**
    * Prints syntax information to STDERR and quits.
    */
   public static void syntax()
   {
         System.err.println("Syntax: svg2jdr [options] <svg file> <jdr file>");
         System.err.println("Options:");
         System.err.println("-v<n>\tSave as JDR version <n>");
         System.err.println("-version\tPrint version and exit");
         System.exit(0);
   }


   public static void main(String[] args)
   {
      String svgFile = null;
      String jdrFile = null;
      float version = JDRAJR.CURRENT_VERSION;
      boolean verbose = true;

      JDRDefaultMessage messageSystem = new JDRDefaultMessage();

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-version"))
         {
            appVersion();
            System.exit(0);
         }
         else if (args[i].startsWith("-v"))
         {
            try
            {
               version = Float.parseFloat(args[i].substring(2));
            }
            catch (NumberFormatException e)
            {
               System.err.println("Unknown switch '"+args[i]+"'");
               System.err.println("Use '-help' for help");

               System.exit(1);
            }
         }
         else if (args[i].equals("-help"))
         {
            syntax();
         }
         else if (args[i].startsWith("-"))
         {
            System.err.println("Unknown switch '"+args[i]+"'");
            System.err.println("Use '-help' for help");

            System.exit(1);
         }
         else if (svgFile == null)
         {
            svgFile = args[i];
         }
         else if (jdrFile == null)
         {
            jdrFile = args[i];
         }
         else
         {
            System.err.println("Too many file names specified");
            System.err.println("Use '-help' for help");

            System.exit(1);
         }
      }

      CanvasGraphics canvasGraphics = new CanvasGraphics(messageSystem);
      messageSystem.setVerbosity(verbose?1:0);

      if (svgFile == null)
      {
         System.err.println("Missing input file");
         System.err.println("Use '-help' for help");

         System.exit(1);
      }
      else if (jdrFile == null)
      {
         System.err.println("Missing output file");
         System.err.println("Use '-help' for help");

         System.exit(1);
      }

      DataOutputStream out=null;

      try
      {
         JDRGroup paths = SVG.load(canvasGraphics, new File(svgFile));

         messageSystem.verbose(1, "Saving "+jdrFile);
         out = new DataOutputStream(new FileOutputStream(jdrFile));

         JDR jdr = new JDR();

         jdr.save(paths, out, version, JDRAJR.NO_SETTINGS);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.exit(1);
      }
      finally
      {
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (Exception e)
            {
               e.printStackTrace();
               System.exit(1);
            }
         }

         messageSystem.messageln("");
      }
   }
}
