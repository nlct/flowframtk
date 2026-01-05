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
           +((SVGAttribute)attributeSet.getAttribute(attrName)).getValue());
      }
   }

   protected void applyAttributes(String uri, Attributes attr)
      throws InvalidFormatException
   {
      addAttribute("id", attr);
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
      Matcher m = stylePattern.matcher(ruleList);

      while (m.matches())
      {
         String name = m.group(1);
         String attrList = m.group(2);
         m = stylePattern.matcher(m.group(3));

         SVGAttributeSet atSet = createAttributeSet(attrList);

         String[] split = name.split("\\s*,\\s*");

         for (int i = 0; i < split.length; i++)
         {
            String elemName = split[i].trim();

            SVGAttributeSet set = (SVGAttributeSet)atSet.clone();
            set.setName(elemName);
            attributeSet.addAttribute(elemName, set);
         }
      }
   }

   public void addAttribute(String name, Attributes saxAttr)
     throws InvalidFormatException
   {
      String value = saxAttr.getValue(name);

      if (value != null
        && getElementAttribute(name) == null
        && attributeSet.getAttribute(name) == null)
      {
         addAttribute(getStyleAttribute(name, value));
      }
   }

   public void addAttribute(SVGAttribute attr)
   {
      if (attr.getValue() != null)
      {
         attributeSet.addAttribute(attr.getName(), attr);
      }
   }

   public void addAttributeSet(String attrList)
   {
      attributeSet.addAttributes(createAttributeSet(attrList));
   }

   protected SVGAttributeSet createAttributeSet(String attrList)
   {
      SVGAttributeSet atSet 
         = new SVGAttributeSet(getId());

      String[] split = attrList.split("\\s*;\\s*");

      for (int i = 0; i < split.length; i++)
      {
         String attrDef = split[i].trim();

         Matcher attrM = styleAttrPattern.matcher(attrDef);

         if (attrM.matches())
         {
            try
            {
               SVGAttribute attr = getStyleAttribute(attrM.group(1), attrM.group(2));

               atSet.addAttribute(attr.getName(), attr);
            }
            catch (InvalidFormatException e)
            {
               getMessageSystem().getPublisher().publishMessages(
                 MessageInfo.createWarning(e));
            }
         }
      }

      return atSet;
   }

   public SVGAttribute getStyleAttribute(String name, String style)
     throws InvalidFormatException
   {
      SVGAttribute attr = null;

      if (name.equals("stroke"))
      {
         attr = new SVGPaintAttribute(handler, name, style,
            getPaintAttribute("color", null));
      }
      else if (name.equals("fill"))
      {
         attr = new SVGPaintAttribute(handler, name, style,
            getPaintAttribute("color", null));
      }
      else if (name.equals("stroke-width"))
      {
         attr = new SVGLengthAttribute(handler, name, style);
      }
      else if (name.equals("stroke-opacity"))
      {
         attr = new SVGDoubleAttribute(handler, name, style);
      }
      else if (name.equals("fill-opacity"))
      {
         attr = new SVGDoubleAttribute(handler, name, style);
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
         attr = new SVGDoubleAttribute(handler, name, style);
      }
      else if (name.equals("stroke-dashoffset"))
      {
         attr = new SVGLengthAttribute(handler, name, style);
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
      else if (name.equals("id") || name.equals("href") || name.equals("xlink:href"))
      {
         attr = new SVGStringAttribute(handler, name, style);
      }
      else if (name.equals("transform"))
      {
         attr = new SVGTransformAttribute(handler, style);
      }

      if (attr == null)
      {
         throw new UnknownSVGAttributeException(name);
      }

      return attr;
   }

   public Object getElementAttribute(String attrName)
   {
      return getElementAttribute(getName(), attrName);
   }

   public Object getElementAttribute(String elementName, String attrName)
   {
      SVGAttributeSet atSet = getAttributeSet(elementName);

      if (atSet != null)
      {
         Object attr = atSet.getAttribute(attrName);

         if (attr != null)
         {
            return attr;
         }
      }

      if (parent != null)
      {
         return parent.getElementAttribute(elementName, attrName);
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

   public SVGAttributeSet getAttributeSet()
   {
      return getAttributeSet(getName());
   }

   public SVGAttributeSet getAttributeSet(String element)
   {
      Object attr = attributeSet.getAttribute(element);

      if (attr != null && attr instanceof SVGAttributeSet)
      {
         return (SVGAttributeSet)attr;
      }

      if (parent != null)
      {
         SVGDefsElement defs = parent.getChildDefs();

         if (defs != null)
         {
            attr = defs.attributeSet.getAttribute(element);

            if (attr != null && attr instanceof SVGAttributeSet)
            {
               SVGAttributeSet atSet = (SVGAttributeSet)attr;

               if (element.equals(atSet.getName()))
               {
                  return atSet;
               }
            }
         }

         return parent.getAttributeSet(element);
      }

      return null;
   }

   public Object getAttribute(String element, String name, Object defValue)
   {
      Object attr = attributeSet.getAttribute(name);

      if (attr != null)
      {
         return attr;
      }

      attr = getElementAttribute(element, name);

      if (attr != null)
      {
         return attr;
      }

      if (parent != null)
      {
         return parent.getAttribute(element, name, defValue);
      }

      return defValue;
   }

   public boolean attributeExists(String name)
   {
      return attributeExists(getName(), name);
   }

   public boolean attributeExists(String element, String name)
   {
      Object attr = getAttribute(element, name, null);

      return attr != null;
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

   public JDRPaint getPaintAttribute(String name, JDRPaint defPaint)
   {
      return getPaintAttribute(getName(), name, defPaint);
   }

   public JDRPaint getPaintAttribute(String element, String name, JDRPaint defPaint)
   {
      Object attr = getAttribute(element, name, defPaint);

      if (attr instanceof SVGPaintAttribute)
      {
         return ((SVGPaintAttribute)attr).getPaint();
      }

      return defPaint;
   }

   public double getDoubleAttribute(String name, double defValue)
   {
      return getDoubleAttribute(getName(), name, defValue);
   }

   public double getDoubleAttribute(String element, String name, double defValue)
   {
      Object attr = getAttribute(element, name, null);

      if (attr != null && attr instanceof SVGNumberAttribute)
      {
         return ((SVGNumberAttribute)attr).doubleValue(this);
      }

      return defValue;
   }

   public int getIntegerAttribute(String name, int defValue)
   {
      return getIntegerAttribute(getName(), name, defValue);
   }

   public int getIntegerAttribute(String element, String name, int defValue)
   {
      Object attr = getAttribute(element, name, null);

      if (attr != null && attr instanceof SVGNumberAttribute)
      {
         return ((SVGNumberAttribute)attr).intValue(this);
      }

      return defValue;
   }

   public JDRLength getLengthAttribute(String name, JDRLength defValue)
   {
      return getLengthAttribute(getName(), name, defValue);
   }

   public JDRLength getLengthAttribute(String element, String name, JDRLength defValue)
   {
      Object attr = getAttribute(element, name, null);

      if (attr != null && attr instanceof SVGLength)
      {
         return ((SVGLengthAttribute)attr).lengthValue(this);
      }

      return defValue;
   }

   public SVGLength[] getLengthArrayAttribute(String name)
   {
      return getLengthArrayAttribute(getName(), name);
   }

   public SVGLength[] getLengthArrayAttribute(String element, String name)
   {
      Object attr = getAttribute(element, name, null);

      if (attr != null && attr instanceof SVGLengthArrayAttribute)
      {
         return ((SVGLengthArrayAttribute)attr).getArray();
      }

      return null;
   }

   public Path2D getPathDataAttribute()
     throws InvalidFormatException
   {
      return getPathDataAttribute(getName());
   }

   public Path2D getPathDataAttribute(String element)
     throws InvalidFormatException
   {
      Object attr = getAttribute(element, "d", null);

      if (attr != null && attr instanceof SVGPathDataAttribute)
      {
         return ((SVGPathDataAttribute)attr).getPath(this);
      }

      return null;
   }

   public DashPattern getDashArrayAttribute()
     throws InvalidFormatException
   {
      return getDashArrayAttribute(getName());
   }

   public DashPattern getDashArrayAttribute(String element)
     throws InvalidFormatException
   {
      Object attr = getAttribute(element, "stroke-dasharray", null);

      if (attr != null && attr instanceof SVGDashArrayAttribute)
      {
         return ((SVGDashArrayAttribute)attr).getDashPattern(this);
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

   public String getId()
   {
      Object attr = attributeSet.getAttribute("id");

      if (attr instanceof SVGStringAttribute)
      {
         return ((SVGStringAttribute)attr).getString();
      }

      return null;
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

   public void makeEqual(SVGAbstractElement element)
   {
      contents.setLength(0);
      contents.append(element.contents);
      parent = element.parent;
      attributeSet = (SVGAttributeSet)element.attributeSet.clone();

      children.clear();
      children.addAll(element.children);
   }

   public JDRMessage getMessageSystem()
   {
      return handler.getMessageSystem();
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
      return String.format("%s[name=%s,contents=%s,attributes=%s]",
        getClass().getSimpleName(), getName(), contents, attributeSet);
   }

   protected SVGHandler handler;
   private StringBuilder contents;
   protected SVGAbstractElement parent;
   protected Vector<SVGAbstractElement> children;

   protected SVGAttributeSet attributeSet;

   private static final Pattern stylePattern =
      Pattern.compile("\\s*([^\\{]+)\\s*\\{([^\\}]*)\\}\\s*(.*)");

   private static final Pattern styleAttrPattern =
      Pattern.compile("\\s*([^:]+)\\s*:\\s*([^;]+)\\s*;?\\s*");
}
