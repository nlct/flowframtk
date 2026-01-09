package com.dickimawbooks.jdr.io.svg;

import java.util.*;
import java.util.regex.*;
import java.awt.geom.*;

import javax.swing.text.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

public abstract class SVGAbstractElement implements Cloneable
{
   public SVGAbstractElement(SVGHandler handler, 
      SVGAbstractElement parent, String uri, Attributes attr)
      throws InvalidFormatException
   {
      this.parent = parent;
      this.handler = handler;
      contents = new StringBuilder();

      children = new Vector<SVGAbstractElement>();

      if (parent == null)
      {
         attributeSet = new SVGAttributeSet();
      }
      else
      {
         attributeSet = new SVGAttributeSet(parent.attributeSet);
      }

      if (attr != null)
      {
         applyAttributes(uri, attr);
      }
   }

   public void addChild(SVGAbstractElement child)
   {
      children.add(child);
   }

   public void addToContents(char[] ch, int start, int length)
   {
      contents.append(ch, start, length);
   }

   public void endElement()
   {
   }

   protected void printAttributes()
   {
      System.out.println("attributes for '"+getName()+"':");

      for (Enumeration en = attributeSet.getAttributeNames(); en.hasMoreElements();)
      {
         String attrName = en.nextElement().toString();

         System.out.println(attrName+" : "
           +((SVGAttribute)attributeSet.getAttribute(attrName)).toString());
      }
   }

   public String getId()
   {
      return id;
   }

   public String[] getCssClassList()
   {
      return cssClassList;
   }

   protected void applyAttributes(String uri, Attributes attr)
      throws InvalidFormatException
   {
      String value = attr.getValue("id");

      if (value != null && !value.isEmpty())
      {
         id = value;
      }

      value = attr.getValue("class");

      if (value != null)
      {
         cssClassList = value.split("\\s+");
      }

      value = attr.getValue("style");

      if (value != null && !value.isEmpty())
      {
         if (styles == null)
         {
            styles = new SVGStyles();
         }

         if (id != null || cssClassList == null || cssClassList.length == 0)
         {
            styles.putRule("", id, "", createAttributeSet(value));
         }
         else
         {
            for (String s : cssClassList)
            {
               styles.putRule("", "", s, createAttributeSet(value));
            }
         }
      }

      addAttribute("href", attr);
      addAttribute("xlink:href", attr);
      addAttribute("transform", attr);
      addAttribute("color", attr);
      addAttribute("opacity", attr);
   }

   protected void applyShapeAttributes(String uri, Attributes attr)
      throws InvalidFormatException
   {
      addAttribute("fill", attr);
      addAttribute("fill-opacity", attr);
      addAttribute("fill-rule", attr);
      addAttribute("stroke", attr);
      addAttribute("stroke-dasharray", attr);
      addAttribute("stroke-dashoffset", attr);
      addAttribute("stroke-linecap", attr);
      addAttribute("stroke-linejoin", attr);
      addAttribute("stroke-miterlimit", attr);
      addAttribute("stroke-opacity", attr);
      addAttribute("stroke-width", attr);
   }

   protected void applyTextAttributes(String uri, Attributes attr)
      throws InvalidFormatException
   {
      addAttribute("text-anchor", attr);
      addAttribute("font-family", attr);
      addAttribute("font-size", attr);
      addAttribute("font-variant", attr);
      addAttribute("font-weight", attr);
      addAttribute("font", attr);
   }

   public static SVGAbstractElement getElement(
     SVGHandler handler, SVGAbstractElement parent, String elementName,
     String uri, Attributes attr)
     throws InvalidFormatException
   {
      if (elementName.equals("svg"))
      {
         return new SVGElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("title"))
      {
         return new SVGTitleElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("desc"))
      {
         return new SVGDescElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("rect"))
      {
         return new SVGRectElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("ellipse"))
      {
         return new SVGEllipseElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("circle"))
      {
         return new SVGCircleElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("line"))
      {
         return new SVGLineElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("polyline"))
      {
         return new SVGPolyLineElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("polygon"))
      {
         return new SVGPolygonElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("path"))
      {
         return new SVGPathElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("text"))
      {
         return new SVGTextElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("style"))
      {
         return new SVGStyleElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("defs"))
      {
         return new SVGDefsElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("use"))
      {
         return new SVGUseElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("g"))
      {
         return new SVGGroupElement(handler, parent, uri, attr);
      }
      else if (elementName.equals("link"))
      {// TODO?
      }

      throw new UnknownSVGElementException(elementName);
   }

