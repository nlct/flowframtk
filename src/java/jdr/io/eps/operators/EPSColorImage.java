// File          : EPSColorImage.java
// Purpose       : class representing colorimage operator
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
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.eps.*;
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Object representing colorimage operator.
 * @author Nicola L C Talbot
 */
public class EPSColorImage extends EPSOperator
{
   public EPSColorImage()
   {
      super("colorimage");
   }

   public void execute(EPSStack stack, EPS eps)
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      GraphicsState currentGraphicsState
         = eps.getCurrentGraphicsState();

      int nComp = stack.popInteger();
      boolean multi = stack.popBoolean().booleanValue();
      String[] data;
         
      if (multi)
      {
         data = new String[nComp];

         for (int i = nComp-1; i >= 0; i--)
         {
            EPSProc proc = stack.popEPSProc();
            stack.execProc(proc);
            data[i] = stack.popString();
         }
      }
      else
      {
         data = new String[1];

         EPSProc proc = stack.popEPSProc();
         stack.execProc(proc);
         data[0] = stack.popString();
      }

      EPSArray trans = stack.popEPSMatrix();
      int bitsPerSample = stack.popInteger();
      int scanLines = stack.popInteger();
      int scanLength = stack.popInteger();

      BufferedImage image;

      if (multi)
      {
         image = getColourImage(scanLength, scanLines, 
            bitsPerSample, data, eps);
      }
      else
      {
         image = getColourImage(scanLength, scanLines, 
           bitsPerSample, data[0], nComp, eps);
      }

      String filename = eps.getNextBitmapName("png");
      eps.printlnMessage("Saving '"+filename+"'");

      File file = new File(filename);

      ImageIO.write(image, "png", file);

      JDRBitmap bitmap = new JDRBitmap(eps.getCanvasGraphics(),
         file.getCanonicalPath());

      // map back onto 1 x 1 unit square
      AffineTransform af = trans.getTransform().createInverse();
      AffineTransform ctm = currentGraphicsState.getTransform();
      af.preConcatenate(ctm);

      // apply transformation to bitmap
      bitmap.setTransformation(af);

