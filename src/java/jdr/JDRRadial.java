// File          : JDRRadial.java
// Creation Date : 15th February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/
// Painting code adapted from "Java 2D Graphics" by Jonathan Knudsen
// (O'Reilly)

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
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing radial gradient shading.
 * This shading consists of a start colour, an end colour and a
 * relative starting location.
 * The location may be one of: North, North-East, East,
 * South-East, South, South-West, West, North-East or Central.
 * The location indicates the starting point of the shading (using
 * the start colour) and radiates out from that point. The shading
 * starting location is relative to the bounding box of the object
 * to which it applies.
 *<p>
 * Note that the start and end colours may not also be shadings
 * @author Nicola L C Talbot
 */

public class JDRRadial extends JDRAbstractShading implements Paint
{
   /**
    * Creates a new radial shading with the given start and end 
    * colours. The starting location is set to {@link #CENTER}.
    * @param sColor the start colour
    * @param eColor the end colour
    */
   public JDRRadial(JDRPaint sColor, JDRPaint eColor)
   {
      this(CENTER, sColor, eColor);
   }

   /**
    * Creates a new radial shading with the given start and end 
    * colours and relative starting location. 
    * @param location the relative starting location, which must be
    * one of: {@link #NORTH}, {@link #NORTH_EAST},
    * {@link #EAST}, {@link #SOUTH_EAST}, {@link #SOUTH},
    * {@link #SOUTH_WEST}, {@link #WEST}, {@link #NORTH_WEST} or
    * {@link #CENTER}.
    * @param sColor the start colour
    * @param eColor the end colour
    */
   public JDRRadial(int location, JDRPaint sColor, JDRPaint eColor)
   {
      this(location, sColor, null, eColor);
   }

   public JDRRadial(int location, JDRPaint sColor, JDRPaint mColor, JDRPaint eColor)
   {
      super(sColor, mColor, eColor);

      setStartLocation(location);

      startPtx   = 0;
      startPty   = 0;
      endPtx = 1;
      endPty = 1;
   }

   /**
    * Creates a new radial shading (black). This sets the start
    * and end colours to black and the location to {@link #CENTER}.
    */
   public JDRRadial(CanvasGraphics cg)
   {
      this(CENTER, new JDRColor(cg), new JDRColor(cg));
   }

   protected JDRRadial()
   {
      this(null);
   }

   @Override
   public JDRPaint average(JDRPaint paint)
   {
      JDRRadial radial;

      if (paint instanceof JDRShading)
      {
         if (paint instanceof JDRRadial)
         {
            radial = (JDRRadial)paint.clone();
         }
         else
         {
            radial = 
              (JDRRadial)((JDRShading)paint).convertShading("JDRRadial");
         }

         radial.setStartLocation((getStartLocation()+radial.getStartLocation())/2);

         radial.setStartColor(getStartColor().average(radial.getStartColor()));
         radial.setEndColor(getEndColor().average(radial.getEndColor()));

         if (hasMidColor() && radial.hasMidColor())
         {
            radial.setMidColor(getMidColor().average(radial.getMidColor()));
         }

         return radial;
      }

      radial = (JDRRadial)clone();
      radial.setStartColor(getStartColor().average(paint));
      radial.setEndColor(getEndColor().average(paint));

      if (hasMidColor())
      {
         radial.setMidColor(getMidColor().average(paint));
      }

      return radial;
   }

   private void setStartPoint(double x, double y)
   {
      startPtx = x;
      startPty = y;
   }

   private void setEndPoint(double x, double y)
   {
      endPtx = x;
      endPty = y;
   }

