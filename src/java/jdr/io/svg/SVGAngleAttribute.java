package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import com.dickimawbooks.jdr.JDRCompleteObject;
import com.dickimawbooks.jdr.JDRAngle;
import com.dickimawbooks.jdr.exceptions.*;

public class SVGAngleAttribute extends SVGMeasurement implements SVGAttribute
{
   protected SVGAngleAttribute(SVGHandler handler)
   {
      this(handler, "angle");
   }

   protected SVGAngleAttribute(SVGHandler handler, String name)
   {
      super(handler);
      this.name = name;
   }

   public static SVGAngleAttribute valueOf(SVGHandler handler, String valueString)
   throws SVGException
   {
      return valueOf(handler, valueString, "");
   }

   public static SVGAngleAttribute valueOf(SVGHandler handler, String valueString, String defUnitName)
   throws SVGException
   {
      SVGAngleAttribute attr = new SVGAngleAttribute(handler);
      attr.parse(valueString, defUnitName);
      return attr;
   }

   @Override
   protected void parse(String str, String defUnitName)
   throws SVGException
   {
      super.parse(str, "deg");

      if (value != null)
      {
         switch (getUnitId())
         {
            case SVGMeasurement.UNIT_DEG:
            case SVGMeasurement.UNIT_RAD:
            case SVGMeasurement.UNIT_GRAD:
            break;
            default:
               throw new InvalidUnitException(handler, name, getUnitName());
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
            return val*GRAD_FACTOR;
      }

      return val;
   }

   public JDRAngle getAngle()
   {
      return new JDRAngle(handler.getCanvasGraphics(), getRadians(), JDRAngle.RADIAN);
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   @Override
   public Object clone()
   {
      SVGAngleAttribute angle = new SVGAngleAttribute(handler, name);
      angle.makeEqual((SVGAttribute)this);
      return angle;
   }

   @Override
   public void makeEqual(SVGAttribute other)
   {
      super.makeEqual((SVGMeasurement)other);
   }

   private static final double GRAD_FACTOR = Math.PI/200;

   private String name;
}
