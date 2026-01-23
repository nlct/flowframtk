// File          : AJR.java
// Purpose       : functions to save and load AJR files
// Creation Date : 1st February 2006
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

package com.dickimawbooks.jdr.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Functions to save and load AJR files.
 * @author Nicola L C Talbot
*/

public class AJR extends JDRAJR
{
   public AJR()
   {
      super();
   }

   /**
    * Saves all objects in latest AJR format.
    *
    * The way in which the settings are
    * saved depends on the settings flag which may be one of:
    * {@link JDR#NO_SETTINGS} (don't
    * save the settings), {@link JDR#ALL_SETTINGS} (save all
    * settings) or {@link JDR#PAPER_ONLY} (only save the
    * paper size.)
    * <p>
    * None of the current AJR formats allow
    * any linear or radial gradient paint to have a start or
    * end colour that isn't either {@link JDRColor} or 
    * {@link JDRColorCMYK}.
    * @param allObjects all objects constituting the image
    * @param out the print writer to the required file
    * @param settingsFlag indicate whether to save settings
    * @throws IOException if there is an I/O error.
   */
   public void save(JDRGroup allObjects,
      PrintWriter out, Charset charset, int settingsFlag)
      throws IOException
   {
      save(allObjects, out, charset, CURRENT_VERSION, settingsFlag);
   }

   @Deprecated
   public void save(JDRGroup allObjects,
      PrintWriter out, int settingsFlag)
      throws IOException
   {
      save(allObjects, out, CURRENT_VERSION, settingsFlag);
   }

   /**
    * Saves all objects in given AJR format.
    *
    * The way in which the settings are
    * saved depends on the settings flag which may be one of:
    * {@link JDRAJR#NO_SETTINGS} (don't
    * save the settings), {@link JDRAJR#ALL_SETTINGS} (save all
    * settings) or {@link JDRAJR#PAPER_ONLY} (only save the
    * paper size.)
    * <p>
    * None of the current AJR formats allow
    * any linear or radial gradient paint to have a start or
    * end colour that isn't either {@link JDRColor} or 
    * {@link JDRColorCMYK}.
    * <p>
    * @param allObjects all objects constituting the image
    * @param out the print writer to the required file
    * @param version AJR version
    * @param settingsFlag indicate whether to save settings
    * @throws IOException if there is an I/O error
   */
   public void save(JDRGroup allObjects,
      PrintWriter out, Charset charset, float version, int settingsFlag)
      throws IOException
   {
      encoding = charset;
      lineNum_ = -1;
      colIdx = 0;
      currentOut = out;

      saveImage(allObjects, version, settingsFlag);

      currentOut = null;
   }

   public void save(JDRGroup allObjects,
      File file, float version, int settingsFlag)
      throws IOException
   {
      encoding = StandardCharsets.UTF_8;
      lineNum_ = -1;
      colIdx = 0;

      try
      {
         currentOut = new PrintWriter(
           Files.newBufferedWriter(file.toPath(), encoding));

         saveImage(allObjects, version, settingsFlag);
      }
      finally
      {
         if (currentOut != null)
         {
            currentOut.close();
         }

         currentOut = null;
      }
   }

   @Deprecated
   public void save(JDRGroup allObjects,
      PrintWriter out, float version, int settingsFlag)
      throws IOException
   {
      save(allObjects, out, StandardCharsets.UTF_8,
        version, settingsFlag);
   }


   protected void saveFormatVersion(String versionString)
     throws IOException
   {
      if (version < 2.2)
      {
         currentOut.println("AJR "+versionString);
      }
      else
      {
         currentOut.println("AJR "+versionString+" "+encoding);
      }

      colIdx = 0;
   }

   public static boolean isAJR(File file)
     throws IOException
   {
      AJR ajr = new AJR();
      ajr.currentIn = null;
      boolean isAJR = false;

      try
      {
         ajr.currentIn = new BufferedReader(new FileReader(file));
         String string = ajr.readString(3);

         isAJR = string.equals("AJR");
      }
      catch (InvalidFormatException e)
      {
         isAJR = false;
      }
      finally
      {
         if (ajr.currentIn != null)
         {
            ajr.currentIn.close();
         }
      }

      return isAJR;
   }