      eps.addJDRObject(bitmap);
   }

   private BufferedImage getColourRGBImage(int width, int height, 
      int nBits, String dataSamples, EPS eps)
   throws InvalidFormatException
   {
      WritableRaster raster
         = ColorModel.getRGBdefault().createCompatibleWritableRaster(width, height);

      eps.printlnMessage("extracting RGB image");

      int n = width*height*4;
      int[] data = new int[n];
      int colType = 0;

      if (nBits == 1)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= dataSamples.length()) j = 0;
            int c = (int)dataSamples.charAt(j++);

            for (int b = 7; b >= 0; b--)
            {
               if (i >= n) break;
               data[i++] = ((c & (1 << b)) >> b)*255;
               colType++;

               if (colType == 3)
               {
                  data[i++] = 255;
                  colType = 0;
               }
            }
         }
      }
      else if (nBits == 2)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= dataSamples.length()) j = 0;
            int c = (int)dataSamples.charAt(j++);

            for (int k = 6; k >= 0; k -= 2)
            {
               if (i >= n) break;
               data[i++] = ((c >> k) & 0x3)*255/3;
               colType++;

               if (colType == 3)
               {
                  data[i++] = 255;
                  colType = 0;
               }
            }
         }
      }
      else if (nBits == 4)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= dataSamples.length()) j = 0;
            int c = (int)dataSamples.charAt(j++);

            for (int k = 4; k >= 0; k -= 4)
            {
               if (i >= n) break;
               data[i++] = ((c >> k) & 0xF)*255/15;
               colType++;

               if (colType == 3)
               {
                  data[i++] = 255;
                  colType = 0;
               }
            }
         }
      }
      else if (nBits == 8)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= dataSamples.length()) j = 0;

            int red, green, blue;

            red = (int)dataSamples.charAt(j++);
            if (j >= dataSamples.length()) j = 0;
            green = (int)dataSamples.charAt(j++);
            if (j >= dataSamples.length()) j = 0;
            blue = (int)dataSamples.charAt(j++);

            data[i++] = red;
            data[i++] = green;
            data[i++] = blue;
            data[i++] = 255;
         }
      }
      else
      {
         throw new InvalidFormatException("invalid nBits value "
            +nBits);
      }

      raster.setPixels(0, 0, width, height, data);

      BufferedImage image = new BufferedImage(width, height,
         BufferedImage.TYPE_INT_ARGB);

      image.setData(raster);

      return image;
   }

   private BufferedImage getColourRGBImage(int width, int height, 
      int nBits, String redSamples, String greenSamples,
      String blueSamples, EPS eps)
   throws InvalidFormatException
   {
      WritableRaster raster
         = ColorModel.getRGBdefault().createCompatibleWritableRaster(width, height);

      eps.printlnMessage("extracting RGB image");

      int n = width*height*4;
      int[] data = new int[n];

      if (nBits == 1)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= redSamples.length()) j = 0;
            int cRed = (int)redSamples.charAt(j);
            int cGreen = (int)greenSamples.charAt(j);
            int cBlue = (int)blueSamples.charAt(j);
            j++;

            for (int b = 7; b >= 0; b--)
            {
               if (i >= n) break;
               data[i++] = ((cRed & (1 << b)) >> b)*255;
               data[i++] = ((cGreen & (1 << b)) >> b)*255;
               data[i++] = ((cBlue & (1 << b)) >> b)*255;
               data[i++] = 255;
            }
         }
      }
      else if (nBits == 2)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= redSamples.length()) j = 0;
            int cRed = (int)redSamples.charAt(j);
            int cGreen = (int)greenSamples.charAt(j);
            int cBlue = (int)blueSamples.charAt(j);
            j++;

            for (int k = 6; k >= 0; k -= 2)
            {
               if (i >= n) break;
               data[i++] = ((cRed >> k) & 0x3)*255/3;
               data[i++] = ((cGreen >> k) & 0x3)*255/3;
               data[i++] = ((cBlue >> k) & 0x3)*255/3;
               data[i++] = 255;
            }
         }
      }
      else if (nBits == 4)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= redSamples.length()) j = 0;
            int cRed = (int)redSamples.charAt(j);
            int cGreen = (int)greenSamples.charAt(j);
            int cBlue = (int)blueSamples.charAt(j);
            j++;

            for (int k = 4; k >= 0; k -= 4)
            {
               if (i >= n) break;
               data[i++] = ((cRed >> k) & 0xF)*255/15;
               data[i++] = ((cGreen >> k) & 0xF)*255/15;
               data[i++] = ((cBlue >> k) & 0xF)*255/15;
               data[i++] = 255;
            }
         }
      }
      else if (nBits == 8)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= redSamples.length()) j = 0;
            int cRed = (int)redSamples.charAt(j);
            int cGreen = (int)greenSamples.charAt(j);
            int cBlue = (int)blueSamples.charAt(j);
            j++;

            data[i++] = cRed;
            data[i++] = cGreen;
            data[i++] = cBlue;
            data[i++] = 255;
         }
      }
      else
      {
         throw new InvalidFormatException("invalid nBits value "
            +nBits, eps.getLineNum());
      }

      raster.setPixels(0, 0, width, height, data);

      BufferedImage image = new BufferedImage(width, height,
         BufferedImage.TYPE_INT_ARGB);

      image.setData(raster);

      return image;
   }

   private BufferedImage getColourCMYKImage(int width, int height, 
      int nBits, String dataSamples, EPS eps)
   throws InvalidFormatException
   {
      CanvasGraphics cg = eps.getCanvasGraphics();

      WritableRaster raster
         = ColorModel.getRGBdefault().createCompatibleWritableRaster(width, height);
      eps.printlnMessage("extracting CMYK image");

      int n = width*height*4;
      int[] data = new int[n];
      int colType = 0;
      double[] colour = new double[4];

      if (nBits == 1)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= dataSamples.length()) j = 0;
            int c = (int)dataSamples.charAt(j++);

            for (int b = 7; b >= 0; b--)
            {
               if (i >= n) break;
               colour[colType++] = (double)((c & (1 << b)) >> b);

               if (colType == 4)
               {
                  JDRColorCMYK cmyk = new JDRColorCMYK(cg,
                     colour[0], colour[1], colour[2], colour[3]);
                  Color col = cmyk.getColor();
                  data[i++] = col.getRed();
                  data[i++] = col.getGreen();
                  data[i++] = col.getBlue();
                  data[i++] = 255;
                  colType = 0;
               }
            }
         }
      }
      else if (nBits == 2)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= dataSamples.length()) j = 0;
            int c = (int)dataSamples.charAt(j++);

            for (int k = 6; k >= 0; k -= 2)
            {
               if (i >= n) break;
               colour[colType++] = (double)((c >> k) & 0x3)/3.0;

               if (colType == 4)
               {
                  JDRColorCMYK cmyk = new JDRColorCMYK(cg,
                     colour[0], colour[1], colour[2], colour[3]);
                  Color col = cmyk.getColor();
                  data[i++] = col.getRed();
                  data[i++] = col.getGreen();
                  data[i++] = col.getBlue();
                  data[i++] = 255;
                  colType = 0;
               }
            }
         }
      }
      else if (nBits == 4)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= dataSamples.length()) j = 0;
            int c = (int)dataSamples.charAt(j++);

            for (int k = 4; k >= 0; k -= 4)
            {
               if (i >= n) break;
               colour[colType++] = (double)((c >> k) & 0xF)/15.0;

               if (colType == 4)
               {
                  JDRColorCMYK cmyk = new JDRColorCMYK(
                     eps.getCanvasGraphics(),
                     colour[0], colour[1], colour[2], colour[3]);
                  Color col = cmyk.getColor();
                  data[i++] = col.getRed();
                  data[i++] = col.getGreen();
                  data[i++] = col.getBlue();
                  data[i++] = 255;
                  colType = 0;
               }
            }
         }
      }
      else if (nBits == 8)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= dataSamples.length()) j = 0;

            int red, green, blue;

            double cyan = ((int)dataSamples.charAt(j++))/255.0;
            if (j >= dataSamples.length()) j = 0;
            double magenta = ((int)dataSamples.charAt(j++))/255.0;
            if (j >= dataSamples.length()) j = 0;
            double yellow = ((int)dataSamples.charAt(j++))/255.0;
            if (j >= dataSamples.length()) j = 0;
            double black = ((int)dataSamples.charAt(j++))/255.0;

            JDRColorCMYK cmyk = new JDRColorCMYK(
               eps.getCanvasGraphics(),
               cyan, magenta, yellow, black);

            Color col = cmyk.getColor();
            red = col.getRed();
            green = col.getGreen();
            blue = col.getBlue();

            data[i++] = red;
            data[i++] = green;
            data[i++] = blue;
            data[i++] = 255;
         }
      }
      else
      {
         throw new InvalidFormatException("invalid nBits value "
            +nBits, eps.getLineNum());
      }

      raster.setPixels(0, 0, width, height, data);

      BufferedImage image = new BufferedImage(width, height,
         BufferedImage.TYPE_INT_ARGB);

      image.setData(raster);

      return image;
   }

   private BufferedImage getColourCMYKImage(int width, int height, 
      int nBits, String cyanSamples, String magentaSamples,
      String yellowSamples, String blackSamples, EPS eps)
   throws InvalidFormatException
   {
      WritableRaster raster
         = ColorModel.getRGBdefault().createCompatibleWritableRaster(width, height);
      eps.printlnMessage("extracting CMYK image");

      int n = width*height*4;
      int[] data = new int[n];

      if (nBits == 1)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= cyanSamples.length()) j = 0;
            int cCyan = (int)cyanSamples.charAt(j);
            int cMagenta = (int)magentaSamples.charAt(j);
            int cYellow = (int)yellowSamples.charAt(j);
            int cBlack = (int)blackSamples.charAt(j);
            j++;

            for (int b = 7; b >= 0; b--)
            {
               if (i >= n) break;
               double cyan = (double)((cCyan & (1 << b)) >> b);
               double magenta = (double)((cMagenta & (1 << b)) >> b);
               double yellow = (double)((cYellow & (1 << b)) >> b);
               double black = (double)((cBlack & (1 << b)) >> b);

               JDRColorCMYK cmyk = new JDRColorCMYK(
                  eps.getCanvasGraphics(),
                  cyan, magenta, yellow, black);
               Color col = cmyk.getColor();
               data[i++] = col.getRed();
               data[i++] = col.getGreen();
               data[i++] = col.getBlue();
               data[i++] = 255;
            }
         }
      }
      else if (nBits == 2)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= cyanSamples.length()) j = 0;
            int cCyan = (int)cyanSamples.charAt(j);
            int cMagenta = (int)magentaSamples.charAt(j);
            int cYellow = (int)yellowSamples.charAt(j);
            int cBlack = (int)blackSamples.charAt(j);
            j++;

            for (int k = 6; k >= 0; k -= 2)
            {
               if (i >= n) break;
               double cyan = (double)((cCyan >> k) & 0x3)/3.0;
	       double magenta = (double)((cMagenta >> k) & 0x3)/3.0;
               double yellow = (double)((cYellow >> k) & 0x3)/3.0;
               double black = (double)((cBlack >> k) & 0x3)/3.0;

               JDRColorCMYK cmyk = new JDRColorCMYK(
                  eps.getCanvasGraphics(),
                  cyan, magenta, yellow, black);
               Color col = cmyk.getColor();
               data[i++] = col.getRed();
               data[i++] = col.getGreen();
               data[i++] = col.getBlue();
               data[i++] = 255;
            }
         }
      }
      else if (nBits == 4)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= cyanSamples.length()) j = 0;
            int cCyan = (int)cyanSamples.charAt(j);
            int cMagenta = (int)magentaSamples.charAt(j);
            int cYellow = (int)yellowSamples.charAt(j);
            int cBlack = (int)blackSamples.charAt(j);
            j++;

            for (int k = 4; k >= 0; k -= 4)
            {
               if (i >= n) break;
               double cyan = (double)((cCyan >> k) & 0xF)/15.0;
               double magenta = (double)((cMagenta >> k) & 0xF)/15.0;
               double yellow = (double)((cYellow >> k) & 0xF)/15.0;
               double black = (double)((cBlack >> k) & 0xF)/15.0;

               JDRColorCMYK cmyk = new JDRColorCMYK(
                  eps.getCanvasGraphics(),
                  cyan, magenta, yellow, black);
               Color col = cmyk.getColor();
               data[i++] = col.getRed();
               data[i++] = col.getGreen();
               data[i++] = col.getBlue();
               data[i++] = 255;
            }
         }
      }
      else if (nBits == 8)
      {
         for (int i = 0, j = 0; i < n; )
         {
            if (j >= cyanSamples.length()) j = 0;
            int cCyan = (int)cyanSamples.charAt(j);
            int cMagenta = (int)magentaSamples.charAt(j);
            int cYellow = (int)yellowSamples.charAt(j);
            int cBlack = (int)blackSamples.charAt(j);
            j++;

            double cyan = ((int)cCyan)/255.0;
            double magenta = ((int)cMagenta)/255.0;
            double yellow = ((int)cYellow)/255.0;
            double black = ((int)cBlack)/255.0;

            JDRColorCMYK cmyk = new JDRColorCMYK(eps.getCanvasGraphics(),
               cyan,magenta, yellow, black);

            Color col = cmyk.getColor();
            int red = col.getRed();
            int green = col.getGreen();
            int blue = col.getBlue();

            data[i++] = red;
            data[i++] = green;
            data[i++] = blue;
            data[i++] = 255;
         }
      }
      else
      {
         throw new InvalidFormatException("invalid nBits value "
            +nBits, eps.getLineNum());
      }

      raster.setPixels(0, 0, width, height, data);

      BufferedImage image = new BufferedImage(width, height,
         BufferedImage.TYPE_INT_ARGB);

      image.setData(raster);

      return image;
   }

   private BufferedImage getColourImage(int width, int height, 
      int nBits, String dataSamples, int ncomp, EPS eps)
   throws InvalidFormatException
   {
      if (ncomp == 1)
      {
         return EPSImage.getGreyImage(width, height, nBits, 
            dataSamples, eps);
      }
      else if (ncomp == 3)
      {
         return getColourRGBImage(width, height, nBits, dataSamples,
            eps);
      }
      else if (ncomp == 4)
      {
         return getColourCMYKImage(width, height, nBits, dataSamples,
            eps);
      }

      throw new InvalidFormatException(
         "(colorimage) invalid nComp value "+ncomp,
          eps.getLineNum());
   }

   private BufferedImage getColourImage(int width, int height, 
      int nBits, String[] dataSamples, EPS eps)
   throws InvalidFormatException
   {
      int ncomp = dataSamples.length;

      if (ncomp == 1)
      {
         return EPSImage.getGreyImage(width, height, nBits,
            dataSamples[0], eps);
      }
      else if (ncomp == 3)
      {
         return getColourRGBImage(width, height, nBits,
           dataSamples[0], dataSamples[1], dataSamples[2], eps);
      }
      else if (ncomp == 4)
      {
         return getColourCMYKImage(width, height, nBits,
           dataSamples[0], dataSamples[1], dataSamples[2],
           dataSamples[3], eps);
      }

      throw new InvalidFormatException(
         "(colorimage) invalid nComp value "+ncomp,
         eps.getLineNum());
   }
}
