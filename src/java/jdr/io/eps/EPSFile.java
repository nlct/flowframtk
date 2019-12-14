// File          : EPSFile.java
// Purpose       : class representing an EPS file
// Date          : 22 May 2008
// Last Modified : 22 May 2008
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
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing an EPS file.
 * @author Nicola L C Talbot
 */
public class EPSFile implements EPSObject,EPSReadable,EPSWriteable
{
   /**
    * Initialises with the given filename and access.
    * @param filename the file name
    * @param access access string (currently only supports "r" and
    * "w")
    */
   public EPSFile(EPSString filename, EPSString access)
      throws IOException,UnsupportedAccessException
   {
      this(filename.value(), access.value());
   }

   /**
    * Initialises with the given filename and access.
    * @param filename the file name
    * @param access access string (currently only supports "r" and
    * "w")
    */
   public EPSFile(String filename, String access)
      throws IOException,UnsupportedAccessException
   {
      if (filename.equals("%stdin"))
      {
         type_ = STDIN;
         file_ = null;
         canRead_ = true;
         canWrite_ = false;
         name_ = "%stdin";
      }
      else if (filename.equals("%stdout"))
      {
         type_ = STDOUT;
         file_ = null;
         canRead_ = false;
         canWrite_ = true;
         name_ = "%stdout";
      }
      else if (filename.equals("%stderr"))
      {
         type_ = STDERR;
         file_ = null;
         canRead_ = false;
         canWrite_ = true;
         name_ = "%stderr";
      }
      else
      {
         file_ = new File(filename);
         name_ = filename;
         type_ = OTHER;

         if (access.equals("r"))
         {
            if (!file_.exists())
            {
               throw new IOException("Can't open file '"+filename
                  +"' in 'r' mode: file doesn't exist");
            }

            canRead_ = true;
            canWrite_ = false;

            in_ = new BufferedReader(new FileReader(file_));
         }
         else if (access.equals("w"))
         {
            canRead_ = false;
            canWrite_ = true;

            out_ = new PrintWriter(file_);
         }
         else
         {
            throw new UnsupportedAccessException(access);
         }
      }
   }

   /**
    * Initialises with the given file and access.
    * @param file the file 
    * @param access access string (currently only supports "r" and
    * "w")
    */
   public EPSFile(File file, String access)
      throws IOException,UnsupportedAccessException
   {
      file_ = file;
      name_ = file.getName();
      type_ = OTHER;

      if (access.equals("r"))
      {
         if (!file_.exists())
         {
            throw new IOException("Can't open file '"+name_
               +"' in 'r' mode: file doesn't exist");
         }

         canRead_ = true;
         canWrite_ = false;

         in_ = new BufferedReader(new FileReader(file_));
      }
      else if (access.equals("w"))
      {
         canRead_ = false;
         canWrite_ = true;

         out_ = new PrintWriter(file_);
      }
      else
      {
         throw new UnsupportedAccessException(access);
      }
   }

   /**
    * Current file.
    * @param in current file reader
    */
   public EPSFile(BufferedReader in)
   {
      type_ = CURRENTFILE;
      file_ = null;
      in_ = in;
      canRead_ = true;
      canWrite_ = false;
      name_ = "currentfile";
   }

   private EPSFile()
   {
   }

   /**
    * Determines what type of file this is.
    * @return one of:  {@link #CURRENTFILE},
    * {@link #STDIN}, {@link #STDOUT}, {@link #STDERR}
    * or {@link #OTHER}
    */
   public int getType()
   {
      return type_;
   }

   /**
    * Determines if this is the current file.
    */
   public boolean isCurrentFile()
   {
      return type_ == CURRENTFILE;
   }

   /**
    * Determines if this is STDIN.
    */
   public boolean isStdin()
   {
      return type_ == STDIN;
   }

   /**
    * Determines if this is STDOUT.
    */
   public boolean isStdout()
   {
      return type_ == STDOUT;
   }

   /**
    * Determines if this is STDERR.
    */
   public boolean isStderr()
   {
      return type_ == STDERR;
   }

