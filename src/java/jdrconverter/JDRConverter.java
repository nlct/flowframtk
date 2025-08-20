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
import java.util.Properties;
import java.text.MessageFormat;
import java.awt.geom.NoninvertibleTransformException;
import org.xml.sax.SAXException;

import com.dickimawbooks.texjavahelplib.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.io.eps.*;
import com.dickimawbooks.jdr.io.svg.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.JDRResources;

public class JDRConverter
{
   public JDRConverter()
   {
      msgPublisher = new ConverterPublisher(this);
      msgPublisher.setVerbosity(0);
      userConfigProperties = new Properties();
   }

   protected void initConfig() throws IOException
   {
      userConfigDir = JDRResources.findUserConfigDir(false);
      BufferedReader in = null;
      File file;

      try
      {
         if (userConfigDir != null && userConfigDir.exists())
         {
            file = new File(userConfigDir, "flowframtk.conf");

            if (!file.exists())
            {
               file = new File(userConfigDir, "jpgfdraw.conf");
            }

            if (file.exists())
            {
               in = new BufferedReader(new FileReader(file));
               loadConfig(in);
               in.close();
               in = null;
            }

            normalsize = getIntProperty("normalsize", normalsize);

            useRelativeBitmaps = getBoolProperty("relative_bitmaps",
               useRelativeBitmaps);

            useTypeblockAsBBox = getBoolProperty("use_typeblock_as_bbox",
               useTypeblockAsBBox);

            addAlphaChannel = getBoolProperty("png_alpha", addAlphaChannel);

            encapsulate = getBoolProperty("png_encap", encapsulate);

            flowframeAbsPages = getBoolProperty("flowframe_abs_pages",
               flowframeAbsPages);

            usePdfInfo = getBoolProperty("pdfinfo", encapsulate);

            useHPaddingShapepar = getBoolProperty("shapeparhpadding",
               useHPaddingShapepar);

            textualShadingExportSetting = getIntProperty("textualshadingexport",
               textualShadingExportSetting);

            textualOutlineExportSetting = getIntProperty("textpathoutlineexport",
               textualOutlineExportSetting);

            file = new File(userConfigDir, "languages.conf");

            if (file.exists())
            {
               in = Files.newBufferedReader(file.toPath());
               userConfigProperties.load(in);
               in.close();
               in = null;
            }
         }
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
   }


   protected void loadConfig(BufferedReader in) throws IOException
   {
      String line;

      while ((line = in.readLine()) != null)
      {
         String trimmed = line.trim();

         if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

         String[] split = line.split("=", 2);

         if (split.length == 2)
         {
            userConfigProperties.put(split[0], split[1]);
         }
      }
   }

   protected boolean getBoolProperty(String propName, boolean defValue)
   {
      String val = userConfigProperties.getProperty(propName);

      if (val != null)
      {
         try
         {
            return (Integer.parseInt(val) == 1);
         }
         catch (NumberFormatException e)
         {
         }
      }

      return defValue;
   }

   protected int getIntProperty(String propName, int defValue)
   {
      String val = userConfigProperties.getProperty(propName);

      if (val != null)
      {
         try
         {
            return Integer.parseInt(val);
         }
         catch (NumberFormatException e)
         {
         }
      }

      return defValue;
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
             msgPublisher.message(msg);
          }

      
          @Override
          public int getExitCode(Throwable e, boolean isFatal)
          {
             if (e instanceof InvalidFormatException)
             {
                return EXIT_INVALID_DATA;
             }
             else if (e instanceof InterruptedException)
             {
                return EXIT_PROCESS_FAILED;
             }
             else
             {
                return super.getExitCode(e, isFatal);
             }
          }

       };


