// File          : SavePgf.java
// Description   : Export to pgfpicture.
// Creation Date : 14th July 2008
// Author        : Nicola L.C. Talbot
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
package com.dickimawbooks.flowframtk;

import java.io.*;
import java.awt.*;
import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdrresources.*;

public class SavePgf extends ExportImage
{
   public SavePgf(JDRFrame frame, File file, JDRGroup jdrImage)
   {
      super(frame, file, jdrImage);
   }

   public void save() throws IOException,InterruptedException
   {
      PrintWriter out = null;

      FlowframTk app = jdrFrame.getApplication();

      try
      {
         out = new PrintWriter(new FileWriter(outputFile));

         PGF pgf = new PGF(outputFile.getParentFile().toPath(), out);

         pgf.comment(jdrFrame.getFilename());

         pgf.setTextualExportShadingSetting(
            app.getTextualExportShadingSetting());
         pgf.setTextPathExportOutlineSetting(
            app.getTextPathExportOutlineSetting());

         pgf.comment(getResources().getMessage("tex.comment.created_by",
               getInvoker().getName(), getInvoker().getVersion()));
         pgf.writeCreationDate();

         pgf.println("\\iffalse");
         pgf.comment(getResources().getString("tex.comment.preamble"));

         pgf.println("\\usepackage{ifpdf}");
         pgf.println("\\makeatletter");
         pgf.writeOutlineDef();
         pgf.println("\\makeatother");
         pgf.writePreambleCommands(image);

         pgf.comment(getResources().getMessage(
            "tex.comment.fontsize", 
            ""+((int)image.getCanvasGraphics().getLaTeXNormalSize())+"pt"));

         pgf.comment(getResources().getString("tex.comment.endpreamble"));
         pgf.println("\\fi");

         pgf.save(image, app.getSettings().useTypeblockAsBoundingBox);

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
