package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import java.awt.geom.Point2D;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGCircleElement extends SVGShape
{
   public SVGCircleElement(SVGHandler handler, 
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   protected void applyAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.applyAttributes(uri, attr);

      addAttribute("cx", attr);
      addAttribute("cy", attr);
      addAttribute("r", attr);

      applyShapeAttributes(uri, attr);
   }

   public String getName()
   {
      return "circle";
   }

   public JDRShape createShape(CanvasGraphics cg)
   {
      Point2D p = new Point2D.Double(
         getDoubleAttribute("cx", 0),
         getDoubleAttribute("cy", 0));

      double r = getDoubleAttribute("r", 0);

      JDRPath shape = JDRPath.constructEllipse(cg, p, r, r);

      return shape;
   }

   public Object clone()
   {
      try
      {
         SVGCircleElement element = new SVGCircleElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }
}
