package com.dickimawbooks.jdr.io.svg;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public abstract class SVGAbstractAttribute implements SVGAttribute
{
   protected SVGAbstractAttribute(SVGHandler handler)
   {
      this.handler = handler;
   }

   public abstract Object clone();

   @Override
   public String getSourceValue()
   {
      return valueString;
   }

   @Override
   public String toString()
   {
      return String.format("%s[name=%s,original=%s,value=%s]",
      getClass().getSimpleName(), getName(), valueString, getValue());
   }

   public String getCss()
   {
      return String.format("%s=\"%s\"", getName(), valueString);
   }

   @Override
   public void makeEqual(SVGAttribute other)
   {
      valueString = other.getSourceValue();
   }

   SVGHandler handler;
   String valueString;
}
