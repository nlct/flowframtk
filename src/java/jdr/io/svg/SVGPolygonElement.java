package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPolygonElement extends SVGPolyLineElement
{
   public SVGPolygonElement(SVGHandler handler, 
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   public String getName()
   {
      return "polygon";
   }

   public JDRShape createShape(CanvasGraphics cg)
     throws InvalidFormatException
   {
      JDRPath path = (JDRPath)super.createShape(cg);

      path.close(JDRPath.CLOSE_LINE);

      return path;
   }


   public Object clone()
   {
      try
      {
         SVGPolygonElement element = new SVGPolygonElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }
}
