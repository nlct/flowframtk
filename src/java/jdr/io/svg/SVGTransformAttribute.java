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
      transform = new AffineTransform();
      parse(valueString);
   }

   public void parse(String valueString)
     throws InvalidFormatException
   {
      if (valueString == null)
      {
         return;
      }

      Matcher m = transformPattern.matcher(valueString);

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
            m = transformPattern.matcher(valueString);
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

      if (function.equals("matrix"))
      {
         m = matrixPattern.matcher(arg);

         if (!m.matches())
         {
            throw new InvalidFormatException(
               "syntax: matrix(a, b, c, d, e, f)\n  in: '"+arg+"'");
         }

         af = new AffineTransform
           (
             Double.parseDouble(m.group(1)),
             Double.parseDouble(m.group(2)),
             Double.parseDouble(m.group(3)),
             Double.parseDouble(m.group(4)),
             Double.parseDouble(m.group(5)),
             Double.parseDouble(m.group(6))
           );
      }
      else if (function.equals("translate"))
      {
         m = translatePattern.matcher(arg);

         if (!m.matches())
         {
            throw new InvalidFormatException(
               "syntax: translate(tx [ty])\n  in: '"+arg+"'");
         }

         af = AffineTransform.getTranslateInstance
           (
              Double.parseDouble(m.group(1)),
              m.groupCount() == 2 ? Double.parseDouble(m.group(2)) : 0
           );
      }
      else if (function.equals("scale"))
      {
         // scale has same pattern as translate
         m = translatePattern.matcher(arg);

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
         m = rotatePattern.matcher(arg);

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
         m = skewPattern.matcher(arg);

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
         m = skewPattern.matcher(arg);

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

   public String getName()
   {
      return "transform";
   }

   public Object getValue()
   {
      return transform;
   }

   public AffineTransform getTransform()
   {
      return transform;
   }

   public Object clone()
   {
      SVGTransformAttribute attr = null;

      try
      {
         attr = new SVGTransformAttribute(handler, null);

         attr.transform = (AffineTransform)transform.clone();
      }
      catch (InvalidFormatException e)
      {
         // this shouldn't happen
      }

      return attr;
   }

   private AffineTransform transform;
   SVGHandler handler;

   private static final Pattern transformPattern 
      = Pattern.compile("\\s*,?\\s*([a-zA-Z]+)\\s*\\(([^\\)]+)\\)(?:[\\s,]+(.*))?");

   private static final Pattern matrixPattern
      = Pattern.compile("\\s*([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)\\s*");

   private static final Pattern translatePattern
      = Pattern.compile("\\s*([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)(?:[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?))?");

   private static final Pattern rotatePattern
      = Pattern.compile("\\s*([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)(?:[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)[\\s,]+([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?))?");

   private static final Pattern skewPattern
      = Pattern.compile("\\s*([+\\-]?\\d*\\.?\\d+(?:[eE][+\\-]?\\d*\\.?\\d+)?)\\s*");

}
