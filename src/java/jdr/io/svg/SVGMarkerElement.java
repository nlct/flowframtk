package com.dickimawbooks.jdr.io.svg;

import java.util.regex.Pattern;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.JDRMarker;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGMarkerElement extends SVGAbstractElement
{
   public SVGMarkerElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "marker";
   }

   public boolean isDisplayed()
   {
      return false;
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("markerHeight", attr);
      addAttribute("markerWidth", attr);
      addAttribute("markerUnits", attr);// userSpaceOnUse | strokeWidth
      addAttribute("orient", attr);// auto | auto-start-reverse | angle
      addAttribute("refX", attr);// left | right | center | coordinate
      addAttribute("refY", attr);// top | center | bottom | coordinate
      addAttribute("viewBox", attr);

      addShapeAttributes(uri, attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("markerWidth"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("markerHeight"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, false);
      }
      else if (name.equals("viewBox"))
      {
         attr = SVGLengthArrayAttribute.valueOf(handler, name, value);
      }
      else if (name.equals("refX"))
      {
         attr = SVGMarkerRefAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("refY"))
      {
         attr = SVGMarkerRefAttribute.valueOf(handler, name, value, false);
      }
      else if (name.equals("orient"))
      {
         attr = SVGMarkerOrientAttribute.valueOf(handler, value);
      }
      else if (name.equals("markerUnits"))
      {
         attr = SVGMarkerUnitAttribute.valueOf(handler, value);
      }
      else
      {
         attr = createPathStyleAttribute(name, value);

         if (attr == null)
         {
            attr = super.createElementAttribute(name, value);
         }
      }

      return attr;
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      orientAttr = getMarkerOrientAttribute("orient");

      SVGLengthAttribute widthAttr = getLengthAttribute("markerWidth");
      SVGLengthAttribute heightAttr = getLengthAttribute("markerHeight");
      SVGLengthAttribute[] viewBox = getLengthArrayAttribute("viewBox");

      unitStrokeWidth = true;

      SVGMarkerUnitAttribute markerUnitAttr = getMarkerUnitAttribute("markerUnits");
      if (markerUnitAttr != null && markerUnitAttr.isUserSpaceOnUse())
      {
         unitStrokeWidth = false;
      }

      markerWidth = 3;
      markerHeight = 3;

      JDRUnit unit = handler.getDefaultUnit();
      JDRUnit storageUnit = handler.getStorageUnit();

      if (widthAttr != null && widthAttr.getValue() != null)
      {
         markerWidth = storageUnit.toUnit(widthAttr.doubleValue(this), unit);
      }

      if (heightAttr != null && heightAttr.getValue() != null)
      {
         markerHeight = storageUnit.toUnit(heightAttr.doubleValue(this), unit);
      }

      if (viewBox != null)
      {
         if (viewBox.length != 4)
         {
            throw new CoordPairsRequiredException(this, "viewBox");
         }

         double x1 = storageUnit.toUnit(viewBox[0].getStorageValue(this, true), unit);
         double y1 = storageUnit.toUnit(viewBox[1].getStorageValue(this, false), unit);
         double x2 = storageUnit.toUnit(viewBox[2].getStorageValue(this, true), unit);
         double y2 = storageUnit.toUnit(viewBox[3].getStorageValue(this, false), unit);

         bounds = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
      }
      else
      {
         bounds = new Rectangle2D.Double(0, 0, markerWidth, markerHeight);
      }

      SVGMarkerRefAttribute refXAttr = getMarkerRefAttribute("refX");
      SVGMarkerRefAttribute refYAttr = getMarkerRefAttribute("refY");

      double refX = 0;
      double refY = 0;

      if (refXAttr != null)
      {
         switch (refXAttr.getRefType())
         {
            case SVGMarkerRefAttribute.MIN:
               refX = bounds.getMinX();
            break;
            case SVGMarkerRefAttribute.MAX:
               refX = bounds.getMaxX();
            break;
            case SVGMarkerRefAttribute.CENTER:
               refX = bounds.getMinX() + 0.5 * bounds.getWidth();
            break;
            case SVGMarkerRefAttribute.COORD:
               refX = storageUnit.toUnit(refXAttr.doubleValue(this), unit);
            break;
         }
      }

      if (refYAttr != null)
      {
         switch (refYAttr.getRefType())
         {
            case SVGMarkerRefAttribute.MIN:
               refY = bounds.getMinY();
            break;
            case SVGMarkerRefAttribute.MAX:
               refY = bounds.getMaxY();
            break;
            case SVGMarkerRefAttribute.CENTER:
               refY = bounds.getMinY() + 0.5 * bounds.getHeight();
            break;
            case SVGMarkerRefAttribute.COORD:
               refY = storageUnit.toUnit(refYAttr.doubleValue(this), unit);
            break;
         }
      }

      refPoint = new Point2D.Double(refX, refY);

      super.startElement();
   }

   protected SVGMarkerRefAttribute getMarkerRefAttribute(String attrName)
   {
      SVGAttribute attr = getAttribute(attrName, null);

      if (attr!= null && attr instanceof SVGMarkerRefAttribute)
      {
         return (SVGMarkerRefAttribute)attr;
      }

      return null;
   }

   protected SVGMarkerOrientAttribute getMarkerOrientAttribute(String attrName)
   {
      SVGAttribute attr = getAttribute(attrName, null);

      if (attr!= null && attr instanceof SVGMarkerOrientAttribute)
      {
         return (SVGMarkerOrientAttribute)attr;
      }

      return null;
   }

   protected SVGMarkerUnitAttribute getMarkerUnitAttribute(String attrName)
   {
      SVGAttribute attr = getAttribute(attrName, null);

      if (attr!= null && attr instanceof SVGMarkerUnitAttribute)
      {
         return (SVGMarkerUnitAttribute)attr;
      }

      return null;
   }

   @Override
   public void endElement() throws InvalidFormatException
   {
      // TODO add custom JDRMarker to support arbitrary shapes

      JDRMarker parentM = null;

      CanvasGraphics cg = getCanvasGraphics();
      JDRUnit unit = handler.getDefaultUnit();

      boolean isDot = DOT_PATTERN.matcher(id).find();
      boolean isBox = BOX_PATTERN.matcher(id).find();
      boolean isBar = BAR_PATTERN.matcher(id).find();
      boolean isTriangle = TRIANGLE_PATTERN.matcher(id).find();
      boolean isStealth = STEALTH_PATTERN.matcher(id).find();
      boolean isUp = UP_PATTERN.matcher(id).find();
      boolean isDown = DOWN_PATTERN.matcher(id).find();

      for (int i = 0, j = 0; i < children.size() && j < 3; i++)
      {
         SVGAbstractElement child = children.get(i);

         if (child instanceof SVGShape)
         {
            SVGShape shape = (SVGShape)child;

            JDRShape jdrShape = shape.getJDRShape();

            if (jdrShape == null) continue;

            // for now just guess an approximate predefined marker

            int markerType = JDRMarker.ARROW_NONE;

            JDRPaint linePaint = shape.getLinePaint();
            JDRPaint fillPaint = shape.getFillPaint();

            if (linePaint == null)
            {
               linePaint = getLinePaint();
            }

            if (linePaint != null)
            {
               jdrShape.setLinePaint(linePaint);
            }

            if (fillPaint == null)
            {
               fillPaint = getFillPaint();
            }

            if (fillPaint != null)
            {
               jdrShape.setFillPaint(fillPaint);
            }

            boolean isFilled = (fillPaint == null
               || !(fillPaint instanceof JDRTransparent));

            String shapeName = shape.getName();

            if (isDot || shapeName.equals("circle") || shapeName.equals("ellipse"))
            {
               if (isFilled)
               {
                  markerType = JDRMarker.ARROW_DOTFILLED;
               }
               else
               {
                  markerType = JDRMarker.ARROW_DOTOPEN;
               }
            }
            else if (isBox || shapeName.equals("rect"))
            {
               if (isFilled)
               {
                  markerType = JDRMarker.ARROW_BOXFILLED;
               }
               else
               {
                  markerType = JDRMarker.ARROW_BOXOPEN;
               }
            }
            else if (isBar)
            {
               markerType = JDRMarker.ARROW_ALT_BAR;
            }
            else if (isStealth)
            {
               markerType = JDRMarker.ARROW_STEALTH2;
            }
            else if (isTriangle)
            {
               if (isUp)
               {
                  if (isFilled)
                  {
                     markerType = JDRMarker.ARROW_TRIANGLE_UP_FILLED;
                  }
                  else
                  {
                     markerType = JDRMarker.ARROW_TRIANGLE_UP_OPEN;
                  }
               }
               else if (isDown)
               {
                  if (isFilled)
                  {
                     markerType = JDRMarker.ARROW_TRIANGLE_DOWN_FILLED;
                  }
                  else
                  {
                     markerType = JDRMarker.ARROW_TRIANGLE_DOWN_OPEN;
                  }
               }
               else
               {
                  markerType = JDRMarker.ARROW_OFFSET_TRIANGLE2;
               }
            }
            else if (shapeName.equals("polygon") || jdrShape.isPolygon())
            {
               markerType = JDRMarker.ARROW_CROSS;

               int numSeg = ((JDRPath)jdrShape).size();

               if (numSeg == 3)
               {
                  markerType = JDRMarker.ARROW_OFFSET_TRIANGLE2;
               }
               else if (numSeg == 4)
               {
                  markerType = JDRMarker.ARROW_STEALTH2;
               }
            }
            else
            {
               if (isFilled)
               {
                  markerType = JDRMarker.ARROW_DOTFILLED;
               }
               else
               {
                  markerType = JDRMarker.ARROW_DOTOPEN;
               }
            }

            if (markerType != JDRMarker.ARROW_NONE)
            {
               JDRMarker m = JDRMarker.getPredefinedMarker(cg, markerType);

               if (m.supportsWidth())
               {
                  m.setWidth(new JDRLength(cg, markerWidth, unit));
               }

               m.setSize(new JDRLength(cg, markerHeight, unit));

               if (isFilled)
               {
                  m.setFillPaint(fillPaint);
               }
               else if (linePaint != null)
               {
                  m.setFillPaint(linePaint);
               }

               JDRAngle angle = null;
               int orientType = getOrientType();

               if (orientAttr != null)
               {
                  angle = orientAttr.getAngle();
               }

               if (angle == null)
               {
                  m.setOrient(true);
               }
               else
               {
                  m.setOrient(false, angle);
               }

               if (jdrMarker == null)
               {
                  jdrMarker = m;
                  parentM = jdrMarker;
               }
               else
               {
                  parentM.setCompositeMarker(m);
                  parentM = m;
               }

               // only support up to three
               j++;
            }
         }
      }

      super.endElement();
   }

   public int getOrientType()
   {
      int orientType = SVGMarkerOrientAttribute.ZERO;

      if (orientAttr != null)
      {
         orientType = orientAttr.getOrientType();
      }

      return orientType;
   }

   public JDRAngle getOrientAngle()
   {
      if (orientAttr != null) 
      {
         return orientAttr.getAngle();
      }

      return null;
   }

   public boolean isUnitStrokeWidth()
   {
      return unitStrokeWidth;
   }

   public Point2D getRefPoint()
   {
      return refPoint;
   }

   public Rectangle2D getViewportBounds()
   {
      return bounds;
   }

   public double getMarkerWidth()
   {
      return markerWidth;
   }

   public double getMarkerHeight()
   {
      return markerHeight;
   }

   @Override
   public double getViewportWidth()
   {
      return bounds == null ? markerWidth : bounds.getWidth();
   }

   @Override
   public double getViewportHeight()
   {
      return bounds == null ? markerHeight : bounds.getHeight();
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
   {
      return null;
   }

   public JDRMarker getMarker()
   {
      return jdrMarker;
   }

   @Override
   public Object clone()
   {
      SVGMarkerElement element = new SVGMarkerElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   public void makeEqual(SVGMarkerElement other)
   {
      super.makeEqual(other);

      markerWidth = other.markerWidth;
      markerHeight = other.markerHeight;
      unitStrokeWidth = other.unitStrokeWidth;

      if (other.orientAttr == null)
      {
         orientAttr = null;
      }
      else if (orientAttr == null)
      {
         orientAttr = (SVGMarkerOrientAttribute)other.orientAttr.clone();
      }
      else
      {
         orientAttr.makeEqual((SVGAttribute)other.orientAttr);
      }

      if (other.bounds == null)
      {
         bounds = null;
      }
      else if (bounds == null) 
      {
         bounds = new Rectangle2D.Double(
           other.bounds.getX(), other.bounds.getY(),
           other.bounds.getWidth(), other.bounds.getHeight());
      }
      else
      {
         bounds.setRect(other.bounds);
      }

      if (other.jdrMarker == null)
      {
         jdrMarker = null;
      }
      else if (jdrMarker == null)
      {
         jdrMarker = (JDRMarker)other.jdrMarker.clone();
      }
      else
      {
         other.jdrMarker.makeOtherEqual(jdrMarker);
      }

      if (other.refPoint == null)
      {
         refPoint = null;
      }
      else if (refPoint == null) 
      {
         refPoint = new Point2D.Double(
           other.refPoint.getX(), other.refPoint.getY());
      }
      else
      {
         refPoint.setLocation(other.refPoint);
      }

   }

   @Override
   public void setTitle(String title) { }
   @Override
   public void setDescription(String desc) { }

   double markerWidth, markerHeight;
   Rectangle2D bounds;
   JDRMarker jdrMarker;
   Point2D.Double refPoint;

   boolean unitStrokeWidth;

   SVGMarkerOrientAttribute orientAttr;

   static final Pattern DOT_PATTERN = Pattern.compile("dot|ball|circle");
   static final Pattern BOX_PATTERN = Pattern.compile("box|square");
   static final Pattern BAR_PATTERN = Pattern.compile("bar|line|pipe");
   static final Pattern TRIANGLE_PATTERN = Pattern.compile("triangle|arrow");
   static final Pattern STEALTH_PATTERN = Pattern.compile("stealth|barb");
   static final Pattern UP_PATTERN = Pattern.compile("up|north");
   static final Pattern DOWN_PATTERN = Pattern.compile("down|south");
}
