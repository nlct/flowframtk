package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGVisibilityStyleAttribute implements SVGNumberAttribute
{
   public SVGVisibilityStyleAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      this.handler = handler;
      parse(valueString);
   }


   public void parse(String valueString)
     throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         style = null;
         return;
      }

      valueString = valueString.toLowerCase();

      if (valueString.equals("visible"))
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
         throw new InvalidFormatException("Unknown visibility style '"+valueString+"'");
      }
   }

   public int getVisibilityStyle()
   {
      return style.intValue();
   }

   public String getName()
   {
      return "visibility";
   }

   public Object getValue()
   {
      return style;
   }

   public int intValue(SVGAbstractElement element)
   {
      return style.intValue();
   }

   public double doubleValue(SVGAbstractElement element)
   {
      return (double)intValue(element);
   }

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
      if (attr.style == null)
      {
         style = null;
      }
      else
      {
         style = Integer.valueOf(attr.style.intValue());
      }
   }

   private Integer style;
   SVGHandler handler;

   public static final int VISIBLE=0;
   public static final int HIDDEN=1;
   public static final int COLLAPSE=2;
}
