/*
    Copyright (C) 2025 Nicola L.C. Talbot

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
package com.dickimawbooks.jdrconverter;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.Map;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLibAppAdapter;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

/**
 * For use where the export function needs to build a LaTeX document
 * as an intermediate step.
 */
public abstract class ExportImage
{
   public ExportImage(JDRConverter converter, File outputFile, JDRGroup jdrImage)
   {
      super();
      this.converter = converter;
      this.outputFile = outputFile;
      this.image = jdrImage;
   }

   public void createImage() 
     throws InvalidFormatException,IOException,InterruptedException
   {
      save();
   }

   public void process(MessageInfo... chunks)
   {
      getMessageSystem().publishMessages(chunks);
   }

   public void publishMessages(MessageInfo... chunks)
   {
      getMessageSystem().publishMessages(chunks);
   }

   public ConverterPublisher getMessageSystem()
   {
      return converter.getMessageSystem();
   }

   protected File getWorkingDirectory() throws IOException
   {
      if (texDir == null)
      {
         texDir = Files.createTempDirectory(converter.NAME).toFile();

         if (converter.isRemoveTempOn())
         {
            texDir.deleteOnExit();
         }
      }

      return texDir;
   }

   // Temporary file for intermediate process
   protected File getTeXFile() throws IOException
   {
      if (texFile == null)
      {
         getWorkingDirectory();
         texFile = File.createTempFile(converter.NAME, ".tex", texDir);
         texBase = getBaseName(texFile);
      }

      return texFile;
   }

   protected void exec(String[] cmdList)
     throws IOException,InterruptedException
   {
      getTeXFile();
      File orgDir = converter.getInputFile().getParentFile();

      if (orgDir == null)
      {
         orgDir = converter.getInputFile().getAbsoluteFile().getParentFile();
      }

      Process process = null;

      BufferedReader in = null;
      BufferedReader err = null;

      String line = null;

      StringBuffer buff = new StringBuffer();

      for (int i = 0; i < cmdList.length; i++)
      {
         buff.append(String.format(i == 0 ? "'%s'" : " '%s'", cmdList[i]));
      }

      String cmdListString = buff.toString();

      publishMessages(MessageInfo.createMessage(converter.getMessage(
         "process.running", cmdListString)));

      try
      {
         ProcessBuilder processBuilder = new ProcessBuilder(cmdList);

         processBuilder.directory(texDir);

         Map<String,String> env = processBuilder.environment();

         env.put("TEXINPUTS", String.format("%s%c",
              orgDir.getAbsolutePath(), File.pathSeparatorChar));

         process = processBuilder.start();

         in = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
         err = new BufferedReader(
            new InputStreamReader(process.getErrorStream()));

         long maxTime = converter.getMaxProcessTime();

         if (!process.waitFor(maxTime, TimeUnit.MILLISECONDS))
         {
            throw new TimedOutException(getMessageSystem(), maxTime);
         }

         int exitCode = process.exitValue();

         while ((line = in.readLine()) != null)
         {
         }

         while ((line = err.readLine()) != null)
         {
            publishMessages(MessageInfo.createWarning(line));
         }

         if (exitCode != 0)
         {
            converter.setExitCode(TeXJavaHelpLibAppAdapter.EXIT_PROCESS_FAILED);

            throw new IOException(String.format("%s%n%s",
                converter.getMessage("error.exec_failed_withcode_and_dir",
              buff.toString(), texDir.toString(), exitCode),
              converter.getMessage("error.try_latex_export")));
         }
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }

         if (err != null)
         {
            err.close();
         }
      }
   }

   public String getBaseName(File file)
   {
      String basename = file.getName();
      int idx = basename.lastIndexOf(".");
         
      if (idx > 0)
      {
         basename = basename.substring(0, idx);
      }

      return basename;
   }

   protected void createTeXFile(PrintWriter out) throws IOException
   {
      File src = converter.getInputFile();
      File base = src.getParentFile();

      if (base == null)
      {
         base = src.getAbsoluteFile().getParentFile();
      }

      PGF pgf = new PGF(base.toPath(), out);

      pgf.setUsePdfInfoEnabled(converter.isUsePdfInfoOn());

      pgf.setTextualExportShadingSetting(
         converter.getTextualExportShadingSetting());
      pgf.setTextPathExportOutlineSetting(
         converter.getTextPathExportOutlineSetting());

      writeComments(pgf);

      String preamble = null;

      preamble = converter.getConfigPreamble();

      pgf.saveDoc(image, preamble, 
        converter.isEncapsulateOn(), 
        converter.isConvertBitmapToEpsOn(),
        converter.isUseTypeblockAsBoundingBoxOn());
   }

   protected void save() 
     throws IOException,InterruptedException,InvalidFormatException,SecurityException
   {
      getTeXFile();

      File auxFile = new File(texDir, texBase+".aux");

      File logFile = new File(texDir, texBase+".log");

      PrintWriter out = null;

      try
      {
         out = new PrintWriter(new FileWriter(texFile));

         converter.verbosenoln(
            converter.getMessageWithFallback("info.saving", "Saving {0}", texFile)+" ");

         createTeXFile(out);

         converter.getMessageSystem().clearEol();

         out.close();
         out = null;

         File result = processImage(texBase);

         if (result != null)
         {
            converter.verboseln(
              converter.getMessageWithFallback("info.saving", "Saving {0}",
                 outputFile)+" ");

            Files.copy(result.toPath(), outputFile.toPath(),
               StandardCopyOption.REPLACE_EXISTING);
         }
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }

         if (converter.isRemoveTempOn())
         {
            File[] files = texDir.listFiles();

            for (File f : files)
            {
               f.deleteOnExit();
            }
         }
      }
   }

   protected abstract File processImage(String texBase)
      throws IOException,InterruptedException;

   protected void writeComments(PGF pgf) throws IOException
   {
      pgf.comment(converter.getMessage("tex.comment.created_by",
            converter.getApplicationName(), JDRResources.APP_VERSION));
      pgf.writeCreationDate();
   }


   protected File outputFile;
   protected JDRGroup image;
   protected JDRConverter converter;
   protected File texFile = null;
   protected File texDir = null;
   protected String texBase;
}
