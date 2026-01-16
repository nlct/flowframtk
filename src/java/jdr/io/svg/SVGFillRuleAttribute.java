package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.GeneralPath;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGFillRuleAttribute extends SVGAbstractAttribute
  implements SVGNumberAttribute
{
   protected SVGFillRuleAttribute(SVGHandler handler)
   {
      super(handler);
   }

   public static SVGFillRuleAttribute valueOf(SVGHandler handler, String valueString)
      throws SVGException
   {
      SVGFillRuleAttribute attr = new SVGFillRuleAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

      if (valueString == null || valueString.equals("inherit"))
      {
         rule = null;
      }
      else if (valueString.equals("nonzero"))
      {
         rule = Integer.valueOf(GeneralPath.WIND_NON_ZERO);
      }
      else if (valueString.equals("evenodd"))
      {
         rule = Integer.valueOf(GeneralPath.WIND_EVEN_ODD);
      }
      else
      {
         throw new InvalidAttributeValueException(handler, getName(), valueString);
      }
   }

   public int getWindingRule()
   {
      return rule.intValue();
   }

   @Override
   public String getName()
   {
      return "fill-rule";
   }

   @Override
   public Object getValue()
   {
      return rule;
   }

   @Override
   public int intValue(SVGAbstractElement element)
   {
      return rule.intValue();
   }

   @Override
   public double doubleValue(SVGAbstractElement element)
   {
      return (double)intValue(element);
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      if (rule != null && object instanceof JDRShape)
      {
         JDRStroke stroke = ((JDRShape)object).getStroke();

         if (stroke instanceof JDRBasicStroke)
         {
            JDRBasicStroke basicStroke = (JDRBasicStroke)stroke;

            basicStroke.setWindingRule(rule.intValue());
         }
      }
   }

   @Override
   public Object clone()
   {
      SVGFillRuleAttribute attr = new SVGFillRuleAttribute(handler);
      attr.makeEqual(this);
      return attr;
   }

   public void makeEqual(SVGFillRuleAttribute attr)
   {
      super.makeEqual(attr);
      rule = attr.rule;
   }

   private Integer rule;
}
