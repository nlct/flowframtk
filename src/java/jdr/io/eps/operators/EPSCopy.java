// File          : EPSCopy.java
// Purpose       : class representing copy operator
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
import java.util.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.eps.*;
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Object representing copy operator.
 * @author Nicola L C Talbot
 */
public class EPSCopy extends EPSOperator
{
   public EPSCopy()
   {
      super("copy");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      EPSObject lastElement = stack.lastElement();

      if (lastElement instanceof EPSNumber)
      {
         int count = stack.popInteger();

         int length = stack.size();

         if (length < count)
         {
            throw new InvalidFormatException(
               "(copy) not enough elements in stack",
               eps.getLineNum());
         }

         Vector<EPSObject> objects = new Vector<EPSObject>(count);

         for (int i = count-1; i >= 0; i--)
         {
            objects.add(stack.get(length-1-i));
         }

         stack.pushObjects(objects);
      }
      else if (lastElement instanceof GraphicsState)
      {
         GraphicsState gstate2 = stack.popGraphicsState();
         GraphicsState gstate1 = stack.popGraphicsState();

         gstate2.copy(gstate1);

         stack.add(gstate2);
      }
      else
      {
         EPSComposite object2 = stack.popEPSComposite();
         EPSComposite object1 = stack.popEPSComposite();

         object2.copy(object1);

         stack.add(object2);
      }
   }
}
