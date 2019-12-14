package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGCapStyleAttribute implements SVGNumberAttribute
{
   public SVGCapStyleAttribute(String valueString)
     throws InvalidFormatException
   {
      parse(valueString);
   }


   public void parse(String valueString)
     throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         capStyle = null;
         return;
      }

      valueString = valueString.toLowerCase();

      if (valueString.equals("butt"))
      {
         capStyle = new Integer(BasicStroke.CAP_BUTT);
      }
      else if (valueString.equals("round"))
      {
         capStyle = new Integer(BasicStroke.CAP_ROUND);
      }
      else if (valueString.equals("square"))
      {
         capStyle = new Integer(BasicStroke.CAP_SQUARE);
      }
      else
      {
         throw new InvalidFormatException("Unknown cap style '"+valueString+"'");
      }
   }

   public int getCapStyle()
   {
      return capStyle.intValue();
   }

   public String getName()
   {
      return "stroke-linecap";
   }

   public Object getValue()
   {
      return capStyle;
   }

   public int intValue(SVGAbstractElement element)
   {
      return capStyle.intValue();
   }

   public double doubleValue(SVGAbstractElement element)
   {
      return (double)intValue(element);
   }

   public Object clone()
   {
      try
      {
         SVGCapStyleAttribute attr = new SVGCapStyleAttribute(null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGCapStyleAttribute attr)
   {
      if (attr.capStyle == null)
      {
         capStyle = null;
      }
      else
      {
         capStyle = new Integer(attr.capStyle.intValue());
      }
   }

   private Integer capStyle;
}
