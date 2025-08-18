/*
    Copyright (C) 2025 Nicola L.C. Talbot
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

package com.dickimawbooks.jdrconverter;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.util.Locale;

import java.text.MessageFormat;

import com.dickimawbooks.texjavahelplib.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.JDRResources;

public class JDRConverter
{
   public JDRConverter()
   {
      msgPublisher = new ConverterPublisher(this);
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
             msgPublisher.message(msg);
          }

       };


      if (helpSetLocale == null)
      {
         if (dictLangTag == null)
         {
            helpSetLocale = new HelpSetLocale(Locale.getDefault());
         }
         else
         {
            helpSetLocale = new HelpSetLocale(Locale.getDefault());
         }
      }

      helpLib = new TeXJavaHelpLib(helpLibApp, NAME, 
       "/resources", "/resources/dictionaries",
       helpSetLocale, helpSetLocale, "jdrcommon", "jdrconverter");

      helpLibApp.setHelpLib(helpLib);
   }

   public TeXJavaHelpLib getHelpLib()
   {
      return helpLib;
   }

   public String getMessageWithFallback(String label,
       String fallbackFormat, Object... params)
   {
      if (helpLib == null)
      {
         MessageFormat fmt = new MessageFormat(fallbackFormat);

         return fmt.format(params);
      }

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
      else
      {
         System.out.format("%s: %s: %s%n", getApplicationName(), msgTag, message);
      }
   }

   public void debug(String message, Throwable e)
   {
      if (debugMode)
      {
         error(message, e);
      }
   }

   public void syntax()
   {
      versionInfo();

      System.out.println();
      System.out.println(getMessage("clisyntax.usage",
        getMessage("syntax.options", getApplicationName(), "--in", "--output")));

      System.out.println();

      helpLib.printSyntaxItem(getMessage("syntax.option_info", "--in", "--output"));

      System.out.println();

      helpLib.printSyntaxItem(getMessage("syntax.in", "--in", "-i"));
         
      helpLib.printSyntaxItem(getMessage("syntax.out", "--output", "-o"));

      System.out.println();

      helpLib.printSyntaxItem(getMessage("syntax.from", "--from", "-f"));
      helpLib.printSyntaxItem(getMessage("syntax.to", "--to", "-t"));

      helpLib.printSyntaxItem(getMessage("syntax.jdr_version",
       "--jdr-version", JDRAJR.CURRENT_VERSION));

      helpLib.printSyntaxItem(getMessage("syntax.settings", "--settings"));

      helpLib.printSyntaxItem(getMessage("syntax.in.charset", "--in-charset"));
      helpLib.printSyntaxItem(getMessage("syntax.out.charset", "--out-charset"));

      helpLib.printSyntaxItem(getMessage("syntax.encapsulate", "--[no]crop", "-C"));
      helpLib.printSyntaxItem(getMessage("syntax.bitmaps_to_eps",
        "--[no]bitmaps-to-eps"));

      helpLib.printSyntaxItem(getMessage("syntax.alpha", "--[no]alpha"));

      System.out.println();

      helpLib.printSyntaxItem(getMessage("syntax.tex_settings"));

      System.out.println();

      helpLib.printSyntaxItem(getMessage("syntax.doc", "--[no]doc"));
      helpLib.printSyntaxItem(getMessage("syntax.use_typeblock", "--[no]use-typeblock"));

      System.out.println();

      helpLib.printSyntaxItem(getMessage("syntax.other"));

      System.out.println();

      helpLib.printSyntaxItem(getMessage("syntax.list_input_formats",
       "--list-input-formats"));

      helpLib.printSyntaxItem(getMessage("syntax.list_output_formats",
       "--list-output-formats"));

      helpLib.printSyntaxItem(getMessage("syntax.locale", "--locale"));

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

   public String getApplicationName()
   {
      return NAME;
   }

   public String getApplicationVersion()
   {
      return JDRResources.APP_VERSION;
   }

   protected void ensureHelpSetLoaded()
   {
      if (helpLib == null)
      {
         try
         {
            initHelpLibrary();

            if (cliParser != null)
            {
               cliParser.setHelpLib(helpLib);
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
            System.exit(EXIT_OTHER);
         }
      }
   }

   private void parseArgs(String[] args) throws InvalidSyntaxException
   {
      cliParser = new CLISyntaxParser(helpLib, args, "-h", "-v")
      {
         @Override
         protected int argCount(String arg)
         {
            if ( arg.equals("--in") || arg.equals("-i")
             || arg.equals("--output") || arg.equals("-o")
             || arg.equals("--in-charset")
             || arg.equals("--out-charset")
             || arg.equals("--from") || arg.equals("-f")
             || arg.equals("--to") || arg.equals("-t")
             || arg.equals("--jdr-version")
             || arg.equals("--settings")
               ) 
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
               msgPublisher.setVerbosity(1);
            }
            else if (originalArgList[preparseIndex].equals("--noverbose"))
            {
               msgPublisher.setVerbosity(0);
            }
            else if (originalArgList[preparseIndex].equals("--nodebug")
                   || originalArgList[preparseIndex].equals("--no-debug")
                    )
            {
               debugMode = false;
            }
            else if (originalArgList[preparseIndex].equals("--locale"))
            {
               preparseIndex++;

               if (preparseIndex >= originalArgList.length)
               {
                  throw new InvalidSyntaxException("Missing value after --locale");
               }

               dictLangTag = originalArgList[preparseIndex];

               ensureHelpSetLoaded();
            }
            else if (originalArgList[preparseIndex].startsWith("--locale="))
            {
               dictLangTag = originalArgList[preparseIndex].substring(10);

               ensureHelpSetLoaded();
            }
// support switches from old *2* tools
            else if (originalArgList[preparseIndex].startsWith("-v")
                   && originalArgList[preparseIndex].length() > 2)
            {
               deque.add("--jdr-version");
               deque.add(originalArgList[preparseIndex].substring(2));
            }
            else if (originalArgList[preparseIndex].equals("-help"))
            {
               help();
            }
            else if (originalArgList[preparseIndex].equals("-settings_as_input"))
            {
               deque.add("--settings");
               deque.add(SaveSettingsType.MATCH_INPUT.getTag());
            }
            else if (originalArgList[preparseIndex].equals("-settings"))
            {
               deque.add("--settings");

               int idx = preparseIndex+1;

               if (idx == originalArgList.length || originalArgList[idx].length() != 1)
               {
                  deque.add(SaveSettingsType.ALL.getTag());
               }
               else
               {
                  switch (originalArgList[idx].charAt(0))
                  {
                     case '0':
                        deque.add(SaveSettingsType.NONE.getTag());
                        preparseIndex++;
                     break;
                     case '1':
                        deque.add(SaveSettingsType.ALL.getTag());
                        preparseIndex++;
                     break;
                     case '2':
                        deque.add(SaveSettingsType.PAPER_ONLY.getTag());
                        preparseIndex++;
                     break;
                     default:
                        deque.add(SaveSettingsType.ALL.getTag());
                  }
               }
            }
            else if (originalArgList[preparseIndex].equals("-use_typeblock"))
            {
               deque.add("--use-typeblock");
            }
            else if (originalArgList[preparseIndex].equals("-nouse_typeblock"))
            {
               deque.add("--nouse-typeblock");
            }
            else if (originalArgList[preparseIndex].equals("-nosettings")
                  || originalArgList[preparseIndex].equals("-doc")
                  || originalArgList[preparseIndex].equals("-nodoc")
                  || originalArgList[preparseIndex].equals("-alpha")
                  || originalArgList[preparseIndex].equals("-noalpha")
                  || originalArgList[preparseIndex].equals("-crop")
                  || originalArgList[preparseIndex].equals("-nocrop")
                    )
            {
               deque.add("-"+originalArgList[preparseIndex]);
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
            ensureHelpSetLoaded();

            syntax();
            System.exit(0);
         }

         @Override
         protected void version()
         {
            ensureHelpSetLoaded();

            System.out.println(helpLib.getAboutInfo(false,
             JDRResources.APP_VERSION, JDRResources.APP_DATE,
             String.format(
              "Copyright (C) %s Nicola L. C. Talbot (%s)",
              getCopyrightDate(),
              JDRResources.COPYRIGHT_YEAR,
              helpLib.getInfoUrl(false, "www.dickimaw-books.com")),
              TeXJavaHelpLib.LICENSE_GPL3,
              true, null
            ));

            System.exit(0);
         }

         @Override
         protected void parseArg(String arg)
         throws InvalidSyntaxException
         {
            if (inFile == null)
            {
               inFile = new File(arg);
            }
            else if (outFile == null)
            {
               outFile = new File(arg);
            }
            else
            {
              throw new InvalidSyntaxException(
                getMessage("error.syntax.too_many_files", arg, "--help"));
            }
         }

         @Override
         protected boolean parseArg(String arg, CLIArgValue[] returnVals)
         throws InvalidSyntaxException
         {
            if (arg.equals("--nosettings"))
            {
               saveSettingsType = SaveSettingsType.NONE;
            }
            else if (arg.equals("--doc"))
            {
               completeDoc = true;
            }
            else if (arg.equals("--nodoc"))
            {
               completeDoc = false;
            }
            else if (arg.equals("--use-typeblock"))
            {
               useTypeblock = true;
            }
            else if (arg.equals("--nouse-typeblock"))
            {
               useTypeblock = false;
            }
            else if (arg.equals("--crop") || arg.equals("-C"))
            {
               encapsulate = true;
            }
            else if (arg.equals("--nocrop"))
            {
               encapsulate = false;
            }
            else if (arg.equals("--bitmaps-to-eps"))
            {
               convertBitmapToEps = true;
            }
            else if (arg.equals("--nobitmaps-to-eps"))
            {
               convertBitmapToEps = false;
            }
            else if (arg.equals("--alpha"))
            {
               addAlphaChannel = true;
            }
            else if (arg.equals("--noalpha"))
            {
               addAlphaChannel = false;
            }
            else if (arg.equals("--list-input-formats"))
            {
               int idx = 0;

               for (FileFormatType type : FileFormatType.values())
               {
                  if (type.isInputSupported())
                  {
                     System.out.print(type);

                     idx += type.toString().length();

                     if (idx >= TeXJavaHelpLib.SYNTAX_ITEM_LINEWIDTH)
                     {
                        System.out.println();
                        idx = 0;
                     }
                     else
                     {
                        System.out.print(" ");
                        idx++;
                     }
                  }
               }

               if (idx != 0)
               {
                  System.out.println();
               }

               System.exit(0);
            }
            else if (arg.equals("--list-output-formats"))
            {
               int idx = 0;

               for (FileFormatType type : FileFormatType.values())
               {
                  System.out.print(type);

                  idx += type.toString().length();

                  if (idx >= TeXJavaHelpLib.SYNTAX_ITEM_LINEWIDTH)
                  {
                     System.out.println();
                     idx = 0;
                  }
                  else
                  {
                     System.out.print(" ");
                     idx++;
                  }

                  if (type == FileFormatType.JDR || type == FileFormatType.AJR)
                  {
                     for (String ver : JDRAJR.VALID_VERSIONS_STRING)
                     {
                        String str = type+"-"+ver;
                        System.out.print(str);

                        idx += str.length();

                        if (idx >= TeXJavaHelpLib.SYNTAX_ITEM_LINEWIDTH)
                        {
                           System.out.println();
                           idx = 0;
                        }
                        else
                        {
                           System.out.print(" ");
                           idx++;
                        }
                     }
                  }
               }

               if (idx != 0)
               {
                  System.out.println();
               }

               System.exit(0);
            }
            else if (isArg(arg, "--jdr-version", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               String ver = returnVals[0].toString();

               try
               {
                  Integer.parseInt(ver);
                  ver += ".0";
               }
               catch (NumberFormatException e)
               {
               }

               boolean found = false;

               for (int i = 0; i < JDRAJR.VALID_VERSIONS_STRING.length; i++)
               {
                  if (ver.equals(JDRAJR.VALID_VERSIONS_STRING[i]))
                  {
                     outVersion = JDRAJR.VALID_VERSIONS[i];
                     found = true;
                     break;
                  }
               }

               if (!found)
               {
                  throw new InvalidSyntaxException(
                   getMessage("error.syntax.invalid_file_version", "JDR/AJR", ver));
               }
            }
            else if (isArg(arg, "--to", "-t", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               String fmt = returnVals[0].toString();
               String ver = null;

               int idx = fmt.indexOf("-");

               if (idx > 0)
               {
                  ver = fmt.substring(idx+1);
                  fmt = fmt.substring(0, idx);
               }

               outFormat = FileFormatType.valueOf(fmt.toUpperCase());

               if (outFormat == null)
               {
                  throw new InvalidSyntaxException(
                    getMessage("error.syntax.unknown_file_format", fmt));
               }

               if (ver != null)
               {
                  try
                  {
                     Integer.parseInt(ver);
                     ver += ".0";
                  }
                  catch (NumberFormatException e)
                  {
                  }

                  if (outFormat == FileFormatType.JDR || outFormat == FileFormatType.AJR)
                  {
                     boolean found = false;

                     for (int i = 0; i < JDRAJR.VALID_VERSIONS_STRING.length; i++)
                     {
                        if (ver.equals(JDRAJR.VALID_VERSIONS_STRING[i]))
                        {
                           outVersion = JDRAJR.VALID_VERSIONS[i];
                           found = true;
                           break;
                        }
                     }

                     if (!found)
                     {
                        throw new InvalidSyntaxException(
                         getMessage("error.syntax.invalid_file_version", fmt, ver));
                     }
                  }
                  else
                  {
                     helpLib.warning(getMessage("warning.ignoring_file_version"));
                  }
               }
            }
            else if (isArg(arg, "--from", "-f", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               String fmt = returnVals[0].toString();

               inFormat = FileFormatType.valueOf(fmt.toUpperCase());

               if (inFormat == null)
               {
                  throw new InvalidSyntaxException(
                    getMessage("error.syntax.unknown_file_format", fmt));
               }
            }
            else if (isArg(arg, "--in", "-i", returnVals))
            {
               if (inFile != null)
               {
                  throw new InvalidSyntaxException(
                    getMessage("error.syntax.only_one", arg));
               }

               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               inFile = new File(returnVals[0].toString());
            }
            else if (isArg(arg, "--output", "-o", returnVals))
            {
               if (outFile != null)
               {
                  throw new InvalidSyntaxException(
                    getMessage("error.syntax.only_one", arg));
               }

               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               outFile = new File(returnVals[0].toString());
            }
            else if (isArg(arg, "--out-charset", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               outCharset = Charset.forName(returnVals[0].toString());
            }
            else if (isArg(arg, "--in-charset", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               inCharset = Charset.forName(returnVals[0].toString());
            }
            else if (isArg(arg, "--settings", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               String val = returnVals[0].toString();

               saveSettingsType = SaveSettingsType.getFromTag(val);

               if (saveSettingsType == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.syntax.invalid_settings", val));
               }
            }
            else
            {
               return false;
            }

            return true;
         }
      };

      cliParser.preparse();
      ensureHelpSetLoaded();
      cliParser.parseArgs();

      if (inFile == null)
      {
         throw new InvalidSyntaxException(
            getMessage("error.syntax.missing_in", "--help"));
      }

      if (outFile == null)
      {
         throw new InvalidSyntaxException(
            getMessage("error.syntax.missing_out", "--help"));
      }

      if (inFormat == null)
      {
         // guess from file extension

         inFormat = FileFormatType.getFormat(inFile);

         if (inFormat == null)
         {
            inFormat = FileFormatType.JDR;
         }
      }

      if (!inFormat.isInputSupported())
      {
         throw new InvalidSyntaxException(getMessage("error.cant_import_from", inFormat));
      }

      if (outFormat == null)
      {
         outFormat = FileFormatType.getFormat(outFile);

         if (outFormat == null)
         {
            outFormat = FileFormatType.JDR;
         }
      }
   }

   protected void run() throws IOException,InvalidFormatException
   {
      JDRGroup paths = null;

      CanvasGraphics canvasGraphics 
         = new CanvasGraphics(msgPublisher);

      BufferedReader in = null;
      DataInputStream din = null;

      if (inFormat.isTextFile())
      {
         if (inCharset == null)
         {
            in = Files.newBufferedReader(inFile.toPath());
         }
         else
         {
            in = Files.newBufferedReader(inFile.toPath(), inCharset);
         }
      }
      else
      {
         din = new DataInputStream(new BufferedInputStream(new FileInputStream(inFile)));
      }

      int settingsFlag = -1;

      try
      {
         switch (inFormat)
         {
            case JDR:
              JDR jdr = new JDR();
              paths = jdr.load(din, canvasGraphics);
              settingsFlag = jdr.getLastLoadedSettingsID();
            break;
            case AJR:
              AJR ajr = new AJR();
              paths = ajr.load(in, canvasGraphics);
              settingsFlag = ajr.getLastLoadedSettingsID();
            break;
            default:
              throw new InvalidFormatException(
                getMessage("error.cant_load_format", inFormat));
         }
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }

         if (din != null)
         {
            din.close();
         }
      }

      JDRPaper paper = null;

      if (settingsFlag != -1 && saveSettingsType != SaveSettingsType.NONE)
      {
         paper = canvasGraphics.getPaper();
      }

      switch (saveSettingsType)
      {
         case NONE:
           settingsFlag = JDR.NO_SETTINGS;
         break;
         case ALL:
           settingsFlag = JDR.ALL_SETTINGS;
         break;
         case PAPER_ONLY:
           settingsFlag = JDR.PAPER_ONLY;
         break;
         case MATCH_INPUT:
           if (settingsFlag == -1)
           {
              if (outVersion < 1.3f)
              {
                 settingsFlag = JDR.ALL_SETTINGS;
              }
              else
              {
                 settingsFlag = JDR.PAPER_ONLY;
              }
           }
         break;
      }

      if (paper == null)
      {
         BBox box = paths.getBpBBox();

         double width = box.getMaxX();
         double height = box.getMaxY();

         paper = JDRPaper.getClosestPredefinedPaper(
            width, height, outVersion);

         if (paper == null)
         {
            paper = new JDRPaper(msgPublisher, width, height);
         }

         canvasGraphics.setPaper(paper);
      }

      if (outFormat == FileFormatType.JDR || outFormat == FileFormatType.AJR)
      {
         if (outVersion < 1.3f && settingsFlag == JDR.PAPER_ONLY)
         {
            helpLib.warning(getMessage("warning.option_not_supported",
              "--settings paper-only", outFormat, outVersion,
              "--settings all"));
         }
      }

      PrintWriter out = null;
      DataOutputStream dout = null;

      if (outFormat.isTextFile())
      {
         if (outCharset == null)
         {
            out = new PrintWriter(Files.newBufferedWriter(outFile.toPath()));
         }
         else
         {
            out = new PrintWriter(Files.newBufferedWriter(outFile.toPath(), inCharset));
         }
      }
      else
      {
         dout = new DataOutputStream(new FileOutputStream(outFile));
      }

      try
      {
         switch (outFormat)
         {
            case JDR:
              JDR jdr = new JDR();
              jdr.save(paths, dout, outVersion, settingsFlag);
            break;
            case AJR:
              AJR ajr = new AJR();
              ajr.save(paths, out, outVersion, settingsFlag);
            break;
            case TEX:
              PGF pgf = new PGF(outFile.getParentFile(), out);
              pgf.comment(getMessage("message.created_by", NAME));
              pgf.writeCreationDate();

              if (completeDoc)
              {
                 pgf.saveDoc(paths, null, encapsulate, convertBitmapToEps, useTypeblock);
              }
              else
              {
                 pgf.println("\\iffalse");

                 pgf.comment(getMessage("message.required_preamble_commands"));
                 pgf.writePreambleCommands(paths);

                 pgf.comment(getMessage("message.assumed_normalsize",
                  paths.getCanvasGraphics().getLaTeXNormalSize()));

                 pgf.comment(getMessage("message.end_preamble_info"));

                 pgf.println("\\fi");

                 pgf.save(paths, useTypeblock);
              }
            break;
            case EPS:
               EPS.save(paths, out, NAME);
            break;
            default:
              throw new InvalidFormatException(
                getMessage("error.cant_save_format", outFormat));
         }
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }
      }
   }

   public static void main(String[] args)
   {
      final JDRConverter app = new JDRConverter();

      try
      {
         app.parseArgs(args);
         app.run();
      }
      catch (InvalidSyntaxException e)
      {
         app.error(e.getMessage(), null);

         System.exit(EXIT_SYNTAX);
      }
      catch (Throwable e)
      {
         app.error(null, e);

         System.exit(EXIT_OTHER);
      }
   }

   protected CLISyntaxParser cliParser;
   protected JDRDefaultMessage msgPublisher;
   protected boolean debugMode = false;
   protected boolean shownVersion = false;
   protected File inFile, outFile; // --in / -i , --output / -o
   protected boolean completeDoc = false; // --doc
   protected boolean useTypeblock = false; // --use-typeblock
   protected boolean encapsulate = false; // --crop / -C
   protected boolean convertBitmapToEps = false; // --bitmaps-to-eps
   protected boolean addAlphaChannel = false; // --alpha

   // --settings --nosettings
   protected SaveSettingsType saveSettingsType = SaveSettingsType.MATCH_INPUT;

   protected float outVersion = JDRAJR.CURRENT_VERSION;
   protected FileFormatType inFormat;// --from
   protected FileFormatType outFormat;// --to 
   protected Charset inCharset, outCharset; // --in-charset --out-charset

   TeXJavaHelpLib helpLib;
   private TeXJavaHelpLibAppAdapter helpLibApp;
   private HelpSetLocale helpSetLocale;
   private String dictLangTag;

   public static final String NAME = "jdrconverter";

   public static final int EXIT_SYNTAX=1;
   public static final int EXIT_OTHER=2;
}

enum SaveSettingsType
{
   NONE("none", 0), ALL("all", 1), PAPER_ONLY("paper-only", 2),
   MATCH_INPUT("match-input", 3);

   private SaveSettingsType(final String tag, final int id)
   {
      this.tag = tag;
      this.id = id;
   }

   public static SaveSettingsType getFromTag(String typeTag)
   {
      for (SaveSettingsType type : values())
      {
         if (type.tag.equals(typeTag))
         {
            return type;
         }
      }

      return null;
   }

   public static SaveSettingsType valueOf(int typeId)
   {
      for (SaveSettingsType type : values())
      {
         if (type.id == typeId)
         {
            return type;
         }
      }

      return null;
   }

   public String getTag()
   {
      return tag;
   }

   public int getId()
   {
      return id;
   }

   private final String tag;
   private final int id;
}

enum FileFormatType
{
  JDR(true, false), AJR(true, true), EPS(true, true), SVG(true, true), 
  PNG(false, false), TEX(false, true), CLS(false, true), STY(false, true);

  private FileFormatType(final boolean inputSupported, final boolean isTextFile)
  {
     this.inputSupported = inputSupported;
     this.isTextFile = isTextFile;
  }

  public boolean isInputSupported()
  {
     return inputSupported;
  }

  public boolean isTextFile()
  {
     return isTextFile;
  }

  public static FileFormatType getFormat(File file)
  {
      FileFormatType type = null;

      // guess from file extension

      String name = file.getName();
      int idx = name.lastIndexOf(".");

      if (idx > 0)
      {
         String ext = name.substring(idx+1).toUpperCase();

         type = valueOf(ext);
      }

      return type;
  }

  private final boolean inputSupported;
  private final boolean isTextFile;
}

class ConverterPublisher extends JDRDefaultMessage
{
   public ConverterPublisher(JDRConverter converter)
   {
      this.converter = converter;
   }

   @Override
   public String getMessageWithFallback(String tag, String altFormat,
     Object... values)
   {  
      return converter.getMessageWithFallback(tag, altFormat, values);
   }

   JDRConverter converter;
}
