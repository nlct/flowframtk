package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGJoinStyleAttribute implements SVGNumberAttribute
{
   public SVGJoinStyleAttribute(String valueString)
     throws InvalidFormatException
   {
      parse(valueString);
   }

   public void parse(String valueString)
     throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         joinStyle = null;
         return;
      }

      valueString = valueString.toLowerCase();

      if (valueString.equals("miter"))
      {
         joinStyle = BasicStroke.JOIN_MITER;
      }
      else if (valueString.equals("round"))
      {
         joinStyle = BasicStroke.JOIN_ROUND;
      }
      else if (valueString.equals("bevel"))
      {
         joinStyle = BasicStroke.JOIN_BEVEL;
      }
      else
      {
         throw new InvalidFormatException("Unknown join style '"+valueString+"'");
      }
   }

   public int getJoinStyle()
   {
      return joinStyle.intValue();
   }

   public String getName()
   {
      return "stroke-linejoin";
   }

   public Object getValue()
   {
      return joinStyle;
   }

   public int intValue(SVGAbstractElement element)
   {
      return joinStyle.intValue();
   }

   public double doubleValue(SVGAbstractElement element)
   {
      return (double)intValue(element);
   }

   public Object clone()
   {
      try
      {
         SVGJoinStyleAttribute attr = new SVGJoinStyleAttribute(null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGJoinStyleAttribute attr)
   {
      if (attr.joinStyle == null)
      {
         joinStyle = null;
      }
      else
      {
         joinStyle = new Integer(attr.joinStyle.intValue());
      }
   }

   private Integer joinStyle;
}
