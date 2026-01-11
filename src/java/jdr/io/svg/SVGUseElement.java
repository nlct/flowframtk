package com.dickimawbooks.jdr.io.svg;

import java.util.Enumeration;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGUseElement extends SVGAbstractElement
{
   public SVGUseElement(SVGHandler handler, 
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   @Override
   public String getName()
   {
      return "use";
   }

   @Override
   protected void addAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.addAttributes(uri, attr);

      addAttribute("x", attr);
      addAttribute("y", attr);

      String ref = getHref();

      if (ref == null)
      {
         throw new InvalidFormatException(
            "No href found for '"+getName()+"' element");
      }

      Matcher m = REF_PATTERN.matcher(ref);

      if (m.matches())
      {
         ref = m.group(1);
      }
      else
      {
         throw new InvalidFormatException("Can't parse href '"+ref+"'");
      }

      xAttr = getLengthAttribute("x");
      yAttr = getLengthAttribute("y");

      element = getRefElement(ref);

      if (element == null)
      {
         throw new InvalidFormatException("Can't find element with id '"+ref+"'");
      }

      element = (SVGAbstractElement)element.clone();

      element.attributeSet.removeAttribute("id");

      element.addAttributes(uri, attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String style)
     throws InvalidFormatException
   {
      SVGAttribute attr;

      if (name.equals("x"))
      {
         attr = new SVGLengthAttribute(handler, name, style, true);
      }
      else if (name.equals("y"))
      {
         attr = new SVGLengthAttribute(handler, name, style, false);
      }
      else
      {
         attr = super.createElementAttribute(name, style);
      }

      return attr;
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
      throws InvalidFormatException
   {
      if (element == null) return null;

      if (title != null && !title.isEmpty())
      {
         element.setTitle(title);
      }

      if (description != null && !description.isEmpty())
      {
         element.setDescription(description);
      }

      JDRCompleteObject newObject = element.addToImage(group);

      if (newObject != null)
      {
         applyElementAttributes(newObject);

         if (xAttr != null || yAttr != null)
         {
            double x = 0;
            double y = 0;

            if (xAttr != null)
            {
               x = xAttr.getStorageValue(element, true);
            }

            if (yAttr != null)
            {
               y = yAttr.getStorageValue(element, false);
            }

            newObject.translate(x, y);
         }
      }

      return newObject;
   }

   protected void applyElementAttributes(JDRCompleteObject object)
   throws InvalidFormatException
   {
      if (object instanceof JDRGroup)
      {
         for (int i = 0; i < ((JDRGroup)object).size(); i++)
         {
            applyElementAttributes(((JDRGroup)object).get(i));
         }
      }
      else
      {
         if (object instanceof JDRShape)
         {
            applyShapeAttributes((JDRShape)object);
         }

         if (object instanceof JDRTextual)
         {
            applyTextAttributes((JDRTextual)object);
         }
      }
   }

   public Object clone()
   {
      try
      {
         SVGUseElement element = new SVGUseElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   @Override
   public void setDescription(String text)
   {
      description = text;
   }

   @Override
   public void setTitle(String text)
   {
      title = text;
   }

   String description = null, title = null;

   SVGAbstractElement element;
   SVGLength xAttr, yAttr;

   private static final Pattern REF_PATTERN 
      = Pattern.compile("\\s*#([^#]+)\\s*");
}
