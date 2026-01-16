package com.dickimawbooks.jdr.io.svg;

public class InvalidUnitException extends SVGException
{
   public InvalidUnitException(SVGHandler h, String attrName, String name)
   {
      super(h, String.format( "Invalid %s unit: %s", attrName, name));

      this.attrName = attrName;
      unitName = name;
   }

   public InvalidUnitException(SVGAbstractElement elem, String attrName, String name)
   {
      super(elem, String.format( "Invalid %s unit: %s", attrName, name));

      this.attrName = attrName;
      unitName = name;
   }

   public InvalidUnitException(SVGHandler h, String attrName, String name,
     Throwable cause)
   {
      super(h, String.format( "Invalid %s unit: %s", attrName, name), cause);

      this.attrName = attrName;
      unitName = name;
   }

   public InvalidUnitException(SVGAbstractElement elem, String attrName, String name,
     Throwable cause)
   {
      super(elem, String.format( "Invalid %s unit: %s", attrName, name), cause);

      this.attrName = attrName;
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
        "error.svg.invalid_unit",
        "invalid {0} unit: {1}",
         attrName, unitName);

      if (element != null)
      {
         msg = handler.getMessageWithFallback(
          "error.svg.element_msg_prefix",
          "Element {0}: {1}",
           element.getName(), msg);
      }

      return msg;
   }

   String attrName;
   String unitName;
}
