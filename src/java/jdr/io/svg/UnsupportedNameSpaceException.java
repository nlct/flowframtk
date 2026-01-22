package com.dickimawbooks.jdr.io.svg;

public class UnsupportedNameSpaceException extends SVGException
{
   public UnsupportedNameSpaceException(SVGHandler h, String name)
   {
      super(h, String.format("Unsupported name space: %s", name));

      nameSpace = name;
   }

   public UnsupportedNameSpaceException(SVGAbstractElement elem, String name)
   {
      super(elem, String.format("Unsupported name space: %s", name));

      nameSpace = name;
   }

   public UnsupportedNameSpaceException(SVGHandler h, String name,
     Throwable cause)
   {
      super(h, String.format("Unsupported name space: %s", name), cause);

      nameSpace = name;
   }

   public UnsupportedNameSpaceException(SVGAbstractElement elem, String name,
     Throwable cause)
   {
      super(elem, String.format("Unsupported name space: %s", name), cause);

      nameSpace = name;
   }

   public String getAttributeName()
   {
      return nameSpace;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.unsupported_namespace",
        "unsupported name space: {0}",
         nameSpace);

      if (element != null)
      {
         msg = handler.getMessageWithFallback(
          "error.svg.element_msg_prefix",
          "Element {0}: {1}",
           element.getName(), msg);
      }

      return msg;
   }

   String nameSpace;
}
