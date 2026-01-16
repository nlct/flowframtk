package com.dickimawbooks.jdr.io.svg;

public class CantParseMeasurementException extends SVGException
{
   public CantParseMeasurementException(SVGHandler h, String value)
   {
      super(h, String.format("Can't parse measurement: %s", value));

      valueString = value;
   }

   public CantParseMeasurementException(SVGAbstractElement elem,
       String value)
   {
      super(elem, String.format("Can't parse measurement: %s", value));

      valueString = value;
   }

   public CantParseMeasurementException(SVGHandler h, String value, Throwable cause)
   {
      super(h, String.format("Can't parse measurement: %s", value), cause);

      valueString = value;
   }

   public CantParseMeasurementException(SVGAbstractElement elem, String value,
      Throwable cause)
   {
      super(elem, String.format("Can't parse measurement: %s", value), cause);

      valueString = value;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.cant_parse_measurement",
        "can''t parse measurement ''{1}''",
         valueString);

      if (element != null)
      {
         msg = handler.getMessageWithFallback(
           "error.svg.element_msg_prefix",
           "Element {0}: {1}",
            element.getName(), msg);
      }

      return msg;
   }

   String valueString;
}
