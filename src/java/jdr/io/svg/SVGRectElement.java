package com.dickimawbooks.jdr.io.svg;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGRectElement extends SVGShape
{
   public SVGRectElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, "rect", parent);
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("x", attr);
      addAttribute("y", attr);
      addAttribute("width", attr);
      addAttribute("height", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("x"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("y"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, false);
      }
// TODO rx and ry (rounded corner radius): auto|<length>|<percent>
      else if (name.equals("width"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("height"))
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
      width = getDoubleAttribute("width", 0);
      height = getDoubleAttribute("height", 0);

      x = getDoubleAttribute("x", 0);
      y = getDoubleAttribute("y", 0);

      super.startElement();
   }

   @Override
   protected Shape constructShape() throws SVGException
   {
      if (width > 0 && height > 0)
      {
         return new Rectangle2D.Double(x, y, width, height);
      }

      return null;
   }

   @Override
   public Object clone()
   {
      SVGRectElement element = new SVGRectElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   public void makeEqual(SVGRectElement other)
   {
      super.makeEqual(other);

      x = other.x;
      y = other.y;

      width = other.width;
      height = other.height;
   }

   double x, y, width, height;
}
