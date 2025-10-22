package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGIntegerAttribute implements SVGNumberAttribute
{
   public SVGIntegerAttribute(SVGHandler handler, String attrName, String valueString)
      throws InvalidFormatException
   {
      this(handler, attrName, valueString, true);
   }

   public SVGIntegerAttribute(SVGHandler handler,
      String attrName, String valueString, boolean horizontal)
      throws InvalidFormatException
   {
      this.handler = handler;

      if (valueString == null || valueString.equals("inherit"))
      {
         value = null;
      }
      else
      {
         Matcher m = pattern.matcher(valueString);

         try
         {
            value = Integer.valueOf(m.group(1));

            isPercent = m.group(2).equals("%");
         }
         catch (NumberFormatException e)
         {
            throw new InvalidFormatException("Invalid numerical attribute "+e.getMessage());
         }
      }

      this.isHorizontal = horizontal;
      this.name = attrName;
   }

   public String getName()
   {
      return name;
   }

   public Object getValue()
   {
      return value;
   }

   public int intValue(SVGAbstractElement element)
   {
      return (int)Math.round(doubleValue(element));
   }

   public double doubleValue(SVGAbstractElement element)
   {
      return isPercent ?
         value.doubleValue()*0.01*(isHorizontal
            ? element.getViewportWidth() :
            element.getViewportHeight()) :
         value.doubleValue();
   }

   public Object clone()
   {
      try
      {
         SVGIntegerAttribute attr = new SVGIntegerAttribute(handler, name, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGIntegerAttribute attr)
   {
      if (attr.value == null)
      {
         value = null;
      }
      else
      {
         value = Integer.valueOf(attr.value.intValue());
      }

      name = attr.name;
      isPercent = attr.isPercent;
      isHorizontal = attr.isHorizontal;
   }

   private String name;
   private Integer value;

   private boolean isPercent, isHorizontal;
   SVGHandler handler;

   private static final Pattern pattern = Pattern.compile("\\s*([+\\-]?\\d+)\\s*(%?)\\s*");
}
