package com.dickimawbooks.jdr.io.svg;

public class UnknownUnitException extends SVGException
{
   public UnknownUnitException(SVGHandler h, String name)
   {
      super(h, String.format( "Unknown SVG unit: %s", name));

      unitName = name;
   }

   public UnknownUnitException(SVGAbstractElement elem, String name)
   {
      super(elem, String.format( "Unknown SVG unit: %s", name));

      unitName = name;
   }

   public UnknownUnitException(SVGHandler h, String name,
     Throwable cause)
   {
      super(h, String.format( "Unknown SVG unit: %s", name), cause);

      unitName = name;
   }

   public UnknownUnitException(SVGAbstractElement elem, String name,
     Throwable cause)
   {
      super(elem, String.format( "Unknown SVG unit: %s", name), cause);

      unitName = name;
   }

   public String getUnitName()
   {
      return unitName;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.unknown_unit",
        "unknown unit: {0}",
         unitName);

      if (element != null)
      {
         msg = handler.getMessageWithFallback(
          "error.svg.element_msg_prefix",
          "Element {0}: {1}",
           element.getName(), msg);
      }

      return msg;
   }

   String unitName;
}