   public String getContents()
   {
      return contents.toString();
   }

   public abstract String getName();

   /**
    * Add object corresponding to element, if applicable.
    * @return the new object added or null if not applicable
    */
   public abstract JDRCompleteObject addToImage(JDRGroup group)
      throws InvalidFormatException;

   public abstract void setDescription(String text);

   public abstract void setTitle(String text);

   public double getViewportWidth()
   {
      if (parent == null)
      {
         return 0;
      }
      else
      {
         return parent.getViewportWidth();
      }
   }

   public double getViewportHeight()
   {
      if (parent == null)
      {
         return 0;
      }
      else
      {
         return parent.getViewportHeight();
      }
   }

   public void addStyleRules(String ruleList)
   {
      /* NB javax.swing.text.html.CSS only supports limited HTML
       attributes. Using
       javax.swing.text.html.StyleSheet.addRule(String)
       will only pick up those attributes (which would only be
       useful for text.)
      */

      CSS_IGNORED.matcher(ruleList).replaceAll("");

      if (styles == null)
      {
         styles = new SVGStyles();
      }

      Matcher m = STYLE_PATTERN.matcher(ruleList);

      while (m.find())
      {
         String selectorRules = m.group(1);
         String attrList = m.group(2);
         m = STYLE_PATTERN.matcher(m.group(3));

         SVGAttributeSet atSet = createAttributeSet(attrList);

         styles.addRules(selectorRules, atSet);
      }
   }

   public void addAttribute(String attrName, Attributes saxAttr)
   {
      String value = saxAttr.getValue(attrName);

      if (value != null)
      {
         try
         {
            addAttribute(createStyleAttribute(attrName, value));
         }
         catch (InvalidFormatException e)
         {
            warning(e);
         }
      }
   }

   public void addAttribute(SVGAttribute attr)
   {
      attributeSet.addAttribute(attr);
   }

   public void addAttributeSet(String attrList)
   {
      attributeSet.addAttributes(createAttributeSet(attrList));
   }

   protected SVGAttributeSet createAttributeSet(String attrList)
   {
      SVGAttributeSet atSet = new SVGAttributeSet();

      Matcher m = STYLE_ATTR_PATTERN.matcher(attrList);

      while (m.find())
      {
         try
         {
            SVGAttribute attr = createStyleAttribute(m.group(1), m.group(2));

            atSet.addAttribute(attr);
         }
         catch (InvalidFormatException e)
         {
            warning(e);
         }
      }

      return atSet;
   }

