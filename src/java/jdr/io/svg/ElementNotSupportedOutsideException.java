package com.dickimawbooks.jdr.io.svg;

public class ElementNotSupportedOutsideException extends SVGException
{
   public ElementNotSupportedOutsideException(SVGHandler h, String parentElement)
   {
      super(h, String.format("not supported outside of element %s", parentElement));

      this.parentElement = parentElement;
   }

   public ElementNotSupportedOutsideException(SVGHandler h, String parentElement,
     Throwable cause)
   {
      super(h, String.format("not supported outside of element %s", parentElement), cause);

      this.parentElement = parentElement;
   }

   public ElementNotSupportedOutsideException(SVGAbstractElement elem, String parentElement)
   {
      super(elem, String.format("not supported outside of element %s", parentElement));

      this.parentElement = parentElement;
   }

   public ElementNotSupportedOutsideException(SVGAbstractElement elem, String parentElement,
     Throwable cause)
   {
      super(elem, String.format("not supported outside of element %s", parentElement), cause);

      this.parentElement = parentElement;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.element_not_supported_outside",
        "not supported outside of element ''{0}''",
         parentElement);

      if (element != null)
      {
         msg = handler.getMessageWithFallback(
          "error.svg.element_msg_prefix",
          "Element {0}: {1}",
           element.getName(), msg);
      }

      return msg;
   }

   String parentElement;
}
