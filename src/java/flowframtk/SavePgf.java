// File          : SavePgf.java
// Description   : Export to pgfpicture.
// Creation Date : 14th July 2008
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2008-2025 Nicola L.C. Talbot

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
import java.awt.*;
import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdrresources.*;

public class SavePgf extends ExportImage
{
   public SavePgf(JDRFrame frame, File file, JDRGroup jdrImage,
       ExportSettings exportSettings)
   {
      super(frame, file, jdrImage, exportSettings);
   }

   protected void writeComments(PGF pgf) throws IOException
   {
      pgf.comment(getResources().getMessage("tex.comment.created_by",
            getInvoker().getName(), getInvoker().getVersion()));
      pgf.writeCreationDate();
   
      pgf.comment(jdrFrame.getFilename());
   }

   public void save() throws IOException,InterruptedException
   {
      PrintWriter out = null;

      FlowframTk app = jdrFrame.getApplication();

      try
      {
         out = new PrintWriter(new FileWriter(outputFile));

         PGF pgf = new PGF(outputFile.getParentFile().toPath(), out,
          exportSettings);

         writeComments(pgf);

         if (exportSettings.type == ExportSettings.Type.IMAGE_DOC)
         {
            String preamble = null;

            try
            {
               preamble = app.getConfigPreamble();
            }
            catch (IOException e)
            {
               publish(MessageInfo.createWarning(e));
            }

            pgf.saveDoc(image, preamble);
         }
         else
         {

            pgf.println("\\iffalse");

            pgf.comment(getResources().getMessage("tex.comment.preamble"));

            pgf.writePreambleCommands(image, true, true);

            pgf.comment(getResources().getMessage(
               "tex.comment.fontsize", 
               ""+((int)image.getCanvasGraphics().getLaTeXNormalSize())+"pt"));

            pgf.comment(getResources().getMessage("tex.comment.endpreamble"));
            pgf.println("\\fi");

            pgf.save(image);
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
}
