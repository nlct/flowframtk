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
package com.dickimawbooks.flowframtk;

import java.util.List;
import java.io.*;
import java.beans.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

/**
 * Import image from Acorn Draw file.
 */
public class LoadAcornDrawFile extends IOSwingWorker
{
   private LoadAcornDrawFile(JDRFrame frame, ImportSettings importSettings)
   {
      super(frame, importSettings.currentFile, true);
      this.importSettings = importSettings;
   }

   public static void createAndRun(JDRFrame frame, ImportSettings importSettings)
   {
      LoadAcornDrawFile worker = new LoadAcornDrawFile(frame, importSettings);
      worker.initialise();
      worker.execute();
   }

   public JDRGroup doInBackground()
    throws IOException,InterruptedException,InvalidFormatException,
           NoninvertibleTransformException
   {
      JDRGroup image=null;

      CanvasGraphics cg = jdrFrame.getCanvasGraphics();

      FlowframTk app = jdrFrame.getApplication();

      if (cg == null)
      {
         cg = (CanvasGraphics)app.getDefaultCanvasGraphics().clone();
      }

      File drawFile = importSettings.currentFile;

      DataInputStream din = null;

      try
      {
         din = new DataInputStream(new BufferedInputStream(new FileInputStream(drawFile)));

         AcornDrawFile adf = new AcornDrawFile(cg, din, importSettings);

         if (importSettings.useMappings)
         {     
            adf.setTextModeMappings(app.getTextModeMappings());
            adf.setMathModeMappings(app.getMathModeMappings());
         }  

         image = adf.readData();

         if (image.anyDraftBitmaps())
         {
            publish(MessageInfo.createWarning(
               getResources().getMessage("warning.draft_bitmaps")));
         }
      }
      finally
      {
         try
         {
            if (din != null)
            {
               din.close();
            }
         }
         catch (IOException e)
         {
            publish(
            MessageInfo.createError(getResources().getMessage("error.io.close")),
            MessageInfo.createError(e));
         }
      }

      return image;
   }

   @Override
   protected void finish(JDRGroup image)
   {
      if (image != null)
      {
         jdrFrame.setImage(image);
         jdrFrame.markAsModified();
         jdrFrame.getApplication().setTool(jdrFrame.currentTool());
      }

      super.finish(image);
   }

   private ImportSettings importSettings;
}
