// File          : EPSPathForAll.java
// Purpose       : class representing pathforall operator
// Date          : 1st June 2008
// Last Modified : 1st June 2008
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

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
package com.dickimawbooks.jdr.io.eps.operators;

import java.io.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.eps.*;
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Object representing pathforall operator.
 * @author Nicola L C Talbot
 */
public class EPSPathForAll extends EPSOperator
{
   public EPSPathForAll()
   {
      super("pathforall");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      EPSProc closeProc = stack.popEPSProc();
      EPSProc curveProc = stack.popEPSProc();
      EPSProc lineProc = stack.popEPSProc();
      EPSProc moveProc = stack.popEPSProc();

      PathIterator pi 
         = eps.getCurrentGraphicsState().getPathIterator(null);

      float[] coords = new float[6];

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_MOVETO:
               stack.pushDouble(coords[0]); // x
               stack.pushDouble(coords[1]); // y
               stack.execProc(moveProc);
            break;
            case PathIterator.SEG_LINETO:
               stack.pushDouble(coords[0]); // x
               stack.pushDouble(coords[1]); // y
               stack.execProc(lineProc);
            break;
            case PathIterator.SEG_CUBICTO:
               for (int i = 0; i < 6; i++)
               {
                  stack.pushDouble(coords[i]);
               }
               stack.execProc(curveProc);
            break;
            case PathIterator.SEG_CLOSE:
               stack.execProc(closeProc);
            break;
         }
         pi.next();
      }
   }

}
