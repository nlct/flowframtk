package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGFontStyleAttribute extends SVGAbstractAttribute
{
   public SVGFontStyleAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      super(handler, valueString);
   }

   @Override
   protected void parse() throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         fontStyle = null;
      }
      else if (valueString.equals("normal"))
      {
         fontStyle = Integer.valueOf(JDRFont.SHAPE_UPRIGHT);
      }
      else if (valueString.equals("italic"))
      {
         fontStyle = Integer.valueOf(JDRFont.SHAPE_ITALIC);
      }
      else if (valueString.equals("oblique"))
      {
         fontStyle = Integer.valueOf(JDRFont.SHAPE_SLANTED);
      }
      else
      {
         throw new InvalidFormatException("Unknown font style '"+valueString+"'");
      }
   }

   @Override
   public String getName()
   {
      return "font-style";
   }

   @Override
   public Object clone()
   {
      try
      {
         SVGFontStyleAttribute attr = new SVGFontStyleAttribute(handler, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGFontStyleAttribute attr)
   {
      super.makeEqual(attr);
      fontStyle = attr.fontStyle;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      if (object instanceof JDRTextual && fontStyle != null)
      {
         JDRTextual textual = (JDRTextual)object;

         if (fontStyle.intValue() == JDRFont.SHAPE_UPRIGHT)
         {
            if (textual.getFontShape() != JDRFont.SHAPE_SC)
            {
               textual.setFontShape(JDRFont.SHAPE_UPRIGHT);
            }
         }
         else
         {
            textual.setFontShape(fontStyle.intValue());
         }

         switch (textual.getFontShape())
         {
            case JDRFont.SHAPE_UPRIGHT:
              textual.setLaTeXShape("\\upshape");
            break;
            case JDRFont.SHAPE_EM:
              textual.setLaTeXShape("\\em");
            break;
            case JDRFont.SHAPE_ITALIC:
              textual.setLaTeXShape("\\itshape");
            break;
            case JDRFont.SHAPE_SLANTED:
              textual.setLaTeXShape("\\slshape");
            break;
            case JDRFont.SHAPE_SC:
              textual.setLaTeXShape("\\scshape");
            break;
         }
      }
   }

   @Override
   public Object getValue()
   {
      return fontStyle;
   }

   private Integer fontStyle;
}
