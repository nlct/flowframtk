// File          : JdrInfo.java
// Description   : Gets information about a JDR or AJR file
// Creation Date : 2014-05-23
// Author        : Nicola L C Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2014-2026 Nicola L.C. Talbot

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
package com.dickimawbooks.jdrinfo;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.Locale;
import java.util.Vector;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.TeXJavaHelpLibAppAdapter;
import com.dickimawbooks.texjavahelplib.InvalidSyntaxException;
import com.dickimawbooks.texjavahelplib.CLISyntaxParser;
import com.dickimawbooks.texjavahelplib.CLIArgValue;
import com.dickimawbooks.texjavahelplib.HelpSetLocale;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

import com.dickimawbooks.jdrresources.JDRResources;

/**
 * Gets information about a JDR or AJR file.
 * TODO: add options to obtain extra information (via JDRAJR.FileInfo)
 * @author Nicola L C Talbot
 */
public class JdrInfo implements FileFilter
{
   public JdrInfo()
   {
      inFileNames = new Vector<String>();
   }

   public int getExitCode()
   {
      return helpLibApp == null ? exitCode : helpLibApp.getExitCode();
   }

   public void setExitCode(int code)
   {
      if (helpLibApp != null)
      {
         helpLibApp.setExitCode(code);
      }

      exitCode = code;
   }

   public int getExitCode(Throwable e, boolean isFatal)
   {
      if (helpLibApp != null)
      {
         return helpLibApp.getExitCode(e, isFatal);
      }
      else if (e instanceof InvalidSyntaxException)
      {
         return TeXJavaHelpLibAppAdapter.EXIT_SYNTAX;
      }
      else if (e instanceof IOException)
      {
         return TeXJavaHelpLibAppAdapter.EXIT_IO;
      }
      else
      {
         return TeXJavaHelpLibAppAdapter.EXIT_OTHER;
      }
   }

   protected void initHelpLibrary() throws IOException
   {
      helpLibApp = new TeXJavaHelpLibAppAdapter()
       {
          @Override
          public boolean isGUI() { return false; }

          @Override
          public String getApplicationName()
          {
             return NAME;
          }

          @Override
          public boolean isDebuggingOn()
          {
             return debugMode;
          }

          @Override
          public void message(String msg)
          {
             if (verboseLevel > 0)
             {
                stdOutMessage(msg);
             }
          }

       };

      if (helpSetLocale == null)
      {
         helpSetLocale = new HelpSetLocale(Locale.getDefault());
      }

      helpLib = new TeXJavaHelpLib(helpLibApp, NAME,
       "/resources", "/resources/dictionaries",
       helpSetLocale, helpSetLocale, "jdrcommon", "jdrinfo");

      helpLibApp.setHelpLib(helpLib);

      if (exitCode != 0)
      {
         helpLibApp.setExitCode(exitCode);
      }
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpLib;
   }

   public String getMessageWithFallback(String label,
       String fallbackFormat, Object... params)
   {
      return helpLib.getMessageWithFallback(label, fallbackFormat, params);
   }

   public String getMessageIfExists(String label, Object... args)
   {
      if (helpLib == null) return null;

      return helpLib.getMessageIfExists(label, args);
   }

   public String getMessage(String label, Object... params)
   {
      if (helpLib == null)
      {// message system hasn't been initialised

         String param = (params.length == 0 ? "" : params[0].toString());

         for (int i = 1; i < params.length; i++)
         {
            param += ","+params[0].toString();
         }

         return String.format("%s[%s]", label, param);
      }

      return helpLib.getMessage(label, params);
   }

   public void error(String message, Throwable e)
   {
      if (helpLibApp != null)
      {
         helpLibApp.error(message, e);
      }
      else
      {
         message("error", message, e);
         setExitCode(getExitCode(e, false));
      }
   }

   public void message(String msgTag, String message, Throwable e)
   {
      if (message == null)
      {
         if (e != null)
         {
            message = e.getMessage();
         }

         if (message == null)
         {
            message = e.getClass().getSimpleName();
         }
      }

      if (e != null)
      {
         System.err.format("%s: %s: %s%n", getApplicationName(), msgTag, message);

         if (debugMode)
         {
            e.printStackTrace();
         }
      }
      else if (msgTag.contains("error"))
      {
         System.err.format("%s: %s: %s%n", getApplicationName(), msgTag, message);
      }
      else
      {
         System.out.format("%s: %s: %s%n", getApplicationName(), msgTag, message);
      }
   }

   public void syntax()
   {
      versionInfo();

      System.out.println();
      System.out.println(getMessage("clisyntax.usage",
        getMessage("syntax.options", getApplicationName())));

      System.out.println();

      helpLib.printSyntaxItem(getMessage("syntax.in", "--in", "-i"));

      System.out.println();

      helpLib.printSyntaxItem(getMessage("syntax.debug", "--[no]debug"));

      helpLib.printSyntaxItem(getMessage("clisyntax.version2", "--version", "-v"));

      helpLib.printSyntaxItem(getMessage("clisyntax.help2", "--help", "-h"));

      System.out.println();

      System.out.println(getMessage("clisyntax.bugreport",
        "https://github.com/nlct/flowframtk"));
   }

   public void versionInfo()
   {
      if (!shownVersion)
      {
         System.out.println(getMessageWithFallback("about.version_date",
           "{0} version {1} ({2})", getApplicationName(),
           JDRResources.APP_VERSION, JDRResources.APP_DATE));
         shownVersion = true;
      }
   }

