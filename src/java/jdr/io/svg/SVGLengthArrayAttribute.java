package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGLengthArrayAttribute implements SVGAttribute
{
   public SVGLengthArrayAttribute(SVGHandler handler, String attrName, String valueString)
      throws InvalidFormatException
   {
      this.handler = handler;
      this.name = attrName;
      parse(valueString);
   }

   protected void parse(String valueString)
      throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         array = null;
         return;
      }

      String[] split = valueString.split("(\\s*,\\s*)|(\\s+,?\\s*)");

      array = new SVGLength[split.length];

      for (int i = 0; i < split.length; i++)
      {
         array[i] = new SVGLength(handler, split[i]);
      }
   }

   public String getName()
   {
      return name;
   }

   public Object getValue()
   {
      return array;
   }

   public SVGLength[] getArray()
   {
      return array;
   }

   public int getArrayLength()
   {
      return array.length;
   }

   public Object clone()
   {
      try
      {
         SVGLengthArrayAttribute attr = new SVGLengthArrayAttribute(handler, name, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGLengthArrayAttribute attr)
   {
      if (attr.array == null)
      {
         array = null;
      }
      else
      {
         array = new SVGLength[attr.array.length];

         for (int i = 0; i < attr.array.length; i++)
         {
            array[i] = (SVGLength)attr.array[i].clone();
         }
      }

      name = attr.name;
   }

   private SVGLength[] array;

   private String name;
   SVGHandler handler;
}
