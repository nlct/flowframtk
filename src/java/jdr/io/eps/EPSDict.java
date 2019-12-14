// File          : EPSDict.java
// Purpose       : class representing an EPS dictionary
// Creation Date : 1st February 2006
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
 * Class representing an EPS dictionary.
 * @author Nicola L C Talbot
 */
public class EPSDict extends Hashtable<String,EPSObject> 
implements EPSObject,EPSComposite,EPSReadable,EPSWriteable,
           EPSDictionaryInterface
{
   /**
    * Initialises with an initial capacity of 11.
    */
   public EPSDict()
   {
      this(11);
   }

   /**
    * Initialises with the given initial capacity.
    * @param initialCapacity the initial capacity of this dictionary
    */
   public EPSDict(int initialCapacity)
   {
      super(initialCapacity);
      capacity_ = initialCapacity;
   }

   /**
    * Gets the capacity of this dictionary.
    * @return the capacity of this dictionary
    */
   public int getCapacity()
   {
      return capacity_;
   }

   public void forall(EPSStack stack, EPSProc proc)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      for (Enumeration<String> en=keys(); en.hasMoreElements();)
      {
         String key = en.nextElement();
         stack.add(new EPSName(key));
         stack.add(get(key));
         stack.execObject(proc);

         if (stack.getExitStatus()) break;
      }
   }

   public void putInterval(int index, EPSComposite object)
      throws InvalidEPSObjectException,
             NoWriteAccessException,
             NoReadAccessException
   {
      throw new InvalidEPSObjectException(
         "putinterval not available for dictionaries");
   }

   public EPSComposite getInterval(int index, int count)
      throws InvalidEPSObjectException,
             NoReadAccessException
   {
      throw new InvalidEPSObjectException(
         "getinterval not available for dictionaries");
   }

   public EPSObject get(EPSObject index)
      throws InvalidEPSObjectException,NoReadAccessException
   {
      if (!hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      if (!(index instanceof EPSName))
      {
         throw new InvalidEPSObjectException("(get) invalid key");
      }

      String name = ((EPSName)index).toString();

      return super.get(name);
   }

   /**
    * Gets an element of this dictionary using the given key.
    * @param key the key identifying the required element
    * @return the object given by the key, or <code>null</code>
    * if not found
    * @throws NoReadAccessException if this object has no read access
    */
   public EPSObject get(String key)
      throws NoReadAccessException
   {
      if (!hasReadAccess_)
      {
         throw new NoReadAccessException();
      }

      EPSObject object = super.get(key);

      if (object == null)
      {
         object = super.get("/"+key);
      }

      return object;
   }

   public void put(EPSObject index, EPSObject value)
      throws InvalidEPSObjectException,NoWriteAccessException
   {
      if (!hasWriteAccess())
      {
         throw new NoWriteAccessException();
      }

      if (!(index instanceof EPSName))
      {
         throw new InvalidEPSObjectException("(put) invalid key");
      }

      String key = ((EPSName)index).toString();

      super.put(key, value);
   }

   public EPSObject putValue(String key, EPSObject value)
      throws NoWriteAccessException
   {
      if (!hasWriteAccess())
      {
         throw new NoWriteAccessException();
      }

      return super.put(key, value);
   }

   /**
    * Gets the element associated with the given key and return as
    * a boolean.
    * @param key the key identifying the required element
    * @return the boolean representation of the required element
    * @throws InvalidEPSObjectException if required element not found
    * or if required element is not an instance of {@link EPSBoolean}
    * @throws NoReadAccessException
    */
   public boolean getBoolean(String key)
      throws InvalidEPSObjectException,NoReadAccessException
   {
      if (!hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      EPSObject value = get(key);

      if (value == null)
      {
         throw new InvalidEPSObjectException(key+" not found in dictionary");
      }

      if (value instanceof EPSBoolean)
      {
         return ((EPSBoolean)value).booleanValue();
      }
      else if ((value instanceof EPSArray)
             && ((EPSArray)value).size() == 1)
      {
         EPSObject element = ((EPSArray)value).get(0);

         if (element instanceof EPSBoolean)
         {
            return ((EPSBoolean)element).booleanValue();
         }
      }

      throw new InvalidEPSObjectException("Boolean expected for key '"
         + key+"'");
   }

   /**
    * Gets the element associated with the given key and return as
    * an integer.
    * @param key the key identifying the required element
    * @return the integer representation of the required element
    * @throws InvalidEPSObjectException if required element not found
    * or if required element does not implement {@link EPSNumber}
    * @throws NoReadAccessException
    * @see EPSNumber#intValue()
    */
   public int getInt(String key)
      throws InvalidEPSObjectException,NoReadAccessException
   {
      if (!hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      EPSObject value = get(key);

      if (value == null)
      {
         throw new InvalidEPSObjectException(key+" not found in dictionary");
      }

      if (value instanceof EPSNumber)
      {
         return ((EPSNumber)value).intValue();
      }
      else if ((value instanceof EPSArray)
             && ((EPSArray)value).size() == 1)
      {
         EPSObject element = ((EPSArray)value).get(0);

         if (element instanceof EPSNumber)
         {
            return ((EPSNumber)element).intValue();
         }
      }

      throw new InvalidEPSObjectException("Number expected for key '"
         + key+"'");
   }

   /**
    * Gets the element associated with the given key and return as
    * a double.
    * @param key the key identifying the required element
    * @return the double representation of the required element
    * @throws InvalidEPSObjectException if required element not found
    * or if required element does not implement {@link EPSNumber}
    * @throws NoReadAccessException if this object has no read access
    */
   public double getDouble(String key)
      throws InvalidEPSObjectException,
             NoReadAccessException
   {
      if (!hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      EPSObject value = get(key);

      if (value == null)
      {
         throw new InvalidEPSObjectException(key+" not found in dictionary");
      }

      if (value instanceof EPSNumber)
      {
         return ((EPSNumber)value).doubleValue();
      }
      else if ((value instanceof EPSArray)
             && ((EPSArray)value).size() == 1)
      {
         EPSObject element = ((EPSArray)value).get(0);

         if (element instanceof EPSNumber)
         {
            return ((EPSNumber)element).doubleValue();
         }
      }

      throw new InvalidEPSObjectException("Number expected for key '"
         + key+"'");
   }

   /**
    * Gets the dictionary associated with the given key.
    * @param key the key identifying the required element
    * @return the required dictionary
    * @throws InvalidEPSObjectException if required element not found
    * or if required element is not an <code>EPSDict</code>
    * @throws NoReadAccessException if this object has no read access
    */
   public EPSDict getDict(String key)
      throws InvalidEPSObjectException,
             NoReadAccessException
   {
      if (!hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      EPSObject value = get(key);

      if (value == null)
      {
         throw new InvalidEPSObjectException(key+" not found in dictionary");
      }

      if (value instanceof EPSDict)
      {
         return (EPSDict)value;
      }

      throw new InvalidEPSObjectException("Key-Val Dictionary expected for key '"+key+"'");
   }

   /**
    * Gets the array associated with the given key.
    * @param key the key identifying the required element
    * @return the required array
    * @throws InvalidEPSObjectException if required element not found
    * or if required element is not an <code>EPSArray</code>
    * @throws NoReadAccessException if this object has no read access
    */
   public EPSArray getArray(String key)
      throws InvalidEPSObjectException,
             NoReadAccessException
   {
      if (!hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      EPSObject value = get(key);

      if (value == null)
      {
         throw new InvalidEPSObjectException(key+" not found in dictionary");
      }

      if (value instanceof EPSArray)
      {
         return (EPSArray)value;
      }

      EPSArray array = new EPSArray(1);
      try
      {
         array.set(0, value);
      }
      catch (NoWriteAccessException ignore)
      {
      }

      return array;
   }

   /**
    * Gets the procedure associated with the given key.
    * @param key the key identifying the required element
    * @return the required procedure
    * @throws InvalidEPSObjectException if required element not found
    * or if required element is not an <code>EPSProc</code>
    * @throws NoReadAccessException if this object has no read access
    */
   public EPSName getProc(String key)
      throws InvalidEPSObjectException,
             NoReadAccessException
   {
      if (!hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      EPSObject value = get(key);

      if (value == null)
      {
         throw new InvalidEPSObjectException(key+" not found in dictionary");
      }

      if (value instanceof EPSName)
      {
         return (EPSName)value;
      }

      throw new InvalidEPSObjectException("procedure or name expected for key '"+key+"'");
   }

   /**
    * Gets the array associated with the given key and returns
    * as an array of <code>double</code>.
    * @param key the key identifying the required element
    * @return the required array
    * @throws InvalidEPSObjectException if required element not found
    * or if required element is not an <code>EPSArray</code>
    * @throws NoReadAccessException if this object has no read access
    */
   public double[] getDoubleArray(String key)
      throws InvalidEPSObjectException,
             NoReadAccessException
   {
      if (!hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      EPSArray array = getArray(key);

      return array.getDoubleArray();
   }

   public void copy(EPSObject object)
      throws InvalidEPSObjectException,NoWriteAccessException
   {
      if (!hasWriteAccess())
      {
         throw new NoWriteAccessException();
      }

      if (!(object instanceof EPSDict))
      {
         throw new InvalidEPSObjectException(
            "(copy) not a dictionary");
      }

      EPSDict dict = (EPSDict)object;

      for (Enumeration<String> en=dict.keys(); en.hasMoreElements();)
      {
         String key = en.nextElement();

         try
         {
            putValue(key, dict.get(key));
         }
         catch (NoReadAccessException e)
         {
            throw new InvalidEPSObjectException(
               "(copy) copied object has no read access");
         }
      }
   }

   public int length()
   {
      return size();
   }

   /**
    * Gets the details of this dictionary.
    * @return the details of this dictionary as a string
    */
   public String getDetails()
   {
      String eol = System.getProperty("line.separator", "\n");
      String str = "<<"+eol;

      for (Enumeration<String> en = keys(); en.hasMoreElements();)
      {
         String key = en.nextElement();
         str += key+" => "+super.get(key);
      }

      str += ">>";
      return str;
   }

   /**
    * Returns a string representation of this object
    * @return a string representation of this object
    */
   public String toString()
   {
      return "-dict-";
   }

   public EPSName pstype()
   {
      return new EPSName("dicttype");
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

   public EPSDict clone()
   {
      EPSDict dict = new EPSDict(capacity_);

      dict.makeEqual(this);

      return dict;
   }

   private EPSObject getValue(String key)
   {
      return super.get(key);
   }

   public void makeEqual(EPSObject object)
   {
      EPSDict dict = (EPSDict)object;

      hasReadAccess_ = dict.hasReadAccess_;
      hasWriteAccess_ = dict.hasWriteAccess_;

      clear();

      for (Enumeration<String> en=dict.keys(); en.hasMoreElements();)
      {
         String key = en.nextElement();

         EPSObject value = dict.getValue(key);

         super.put(key, value);
      }
   }

   private int capacity_;
   private boolean hasReadAccess_=true, hasWriteAccess_=true;
}
