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

   protected void applyAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.applyAttributes(uri, attr);

      addAttribute("x", attr);
      addAttribute("y", attr);
      addAttribute("width", attr);
      addAttribute("height", attr);

      addAttribute("cx", attr);
      addAttribute("cy", attr);
      addAttribute("r", attr);
      addAttribute("d", attr);
      addAttribute("x1", attr);
      addAttribute("y1", attr);
      addAttribute("x2", attr);
      addAttribute("y2", attr);

      applyShapeAttributes(uri, attr);
      applyTextAttributes(uri, attr);

      if (getHref() == null)
      {
         throw new InvalidFormatException(
            "No href found for '"+getName()+"' element");
      }
   }

   @Override
   public String getName()
   {
      return "use";
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
      throws InvalidFormatException
   {
      String ref = getHref();

      Matcher m = REF_PATTERN.matcher(ref);

      if (m.matches())
      {
         ref = m.group(1);
      }
      else
      {
         throw new InvalidFormatException("Can't parse href '"+ref+"'");
      }

      SVGAbstractElement element = getRefElement(ref);

      if (element == null)
      {
         throw new InvalidFormatException("Can't find element with id '"+ref+"'");
      }

      element = (SVGAbstractElement)element.clone();

      element.attributeSet.removeAttribute("id");

      SVGAttributeSet newAttrs = (SVGAttributeSet)attributeSet.copyAttributes();

      newAttrs.removeAttribute("href");
      newAttrs.removeAttribute("xlink:href");

      SVGLengthAttribute xAttr = (SVGLengthAttribute)newAttrs.getAttribute("x");

      if (xAttr != null)
      {
         newAttrs.removeAttribute("x");
      }

      SVGLengthAttribute yAttr = (SVGLengthAttribute)newAttrs.getAttribute("y");

      if (yAttr != null)
      {
         newAttrs.removeAttribute("y");
      }

      element.attributeSet.addAttributes(newAttrs);

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
         if (xAttr != null || yAttr != null)
         {
            double x = 0;
            double y = 0;

            JDRUnit unit = group.getCanvasGraphics().getStorageUnit();

            if (xAttr != null)
            {
               x = xAttr.lengthValue(element).getValue(unit);
            }

            if (yAttr != null)
            {
               y = yAttr.lengthValue(element).getValue(unit);
            }

            newObject.translate(x, y);
         }
      }

      return newObject;
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

   private static final Pattern REF_PATTERN 
      = Pattern.compile("\\s*#([^#]+)\\s*");
}
