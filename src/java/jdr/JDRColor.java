// File          : JDRColor.java
// Creation Date : 1st February 2006
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

package com.dickimawbooks.jdr;

import java.io.*;
import java.util.Locale;
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing RGB colour.
 * @author Nicola L C Talbot
 */

public class JDRColor extends JDRPaint implements Serializable
{
   /**
    * Creates a new RGB colour. The transparency is set to 1 (opaque).
    * The RGB parameters must be in the range 0-1 (inclusive).
    * @param r red 
    * @param g green
    * @param b blue
    */
   public JDRColor(CanvasGraphics cg, double r, double g, double b)
   {
      this(cg, r, g, b, 1.0);
   }

   /**
    * Creates a new RGB colour. 
    * All parameters must be in the range 0-1 (inclusive). A value of a = 0 
    * indicates completely transparent and value of a = 1 
    * indicates completely opaque.
    * @param r red
    * @param g green
    * @param b blue
    * @param a alpha (transparency)
    */
   public JDRColor(CanvasGraphics cg, double r, double g, double b, double a)
   {
      super(cg);

      setRed(r);
      setGreen(g);
      setBlue(b);
      setAlpha(a);
   }

   /**
    * Creates a new RGB colour.
    * @param c RGB colour specification
    */
   public JDRColor(CanvasGraphics cg, Color c)
   {
      super(cg);

      red = c.getRed()/255.0;
      green = c.getGreen()/255.0;
      blue = c.getBlue()/255.0;
      alpha = c.getAlpha()/255.0;
   }

   /**
    * Creates a new RGB colour (black).
    */
   public JDRColor(CanvasGraphics cg)
   {
      super(cg);

      red   = 0;
      green = 0;
      blue  = 0;
      alpha = 1;
   }

   public JDRColor()
   {
      this(null);
   }

   public JDRPaint average(JDRPaint paint)
   {
      JDRColor c;

      if (paint instanceof JDRColor)
      {
         c = (JDRColor)paint.clone();
      }
      else
      {
         c = paint.getJDRColor();
      }

      c.setRed(0.5*(c.getRed()+getRed()));
      c.setGreen(0.5*(c.getGreen()+getGreen()));
      c.setBlue(0.5*(c.getBlue()+getBlue()));
      c.setAlpha(0.5*(c.getAlpha()+getAlpha()));

      return c;
   }

   /**
    * Gets this.
    * @return this colour
    */
   public JDRColor getJDRColor()
   {
      return this;
   }

   public JDRGray getJDRGray()
   {
      double gray = (red+green+blue)/3;

      return new JDRGray(getCanvasGraphics(), gray, alpha);
   }

   public JDRColorCMYK getJDRColorCMYK()
   {
      double black   = Math.min(1.0-red,
                       Math.min(1.0-green,1.0-blue));
      double cyan    = 0;
      double magenta = 0;
      double yellow  = 0;

      if (black < 1)
      {
         cyan    = (1.0-red-black)/(1.0-black);
         magenta = (1.0-green-black)/(1.0-black);
         yellow  = (1.0-blue-black)/(1.0-black);
      }

      return new JDRColorCMYK(getCanvasGraphics(),
        cyan,magenta,yellow,black,alpha);
   }

   public JDRColorHSB getJDRColorHSB()
   {
      double max = Math.max(Math.max(red, green), blue);
      double min = Math.min(Math.min(red, green), blue);

      double hue=0, saturation, brightness;

      if (max == min)
      {
         hue = 0;
      }
      else if (max == red && green >= blue)
      {
         hue = 60*(green-blue)/(max-min);
      }
      else if (max == red && green < blue)
      {
         hue = 60*(green-blue)/(max-min) + 360;
      }
      else if (max == green)
      {
         hue = 60*(blue-red)/(max-min) + 120;
      }
      else if (max == blue)
      {
         hue = 60*(red-green)/(max-min) + 240;
      }

      saturation = (max == 0 ? 0 : 1- min/max);

      brightness = max;

      return new JDRColorHSB(getCanvasGraphics(), 
         hue, saturation, brightness);
   }

   public Paint getPaint(BBox box)
   {
      return getColor();
   }

   public Color getColor()
   {
      return new Color((float)red,(float)green,(float)blue,(float)alpha);
   }

