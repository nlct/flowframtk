package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGAngleArrayAttribute extends SVGAbstractAttribute
{
   public SVGAngleArrayAttribute(SVGHandler handler, String attrName, String valueString)
      throws InvalidFormatException
   {
      super(handler, valueString);
      this.name = attrName;
   }

   @Override
   protected void parse() throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         array = null;
         return;
      }

      String[] split = valueString.split("(\\s*,\\s*)|(\\s+,?\\s*)");

      array = new SVGAngleAttribute[split.length];

      for (int i = 0; i < split.length; i++)
      {
         array[i] = new SVGAngleAttribute(handler, split[i]);
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
      try
      {
         SVGAngleArrayAttribute attr = new SVGAngleArrayAttribute(handler, name, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGAngleArrayAttribute attr)
   {
      super.makeEqual(attr);

      if (attr.array == null)
      {
         array = null;
      }
      else
      {
         array = new SVGAngleAttribute[attr.array.length];

         for (int i = 0; i < attr.array.length; i++)
         {
            array[i] = (SVGAngleAttribute)attr.array[i].clone();
         }
      }

      name = attr.name;
   }

   private SVGAngleAttribute[] array;

   private String name;
}
