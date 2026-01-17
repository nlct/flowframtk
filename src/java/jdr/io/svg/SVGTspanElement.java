package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGTspanElement extends SVGTextElement
{
   public SVGTspanElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "tspan";
   }

   protected void assignTextAncestor() throws SVGException
   {
      textElement = getTextAncestor();

      if (textElement == null)
      {
         throw new ElementNotInsideException(this, "text");
      }
   }

   @Override
   protected void createTemplate() throws SVGException
   {        
      if (textElement == null)
      {
         super.createTemplate();
      }
      else
      {
         templateText = (JDRText)textElement.templateText.clone();

         applyTextAttributes(templateText);
      }
   } 

   @Override
   public void startElement() throws InvalidFormatException
   {
      assignTextAncestor();

      super.startElement();

      if (textElement != null)
      {
         textElement.process();

         x = textElement.x;
         y = textElement.y;
      }
   }

   @Override
   public void endElement() throws InvalidFormatException
   {
      if (textElement == null) return;

      super.endElement();

      textElement.x = x;

      if (objects.isEmpty())
      {// do nothing
      }
      else if (objects.size() == 1)
      {
         JDRCompleteObject obj = objects.firstElement();

         String desc = obj.getDescription();

         if (desc == null || desc.isEmpty())
         {
            obj.setDescription(objects.getDescription());
         }

         textElement.append(obj);
      }
      else
      {
         textElement.append(objects);
      }
   }

   @Override
   public Object clone()
   {
      SVGTspanElement element = new SVGTspanElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
   {
      return null;
   }

   SVGTextElement textElement;
}
