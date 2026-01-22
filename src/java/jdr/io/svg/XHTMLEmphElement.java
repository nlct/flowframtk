package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
   
import com.dickimawbooks.jdr.exceptions.*;

public class XHTMLEmphElement extends XHTMLSpanElement
{
   public XHTMLEmphElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super("em", handler, parent);
   }

   @Override
   public String getTextBlockCommand()
   {
      return "\\emph";
   }

   @Override
   public Object clone()
   {
      XHTMLEmphElement elem = new XHTMLEmphElement(handler, parent);
      elem.makeEqual(this);
      return elem;
   }
}
