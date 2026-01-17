package com.dickimawbooks.jdr.io.svg;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public abstract class SVGShape extends SVGAbstractElement
{
   public SVGShape(SVGHandler handler, String name, SVGAbstractElement parent)
   {
      super(handler, parent);
      this.name = name;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addShapeAttributes(uri, attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      SVGAttribute attr = createPathStyleAttribute(name, value);

      if (attr == null)
      {
         return super.createElementAttribute(name, value);
      }

      return attr;
   }

   @Override
   public void endElement() throws InvalidFormatException
   {
      jdrShape = createShape(getCanvasGraphics());

      if (jdrShape != null)
      {
         jdrShape.setLinePaint(handler.createDefaultLinePaint());
         jdrShape.setFillPaint(handler.createDefaultFillPaint());
         jdrShape.setStroke(handler.createDefaultStroke());

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
            jdrShape.setDescription(desc.replaceAll("\\R", " "));
         }

         AffineTransform af = getTransform();

         if (af != null)
         {
            double[] matrix = new double[6];

            af.getMatrix(matrix);

            jdrShape.transform(matrix);
         }
      }

      super.endElement();
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      if (jdrShape != null)
      {
         applyShapeAttributes(jdrShape);

         group.add(jdrShape);
      }

      return jdrShape;
   }

   protected abstract Shape constructShape() throws SVGException;

   public PathIterator getPathIterator() throws SVGException
   {
      if (shape == null)
      {
         shape = constructShape();
      }

      return shape == null ? null : shape.getPathIterator(null);
   }

   public JDRShape createShape(CanvasGraphics cg)
     throws InvalidFormatException
   {
      PathIterator pi = getPathIterator();

      JDRPath jdrPath = null;

      if (pi != null)
      {
         jdrPath = JDRPath.getPath(cg, pi);
      }

      return jdrPath;
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

   public void makeEqual(SVGShape other)
   {
      super.makeEqual(this);
      path = other.path;

      if (other.jdrShape == null)
      {
         jdrShape = null;
      }
      else if (jdrShape == null)
      {
         jdrShape = (JDRShape)other.jdrShape.clone();
      }
      else
      {
         jdrShape.makeEqual(other.jdrShape);
      }
   }

   String name;
   Path2D path;
   String description = null, title = null;
   Shape shape;
   JDRShape jdrShape;
}
