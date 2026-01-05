package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.AffineTransform;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGGroupElement extends SVGAbstractElement
{
   public SVGGroupElement(SVGHandler handler, 
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   protected void applyAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.applyAttributes(uri, attr);

      addAttribute("stroke", attr);
      addAttribute("fill", attr);
      addAttribute("stroke-width", attr);
      addAttribute("stroke-opacity", attr);
      addAttribute("fill-opacity", attr);
      addAttribute("fill-rule", attr);
      addAttribute("stroke-linecap", attr);
      addAttribute("stroke-linejoin", attr);
      addAttribute("stroke-miterlimit", attr);
      addAttribute("stroke-dashoffset", attr);
      addAttribute("stroke-dasharray", attr);

   }

   public String getName()
   {
      return "g";
   }

   public JDRCompleteObject addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      JDRGroup subgroup = new JDRGroup(group.getCanvasGraphics());
      group.add(subgroup);

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

      for (SVGAbstractElement element : children)
      {
         getMessageSystem().getPublisher().publishMessages(
           MessageInfo.createVerbose(1, 
             "Adding "+element.getName()+" to subgroup"));

         element.addToImage(subgroup);
      }

      AffineTransform af = getTransform();

      if (af != null)
      {
         double[] matrix = new double[6];

         af.getMatrix(matrix);

         subgroup.transform(matrix);
      }

      return subgroup;
   }

   public Object clone()
   {
      try
      {
         SVGGroupElement element = new SVGGroupElement(handler,null, null, null);

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
}
