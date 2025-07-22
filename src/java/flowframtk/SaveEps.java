// File          : SaveEps.java
// Description   : Export to EPS.
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

import java.io.*;
import java.awt.*;
import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdrresources.*;

public class SaveEps extends ExportDocImage
{
   public SaveEps(JDRFrame frame, File file, JDRGroup jdrImage,
     String latexApp, String dvipsApp)
   {
      super(frame, file, jdrImage, true, true);
      this.latexApp = latexApp;
      this.dvipsApp = dvipsApp;
   }

   protected File processImage(String texBase)
      throws IOException,InterruptedException
   {
      File dir = getTeXFile().getParentFile();

      File dviFile = new File(dir, texBase+".dvi");
      dviFile.deleteOnExit();

      exec(new String[] {latexApp, "-interaction", "batchmode", texBase});

      File epsFile = new File(dir, outputFile.getName());

      exec(new String[] {dvipsApp, "-o", epsFile.getName(), dviFile.getName()});

      return epsFile;
   }

   protected File getTeXFile() throws IOException
   {
      File texFile = File.createTempFile("jdr2eps", ".tex");
      texFile.deleteOnExit();

      return texFile;
   }

   private String latexApp, dvipsApp;
}