   /**
    * Updates the bounding box associated with this paint.
    * @param box the new bounding box
    */
   public void update(BBox box)
   {
      // divide box into 9 equal partitions.

      double width = box.getWidth();
      double height = box.getHeight();

      double sixthWidth  = width/6;
      double sixthHeight = height/6;
      double halfWidth   = width/2;
      double halfHeight  = height/2;

      double minX = box.getMinX();
      double minY = box.getMinY();
      double maxX = box.getMaxX();
      double maxY = box.getMaxY();

      switch (direction)
      {
         case NORTH :
            setStartPoint(minX+halfWidth, minY+sixthHeight);
            setEndPoint(minX, maxY);
            break;
         case NORTH_EAST :
            setStartPoint(minX+5*sixthWidth, minY+sixthHeight);
            setEndPoint(minX, maxY);
            break;
         case EAST :
            setStartPoint(minX+5*sixthWidth, minY+halfHeight);
            setEndPoint(minX, minY);
            break;
         case SOUTH_EAST :
            setStartPoint(minX+5*sixthWidth, minY+5*sixthHeight);
            setEndPoint(minX, minY);
            break;
         case SOUTH :
            setStartPoint(minX+halfWidth, minY+5*sixthHeight);
            setEndPoint(maxX, minY);
            break;
         case SOUTH_WEST :
            setStartPoint(minX+sixthWidth, minY+5*sixthHeight);
            setEndPoint(maxX, minY);
            break;
         case WEST :
            setStartPoint(minX+sixthWidth, minY+halfHeight);
            setEndPoint(maxX, maxY);
            break;
         case NORTH_WEST :
            setStartPoint(minX+sixthWidth, minY+sixthHeight);
            setEndPoint(maxX, maxY);
            break;
         default :
            // centre
            setStartPoint(minX+halfWidth, minY+halfHeight);
            setEndPoint(minX, minY);
      }
   }

   /**
    * Gets the radius for this radial paint.
    * @return radius for this radial paint
    */
   public double getRadius()
   {
      double dx = startPtx-endPtx;
      double dy = startPty-endPty;
      return Math.sqrt(dx*dx+dy*dy);
   }

   @Override
   public Paint getPaint(BBox box)
   {
      update(box);

      if (hasMidColor())
      {
         return new RadialGradientPaint(
           (float)startPtx, (float)startPty,
           (float)getRadius(),
           new float[] { 0.0f, 0.5f, 1.0f },
           new Color[] 
            {
               startColor.getColor(),
               midColor.getColor(),
               endColor.getColor()
            }
         );
      }

      return this;
   }

   /**
    * Gets the paint context for this paint.
    * @param model the associated colour model
    * @param deviceBounds the device bounds
    * @param userBounds the user bounds
    * @param af the affine transformation to apply
    * @param hints the rendering hints to use
    * @return the paint context
    */
   public PaintContext createContext(ColorModel model,
      Rectangle deviceBounds, Rectangle2D userBounds,
      AffineTransform af, RenderingHints hints)
   {
      Point2D startPt = new Point2D.Double(startPtx, startPty);
      Point2D endPt = new Point2D.Double(endPtx, endPty);
      Point2D transStart = af.transform(startPt, null);
      Point2D transEnd = af.transform(endPt, null);

      return new RadialPaintContext(transStart, startColor.getColor(),
         transEnd, endColor.getColor());
   }

   /**
    * Gets the transparency for this shading.
    * @return {@link #OPAQUE} if <code>As &amp; Ae == 0xff</code>
    * otherwise returns {@link #TRANSLUCENT} where <code>As</code>
    * is the alpha value of the start colour and <code>Ae</code>
    * is the alpha value of the end colour
    */
   public int getTransparency()
   {
      if (hasMidColor())
      {
         return (((startColor.getColor().getAlpha()
                 & midColor.getColor().getAlpha()
                 & endColor.getColor().getAlpha()) == 0xff)
            ? OPAQUE : TRANSLUCENT);
      }
      else
      {
         return (((startColor.getColor().getAlpha()
                 & endColor.getColor().getAlpha()) == 0xff)
            ? OPAQUE : TRANSLUCENT);
      }
   }

