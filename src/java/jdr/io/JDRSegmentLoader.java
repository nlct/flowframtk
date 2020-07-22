// File          : JDRSegmentLoader.java
// Creation Date : 29th February 2008
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
 * Class dealing with saving and loading {@link JDRPathSegment} segments 
 * to/from JDR and AJR files.
 * @author Nicola L C Talbot
 */
public class JDRSegmentLoader
{
   /**
    * Creates new loader.
    */
   public JDRSegmentLoader()
   {
      listeners_ = new Vector<JDRSegmentLoaderListener>();
   }

   /**
    * Saves the given segment in JDR/AJR format. This first writes the
    * ID character specified by 
    * {@link JDRSegmentLoaderListener#getId(float)} and then 
    * writes the segment specifications using 
    * {@link JDRSegmentLoaderListener#write(JDRAJR,JDRObject)}
    * @param theSegment the segment that needs to be saved
    * @throws IOException if an I/O error occurs
    * @see #load(JDRAJR,double,double)
    */
   public void save(JDRAJR jdr, JDRObject theSegment)
      throws IOException
   {
      float version = jdr.getVersion();

      JDRObjectLoaderListener listener 
         = ((JDRPathSegment)theSegment).getListener();

      if (listener == null)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.SEGMENT_LISTENER,
            theSegment.getClass().getName(), jdr);
      }

      JDRObject segment = listener.getObject(jdr, theSegment, version);

      if (segment != theSegment)
      {
         String name = theSegment.getName();

         jdr.warningMessage("Segment type ''{0}'' not supported in version {1}",
            "warning.save_unsupported_segment",
             name, version);

         save(jdr, segment);

         return;
      }

      jdr.writeChar(listener.getId(version));
      listener.write(jdr, segment);
   }

   /**
    * Loads an segment specified in JDR/AJR format. This first reads a
    * character and checks through the list of listeners
    * to determine which type is identified by the  
    * character (using {@link JDRSegmentLoaderListener#getId(float)})
    * @param x the current x coordinate
    * @param y the current y coordinate
    * @throws InvalidFormatException if there is something wrong
    * with the format
    * @see #save(JDRAJR,JDRObject)
    * @see #addListener(JDRSegmentLoaderListener)
    */
   public JDRObject load(JDRAJR jdr, double x, double y)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      char c = jdr.readChar(InvalidFormatException.SEGMENT_ID);

      for (Enumeration<JDRSegmentLoaderListener> 
             e = listeners_.elements();e.hasMoreElements();)
      {
         JDRSegmentLoaderListener listener = e.nextElement();

         if (listener.getId(version) == c)
         {
            return listener.read(jdr, x, y);
         }
      }

      throw new InvalidValueException(
         InvalidFormatException.SEGMENT_ID, c, jdr);
   }

   public JDRObject load(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      char c = jdr.readChar(InvalidFormatException.SEGMENT_ID);

      for (Enumeration<JDRSegmentLoaderListener> 
             e = listeners_.elements();e.hasMoreElements();)
      {
         JDRSegmentLoaderListener listener = e.nextElement();

         if (listener.getId(version) == c)
         {
            return listener.read(jdr);
         }
      }

      throw new InvalidValueException(
         InvalidFormatException.SEGMENT_ID, c, jdr);
   }

   /**
    * Adds a new listener.
    * @param listener the new listener
    * @see #getListeners()
    */
   public void addListener(JDRSegmentLoaderListener listener)
   {
      listeners_.add(listener);
   }

   /**
    * Gets all the listeners registered with this loader.
    * @return list of all listeners registered with this loader
    * @see #addListener(JDRSegmentLoaderListener)
    */
   public Vector<JDRSegmentLoaderListener> getListeners()
   {
      return listeners_;
   }

   private Vector<JDRSegmentLoaderListener> listeners_;
}
