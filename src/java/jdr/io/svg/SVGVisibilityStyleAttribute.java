package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGVisibilityStyleAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
{
   protected SVGVisibilityStyleAttribute(SVGHandler handler)
   {
      super(handler);
   }

   public static SVGVisibilityStyleAttribute valueOf(SVGHandler handler, String valueString)
   throws SVGException
   {
      SVGVisibilityStyleAttribute attr = new SVGVisibilityStyleAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

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
      SVGVisibilityStyleAttribute attr = new SVGVisibilityStyleAttribute(handler);
      attr.makeEqual(this);
      return attr;
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
