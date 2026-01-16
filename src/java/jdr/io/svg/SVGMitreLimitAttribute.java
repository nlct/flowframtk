package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGMitreLimitAttribute extends SVGDoubleAttribute
{
   protected SVGMitreLimitAttribute(SVGHandler handler)
   {
      super(handler, "stroke-mitrelimit");
   }

   public static SVGMitreLimitAttribute valueOf(SVGHandler handler, String valueString)
      throws SVGException
   {
      SVGMitreLimitAttribute attr = new SVGMitreLimitAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   @Override
   protected void parse(String str) throws SVGException
   {
      super.parse(str);
      
      if (value != null && value.doubleValue() < 1.0)
      {
         throw new InvalidAttributeValueException(handler, getName(), valueString);
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

            basicStroke.setMitreLimit(value.doubleValue());
         }
      }
   }

   @Override
   public Object clone()
   {
      SVGMitreLimitAttribute attr = new SVGMitreLimitAttribute(handler);
      attr.makeEqual(this);
      return attr;
   }

}
