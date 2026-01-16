package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGLengthArrayAttribute extends SVGAbstractAttribute
{
   protected SVGLengthArrayAttribute(SVGHandler handler, String attrName)
   {
      super(handler);
      this.name = attrName;
   }

   public static SVGLengthArrayAttribute valueOf(SVGHandler handler, String attrName, String valueString)
   throws SVGException
   {
      SVGLengthArrayAttribute attr = new SVGLengthArrayAttribute(handler, attrName);

      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

      if (valueString == null || valueString.equals("inherit"))
      {
         array = null;
         return;
      }

      String[] split = valueString.split("(\\s*,\\s*)|(\\s+,?\\s*)");

      array = new SVGLengthAttribute[split.length];

      for (int i = 0; i < split.length; i++)
      {
         array[i] = SVGLengthAttribute.valueOf(handler, getName(), split[i]);
      }
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public Object getValue()
   {
      return array;
   }

   public SVGLengthAttribute[] getArray()
   {
      return array;
   }

   public int getArrayLength()
   {
      return array == null ? 0 : array.length;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   @Override
   public Object clone()
   {
      SVGLengthArrayAttribute attr = new SVGLengthArrayAttribute(handler, name);

      attr.makeEqual(this);

      return attr;
   }

   public void makeEqual(SVGLengthArrayAttribute attr)
   {
      super.makeEqual(attr);

      if (attr.array == null)
      {
         array = null;
      }
      else
      {
         array = new SVGLengthAttribute[attr.array.length];

         for (int i = 0; i < attr.array.length; i++)
         {
            array[i] = (SVGLengthAttribute)attr.array[i].clone();
         }
      }

      name = attr.name;
   }

   private SVGLengthAttribute[] array;

   private String name;
}
