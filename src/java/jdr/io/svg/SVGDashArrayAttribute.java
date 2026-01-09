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

   @Override
   protected void parse() throws InvalidFormatException
   {
      if ("none".equals(valueString))
      {
         isSolid = true;
      }
      else
      {
         isSolid = false;

         super.parse();
      }
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

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      if (object instanceof JDRShape)
      {
         JDRStroke stroke = ((JDRShape)object).getStroke();

         if (stroke instanceof JDRBasicStroke)
         {
            JDRBasicStroke basicStroke = (JDRBasicStroke)stroke;

            basicStroke.setDashPattern(getDashPattern(element));
         }
      }
   }

   @Override
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
