// File          : JDRColorCMYK.java
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
 * Class representing CMYK colour.
 * @author Nicola L C Talbot
 */
public class JDRColorCMYK extends JDRPaint implements Serializable
{
   /**
    * Creates a new CYMK colour. The transparency is set to 1.0
    * (opaque).
    * The parameters must all be in the range 0-1, inclusive.
    * @param c cyan
    * @param m magenta
    * @param y yellow
    * @param k black
    */
   public JDRColorCMYK(CanvasGraphics cg, double c, double m, double y, double k)
   {
      this(cg, c, m, y, k, 1.0);
   }

   /**
    * Creates a new CYMK colour.
    * The parameters must all be in the range 0-1, inclusive.
    * @param c cyan
    * @param m magenta
    * @param y yellow
    * @param k black
    * @param a alpha (transparency)
    */
   public JDRColorCMYK(CanvasGraphics cg,
      double c, double m, double y, double k, double a)
   {
      super(cg);

      setCyan(c);
      setMagenta(m);
      setYellow(y);
      setKey(k);
      setAlpha(a);
   }

   /**
    * Creates a new CMYK colour (black).
    */
   public JDRColorCMYK(CanvasGraphics cg)
   {
      super(cg);

      cyan    = 0;
      magenta = 0;
      yellow  = 0;
      key     = 1;
      alpha   = 1;
   }

   public JDRColorCMYK()
   {
      this(null);
   }

   public JDRPaint average(JDRPaint paint)
   {
      JDRColorCMYK c;

      if (paint instanceof JDRColorCMYK)
      {
         c = (JDRColorCMYK)paint.clone();
      }
      else
      {
         c = paint.getJDRColorCMYK();
      }

      c.setCyan(0.5*(c.getCyan()+getCyan()));
      c.setMagenta(0.5*(c.getMagenta()+getMagenta()));
      c.setYellow(0.5*(c.getYellow()+getYellow()));
      c.setKey(0.5*(c.getKey()+getKey()));
      c.setAlpha(0.5*(c.getAlpha()+getAlpha()));

      return c;
   }

   /**
    * Gets this.
    * @return this
    */
   public JDRColorCMYK getJDRColorCMYK()
   {
      return this;
   }

   /**
    * Gets the closest matching {@link JDRGray}. This is obtained
    * as follows: if key=0, the gray value is
    * computed as 1-(cyan+magenta+yellow)/3, otherwise it is
    * computed as 1-key.
    */
   public JDRGray getJDRGray()
   {
      double gray;

      if (key == 0)
      {
         gray = 1-(cyan+magenta+yellow)/3;
      }
      else
      {
         gray = 1-key;
      }

      return new JDRGray(getCanvasGraphics(), gray, alpha);
   }

   public JDRColor getJDRColor()
   {
      // convert from cmyk to rgb
      double red   = 1.0-Math.min(1.0,cyan*(1-key)+key);
      double green = 1.0-Math.min(1.0,magenta*(1-key)+key);
      double blue  = 1.0-Math.min(1.0,yellow*(1-key)+key);

      return new JDRColor(getCanvasGraphics(), red, green,blue,alpha);
   }

   public JDRColorHSB getJDRColorHSB()
   {
      return getJDRColor().getJDRColorHSB();
   }

   public Paint getPaint(BBox box)
   {
      return getColor();
   }

   public Color getColor()
   {
      // convert from cmyk to rgb
      double red   = 1.0-Math.min(1.0,cyan*(1-key)+key);
      double green = 1.0-Math.min(1.0,magenta*(1-key)+key);
      double blue  = 1.0-Math.min(1.0,yellow*(1-key)+key);

      return new Color((float)red, (float)green, (float)blue, (float)alpha);
   }

   @Override
   public boolean isBlack()
   {
      return key == 1.0 && cyan == 0.0 && magenta == 0.0 && yellow == 0.0
             && alpha == 1.0;
   }

   public String toString()
   {
      return new String("JDRColorCMYK@"+"C:" +cyan+"M:" +magenta+"Y:"
                        +yellow+"K:"+key+"A:"+alpha);
   }

   public Object clone()
   {
      return new JDRColorCMYK(getCanvasGraphics(),
         cyan,magenta,yellow,key,alpha);
   }

