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
   public SVGUseElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "use";
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("x", attr);
      addAttribute("y", attr);
      addAttribute("width", attr);
      addAttribute("height", attr);

      elemUri = uri;
      elemAttrs = attr;
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
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

      element.addAttributes(elemUri, elemAttrs);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("x"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("y"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, false);
      }
      else if (name.equals("width"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("height"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, false);
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
      SVGUseElement element = new SVGUseElement(handler, null);

      element.makeEqual(this);

      return element;
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
   SVGLengthAttribute xAttr, yAttr;
   String elemUri;
   Attributes elemAttrs;

   private SVGLengthAttribute widthAttr, heightAttr;
}
