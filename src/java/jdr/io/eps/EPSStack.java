// File          : EPSStack.java
// Purpose       : class representing an EPS stack
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
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing an EPS stack.
 * @author Nicola L C Talbot
 */
public class EPSStack extends Vector<EPSObject>
{
   /**
    * Initialise stack using given {@link EPS} object.
    */
   public EPSStack(EPS eps)
   {
      super();
      eps_ = eps;

      userdict = new EPSDict();
      statusdict = new EPSDict();

      dictionaries = new Vector<EPSDict>();

      dictionaries.add(eps.getSystemDict());
      dictionaries.add(eps.getGlobalDict());
      dictionaries.add(userdict);
   }

   /**
    * Gets the current dictionary. (The last element on the
    * dictionary stack.
    * @return the current dictionary
    */
   public EPSDict getCurrentDictionary()
   {
      return dictionaries.lastElement();
   }

   /**
    * Pushes given objects onto this stack.
    */
   public void pushObjects(Vector<EPSObject> objects)
   {
      for (int i = 0; i < objects.size(); i++)
      {
         add(objects.get(i));
      }
   }

   /**
    * Converts given string into an {@link EPSString} and pushes onto
    * the stack.
    * @param string the string to be pushed onto the stack
    */
   public void pushString(String string)
   {
      add(new EPSString(string));
   }

   /**
    * Converts given value into an {@link EPSInteger} and pushes onto
    * the stack.
    * @param value the value to push onto the stack
    */
   public void pushInteger(int value)
   {
      add(new EPSInteger(value));
   }

   /**
    * Converts given value into an {@link EPSLong} and pushes onto
    * the stack.
    * @param value the value to push onto the stack
    */
   public void pushLong(long value)
   {
      add(new EPSLong(value));
   }

   /**
    * Converts given value into an {@link EPSLong} and pushes onto
    * the stack.
    * @param value the value to push onto the stack
    */
   public void pushLong(int value)
   {
      add(new EPSLong((long)value));
   }

   /**
    * Converts given value into an {@link EPSDouble} and pushes onto
    * the stack.
    * @param value the value to push onto the stack
    */
   public void pushDouble(double value)
   {
      add(new EPSDouble(value));
   }

   /**
    * Converts given value into an {@link EPSBoolean} and pushes onto
    * the stack.
    * @param value the value to push onto the stack
    */
   public void pushBoolean(boolean value)
   {
      add(new EPSBoolean(value));
   }

   /**
    * Converts given value into an {@link EPSArray} and pushes onto
    * the stack.
    * @param array the array to push onto the stack
    */
   public void pushArray(double[] array)
   {
      add(new EPSArray(array));
   }