   public String pgfmodel()
   {
      return "cmyk";
   }

   public String pgfspecs()
   {
      return PGF.format(cyan)+","
           + PGF.format(magenta)+","+PGF.format(yellow)+","
           + PGF.format(key);
   }

   public void saveEPS(PrintWriter out, BBox box)
      throws IOException
   {
      out.println(""+cyan+" "+magenta+" "+yellow+" "+key
                 +" setcmykcolor");
   }

   public int psLevel()
   {
      return 2;
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
    * Gets the cyan component. This will be a value in the range
    * 0 to 1.
    * @return the cyan component of this colour
    */
   public double getCyan()
   {
      return cyan;
   }

   /**
    * Gets the magenta component. This will be a value in the range
    * 0 to 1.
    * @return the magenta component of this colour
    */
   public double getMagenta()
   {
      return magenta;
   }

   /**
    * Gets the yellow component. This will be a value in the range
    * 0 to 1.
    * @return the yellow component of this colour
    */
   public double getYellow()
   {
      return yellow;
   }

   /**
    * Gets the key (black) component. This will be a value in the range
    * 0 to 1.
    * @return the key (black) component of this colour
    */
   public double getKey()
   {
      return key;
   }

   /**
    * Sets the cyan component for this colour. This value must be
    * in the range 0 to 1, inclusive.
    * @param c the cyan component
    */
   public void setCyan(double c)
   {
      if (c < 0.0 || c > 1.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.CYAN, c, getCanvasGraphics());
      }

      cyan = c;
   }

   /**
    * Sets the magenta component for this colour. This value must be
    * in the range 0 to 1, inclusive.
    * @param m the magenta component
    */
   public void setMagenta(double m)
   {
      if (m < 0.0 || m > 1.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.MAGENTA, m, getCanvasGraphics());
      }

      magenta = m;
   }

   /**
    * Sets the yellow component for this colour. This value must be
    * in the range 0 to 1, inclusive.
    * @param y the yellow component
    */
   public void setYellow(double y)
   {
      if (y < 0.0 || y > 1.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.YELLOW, y, getCanvasGraphics());
      }

      yellow = y;
   }

   /**
    * Sets the key (black) component for this colour. This value must be
    * in the range 0 to 1, inclusive.
    * @param k the key component
    */
   public void setKey(double k)
   {
      if (k < 0.0 || k > 1.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.BLACK, k, getCanvasGraphics());
      }

      key = k;
   }

   public String getID()
   {
      return Integer.toHexString((int)(255*cyan))
           + "." + Integer.toHexString((int)(255*magenta))
           + "." + Integer.toHexString((int)(255*yellow))
           + "." + Integer.toHexString((int)(255*key))
           + "." + Integer.toHexString((int)(255*alpha));
   }

   public String svg()
   {
      // svg doesn't really support CMYK
      return getJDRColor().svg();
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

   public boolean equals(Object obj)
   {
      if (this == obj) return true;

      if (obj == null)
      {
         return false;
      }

      if (!(obj instanceof JDRColorCMYK))
      {
         return false;
      }

      JDRColorCMYK c = (JDRColorCMYK)obj;

      return (getCyan() == c.getCyan()
           && getYellow() == c.getYellow()
           && getMagenta() == c.getMagenta()
           && getKey() == c.getKey()
           && getAlpha() == c.getAlpha());
   }

   public JDRPaintLoaderListener getListener()
   {
      return listener;
   }

   public void makeEqual(JDRPaint paint)
   {
      super.makeEqual(paint);

      JDRColorCMYK cmyk = (JDRColorCMYK)paint;

      cyan = cmyk.cyan;
      magenta = cmyk.magenta;
      yellow = cmyk.yellow;
      key = cmyk.key;
      alpha = cmyk.alpha;
   }

   public String getPdfStrokeSpecs()
   {
      return String.format(Locale.ROOT,
        "%f %f %f %f K", cyan, magenta, yellow, key);
   }

   public String getPdfFillSpecs()
   {
      return String.format(Locale.ROOT,
         "%f %f %f %f k", cyan, magenta, yellow, key);
   }

   private double cyan, magenta, yellow, key, alpha;

   private static JDRColorCMYKListener listener = new JDRColorCMYKListener();
}
