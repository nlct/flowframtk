package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
   
import com.dickimawbooks.jdr.exceptions.*;

public class XHTMLStrongElement extends XHTMLSpanElement
{
   public XHTMLStrongElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super("strong", handler, parent);
   }

   @Override
   public String getTextBlockCommand()
   {
      return "\\strong";
   }

   @Override
   public Object clone()
   {
      XHTMLStrongElement elem = new XHTMLStrongElement(handler, parent);
      elem.makeEqual(this);
      return elem;
   }
}
