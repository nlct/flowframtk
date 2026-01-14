package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGRectElement extends SVGShape
{
   public SVGRectElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
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
     throws InvalidFormatException
   {
      SVGAttribute attr;

      if (name.equals("x"))
      {
         attr = new SVGLengthAttribute(handler, name, value, true);
      }
      else if (name.equals("y"))
      {
         attr = new SVGLengthAttribute(handler, name, value, false);
      }
// TODO rx and ry (rounded corner radius): auto|<length>|<percent>
      else if (name.equals("width"))
      {
         attr = new SVGLengthAttribute(handler, name, value, true);
      }
      else if (name.equals("height"))
      {
         attr = new SVGLengthAttribute(handler, name, value, false);
      }
      else
      {
         attr = super.createElementAttribute(name, value);
      }

      return attr;
   }

   @Override
   public String getName()
   {
      return "rect";
   }

   @Override
   public JDRShape createShape(CanvasGraphics cg)
   {
      double width = getDoubleAttribute("width", 0);
      double height = getDoubleAttribute("height", 0);

      if (width <= 0 || height <= 0)
      {
         return null;
      }

      double p1x = getDoubleAttribute("x", 0);
      double p1y = getDoubleAttribute("y", 0);
      double p2x = p1x + width;
      double p2y = p1y + height;

      JDRPath rect = JDRPath.constructRectangle(cg, p1x, p1y, p2x, p2y);

      return rect;
   }


   @Override
   public Object clone()
   {
      SVGRectElement element = new SVGRectElement(handler, null);

      element.makeEqual(this);

      return element;
   }
}
