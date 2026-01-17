package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGIntegerAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
{
   protected SVGIntegerAttribute(SVGHandler handler, String attrName)
   {
      this(handler, attrName, true);
   }

   protected SVGIntegerAttribute(SVGHandler handler,
      String attrName, boolean horizontal)
   {
      super(handler);
      this.isHorizontal = horizontal;
      this.name = attrName;
   }

   public static SVGIntegerAttribute valueOf(SVGHandler handler,
      String attrName, String valueString)
   throws SVGException
   {
      return valueOf(handler, attrName, valueString, true);
   }

   public static SVGIntegerAttribute valueOf(SVGHandler handler,
      String attrName, String valueString, boolean horizontal)
   throws SVGException
   {
      SVGIntegerAttribute attr = new SVGIntegerAttribute(handler, attrName, horizontal);
      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

      if (valueString == null || valueString.equals("inherit"))
      {
         value = null;
      }
      else
      {
         Matcher m = VALUE_PATTERN.matcher(valueString);

         try
         {
            value = Integer.valueOf(m.group(1));

            isPercent = m.group(2).equals("%");
         }
         catch (NumberFormatException e)
         {
            throw new InvalidAttributeValueException(handler, getName(), valueString, e);
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
   public Number getNumber()
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
         value.doubleValue()*0.01*(isHorizontal
            ? element.getViewportWidth() :
            element.getViewportHeight()) :
         value.doubleValue();
   }

   @Override
   public boolean isPercentage()
   {
      return isPercent;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   @Override
   public Object clone()
   {
      SVGIntegerAttribute attr = new SVGIntegerAttribute(handler, name);
      attr.makeEqual(this);
      return attr;
   }

   public boolean isHorizontal()
   {
      return isHorizontal;
   }

   public void makeEqual(SVGAttribute other)
   {
      super.makeEqual(other);

      if (other instanceof SVGNumberAttribute)
      {
         SVGNumberAttribute attr = (SVGNumberAttribute)other;

         Number num = attr.getNumber();

         if (num instanceof Integer || num == null)
         {
            value = (Integer)num;
         }
         else
         {
            value = Integer.valueOf(num.intValue());
         }

         isPercent = attr.isPercentage();
         isHorizontal = attr.isHorizontal();
      }
   }

   protected Integer value;
   String name;

   private boolean isPercent, isHorizontal;

   private static final Pattern VALUE_PATTERN
     = Pattern.compile("\\s*([+\\-]?\\d+)\\s*(%?)\\s*");
}
