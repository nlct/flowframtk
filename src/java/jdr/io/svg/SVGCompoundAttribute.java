package com.dickimawbooks.jdr.io.svg;

import java.util.Enumeration;
import javax.swing.text.SimpleAttributeSet;

import com.dickimawbooks.jdr.JDRCompleteObject;
import com.dickimawbooks.jdr.exceptions.*;

public abstract class SVGCompoundAttribute extends SimpleAttributeSet implements SVGAttribute
{
   public SVGCompoundAttribute(SVGHandler handler, String name, String valueString)
    throws InvalidFormatException
   {
      this(handler, name);
      this.valueString = valueString;

      parse();
   }

   protected SVGCompoundAttribute(SVGHandler handler, String name)
   {
      this.handler = handler;
      this.name = name;
   }

   protected abstract void parse() throws InvalidFormatException;

   @Override
   public String getName()
   {
      return name;
   }

   public void addAttribute(SVGAttribute attr)
   {
      addAttribute(attr.getName(), attr);
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      for (Enumeration en = getAttributeNames(); en.hasMoreElements(); )
      {
         SVGAttribute attr = (SVGAttribute)getAttribute(en.nextElement());
         attr.applyTo(element, object);
      }
   }

   @Override
   public Object getValue()
   {
      return valueString;
   }

   String name;
   String valueString;
   SVGHandler handler;
}
