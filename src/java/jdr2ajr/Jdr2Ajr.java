// File          : Jdr2Ajr.java
// Description   : command line jdr to ajr converter
// Creation Date : 6 February 2007
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
package com.dickimawbooks.jdr2ajr;

import java.io.*;
import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * JDR to AJR command line converter.
 * @author Nicola L C Talbot
 */
public class Jdr2Ajr
{
   public static void appVersion()
   {
      System.err.println("jdr2ajr 1.7");
      System.err.println("Copyright (C) 2007 Nicola L C Talbot");
      System.err.println("This is free software distributed under the GNU General Public License.");
      System.err.println("There is NO WARRANTY. See accompanying licence file for details.");
   }

   public static void syntax()
   {
         System.err.println("Syntax: jdr2ajr [options] <jdr file> <ajr file>");
         System.err.println("Options:");
         System.err.println("-v<n>\tSave as AJR version <n>");
         System.err.println("-settings [<n>]\tControls how settings are saved in AJR file. Optionally followed by integer <n> (if omitted, <n>=1):");
         System.err.println("\t0 : don't save settings");
         System.err.println("\t1 : save all settings");
         System.err.println("\t2 : only save paper size (v1.3 and above, for earlier versions equivalent to 0)");
         System.err.println("-nosettings\tEquivalent to -settings 0");
         System.err.println("-settings_as_input\tOnly save settings if they were given in the input file (Default)");
         System.err.println("-version\tPrint version and exit.");
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
      String jdrFile = null;
      String ajrFile = null;
      int settingsFlag = JDRAJR.ALL_SETTINGS;
      float version = JDRAJR.CURRENT_VERSION;
      boolean settingsAsInput = true;

      JDRMessage messageSystem = new JDRDefaultMessage();

      CanvasGraphics canvasGraphics = new CanvasGraphics(messageSystem);

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-nosettings"))
         {
            settingsFlag = JDR.NO_SETTINGS;
            settingsAsInput = false;
         }
         else if (args[i].equals("-settings"))
         {
            if (i == args.length-1)
            {
               settingsFlag = JDR.ALL_SETTINGS;
            }
            else
            {
               try
               {
                  settingsFlag = Integer.parseInt(args[i+1]);
                  i++;
               }
               catch (NumberFormatException e)
               {
                  settingsFlag = JDR.ALL_SETTINGS;
               }
            }
            settingsAsInput = false;
         }
         else if (args[i].equals("-settings_as_input"))
         {
            settingsAsInput = true;
         }
         else if (args[i].equals("-version"))
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
               syntaxError("Unknown switch '"+args[i]+"'");
            }
         }
         else if (args[i].equals("-help"))
         {
            syntax();
         }
         else if (args[i].startsWith("-"))
         {
            syntaxError("Unknown switch '"+args[i]+"'");
         }
         else if (jdrFile == null)
         {
            jdrFile = args[i];
         }
         else if (ajrFile == null)
         {
            ajrFile = args[i];
         }
         else
         {
            syntaxError("Too many file names specified");
         }
      }

      if (jdrFile == null)
      {
         syntaxError("Missing input file");
      }
      else if (ajrFile == null)
      {
         syntaxError("Missing output file");
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
      finally
      {
         try
         {
            in.close();
         }
         catch (IOException e)
         { 
            System.err.println(e);
            System.exit(1);
         }
      }

      int inputSettingsFlag = jdr.getLastLoadedSettingsID();

      if (settingsAsInput)
      {
         settingsFlag = inputSettingsFlag;

         if (settingsFlag == JDR.PAPER_ONLY && version < 1.3f)
         {
            System.err.println("Warning: paper only settings not available for versions prior to 1.3 - defaulting to -settings 1");
            settingsFlag = JDR.ALL_SETTINGS;
         }
      }

      if (inputSettingsFlag == JDR.NO_SETTINGS
        && settingsFlag != JDR.NO_SETTINGS)
      {
         BBox box = paths.getBpBBox();
         double width = box.getWidth();
         double height = box.getHeight();

         JDRPaper paper = JDRPaper.getClosestPredefinedPaper(
            width, height, version);

         if (paper == null)
         {
            paper = new JDRPaper(messageSystem, width, height);
         }

         canvasGraphics.setPaper(paper);
      }


      PrintWriter out=null;

      try
      {
         out = new PrintWriter(new File(ajrFile));
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

      AJR ajr = new AJR();

      try
      {
         ajr.save(paths, out, version, settingsFlag);
      }
      catch (Exception e)
      {
         System.err.println(e);
         System.exit(1);
      }

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
