package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import java.awt.geom.Point2D;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGEllipseElement extends SVGShape
{
   public SVGEllipseElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "ellipse";
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("cx", attr);
      addAttribute("cy", attr);
      addAttribute("rx", attr);
      addAttribute("ry", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("cx"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("cy"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, false);
      }
      else if (name.equals("rx"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("ry"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, false);
      }
      else
      {
         attr = super.createElementAttribute(name, value);
      }

      return attr;
   }

   @Override
   public JDRShape createShape(CanvasGraphics cg)
   {
      Point2D p = new Point2D.Double(
                    getDoubleAttribute("cx", 0),
                    getDoubleAttribute("cy", 0));

      double rx = 0;
      double ry = 0;

      SVGLengthAttribute rxAttr = getLengthAttribute("rx");
      SVGLengthAttribute ryAttr = getLengthAttribute("ry");

      if (rxAttr == null || rxAttr.isAuto())
      {
         if (ryAttr != null && !ryAttr.isAuto())
         {
            ry = ryAttr.doubleValue(this);
            rx = ry;
         }
      }
      else if (ryAttr == null || ryAttr.isAuto())
      {
         rx = rxAttr.doubleValue(this);
         ry = rx;
      }
      else
      {
         rx = rxAttr.doubleValue(this);
         ry = ryAttr.doubleValue(this);
      }

      if (rx <= 0 || ry <= 0)
      {
         return null;
      }

      JDRPath shape = JDRPath.constructEllipse(cg, p, rx, ry);

      return shape;
   }

   @Override
   public Object clone()
   {
      SVGEllipseElement element = new SVGEllipseElement(handler, null);

      element.makeEqual(this);

      return element;
   }
}
