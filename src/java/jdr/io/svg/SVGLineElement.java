package com.dickimawbooks.jdr.io.svg;

import java.awt.Shape;
import java.awt.geom.Line2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.MessageInfo;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGLineElement extends SVGShape
{
   public SVGLineElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, "line", parent);
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
   protected SVGAttribute createElementAttribute(String name, String style)
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("x1"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, style, true);
      }
      else if (name.equals("y1"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, style, false);
      }
      else if (name.equals("x2"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, style, true);
      }
      else if (name.equals("y2"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, style, false);
      }
      else
      {
         attr = super.createElementAttribute(name, style);
      }

      return attr;
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      p1x = getDoubleAttribute("x1", 0);
      p1y = getDoubleAttribute("y1", 0);
      p2x = getDoubleAttribute("x2", 0);
      p2y = getDoubleAttribute("y2", 0);

      super.startElement();
   }

   protected Shape constructShape() throws SVGException
   {
      return new Line2D.Double(p1x, p1y, p2x, p2y);
   }


   @Override
   public Object clone()
   {
      SVGLineElement element = new SVGLineElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   public void makeEqual(SVGLineElement other)
   {
      super.makeEqual(other);

      p1x = other.p1x;
      p1y = other.p1y;
      p2x = other.p2x;
      p2y = other.p2y;
   }

   double p1x, p1y, p2x, p2y;
}
