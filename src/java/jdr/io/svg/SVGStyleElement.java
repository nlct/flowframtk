package com.dickimawbooks.jdr.io.svg;

import java.util.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGStyleElement extends SVGAbstractElement
{
   public SVGStyleElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "style";
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      String type = (String)attributeSet.getAttribute("type");

      if (type != null && !type.equals("text/css"))
      {
         throw new InvalidAttributeValueException(this, "type", type);
      }
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("type"))
      {
         attr = new SVGStringAttribute(handler, name, value);
      }
      else
      {
         attr = super.createElementAttribute(name, value);

         if (attr == null)
         {
            attr = createTextStyleAttribute(name, value);
         }

         if (attr == null)
         {
            attr = createPathStyleAttribute(name, value);
         }
      }

      return attr;
   }  

   @Override
   public void endElement() throws InvalidFormatException
   {
      addStyleRules(parent, getContents());
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
      SVGStyleElement element = new SVGStyleElement(handler, null);

      element.makeEqual(this);

      return element;
   }
}
