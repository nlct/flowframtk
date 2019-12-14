// File          : EPSArray.java
// Purpose       : class representing an EPS array
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
 * Class representing an EPS array.
 * @author Nicola L C Talbot
 */
public class EPSArray 
   implements EPSIndexComposite,EPSReadable,EPSWriteable
{
   /**
    * Initialises a new EPS array of given size.
    * @param size the size of the new array
    */
   public EPSArray(int size)
   {
      objects_ = new EPSObject[size];

      for (int i = 0; i < size; i++)
      {
         objects_[i] = new EPSNull();
      }
   }

   /**
    * Initialises a new EPS array containing all the objects
    * stored in the given vector.
    * @param vector the elements to store in the array
    * @param isReadOnly determines whether the array is read only
    */
   public EPSArray(Vector<EPSObject> vector, boolean isReadOnly)
   {
      int n = vector.size();
      objects_ = new EPSObject[n];

      for (int i = 0; i < n; i++)
      {
         objects_[i] = vector.get(i);
      }

      hasWriteAccess_ = !isReadOnly;
   }

   /**
    * Initialises a new EPS array containing all the objects
    * stored in the given vector. The array has write access.
    * @param vector the elements to store in the array
    */
   public EPSArray(Vector<EPSObject> vector)
   {
      this(vector, false);
   }

   /**
    * Initialises a new EPS array containing all the objects
    * stored in the given array.
    * @param array the elements to store in the EPS array
    * @param isReadOnly determines whether the EPS array is read only
    */
   public EPSArray(EPSObject[] array, boolean isReadOnly)
   {
      objects_ = new EPSObject[array.length];

      for (int i = 0; i < array.length; i++)
      {
         objects_[i] = array[i];
      }

      hasWriteAccess_ = !isReadOnly;
   }

   /**
    * Initialises a new EPS array containing all the numbers
    * stored in the given array. The numbers are stored as
    * {@link EPSDouble} objects.
    * @param array the elements to store in the EPS array
    * @param isReadOnly determines whether the EPS array is read only
    */
   public EPSArray(double[] array, boolean isReadOnly)
   {
      objects_ = new EPSObject[array.length];

      for (int i = 0; i < array.length; i++)
      {
         objects_[i] = new EPSDouble(array[i]);
      }

      hasWriteAccess_ = !isReadOnly;
   }

   /**
    * Initialises a new EPS array containing all the numbers
    * stored in the given array. The numbers are stored as
    * {@link EPSDouble} objects.
    * @param array the elements to store in the EPS array
    * @param isReadOnly determines whether the EPS array is read only
    */
   public EPSArray(float[] array, boolean isReadOnly)
   {
      objects_ = new EPSObject[array.length];

      for (int i = 0; i < array.length; i++)
      {
         objects_[i] = new EPSDouble((double)array[i]);
      }

      hasWriteAccess_ = !isReadOnly;
   }

   /**
    * Initialises a new EPS array containing all the objects
    * stored in the given array. The array has write access.
    * @param array the elements to store in the EPS array
    */
   public EPSArray(EPSObject[] array)
   {
      this(array, false);
   }

   /**
    * Initialises a new EPS array containing all the numbers
    * stored in the given array. The numbers are stored as
    * {@link EPSDouble} objects. The array has write access.
    * @param array the elements to store in the EPS array
    */
   public EPSArray(double[] array)
   {
      this(array, false);
   }

   /**
    * Initialises a new EPS array containing all the numbers
    * stored in the given array. The numbers are stored as
    * {@link EPSDouble} objects. The array has write access.
    * @param array the elements to store in the EPS array
    */
   public EPSArray(float[] array)
   {
      this(array, false);
   }

   private EPSArray()
   {
      this(0);
   }

   /**
    * Binds any procedures or arrays contained in this array, unless
    * this array is read only.
    * @param stack the stack
    * @throws NoReadAccessException if this doesn't have read access
    */
   public void bind(EPSStack stack) throws NoReadAccessException
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

            if (op != null)
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
   }

   public void forall(EPSStack stack, EPSProc proc)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      for (int i = 0, n = size(); i < n; i++)
      {
         stack.add(get(i));
         stack.execObject(proc);

         if (stack.getExitStatus()) break;
      }
   }

   /**
    * Gets the size of this array.
    * @return the size of this array
    */
   public int size()
   {
      return objects_.length;
   }

   public int length()
   {
      return size();
   }

   public void copy(EPSObject object)
      throws InvalidEPSObjectException,
             NoWriteAccessException
   {
      if (!hasWriteAccess())
      {
         throw new NoWriteAccessException();
      }

      if (!(object instanceof EPSArray))
      {
         throw new InvalidEPSObjectException("(copy) not an array");
      }

      EPSObject[] array = ((EPSArray)object).objects_;

      for (int i = 0; i < array.length; i++)
      {
         set(i, array[i]);
      }
   }

   /**
    * Replaces the element at the given index with the given
    * object.
    * @param idx the index of the element to replace
    * @param object the object to be stored at the specified index
    * @throws NoWriteAccessException if this EPSArray doesn't
    * have write access
    * @throws ArrayIndexOutOfBoundsException if the index is out
    * of range
    */
   public void set(int idx, EPSObject object)
      throws NoWriteAccessException,
      ArrayIndexOutOfBoundsException
   {
      if (!hasWriteAccess_)
      {
         throw new NoWriteAccessException("array has no write access");
      }

      if (idx < 0 || idx >= objects_.length)
      {
         throw new ArrayIndexOutOfBoundsException(idx);
      }

      objects_[idx] = object;
   }

   public void put(EPSObject index, EPSObject value)
      throws InvalidEPSObjectException,
             NoWriteAccessException
   {
      if (!(index instanceof EPSNumber))
      {
         throw new InvalidEPSObjectException("(put) invalid index");
      }

      if (!hasWriteAccess())
      {
         throw new NoWriteAccessException();
      }

      set(((EPSNumber)index).intValue(), value);
   }

   public EPSObject get(int idx)
      throws NoReadAccessException
   {
      if (!hasReadAccess_)
      {
         throw new NoReadAccessException("array has no read access");
      }

      if (idx < 0 || idx >= objects_.length)
      {
         throw new ArrayIndexOutOfBoundsException(idx);
      }

      return objects_[idx];
   }

   public EPSObject get(EPSObject index)
      throws InvalidEPSObjectException,
             NoReadAccessException
   {
      if (!(index instanceof EPSNumber))
      {
         throw new InvalidEPSObjectException("(get) invalid index");
      }

      return get(((EPSNumber)index).intValue());
   }

   /**
    * Gets the element at the given index and returns as a 
    * <code>double</code>.
    * @param idx the index of the required element
    * @return the element at the given index as a <code>double</code>
    * @throws InvalidEPSObjectException if the element at the given
    * index is not an {@link EPSNumber}.
    * @throws NoReadAccessException if this array has no read access
    */
   public double getDouble(int idx)
      throws InvalidEPSObjectException,
             NoReadAccessException
   {
      if (!hasReadAccess_)
      {
         throw new NoReadAccessException();
      }

      if (objects_[idx] instanceof EPSNumber)
      {
         return ((EPSNumber)objects_[idx]).doubleValue();
      }

      throw new InvalidEPSObjectException("array element "+idx+
         " is not a number");
   }

   /**
    * Gets the element at the given index and returns as an 
    * <code>int</code>. (Note rounding may occur if the number
    * is stored as an {@link EPSDouble}.)
    * @param idx the index of the required element
    * @return the element at the given index as an <code>int</code>
    * @throws InvalidEPSObjectException if the element at the given
    * index is not an {@link EPSNumber}.
    * @throws NoReadAccessException if this array has no read access
    * @see EPSNumber#intValue()
    */
   public int getInteger(int idx)
      throws InvalidEPSObjectException,NoReadAccessException
   {
      if (!hasReadAccess_)
      {
         throw new NoReadAccessException();
      }

      if (objects_[idx] instanceof EPSNumber)
      {
         return ((EPSNumber)objects_[idx]).intValue();
      }

      throw new InvalidEPSObjectException("array element "+idx+
         " is not a number");
   }

   /**
    * Tests whether this <code>EPSArray</code> is a transformation
    * matrix. A transformation matrix is an array with 6 elements
    * where all elements implement {@link EPSNumber}.
    * @return <code>true</code> if this <code>EPSArray</code> is a 
    * transformation matrix, otherwise <code>false</code>
    */
   public boolean isMatrix()
   {
      if (objects_.length != 6)
      {
         return false;
      }

      for (int i = 0; i < 6; i++)
      {
         if (!(objects_[i] instanceof EPSNumber))
         {
            return false;
         }
      }

      return true;
   }

   /**
    * Sets all elements of this array to the given transformation
    * matrix.
    * @param matrix the transformation matrix to which this
    * <code>EPSArray</code> must be set
    * @throws NotMatrixException if the size of either this 
    * <code>EPSArray</code> or the given matrix isn't 6
    * @throws NoWriteAccessException if this array has no write access
    */
   public void setMatrix(double[] matrix)
      throws NotMatrixException,
             NoWriteAccessException
   {
      if (!hasWriteAccess_)
      {
         throw new NoWriteAccessException();
      }

      if (objects_.length != 6 || matrix.length != 6)
      {
         throw new NotMatrixException();
      }

      for (int i = 0; i < 6; i++)
      {
         set(i, new EPSDouble(matrix[i]));
      }
   }

   /**
    * Sets this <code>EPSArray</code> to the identity transformation
    * matrix.
    * @throws NotMatrixException if the size of this 
    * <code>EPSArray</code> isn't 6
    * @throws NoWriteAccessException if this array has no write
    * access
    */
   public void setIdentity()
      throws NotMatrixException,
             NoWriteAccessException
   {
      if (!hasWriteAccess_)
      {
         throw new NoWriteAccessException();
      }

      if (objects_.length != 6)
      {
         throw new NotMatrixException();
      }

      set(0, new EPSDouble(1));
      set(1, new EPSDouble(0));
      set(2, new EPSDouble(0));
      set(3, new EPSDouble(1));
      set(4, new EPSDouble(0));
      set(5, new EPSDouble(0));
   }

   /**
    * Gets the elements of this <code>EPSArray</code> as an array
    * of <code>double</code>.
    * @throws InvalidEPSObjectException if any of the elements of this
    * <code>EPSArray</code> don't implement {@link EPSNumber}
    */
   public double[] getDoubleArray()
      throws InvalidEPSObjectException
   {
      double[] array = new double[objects_.length];

      for (int i = 0; i < objects_.length; i++)
      {
         if (!(objects_[i] instanceof EPSNumber))
         {
            throw new InvalidEPSObjectException("not a numerical array");
         }

         array[i] = ((EPSNumber)objects_[i]).doubleValue();
      }

      return array;
   }

   /**
    * Gets the elements of this <code>EPSArray</code> as 
    * transformation matrix.
    * @throws NotMatrixException if any of the elements of this
    * <code>EPSArray</code> don't implement {@link EPSNumber} or
    * if the size of this <code>EPSArray</code> isn't 6
    */
   public double[] getMatrix()
      throws NotMatrixException
   {
      double[] array = null;
      try
      {
         array = getDoubleArray();
      }
      catch (InvalidFormatException e)
      {
         throw new NotMatrixException();
      }

      if (array.length != 6)
      {
         throw new NotMatrixException();
      }

       return array;
   }

   /**
    * Gets the transformation defined by this <code>EPSArray</code>.
    * @throws NotMatrixException if this <code>EPSArray</code>
    * doesn't represent a transformation matrix
    * @see #isMatrix()
    * @return the transformation as an <code>AffineTransform</code>
    */
   public AffineTransform getTransform()
      throws NotMatrixException
   {
      return new AffineTransform(getMatrix());
   }

   public EPSComposite getInterval(int index, int count)
      throws InvalidEPSObjectException,NoReadAccessException
   {
      if (!hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      int n = size();

      if ((index+count) > n)
      {
         throw new ArrayIndexOutOfBoundsException(
            "array subscript out of bounds");
      }

      EPSArray subarray = new EPSArray(count);

      for (int i = 0; i < count; i++)
      {
         try
         {
            subarray.set(i, objects_[index+i]);
         }
         catch (NoWriteAccessException e)
         {
         }
      }

      return subarray;
   }

   public void putInterval(int index, EPSComposite object)
      throws InvalidEPSObjectException,
             NoWriteAccessException,
             NoReadAccessException
   {
      if (!(object instanceof EPSArray))
      {
         throw new InvalidEPSObjectException(
            "array putinterval requires array subinterval");
      }

      EPSArray array = (EPSArray)object;

      if (!hasWriteAccess())
      {
         throw new NoWriteAccessException();
      }

      if (!array.hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      for (int i = 0; i < array.size(); i++)
      {
         set(i+index, array.objects_[i]);
      }
   }

   /**
    * Returns a string representation of this <code>EPSArray</code>
    * @return a string representation of this <code>EPSArray</code>
    */
   public String toString()
   {
      String str = "[";
      for (int i = 0, n = objects_.length; i < n; i++)
      {
         str += objects_[i];
         if (i != n-1) str += " ";
      }
      str += "]";
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

   public Object clone()
   {
      EPSArray array = new EPSArray(objects_.length);

      for (int i = 0; i < objects_.length; i++)
      {
         array.objects_[i] = (EPSObject)objects_[i].clone();
      }

      array.hasReadAccess_ = hasReadAccess_;
      array.hasWriteAccess_ = hasWriteAccess_;

      return array;
   }

   public void makeEqual(EPSObject object)
   {
      EPSObject[] array = ((EPSArray)object).objects_;

      for (int i = 0; i < array.length; i++)
      {
         objects_[i] = array[i];
      }

      hasReadAccess_ = ((EPSArray)object).hasReadAccess_;
      hasWriteAccess_ = ((EPSArray)object).hasWriteAccess_;
   }

   protected EPSObject[] objects_;
   private boolean hasReadAccess_=true, hasWriteAccess_=true;
}
