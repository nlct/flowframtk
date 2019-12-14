package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.Path2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPathElement extends SVGShape
{
   public SVGPathElement(SVGHandler handler,
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   protected void applyAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.applyAttributes(uri, attr);

      addAttribute("d", attr);
   }

   public String getName()
   {
      return "path";
   }

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
      try
      {
         SVGPathElement element = new SVGPathElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }
}
