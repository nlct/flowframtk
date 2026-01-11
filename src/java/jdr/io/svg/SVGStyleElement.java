package com.dickimawbooks.jdr.io.svg;

import java.util.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGStyleElement extends SVGAbstractElement
{
   public SVGStyleElement(SVGHandler handler, 
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   @Override
   public String getName()
   {
      return "style";
   }

   @Override
   protected void addAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      String type = attr.getValue("type");

      if (type != null && !type.equals("text/css"))
      {
         throw new InvalidFormatException("Style type '"+type
            +"' not recognised. (Can only recognise 'text/css' type.)");
      }
   }

   @Override
   public void endElement()
   {
      if (parent != null)
      {
         parent.addStyleRules(getContents());
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

   @Override
   public Object clone()
   {
      try
      {
         SVGStyleElement element = new SVGStyleElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }
}
