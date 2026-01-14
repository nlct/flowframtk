package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import java.awt.geom.Point2D;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGCircleElement extends SVGShape
{
   public SVGCircleElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "circle";
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
     throws InvalidFormatException
   {
      SVGAttribute attr;

      if (name.equals("cx"))
      {
         attr = new SVGLengthAttribute(handler, name, value, true);
      }
      else if (name.equals("cy"))
      {
         attr = new SVGLengthAttribute(handler, name, value, false);
      }
      else if (name.equals("r"))
      {
         attr = new SVGLengthAttribute(handler, name, value);
      }
      else
      {
         attr = super.createElementAttribute(name, value);
      }

      return attr;
   }

   @Override
   public JDRShape createShape(CanvasGraphics cg)
   {
      Point2D p = new Point2D.Double(
         getDoubleAttribute("cx", 0),
         getDoubleAttribute("cy", 0));

      double r = getDoubleAttribute("r", 0);

      JDRPath shape = JDRPath.constructEllipse(cg, p, r, r);

      return shape;
   }

   @Override
   public Object clone()
   {
      SVGCircleElement element = new SVGCircleElement(handler, null);

      element.makeEqual(this);

      return element;
   }
}