   @Override
   public Object clone()
   {
      if (midColor == null)
      {
         return new JDRRadial(direction,
           (JDRPaint)startColor.clone(),
           (JDRPaint)endColor.clone());
      }
      else
      {
         return new JDRRadial(direction,
           (JDRPaint)startColor.clone(),
           (JDRPaint)midColor.clone(),
           (JDRPaint)endColor.clone());
      }
   }

   @Override
   public String toString()
   {
      return String.format("%s[D=%d,start=%s,mid=%s,end=%s]",
       getClass().getSimpleName(), direction, startColor, midColor, endColor);
   }

   @Override
   public String info()
   {
      if (hasMidColor())
      {
         return getCanvasGraphics().getMessageWithFallback(
           "objectinfo.paint.radialwithmid",
           "radial: direction={0} start={1} mid={2} end={3}",
            direction, startColor.info(), midColor.info(), endColor.info());
      }
      else
      {
         return getCanvasGraphics().getMessageWithFallback(
           "objectinfo.paint.radial",
           "radial: direction={0} start={1} end={2}",
            direction, startColor.info(), endColor.info());
      }
   }

   private String pgfdeclareradialshading(BBox box)
   {
      String eol = System.getProperty("line.separator", "\n");
      update(box);

      String startPaintID = "jdrradial-start-"+pgfshadeid;
      String midPaintID = "jdrradial-mid-"+pgfshadeid;
      String endPaintID = "jdrradial-end-"+pgfshadeid;

      StringBuilder builder = new StringBuilder();

      builder.append("\\definecolor{");
      builder.append(startPaintID);
      builder.append("}{");
      builder.append(startColor.pgfmodel());
      builder.append("}{");
      builder.append(startColor.pgfspecs());
      builder.append("}");
      builder.append(eol);

      if (hasMidColor())
      {
         builder.append("\\definecolor{");
         builder.append(midPaintID);
         builder.append("}{");
         builder.append(midColor.pgfmodel());
         builder.append("}{");
         builder.append(midColor.pgfspecs());
         builder.append("}");
         builder.append(eol);
      }

      builder.append("\\definecolor{");
      builder.append(endPaintID);
      builder.append("}{");
      builder.append(endColor.pgfmodel());
      builder.append("}{");
      builder.append(endColor.pgfspecs());
      builder.append("}");
      builder.append(eol);

      builder.append("\\pgfdeclareradialshading{jdrradial");
      builder.append(pgfshadeid);
      builder.append("}{\\pgfpoint{");

      double midX = box.getMidX();
      double midY = box.getMidY();

      builder.append(PGF.format(startPtx-midX));
      builder.append("bp}{");
      builder.append(PGF.format(midY-startPty));
      builder.append("bp}");
      builder.append("}{");

      builder.append("color(0bp)=(");
      builder.append(startPaintID);
      builder.append(");");

      if (hasMidColor())
      {
         builder.append("color(25bp)=(");
         builder.append(midPaintID);
         builder.append("); ");
      }
      else
      {
         builder.append("color(32.5bp)=(");
         builder.append(endPaintID);
         builder.append("); ");
      }

      builder.append("color(50bp)=(");
      builder.append(endPaintID);
      builder.append(")");
      builder.append("}");
      builder.append(eol);

      double width = box.getWidth();
      double height = box.getHeight();

      builder.append("\\pgfsetadditionalshadetransform{");

      if (width < height && width != 0)
      {
         builder.append("\\pgftransformxscale{");
         builder.append(PGF.format(height/width));
         builder.append("}");
      }
      else if (height != 0)
      {
         builder.append("\\pgftransformyscale{");
         builder.append(PGF.format(width/height));
         builder.append("}");
      }

      builder.append("}");

      return builder.toString();
   }

