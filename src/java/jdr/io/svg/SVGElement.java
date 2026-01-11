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

      for (SVGAbstractElement element : children)
      {
         getMessageSystem().getPublisher().publishMessages(
            MessageInfo.createVerbose(1, "Adding "+element.getName()));

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
      return width.getStorageValue(parent, true);
   }

   @Override
   public double getViewportHeight()
   {
      return height.getStorageValue(parent, false);
   }

   public void makeEqual(SVGElement element)
   {
      super.makeEqual(element);
      width = element.width;
      height = element.height;
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

   private SVGLength width, height;
}
