// File          : JdrInfo.java
// Description   : Gets information about a JDR or AJR file
// Creation Date : 2014-05-23
// Author        : Nicola L C Talbot
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
package com.dickimawbooks.jdrinfo;

import java.io.*;
import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Gets information about a JDR or AJR file.
 * @author Nicola L C Talbot
 */
public class JdrInfo implements FileFilter
{
   public JdrInfo()
   {
   }

   public boolean accept(File pathname)
   {
      String name = pathname.getName().toLowerCase();

      return name.endsWith(".jdr") || name.endsWith(".ajr");
   }


   private void checkFile(File file)
   {
      if (!file.exists())
      {
         System.err.println("jdrinfo: "+"'"+file.toString()+"' doesn't exist");
         return;
      }

      try
      {
         String fileFormat = JDRAJR.getFileFormat(file);

         System.out.println(file.toString()+": "+fileFormat);
      }
      catch (InvalidFormatException e)
      {
         System.err.println("jdrinfo: '"+file.toString()
            +"' is not a JDR or AJR file");
      }
      catch (FileNotFoundException e)
      {
         System.err.println("jdrinfo: "+e.getMessage());
      }
      catch (IOException e)
      {
         System.err.println("jdrinfo: unable to read file '"+file.toString()+"'");
         System.err.println(e);
      }
   }

   public void checkFiles(String[] args)
   {
      for (int i = 0; i < args.length; i++)
      {
         File file = new File(args[i]);

         if (file.isDirectory())
         {
            File[] list = file.listFiles(this);

            for (File f : list)
            {
               checkFile(f);
            }
         }
         else
         {
            checkFile(file);
         }
      }
   }

   /**
    * Prints version information to STDERR.
    */
   public static void appVersion()
   {
      System.err.println("jdrinfo 1.0");
      System.err.println("Copyright (C) 2014 Nicola L C Talbot");
      System.err.println("This is free software distributed under the GNU General Public License.");
      System.err.println("There is NO WARRANTY. See accompanying licence file for details.");
   }

   /**
    * Prints syntax information to STDERR and quits.
    */
   public static void syntax()
   {
      System.err.println("Syntax: jdrinfo <file or directory>+");
      System.exit(0);
   }

   public static void main(String[] args)
   {
      if (args.length == 0)
      {
         System.err.println("Missing input file(s)");
         System.err.println("Use '-help' for help");

         System.exit(1);
      }

      JdrInfo info = new JdrInfo();

      info.checkFiles(args);
   }

}
