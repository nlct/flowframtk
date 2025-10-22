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
public class LoadAcornDrawFile extends SwingWorker<Void,MessageInfo>
  implements MessageInfoPublisher
{
   public LoadAcornDrawFile(JDRFrame frame, ImportSettings importSettings)
   {
      jdrFrame = frame;
      this.importSettings = importSettings;
   }

   public Void doInBackground()
    throws IOException,InterruptedException,InvalidFormatException,
           NoninvertibleTransformException
   {
      jdrFrame.setIoInProgress(true);
      JDRGroup image=null;

      getMessageSystem().setPublisher(this);

      CanvasGraphics cg = jdrFrame.getCanvasGraphics();

      FlowframTk app = jdrFrame.getApplication();

      if (cg == null)
      {
         cg = (CanvasGraphics)app.getDefaultCanvasGraphics().clone();
      }

      File drawFile = importSettings.currentFile;
      String fileName = drawFile.getAbsolutePath();

      app.showMessageFrame(getResources().getMessage("info.loading", fileName));

      Cursor oldCursor = jdrFrame.getCursor();
      jdrFrame.setCursor(
         Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

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
         jdrFrame.setCursor(oldCursor);
         app.setTool(jdrFrame.currentTool());
         jdrFrame.setIoInProgress(false);

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

         if (image != null)
         {
            jdrFrame.setImage(image);
            jdrFrame.markAsModified();
         }
      }

      return null;
   }

   public void done()
   {
      try
      {
         get();
      }
      catch (java.util.concurrent.ExecutionException e)
      {
         Throwable cause = e.getCause();

         if (cause != null)
         {
            getMessageSystem().error(cause);
         }
         else
         {
            getMessageSystem().error(e);
         }
      }
      catch (Exception e)
      {
         getMessageSystem().error(e);
      }

      getMessageSystem().finished(jdrFrame);
   }

   public void process(MessageInfo... chunks)
   {
      getMessageSystem().publishMessages(chunks);
   }

   public void publishMessages(MessageInfo... chunks)
   {
      getMessageSystem().publishMessages(chunks);
   }

   public JDRGuiMessage getMessageSystem()
   {
      return jdrFrame.getApplication().getMessageSystem();
   }

   public JDRResources getResources()
   {
      return jdrFrame.getResources();
   }

   private JDRFrame jdrFrame;
   private ImportSettings importSettings;
}
