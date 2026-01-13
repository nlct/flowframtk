package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGTspanElement extends SVGAbstractElement
{
   public SVGTspanElement(SVGHandler handler,
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   @Override
   public String getName()
   {
      return "tspan";
   }

   @Override
   public void endElement()
   {
      // no support for changing style within a text area

      SVGAbstractElement element = getAncestor("text");

      if (element != null)
      {
         element.addToContents(getContents());
      }
   }

   @Override
   public Object clone()
   {
      try
      {
         SVGTspanElement element = new SVGTspanElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
   {
      return null;
   }

   @Override
   public void setTitle(String title)
   {
   }

   @Override
   public void setDescription(String description)
   {
   }
}
