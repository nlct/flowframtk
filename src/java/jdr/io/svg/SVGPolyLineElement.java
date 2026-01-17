package com.dickimawbooks.jdr.io.svg;

import java.awt.Shape;
import java.awt.geom.Path2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPolyLineElement extends SVGShape
{
   public SVGPolyLineElement(SVGHandler handler, SVGAbstractElement parent)
   {
      this(handler, "polyline", parent);
   }

   public SVGPolyLineElement(SVGHandler handler, String name, SVGAbstractElement parent)
   {
      super(handler, name, parent);
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("points", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String style)
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("points"))
      {
         attr = SVGLengthArrayAttribute.valueOf(handler, name, style);
      }
      else
      {
         attr = super.createElementAttribute(name, style);
      }

      return attr;
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      points = getLengthArrayAttribute("points");

      if (points == null)
      {
         throw new ElementMissingAttributeException(this, "points");
      }

      if (points.length%2 == 1)
      {
         points = null;

         throw new CoordPairsRequiredException(this, "points");
      }

      super.startElement();
   }

   @Override
   protected Shape constructShape() throws SVGException
   {
      if (points == null) return null;

      Path2D.Double path = new Path2D.Double();

      double px = points[0].getStorageValue(this, true);
      double py = points[1].getStorageValue(this, false);

      path.moveTo(px, py);

      for (int i = 2; i < points.length; i += 2)
      {
         px = points[i].getStorageValue(this, true);
         py = points[i+1].getStorageValue(this, false);

         path.lineTo(px, py);
      }

      return path;
   }

   @Override
   public Object clone()
   {
      SVGPolyLineElement element = new SVGPolyLineElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   public void makeEqual(SVGPolyLineElement other)
   {
      super.makeEqual(other);

      if (other.points == null)
      {
         points = null;
      }
      else if (points == null || points.length != other.points.length)
      {
         points = new SVGLengthAttribute[other.points.length];

         for (int i = 0; i < other.points.length; i++)
         {
            points[i] = (SVGLengthAttribute)other.points[i];
         }
      }
      else
      {
         for (int i = 0; i < other.points.length; i++)
         {
            points[i].makeEqual((SVGAttribute)other.points[i]);
         }
      }
   }

   SVGLengthAttribute[] points;
}
