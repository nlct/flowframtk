package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGTspanElement extends SVGTextElement
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
   public void startElement() throws InvalidFormatException
   {
      super.startElement();

      textElement = (SVGTextElement)getAncestor("text");

      if (textElement == null)
      {
         throw new ElementNotInsideException(this, "text");
      }

      textElement.process();

      x = textElement.x;
      y = textElement.y;

   }

   @Override
   public void endElement() throws InvalidFormatException
   {
      super.endElement();

      textElement.x = x;

      if (objects.isEmpty())
      {// do nothing
      }
      else if (objects.size() == 1)
      {
         textElement.append(objects.firstElement());
      }
      else
      {
         textElement.append(objects);
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

   SVGTextElement textElement;
}
