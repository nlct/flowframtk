// File          : JDRViewInvoker.java
// Description   : JDR/AJR viewer
// Date          : 4th June 2008
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

package com.dickimawbooks.jdrview;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import java.awt.Component;
import javax.swing.SwingUtilities;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

public class JDRViewInvoker
{
   public JDRViewInvoker(String[] args) 
     throws IOException,URISyntaxException
   {
      this.args = args;
      this.resources = new JDRResources();
   }

   public JDRResources getResources()
   {
      return resources;
   }

   public JDRGuiMessage getMessageSystem()
   {
      return messageSystem;
   }

   public String getName()
   {
      return appName;
   }

   public String getVersion()
   {
      return version;
   }

   /**
    * Prints the version information to STDERR.
    */
   public void appVersion()
   {
      System.err.println(appName.toLowerCase()+" "+version);
      System.err.println("Copyright (C) 2007 Nicola L C Talbot");
      System.err.println("This is free software distributed under the GNU General Public License.");
      System.err.println("There is NO WARRANTY. See accompanying licence file for details.");
   }

   /**
    * Prints the command line syntax to STDERR and quits application.
    */
   public void syntax()
   {
      System.err.println("Syntax: jdrview [options] [<jdr/ajr file>]");
      System.err.println();   
      System.err.println("Options:");   
      System.err.println();
      System.err.println("-antialias\t: Switch on anti-aliasing (default)");
      System.err.println("-noantialias\t: Switch off anti-aliasing");
      System.err.println("-cwd <path>\t: Set the current working directory to <path>");
      System.err.println("-version\t: Print version number and exit");
      System.err.println("-help\t: Print this help message");
      System.exit(0);
   }

   private void createAndShowGui()
   {
      try
      {
         resources.initialiseDictionary();
      }
      catch (IOException ioe)
      {
         resources.internalError(null, ioe);
      }

      messageSystem = new JDRGuiMessage(resources);

      String filename = null;
      boolean antiAlias = true;
      String cwd = ".";

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-version")
          || args[i].equals("--version"))
         {
            appVersion();
            System.exit(0);
         }
         else if (args[i].equals("-help")
          || args[i].equals("--help")
          || args[i].equals("-h"))
         {
            appVersion();
            syntax();
         }
         else if (args[i].equals("-antialias"))
         {
            antiAlias = true;
         }
         else if (args[i].equals("-noantialias"))
         {
            antiAlias = false;
         }
         else if (args[i].equals("-cwd"))
         {
            if (args.length == i+1)
            {
               resources.error(resources.getString(
                  "error.missing_cwd"));
            }
            else
            {
               cwd = args[++i];
            }
         }
         else if (args[i].startsWith("-"))
         {
            resources.error(
               resources.getStringWithValue(
                  "error.unknown_option",args[i]));
            syntax();
         }
         else if (filename == null)
         {
            filename = args[i];
         }
         else
         {
            resources.error(
               resources.getString("error.one_filename"));
            syntax();
         }
      }

      File cwdFile = new File(cwd);

      if (!cwdFile.exists())
      {
         resources.warning(
            resources.getStringWithValue(
               "error.io.dir_not_exists", cwd));
         cwdFile = null;
      }
      else if (!cwdFile.isDirectory())
      {
         resources.warning(
            resources.getStringWithValue(
               "error.io.not_directory", cwd));
         cwdFile = null;
      }

      try
      {
         JDRView app = new JDRView(this, filename, antiAlias, cwdFile);
      }
      catch (Exception e)
      {
         resources.internalError((Component)null, e);
      }
   }

   public static void main(String[] args)
   {
      try
      {
         final JDRViewInvoker invoker = new JDRViewInvoker(args);

         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               invoker.createAndShowGui();
            }
         });
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private String[] args;
   private JDRResources resources;
   private JDRGuiMessage messageSystem;

   private static final String version = "1.6";
   private static final String appName = "JDRView";
}