   public String toString()
   {
      return new String("JDRColor@"+"R:" +red+"G:" +green+"B:"
                        +blue+"A:"+alpha);
   }

   public Object clone()
   {
      return new JDRColor(getCanvasGraphics(), red,green,blue,alpha);
   }

   public String pgfmodel()
   {
      return "rgb";
   }

   public String pgfspecs()
   {
      return PGF.format(red)+","+PGF.format(green) +","+PGF.format(blue);
   }

   public void saveEPS(PrintWriter out, BBox box)
      throws IOException
   {
      out.println(""+red+" "+green+" "+blue+" setrgbcolor");
   }

   public int psLevel()
   {
      return 1;
   }

   public double getAlpha()
   {
      return alpha;
   }

   /**
    * Gets the red component. This will be a value in the range
    * 0 to 1.
    * @return the red component of this colour
    */
   public double getRed()
   {
      return red;
   }

   /**
    * Gets the blue component. This will be a value in the range
    * 0 to 1.
    * @return the blue component of this colour
    */
   public double getBlue()
   {
      return blue;
   }

   /**
    * Gets the green component. This will be a value in the range
    * 0 to 1.
    * @return the green component of this colour
    */
   public double getGreen()
   {
      return green;
   }

   /**
    * Sets the red component for this colour. This value must be
    * in the range 0 to 1, inclusive.
    * @param r the red component
    */
   public void setRed(double r)
   {
      if (r < 0.0 || r > 1.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.RED, r, getCanvasGraphics());
      }

      red = r;
   }

   /**
    * Sets the green component for this colour. This value must be
    * in the range 0 to 1, inclusive.
    * @param g the green component
    */
   public void setGreen(double g)
   {
      if (g < 0.0 || g > 1.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.GREEN, g, getCanvasGraphics());
      }

      green = g;
   }

   /**
    * Sets the blue component for this colour. This value must be
    * in the range 0 to 1, inclusive.
    * @param b the blue component
    */
   public void setBlue(double b)
   {
      if (b < 0.0 || b > 1.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.BLUE, b, getCanvasGraphics());
      }

      blue = b;
   }

   /**
    * Sets the alpha component for this colour. This value must be
    * in the range 0 to 1, inclusive.
    * @param a the alpha component
    */
   public void setAlpha(double a)
   {
      if (a < 0.0 || a > 1.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.ALPHA, a, getCanvasGraphics());
      }

      alpha = a;
   }

   public String svg()
   {
      return "rgb("+(100*red)+"%," +(100*green)+"%,"+(100*blue)+"%)";
   }

   public String svgFill()
   {
      return "fill=\""+svg()+"\" fill-opacity=\""
      +getAlpha()+"\"";
   }

   public String svgLine()
   {
      return "stroke=\""+svg()+"\" stroke-opacity=\""
      +getAlpha()+"\"";
   }

   public String getID()
   {
      Color c = getColor();

      return Integer.toHexString(c.getRed())
           + "." + Integer.toHexString(c.getGreen())
           + "." + Integer.toHexString(c.getBlue())
           + "." + Integer.toHexString(c.getAlpha());
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;

      if (obj == null)
      {
         return false;
      }

      if (!(obj instanceof JDRColor))
      {
         return false;
      }

      JDRColor c = (JDRColor)obj;

      return (getRed() == c.getRed()
           && getGreen() == c.getGreen()
           && getBlue() == c.getBlue()
           && getAlpha() == c.getAlpha());
   }

   public JDRPaintLoaderListener getListener()
   {
      return listener;
   }

   public void makeEqual(JDRPaint paint)
   {
      super.makeEqual(paint);

      JDRColor col = (JDRColor)paint;

      alpha = col.alpha;

      red = col.red;
      green = col.green;
      blue = col.blue;
      alpha = col.alpha;
   }

   public String getPdfStrokeSpecs()
   {
      return String.format(Locale.ROOT, "%f %f %f RG", red, green, blue);
   }

   public String getPdfFillSpecs()
   {
      return String.format(Locale.ROOT, "%f %f %f rg", red, green, blue);
   }

   private double red, green, blue, alpha;

   private static JDRColorListener listener = new JDRColorListener();
}
