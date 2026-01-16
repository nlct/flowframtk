package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGCapStyleAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
{
   protected SVGCapStyleAttribute(SVGHandler handler)
   {
      super(handler);
   }

   public static SVGCapStyleAttribute valueOf(SVGHandler handler, String valueString)
      throws SVGException
   {
      SVGCapStyleAttribute attr = new SVGCapStyleAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

      if (valueString == null || valueString.equals("inherit"))
      {
         capStyle = null;
      }
      else if (valueString.equals("butt"))
      {
         capStyle = Integer.valueOf(BasicStroke.CAP_BUTT);
      }
      else if (valueString.equals("round"))
      {
         capStyle = Integer.valueOf(BasicStroke.CAP_ROUND);
      }
      else if (valueString.equals("square"))
      {
         capStyle = Integer.valueOf(BasicStroke.CAP_SQUARE);
      }
      else
      {
         throw new InvalidAttributeValueException(handler,
           getName(), valueString);
      }
   }

   public int getCapStyle()
   {
      return capStyle.intValue();
   }

   @Override
   public String getName()
   {
      return "stroke-linecap";
   }

   @Override
   public Object getValue()
   {
      return capStyle;
   }

   @Override
   public int intValue(SVGAbstractElement element)
   {
      return capStyle.intValue();
   }

   @Override
   public double doubleValue(SVGAbstractElement element)
   {
      return (double)intValue(element);
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      if (capStyle != null && object instanceof JDRShape)
      {
         JDRStroke stroke = ((JDRShape)object).getStroke();

         if (stroke instanceof JDRBasicStroke)
         {
            JDRBasicStroke basicStroke = (JDRBasicStroke)stroke;

            basicStroke.setCapStyle(capStyle.intValue());
         }
      }
   }

   @Override
   public Object clone()
   {
      SVGCapStyleAttribute attr = new SVGCapStyleAttribute(handler);
      attr.makeEqual(this);
      return attr;
   }

   public void makeEqual(SVGCapStyleAttribute attr)
   {
      super.makeEqual(attr);
      capStyle = attr.capStyle;
   }

   private Integer capStyle;
}
