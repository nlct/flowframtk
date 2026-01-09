package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.GeneralPath;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGGradientUnitsAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
{
   public SVGGradientUnitsAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      super(handler, valueString);
   }

   @Override
   protected void parse() throws InvalidFormatException
   {
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
         throw new InvalidFormatException("Unknown gradientUnits '"+valueString+"'");
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
      try
      {
         SVGGradientUnitsAttribute attr = new SVGGradientUnitsAttribute(handler, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
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
