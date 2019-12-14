// File          : JDRTextPathListener.java
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
 * Loader listener for text paths. This is for JDR/AJR versions
 * 1.5 and above. If the version number is less than 1.5, only the
 * path information is save.
 * @author Nicola L C Talbot
 */

public class JDRTextPathListener extends JDRPathListener
{
   public char getId(float version)
   {
      return 'X';
   }

   public JDRObject getObject(JDRAJR jdr, JDRObject object, float version)
   {
      JDRTextPath path = (JDRTextPath)object;

      if (version < 1.5f)
      {
         return path.separate();
      }

      return object;
   }

   public void write(JDRAJR jdr, JDRObject object)
      throws IOException
   {
      float version = jdr.getVersion();

      if (version < 1.5f)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.UNSUPPORTED_VERSION,
            object.getClass().getName()+" ("+version+")", jdr);
      }

      JDRTextPath path = (JDRTextPath)object;

      if (version >= 1.8f)
      {
         jdr.writeBoolean(path.isOutline());

         if (path.isOutline())
         {
            JDRPaint paint = path.getFillPaint();
            JDRPaintLoader paintLoader = jdr.getPaintLoader();
            paintLoader.save(jdr, (paint==null? 
              new JDRTransparent(path.getCanvasGraphics()) :
              paint));
         }
      }

      if (version < 1.6f)
      {
         JDRPaintLoader paintLoader = jdr.getPaintLoader();
         paintLoader.save(jdr, path.getTextPaint());
         path.getStroke().save(jdr);

         writePathSpecs(jdr, path);
      }
      else
      {
         JDRObjectLoader loader = jdr.getObjectLoader();

         loader.save(jdr, path.getUnderlyingShape());
      }
   }

   public JDRObject read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();
      CanvasGraphics cg = jdr.getCanvasGraphics();

      JDRShape shape;

      boolean outline = false;
      JDRPaint fillPaint = null;

      if (version >= 1.8f)
      {
         outline = jdr.readBoolean(InvalidFormatException.TEXT_OUTLINE_FLAG);

         if (outline)
         {
            JDRPaintLoader paintLoader = jdr.getPaintLoader();
            fillPaint = paintLoader.load(jdr);
         }
      }

      if (version < 1.5f)
      {
         throw new InvalidValueException(
          InvalidFormatException.TEXT_PATH_VERSION_UNSUPPORTED,
          version, jdr);
      }
      else if (version < 1.6f)
      {
         shape = new JDRPath(cg);

         JDRPaintLoader paintLoader = jdr.getPaintLoader();

         shape.setLinePaint(paintLoader.load(jdr));
         shape.setFillPaint(new JDRTransparent(cg));
         shape.setStroke(JDRTextPathStroke.read(jdr));

         readPathSpecs(jdr, shape);
      }
      else
      {
         JDRObjectLoader loader = jdr.getObjectLoader();
         JDRObject object = loader.load(jdr);

         if (!(object instanceof JDRShape))
         {
            throw new InvalidValueException(
                InvalidFormatException.TEXT_PATH_SHAPE,
                object.getClass().getName(), jdr);
         }

         shape = (JDRShape)object;
      }

      JDRTextPath textpath = JDRTextPath.createFrom(shape);

      textpath.setOutlineMode(outline);

      if (fillPaint != null)
      {
         textpath.setFillPaint(fillPaint);
      }

      return textpath;
   }

}
