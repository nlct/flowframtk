// File          : EPSImage.java
// Purpose       : class representing image operator
// Creation Date : 1st June 2008
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
package com.dickimawbooks.jdr.io.eps.operators;

import java.io.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.eps.*;
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Object representing image operator.
 * @author Nicola L C Talbot
 */
public class EPSImage extends EPSOperator
{
   public EPSImage()
   {
      super("image");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      GraphicsState currentGraphicsState
         = eps.getCurrentGraphicsState();

      EPSObject object = stack.popObject();

      BufferedImage image=null;
      EPSArray trans=null;

      if (object instanceof EPSProc)
      {
         EPSProc proc = (EPSProc)object;

         trans = stack.popEPSMatrix();
         int bitsPerSample = stack.popInteger();
         int scanLines = stack.popInteger();
         int scanLength = stack.popInteger();
         image = getGreyImage(scanLength, scanLines,
           bitsPerSample, 0, 1, proc, stack, eps);
      }
      else if (object instanceof EPSDict)
      {
         EPSDict dict = (EPSDict)object;
         image = getImage(dict, currentGraphicsState, stack, eps);

         trans = dict.getArray("ImageMatrix");
      }
      else
      {
         throw new InvalidEPSObjectException(
            "invalid parameter to image", eps.getLineNum());
      }

      File file = eps.getNextBitmapFile("png");

      if (file != null)
      {
         String filename = file.getCanonicalPath();
         eps.printlnMessage("Saving '"+filename+"'");

         ImageIO.write(image, "png", file);

         JDRBitmap bitmap = new JDRBitmap(
           eps.getCanvasGraphics(), filename);

         // map back onto 1 x 1 unit square
         AffineTransform af = trans.getTransform().createInverse();
         AffineTransform ctm = currentGraphicsState.getTransform();
         af.preConcatenate(ctm);

         bitmap.setTransformation(af);
         eps.addJDRObject(bitmap);
      }
   }


   private BufferedImage getImage(EPSDict dict,
      GraphicsState currentGraphicsState, EPSStack stack, EPS eps)
   throws InvalidFormatException,IOException,
      NoninvertibleTransformException
   {
      int type = dict.getInt("ImageType");

      if (type != 1)
      {
         throw new InvalidFormatException(
           "Image type "+type+" not supported",
           eps.getLineNum());
      }

      int width = dict.getInt("Width");
      int height = dict.getInt("Height");
      int nBits = dict.getInt("BitsPerComponent");
      EPSArray decode = dict.getArray("Decode");
      EPSObject data = dict.get("DataSource");

      boolean multipleDataSources = false;

      if (dict.containsKey("MultipleDataSources"))
      {
         multipleDataSources = dict.getBoolean("MultipleDataSources");
      }

      boolean interpolate = false;

      if (dict.containsKey("Interpolate"))
      {
         interpolate = dict.getBoolean("Interpolate");
      }

      JDRPaint paint = currentGraphicsState.getPaint();

      if (paint instanceof JDRColor)
      {
         if (decode.length() != 6)
         {
            throw new InvalidFormatException(
               "(image) decode array requires 6 elements "
              +"for RGB space", eps.getLineNum());
         }

         if (multipleDataSources)
         {
            if (!(data instanceof EPSArray))
            {
               throw new InvalidFormatException(
                  "(image) "
                  +"array data source expected for "
                  +"multiple data sources", eps.getLineNum());
            }

            EPSArray array = (EPSArray)data;

            if (array.length() != 3)
            {
               throw new InvalidFormatException(
                  "(image) "
                  +"RGB array data source for "
                  +"multiple data sources requires 3 components "
                  +array.length()+" found",
                  eps.getLineNum());
            }

            return getRGBImage(width, height, nBits, 
               decode.getDouble(0), decode.getDouble(1),
               decode.getDouble(2), decode.getDouble(3),
               decode.getDouble(4), decode.getDouble(5),
               array.get(0), array.get(1), array.get(2),
               stack, eps);
         }
         else
         {
            return getRGBImage(width, height, nBits, 
               decode.getDouble(0), decode.getDouble(1),
               decode.getDouble(2), decode.getDouble(3),
               decode.getDouble(4), decode.getDouble(5),
               data, stack, eps);
         }
      }
      else if (paint instanceof JDRColorCMYK)
      {
         if (decode.length() != 8)
         {
            throw new InvalidFormatException(
               "(image) decode array requires 8 elements "
              +"for CMYK space", eps.getLineNum());
         }

         if (multipleDataSources)
         {
            if (!(data instanceof EPSArray))
            {
               throw new InvalidFormatException(
                  "(image) "
                  +"array data source expected for "
                  +"multiple data sources", eps.getLineNum());
            }

            EPSArray array = (EPSArray)data;

            if (array.length() != 4)
            {
               throw new InvalidFormatException(
                  "(image) "
                  +"CMYK array data source for "
                  +"multiple data sources requires 4 components "
                  +array.length()+" found", eps.getLineNum());
            }

            return getCMYKImage(width, height, nBits, 
               decode.getDouble(0), decode.getDouble(1),
               decode.getDouble(2), decode.getDouble(3),
               decode.getDouble(4), decode.getDouble(5),
               decode.getDouble(6), decode.getDouble(7),
               array.get(0), array.get(1), array.get(2),
               array.get(3), stack, eps);
         }
         else
         {
            return getCMYKImage(width, height, nBits, 
               decode.getDouble(0), decode.getDouble(1),
               decode.getDouble(2), decode.getDouble(3),
               decode.getDouble(4), decode.getDouble(5),
               decode.getDouble(6), decode.getDouble(7),
               data, stack, eps);
         }
      }
      else if (paint instanceof JDRColorHSB)
      {
         if (decode.length() != 6)
         {
            throw new InvalidFormatException(
               "(image) decode array requires 6 elements "
              +"for HSB space", eps.getLineNum());
         }

         if (multipleDataSources)
         {
            if (!(data instanceof EPSArray))
            {
               throw new InvalidFormatException(
                  "(image) "
                  +"array data source expected for "
                  +"multiple data sources", eps.getLineNum());
            }

            EPSArray array = (EPSArray)data;

            if (array.length() != 3)
            {
               throw new InvalidFormatException(
                  "(image) "
                  +"HSB array data source for "
                  +"multiple data sources requires 3 components "
                  +array.length()+" found", eps.getLineNum());
            }

            return getHSBImage(width, height, nBits, 
               decode.getDouble(0), decode.getDouble(1),
               decode.getDouble(2), decode.getDouble(3),
               decode.getDouble(4), decode.getDouble(5),
               array.get(0), array.get(1), array.get(2),
               stack, eps);
         }
         else
         {
            return getHSBImage(width, height, nBits, 
               decode.getDouble(0), decode.getDouble(1),
               decode.getDouble(2), decode.getDouble(3),
               decode.getDouble(4), decode.getDouble(5),
               data, stack, eps);
         }
      }
      else if (paint instanceof JDRGray)
      {
         if (multipleDataSources)
         {
            throw new InvalidFormatException(
               "(image) grey can't have multiple sources",
               eps.getLineNum());
         }
         else
         {
            if (decode.length() != 2)
            {
               throw new InvalidFormatException(
                  "(image) decode array requires 2 elements "
                 +"for grey space", eps.getLineNum());
            }

            return getGreyImage(width, height, nBits,
               decode.getDouble(0), decode.getDouble(1), data,
               stack, eps);
         }
      }
      else
      {
         throw new InvalidFormatException(
            "Invalid paint type for image", eps.getLineNum());
      }
   }

