package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.Rectangle2D;

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
//      addAttribute("markerUnits", attr);// userSpaceOnUse | strokeWidth
//      addAttribute("orient", attr);// auto | auto-start-reverse | angle
//      addAttribute("refX", attr);// left | right | center | coordinate
//      addAttribute("refY", attr);// top | center | bottom | coordinate
      addAttribute("viewBox", attr);
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
      else
      {
         attr = super.createElementAttribute(name, value);
      }

      return attr;
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      SVGLengthAttribute widthAttr = getLengthAttribute("markerWidth");
      SVGLengthAttribute heightAttr = getLengthAttribute("markerHeight");
      SVGLengthAttribute[] viewBox = getLengthArrayAttribute("viewBox");

      markerWidth = 3;
      markerHeight = 3;

      JDRUnit unit = handler.getDefaultUnit();
      JDRUnit storageUnit = handler.getStorageUnit();

      if (widthAttr != null && widthAttr.getValue() == null)
      {
         markerWidth = storageUnit.toUnit(widthAttr.doubleValue(this), unit);
      }

      if (heightAttr != null && heightAttr.getValue() == null)
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

      super.startElement();
   }

   @Override
   public void endElement() throws InvalidFormatException
   {
      // TODO add custom JDRMarker to support arbitrary shapes

      JDRMarker parentM = null;

      CanvasGraphics cg = getCanvasGraphics();
      JDRUnit unit = handler.getDefaultUnit();

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

            boolean isFilled = (fillPaint == null
               || !(fillPaint instanceof JDRTransparent));

            String shapeName = shape.getName();

            if (shapeName.equals("circle") || shapeName.equals("ellipse"))
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
            else if (shapeName.equals("rect"))
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

   public Rectangle2D getViewportBounds()
   {
      return bounds;
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
   }

   @Override
   public void setTitle(String title) { }
   @Override
   public void setDescription(String desc) { }

   double markerWidth, markerHeight;
   Rectangle2D bounds;
   JDRMarker jdrMarker;
}
