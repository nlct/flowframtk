// File          : JDRPaintLoader.java
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
 * Class dealing with saving and loading {@link JDRPaint} objects 
 * to/from JDR and AJR files.
 * @author Nicola L C Talbot
 */
public class JDRPaintLoader
{
   /**
    * Creates new loader.
    */
   public JDRPaintLoader()
   {
      listeners_ = new Vector<JDRPaintLoaderListener>();
   }

   /**
    * Saves the given paint in JDR format. This first writes the
    * ID character specified by 
    * {@link JDRPaintLoaderListener#getId(float)} and then 
    * writes the paint specifications using 
    * {@link JDRPaintLoaderListener#write(JDRAJR,JDRPaint)}
    * @param thePaint the paint that needs to be saved
    * @throws IOException if an I/O error occurs
    * @see #load(JDRAJR)
    */
   public void save(JDRAJR jdr, JDRPaint thePaint)
      throws IOException
   {
      float version = jdr.getVersion();

      if (thePaint == null)
      {
         jdr.writeChar('T');
         return;
      }

      JDRPaintLoaderListener listener = thePaint.getListener();

      if (listener == null)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.PAINT_LISTENER,
            thePaint.getClass().getName(), jdr);
      }

      JDRPaint paint = listener.getPaint(jdr, thePaint, version);

      if (paint != thePaint)
      {
         String name = thePaint.getName();

         jdr.warning("warning.save_unsupported_paint",
            new String[] {name, ""+version},
            String.format("Paint type '%s' not supported in version %f",
             name, version));

         save(jdr, paint);

         return;
      }

      jdr.writeChar(listener.getId(version));
      listener.write(jdr, paint);
   }

   /**
    * Loads a paint specified in JDR/AJR format. This first reads a
    * character and checks through the list of paint listeners
    * to determine which paint type is identified by the  
    * character (using {@link JDRPaintLoaderListener#getId(float)})
    * @return the specified paint
    * @throws InvalidFormatException if there is something wrong
    * with the paint format
    * @see #save(JDRAJR,JDRPaint)
    * @see #addListener(JDRPaintLoaderListener)
    */
   public JDRPaint load(JDRAJR jdr)
      throws InvalidFormatException
   {
      char c = jdr.readChar(InvalidFormatException.PAINT_ID);

      float version = jdr.getVersion();

      for (Enumeration<JDRPaintLoaderListener> 
             e = listeners_.elements();e.hasMoreElements();)
      {
         JDRPaintLoaderListener listener = e.nextElement();

         if (listener.getId(version) == c)
         {
            return listener.read(jdr);
         }
      }

      throw new InvalidValueException(
        InvalidFormatException.PAINT_ID, c, jdr);
   }


   /**
    * Gets string describing paint for FlowframTk's configuration file.
    * This returns the paint ID as specified by
    * {@link JDRPaintLoaderListener#getConfigId()}
    * optionally followed by a comma and
    * {@link JDRPaintLoaderListener#getConfigString(JDRPaint)}.
    * If {@link JDRPaintLoaderListener#getConfigString(JDRPaint)}
    * returns an empty string, only 
    * {@link JDRPaintLoaderListener#getConfigId()}
    * is returned.
    * @param paint the paint to describe
    */
   public String getConfigString(JDRPaint paint)
   {
      int id = paint.getListener().getConfigId();
      String config = paint.getListener().getConfigString(paint);

      if (config.equals(""))
      {
         return ""+id;
      }

      return ""+id+","+config;
   }

   /**
    * Parses configuration paint specs. The specs string must 
    * start with an integer identifying the paint (as specified
    * by {@link JDRPaintLoaderListener#getConfigId()}).
    */
   public JDRPaint parseConfig(CanvasGraphics cg, String specs)
      throws InvalidFormatException
   {
      String[] split = specs.split(",", 2);

      int id = 0;

      try
      {
         id = Integer.parseInt(split[0]);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PAINT_ID, split[0], cg, e);
      }

      String paintspecs = (split.length < 2 ? "" : split[1]);

      for (Enumeration<JDRPaintLoaderListener> 
             e = listeners_.elements();e.hasMoreElements();)
      {
         JDRPaintLoaderListener listener = e.nextElement();

         if (listener.getConfigId() == id)
         {
            JDRPaint paint = listener.parseConfig(cg, paintspecs);

            remainder = listener.getConfigRemainder();

            return paint;
         }
      }

      remainder = paintspecs;

      throw new InvalidValueException(
         InvalidFormatException.PAINT_ID, id, cg);
   }

   /**
    * Gets the remainder of the specs String after it has been
    * parsed by {@link #parseConfig(String)}.
    */
   public String getConfigRemainder()
   {
      return remainder;
   }

   /**
    * Adds a new paint listener to this loader.
    * @param listener the new listener to add
    */
   public void addListener(JDRPaintLoaderListener listener)
   {
      listeners_.add(listener);
   }

   /**
    * Gets a list of the paint listeners that have been add
    * to this loader.
    * @return list of paint listeners associated with this loader
    */
   public Vector<JDRPaintLoaderListener> getListeners()
   {
      return listeners_;
   }

   private Vector<JDRPaintLoaderListener> listeners_;

   private String remainder="";
}
