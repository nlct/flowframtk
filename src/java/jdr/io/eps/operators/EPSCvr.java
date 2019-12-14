// File          : EPSCvr.java
// Purpose       : class representing cvr operator
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
 * Object representing cvr operator.
 * @author Nicola L C Talbot
 */
public class EPSCvr extends EPSOperator
{
   public EPSCvr()
   {
      super("cvr");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      EPSObject object = stack.popObject();

      if (object instanceof EPSNumber)
      {
         EPSNumber number = (EPSNumber)object;

         if (number instanceof EPSDouble)
         {
            stack.add(number);
         }
         else
         {
            stack.pushDouble(number.doubleValue());
         }
      }
      else if (object instanceof EPSString)
      {
         try
         {
            String[] split = 
               ((EPSString)object).value().split(" ", 2);
            String str = split[0];
            split = str.split("#", 2);

            if (split.length == 1)
            {
               stack.pushDouble(Double.parseDouble(str));
            }
            else
            {
               stack.pushDouble((double)Integer.parseInt(split[1],
                  Integer.parseInt(split[0])));
            }
         }
         catch (NumberFormatException e)
         {
            throw new InvalidEPSObjectException(
               "(cvr) invalid string", eps.getLineNum());
         }
      }
      else
      {
         throw new InvalidEPSObjectException("(cvr) invalid type",
            eps.getLineNum());
      }
   }
}
