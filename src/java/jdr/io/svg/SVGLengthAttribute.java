package com.dickimawbooks.jdr.io.svg;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGLengthAttribute extends SVGLength implements SVGNumberAttribute
{
   public SVGLengthAttribute(SVGHandler handler, String attrName, String valueString)
      throws InvalidFormatException
   {
      this(handler, attrName, valueString, true);
   }

   public SVGLengthAttribute(SVGHandler handler, String attrName, String valueString, boolean horizontal)
      throws InvalidFormatException
   {
      super(handler, valueString);
      this.isHorizontal = horizontal;
      this.name = attrName;
   }

   public String getName()
   {
      return name;
   }

   public JDRLength lengthValue(SVGAbstractElement element)
   {
      return getLength(element, isHorizontal);
   }

   public double doubleValue(SVGAbstractElement element)
   {
      return getBpValue(element, isHorizontal);
   }

   public int intValue(SVGAbstractElement element)
   {
      return (int)Math.round(doubleValue(element));
   }

   public Object clone()
   {
      try
      {
         SVGLengthAttribute attr = new SVGLengthAttribute(handler, name, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGLengthAttribute attr)
   {
      super.makeEqual(attr);

      name = attr.name;
      isHorizontal = attr.isHorizontal;
   }

   private boolean isHorizontal;

   private String name;
}
