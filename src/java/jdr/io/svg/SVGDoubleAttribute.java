package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGDoubleAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
{
   protected SVGDoubleAttribute(SVGHandler handler, String attrName)
   {
      this(handler, attrName, true);
   }

   protected SVGDoubleAttribute(SVGHandler handler, 
      String attrName, boolean horizontal)
   {
      super(handler);
      isHorizontal = horizontal;
      name = attrName;
   }

   public static SVGDoubleAttribute valueOf(SVGHandler handler, 
      String attrName, String value)
   throws SVGException
   {
      return valueOf(handler, attrName, value, true);
   }

   public static SVGDoubleAttribute valueOf(SVGHandler handler, 
      String attrName, String value, boolean horizontal)
   throws SVGException
   {
      SVGDoubleAttribute attr = new SVGDoubleAttribute(handler, attrName, horizontal);

      attr.parse(value);

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
            if (m.matches())
            {
               value = new Double(m.group(1));

               isPercent = m.group(2).equals("%");
            }
            else
            {
               throw new CantParseAttributeValueException(handler, getName(), valueString);
            }
         }
         catch (NumberFormatException e)
         {
            throw new CantParseAttributeValueException(handler, getName(), valueString, e);
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
          value.doubleValue()*0.01
            * (isHorizontal ? element.getViewportWidth(): element.getViewportHeight())
        : value.doubleValue();
   }

   @Override
   public boolean isPercentage()
   {
      return isPercent;
   }

   @Override
   public boolean isHorizontal()
   {
      return isHorizontal;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   @Override
   public Object clone()
   {
      SVGDoubleAttribute attr = new SVGDoubleAttribute(handler, name);
      attr.makeEqual(this);
      return attr;
   }

   public void makeEqual(SVGAttribute other)
   {
      super.makeEqual(other);

      if (other instanceof SVGNumberAttribute)
      {
         SVGNumberAttribute attr = (SVGNumberAttribute)other;

         Number num = attr.getNumber();

         if (num instanceof Double || num == null)
         {
            value = (Double)num;
         }
         else
         {
            value = Double.valueOf(num.doubleValue());
         }

         isPercent = attr.isPercentage();
         isHorizontal = attr.isHorizontal();
      }
   }

   private String name;
   protected Double value;

   private boolean isPercent;
   private boolean isHorizontal;

   private static final Pattern VALUE_PATTERN
     = Pattern.compile("\\s*([+\\-]?\\d*\\.?\\d+(?:[Ee][+\\-]?\\d*\\.?\\d+)?)\\s*(%?)\\s*");
}
