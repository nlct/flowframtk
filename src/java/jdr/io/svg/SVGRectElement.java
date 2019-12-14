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

   protected void applyAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.applyAttributes(uri, attr);

      addAttribute("width", attr);
      addAttribute("height", attr);
      addAttribute("x", attr);
      addAttribute("y", attr);
   }

   protected double getRectX()
   {
      return getDoubleAttribute("x", 0);
   }

   protected double getRectY()
   {
      return getDoubleAttribute("y", 0);
   }

   protected double getRectWidth()
   {
      return getDoubleAttribute("width", 0);
   }

   protected double getRectHeight()
   {
      return getDoubleAttribute("height", 0);
   }

   public String getName()
   {
      return "rect";
   }

   public JDRShape createShape(CanvasGraphics cg)
   {
      double p1x = getRectX();
      double p1y = getRectY();
      double p2x = p1x + getRectWidth();
      double p2y = p1y + getRectHeight();

      JDRPath rect = JDRPath.constructRectangle(cg, p1x, p1y, p2x, p2y);

      return rect;
   }


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
