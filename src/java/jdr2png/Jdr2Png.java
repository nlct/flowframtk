// File          : Jdr2Png.java
// Description   : command line jdr to png converter
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
package com.dickimawbooks.jdr2png;

import java.io.*;
import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Command line JDR to PNG converter.
 * @author Nicola L C Talbot
 */
public class Jdr2Png
{
   public static void appVersion()
   {
      System.err.println("jdr2png 1.8");
      System.err.println("Copyright (C) 2007 Nicola L C Talbot");
      System.err.println("This is free software distributed under the GNU General Public License.");
      System.err.println("There is NO WARRANTY. See accompanying licence file for details.");
   }

   public static void syntax(int exitCode)
   {
      System.err.println("Syntax: jdr2png [<options>] <jdr file> <png file>");
      System.err.println("Options:");
      System.err.println("-version:\tDisplay version number and exit");
      System.err.println("-alpha:\tAdd alpha channel");
      System.err.println("-noalpha:\tNo alpha channel (default)");
      System.err.println("-crop:\tCrop image to bounding box (default)");
      System.err.println("-nocrop:\tMake image the same size as the canvas");
      System.exit(exitCode);
   }

   public static void main(String[] args)
   {
      String jdrFile = null;
      String pngFile = null;

      JDRMessage messageSystem = new JDRDefaultMessage();
      CanvasGraphics canvasGraphics = new CanvasGraphics(messageSystem);

      boolean crop = true;
      boolean hasAlpha = false;

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-version"))
         {
            appVersion();
            System.exit(0);
         }
         else if (args[i].equals("-help"))
         {
            syntax(0);
         }
         else if (args[i].equals("-crop"))
         {
            crop = true;
         }
         else if (args[i].equals("-nocrop"))
         {
            crop = false;
         }
         else if (args[i].equals("-alpha"))
         {
            hasAlpha = true;
         }
         else if (args[i].equals("-noalpha"))
         {
            hasAlpha = false;
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
         else if (pngFile == null)
         {
            pngFile = args[i];
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
      else if (pngFile == null)
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

      try
      {
         PNG.save(paths, pngFile, hasAlpha, crop);
      }
      catch (Exception e)
      {
         System.err.println(e);
         System.exit(1);
      }
   }
}
