package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.MessageInfo;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGLineElement extends SVGShape
{
   public SVGLineElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("x1", attr);
      addAttribute("y1", attr);
      addAttribute("x2", attr);
      addAttribute("y2", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String style)
     throws InvalidFormatException
   {
      SVGAttribute attr;

      if (name.equals("x1"))
      {
         attr = new SVGLengthAttribute(handler, name, style, true);
      }
      else if (name.equals("y1"))
      {
         attr = new SVGLengthAttribute(handler, name, style, false);
      }
      else if (name.equals("x2"))
      {
         attr = new SVGLengthAttribute(handler, name, style, true);
      }
      else if (name.equals("y2"))
      {
         attr = new SVGLengthAttribute(handler, name, style, false);
      }
      else
      {
         attr = super.createElementAttribute(name, style);
      }

      return attr;
   }

   @Override
   public String getName()
   {
      return "line";
   }

   @Override
   public JDRShape createShape(CanvasGraphics cg)
   {
      double p1x = getDoubleAttribute("x1", 0);
      double p1y = getDoubleAttribute("y1", 0);
      double p2x = getDoubleAttribute("x2", 0);
      double p2y = getDoubleAttribute("y2", 0);

      JDRPath path = new JDRPath(cg);

      try
      {
         path.add(new JDRLine(cg, p1x, p1y, p2x, p2y));
      }
      catch (InvalidPathException e)
      {
         // shouldn't happen
         cg.getMessageSystem().postMessage(
           MessageInfo.createInternalError(e));
      }

      return path;
   }


   @Override
   public Object clone()
   {
      SVGLineElement element = new SVGLineElement(handler, null);

      element.makeEqual(this);

      return element;
   }
}
