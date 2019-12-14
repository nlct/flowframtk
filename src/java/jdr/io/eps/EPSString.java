// File          : EPSString.java
// Purpose       : class representing an EPS string
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
 * Class representing an EPS string. This class stores the
 * string as an array of characters.
 * @author Nicola L C Talbot
 */
public class EPSString
   implements EPSIndexComposite,
              EPSReadable,EPSWriteable,EPSRelational
{
   /**
    * Initialise, obtaining the value from the given string.
    * @param str the string representing this object
    */
   public EPSString(String str)
   {
      chars_ = str.toCharArray();
   }

   /**
    * Initialise, obtaining the value from the given array.
    * @param array the array of characters representing this object
    */
   public EPSString(char[] array)
   {
      chars_ = new char[array.length];
      for (int i = 0; i < array.length; i++)
      {
         chars_[i] = array[i];
      }
   }

   /**
    * Initialise an empty string, allocating the given number of
    * characters.
    * @param n the capacity of this object
    */
   public EPSString(int n)
   {
      chars_ = new char[n];

      for (int i = 0; i < n; i++)
      {
         chars_[i] = (char)0;
      }
   }

   public boolean isEmpty()
   {
      return value().equals("");
   }

   public void forall(EPSStack stack, EPSProc proc)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      for (int i = 0, n = chars_.length; i < n; i++)
      {
         stack.pushInteger((int)chars_[i]);
         stack.execObject(proc);

         if (stack.getExitStatus()) break;
      }
   }

   public boolean equals(Object object)
   {
      if (object instanceof EPSString)
      {
         return value().equals(((EPSString)object).value());
      }

      return false;
   }

   public boolean ge(EPSRelational object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSString)
      {
         return (value().compareTo(((EPSString)object).value()) >= 0);
      }

      throw new InvalidEPSObjectException("(ge) invalid type");
   }

   public boolean gt(EPSRelational object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSString)
      {
         return (value().compareTo(((EPSString)object).value()) > 0);
      }

      throw new InvalidEPSObjectException("(gt) invalid type");
   }

   public boolean le(EPSRelational object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSString)
      {
         return (value().compareTo(((EPSString)object).value()) <= 0);
      }

      throw new InvalidEPSObjectException("(le) invalid type");
   }

   public boolean lt(EPSRelational object)
      throws InvalidEPSObjectException
   {
      if (object instanceof EPSString)
      {
         return (value().compareTo(((EPSString)object).value()) < 0);
      }

      throw new InvalidEPSObjectException("(lt) invalid type");
   }

   /**
    * Returns a string representation of this object
    * @return a string representation of this object
    */
   public String toString()
   {
      String str = "";
      boolean escape=false;
      for (int i = 0, n=chars_.length; i < n; i++)
      {
         if (chars_[i] == '(' || chars_[i] == ')')
         {
            str += "\\";
            str += chars_[i];
         }
         else if (chars_[i] == '\\')
         {
            str += "\\\\";
         }
         else if (chars_[i] == '\n')
         {
            str += "\\n";
         }
         else if (chars_[i] == '\r')
         {
            str += "\\r";
         }
         else if (chars_[i] == '\t')
         {
            str += "\\t";
         }
         else if (chars_[i] == '\f')
         {
            str += "\\f";
         }
         else if (chars_[i] == '\b')
         {
            str += "\\b";
         }
         else if (chars_[i] == 0)
         {
            str += "\\000";
         }
         else if (chars_[i] > 31 && chars_[i] < 128)
         {
            str += chars_[i];
         }
         else
         {
            String oct = Integer.toOctalString(chars_[i]);

            if (chars_[i] < 8)
            {
               oct = "00"+oct;
            }
            else if (chars_[i] < 64)
            {
               oct = "0"+oct;
            }

            str += "\\"+oct;
         }
      }

      return "("+str+")";
   }

   /**
    * The value of this object returned as a <code>String</code>.
    * @return the value of this object as a <code>String</code>.
    */
   public String value()
   {
      return new String(chars_);
   }

   public int length()
   {
      return chars_.length;
   }

   /**
    * Returns the character at the given index of this object.
    * @param idx the index of the required character
    * @throws NoReadAccessException if this object has no read access
    * @throws ArrayOutOfBoundsException if the given index is out
    * of bounds
    */
   public char get(int idx)
      throws NoReadAccessException,
             ArrayIndexOutOfBoundsException
   {
      if (idx < 0 || idx >= chars_.length)
      {
         throw new ArrayIndexOutOfBoundsException(idx);
      }

      return chars_[idx];
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
         throw new InvalidEPSObjectException(
            "(get) numerical index required");
      }

      int i = ((EPSNumber)index).intValue();

      return new EPSInteger((int)chars_[i]);
   }

   /**
    * Puts the numeric value at the given index of this object.
    * @param index the required numerical index
    * @param value the required numerical value
    * @throws InvalidEPSObjectException if either of the arguments
    * are not instances of {@link EPSNumber}
    */
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

      if (!(value instanceof EPSNumber))
      {
         throw new InvalidEPSObjectException("(put) invalid value");
      }

      int i = ((EPSNumber)index).intValue();
      int c = ((EPSNumber)value).intValue();

      chars_[i] = (char)c;
   }

   /**
    * Puts the character at the given index of this string.
    * @param index the index
    * @param value the character
    * @throws NoWriteAccessException if this string is read only
    * @throws ArrayIndexOutOfBoundsException if the index is invalid
    */
   public void put(int index, char value)
      throws NoWriteAccessException,
             ArrayIndexOutOfBoundsException
   {
      if (!hasWriteAccess_)
      {
         throw new NoWriteAccessException();
      }

      if (index < 0 || index >= chars_.length)
      {
         throw new ArrayIndexOutOfBoundsException(index);
      }

      chars_[index] = (char)value;
   }

   public EPSComposite getInterval(int index, int count)
      throws NoReadAccessException,InvalidEPSObjectException
   {
      if (!hasReadAccess_)
      {
         throw new NoReadAccessException();
      }

      return new EPSString(new String(chars_, index, count));
   }

   public void putInterval(int index, EPSComposite object)
      throws NoWriteAccessException,
             NoReadAccessException,
             InvalidEPSObjectException
   {
      if (!(object instanceof EPSString))
      {
         throw new InvalidEPSObjectException(
            "string putinterval requires string subinterval");
      }

      EPSString interval = (EPSString)object;

      if (!hasWriteAccess())
      {
         throw new NoWriteAccessException();
      }

      if (!interval.hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      if (index+interval.chars_.length > chars_.length)
      {
         throw new ArrayIndexOutOfBoundsException(
            "overflow in putInterval");
      }

      for (int i = 0; i < interval.chars_.length; i++)
      {
         chars_[index+i] = interval.get(i);
      }  
   }

   /**
    * Puts the given subset into this object starting from the
    * given index.
    * @param index the starting index
    * @param interval the subset
    * @throws NoWriteAccessException if this object has no write
    * access
    * @throws NoReadAccessException if the subset has no read access
    * @see #putInterval(int,EPSComposite)
    */
   public void putInterval(int index, char[] interval)
      throws NoWriteAccessException,
             NoReadAccessException
   {
      if (!hasWriteAccess_)
      {
         throw new NoWriteAccessException();
      }

      if (index+interval.length-1 >= chars_.length)
      {
         throw new ArrayIndexOutOfBoundsException(
            "overflow in putInterval");
      }

      for (int i = 0; i < interval.length; i++)
      {
         chars_[index+i] = interval[i];
      }  
   }

   /**
    * Returns the index of the first occurrence of
    * the given substring.
    * @param string the substring
    * @return the index of the first occurrence of the given 
    * substring or -1 if not found
    */
   public int indexOf(EPSString string)
   {
      return value().indexOf(string.value());
   }

   /**
    * Returns the index of the first occurrence of
    * the given substring starting from the given start index.
    * @param string the substring
    * @param fromIndex the index from which to start the search
    * @return the index of the first occurrence of the given 
    * substring or -1 if not found
    */
   public int indexOf(EPSString string, int fromIndex)
   {
      return value().indexOf(string.value(), fromIndex);
   }

   /**
    * Returns a copy of this object.
    * @return a copy of this object
    */
   public Object clone()
   {
      return new EPSString(chars_);
   }

   /**
    * Gets the array of characters forming this string.
    * @return the array of characters forming this string
    */
   public char[] getChars()
   {
      return chars_;
   }

   public void copy(EPSObject object)
      throws InvalidEPSObjectException,
             NoWriteAccessException
   {
      if (!hasWriteAccess())
      {
         throw new NoWriteAccessException();
      }

      if (!(object instanceof EPSString))
      {
         throw new InvalidEPSObjectException(
            "(copy) not a EPSString");
      }

      char[] chrs = ((EPSString)object).chars_;

      for (int i = 0; i < chrs.length; i++)
      {
         chars_[i] = chrs[i];
      }
   }

   /**
    * Tests if the given character is an alpha-numeric character.
    * Calls <code>java.lang.Character.isLetterOrDigit(char)</code>.
    * @return <code>true</code> if the given character is an 
    * alpha-numeric character, otherwise <code>false</code>.
    */
   public static boolean isAlphaNumeric(char c)
   {
      return Character.isLetterOrDigit(c);
   }

   /**
    * Tests if the given character is a white space character.
    * Calls <code>java.lang.Character.isWhiteSpace(char)</code>.
    * @return <code>true</code> if the given character is a
    * white space character, otherwise <code>false</code>.
    */
   public static boolean isWhiteSpace(char c)
   {
      return Character.isWhitespace(c);
   }

   /**
    * Tests if the given character is a decimal digit.
    * Calls <code>java.lang.Character.isDigit(char)</code>.
    * @return <code>true</code> if the given character is a
    * decimal digit, otherwise <code>false</code>.
    */
   public static boolean isDigit(char c)
   {
      return Character.isDigit(c);
   }

   /**
    * Tests if the given character is a hexadecimal digit.
    * Calls <code>java.lang.Character.isDigit(char,int)</code>.
    * @return <code>true</code> if the given character is a
    * hexadecimal digit, otherwise <code>false</code>.
    */
   public static boolean isHex(char c)
   {
      return (Character.digit(c, 16) != -1);
   }

   /**
    * Removes first token from this string, parses it,
    * and returns as an EPSObject.
    * @return the first object parsed from this string
    */
   public EPSObject token() throws InvalidFormatException
   {
      idx_ = 0;

      EPSObject object = readObject();

      String string = new String(chars_, idx_, chars_.length-idx_);
      chars_ = string.toCharArray();

      return object;
   }

   private EPSObject readObject()
      throws InvalidFormatException
   {
      EPSObject object = null;

      if (idx_ == chars_.length) return null;

      while (isWhiteSpace(chars_[idx_]))
      {
         idx_++;

         if (idx_ == chars_.length) return null;
      }

      if (chars_[idx_] == '(')
      {
         object = readString();
      }
      else if (chars_[idx_] == '[')
      {
         object = new EPSMark();
         idx_++;
      }
      else if (chars_[idx_] == '{')
      {
         object = readGroup();
      }
      else if (chars_[idx_] == '<')
      {
         if (chars_[idx_+1] == '<')
         {
            object = new EPSDictMark();
            idx_+= 2;
         }
         else
         {
            object = readHex();
         }
      }
      else if (isDigit(chars_[idx_]) || 
         (chars_[idx_] == '.' && isDigit(chars_[idx_+1])))
      {
         object = readNum();
      }
      else
      {
         object = readProc();
      }

      return object;
   }

   private EPSName readProc()
   {
      String string="";
      boolean escape = false;

      for (; idx_ < chars_.length; idx_++)
      {
         if (chars_[idx_] == '{' || chars_[idx_] == '['
          || chars_[idx_] == '}' || chars_[idx_] == ']'
          || chars_[idx_] == '(' || chars_[idx_] == ')'
          || chars_[idx_] == '<' || chars_[idx_] == '>'
          || chars_[idx_] == '/')
         {
            break;
         }

         if (isWhiteSpace(chars_[idx_]))
         {
            idx_++;
            break;
         }

         if (chars_[idx_] == '\\')
         {
            escape = !escape;

            if (idx_ == chars_.length-1)
            {
               string += chars_[idx_];
               break;
            }

            if (escape && isDigit(chars_[idx_+1]))
            {
               string += readChar();
               escape = false;
               continue;
            }
         }
         else
         {
            escape = false;
         }

         string += chars_[idx_];
      }

      return new EPSName(string);
   }

   private EPSObject readNum()
      throws InvalidFormatException
   {
      String string = "";
      boolean isInt=true;
      boolean isExp=false;

      if (chars_[idx_] == '+' || chars_[idx_] == '-')
      {
         string += chars_[idx_];
         idx_++;
      }

      for (; idx_ < chars_.length; idx_++)
      {
         if (chars_[idx_] == '.')
         {
            if (!(isInt || isExp))
            {
               break;
            }
            isInt = false;
            isExp = true;
            string += '.';
         }
         else if (chars_[idx_] == 'e' || chars_[idx_] == 'E')
         {
            isExp = true;
            isInt = false;
            string += chars_[idx_];
         }
         else if ((chars_[idx_] == '+' || chars_[idx_] == '-')
            && (chars_[idx_-1] == 'e' || chars_[idx_-1] == 'E'))
         {
            string += chars_[idx_];
         }
         else if (isDigit(chars_[idx_]))
         {
            string += chars_[idx_];
         }
         else
         {
            break;
         }
      }

      if (isWhiteSpace(chars_[idx_]))
      {
         idx_++;
      }

      try
      {
         if (isInt)
         {
            return new EPSInteger(string);
         }
         else
         {
            return new EPSDouble(string);
         }
      }
      catch (NumberFormatException e)
      {
         throw new InvalidFormatException("(token) can't parse number");
      }
   }

   private char readChar()
   {
      if (chars_[idx_] == '\\')
      {
         idx_++;
      }

      String str = "";

      for (int i = 0; i < 3; i++)
      {
         char c = chars_[idx_];
         if (!isDigit(c))
         {
            break;
         }

         str += c;
         idx_++;
      }

      idx_--;

      return (char)Integer.parseInt(str, 8);
   }

   private EPSObject readString()
      throws InvalidFormatException
   {
      boolean escape=false;

      String string = "";

      int bracket = 1;

      while (true)
      {
         idx_++;
         if (idx_ >= chars_.length)
         {
            return null;
         }

         if (chars_[idx_] == '\\')
         {
            escape = !escape;

            if (idx_==chars_.length-1)
            {
               break;
            }

            if (escape && isDigit(chars_[idx_+1]))
            {
               idx_++;
               string += readChar();
               escape = false;
               continue;
            }
         }
         else
         {
            escape = false;
         }

         if (chars_[idx_] == ')' && !escape)
         {
            bracket--;
            if (bracket == 0) break;
         }
         else if (chars_[idx_] == '(' && !escape)
         {
            bracket++;
         }

         string += chars_[idx_];
      }

      return new EPSString(string);
   }

   private EPSArray readArray()
      throws InvalidFormatException
   {
      idx_++;
      Vector<EPSObject> vector = new Vector<EPSObject>();

      while (idx_ < chars_.length)
      {
         while (isWhiteSpace(chars_[idx_]))
         {
            idx_++;
         }

         if (idx_ == chars_.length) break;

         if (chars_[idx_] == ']')
         {
            idx_++;
            break;
         }

         EPSObject object = readObject();

         if (object == null)
         {
            return null;
         }

         vector.add(object);
      }

      return new EPSArray(vector);
   }

   private EPSProc readGroup()
      throws InvalidFormatException
   {
      idx_++;
      EPSProc vector = new EPSProc();

      while (idx_ < chars_.length)
      {
         while (isWhiteSpace(chars_[idx_]))
         {
            idx_++;
         }

         if (idx_ == chars_.length) break;

         if (chars_[idx_] == '}')
         {
            idx_++;
            break;
         }

         EPSObject object = readObject();

         if (object == null)
         {
            return null;
         }

         vector.add(object);
      }

      return vector;
   }

   private EPSString readHex()
      throws InvalidFormatException
   {
      idx_++;
      String string = "<";

      for (; idx_ < chars_.length; idx_++)
      {
         if (isHex(chars_[idx_]))
         {
            string += chars_[idx_];
         }
         else if (chars_[idx_] == '>')
         {
            idx_++;
            break;
         }
         else if (!isWhiteSpace(chars_[idx_]))
         {
            return null;
         }
      }

      int n = string.length();

      if (n%2 == 1)
      {
         string += '0';
         n++;
      }

      char[] chars = new char[n/2];

      try
      {
         for (int i = 0, j = 0; i < n; i += 2, j++)
         {
            chars[j] = (char)Integer.parseInt(string.substring(i, 2), 16);
         }
      }
      catch (NumberFormatException e)
      {
         throw new InvalidFormatException("Can't parse hexadecimal string");
      }

      return new EPSString(chars);
   }

   public EPSName pstype()
   {
      return new EPSName("stringtype");
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

   public void makeEqual(EPSObject object)
   {
      EPSString str = (EPSString)object;

      if (chars_.length != str.chars_.length)
      {
         chars_ = new char[str.chars_.length];

      }

      for (int i = 0; i < chars_.length; i++)
      {
         chars_[i] = str.chars_[i];
      }

      hasReadAccess_ = str.hasReadAccess_;
      hasWriteAccess_ = str.hasWriteAccess_;
   }

   private char[] chars_;
   private int idx_=0;
   private boolean hasReadAccess_=true, hasWriteAccess_=true;
}
