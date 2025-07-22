// File          : JDRViewInvoker.java
// Description   : JDR/AJR viewer
// Date          : 4th June 2008
// Author        : Nicola L C Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2025 Nicola L.C. Talbot

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

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.InvalidSyntaxException;
import com.dickimawbooks.texjavahelplib.CLISyntaxParser;
import com.dickimawbooks.texjavahelplib.CLIArgValue;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

public class JDRViewInvoker
{
   public JDRViewInvoker(String[] args) 
   {
      this.args = args;
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
      return APP_NAME;
   }

   public String getVersion()
   {
      return JDRResources.APP_VERSION;
   }

   /**
    * Prints the version information to STDERR.
    */
   public void appVersion()
   {
      System.out.println(resources.getAppInfo(false));
   }

   /**
    * Prints the command line syntax to STDERR and quits application.
    */
   public void syntax()
   {
      TeXJavaHelpLib helpLib = resources.getHelpLib();

      System.out.println(
         helpLib.getMessageWithFallback(
        "about.version_date",
        "{0} version {1} ({2})",
         APP_NAME, JDRResources.APP_VERSION, JDRResources.APP_DATE));

      System.out.println();

      System.out.println(helpLib.getMessage("clisyntax.usage",
        helpLib.getMessage("syntax.options", APP_NAME)));

      System.out.println();

      helpLib.printSyntaxItem(
         helpLib.getMessage("syntax.antialias", "--[no]antialias"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("syntax.cwd", "--cwd"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("syntax.in", "--in", "-i"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("clisyntax.help2", "--help", "-h"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("clisyntax.version2", "--version", "-v"));

      System.out.println();
      System.out.println(helpLib.getMessage("clisyntax.bugreport",
        "https://github.com/nlct/flowframtk"));

      System.exit(0);
   }

   private void createAndShowGui()
   throws IOException,URISyntaxException,InvalidFormatException,
          InvalidSyntaxException
   {
      resources = new JDRResources(APP_NAME);

      messageSystem = new JDRGuiMessage(resources);

      TeXJavaHelpLib helpLib = resources.getHelpLib();

      CLISyntaxParser cliParser = new CLISyntaxParser(helpLib, args, "-h", "-v")
      {             
         @Override
         protected int argCount(String arg)
         {
            if (arg.equals("--cwd") || arg.equals("-cwd")
             || arg.equals("--in") || arg.equals("-i")
               )
            {
               return 1;
            }

            return 0;

         }

         @Override
         protected void help()
         {
            syntax();
            System.exit(0);
         }

         @Override
         protected void version()
         {
            appVersion();
            System.exit(0);
         }

         @Override
         protected void parseArg(String arg)
         throws InvalidSyntaxException
         {
            if (filename == null)
            {
               filename = arg;
            }
            else
            {
               throw new InvalidSyntaxException(
                  helpLib.getMessage("error.one_filename"));
            }
         }

         @Override
         protected boolean parseArg(String arg, CLIArgValue[] returnVals)
         throws InvalidSyntaxException
         {
            // support single hyphen for backward-compatibility

            if (arg.equals("-version"))
            {
               version();
            }
            else if (arg.equals("-help"))
            {
               help();
            }
            else if ( arg.equals("--antialias")
                   || arg.equals("-antialias")
                    )
            {
               antiAlias = true;
            }
            else if ( arg.equals("--noantialias")
                   || arg.equals("-noantialias")
                    )
            {
               antiAlias = false;
            }
            else if (isArg(arg, "--cwd", "-cwd", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     helpLib.getMessage("error.clisyntax.missing.value", arg));
               }

               cwd = returnVals[0].toString();
            }
            else if (isArg(arg, "--in", "-i", returnVals))
            {
               if (filename != null)
               {
                  throw new InvalidSyntaxException(
                     resources.getMessage("error.one_filename"));
               }

               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     helpLib.getMessage("error.clisyntax.missing.value", arg));
               }

               filename = returnVals[0].toString();
            }
            else
            {
               return false;
            }

            return true;

        }
      };

      cliParser.process();

      File cwdFile = new File(cwd);

      if (!cwdFile.exists())
      {
         resources.warning(
            resources.getMessage("error.io.dir_not_exists", cwd));
         cwdFile = null;
      }
      else if (!cwdFile.isDirectory())
      {
         resources.warning(
            resources.getMessage("error.io.not_directory", cwd));
         cwdFile = null;
      }

      new JDRView(this, filename, antiAlias, cwdFile);
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
               JDRResources resources = invoker.getResources();

               try
               {
                  invoker.createAndShowGui();
               }
               catch (InvalidSyntaxException e)
               {
                  System.err.println(e.getMessage());
                  System.exit(JDRResources.EXIT_FATAL_ERROR);
               }
               catch (Throwable e)
               {
                  String msg = e.getMessage();

                  if (msg == null)
                  {
                     msg = e.getClass().getSimpleName();
                  }

                  System.err.println(msg);

                  if (resources == null)
                  {
                     e.printStackTrace();
                     System.exit(JDRResources.EXIT_FATAL_ERROR);
                  }
                  else
                  {
                     resources.internalError(null, msg, e);
                  }
               }
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

   private String filename = null;
   private boolean antiAlias = true;
   private String cwd = ".";

   private static final String APP_NAME = "JDRView";
}
