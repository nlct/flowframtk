// File          : LoadJdr.java
// Description   : Runnable to load image in thread.
// Creation Date : 2014-03-30
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
import java.beans.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

/**
 * Runnable to load image in JDR/AJR format.
 */
public abstract class LoadJdrAjr extends SwingWorker<JDRGroup,MessageInfo>
  implements MessageInfoPublisher
{
   private LoadJdrAjr()
   {
   }

   public LoadJdrAjr(JDRFrame frame, File file)
   {
      this.jdrFrame = frame;
      this.file = file;

      JDRMessage msgSys = frame.getApplication().getMessageSystem();

      if (msgSys instanceof PropertyChangeListener)
      {
         addPropertyChangeListener((PropertyChangeListener)msgSys);
      }
   }

   protected abstract JDRAJR openInputStream(File file)
      throws IOException;

   protected abstract JDRGroup loadImage(JDRAJR jdr, CanvasGraphics cg)
      throws InvalidFormatException,IOException;

   protected abstract void closeInputStream() throws IOException;

   protected JDRGroup doInBackground()
    throws InvalidFormatException,IOException,InterruptedException
   {
      jdrFrame.setIoInProgress(true);

      getMessageSystem().setPublisher(this);

      JDRGroup image=null;

      FlowframTk app = jdrFrame.getApplication();

      String msg = getResources().getStringWithValue("info.loading", 
         file.toString());

      app.showMessageFrame(msg);

      oldCursor = jdrFrame.getCursor();
      jdrFrame.setCursor(
         Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      JDRAJR jdr = null;

      CanvasGraphics cg = jdrFrame.getCanvasGraphics();

      try
      {
         jdr = openInputStream(file);

         jdr.setBaseDir(file.getParentFile());

         cg.setUseSettingsOnLoad(app.useJDRsettings());

         image = loadImage(jdr, cg);

         float versionNum = jdr.getVersion();

         if (jdrFrame.warnOnOldJdr())
         {
            if (versionNum < JDRAJR.CURRENT_VERSION)
            {
               publish(MessageInfo.createWarning(
                  getResources().getStringWithValue("warning.load.jdr",
                     ""+versionNum)));
            }
         }

         if (jdr.hasDraftBitmap())
         {
            publish(MessageInfo.createWarning(
               getResources().getString("warning.draft_bitmaps")));
         }

      }
      finally
      {
         try
         {
            closeInputStream();
         }
         catch (IOException e)
         {
            publish(
              MessageInfo.createError(getResources().getString("error.io.close")), 
              MessageInfo.createError(e));
         }

      }

      return image;
   }

   protected void done()
   {
      try
      {
         jdrFrame.setIoInProgress(false);
         jdrFrame.setCursor(oldCursor);

         JDRGroup image = get();

         if (image != null)
         {
            jdrFrame.setFile(file);
            jdrFrame.addRecentFile(file);

            if (image.getCanvasGraphics().isBitmapReplaced())
            {
               getResources().debugMessage("bitmap(s) replaced");
               jdrFrame.markAsModified();
               image.getCanvasGraphics().setBitmapReplaced(false);
            }

            jdrFrame.setImage(image);
         }

         jdrFrame.getApplication().setTool(jdrFrame.currentTool());
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

   public JDRResources getResources()
   {
      return jdrFrame.getResources();
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

   private File file;
   private JDRFrame jdrFrame;
   private Cursor oldCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
}
