// File          : JDRColorHSB.java
// Creation Date : 20th March 2007
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
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing HSB colour.
 * @author Nicola L C Talbot
 */

public class JDRColorHSB extends JDRPaint implements Serializable
{
   /**
    * Creates a new HSB colour. The transparency is set to 1 (opaque).
    * The hue must be in the range [0, 360).
    * The saturation and brightness must be in the range 0-1,
    * inclusive.
    * @param h hue
    * @param s saturation
    * @param b brightness
    */
   public JDRColorHSB(CanvasGraphics cg, double h, double s, double b)
   {
      this(cg, h, s, b, 1.0);

   }

   /**
    * Creates a new HSB colour.
    * The hue must be in the range [0, 360).
    * The saturation and brightness must be in the range 0-1.
    * @param h hue
    * @param s saturation
    * @param b brightness
    * @param a alpha (transparency)
    */
   public JDRColorHSB(CanvasGraphics cg, double h, double s, double b, double a)
   {
      super(cg);

      setHue(h);
      setSaturation(s);
      setBrightness(b);
      setAlpha(a);
   }

   /**
    * Creates a new HSB colour (black).
    */
   public JDRColorHSB(CanvasGraphics cg)
   {
      super(cg);

      hue        = 0;
      saturation = 0;
      brightness = 0;
      alpha      = 1;
   }

   public JDRColorHSB()
   {
      this(null);
   }

   public JDRPaint average(JDRPaint paint)
   {
      JDRColorHSB c;

      if (paint instanceof JDRColorHSB)
      {
         c = (JDRColorHSB)paint.clone();
      }
      else
      {
         c = paint.getJDRColorHSB();
      }

      c.setHue(0.5*(c.getHue()+getHue()));
      c.setSaturation(0.5*(c.getSaturation()+getSaturation()));
      c.setBrightness(0.5*(c.getBrightness()+getBrightness()));
      c.setAlpha(0.5*(c.getAlpha()+getAlpha()));

      return c;
   }

   public JDRGray getJDRGray()
   {
      return getJDRColor().getJDRGray();
   }

   public JDRColorCMYK getJDRColorCMYK()
   {
      return getJDRColor().getJDRColorCMYK();
   }

   /**
    * Gets this.
    * @return this colour
    */
   public JDRColorHSB getJDRColorHSB()
   {
      return this;
   }

   public JDRColor getJDRColor()
   {
      // convert from hsb to rgb
      int h = ((int)Math.floor(hue/60)) % 6;
      double f = hue/60 - Math.floor(hue/60);
      double p = brightness*(1-saturation);
      double q = brightness*(1-f*saturation);
      double t = brightness*(1-(1-f)*saturation);
      double red, green, blue;

      switch (h)
      {
         case 0:
            red   = brightness;
            green = t;
            blue  = p;
         break;
         case 1:
            red   = q;
            green = brightness;
            blue  = p;
         break;
         case 2:
            red   = p;
            green = brightness;
            blue  = t;
         break;
         case 3:
            red   = p;
            green = q;
            blue  = brightness;
         break;
         case 4:
            red   = t;
            green = p;
            blue  = brightness;
         break;
         default:
            red   = brightness;
            green = p;
            blue  = q;
      }

      return new JDRColor(getCanvasGraphics(), red, green,blue,alpha);
   }

   public Paint getPaint(BBox box)
   {
      return getColor();
   }

   public Color getColor()
   {
      return Color.getHSBColor((float)hue, (float)saturation, (float)brightness);
   }

   public String toString()
   {
      return new String("JDRColorHSB@"+"H:" +hue+"S:" +saturation+"B:"
                        +brightness+"A:"+alpha);
   }

   public Object clone()
   {
      return new JDRColorHSB(getCanvasGraphics(),
        hue,saturation,brightness,alpha);
   }

   public String pgfmodel()
   {
      return "hsb";
   }

   public String pgfspecs()
   {
      return PGF.format(hue/360.0)+","+PGF.format(saturation)+","+PGF.format(brightness);
   }

   public void saveEPS(PrintWriter out, BBox box)
      throws IOException
   {
      out.println(""+(hue/360.0)+" "+saturation+" "+brightness +" sethsbcolor");
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

   /**
    * Sets the hue component for this colour. This value must be
    * in the range [0, 360).
    * @param h the hue component
    */
   public void setHue(double h)
   {
      if (h < 0.0 || h >= 360)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.HUE, h, getCanvasGraphics());
      }

      hue = h;
   }

   /**
    * Sets the saturation component for this colour. This value must be
    * in the range 0 to 1, inclusive.
    * @param s the saturation component
    */
   public void setSaturation(double s)
   {
      if (s < 0.0 || s > 1.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.SATURATION, s, getCanvasGraphics());
      }

      saturation = s;
   }

   /**
    * Sets the brightness component for this colour. This value must be
    * in the range 0 to 1, inclusive.
    * @param b the brightness component
    */
   public void setBrightness(double b)
   {
      if (b < 0.0 || b > 1.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.BRIGHTNESS, b, getCanvasGraphics());
      }

      brightness = b;
   }

   /**
    * Gets the hue component. This will be a value in the range
    * 0 to 1.
    * @return the hue component of this colour
    */
   public double getHue()
   {
      return hue;
   }

   /**
    * Gets the saturation component. This will be a value in the range
    * 0 to 1.
    * @return the saturation component of this colour
    */
   public double getSaturation()
   {
      return saturation;
   }

   /**
    * Gets the brightness component. This will be a value in the range
    * 0 to 1.
    * @return the brightness component of this colour
    */
   public double getBrightness()
   {
      return brightness;
   }

   public String getID()
   {
      return Integer.toHexString((int)hue)
           + "." + Integer.toHexString((int)(255*saturation))
           + "." + Integer.toHexString((int)(255*brightness))
           + "." + Integer.toHexString((int)(255*alpha));
   }

   public String svg()
   {
      return getJDRColor().svg();
   }

   public String svgFill()
   {
      return "fill=\""+getJDRColor().svg()+"\" fill-opacity=\""
      +getAlpha()+"\"";
   }

   public String svgLine()
   {
      return "stroke=\""+getJDRColor().svg()+"\" stroke-opacity=\""
      +getAlpha()+"\"";
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;

      if (obj == null)
      {
         return false;
      }

      if (!(obj instanceof JDRColorHSB))
      {
         return false;
      }

      JDRColorHSB c = (JDRColorHSB)obj;

      return (getHue() == c.getHue()
           && getSaturation() == c.getSaturation()
           && getBrightness() == c.getBrightness()
           && getAlpha() == c.getAlpha());
   }

   public JDRPaintLoaderListener getListener()
   {
      return listener;
   }

   public void makeEqual(JDRPaint paint)
   {
      super.makeEqual(paint);

      JDRColorHSB hsb = (JDRColorHSB)paint;

      hue = hsb.hue;
      saturation = hsb.saturation;
      brightness = hsb.brightness;
      alpha = hsb.alpha;
   }

   public String getPdfStrokeSpecs()
   {
      return getJDRColor().getPdfStrokeSpecs();
   }

   public String getPdfFillSpecs()
   {
      return getJDRColor().getPdfFillSpecs();
   }

   private double hue, saturation, brightness, alpha;

   private static JDRColorHSBListener listener = new JDRColorHSBListener();
}
