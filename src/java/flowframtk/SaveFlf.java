// File          : SaveFlf.java
// Description   : Export flowfram data.
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
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

public class SaveFlf extends ExportImage
{
   public SaveFlf(JDRFrame frame, File file, JDRGroup jdrImage)
   {
      super(frame, file, jdrImage);
   }

   public void save() 
     throws IOException,InterruptedException,InvalidFormatException
   {
      PrintWriter out = null;

      FlowframTk app = jdrFrame.getApplication();

      try
      {
         out = new PrintWriter(new FileWriter(outputFile));

         FLF flf = new FLF(outputFile.getParentFile(), out,
          app.getSettings().hasMinimumFlowFramSty2_0());

         flf.setTextualExportShadingSetting(
            app.getTextualExportShadingSetting());
         flf.setTextPathExportOutlineSetting(
            app.getTextPathExportOutlineSetting());

         flf.comment(getResources().getMessage("tex.comment.created_by",
               getInvoker().getName(), getInvoker().getVersion()));
         flf.writeCreationDate();
         flf.comment(jdrFrame.getFilename());

         if (!outputFile.getName().toLowerCase().endsWith(".cls"))
         {
            flf.comment(getResources().getMessage(
               "tex.comment.fontsize",
               ""+((int)image.getCanvasGraphics().getLaTeXNormalSize())+"pt"));
         }

         flf.save(image, outputFile.getName(), 
           jdrFrame.getApplication().useHPaddingShapepar());

         if (!image.anyFlowFrameData())
         {
            publish(
               MessageInfo.createWarning(
                  getResources().getMessage("warning.no_flowframe_data")));
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
