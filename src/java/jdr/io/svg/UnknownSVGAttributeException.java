package com.dickimawbooks.jdr.io.svg;

import com.dickimawbooks.jdr.exceptions.*;

public class UnknownSVGAttributeException extends InvalidFormatException
{
   public UnknownSVGAttributeException(String name)
   {
      super("Unknown SVG attribute '"+name+"'");
   }
}
