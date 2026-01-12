package com.dickimawbooks.jdr.io.svg;

import java.util.Enumeration;
import java.util.regex.*;

import java.net.URI;
import java.net.URISyntaxException;

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
      addAttribute("width", attr);
      addAttribute("height", attr);

      String ref = getHref();

      if (ref == null)
      {
         throw new ElementMissingAttributeException(this, "href");
      }

      try
      {
         URI uriRef = new URI(ref);
         String path = uriRef.getPath();

         if (path != null && !path.isEmpty())
         {
            throw new ExternalRefUnsupportedException(this, ref);
         }

         ref = uriRef.getFragment();
      }
      catch (URISyntaxException e)
      {
         throw new InvalidAttributeValueException(this, "href", ref, e);
      }

      widthAttr = getLengthAttribute("width");
      heightAttr = getLengthAttribute("height");

      if (widthAttr != null && widthAttr.getValue() == null)
      {
         widthAttr = null;
      }

      if (heightAttr != null && heightAttr.getValue() == null)
      {
         heightAttr = null;
      }

      xAttr = getLengthAttribute("x");
      yAttr = getLengthAttribute("y");

      element = getRefElement(ref);

      if (element == null)
      {
         throw new UnknownReferenceException(element, ref);
      }

      element = (SVGAbstractElement)element.clone();

      element.attributeSet.removeAttribute("id");

      element.addAttributes(uri, attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws InvalidFormatException
   {
      SVGAttribute attr;

      if (name.equals("x"))
      {
         attr = new SVGLengthAttribute(handler, name, value, true);
      }
      else if (name.equals("y"))
      {
         attr = new SVGLengthAttribute(handler, name, value, false);
      }
      else if (name.equals("width"))
      {
         attr = new SVGLengthAttribute(handler, name, value, true);
      }
      else if (name.equals("height"))
      {
         attr = new SVGLengthAttribute(handler, name, value, false);
      }
      else
      {
         attr = super.createElementAttribute(name, value);
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

   @Override
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

   public void makeEqual(SVGUseElement element)
   {
      super.makeEqual(element);

      widthAttr = element.widthAttr;
      heightAttr = element.heightAttr;
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

   @Override
   public double getViewportWidth()
   {
      if (widthAttr == null)
      {
         return super.getViewportWidth();
      }

      return widthAttr.getStorageValue(parent, true);
   }

   @Override
   public double getViewportHeight()
   {
      if (heightAttr == null)
      {
         return super.getViewportHeight();
      }

      return heightAttr.getStorageValue(parent, false);
   }

   String description = null, title = null;

   SVGAbstractElement element;
   SVGLength xAttr, yAttr;

   private SVGLengthAttribute widthAttr, heightAttr;
}
