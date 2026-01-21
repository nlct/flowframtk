// File          : LoadEps.java
// Description   : Import EPS file.
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
 * Import image from EPS format. (This is still
 * experimental.)
 */
public class LoadEps extends IOSwingWorker
{
   private LoadEps(JDRFrame frame, ImportSettings importSettings)
   {
      super(frame, importSettings.currentFile, true);
      this.importSettings = importSettings;
   }

   public static void createAndRun(JDRFrame frame, ImportSettings importSettings)
   {
      LoadEps worker = new LoadEps(frame, importSettings);
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

      String fileName = file.getAbsolutePath();

      BufferedReader in = null;

      try
      {
         in = new BufferedReader(new FileReader(file));

         image = EPS.load(cg, in, importSettings);

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
            if (in != null)
            {
               in.close();
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

   ImportSettings importSettings;
}
