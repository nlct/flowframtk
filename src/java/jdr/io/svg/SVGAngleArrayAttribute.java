package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGAngleArrayAttribute extends SVGAbstractAttribute
{
   protected SVGAngleArrayAttribute(SVGHandler handler, String attrName)
   {
      super(handler);
      this.name = attrName;
   }

   public static SVGAngleArrayAttribute valueOf(SVGHandler handler, String attrName, String valueString)
   throws SVGException
   {
      SVGAngleArrayAttribute attr = new SVGAngleArrayAttribute(handler, attrName);
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

      array = new SVGAngleAttribute[split.length];

      for (int i = 0; i < split.length; i++)
      {
         array[i] = SVGAngleAttribute.valueOf(handler, split[i]);
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

   public SVGAngleAttribute[] getArray()
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
      SVGAngleArrayAttribute attr = new SVGAngleArrayAttribute(handler, name);
      attr.makeEqual(this);
      return attr;
   }

   @Override
   public void makeEqual(SVGAttribute other)
   {
      super.makeEqual(other);

      if (other instanceof SVGAngleArrayAttribute)
      {
         SVGAngleArrayAttribute attr = (SVGAngleArrayAttribute)other;

         if (attr.array == null)
         {
            array = null;
         }
         else if (this.array != null && this.array.length == attr.array.length)
         {
            for (int i = 0; i < attr.array.length; i++)
            {
               array[i].makeEqual((SVGAttribute)attr.array[i]);
            }
         }
         else
         {
            array = new SVGAngleAttribute[attr.array.length];

            for (int i = 0; i < attr.array.length; i++)
            {
               array[i] = (SVGAngleAttribute)attr.array[i].clone();
            }
         }
      }
   }

   private SVGAngleAttribute[] array;

   private String name;
}
