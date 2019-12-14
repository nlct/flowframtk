package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.GeneralPath;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGGradientUnitsAttribute implements SVGNumberAttribute
{
   public SVGGradientUnitsAttribute(String valueString)
     throws InvalidFormatException
   {
      parse(valueString);
   }


   public void parse(String valueString)
     throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         rule = null;
         return;
      }

      if (valueString.equals("userSpaceOnUse"))
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

   public String getName()
   {
      return "gradientUnits";
   }

   public Object getValue()
   {
      return rule;
   }

   public int intValue(SVGAbstractElement element)
   {
      return rule.intValue();
   }

   public double doubleValue(SVGAbstractElement element)
   {
      return (double)intValue(element);
   }

   public Object clone()
   {
      try
      {
         SVGGradientUnitsAttribute attr = new SVGGradientUnitsAttribute(null);

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
      if (attr.rule == null)
      {
         rule = null;
      }
      else
      {
         rule = new Integer(attr.rule.intValue());
      }
   }

   private Integer rule;

   public static final int USER_SPACE=0;
   public static final int OBJECT_BBOX=1;
}
