package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGMeasurement
{
   protected SVGMeasurement(SVGHandler handler)
   {
      this.handler = handler;
      autoValue = false;
   }

   protected void parse(String str, String defUnitName)
    throws SVGException
   {
      this.valueString = str;

      if (valueString == null || valueString.equals("inherit"))
      {
         value = null;
         return;
      }

      if (valueString.equals("auto"))
      {
         autoValue = true;
         value = null;
         return;
      }

      Matcher m = pattern.matcher(valueString);

      if (m.matches())
      {
         try
         {
            value = Double.valueOf(m.group(1));
         }
         catch (NumberFormatException e)
         {
            throw new CantParseMeasurementException(handler, valueString, e);
         }

         unitName = null;

         if (m.groupCount() == 2)
         {
            unitName = m.group(2);
         }

         if (unitName == null || unitName.isEmpty())
         {
            unitName = defUnitName;
         }

         setValue(value, unitName);
      }
      else
      {
         throw new CantParseMeasurementException(handler, valueString);
      }
   }

   private void setValue(double val, String unitName)
     throws UnknownUnitException
   {
      this.value = val;
      this.unitName = unitName.toLowerCase();
      unitId = -1;

      for (int i = 0; i < UNIT_NAMES.length; i++)
      {
         if (UNIT_NAMES[i].equals(unitName))
         {
            unitId = i;
            break;
         }
      }

      if (unitId == -1)
      {
         throw new UnknownUnitException(handler, unitName);
      }
   }

   public int getUnitId()
   {
      return unitId;
   }

   public Object getValue()
   {
      return value;
   }

   public double doubleValue()
   {
      return value.doubleValue();
   }

   public boolean isAuto()
   {
      return autoValue;
   }

   public String getUnitName()
   {
      return unitName;
   }

   public void makeEqual(SVGMeasurement measurement)
   {
      if (measurement.value == null)
      {
         value = null;
      }
      else
      {
         value = Double.valueOf(measurement.value.doubleValue());
      }

      unitName = measurement.unitName;
      unitId = measurement.unitId;
      valueString = measurement.valueString;
   }

   public String getSourceValue()
   {
      return valueString;
   }

   public static final Pattern pattern
      = Pattern.compile("((?:[+\\-]?\\d*)(?:\\.\\d+)?(?:[eE][=\\-]?\\d+)?)\\s*([a-zA-Z]*)");

   protected Double value;
   private String unitName;
   private int unitId=-1;
   protected String valueString;
   protected boolean autoValue;
   SVGHandler handler;

   public static final String[] UNIT_NAMES =
   {
      "deg",
      "rad",
      "grad",
      "em",
      "ex",
      "px",
      "in",
      "cm",
      "mm",
      "pt",
      "pc",
      "%",
      ""
   };

   public static final int UNIT_DEG=0;
   public static final int UNIT_RAD=1;
   public static final int UNIT_GRAD=2;
   public static final int UNIT_EM=3;
   public static final int UNIT_EX=4;
   public static final int UNIT_PX=5;
   public static final int UNIT_IN=6;
   public static final int UNIT_CM=7;
   public static final int UNIT_MM=8;
   public static final int UNIT_PT=9;
   public static final int UNIT_PC=10;
   public static final int UNIT_PERCENT=11;
   public static final int UNIT_DEFAULT=12;
}
