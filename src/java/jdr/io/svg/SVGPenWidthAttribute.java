package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPenWidthAttribute extends SVGLengthAttribute
{
   public SVGPenWidthAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      super(handler, "stroke-width", valueString);
      
      if (value != null && value.doubleValue() < 0.0)
      {
         throw new InvalidFormatException(
           handler.getMessageWithFallback("error.svg.invalid_attribute_value",
           "Invalid {0} value: {1}",
           getName(), valueString));
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
      try
      {
         SVGPenWidthAttribute attr = new SVGPenWidthAttribute(handler, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

}
