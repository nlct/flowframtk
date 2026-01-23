// File          : JDRGray.java
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
import java.util.Locale;
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing grey.
 * @author Nicola L C Talbot
 */

public class JDRGray extends JDRPaint implements Serializable
{
   /**
    * Creates a new grey.
    * The transparency is set to 1 (opaque).
    * The grey value must be in the range 0 (black) to 1 (white), inclusive.
    * @param g grey scale
    */
   public JDRGray(CanvasGraphics cg, double g)
   {
      this(cg, g, 1.0);
   }

   /**
    * Creates a new grey.
    * The grey and alpha values must be in the range 0-1, inclusive.
    * @param g grey scale
    * @param a alpha (transparency)
    */
   public JDRGray(CanvasGraphics cg, double g, double a)
   {
      super(cg);

      setGray(g);
      setAlpha(a);
   }

   /**
    * Creates a new grey (black).
    */
   public JDRGray(CanvasGraphics cg)
   {
      super(cg);

      gray  = 0;
      alpha = 1;
   }

   public JDRGray()
   {
      this(null);
   }

   public JDRPaint average(JDRPaint paint)
   {
      JDRGray c;

      if (paint instanceof JDRGray)
      {
         c = (JDRGray)paint.clone();
      }
      else
      {
         c = paint.getJDRGray();
      }

      c.setGray(0.5*(c.getGray()+getGray()));
      c.setAlpha(0.5*(c.getAlpha()+getAlpha()));

      return c;
   }

   public JDRGray getJDRGray()
   {
      return this;
   }

   public JDRColorCMYK getJDRColorCMYK()
   {
      double black   = (1-gray);
      double cyan    = 0;
      double magenta = 0;
      double yellow  = 0;

      return new JDRColorCMYK(getCanvasGraphics(),
        cyan,magenta,yellow,black,alpha);
   }

   public JDRColor getJDRColor()
   {
      return new JDRColor(getCanvasGraphics(), gray, gray, gray, alpha);
   }

   public JDRColorHSB getJDRColorHSB()
   {
      return new JDRColorHSB(getCanvasGraphics(), 0, 0, gray);
   }

   public Paint getPaint(BBox box)
   {
      return getColor();
   }

   public Color getColor()
   {
      float val = (float)gray;
      return new Color(val,val,val,(float)alpha);
   }

   @Override
   public boolean isBlack()
   {
      return gray == 0.0 && alpha == 1.0;
   }

   public String toString()
   {
      return new String("JDRGray@"+"Gray:"+gray+"Alpha:"+alpha);
   }

   public Object clone()
   {
      return new JDRGray(getCanvasGraphics(), gray,alpha);
   }

   public String pgfmodel()
   {
      return "gray";
   }

   public String pgfspecs()
   {
      return PGF.format(gray);
   }

   public void saveEPS(PrintWriter out, BBox box)
      throws IOException
   {
      out.println(""+gray+" setgray");
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
    * Gets the gray scale parameter for this paint. This will be a value in the range
    * 0 to 1.
    * @return the gray scale
    */
   public double getGray()
   {
      return gray;
   }

   /**
    * Sets the alpha component for this paint. This value must be
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
    * Sets the grey scale for this paint. This value must be
    * in the range 0 to 1, inclusive.
    * @param g the gray scale
    */
   public void setGray(double g)
   {
      if (g < 0.0 || g > 1.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.GREY, g, getCanvasGraphics());
      }

      gray = g;
   }


   public String svg()
   {
      return "gray("+(100*gray)+"%)";
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
      return Integer.toHexString((int)(gray*255))
           + "." + Integer.toHexString((int)(gray*255));
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;

      if (obj == null)
      {
         return false;
      }

      if (!(obj instanceof JDRGray))
      {
         return false;
      }

      JDRGray c = (JDRGray)obj;

      return (getGray() == c.getGray()
           && getAlpha() == c.getAlpha());
   }

   public JDRPaintLoaderListener getListener()
   {
      return listener;
   }

   public void makeEqual(JDRPaint paint)
   {
      super.makeEqual(paint);

      JDRGray p = (JDRGray)paint;

      gray = p.gray;
      alpha = p.alpha;
   }

   public String getPdfStrokeSpecs()
   {
      return String.format(Locale.ROOT, "%f G", gray);
   }

   public String getPdfFillSpecs()
   {
      return String.format(Locale.ROOT, "%f g", gray);
   }

   private double gray, alpha;
   // gray ranges from 0 (black) to 1 (white)

   private static JDRGrayListener listener = new JDRGrayListener();
}
