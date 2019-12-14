// File          : JDRGridLoader.java
// Creation Date : 17th August 2010
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

package com.dickimawbooks.jdr.io;

import java.io.*;
import java.util.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class dealing with saving and loading {@link JDRGrid} objects 
 * to/from JDR and AJR files.
 * @author Nicola L C Talbot
 */
public class JDRGridLoader
{
   /**
    * Creates new loader.
    */
   public JDRGridLoader()
   {
      listeners_ = new Vector<JDRGridLoaderListener>();
   }

   /**
    * Saves the given grid in JDR/AJR format. This first writes the
    * ID specified by 
    * {@link JDRGridLoaderListener#getId(float)} and then 
    * writes the grid specifications using 
    * {@link JDRGridLoaderListener#write(JDRAJR,JDRGrid)}.
    * @param theGrid the grid that needs to be saved
    * @throws IOException if an I/O error occurs
    * @see #load(JDRAJR)
    */
   public void save(JDRAJR jdr, JDRGrid theGrid)
      throws IOException
   {
      float version = jdr.getVersion();

      JDRGridLoaderListener listener = theGrid.getListener();

      if (listener == null)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.GRID_LISTENER,
           theGrid.getClass().getName(), jdr);
      }

      JDRGrid grid = listener.getGrid(jdr, theGrid, version);

      if (grid != theGrid)
      {
         String name = theGrid.getName();

         jdr.warning("warning.save_unsupported_grid",
            new String[] {name, ""+version},
            "Grid type '"+name+"' not supported in version "+version);

         save(jdr, grid);

         return;
      }

      jdr.writeByte(listener.getId(version));
      listener.write(jdr, grid);
   }

   /**
    * Loads a grid specified in JDR/AJR format. This first reads a
    * byte and checks through the list of listeners
    * to determine which type is identified by the  
    * number (using {@link JDRGridLoaderListener#getId(float)}).
    * @throws InvalidFormatException if there is something wrong
    * with the format
    * @see #saveJDR(JDRAJR,JDRGrid)
    * @see #addListener(JDRGridLoaderListener)
    */
   public JDRGrid load(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      byte id = jdr.readByte(InvalidFormatException.GRID_ID);

      for (Enumeration<JDRGridLoaderListener> 
             e = listeners_.elements();e.hasMoreElements();)
      {
         JDRGridLoaderListener listener = e.nextElement();

         if (listener.getId(version) == id)
         {
            return listener.read(jdr);
         }
      }

      throw new InvalidValueException(
         InvalidFormatException.GRID_ID, id, jdr);
   }

   /**
    * Adds a new listener.
    * @param listener the new listener
    * @see #getListeners()
    */
   public void addListener(JDRGridLoaderListener listener)
   {
      listeners_.add(listener);
   }

   /**
    * Gets all the listeners registered with this loader.
    * @return list of all listeners registered with this loader
    * @see #addListener(JDRGridLoaderListener)
    */
   public Vector<JDRGridLoaderListener> getListeners()
   {
      return listeners_;
   }

   private Vector<JDRGridLoaderListener> listeners_;
}
