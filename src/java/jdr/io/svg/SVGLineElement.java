package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.MessageInfo;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGLineElement extends SVGShape
{
   public SVGLineElement(SVGHandler handler, 
      SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   protected void applyAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.applyAttributes(uri, attr);

      setLineX1(attr.getValue("x1"));
      setLineY1(attr.getValue("y1"));
      setLineX2(attr.getValue("x2"));
      setLineY2(attr.getValue("y2"));
   }

   protected void setLineX1(String valueString)
     throws InvalidFormatException
   {
      addAttribute(new SVGLengthAttribute("x1", valueString, true));
   }

   protected void setLineY1(String valueString)
     throws InvalidFormatException
   {
      addAttribute(new SVGLengthAttribute("y1", valueString, false));
   }

   protected void setLineX2(String valueString)
     throws InvalidFormatException
   {
      addAttribute(new SVGLengthAttribute("x2", valueString, true));
   }

   protected void setLineY2(String valueString)
     throws InvalidFormatException
   {
      addAttribute(new SVGLengthAttribute("y2", valueString, false));
   }

   public String getName()
   {
      return "line";
   }

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


   public Object clone()
   {
      try
      {
         SVGLineElement element = new SVGLineElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
         getCanvasGraphics().getMessageSystem().postMessage(
           MessageInfo.createInternalError(e));
      }

      return null;
   }
}
