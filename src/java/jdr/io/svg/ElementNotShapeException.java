package com.dickimawbooks.jdr.io.svg;

public class ElementNotShapeException extends SVGException
{
   public ElementNotShapeException(SVGHandler h, String ref,
     String refElementName)
   {
      super(h, String.format("shape expected in reference ''%s'' but found element %s", ref, refElementName));

      this.ref = ref;
      this.refElementName = refElementName;
   }

   public ElementNotShapeException(SVGHandler h, String ref,
     String refElementName, Throwable cause)
   {
      super(h, String.format("shape expected in reference ''%s'' but found element %s", ref, refElementName), cause);

      this.ref = ref;
      this.refElementName = refElementName;
   }

   public ElementNotShapeException(SVGAbstractElement elem, String ref,
     String refElementName)
   {
      super(elem, String.format("shape expected in reference ''%s'' but found element %s", ref, refElementName));

      this.ref = ref;
      this.refElementName = refElementName;
   }

   public ElementNotShapeException(SVGAbstractElement elem, String ref,
     String refElementName, Throwable cause)
   {
      super(elem, String.format("shape expected in reference ''%s'' but found element %s", ref, refElementName), cause);

      this.ref = ref;
      this.refElementName = refElementName;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.shape_expected",
        "shape expected in reference ''{0}'' but found element {1}",
         ref, refElementName);

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
   String refElementName;
}
