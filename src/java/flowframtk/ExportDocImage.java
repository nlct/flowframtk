// File          : ExportDocImage.java
// Description   : Export image using a LaTeX document.
// Creation Date : 2015-10-01
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2015-2025 Nicola L.C. Talbot

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
package com.dickimawbooks.flowframtk;

import java.io.*;
import java.nio.file.*;
import java.awt.*;
import javax.swing.*;
import java.util.concurrent.TimeUnit;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdrresources.*;

public abstract class ExportDocImage extends ExportImage
{
   public ExportDocImage(JDRFrame frame, File file, JDRGroup jdrImage,
      boolean encapsulate, boolean convertBitmapToEps)
   {
      super(frame, file, jdrImage);
      this.encapsulate = encapsulate;
      this.convertBitmapToEps = convertBitmapToEps;
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

      publish(MessageInfo.createMessage(getResources().getMessage(
         "process.running", cmdListString)));

      try
      {
         ProcessBuilder processBuilder = new ProcessBuilder(cmdList);

         processBuilder.directory(dir);

         process = processBuilder.start();

         getMessageSystem().registerProcess(process);

         in = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
         err = new BufferedReader(
            new InputStreamReader(process.getErrorStream()));

         long maxTime = jdrFrame.getApplication().getMaxProcessTime();

         if (!process.waitFor(maxTime, TimeUnit.MILLISECONDS))
         {
            throw new TimedOutException(getResources(), maxTime);
         }

         int exitCode = process.exitValue();

         getMessageSystem().checkForInterrupt();

         while ((line = in.readLine()) != null)
         {
            getMessageSystem().checkForInterrupt();
         }

         while ((line = err.readLine()) != null)
         {
            publish(MessageInfo.createWarning(line));
            getMessageSystem().checkForInterrupt();
         }

         if (exitCode != 0)
         {
            throw new IOException(String.format("%s%n%s",
                getResources().getMessage("error.exec_failed_withcode_and_dir",
              buff.toString(), dir.toString(), exitCode),
              getResources().getMessage("error.try_latex_export")));
         }
      }
      finally
      {
         getMessageSystem().processDone();

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

   protected void writeComments(PGF pgf) throws IOException
   {
      pgf.comment(getResources().getMessage("tex.comment.created_by",
            getInvoker().getName(), getInvoker().getVersion()));
      pgf.writeCreationDate();

      pgf.comment(jdrFrame.getFilename());
   }

   protected abstract File processImage(String texBase)
      throws IOException,InterruptedException;

   protected abstract File getTeXFile() throws IOException;

   protected void save() throws IOException,InterruptedException
   {
      PrintWriter out = null;

      FlowframTk app = jdrFrame.getApplication();

      try
      {
         File texFile = getTeXFile();

         File dir = texFile.getParentFile();
         String texBase = texFile.getName();
         texBase = texBase.substring(0, texBase.lastIndexOf("."));

         File auxFile = new File(dir, texBase+".aux");
         auxFile.deleteOnExit();

         File logFile = new File(dir, texBase+".log");
         logFile.deleteOnExit();

         out = new PrintWriter(new FileWriter(texFile));

         PGF pgf = new PGF(texFile.getParentFile().toPath(), out);

         pgf.setUsePdfInfoEnabled(app.usePdfInfo());

         pgf.setTextualExportShadingSetting(
            app.getTextualExportShadingSetting());
         pgf.setTextPathExportOutlineSetting(
            app.getTextPathExportOutlineSetting());

         writeComments(pgf);

         String preamble = null;

         try
         {
            preamble = app.getConfigPreamble();
         }
         catch (IOException e)
         {
            publish(MessageInfo.createWarning(e));
         }

         pgf.saveDoc(image, preamble, encapsulate, convertBitmapToEps, 
           app.getSettings().useTypeblockAsBoundingBox);

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

   protected boolean encapsulate=true;
   protected boolean convertBitmapToEps=false;
}
