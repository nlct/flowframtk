package com.dickimawbooks.jdr.io.svg;

public class MissingElementException extends SVGException
{
   public MissingElementException(SVGHandler h, String name)
   {
      super(h, String.format( "Missing SVG element: %s", name));
      elementName = name;
   }

   public MissingElementException(SVGHandler h, String name,
     Throwable cause)
   {
      super(h, String.format( "Missing SVG element: %s", name), cause);
      elementName = name;
   }

   public String getElementName()
   {
      return elementName;
   }

   @Override
   public String getLocalizedMessage()
   {
      return handler.getMessageWithFallback(
        "error.svg.missing_element",
        "missing element: {0}",
         elementName);
   }

   String elementName;
}
