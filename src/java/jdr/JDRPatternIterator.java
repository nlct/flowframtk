// File          : JDRPatternIterator.java
// Date          : 9th Sept 2010
// Last Modified : 8th April 2011
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

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

package com.dickimawbooks.jdr;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing an iterator for JDRPattern objects.
 * @author Nicola L C Talbot
 */

public class JDRPatternIterator extends JDRPathIterator 
   implements Serializable
{
   public JDRPatternIterator(JDRPattern path)
   {
      super(path);
   }

   public int pathSize()
   {
      return ((JDRPattern)path_).getTotalPathSegments();
   }


}
