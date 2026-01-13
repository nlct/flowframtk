package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;
import java.awt.geom.AffineTransform;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGTransformAttribute implements SVGAttribute
{
   public SVGTransformAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      this.handler = handler;
      this.valueString = valueString;
      transform = new AffineTransform();
      parse();
   }

   protected void parse() throws InvalidFormatException
   {
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
            // shouldn't happen
         }
      }
   }

   private void addTransform(String function, String arg)
     throws InvalidFormatException,NumberFormatException
   {
      Matcher m;
      AffineTransform af = null;
      JDRUnit storageUnit = handler.getStorageUnit();

      if (function.equals("matrix"))
      {
         m = MATRIX_PATTERN.matcher(arg);

         if (!m.matches())
         {
            throw new InvalidFormatException(
               "syntax: matrix(a, b, c, d, e, f)\n  in: '"+arg+"'");
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
            throw new InvalidFormatException(
               "syntax: translate(tx [ty])\n  in: '"+arg+"'");
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
            throw new InvalidFormatException(
               "syntax: scale(sx [sy])\n  in: '"+arg+"'");
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
            throw new InvalidFormatException(
               "syntax: rotate(angle [cx cy])\n  in: '"+arg+"'");
         }

         double angle = Math.toRadians(Double.parseDouble(m.group(1)));

         if (m.groupCount() == 3)
         {
            af = AffineTransform.getRotateInstance(angle,
               Double.parseDouble(m.group(2)),
               Double.parseDouble(m.group(3)));
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
            throw new InvalidFormatException(
               "syntax: skew(angle)\n  in: '"+arg+"'");
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
            throw new InvalidFormatException(
               "syntax: skew(angle)\n  in: '"+arg+"'");
         }

         af = AffineTransform.getShearInstance(0, 
            Math.tan(Math.toRadians(Double.parseDouble(m.group(1)))));
      }
      else
      {
         throw new InvalidFormatException(
            "Unknown transform function '"+function+"'");
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
      SVGTransformAttribute attr = null;

      try
      {
         attr = new SVGTransformAttribute(handler, null);

         attr.makeEqual(this);
      }
      catch (InvalidFormatException e)
      {
         // this shouldn't happen
      }

      return attr;
   }

   public void makeEqual(SVGTransformAttribute attr)
   {
      valueString = attr.valueString;
      transform.setTransform(attr.transform);
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
