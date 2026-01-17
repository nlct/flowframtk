package com.dickimawbooks.jdr.io.svg;

import java.awt.Shape;
import java.awt.geom.Path2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPathElement extends SVGShape
{
   public SVGPathElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, "path", parent);
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("d", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String style)
     throws SVGException
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
   public void startElement() throws InvalidFormatException
   {
      pathDataAttr = getPathDataAttribute("d");

      super.startElement();
   }

   @Override
   protected Shape constructShape() throws SVGException
   {
      return pathDataAttr == null ? null : pathDataAttr.getPath(this);
   }

   @Override
   public Object clone()
   {
      SVGPathElement element = new SVGPathElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   public void makeEqual(SVGPathElement other)
   {
      super.makeEqual(other);

      if (other.pathDataAttr == null)
      {
         pathDataAttr = null;
      }
      else if (pathDataAttr == null)
      {
         pathDataAttr = (SVGPathDataAttribute)other.pathDataAttr.clone();
      }
      else
      {
         pathDataAttr.makeEqual(other.pathDataAttr);
      }
   }

   SVGPathDataAttribute pathDataAttr;
}
