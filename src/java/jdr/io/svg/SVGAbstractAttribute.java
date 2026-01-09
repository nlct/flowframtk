package com.dickimawbooks.jdr.io.svg;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public abstract class SVGAbstractAttribute implements SVGAttribute
{
   public SVGAbstractAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      this.handler = handler;
      this.valueString = valueString;
      parse();
   }

   protected abstract void parse() throws InvalidFormatException;

   public abstract Object clone();

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

   public void makeEqual(SVGAbstractAttribute other)
   {
      valueString = other.valueString;
   }

   SVGHandler handler;
   String valueString;
}
