package com.dickimawbooks.jdr.io.svg;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.xml.sax.*;

import com.dickimawbooks.texjavahelplib.HelpFontSettings;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

public class SVGFontFamilyAttribute extends SVGAbstractAttribute
{
   protected SVGFontFamilyAttribute(SVGHandler handler)
   {
      super(handler);
   }

   public static SVGFontFamilyAttribute valueOf(SVGHandler handler, String valueString)
    throws SVGException
   {
      SVGFontFamilyAttribute attr = new SVGFontFamilyAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

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

         if (fontFamily == null)
         {
            throw new UnsupportedFontFamilyException(handler, valueString);
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
      SVGFontFamilyAttribute attr = new SVGFontFamilyAttribute(handler);
      attr.makeEqual(this);
      return attr;
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

   @Override
   public Object getValue()
   {
      return fontFamily;
   }

   private String fontFamily;

   static final Pattern FAMILY_PATTERN = Pattern.compile(
    "\\s*(\"[^\"]+\"|[^,\\s]+)\\s*,?", Pattern.DOTALL);
}
