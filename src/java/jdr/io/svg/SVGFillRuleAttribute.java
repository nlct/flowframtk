package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.GeneralPath;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGFillRuleAttribute implements SVGNumberAttribute
{
   public SVGFillRuleAttribute(String valueString)
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

      valueString = valueString.toLowerCase();

      if (valueString.equals("nonzero"))
      {
         rule = new Integer(GeneralPath.WIND_NON_ZERO);
      }
      else if (valueString.equals("evenodd"))
      {
         rule = new Integer(GeneralPath.WIND_EVEN_ODD);
      }
      else
      {
         throw new InvalidFormatException("Unknown winding rule '"+valueString+"'");
      }
   }

   public int getWindingRule()
   {
      return rule.intValue();
   }

   public String getName()
   {
      return "fill-rule";
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
         SVGFillRuleAttribute attr = new SVGFillRuleAttribute(null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGFillRuleAttribute attr)
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
}