   public boolean equals(Object object)
   {
      if (object instanceof EPSFile)
      {
         EPSFile f = (EPSFile)object;

         if (type_ == f.type_ && type_ != OTHER)
         {
            return true;
         }

         if (file_ == null || f.file_ == null)
         {
            return false;
         }

         return file_.equals(f.file_);
      }

      return false;
   }

   /**
    * Gets this file's name.
    * @return name associated with this file
    */
   public String getName()
   {
      switch (type_)
      {
         case CURRENTFILE :
            return "currentfile";
         case STDIN :
            return "%stdin";
         case STDOUT :
            return "%stdout";
         case STDERR :
            return "%stderr";
      }

      return name_;
   }

   /**
    * Returns a string representation of this object
    * @return a string representation of this object
    */
   public String toString()
   {
      if (type_ != OTHER)
      {
         return getName();
      }

      String access = "";

      if (canRead_)
      {
         access = "r";
      }

      if (canWrite_)
      {
         access += "w";
      }

      return name_+" ("+access+")";
   }

   /**
    * Gets the current line number.
    * @return current line number
    */
   public int getLineNum()
   {
      return lineNum;
   }

   public EPSName pstype()
   {
      return new EPSName("file");
   }

   public boolean hasReadAccess()
   {
      return canRead_;
   }

   public boolean hasWriteAccess()
   {
      return canWrite_;
   }

   public void setWriteAccess(boolean access)
      throws InvalidEPSObjectException
   {
      throw new InvalidEPSObjectException("invalid type");
   }

   public void setReadAccess(boolean access)
      throws InvalidEPSObjectException
   {
      throw new InvalidEPSObjectException("invalid type");
   }

   public void closefile()
      throws IOException
   {
      if (in_ != null)
      {
         in_.close();
      }
      if (out_ != null)
      {
         out_.close();
      }
   }

   public void flush()
   {
      if (canWrite_)
      {
         if (out_ != null)
         {
            out_.flush();
         }
         else if (isStdout())
         {
            System.out.flush();
         }
         else if (isStderr())
         {
            System.err.flush();
         }
      }
   }

   /**
    * Returns true if file is still open.
    */
   public boolean status()
   {
      if (type_ == OTHER || type_ == CURRENTFILE)
      {
         if (canRead_ && in_ != null)
         {
            return true;
         }

         if (canWrite_ && out_ != null)
         {
            return true;
         }
      }

      return true;
   }

   /**
    * Reads a single character.
    * @return character read from file
    * @throws NoReadAccessException if file does not have read
    * access or has been closed
    */
   public int read() throws NoReadAccessException,IOException
   {
      if (!canRead_)
      {
         throw new NoReadAccessException("no read access for file '"
            +name_+"'");
      }

      int c = -1;

      if (type_ == STDIN)
      {
         c = System.in.read();

         if (discardLF && c == '\n')
         {
            c = System.in.read();
         }
      }
      else if (in_ == null)
      {
         throw new NoReadAccessException("can't read from file '"
            +name_+"' (file closed)");
      }
      else
      {
         c = in_.read();

         if (discardLF && c == '\n')
         {
            c = in_.read();
         }
      }

      discardLF = false;

      if (c == -1)
      {
         eof = true;
      }
      else if (c == '\r')
      {
         lineNum++;
      }
      else if (c == '\n')
      {
         if (previousRead != '\r')
         {
            lineNum++;
         }
      }

      previousRead = c;

      return c;
   }

   /**
    * Writes a single character.
    * @param c character to write to file
    * @throws NoWriteAccessException if file does not have write
    * access or has been closed
    */
   public void write(int c)
      throws NoWriteAccessException,IOException
   {
      write((char)c);
   }

