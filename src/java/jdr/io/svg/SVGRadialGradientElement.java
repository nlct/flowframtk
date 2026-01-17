package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGRadialGradientElement extends SVGGradientElement
{
   public SVGRadialGradientElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, "radialGradient", parent);

      width = 100;
      height = 100;
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("fx", attr);
      addAttribute("fy", attr);
      addAttribute("fr", attr);

      addAttribute("cx", attr);
      addAttribute("cy", attr);
      addAttribute("r", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      if (name.equals("cx"))
      {
         return SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("cy"))
      {
         return SVGLengthAttribute.valueOf(handler, name, value, false);
      }
      else if (name.equals("fx"))
      {
         return SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("fy"))
      {
         return SVGLengthAttribute.valueOf(handler, name, value, false);
      }
      else if (name.equals("fr"))
      {
         return SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("r"))
      {
         return SVGLengthAttribute.valueOf(handler, name, value, true);
      }

      return super.createElementAttribute(name, value);
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      SVGLengthAttribute cxAttr = getLengthAttribute("cx");
      SVGLengthAttribute cyAttr = getLengthAttribute("cy");
      SVGLengthAttribute fxAttr = getLengthAttribute("fx");
      SVGLengthAttribute fyAttr = getLengthAttribute("fy");

      SVGLengthAttribute frAttr = getLengthAttribute("fr");
      SVGLengthAttribute rAttr = getLengthAttribute("r");

      SVGGradientUnitsAttribute unitAttr = getGradientUnitsAttribute("gradientUnits");
      AffineTransform af = getTransform("gradientTransform");

      startRadius = 0;
      endRadius = 50;

      endCircleX = 50;
      endCircleY = 50;

      if (cxAttr != null)
      {
         endCircleX = cxAttr.doubleValue(this);
      }

      if (fxAttr == null)
      {
         startCircleX = endCircleX;
      }
      else
      {
         startCircleX = fxAttr.doubleValue(this);
      }

      if (cyAttr != null)
      {
         endCircleY = cyAttr.doubleValue(this);
      }

      if (fyAttr == null)
      {
         startCircleY = endCircleY;
      }
      else
      {
         startCircleY = fyAttr.doubleValue(this);
      }

      if (frAttr != null)
      {
         startRadius = frAttr.doubleValue(this);
      }

      if (rAttr != null)
      {
         endRadius = rAttr.doubleValue(this);
      }

      if (af != null)
      {
         double[] pts = new double[] {
           startCircleX, startCircleY,
           endCircleX, endCircleY };

         af.transform(pts, 0, pts, 0, 2);

         startCircleX = pts[0];
         startCircleY = pts[1];
         endCircleX = pts[2];
         endCircleY = pts[3];
      }

      // this is quite rough

      double d = 2 * startRadius;

      bounds = new Rectangle2D.Double(
        startCircleX - startRadius,
        startCircleY - startRadius,
        d, d);

      d = 2 * endRadius;
      double x = endCircleX - endRadius;
      double y = endCircleY - endRadius;

      bounds.add(x, y);
      bounds.add(x+d, y+d);
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
         int location;

         int locX = getLocationX(startCircleX, bounds.getX(), bounds.getWidth());
         int locY = getLocationY(startCircleY, bounds.getY(), bounds.getHeight());

         if (locX == X_LEFT)
         {
            if (locY == Y_TOP)
            {
               location = JDRRadial.NORTH_WEST;
            }
            else if (locY == Y_MID)
            {
               location = JDRRadial.WEST;
            }
            else
            {
               location = JDRRadial.SOUTH_WEST;
            }
         }
         else if (locX == X_MID)
         {
            if (locY == Y_TOP)
            {
               location = JDRRadial.NORTH;
            }
            else if (locY == Y_MID)
            {
               location = JDRRadial.CENTER;
            }
            else
            {
               location = JDRRadial.SOUTH;
            }
         }
         else
         {
            if (locY == Y_TOP)
            {
               location = JDRRadial.NORTH_EAST;
            }
            else if (locY == Y_MID)
            {
               location = JDRRadial.EAST;
            }
            else
            {
               location = JDRRadial.SOUTH_EAST;
            }
         }

         paint = new JDRRadial(location, startPaint, endPaint);
      }
   }

   protected int getLocationX(double x, double minX, double width)
   {
      double thirdW = width/3;

      if (x < minX + thirdW)
      {
         return X_LEFT;
      }
      else if (x > minX + 2 * thirdW)
      {
         return X_RIGHT;
      }
      else
      {
         return X_MID;
      }
   }

   protected int getLocationY(double y, double minY, double height)
   {
      double thirdH = height/3;

      if (y < minY + thirdH)
      {
         return Y_TOP;
      }
      else if (y > minY + 2 * thirdH)
      {
         return Y_BOT;
      }
      else
      {
         return Y_MID;
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
      SVGRadialGradientElement elem = new SVGRadialGradientElement(handler, parent);
      elem.makeEqual(this);
      return elem;
   }

   public void makeEqual(SVGRadialGradientElement other)
   {
      super.makeEqual(other);

      width = other.width;
      height = other.height;

      startCircleX = other.startCircleX;
      startCircleY = other.startCircleY;
      startRadius = other.startRadius;

      endCircleX = other.endCircleX;
      endCircleY = other.endCircleY;
      endRadius = other.endRadius;

      if (other.bounds == null)
      {
         bounds = null;
      }
      else if (bounds == null)
      {
         bounds = new Rectangle2D.Double(bounds.getX(), bounds.getY(),
           bounds.getWidth(), bounds.getHeight());
      }
      else
      {
         bounds.setRect(other.bounds);
      }
   }

   double width, height;
   double endCircleX, endCircleY, startCircleX, startCircleY,
    startRadius, endRadius;

   Rectangle2D bounds;

   public static final int X_LEFT=0;
   public static final int X_MID=1;
   public static final int X_RIGHT=2;

   public static final int Y_TOP=0;
   public static final int Y_MID=1;
   public static final int Y_BOT=2;
}
