// File          : SaveImage.java
// Description   : Export image.
// Creation Date : 2015-10-01
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
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

public abstract class ExportImage extends SwingWorker<Void,MessageInfo>
 implements MessageInfoPublisher
{
   public ExportImage(JDRFrame frame, File file, JDRGroup jdrImage)
   {
      super();
      this.jdrFrame = frame;
      this.outputFile = file;
      this.image = jdrImage;
   }

   public Void doInBackground() 
     throws InvalidFormatException,IOException,InterruptedException
   {
      FlowframTk app = jdrFrame.getApplication();
      JDRResources resources = app.getResources();

      jdrFrame.setIoInProgress(true);
      resources.getMessageSystem().setPublisher(this);

      Cursor oldCursor = jdrFrame.getCursor();
      jdrFrame.setCursor(
         Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      String msg = resources.getMessage("info.saving",
         outputFile.toString());

      app.showMessageFrame(msg);
      app.setStatusInfo(msg);

      try
      {
         jdrFrame.preSave();

         save();

         publish(
            MessageInfo.createMessage(resources.getString("message.done")));
      }
      catch (UserCancelledException e)
      {
         publish(MessageInfo.createMessage(e.getMessage()));
      }
      finally
      {
         jdrFrame.setCursor(oldCursor);
         app.setTool(jdrFrame.currentTool());

         publish(MessageInfo.createSetVisible(false));
         jdrFrame.setIoInProgress(false);
      }

      return null;
   }

   public FlowframTkInvoker getInvoker()
   {
      return jdrFrame.getApplication().getInvoker();
   }

   public JDRResources getResources()
   {
      return jdrFrame.getResources();
   }

   public FlowframTkSettings getApplicationSettings()
   {
      return jdrFrame.getApplication().getSettings();
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

   protected abstract void save() 
     throws IOException,InterruptedException,InvalidFormatException;

   protected File outputFile;
   protected JDRGroup image;
   protected JDRFrame jdrFrame;
}
