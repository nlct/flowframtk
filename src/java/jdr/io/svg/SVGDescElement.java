package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGDescElement extends SVGAbstractElement
{
   public SVGDescElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   public String getName()
   {
      return "desc";
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
   {
      return null;
   }

   @Override
   public void endElement() throws InvalidFormatException
   {
      if (parent != null)
      {
         parent.setDescription(getContents());
      }
   }

   @Override
   public void setDescription(String text)
   {
   }

   @Override
   public void setTitle(String text)
   {
   }

   public Object clone()
   {
      SVGDescElement element = new SVGDescElement(handler, null);

      element.makeEqual(this);

      return element;
   }
}
