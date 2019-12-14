// File          : EPSProc.java
// Purpose       : class representing an EPS procedure
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
 * Class representing an EPS procedure.
 * @author Nicola L C Talbot
 */
public class EPSProc extends Vector<EPSObject> 
   implements EPSObject,EPSReadable,EPSWriteable
{
   /**
    * Initialises empty procedure.
    */
   public EPSProc()
   {
      super();
   }

   /**
    * Returns the number of elements in this procedure.
    * @return the number of elements in this procedure
    */
   public int length() throws InvalidEPSObjectException
   {
      return size();
   }

   /**
    * Bind this procedure.
    * @param stack the stack
    */
   public void bind(EPSStack stack)
      throws NoReadAccessException
   {
      if (!hasWriteAccess())
      {
         return;
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
               set(i, op);
            }
         }
      }

      try
      {
         setWriteAccess(false);
      }
      catch (InvalidEPSObjectException e)
      {
      }
   }

   public EPSObject get(EPSObject index)
      throws InvalidEPSObjectException,
             NoReadAccessException
   {
      if (!hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      if (!(index instanceof EPSNumber))
      {
         throw new InvalidEPSObjectException("(get) invalid index");
      }

      if (!hasReadAccess())
      {
         throw new InvalidEPSObjectException("(get) no read access");
      }

      return get(((EPSNumber)index).intValue());
   }

   public void put(EPSObject index, EPSObject value)
      throws InvalidEPSObjectException,
             NoWriteAccessException
   {
      if (!hasWriteAccess())
      {
         throw new NoWriteAccessException();
      }

      if (!(index instanceof EPSNumber))
      {
         throw new InvalidEPSObjectException("(put) invalid index");
      }

      put(((EPSNumber)index).intValue(), value);
   }

   public void copy(EPSObject object)
      throws InvalidEPSObjectException,
             NoWriteAccessException
   {
      if (!hasWriteAccess())
      {
         throw new NoWriteAccessException();
      }

      if (!(object instanceof EPSProc))
      {
         throw new InvalidEPSObjectException("(copy) not a proc");
      }

      EPSProc group = (EPSProc)object;

      for (int i = 0, n=group.size(); i < n; i++)
      {
         put(i, group.get(i));
      }
   }

   /**
    * Puts the given object at the given index of this object.
    * @param index the index at which to put the given object
    * @param object the object to put at the given index
    * @return the previous element at the given index
    * @throws NoWriteAccessException if this object has no write
    * access
    * @throws ArrayIndexOutOfBoundsException if the index is
    * out of bounds (<code>index</code> &lt; 0 or <code>index</code>
    * &gt;= size())
    */
   public EPSObject put(int index, EPSObject object)
      throws NoWriteAccessException
   {
      if (!hasWriteAccess_)
      {
         throw new NoWriteAccessException("(put) no write access");
      }

      return set(index, object);
   }

   /**
    * Returns a string representation of this object
    * @return a string representation of this object
    */
   public String toString()
   {
      String str = "{";
      int n = size();
      int i = 0;

      for (Enumeration<EPSObject> en=elements();
           en.hasMoreElements();i++)
      {
         EPSObject object = en.nextElement();
         str += object;

         if (i < n-1) str += " ";
      }
      str += "}";

      return str;
   }

   public EPSName pstype()
   {
      return new EPSName("arraytype");
   }

   public boolean hasReadAccess()
   {
      return hasReadAccess_;
   }

   public boolean hasWriteAccess()
   { 
      return hasWriteAccess_;
   }

   public void setReadAccess(boolean access)
      throws InvalidEPSObjectException
   {
      hasReadAccess_ = access;
   }

   public void setWriteAccess(boolean access)
      throws InvalidEPSObjectException
   {
      hasWriteAccess_ = access;
   }

   private boolean hasReadAccess_=true, hasWriteAccess_=true;
}
