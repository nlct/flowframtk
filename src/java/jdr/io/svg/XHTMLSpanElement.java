package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
   
import com.dickimawbooks.jdr.exceptions.*;

public class XHTMLSpanElement extends XHTMLAbstractElement
{
   public XHTMLSpanElement(SVGHandler handler, SVGAbstractElement parent)
   {
      this("span", handler, parent);
   }

   protected XHTMLSpanElement(String name, SVGHandler handler, SVGAbstractElement parent)
   {
      super(name, false, handler, parent);
   }

   public String getTextBlockCommand()
   {
      return null;
   }

   @Override
   protected CharSequence getLaTeXContent()
   {
      String cmd = getTextBlockCommand();

      if (cmd == null)
      {
         return contents;
      }
      else
      {
         return cmd + "{" + contents + "}";
      }
   }

   @Override
   public Object clone()
   {
      XHTMLSpanElement elem = new XHTMLSpanElement(handler, parent);
      elem.makeEqual(this);
      return elem;
   }
}
