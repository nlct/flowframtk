// File          : EPSComposite.java
// Purpose       : interface representing an EPS composite object
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
 * The <code>EPSComposite</code> interface represents an 
 * Encapsulated PostScript composite object.
 * @author Nicola L C Talbot
 */
public interface EPSComposite extends EPSObject
{
   /**
    * Copies object into this.
    * @param object the object to be copied into this
    * @throws InvalidEPSObjectException if <code>object</code>
    * isn't of the same class
    * @throws NoWriteAccessException if this has no
    * write access
    */
   public void copy(EPSObject object)
      throws InvalidEPSObjectException, NoWriteAccessException;

   /**
    * Returns the length of this object.
    * @return the length of this object
    */
   public int length();

   /**
    * Gets an element of this object specified by <code>index</code>.
    * @param index the means of identifying the required element
    * @return the required element
    * @throws InvalidEPSObjectException if <code>index</code> is an
    * invalid object type
    * @throws NoReadAccessException if this object does not
    * have read access
    * @see #put(EPSObject,EPSObject)
    */
   public EPSObject get(EPSObject index)
   throws InvalidEPSObjectException,NoReadAccessException;

   /**
    * Puts an element into this object using the given 
    * <code>index</code>.
    * @param index the means of identifying the required element
    * @param value the element to put into this object
    * @throws InvalidEPSObjectException <code>index</code> is an
    * invalid object type
    * @throws NoWriteAccessException if this object doesn't
    * have write access
    * @see #get(EPSObject)
    */
   public void put(EPSObject index, EPSObject value)
      throws InvalidEPSObjectException,
             NoWriteAccessException;

   /**
    * Accesses all elements in sequence, calling a procedure for
    * each one.
    * @param stack the stack
    * @param proc the procedure
    */
   public void forall(EPSStack stack, EPSProc proc)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException;

   public Object clone();

   public void makeEqual(EPSObject object);
}
