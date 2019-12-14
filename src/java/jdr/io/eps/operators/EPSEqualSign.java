// File          : EPSEqualSign.java
// Purpose       : class representing = operator
// Creation Date : 1st June 2008
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
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Object representing = operator.
 * @author Nicola L C Talbot
 */
public class EPSEqualSign extends EPSOperator
{
   public EPSEqualSign()
   {
      super("=");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      EPSObject object = stack.popObject();

      if (object instanceof EPSNumber
       || object instanceof EPSBoolean)
      {
         eps.getMessageSystem().getPublisher().publishMessages(
            MessageInfo.createMessage(object.toString()));
      }
      else if (object instanceof EPSName)
      {
         eps.getMessageSystem().getPublisher().publishMessages(
            MessageInfo.createMessage(
            ((EPSName)object).toString().replaceFirst("^/","")));
      }
      else if (object instanceof EPSString)
      {
         eps.getMessageSystem().getPublisher().publishMessages(
           MessageInfo.createMessage(((EPSString)object).value()));
      }
      else
      {
         eps.getMessageSystem().getPublisher().publishMessages(
           MessageInfo.createMessage("--nostringval--"));
      }
   }

}
