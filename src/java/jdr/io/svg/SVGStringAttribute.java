package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGStringAttribute implements SVGAttribute
{
   public SVGStringAttribute(SVGHandler handler, String attrName, String attrValue)
   {
      this.handler = handler;
      this.name = attrName;
      this.value = attrValue;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public Object getValue()
   {
      return value;
   }

   @Override
   public String getSourceValue()
   {
      return value;
   }

   public String getString()
   {
      return value;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   @Override
   public Object clone()
   {
      SVGStringAttribute attr = new SVGStringAttribute(handler, name, value);

      return attr;
   }

   @Override
   public String toString()
   {
      return String.format("%s[name=%s,value=%s]",
      getClass().getSimpleName(), getName(), getValue());
   }

   private String name, value;
   SVGHandler handler;
}
