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

   protected void applyAttributes(String uri, Attributes attr)
      throws InvalidFormatException
   {
      super.applyAttributes(uri, attr);

      String style = attr.getValue("style");

      if (style != null)
      {
         addAttributeSet(style);
      }

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

   public JDRPaint getLinePaint(CanvasGraphics cg)
   {
      return getPaintAttribute("stroke",
         new JDRTransparent(cg));
   }

   public JDRPaint getFillPaint(CanvasGraphics cg)
     throws InvalidFormatException
   {
      return getPaintAttribute("fill", new JDRColor(cg, 0,0,0));
   }

   public double getStrokeOpacity()
   {
      return getDoubleAttribute("stroke-opacity", 1.0);
   }

   public double getFillOpacity()
   {
      return getDoubleAttribute("fill-opacity", 1.0);
   }

   public JDRLength getPenWidth()
   {
      return getLengthAttribute("stroke-width", 
         new JDRLength(getCanvasGraphics(), 1.0, JDRUnit.bp));
   }

   public int getCapStyle()
   {
      return getIntegerAttribute("stroke-linecap", BasicStroke.CAP_BUTT);
   }

   public int getJoinStyle()
   {
      return getIntegerAttribute("stroke-linejoin", BasicStroke.JOIN_MITER);
   }

   public double getMiterLimit()
   {
      return getDoubleAttribute("stroke-miterlimit", 4.0);
   }

   public int getFillRule()
   {
      return getIntegerAttribute("fill-rule", GeneralPath.WIND_NON_ZERO);
   }

   public DashPattern getDashPattern(CanvasGraphics cg)
     throws InvalidFormatException
   {
      DashPattern dashPattern = getDashArrayAttribute();

      if (dashPattern == null)
      {
         return new DashPattern(cg);
      }

      if (dashPattern.getStoragePattern() == null)
      {
         return dashPattern;
      }

      dashPattern.setStorageOffset(
         (float)getDoubleAttribute("stroke-dashoffset", 0.0));

      return dashPattern;
   }

   public void applyShapeAttributes(JDRShape shape)
     throws InvalidFormatException
   {
      CanvasGraphics cg = shape.getCanvasGraphics();

      JDRBasicStroke stroke = new JDRBasicStroke(cg);

      stroke.setPenWidth(getPenWidth());
      stroke.setCapStyle(getCapStyle());
      stroke.setJoinStyle(getJoinStyle());
      stroke.setMitreLimit(getMiterLimit());
      stroke.setDashPattern(getDashPattern(cg));
      stroke.setWindingRule(getFillRule());

      shape.setStroke(stroke);

      JDRPaint fillPaint = getFillPaint(cg);
      fillPaint.setAlpha(getFillOpacity());

      shape.setFillPaint(fillPaint);

      JDRPaint linePaint = getLinePaint(cg);
      linePaint.setAlpha(getStrokeOpacity());

      shape.setLinePaint(linePaint);
   }

   public void addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      JDRShape shape = createShape(group.getCanvasGraphics());

      applyShapeAttributes(shape);

      AffineTransform af = getTransform();

      if (af != null)
      {
         double[] matrix = new double[6];

         af.getMatrix(matrix);

         shape.transform(matrix);
      }

      group.add(shape);
   }

   public abstract JDRShape createShape(CanvasGraphics cg)
     throws InvalidFormatException;
}
