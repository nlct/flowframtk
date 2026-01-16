package com.dickimawbooks.jdr.io.svg;

import java.awt.Color;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPaintAttribute implements SVGAttribute
{
   protected SVGPaintAttribute(SVGHandler handler, String attrName)
   {
      this.handler = handler;
      this.name = attrName;
   }

   protected SVGPaintAttribute(SVGAbstractElement element, String attrName)
   {
      this.handler = element.getHandler();
      this.name = attrName;
      this.elementOwner = element;
   }

   public static SVGPaintAttribute valueOf(SVGHandler handler,
     String attrName, String valueString)
   throws SVGException
   {
      SVGPaintAttribute attr = new SVGPaintAttribute(handler, attrName);
      attr.valueString = valueString;
      attr.parse();
      return attr;
   }

   public static SVGPaintAttribute valueOf(SVGAbstractElement element,
     String attrName, String valueString)
   throws SVGException
   {
      SVGPaintAttribute attr = new SVGPaintAttribute(element, attrName);
      attr.valueString = valueString;
      attr.parse();
      return attr;
   }

   protected void parse() throws SVGException
   {
      parse(valueString);
   }

   private void parse(String str) throws SVGException
   {
      if (str == null || str.equals("inherit"))
      {
         paint = null;
      }
      else if (str.equals("none") || str.equals("transparent"))
      {
         paint = new JDRTransparent(handler.getCanvasGraphics());
      }
      else if (str.equals("currentColor"))
      {
         if (elementOwner != null)
         {
            paint = elementOwner.getCurrentPaint();
         }
      }
      else
      {
         referencedElement = handler.getAttributeValueRef(this);

         if (referencedElement == null)
         {
            parsePattern(str);
         }
         else
         {
            try
            {
               paint = referencedElement.getPaint();
            }
            catch (NoElementPaintException e)
            {
               throw new InvalidAttributeValueException(handler, getName(),
                  str, e);
            }
         }
      }
   }

   protected void parsePattern(String str) throws SVGException
   {
      CanvasGraphics cg = handler.getCanvasGraphics();

      for (int i = 0; i < PATTERNS.length; i++)
      {
         Matcher m = PATTERNS[i].matcher(str);

         try
         {
            if (m.matches())
            {
               switch (i)
               {
                  case PATTERN_HEX:

                     char[] red   = new char[2];
                     char[] green = new char[2];
                     char[] blue  = new char[2];

                     String group1 = m.group(1);

                     String group2 = m.group(2);

                     if (group2 == null)
                     {// 3 hex digits
                        red[0]   = group1.charAt(0);
                        green[0] = group1.charAt(1);
                        blue[0]  = group1.charAt(2);

                        red[1]   = red[0];
                        green[1] = green[0];
                        blue[1]  = blue[0];
                     }
                     else
                     {// 6 hex digits
                        red[0]   = group1.charAt(0);
                        red[1]   = group1.charAt(1);
                        green[0] = group1.charAt(2);
                        green[1] = group2.charAt(0);
                        blue[0]  = group2.charAt(1);
                        blue[1]  = group2.charAt(2);
                     }

                     paint = new JDRColor(cg, new Color
                      (
                         Integer.parseInt(new String(red), 16),
                         Integer.parseInt(new String(green), 16),
                         Integer.parseInt(new String(blue), 16)
                      ));
                  break;
                  case PATTERN_RGB:
                     paint = new JDRColor(cg, new Color
                      (
                         Integer.parseInt(m.group(1)),
                         Integer.parseInt(m.group(2)),
                         Integer.parseInt(m.group(3))
                      ));
                  break;
                  case PATTERN_PERCENT:
                     paint = new JDRColor
                      (cg, 
                         Double.parseDouble(m.group(1))*0.01,
                         Double.parseDouble(m.group(2))*0.01,
                         Double.parseDouble(m.group(3))*0.01
                      );
                  break;
                  case PATTERN_FUNCIRI:
                    String iri = m.group(1);
                    String altValue = m.group(2);

                    if (altValue != null)
                    {
                       parse(altValue);
                    }
                    else
                    {// FuncIRI not yet implemented
                       throw new UnsupportedAttributeValueException(
                         handler, getName(), valueString);
                    }
                  break;
                  case PATTERN_KEYWORD:
                     paint = SVGColorFactory.getPredefinedColor(handler, str);
                  break;
               }
            }
         }
         catch (NumberFormatException e)
         {
            throw new CantParseAttributeValueException(handler, getName(), valueString, e);
         }
      }

      if (paint == null)
      {
         throw new CantParseAttributeValueException(handler, getName(), valueString);
      }
   }

   public JDRPaint getPaint()
   {
      return paint;
   }

   @Override
   public Object getValue()
   {
      return paint;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   @Override
   public Object clone()
   {
      SVGPaintAttribute attr = new SVGPaintAttribute(handler, name);
      attr.makeEqual(this);
      return attr;
   }

   public void makeEqual(SVGPaintAttribute attr)
   {
      if (attr.paint == null)
      {
         paint = null;
      }
      else
      {
         paint = (JDRPaint)attr.paint.clone();
      }

      name = attr.name;
      valueString = attr.valueString;
      referencedElement = attr.referencedElement;
   }

   @Override
   public String toString()
   {
      return String.format("%s[name=%s,original=%s,value=%s,ref=%s]",
      getClass().getSimpleName(), getName(), valueString, getValue(), referencedElement);
   }

   @Override
   public String getSourceValue()
   {
      return valueString;
   }

   private JDRPaint paint;

   private String name;
   private String valueString;
   SVGHandler handler;

   SVGAbstractElement referencedElement, elementOwner;

   private static final Pattern[] PATTERNS =
   {
      Pattern.compile("#([0-9A-Fa-f]{3})([0-9A-Fa-f]{3})?\\s*(icc-color\\(.*\\))?"),
      Pattern.compile("rgb\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*\\)\\s*(icc-color\\(.*\\))?"),
      Pattern.compile("rgb\\(\\s*([+\\-]?[0-9]*\\.?[0-9]+)%\\s*,\\s*([+\\-]?[0-9]*\\.?[0-9]+)%\\s*,\\s*([+\\-]?[0-9]*\\.?[0-9]+)%\\s*\\)\\s*(icc-color\\(.*\\))?"),
      Pattern.compile("url\\(([^\\(\\)]+)\\)\\s*(.*)"),
      Pattern.compile("[a-zA-Z]+\\s*(icc-color\\(.*\\))?")
   };

/*
   private static final Pattern ICCPATTERN =
     Pattern.compile("icc-color\\(([^,\\(\\)\\s]+)((?:\\s*,\\s*[+\\-]?\\d*(?:\\.\\d+(?:[Ee][+\\-]\\d+)?)?)+)\\)");
*/

   private static final int PATTERN_HEX=0;
   private static final int PATTERN_RGB=1;
   private static final int PATTERN_PERCENT=2;
   private static final int PATTERN_FUNCIRI=3;
   private static final int PATTERN_KEYWORD=4;
}
