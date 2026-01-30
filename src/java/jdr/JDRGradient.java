// File          : JDRGradient.java
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
import java.util.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing linear gradient shading.
 * This shading consists of a start colour, an end colour and
 * a direction which may be one of: North, North-East, East,
 * South-East, South, South-West, West or North-West. The starting
 * and end colours may not be shadings.
 * @author Nicola L C Talbot
 */
public class JDRGradient extends JDRAbstractShading
{
   /**
    * Creates a new linear gradient shading with the given start
    * and end colours. The direction is set to {@link #NORTH}.
    * @param sColor the start colour
    * @param eColor the end colour
    */
   public JDRGradient(JDRPaint sColor, JDRPaint eColor)
   {
      this(NORTH, sColor, null, eColor);
   }

   /**
    * Creates a new linear gradient shading with the given
    * direction, start colour and end colour.
    * @param d the shading direction, which must be 
    * one of: {@link #NORTH}, {@link #NORTH_EAST},
    * {@link #EAST}, {@link #SOUTH_EAST}, {@link #SOUTH},
    * {@link #SOUTH_WEST}, {@link #WEST} or {@link #NORTH_WEST}.
    * @param sColor the start colour
    * @param eColor the end colour
    */
   public JDRGradient(int d, JDRPaint sColor, JDRPaint eColor)
   {
      this(d, sColor, null, eColor);
   }

   public JDRGradient(int d, JDRPaint sColor, JDRPaint mColor, JDRPaint eColor)
   {
      super(sColor, mColor, eColor);
      setDirection(d);
   }

   /**
    * Creates a new shading (black). This sets the start and end
    * colour to black and the direction to {@link #NORTH}.
    */
   public JDRGradient(CanvasGraphics cg)
   {
      this(NORTH, new JDRColor(cg), new JDRColor(cg));
   }

   protected JDRGradient()
   {
      this(null);
   }

   @Override
   public JDRPaint average(JDRPaint paint)
   {
      JDRGradient gradient;

      if (paint instanceof JDRShading)
      {
         if (paint instanceof JDRGradient)
         {
            gradient = (JDRGradient)paint.clone();
         }
         else
         {
            gradient = 
              (JDRGradient)((JDRShading)paint).convertShading("JDRGradient");
         }

         gradient.setDirection((getDirection()+gradient.getDirection())/2);

         gradient.setStartColor(getStartColor().average(gradient.getStartColor()));
         gradient.setEndColor(getEndColor().average(gradient.getEndColor()));

         if (hasMidColor() && gradient.hasMidColor())
         {
            gradient.setMidColor(getMidColor().average(gradient.getMidColor()));
         }

         return gradient;
      }

      gradient = (JDRGradient)clone();
      gradient.setStartColor(getStartColor().average(paint));
      gradient.setEndColor(getEndColor().average(paint));

      if (hasMidColor())
      {
         gradient.setMidColor(getMidColor().average(paint));
      }

      return gradient;
   }

   @Override
   public Paint getPaint(BBox box)
   {
      Point2D startPt, endPt;
      double midX = box.getMinX() + 0.5*box.getWidth();
      double midY = box.getMinY() + 0.5*box.getHeight();

      switch (direction)
      {
         case NORTH :
            startPt  = new Point2D.Double(midX, box.getMaxY());
            endPt    = new Point2D.Double(midX, box.getMinY());
            break;
         case NORTH_EAST :
            startPt = new Point2D.Double(box.getMinX(),box.getMaxY());
            endPt   = new Point2D.Double(box.getMaxX(),box.getMinY());
            break;
         case EAST :
            startPt = new Point2D.Double(box.getMinX(),midY); 
            endPt   = new Point2D.Double(box.getMaxX(),midY);
            break;
         case SOUTH_EAST :
            startPt = new Point2D.Double(box.getMinX(),box.getMinY());
            endPt   = new Point2D.Double(box.getMaxX(),box.getMaxY());
            break;
         case SOUTH :
            startPt = new Point2D.Double(midX, box.getMinY());
            endPt   = new Point2D.Double(midX, box.getMaxY());
            break;
         case SOUTH_WEST :
            startPt = new Point2D.Double(box.getMaxX(),box.getMinY());
            endPt   = new Point2D.Double(box.getMinX(),box.getMaxY());
            break;
         case WEST :
            startPt = new Point2D.Double(box.getMaxX(),midY);
            endPt   = new Point2D.Double(box.getMinX(),midY); 
            break;
         case NORTH_WEST :
            startPt = new Point2D.Double(box.getMaxX(),box.getMaxY());
            endPt   = new Point2D.Double(box.getMinX(),box.getMinY());
            break;
         default :
            startPt = new Point2D.Double(0,0);
            endPt = new Point2D.Double(0,0);
      }

      if (hasMidColor())
      {
         return new LinearGradientPaint(
                        startPt, endPt,
                        new float[] { 0.0f, 0.5f, 1.0f },
                        new Color[]
                         {
                                  startColor.getColor(),
                                  midColor.getColor(),
                                  endColor.getColor()
                         }
                    );
      }
      else
      {
         return new GradientPaint(startPt, startColor.getColor(),
                                  endPt, endColor.getColor());
      }
   }

