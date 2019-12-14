// File          : JDRBasicPathStyleListener.java
// Creation Date : 29th August 2010
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
 * Basic path style listener.
 * @author Nicola L C Talbot
 * @see JDRPathStyleLoader
 */

public class JDRBasicPathStyleListener implements JDRPathStyleListener
{
   public byte getId(float version)
   {
      return (byte)0;
   }

   public JDRShape getShape(JDRAJR jdr, JDRShape shape, float version)
   {
      return shape;
   }

   public void write(JDRAJR jdr, JDRShape shape)
      throws IOException
   {
      JDRPaintLoader paintLoader = jdr.getPaintLoader();

      paintLoader.save(jdr, shape.getLinePaint());
      paintLoader.save(jdr, shape.getFillPaint());
      shape.getStroke().save(jdr);
   }

   public void read(JDRAJR jdr, JDRShape shape)
      throws InvalidFormatException
   {
      JDRPaintLoader paintLoader = jdr.getPaintLoader();

      shape.setLinePaint(paintLoader.load(jdr));
      shape.setFillPaint(paintLoader.load(jdr));
      shape.setStroke(JDRBasicStroke.read(jdr));
   }
}
