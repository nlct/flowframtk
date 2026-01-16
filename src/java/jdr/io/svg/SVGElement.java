package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGElement extends SVGAbstractElement
{
   public SVGElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "svg";
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("width", attr);
      addAttribute("height", attr);
      addAttribute("viewBox", attr);
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      SVGLengthAttribute widthAttr = getLengthAttribute("width");
      SVGLengthAttribute heightAttr = getLengthAttribute("height");
      SVGLengthAttribute[] viewBox = getLengthArrayAttribute("viewBox");

      if (widthAttr != null && widthAttr.getValue() == null)
      {
         widthAttr = null;
      }

      if (heightAttr != null && heightAttr.getValue() == null)
      {
         heightAttr = null;
      }

      hasWidth = (widthAttr != null);
      hasHeight = (heightAttr != null);

      width = hasWidth ? widthAttr.doubleValue(this) : 0.0;
      height = hasHeight ? heightAttr.doubleValue(this) : 0.0;

      if (viewBox != null)
      {
         if (viewBox.length != 4)
         {
            throw new CoordPairsRequiredException(this, "viewBox");
         }

         double x1 = viewBox[0].getStorageValue(this, true);
         double y1 = viewBox[1].getStorageValue(this, false);
         double x2 = viewBox[2].getStorageValue(this, true);
         double y2 = viewBox[3].getStorageValue(this, false);

         bounds = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);

         if (!hasWidth)
         {
            width = bounds.getWidth();
            hasWidth = true;
         }

         if (!hasHeight)
         {
            height = bounds.getHeight();
            hasHeight = true;
         }
      }
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("width"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("height"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, false);
      }
      else if (name.equals("viewBox"))
      {
         attr = SVGLengthArrayAttribute.valueOf(handler, name, value);
      }
      else
      {
         attr = super.createElementAttribute(name, value);
      }

      return attr;
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      String desc = null;

      if (title != null && !title.isEmpty())
      {
         desc = title;
      }
      else if (description != null && !description.isEmpty())
      {
         desc = description;
      }

      if (desc != null)
      {
         group.setDescription(desc.replaceAll("\\R", " "));
      }

      handler.setDeterminate(children.size());

      for (SVGAbstractElement element : children)
      {
         handler.incProgress();

         handler.debugMessage("Adding "+element.getName());

         if (element instanceof SVGElement)
         {
            JDRGroup subgroup = new JDRGroup(group.getCanvasGraphics());

            if (isDisplayed())
            {
               group.add(subgroup);
               element.addToImage(subgroup);
            }
         }
         else
         {
            if (element.isVisible() && element.isDisplayed())
            {
               element.addToImage(group);
            }
         }
      }

      AffineTransform af = getTransform();

      if (af != null)
      {
         double[] matrix = new double[6];

         af.getMatrix(matrix);

         group.transform(matrix);
      }

      return group;
   }

   public Rectangle2D getViewportBounds()
   {
      return bounds;
   }

   @Override
   public double getViewportWidth()
   {
      return hasWidth ? width : super.getViewportWidth();
   }

   @Override
   public double getViewportHeight()
   {
      return hasHeight ? height : super.getViewportHeight();
   }

   public void makeEqual(SVGElement element)
   {
      super.makeEqual(element);

      hasWidth = element.hasWidth;
      hasHeight = element.hasHeight;
      width = element.width;
      height = element.height;

      if (element.bounds == null)
      {
         bounds = null;
      }
      else if (bounds == null)
      {
         bounds = new Rectangle2D.Double(
           element.bounds.getX(), element.bounds.getY(),
           element.bounds.getWidth(), element.bounds.getHeight());
      }
      else
      {
         bounds.setRect(element.bounds);
      }
   }

   @Override
   public Object clone()
   {
      SVGElement element = new SVGElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   @Override
   public void setDescription(String text)
   {
      if (text != null)
      {
         description = text.trim();
      }
   }

   @Override
   public void setTitle(String text)
   {
      if (text != null)
      {
         title = text.trim();
      }
   }

   String description = null, title = null;

   private Rectangle2D bounds;
   boolean hasWidth, hasHeight;
   double width, height;
}
