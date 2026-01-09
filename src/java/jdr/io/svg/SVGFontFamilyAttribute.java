package com.dickimawbooks.jdr.io.svg;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.xml.sax.*;

import com.dickimawbooks.texjavahelplib.HelpFontSettings;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

public class SVGFontFamilyAttribute extends SVGAbstractAttribute
{
   public SVGFontFamilyAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      super(handler, valueString);
   }

   @Override
   protected void parse() throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         fontFamily = null;
      }
      else
      {
         Matcher m = FAMILY_PATTERN.matcher(valueString);

         while (m.find())
         {
            String cssName = m.group(1);

            if (cssName.startsWith("\"") && cssName.endsWith("\""))
            {
               cssName = cssName.substring(1, cssName.length()-2);
            }

            String family = HelpFontSettings.getFontNameFromCss(cssName);

            if (handler.isFontFamilyAvailable(family))
            {
               fontFamily = family;
               break;
            }
         }
      }
   }

   @Override
   public String getName()
   {
      return "font-family";
   }

   @Override
   public Object clone()
   {
      try
      {
         SVGFontFamilyAttribute attr = new SVGFontFamilyAttribute(handler, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGFontFamilyAttribute attr)
   {
      super.makeEqual(attr);
      fontFamily = attr.fontFamily;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      if (object instanceof JDRTextual && fontFamily != null)
      {
         JDRTextual textual = (JDRTextual)object;

         textual.setFontFamily(fontFamily);
      }
   }

   private String fontFamily = null;

   static final Pattern FAMILY_PATTERN = Pattern.compile(
    "\\s*(\"[^\"]+\"|[^,\\s]+)\\s*,?", Pattern.DOTALL);
}
