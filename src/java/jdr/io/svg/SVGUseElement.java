package com.dickimawbooks.jdr.io.svg;

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

      if (getHref() == null)
      {
         throw new InvalidFormatException(
            "No xlink:href found for '"+getName()+"' element");
      }
   }

   public String getName()
   {
      return "use";
   }

   public void addToImage(JDRGroup group)
      throws InvalidFormatException
   {
      String ref = getHref();

      Matcher m = pattern.matcher(ref);

      if (m.matches())
      {
         ref = m.group(1);
      }
      else
      {
         throw new InvalidFormatException("Can't parse xlink:href '"+ref+"'");
      }

      SVGAbstractElement element = getRefElement(ref);

      if (element == null)
      {
         throw new InvalidFormatException("Can't find element with id '"+ref+"'");
      }

      element = (SVGAbstractElement)element.clone();

      element.attributeSet.removeAttribute("id");

      SVGAttribute attr = (SVGAttribute)getAttribute(element.getName(),
           "x", null);

      if (attr != null)
      {
         element.addAttribute((SVGAttribute)attr.clone());
      }

      attr = (SVGAttribute)getAttribute(element.getName(),
           "y", null);

      if (attr != null)
      {
         element.addAttribute((SVGAttribute)attr.clone());
      }

      attr = (SVGAttribute)getAttribute(element.getName(),
           "width", null);

      if (attr != null)
      {
         element.addAttribute((SVGAttribute)attr.clone());
      }

      attr = (SVGAttribute)getAttribute(element.getName(),
           "height", null);

      if (attr != null)
      {
         element.addAttribute((SVGAttribute)attr.clone());
      }

      attr = (SVGAttribute)getAttribute(element.getName(),
           "transform", null);

      if (attr != null)
      {
         element.addAttribute((SVGAttribute)attr.clone());
      }

      element.addToImage(group);
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

   private static final Pattern pattern 
      = Pattern.compile("\\s*#([^#]+)\\s*");
}
