package com.dickimawbooks.jdr.io.svg;

public class ExternalRefUnsupportedException extends SVGException
{
   public ExternalRefUnsupportedException(SVGHandler h, String value)
   {
      super(h, String.format( "external references unsupported (%s)", value));

      ref = value;
   }

   public ExternalRefUnsupportedException(SVGHandler h, String value,
     Throwable cause)
   {
      super(h, String.format( "external references unsupported (%s)", value), cause);

      ref = value;
   }

   public ExternalRefUnsupportedException(SVGAbstractElement elem, String value)
   {
      super(elem, String.format( "external references unsupported (%s)", value));

      ref = value;
   }

   public ExternalRefUnsupportedException(SVGAbstractElement elem, String value,
     Throwable cause)
   {
      super(elem, String.format( "external references unsupported (%s)", value), cause);

      ref = value;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.external_ref_unsupported",
        "external references unsupported ({0})",
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
