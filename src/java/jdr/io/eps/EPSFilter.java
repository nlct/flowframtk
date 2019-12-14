// File          : EPSFilter.java
// Purpose       : class representing a filter
// Date          : 25 May 2008
// Last Modified : 25 May 2008
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
 * Class representing a filter.
 * @author Nicola L C Talbot
 */
public abstract class EPSFilter implements EPSObject
{
   /**
    * Creates new filter.
    * @param epsdata data source/target
    * @throws InvalidFilterNameException if filter name is unknown
    * (currently only supports ASCIIHexDecode and ASCII85Decode)
    * @throws InvalidEPSObjectException if data is not one of
    * EPSFile, EPSProc or EPSString
    */
   public EPSFilter(EPSObject epsdata)
      throws InvalidEPSObjectException
   {
      if (!(epsdata instanceof EPSFile)
       && !(epsdata instanceof EPSProc)
       && !(epsdata instanceof EPSString))
      {
         throw new InvalidEPSObjectException(
            "Invalid data source/target type");
      }

      data = epsdata;
      dict = null;
      param = null;
   }

   /**
    * Creates new filter.
    * @param epsdata data source/target
    * @param epsdict dictionary containing additional parameters
    * @throws InvalidEPSObjectException if data is not one of
    * EPSFile, EPSProc or EPSString
    */
   public EPSFilter(EPSObject epsdata, EPSDict epsdict)
      throws InvalidEPSObjectException
   {
      if (!(epsdata instanceof EPSFile)
       && !(epsdata instanceof EPSProc)
       && !(epsdata instanceof EPSString))
      {
         throw new InvalidEPSObjectException(
            "Invalid data source/target type");
      }

      data = epsdata;
      dict = epsdict;
      param = null;
   }

   /**
    * Creates new filter.
    * @param epsdata data source/target
    * @param epsdict dictionary containing additional parameters
    * @param filterparam filter parameters
    * @throws InvalidEPSObjectException if data is not one of
    * EPSFile, EPSProc or EPSString
    */
   public EPSFilter(EPSObject epsdata, EPSDict epsdict,
      EPSObject[] filterparam, String filtername)
      throws InvalidEPSObjectException
   {
      if (!(epsdata instanceof EPSFile)
       && !(epsdata instanceof EPSProc)
       && !(epsdata instanceof EPSString))
      {
         throw new InvalidEPSObjectException(
            "Invalid data source/target type");
      }

      data = epsdata;
      dict = epsdict;
      param = filterparam;
   }

   protected EPSFilter()
   {
      data = null;
      dict = null;
      param = null;
   }

   /**
    * Gets this filter's name.
    * @return filter name
    */
   public abstract String getName();

   /**
    * Gets whether this filter is encoding.
    * @return true if encoding or false
    * if decoding
    */
   public abstract boolean isEncoding();

   /**
    * Gets data source/target.
    * @return data source/target
    */
   public EPSObject getData()
   {
      return data;
   }

   /**
    * Gets dictionary associated with this filter.
    * @return dictionary of parameters
    */
   public EPSDict getDict()
   {
      return dict;
   }

   /**
    * Gets parameters associated with this filter.
    * @return array of parameters
    */
   public EPSObject[] getParams()
   {
      return param;
   }

   /**
    * Do encoding.
    * @param out stream to write encoded data
    * @param stack the stack
    * @throws InvalidFormatObjectException if data is of
    * type {@link EPSProc} and produces invalid data
    * @throws IOException if I/O error occurs
    * @throws NoninvertibleTransformException data is 
    * a procedure and it encounters a non invertible transform
    */
   public void encode(PrintWriter out, EPSStack stack)
      throws InvalidFormatException,IOException,
         NoninvertibleTransformException
   {
   }

   /**
    * Do decoding.
    * @param out stream to write decoded data
    * @param stack the stack
    * @throws InvalidFormatObjectException if data is of
    * type {@link EPSProc} and produces invalid data
    * @throws IOException if I/O error occurs
    * @throws NoninvertibleTransformException data is 
    * a procedure and it encounters a non invertible transform
    */
   public void decode(PrintWriter out, EPSStack stack)
      throws InvalidFormatException,IOException,
         NoninvertibleTransformException
   {
   }

   /**
    * Creates a temporary file containing decoded data and returns.
    * If the file has already been created, just returns the file.
    * @param stack the stack
    * @return the temporary file
    * @throws IOException if I/O error occurs
    * @throws InvalidFormatObjectException if data is of
    * type {@link EPSProc} and produces invalid data
    * @throws NoninvertibleTransformException data is 
    * a procedure and it encounters a non invertible transform
    */
   public EPSFile getFile(EPSStack stack)
      throws IOException,InvalidFormatException,
        NoninvertibleTransformException
   {
      if (file != null)
      {
         return file;
      }

      File f = File.createTempFile("epsjdr", null);

      f.deleteOnExit();

      PrintWriter out = new PrintWriter(f);

      if (isEncoding())
      {
         encode(out, stack);
      }
      else
      {
         decode(out, stack);
      }

      out.close();

      file = new EPSFile(f, "r");

      return file;
   }

   /**
    * Returns a string representation of this object
    * @return a string representation of this object
    */
   public String toString()
   {
      return getName()+" filter";
   }

   public boolean equals(Object object)
   {
      if (!(object instanceof EPSFilter)) return false;

      EPSFilter filter = (EPSFilter)object;

      if (filter.dict == null && dict != null) return false;
      if (dict == null && filter.dict != null) return false;
      if (filter.param == null && param != null) return false;
      if (param == null && filter.param != null) return false;
      if (filter.data == null && data != null) return false;
      if (data == null && filter.data != null) return false;

      if (!getName().equals(filter.getName())) return false;

      if (dict != null && !dict.equals(filter.dict)) return false;
      if (param != null && !param.equals(filter.param))
      {
         return false;
      }
      if (data != null && !data.equals(filter.data)) return false;

      return true;
   }

   public EPSName pstype()
   {
      return new EPSName(getName()+" filter");
   }

   public abstract Object clone();

   public void makeEqual(EPSObject object)
   {
      EPSFilter filter = (EPSFilter)object;

      data = (EPSObject)filter.data.clone();

      if (filter.dict == null)
      {
         dict = null;
      }
      else
      {
         dict = (EPSDict)filter.dict.clone();
      }

      if (filter.param == null)
      {
         param = null;
      }
      else
      {
         param = new EPSObject[filter.param.length];

         for (int i = 0; i < param.length; i++)
         {
            param[i] = (EPSObject)filter.param[i].clone();
         }
      }

      file = filter.file;
   }

   private EPSObject data;
   private EPSDict dict;
   private EPSObject[] param;
   private EPSFile file=null;
}
