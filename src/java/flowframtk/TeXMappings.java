// File          : TeXMappings.java
// Purpose       : mappings of symbols
// Creation Date : 2014-05-10
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

package com.dickimawbooks.flowframtk;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;

import com.dickimawbooks.jdrresources.*;

public class TeXMappings extends Hashtable<Integer,TeXLookup>
{
   public TeXMappings(JDRResources resources, String name)
   {
      super();
      this.resources = resources;
      this.modeName = name;
   }

   public TeXLookup get(int i)
   {
      return get(new Integer(i));
   }

   public TeXLookup put(char chr, String map)
   {
      return put((int)chr, map, "none");
   }

   public TeXLookup put(char chr, String map, String styName)
   {
      return put((int)chr, map, styName);
   }

   public TeXLookup put(int code, String map, String styName)
   {
      return put(new Integer(code), new TeXLookup(map, styName));
   }

   public String applyMappings(String original, Vector<String> styNames)
   {
      if (original == null || original.isEmpty()) return "";

      int n = original.length();

      StringBuilder builder = new StringBuilder(
       (int)Math.max(n, 10));

      for (int i = 0; i < n; )
      {
         Integer codePoint = new Integer(original.codePointAt(i));
         i += Character.charCount(codePoint.intValue());

         TeXLookup map = get(codePoint);

         if (map == null)
         {
            builder.appendCodePoint(codePoint.intValue());
         }
         else
         {
            builder.append(map.getCommand());

            String styName = map.getStyName();

            if (!styName.isEmpty() && !styName.equals("none"))
            {
               Matcher m = STY_PATTERN.matcher(styName);

               while (m.find())
               {
                  String opt = m.group(1);
                  String name = m.group(2);

                  if (name.equals("none"))
                  {
                     continue;
                  }

                  String[] optSplit = null;

                  if (opt != null)
                  {
                     opt = opt.trim();

                     if (opt.isEmpty())
                     {
                        opt = null;
                     }
                     else
                     {
                        optSplit = opt.split(" *, *");
                     }
                  }

                  boolean found = false;

                  for (int j = 0; j < styNames.size(); j++)
                  {
                     String theSty = styNames.get(j);

                     Matcher theMatcher = STY_PATTERN.matcher(theSty);

                     if (theMatcher.matches())
                     {
                        String theOpt = theMatcher.group(1);
                        String theName = theMatcher.group(2);

                        if (theName.equals(name))
                        {
                           if (opt != null)
                           {
                              if (theOpt == null)
                              {
                                 styNames.set(j,
                                    String.format("[%s]%s", opt, name));
                              }
                              else
                              {
                                 String[] theSplit = theOpt.split(",");

                                 boolean replace = false;

                                 for (int k1 = 0; k1 < optSplit.length; k1++)
                                 {
                                    boolean optFound = false;

                                    for (int k2 = 0; k2 < theSplit.length; k2++)
                                    {
                                       if (optSplit[k1].equals(theSplit[k2]))
                                       {
                                          optFound = true;
                                          break;
                                       }
                                    }

                                    if (!optFound)
                                    {
                                       theOpt = theOpt+","+optSplit[k1];
                                       replace = true;
                                    }
                                 }

                                 if (replace)
                                 {
                                    styNames.set(j,
                                       String.format("[%s]%s", theOpt, name));
                                 }
                              }
                           }

                           found = true;
                           break;
                        }
                     }
                  }

                  if (!found)
                  {
                     if (opt == null)
                     {
                        styNames.add(name);
                     }
                     else
                     {
                        styNames.add(String.format("[%s]%s", opt, name));
                     }
                  }
               }
            }
         }
      }

      return builder.toString();
   }

   public void save(PrintWriter writer)
     throws IOException
   {
      for (Enumeration<Integer> en = keys(); en.hasMoreElements(); )
      {
         Integer codePoint = en.nextElement();
         TeXLookup map = get(codePoint);

         writer.println(String.format("%X", codePoint)+"\t"+map.getCommand()
           +"\t"+map.getStyName());
      }
   }

   public void read(File file) throws IOException
   {
      BufferedReader reader = null;

      try
      {
         reader = new BufferedReader(new FileReader(file));

         read(reader);
      }
      finally
      {
         if (reader != null)
         {
            reader.close();
         }
      }
   }

   public void read(BufferedReader reader)
      throws IOException
   {
      String line;
      int lineNum = 0;

      while ((line = reader.readLine()) != null)
      {
         lineNum++;

         if (line.startsWith("#"))
         {
            continue;
         }

         String[] split = line.split("\t", 3);

         if (split == null || split.length != 3)
         {
            throw new IOException(resources.getStringWithValue(lineNum,
               "error.io.invalid_map", line));
         }

         try
         {
            put(Integer.parseInt(split[0], 16), 
              new TeXLookup(split[1], split[2]));
         }
         catch (NumberFormatException e)
         {
            throw new IOException(resources.getStringWithValue(
               lineNum, "error.io.invalid_map", line));
         }
      }
   }

   public String getModeName()
   {
      return modeName;
   }

   private JDRResources resources;

   private String modeName;

   public static final Pattern STY_PATTERN = 
     Pattern.compile("(?:\\[([^\\]]*)\\])?\\s*([\\w\\-]+)");
}

