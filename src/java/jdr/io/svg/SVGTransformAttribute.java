package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;
import java.awt.geom.AffineTransform;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGTransformAttribute implements SVGAttribute
{
   protected SVGTransformAttribute(SVGHandler handler)
   {
      this.handler = handler;
      transform = new AffineTransform();
   }

   public static SVGTransformAttribute valueOf(SVGHandler handler, String valueString)
    throws SVGException
   {
      SVGTransformAttribute attr = new SVGTransformAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

      if (valueString == null)
      {
         return;
      }

      Matcher m = TRANSFORM_PATTERN.matcher(valueString);

      while (valueString != null && m.matches())
      {
         String function = m.group(1);
         String arg = m.group(2);

         if (m.groupCount() == 3)
         {
            valueString = m.group(3);
         }
         else
         {
            valueString = null;
         }

         if (valueString != null)
         {
            m = TRANSFORM_PATTERN.matcher(valueString);
         }

         try
         {
            addTransform(function, arg);
         }
         catch (NumberFormatException e)
         {
            throw new CantParseAttributeValueException(handler, getName(), valueString, e);
         }
      }
   }

   private void addTransform(String function, String arg)
     throws SVGException,NumberFormatException
   {
      Matcher m;
      AffineTransform af = null;
      JDRUnit storageUnit = handler.getStorageUnit();

      if (function.equals("matrix"))
      {
         m = MATRIX_PATTERN.matcher(arg);

         if (!m.matches())
         {
            throw new CantParseAttributeValueException(handler, getName(),
               valueString);
         }

         double tx = Double.parseDouble(m.group(5));
         double ty = Double.parseDouble(m.group(6));

         tx = handler.getDefaultUnit().toUnit(tx, storageUnit);
         ty = handler.getDefaultUnit().toUnit(ty, storageUnit);

         af = new AffineTransform
           (
             Double.parseDouble(m.group(1)),
             Double.parseDouble(m.group(2)),
             Double.parseDouble(m.group(3)),
             Double.parseDouble(m.group(4)),
             tx, ty
           );
      }
      else if (function.equals("translate"))
      {
         m = TRANSLATE_PATTERN.matcher(arg);

         if (!m.matches())
         {
            throw new CantParseAttributeValueException(handler, getName(), valueString);
         }

         double tx = Double.parseDouble(m.group(1));
         double ty = 0;

         tx = handler.getDefaultUnit().toUnit(tx, storageUnit);

         if (m.groupCount() == 2)
         {
            ty = Double.parseDouble(m.group(2));
            ty = handler.getDefaultUnit().toUnit(ty, storageUnit);
         }

         af = AffineTransform.getTranslateInstance(tx, ty);
      }
      else if (function.equals("scale"))
      {
         // scale has same pattern as translate
         m = TRANSLATE_PATTERN.matcher(arg);

         if (!m.matches())
         {
            throw new CantParseAttributeValueException(handler, getName(), valueString);
         }

         double sx = Double.parseDouble(m.group(1));

         double sy = m.groupCount() == 2 ? Double.parseDouble(m.group(2)) : sx;

         af = AffineTransform.getScaleInstance(sx, sy);
      }
      else if (function.equals("rotate"))
      {
         m = ROTATE_PATTERN.matcher(arg);

         if (!m.matches())
         {
            throw new CantParseAttributeValueException(handler, getName(), valueString);
         }

         double angle = Math.toRadians(Double.parseDouble(m.group(1)));

         if (m.groupCount() == 3)
         {
            double x = Double.parseDouble(m.group(2));
            double y = Double.parseDouble(m.group(3));

            x = handler.getDefaultUnit().toUnit(x, storageUnit);
            y = handler.getDefaultUnit().toUnit(y, storageUnit);

            af = AffineTransform.getRotateInstance(angle, x, y);
         }
         else
         {
            af = AffineTransform.getRotateInstance(angle);
         }
      }
      else if (function.equals("skewX"))
      {
         m = SKEW_PATTERN.matcher(arg);

         if (!m.matches())
         {
            throw new CantParseAttributeValueException(handler, getName(), valueString);
         }

         af = AffineTransform.getShearInstance(
            Math.tan(Math.toRadians(Double.parseDouble(m.group(1)))),
            0);
      }
      else if (function.equals("skewY"))
      {
         m = SKEW_PATTERN.matcher(arg);

         if (!m.matches())
         {
            throw new CantParseAttributeValueException(handler, getName(), valueString);
         }

         af = AffineTransform.getShearInstance(0, 
            Math.tan(Math.toRadians(Double.parseDouble(m.group(1)))));
      }
      else
      {
         throw new UnknownAttributeValueException(handler, getName(), valueString);
      }

      transform.concatenate(af);
   }

   @Override
   public String getName()
   {
      return "transform";
   }

   @Override
   public Object getValue()
   {
      return transform;
   }

   public AffineTransform getTransform()
   {
      return transform;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   @Override
   public Object clone()
   {
      SVGTransformAttribute attr = new SVGTransformAttribute(handler);
      attr.makeEqual(this);
      return attr;
   }

   @Override
   public void makeEqual(SVGAttribute other)
   {
      if (other instanceof SVGTransformAttribute)
      {
         SVGTransformAttribute attr = (SVGTransformAttribute)other;

         valueString = attr.valueString;
         transform.setTransform(attr.transform);
      }
   }

   @Override
   public String getSourceValue()
   {
      return valueString;
   }

   private AffineTransform transform;
   SVGHandler handler;
   String valueString;

   private static final Pattern TRANSFORM_PATTERN 
      = Pattern.compile("\\s*,?\\s*([a-zA-Z]+)\\s*\\(([^\\)]+)\\)(?:[\\s,]+(.*))?");

   private static final Pattern MATRIX_PATTERN
      = Pattern.compile("\\s*([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)\\s*");

   private static final Pattern TRANSLATE_PATTERN
      = Pattern.compile("\\s*([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)(?:[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?))?");

   private static final Pattern ROTATE_PATTERN
      = Pattern.compile("\\s*([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)(?:[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?))?");

   private static final Pattern SKEW_PATTERN
      = Pattern.compile("\\s*([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)\\s*");

}
