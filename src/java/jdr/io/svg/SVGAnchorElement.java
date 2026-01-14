package com.dickimawbooks.jdr.io.svg;

import java.net.URI;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGAnchorElement extends SVGTspanElement
{
   public SVGAnchorElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "a";
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

         StringBuilder buffer = new StringBuilder();

         String href = uriRef.toASCIIString();

         for (int i = 0; i < href.length(); i++)
         {
            char c = href.charAt(i);

            switch (c)
            {
               case '\\' :
               case '^' :
               case '~' :
               case '$' :
               case '_' :
               case '{' :
               case '}' :
               case ' ' :
                  buffer.append("\\%");
                  buffer.append(String.format("%02x", (int)c));
               break;
               case '&' :
               case '#' :
               case '%' :
                  buffer.append("\\");
                  buffer.appendCodePoint(c);
               break;
               default :
                  buffer.appendCodePoint(c);
            }
         }

         uriLaTeXRef = buffer.toString();
      }
      catch (Exception e)
      {
         throw new InvalidAttributeValueException(this, "href", ref, e);
      }

      super.startElement();
   }

   @Override
   protected void setLaTeXText(JDRText textArea)
   {
      handler.setLaTeXText(textArea);

      if (uriLaTeXRef != null && !uriLaTeXRef.isEmpty())
      {
         String ltext = textArea.getLaTeXText();

         if (ltext == null)
         {
            ltext = textArea.getText();
         }

         if (!ltext.isEmpty())
         {
            textArea.setLaTeXText(
              String.format("\\href{%s}{%s}", uriLaTeXRef, ltext));

            handler.getSVG().requirePackage("hyperref");
         }
      }
   }

   @Override
   public Object clone()
   {
      SVGAnchorElement element = new SVGAnchorElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   String uriLaTeXRef;
}
