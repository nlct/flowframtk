package com.dickimawbooks.jdr.io.svg;

public class UnknownElementException extends SVGException
{
   public UnknownElementException(SVGHandler h, String name)
   {
      super(h, String.format( "Unknown SVG element: %s", name));
      elementName = name;
   }

   public UnknownElementException(SVGHandler h, String name,
     Throwable cause)
   {
      super(h, String.format( "Unknown SVG element: %s", name), cause);
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
        "error.svg.unknown_element",
        "unknown element: {0}",
         elementName);
   }

   String elementName;
}
