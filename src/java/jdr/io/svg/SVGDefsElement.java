package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGDefsElement extends SVGAbstractElement
{
   public SVGDefsElement(SVGHandler handler, 
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   public String getName()
   {
      return "defs";
   }

   public boolean isDisplayed()
   {
      return false;
   }

   public void addToImage(JDRGroup group)
   {
   }

   public Object clone()
   {
      try
      {
         SVGDefsElement element = new SVGDefsElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }
}
