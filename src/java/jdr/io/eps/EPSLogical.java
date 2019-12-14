// File          : EPSLogical.java
// Purpose       : interface representing an EPS object that 
//                 can have logical  operations performed on it
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
 * The <code>EPSLogical</code> interface represents an 
 * Encapsulated PostScript object that 
 * can have logical operations performed on it
 * @author Nicola L C Talbot
 */
public interface EPSLogical extends EPSObject
{
   /**
    * And boolean/bitwise operation.
    * @param object other object
    * @return the result of this and the other
    * @throws InvalidEPSObjectException if the other object is
    * not compatible with this object
    */
   public EPSLogical and(EPSLogical object)
      throws InvalidEPSObjectException;

   /**
    * Or boolean/bitwise operation.
    * @param object other object
    * @return the result of this or the other
    * @throws InvalidEPSObjectException if the other object is
    * not compatible with this object
    */
   public EPSLogical or(EPSLogical object)
      throws InvalidEPSObjectException;

   /**
    * Xor boolean/bitwise operation.
    * @param object other object
    * @return the result of this xor the other
    * @throws InvalidEPSObjectException if the other object is
    * not compatible with this object
    */
   public EPSLogical xor(EPSLogical object)
      throws InvalidEPSObjectException;

   /**
    * Boolean/bitwise negation.
    * @return the negation of this
    */
   public EPSLogical not();
}
