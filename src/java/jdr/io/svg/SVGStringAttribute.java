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

   public String getName()
   {
      return name;
   }

   public Object getValue()
   {
      return value;
   }

   public String getString()
   {
      return value;
   }

   public Object clone()
   {
      SVGStringAttribute attr = new SVGStringAttribute(handler, name, null);

      attr.makeEqual(this);

      return attr;
   }

   public void makeEqual(SVGStringAttribute attr)
   {
      name = attr.name;
      value = attr.value;
   }

   private String name, value;
   SVGHandler handler;
}
