package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGDashOffsetAttribute extends SVGLengthAttribute
{
   protected SVGDashOffsetAttribute(SVGHandler handler)
   {
      super(handler, "stroke-dashoffset");
   }

   public static SVGDashOffsetAttribute valueOf(SVGHandler handler, String valueString)
      throws SVGException
   {
      SVGDashOffsetAttribute attr = new SVGDashOffsetAttribute(handler);
      attr.parse(valueString, "");
      return attr;
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

            JDRLength len = lengthValue(element);

            DashPattern pattern = basicStroke.getDashPattern();

            if (pattern != null)
            {
               pattern.setOffset(len);
            }
         }
      }
   }

   @Override
   public Object clone()
   {
      SVGDashOffsetAttribute attr = new SVGDashOffsetAttribute(handler);
      attr.makeEqual(this);
      return attr;
   }

}
