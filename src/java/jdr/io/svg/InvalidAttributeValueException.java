package com.dickimawbooks.jdr.io.svg;

public class InvalidAttributeValueException extends SVGException
{
   public InvalidAttributeValueException(SVGHandler h,
       String name, Object value)
   {
      super(h, String.format("Invalid SVG %s value: %s", name, value));

      attributeName = name;
      attributeValue = value;
   }

   public InvalidAttributeValueException(SVGAbstractElement elem,
       String name, Object value)
   {
      super(elem, String.format("Invalid SVG %s value: %s", name, value));

      attributeName = name;
      attributeValue = value;
   }

   public InvalidAttributeValueException(SVGHandler h, String name,
     Object value, Throwable cause)
   {
      super(h, String.format("Invalid SVG %s value: %s", name, value), cause);

      attributeName = name;
      attributeValue = value;
   }

   public InvalidAttributeValueException(SVGAbstractElement elem, String name,
     Object value, Throwable cause)
   {
      super(elem, String.format("Invalid SVG %s value: %s", name, value), cause);

      attributeName = name;
      attributeValue = value;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.invalid_attribute_value",
        "invalid {0} value ''{1}''",
         attributeName, attributeValue);

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
   Object attributeValue;
}
