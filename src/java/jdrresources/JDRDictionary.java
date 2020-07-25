// File          : JDRDictionary.java
// Purpose       : Dictionary containing language resources
// Creation Date : 4th June 2008
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

package com.dickimawbooks.jdrresources;

import java.io.*;
import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.text.MessageFormat;
import java.text.ChoiceFormat;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.basic.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;
import com.dickimawbooks.jdrresources.filter.*;

/**
 * Dictionary containing FlowframTk's language resources.
 * @author Nicola L C Talbot
 */

public class JDRDictionary 
 implements JDRMessageDictionary,Serializable
{
   /**
    * Creates a new instance of the language set.
    * @param appClass the application class
    */
   public JDRDictionary(JDRResources resources)
      throws IOException
   {
      InputStream dictionaryInputStream 
         = getDictionaryInputStream(resources);;

      InputStream licenceInputStream 
         = resources.getClass().getResourceAsStream(
            "/resources/LICENSE");

      if (licenceInputStream == null)
      {
         throw new IOException(
            "Unable to open 'resources/LICENSE'");
      }

      loadInputStreams(resources, dictionaryInputStream,
            licenceInputStream);
   }

   private InputStream getDictionaryInputStream(JDRResources resources)
      throws IOException
   {
      String name = "/resources/dictionaries/flowframtk";

      InputStream dictionaryInputStream;
      String filename;

      if (resources.getDictLocaleId() != null)
      {
         filename = name+"-"+resources.getDictLocaleId()+".prop";

         dictionaryInputStream 
            = resources.getClass().getResourceAsStream(filename);

         if (dictionaryInputStream == null)
         {
            resources.warning("No dictionary available for language '"
               +resources.getDictLocaleId()+"'");
            resources.setDictLocaleId(null);
         }

         return dictionaryInputStream;
      }

      Locale locale = Locale.getDefault();

      String id = locale.getLanguage()+"-"+locale.getCountry();

      filename = name+"-"+id+".prop";

      resources.setDictLocaleId(id);

      dictionaryInputStream 
         = resources.getClass().getResourceAsStream(filename);

      if (dictionaryInputStream == null)
      {
         String tried = filename;

         resources.setDictLocaleId(locale.getLanguage());

         filename = name+"-"+resources.getDictLocaleId()+".prop";

         dictionaryInputStream 
           = resources.getClass().getResourceAsStream(filename);

         if (dictionaryInputStream == null)
         {
            tried += "\n"+filename;

            if (!id.equals("en-GB"))
            {
               resources.setDictLocaleId("en-GB");

               filename = name+"-"+resources.getDictLocaleId()+".prop";

               dictionaryInputStream 
                 = resources.getClass().getResourceAsStream(filename);

               if (dictionaryInputStream == null)
               {
                  tried += "\n"+filename;
               }
            }

            if (dictionaryInputStream == null)
            {
               throw new IOException(
                 "Unable to open dictionary file. Tried: \n"+tried);
            }
         }
      }

      return dictionaryInputStream;
   }

   /**
    * Creates a new instance of the language set. The 
    * dictionary is loaded from the dictionary input stream.
    * The licence text is loaded from the licence input stream
    * and added to the dictionary with the key "licence".
    * @param dictionaryStream the dictionary input stream
    * @param licenceStream the licence input stream
    */
   public JDRDictionary(JDRResources resources, InputStream dictionaryStream,
      InputStream licenceStream)
      throws IOException
   {
      loadInputStreams(resources, dictionaryStream, licenceStream);
   }

   private void loadInputStreams(JDRResources resources, 
      InputStream dictionaryStream, InputStream licenceStream)
      throws IOException
   {
      BufferedReader reader;

      dictionary = new Properties();

      dictionary.loadFromXML(dictionaryStream);

/*
      dictionary = null;

      reader = new BufferedReader(
         new InputStreamReader(dictionaryStream, 
          resources.getResourceEncoding(resources.getDictLocaleId())));

      initDictionary(reader);

      reader.close();
*/

      reader = new BufferedReader(
         new InputStreamReader(licenceStream));

      initLicence(reader);

      reader.close();
   }

/*
   private void initDictionary(BufferedReader reader)
      throws IOException
   {
      dictionary = new Hashtable<String,String>(1024);

      String s;

      while ((s=reader.readLine()) != null)
      {
         if (s.trim().isEmpty()) continue;

         String[] split = s.split("=", 2);

         String key = split[0].trim();

         if (key.startsWith("#")) continue;

         String value = split[1];

         int idx;
         int lastIdx=0;

         while((idx = value.indexOf("\\n", lastIdx)) != -1)
         {
            if (idx == 0)
            {
               s = value.substring(2);
            }
            else
            {
               s = value.substring(0,idx)+"\n"
                 + value.substring(idx+2);
            }

            value = s;

            lastIdx = idx;
         }

         dictionary.put(key, value);
      }
   }
*/

   private void initLicence(BufferedReader reader)
      throws IOException
   {
      StringBuilder licenceText = new StringBuilder();

      String s;

      while ((s=reader.readLine()) != null)
      {
         licenceText.append(String.format("%s%n", s));
      }

      if (dictionary != null)
      {
         dictionary.put("licence", licenceText.toString());
      }
   }

   /**
    * Gets the string from the dictionary associated with the 
    * given key. If the key is not found, the key is returned
    * instead.
    * @param key the key identifying the required string
    * @return the string identified by the key if found, or 
    * the key if the dictionary doesn't contain the key
    */
   public String getString(String key)
   {
      if (dictionary == null)
      {
         return key;
      }

      //String s = (String)dictionary.get(key);
      String s = dictionary.getProperty(key);

      if (s == null)
      {
         s = key;
         System.err.println(String.format("Unknown key '%s'", key));
      }

      return s;
   }

   /**
    * Gets the string associated with the given key. If the key
    * is not found, the given default value is returned instead.
    * @param key the key identifying the required string
    * @param defVal the default value to return in the event the
    * entry is not found
    * @return the string associated with the given key, or the
    * default value if not found
    */
   public String getString(String key, String defVal)
   {
      if (dictionary == null)
      {
         return defVal;
      }

      return dictionary.getProperty(key, defVal);
/*
      String s = (String)dictionary.get(key);

      if (s == null)
      {
         s = defVal;
      }

      return s;
*/
   }

   public String applyMessagePattern(String key, Number value)
   {
      String str = getString(key);

      if (str == null)
      {
         System.err.println(String.format("Unknown key '%s'", key));
         return key;
      }

      ChoiceFormat fmt = new ChoiceFormat(str);
      return fmt.format(value);
   }

   public String getMessage(String key, Object... values)
   {
      String str = getString(key);

      if (str == null)
      {
         System.err.println(String.format("Unknown key '%s'", key));
         return key;
      }

      return MessageFormat.format(str, values);
   }

   public String getMessageWithAlt(String altFormat, String key, Object... values)
   {
      String str = dictionary.getProperty(key, altFormat);

      return MessageFormat.format(str, values);
   }

   /**
    * Gets the first character of the string identified by the
    * given key.
    * Deprecated. Use {@link getCodePoint(String)} instead.
    * @param key the key identifying the required string
    * @return the first character of the string identified by the
    * key, or the first character of the key if this dictionary
    * doesn't contain the key
    */
   @Deprecated
   public char getChar(String key)
   {
      String s = getString(key);
      return s.isEmpty() ? (char)0 : s.charAt(0);
   }

   /**
    * Gets the first character of the string identified by the
    * given key or the default value if not found.
    * Deprecated. Use {@link getCodePoint(String,int)} instead.
    * @param key the key identifying the required string
    * @param defVal the default value if not found
    * @return the first character of the string identified by the
    * key, or the default value if this dictionary
    * doesn't contain the key
    */
   @Deprecated
   public char getChar(String key, char defVal)
   {
      if (dictionary == null)
      {
         return defVal;
      }

      String s = dictionary.getProperty(key);
      //String s = (String)dictionary.get(key);

      if (s == null || s.isEmpty()) return defVal;

      return s.charAt(0);
   }

   public int getCodePoint(String key)
   {
      String s = getString(key);
      return s.isEmpty() ? -1 : s.codePointAt(0);
   }

   public int getCodePoint(String key, int defVal)
   {
      if (dictionary == null)
      {
         return defVal;
      }

      String s = dictionary.getProperty(key);
      //String s = (String)dictionary.get(key);

      if (s == null || s.isEmpty()) return defVal;

      return s.codePointAt(0);
   }

   /**
    * Determines if this dictionary contains the given key.
    * @param key the key to check
    * @return true if this dictionary contains the given key
    */
   public boolean containsKey(String key)
   {
      if (dictionary == null)
      {
         return false;
      }

      return dictionary.containsKey(key);
   }

   private Properties dictionary;
}

