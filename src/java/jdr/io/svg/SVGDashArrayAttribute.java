package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGDashArrayAttribute extends SVGLengthArrayAttribute
{
   protected SVGDashArrayAttribute(SVGHandler handler)
   {
      super(handler, "stroke-dasharray");
   }

   public static SVGDashArrayAttribute valueOf(SVGHandler handler, String valueString)
   throws SVGException
   {
      SVGDashArrayAttribute attr = new SVGDashArrayAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   @Override
   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

      if ("none".equals(valueString))
      {
         isSolid = true;
      }
      else
      {
         isSolid = false;

         super.parse(str);
      }
   }

   public DashPattern getDashPattern(SVGAbstractElement element)
   {
      if (isSolid)
      {
         return new DashPattern(handler.getCanvasGraphics());
      }

      SVGLengthAttribute[] lengtharray = getArray();

      float[] dashPattern = new float[lengtharray.length];

      for (int i = 0; i < lengtharray.length; i++)
      {
         dashPattern[i] = (float)lengtharray[i].getStorageValue(element, true);
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
      SVGDashArrayAttribute attr = new SVGDashArrayAttribute(handler);
      attr.makeEqual(this);
      return attr;
   }

   public void makeEqual(SVGDashArrayAttribute attr)
   {
      super.makeEqual(attr);
      isSolid = attr.isSolid;
   }

   private boolean isSolid;
}