   @Override
   public Object clone()
   {
      if (midColor == null)
      {
         return new JDRGradient(direction,
           (JDRPaint)startColor.clone(),
           (JDRPaint)endColor.clone());
      }
      else
      {
         return new JDRGradient(direction,
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
           "objectinfo.paint.gradientwithmid",
           "gradient: direction={0} start={1} mid={2} end={3}",
            direction, startColor.info(), midColor.info(), endColor.info());
      }
      else
      {
         return getCanvasGraphics().getMessageWithFallback(
           "objectinfo.paint.gradient",
           "gradient: direction={0} start={1} end={2}",
            direction, startColor.info(), endColor.info());
      }
   }

   private String pgfdeclareverticalshading(
      JDRPaint start, JDRPaint mid, JDRPaint end)
   {
      StringBuilder builder = new StringBuilder();

      String eol = System.getProperty("line.separator", "\n");
      boolean hasMid = (mid != null && !(mid instanceof JDRTransparent));

      String startPaintID = "jdrlinear-start-"+pgfshadeid;
      String midPaintID = "jdrlinear-mid-"+pgfshadeid;
      String endPaintID = "jdrlinear-end-"+pgfshadeid;

      builder.append("\\definecolor{");
      builder.append(startPaintID);
      builder.append("}{");
      builder.append(start.pgfmodel());
      builder.append("}{");
      builder.append(start.pgfspecs());
      builder.append("}");
      builder.append(eol);

      if (hasMid)
      {
         builder.append("\\definecolor{");
         builder.append(midPaintID);
         builder.append("}{");
         builder.append(mid.pgfmodel());
         builder.append("}{");
         builder.append(mid.pgfspecs());
         builder.append("}");
         builder.append(eol);
      }

      builder.append("\\definecolor{");
      builder.append(endPaintID);
      builder.append("}{");
      builder.append(end.pgfmodel());
      builder.append("}{");
      builder.append(end.pgfspecs());
      builder.append("}");
      builder.append(eol);

      builder.append("\\pgfdeclareverticalshading{jdrlinear");
      builder.append(pgfshadeid);
      builder.append("}{100bp}{");

      builder.append("color(0bp)=(");
      builder.append(startPaintID);
      builder.append("); ");

      builder.append("color(32.5bp)=(");
      builder.append(startPaintID);
      builder.append("); ");

      if (hasMid)
      {
         builder.append("color(50bp)=(");
         builder.append(midPaintID);
         builder.append("); ");
      }

      builder.append("color(67.5bp)=(");
      builder.append(endPaintID);
      builder.append("); ");

      builder.append("color(100bp)=(");
      builder.append(endPaintID);
      builder.append(")");

      builder.append("}");

      return builder.toString();
   }

