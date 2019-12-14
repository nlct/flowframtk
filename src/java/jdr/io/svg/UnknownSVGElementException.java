package com.dickimawbooks.jdr.io.svg;

import com.dickimawbooks.jdr.exceptions.*;

public class UnknownSVGElementException extends InvalidFormatException
{
   public UnknownSVGElementException(String elementName)
   {
      super("Unknown SVG element '"+elementName+"'");
   }
}
