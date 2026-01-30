// File          : JDRAJR.java
// Purpose       : functions to save and load JDR files
// Creation Date : 2014-03-26
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
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

public abstract class JDRAJR
{
   protected JDRAJR()
   {
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      canvasGraphics = cg;
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   protected abstract void saveFormatVersion(String versionString)
      throws IOException;

   /**
    * Saves all objects in given JDR/AJR format.
    *
    * The way in which the settings are
    * saved depends on the settings flag which may be one of:
    * {@link #NO_SETTINGS} (don't
    * save the settings), {@link #ALL_SETTINGS} (save all
    * settings) or {@link #PAPER_ONLY} (only save the
    * paper size.)
    * <p>
    * None of the current JDR/AJR formats allow
    * any linear or radial gradient paint to have a start or
    * end colour that isn't either {@link JDRColor} or
    * {@link JDRColorCMYK}.
    * @param allObjects all objects constituting the image
    * @param settingsFlag indicate whether to save settings
    * @throws InvalidFormatException 
    */
   protected void saveImage(JDRGroup allObjects,
      float version, int settingsFlag)
      throws IOException
   {
      CanvasGraphics cg = allObjects.getCanvasGraphics();

      setCanvasGraphics(cg);

      this.settingsFlag = settingsFlag;
      this.version = version;
      cg.setBitmapReplaced(false);

      addAllListeners();

      String vers = null;

      for (int i = 0; i < VALID_VERSIONS.length; i++)
      {
         if (version == VALID_VERSIONS[i])
         {
            vers = VALID_VERSIONS_STRING[i];
            versionId = i;
            break;
         }
      }

      if (vers == null)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.VERSION, version, cg);
      }

      saveFormatVersion(vers);

      if (version >= 1.8f)
      {
         // save storage unit.

         writeByte((byte)cg.getStorageUnit().getID());

         if (version >= 2.1f)
         {
            writeDouble(cg.getOriginX());
            writeDouble(cg.getOriginY());
         }
      }
      else if (cg.getStorageUnit().getID() != JDRUnit.BP)
      {
         String label = cg.getStorageUnit().getLabel();

         warningMessage("Storage unit ''{0}'' not supported in JDR/AJR version {1}",
           "warning.save_unsupported_storage_unit", label, version);
      }

      switch (settingsFlag)
      {
         case NO_SETTINGS:
           // don't save settings

           if (version < 1.3f)
           {
              writeBoolean(false);
           }
           else
           {
              writeByte((byte)NO_SETTINGS);
           }

         break;
         case ALL_SETTINGS :

           if (version < 1.3f)
           {
              writeBoolean(true);
           }
           else
           {
              writeByte((byte)ALL_SETTINGS);
           }

           cg.save(this);

         break;
         case PAPER_ONLY :

           if (version < 1.3f)
           {
              writeBoolean(false);
           }
           else
           {
              writeByte((byte)PAPER_ONLY);
              cg.getPaper().save(this);
           }

         break;
         default :
            throw new JdrIllegalArgumentException(
              JdrIllegalArgumentException.SETTINGS_ID,
              settingsFlag, cg);
      }

      if (version >= 1.8f)
      {
         // Save preamble, document class and absolute pages setting

         writeInt((int)cg.getLaTeXNormalSize());
         writeString(cg.getPreamble());

         if (version >= 1.9f)
         {
            writeString(cg.getMidPreamble());
            writeString(cg.getEndPreamble());

            if (version >= 2.1f)
            {
               // Save document body and magic comments
               writeString(cg.getDocBody());
               writeString(cg.getMagicComments());
            }
            else
            {
               if (cg.hasDocBody())
               {
                  warningMessage("Document body not supported in JDR/AJR version {0}",
                    "warning.save_unsupported_doc_body", version);
               }

               if (cg.hasMagicComments())
               {
                  warningMessage("Magic comments not supported in JDR/AJR version {0}",
                    "warning.save_unsupported_magic_comments", version);
               }
            }
         }
         else
         {
            if (cg.hasMidPreamble() || cg.hasEndPreamble())
            {
               warningMessage("Mid/End Preamble not supported in JDR/AJR version {0}",
                 "warning.save_unsupported_extra_preamble", version);
            }

            if (cg.hasDocBody())
            {
               warningMessage("Document body not supported in JDR/AJR version {0}",
                 "warning.save_unsupported_doc_body", version);
            }
         }

         writeString(cg.getDocClass());
         writeBoolean(cg.useAbsolutePages());
      }
      else
      {
         if (cg.hasPreamble() || cg.hasMidPreamble() || cg.hasEndPreamble())
         {
            warningMessage("Preamble not supported in JDR/AJR version {0}",
              "warning.save_unsupported_preamble", version);
         }

         if (cg.hasDocBody())
         {
            warningMessage("Document body not supported in JDR/AJR version {0}",
              "warning.save_unsupported_doc_body", version);
         }

         if (cg.hasDocClass())
         {
            warningMessage("Document class not supported in JDR/AJR version {0}",
              "warning.save_unsupported_docclass", version);
         }
      }

