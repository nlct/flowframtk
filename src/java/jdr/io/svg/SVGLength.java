package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGLength extends SVGMeasurement
{
   public SVGLength(SVGHandler handler, String value)
     throws InvalidFormatException
   {
      super(handler, value, "pt");

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

   public double getBpValue(SVGAbstractElement element, boolean isHorizontal)
   {
      double val = doubleValue();

      switch (getUnitId())
      {
         case SVGMeasurement.UNIT_IN :
            return JDRUnit.in.toBp(val);
         case SVGMeasurement.UNIT_CM :
            return JDRUnit.cm.toBp(val);
         case SVGMeasurement.UNIT_MM :
            return JDRUnit.mm.toBp(val);
         case SVGMeasurement.UNIT_PT :
            return val;
         case SVGMeasurement.UNIT_PC :
            return JDRUnit.pc.toBp(val);
         case SVGMeasurement.UNIT_PERCENT :
            double relValue;

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

      switch (getUnitId())
      {
         case SVGMeasurement.UNIT_IN :
            return new JDRLength(element.getCanvasGraphics(), val, JDRUnit.in);
         case SVGMeasurement.UNIT_CM :
            return new JDRLength(element.getCanvasGraphics(), val, JDRUnit.cm);
         case SVGMeasurement.UNIT_MM :
            return new JDRLength(element.getCanvasGraphics(), val, JDRUnit.mm);
         case SVGMeasurement.UNIT_PT :
            return new JDRLength(element.getCanvasGraphics(), val, JDRUnit.bp);
         case SVGMeasurement.UNIT_PC :
            return new JDRLength(element.getCanvasGraphics(), val, JDRUnit.pc);
         case SVGMeasurement.UNIT_PERCENT :
            double relValue;

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

            return new JDRLength(handler.getCanvasGraphics(), 
               0.01*val*relValue, JDRUnit.bp);
// TODO:
         case SVGMeasurement.UNIT_PX :
         case SVGMeasurement.UNIT_EM :
         case SVGMeasurement.UNIT_EX :
      }

      return new JDRLength(handler.getCanvasGraphics(), 1, JDRUnit.bp);
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
