// File          : EPSGetInterval.java
// Purpose       : class representing getinterval operator
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
 * Object representing getinterval operator.
 * @author Nicola L C Talbot
 */
public class EPSGetInterval extends EPSOperator
{
   public EPSGetInterval()
   {
      super("getinterval");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      int count = stack.popInteger();
      int index = stack.popInteger();
      EPSIndexComposite object = stack.popEPSIndexComposite();

      try
      {
         stack.add(object.getInterval(index, count));
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         throw new InvalidFormatException(
            "(getinterval) index out of bounds", eps.getLineNum());
      }
      catch (NoReadAccessException e)
      {
         throw new NoReadAccessException(
            "(getinterval) no read access", eps.getLineNum());
      }

   }

}
