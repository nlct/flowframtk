// File          : JDRScaledPatternListener.java
// Creation Date : 9th April 2011
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
 * Loader listener for scaled pattern paths. This is for JDR/AJR versions
 * 1.6 and above. If the version number is less than 1.6, saved as
 * a group of paths.
 * @author Nicola L C Talbot
 */

public class JDRScaledPatternListener extends JDRPathListener
{
   public char getId(float version)
   {
      return 'C';
   }

   public JDRObject getObject(JDRAJR jdr, JDRObject object, float version)
   {
      if (version < 1.6f)
      {
         JDRScaledPattern path = (JDRScaledPattern)object;

         return path.separate();
      }

      return object;
   }

   public void write(JDRAJR jdr, JDRObject object)
      throws IOException
   {
      float version = jdr.getVersion();

      JDRScaledPattern path = (JDRScaledPattern)object;

      if (version < 1.6f)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.UNSUPPORTED_VERSION,
            path.getClass().getName()+" ("+version+")", jdr);
      }

      JDRObjectLoader objectLoader = jdr.getObjectLoader();

      objectLoader.save(jdr, path.getUnderlyingShape());

      double factor = (version < 1.8f ?
                       jdr.getCanvasGraphics().storageToBp(1.0) : 1.0);

      JDRPoint p = path.getPatternAnchor();

      jdr.writeDouble(factor*p.x);
      jdr.writeDouble(factor*p.y);

      JDRPoint adjust = path.getPatternAdjust();

      if (adjust == null)
      {
         adjust = p;
      }

      jdr.writeDouble(factor*adjust.x);
      jdr.writeDouble(factor*adjust.y);

      jdr.writeDouble(path.getScaleX());
      jdr.writeDouble(path.getScaleY());
      jdr.writeInt(path.getNumReplicas());
      jdr.writeBoolean(path.isSinglePath());
      jdr.writeBoolean(path.showOriginal());
   }

   public JDRObject read(JDRAJR jdr)
      throws InvalidFormatException
   {
      JDRObjectLoader objectLoader = jdr.getObjectLoader();

      JDRObject object = objectLoader.load(jdr);

      if (!(object instanceof JDRShape))
      {
         throw new InvalidValueException(
           InvalidFormatException.SCALED_SHAPE,
           object.getClass().getName(), jdr);
      }

      double x = jdr.readDouble(
         InvalidFormatException.SCALED_ANCHOR_X);
      double y = jdr.readDouble(
         InvalidFormatException.SCALED_ANCHOR_Y);

      double adjustX = jdr.readDouble(
         InvalidFormatException.SCALED_ADJUST_X);
      double adjustY = jdr.readDouble(
         InvalidFormatException.SCALED_ADJUST_Y);

      double scaleX = jdr.readDouble(
         InvalidFormatException.SCALED_FACTOR_X);
      double scaleY = jdr.readDouble(
         InvalidFormatException.SCALED_FACTOR_Y);

      int replicas = jdr.readInt(
         InvalidFormatException.SCALED_REPLICAS);

      boolean isSinglePath = jdr.readBoolean(
         InvalidFormatException.SCALED_SINGLE);
      boolean showOriginal = jdr.readBoolean(
         InvalidFormatException.SCALED_SHOW_ORIGINAL);

      CanvasGraphics cg = jdr.getCanvasGraphics();

      return new JDRScaledPattern(cg, (JDRShape)object,
         new JDRPatternAnchorPoint(cg, x,y),
         new JDRPatternAdjustPoint(cg, adjustX, adjustY),
         scaleX, scaleY, replicas, isSinglePath, showOriginal);
   }

}