   /**
   * Reads image from AJR format.
   *
   * The image is stored as a {@link JDRGroup}.
   * The settings flag can afterwards be retrieved using
   * {@link #getLastLoadedSettingsID()}.
   * @param in BufferedReader to input file
   * @param cg canvas graphics
   * @return the image as a <code>JDRGroup</code>
   * @throws InvalidFormatException if the file is incorrectly
   * formatted
   */
   public JDRGroup load(File file, CanvasGraphics cg)
      throws InvalidFormatException,IOException
   {
      return load(file.toPath(), cg);
   }

   public JDRGroup load(Path path, CanvasGraphics cg)
      throws InvalidFormatException,IOException
   {
      encoding = StandardCharsets.UTF_8;

      lineNum_ = 1;
      colIdx = 0;

      JDRGroup image;

      try
      {
         currentIn = Files.newBufferedReader(path, encoding);
         image = loadImage(cg);
      }
      catch (MismatchedEncodingException e)
      {
         if (currentIn != null)
         {
            currentIn.close();
         }

         encoding = e.getFound();

         lineNum_ = 1;
         colIdx = 0;

         try
         {
            currentIn = Files.newBufferedReader(path, encoding);
            image = loadImage(cg);
         }
         finally
         {
            if (currentIn != null)
            {
               currentIn.close();
            }

            currentIn = null;
         }
      }
      finally
      {
         if (currentIn != null)
         {
            currentIn.close();
         }

         currentIn = null;
      }

      return image;
   }

   /**
   * Reads image from AJR format.
   *
   * The image is stored as a {@link JDRGroup}.
   * The settings flag can afterwards be retrieved using
   * {@link #getLastLoadedSettingsID()}.
   * @param in BufferedReader to input file
   * @param inCharset the encoding of the BufferedReader
   * @param cg canvas graphics
   * @return the image as a <code>JDRGroup</code>
   * @throws InvalidFormatException if the file is incorrectly
   * formatted
   * @throws MismatchedEncodingException if the file contains an
   * encoding identifier that doesn't match inCharset
   */
   public JDRGroup load(BufferedReader in, Charset inCharset,
      CanvasGraphics cg)
      throws InvalidFormatException,MismatchedEncodingException
   {
      encoding = inCharset;
      currentIn = in;
      lineNum_ = 1;
      colIdx = 0;

      JDRGroup image = loadImage(cg);

      currentIn = null;

      return image;
   }

   @Deprecated
   public JDRGroup load(BufferedReader in,
      CanvasGraphics cg)
      throws InvalidFormatException
   {
      currentIn = in;
      lineNum_ = 1;
      colIdx = 0;

      JDRGroup image = loadImage(cg);

      currentIn = null;

      return image;
   }

   @Override
   protected String readFormatVersion()
     throws InvalidFormatException
   {
      return readFormatVersion(false);
   }

   protected String readFormatVersion(boolean prefixAlreadyFound)
     throws InvalidFormatException
   {
      String word;

      if (!prefixAlreadyFound)
      {
         try
         {
            word = readWord();
         }
         catch (Exception e)
         {
            throw new InvalidValueException(
               InvalidFormatException.PARSE_FORMAT, this, e);
         }

         if (!word.equals("AJR"))
         {
            throw new InvalidValueException(
              InvalidFormatException.FORMAT_TAG, word, this);
         }
      }

      try
      {
         return readWord();
      }
      catch (Exception e)
      {
         throw new InvalidValueException(
          InvalidFormatException.VERSION, this, e);
      }
   }

   protected void readPostVersion()
     throws InvalidFormatException
   {
      if (version >= 2.2f)
      {
         try
         {
            String encodingStr = readWord();

            Charset cs = Charset.forName(encodingStr);

            if (!cs.equals(encoding))
            {
               throw new MismatchedEncodingException(encoding, cs, this);
            }
         }
         catch (IllegalArgumentException e)
         {
            throw new InvalidValueException(
              InvalidFormatException.ENCODING, this, e);
         }
         catch (IOException e)
         {
            throw new InvalidValueException(
              InvalidFormatException.ENCODING, this, e);
         }
      }
   }

