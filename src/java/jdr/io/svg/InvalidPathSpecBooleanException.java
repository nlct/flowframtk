package com.dickimawbooks.jdr.io.svg;

public class InvalidPathSpecBooleanException extends SVGException
{
   public InvalidPathSpecBooleanException(SVGHandler h,
       String name, String value)
   {
      super(h, String.format("Invalid path data %s expected boolean (found %s)",
          name, value));

      attributeName = name;
      this.value = value;
   }

   public InvalidPathSpecBooleanException(SVGAbstractElement elem,
       String name, String value)
   {
      super(elem, String.format("Invalid path data %s for %c (expected %s)",
          name, value));

      attributeName = name;
      this.value = value;
   }

   public InvalidPathSpecBooleanException(SVGHandler h, String name,
     String value, Throwable cause)
   {
      super(h, String.format("Invalid path data %s for %c (expected %s)",
          name, value), cause);

      attributeName = name;
      this.value = value;
   }

   public InvalidPathSpecBooleanException(SVGAbstractElement elem, String name,
     String value, Throwable cause)
   {
      super(elem, String.format("Invalid path data %s for %c (expected %s)",
          name, value), cause);

      attributeName = name;
      this.value = value;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.path_data_boolean_expected",
        "path data specified by {0}: boolean 0 or 1 expected (found {1})",
         attributeName, value);

      if (element != null)
      {
         msg = handler.getMessageWithFallback(
           "error.svg.element_msg_prefix",
           "Element {0}: {1}",
            element.getName(), msg);
      }

      return msg;
   }

   String attributeName;
   String value;
}