   private String pgfdeclarehorizontalshading(
      JDRPaint start, JDRPaint mid, JDRPaint end)
   {
      StringBuilder builder = new StringBuilder();
      String eol = System.getProperty("line.separator", "\n");

      boolean hasMid = (mid != null && !(mid instanceof JDRTransparent));

      String startPaintID = "jdrlinear-start-"+pgfshadeid;
      String midPaintID = "jdrlinear-mid-"+pgfshadeid;
      String endPaintID = "jdrlinear-end-"+pgfshadeid;

      builder.append("\\definecolor{");
      builder.append(startPaintID);
      builder.append("}{");
      builder.append(start.pgfmodel());
      builder.append("}{");
      builder.append(start.pgfspecs());
      builder.append("}");
      builder.append(eol);

      if (hasMid)
      {
         builder.append("\\definecolor{");
         builder.append(midPaintID);
         builder.append("}{");
         builder.append(mid.pgfmodel());
         builder.append("}{");
         builder.append(mid.pgfspecs());
         builder.append("}");
         builder.append(eol);
      }

      builder.append("\\definecolor{");
      builder.append(endPaintID);
      builder.append("}{");
      builder.append(end.pgfmodel());
      builder.append("}{");
      builder.append(end.pgfspecs());
      builder.append("}");
      builder.append(eol);

      builder.append("\\pgfdeclarehorizontalshading{jdrlinear");
      builder.append(pgfshadeid);
      builder.append("}{");
      builder.append("100bp}{");

      builder.append("color(0bp)=(");
      builder.append(startPaintID);
      builder.append("); ");

      builder.append("color(32.5bp)=(");
      builder.append(startPaintID);
      builder.append("); ");

      if (hasMid)
      {
         builder.append("color(50bp)=(");
         builder.append(midPaintID);
         builder.append("); ");
      }

      builder.append("color(67.5bp)=(");
      builder.append(endPaintID);
      builder.append("); ");

      builder.append("color(100bp)=(");
      builder.append(endPaintID);
      builder.append(")");

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
      int angle=0;
      double height = box.getHeight();
      double width  = box.getWidth();

      switch (direction)
      {
         case NORTH:
           str = pgfdeclareverticalshading(startColor,midColor,endColor);
           angle=0;
         break;
         case NORTH_EAST :
           str = pgfdeclareverticalshading(startColor,midColor,endColor);
           angle=45;
         break;
         case EAST :
           str = pgfdeclarehorizontalshading(startColor,midColor,endColor);
           angle = 0;
         break;
         case SOUTH_EAST :
           str = pgfdeclarehorizontalshading(startColor,midColor,endColor);
           angle = 45;
         break;
         case SOUTH :
           str = pgfdeclareverticalshading(endColor,midColor,startColor);
           angle = 0;
         break;
         case SOUTH_WEST :
           str = pgfdeclareverticalshading(endColor,midColor,startColor);
           angle = 45;
         break;
         case WEST :
           str = pgfdeclarehorizontalshading(endColor,midColor,startColor);
           angle = 0;
         break;
         case NORTH_WEST :
           str = pgfdeclarehorizontalshading(endColor,midColor,startColor);
           angle = 45;
         break;
      }

      double opacity = getAlpha();

      if (opacity != 1.0)
      {
         if (startColor.getAlpha() != endColor.getAlpha())
         {
            str += eol+"% "
             +getCanvasGraphics().warning(
             "warning.pgf-gradient-mixed-opacity",
             "pgf gradient shading mixed opacity has been averaged")+eol;
         }

         str += "\\pgfsetfillopacity{"+PGF.format(opacity)+"}";
      }

      str += eol+ "\\pgfshadepath{jdrlinear"+pgfshadeid+"}{"+PGF.format(-angle)+"}";
      pgfshadeid++;

      return str;
   }

   @Override
   public String getID()
   {
      if (hasMidColor())
      {
         return "gradient-"+startColor.getID()
          +"-"+endColor.getID()+"-"+endColor.getID()+"-"+direction;
      }
      else
      {
         return "gradient-"+startColor.getID()+"-"+endColor.getID()+"-"+direction;
      }
   }