   @Override
   public String pgffillcolor(BBox box)
   {
      if (box == null)
      {
         return startColor.pgffillcolor(box);
      }

      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = pgfdeclareradialshading(box);

      double opacity = getAlpha();

      if (opacity != 1.0)
      {
         if (startColor.getAlpha() != endColor.getAlpha())
         {
            str += eol+"% "
             +getCanvasGraphics().warning(
             "warning.pgf-shading-mixed-opacity",
             "pgf shading mixed opacity has been averaged")+eol;
         }

         str += "\\pgfsetfillopacity{"+PGF.format(opacity)+"}";
      }

      str += eol+ "\\pgfshadepath{jdrradial"+pgfshadeid+"}{0}";
      pgfshadeid++;

      return str;
   }

   @Override
   public String getID()
   {
      if (hasMidColor())
      {
         return "radial-"+startColor.getID()
           + "-" + midColor.getID() + "-" + endColor.getID()+"-"+direction;
      }
      else
      {
         return "radial-"+startColor.getID()+"-"+endColor.getID()+"-"+direction;
      }
   }

   @Override
   protected void svgDef(SVG svg, String id) throws IOException
   {
      svg.println("      <radialGradient id=\""+getID()+"\"");
      svg.println("         gradientUnits=\"objectBoundingBox\"");

      Point2D spt, ept;

      double fiveSixths = 100.0*5.0/6.0;
      double oneSixth   = 100.0/6.0;

      switch (direction)
      {
         case NORTH :
            spt = new Point2D.Double(50, fiveSixths);
            ept = new Point2D.Double(0, 0);
         break;
         case NORTH_EAST :
            spt = new Point2D.Double(fiveSixths, fiveSixths);
            ept = new Point2D.Double(0, 0);
         break;
         case EAST :
            spt = new Point2D.Double(fiveSixths, 50);
            ept = new Point2D.Double(0, 0);
         break;
         case SOUTH_EAST :
            spt = new Point2D.Double(fiveSixths, oneSixth);
            ept = new Point2D.Double(0, 100);
         break;
         case SOUTH :
            spt = new Point2D.Double(50, oneSixth);
            ept = new Point2D.Double(0, 100);
         break;
         case SOUTH_WEST :
            spt = new Point2D.Double(oneSixth, oneSixth);
            ept = new Point2D.Double(100, 100);
         break;
         case WEST :
            spt = new Point2D.Double(oneSixth, 50);
            ept = new Point2D.Double(100, 100);
         break;
         case NORTH_WEST :
            spt = new Point2D.Double(oneSixth, fiveSixths);
            ept = new Point2D.Double(100, 0);
         break;
         default :
            // centre
            spt = new Point2D.Double(50, 50);
            ept = new Point2D.Double(0, 0);
      }

      double r = spt.distance(ept);

      svg.println("         cx=\""+spt.getX()+"%\" "
        +"cy=\""+spt.getY()+"%\" " + "r=\""+r+"%\" "
        +">");

      svg.println("         <stop offset=\"0%\" stop-color=\""+
         startColor.svg()+"\" stroke-opacity=\""
        +startColor.getAlpha()+ "\"/>");

      if (hasMidColor())
      {
         svg.println("         <stop offset=\"50%\" stop-color=\""+
            midColor.svg()+"\" stroke-opacity=\""
           +midColor.getAlpha()+ "\"/>");
      }

      svg.println("         <stop offset=\"100%\" stop-color=\""+
         endColor.svg()+"\" stroke-opacity=\""
        +endColor.getAlpha()+ "\"/>");

      svg.println("      </radialGradient>");
   }

