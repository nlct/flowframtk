// File          : EPSReadable.java
// Purpose       : interface representing an EPS object that can have read access
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
 * The <code>EPSReadable</code> interface represents an 
 * Encapsulated PostScript object that can have read access.
 * @author Nicola L C Talbot
 */
public interface EPSReadable extends EPSObject
{
   /**
    * Determines whether this object has read access.
    * @return true if this has read access
    */
   public boolean hasReadAccess();

   /**
    * Sets the read access for this object.
    * @param access true if this object should have read access
    * @throws InvalidEPSObjectException if this object's read 
    * access can't be changed
    */
   public void setReadAccess(boolean access)
      throws InvalidEPSObjectException;
}
