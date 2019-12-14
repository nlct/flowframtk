// File          : LoadEps.java
// Description   : Import EPS file.
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
public class LoadEps extends SwingWorker<Void,MessageInfo>
  implements MessageInfoPublisher
{
   public LoadEps(JDRFrame frame, File file)
   {
      jdrFrame = frame;
      epsFile = file;
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

      String fileName = epsFile.getAbsolutePath();

      app.showMessageFrame(getResources().getStringWithValue("info.loading", fileName));

      Cursor oldCursor = jdrFrame.getCursor();
      jdrFrame.setCursor(
         Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      BufferedReader in = null;

      try
      {
         in = new BufferedReader(new FileReader(epsFile));

         int index = fileName.lastIndexOf(".");

         if (index != -1)
         {
            fileName = fileName.substring(0, index);
         }

         image = EPS.load(cg, in, fileName+"Img");

         if (image.anyDraftBitmaps())
         {
            publish(MessageInfo.createWarning(
               getResources().getString("warning.draft_bitmaps")));
         }
      }
      finally
      {
         jdrFrame.setCursor(oldCursor);
         app.setTool(jdrFrame.currentTool());
         jdrFrame.setIoInProgress(false);

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
            MessageInfo.createError(getResources().getString("error.io.close")),
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

   private File epsFile;
   private JDRFrame jdrFrame;
}