   @Deprecated
   public static void svgDefs(SVG svg, JDRGroup paths)
      throws IOException
   {
      Hashtable<String,JDRRadial> gradients
         = new Hashtable<String,JDRRadial>();

      for (int i = 0; i < paths.size(); i++)
      {
         JDRCompleteObject object = paths.get(i);

         JDRPaint p;

         if (object instanceof JDRShape)
         {
            p = ((JDRShape)object).getLinePaint();

            if (p instanceof JDRRadial)
            {
               gradients.put(((JDRRadial)p).getID(), (JDRRadial)p);
            }

            p = ((JDRShape)object).getFillPaint();

            if (p instanceof JDRRadial)
            {
               gradients.put(((JDRRadial)p).getID(), (JDRRadial)p);
            }
         }

         if (object instanceof JDRTextual)
         {
            p = ((JDRTextual)object).getTextPaint();

            if (p instanceof JDRRadial)
            {
               gradients.put(((JDRRadial)p).getID(), (JDRRadial)p);
            }
         }
      }

      for (Enumeration e = gradients.keys(); e.hasMoreElements(); )
      {
         String id = (String)e.nextElement();

         JDRRadial p = (JDRRadial)gradients.get(id);
         p.svgDef(svg, id);
      }
   }

   /**
    * Gets the relative starting location of this shading.
    * @return the relative starting location of this shading
    */
   public int getStartLocation()
   {
      return direction;
   }

   /**
    * Sets the relative starting location for this shading.
    * @param location the relative starting location, which must be
    * one of: {@link #NORTH}, {@link #NORTH_EAST},
    * {@link #EAST}, {@link #SOUTH_EAST}, {@link #SOUTH},
    * {@link #SOUTH_WEST}, {@link #WEST}, {@link #NORTH_WEST} or
    * {@link #CENTER}.
    */
   public void setStartLocation(int location)
   {
      if (location < 0 || location > 8)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.SHADING_LOCATION, 
            location, getCanvasGraphics());
      }

      direction = location;
   }

   /**
    * Gets the closest matching linear gradient paint.
    * @return closest matching linear gradient paint
    */
   public JDRGradient getJDRGradient()
   {
      return new JDRGradient(
         direction == CENTER ? NORTH : direction,
         startColor, midColor, endColor);
   }

   @Override
   public JDRShading convertShading(String label)
   {
      if (label.equals("JDRRadial"))
      {
         return this;
      }
      else if (label.equals("JDRGradient"))
      {
         return getJDRGradient();
      }

      throw new JdrIllegalArgumentException(
        JdrIllegalArgumentException.CONVERT_SHADING, label,
        getCanvasGraphics());
   }

   public int psLevel()
   {
      return 2;
   }

   public void saveEPS(PrintWriter out, BBox box)
      throws IOException
   {
// TODO add mid color
      if (box == null)
      {
         startColor.saveEPS(out, box);
         return;
      }

      double minX = box.getMinX();
      double maxX = box.getMaxX();
      double minY = box.getMinY();
      double maxY = box.getMaxY();

      update(box);
      double radius = getRadius();

      out.println("<<");

      out.println("   /ShadingType 3");

      String c0, c1;

      if (startColor instanceof JDRColor)
      {
         JDRColor c = (JDRColor)startColor;
         out.println("   /ColorSpace /DeviceRGB");
         c0 = "/C0 ["+c.getRed()+" "+c.getGreen()+" "+c.getBlue()+"]";

         c = (endColor instanceof JDRColor) ? 
             (JDRColor)endColor :
             ((JDRColorCMYK)endColor).getJDRColor();

         c1 = "/C1 ["+c.getRed()+" "+c.getGreen()+" "+c.getBlue()+"]";
      }
      else
      {
         JDRColorCMYK c = (JDRColorCMYK)startColor;
         out.println("   /ColorSpace /DeviceCMYK");
         c0 = "/C0 ["+c.getCyan()+" "+c.getMagenta()+" "+c.getYellow()+" "+c.getKey()+"]";

         c = (endColor instanceof JDRColorCMYK) ? 
             (JDRColorCMYK)endColor :
             ((JDRColor)endColor).getJDRColorCMYK();

         c1 = "/C1 ["+c.getCyan()+" "+c.getMagenta()+" "+c.getYellow()+" "+c.getKey()+"]";
      }

      out.println("   /BBox ["+minX+" "+minY+" "+maxX+" "+maxY+"]");
      out.println("   /Coords ["+startPtx+" "+startPty
         +" 0 "+startPtx+" "+startPty+" "+radius+"]");
      out.println("      /Function <<");
      out.println("         /FunctionType 2");
      out.println("         /Domain [0 1]");
      out.println("         "+c0);
      out.println("         "+c1);
      out.println("         /N 1");
      out.println("   >>");
      out.print(">> ");
   }

   /**
    * Determines if this paint is the same as another object.
    * @param obj the other object
    * @return true if this paint is the same as the other object
    */
   public boolean equals(Object obj)
   {
      if (this == obj) return true;

      if (obj == null)
      {
         return false;
      }

      if (!(obj instanceof JDRRadial))
      {
         return false;
      }

      JDRRadial c = (JDRRadial)obj;

      if ( midColor != c.midColor
          &&
           (
             ( midColor == null && c.midColor != null ) 
          || ( c.midColor == null && midColor != null ) )
          || !midColor.equals(c.midColor)
           )
      {
         return false;
      }

      return (direction == c.direction
           && startColor.equals(c.startColor)
           && endColor.equals(c.endColor));
   }

   public JDRPaintLoaderListener getListener()
   {
      return listener;
   }

   @Override
   public void makeEqual(JDRPaint paint)
   {
      super.makeEqual(paint);

      JDRRadial rad = (JDRRadial)paint;

      direction = rad.direction;
   }

   private int direction;

   /**
    * Shading starts in the North.
    */
   public final static int NORTH=0;
   /**
    * Shading starts in the North-East.
    */
   public final static int NORTH_EAST=1;
   /**
    * Shading starts in the East.
    */
   public final static int EAST=2;
   /**
    * Shading starts in the South-East.
    */
   public final static int SOUTH_EAST=3;
   /**
    * Shading starts in the South.
    */
   public final static int SOUTH=4;
   /**
    * Shading starts in the South-West.
    */
   public final static int SOUTH_WEST=5;
   /**
    * Shading starts in the West.
    */
   public final static int WEST=6;
   /**
    * Shading starts in the North-West.
    */
   public final static int NORTH_WEST=7;
   /**
    * Shading starts in the centre.
    */
   public final static int CENTER=8;

   private double startPtx, startPty, endPtx, endPty;

   private static JDRPaintLoaderListener listener = new JDRRadialListener();
}

