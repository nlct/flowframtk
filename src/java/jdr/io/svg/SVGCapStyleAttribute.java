package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGCapStyleAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
{
   public SVGCapStyleAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      super(handler, valueString);
   }

   @Override
   protected void parse() throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         capStyle = null;
      }
      else if (valueString.equals("butt"))
      {
         capStyle = Integer.valueOf(BasicStroke.CAP_BUTT);
      }
      else if (valueString.equals("round"))
      {
         capStyle = Integer.valueOf(BasicStroke.CAP_ROUND);
      }
      else if (valueString.equals("square"))
      {
         capStyle = Integer.valueOf(BasicStroke.CAP_SQUARE);
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

   @Override
   public String getName()
   {
      return "stroke-linecap";
   }

   public Object getValue()
   {
      return capStyle;
   }

   @Override
   public int intValue(SVGAbstractElement element)
   {
      return capStyle.intValue();
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
         SVGCapStyleAttribute attr = new SVGCapStyleAttribute(handler, null);

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
      super.makeEqual(attr);
      capStyle = attr.capStyle;
   }

   private Integer capStyle;
}