   protected SVGAttribute createStyleAttribute(String name, String style)
     throws InvalidFormatException
   {
      SVGAttribute attr = null;

      if (name.equals("stroke"))
      {
         attr = new SVGPaintAttribute(handler, name, style,
            getPaintAttribute("color", null));
      }
      else if (name.equals("stroke-opacity"))
      {
         attr = new SVGDoubleAttribute(handler, name, style);
      }
      else if (name.equals("fill"))
      {
         attr = new SVGPaintAttribute(handler, name, style,
            getPaintAttribute("color", null));
      }
      else if (name.equals("fill-opacity"))
      {
         attr = new SVGDoubleAttribute(handler, name, style);
      }
      else if (name.equals("stroke-width"))
      {
         attr = new SVGPenWidthAttribute(handler, style);
      }
      else if (name.equals("fill-rule"))
      {
         attr = new SVGFillRuleAttribute(handler, style);
      }
      else if (name.equals("stroke-linecap"))
      {
         attr = new SVGCapStyleAttribute(handler, style);
      }
      else if (name.equals("stroke-linejoin"))
      {
         attr = new SVGJoinStyleAttribute(handler, style);
      }
      else if (name.equals("stroke-miterlimit"))
      {
         attr = new SVGMitreLimitAttribute(handler, style);
      }
      else if (name.equals("stroke-dashoffset"))
      {
         attr = new SVGDashOffsetAttribute(handler, style);
      }
      else if (name.equals("stroke-dasharray"))
      {
         attr = new SVGDashArrayAttribute(handler, style);
      }
      else if (name.equals("x"))
      {
         attr = new SVGLengthAttribute(handler, name, style, true);
      }
      else if (name.equals("y"))
      {
         attr = new SVGLengthAttribute(handler, name, style, false);
      }
      else if (name.equals("x1"))
      {
         attr = new SVGLengthAttribute(handler, name, style, true);
      }
      else if (name.equals("y1"))
      {
         attr = new SVGLengthAttribute(handler, name, style, false);
      }
      else if (name.equals("x2"))
      {
         attr = new SVGLengthAttribute(handler, name, style, true);
      }
      else if (name.equals("y2"))
      {
         attr = new SVGLengthAttribute(handler, name, style, false);
      }
      else if (name.equals("cx"))
      {
         attr = new SVGLengthAttribute(handler, name, style, true);
      }
      else if (name.equals("cy"))
      {
         attr = new SVGLengthAttribute(handler, name, style, false);
      }
      else if (name.equals("r"))
      {
         attr = new SVGLengthAttribute(handler, name, style);
      }
      else if (name.equals("rx"))
      {
         attr = new SVGLengthAttribute(handler, name, style, true);
      }
      else if (name.equals("ry"))
      {
         attr = new SVGLengthAttribute(handler, name, style, false);
      }
      else if (name.equals("width"))
      {
         attr = new SVGLengthAttribute(handler, name, style, true);
      }
      else if (name.equals("height"))
      {
         attr = new SVGLengthAttribute(handler, name, style, false);
      }
      else if (name.equals("points"))
      {
         attr = new SVGLengthArrayAttribute(handler, name, style);
      }
      else if (name.equals("d"))
      {
         attr = new SVGPathDataAttribute(handler, style);
      }
      else if (name.equals("display"))
      {
         attr = new SVGDisplayStyleAttribute(handler, style);
      }
      else if (name.equals("visibility"))
      {
         attr = new SVGVisibilityStyleAttribute(handler, style);
      }
      else if (name.equals("href") || name.equals("xlink:href"))
      {
         attr = new SVGStringAttribute(handler, name, style);
      }
      else if (name.equals("transform"))
      {
         attr = new SVGTransformAttribute(handler, style);
      }
      else if (name.equals("font-size"))
      {
         attr = new SVGFontSizeAttribute(handler, style);
      }
      else if (name.equals("font-family"))
      {
         attr = new SVGFontFamilyAttribute(handler, style);
      }
      else if (name.equals("font-weight"))
      {
         attr = new SVGFontWeightAttribute(handler, style);
      }
      else if (name.equals("font-style"))
      {
         attr = new SVGFontStyleAttribute(handler, style);
      }
      else if (name.equals("font-variant"))
      {
         attr = new SVGFontVariantAttribute(handler, style);
      }
      else if (name.equals("font"))
      {
         attr = new SVGFontAttribute(handler, style);
      }

      if (attr == null)
      {
         throw new UnknownSVGAttributeException(name);
      }

      return attr;
   }

   public SVGAttribute getAttribute(String attrName, SVGAttribute defValue)
   {
      SVGAttribute attr = getElementAttribute(attrName);

      return attr == null ? defValue : attr;
   }

   public SVGAttribute getElementAttribute(String attrName)
   {
      return getElementAttribute(getName(), id, cssClassList, attrName);
   }

   public SVGAttribute getElementAttribute(String elementName, String elementId,
     String[] elementClasses, String attrName)
   {
      Object object;

      if (elementName.equals(getName()))
      {
         object = attributeSet.getAttribute(attrName);

         if (object != null)
         {
            return (SVGAttribute)object;
         }
      }

      if (styles != null)
      {
         SVGAttributeSet set = styles.getFor(elementName, elementId, elementClasses);

         if (set != null)
         {
            object = set.getAttribute(attrName);

            if (object != null)
            {
               return (SVGAttribute)object;
            }
         }
      }

      if (parent != null)
      {
         return parent.getElementAttribute(elementName, elementId, elementClasses,
           attrName);
      }

      return null;
   }

   public SVGDefsElement getChildDefs()
   {
      for (SVGAbstractElement child : children)
      {
         if (child instanceof SVGDefsElement)
         {
            return (SVGDefsElement)child;
         }
      }

      return null;
   }

   public SVGAbstractElement getRefElement(String id)
   {
      if (parent == null)
      {
         return null;
      }

      SVGDefsElement defs = parent.getChildDefs();

      if (defs != null)
      {
         for (SVGAbstractElement child : defs.children)
         {
            if (id.equals(child.getId()))
            {
               return child;
            }
         }
      }

      return parent.getRefElement(id);
   }

   public JDRPaint getPaintAttribute(String attrName, JDRPaint defPaint)
   {
      SVGAttribute attr = getAttribute(attrName, null);

      if (attr != null && attr instanceof SVGPaintAttribute)
      {
         return ((SVGPaintAttribute)attr).getPaint();
      }

      return defPaint;
   }

