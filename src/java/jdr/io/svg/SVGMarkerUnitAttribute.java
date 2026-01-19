package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGMarkerUnitAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
{
   protected SVGMarkerUnitAttribute(SVGHandler handler)
   {
      super(handler);
   }

   public static SVGMarkerUnitAttribute valueOf(SVGHandler handler, String valueString)
      throws SVGException
   {
      SVGMarkerUnitAttribute attr = new SVGMarkerUnitAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

      if (valueString == null || valueString.equals("inherit"))
      {
         type = null;
      }
      else if (valueString.equals("strokeWidth"))
      {
         type = Integer.valueOf(STROKE_WIDTH);
      }
      else if (valueString.equals("userSpaceOnUse"))
      {
         type = Integer.valueOf(USER_SPACE);
      }
      else
      {
         throw new InvalidAttributeValueException(handler, getName(), valueString);
      }
   }

   public int getUnitType()
   {
      return type.intValue();
   }

   @Override
   public String getName()
   {
      return "markerUnits";
   }

   @Override
   public Object getValue()
   {
      return type;
   }

   @Override
   public Number getNumber()
   {
      return type;
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
      return type.intValue();
   }

   @Override
   public double doubleValue(SVGAbstractElement element)
   {
      return (double)intValue(element);
   }

   public boolean isUserSpaceOnUse()
   {
      return type != null && type.intValue() == USER_SPACE;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   @Override
   public Object clone()
   {
      SVGMarkerUnitAttribute attr = new SVGMarkerUnitAttribute(handler);
      attr.makeEqual(this);
      return attr;
   }

   @Override
   public void makeEqual(SVGAttribute attr)
   {
      super.makeEqual(attr);

      if (attr instanceof SVGMarkerUnitAttribute)
      {
         type = (Integer)attr.getValue();
      }
   }

   private Integer type;

   public final int STROKE_WIDTH=0, USER_SPACE = 1;
}
