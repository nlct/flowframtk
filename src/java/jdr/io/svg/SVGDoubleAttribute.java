package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGDoubleAttribute implements SVGNumberAttribute
{
   public SVGDoubleAttribute(SVGHandler handler, String attrName, String valueString)
      throws InvalidFormatException
   {
      this(handler, attrName, valueString, true);
   }

   public SVGDoubleAttribute(SVGHandler handler, 
      String attrName, String valueString, boolean horizontal)
      throws InvalidFormatException
   {
      this.handler = handler;
      isHorizontal = horizontal;

      if (valueString == null || valueString.equals("inherit"))
      {
         value = null;
      }
      else
      {
         Matcher m = pattern.matcher(valueString);

         try
         {
            if (m.matches())
            {
               value = new Double(m.group(1));

               isPercent = m.group(2).equals("%");
            }
            else
            {
               throw new InvalidFormatException("Can't parse number '"+valueString+"'");
            }
         }
         catch (NumberFormatException e)
         {
            throw new InvalidFormatException("Invalid numerical attribute "+e.getMessage());
         }
      }

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
          value.doubleValue()*0.01
            * (isHorizontal ? element.getViewportWidth(): element.getViewportHeight())
        : value.doubleValue();
   }

   public Object clone()
   {
      try
      {
         SVGDoubleAttribute attr = new SVGDoubleAttribute(handler, name, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGDoubleAttribute attr)
   {
      if (attr.value == null)
      {
         value = null;
      }
      else
      {
         value = new Double(attr.value.doubleValue());
      }

      name = attr.name;
      isPercent = attr.isPercent;
      isHorizontal = attr.isHorizontal;
   }

   private String name;
   private Double value;

   private boolean isPercent;
   private boolean isHorizontal;

   SVGHandler handler;

   private static final Pattern pattern = Pattern.compile("\\s*([+\\-]?\\d*\\.?\\d+(?:[Ee][+\\-]?\\d*\\.?\\d+)?)\\s*(%?)\\s*");
}
