// File          : JDRObjectLoader.java
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
 * Class dealing with saving and loading {@link JDRObject} objects 
 * to/from JDR and AJR files.
 * @author Nicola L C Talbot
 */
public class JDRObjectLoader
{
   /**
    * Creates new loader.
    */
   public JDRObjectLoader()
   {
      listeners_ = new Vector<JDRObjectLoaderListener>();
   }

   /**
    * Saves the given object in JDR/AJR format. This first writes the
    * ID character specified by 
    * {@link JDRObjectLoaderListener#getId(float)} and then 
    * writes the object specifications using 
    * {@link JDRObjectLoaderListener#write(JDRAJR,JDRObject)}.
    * Additionally writes flowframe, tag and description information if
    * the object is an instance of {@link JDRCompleteObject}.
    * @param theObject the object that needs to be saved
    * @throws IOException if an I/O error occurs
    * @see #load(JDRAJR)
    */
   public void save(JDRAJR jdr, JDRObject theObject)
      throws IOException
   {
      float version = jdr.getVersion();

      JDRObjectLoaderListener listener = theObject.getListener();

      if (listener == null)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.OBJECT_LISTENER,
            theObject.getClass().getName(), jdr);
      }

      JDRObject object = listener.getObject(jdr, theObject, version);

      if (object != theObject)
      {
         String name = theObject.getName();

         jdr.warningWithFallback(
            "warning.save_unsupported_object",
            "Object type ''{0}'' not supported in version {1}",
             name, version);

         save(jdr, object);

         return;
      }

      jdr.writeChar(listener.getId(version));
      listener.write(jdr, object);

      if (object instanceof JDRCompleteObject)
      {
         JDRCompleteObject obj = (JDRCompleteObject)object;

         FlowFrame flowframe = obj.getFlowFrame();

         if (flowframe == null)
         {
            jdr.writeBoolean(false);
         }
         else
         {
            jdr.writeBoolean(true);
            flowframe.save(jdr);
         }

         if (version >= 1.2f)
         {
            jdr.writeString(obj.getDescription());

            if (version >= 2.1f)
            {
               jdr.writeString(obj.getTag());
            }
         }
      }
   }

   /**
    * Loads an object specified in JDR/AJR format. This first reads a
    * character and checks through the list of listeners
    * to determine which type is identified by the  
    * character (using {@link JDRObjectLoaderListener#getId(float)}).
    * Additionally reads flowframe, tag and description information if
    * the object is an instance of {@link JDRCompleteObject}.
    * @return the specified object (maybe null if bitmap link is
    * invalid, in which case object needs to be discarded)
    * @throws InvalidFormatException if there is something wrong
    * with the format
    * @see #save(JDRAJR,JDRObject)
    * @see #addListener(JDRObjectLoaderListener)
    */
   public JDRObject load(JDRAJR jdr)
      throws InvalidFormatException
   {
      char c = jdr.readChar(InvalidFormatException.OBJECT_ID);
      float version = jdr.getVersion();

      for (Enumeration<JDRObjectLoaderListener> 
             e = listeners_.elements();e.hasMoreElements();)
      {
         JDRObjectLoaderListener listener = e.nextElement();

         if (listener.getId(version) == c)
         {
            JDRObject object = listener.read(jdr);

            // If object is null, it's a discarded bitmap
            // Need to read the flowframe, tag and description
            // even though they're not required.

            if (object == null || object instanceof JDRCompleteObject)
            {
               FlowFrame flowframe = null;

               if (jdr.readBoolean(InvalidFormatException.FRAME_FLAG))
               {
                  flowframe = FlowFrame.read(jdr);
               }

               String description = "";
               String tag = "";

               if (version >= 1.2f)
               {
                  description = jdr.readString(
                    InvalidFormatException.DESCRIPTION);

                  if (version >= 2.1f)
                  {
                     tag = jdr.readString(
                       InvalidFormatException.OBJECT_TAG);
                  }
               }

               if (object != null)
               {
                  JDRCompleteObject obj = (JDRCompleteObject)object;

                  obj.setFlowFrame(flowframe);
                  obj.setDescription(description);
                  obj.setTag(tag);
               }
            }

            return object;
         }
      }

      throw new InvalidValueException(
         InvalidFormatException.OBJECT_ID, c, jdr);
   }

   /**
    * Adds a new listener.
    * @param listener the new listener
    * @see #getListeners()
    */
   public void addListener(JDRObjectLoaderListener listener)
   {
      listeners_.add(listener);
   }

   /**
    * Gets all the listeners registered with this loader.
    * @return list of all listeners registered with this loader
    * @see #addListener(JDRObjectLoaderListener)
    */
   public Vector<JDRObjectLoaderListener> getListeners()
   {
      return listeners_;
   }

   private Vector<JDRObjectLoaderListener> listeners_;
}