   @Override
   protected void svgDef(SVG svg, String id) throws IOException
   {
      svg.println("      <linearGradient id=\""+id+"\"");
      svg.println("         gradientUnits=\"objectBoundingBox\"");

      int x1=0, y1=0, x2=0, y2=100;

      switch (direction)
      {
         case NORTH :
            x1 = 50;
            y1 = 100;
            x2 = 50;
            y1 = 0;
         break;
         case NORTH_EAST :
            x1 = 0;
            y1 = 100;
            x2 = 100;
            y2 = 0;
         break;
         case EAST :
            x1 = 0;
            y1 = 50;
            x2 = 100;
            y2 = 50;
         break;
         case SOUTH_EAST :
            x1 = 0;
            y1 = 0;
            x2 = 100;
            y2 = 100;
         break;
         case SOUTH :
            x1 = 50;
            y1 = 0;
            x2 = 50;
            y2 = 100;
         break;
         case SOUTH_WEST :
            x1 = 100;
            y1 = 0;
            x2 = 0;
            y2 = 100;
         break;
         case WEST :
            x1 = 100;
            y1 = 50;
            x2 = 0;
            y2 = 50;
         break;
         case NORTH_WEST :
            x1 = 100;
            y1 = 100;
            x2 = 0;
            y2 = 0;
         break;
      }

      svg.println("         x1=\""+x1+"%\" "
        +"y1=\""+y1+"%\" " + "x2=\""+x2+"%\" "
        +"y2=\""+y2+"%\">");

      svg.println("         <stop offset=\"0%\" stop-color=\""+
         startColor.svg()+"\" stroke-opacity=\""
        +startColor.getAlpha()+"\"/>");

      if (hasMidColor())
      {
         svg.println("         <stop offset=\"50%\" stop-color=\""+
            midColor.svg()+"\" stroke-opacity=\""
           +midColor.getAlpha()+"\"/>");
      }

      svg.println("         <stop offset=\"100%\" stop-color=\""+
         endColor.svg()+"\" stroke-opacity=\""
        +endColor.getAlpha()+"\"/>");

      svg.println("      </linearGradient>");
   }

   /**
    * Iterates through all elements of the specified group 
    * and writes the SVG definitions for any gradient paint
    * used. {@link #svgFill()} and {@link #svgLine()} use
    * these gradient definitions.
    * @param svg the output device
    * @param group the JDR image
    * @throws IOException if I/O error occurs
    */
   public static void svgDefs(SVG svg, JDRGroup group)
      throws IOException
   {
      Hashtable<String,JDRGradient> gradients
         = new Hashtable<String,JDRGradient>();

      for (int i = 0; i < group.size(); i++)
      {
         JDRCompleteObject object = group.get(i);

         JDRPaint p;

         if (object instanceof JDRShape)
         {
            p = ((JDRShape)object).getLinePaint();

            if (p instanceof JDRGradient)
            {
               gradients.put(((JDRGradient)p).getID(), (JDRGradient)p);
            }

            p = ((JDRShape)object).getShapeFillPaint();

            if (p instanceof JDRGradient)
            {
               gradients.put(((JDRGradient)p).getID(), (JDRGradient)p);
            }
         }

         if (object instanceof JDRTextual)
         {
            p = ((JDRTextual)object).getTextPaint();

            if (p instanceof JDRGradient)
            {
               gradients.put(((JDRGradient)p).getID(), (JDRGradient)p);
            }

            p = ((JDRTextual)object).getOutlineFillPaint();

            if (p != null && p instanceof JDRGradient)
            {
               gradients.put(((JDRGradient)p).getID(), (JDRGradient)p);
            }
         }
      }

      for (Enumeration e = gradients.keys(); e.hasMoreElements(); )
      {
         String id = (String)e.nextElement();

         JDRGradient p = (JDRGradient)gradients.get(id);
         p.svgDef(svg, id);
      }
   }

   public int psLevel()
   {
      return 2;
   }

