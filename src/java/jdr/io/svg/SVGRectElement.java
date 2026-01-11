package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGRectElement extends SVGShape
{
   public SVGRectElement(SVGHandler handler,
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   @Override
   protected void addAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.addAttributes(uri, attr);

      addAttribute("x", attr);
      addAttribute("y", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String style)
     throws InvalidFormatException
   {
      SVGAttribute attr;

      if (name.equals("x"))
      {
         attr = new SVGLengthAttribute(handler, name, style, true);
      }
      else if (name.equals("y"))
      {
         attr = new SVGLengthAttribute(handler, name, style, false);
      }
// TODO rx and ry (rounded corner radius): auto|<length>|<percent>
      else
      {
         attr = super.createElementAttribute(name, style);
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
      try
      {
         SVGRectElement element = new SVGRectElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }
}
