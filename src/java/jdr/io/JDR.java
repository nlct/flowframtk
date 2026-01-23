// File          : JDR.java
// Purpose       : functions to save and load JDR files
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
import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Functions to save and load JDR files.
 *
 * @author Nicola L C Talbot
 */

public class JDR extends JDRAJR
{
   public JDR()
   {
      super();
   }

   /**
    * Saves all objects in latest JDR format.
    * The way in which the settings are
    * saved depends on the settings flag which may be one of:
    * {@link #NO_SETTINGS} (don't
    * save the settings), {@link #ALL_SETTINGS} (save all
    * settings) or {@link #PAPER_ONLY} (only save the
    * paper size.)
    * <p>
    * None of the current JDR formats allow
    * any linear or radial gradient paint to have a start or
    * end colour that isn't either {@link JDRColor} or
    * {@link JDRColorCMYK}.
    * @param allObjects all objects constituting the image
    * @param out the output stream
    * @param settingsFlag indicate whether to save settings
    * @throws IOException if an I/O error occurs
    */
   public void save(JDRGroup allObjects,
      DataOutputStream out, int settingsFlag)
      throws IOException
   {
      save(allObjects, out, CURRENT_VERSION, settingsFlag);
   }

   /**
    * Saves all objects in given JDR format.
    *
    * The way in which the settings are
    * saved depends on the settings flag which may be one of:
    * {@link #NO_SETTINGS} (don't
    * save the settings), {@link #ALL_SETTINGS} (save all
    * settings) or {@link #PAPER_ONLY} (only save the
    * paper size.)
    * <p>
    * None of the current JDR formats allow
    * any linear or radial gradient paint to have a start or
    * end colour that isn't either {@link JDRColor} or
    * {@link JDRColorCMYK}.
    * @param allObjects all objects constituting the image
    * @param out the output stream
    * @param settingsFlag indicate whether to save settings
    * @throws IOException if an I/O error occurs
    */
   public void save(JDRGroup allObjects, DataOutputStream out,
      float version, int settingsFlag)
      throws IOException
   {
      currentOut = out;
      saveImage(allObjects, version, settingsFlag);
      currentOut = null;
   }

   protected void saveFormatVersion(String versionString) throws IOException
   {
      currentOut.writeChars("JDR");
      writeString(versionString);
   }

   public byte[] toByteArray(JDRGroup selection) throws IOException
   {
      ByteArrayOutputStream bout = null;
      DataOutputStream dout = null;
      byte[] array = null;

      try
      {
         bout = new ByteArrayOutputStream();
         dout = new DataOutputStream(bout);
         save(selection, dout, JDR.NO_SETTINGS);
         array = bout.toByteArray();
      }
      finally
      {
         if (dout != null)
         {
            dout.close();
         }

         if (bout != null)
         {
            bout.close();
         }
      }

      return array;
   }

   public JDRGroup fromByteArray(byte[] array, CanvasGraphics cg)
     throws InvalidFormatException,IOException
   {
      ByteArrayInputStream bin = null;
      DataInputStream din = null;
      JDRGroup grp = null;

      try
      {
         bin = new ByteArrayInputStream(array);
         din = new DataInputStream(bin);
         grp = load(din, canvasGraphics);
      }
      finally
      {
         if (din != null)
         {
            din.close();
         }

         if (bin != null)
         {
            bin.close();
         }
      }

      return grp;
   }

   /**
    * Reads image from JDR formatted file. The
    * image is returned as a {@link JDRGroup}.
    * <p>
    * Any settings found in the file are put in <code>settings</code>
    * which should be initialised prior to calling this method.
    * The settings flag can afterwards be retrieved using 
    * {@link #getLastLoadedSettingsID()} .
    * @param in the input stream
    * @return the image as a <code>JDRGroup</code>.
    * @throws IOException if I/O error occurs
    * @throws InvalidFormatException if file is incorrectly 
    * formatted
    */
   public JDRGroup load(DataInputStream in, 
      CanvasGraphics cg)
      throws InvalidFormatException
   {
      currentIn = in;

      JDRGroup image = loadImage(cg);

      currentIn = null;

      return image;
   }

   @Override
   protected String readFormatVersion()
     throws InvalidFormatException
   {
      String formatId = readString(
         InvalidFormatException.FORMAT_TAG, 3);

      if (!(formatId).equals("JDR"))
      {
         throw new InvalidFormatException(
           getMessageSystem().getMessageWithFallback(
             "invalid_"+InvalidFormatException.INVALID_JDR_FORMAT,
             "Invalid JDR file format"), this);
      }

      return readString(
         InvalidFormatException.VERSION);
   }

   @Override
   protected void readPostVersion()
     throws InvalidFormatException
   {
   }

   public boolean readBoolean()
     throws InvalidFormatException
   {
      try
      {
         return currentIn.readBoolean();
      }
      catch (IOException e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_BOOLEAN, this, e);
      }
   }

   public int readInt()
     throws InvalidFormatException
   {
      try
      {
         return currentIn.readInt();
      }
      catch (IOException e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_INT, this, e);
      }
   }

   public char readChar()
     throws InvalidFormatException
   {
      try
      {
         return currentIn.readChar();
      }
      catch (IOException e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_CHAR, this, e);
      }
   }

   public String readString(int n)
     throws InvalidFormatException
   {
      char[] str = new char[n];

      for (int i = 0; i < n; i++)
      {
         try
         {
            str[i] = currentIn.readChar();
         }
         catch (IOException e)
         {
            throw new InvalidValueException(
              InvalidFormatException.PARSE_STRING, this, e);
         }
      }

      return new String(str);
   }

   public byte readByte()
     throws InvalidFormatException
   {
      try
      {
         return currentIn.readByte();
      }
      catch (IOException e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_BYTE, this, e);
      }
   }

   public float readFloat()
     throws InvalidFormatException
   {
      try
      {
         return currentIn.readFloat();
      }
      catch (IOException e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_FLOAT, this, e);
      }
   }

   public double readDouble()
     throws InvalidFormatException
   {
      try
      {
         return currentIn.readDouble();
      }
      catch (IOException e)
      {
         throw new InvalidValueException(
           InvalidFormatException.PARSE_DOUBLE, this, e);
      }
   }

   public void writeBoolean(boolean value)
      throws IOException
   {
      currentOut.writeBoolean(value);
   }

   public void writeInt(int value)
      throws IOException
   {
      currentOut.writeInt(value);
   }

   public void writeChar(char value)
      throws IOException
   {
      currentOut.writeChar(value);
   }

   public void writeByte(byte value)
      throws IOException
   {
      currentOut.writeByte(value);
   }

   public void writeFloat(float value)
      throws IOException
   {
      currentOut.writeFloat(value);
   }

   public void writeDouble(double value)
      throws IOException
   {
      currentOut.writeDouble(value);
   }

   public void writeString(String string)
      throws IOException
   {
      int n = string == null ? 0 : string.length();

      currentOut.writeInt(n);

      if (n > 0)
      {
         currentOut.writeChars(string);
      }
   }

   public int getLineNum()
   {
      return -1;
   }

   public int getColumnIndex()
   {
      return -1;
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

   protected DataInputStream currentIn;

   private DataOutputStream currentOut;
}
