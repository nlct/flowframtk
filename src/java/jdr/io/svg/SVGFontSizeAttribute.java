package com.dickimawbooks.jdr.io.svg;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGFontSizeAttribute extends SVGAbstractAttribute
{
   public SVGFontSizeAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      super(handler, valueString);
   }

   @Override
   protected void parse() throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         valueType = INHERIT;
      }
      else if (valueString.equals("xx-small"))
      {
         valueType = XX_SMALL;
      }
      else if (valueString.equals("x-small"))
      {
         valueType = X_SMALL;
      }
      else if (valueString.equals("small"))
      {
         valueType = SMALL;
      }
      else if (valueString.equals("medium"))
      {
         valueType = MEDIUM;
      }
      else if (valueString.equals("large"))
      {
         valueType = LARGE;
      }
      else if (valueString.equals("x-large"))
      {
         valueType = X_LARGE;
      }
      else if (valueString.equals("xx-large"))
      {
         valueType = XX_LARGE;
      }
      else if (valueString.equals("smaller"))
      {
         valueType = SMALLER;
      }
      else if (valueString.equals("larger"))
      {
         valueType = LARGER;
      }
      else
      {
         length = new SVGLength(handler, valueString);
         valueType = VALUE;
      }
   }

   public String getName()
   {
      return "font-size";
   }

   public Object clone()
   {
      SVGFontSizeAttribute attr = null;

      try
      {
         attr = new SVGFontSizeAttribute(handler, null);
         attr.makeEqual(this);
      }
      catch (InvalidFormatException e)
      {
         // this shouldn't happen
      }

      return attr;
   }

   public void makeEqual(SVGFontSizeAttribute attr)
   {
      super.makeEqual(attr);
      length = attr.length;
      valueType = attr.valueType;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      if (object instanceof JDRTextual && valueType != INHERIT)
      {
         JDRTextual textual = (JDRTextual)object;
         JDRLength fontSize = textual.getFontSize();

         if (length == null || valueType != VALUE)
         {
            if (valueType == LARGER)
            {
               fontSize.add(1, JDRUnit.bp);
            }
            else if (valueType == SMALLER)
            {
               fontSize.subtract(1, JDRUnit.bp);
            }
            else
            {
               LaTeXFontBase fontBase
                 = handler.getCanvasGraphics().getLaTeXFontBase();

               switch (valueType)
               {
                  case XX_SMALL:
                    fontSize.setValue(
                      fontBase.getFontSize(LaTeXFontBase.SCRIPTSIZE), JDRUnit.pt);
                  break;
                  case X_SMALL:
                    fontSize.setValue(
                      fontBase.getFontSize(LaTeXFontBase.FOOTNOTESIZE), JDRUnit.pt);
                  break;
                  case SMALL:
                    fontSize.setValue(
                      fontBase.getFontSize(LaTeXFontBase.SMALL), JDRUnit.pt);
                  break;
                  case MEDIUM:
                    fontSize.setValue(fontBase.getNormalSize(), JDRUnit.pt);
                  break;
                  case LARGE:
                    fontSize.setValue(
                      fontBase.getFontSize(LaTeXFontBase.LARGE), JDRUnit.pt);
                  break;
                  case X_LARGE:
                    fontSize.setValue(
                      fontBase.getFontSize(LaTeXFontBase.XLARGE), JDRUnit.pt);
                  break;
                  case XX_LARGE:
                    fontSize.setValue(
                      fontBase.getFontSize(LaTeXFontBase.XXLARGE), JDRUnit.pt);
                  break;
               }
            }
         }
         else
         {
            fontSize = length.getLength(element, true);
         }

         textual.setFontSize(fontSize);
      }
   }

   @Override
   public Object getValue()
   {
      return Integer.valueOf(valueType);
   }

   private SVGLength length;

   static final int INHERIT = -1;
   static final int VALUE = 0;
   static final int XX_SMALL = 1;
   static final int X_SMALL = 2;
   static final int SMALL = 3;
   static final int MEDIUM = 4;
   static final int LARGE = 5;
   static final int X_LARGE = 6;
   static final int XX_LARGE = 7;
   static final int SMALLER = 8;
   static final int LARGER = 9;

   private int valueType;
}
