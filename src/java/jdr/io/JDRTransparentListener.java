// File          : JDRTransparentListener.java
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
 * Loader listener for transparent paint.
 * @author Nicola L C Talbot
 */

public class JDRTransparentListener implements JDRPaintLoaderListener
{
   public char getId(float version)
   {
      return 'T';
   }

   public JDRPaint getPaint(JDRAJR jdr, JDRPaint paint, float version)
   {
      return paint;
   }

   public void write(JDRAJR jdr, JDRPaint paint) throws IOException
   {
   }

   public JDRPaint read(JDRAJR jdr)
      throws InvalidFormatException
   {
      return new JDRTransparent(jdr.getCanvasGraphics());
   }

   public int getConfigId()
   {
      return 0;
   }

   public String getConfigString(JDRPaint paint)
   {
      return "";
   }

   public JDRPaint parseConfig(CanvasGraphics cg, String paintSpecs)
      throws InvalidFormatException
   {
      remainder = paintSpecs;

      return new JDRTransparent(cg);
   }

   /**
    * Gets the remainder of the specs String after it has been
    * parsed by {@link #parseConfig(CanvasGraphics,String)}.
    */
   public String getConfigRemainder()
   {
      return remainder;
   }

   private String remainder="";
}
