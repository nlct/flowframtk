/*
    Copyright (C) 2026 Nicola L.C. Talbot

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

import java.io.IOException;

import java.awt.Color;
import java.awt.Paint;

import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.*;

public abstract class JDRAbstractShading extends JDRPaint implements JDRShading
{
   protected JDRAbstractShading(JDRPaint sColor, JDRPaint mColor, JDRPaint eColor)
   {
      super(sColor.getCanvasGraphics());

      setStartColor(sColor);
      setMidColor(mColor);
      setEndColor(eColor);
   }

   protected JDRAbstractShading(CanvasGraphics cg)
   {
      super(cg);
   }

   public Color getColor()
   {
      return startColor.getColor();
   }

   @Override
   public boolean isBlack()
   {
      if (hasMidColor())
      {
         return startColor.isBlack() && midColor.isBlack() && endColor.isBlack();
      }
      else
      {
         return startColor.isBlack() && endColor.isBlack();
      }
   }

   public JDRPaint average()
   {
      if (hasMidColor())
      {
         JDRColor start = getStartColor().getJDRColor();
         JDRColor mid = getMidColor().getJDRColor();
         JDRColor end = getEndColor().getJDRColor();

         double r = (start.getRed()+mid.getRed()+end.getRed())/3;
         double g = (start.getGreen()+mid.getGreen()+end.getGreen())/3;
         double b = (start.getBlue()+mid.getBlue()+end.getBlue())/3;
         double a = (start.getAlpha()+mid.getAlpha()+end.getAlpha())/3;

         return new JDRColor(getCanvasGraphics(), r, g, b, a);
      }
      else
      {
         return getStartColor().average(getEndColor());
      }
   }

   public abstract JDRPaint average(JDRPaint paint);

   public JDRColor getJDRColor()
   {
      return startColor.getJDRColor();
   }

   public JDRColorCMYK getJDRColorCMYK()
   {
      return startColor.getJDRColorCMYK();
   }

   public JDRColorHSB getJDRColorHSB()
   {
      return startColor.getJDRColorHSB();
   }

   public JDRGray getJDRGray()
   {
      return startColor.getJDRGray();
   }

   public String getPdfStrokeSpecs()
   {
      return getJDRColor().getPdfStrokeSpecs();
   }

   public String getPdfFillSpecs()
   {
      return getJDRColor().getPdfFillSpecs();
   }

   public abstract Paint getPaint(BBox box);

   public abstract String pgffillcolor(BBox box);

   public abstract String getID();

   @Override
   public void writeSVGdefs(SVG svg) throws IOException
   {
      String id = getID();

      if (svg.addReferenceID(id))
      {
         svgDef(svg, id);
      }
   }

   protected abstract void svgDef(SVG svg, String id) throws IOException;

   public String svgFill()
   {
      return "fill=\"url(#"+getID()+")\"";
   }

   public String svgLine()
   {
      return "stroke=\"url(#"+getID()+")\"";
   }

   public String svg()
   {
      return "url(#"+getID()+")";
   }

   public double getAlpha()
   {
      if (hasMidColor())
      {
         return (startColor.getAlpha()+midColor.getAlpha()+endColor.getAlpha())/3;
      }
      else
      {
         return 0.5*(startColor.getAlpha()+endColor.getAlpha());
      }
   }

   public void setAlpha(double alpha)
   {
      startColor.setAlpha(alpha);
      endColor.setAlpha(alpha);

      if (hasMidColor())
      {
         midColor.setAlpha(alpha);
      }
   }

   /**
    * Gets this shading's start colour.
    * @return this shading's start colour
    */
   @Override
   public JDRPaint getStartColor()
   {
      return startColor;
   }

   /**
    * Gets this shading's end colour.
    * @return this shading's end colour
    */
   @Override
   public JDRPaint getEndColor()
   {
      return endColor;
   }

   /**
    * Gets this shading's mid colour.
    * @return this shading's mid colour
    */
   @Override
   public JDRPaint getMidColor()
   {
      return midColor;
   }

   @Override
   public boolean hasMidColor()
   {
      return midColor != null && !(midColor instanceof JDRTransparent);
   }

   /**
    * Sets this shading's start colour.
    */
   public void setStartColor(JDRPaint sColor)
   {
      if (sColor instanceof JDRShading)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.SHADING_START,
            sColor.getClass().toString(), getCanvasGraphics());
      }

      startColor = sColor;
   }

   /**
    * Sets this shading's end colour.
    */
   public void setEndColor(JDRPaint eColor)
   {
      if (eColor instanceof JDRShading)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.SHADING_END,
            eColor.getClass().toString(), getCanvasGraphics());
      }

      endColor = eColor;
   }

   /**
    * Sets this shading's mid colour.
    */
   public void setMidColor(JDRPaint mColor)
   {
      if (mColor instanceof JDRShading)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.SHADING_MID,
            mColor.getClass().toString(), getCanvasGraphics());
      }

      midColor = mColor;
   }

   @Override
   public void reduceToGreyScale()
   {
      startColor = startColor.getJDRGray();
      endColor = endColor.getJDRGray();

      if (hasMidColor())
      {
          midColor = midColor.getJDRGray();
      }
   }

   @Override
   public void convertToCMYK()
   {
      startColor = startColor.getJDRColorCMYK();
      endColor = endColor.getJDRColorCMYK();

      if (hasMidColor())
      {
         midColor = midColor.getJDRColorCMYK();
      }
   }

   @Override
   public void convertToRGB()
   {
      startColor = startColor.getJDRColor();
      endColor = endColor.getJDRColor();

      if (hasMidColor())
      {
         midColor = midColor.getJDRColor();
      }
   }

   @Override
   public void convertToHSB()
   {
      startColor = startColor.getJDRColorHSB();
      endColor = endColor.getJDRColorHSB();

      if (hasMidColor())
      {
         midColor = midColor.getJDRColorHSB();
      }
   }

   public void fade(double value)
   {
      startColor.fade(value);
      endColor.fade(value);

      if (hasMidColor())
      {
         midColor.fade(value);
      }
   }

   public String pgfmodel()
   {     
      return startColor.pgfmodel();
   }        
            
   public String pgfspecs()
   {     
      return startColor.pgfspecs();
   }

   public String pgf(BBox box)
   {        
      return startColor.pgf(box);
   }     

   public String pgfstrokecolor(BBox box)
   {        
      return startColor.pgfstrokecolor(box);
   }     

   public abstract JDRShading convertShading(String label);

   @Override
   public void setCanvasGraphics(CanvasGraphics cg)
   {
      super.setCanvasGraphics(cg);

      if (startColor != null)
      {
         startColor.setCanvasGraphics(cg);
      }

      if (midColor != null)
      {
         midColor.setCanvasGraphics(cg);
      }

      if (endColor != null)
      {
         endColor.setCanvasGraphics(cg);
      }
   }

   public void makeEqual(JDRPaint paint)
   {
      super.makeEqual(paint);

      JDRAbstractShading shading = (JDRAbstractShading)paint;

      startColor.makeEqual(shading.startColor);
      endColor.makeEqual(shading.startColor);

      if (!shading.hasMidColor())
      {
         midColor = null;
      }
      else if (midColor == null)
      {
         midColor = (JDRPaint)shading.midColor.clone();
      }
      else
      {
         midColor.makeEqual(shading.midColor);
      }
   }

   protected JDRPaint startColor, midColor, endColor;

   protected static int pgfshadeid=0;
}
