// File          : EPSStatus.java
// Purpose       : class representing status operator
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
 * Object representing status operator.
 * @author Nicola L C Talbot
 */
public class EPSStatus extends EPSOperator
{
   public EPSStatus()
   {
      super("status");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      EPSObject object = stack.popObject();

      if (object instanceof EPSFile)
      {
         EPSFile file = (EPSFile)object;
         stack.pushBoolean(file.status());
      }
      else if (object instanceof EPSString)
      {
         EPSString string = (EPSString)object;

         File file = new File(string.value());

         if (file.exists())
         {
            eps.warning("(status) storage space and "
               +"creation date not implemented");
            stack.pushInteger(0);
            stack.pushLong(file.length());
            long date = file.lastModified();
            stack.pushLong(date);
            stack.pushLong(date);
            stack.pushBoolean(true);
         }
         else
         {
            stack.pushBoolean(false);
         }
      }
      else
      {
         throw new InvalidFormatException(
            "(status) invalid parameter", eps.getLineNum());
      }
   }

}
