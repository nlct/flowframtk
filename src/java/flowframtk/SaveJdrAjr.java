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

import java.util.List;
import java.io.*;
import java.beans.*;
import java.awt.*;
import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdrresources.*;

public abstract class SaveJdrAjr extends IOSwingWorker
{
   protected SaveJdrAjr(JDRFrame frame, File file, float version,
      JDRGroup image, boolean exitAfter)
   {
      this(frame, file, version, image, JDRAJR.ALL_SETTINGS, exitAfter);
   }

   protected SaveJdrAjr(JDRFrame frame, File file, float version,
      JDRGroup image, int settingsFlag, boolean exitAfter)
   {
      super(frame, file, false);

      if (image == null)
      {
         throw new NullPointerException();
      }

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

   @Override
   public JDRGroup doInBackground() throws IOException,InterruptedException
   {
      boolean success = false;

      FlowframTk app = jdrFrame.getApplication();

      try
      {
         JDRAJR jdr = openOutputStream(file);

         jdr.setBaseDir(app.useRelativeBitmaps() ? 
            file.getParentFile() : null);

         jdrFrame.preSave();
         saveImage(jdr, jdrImage, jdrVersion, settingsFlag);

         success = true;
      }
      finally
      {
         closeInputStream();
      }

      return success ? jdrImage : null;
   }

   @Override
   protected void finish(JDRGroup image)
   {
      super.finish(image);

      if (image != null)
      {
         jdrFrame.setFile(file);
         jdrFrame.markAsSaved();
         jdrFrame.addRecentFile(file);

         jdrFrame.getApplication().setTool(jdrFrame.currentTool());
         jdrFrame.getCanvas().updateGeneralActions(true);

         if (exitAfter)
         {
            System.exit(0);
         }
      }
   }

   private float jdrVersion;
   private JDRGroup jdrImage;
   private int settingsFlag;
   private boolean exitAfter;
}
