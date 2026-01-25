// File          : JDRPathListener.java
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Loader listener for paths.
 * @author Nicola L C Talbot
 */

public class JDRPathListener implements JDRObjectLoaderListener
{
   public char getId(float version)
   {
      return 'P';
   }

   public JDRObject getObject(JDRAJR jdr, JDRObject object, float version)
   {
      return object;
   }

   public void write(JDRAJR jdr, JDRObject object)
      throws IOException
   {
      float version = jdr.getVersion();

      JDRPath path = (JDRPath)object;

      if (version < 1.6f)
      {
         JDRPaintLoader paintLoader = jdr.getPaintLoader();

         paintLoader.save(jdr, path.getLinePaint());

         paintLoader.save(jdr, path.getShapeFillPaint());

         path.getStroke().save(jdr);
      }
      else
      {
         JDRPathStyleLoader pathStyleLoader = jdr.getPathStyleLoader();

         pathStyleLoader.save(jdr, path);
      }

      writePathSpecs(jdr, path);
   }

   protected void writePathSpecs(JDRAJR jdr, JDRShape path)
      throws IOException
   {
      float version = jdr.getVersion();

      double factor = (version < 1.8f ? 
                       jdr.getCanvasGraphics().storageToBp(1.0) : 1.0);

      jdr.writeChar(path.isClosed() ? 'C' : 'O');

      int n = path.size();
      jdr.writeInt(n);

      JDRSegmentLoader segmentLoader = JDR.getSegmentLoader();

      for (int i = 0; i < n; i++)
      {
         JDRSegment segment = (JDRSegment)path.get(i);

         if (i == 0 && version >= 1.3f)
         {
            jdr.writeDouble(factor*segment.getStartX());
            jdr.writeDouble(factor*segment.getStartY());
         }

         segmentLoader.save(jdr, segment);
      }

      if (version >= 1.7f)
      {
         JDRPointIterator it = new JDRPointIterator(path);

         while (it.hasNext())
         {
            JDRPoint point = it.next();

            if (point.isAnchored())
            {
               jdr.writeInt(it.getCurrentPointIndex());
            }
         }

         jdr.writeInt(-1);
      }
   }

   public JDRObject read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      JDRPath path = new JDRPath(jdr.getCanvasGraphics());

      if (version < 1.6f)
      {
         JDRPaintLoader paintLoader = jdr.getPaintLoader();

         path.setLinePaint(paintLoader.load(jdr));
         path.setShapeFillPaint(paintLoader.load(jdr));
         path.setStroke(JDRBasicStroke.read(jdr));
      }
      else
      {
         JDRPathStyleLoader pathStyleLoader = jdr.getPathStyleLoader();

         pathStyleLoader.load(jdr, path);
      }

      readPathSpecs(jdr, path);

      return path;
   }

   protected void readPathSpecs(JDRAJR jdr, JDRShape path)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      CanvasGraphics cg = jdr.getCanvasGraphics();

      char c = jdr.readChar(InvalidFormatException.PATH_OPEN_CLOSE_ID);
      boolean closedPath;

      if (c == 'O')
      {
         closedPath = false;
      }
      else if (c == 'C')
      {
         closedPath = true;
      }
      else
      {
         throw new InvalidValueException(
           InvalidFormatException.PATH_OPEN_CLOSE_ID, c, jdr);
      }

      int n = jdr.readIntGe(InvalidFormatException.PATH_SIZE, 1);

      if (n > path.getCapacity())
      {
         path.setCapacity(n);
      }

      double x = 0;
      double y = 0;

      if (version >= 1.3f)
      {
         x = jdr.readDouble(InvalidFormatException.PATH_START_X);
         y = jdr.readDouble(InvalidFormatException.PATH_START_X);
      }

      JDRSegmentLoader segmentLoader = jdr.getSegmentLoader();

      for (int i = 0; i < n; i++)
      {
         JDRSegment segment 
            = (JDRSegment)segmentLoader.load(jdr, x, y);

         if (closedPath && i == n-1)
         {
            path.close(segment);
         }
         else
         {
            path.add(segment);
         }

         JDRPoint endPt = segment.getEnd();

         x = endPt.getX();
         y = endPt.getY();
      }

      if (version >= 1.7f)
      {
         if (version == 1.7f)
         {
            try
            {
               jdr.mark(1024);
            }
            catch (IOException e)
            {
               jdr.getMessageSystem().getPublisher().publishMessages(
                  MessageInfo.createWarning(e));
            }
         }

         int index = jdr.readInt(InvalidFormatException.PATH_ANCHOR_INDEX);

         JDRPointIterator it = new JDRPointIterator(path);

         while (index != -1)
         {
            JDRPoint point = null;

            while (it.hasNext())
            {
               point = it.next();

               if (it.getCurrentPointIndex() == index)
               {
                  point.setAnchored(true);
                  break;
               }
            }

            if (point == null)
            {
               if (version == 1.7f)
               {
                  // Kludge to compensate for some version 1.7 files
                  // failing to save the indexes due to an earlier bug
                  // in the initial release of flowframtk.

                  jdr.warningWithFallback("error.possible_malformed_1_7",
                   "Possible malformed v1.7 file. Attempting to read again.");

                  try
                  {
                    jdr.reset();
                    return;
                  }
                  catch (IOException e)
                  {
                     jdr.getMessageSystem().getPublisher().publishMessages(
                        MessageInfo.createWarning(e));
                  }
               }

               throw new InvalidValueException(
                  InvalidFormatException.PATH_ANCHOR_INDEX, index, jdr);
            }

            try
            {
               index = jdr.readInt(InvalidFormatException.PATH_ANCHOR_INDEX);
            }
            catch (InvalidFormatException e)
            {
               if (version == 1.7f)
               {
                  jdr.warningWithFallback("error.possible_malformed_1_7",
                   "Possible malformed v1.7 file. Attempting to read again.");

                  try
                  {
                    jdr.reset();
                    return;
                  }
                  catch (IOException ioe)
                  {
                     jdr.getMessageSystem().getPublisher().publishMessages(
                       MessageInfo.createWarning(ioe));
                  }
               }

               throw e;
            }
         }
      }
   }

}
