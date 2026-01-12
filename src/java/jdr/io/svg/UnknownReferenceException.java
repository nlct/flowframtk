package com.dickimawbooks.jdr.io.svg;

public class UnknownReferenceException extends SVGException
{
   public UnknownReferenceException(SVGHandler h, String name)
   {
      super(h, String.format( "Unknown reference: %s", name));

      ref = name;
   }

   public UnknownReferenceException(SVGHandler h, String name,
     Throwable cause)
   {
      super(h, String.format( "Unknown reference: %s", name), cause);

      ref = name;
   }

   public UnknownReferenceException(SVGAbstractElement elem, String name)
   {
      super(elem, String.format( "Unknown reference: %s", name));

      ref = name;
   }

   public UnknownReferenceException(SVGAbstractElement elem, String name,
     Throwable cause)
   {
      super(elem, String.format( "Unknown reference: %s", name), cause);

      ref = name;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.unknown_ref",
        "unknown reference: {0}",
         ref);

      if (element != null)
      {
         msg = handler.getMessageWithFallback(
          "error.svg.element_msg_prefix",
          "Element {0}: {1}",
           element.getName(), msg);
      }

      return msg;
   }

   String ref;
}
