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

   public void addToImage(JDRGroup group)
   {
      group.setDescription(getContents());
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
