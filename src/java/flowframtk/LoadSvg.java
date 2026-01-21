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
package com.dickimawbooks.flowframtk;

import java.util.List;
import java.io.*;
import java.beans.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

import org.xml.sax.SAXException;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

/**
 * Import image from SVG format. 
 */
public class LoadSvg extends IOSwingWorker
{
   private LoadSvg(JDRFrame frame, ImportSettings importSettings)
   {
      super(frame, importSettings.currentFile, true);
      this.importSettings = importSettings;
   }

   public static void createAndRun(JDRFrame frame, ImportSettings importSettings)
   {
      LoadSvg worker = new LoadSvg(frame, importSettings);
      worker.initialise();
      worker.execute();
   }

   public JDRGroup doInBackground()
    throws IOException,InterruptedException,
           SAXException
   {
      JDRGroup image=null;

      CanvasGraphics cg = jdrFrame.getCanvasGraphics();

      FlowframTk app = jdrFrame.getApplication();

      if (cg == null)
      {
         cg = (CanvasGraphics)app.getDefaultCanvasGraphics().clone();
      }

      BufferedReader in = null;
      Graphics2D orgG2 = cg.getGraphics();
      Graphics2D g2 = (Graphics2D)jdrFrame.getCanvas().getGraphics();

      try
      {
         if (g2 != null)
         {
            cg.setGraphicsDevice(g2);
         }

         in = new BufferedReader(new FileReader(file));

         image = SVG.load(cg, file.getParentFile(), in, importSettings,
           app.getTextModeMappings(), app.getMathModeMappings());
      }
      finally
      {
         if (g2 != null)
         {
            cg.setGraphicsDevice(orgG2);
            g2.dispose();
         }

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

   ImportSettings importSettings;
}