   public void saveEPS(PrintWriter out, BBox box)
      throws IOException
   {
// TODO add mid colour
      if (box == null)
      {
         startColor.saveEPS(out, box);
         return;
      }

      double x0=0, y0=0, x1=0, y1=0;
      double minX = box.getMinX();
      double maxX = box.getMaxX();
      double minY = box.getMinY();
      double maxY = box.getMaxY();

      switch (direction)
      {
         case NORTH:
            x0 = minX+0.5*box.getWidth();
            y1 = minY;
            x1 = x0;
            y0 = maxY;
         break;
         case NORTH_EAST :
            x0 = minX;
            y1 = minY;
            x1 = maxX;
            y0 = maxY;
         break;
         case EAST :
            x0 = minX;
            y1 = minY+0.5*box.getHeight();
            x1 = maxX;
            y0 = y1;
         break;
         case SOUTH_EAST :
            x0 = minX;
            y1 = maxY;
            x1 = maxX;
            y0 = minY;
         break;
         case SOUTH :
            x1 = minX+0.5*box.getWidth();
            y0 = minY;
            x0 = x0;
            y1 = maxY;
         break;
         case SOUTH_WEST :
            x0 = maxX;
            y1 = maxY;
            x1 = minX;
            y0 = minY;
         break;
         case WEST :
            x1 = minX;
            y0 = minY+0.5*box.getHeight();
            x0 = maxX;
            y1 = y0;
         break;
         case NORTH_WEST :
            x0 = maxX;
            y1 = minY;
            x1 = minX;
            y0 = maxY;
         break;
      }

      out.println("<<");

      out.println("   /ShadingType 2");

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
      out.println("   /Coords ["+x0+" "+y0+" "+x1+" "+y1+"]");
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
    * Converts this shading to a radial shading. This creates a
    * new radial shading with the same start, mid and end colour.
    * @return a radial shading with the same start, mid and end colour
    */
   public JDRRadial getJDRRadial()
   {
      return new JDRRadial(direction, startColor, midColor, endColor);
   }

   /**
    * Converts this shading to the given shading type.
    * If <code>label</code> is <code>"JDRGradient"</code>, returns
    * this shading. If <code>label</code> is 
    * <code>"JDRRadial"</code>, returns {@link #getJDRRadial()}.
    * Otherwise throws {@link InvalidFormatException}.
    */
   @Override
   public JDRShading convertShading(String label)
   {
      if (label.equals("JDRRadial"))
      {
         return getJDRRadial();
      }
      else if (label.equals("JDRGradient"))
      {
         return this;
      }

      throw new JdrIllegalArgumentException(
        JdrIllegalArgumentException.CONVERT_SHADING, label, 
        getCanvasGraphics());
   }

   public JDRPaintLoaderListener getListener()
   {
      return listener;
   }

   /**
    * Gets the direction of this shading.
    * @return the direction of this shading
    */
   public int getDirection()
   {
      return direction;
   }

   /**
    * Sets this shading's direction.
    * @param d the gradient, which must be
    * one of: {@link #NORTH}, {@link #NORTH_EAST},
    * {@link #EAST}, {@link #SOUTH_EAST}, {@link #SOUTH},
    * {@link #SOUTH_WEST}, {@link #WEST} or {@link #NORTH_WEST}.
    */
   public void setDirection(int d)
   {
      if (d < 0 || d > 7)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.SHADING_DIRECTION, d, 
            getCanvasGraphics());
      }

      direction = d;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj) return true;

      if (obj == null)
      {
         return false;
      }

      if (!(obj instanceof JDRGradient))
      {
         return false;
      }

      JDRGradient c = (JDRGradient)obj;

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

      return (getDirection() == c.getDirection()
           && startColor.equals(c.startColor)
           && endColor.equals(c.endColor));
   }

   @Override
   public void makeEqual(JDRPaint paint)
   {
      super.makeEqual(paint);

      JDRGradient grad = (JDRGradient)paint;

      direction = grad.direction;
   }

   private int direction;

   /**
    * Indicates that the shading goes from South to North.
    */
   public final static int NORTH=0;
   /**
    * Indicates that the shading goes from South-West to North-East.
    */
   public final static int NORTH_EAST=1;
   /**
    * Indicates that the shading goes from West to East.
    */
   public final static int EAST=2;
   /**
    * Indicates that the shading goes from North-West to South-East.
    */
   public final static int SOUTH_EAST=3;
   /**
    * Indicates that the shading goes from North to South.
    */
   public final static int SOUTH=4;
   /**
    * Indicates that the shading goes from North-East to South-West.
    */
   public final static int SOUTH_WEST=5;
   /**
    * Indicates that the shading goes from East to West.
    */
   public final static int WEST=6;
   /**
    * Indicates that the shading goes from South-East to North-West.
    */
   public final static int NORTH_WEST=7;

   private static JDRGradientListener listener = new JDRGradientListener();
}
