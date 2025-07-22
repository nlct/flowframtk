// File          : AddJdrAjr.java
// Description   : Runnable to load image in thread and add to
//                 current canvas.
// Creation Date : 2015-10-17
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
import java.awt.*;
import javax.swing.*;
import java.beans.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

public abstract class AddJdrAjr extends SwingWorker<JDRGroup,MessageInfo>
  implements MessageInfoPublisher
{
   private AddJdrAjr()
   {
   }

   public AddJdrAjr(JDRFrame frame, File file, String undoName)
   {
      this.jdrFrame = frame;
      this.file = file;
      this.undoName = undoName;

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
      getMessageSystem().setPublisher(this);

      JDRGroup image=null;

      FlowframTk app = jdrFrame.getApplication();

      String msg = getResources().getMessage("info.loading", 
         file.toString());

      app.showMessageFrame(msg);

      JDRAJR jdr = null;

      CanvasGraphics cg = 
        (CanvasGraphics)jdrFrame.getCanvasGraphics().clone();

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
                  getResources().getMessage("warning.load.jdr",
                     versionNum)));
            }
         }

         if (jdr.hasDraftBitmap())
         {
            publish(MessageInfo.createWarning(
               getResources().getMessage("warning.draft_bitmaps")));
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
              MessageInfo.createError(getResources().getMessage("error.io.close")), 
              MessageInfo.createError(e));
         }

      }

      return image;
   }

   protected void done()
   {
      try
      {
         JDRGroup image = get();

         if (image != null)
         {
            if (image.getCanvasGraphics().isBitmapReplaced())
            {
               getResources().debugMessage("bitmap(s) replaced");
               jdrFrame.markAsModified();
               image.getCanvasGraphics().setBitmapReplaced(false);
            }

            JDRCanvasCompoundEdit ce = new JDRCanvasCompoundEdit(
              jdrFrame.getCanvas(), undoName);

            jdrFrame.getCanvas().copySelection(ce, image, false);

            ce.end();
            if (ce.canUndo()) jdrFrame.postEdit(ce);
         }
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
      jdrFrame.getApplication().setStatusInfo(
        getResources().getMessage("info.select"), "selectobjects");
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
   private String undoName;
}
