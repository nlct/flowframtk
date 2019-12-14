// File          : EPSOperator.java
// Purpose       : class representing an EPS operator
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
package com.dickimawbooks.jdr.io.eps;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.font.*;
import java.util.*;
import java.util.regex.*;
import java.text.DateFormat;
import java.math.*;
import javax.imageio.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Object representing an Encapsulated PostScript operator.
 * @author Nicola L C Talbot
 */
public abstract class EPSOperator implements EPSObject
{
   public EPSOperator(String operatorName)
   {
      name = operatorName;
   }

   /**
    * Executes this operator.
    * @param stack the stack
    * @param eps EPS object containing image information
    * @throws InvalidFormatException if there is something wrong
    * with the format of the PostScript file
    * @throws NoninvertibleTransformException if an attempt is
    * made to invert a non-invertible matrix
    * @throws IOException if I/O error occurs
    */
   public abstract void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException;

   /**
    * Gets the name associated with this operator.
    */
   public String getName()
   {
      return name;
   }

   public EPSName pstype()
   {
      return new EPSName("operator");
   }

   public String toString()
   {
      return name;
   }

   /**
    * Just returns this. (It doesn't make sense to clone an
    * operator.)
    */
   public Object clone()
   {
      return this;
   }

   private String name;
}
