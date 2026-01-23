// File          : SaveAjr.java
// Description   : Save image.
// Creation Date : 14th July 2008
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2008-2025 Nicola L.C. Talbot

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
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.awt.*;
import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdrresources.*;

public class SaveAjr extends SaveJdrAjr
{
   private SaveAjr(JDRFrame frame, File file, float version, JDRGroup image,
      boolean exitAfter)
   {
      super(frame, file, version, image, JDRAJR.ALL_SETTINGS, exitAfter);
   }

   private SaveAjr(JDRFrame frame, File file, float version, JDRGroup image,
      int settingsFlag, boolean exitAfter)
   {
      super(frame, file, version, image, settingsFlag, exitAfter);
   }

   public static void createAndRun(JDRFrame frame, File file, float version,
      JDRGroup image, boolean exitAfter)
   {
      SaveAjr worker = new SaveAjr(frame, file, version,
         image, exitAfter);
      worker.initialise();
      worker.execute();
   }

   public static void createAndRun(JDRFrame frame, File file, float version,
      JDRGroup image, int settingsFlag, boolean exitAfter)
   {
      SaveAjr worker = new SaveAjr(frame, file, version,
         image, settingsFlag, exitAfter);
      worker.initialise();
      worker.execute();
   }

   @Override
   protected JDRAJR openOutputStream()
      throws IOException
   {
      out = new PrintWriter(Files.newBufferedWriter(file.toPath(), encoding));

      return new AJR();
   }

   @Override
   protected void closeInputStream() throws IOException
   {
      if (out != null)
      {
         out.close();
      }
   }

   @Override
   protected void saveImage(JDRAJR ajr, JDRGroup image, float version, int settingsFlag) throws IOException
   {
      ((AJR)ajr).save(image, out, encoding, version, settingsFlag);
   }

   private PrintWriter out = null;

   Charset encoding = StandardCharsets.UTF_8;
}
