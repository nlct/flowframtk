// File          : EPSArcTo.java
// Purpose       : class representing arcto operator
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
 * Object representing arcto operator.
 * @author Nicola L C Talbot
 */
public class EPSArcTo extends EPSOperator
{
   public EPSArcTo()
   {
      super("arcto");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      double radius = stack.popDouble();
      double y2 = stack.popDouble();
      double x2 = stack.popDouble();
      double y1 = stack.popDouble();
      double x1 = stack.popDouble();

      Point2D tangent1 = new Point2D.Double();
      Point2D tangent2 = new Point2D.Double();

      try
      {
         eps.getCurrentGraphicsState().arcTo(x1, y1, x2, y2, radius,
           tangent1, tangent2);
      }
      catch (NoCurrentPointException e)
      {
         throw new NoCurrentPointException(
            "(arcto) "+e.getMessage(), eps.getLineNum());
      }

      stack.pushDouble(tangent1.getX());
      stack.pushDouble(tangent1.getY());
      stack.pushDouble(tangent2.getX());
      stack.pushDouble(tangent2.getY());
   }
}
