package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGFontVariantAttribute extends SVGAbstractAttribute
{
   public SVGFontVariantAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      super(handler, valueString);
   }

   @Override
   protected void parse() throws InvalidFormatException
   {
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
         isInherit = true;
         isSmallCaps = true;
      }
      else
      {
         throw new InvalidFormatException("Unknown font variant '"+valueString+"'");
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
      try
      {
         SVGFontVariantAttribute attr = new SVGFontVariantAttribute(handler, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
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

   boolean isInherit = false;
   boolean isSmallCaps = false;
}
