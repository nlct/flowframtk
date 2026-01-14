package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGUnknownElement extends SVGAbstractElement
{
   public SVGUnknownElement(String name, SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
      this.name = name;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      throw new UnknownElementException(handler, name);
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      return null;
   }

   public Object clone()
   {
      SVGUnknownElement element = new SVGUnknownElement(name, handler, null);

      element.makeEqual(this);

      return element;
   }

   @Override
   public void setDescription(String text)
   {
   }

   @Override
   public void setTitle(String text)
   {
   }

   String name;
}
