package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGTitleElement extends SVGAbstractElement
{
   public SVGTitleElement(SVGHandler handler,
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   @Override
   public String getName()
   {
      return "title";
   }

   @Override
   public void endElement() throws InvalidFormatException
   {
      if (parent != null)
      {
         parent.setTitle(getContents());
      }
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
   {
      return null;
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
         SVGTitleElement element = new SVGTitleElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }
}