   public JDRPaint getLinePaint()
   {
      return getPaintAttribute("stroke", null);
   }

   public JDRPaint getFillPaint()
   {
      return getPaintAttribute("fill", null);
   }

   public JDRPaint getDefaultPaint()
   {
      return getPaintAttribute("color", null);
   }

   public SVGNumberAttribute getNumberAttribute(String attrName)
   {
      SVGAttribute attr = getAttribute(attrName, null);

      if (attr != null && attr instanceof SVGNumberAttribute)
      {
         return (SVGNumberAttribute)attr;
      }

      return null;
   }

   public double getDoubleAttribute(String attrName, double defValue)
   {
      SVGAttribute attr = getAttribute(attrName, null);

      if (attr != null && attr instanceof SVGNumberAttribute)
      {
         return ((SVGNumberAttribute)attr).doubleValue(this);
      }

      return defValue;
   }

   public int getIntegerAttribute(String attrName, int defValue)
   {
      SVGAttribute attr = getAttribute(attrName, null);

      if (attr != null && attr instanceof SVGNumberAttribute)
      {
         return ((SVGNumberAttribute)attr).intValue(this);
      }

      return defValue;
   }

   public JDRLength getLengthAttribute(String attrName)
   {
      return getLengthAttribute(attrName, null);
   }

   public JDRLength getLengthAttribute(String attrName, JDRLength defValue)
   {
      SVGAttribute attr = getAttribute(attrName, null);

      if (attr != null && attr instanceof SVGLength)
      {
         return ((SVGLengthAttribute)attr).lengthValue(this);
      }

      return defValue;
   }

   public SVGLength[] getLengthArrayAttribute(String attrName)
   {
      SVGAttribute attr = getAttribute(attrName, null);

      if (attr != null && attr instanceof SVGLengthArrayAttribute)
      {
         return ((SVGLengthArrayAttribute)attr).getArray();
      }

      return null;
   }

   public Path2D getPathDataAttribute()
     throws InvalidFormatException
   {
      SVGAttribute attr = getAttribute("d", null);

      if (attr != null && attr instanceof SVGPathDataAttribute)
      {
         return ((SVGPathDataAttribute)attr).getPath(this);
      }

      return null;
   }

   public SVGDashArrayAttribute getDashArrayAttribute()
     throws InvalidFormatException
   {
      SVGAttribute attr = getAttribute("stroke-dasharray", null);

      if (attr != null && attr instanceof SVGDashArrayAttribute)
      {
         return (SVGDashArrayAttribute)attr;
      }

      return null;
   }

   public boolean isDisplayed()
   {
      return getIntegerAttribute("display", SVGDisplayStyleAttribute.INLINE)
         != SVGDisplayStyleAttribute.NONE;
   }

   public boolean isVisible()
   {
      return getIntegerAttribute("visibility", SVGVisibilityStyleAttribute.VISIBLE)
         == SVGVisibilityStyleAttribute.VISIBLE;
   }

   public String getHref()
   {
      Object attr = attributeSet.getAttribute("href");

      if (attr instanceof SVGStringAttribute)
      {
         return ((SVGStringAttribute)attr).getString();
      }

      attr = attributeSet.getAttribute("xlink:href");

      if (attr instanceof SVGStringAttribute)
      {
         return ((SVGStringAttribute)attr).getString();
      }

      return null;
   }

   public AffineTransform getTransform()
   {
      Object attr = attributeSet.getAttribute("transform");

      if (attr == null)
      {
         return null;
      }

      if (attr instanceof SVGTransformAttribute)
      {
         return ((SVGTransformAttribute)attr).getTransform();
      }

      return null;
   }

   public void applyTextAttributes(JDRTextual text)
     throws InvalidFormatException
   {
      JDRPaint paint = getDefaultPaint();

      if (paint != null)
      {
         text.setTextPaint(paint);
      }

      SVGNumberAttribute numAttr = getNumberAttribute("opacity");

      if (numAttr != null)
      {
         text.getTextPaint().setAlpha(numAttr.doubleValue(this));
      }

      SVGAttribute fontFam = getAttribute("font-family", null);

      if (fontFam != null)
      {
         fontFam.applyTo(this, (JDRCompleteObject)text);
      }

      SVGAttribute fontSize = getAttribute("font-size", null);

      if (fontSize != null)
      {
         fontSize.applyTo(this, (JDRCompleteObject)text);
      }

      SVGAttribute fontStyle = getAttribute("font-style", null);

      if (fontStyle != null)
      {
         fontStyle.applyTo(this, (JDRCompleteObject)text);
      }

      SVGAttribute fontVariant = getAttribute("font-variant", null);

      if (fontVariant != null)
      {
         fontVariant.applyTo(this, (JDRCompleteObject)text);
      }

      SVGAttribute fontWeight = getAttribute("font-weight", null);

      if (fontWeight != null)
      {
         fontWeight.applyTo(this, (JDRCompleteObject)text);
      }
   }

