package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import java.awt.geom.Point2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGMarkerRefAttribute extends SVGLengthAttribute
{
   protected SVGMarkerRefAttribute(SVGHandler handler, String name,
      boolean isHorizontal)
   {
      super(handler, name, isHorizontal);
   }

   public static SVGMarkerRefAttribute valueOf(SVGHandler handler,
     String name, String valueString, boolean isHorizontal)
      throws SVGException
   {
      SVGMarkerRefAttribute attr = new SVGMarkerRefAttribute(handler,
        name, isHorizontal);

      attr.parse(valueString);

      return attr;
   }

   protected void parse(String valueString) throws SVGException
   {
      refType = COORD;

      if ("center".equals(valueString))
      {
         refType = CENTER;
      }
      else if (isHorizontal)
      {
         if ("left".equals(valueString))
         {
            refType = MIN;
         }
         else if ("right".equals(valueString))
         {
            refType = MAX;
         }
      }
      else
      {
         if ("top".equals(valueString))
         {
            refType = MIN;
         }
         else if ("bottom".equals(valueString))
         {
            refType = MAX;
         }
      }

      if (refType == COORD)
      {
         parse(valueString, "");
      }
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   public int getRefType()
   {
      return refType;
   }

   @Override
   public Object clone()
   {
      SVGMarkerRefAttribute attr = new SVGMarkerRefAttribute(handler, name,
       isHorizontal);
      attr.makeEqual((SVGAttribute)this);
      return attr;
   }

   @Override
   public void makeEqual(SVGAttribute other)
   {
      super.makeEqual((SVGAttribute)other);

      refType = ((SVGMarkerRefAttribute)other).refType;
   }

   public static final int MIN=0, MAX=1, CENTER=2, COORD=3;
   int refType;
}
