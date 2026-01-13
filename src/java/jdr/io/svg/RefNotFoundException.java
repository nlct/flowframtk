package com.dickimawbooks.jdr.io.svg;

import java.io.File;
import java.net.URI;

public class RefNotFoundException extends SVGException
{
   public RefNotFoundException(SVGHandler h, URI uriRef, File file)
   {
      super(h, String.format("reference %s not found (%s)", uriRef, file));

      this.uriRef = uriRef;
      this.file = file;
   }

   public RefNotFoundException(SVGHandler h, URI uriRef, File file,
     Throwable cause)
   {
      super(h, String.format("reference %s not found (%s)", uriRef, file), cause);

      this.uriRef = uriRef;
      this.file = file;
   }

   public RefNotFoundException(SVGAbstractElement elem, URI uriRef, File file)
   {
      super(elem, String.format("reference %s not found (%s)", uriRef, file));

      this.uriRef = uriRef;
      this.file = file;
   }

   public RefNotFoundException(SVGAbstractElement elem, URI uriRef, File file,
     Throwable cause)
   {
      super(elem, String.format("reference %s not found (%s)", uriRef, file), cause);

      this.uriRef = uriRef;
      this.file = file;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.ref_not_found",
        "reference {0} not found ({1})",
         uriRef, file);

      if (element != null)
      {
         msg = handler.getMessageWithFallback(
          "error.svg.element_msg_prefix",
          "Element {0}: {1}",
           element.getName(), msg);
      }

      return msg;
   }

   URI uriRef;
   File file;
}
