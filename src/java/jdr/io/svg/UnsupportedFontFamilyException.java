package com.dickimawbooks.jdr.io.svg;

public class UnsupportedFontFamilyException extends SVGException
{
   public UnsupportedFontFamilyException(SVGHandler h, String name)
   {
      super(h, String.format( "No font family available for: %s", name));

      valueString = name;
   }

   public UnsupportedFontFamilyException(SVGAbstractElement elem, String name)
   {
      super(elem, String.format( "No font family available for: %s", name));

      valueString = name;
   }

   public UnsupportedFontFamilyException(SVGHandler h, String name,
     Throwable cause)
   {
      super(h, String.format( "No font family available for: %s", name), cause);

      valueString = name;
   }

   public UnsupportedFontFamilyException(SVGAbstractElement elem, String name,
     Throwable cause)
   {
      super(elem, String.format( "No font family available for: %s", name), cause);

      valueString = name;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.unknown_font_family",
        "no font family available for: {0}",
         valueString);

      if (element != null)
      {
         msg = handler.getMessageWithFallback(
          "error.svg.element_msg_prefix",
          "Element {0}: {1}",
           element.getName(), msg);
      }

      return msg;
   }

   String valueString;
}
