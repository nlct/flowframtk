// File          : JDRBitmapListener.java
// Creation Date : 29th February 2008
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

package com.dickimawbooks.jdr.io;

import java.io.*;
import java.util.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Loader listener for bitmaps.
 * @author Nicola L C Talbot
 */

public class JDRBitmapListener implements JDRObjectLoaderListener
{
   public char getId(float version)
   {
      return 'I';
   }

   public JDRObject getObject(JDRAJR jdr, JDRObject object, float version)
   {
      return object;
   }

   public void write(JDRAJR jdr, JDRObject object)
     throws IOException
   {
      float version = jdr.getVersion();

      JDRBitmap bitmap = (JDRBitmap)object;

      // filename

      if (version < 1.8f)
      {
         jdr.writeString(bitmap.getFilename());
      }
      else
      {
         jdr.writeString(jdr.relativizePath(bitmap.getFile().toPath()).toString());
      }

      // LaTeX flag

      jdr.writeBoolean(true);

      // LaTeX link to file

      jdr.writeString(bitmap.getLaTeXLinkName());

      // LaTeX image command

      jdr.writeString(bitmap.getLaTeXCommand());

      // affine transformation matrix
      double[] flatmatrix = new double[6];
      bitmap.getTransformation(flatmatrix);

      jdr.writeTransform(flatmatrix);
   }

   public JDRObject read(JDRAJR jdr)
     throws InvalidFormatException
   {
      float version = jdr.getVersion();

      String filename = jdr.readString(
            InvalidFormatException.BITMAP_FILENAME);

      if (version >= 1.8f)
      {
         filename = (jdr.resolveFile(filename)).getPath();
      }

      String latexfilename = null;
      String imageCmd = "\\pgfimage";

      if (jdr.readBoolean(InvalidFormatException.BITMAP_LATEX_FLAG))
      {
         latexfilename = jdr.readString(
            InvalidFormatException.LATEX_FILENAME);

         imageCmd = jdr.readString(
            InvalidFormatException.LATEX_IMAGECMD);
      }

      // affine transformation matrix
      double[] matrix = jdr.readTransform(
         InvalidFormatException.BITMAP_TRANSFORM);

      // check filename exists

      String oldFilename = filename;

      filename = JDRBitmap.checkFilename(jdr, filename);

      if (filename == null)
      {
         return null;
      }

      if (!oldFilename.equals(filename) && latexfilename != null)
      {
         latexfilename = JDRBitmap.getLaTeXPath(filename);
      }

      JDRBitmap bitmap;

      try
      {
         if (latexfilename==null)
         {
            bitmap = new JDRBitmap(jdr.getCanvasGraphics(), filename);
         }
         else
         {
            bitmap = new JDRBitmap(jdr.getCanvasGraphics(), filename,
               latexfilename);
         }
      }
      catch (FileNotFoundException e)
      {
         // This shouldn't happen as we've already checked for the
         // existence of the file.

         throw new IllegalArgumentException(
            "Image file has unexpectedly ceased to exist.\n"+filename, e);
      }

      bitmap.transform(matrix);
      bitmap.setLaTeXCommand(imageCmd);

      if (bitmap.isDraft())
      {
         jdr.setDraftBitmap(true);
      }

      return bitmap;
   }

}
