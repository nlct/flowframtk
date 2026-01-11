package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPolyLineElement extends SVGShape
{
   public SVGPolyLineElement(SVGHandler handler,
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   protected void applyAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.applyAttributes(uri, attr);

      addAttribute("points", attr);
   }

   public String getName()
   {
      return "polyline";
   }

   public JDRShape createShape(CanvasGraphics cg)
     throws InvalidFormatException
   {
      SVGLength[] points = getLengthArrayAttribute("points");

      if (points == null)
      {
         throw new InvalidFormatException("No points given for polyline");
      }

      if (points.length%2 == 1)
      {
         throw new InvalidFormatException(
           "Even number of coordinates required for "+getName()+" point list");
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


   public Object clone()
   {
      try
      {
         SVGPolyLineElement element = new SVGPolyLineElement(handler,null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }
}
