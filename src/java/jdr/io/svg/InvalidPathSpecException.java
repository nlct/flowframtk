package com.dickimawbooks.jdr.io.svg;

public class InvalidPathSpecException extends SVGException
{
   public InvalidPathSpecException(SVGHandler h,
       String name, char command, String expected)
   {
      super(h, String.format("Invalid path data %s for %c (expected %s)",
          name, command, expected));

      attributeName = name;
      this.command = command;
      this.expected = expected;
   }

   public InvalidPathSpecException(SVGAbstractElement elem,
       String name, char command, String expected)
   {
      super(elem, String.format("Invalid path data %s for %c (expected %s)",
          name, command, expected));

      attributeName = name;
      this.command = command;
      this.expected = expected;
   }

   public InvalidPathSpecException(SVGHandler h, String name,
     char command, String expected, Throwable cause)
   {
      super(h, String.format("Invalid path data %s for %c (expected %s)",
          name, command, expected), cause);

      attributeName = name;
      this.command = command;
      this.expected = expected;
   }

   public InvalidPathSpecException(SVGAbstractElement elem, String name,
     char command, String expected, Throwable cause)
   {
      super(elem, String.format("Invalid path data %s for %c (expected %s)",
          name, command, expected), cause);

      attributeName = name;
      this.command = command;
      this.expected = expected;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.path_data_missing_coord",
        "path data specified by {0} missing one or more co-ordinates for command ''{1}'' (expected {2})",
         attributeName, command, expected);

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
   char command;
   String expected;
}
