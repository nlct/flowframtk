// File          : JDRRotationalPatternListener.java
// Creation Date : 9th Sept 2010
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
 * Loader listener for rotational pattern paths. This is for JDR/AJR versions
 * 1.6 and above. If the version number is less than 1.6, saved as
 * a group of paths.
 * @author Nicola L C Talbot
 */

public class JDRRotationalPatternListener extends JDRPathListener
{
   public char getId(float version)
   {
      return 'R';
   }

   public JDRObject getObject(JDRAJR jdr, JDRObject object, float version)
   {
      if (version < 1.6f)
      {
         JDRRotationalPattern path = (JDRRotationalPattern)object;

         return path.separate();
      }

      return object;
   }

   public void write(JDRAJR jdr, JDRObject object)
      throws IOException
   {
      float version = jdr.getVersion();

      JDRRotationalPattern path = (JDRRotationalPattern)object;

      if (version < 1.6f)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.UNSUPPORTED_VERSION,
            path.getClass().getName()+" ("+version+")", jdr);
      }

      JDRObjectLoader objectLoader = jdr.getObjectLoader();

      objectLoader.save(jdr, path.getUnderlyingShape());

      JDRPoint p = path.getPatternAnchor();

      if (version < 1.8f)
      {
         double factor = jdr.getCanvasGraphics().storageToBp(1.0);

         jdr.writeDouble(factor*p.x);
         jdr.writeDouble(factor*p.y);

         jdr.writeDouble(path.getRotationAngle().toRadians());
      }
      else
      {
         jdr.writeDouble(p.x);
         jdr.writeDouble(p.y);

         jdr.writeAngle(path.getRotationAngle());
      }

      jdr.writeInt(path.getNumReplicas());
      jdr.writeBoolean(path.isSinglePath());
      jdr.writeBoolean(path.showOriginal());
   }

   public JDRObject read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      JDRObjectLoader objectLoader = jdr.getObjectLoader();

      JDRObject object = objectLoader.load(jdr);

      if (!(object instanceof JDRShape))
      {
         throw new InvalidValueException(
            InvalidFormatException.ROTATIONAL_SHAPE,
            object.getClass().getName(), jdr);
      }

      double x = jdr.readDouble(
         InvalidFormatException.ROTATIONAL_ANCHOR_X);
      double y = jdr.readDouble(
         InvalidFormatException.ROTATIONAL_ANCHOR_Y);

      CanvasGraphics cg = jdr.getCanvasGraphics();

      JDRAngle angle;

      if (version < 1.8f)
      {
         angle = new JDRAngle(cg,
          jdr.readDouble(InvalidFormatException.ROTATIONAL_ANGLE),
          JDRAngle.RADIAN);
      }
      else
      {
         angle = jdr.readAngle(InvalidFormatException.ROTATIONAL_ANGLE);
      }

      int replicas = jdr.readInt(InvalidFormatException.ROTATIONAL_REPLICAS);
      boolean isSinglePath = jdr.readBoolean(
         InvalidFormatException.ROTATIONAL_SINGLE);
      boolean showOriginal = jdr.readBoolean(
         InvalidFormatException.ROTATIONAL_SHOW_ORIGINAL);

      return new JDRRotationalPattern(cg, (JDRShape)object,
         new JDRPatternAnchorPoint(cg, x,y),
         angle, replicas, isSinglePath, showOriginal);
   }

}
