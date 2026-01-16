package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.GeneralPath;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGGradientUnitsAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
{
   protected SVGGradientUnitsAttribute(SVGHandler handler)
   {
      super(handler);
   }

   public static SVGGradientUnitsAttribute valueOf(SVGHandler handler, String valueString)
   throws SVGException
   {
      SVGGradientUnitsAttribute attr = new SVGGradientUnitsAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

      if (valueString == null || valueString.equals("inherit"))
      {
         rule = null;
      }
      else if (valueString.equals("userSpaceOnUse"))
      {
         rule = USER_SPACE;
      }
      else if (valueString.equals("objectBoundingBox"))
      {
         rule = OBJECT_BBOX;
      }
      else
      {
         throw new UnknownAttributeValueException(handler, getName(), valueString);
      }
   }

   public int getGradientUnits()
   {
      return rule.intValue();
   }

   @Override
   public String getName()
   {
      return "gradientUnits";
   }

   @Override
   public Object getValue()
   {
      return rule;
   }

   @Override
   public int intValue(SVGAbstractElement element)
   {
      return rule.intValue();
   }

   @Override
   public double doubleValue(SVGAbstractElement element)
   {
      return (double)intValue(element);
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   @Override
   public Object clone()
   {
      SVGGradientUnitsAttribute attr = new SVGGradientUnitsAttribute(handler);
      attr.makeEqual(this);
      return attr;
   }

   public void makeEqual(SVGGradientUnitsAttribute attr)
   {
      super.makeEqual(attr);
      rule = attr.rule;
   }

   private Integer rule;

   public static final int USER_SPACE=0;
   public static final int OBJECT_BBOX=1;
}
