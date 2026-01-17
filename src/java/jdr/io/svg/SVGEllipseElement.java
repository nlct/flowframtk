package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGEllipseElement extends SVGShape
{
   public SVGEllipseElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, "ellipse", parent);
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("cx", attr);
      addAttribute("cy", attr);
      addAttribute("rx", attr);
      addAttribute("ry", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("cx"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("cy"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, false);
      }
      else if (name.equals("rx"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("ry"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, false);
      }
      else
      {
         attr = super.createElementAttribute(name, value);
      }

      return attr;
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      px = getDoubleAttribute("cx", 0);
      py = getDoubleAttribute("cy", 0);

      rx = 0;
      ry = 0;

      SVGLengthAttribute rxAttr = getLengthAttribute("rx");
      SVGLengthAttribute ryAttr = getLengthAttribute("ry");

      if (rxAttr == null || rxAttr.isAuto())
      {
         if (ryAttr != null && !ryAttr.isAuto())
         {
            ry = ryAttr.doubleValue(this);
            rx = ry;
         }
      }
      else if (ryAttr == null || ryAttr.isAuto())
      {
         rx = rxAttr.doubleValue(this);
         ry = rx;
      }
      else
      {
         rx = rxAttr.doubleValue(this);
         ry = ryAttr.doubleValue(this);
      }

      super.startElement();
   }

   @Override
   protected Shape constructShape() throws SVGException
   {
      if (rx > 0 && ry > 0)
      {
         return new Ellipse2D.Double(px - rx, py - ry, 2*rx, 2*ry);
      }

      return null;
   }

   @Override
   public Object clone()
   {
      SVGEllipseElement element = new SVGEllipseElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   public void makeEqual(SVGEllipseElement other)
   {
      super.makeEqual(other);

      rx = other.rx;
      ry = other.ry;
      px = other.px;
      py = other.py;
   }

   double rx, ry, px, py;
}
