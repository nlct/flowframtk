package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

public abstract class XHTMLAbstractElement extends SVGForeignObjectElement
{
   protected XHTMLAbstractElement(String name, boolean isBlock, SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
      this.name = name;
      this.isBlock = isBlock;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      super.startElement();

      foreignObject = getForeignObjectAncestor();

      if (foreignObject == null)
      {
         throw new ElementNotInsideException(this, "foreignObject");
      }
   }

   protected abstract CharSequence getLaTeXContent();

   @Override
   public void endElement() throws InvalidFormatException
   {
      super.endElement();

      if (foreignObject != null)
      {
         if (isBlock)
         {
            appendParToFrameContent();
         }

         CharSequence cseq = getLaTeXContent();

         if (cseq != null)
         {
            foreignObject.appendToFrameContent(cseq, containsVerb);

            if (isBlock)
            {
               appendParToFrameContent();
            }
         }
      }
   }

   public void makeEqual(XHTMLAbstractElement other)
   {
      super.makeEqual(other);
      isBlock = other.isBlock;
      foreignObject = other.foreignObject;
   }

   private String name;
   protected boolean isBlock;
   SVGForeignObjectElement foreignObject;
}
