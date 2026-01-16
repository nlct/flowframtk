package com.dickimawbooks.jdr.io.svg;

import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.dickimawbooks.jdr.JDRCompleteObject;
import com.dickimawbooks.jdr.exceptions.*;

public class SVGFontAttribute extends SVGCompoundAttribute
{
   protected SVGFontAttribute(SVGHandler handler)
   {
      super(handler, "font");
   }

   public static SVGFontAttribute valueOf(SVGHandler handler, String valueString)
    throws SVGException
   {
      SVGFontAttribute attr = new SVGFontAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

      if (valueString == null)
      {
      }
      else if (valueString.equals("inherit"))
      {
         addAttribute(SVGFontFamilyAttribute.valueOf(handler, valueString));
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
               addAttribute(SVGFontStyleAttribute.valueOf(handler, fontStyle));
            }

            if (fontVariant != null)
            {
               addAttribute(SVGFontVariantAttribute.valueOf(handler, fontVariant));
            }

            if (fontWeight != null)
            {
               addAttribute(SVGFontWeightAttribute.valueOf(handler, fontWeight));
            }

            if (fontSize != null)
            {
               addAttribute(SVGFontSizeAttribute.valueOf(handler, fontSize));
            }

            if (fontFamily != null)
            {
               addAttribute(SVGFontFamilyAttribute.valueOf(handler, fontFamily));
            }
         }
         else
         {
            throw new CantParseAttributeValueException(handler, getName(), valueString);
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
