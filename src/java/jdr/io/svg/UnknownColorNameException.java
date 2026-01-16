package com.dickimawbooks.jdr.io.svg;

public class UnknownColorNameException extends SVGException
{
   public UnknownColorNameException(SVGHandler h, String name)
   {
      super(h, String.format("Unknown color name: %s", name));

      attributeName = name;
   }

   public UnknownColorNameException(SVGAbstractElement elem, String name)
   {
      super(elem, String.format("Unknown color name: %s", name));

      attributeName = name;
   }

   public UnknownColorNameException(SVGHandler h, String name,
     Throwable cause)
   {
      super(h, String.format("Unknown color name: %s", name), cause);

      attributeName = name;
   }

   public UnknownColorNameException(SVGAbstractElement elem, String name,
     Throwable cause)
   {
      super(elem, String.format("Unknown color name: %s", name), cause);

      attributeName = name;
   }

   public String getColorName()
   {
      return attributeName;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.unknown_color_name",
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
