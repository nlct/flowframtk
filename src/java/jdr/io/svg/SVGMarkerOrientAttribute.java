package com.dickimawbooks.jdr.io.svg;

import java.awt.*;
import java.util.regex.*;

import java.awt.geom.Point2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGMarkerOrientAttribute extends SVGAngleAttribute
{
   protected SVGMarkerOrientAttribute(SVGHandler handler)
   {
      super(handler, "orient");
   }

   public static SVGMarkerOrientAttribute valueOf(SVGHandler handler,
     String valueString)
      throws SVGException
   {
      SVGMarkerOrientAttribute attr = new SVGMarkerOrientAttribute(handler);

      attr.parse(valueString, "");

      return attr;
   }

   @Override
   protected void parse(String valueString, String defUnit) throws SVGException
   {
      if (valueString == null || valueString.equals("0"))
      {
         orientType = ZERO;
      }
      else if ("auto".equals(valueString))
      {
         orientType = AUTO;
      }
      else if ("auto-start-reverse".equals(valueString))
      {
         orientType = AUTO_START_REVERSE;
      }
      else
      {
         super.parse(valueString, defUnit);
      }
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   public int getOrientType()
   {
      return orientType;
   }

   @Override
   public JDRAngle getAngle()
   {
      if (orientType == ANGLE)
      {
         return super.getAngle();
      }
      else if (orientType == ZERO)
      {
         return new JDRAngle(handler.getCanvasGraphics());
      }
      else
      {
         return null;
      }
   }

   @Override
   public Object clone()
   {
      SVGMarkerOrientAttribute attr = new SVGMarkerOrientAttribute(handler);
      attr.makeEqual((SVGAttribute)this);
      return attr;
   }

   @Override
   public void makeEqual(SVGAttribute other)
   {
      super.makeEqual((SVGAttribute)other);

      orientType = ((SVGMarkerOrientAttribute)other).orientType;
   }

   public static final int ZERO=0, AUTO=1, AUTO_START_REVERSE=2, ANGLE=3;
   int orientType;
}
