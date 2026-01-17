package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGCircleElement extends SVGShape
{
   public SVGCircleElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, "circle", parent);
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("cx", attr);
      addAttribute("cy", attr);
      addAttribute("r", attr);
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
      else if (name.equals("r"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value);
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
      x = getDoubleAttribute("cx", 0);
      y = getDoubleAttribute("cy", 0);
      r = getDoubleAttribute("r", 0);

      super.startElement();
   }

   @Override
   protected Shape constructShape() throws SVGException
   {
      double d = 2 * r;

      return new Ellipse2D.Double(x - r, y - r, d, d);
   }

   @Override
   public Object clone()
   {
      SVGCircleElement element = new SVGCircleElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   public void makeEqual(SVGCircleElement other)
   {
      super.makeEqual(other);

      r = other.r;
      x = other.x;
      y = other.y;
   }

   double x, y, r;
}