   public void applyShapeAttributes(JDRShape shape)
     throws InvalidFormatException
   {
      SVGAttribute attr;
      SVGNumberAttribute numAttr;

      attr = getElementAttribute("stroke-width");

      if (attr != null)
      {
         attr.applyTo(this, shape);
      }

      attr = getElementAttribute("stroke-linecap");

      if (attr != null)
      {
         attr.applyTo(this, shape);
      }

      attr = getElementAttribute("stroke-linejoin");

      if (attr != null)
      {
         attr.applyTo(this, shape);
      }

      attr = getElementAttribute("stroke-miterlimit");

      if (attr != null)
      {
         attr.applyTo(this, shape);
      }

      attr = getElementAttribute("fill-rule");

      if (attr != null)
      {
         attr.applyTo(this, shape);
      }

      SVGDashArrayAttribute dashAttr = getDashArrayAttribute();

      if (dashAttr != null)
      {
         dashAttr.applyTo(this, shape);
      }

      attr = getElementAttribute("stroke-dashoffset");

      if (attr != null)
      {
         attr.applyTo(this, shape);
      }

      JDRPaint paint = getFillPaint();

      if (paint != null)
      {
         shape.setFillPaint(paint);
      }

      numAttr = getNumberAttribute("fill-opacity");

      if (numAttr != null && numAttr.getValue() != null)
      {
         shape.getFillPaint().setAlpha(numAttr.doubleValue(this));
      }

      paint = getLinePaint();

      if (paint != null)
      {
         shape.setLinePaint(paint);
      }

      numAttr = getNumberAttribute("stroke-opacity");

      if (numAttr != null && numAttr.getValue() != null)
      {
         shape.getLinePaint().setAlpha(numAttr.doubleValue(this));
      }
   }


   public void makeEqual(SVGAbstractElement element)
   {
      contents.setLength(0);
      contents.append(element.contents);
      parent = element.parent;
      attributeSet = (SVGAttributeSet)element.attributeSet.clone();
      styles = (element.styles == null ? null : (SVGStyles)element.styles.clone());
      id = element.id;
      cssClassList = element.cssClassList;

      children.clear();
      children.addAll(element.children);
   }

   public JDRMessage getMessageSystem()
   {
      return handler.getMessageSystem();
   }

   public void warning(Throwable e)
   {
      getMessageSystem().getPublisher().publishMessages(
        MessageInfo.createWarning(e));
   }

   public void warning(String msg)
   {
      getMessageSystem().getPublisher().publishMessages(
        MessageInfo.createWarning(msg));
   }

   public String getMessageWithFallback(String label,
       String fallbackFormat, Object... params)
   {
      return getMessageSystem().getMessageWithFallback(label, fallbackFormat, params);
   }

   public SVG getSVG()
   {
      return handler.getSVG();
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return handler.getCanvasGraphics();
   }

   public abstract Object clone();

   @Override
   public String toString()
   {
      return String.format("%s[name=%s,id=%s,contents=%s,attributes=%s,styles=%s]",
        getClass().getSimpleName(), getName(), id, contents, attributeSet,styles);
   }

   protected SVGHandler handler;
   private StringBuilder contents;
   protected SVGAbstractElement parent;
   protected Vector<SVGAbstractElement> children;

   protected SVGAttributeSet attributeSet;

   protected SVGStyles styles;

   protected String id;
   protected String[] cssClassList;

   // pseudo-classes and hierarchy not supported
   private static final Pattern STYLE_PATTERN =
      Pattern.compile("\\s*([^\\{]+)\\s*\\{([^\\}]*)\\}\\s*(.*)",
      Pattern.DOTALL);

   private static final Pattern STYLE_ATTR_PATTERN =
      Pattern.compile("\\s*([^:]+)\\s*:\\s*([^;]+)\\s*;?\\s*",
      Pattern.DOTALL);

   private static final Pattern CSS_IGNORED =
      Pattern.compile("(\\/\\*.+?\\*\\/|\\@import\\(.+?\\);)",
       Pattern.DOTALL);
}
