package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGFontVariantAttribute extends SVGAbstractAttribute
{
   protected SVGFontVariantAttribute(SVGHandler handler)
   {
      super(handler);
   }

   public static SVGFontVariantAttribute valueOf(SVGHandler handler, String valueString)
    throws SVGException
   {
      SVGFontVariantAttribute attr = new SVGFontVariantAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

      if (valueString == null || valueString.equals("inherit"))
      {
         isInherit = true;
      }
      else if (valueString.equals("normal"))
      {
         isInherit = false;
         isSmallCaps = false;
      }
      else if (valueString.equals("small-caps"))
      {
         isInherit = false;
         isSmallCaps = true;
      }
      else
      {
         throw new UnknownAttributeValueException(handler, getName(), valueString);
      }
   }

   @Override
   public String getName()
   {
      return "font-variant";
   }

   @Override
   public Object clone()
   {
      SVGFontVariantAttribute attr = new SVGFontVariantAttribute(handler);
      attr.makeEqual(this);
      return attr;
   }

   public void makeEqual(SVGFontVariantAttribute attr)
   {
      super.makeEqual(attr);
      isSmallCaps = attr.isSmallCaps;
      isInherit = attr.isInherit;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      if (object instanceof JDRTextual && !isInherit)
      {
         JDRTextual textual = (JDRTextual)object;

         if (isSmallCaps)
         {
            textual.setFontShape(JDRFont.SHAPE_SC);
         }
         else if (textual.getFontShape() == JDRFont.SHAPE_SC)
         {
            textual.setFontShape(JDRFont.SHAPE_UPRIGHT);
         }
      }
   }

   @Override
   public Object getValue()
   {
      return isInherit ? null : Boolean.valueOf(isSmallCaps);
   }

   boolean isInherit;
   boolean isSmallCaps;
}
