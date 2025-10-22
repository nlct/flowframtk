package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGMeasurement
{
   public SVGMeasurement(SVGHandler handler, String valueString, int defUnitId)
     throws InvalidFormatException
   {
      if (defUnitId < 0 || defUnitId >= UNIT_NAMES.length)
      {
         throw new IllegalArgumentException(
            "Invalid default unit id: "+defUnitId);
      }

      this.handler = handler;

      parse(valueString, UNIT_NAMES[defUnitId]);
   }

   public SVGMeasurement(SVGHandler handler, String valueString, String defUnit)
     throws InvalidFormatException
   {
      this.handler = handler;
      parse(valueString, defUnit);
   }

   private void parse(String valueString, String defUnit)
     throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         value = null;
         return;
      }

      Matcher m = pattern.matcher(valueString);

      if (m.matches())
      {
         try
         {
            value = new Double(m.group(1));
         }
         catch (NumberFormatException e)
         {
            throw new InvalidFormatException
              ("Unable to parse '"+valueString+"'");
         }

         unitName = defUnit;

         if (m.groupCount() == 2)
         {
            unitName = m.group(2);

            if ("".equals(unitName))
            {
               unitName = defUnit;
            }
         }

         if (unitName == null)
         {
            throw new InvalidFormatException("Missing unit in '"+valueString+"'");
         }

         setValue(value, unitName);
      }
      else
      {
         throw new InvalidFormatException("Unable to parse measurement from '"
           +valueString+"'");
      }
   }

   private void setValue(double val, String unitName)
     throws InvalidFormatException
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
         throw new InvalidFormatException("Unknown unit '"+unitName+"'");
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
         value = new Double(measurement.value.doubleValue());
      }

      unitName = measurement.unitName;
      unitId = measurement.unitId;
   }

   public static final Pattern pattern
      = Pattern.compile("((?:[+\\-]?\\d*)(?:\\.\\d+)?(?:[eE][=\\-]?\\d+)?)\\s*([a-zA-Z]*)");

   private Double value;
   private String unitName;
   private int unitId=-1;
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
}