   /**
    * Writes a single character.
    * @param c character to write to file
    * @throws NoWriteAccessException if file does not have write
    * access or has been closed
    */
   public void write(char c)
      throws NoWriteAccessException,IOException
   {
      if (type_ == STDOUT)
      {
         System.out.print(c);
      }
      else if (type_ == STDERR)
      {
         System.err.print(c);
      }
      else if (!canWrite_)
      {
         throw new NoWriteAccessException("no write access for file '"
            +name_+"'");
      }
      else if (out_ == null)
      {
         throw new IOException("can't write to file '"
            +name_+"' (file closed)");
      }
      else
      {
         out_.print(c);
      }
   }

   /**
    * Reads a string from file and stores in buffer.
    * @param buffer string stored in buffer
    * @return true if successful
    */
  public boolean readstring(char[] buffer)
  {
     boolean success=true;

     if (!canRead_)
     {
        return false;
     }

     try
     {
        for (int i = 0; i < buffer.length; i++)
        {
           int c = read();

           if (c == -1)
           {
              success = false;
              break;
           }

           buffer[i] = (char)c;
        }
     }
     catch (NoReadAccessException e)
     {
        return false;
     }
     catch (IOException e)
     {
        return false;
     }

     return success;
  }

  /**
   * Writes a string to file.
   * @param buffer string to write to file
   */
   public void writestring(EPSString buffer)
   {
      writestring(buffer.value());
   }

  /**
   * Writes a string to file.
   * @param buffer string to write to file
   */
   public void writestring(String buffer)
   {
      if (!canWrite_)
      {
         return;
      }

      switch (type_)
      {
         case STDOUT :
            System.out.print(buffer);
            return;
         case STDERR :
            System.err.print(buffer);
            return;
         default :
            out_.print(buffer);
      }
   }

  /**
   * Writes a string to file.
   * @param buffer string to write to file
   */
   public void writestring(char[] buffer)
   {
      if (!canWrite_)
      {
         return;
      }

      switch (type_)
      {
         case STDOUT :
            System.out.print(buffer);
            return;
         case STDERR :
            System.err.print(buffer);
            return;
         default :
            out_.print(buffer);
      }
   }

  /**
   * Writes a hexadecimal string to file.
   * @param buffer string to write to file
   */
   public void writehexstring(EPSString buffer)
   {
      writehexstring(buffer.getChars());
   }

  /**
   * Writes a hexadecimal string to file.
   * @param buffer string to write to file
   */
   public void writehexstring(char[] buffer)
   {
      if (!canWrite_)
      {
         return;
      }

      switch (type_)
      {
         case STDOUT :
            for (int i = 0; i < buffer.length; i++)
            {
               System.out.print(Integer.toHexString((char)buffer[i]));
            }
            return;
         case STDERR :
            for (int i = 0; i < buffer.length; i++)
            {
               System.err.print(Integer.toHexString((char)buffer[i]));
            }
            return;
         default :
            if (out_ != null)
            {
               for (int i = 0; i < buffer.length; i++)
               {
                  out_.print(Integer.toHexString((char)buffer[i]));
               }
            }
      }
   }

   /**
    * Reads a hexadecimal string from file and stores in buffer.
    * @param buffer string stored in buffer
    * @return number of characters inserted into buffer
    */
   public int readhexstring(char[] buffer)
   {
      int n = 0;

      if (!canRead_)
      {
         return 0;
      }

      try
      {
         int prev = -1;

         char[] hex = new char[2];

         int i = 0;

         for (i = 0; i < buffer.length;)
         {
            int c = read();

            if (c == -1)
            {
               break;
            }

            if (EPSString.isHex((char)c))
            {
               if (prev == -1)
               {
                  prev = c;
               }
               else
               {
                  hex[0] = (char)prev;
                  hex[1] = (char)c;

                  buffer[i] = 
                     (char)Integer.parseInt(new String(hex), 16);
                     
                  prev = -1;
                  i++;
               }
            }
         }

         n = i;
      }
      catch (NumberFormatException e)
      {
         // shouldn't happen
         return 0;
      }
      catch (IOException e)
      {
      }
      catch (NoReadAccessException e)
      {
         return 0;
      }

      return n;
   }

