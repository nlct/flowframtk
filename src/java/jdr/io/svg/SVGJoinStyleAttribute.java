package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGJoinStyleAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
{
   public SVGJoinStyleAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      super(handler, valueString);
   }

   @Override
   protected void parse() throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         joinStyle = null;
      }
      else if (valueString.equals("miter"))
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

   @Override
   public String getName()
   {
      return "stroke-linejoin";
   }

   @Override
   public Object getValue()
   {
      return joinStyle;
   }

   @Override
   public int intValue(SVGAbstractElement element)
   {
      return joinStyle.intValue();
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
         SVGJoinStyleAttribute attr = new SVGJoinStyleAttribute(handler, null);

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
      super.makeEqual(attr);
      joinStyle = attr.joinStyle;
   }

   private Integer joinStyle;
}
