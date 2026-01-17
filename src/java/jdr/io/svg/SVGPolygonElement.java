package com.dickimawbooks.jdr.io.svg;

import java.awt.Shape;
import java.awt.geom.Path2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPolygonElement extends SVGPolyLineElement
{
   public SVGPolygonElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, "polygon", parent);
   }

   @Override
   protected Shape constructShape() throws SVGException
   {
      Path2D path = (Path2D)super.constructShape();

      path.closePath();

      return path;
   }

   public Object clone()
   {
      SVGPolygonElement element = new SVGPolygonElement(handler, null);

      element.makeEqual(this);

      return element;
   }
}
