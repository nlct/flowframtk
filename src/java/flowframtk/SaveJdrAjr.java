// File          : SaveJdrAjr.java
// Description   : Save image.
// Creation Date : 2014-03-31
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2014-2025 Nicola L.C. Talbot

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
import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdrresources.*;

public abstract class SaveJdrAjr extends SwingWorker<Void,MessageInfo>
   implements MessageInfoPublisher
{
   public SaveJdrAjr(JDRFrame frame, File file, float version,
      JDRGroup image, boolean exitAfter)
   {
      this(frame, file, version, image, JDRAJR.ALL_SETTINGS, exitAfter);
   }

   public SaveJdrAjr(JDRFrame frame, File file, float version,
      JDRGroup image, int settingsFlag, boolean exitAfter)
   {
      if (file == null)
      {
         throw new NullPointerException();
      }

      if (frame == null)
      {
         throw new NullPointerException();
      }

      if (image == null)
      {
         throw new NullPointerException();
      }

      this.jdrFrame = frame;
      this.file = file;
      this.jdrVersion = version;
      this.jdrImage = image;
      this.settingsFlag = settingsFlag;
      this.exitAfter = exitAfter;
   }

   protected abstract JDRAJR openOutputStream(File file)
      throws IOException;

   protected abstract void saveImage(JDRAJR jdr, JDRGroup image, float version, int settingsFlag)
      throws IOException;

   protected abstract void closeInputStream() throws IOException;

   public Void doInBackground() throws IOException,InterruptedException
   {
      jdrFrame.setIoInProgress(true);
      getResources().getMessageSystem().setPublisher(this);

      boolean success = false;

      Cursor oldCursor = jdrFrame.getCursor();
      jdrFrame.setCursor(
         Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      FlowframTk app = jdrFrame.getApplication();

      String msg = getResources().getMessage("info.saving", 
         file.toString());

      app.showMessageFrame(msg);
      app.setStatusInfo(msg);

      try
      {
         JDRAJR jdr = openOutputStream(file);

         jdr.setBaseDir(app.useRelativeBitmaps() ? 
            file.getParentFile() : null);

         jdrFrame.preSave();
         saveImage(jdr, jdrImage, jdrVersion, settingsFlag);

         publish(MessageInfo.createMessage(
            getResources().getMessage("message.done")));

         success = true;
      }
      finally
      {
         jdrFrame.setCursor(oldCursor);

         if (success)
         {
            jdrFrame.setFile(file);
            jdrFrame.markAsSaved();

            jdrFrame.addRecentFile(file);
         }

         app.setTool(jdrFrame.currentTool());
         jdrFrame.setIoInProgress(false);

         jdrFrame.getCanvas().updateGeneralActions(true);

         closeInputStream();
      }

      return null;
   }

   public JDRResources getResources()
   {
      return jdrFrame.getResources();
   }

   public void done()
   {
      try
      {
         get();

         if (exitAfter)
         {
            System.exit(0);
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
   private float jdrVersion;
   private JDRGroup jdrImage;
   private JDRFrame jdrFrame;
   private int settingsFlag;
   private boolean exitAfter;
}
