// File          : SavePgfDoc.java
// Description   : Export image as a LaTeX Document.
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

public class SavePgfDoc extends ExportDocImage
{
   public SavePgfDoc(JDRFrame frame, File file, JDRGroup jdrImage,
     boolean encapsulate)
   {
      super(frame, file, jdrImage, encapsulate, false);
   }

   public File getTeXFile()
   {
      return outputFile;
   }

   protected File processImage(String texBase)
      throws IOException,InterruptedException
   {
      return null;
   }
}