   /**
    * Mark the present position in the stream.
    * @param readAheadLimit limit on the number of characters that
    * may be read while still preserving the mark.
    * @throws IllegalArgumentException if readAheadLimit is &lt; 0
    * @throws IOException if an I/O error occurs
    */
   public void mark(int readAheadLimit)
      throws EOFException,IOException,IllegalArgumentException
   {
      if (eof)
      {
         throw new EOFException("file mark failed (end of file)");
      }
      else if (in_ != null)
      {
         in_.mark(readAheadLimit);
      }
      else
      {
         if (type_ == STDIN)
         {
            throw new IOException("file mark not supported for stdin");
         }
         else if (!canRead_)
         {
            throw new IOException("file mark not supported ('"
               +name_+"' has no read access)");
         }
         else
         {
            throw new IOException("file mark failed ('"
               +name_+"' is closed)");
         }
      }

      markLineNum = lineNum;
   }

   /**
    * Reset the stream to the most recent mark.
    */
   public void reset() throws IOException
   {
      if (in_ != null)
      {
         if (!in_.markSupported())
         {
            throw new IOException("mark not supported");
         }

         in_.reset();
         eof = false;
      }
      else
      {
         if (type_ == STDIN)
         {
            throw new IOException("file reset not supported for stdin");
         }
         else if (!canRead_)
         {
            throw new IOException("file reset not supported ('"
               +name_+"' has no read access)");
         }
         else
         {
            throw new IOException("file reset failed ('"
               +name_+"' is closed)");
         }
      }

      lineNum = markLineNum;
   }

   /**
    * Goes back to the start of the file.
    * @throws IOException if file doesn't have read access
    * or if restart not supported
    */
   public void restart() throws IOException
   {
      if (type_ != OTHER)
      {
         throw new IOException("file restart not supported for "
            + getName());
      }

      if (!canRead_)
      {
         throw new IOException("file restart not supported ('"
            +name_+"' has no read access)");
      }

      if (in_ != null)
      {
         in_.close();
      }

      in_ = new BufferedReader(new FileReader(file_));
      lineNum = 0;
      markLineNum = 0;
      previousRead = -1;
      discardLF = false;
      eof = false;
   }

   /**
    * Reads a line of text.
    * @return string containing the contents of the line, not including
    * any line-termination characters
    * @throws IOException if an I/O error occurs
    */
   public String readline()
      throws IOException,NoReadAccessException
   {
      String line = null;

      if (!canRead_)
      {
         throw new NoReadAccessException("readline failed ('"
               +name_+"' has no read access)");
      }

      int b=0;

      line = "";

      while (b != '\n' && b != '\r')
      {
         b = read();

         if (b == -1)
         {
            break;
         }

         if (b != '\n' && b != '\r')
         {
            line += (char)b;
         }
      }

      discardLF = (b == '\r');

      return line;
   }

   /**
    * Determines if the end of file has been reached.
    * @return true if EOF encountered
    */
   public boolean atEOF()
   {
      return eof;
   }

   public EPSObject readObject(EPS eps)
      throws IOException,InvalidFormatException
   {
      int c = read();

      if (c == -1) return null;

      while (Character.isWhitespace(c))
      {
         c = read();
      }

      if (c == -1) return null;

      if (c == '%')
      {
         String line = readline();

         if (line.startsWith("%Trailer") || line.equals("%EOF"))
         {
            return null;
         }

         return readObject(eps);
      }

      if (c == '{')
      {
         return readGroup(eps);
      }
      else if (c == '[')
      {
         return new EPSMark();
      }
      else if (c == ']')
      {
         return new EPSName("]");
      }
      else if (c == '<')
      {
         mark(2);
         c = read();
         if (c == '<')
         {
            return new EPSDictMark();
         }
         else
         {
            reset();
            return readHexString(eps);
         }
      }
      else if (c == '>')
      {
         mark(2);
         c = read();
         if (c == '>')
         {
            return new EPSName(">>");
         }
         else
         {
            reset();
            c = '>';
         }
      }
      else if (c == '(')
      {
         return readString(eps);
      }
      else if (c == '/')
      {
         return readProc((char)c);
      }
      else if (c == '.' || Character.isDigit(c) || c == '+'
        || c == '-')
      {
         return readNum((char)c);
      }

      String object = new String("");

      while (!Character.isWhitespace(c))
      {
         object += (char)c;
         mark(2);
         c = read();

         if (c == '%')
         {
            readline();
            break;
         }

         if (c == '{' || c == '}' || c == '[' || c == ']' 
            || c == '<' || c == '>' || c == '/')
         {
            reset();
            break;
         }
      }

      return new EPSName(object);
   }

