package com.dickimawbooks.jdr.io.svg;

import com.dickimawbooks.jdr.exceptions.InvalidFormatException;

public class SVGException extends InvalidFormatException
{
   public SVGException(SVGHandler handler, String msg)
   {
      super(msg);

      this.handler = handler;
   }

   public SVGException(SVGHandler handler, String msg, Throwable cause)
   {
      super(msg, cause);

      this.handler = handler;
   }

   public SVGException(SVGAbstractElement element, String msg)
   {
      super(msg);

      this.element = element;
      this.handler = element.getHandler();
   }

   public SVGException(SVGAbstractElement element, String msg, Throwable cause)
   {
      super(msg, cause);

      this.element = element;
      this.handler = element.getHandler();
   }

   public void setElement(SVGAbstractElement element)
   {
      this.element = element;
   }

   public SVGAbstractElement getElement()
   {
      return element;
   }

   public SVGHandler getHandler()
   {
      return handler;
   }

   SVGHandler handler;
   SVGAbstractElement element;
}
