package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import java.awt.geom.Point2D;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGEllipseElement extends SVGShape
{
   public SVGEllipseElement(SVGHandler handler,
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
      addAttribute("rx", attr);
      addAttribute("ry", attr);
   }

   public String getName()
   {
      return "ellipse";
   }

   public JDRShape createShape(CanvasGraphics cg)
   {
      Point2D p = new Point2D.Double(
                    getDoubleAttribute("cx", 0),
                    getDoubleAttribute("cy", 0));

      JDRPath shape = JDRPath.constructEllipse(cg, p,
         getDoubleAttribute("rx", 0),
         getDoubleAttribute("ry", 0));

      return shape;
   }


   public Object clone()
   {
      try
      {
         SVGEllipseElement element = new SVGEllipseElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }
}
