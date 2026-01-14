package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.Path2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPathElement extends SVGShape
{
   public SVGPathElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "path";
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("d", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String style)
     throws InvalidFormatException
   {
      SVGAttribute attr;

      if (name.equals("d"))
      {
         attr = new SVGPathDataAttribute(handler, style);
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
      Path2D path = getPathDataAttribute();

      if (path == null)
      {
         throw new InvalidFormatException("Missing path data");
      }

      return JDRPath.getPath(cg, path.getPathIterator(null));
   }

   public Object clone()
   {
      SVGPathElement element = new SVGPathElement(handler, null);

      element.makeEqual(this);

      return element;
   }
}
