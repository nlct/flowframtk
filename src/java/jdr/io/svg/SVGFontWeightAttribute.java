package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGFontWeightAttribute extends SVGAbstractAttribute
{
   public SVGFontWeightAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      super(handler, valueString);
   }

   @Override
   protected void parse() throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         fontWeight = null;
      }
      else if (valueString.equals("bold") || valueString.equals("bolder")
          || valueString.equals("500")
          || valueString.equals("600")
          || valueString.equals("700")
          || valueString.equals("800")
          || valueString.equals("900"))
      {
         fontWeight = Integer.valueOf(JDRFont.SERIES_BOLD);
      }
      else if (valueString.equals("normal") || valueString.equals("lighter")
             || valueString.equals("100")
             || valueString.equals("200")
             || valueString.equals("300")
             || valueString.equals("400"))
      {
         fontWeight = Integer.valueOf(JDRFont.SERIES_MEDIUM);
      }
      else
      {
         throw new InvalidFormatException("Unknown font weight '"+valueString+"'");
      }
   }

   @Override
   public String getName()
   {
      return "font-weight";
   }

   @Override
   public Object clone()
   {
      try
      {
         SVGFontWeightAttribute attr = new SVGFontWeightAttribute(handler, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGFontWeightAttribute attr)
   {
      super.makeEqual(attr);
      fontWeight = attr.fontWeight;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      if (object instanceof JDRTextual && fontWeight != null)
      {
         JDRTextual textual = (JDRTextual)object;

         textual.setFontSeries(fontWeight);
      }
   }

   private Integer fontWeight;
}
