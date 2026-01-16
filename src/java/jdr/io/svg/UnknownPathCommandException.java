package com.dickimawbooks.jdr.io.svg;

public class UnknownPathCommandException extends SVGException
{
   public UnknownPathCommandException(SVGHandler h,
       String name, char command)
   {
      super(h, String.format("Invalid path data %s unknown command %c",
          name, command));

      attributeName = name;
      this.command = command;
   }

   public UnknownPathCommandException(SVGAbstractElement elem,
       String name, char command)
   {
      super(elem, String.format("Invalid path data %s unknown command %c",
          name, command));

      attributeName = name;
      this.command = command;
   }

   public UnknownPathCommandException(SVGHandler h, String name,
     char command, Throwable cause)
   {
      super(h, String.format("Invalid path data %s unknown command %c",
          name, command), cause);

      attributeName = name;
      this.command = command;
   }

   public UnknownPathCommandException(SVGAbstractElement elem, String name,
     char command, Throwable cause)
   {
      super(elem, String.format("Invalid path data %s unknown command %c",
          name, command), cause);

      attributeName = name;
      this.command = command;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.path_data_unknown_command",
        "path data specified by {0}: command expected found ''{1}''",
         attributeName, command);

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
}