   private BufferedImage getRGBImage(int width, int height, int nBits,
      double rMin, double rMax, double gMin, double gMax, 
      double bMin, double bMax, EPSObject redData, EPSObject greenData,
      EPSObject blueData, EPSStack stack, EPS eps)
   throws InvalidFormatException,IOException,
      NoninvertibleTransformException
   {
      WritableRaster raster
         = ColorModel.getRGBdefault()
           .createCompatibleWritableRaster(width, height);

      eps.printlnMessage("extracting RGB image");

      int n = width*height*4;
      int[] data = new int[n];

      if (redData instanceof EPSString)
      {
         EPSString string = (EPSString)redData;

         for (int i = 0, j = 0; i < n; i+=4)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double value = rMin+(c*(rMax-rMin)/((2>>nBits)-1.0));

            data[i] = (int)(value*255);
            data[i+3] = 255;
         }
      }
      else if (redData instanceof EPSProc)
      {
         stack.execProc((EPSProc)redData);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i+=4)
         {
            char c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)redData);
               string = stack.popEPSString();
            }

            double value = rMin+(c*(rMax-rMin)/((2>>nBits)-1.0));

            data[i] = (int)(value*255);
            data[i+3] = 255;
         }
      }
      else if (redData instanceof EPSFilter 
            || redData instanceof EPSFile)
      {
         EPSFile file;

         if (redData instanceof EPSFilter)
         {
            file = ((EPSFilter)redData).getFile(stack);
         }
         else
         {
            file = (EPSFile)redData;
         }

         for (int i = 0; i < n; i += 4)
         {
            int c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            double value = rMin+(c*(rMax-rMin)/((2>>nBits)-1.0));

            data[i] = (int)(value*255);
            data[i+3] = 255;
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      if (greenData instanceof EPSString)
      {
         EPSString string = (EPSString)greenData;

         for (int i = 0, j = 0; i < n; i+=4)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double value = gMin+(c*(gMax-gMin)/((2>>nBits)-1.0));

            data[i+1] = (int)(value*255);
         }
      }
      else if (greenData instanceof EPSProc)
      {
         stack.execProc((EPSProc)greenData);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i+=4)
         {
            char c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)greenData);
               string = stack.popEPSString();
            }

            double value = gMin+(c*(gMax-gMin)/((2>>nBits)-1.0));

            data[i+1] = (int)(value*255);
         }
      }
      else if (greenData instanceof EPSFilter 
            || greenData instanceof EPSFile)
      {
         EPSFile file;

         if (greenData instanceof EPSFilter)
         {
            file = ((EPSFilter)greenData).getFile(stack);
         }
         else
         {
            file = (EPSFile)greenData;
         }

         for (int i = 0; i < n; i += 4)
         {
            int c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            double value = gMin+(c*(gMax-gMin)/((2>>nBits)-1.0));

            data[i+1] = (int)(value*255);
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      if (blueData instanceof EPSString)
      {
         EPSString string = (EPSString)blueData;

         for (int i = 0, j = 0; i < n; i+=4)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double value = bMin+(c*(bMax-bMin)/((2>>nBits)-1.0));

            data[i+2] = (int)(value*255);
         }
      }
      else if (blueData instanceof EPSProc)
      {
         stack.execProc((EPSProc)blueData);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i+=4)
         {
            char c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)blueData);
               string = stack.popEPSString();
            }

            double value = bMin+(c*(bMax-bMin)/((2>>nBits)-1.0));

            data[i+2] = (int)(value*255);
         }
      }
      else if (blueData instanceof EPSFilter 
            || blueData instanceof EPSFile)
      {
         EPSFile file;

         if (blueData instanceof EPSFilter)
         {
            file = ((EPSFilter)blueData).getFile(stack);
         }
         else
         {
            file = (EPSFile)blueData;
         }

         for (int i = 0; i < n; i += 4)
         {
            int c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            double value = bMin+(c*(bMax-bMin)/((2>>nBits)-1.0));

            data[i+2] = (int)(value*255);
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      raster.setPixels(0, 0, width, height, data);

      BufferedImage image = new BufferedImage(width, height,
         BufferedImage.TYPE_INT_ARGB);

      image.setData(raster);

      return image;
   }

   private BufferedImage getRGBImage(int width, int height, int nBits,
      double rMin, double rMax, double gMin, double gMax, 
      double bMin, double bMax, EPSObject source,
      EPSStack stack, EPS eps)
   throws InvalidFormatException,IOException,
      NoninvertibleTransformException
   {
      WritableRaster raster
         = ColorModel.getRGBdefault()
           .createCompatibleWritableRaster(width, height);

      eps.printlnMessage("extracting RGB image");

      int n = width*height*4;
      int[] data = new int[n];

      if (source instanceof EPSString)
      {
         EPSString string = (EPSString)source;

         for (int i = 0, j = 0; i < n; i+=4)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double value = rMin+(c*(rMax-rMin)/((2>>nBits)-1.0));

            data[i] = (int)(value*255);

            c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            value = gMin+(c*(gMax-gMin)/((2>>nBits)-1.0));

            data[i+1] = (int)(value*255);

            c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            value = bMin+(c*(bMax-bMin)/((2>>nBits)-1.0));

            data[i+2] = (int)(value*255);
            data[i+3] = 255;
         }
      }
      else if (source instanceof EPSProc)
      {
         stack.execProc((EPSProc)source);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i+=4)
         {
            char c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)source);
               string = stack.popEPSString();
            }

            double value = rMin+(c*(rMax-rMin)/((2>>nBits)-1.0));

            data[i] = (int)(value*255);

            c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)source);
               string = stack.popEPSString();
            }

            value = gMin+(c*(gMax-gMin)/((2>>nBits)-1.0));

            data[i+1] = (int)(value*255);

            c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)source);
               string = stack.popEPSString();
            }

            value = bMin+(c*(bMax-bMin)/((2>>nBits)-1.0));

            data[i+2] = (int)(value*255);
            data[i+3] = 255;
         }
      }
      else if (source instanceof EPSFilter 
            || source instanceof EPSFile)
      {
         EPSFile file;

         if (source instanceof EPSFilter)
         {
            file = ((EPSFilter)source).getFile(stack);
         }
         else
         {
            file = (EPSFile)source;
         }

         for (int i = 0; i < n; i += 4)
         {
            int c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            double value = rMin+(c*(rMax-rMin)/((2>>nBits)-1.0));

            data[i] = (int)(value*255);

            c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            value = gMin+(c*(gMax-gMin)/((2>>nBits)-1.0));

            data[i+1] = (int)(value*255);

            c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            value = bMin+(c*(bMax-bMin)/((2>>nBits)-1.0));

            data[i+2] = (int)(value*255);
            data[i+3] = 255;
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      raster.setPixels(0, 0, width, height, data);

      BufferedImage image = new BufferedImage(width, height,
         BufferedImage.TYPE_INT_ARGB);

      image.setData(raster);

      return image;
   }

   private BufferedImage getHSBImage(int width, int height, int nBits,
      double hueMin, double hueMax,
      double saturationMin, double saturationMax, 
      double brightnessMin, double brightnessMax, EPSObject hueData,
      EPSObject saturationData, EPSObject brightnessData,
      EPSStack stack, EPS eps)
   throws InvalidFormatException,IOException,
      NoninvertibleTransformException
   {
      WritableRaster raster
         = ColorModel.getRGBdefault()
           .createCompatibleWritableRaster(width, height);

      eps.printlnMessage("extracting HSB image");

      int n = width*height;

      JDRColorHSB[] colData = new JDRColorHSB[n];

      if (hueData instanceof EPSString)
      {
         EPSString string = (EPSString)hueData;

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double value = hueMin
               +(c*(hueMax-hueMin)/((2>>nBits)-1.0));

            colData[i] = new JDRColorHSB(eps.getCanvasGraphics());
            colData[i].setHue(value);
         }
      }
      else if (hueData instanceof EPSProc)
      {
         stack.execProc((EPSProc)hueData);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length() && i+1 < n)
            {
               j = 0;
               stack.execProc((EPSProc)hueData);
               string = stack.popEPSString();
            }

            double value = hueMin
               +(c*(hueMax-hueMin)/((2>>nBits)-1.0));

            colData[i] = new JDRColorHSB(eps.getCanvasGraphics());
            colData[i].setHue(value);
         }
      }
      else if (hueData instanceof EPSFilter 
            || hueData instanceof EPSFile)
      {
         EPSFile file;

         if (hueData instanceof EPSFilter)
         {
            file = ((EPSFilter)hueData).getFile(stack);
         }
         else
         {
            file = (EPSFile)hueData;
         }

         for (int i = 0; i < n; i++)
         {
            int c = file.read();

            if (c == -1 && i+1 < n)
            {
               file.restart();
            }

            double value = hueMin
               +(c*(hueMax-hueMin)/((2>>nBits)-1.0));

            colData[i] = new JDRColorHSB(eps.getCanvasGraphics());
            colData[i].setHue(value);
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      if (saturationData instanceof EPSString)
      {
         EPSString string = (EPSString)saturationData;

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double value = saturationMin
               +(c*(saturationMax-saturationMin)/((2>>nBits)-1.0));

            colData[i].setSaturation(value);
         }
      }
      else if (saturationData instanceof EPSProc)
      {
         stack.execProc((EPSProc)saturationData);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length() && i+1 < n)
            {
               j = 0;
               stack.execProc((EPSProc)saturationData);
               string = stack.popEPSString();
            }

            double value = saturationMin
               +(c*(saturationMax-saturationMin)/((2>>nBits)-1.0));

            colData[i].setSaturation(value);
         }
      }
      else if (saturationData instanceof EPSFilter 
            || saturationData instanceof EPSFile)
      {
         EPSFile file;

         if (saturationData instanceof EPSFilter)
         {
            file = ((EPSFilter)saturationData).getFile(stack);
         }
         else
         {
            file = (EPSFile)saturationData;
         }

         for (int i = 0; i < n; i++)
         {
            int c = file.read();

            if (c == -1 && i+1 < n)
            {
               file.restart();
            }

            double value = saturationMin
               +(c*(saturationMax-saturationMin)/((2>>nBits)-1.0));

            colData[i].setSaturation(value);
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      if (brightnessData instanceof EPSString)
      {
         EPSString string = (EPSString)brightnessData;

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double value = brightnessMin
               +(c*(brightnessMax-brightnessMin)/((2>>nBits)-1.0));

            colData[i].setBrightness(value);
         }
      }
      else if (brightnessData instanceof EPSProc)
      {
         stack.execProc((EPSProc)brightnessData);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length() && i+1 < n)
            {
               j = 0;
               stack.execProc((EPSProc)brightnessData);
               string = stack.popEPSString();
            }

            double value = brightnessMin
               +(c*(brightnessMax-brightnessMin)/((2>>nBits)-1.0));

            colData[i].setBrightness(value);
         }
      }
      else if (brightnessData instanceof EPSFilter 
            || brightnessData instanceof EPSFile)
      {
         EPSFile file;

         if (brightnessData instanceof EPSFilter)
         {
            file = ((EPSFilter)brightnessData).getFile(stack);
         }
         else
         {
            file = (EPSFile)brightnessData;
         }

         for (int i = 0; i < n; i++)
         {
            int c = file.read();

            if (c == -1 && i+1 < n)
            {
               file.restart();
            }

            double value = brightnessMin
               +(c*(brightnessMax-brightnessMin)/((2>>nBits)-1.0));

            colData[i].setBrightness(value);
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      int[] data = new int[n*4];

      for (int i = 0, j = 0; i < n; i++)
      {
         JDRColor col = colData[i].getJDRColor();

         data[j++] = (int)Math.round(col.getRed()*255);
         data[j++] = (int)Math.round(col.getGreen()*255);
         data[j++] = (int)Math.round(col.getBlue()*255);
         data[j++] = 255;
      }


      raster.setPixels(0, 0, width, height, data);

      BufferedImage image = new BufferedImage(width, height,
         BufferedImage.TYPE_INT_ARGB);

      image.setData(raster);

      return image;
   }

   private BufferedImage getHSBImage(int width, int height, int nBits,
      double hueMin, double hueMax,
      double saturationMin, double saturationMax, 
      double brightnessMin, double brightnessMax,
      EPSObject source, EPSStack stack, EPS eps)
   throws InvalidFormatException,IOException,
      NoninvertibleTransformException
   {
      WritableRaster raster
         = ColorModel.getRGBdefault()
           .createCompatibleWritableRaster(width, height);

      eps.printlnMessage("extracting HSB image");

      int n = width*height*4;
      int[] data = new int[n];

      if (source instanceof EPSString)
      {
         EPSString string = (EPSString)source;

         for (int i = 0, j = 0; i < n; i+=4)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double hue = hueMin
               +(c*(hueMax-hueMin)/((2>>nBits)-1.0));

            c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double saturation = saturationMin
               +(c*(saturationMax-saturationMin)/((2>>nBits)-1.0));

            c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double brightness = brightnessMin
               +(c*(brightnessMax-brightnessMin)/((2>>nBits)-1.0));

            JDRColorHSB colHSB = new JDRColorHSB(
               eps.getCanvasGraphics(), hue, saturation,
               brightness);

            JDRColor col = colHSB.getJDRColor();

            data[i] = (int)Math.round(col.getRed()*255);
            data[i+1] = (int)Math.round(col.getGreen()*255);
            data[i+2] = (int)Math.round(col.getBlue()*255);
            data[i+3] = 255;
         }
      }
      else if (source instanceof EPSProc)
      {
         stack.execProc((EPSProc)source);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i+=4)
         {
            char c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)source);
               string = stack.popEPSString();
            }

            double hue = hueMin
               +(c*(hueMax-hueMin)/((2>>nBits)-1.0));

            c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)source);
               string = stack.popEPSString();
            }

            double saturation = saturationMin
               +(c*(saturationMax-saturationMin)/((2>>nBits)-1.0));

            c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)source);
               string = stack.popEPSString();
            }

            double brightness = brightnessMin
               +(c*(brightnessMax-brightnessMin)/((2>>nBits)-1.0));

            JDRColorHSB colHSB = new JDRColorHSB(
               eps.getCanvasGraphics(), hue, saturation,
               brightness);

            JDRColor col = colHSB.getJDRColor();

            data[i] = (int)Math.round(col.getRed()*255);
            data[i+1] = (int)Math.round(col.getGreen()*255);
            data[i+2] = (int)Math.round(col.getBlue()*255);
            data[i+3] = 255;
         }
      }
      else if (source instanceof EPSFilter 
            || source instanceof EPSFile)
      {
         EPSFile file;

         if (source instanceof EPSFilter)
         {
            file = ((EPSFilter)source).getFile(stack);
         }
         else
         {
            file = (EPSFile)source;
         }

         for (int i = 0; i < n; i += 4)
         {
            int c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            double hue = hueMin
               +(c*(hueMax-hueMin)/((2>>nBits)-1.0));

            c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            double saturation = saturationMin
               +(c*(saturationMax-saturationMin)/((2>>nBits)-1.0));

            c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            double brightness = brightnessMin
               +(c*(brightnessMax-brightnessMin)/((2>>nBits)-1.0));

            JDRColorHSB colHSB = new JDRColorHSB(
               eps.getCanvasGraphics(), hue, saturation,
               brightness);

            JDRColor col = colHSB.getJDRColor();

            data[i] = (int)Math.round(col.getRed()*255);
            data[i+1] = (int)Math.round(col.getGreen()*255);
            data[i+2] = (int)Math.round(col.getBlue()*255);
            data[i+3] = 255;
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      raster.setPixels(0, 0, width, height, data);

      BufferedImage image = new BufferedImage(width, height,
         BufferedImage.TYPE_INT_ARGB);

      image.setData(raster);

      return image;
   }

   private BufferedImage getGreyImage(int width, int height, int nBits,
      double greyMin, double greyMax,
      EPSObject source, EPSStack stack, EPS eps)
   throws InvalidFormatException,IOException,
      NoninvertibleTransformException
   {
      WritableRaster raster
         = ColorModel.getRGBdefault()
           .createCompatibleWritableRaster(width, height);

      eps.printlnMessage("extracting grey image");

      EPSProc transfer = eps.getCurrentGraphicsState().getTransfer();

      int m = width*height;
      int[] greyData = new int[m];

      if (source instanceof EPSString)
      {
         EPSString string = (EPSString)source;

         for (int i = 0, j = 0; i < m; i++)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double grey = greyMin
               +(c*(greyMax-greyMin)/((2>>nBits)-1.0));

            greyData[i] = (int)Math.round(grey*255);
         }
      }
      else if (source instanceof EPSProc)
      {
         stack.execProc((EPSProc)source);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < m; i++)
         {
            double grey;

            if (string.isEmpty())
            {
               grey = 1;

               if (transfer != null)
               {
                  stack.pushDouble(grey);
                  stack.execProc(transfer);
                  grey = stack.popDouble();
               }

               greyData[i] = (int)Math.round(grey*255);
            }
            else
            {
               char c = string.get(j++);

               if (j == string.length() && i < m)
               {
                  j = 0;

                  stack.execProc((EPSProc)source);
                  string = stack.popEPSString();
               }

//               grey = greyMin
//                  +(c*(greyMax-greyMin)/((2>>nBits)-1.0));

               if (nBits == 1)
               {
                  for (int k = 128; k > 0; k >>= 1, i++)
                  {
                     if (i >= m)
                     {
                        break;
                     }

                     grey = (((int)c & k) > 0 ? 1 : 0);

                     if (transfer != null)
                     {
                        stack.pushDouble(grey);
                        stack.execProc(transfer);
                        grey = stack.popDouble();
                     }

                     greyData[i] = (int)Math.round(grey*255);
                  }

                  i--;
               }
               else if (nBits == 2)
               {
                  for (int k = 192, r=6; k > 0; k >>= 2, i++,r-=2)
                  {
                     if (i >= m)
                     {
                        break;
                     }

                     grey = (((int)c & k) >> r)/3.0;

                     if (transfer != null)
                     {
                        stack.pushDouble(grey);
                        stack.execProc(transfer);
                        grey = stack.popDouble();
                     }

                     greyData[i] = (int)Math.round(grey*255);
                  }

                  i--;
               }
               else if (nBits == 4)
               {
                  grey = (((int)c & 0xf0) >> 4)/15.0;

                  if (transfer != null)
                  {
                     stack.pushDouble(grey);
                     stack.execProc(transfer);
                     grey = stack.popDouble();
                  }

                  greyData[i] = (int)Math.round(grey*255);

                  i++;

                  grey = ((int)c & 0xf)/15.0;

                  if (transfer != null)
                  {
                     stack.pushDouble(grey);
                     stack.execProc(transfer);
                     grey = stack.popDouble();
                  }

                  greyData[i] = (int)Math.round(grey*255);
               }
               else
               {
                  grey = ((int)c)/255.0;

                  if (transfer != null)
                  {
                     stack.pushDouble(grey);
                     stack.execProc(transfer);
                     grey = stack.popDouble();
                  }

                  greyData[i] = (int)Math.round(grey*255);
               }
            }
         }
      }
      else if (source instanceof EPSFilter 
            || source instanceof EPSFile)
      {
         EPSFile file;

         if (source instanceof EPSFilter)
         {
            file = ((EPSFilter)source).getFile(stack);
         }
         else
         {
            file = (EPSFile)source;
         }

         for (int i = 0; i < m; i++)
         {
            int c = file.read();

            if (c == -1 && i < m)
            {
               file.restart();
            }

            double grey = greyMin
               +(c*(greyMax-greyMin)/((2>>nBits)-1.0));

            greyData[i] = (int)Math.round(grey*255);
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      int n = width*height*4;
      int[] data = new int[n];

      for (int i = 0, j = 0; i < m; i++)
      {
         data[j++] = greyData[i];
         data[j++] = greyData[i];
         data[j++] = greyData[i];
         data[j++] = 255;
      }

      raster.setPixels(0, 0, width, height, data);

      BufferedImage image = new BufferedImage(width, height,
         BufferedImage.TYPE_INT_ARGB);

      image.setData(raster);

      return image;
   }

   private BufferedImage getCMYKImage(int width, int height, int nBits,
      double cyanMin, double cyanMax, double magentaMin,
      double magentaMax, double yellowMin, double yellowMax,
      double blackMin, double blackMax,
      EPSObject cyanData, EPSObject magentaData, EPSObject yellowData,
      EPSObject blackData, EPSStack stack, EPS eps)
   throws InvalidFormatException,IOException,
      NoninvertibleTransformException
   {
      WritableRaster raster
         = ColorModel.getRGBdefault()
           .createCompatibleWritableRaster(width, height);

      eps.printlnMessage("extracting CMYK image");

      int n = width*height;

      JDRColorCMYK[] colData = new JDRColorCMYK[n];

      if (cyanData instanceof EPSString)
      {
         EPSString string = (EPSString)cyanData;

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double value = cyanMin
               +(c*(cyanMax-cyanMin)/((2>>nBits)-1.0));

            colData[i] = new JDRColorCMYK(eps.getCanvasGraphics());
            colData[i].setCyan(value);
         }
      }
      else if (cyanData instanceof EPSProc)
      {
         stack.execProc((EPSProc)cyanData);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length() && i+1 < n)
            {
               j = 0;
               stack.execProc((EPSProc)cyanData);
               string = stack.popEPSString();
            }

            double value = cyanMin
               +(c*(cyanMax-cyanMin)/((2>>nBits)-1.0));

            colData[i] = new JDRColorCMYK(eps.getCanvasGraphics());
            colData[i].setCyan(value);
         }
      }
      else if (cyanData instanceof EPSFilter 
            || cyanData instanceof EPSFile)
      {
         EPSFile file;

         if (cyanData instanceof EPSFilter)
         {
            file = ((EPSFilter)cyanData).getFile(stack);
         }
         else
         {
            file = (EPSFile)cyanData;
         }

         for (int i = 0; i < n; i++)
         {
            int c = file.read();

            if (c == -1 && i+1 < n)
            {
               file.restart();
            }

            double value = cyanMin
               +(c*(cyanMax-cyanMin)/((2>>nBits)-1.0));

            colData[i] = new JDRColorCMYK(eps.getCanvasGraphics());
            colData[i].setCyan(value);
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      if (magentaData instanceof EPSString)
      {
         EPSString string = (EPSString)magentaData;

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double value = magentaMin
               +(c*(magentaMax-magentaMin)/((2>>nBits)-1.0));

            colData[i].setMagenta(value);
         }
      }
      else if (magentaData instanceof EPSProc)
      {
         stack.execProc((EPSProc)magentaData);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length() && i+1 < n)
            {
               j = 0;
               stack.execProc((EPSProc)magentaData);
               string = stack.popEPSString();
            }

            double value = magentaMin
               +(c*(magentaMax-magentaMin)/((2>>nBits)-1.0));

            colData[i].setMagenta(value);
         }
      }
      else if (magentaData instanceof EPSFilter 
            || magentaData instanceof EPSFile)
      {
         EPSFile file;

         if (magentaData instanceof EPSFilter)
         {
            file = ((EPSFilter)magentaData).getFile(stack);
         }
         else
         {
            file = (EPSFile)magentaData;
         }

         for (int i = 0; i < n; i++)
         {
            int c = file.read();

            if (c == -1 && i+1 < n)
            {
               file.restart();
            }

            double value = magentaMin
               +(c*(magentaMax-magentaMin)/((2>>nBits)-1.0));

            colData[i].setMagenta(value);
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      if (yellowData instanceof EPSString)
      {
         EPSString string = (EPSString)yellowData;

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double value = yellowMin
               +(c*(yellowMax-yellowMin)/((2>>nBits)-1.0));

            colData[i].setYellow(value);
         }
      }
      else if (yellowData instanceof EPSProc)
      {
         stack.execProc((EPSProc)yellowData);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length() && i+1 < n)
            {
               j = 0;
               stack.execProc((EPSProc)yellowData);
               string = stack.popEPSString();
            }

            double value = yellowMin
               +(c*(yellowMax-yellowMin)/((2>>nBits)-1.0));

            colData[i].setYellow(value);
         }
      }
      else if (yellowData instanceof EPSFilter 
            || yellowData instanceof EPSFile)
      {
         EPSFile file;

         if (yellowData instanceof EPSFilter)
         {
            file = ((EPSFilter)yellowData).getFile(stack);
         }
         else
         {
            file = (EPSFile)yellowData;
         }

         for (int i = 0; i < n; i++)
         {
            int c = file.read();

            if (c == -1 && i+1 < n)
            {
               file.restart();
            }

            double value = yellowMin
               +(c*(yellowMax-yellowMin)/((2>>nBits)-1.0));

            colData[i].setYellow(value);
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      if (blackData instanceof EPSString)
      {
         EPSString string = (EPSString)blackData;

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double value = blackMin
               +(c*(blackMax-blackMin)/((2>>nBits)-1.0));

            colData[i].setKey(value);
         }
      }
      else if (blackData instanceof EPSProc)
      {
         stack.execProc((EPSProc)blackData);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i++)
         {
            char c = string.get(j++);

            if (j == string.length() && i+1 < n)
            {
               j = 0;
               stack.execProc((EPSProc)blackData);
               string = stack.popEPSString();
            }

            double value = blackMin
               +(c*(blackMax-blackMin)/((2>>nBits)-1.0));

            colData[i].setKey(value);
         }
      }
      else if (blackData instanceof EPSFilter 
            || blackData instanceof EPSFile)
      {
         EPSFile file;

         if (blackData instanceof EPSFilter)
         {
            file = ((EPSFilter)blackData).getFile(stack);
         }
         else
         {
            file = (EPSFile)blackData;
         }

         for (int i = 0; i < n; i++)
         {
            int c = file.read();

            if (c == -1 && i+1 < n)
            {
               file.restart();
            }

            double value = blackMin
               +(c*(blackMax-blackMin)/((2>>nBits)-1.0));

            colData[i].setKey(value);
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      int[] data = new int[n*4];

      for (int i = 0, j = 0; i < n; i++)
      {
         JDRColor col = colData[i].getJDRColor();

         data[j++] = (int)Math.round(col.getRed()*255);
         data[j++] = (int)Math.round(col.getGreen()*255);
         data[j++] = (int)Math.round(col.getBlue()*255);
         data[j++] = 255;
      }

      raster.setPixels(0, 0, width, height, data);

      BufferedImage image = new BufferedImage(width, height,
         BufferedImage.TYPE_INT_ARGB);

      image.setData(raster);

      return image;
   }

   private BufferedImage getCMYKImage(int width, int height, int nBits,
      double cyanMin, double cyanMax,
      double magentaMin, double magentaMax, 
      double yellowMin, double yellowMax,
      double blackMin, double blackMax, EPSObject source,
      EPSStack stack, EPS eps)
   throws InvalidFormatException,IOException,
      NoninvertibleTransformException
   {
      WritableRaster raster
         = ColorModel.getRGBdefault()
           .createCompatibleWritableRaster(width, height);

      eps.printlnMessage("extracting CMYK image");

      int n = width*height*4;
      int[] data = new int[n];

      if (source instanceof EPSString)
      {
         EPSString string = (EPSString)source;

         for (int i = 0, j = 0; i < n; i+=4)
         {
            char c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double cyan = cyanMin
               +(c*(cyanMax-cyanMin)/((2>>nBits)-1.0));

            c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double magenta = magentaMin
               +(c*(magentaMax-magentaMin)/((2>>nBits)-1.0));

            c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double yellow = yellowMin
               +(c*(yellowMax-yellowMin)/((2>>nBits)-1.0));

            c = string.get(j++);

            if (j == string.length())
            {
               j = 0;
            }

            double black = blackMin
               +(c*(blackMax-blackMin)/((2>>nBits)-1.0));

            JDRColorCMYK colCMYK = new JDRColorCMYK(
               eps.getCanvasGraphics(), cyan, magenta,
               yellow, black);

            JDRColor col = colCMYK.getJDRColor();

            data[i] = (int)Math.round(col.getRed()*255);
            data[i+1] = (int)Math.round(col.getGreen()*255);
            data[i+2] = (int)Math.round(col.getBlue()*255);
            data[i+3] = 255;
         }
      }
      else if (source instanceof EPSProc)
      {
         stack.execProc((EPSProc)source);
         EPSString string = stack.popEPSString();

         for (int i = 0, j = 0; i < n; i+=4)
         {
            char c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)source);
               string = stack.popEPSString();
            }

            double cyan = cyanMin
               +(c*(cyanMax-cyanMin)/((2>>nBits)-1.0));

            c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)source);
               string = stack.popEPSString();
            }

            double magenta = magentaMin
               +(c*(magentaMax-magentaMin)/((2>>nBits)-1.0));

            c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)source);
               string = stack.popEPSString();
            }

            double yellow = yellowMin
               +(c*(yellowMax-yellowMin)/((2>>nBits)-1.0));

            c = string.get(j++);

            if (j == string.length() && i+4 < n)
            {
               j = 0;
               stack.execProc((EPSProc)source);
               string = stack.popEPSString();
            }

            double black = blackMin
               +(c*(blackMax-blackMin)/((2>>nBits)-1.0));

            JDRColorCMYK colCMYK = new JDRColorCMYK(
               eps.getCanvasGraphics(), cyan, magenta,
               yellow, black);

            JDRColor col = colCMYK.getJDRColor();

            data[i] = (int)Math.round(col.getRed()*255);
            data[i+1] = (int)Math.round(col.getGreen()*255);
            data[i+2] = (int)Math.round(col.getBlue()*255);
            data[i+3] = 255;
         }
      }
      else if (source instanceof EPSFilter 
            || source instanceof EPSFile)
      {
         EPSFile file;

         if (source instanceof EPSFilter)
         {
            file = ((EPSFilter)source).getFile(stack);
         }
         else
         {
            file = (EPSFile)source;
         }

         for (int i = 0; i < n; i += 4)
         {
            int c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            double cyan = cyanMin
               +(c*(cyanMax-cyanMin)/((2>>nBits)-1.0));

            c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            double magenta = magentaMin
               +(c*(magentaMax-magentaMin)/((2>>nBits)-1.0));

            c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            double yellow = yellowMin
               +(c*(yellowMax-yellowMin)/((2>>nBits)-1.0));

            c = file.read();

            if (c == -1 && i+4 < n)
            {
               file.restart();
            }

            double black = blackMin
               +(c*(blackMax-blackMin)/((2>>nBits)-1.0));

            JDRColorCMYK colCMYK = new JDRColorCMYK(
               eps.getCanvasGraphics(), cyan, magenta,
               yellow, black);

            JDRColor col = colCMYK.getJDRColor();

            data[i] = (int)Math.round(col.getRed()*255);
            data[i+1] = (int)Math.round(col.getGreen()*255);
            data[i+2] = (int)Math.round(col.getBlue()*255);
            data[i+3] = 255;
         }
      }
      else
      {
         throw new InvalidEPSObjectException(
            "image data source invalid", eps.getLineNum());
      }

      raster.setPixels(0, 0, width, height, data);

      BufferedImage image = new BufferedImage(width, height,
         BufferedImage.TYPE_INT_ARGB);

      image.setData(raster);

      return image;
   }

   protected static BufferedImage getGreyImage(int width, int height,
      int nBits, String dataSamples, EPS eps)
   throws InvalidFormatException
   {
      WritableRaster raster
         = ColorModel.getRGBdefault().createCompatibleWritableRaster(width, height);

      eps.printlnMessage("extracting gray image");

      int n = width*height*4;
      int[] data = new int[n];
      //int max = 1 << nBits;

      for (int i = 0, j = 0; i < n; j++)
      {
         if (j >= dataSamples.length()) j = 0;

         char c = dataSamples.charAt(j);

         if (nBits == 1)
         {
            for (int b = 7; b >= 0; b--)
            {
               if (i >= n) break;

               int val = ((c & (1 << b)) >> b)*255;
               data[i++] = val;
               data[i++] = val;
               data[i++] = val;
               data[i++] = 255;
            }
         }
         else if (nBits == 2)
         {
            for (int k = 6; k >= 0; k -= 2)
            {
               if (i >= n) break;

               int val = ((c >> k) & 0x3)*255/3;

               data[i++] = val;
               data[i++] = val;
               data[i++] = val;
               data[i++] = 255;
            }
         }
         else if (nBits == 4)
         {
            for (int k = 4; k >= 0; k -= 4)
            {
               if (i >= n) break;

               int val = ((c >> k) & 0xF)*255/15;

               data[i++] = val;
               data[i++] = val;
               data[i++] = val;
               data[i++] = 255;
            }
         }
         else if (nBits == 8)
         {
            data[i++] = (int)c;
            data[i++] = (int)c;
            data[i++] = (int)c;
            data[i++] = 255;
         }
         else
         {
            throw new InvalidFormatException("invalid nBits value "
               +nBits);
         }
      }

      raster.setPixels(0, 0, width, height, data);

      BufferedImage image = new BufferedImage(width, height,
         BufferedImage.TYPE_INT_ARGB);

      image.setData(raster);

      return image;
   }

}