class RadialPaintContext implements PaintContext
{
   private Point2D startPt;
   private double radius;
   private Color startCol, endCol;

   public RadialPaintContext(Point2D start, Color c1, Point2D end,
      Color c2)
   {
      startPt = start;
      radius = start.distance(end);
      startCol = c1;
      endCol = c2;
   }

   public void dispose()
   {
   }

   public ColorModel getColorModel()
   {
      return ColorModel.getRGBdefault();
   }

   public Raster getRaster(int x, int y, int w, int h)
   {
      WritableRaster raster
         = getColorModel().createCompatibleWritableRaster(w, h);

      int[] data = new int[w*h*4];

      for (int j = 0; j < h; j++)
      {
         for (int i = 0; i < w; i++)
         {
            double d = startPt.distance(x+i, y+j);
            double ratio = d/radius;

            int base = (j*w+i)*4;
            int sRed   = startCol.getRed();
            int sGreen = startCol.getGreen();
            int sBlue  = startCol.getBlue();
            int sAlpha = startCol.getAlpha();
            int eRed   = endCol.getRed();
            int eGreen = endCol.getGreen();
            int eBlue  = endCol.getBlue();
            int eAlpha = endCol.getAlpha();

            data[base+0] = (int)(sRed+ratio*(eRed-sRed));
            data[base+1] = (int)(sGreen+ratio*(eGreen-sGreen));
            data[base+2] = (int)(sBlue+ratio*(eBlue-sBlue));
            data[base+3] = (int)(sAlpha+ratio*(eAlpha-sAlpha));
         }
      }

      raster.setPixels(0, 0, w, h, data);

      return raster;
   }

}
