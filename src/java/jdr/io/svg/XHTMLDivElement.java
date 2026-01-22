package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
   
import com.dickimawbooks.jdr.exceptions.*;

public class XHTMLDivElement extends XHTMLAbstractElement
{
   public XHTMLDivElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super("div", true, handler, parent);
   }

   @Override
   protected CharSequence getLaTeXContent()
   {
      return contents;
   }

   @Override
   public Object clone()
   {
      XHTMLDivElement elem = new XHTMLDivElement(handler, parent);
      elem.makeEqual(this);
      return elem;
   }
}
