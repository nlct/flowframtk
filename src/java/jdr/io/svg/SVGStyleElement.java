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

   protected void applyAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.applyAttributes(uri, attr);

      String type = attr.getValue("type");

      if (type == null)
      {
         throw new InvalidFormatException("No type specified for 'style' element");
      }

      if (!type.equals("text/css"))
      {
         throw new InvalidFormatException("Style type '"+type
            +"' not recognised. (Can only recognise 'text/css' type.)");
      }
   }

   public String getName()
   {
      return "style";
   }

   public void endElement()
   {
      if (parent != null)
      {
         parent.addStyleRules(getContents());
      }
   }

   public void addToImage(JDRGroup group)
   {
   }

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