      objectLoader.save(this, allObjects);
   }

   public static String getFileFormat(CanvasGraphics cg, File file)
     throws IOException,InvalidFormatException
   {
      FileInfo info = FileInfo.parseFile(cg, file);

      return info.getFileFormat();
   }

   protected abstract String readFormatVersion()
     throws InvalidFormatException;

   protected abstract void readPostVersion()
     throws InvalidFormatException;

   protected JDRGroup loadImage(CanvasGraphics cg)
      throws InvalidFormatException
   {
      this.version = lastLoadedVersion;
      settingsFlag = lastLoadedSettings;

      setCanvasGraphics(cg);

      // add listeners if not already done so
      addAllListeners();

      cg.setBitmapReplaced(false);
      setDraftBitmap(false);
      String thisFileVersion = readFormatVersion();

      boolean found = false;

      for (int i = 0; i < VALID_VERSIONS.length; i++)
      {
         if (thisFileVersion.equals(VALID_VERSIONS_STRING[i]))
         {
            version = VALID_VERSIONS[i];
            versionId = i;
            found = true;
            break;
         }
      }

      if (!found)
      {
         throw new JdrIllegalArgumentException(
          JdrIllegalArgumentException.VERSION, thisFileVersion, this);
      }

      readPostVersion();
      readStorageUnit();
      readSettings();
      readLaTeXSettings();

      JDRGroup image = loadObjects();

      lastLoadedVersion = version;
      lastLoadedSettings = settingsFlag;

      return image;
   }

   protected void readStorageUnit()
      throws InvalidFormatException
   {
      if (version >= 1.8f)
      {
         byte id = readByte(InvalidFormatException.UNIT_ID);

         try
         {
            canvasGraphics.setStorageUnit(id);
         }
         catch (IllegalArgumentException e)
         {
            throw new InvalidValueException(
               InvalidValueException.UNIT_ID, id, this, e);
         }

         if (version >= 2.1f)
         {
            double x = readDouble(InvalidFormatException.ORIGIN_X);
            double y = readDouble(InvalidFormatException.ORIGIN_Y);
         }
      }
      else
      {
         canvasGraphics.setStorageUnit(JDRUnit.BP);
      }
   }

   protected void readSettings()
      throws InvalidFormatException
   {
      if (version < 1.3f)
      {
         settingsFlag = 
          (readBoolean(InvalidFormatException.SETTINGS_ID) 
           ? ALL_SETTINGS : NO_SETTINGS);
      }
      else
      {
         settingsFlag = (int)readByte(InvalidFormatException.SETTINGS_ID);
      }

      if (settingsFlag == ALL_SETTINGS)
      {
         canvasGraphics.read(this);
      }
      else if (settingsFlag == PAPER_ONLY)
      {
         canvasGraphics.setPaper(JDRPaper.read(this));
      }
      else if (settingsFlag != NO_SETTINGS)
      {
         throw new InvalidValueException(
            InvalidFormatException.SETTINGS_ID, settingsFlag, this);
      }

   }

   protected void readLaTeXSettings()
      throws InvalidFormatException
   {
      if (version >= 1.8f)
      {
         // Read preamble, document body, document class and absolute pages setting

         canvasGraphics.setLaTeXNormalSize(readIntGt(
            InvalidFormatException.SETTING_NORMALSIZE, 0));

         canvasGraphics.setPreamble(
            readString(InvalidFormatException.SETTING_PREAMBLE));

         if (version >= 1.9f)
         {
            canvasGraphics.setMidPreamble(
              readString(InvalidFormatException.SETTING_MID_PREAMBLE));

            canvasGraphics.setEndPreamble(
              readString(InvalidFormatException.SETTING_END_PREAMBLE));

            if (version >= 2.1f)
            {
               canvasGraphics.setDocBody(
                 readString(InvalidFormatException.SETTING_DOC_BODY));

               canvasGraphics.setMagicComments(
                 readString(InvalidFormatException.SETTING_MAGIC_COMMENTS));
            }
         }

         canvasGraphics.setDocClass(
           readString(InvalidFormatException.SETTING_DOCCLASS));

         canvasGraphics.setUseAbsolutePages(
           readBoolean(InvalidFormatException.SETTING_ABS_PAGES));
      }
   }

   protected JDRGroup loadObjects()
      throws InvalidFormatException
   {
      JDRObject allObjects = null;

      try
      {
         allObjects = objectLoader.load(this);
      }
      catch (JdrIllegalArgumentException e)
      {
         throw new InvalidFormatException(
            e.getMessage(), this, e);
      }

      if (!(allObjects instanceof JDRGroup))
      {
         throw new JDRMissingTopLevelException(canvasGraphics);
      }

      return (JDRGroup)allObjects;
   }

   /**
    * Adds all the supported listeners if not already done.
    */
    protected static void addAllListeners()
    {
       if (paintLoader.getListeners().isEmpty())
       {
          paintLoader.addListener(new JDRColorListener());
          paintLoader.addListener(new JDRTransparentListener());
          paintLoader.addListener(new JDRColorCMYKListener());
          paintLoader.addListener(new JDRGradientListener());
          paintLoader.addListener(new JDRRadialListener());
          paintLoader.addListener(new JDRGrayListener());
          paintLoader.addListener(new JDRColorHSBListener());
       }

       if (objectLoader.getListeners().isEmpty())
       {
          objectLoader.addListener(new JDRGroupListener());
          objectLoader.addListener(new JDRPathListener());
          objectLoader.addListener(new JDRTextListener());
          objectLoader.addListener(new JDRBitmapListener());
          objectLoader.addListener(new JDRTextPathListener());
          objectLoader.addListener(new JDRSymmetricPathListener());
          objectLoader.addListener(new JDRRotationalPatternListener());
          objectLoader.addListener(new JDRScaledPatternListener());
          objectLoader.addListener(new JDRSpiralPatternListener());
       }

       if (segmentLoader.getListeners().isEmpty())
       {
          segmentLoader.addListener(new JDRSegmentLoaderListener());
          segmentLoader.addListener(new JDRLineLoaderListener());
          segmentLoader.addListener(new JDRBezierLoaderListener());
          segmentLoader.addListener(new JDRClosingMoveLoaderListener());
          segmentLoader.addListener(new JDRPartialSegmentLoaderListener());
          segmentLoader.addListener(new JDRPartialLineLoaderListener());
          segmentLoader.addListener(new JDRPartialBezierLoaderListener());
       }

       if (pathStyleLoader.getListeners().isEmpty())
       {
          pathStyleLoader.addListener(new JDRBasicPathStyleListener());
          pathStyleLoader.addListener(new JDRTextPathStyleListener());
       }

       if (gridLoader.getListeners().isEmpty())
       {
          gridLoader.addListener(new JDRRectangularGridListener());
          gridLoader.addListener(new JDRRadialGridListener());
          gridLoader.addListener(new JDRIsoGridListener());
          gridLoader.addListener(new JDRTschicholdGridListener());
          gridLoader.addListener(new JDRPathGridListener());
       }

    }

   public static JDRPaintLoader getPaintLoader()
   {
      addAllListeners();
      return paintLoader;
   }

   public static JDRObjectLoader getObjectLoader()
   {
      addAllListeners();
      return objectLoader;
   }

   public static JDRSegmentLoader getSegmentLoader()
   {
      addAllListeners();
      return segmentLoader;
   }

   public static JDRPathStyleLoader getPathStyleLoader()
   {
      addAllListeners();
      return pathStyleLoader;
   }

   public static JDRGridLoader getGridLoader()
   {
      addAllListeners();
      return gridLoader;
   }

   public void writeLength(JDRLength length)
     throws IOException
   {
      writeDouble(length.getValue());
      writeInt(length.getUnit().getID());
   }

   public JDRLength readLength()
      throws InvalidFormatException
   {
      double val = readDouble(
        InvalidFormatException.LENGTH);

      int id = readInt(
        InvalidFormatException.UNIT_ID);

      JDRUnit unit = JDRUnit.getUnit(id);

      if (unit == null)
      {
         throw new InvalidValueException(
            InvalidFormatException.UNIT_ID, id, this);
      }

      return new JDRLength(getCanvasGraphics(), val, unit);
   }

   public JDRLength readLength(String identifier)
      throws InvalidFormatException
   {
      double val = readDouble(
        identifier+"-"+InvalidFormatException.LENGTH);

      int id = readInt(
        identifier+"-"+InvalidFormatException.UNIT_ID);

      JDRUnit unit = JDRUnit.getUnit(id);

      if (unit == null)
      {
         throw new InvalidValueException(
            identifier+"-"+InvalidFormatException.UNIT_ID, id, this);
      }

      return new JDRLength(getCanvasGraphics(), val, unit);
   }

   public void readLength(String identifier, JDRLength length)
      throws InvalidFormatException
   {
      double val = readDouble(
        identifier+"-"+InvalidFormatException.LENGTH);

      int id = readInt(
        identifier+"-"+InvalidFormatException.UNIT_ID);

      JDRUnit unit = JDRUnit.getUnit(id);

      if (unit == null)
      {
         throw new InvalidValueException(
            identifier+"-"+InvalidFormatException.UNIT_ID, id, this);
      }

      length.setValue(val, unit);
   }

   public JDRLength readNonNegLength(String identifier)
      throws InvalidFormatException
   {
      JDRLength length = readLength(identifier);

      if (length.getValue() < 0)
      {
         throw new InvalidValueException(
            identifier, length, this);
      }

      return length;
   }

   public JDRAngle readAngle(String identifier)
      throws InvalidFormatException
   {
      double val = readDouble(
        identifier+"-"+InvalidFormatException.ANGLE_VALUE);

      byte id = readByte(
        identifier+"-"+InvalidFormatException.ANGLE_ID);

      try
      {
         return new JDRAngle(getCanvasGraphics(), val, id);
      }
      catch (JdrIllegalArgumentException e)
      {
         throw new InvalidValueException(
            identifier+"-"+InvalidFormatException.ANGLE_ID, id, this, e);
      }
   }

   public void writeAngle(JDRAngle angle)
     throws IOException
   {
      writeDouble(angle.getValue());
      writeByte(angle.getUnitId());
   }

   public String readString(String identifier)
     throws InvalidFormatException
   {
      try
      {
         return readString();
      }
      catch (InvalidFormatException e)
      {
         e.setIdentifier(identifier);
         throw e;
      }
   }

   public String readString(String identifier, int n)
     throws InvalidFormatException
   {
      try
      {
         return readString(n);
      }
      catch (InvalidFormatException e)
      {
         e.setIdentifier(identifier);
         throw e;
      }
   }

   public String readString()
     throws InvalidFormatException
   {
      int n = readInt();

      if (n < 0)
      {
         throw new InvalidArrayLengthException(n, this);
      }

      return n == 0 ? "" : readString(n);
   }

   public abstract String readString(int n)
      throws InvalidFormatException;

   public boolean readBoolean(String identifier)
     throws InvalidFormatException
   {
      try
      {
         return readBoolean();
      }
      catch (InvalidFormatException e)
      {
         e.setIdentifier(identifier);
         throw e;
      }
   }

   public abstract boolean readBoolean()
     throws InvalidFormatException;

   public int readInt(String identifier, int min, int max,
      boolean minInclusive, boolean maxInclusive)
     throws InvalidFormatException
   {
      int value = readInt(identifier);

      if ( (minInclusive ? value < min : value <= min)
        || (maxInclusive ? value > max : value >= max)
         )
      {
         throw new InvalidValueException(identifier, value, this);
      }

      return value;
   }

   public int readIntGe(String identifier, int min)
     throws InvalidFormatException
   {
      int value = readInt(identifier);

      if (value < min)
      {
         throw new InvalidValueException(identifier, value, this);
      }

      return value;
   }

   public int readIntGt(String identifier, int min)
     throws InvalidFormatException
   {
      int value = readInt(identifier);

      if (value <= min)
      {
         throw new InvalidValueException(identifier, value, this);
      }

      return value;
   }

   public int readInt(String identifier)
     throws InvalidFormatException
   {
      try
      {
         return readInt();
      }
      catch (InvalidFormatException e)
      {
         e.setIdentifier(identifier);
         throw e;
      }
   }

   public abstract int readInt()
     throws InvalidFormatException;

   public char readChar(String identifier)
     throws InvalidFormatException
   {
      try
      {
         return readChar();
      }
      catch (InvalidFormatException e)
      {
         e.setIdentifier(identifier);
         throw e;
      }
   }

   public abstract char readChar()
     throws InvalidFormatException;

   public byte readByte(String identifier, int min, int max,
      boolean minInclusive, boolean maxInclusive)
     throws InvalidFormatException
   {
      return readByte(identifier, (byte)min, (byte)max,
         minInclusive, maxInclusive);
   }

   public byte readByteGe(String identifier, int min)
     throws InvalidFormatException
   {
      return readByteGe(identifier, (byte)min);
   }

   public byte readByteGe(String identifier, byte min)
     throws InvalidFormatException
   {
      byte value = readByte(identifier);

      if (value < min)
      {
         throw new InvalidValueException(identifier, value, this);
      }

      return value;
   }

   public byte readByte(String identifier, byte min, byte max,
      boolean minInclusive, boolean maxInclusive)
     throws InvalidFormatException
   {
      byte value = readByte(identifier);

      if ( (minInclusive ? value < min : value <= min)
        || (maxInclusive ? value > max : value >= max)
         )
      {
         throw new InvalidValueException(identifier, value, this);
      }

      return value;
   }

   public byte readByte(String identifier)
     throws InvalidFormatException
   {
      try
      {
         return readByte();
      }
      catch (InvalidFormatException e)
      {
         e.setIdentifier(identifier);
         throw e;
      }
   }

   public abstract byte readByte()
     throws InvalidFormatException;

   public float readFloat(String identifier, float min, float max,
      boolean minInclusive, boolean maxInclusive)
     throws InvalidFormatException
   {
      float value = readFloat(identifier);

      if ( (minInclusive ? value < min : value <= min)
        || (maxInclusive ? value > max : value >= max)
         )
      {
         throw new InvalidValueException(identifier, value, this);
      }

      return value;
   }

   public float readFloatGe(String identifier, float min)
     throws InvalidFormatException
   {
      float value = readFloat(identifier);

      if (value < min)
      {
         throw new InvalidValueException(identifier, value, this);
      }

      return value;
   }

   public float readFloat(String identifier)
     throws InvalidFormatException
   {
      try
      {
         return readFloat();
      }
      catch (InvalidFormatException e)
      {
         e.setIdentifier(identifier);
         throw e;
      }
   }

   public abstract float readFloat()
     throws InvalidFormatException;

   public double readDoubleGe(String identifier, double min)
     throws InvalidFormatException
   {
      double value = readDouble(identifier);

      if (value < min)
      {
         throw new InvalidValueException(identifier, value, this);
      }

      return value;
   }

   public double readDoubleGt(String identifier, double min)
     throws InvalidFormatException
   {
      double value = readDouble(identifier);

      if (value <= min)
      {
         throw new InvalidValueException(identifier, value, this);
      }

      return value;
   }

   public double readDouble(String identifier)
     throws InvalidFormatException
   {
      try
      {
         return readDouble();
      }
      catch (InvalidFormatException e)
      {
         e.setIdentifier(identifier);
         throw e;
      }
   }

   public abstract double readDouble()
     throws InvalidFormatException;

   public double[] readTransform(String identifier)
     throws InvalidFormatException
   {
      double[] matrix = new double[6];

      for (int i = 0; i < 6; i++)
      {
         matrix[i] = readDouble(identifier+"["+i+"]");
      }

      return matrix;
   }

   public double[] readTransform()
      throws InvalidFormatException
   {
      double[] matrix = new double[6];

      for (int i = 0; i < 6; i++)
      {
         matrix[i] = readDouble();
      }

      return matrix;
   }

   public float[] readFloatArray(String identifier)
     throws InvalidFormatException
   {
      int n = readInt(identifier);

      if (n == 0) return null;

      if (n < 0)
      {
         throw new InvalidArrayLengthException(
            identifier, n, this);
      }

      float[] array = new float[n];

      for (int i = 0; i < n; i++)
      {
         array[i] = readFloat(identifier+"["+i+"]");
      }

      return array;
   }

   public void writeArray(float[] array)
      throws IOException
   {
      if (array == null)
      {
         writeInt(0);
         return;
      }

      int n = array.length;

      writeInt(n);

      for (int i = 0; i < n; i++)
      {
         writeFloat(array[i]);
      }
   }

   public abstract void writeBoolean(boolean value)
      throws IOException;

   public abstract void writeByte(byte value)
      throws IOException;

   public abstract void writeChar(char c)
      throws IOException;

   public abstract void writeInt(int c)
      throws IOException;

   public abstract void writeFloat(float c)
      throws IOException;

   public abstract void writeDouble(double c)
      throws IOException;

   public abstract void writeString(String string)
      throws IOException;

   public void writeTransform(double[] matrix)
      throws IOException
   {
      if (getVersion() < 1.8f)
      {
         double factor = getCanvasGraphics().storageToBp(1.0);

         for (int i = 0; i < 4; i++)
         {
            writeDouble(matrix[i]);
         }

         writeDouble(factor*matrix[4]);
         writeDouble(factor*matrix[5]);
      }
      else
      {
         for (int i = 0; i < 6; i++)
         {
            writeDouble(matrix[i]);
         }
      }
   }

   public void writeTransform(AffineTransform af)
      throws IOException
   {
      double[] matrix = new double[6];
      af.getMatrix(matrix);
      writeTransform(matrix);
   }

   /**
    * Writes a Java Shape with no additional information. 
    * @param shape the shape to save
    */ 
   public void writeShape(Shape shape)
     throws IOException
   {
      PathIterator pi = shape.getPathIterator(null);

      double[] coords = new double[6];

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         writeInt(type);

         switch (type)
         {
            case PathIterator.SEG_MOVETO :
            case PathIterator.SEG_LINETO :
               writeDouble(coords[0]);
               writeDouble(coords[1]);
            break;
            case PathIterator.SEG_QUADTO :
               writeDouble(coords[0]);
               writeDouble(coords[1]);
               writeDouble(coords[2]);
               writeDouble(coords[3]);
            break;
            case PathIterator.SEG_CUBICTO :
               writeDouble(coords[0]);
               writeDouble(coords[1]);
               writeDouble(coords[2]);
               writeDouble(coords[3]);
               writeDouble(coords[4]);
               writeDouble(coords[5]);
            break;
         }

         pi.next();
      }

      writeInt(-1);// end of path
   }

   public Shape readShape()
     throws InvalidFormatException
   {
      Path2D path = new Path2D.Double();

      int type = readInt(InvalidFormatException.SEGMENT_ID);

      while (type != -1)
      {
         switch (type)
         {
            case PathIterator.SEG_MOVETO :
               path.moveTo(readDouble(InvalidFormatException.LINE_X1),
                  readDouble(InvalidFormatException.LINE_Y1));
            break;
            case PathIterator.SEG_LINETO :
               path.lineTo(readDouble(InvalidFormatException.LINE_X1),
                  readDouble(InvalidFormatException.LINE_Y1));
            break;
            case PathIterator.SEG_QUADTO :
               path.quadTo(readDouble(InvalidFormatException.BEZIER_C1X), 
                 readDouble(InvalidFormatException.BEZIER_C1Y), 
                 readDouble(InvalidFormatException.BEZIER_X1), 
                 readDouble(InvalidFormatException.BEZIER_Y1));
            break;
            case PathIterator.SEG_CUBICTO :
               path.curveTo(readDouble(InvalidFormatException.BEZIER_C1X), 
                 readDouble(InvalidFormatException.BEZIER_C1Y), 
                 readDouble(InvalidFormatException.BEZIER_C2X),
                 readDouble(InvalidFormatException.BEZIER_C2Y),
                 readDouble(InvalidFormatException.BEZIER_X1),
                 readDouble(InvalidFormatException.BEZIER_Y1));
            break;
            case PathIterator.SEG_CLOSE :
               path.closePath();
            break;
            default:
               throw new InvalidValueException(
                 InvalidFormatException.SEGMENT_ID, type, this);
         }

         type = readInt(InvalidFormatException.SEGMENT_ID);
      }

      return path;
   }

   public float getVersion()
   {
      return this.version;
   }

   public int getVersionId()
   {
      return versionId;
   }

   public int getSettingsFlag()
   {
      return settingsFlag;
   }

   public void setDraftBitmap(boolean flag)
   {
      hasDraftBitmap = flag;
   }

   public boolean hasDraftBitmap()
   {
      return hasDraftBitmap;
   }

   public JDRMessage getMessageSystem()
   {
      return getCanvasGraphics().getMessageSystem();
   }

   public JDRMessageDictionary getMessageDictionary()
   {
      return getCanvasGraphics().getMessageDictionary();
   }

   @Deprecated
   public void warning(String tag, String alt)
   {
      warningWithFallback(tag, alt);
   }

   @Deprecated
   public void warningMessage(String altFormat, String tag, Object... values)
   {
      warningWithFallback(tag, altFormat, values);
   }

   public void warningWithFallback(String tag, String altFormat, Object... values)
   {
      JDRMessage msg = getMessageSystem();

      if (msg == null)
      {
         System.out.println(MessageFormat.format(altFormat, values));
      }
      else
      {
         msg.getPublisher().publishMessages(
          MessageInfo.createWarning(msg.getMessageWithFallback(tag, altFormat, values)));
      }
   }

   /**
    * Gets the version number of last AJR file to be loaded.
    * If no AJR file has been loaded using 
    * {@link #loadImage(CanvasGraphics)}, 0 is returned.
    * @return version number or 0 if no AJR file has been loaded
    */
   public static float getLastLoadedVersion()
   {
      return lastLoadedVersion;
   }

   /**
    * Gets the settings ID from last AJR file to be loaded.
    * The settings ID will be one of: {@link JDR#NO_SETTINGS},
    * {@link JDR#ALL_SETTINGS}, {@link JDR#PAPER_ONLY} or
    * -1 if no AJR file has been loaded using
    * {@link #loadImage(CanvasGraphics)}.
    * @return settings ID or -1 if no AJR file has been loaded
    */
   public static int getLastLoadedSettingsID()
   {
      return lastLoadedSettings;
   }

   public void setBaseDir(File dir)
   {
      basePath = (dir == null ? null : dir.toPath());
   }

   public void setBasePath(Path path)
   {
      basePath = path;
   }

   public Path getBasePath()
   {
      return basePath;
   }

   public File resolveFile(URI uri)
   {
      return resolveFile(new File(uri));
   }

   public File resolveFile(File file)
   {
      return basePath == null ? file : basePath.resolve(file.toPath()).toFile();
   }

   public File resolveFile(String filename)
   {
      return basePath == null ? new File(filename) :
             basePath.resolve(filename).toFile();
   }

   public Path resolvePath(String filename)
   {
      return basePath == null ? (new File(filename)).toPath() :
             basePath.resolve(filename);
   }

   public Path resolvePath(Path path)
   {
      return basePath == null ? path : basePath.resolve(path);
   }

   public Path relativizePath(String filename)
   {
      Path path = new File(filename).toPath();

      if (basePath == null || !path.isAbsolute())
      {
         return path;
      }

      return basePath.relativize(path);
   }

   public Path relativizePath(Path path)
   {
      if (basePath == null || !path.isAbsolute())
      {
         return path;
      }

      Path relPath = basePath.relativize(path);

      // check that it can be resolved back

      Path resolved = resolvePath(relPath);

      if (!Files.exists(resolved))
      {
         // The base path may be a symlink

         return path;
      }

      return relPath;
   }

   public static class FileInfo
   {
      public static FileInfo parseFile(JDRMessage msgSys, File file)
        throws IOException,InvalidFormatException
      {
         return parseFile(new CanvasGraphics(msgSys), file);
      }

      public static FileInfo parseFile(CanvasGraphics cg, File file)
        throws IOException,InvalidFormatException
      {
         return parseFile(cg, file, FORMAT_FLAG);
      }

      public static FileInfo parseFile(CanvasGraphics cg, File file, int infoFlags)
        throws IOException,InvalidFormatException
      {
         FileInfo info = new FileInfo();

         info.canvasGraphics = cg;
         info.file = file;
         info.infoFlags = infoFlags;

         info.parseFile();

         return info;
      }

      protected void parseFile()
        throws IOException,InvalidFormatException
      {
         jdrAjr = null;

         AJR ajr = new AJR();
         ajr.setCanvasGraphics(canvasGraphics);
         ajr.currentIn = null;

         try
         {
            ajr.currentIn = Files.newBufferedReader(file.toPath());
            String string = ajr.readString(3);

            if (string.equals("AJR"))
            {
               formatInfo = string;
               jdrAjr = ajr;

               encoding = StandardCharsets.UTF_8;
               String thisFileVersion = ajr.readFormatVersion(true);

               read(thisFileVersion);
            }
         }
         catch (FileNotFoundException e)
         {
            throw e;
         }
         catch (InvalidValueException e)
         {
            // not AJR
         }
         catch (IOException e)
         {
            // maybe character encoding if binary file
            canvasGraphics.debugMessage(e);
         }
         finally
         {
            if (ajr.currentIn != null)
            {
               ajr.currentIn.close();
            }
         }

         if (jdrAjr == null)
         {
            JDR jdr = new JDR();
            jdr.setCanvasGraphics(canvasGraphics);
            jdr.currentIn = null;

            try
            {
               jdr.currentIn = new DataInputStream(new FileInputStream(file));

               jdrAjr = jdr;
               formatInfo = "JDR";

               String thisFileVersion = jdr.readFormatVersion();

               read(thisFileVersion);
            }
            finally
            {
               if (jdr.currentIn != null)
               {
                  jdr.currentIn.close();
               }
            }
         }
      }

      protected void read(String thisFileVersion)
        throws IOException,InvalidFormatException
      {
         boolean found = false;

         for (int i = 0; i < VALID_VERSIONS.length; i++)
         {
            if (thisFileVersion.equals(VALID_VERSIONS_STRING[i]))
            {
               jdrAjr.version = VALID_VERSIONS[i];
               jdrAjr.versionId = i;
               found = true;
               break;
            }
         }

         if (!found)
         {
            throw new JdrIllegalArgumentException(
             JdrIllegalArgumentException.VERSION, thisFileVersion, jdrAjr);
         }

         versionString = thisFileVersion;
         formatInfo += " " + versionString;

         AJR ajr = getAJR();

         try
         {
            jdrAjr.readPostVersion();
         }
         catch (MismatchedEncodingException mme)
         {
            encoding = mme.getFound();

            if (ajr != null)
            {
               ajr.currentIn.close();

               ajr.currentIn = Files.newBufferedReader(file.toPath(), encoding);
               ajr.readFormatVersion();
               ajr.readPostVersion();
            }
         }

         if (ajr != null && ajr.version >= 2.2)
         {
            formatInfo += " " + encoding;
         }

         if (((infoFlags | ANY_DETAILS_FLAG) & ANY_DETAILS_FLAG) == ANY_DETAILS_FLAG)
         {
            jdrAjr.addAllListeners();
            jdrAjr.readStorageUnit();
            jdrAjr.readSettings();

            if (((infoFlags | LATEX_OR_IMAGE_FLAG) & LATEX_OR_IMAGE_FLAG)
                  == LATEX_OR_IMAGE_FLAG )
            {
               jdrAjr.readLaTeXSettings();

               if ((infoFlags & IMAGE_FLAG) == IMAGE_FLAG)
               {
                  image = jdrAjr.loadObjects();
               }
            }
         }
      }

      public boolean isAJR()
      {
         return jdrAjr instanceof AJR;
      }

      public AJR getAJR()
      {
         return isAJR() ? (AJR)jdrAjr : null;
      }

      public Charset getEncoding()
      {
         return isAJR() ? encoding : null;
      }

      public boolean isJDR()
      {
         return jdrAjr instanceof JDR;
      }

      public JDR getJDR()
      {
         return isJDR() ? (JDR)jdrAjr : null;
      }

      public String getFileFormat()
      {
         return formatInfo;
      }

      public CanvasGraphics getCanvasGraphics()
      {
         return canvasGraphics;
      }

      public JDRGroup getImage()
      {
         return image;
      }

      JDRAJR jdrAjr;
      String formatInfo, versionString;
      File file;
      CanvasGraphics canvasGraphics;
      Charset encoding;
      int infoFlags;
      JDRGroup image;

      public static final int INFO_FORMAT = 0;
      public static final int INFO_STORAGE_UNIT = 1;
      public static final int INFO_SETTINGS = 2;
      public static final int INFO_LATEX_SETTINGS = 3;
      public static final int INFO_IMAGE = 4;

      public static final int FORMAT_FLAG = (1 << INFO_FORMAT);
      public static final int STORAGE_UNIT_FLAG = (1 << INFO_STORAGE_UNIT);
      public static final int SETTINGS_FLAG = (1 << INFO_SETTINGS);
      public static final int LATEX_SETTINGS_FLAG = (1 << INFO_LATEX_SETTINGS);
      public static final int IMAGE_FLAG = (1 << INFO_IMAGE);

      public static final int LATEX_OR_IMAGE_FLAG =
       (
          LATEX_SETTINGS_FLAG
        | IMAGE_FLAG
       );

      public static final int ANY_DETAILS_FLAG =
       (
          STORAGE_UNIT_FLAG
        | SETTINGS_FLAG
        | LATEX_OR_IMAGE_FLAG
       );
   }

   public abstract int getLineNum();

   public abstract int getColumnIndex();

   public abstract void mark(int readlimit) throws IOException;

   public abstract void reset() throws IOException;

   protected float version;
   protected int versionId=-1;// index 

   private Path basePath;

   protected int settingsFlag;

   /**
    * Newest JDR/AJR version number.
    */
   public static final float CURRENT_VERSION = 2.1f;// latest stable version

   public static final float[] VALID_VERSIONS
    = {1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f, 1.7f, 1.8f, 1.9f, 2.0f, 2.1f, 2.2f};

   public static final String[] VALID_VERSIONS_STRING
    = {"1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9", "2.0", "2.1", "2.2"};

   /**
    * No canvas settings saved in JDR file.
    */
   public static final int NO_SETTINGS=0;
   /**
    * All canvas settings saved in JDR file.
    *
    * @see CanvasGraphics
    */
   public static final int ALL_SETTINGS=1;
   /**
    * Only required paper saved in JDR file.
    */
   public static final int PAPER_ONLY=2;

   private static JDRPaintLoader paintLoader = new JDRPaintLoader();

   private static JDRObjectLoader objectLoader = new JDRObjectLoader();

   private static JDRSegmentLoader segmentLoader = new JDRSegmentLoader();

   private static JDRPathStyleLoader pathStyleLoader = new JDRPathStyleLoader();

   private static JDRGridLoader gridLoader = new JDRGridLoader();



   protected boolean hasDraftBitmap = false;

   protected CanvasGraphics canvasGraphics;

   /**
    * Stores the version number of the last JDR/AJR file to be loaded.
    *
    * @see #getLastLoadedVersion()
    */
   private static float lastLoadedVersion=0.0f;

   /**
    * Stores the value of the settings flag for the last JDR/AJR file
    * to be loaded.
    *
    * @see #getLastLoadedSettingsID()
    */
   private static int lastLoadedSettings=-1;
}
