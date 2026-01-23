package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
   
import com.dickimawbooks.jdr.exceptions.*;

public class XHTMLPreElement extends XHTMLAbstractElement
{
   public XHTMLPreElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super("pre", true, handler, parent);
      containsVerb = true;
   }

   @Override
   protected CharSequence getLaTeXContent()
   {
      return contents;
   }

   @Override
   public boolean supportsParseMaths()
   {
      return false;
   }

   @Override
   public boolean isVerbatim()
   {
      return true;
   }

   @Override
   public Object clone()
   {
      XHTMLPreElement elem = new XHTMLPreElement(handler, parent);
      elem.makeEqual(this);
      return elem;
   }
}
