package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import com.dickimawbooks.jdr.JDRCompleteObject;
import com.dickimawbooks.jdr.exceptions.*;

public class SVGAngleAttribute extends SVGMeasurement implements SVGAttribute
{
   public SVGAngleAttribute(SVGHandler handler, String value)
     throws InvalidFormatException
   {
      this(handler, "angle", value);
   }

   public SVGAngleAttribute(SVGHandler handler, String attrName, String value)
     throws InvalidFormatException
   {
      super(handler, value, "deg");

      if (value != null)
      {
         switch (getUnitId())
         {
            case SVGMeasurement.UNIT_DEG:
            case SVGMeasurement.UNIT_RAD:
            case SVGMeasurement.UNIT_GRAD:
            break;
            default:
               throw new InvalidFormatException("Invalid angle unit '"+getUnitName()+"'");
         }
      }
   }

   @Override
   public String getName()
   {
      return name;
   }

   public double getRadians()
   {
      double val = doubleValue();

      switch (getUnitId())
      {
         case SVGMeasurement.UNIT_DEG :
            return Math.toRadians(val);
         case SVGMeasurement.UNIT_GRAD :
            return val*gradFactor;
      }

      return val;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   @Override
   public Object clone()
   {
      try
      {
         SVGAngleAttribute angle = new SVGAngleAttribute(handler, name, null);

         angle.makeEqual(this);

         return angle;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   private static final double gradFactor = Math.PI/200;

   private String name;
}
