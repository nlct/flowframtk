package com.dickimawbooks.jdr.io.svg;

public class UnknownAttributeException extends SVGException
{
   public UnknownAttributeException(SVGHandler h, String name)
   {
      super(h, String.format( "Unknown SVG attribute: %s", name));

      attributeName = name;
   }

   public UnknownAttributeException(SVGAbstractElement elem, String name)
   {
      super(elem, String.format( "Unknown SVG attribute: %s", name));

      attributeName = name;
   }

   public UnknownAttributeException(SVGHandler h, String name,
     Throwable cause)
   {
      super(h, String.format( "Unknown SVG attribute: %s", name), cause);

      attributeName = name;
   }

   public UnknownAttributeException(SVGAbstractElement elem, String name,
     Throwable cause)
   {
      super(elem, String.format( "Unknown SVG attribute: %s", name), cause);

      attributeName = name;
   }

   public String getAttributeName()
   {
      return attributeName;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.unknown_attribute",
        "unknown attribute: {0}",
         attributeName);

      if (element != null)
      {
         msg = handler.getMessageWithFallback(
          "error.svg.element_msg_prefix",
          "Element {0}: {1}",
           element.getName(), msg);
      }

      return msg;
   }

   String attributeName;
}
