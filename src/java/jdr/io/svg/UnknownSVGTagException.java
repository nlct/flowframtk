package com.dickimawbooks.jdr.io.svg;

import com.dickimawbooks.jdr.exceptions.*;

public class UnknownSVGTagException extends InvalidFormatException
{
   public UnknownSVGTagException(String tagName)
   {
      super("Unknown SVG tag '"+tagName+"'");
   }
}
