/*
    Copyright (C) 2025 Nicola L.C. Talbot

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
import com.dickimawbooks.jdrresources.*;

public class SaveNoProcessEps extends ExportImage
{
   public SaveNoProcessEps(JDRFrame frame, File file, JDRGroup jdrImage,
     ExportSettings exportSettings)
   {
      super(frame, file, jdrImage, exportSettings);
   }

   public void save() throws InterruptedException,IOException
   {
      PrintWriter out = null;

      try
      {
         out = new PrintWriter(Files.newBufferedWriter(outputFile.toPath()));
         EPS.save(image, out, frame.getApplication().getApplicationName());
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }
      }
   }
}
