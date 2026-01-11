package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGLength extends SVGMeasurement
{
   public SVGLength(SVGHandler handler, String value)
     throws InvalidFormatException
   {
      super(handler, value);

      if (value != null)
      {
         switch (getUnitId())
         {
            case SVGMeasurement.UNIT_PERCENT :
            case SVGMeasurement.UNIT_EM :
            case SVGMeasurement.UNIT_EX :
            case SVGMeasurement.UNIT_PX :
            case SVGMeasurement.UNIT_IN :
            case SVGMeasurement.UNIT_CM :
            case SVGMeasurement.UNIT_MM :
            case SVGMeasurement.UNIT_PT :
            case SVGMeasurement.UNIT_PC :
            case SVGMeasurement.UNIT_DEFAULT :
            break;
            default :
              throw new InvalidFormatException
               ("Invalid length unit '"+getUnitName()+"' in "
                +" '"+value+"'");
         }
      }
   }

   public String getName()
   {
      return "length";
   }

   public double getStorageValue(SVGAbstractElement element, boolean isHorizontal)
   {
      double val = doubleValue();
      JDRUnit storageUnit = handler.getStorageUnit();

      switch (getUnitId())
      {
         case SVGMeasurement.UNIT_DEFAULT :
            return handler.getDefaultUnit().toUnit(val, storageUnit);
         case SVGMeasurement.UNIT_IN :
            return JDRUnit.in.toUnit(val, storageUnit);
         case SVGMeasurement.UNIT_CM :
            return JDRUnit.cm.toUnit(val, storageUnit);
         case SVGMeasurement.UNIT_MM :
            return JDRUnit.mm.toUnit(val, storageUnit);
         case SVGMeasurement.UNIT_PT :
            return JDRUnit.bp.toUnit(val, storageUnit);
         case SVGMeasurement.UNIT_PC :
            return JDRUnit.pc.toUnit(val, storageUnit);
         case SVGMeasurement.UNIT_PERCENT :
            double relValue; // storage units

            if (element == null)
            {
               relValue = 1.0;
            }
            else
            {
               if (isHorizontal)
               {
                  relValue = element.getViewportWidth();
               }
               else
               {
                  relValue = element.getViewportHeight();
               }
            }

            return 0.01*val*relValue;
// TODO:
         case SVGMeasurement.UNIT_PX :
         case SVGMeasurement.UNIT_EM :
         case SVGMeasurement.UNIT_EX :
      }

      return 0;
   }

   public JDRLength getLength(SVGAbstractElement element, boolean isHorizontal)
   {
      double val = doubleValue();
      CanvasGraphics cg = element.getCanvasGraphics();

      switch (getUnitId())
      {
         case SVGMeasurement.UNIT_DEFAULT :
            return handler.toStorageLength(val);
         case SVGMeasurement.UNIT_IN :
            return new JDRLength(cg, val, JDRUnit.in);
         case SVGMeasurement.UNIT_CM :
            return new JDRLength(cg, val, JDRUnit.cm);
         case SVGMeasurement.UNIT_MM :
            return new JDRLength(cg, val, JDRUnit.mm);
         case SVGMeasurement.UNIT_PX : // treat as bp
         case SVGMeasurement.UNIT_PT :
            return new JDRLength(cg, val, JDRUnit.bp);
         case SVGMeasurement.UNIT_PC :
            return new JDRLength(cg, val, JDRUnit.pc);
         case SVGMeasurement.UNIT_PERCENT :
            double relValue;// storage unit

            if (element == null)
            {
               relValue = 1.0;
            }
            else
            {
               if (isHorizontal)
               {
                  relValue = element.getViewportWidth();
               }
               else
               {
                  relValue = element.getViewportHeight();
               }
            }

            return new JDRLength(cg, 0.01*val*relValue, handler.getStorageUnit());
// TODO:
         case SVGMeasurement.UNIT_EM :
         case SVGMeasurement.UNIT_EX :
      }

      return new JDRLength(cg, 1, JDRUnit.bp);
   }

   public Object clone()
   {
      try
      {
         SVGLength length = new SVGLength(handler, null);

         length.makeEqual(this);

         return length;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }
}
