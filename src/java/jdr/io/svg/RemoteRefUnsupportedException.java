package com.dickimawbooks.jdr.io.svg;

public class RemoteRefUnsupportedException extends SVGException
{
   public RemoteRefUnsupportedException(SVGHandler h, String value)
   {
      super(h, String.format( "remote references unsupported (%s)", value));

      ref = value;
   }

   public RemoteRefUnsupportedException(SVGHandler h, String value,
     Throwable cause)
   {
      super(h, String.format( "remote references unsupported (%s)", value), cause);

      ref = value;
   }

   public RemoteRefUnsupportedException(SVGAbstractElement elem, String value)
   {
      super(elem, String.format( "remote references unsupported (%s)", value));

      ref = value;
   }

   public RemoteRefUnsupportedException(SVGAbstractElement elem, String value,
     Throwable cause)
   {
      super(elem, String.format( "remote references unsupported (%s)", value), cause);

      ref = value;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.remote_ref_unsupported",
        "remote references unsupported ({0})",
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
