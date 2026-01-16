package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPolyLineElement extends SVGShape
{
   public SVGPolyLineElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "polyline";
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("points", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String style)
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("points"))
      {
         attr = SVGLengthArrayAttribute.valueOf(handler, name, style);
      }
      else
      {
         attr = super.createElementAttribute(name, style);
      }

      return attr;
   }

   @Override
   public JDRShape createShape(CanvasGraphics cg)
     throws InvalidFormatException
   {
      SVGLengthAttribute[] points = getLengthArrayAttribute("points");

      if (points == null)
      {
         throw new ElementMissingAttributeException(this, "points");
      }

      if (points.length%2 == 1)
      {
         throw new CoordPairsRequiredException(this, "points");
      }

      JDRPath path = new JDRPath(cg);

      double p1x = points[0].getStorageValue(this, true);
      double p1y = points[1].getStorageValue(this, false);

      for (int i = 2; i < points.length; i += 2)
      {
         double p2x = points[i].getStorageValue(this, true);
         double p2y = points[i+1].getStorageValue(this, false);

         path.add(new JDRLine(cg, p1x, p1y, p2x, p2y));

         p1x = p2x;
         p1y = p2y;
      }

      return path;
   }

   @Override
   public Object clone()
   {
      SVGPolyLineElement element = new SVGPolyLineElement(handler, null);

      element.makeEqual(this);

      return element;
   }
}