   /**
   * Reads sequence of characters terminated by white space
   * from the input stream. The result is returned as a string. Skips
   * any following white space.
   * <p>
   * The word can not contain more than {@link #buffLength} 
   * characters.
   * @return the word as a string
   * @throws IOException if I/O error occurs
   * @throws EOFException if EOF encountered
   * @throws BufferOverflowException if word exceeds buffer length
   */
   public String readWord()
      throws IOException,java.nio.BufferOverflowException
   {
      int c = currentIn.read();

      colIdx++;

      if (c == '\n')
      {
         lineNum_++;
         colIdx = 0;
      }

      while (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f')
      {
         c = currentIn.read();
         colIdx++;

         if (c == '\n')
         {
            lineNum_++;
            colIdx = 0;
         }
      }

      char[] buffer = new char[buffLength];
      int i = 0;

      while (!(c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f'))
      {
         if (c == -1)
         {
            throw new EOFException("EOF found while reading word");
         }

         if (i == buffLength)
         {
            throw new java.nio.BufferOverflowException();
         }

         buffer[i++] = (char)c;

         c = currentIn.read();
         colIdx++;
      }

      if (c == '\n')
      {
         lineNum_++;
         colIdx = 0;
      }

      return (new String(buffer)).substring(0, i);
   }

   /**
   * Reads an integer terminated by white space from input 
   * stream. Skips through any following white space.
   * @return the integer
   * @throws InvalidFormatException if invalid format
   */
   public int readInt()
     throws InvalidFormatException
   {
      String word;

      try
      {
         word = readWord();
      }
      catch (Exception e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_INT, this, e);
      }

      try
      {
         return Integer.parseInt(word);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException("int", word, this, e);
      }
   }

   /**
   * Reads a byte terminated by white space from input 
   * stream. Skips through any following white space.
   * @return the byte
   * @throws InvalidFormatException if invalid format
   */
   public byte readByte()
     throws InvalidFormatException
   {
      String word;

      try
      {
         word = readWord();
      }
      catch (Exception e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_BYTE, this, e);
      }

      try
      {
         return Byte.parseByte(word);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException("byte", word, this);
      }
   }

   /**
   * Reads a real number terminated by white space from input 
   * stream. Skips through any following white space. The
   * value is returned as a float.
   * @return the number as a float
   * @throws InvalidFormatException if invalid format
   */
   public float readFloat()
      throws InvalidFormatException
   {
      String word;

      try
      {
         word = readWord();
      }
      catch (Exception e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_FLOAT, this, e);
      }

      try
      {
         return Float.parseFloat(word);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException("float", word, this);
      }
   }

   /**
   * Reads a real number terminated by white space from input 
   * stream. Skips through any following white space. The
   * value is returned as a double.
   * @return the value as a double
   * @throws InvalidFormatException if invalid format
   */
   public double readDouble()
      throws InvalidFormatException
   {
      String word;

      try
      {
         word = readWord();
      }
      catch (Exception e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_DOUBLE, this, e);
      }

