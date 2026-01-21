/*
    Copyright (C) 2026 Nicola L.C. Talbot

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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.awt.*;
import javax.swing.*;
import java.beans.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

public abstract class IOSwingWorker extends SwingWorker<JDRGroup,MessageInfo>
{
   protected IOSwingWorker(JDRFrame frame, File file, boolean isInput)
   {
      if (file == null)
      {
         throw new NullPointerException();
      }

      if (frame == null)
      {
         throw new NullPointerException();
      }

      this.jdrFrame = frame;
      this.file = file;
      this.isInput = isInput;
   }

   protected void initialise()
   {
      FlowframTk app = jdrFrame.getApplication();

      JDRMessage msgSys = app.getMessageSystem();

      if (msgSys instanceof PropertyChangeListener)
      {
         addPropertyChangeListener((PropertyChangeListener)msgSys);
      }

      jdrFrame.setIoInProgress(true);

      app.addMessageAndStatus(getResources().getMessage(
        isInput ? "info.loading" : "info.saving", file));
   }

   @Override
   protected void done()
   {
      JDRGroup image = null;

      long millisecs = jdrFrame.getApplication().getSettings().getSwingWorkerTimeout();

      try
      {
         image = get(millisecs, TimeUnit.MILLISECONDS);

         publish(MessageInfo.createMessage(
            getResources().getMessage("message.done")));
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
      catch (Throwable e)
      {
         getMessageSystem().error(e);
      }

      finish(image);
   }

   protected void finish(JDRGroup image)
   {
      jdrFrame.setIoInProgress(false);
   }

   public JDRResources getResources()
   {
      return jdrFrame.getResources();
   }

   @Override
   public void process(List<MessageInfo> chunks)
   {
      int n = chunks.size();

      if (n == 1)
      {
         getMessageSystem().publishMessages(chunks.get(0));
      }
      else if (n > 0)
      {
         getMessageSystem().publishMessages(chunks.toArray(new MessageInfo[n]));
      }
   }

   public JDRGuiMessage getMessageSystem()
   {
      return jdrFrame.getApplication().getMessageSystem();
   }

   protected File file;
   protected JDRFrame jdrFrame;
   protected boolean isInput;
}
