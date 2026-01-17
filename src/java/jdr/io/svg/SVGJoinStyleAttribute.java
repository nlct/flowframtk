package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGJoinStyleAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
{
   protected SVGJoinStyleAttribute(SVGHandler handler)
   {
      super(handler);
   }

   public static SVGJoinStyleAttribute valueOf(SVGHandler handler, String valueString)
      throws SVGException
   {
      SVGJoinStyleAttribute attr = new SVGJoinStyleAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

      if (valueString == null || valueString.equals("inherit"))
      {
         joinStyle = null;
      }
      else if (valueString.equals("miter"))
      {
         joinStyle = BasicStroke.JOIN_MITER;
      }
      else if (valueString.equals("round"))
      {
         joinStyle = BasicStroke.JOIN_ROUND;
      }
      else if (valueString.equals("bevel"))
      {
         joinStyle = BasicStroke.JOIN_BEVEL;
      }
      else
      {
         throw new InvalidAttributeValueException(handler, getName(), valueString);
      }
   }

   public int getJoinStyle()
   {
      return joinStyle.intValue();
   }

   @Override
   public String getName()
   {
      return "stroke-linejoin";
   }

   @Override
   public Object getValue()
   {
      return joinStyle;
   }

   @Override
   public Number getNumber()
   {
      return joinStyle;
   }

   @Override
   public boolean isPercentage()
   {
      return false;
   }

   @Override
   public boolean isHorizontal()
   {
      return false;
   }

   @Override
   public int intValue(SVGAbstractElement element)
   {
      return joinStyle.intValue();
   }

   @Override
   public double doubleValue(SVGAbstractElement element)
   {
      return (double)intValue(element);
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      if (joinStyle != null && object instanceof JDRShape)
      {
         JDRStroke stroke = ((JDRShape)object).getStroke();

         if (stroke instanceof JDRBasicStroke)
         {
            JDRBasicStroke basicStroke = (JDRBasicStroke)stroke;

            basicStroke.setJoinStyle(joinStyle.intValue());
         }
      }
   }

   @Override
   public Object clone()
   {
      SVGJoinStyleAttribute attr = new SVGJoinStyleAttribute(handler);
      attr.makeEqual(this);
      return attr;
   }

   @Override
   public void makeEqual(SVGAttribute attr)
   {
      super.makeEqual(attr);

      if (attr instanceof SVGJoinStyleAttribute)
      {
         joinStyle = (Integer)attr.getValue();
      }
   }

   private Integer joinStyle;
}