      try
      {
         return Double.parseDouble(word);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException("double", word, this);
      }
   }

   /**
   * Reads a boolean value (that is 0 or 1) from the input stream.
   * Skips through any following white space.
   * @return the value as a boolean
   * @throws InvalidFormatException if next word is not a 0 or 1
   */
   public boolean readBoolean()
     throws InvalidFormatException
   {
      String word;

      try
      {
         word = readWord();
      }
      catch (Exception e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_BOOLEAN, this, e);
      }

      try
      {
         int value = Integer.parseInt(word);

         if (value == 0)
         {
            return false;
         }
         else if (value == 1)
         {
            return true;
         }
         else
         {
            throw new NumberFormatException();
         }
      }
      catch (NumberFormatException e)
      {
         throw new InvalidValueException("boolean", word, this);
      }
   }

   /**
   * Reads given number of characters (including white space)
   * from the input stream. The result is stored as a 
   * <code>String</code>.
   * <p>
   * Note: if the end of line terminator consists of two characters
   * it will be counted as two characters.
   * @param stringLength number of characters
   * @return the string
   * @throws InvalidFormatException if invalid format
   */
   public String readString(int stringLength)
     throws InvalidFormatException
   {
      if (stringLength <= 0)
      {
         throw new InvalidArrayLengthException(stringLength, this);
      }

      try
      {
         int c = currentIn.read();
         colIdx++;

         if (c == '\n')
         {
            lineNum_++;
            colIdx = 0;
         }

         char[] buffer = new char[stringLength];

         for (int i = 0; i < stringLength; i++)
         {
            if (c == -1)
            {
               throw new InvalidValueException(
                 InvalidFormatException.PARSE_STRING, this,
                 new EOFException("EOF found while reading string"));
            }

            buffer[i] = (char)c;

            c = currentIn.read();
            colIdx++;

            if (c == '\n')
            {
               lineNum_++;
               colIdx = 0;
            }
         }

         return new String(buffer);
      }
      catch (IOException e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_STRING, this, e);
      }
   }

   /**
    * Reads a character from input stream. Skips through any
    * following white space.
    * @return the character
    * @throws InvalidFormatException 
    */
   public char readChar()
      throws InvalidFormatException
   {
      try
      {
        int c = currentIn.read();
        colIdx++;

        if (c == -1)
        {
           throw new EOFException();
        }

        if (c == '\n')
        {
           lineNum_++;
           colIdx = 0;
        }

        while (c == ' ' || c == '\t' || c == '\n' || c == '\r')
        {
           c = currentIn.read();
           colIdx++;

           if (c == -1)
           {
              throw new EOFException();
           }

           if (c == '\n')
           {
              lineNum_++;
              colIdx = 0;
           }
        }
  
        return (char)c;
      }
      catch (IOException e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_CHAR, this, e);
      }
   }

   protected void writeWord(String word)
   {
      currentOut.print(word);
      colIdx += word.length();

      if (colIdx > PREFERRED_COL_WIDTH)
      {
         println();
      }
      else
      {
         currentOut.print(' ');
         colIdx++;
      }
   }

   public void writeString(String string)
   {
      int n = (string == null ? 0 : string.length());

      // Must have a single space following the number
      currentOut.print(String.format(Locale.ROOT, "%d ", n));

      if (n > 0)
      {
         currentOut.println(string);
         colIdx = 0;
      }
      else
      {
         colIdx += 2;
      }
   }

   protected void println()
   {
      currentOut.println();
      colIdx = 0;
   }

   /**
    * Writes boolean value as 0 (if false) or 1 (if true) to output 
    * stream terminated by white space.
    * @param value boolean value
    */
   public void writeBoolean(boolean value)
   {
      writeWord((value?"1":"0"));
   }

   /**
    * Writes the character to output 
    * stream terminated by white space.
    * @param c character
    */
   public void writeChar(char c)
   {
      writeWord(""+c);
   }

   /**
    * Writes the value of an integer to output 
    * stream terminated by white space.
    * @param value number
    */
   public void writeInt(int value)
   {
      writeWord(""+value);
   }

   /**
    * Writes the value of a byte to output 
    * stream terminated by white space.
    * @param value number
    */
   public void writeByte(byte value)
   {
      writeWord(""+value);
   }

   /**
    * Writes the value of a float to output 
    * stream terminated by white space.
    * @param value number
    */
   public void writeFloat(float value)
   {
      writeWord(""+value);
   }

   /**
    * Writes the value of a double to output 
    * stream terminated by white space.
    * @param value number
    */
   public void writeDouble(double value)
   {
      writeWord(""+value);
   }

   /**
    * Gets current line number.
    * The current line number is set to 1 at the start of
    * {@link #load(BufferedReader, CanvasGraphics)} and 
    * incremented each time <code>\n</code> is encountered.
    * @return current line number
    */
   public int getLineNum()
   {
      return lineNum_;
   }

   public int getColumnIndex()
   {
      return colIdx;
   }

   public void mark(int readlimit)
      throws IOException
   {
      currentIn.mark(readlimit);
   }

   public void reset()
      throws IOException
   {
      currentIn.reset();
   }

   /** 
    * Maximum buffer length. 
    *
    * @see #readWord()
    */
   public static int buffLength=255;

   /**
    * Stores the value of the current line number.
    *
    * @see #getLineNum()
    */
   private int lineNum_=0;

   private int colIdx = 0;

   private PrintWriter currentOut;

   protected BufferedReader currentIn;

   Charset encoding = StandardCharsets.UTF_8;

   public static final int PREFERRED_COL_WIDTH = 80;
}