   public EPSString readHexString(EPS eps)
      throws IOException,EOFException,InvalidFormatException
   {
      String string = new String("");

      eps.printMessage("Reading hexadecimal data");

      int c = read();

      if (c == -1)
      {
         throw new EOFException(
            "EOF encountered while scanning hexadecimal string");
      }

      int i = 0;

      eps.resetProgress();
      eps.setIndeterminate(true);

      while (c != '>')
      {
         if (c == '%')
         {
            readline();
            c = ' ';
         }

         if (!Character.isWhitespace(c))
         {
            string += (char)c;

            if ((i++)%256 == 0) eps.incrementProgress();
         }

         c = read();
      }

      eps.printlnMessage("");

      int n = string.length();

      if (n%2 == 1)
      {
         string += '0';
         n++;
      }

      char[] chars = new char[n/2];

      try
      {
         i = 0;
         for (int j = 0; i < n; i += 2, j++)
         {
            chars[j] = (char)Integer.parseInt(string.substring(i, i+2), 16);
         }
      }
      catch (NumberFormatException e)
      {
         throw new InvalidFormatException(
            "Can't parse hexadecimal string");
      }

      return new EPSString(chars);
   }

   public EPSString readString(EPS eps)
   throws IOException,EOFException,InvalidFormatException
   {
      String string = new String("");

      boolean escape = false;
      int bracket=1;

      int c = read();

      if (c == -1)
      {
         throw new EOFException(
           "EOF encountered while scanning literal string");
      }

      int i = 0;

      eps.resetProgress();
      eps.setIndeterminate(true);

      while (true)
      {
         if ((i++)%256 == 0) eps.incrementProgress();

         if (c == '\\')
         {
            escape = !escape;

            if (!escape)
            {
               string += (char)c;
            }
         }
         else
         {
            if (c == '(')
            {
               if (!escape)
               {
                  bracket++;
               }
               string += (char)c;
            }
            else if (c == ')')
            {
               if (!escape)
               {
                  bracket--;

                  if (bracket == 0)
                  {
                     break;
                  }
               }
               string += (char)c;
            }
            else if (escape && Character.digit((char)c, 8) != -1)
            {
               string += (char)readOct((char)c);
            }
            else if (escape && c == 'n')
            {
               string += '\n';
            }
            else if (escape && c == 'r')
            {
               string += '\r';
            }
            else if (escape && c == 't')
            {
               string += '\t';
            }
            else if (escape && c == 'b')
            {
               string += '\b';
            }
            else if (escape && c == 'f')
            {
               string += '\f';
            }
            else if (!(escape && (c == '\n' || c == '\r')))
            {
               string += (char)c;
            }
            escape = false;
         }

         c = read();
         if (c == -1)
         {
            eof = true;
            throw new EOFException(
               "EOF encountered while scanning literal string");
         }
      }

      return new EPSString(string);
   }

   public int readOct(char start)
      throws IOException,EOFException,InvalidFormatException
   {
      String string = ""+start;

      for (int i = 0; i < 2; i++)
      {
         mark(2);
         int c = read();
         if (c == -1) break; 

         if (c == '%')
         {
            readline();
            c = ' ';
         }

         if (Character.digit((char)c, 8) == -1)
         {
            reset();
            break;
         }

         string += (char)c;
      }

      try
      {
         return Integer.parseInt(string, 8);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidFormatException(e.getMessage());
      }
   }

