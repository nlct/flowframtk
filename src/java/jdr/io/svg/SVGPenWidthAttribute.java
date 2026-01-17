package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPenWidthAttribute extends SVGLengthAttribute
{
   protected SVGPenWidthAttribute(SVGHandler handler)
   {
      super(handler, "stroke-width");
   }

   public static SVGPenWidthAttribute valueOf(SVGHandler handler, String valueString)
   throws SVGException
   {
      SVGPenWidthAttribute attr = new SVGPenWidthAttribute(handler);
      attr.parse(valueString, "");
      return attr;
   }

   @Override
   protected void parse(String str, String defUnitName) throws SVGException
   {
      super.parse(str, defUnitName);

      if (value != null && value.doubleValue() < 0.0)
      {
         throw new InvalidAttributeValueException(handler,
           getName(), valueString);
      }
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      if (value != null && object instanceof JDRShape)
      {
         JDRStroke stroke = ((JDRShape)object).getStroke();

         if (stroke instanceof JDRBasicStroke)
         {
            JDRBasicStroke basicStroke = (JDRBasicStroke)stroke;

            basicStroke.setPenWidth(lengthValue(element));
         }
      }
   }

   @Override
   public Object clone()
   {
      SVGPenWidthAttribute attr = new SVGPenWidthAttribute(handler);

      attr.makeEqual((SVGAttribute)this);

      return attr;
   }

}
