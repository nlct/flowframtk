// File          : ExportImage.java
// Description   : Export image.
// Creation Date : 2015-10-01
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
import java.awt.Cursor;
import java.util.List;
import javax.swing.*;

import com.dickimawbooks.texjavahelplib.*;
import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

public abstract class ExportImage extends IOSwingWorker
{
   protected ExportImage(JDRFrame frame, File file, JDRGroup jdrImage,
     ExportSettings exportSettings)
   {
      super(frame, file, false);
      this.image = jdrImage;
      this.exportSettings = exportSettings;
   }

   @Override
   public JDRGroup doInBackground() 
     throws InvalidFormatException,IOException,InterruptedException,
      MissingProcessorException
   {
      FlowframTk app = jdrFrame.getApplication();
      JDRResources resources = app.getResources();

      try
      {
         jdrFrame.preSave();

         save();

         publish(
            MessageInfo.createMessage(resources.getMessage("message.done")));
      }
      catch (UserCancelledException e)
      {
         publish(MessageInfo.createMessage(e.getMessage()));

         return null;
      }

      return image;
   }

   public FlowframTkInvoker getInvoker()
   {
      return jdrFrame.getApplication().getInvoker();
   }

   public FlowframTkSettings getApplicationSettings()
   {
      return jdrFrame.getApplication().getSettings();
   }

   @Override
   protected void finish(JDRGroup image)
   {
      if (image != null)
      {
         jdrFrame.getApplication().setTool(jdrFrame.currentTool());
      }

      super.finish(image);
   }

   public FlowframTkSettings getSettings()
   {
      return jdrFrame.getApplication().getSettings();
   }

   protected abstract void save() 
     throws IOException,InterruptedException,InvalidFormatException,
     MissingProcessorException;

   protected JDRGroup image;
   protected ExportSettings exportSettings;
}