   public EPSProc readGroup(EPS eps)
      throws EOFException,IOException,InvalidFormatException
   {
      EPSProc vector = new EPSProc();

      while (true)
      {
         mark(2);
         int c = read();
         if (c == -1)
         {
            eof = true;
            throw new EOFException(
               "Unexpected EOF while scanning {...}");
         }

         while (Character.isWhitespace(c))
         {
            mark(2);
            c = read();
            if (c == -1)
            {
               eof = true;
               throw new EOFException(
                  "Unexpected EOF while scanning {...}");
            }
         }

         if (c == '}')
         {
            break;
         }

         if (c == '%')
         {
            readline();
            continue;
         }

         reset();

         EPSObject object = readObject(eps);

         vector.add(object);
      }

      return vector;
   }

   public EPSName readProc(char start)
      throws IOException,InvalidFormatException
   {
      String string=""+start;

      while (true)
      {
         mark(2);
         int c = read();

         if (c == -1) break;

         if (c == '{' || c == '['
          || c == '}' || c == ']'
          || c == '(' || c == ')'
          || c == '<' || c == '>'
          || c == '/')
         {
            reset();
            break;
         }

         if (c == '%')
         {
            readline();
            break;
         }

         if (Character.isWhitespace((char)c))
         {
            break;
         }

         string += (char)c;
      }

      return new EPSName(string);
   }

   public EPSObject readNum(char start)
      throws IOException,InvalidFormatException
   {
      String string = ""+start;
      boolean isInt = (start != '.');
      boolean isExp=false;
      boolean isRadix = false;
      int prev = (int)start;

      while (true)
      {
         mark(2);
         int c = read();

         if (c == -1) break;

         if (c == '%')
         {
            readline();
            c = ' ';
         }

         if (c == '.')
         {
            if (isRadix)
            {
               reset();
               break;
            }

            if (!(isInt || isExp))
            {
               break;
            }
            isInt = false;
            isExp = true;
            string += '.';
         }
         else if (c == 'e' || c == 'E')
         {
            isExp = true;
            isInt = false;
            string += (char)c;
         }
         else if ((c == '+' || c == '-')
            && (prev == 'e' || prev == 'E'))
         {
            string += (char)c;
         }
         else if (Character.isDigit((char)c))
         {
            string += (char)c;
         }
         else if (c == '#')
         {
            if (isRadix)
            {
               reset();
               break;
            }

            string += (char)c;
            isRadix = true;
         }
         else
         {
            if (!Character.isWhitespace((char)c)) reset();

            break;
         }

         prev = c;
      }

      try
      {
         if (isRadix)
         {
            String[] split = string.split("#", 2);
            int radix = Integer.parseInt(split[0]);
            int value = Integer.parseInt(split[1], radix);
            return new EPSInteger(value);
         }
         else if (isInt)
         {
            return new EPSInteger(string);
         }
         else
         {
            return new EPSDouble(string);
         }
      }
      catch (NumberFormatException e)
      {
         return new EPSName(string);
      }
   }

   /**
    * Returns this.
    */
   public Object clone()
   {
      return this;
   }

   public void execute(EPS eps)
      throws InvalidFormatException,InvalidPathException,
         NoninvertibleTransformException,
         IOException
   {
      EPSObject object = readObject(eps);

      while (object != null)
      {
         eps.processObject(object);

         object = readObject(eps);
      }
   }

   private File file_;

   private String name_;

   /**
    * The current file (currentfile).
    */
   public static final int CURRENTFILE=0;
   /**
    * Standard input (%stdin).
    */
   public static final int STDIN=1;
   /**
    * Standard output (%stdout).
    */
   public static final int STDOUT=2;
   /**
    * Standard error (%stderr).
    */
   public static final int STDERR=4;
   /**
    * Not a special file.
    */
   public static final int OTHER=5;

   private int type_;

   private boolean canRead_, canWrite_;

   private BufferedReader in_=null;
   private PrintWriter out_ = null;

   private boolean eof=false;

   private int lineNum=0, markLineNum=0;

   private int previousRead=-1;

   private boolean discardLF=false;
}
