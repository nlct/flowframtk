package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGTextElement extends SVGAbstractElement
{
   public SVGTextElement(SVGHandler handler,
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   protected void applyAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.applyAttributes(uri, attr);

      applyTextAttributes(uri, attr);

      addAttribute("x", attr);
      addAttribute("y", attr);
   }

   public String getName()
   {
      return "text";
   }

   public JDRCompleteObject addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      CanvasGraphics cg = group.getCanvasGraphics();

      Point2D p = new Point2D.Double(
                    getDoubleAttribute("x", 0),
                    getDoubleAttribute("y", 0));

      JDRText textArea = new JDRText(cg, p, handler.createDefaultFont(),
        getContents());

      textArea.setTextPaint(handler.createDefaultTextPaint());

      AffineTransform af = getTransform();

      if (af != null)
      {
         double[] matrix = new double[6];

         af.getMatrix(matrix);

         textArea.transform(matrix);
      }

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
         textArea.setDescription(desc.replaceAll("\\R", " "));
      }

      applyTextAttributes(textArea);

      group.add(textArea);

      return textArea;
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

   public Object clone()
   {
      try
      {
         SVGTextElement element = new SVGTextElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   String title, description;
}
