package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.AffineTransform;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGLinearGradientElement extends SVGGradientElement
{
   public SVGLinearGradientElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, "linearGradient", parent);
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("x1", attr);
      addAttribute("y1", attr);
      addAttribute("x2", attr);
      addAttribute("y2", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      if (name.equals("x1"))
      {
         return SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("y1"))
      {
         return SVGLengthAttribute.valueOf(handler, name, value, false);
      }
      else if (name.equals("x2"))
      {
         return SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("y2"))
      {
         return SVGLengthAttribute.valueOf(handler, name, value, false);
      }

      return super.createElementAttribute(name, value);
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      SVGLengthAttribute x1Attr = getLengthAttribute("x1");
      SVGLengthAttribute x2Attr = getLengthAttribute("x2");
      SVGLengthAttribute y1Attr = getLengthAttribute("y1");
      SVGLengthAttribute y2Attr = getLengthAttribute("y2");
      SVGGradientUnitsAttribute unitAttr = getGradientUnitsAttribute("gradientUnits");
      AffineTransform af = getTransform("gradientTransform");

      x1 = 0;
      y1 = 0;
      x2 = 100;
      y2 = 100;

      if (x1Attr != null)
      {
         x1 = x1Attr.doubleValue(this);
      }

      if (y1Attr != null)
      {
         y1 = y1Attr.doubleValue(this);
      }

      if (x2Attr != null)
      {
         x2 = x2Attr.doubleValue(this);
      }

      if (y2Attr != null)
      {
         y2 = y2Attr.doubleValue(this);
      }

      if (af != null)
      {
         double[] pts = new double[] { x1, y1, x2, y2 };

         af.transform(pts, 0, pts, 0, 2);

         x1 = pts[0];
         y1 = pts[1];
         x2 = pts[2];
         y2 = pts[3];
      }

      width = x2 - x1;
      height = y2 - y1;
   }

   @Override
   public void endElement() throws InvalidFormatException
   {
      if (minStopElement == null)
      {
         paint = new JDRColor(getCanvasGraphics());
      }
      else if (maxStopElement == null)
      {
         paint = minStopElement.getPaint();
      }
      else
      {
         JDRPaint startPaint = minStopElement.getPaint();
         JDRPaint endPaint = maxStopElement.getPaint();
         int direction;

         double theta = Math.atan2(y2-y1, x2-x1);

         if (theta < 0.0)
         {
            theta += TWO_PI;
         }

         if (theta < ONE_EIGHTH_PI || theta > FIFTEEN_EIGHTH_PI)
         {
            direction = JDRGradient.EAST;
         }
         else if (theta < THREE_EIGHTH_PI)
         {
            direction = JDRGradient.NORTH_EAST;
         }
         else if (theta < FIVE_EIGHTH_PI)
         {
            direction = JDRGradient.NORTH;
         }
         else if (theta < SEVEN_EIGHTH_PI)
         {
            direction = JDRGradient.NORTH_WEST;
         }
         else if (theta < NINE_EIGHTH_PI)
         {
            direction = JDRGradient.WEST;
         }
         else if (theta < ELEVEN_EIGHTH_PI)
         {
            direction = JDRGradient.SOUTH_WEST;
         }
         else if (theta < THIRTEEN_EIGHTH_PI)
         {
            direction = JDRGradient.SOUTH;
         }
         else
         {
            direction = JDRGradient.SOUTH_EAST;
         }

         paint = new JDRGradient(direction, startPaint, endPaint);
      }
   }

   @Override
   public double getViewportWidth()
   {
      return width;
   }

   @Override
   public double getViewportHeight()
   {
      return height;
   }

   @Override
   public Object clone()
   {
      SVGLinearGradientElement elem = new SVGLinearGradientElement(handler, parent);
      elem.makeEqual(this);
      return elem;
   }

   public void makeEqual(SVGLinearGradientElement other)
   {
      super.makeEqual(other);

      width = other.width;
      height = other.height;
      x1 = other.x1;
      x2 = other.x2;
      y1 = other.y1;
      y2 = other.y2;
   }

   double width, height, x1, y1, x2, y2;

   public static final double TWO_PI = 2 * Math.PI;
   public static final double ONE_EIGHTH_PI = Math.PI / 8;
   public static final double THREE_EIGHTH_PI = 3 * ONE_EIGHTH_PI;
   public static final double FIVE_EIGHTH_PI = 5 * ONE_EIGHTH_PI;
   public static final double SEVEN_EIGHTH_PI = 7 * ONE_EIGHTH_PI;
   public static final double NINE_EIGHTH_PI = Math.PI + ONE_EIGHTH_PI;
   public static final double ELEVEN_EIGHTH_PI = Math.PI + THREE_EIGHTH_PI;
   public static final double THIRTEEN_EIGHTH_PI = Math.PI + FIVE_EIGHTH_PI;
   public static final double FIFTEEN_EIGHTH_PI = Math.PI + SEVEN_EIGHTH_PI;
}
