// File          : JDRSymmetricPathListener.java
// Creation Date : 25th July 2010
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
 * Loader listener for symmetric paths. This is for JDR/AJR versions
 * 1.6 and above. If the version number is less than 1.6, saved as
 * a group of paths.
 * @author Nicola L C Talbot
 */

public class JDRSymmetricPathListener extends JDRPathListener
{
   public char getId(float version)
   {
      return 'S';
   }

   public JDRObject getObject(JDRAJR jdr, JDRObject object, float version)
   {
      JDRSymmetricPath path = (JDRSymmetricPath)object;

      if (version < 1.6f)
      {
         try
         {
            return path.separate();
         }
         catch (InvalidPathException e)
         {
            return new JDRGroup(jdr.getCanvasGraphics());
         }
      }
      else
      {
         return object;
      }
   }

   public void write(JDRAJR jdr, JDRObject object)
      throws IOException
   {
      float version = jdr.getVersion();

      if (version < 1.6f)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.UNSUPPORTED_VERSION,
            object.getClass().getName()+" ("+version+")", jdr);
      }

      JDRSymmetricPath path = (JDRSymmetricPath)object;

      JDRObjectLoader objectLoader = jdr.getObjectLoader();

      objectLoader.save(jdr, path.getUnderlyingShape());

      jdr.writeBoolean(path.isAnchored());

      JDRSegmentLoader loader = jdr.getSegmentLoader();

      if (!path.isAnchored())
      {
         // write join segment

         loader.save(jdr, path.getJoin());
      }

      JDRLine line = path.getSymmetry();

      JDRPoint startPt = line.getStart();
      JDRPoint endPt = line.getEnd();

      jdr.writeDouble(startPt.getX());
      jdr.writeDouble(startPt.getY());
      jdr.writeDouble(endPt.getX());
      jdr.writeDouble(endPt.getY());

      jdr.writeBoolean(path.isClosed());

      if (path.isClosed())
      {
         JDRPartialSegment segment = path.getClosingSegment();

         jdr.writeBoolean(segment == null);

         if (segment != null)
         {
            loader.save(jdr, segment);
         }
      }
   }

   public JDRObject read(JDRAJR jdr)
      throws InvalidFormatException
   {
      JDRObjectLoader objectLoader = jdr.getObjectLoader();

      JDRObject object = objectLoader.load(jdr);

      if (!(object instanceof JDRShape))
      {
         throw new InvalidValueException(
           InvalidFormatException.SYMMETRIC_SHAPE,
           object.getClass().getName(), jdr);
      }

      JDRSymmetricPath sPath = JDRSymmetricPath.createFrom((JDRShape)object);

      boolean anchored = jdr.readBoolean(
         InvalidFormatException.SYMMETRIC_ANCHORED);

      JDRPartialSegment joinSegment = null;

      JDRSegmentLoader loader = jdr.getSegmentLoader();

      if (!anchored)
      {
         // read join information

         joinSegment = (JDRPartialSegment)loader.load(jdr);
      }

      double x0 = jdr.readDouble(
         InvalidFormatException.SYMMETRIC_LINE_X0);
      double y0 = jdr.readDouble(
         InvalidFormatException.SYMMETRIC_LINE_Y0);
      double x1 = jdr.readDouble(
         InvalidFormatException.SYMMETRIC_LINE_X1);
      double y1 = jdr.readDouble(
         InvalidFormatException.SYMMETRIC_LINE_Y1);

      sPath.setSymmetry(x0, y0, x1, y1);
      sPath.setJoin(joinSegment);

      boolean isClosed = jdr.readBoolean(
         InvalidFormatException.SYMMETRIC_CLOSED);

      if (isClosed)
      {
         boolean isClosedAnchored = jdr.readBoolean(
            InvalidFormatException.SYMMETRIC_CLOSE_ANCHORED);

         if (!isClosedAnchored)
         {
            JDRPartialSegment segment 
               = (JDRPartialSegment)loader.load(jdr);

            sPath.close(segment);
         }
         else
         {
            sPath.close();
         }
      }

      return sPath;
   }

}
