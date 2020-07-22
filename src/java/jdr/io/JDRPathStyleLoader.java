// File          : JDRPathStyleLoader.java
// Creation Date : 29th August 2010
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class dealing with saving and loading path styles (line/fill
 * paint and stroke) to/from JDR and AJR files. This doesn't include
 * the segments that make up the path or the open/close attribute.
 * @author Nicola L C Talbot
 */
public class JDRPathStyleLoader
{
   /**
    * Creates new loader.
    */
   public JDRPathStyleLoader()
   {
      listeners_ = new Vector<JDRPathStyleListener>();
   }

   /**
    * Saves the given path styles in JDR/AJR format. This first writes the
    * ID character specified by 
    * {@link JDRPathStyleListener#getId(float)} and then 
    * writes the specifications using 
    * {@link JDRPathStyleListener#write(JDRAJR,JDRShape)}.
    * @param theShape the shape whose style needs to be saved
    * @throws IOException if an I/O error occurs
    * @see #load(JDRAJR,JDRShape)
    */
   public void save(JDRAJR jdr, JDRShape theShape)
      throws IOException
   {
      float version = jdr.getVersion();

      JDRPathStyleListener listener = theShape.getPathStyleListener();

      if (listener == null)
      {
         throw new JdrIllegalArgumentException(
          JdrIllegalArgumentException.PATH_STYLE_LISTENER,
            theShape.getClass().getName(), jdr);
      }

      JDRShape shape = listener.getShape(jdr, theShape, version);

      if (shape != theShape)
      {
         String name = theShape.getName();

         jdr.warningMessage("Path style ''{0}'' not supported in version {1}",
            "warning.save_unsupported_path_style",
             name, version);

         save(jdr, shape);

         return;
      }


      jdr.writeByte(listener.getId(version));

      listener.write(jdr,shape);
   }

   /**
    * Loads a path style specified in JDR/AJR format. This first reads a
    * byte and checks through the list of listeners
    * to determine which type is identified by that  
    * byte (using {@link JDRPathStyleListener#getId(float)}).
    * @param shape the shape whose style needs to be set
    * @throws InvalidFormatException if there is something wrong
    * with the format
    * @see #save(JDRAJR,JDRShape)
    * @see #addListener(JDRPathStyleListener)
    */
   public void load(JDRAJR jdr, JDRShape shape)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      byte id = jdr.readByte(InvalidFormatException.PATH_STYLE_ID);

      for (Enumeration<JDRPathStyleListener> 
             e = listeners_.elements();e.hasMoreElements();)
      {
         JDRPathStyleListener listener = e.nextElement();

         if (listener.getId(version) == id)
         {
            listener.read(jdr, shape);

            return;
         }
      }

      throw new InvalidValueException(
        InvalidFormatException.PATH_STYLE_ID, id, jdr);
   }

   /**
    * Adds a new listener.
    * @param listener the new listener
    * @see #getListeners()
    */
   public void addListener(JDRPathStyleListener listener)
   {
      listeners_.add(listener);
   }

   /**
    * Gets all the listeners registered with this loader.
    * @return list of all listeners registered with this loader
    * @see #addListener(JDRPathStyleListener)
    */
   public Vector<JDRPathStyleListener> getListeners()
   {
      return listeners_;
   }

   private Vector<JDRPathStyleListener> listeners_;
}