      if (helpSetLocale == null)
      {
         String dictLangTag = userConfigProperties.getProperty("dict_lang");

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

   public ConverterPublisher getMessageSystem()
   {
      return msgPublisher;
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

      helpLib.printSyntaxItem(getMessage("syntax.latex-dvi", "--latex-dvi"));
      helpLib.printSyntaxItem(getMessage("syntax.latex-pdf", "--latex-pdf"));
      helpLib.printSyntaxItem(getMessage("syntax.dvips", "--dvips"));
      helpLib.printSyntaxItem(getMessage("syntax.dvisvgm", "--dvisvgm"));
      helpLib.printSyntaxItem(getMessage("syntax.libgs", "--libgs"));

      helpLib.printSyntaxItem(getMessage("syntax.alpha", "--[no]alpha"));
      helpLib.printSyntaxItem(getMessage("syntax.normalsize", "--normalsize"));
      helpLib.printSyntaxItem(getMessage("syntax.bitmap_basename", "--bitmap-basename"));

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

      helpLib.printSyntaxItem(getMessage("syntax.rm-tmp-files", "--[no]rm-tmp-files"));

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

            initConfig();
         }
         catch (IOException e)
         {
            e.printStackTrace();
            System.exit(TeXJavaHelpLibAppAdapter.EXIT_HELPSET);
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
             || arg.equals("--normalsize")
             || arg.equals("--bitmap-basename")
             || arg.equals("--latex-dvi")
             || arg.equals("--latex-pdf")
             || arg.equals("--dvips")
             || arg.equals("--dvisvgm")
             || arg.equals("--libgs")
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
            // called by --debug
            debugMode = true;
            msgPublisher.setDebugMode(true);
            msgPublisher.setVerbosity(2);
            msgPublisher.displayMessages();

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

            if (originalArgList[preparseIndex].equals("--nodebug")
                   || originalArgList[preparseIndex].equals("--no-debug")
                    )
            {
               msgPublisher.setDebugMode(false);
            }
            else if (originalArgList[preparseIndex].equals("--locale"))
            {
               preparseIndex++;

               if (preparseIndex >= originalArgList.length)
               {
                  throw new InvalidSyntaxException("Missing value after --locale");
               }

               userConfigProperties.put("dict_lang", originalArgList[preparseIndex]);

               ensureHelpSetLoaded();
            }
            else if (originalArgList[preparseIndex].startsWith("--locale="))
            {
               userConfigProperties.put("dict_lang",
                  originalArgList[preparseIndex].substring(10));

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
            else if (originalArgList[preparseIndex].equals("-bitmap"))
            {
               deque.add("--bitmap-basename");
            }
            else if (originalArgList[preparseIndex].equals("-nosettings")
                  || originalArgList[preparseIndex].equals("-doc")
                  || originalArgList[preparseIndex].equals("-nodoc")
                  || originalArgList[preparseIndex].equals("-alpha")
                  || originalArgList[preparseIndex].equals("-noalpha")
                  || originalArgList[preparseIndex].equals("-crop")
                  || originalArgList[preparseIndex].equals("-nocrop")
                  || originalArgList[preparseIndex].equals("-normalsize")
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
            if (arg.equals("--verbose"))
            {
               msgPublisher.setVerbosity(1);
               msgPublisher.displayMessages();
            }
            else if (arg.equals("--noverbose"))
            {
               msgPublisher.setVerbosity(0);
               msgPublisher.displayMessages();
            }
            else if (arg.equals("--quiet"))
            {
               msgPublisher.hideMessages();
            }
            else if (arg.equals("--rm-tmp-files"))
            {
               removeTempFiles = true;
            }
            else if (arg.equals("--norm-tmp-files"))
            {
               removeTempFiles = false;
            }
            else if (arg.equals("--nosettings"))
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
               useTypeblockAsBBox = true;
            }
            else if (arg.equals("--nouse-typeblock"))
            {
               useTypeblockAsBBox = false;
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
                  if (type.isOutputSupported())
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
            else if (isIntArg(arg, "--normalsize", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               normalsize = returnVals[0].intValue();
            }
            else if (isArg(arg, "--bitmap-basename", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               bitmapBase = returnVals[0].toString();
            }
            else if (isArg(arg, "--latex-dvi", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               userConfigProperties.put("latex_app", returnVals[0].toString());
            }
            else if (isArg(arg, "--pdflatex-dvi", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               userConfigProperties.put("pdflatex_app", returnVals[0].toString());
            }
            else if (isArg(arg, "--dvips", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               userConfigProperties.put("dvips_app", returnVals[0].toString());
            }
            else if (isArg(arg, "--dvisvgm", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               userConfigProperties.put("dvisvgm_app", returnVals[0].toString());
            }
            else if (isArg(arg, "--libgs", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getMessage("error.clisyntax.missing.value", arg));
               }

               userConfigProperties.put("libgs_app", returnVals[0].toString());
            }
            else
            {
               return false;
            }

            return true;
         }
      };

      cliParser.preparse();

      if (debugMode)
      {
         msgPublisher.setDebugVerbosityThreshold(-1);
      }

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

      if (!outFormat.isOutputSupported())
      {
         throw new InvalidSyntaxException(getMessage("error.cant_export_to", outFormat));
      }

      if (!outFormat.canTeXToolsCreate())
      {
         useLaTeX = false;
      }
      else if (outFormat.requiresTeXTools())
      {
         useLaTeX = true;
      }

      if (bitmapBase == null)
      {
         String name = outFile.getName();
         int idx = name.lastIndexOf(".");

         if (idx > 0)
         {
            bitmapBase = name.substring(0, idx);
         }
         else
         {
            bitmapBase = name;
         }
      }

      if (bitmapDir == null)
      {
         bitmapDir = outFile.getParentFile();

         if (bitmapDir == null)
         {
            bitmapDir = outFile.getAbsoluteFile().getParentFile();
         }
      }
   }

   protected void run()
     throws IOException,
            InvalidFormatException,
            NoninvertibleTransformException,
            InterruptedException,
            SAXException
   {
      JDRGroup paths = null;

      CanvasGraphics canvasGraphics 
         = new CanvasGraphics(msgPublisher);

      canvasGraphics.getLaTeXFontBase().setNormalSize(normalsize);
      canvasGraphics.setUseAbsolutePages(flowframeAbsPages);

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
            case EPS:
               paths = loadEps(canvasGraphics, in);
            break;
            case SVG:
// TODO fix this
               paths = SVG.load(canvasGraphics, in);
            break;
            case ACORN_DRAWFILE:
// TODO Work in progress
               paths = AcornDrawFile.load(canvasGraphics, din, bitmapDir, bitmapBase);
               settingsFlag = JDR.ALL_SETTINGS;
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

      if (paths == null || paths.size() == 0)
      {
         throw new InvalidFormatException(getMessage("error.no_image"));
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

      if (!useLaTeX)
      {
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
         else if (outFormat != FileFormatType.PNG)
         {
            dout = new DataOutputStream(new FileOutputStream(outFile));
         }
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
              savePgf(paths, out);
            break;
            case CLS:
            case STY:
              saveFlf(paths, out);
            break;
            case EPS:
               saveEps(paths, out);
            break;
            case PNG:
               PNG.save(paths, outFile, addAlphaChannel, encapsulate);
            break;
            case SVG:
               saveSvg(paths, out);
            break;
            case PDF:
               savePdf(paths);
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

   protected void savePgf(JDRGroup paths, PrintWriter out)
     throws IOException,InvalidFormatException
   {
      PGF pgf = new PGF(outFile.getParentFile(), out);
      pgf.comment(getMessage("message.created_by", NAME));
      pgf.writeCreationDate();

      if (completeDoc)
      {
         pgf.saveDoc(paths, null, encapsulate, convertBitmapToEps, useTypeblockAsBBox);
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

         pgf.save(paths, useTypeblockAsBBox);
      }
   }

   protected void saveFlf(JDRGroup paths, PrintWriter out)
     throws IOException,InvalidFormatException
   {
      File dir = outFile.getParentFile();

      if (dir == null)
      {
         dir = new File(System.getProperty("user.dir"));
      }

      FLF flf = new FLF(dir, out);

      flf.setTextualExportShadingSetting(
         getTextualExportShadingSetting());
      flf.setTextPathExportOutlineSetting(
         getTextPathExportOutlineSetting());

      flf.comment(getMessage("tex.comment.created_by",
            NAME, JDRResources.APP_VERSION));
      flf.writeCreationDate();

      if (outFormat == FileFormatType.STY)
      {
         flf.comment(getMessage("tex.comment.fontsize",
            ""+normalsize+"pt"));
      }

      flf.save(paths, outFile.getName(), useHPaddingShapepar);

      if (!paths.anyFlowFrameData())
      {
         getMessageSystem().warning(getMessage("warning.no_flowframe_data"));
      }
   }

   protected void savePdf(JDRGroup paths)
      throws IOException,InvalidFormatException,InterruptedException
   {
      ExportImage exporter = new ExportImage(this, outFile, paths)
       {
          @Override
          protected File processImage(String texBase)
            throws IOException,InterruptedException
          {
             File dir = getTeXFile().getParentFile();

             File pdfFile = new File(dir, texBase+".pdf");

             exec(new String[] {getPdfLaTeXPath(), "-interaction", "batchmode", texBase});

             return pdfFile;
          }
       };

       exporter.createImage();
   }

   protected void saveSvg(JDRGroup paths, PrintWriter out)
      throws IOException,InvalidFormatException,InterruptedException
   {
      if (useLaTeX)
      {
         ExportImage exporter = new ExportImage(this, outFile, paths)
          {
             @Override
             protected File processImage(String texBase)
               throws IOException,InterruptedException
             {
                File dir = getTeXFile().getParentFile();

                File dviFile = new File(dir, texBase+".dvi");
                dviFile.deleteOnExit();

                exec(new String[] {getDviLaTeXPath(),
                 "-interaction", "batchmode", texBase});

                String[] cmd;

                File svgFile = new File(dir, texBase+".svg");

                String libgs = getLibGsPath();

                if (libgs == null || libgs.isEmpty())
                {
                   cmd = new String[] {getDviSvgmPath(), "-o", svgFile.getName(),
                      dviFile.getName()};
                }
                else
                {
                   cmd = new String[] {getDviSvgmPath(),
                     "--libgs="+libgs,
                     "-o", svgFile.getName(), dviFile.getName()};
                }

                exec(cmd);

                return svgFile;
             }
          };

          exporter.createImage();
      }
      else
      {
         SVG.save(paths, paths.getDescription(), out);
      }
   }

   protected void saveEps(JDRGroup paths, PrintWriter out)
    throws IOException,InvalidFormatException,InterruptedException
   {
      if (useLaTeX)
      {
         ExportImage exporter = new ExportImage(this, outFile, paths)
          {
             @Override
             protected File processImage(String texBase)
               throws IOException,InterruptedException
             {
                File dir = getTeXFile().getParentFile();

                File dviFile = new File(dir, texBase+".dvi");
                dviFile.deleteOnExit();

                exec(new String[] {getDviLaTeXPath(),
                  "-interaction", "batchmode", texBase});

                File epsFile = new File(dir, outputFile.getName());

                exec(new String[] {getDviPsPath(),
                  "-o", epsFile.getName(), dviFile.getName()});

                return epsFile;
             }
          };

          exporter.createImage();
      }
      else
      {
         EPS.save(paths, out, NAME);
      }
   }

   protected JDRGroup loadEps(CanvasGraphics cg, BufferedReader in)
   throws IOException,InvalidFormatException,NoninvertibleTransformException
   {
      if (userConfigDir != null)
      {
         File file = new File(userConfigDir, "psfontmap");

         if (file.exists() && file.isFile())
         {
            LaTeXFont.loadPostScriptMappings(getMessageSystem(), file);
         }
      }

      return EPS.load(cg, in, bitmapBase);
   }


   public String getDviLaTeXPath()
   {
      return userConfigProperties.getProperty("latex_app", "latex");
   }

   public String getPdfLaTeXPath()
   {
      return userConfigProperties.getProperty("pdflatex_app", "pdflatex");
   }

   public String getDviPsPath()
   {
      return userConfigProperties.getProperty("dvips_app", "dvips");
   }

   public String getDviSvgmPath()
   {
      return userConfigProperties.getProperty("dvisvgm_app", "dvisvgm");
   }

   public String getLibGsPath()
   {
      String path = userConfigProperties.getProperty("libgs");

      if (path == null)
      {
         path = System.getenv("LIBGS");
      }

      return path;
   }

   public String getBitmapCs()
   {
      return userConfigProperties.getProperty("bitmap_default_cs", "\\includegraphics");
   }

   public long getMaxProcessTime()
   {
      return maxProcessTime;
   }

   public boolean isUsePdfInfoOn()
   {
      return usePdfInfo;
   }

   public int getTextualExportShadingSetting()
   {
      return textualShadingExportSetting;
   }

   public int getTextPathExportOutlineSetting()
   {
      return textualOutlineExportSetting;
   }

   public boolean isEncapsulateOn()
   {
      return encapsulate;
   }

   public boolean isConvertBitmapToEpsOn()
   {
      return convertBitmapToEps;
   }

   public boolean isUseTypeblockAsBoundingBoxOn()
   {
      return useTypeblockAsBBox;
   }

   protected void initConfigPreamble() throws IOException
   {
      File file = new File(userConfigDir, "preamble.tex");

      if (file.exists())
      {
         StringBuilder builder = new StringBuilder();

         BufferedReader in = null;

         String eol = System.getProperty("line.separator");

         try
         {
            in = Files.newBufferedReader(file.toPath());

            String line;

            while ((line = in.readLine()) != null)
            {
               builder.append(line);
               builder.append(eol);
            }
         }
         finally
         {
            if (in != null)
            {
               in.close();
            }
         }

         configPreamble = builder.toString();
      }
   }

   public String getConfigPreamble()
   {
      if (configPreamble == null)
      {
         configPreamble = "";

         if (userConfigDir != null)
         {
            try
            {
               initConfigPreamble();
            }
            catch (IOException e)
            {
               getMessageSystem().warning(e);
            }
         }
      }

      return configPreamble;
   }

   public File getInputFile()
   {
      return inFile;
   }

   public File getOutputFile()
   {
      return outFile;
   }

   public boolean isRemoveTempOn()
   {
      return removeTempFiles;
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
         app.setExitCode(TeXJavaHelpLibAppAdapter.EXIT_SYNTAX);
      }
      catch (IOException e)
      {
         app.error(e.getMessage(), app.debugMode ? e : null);
      }
      catch (InvalidFormatException e)
      {
         app.error(e.getMessage(), app.debugMode ? e : null);
      }
      catch (Throwable e)
      {
         app.error(null, e);
      }

      System.exit(app.getExitCode());
   }

   protected CLISyntaxParser cliParser;
   protected ConverterPublisher msgPublisher;

   protected int exitCode = 0;
   protected boolean debugMode = false;
   protected boolean removeTempFiles = true;
   protected boolean shownVersion = false;
   protected File inFile, outFile; // --in / -i , --output / -o
   protected boolean completeDoc = false; // --doc
   protected boolean useTypeblockAsBBox = false; // --use-typeblock
   protected boolean encapsulate = false; // --crop / -C
   protected boolean convertBitmapToEps = false; // --bitmaps-to-eps
   protected boolean addAlphaChannel = false; // --alpha
   protected String bitmapBase; // --bitmap-basename
   protected File bitmapDir;
   protected boolean useRelativeBitmaps = true;
   protected boolean flowframeAbsPages = false;
   protected boolean usePdfInfo = false;
   protected boolean antialias=true, renderquality=true;
   protected long maxProcessTime = 300000L;
   protected boolean useLaTeX=false;
   protected int textualShadingExportSetting = TeX.TEXTUAL_EXPORT_SHADING_AVERAGE;
   protected int textualOutlineExportSetting = TeX.TEXTPATH_EXPORT_OUTLINE_TO_PATH;
   protected boolean useHPaddingShapepar = false;

   protected int normalsize=10;// --normalsize

   // --settings --nosettings
   protected SaveSettingsType saveSettingsType = SaveSettingsType.MATCH_INPUT;

   protected float outVersion = JDRAJR.CURRENT_VERSION;
   protected FileFormatType inFormat;// --from
   protected FileFormatType outFormat;// --to 
   protected Charset inCharset, outCharset; // --in-charset --out-charset

   TeXJavaHelpLib helpLib;
   private TeXJavaHelpLibAppAdapter helpLibApp;
   private HelpSetLocale helpSetLocale;

   private File userConfigDir = null;
   private Properties userConfigProperties = null;
   private String configPreamble=null;

   public static final String NAME = "jdrconverter";
}
