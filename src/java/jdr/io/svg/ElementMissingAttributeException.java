package com.dickimawbooks.jdr.io.svg;

public class ElementMissingAttributeException extends SVGException
{
   public ElementMissingAttributeException(SVGAbstractElement elem,
       String attributeName)
   {
      super(elem, String.format("SVG element %s missing attribute: %s",
         elem.getName(), attributeName));

      this.attributeName = attributeName;
   }

   public ElementMissingAttributeException(SVGAbstractElement elem,
     String attributeName, Throwable cause)
   {
      super(elem, String.format("SVG element %s missing attribute: %s",
         elem.getName(), attributeName), cause);

      this.attributeName = attributeName;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.element_missing_attribute",
        "missing attribute: {0}",
         attributeName);

      return handler.getMessageWithFallback(
        "error.svg.element_msg_prefix",
        "Element {0}: {1}",
         element.getName(), msg);
   }

   String attributeName;
}
