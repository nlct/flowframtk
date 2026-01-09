package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGDoubleAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
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
      super(handler, valueString);
      isHorizontal = horizontal;
      name = attrName;
   }

   @Override
   protected void parse() throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         value = null;
      }
      else
      {
         Matcher m = VALUE_PATTERN.matcher(valueString);

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
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public Object getValue()
   {
      return value;
   }

   @Override
   public int intValue(SVGAbstractElement element)
   {
      return (int)Math.round(doubleValue(element));
   }

   @Override
   public double doubleValue(SVGAbstractElement element)
   {
      return isPercent ?
          value.doubleValue()*0.01
            * (isHorizontal ? element.getViewportWidth(): element.getViewportHeight())
        : value.doubleValue();
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
      super.makeEqual(attr);
      value = attr.value;
      isPercent = attr.isPercent;
      isHorizontal = attr.isHorizontal;
   }

   private String name;
   private Double value;

   private boolean isPercent;
   private boolean isHorizontal;

   private static final Pattern VALUE_PATTERN
     = Pattern.compile("\\s*([+\\-]?\\d*\\.?\\d+(?:[Ee][+\\-]?\\d*\\.?\\d+)?)\\s*(%?)\\s*");
}
