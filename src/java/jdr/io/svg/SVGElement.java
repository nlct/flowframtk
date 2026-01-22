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
   public void startElement() throws InvalidFormatException
   {
      super.startElement();

      if (viewBoxBounds == null)
      {
         double w = elementWidth;
         double h = elementHeight;

         if (parent != null)
         {
            if (elementWidth <= 0)
            {
               w = parent.getElementWidth();
            }

            if (elementHeight <= 0)
            {
               h = parent.getElementHeight();
            }
         }

         CanvasGraphics cg = getCanvasGraphics();

         if (w <= 0)
         {
            w = cg.getStoragePaperWidth();
         }

         if (h <= 0)
         {
            h = cg.getStoragePaperHeight();
         }

         viewBoxBounds = new Rectangle2D.Double(0, 0, w, h);
      }
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
}
