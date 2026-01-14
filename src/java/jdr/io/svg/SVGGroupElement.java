package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.AffineTransform;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGGroupElement extends SVGAbstractElement
{
   public SVGGroupElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "g";
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      JDRGroup subgroup = new JDRGroup(group.getCanvasGraphics());

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
         subgroup.setDescription(desc.replaceAll("\\R", " "));
      }

      // subgroup may be empty even if this element has children
      // (there's no guarantee that child elements will create
      // objects)

      for (SVGAbstractElement element : children)
      {
         handler.debugMessage("Adding "+element.getName()+" to subgroup");

         element.addToImage(subgroup);
      }

      if (subgroup.isEmpty()) return null;

      AffineTransform af = getTransform();

      if (af != null)
      {
         double[] matrix = new double[6];

         af.getMatrix(matrix);

         subgroup.transform(matrix);
      }

      if (subgroup.size() == 1)
      {
         JDRCompleteObject obj = subgroup.firstElement();

         group.add(obj);

         return obj;
      }
      else
      {
         group.add(subgroup);

         return subgroup;
      }
   }

   public Object clone()
   {
      SVGGroupElement element = new SVGGroupElement(handler, null);

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
