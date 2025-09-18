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

import com.dickimawbooks.texjavahelplib.*;
import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdrresources.*;

/**
 * Export to a file format that's created from a temporary LaTeX
 * document using external processes.
 */

public abstract class ExportDocImage extends ExportImage
{
   public ExportDocImage(JDRFrame frame, File file, JDRGroup jdrImage,
      ExportSettings exportSettings)
   {
      super(frame, file, jdrImage, exportSettings);

      File imageFile = frame.getFile();

      if (imageFile != null)
      {
         imageBase = imageFile.getParentFile();

         if (imageBase == null)
         {
            imageBase = imageFile.getAbsoluteFile().getParentFile();
         }
      }
   }

   protected void exec(String... cmdList)
     throws IOException,InterruptedException
   {
      TeXJavaHelpLib helpLib = getResources().getHelpLib();

      File dir = getTeXFile().getParentFile();

      long maxTime = jdrFrame.getApplication().getMaxProcessTime();

      String cmdListString = helpLib.cmdListToString(cmdList);

      publish(MessageInfo.createMessage(getResources().getMessage(
         "process.running", cmdListString)));

      int exitCode = -1;

      try
      {
         exitCode = helpLib.execCommandAndWaitFor(dir,
           imageBase, true, TeXJavaHelpLib.MessageType.NORMAL,
            (StringBuilder)null, maxTime, 0,
            getMessageSystem(),
            cmdList);
      }
      finally
      {
         getMessageSystem().processDone();
      }
   }

   protected void writeComments(TeX tex) throws IOException
   {
      tex.comment(getResources().getMessage("tex.comment.created_by",
            getInvoker().getName(), getInvoker().getVersion()));
      tex.writeCreationDate();

      tex.comment(jdrFrame.getFilename());
   }

   protected abstract File processImage()
      throws IOException,InterruptedException,MissingProcessorException;

   protected File getTeXFile() throws IOException
   {
      return texFile;
   }

   protected void ensureWorkingDirectoryCreated()
   throws IOException
   {
      if (texDir == null)
      {
         texDir = Files.createTempDirectory(
           getResources().getApplicationName().toLowerCase()).toFile();

         texDir.deleteOnExit();

         texBase = texDir.getName();
         texFile = new File(texDir, texBase + ".tex");
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
    throws IOException,InterruptedException,
      MissingTypeBlockException,
      InvalidShapeException,
      MissplacedTypeBlockException,
      MissingProcessorException
   {
      ensureWorkingDirectoryCreated();

      PrintWriter out = null;

      FlowframTk app = jdrFrame.getApplication();

      try
      {
         File texFile = getTeXFile();

         File dir = texFile.getParentFile();

         out = new PrintWriter(new FileWriter(texFile));

         String preamble = null;

         try
         {
            preamble = app.getConfigPreamble();
         }
         catch (IOException e)
         {
            publish(MessageInfo.createWarning(e));
         }

         if (preamble == null)
         {
            preamble = "\\batchmode ";
         }
         else
         {
            preamble = "\\batchmode " + preamble;
         }

         if (exportSettings.type == ExportSettings.Type.FLF_PDF)
         {
            FLF flf = new FLF(dir, out, exportSettings);
   
            writeComments(flf);

            flf.saveCompleteDoc(image, preamble);
         }
         else
         {
            PGF pgf = new PGF(texFile.getParentFile().toPath(), out,
             exportSettings);

            writeComments(pgf);

            pgf.saveDoc(image, preamble);
         }

         out.close();
         out = null;

         File result = processImage();

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
         removeTempFiles();

         if (out != null)
         {
            out.close();
         }
      }
   }

   protected void removeTempFiles()
   {
      File[] files = texDir.listFiles();

      for (File f : files)
      {
         f.deleteOnExit();
      }
   }

   protected File imageBase;

   protected File texFile = null;
   protected File texDir = null;
   protected String texBase;
}
