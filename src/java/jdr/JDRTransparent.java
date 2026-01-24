// File          : JDRTransparent.java
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
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.*;

/**
 * Class representing completely transparent colour (i.e no colour).
 * This is used to indicate that something shouldn't be drawn
 * or filled. For example, a fill colour of this type indicates
 * that the path shouldn't be filled (but may be stroked) and a 
 * line colour of this type indicates that the line shouldn't be 
 * stroked (but may be filled). A marker fill colour of this type
 * indicates that the marker should use the same colour as the path
 * to which it is attached.
 * @author Nicola L C Talbot
 */
public class JDRTransparent extends JDRPaint implements Serializable
{
   public JDRTransparent()
   {
      this(null);
   }

   public JDRTransparent(CanvasGraphics cg)
   {
      super(cg);
   }

   public Color getColor()
   {
      return colour;
   }

   public JDRColor getJDRColor()
   {
      return new JDRColor(getCanvasGraphics(),0,0,0,0);
   }

   public JDRColorHSB getJDRColorHSB()
   {
      return new JDRColorHSB(getCanvasGraphics(),0,0,0,0);
   }

   public JDRColorCMYK getJDRColorCMYK()
   {
      return new JDRColorCMYK(getCanvasGraphics(),0,0,0,0,0);
   }

   public JDRGray getJDRGray()
   {
      return new JDRGray(getCanvasGraphics(),0,0);
   }

   public Paint getPaint(BBox box)
   {
      return colour;
   }

   public String getID()
   {
      return "0.0.0.0";
   }

   public String pgfmodel() {return "";}

   public String pgfspecs() {return "";}

   public String pgf(BBox box) {return "\\relax ";}

   public String pgffillcolor(BBox box) {return "\\relax ";}

   public String pgfstrokecolor(BBox box) {return "\\relax ";}

   public void saveEPS(PrintWriter out, BBox box)
      throws IOException
   {
   }

   public int psLevel()
   {
      return 1;
   }

   public double getAlpha()
   {
      return 0.0;
   }

   /**
    * Does nothing
    */  
   public void setAlpha(double alpha)
   {
   }

   public String svg()
   {
      return "none";
   }

   public String svgFill()
   {
      return "fill=\"none\"";
   }

   public String svgLine()
   {
      return "stroke=\"none\"";
   }

   public Object clone()
   {
      return new JDRTransparent(getCanvasGraphics());
   }

   @Override
   public String toString()
   {
      return new String(getClass().getSimpleName());
   }

   @Override
   public String info()
   {
      return getCanvasGraphics().getMessageWithFallback(
        "objectinfo.paint.transparent", "transparent");
   }

   public JDRPaintLoaderListener getListener()
   {
      return listener;
   }

   public void fade(double value)
   {
   }

   public JDRPaint removeTransparency()
   {
      return this;
   }

   public JDRPaint average(JDRPaint paint)
   {
      JDRPaint p = (JDRPaint)paint.clone();
      p.setAlpha(0.5*(paint.getAlpha()+getAlpha()));

      return p;
   }

   public String getPdfStrokeSpecs()
   {
      return "";
   }

   public String getPdfFillSpecs()
   {
      return "";
   }

   private static Color colour = new Color(0, 0, 0, 0);

   private static JDRTransparentListener listener = new JDRTransparentListener();
}
