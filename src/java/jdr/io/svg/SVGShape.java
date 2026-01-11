package com.dickimawbooks.jdr.io.svg;

import java.awt.BasicStroke;
import java.awt.geom.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public abstract class SVGShape extends SVGAbstractElement
{
   public SVGShape(SVGHandler handler,
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   @Override
   protected void addAttributes(String uri, Attributes attr)
      throws InvalidFormatException
   {
      super.addAttributes(uri, attr);

      addShapeAttributes(uri, attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws InvalidFormatException
   {
      SVGAttribute attr = createPathStyleAttribute(name, value);

      if (attr == null)
      {
         return super.createElementAttribute(name, value);
      }

      return attr;
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      JDRShape shape = createShape(group.getCanvasGraphics());

      shape.setLinePaint(handler.createDefaultLinePaint());
      shape.setFillPaint(handler.createDefaultFillPaint());
      shape.setStroke(handler.createDefaultStroke());

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
         shape.setDescription(desc.replaceAll("\\R", " "));
      }

      applyShapeAttributes(shape);

      AffineTransform af = getTransform();

      if (af != null)
      {
         double[] matrix = new double[6];

         af.getMatrix(matrix);

         shape.transform(matrix);
      }

      group.add(shape);

      return shape;
   }

   public abstract JDRShape createShape(CanvasGraphics cg)
     throws InvalidFormatException;

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
