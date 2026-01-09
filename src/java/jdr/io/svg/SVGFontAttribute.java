package com.dickimawbooks.jdr.io.svg;

import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.dickimawbooks.jdr.JDRCompleteObject;
import com.dickimawbooks.jdr.exceptions.*;

public class SVGFontAttribute extends SVGCompoundAttribute
{
   public SVGFontAttribute(SVGHandler handler, String valueString)
    throws InvalidFormatException
   {
      super(handler, "font", valueString);
   }

   protected SVGFontAttribute(SVGHandler handler)
   {
      super(handler, "font");
   }

   @Override
   protected void parse() throws InvalidFormatException
   {
      if (valueString == null)
      {
      }
      else if (valueString.equals("inherit"))
      {
         addAttribute(new SVGFontFamilyAttribute(handler, valueString));
      }
      else
      {
         Matcher m = FONT_PATTERN.matcher(valueString);

         if (m.matches())
         {
            String fontStyle = m.group(1);
            String fontVariant = m.group(2);
            String fontWeight = m.group(3);

            String fontSize = m.group(4);
            String fontLineHeight = m.group(5);
            String fontFamily = m.group(6);

            if (fontStyle != null)
            {
               addAttribute(new SVGFontStyleAttribute(handler, fontStyle));
            }

            if (fontVariant != null)
            {
               addAttribute(new SVGFontVariantAttribute(handler, fontVariant));
            }

            if (fontWeight != null)
            {
               addAttribute(new SVGFontWeightAttribute(handler, fontWeight));
            }

            if (fontSize != null)
            {
               addAttribute(new SVGFontSizeAttribute(handler, fontSize));
            }

            if (fontFamily != null)
            {
               addAttribute(new SVGFontFamilyAttribute(handler, fontFamily));
            }
         }
         else
         {
            throw new InvalidFormatException("Unknown font '"+valueString+"'");
         }
      }
   }

   @Override
   public Object clone()
   {
      SVGFontAttribute fontAttr = new SVGFontAttribute(handler);
      fontAttr.valueString = valueString;

      for (Enumeration en = getAttributeNames(); en.hasMoreElements(); )
      {
         SVGAttribute attr = (SVGAttribute)getAttribute(en.nextElement());
         fontAttr.addAttribute(attr);
      }

      return fontAttr;
   }

   private static final Pattern FONT_PATTERN =
     Pattern.compile(
      "(?:(normal|italic|oblique)\\s+)?(?:(normal|small-caps)\\s+)?(?:(normal|bold|bolder|lighter|[1-9]00)\\s+)?(.+?)(\\/.+?)?\\s+(.+)",
      Pattern.DOTALL);
}