   /**
    * Pops a composite object from the stack and returns.
    * @return the popped composite object
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSComposite</code>
    * @see #popObject()
    */
   public EPSComposite popEPSComposite()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSComposite)
      {
         return (EPSComposite)lastElement;
      }
      else
      {
         throw new InvalidEPSObjectException("Composite expected",
            eps_.getLineNum());
      }
   }

   /**
    * Pops an indexed composite object from the stack and returns.
    * @return the popped composite object
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSIndexComposite</code>
    * @see #popObject()
    */
   public EPSIndexComposite popEPSIndexComposite()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSIndexComposite)
      {
         return (EPSIndexComposite)lastElement;
      }
      else
      {
         throw new InvalidEPSObjectException(
            "Indexed composite expected",
            eps_.getLineNum());
      }
   }

   /**
    * Pops a saved state from the stack and returns.
    * @return the popped object
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSSaveState</code>
    * @see #popObject()
    */
   public EPSSaveState popEPSSaveState()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSSaveState)
      {
         return (EPSSaveState)lastElement;
      }
      else
      {
         throw new InvalidEPSObjectException("Saved state expected",
            eps_.getLineNum());
      }
   }

   /**
    * Pops a readable object from the stack and returns.
    * @return the popped readable object
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSReadable</code>
    * @see #popObject()
    */
   public EPSReadable popEPSReadable()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSReadable)
      {
         return (EPSReadable)lastElement;
      }
      else
      {
         throw new InvalidEPSObjectException(
            "Readable object expected",
            eps_.getLineNum());
      }
   }

   /**
    * Pops a writeable object from the stack and returns.
    * @return the popped writeable object
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSWriteable</code>
    * @see #popObject()
    */
   public EPSWriteable popEPSWriteable()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSWriteable)
      {
         return (EPSWriteable)lastElement;
      }
      else
      {
         throw new InvalidEPSObjectException(
            "Writeable object expected",
            eps_.getLineNum());
      }
   }

   /**
    * Pops a relational object from the stack and returns.
    * @return the popped relational object
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSRelational</code>
    * @see #popObject()
    */
   public EPSRelational popEPSRelational()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSRelational)
      {
         return (EPSRelational)lastElement;
      }
      else
      {
         throw new InvalidEPSObjectException(
            "Relational object expected",
            eps_.getLineNum());
      }
   }

   /**
    * Pops a logical object from the stack and returns.
    * @return the popped logical object
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSLogical</code>
    * @see #popObject()
    */
   public EPSLogical popEPSLogical()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSLogical)
      {
         return (EPSLogical)lastElement;
      }
      else
      {
         throw new InvalidEPSObjectException(
            "Logical object expected",
            eps_.getLineNum());
      }
   }

   /**
    * Pops a PostScript procedure from the stack and returns.
    * @return the popped procedure
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSProc</code>
    * @see #popObject()
    */
   public EPSProc popEPSProc()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSProc)
      {
         return (EPSProc)lastElement;
      }
      else
      {
         throw new InvalidEPSObjectException("Procedure expected");
      }
   }

   /**
    * Pops a graphics state from the stack and returns.
    * @return the popped graphics state
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>GraphicsState</code>
    * @see #popObject()
    */
   public GraphicsState popGraphicsState()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof GraphicsState)
      {
         return (GraphicsState)lastElement;
      }
      else
      {
         throw new InvalidEPSObjectException(
            "Graphics state expected", eps_.getLineNum());
      }
   }

   /**
    * Pops the top most element from the stack (which must be
    * an <code>EPSName</code>) and returns.
    * @return the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSName</code>
    * @throws EmptyStackException if this stack is empty
    * @see #popObject()
    */
   public EPSName popEPSName()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSName)
      {
         return (EPSName)lastElement;
      }
      else
      {
         throw new InvalidEPSObjectException(
            "Invalid parameter (name expected "
            +lastElement.pstype()+" found)", eps_.getLineNum());
      }
   }

   /**
    * Pops the top most element from the stack (which must be
    * an <code>EPSString</code>) and returns as a <code>String</code>.
    * @return the value of the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSString</code>
    * @throws EmptyStackException if this stack is empty
    * @see #popObject()
    * @see #popEPSString()
    */
   public String popString()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      return popEPSString().value();
   }

   /**
    * Pops the top most element from the stack (which must be
    * an <code>EPSString</code>) and returns.
    * @return the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSString</code>
    * @throws EmptyStackException if this stack is empty
    * @see #popObject()
    * @see #popString()
    */
   public EPSString popEPSString()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSString)
      {
         return (EPSString)lastElement;
      }
      else
      {
         throw new InvalidEPSObjectException("EPSString expected, "
            +lastElement.getClass()+" found", eps_.getLineNum());
      }
   }

   /**
    * Pops the top most element from the stack (which must be
    * an <code>EPSNumber</code>) and returns.
    * @return the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSNumber</code>
    * @throws EmptyStackException if this stack is empty
    * @see #popObject()
    * @see #popDouble()
    * @see #popInteger()
    * @see #popLong()
    */
   public EPSNumber popNumber()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSNumber)
      {
         return (EPSNumber)lastElement;
      }
      else
      {
         throw new InvalidEPSObjectException(
            "Number expected", eps_.getLineNum());
      }
   }

   /**
    * Pops the top most element from the stack (which must be
    * an <code>EPSNumber</code>) and returns its double precision
    * value.
    * @return the value of the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSNumber</code>
    * @throws EmptyStackException if this stack is empty
    * @see #popObject()
    * @see #popNumber()
    * @see #popInteger()
    * @see #popLong()
    */
   public double popDouble()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      return popNumber().doubleValue();
   }

   /**
    * Pops the top most element from the stack (which must be
    * an <code>EPSNumber</code>) and returns its integer value.
    * @return the value of the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSNumber</code>
    * @throws EmptyStackException if this stack is empty
    * @see #popObject()
    * @see #popNumber()
    * @see #popDouble()
    * @see #popLong()
    */
   public int popInteger()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      return popNumber().intValue();
   }

   /**
    * Pops the top most element from the stack (which must be
    * an <code>EPSNumber</code>) and returns its long integer value.
    * @return the value of the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSNumber</code>
    * @throws EmptyStackException if this stack is empty
    * @see #popObject()
    * @see #popNumber()
    * @see #popDouble()
    * @see #popInteger()
    */
   public long popLong()
      throws InvalidEPSObjectException,
             EmptyStackException
   {
      return popNumber().longValue();
   }

   /**
    * Pops the top most element from the stack (which must be
    * a transformation matrix) and returns it as an array
    * of type <code>double[]</code>.
    * @return the value of the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSArray</code>
    * @throws NotMatrixException if popped object is not a 
    * transformation matrix
    * @throws EmptyStackException if this stack is empty
    * @see EPSArray#isMatrix()
    * @see #popEPSMatrix()
    * @see #popEPSArray()
    * @see #popPackedArray()
    * @see #popDoubleArray()
    * @see #popObject()
    */
   public double[] popMatrix()
      throws InvalidEPSObjectException,NotMatrixException,
             EmptyStackException
   {
      EPSArray array = popEPSArray();

      return array.getMatrix();
   }

   /**
    * Pops the top most element from the stack (which must be
    * a transformation matrix) and returns it.
    * @return the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSArray</code>
    * @throws NotMatrixException if array is not a transformation
    * matrix.
    * @throws EmptyStackException if this stack is empty
    * @see EPSArray#isMatrix()
    * @see #popEPSArray()
    * @see #popMatrix()
    * @see #popPackedArray()
    * @see #popDoubleArray()
    * @see #popObject()
    */
   public EPSArray popEPSMatrix()
      throws InvalidEPSObjectException,
             NotMatrixException,
             EmptyStackException
   {
      EPSArray array = popEPSArray();

      if (array.isMatrix())
      {
         return array;
      }

      throw new InvalidEPSObjectException(
         "matrix expected", eps_.getLineNum());
   }

   /**
    * Pops the top most element from the stack (which must be
    * an <code>EPSArray</code>) and returns it as an array
    * of type <code>double[]</code>.
    * @return the value of the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSArray</code>
    * @throws InvalidFormatException if one or more of the
    * popped array's elements don't implement <code>EPSNumber</code>
    * @throws EmptyStackException if this stack is empty
    * @see EPSArray#getDoubleArray()
    * @see #popEPSArray()
    * @see #popEPSMatrix()
    * @see #popMatrix()
    * @see #popPackedArray()
    * @see #popObject()
    */
   public double[] popDoubleArray()
      throws InvalidEPSObjectException,InvalidFormatException,
             EmptyStackException
   {
      EPSArray object = popEPSArray();

      return object.getDoubleArray();
   }

   /**
    * Pops the top most element from the stack (which must be
    * an <code>EPSArray</code>) and returns popped object.
    * @return the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSArray</code>
    * @throws EmptyStackException if this stack is empty
    * @see #popEPSMatrix()
    * @see #popMatrix()
    * @see #popPackedArray()
    * @see #popDoubleArray()
    * @see #popObject()
    */
   public EPSArray popEPSArray()
      throws InvalidEPSObjectException,EmptyStackException
   {
      EPSObject object = popObject();

      if (object instanceof EPSArray)
      {
         return (EPSArray)object;
      }

      throw new InvalidEPSObjectException(
         "Array expected", eps_.getLineNum());
   }

   /**
    * Pops the top most element from the stack (which must be
    * an <code>EPSArray</code> without write access) 
    * and returns popped object.
    * @return the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSArray</code> or has write access
    * @throws EmptyStackException if this stack is empty
    * @see #popEPSArray()
    * @see #popEPSMatrix()
    * @see #popMatrix()
    * @see #popDoubleArray()
    * @see #popObject()
    */
   public EPSPackedArray popPackedArray()
      throws InvalidEPSObjectException,EmptyStackException
   {
      EPSArray array = popEPSArray();

      if (array instanceof EPSPackedArray)
      {
         return (EPSPackedArray)array;
      }

      throw new InvalidEPSObjectException(
         "Packed array expected", eps_.getLineNum());
   }

   /**
    * Pops the top most element from the stack (which must be
    * an instance of <code>EPSBoolean</code>) and returns the
    * popped object.
    * @return the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSBoolean</code>
    * @throws EmptyStackException if this stack is empty
    * @see #popObject()
    */
   public EPSBoolean popBoolean()
      throws InvalidEPSObjectException,EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSBoolean)
      {
         return (EPSBoolean)lastElement;
      }

      throw new InvalidEPSObjectException(
         "Boolean expected", eps_.getLineNum());
   }

   /**
    * Pops the top most element from the stack (which must be
    * an instance of <code>EPSFont</code>) and returns the
    * popped object.
    * @return the popped element
    * @throws InvalidEPSObjectException if the popped object is not
    * an instance of <code>EPSFont</code>
    * @throws EmptyStackException if this stack is empty
    * @see #popObject()
    */
   public EPSFont popFont()
      throws InvalidEPSObjectException,EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSFont)
      {
         return (EPSFont)lastElement;
      }

      throw new InvalidEPSObjectException(
         "Font expected", eps_.getLineNum());
   }

   /**
    * Pops the top most element from the stack (which must be
    * an instance of <code>EPSDict</code>) and returns the
    * popped object.
    * @return the popped element
    * @throws InvalidFormatException if the popped object is not
    * an instance of <code>EPSDictionaryInterface</code>
    * @throws EmptyStackException if this stack is empty
    * @see #popObject()
    */
   public EPSDict popDict()
      throws InvalidEPSObjectException,EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSDictionaryInterface)
      {
         return (EPSDict)lastElement;
      }

      throw new InvalidEPSObjectException("Dictionary expected",
         eps_.getLineNum());
   }

   /**
    * Pops the top most element from the stack (which must be
    * an instance of <code>EPSMark</code>) and returns the
    * popped object.
    * @return the popped element
    * @throws InvalidFormatException if the popped object is not
    * an instance of <code>EPSMark</code>
    * @throws EmptyStackException if this stack is empty
    * @see #popObject()
    */
   public EPSMark popEPSMark()
      throws InvalidEPSObjectException,EmptyStackException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSMark)
      {
         return (EPSMark)lastElement;
      }

      throw new InvalidEPSObjectException("Mark expected",
         eps_.getLineNum());
   }

   /**
    * Pops the top most element from the stack (which must be
    * an instance of <code>EPSFile</code> or <code>EPSFilter</code>)
    * and returns the popped object.
    * @return the popped element
    * @throws InvalidFormatException if the popped object is a
    * filter and encounters invalid data
    * @throws NonInvertibleTransformException if the popped object
    * is a filter and encounters a non invertible transform
    * @see #popObject()
    */
   public EPSFile popEPSFile()
      throws InvalidFormatException,NoninvertibleTransformException,
             IOException
   {
      EPSObject lastElement = popObject();

      if (lastElement instanceof EPSFile)
      {
         return (EPSFile)lastElement;
      }
      else if (lastElement instanceof EPSFilter)
      {
         return ((EPSFilter)lastElement).getFile(this);
      }

      throw new InvalidEPSObjectException("File expected",
         eps_.getLineNum());
   }

   /**
    * Pops the topmost object of this stack, and returns 
    * popped object.
    * @return the popped object
    * @throws EmptyStackException if this stack is empty
    */
   public EPSObject popObject() throws EmptyStackException
   {
      int n = size();

      if (n == 0)
      {
         throw new EmptyStackException(eps_.getLineNum());
      }

      EPSObject object = remove(n-1);

      return object;
   }

   public boolean add(EPSObject object)
   {
      if (object == null)
      {
         return super.add(new EPSNull());
      }
      else
      {
         return super.add(object);
      }
   }

   /**
    * Adds a dictionary to the end of the dictionary stack.
    * @param dict the dictionary to add to the dictionary stack
    * @see #popFromDictStack()
    */
   public void pushToDictStack(EPSDict dict)
   {
      dictionaries.add(dict);
   }

   /**
    * Pops last dictionary off from the dictionary stack.
    * @return popped dictionary
    * @see #pushToDictStack(EPSDict)
    */
   public EPSDict popFromDictStack()
   {
      return dictionaries.remove(dictionaries.size()-1);
   }

   /**
    * Gets the size of the dictionary stack.
    * @return size of the dictionary stack
    */
   public int getDictStackSize()
   {
      return dictionaries.size();
   }

   /**
    * Gets the dictionary at given index in dictionary stack.
    * @param index the required index
    * @return the dictionary at the given index
    */
   public EPSDict getDict(int index)
   {
      return dictionaries.get(index);
   }

   /**
    * Processes the given object. If the given object is an instance
    * of EPSName, passes the object to {@link #process(EPSName)},
    * if object is an instance of EPSOperator, executes it,
    * otherwise adds the object to the stack.
    * @param object the object to process
    */
   public void processObject(EPSObject object)
   throws InvalidFormatException,
      NoninvertibleTransformException,
      IOException
   {
      if (object instanceof EPSName)
      {
         process((EPSName)object);
      }
      else if (object instanceof EPSOperator)
      {
         ((EPSOperator)object).execute(this, eps_);
      }
      else
      {
         add(object);
      }
   }

   /**
    * If the given object is an instance of EPSProc, it is
    * passed to {@link #execProc(EPSProc)} otherwise it is
    * passed to {@link #processObject(EPSObject)}
    */
   public void execObject(EPSObject object)
   throws InvalidFormatException,
      NoninvertibleTransformException,
      IOException
   {
      if (object instanceof EPSProc)
      {
         execProc((EPSProc)object);
      }
      else if (object instanceof EPSFile)
      {
         ((EPSFile)object).execute(eps_);
      }
      else if (object instanceof EPSFilter)
      {
         EPSFile file = ((EPSFilter)object).getFile(this);

         file.execute(eps_);
      }
      else
      {
         processObject(object);
      }
   }

   /**
    * Processes each element of the given procedure.
    * @param proc the procedure to execute
    */
   public void execProc(EPSProc proc)
   throws InvalidFormatException,
      NoninvertibleTransformException,
      IOException
   {
      if (proc.size() == 0)
      {
         return;
      }

      for (Enumeration<EPSObject> en=proc.elements();
           en.hasMoreElements();)
      {
         EPSObject element = en.nextElement();
         processObject(element);

         if (exit_) break;
      }
   }

   /**
    * Processes the given PostScript command.
    * @param command
    */
   public void process(EPSName command)
   throws InvalidFormatException,
      NoninvertibleTransformException,
      IOException
   {
      String string = command.toString();

      if (string.startsWith("/"))
      {
         add(command);
      }
      else if (string.equals("systemdict"))
      {
         add(eps_.getSystemDict());
      }
      else if (string.equals("userdict"))
      {
         add(userdict);
      }
      else if (string.equals("statusdict"))
      {
         add(statusdict);
      }
      else
      {
         // go through each dictionary to see if the command is
         // defined

         for (int i = dictionaries.size()-1; i >= 0; i--)
         {
            EPSDict dict = dictionaries.get(i);

            EPSObject value = dict.get(string);

            if (value != null)
            {
               if (value instanceof EPSName)
               {
                  process((EPSName)value);
               }
               else if (value instanceof EPSOperator)
               {
                  ((EPSOperator)value).execute(this, eps_);
               }
               else if (value instanceof EPSProc)
               {
                  execProc((EPSProc)value);
               }
               else
               {
                  add(value);
               }
               return;
            }
         }

         throw new InvalidFormatException(
            "unknown command '"+string+"'", eps_.getLineNum());
      }
   }

   /**
    * Sets the exit status.
    * @param status the exit status
    * @see #getExitStatus()
    */
   public void setExitStatus(boolean status)
   {
      exit_ = status;
   }

   /**
    * Gets the exit status.
    * @return exit status
    * @see #setExitStatus(boolean)
    */
   public boolean getExitStatus()
   {
      return exit_;
   }

   /**
    * Sets stopped status.
    * @param status the stop status
    * @see #getStopStatus()
    */
   public void setStopStatus(boolean status)
   {
      stop_ = status;
   }

   /**
    * Gets stopped status.
    * @return stopped status
    * @see #setStopStatus(boolean)
    */
   public boolean getStopStatus()
   {
      return stop_;
   }

   public void printStack()
   {
      for (int i = size()-1; i >= 0; i--)
      {
         eps_.getMessageSystem().getPublisher().publishMessages(
           MessageInfo.createMessage(get(i).toString()));
      }
   }

   private EPS eps_;
   private Vector<EPSDict> dictionaries;
   private boolean exit_=false;
   private boolean stop_=false;

   private EPSDict userdict, statusdict;
}
