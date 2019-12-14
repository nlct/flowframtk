// File          : EPSSetCmykColor.java
// Purpose       : class representing setcmykcolor operator
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
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Object representing setcmykcolor operator.
 * @author Nicola L C Talbot
 */
public class EPSSetCmykColor extends EPSOperator
{
   public EPSSetCmykColor()
   {
      super("setcmykcolor");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      double black = stack.popDouble();
      double yellow = stack.popDouble();
      double magenta = stack.popDouble();
      double cyan = stack.popDouble();

      if (black > 1)
      {
         black = 1;
      }
      else if (black < 0)
      {
         black = 0;
      }

      if (yellow > 1)
      {
         yellow = 1;
      }
      else if (yellow < 0)
      {
         yellow = 0;
      }

      if (magenta > 1)
      {
         magenta = 1;
      }
      else if (magenta < 0)
      {
         magenta = 0;
      }

      if (cyan > 1)
      {
         cyan = 1;
      }
      else if (cyan < 0)
      {
         cyan = 0;
      }

      eps.getCurrentGraphicsState().setPaint(
         new JDRColorCMYK(eps.getCanvasGraphics(),
            cyan, magenta, yellow, black));
   }

}
