// File          : EPSReadString.java
// Purpose       : class representing readstring operator
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
 * Object representing readstring operator.
 * @author Nicola L C Talbot
 */
public class EPSReadString extends EPSOperator
{
   public EPSReadString()
   {
      super("readstring");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      EPSString str = stack.popEPSString();
      EPSFile file = stack.popEPSFile();

      try
      {
         char[] buffer = new char[str.length()];
         boolean success = file.readstring(buffer);
         EPSString substr = new EPSString(buffer);
         str.putInterval(0, substr);

         stack.add(substr);
         stack.pushBoolean(success);
      }
      catch (NoReadAccessException e)
      {
         stack.add(new EPSString(0));
         stack.pushBoolean(false);
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         throw new ArrayIndexOutOfBoundsException(
            "readstring overflow on line "
           +eps.getLineNum()+" ("+file.getName() +" line "
           +file.getLineNum()+")");
      }
   }

}
