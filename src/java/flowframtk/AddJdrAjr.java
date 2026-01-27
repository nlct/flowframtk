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

import java.util.List;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.beans.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

public abstract class AddJdrAjr extends IOSwingWorker
{
   protected AddJdrAjr(JDRFrame frame, File file, String undoName)
   {
      super(frame, file, true);
      this.undoName = undoName;
   }

   protected abstract JDRAJR openInputStream()
      throws IOException;

   protected abstract JDRGroup loadImage(JDRAJR jdr, CanvasGraphics cg)
      throws InvalidFormatException,IOException;

   protected abstract void closeInputStream() throws IOException;

   @Override
   protected JDRGroup doInBackground()
    throws InvalidFormatException,IOException,InterruptedException
   {
      JDRGroup image=null;

      FlowframTk app = jdrFrame.getApplication();

      JDRAJR jdr = null;

      CanvasGraphics cg = 
        (CanvasGraphics)jdrFrame.getCanvasGraphics().clone();

      try
      {
         jdr = openInputStream();

         jdr.setBaseDir(file.getParentFile());

         cg.setUseSettingsOnLoad(app.useJDRsettings());

         image = loadImage(jdr, cg);

         if (jdrFrame.warnOnOldJdr())
         {
            float versionNum = jdr.getVersion();
            int versionId = jdr.getVersionId();

            String verStr;

            if (versionId >= 0 && versionId < JDRAJR.VALID_VERSIONS_STRING.length)
            {
               verStr = JDRAJR.VALID_VERSIONS_STRING[versionId];
            }
            else
            {
               verStr = ""+versionNum;
            }

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

   @Override
   protected void finish(JDRGroup image)
   {
      super.finish(image);

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

         jdrFrame.getCanvas().copySelection(ce, image);

         ce.end();
         if (ce.canUndo()) jdrFrame.postEdit(ce);
      }

      jdrFrame.getApplication().setStatusInfo(
        getResources().getMessage("info.select"), "sec:selectobjects");

   }

   private String undoName;
}
