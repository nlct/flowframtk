package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGDefsElement extends SVGAbstractElement
{
   public SVGDefsElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   public String getName()
   {
      return "defs";
   }

   public boolean isDisplayed()
   {
      return false;
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
   {
      return null;
   }

   public Object clone()
   {
      SVGDefsElement element = new SVGDefsElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   @Override
   public void setDescription(String text)
   {
   }

   @Override
   public void setTitle(String text)
   {
   }

}
