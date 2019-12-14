// File          : EPSPackedArray.java
// Purpose       : class representing an EPS packed array
// Date          : 1st February 2006
// Last Modified : 28 July 2007
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
 * Class representing an EPS packed array.
 * @author Nicola L C Talbot
 */
public class EPSPackedArray extends EPSArray
{
   public EPSPackedArray(Vector<EPSObject> vector)
   {
      super(vector, true);
   }

   public EPSPackedArray(EPSObject[] array)
   {
      super(array, true);
   }

   public EPSPackedArray(double[] array)
   {
      super(array, true);
   }

   public EPSPackedArray(float[] array)
   {
      super(array, true);
   }

   /**
    * Binds any procedures or arrays contained in this array, unless
    * this array is read only.
    * @param stack the stack
    */
   public void bind(EPSStack stack)
      throws NoReadAccessException
   {
      try
      {
         super.setWriteAccess(true);
      }
      catch (InvalidEPSObjectException e)
      {
      }

      for (int i = 0; i < size(); i++)
      {
         EPSObject object = get(i);

         if (object instanceof EPSProc)
         {
            ((EPSProc)object).bind(stack);
         }
         else if (object instanceof EPSArray)
         {
            ((EPSArray)object).bind(stack);
         }
         else if (object instanceof EPSName)
         {
            EPSOperator op = ((EPSName)object).getOperator(stack);

            if (op != (EPSOperator)null)
            {
               try
               {
                  set(i, op);
               }
               catch (NoWriteAccessException e)
               {
               }
            }
         }
      }

      try
      {
         super.setWriteAccess(false);
      }
      catch (InvalidEPSObjectException e)
      {
      }

   }

   public EPSName pstype()
   {
      return new EPSName("packedarraytype");
   }

   public void setWriteAccess(boolean access)
      throws InvalidEPSObjectException
   {
      if (access)
      {
         throw new InvalidEPSObjectException(
            "packed arrays can't have write access");
      }

      super.setWriteAccess(false);
   }

   public Object clone()
   {
      return new EPSPackedArray(objects_);
   }
}
