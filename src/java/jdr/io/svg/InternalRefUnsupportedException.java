package com.dickimawbooks.jdr.io.svg;

public class InternalRefUnsupportedException extends SVGException
{
   public InternalRefUnsupportedException(SVGHandler h, String value)
   {
      super(h, String.format( "internal references unsupported (%s)", value));

      ref = value;
   }

   public InternalRefUnsupportedException(SVGHandler h, String value,
     Throwable cause)
   {
      super(h, String.format( "internal references unsupported (%s)", value), cause);

      ref = value;
   }

   public InternalRefUnsupportedException(SVGAbstractElement elem, String value)
   {
      super(elem, String.format( "internal references unsupported (%s)", value));

      ref = value;
   }

   public InternalRefUnsupportedException(SVGAbstractElement elem, String value,
     Throwable cause)
   {
      super(elem, String.format( "internal references unsupported (%s)", value), cause);

      ref = value;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.internal_ref_unsupported",
        "internal references unsupported ({0})",
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
