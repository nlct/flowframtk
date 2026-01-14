package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPolygonElement extends SVGPolyLineElement
{
   public SVGPolygonElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
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
      SVGPolygonElement element = new SVGPolygonElement(handler, null);

      element.makeEqual(this);

      return element;
   }
}
