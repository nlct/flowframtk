package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGLengthAttribute extends SVGMeasurement implements SVGNumberAttribute
{
   protected SVGLengthAttribute(SVGHandler handler)
   {
      this(handler, "length");
   }

   protected SVGLengthAttribute(SVGHandler handler, String attrName)
   {
      this(handler, attrName, true);
   }

   protected SVGLengthAttribute(SVGHandler handler, String attrName, boolean isHorizontal)
   {
      super(handler);
      this.name = attrName;
      this.isHorizontal = isHorizontal;
   }


   public static SVGLengthAttribute valueOf(SVGHandler handler, String attrName, String valueString)
   throws SVGException
   {
      return valueOf(handler, attrName, valueString, "", true);
   }

   public static SVGLengthAttribute valueOf(SVGHandler handler, String attrName, String valueString, boolean horizontal)
   throws SVGException
   {
      return valueOf(handler, attrName, valueString, "", horizontal);
   }

   public static SVGLengthAttribute valueOf(SVGHandler handler, String attrName,
      String valueString, String defUnitName)
   throws SVGException
   {
      return valueOf(handler, attrName, valueString, defUnitName, true);
   }

   public static SVGLengthAttribute valueOf(SVGHandler handler, String attrName,
      String valueString, String defUnitName, boolean horizontal)
   throws SVGException
   {
      SVGLengthAttribute attr = new SVGLengthAttribute(handler, attrName, horizontal);
      attr.parse(valueString, defUnitName);
      return attr;
   }

   @Override
   protected void parse(String str, String defUnitName)
   throws SVGException
   {
      super.parse(str, defUnitName);

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
              throw new InvalidUnitException(handler, getName(), getUnitName());
         }
      }
   }

   @Override
   public double doubleValue(SVGAbstractElement element)
   {
      return getStorageValue(element, isHorizontal);
   }

   @Override
   public int intValue(SVGAbstractElement element)
   {
      return (int)Math.round(doubleValue(element));
   }

   @Override
   public boolean isHorizontal()
   {
      return isHorizontal;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
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

   public JDRLength lengthValue(SVGAbstractElement element)
   {
      return getLength(element, isHorizontal);
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

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public Object clone()
   {
      SVGLengthAttribute length = new SVGLengthAttribute(handler, getName(), isHorizontal);

      length.makeEqual((SVGAttribute)this);

      return length;
   }

   @Override
   public void makeEqual(SVGAttribute other)
   {
      super.makeEqual((SVGMeasurement)other);

      if (other instanceof SVGLengthAttribute)
      {
         isHorizontal = ((SVGLengthAttribute)other).isHorizontal();
      }
   }

   String name;
   boolean isHorizontal;
}
