package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.AffineTransform;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGElement extends SVGAbstractElement
{
   public SVGElement(SVGHandler handler, 
      SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   @Override
   public String getName()
   {
      return "svg";
   }

   @Override
   protected void addAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.addAttributes(uri, attr);

      addAttribute("width", attr);
      addAttribute("height", attr);

      widthAttr = getLengthAttribute("width");
      heightAttr = getLengthAttribute("height");

      if (widthAttr != null && widthAttr.getValue() == null)
      {
         widthAttr = null;
      }

      if (heightAttr != null && heightAttr.getValue() == null)
      {
         heightAttr = null;
      }
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws InvalidFormatException
   {
      SVGAttribute attr;

      if (name.equals("width"))
      {
         attr = new SVGLengthAttribute(handler, name, value, true);
      }
      else if (name.equals("height"))
      {
         attr = new SVGLengthAttribute(handler, name, value, false);
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

   @Override
   public double getViewportWidth()
   {
      if (widthAttr == null)
      {
         return super.getViewportWidth();
      }

      return widthAttr.getStorageValue(parent, true);
   }

   @Override
   public double getViewportHeight()
   {
      if (heightAttr == null)
      {  
         return super.getViewportHeight();
      }

      return heightAttr.getStorageValue(parent, false);
   }

   public void makeEqual(SVGElement element)
   {
      super.makeEqual(element);
      widthAttr = element.widthAttr;
      heightAttr = element.heightAttr;
   }

   @Override
   public Object clone()
   {
      try
      {
         SVGElement element = new SVGElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
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

   private SVGLengthAttribute widthAttr, heightAttr;
}
