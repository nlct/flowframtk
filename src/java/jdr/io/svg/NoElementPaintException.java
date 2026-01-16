package com.dickimawbooks.jdr.io.svg;

public class NoElementPaintException extends SVGException
{
   public NoElementPaintException(SVGAbstractElement elem)
   {
      super(elem, String.format("No paint for element: %s", elem.getName()));
   }

   public NoElementPaintException(SVGAbstractElement elem,
     Throwable cause)
   {
      super(elem, String.format("No paint for element: %s", elem.getName()), cause);
   }

   @Override
   public String getLocalizedMessage()
   {
      String id = element.getId();

      String msg;

      if (id == null)
      {
         msg = handler.getMessageWithFallback(
              "error.svg.no_associated_paint",
              "no associated paint");
      }
      else
      {
         msg = handler.getMessageWithFallback(
              "error.svg.no_associated_paint_for_id",
              "no associated paint for id {0}", id);
      }

      return handler.getMessageWithFallback(
           "error.svg.element_msg_prefix",
           "Element {0}: {1}",
            element.getName(), msg);
   }

}
