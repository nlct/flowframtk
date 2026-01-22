package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
   
import com.dickimawbooks.jdr.exceptions.*;

public class XHTMLParElement extends XHTMLAbstractElement
{
   public XHTMLParElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super("p", true, handler, parent);
   }

   @Override
   protected CharSequence getLaTeXContent()
   {
      return contents;
   }

   @Override
   public Object clone()
   {
      XHTMLParElement elem = new XHTMLParElement(handler, parent);
      elem.makeEqual(this);
      return elem;
   }
}
