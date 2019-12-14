// File          : EPSIndexComposite.java
// Purpose       : interface representing an EPS composite object
//                 that uses integer indices (such as arrays but not
//                 dictionaries)
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
 * Encapsulated PostScript composite object that uses an
 * integer index, such as arrays or strings.
 * @author Nicola L C Talbot
 */
public interface EPSIndexComposite extends EPSComposite
{
   /**
    * Creates a new object that shares a subinterval of this
    * composite object.
    * @param index the starting index
    * @param count the length of the subinterval
    * @throws NoReadAccessException if this object has no read
    * access
    * @throws InvalidEPSObjectException if this is a dictionary
    */
   public EPSComposite getInterval(int index, int count)
      throws NoReadAccessException,
             InvalidEPSObjectException;

   /**
    * Overwrites a subinterval of this composite object with
    * another composite object.
    * @param index the starting index
    * @param object the other object with which to overwrite the
    * subinterval
    * @throws NoReadAccessException if the other object has no read
    * access
    * @throws NoWriteAccessException if this object has no write
    * access
    * @throws InvalidEPSObjectException if the other object is not
    * the same class as this object
    */
   public void putInterval(int index, EPSComposite object)
      throws InvalidEPSObjectException,
             NoWriteAccessException,
             NoReadAccessException;
}
