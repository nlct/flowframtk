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

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

public abstract class ExportImage implements MessageInfoPublisher
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
      getMessageSystem().setPublisher(this);

      publishMessages(MessageInfo.createMessage(
        converter.getMessage("info.saving", outputFile.toString())));

      save();

      publishMessages(
         MessageInfo.createMessage(converter.getMessage("message.done")));
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

   // Temporary file for intermediate process
   protected File getTeXFile() throws IOException
   {
      if (texFile == null)
      {
         texFile = File.createTempFile(converter.NAME, ".tex");
         texFile.deleteOnExit();
      }

      return texFile;
   }

   protected void exec(String[] cmdList)
     throws IOException,InterruptedException
   {
      File dir = getTeXFile().getParentFile();

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

         processBuilder.directory(dir);

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
            throw new IOException(String.format("%s%n%s",
                converter.getMessage("error.exec_failed_withcode_and_dir",
              buff.toString(), dir.toString(), exitCode),
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

   protected void save() 
     throws IOException,InterruptedException,InvalidFormatException
   {
      File texFile = getTeXFile();

      File dir = texFile.getParentFile();
      String texBase = getBaseName(texFile);

      File auxFile = new File(dir, texBase+".aux");
      auxFile.deleteOnExit();

      File logFile = new File(dir, texBase+".log");
      logFile.deleteOnExit();

      PrintWriter out = null;

      try
      {
         out = new PrintWriter(new FileWriter(texFile));

         PGF pgf = new PGF(texFile.getParentFile().toPath(), out);

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

         out.close();
         out = null;

         File result = processImage(texBase);

         if (result != null && !result.equals(outputFile))
         {
            if (outputFile.exists())
            {
               outputFile.delete();
            }

            if (!result.renameTo(outputFile))
            {
               Files.copy(result.toPath(), outputFile.toPath(),
                  StandardCopyOption.REPLACE_EXISTING);
               result.deleteOnExit();
            }
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
}