   public void license()
   {
      System.out.println();
      System.out.format("Copyright %s Nicola Talbot%n",
       getCopyrightDate());
      System.out.println(getMessage("about.license"));
      System.out.println("https://github.com/nlct/flowframtk");
   }

   public String getCopyrightStartYear()
   {
      return "2025";
   }

   public String getCopyrightDate()
   {
      String startYr = getCopyrightStartYear();
      String endYr = JDRResources.APP_DATE.substring(0, 4);

      if (startYr.equals(endYr))
      {
         return endYr;
      }
      else
      {
         return String.format("%s-%s", startYr, endYr);
      }
   }

   @Override
   public boolean accept(File pathname)
   {
      String name = pathname.getName().toLowerCase();

      return name.endsWith(".jdr") || name.endsWith(".ajr");
   }

   public String getApplicationName()
   {              
      return NAME; 
   }           
            
   public String getApplicationVersion()
   {        
      return JDRResources.APP_VERSION;
   }

   private void checkFile(File file) throws IOException
   {
      if (!file.exists())
      {
         throw new FileNotFoundException(
           getMessageWithFallback(
            "error.io.file_not_found", "File not found: {0}", file));
      }

      try
      {
         String fileFormat = JDRAJR.getFileFormat(canvasGraphics, file);

         System.out.println(file.toString()+": "+fileFormat);
      }
      catch (InvalidFormatException e)
      {
         System.err.format("jdrinfo: %s%n",
           getMessageWithFallback(
             "error.not_jdr_or_ajr", "Not a JDR or AJR file: {0}", file));

         setExitCode(TeXJavaHelpLibAppAdapter.EXIT_RUNTIME);
      }
   }

   protected void run() throws IOException
   {
      canvasGraphics = new CanvasGraphics(
         new JDRDefaultMessage(helpLib, NAME, JDRResources.APP_VERSION)
       );

      for (int i = 0; i < inFileNames.size(); i++)
      {
         File file = new File(inFileNames.get(i));

         if (file.isDirectory())
         {
            File[] list = file.listFiles(this);

            for (File f : list)
            {
               checkFile(f);
            }
         }
         else
         {
            checkFile(file);
         }
      }
   }

   private void parseArgs(String[] args) throws InvalidSyntaxException
   {
      CLISyntaxParser cliParser = new CLISyntaxParser(helpLib, args, "-h", "-v")
      {
         @Override
         protected int argCount(String arg)
         {
            if (arg.equals("--in") || arg.equals("-i"))
            {
               return 1;
            }

            return 0;
         }

         @Override
         public boolean setDebugOption(String option, Integer value)
         throws InvalidSyntaxException
         {
            debugMode = true;
          
            return true;
         }
      
         @Override
         protected boolean preparseCheckArg()
         throws InvalidSyntaxException
         {
            if (super.preparseCheckArg())
            {
               return true;
            }
          
            if (originalArgList[preparseIndex].equals("--verbose"))
            {
               verboseLevel = 1;
            }
            else if (originalArgList[preparseIndex].equals("--noverbose"))
            {
               verboseLevel = 0;
            }
            else if (originalArgList[preparseIndex].equals("--nodebug")
                   || originalArgList[preparseIndex].equals("--no-debug")
                    )
            {
               debugMode = false;
            }
            else
            {
               return false;
            }

            return true;
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
            System.out.println(helpLib.getAboutInfo(false,
             JDRResources.APP_VERSION, JDRResources.APP_DATE,
             String.format(
              "Copyright (C) %s Nicola L. C. Talbot (%s)",
              getCopyrightDate(),
              JDRResources.COPYRIGHT_YEAR,
              helpLib.getInfoUrl(false, "www.dickimaw-books.com")),
              TeXJavaHelpLib.LICENSE_GPL3,
              false, null
            ));

            System.exit(0);
         }

         @Override
         protected void parseArg(String arg)
         throws InvalidSyntaxException
         {
            inFileNames.add(arg);
         }

         @Override
         protected boolean parseArg(String arg, CLIArgValue[] returnVals)
         throws InvalidSyntaxException
         {
            if (isArg(arg, "--in", "-i", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               inFileNames.add(returnVals[0].toString());
            }
            else
            {
               return false;
            }

            return true;
         }
      };

      cliParser.process();

      if (inFileNames.isEmpty())
      {
         throw new InvalidSyntaxException(
            getMessageWithFallback(
              "error.syntax.missing_in", "Missing input file. (Use {0} for help.)",
              "--help"));
      }
   }

   public static void main(String[] args)
   {
      final JdrInfo app = new JdrInfo();

      try
      {
         app.initHelpLibrary();
         app.parseArgs(args);
         app.run();
      }
      catch (InvalidSyntaxException e)
      {
         app.error(e.getMessage(), null);
         app.setExitCode(TeXJavaHelpLibAppAdapter.EXIT_SYNTAX);
      }
      catch (Throwable e)
      {
         app.error(null, e);
      }

      System.exit(app.getExitCode());
   }

   protected int exitCode = 0;
   protected boolean shownVersion = false;
   protected boolean debugMode = false;
   protected int verboseLevel = 0;

   private Vector<String> inFileNames;

   private TeXJavaHelpLib helpLib;
   private TeXJavaHelpLibAppAdapter helpLibApp;
   private HelpSetLocale helpSetLocale;
   private CanvasGraphics canvasGraphics;

   public static final String NAME = "jdrinfo";
}
