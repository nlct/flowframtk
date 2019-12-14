// File          : LoadJdr.java
// Description   : Runnable to load image in thread.
// Creation Date : 14th July 2008
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

/**
 * Runnable to load image in JDR format.
 */
public class LoadJdr extends LoadJdrAjr
{
   private DataInputStream in = null;

   public LoadJdr(JDRFrame frame, File file)
   {
      super(frame, file);
      in = null;
   }

   protected JDRAJR openInputStream(File file)
     throws IOException
   {
      in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

      return new JDR();
   }

   protected void closeInputStream()
     throws IOException
   {
     if (in != null)
     {
        in.close();
     }
   }

   protected JDRGroup loadImage(JDRAJR jdr, CanvasGraphics cg)
      throws IOException,InvalidFormatException
   {
      return ((JDR)jdr).load(in, cg);
   }

}
