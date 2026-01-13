package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGDescElement extends SVGAbstractElement
{
   public SVGDescElement(SVGHandler handler,
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
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
      try
      {
         SVGDescElement element = new SVGDescElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }
}
