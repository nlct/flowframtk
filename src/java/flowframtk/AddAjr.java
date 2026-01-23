// File          : AddAjr.java
// Description   : Load image.
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

import java.io.*;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.awt.*;
import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

public class AddAjr extends AddJdrAjr
{
   private BufferedReader in = null;
   Charset encoding = StandardCharsets.UTF_8;

   private AddAjr(JDRFrame frame, File file, String undoName)
   {
      super(frame, file, undoName);
      in = null;
   }

   public static void createAndRun(JDRFrame frame, File file, String undoName)
   {
      AddAjr worker = new AddAjr(frame, file, undoName);
      worker.initialise();
      worker.execute();
   }

   @Override
   protected JDRAJR openInputStream()
    throws IOException
   {
      in = Files.newBufferedReader(file.toPath(), encoding);

      return new AJR();
   }

   @Override
   protected void closeInputStream()
    throws IOException
   {
      if (in != null)
      {
         in.close();
      }
   }

   @Override
   protected JDRGroup loadImage(JDRAJR ajr, CanvasGraphics cg)
      throws IOException,InvalidFormatException
   {
      try
      {
         return ((AJR)ajr).load(in, encoding, cg);
      }
      catch (MismatchedEncodingException e)
      {
         encoding = e.getFound();
         closeInputStream();

         in = Files.newBufferedReader(file.toPath(), encoding);
         return ((AJR)ajr).load(in, encoding, cg);
      }
   }

}
