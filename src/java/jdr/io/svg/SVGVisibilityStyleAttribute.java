package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGVisibilityStyleAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
{
   public SVGVisibilityStyleAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      super(handler, valueString);
   }

   @Override
   protected void parse() throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         style = null;
      }
      else if (valueString.equals("visible"))
      {
         style = Integer.valueOf(VISIBLE);
      }
      else if (valueString.equals("hidden"))
      {
         style = Integer.valueOf(HIDDEN);
      }
      else if (valueString.equals("collapse"))
      {
         style = Integer.valueOf(COLLAPSE);
      }
      else
      {
         throw new UnknownAttributeValueException(handler, getName(), valueString);
      }
   }

   public int getVisibilityStyle()
   {
      return style.intValue();
   }

   @Override
   public String getName()
   {
      return "visibility";
   }

   @Override
   public Object getValue()
   {
      return style;
   }

   @Override
   public int intValue(SVGAbstractElement element)
   {
      return style.intValue();
   }

   @Override
   public double doubleValue(SVGAbstractElement element)
   {
      return (double)intValue(element);
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   @Override
   public Object clone()
   {
      try
      {
         SVGVisibilityStyleAttribute attr = new SVGVisibilityStyleAttribute(handler, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGVisibilityStyleAttribute attr)
   {
      super.makeEqual(attr);
      style = attr.style;
   }

   private Integer style;

   public static final int VISIBLE=0;
   public static final int HIDDEN=1;
   public static final int COLLAPSE=2;
}
