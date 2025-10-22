package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGDashArrayAttribute extends SVGLengthArrayAttribute
{
   public SVGDashArrayAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      super(handler, "stroke-dasharray", valueString);
   }

   protected void parse(String valueString)
     throws InvalidFormatException
   {
      if ("none".equals(valueString))
      {
         isSolid = true;
         return;
      }

      isSolid = false;

      super.parse(valueString);
   }

   public DashPattern getDashPattern(SVGAbstractElement element)
   {
      if (isSolid)
      {
         return new DashPattern(handler.getCanvasGraphics());
      }

      SVGLength[] lengtharray = getArray();

      float[] dashPattern = new float[lengtharray.length];

      for (int i = 0; i < lengtharray.length; i++)
      {
         dashPattern[i] = (float)lengtharray[i].getBpValue(element, true);
      }

      return new DashPattern(handler.getCanvasGraphics(), dashPattern);
   }

   public Object clone()
   {
      try
      {
         SVGDashArrayAttribute attr = new SVGDashArrayAttribute(handler, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGDashArrayAttribute attr)
   {
      super.makeEqual(attr);
      isSolid = attr.isSolid;
   }


   private boolean isSolid;
}
