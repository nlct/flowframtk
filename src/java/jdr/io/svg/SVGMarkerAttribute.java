package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.JDRMarker;

import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.ImportSettings;

public class SVGMarkerAttribute extends SVGAbstractAttribute
{
   protected SVGMarkerAttribute(SVGHandler handler, String name,
    boolean start, boolean mid, boolean end)
   {
      super(handler);
      this.name = name;
      this.start = start;
      this.mid = mid;
      this.end = end;
   }

   public static SVGMarkerAttribute createAll(SVGHandler handler,
       String valueString)
    throws SVGException
   {
      SVGMarkerAttribute attr = new SVGMarkerAttribute(handler, 
        "marker", true, true, true);
      attr.parse(valueString);
      return attr;
   }

   public static SVGMarkerAttribute createStart(SVGHandler handler,
       String valueString)
    throws SVGException
   {
      SVGMarkerAttribute attr = new SVGMarkerAttribute(handler, 
        "marker-start", true, false, false);
      attr.parse(valueString);
      return attr;
   }

   public static SVGMarkerAttribute createMid(SVGHandler handler,
       String valueString)
    throws SVGException
   {
      SVGMarkerAttribute attr = new SVGMarkerAttribute(handler, 
        "marker-mid", false, true, false);
      attr.parse(valueString);
      return attr;
   }

   public static SVGMarkerAttribute createEnd(SVGHandler handler,
       String valueString)
    throws SVGException
   {
      SVGMarkerAttribute attr = new SVGMarkerAttribute(handler, 
        "marker-end", false, false, true);
      attr.parse(valueString);
      return attr;
   }

   protected void parse(String str) throws SVGException
   {
      this.valueString = str;

      if (valueString == null || valueString.equals("none"))
      {
         markerElement = null;
      }
      else
      {
         SVGAbstractElement elem = handler.getAttributeValueRef(this);

         if (elem == null)
         {
            throw new UnknownReferenceException(handler, valueString);
         }
         else if (elem instanceof SVGMarkerElement)
         {
            markerElement = (SVGMarkerElement)elem;
         }
         else
         {
            throw new InvalidAttributeValueException(handler, getName(), valueString);
         }
      }
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public Object clone()
   {
      SVGMarkerAttribute attr = new SVGMarkerAttribute(handler, name,
       start, mid, end);

      attr.makeEqual(this);

      return attr;
   }

   public void makeEqual(SVGMarkerAttribute attr)
   {
      super.makeEqual(attr);
      markerElement = attr.markerElement;
      start = attr.start;
      mid = attr.mid;
      end = attr.end;
   }

   protected void addMarkerShape(JDRShape shape, JDRGroup group, Point2D pt,
     Point2D ref, boolean isFirst, Point2D gradient)
   throws InvalidFormatException
   {
      CanvasGraphics cg = group.getCanvasGraphics();

      double shiftX = pt.getX() - ref.getX();
      double shiftY = pt.getY() - ref.getY();

      JDRUnit unit = handler.getDefaultUnit();
      JDRUnit storageUnit = cg.getStorageUnit();

      Rectangle2D bounds = markerElement.getViewportBounds();
      double markerWidth = markerElement.getMarkerWidth();
      double markerHeight = markerElement.getMarkerHeight();

      JDRGroup subGrp = new JDRGroup(cg);

      for (int i = 0, n = markerElement.getChildCount(); i < n; i++)
      {
         SVGAbstractElement child = markerElement.getChild(i);

         child = (SVGAbstractElement)child.clone();
         child.attributeSet.removeAttribute("id");

         child.addToImage(subGrp);
      }

      JDRCompleteObject obj = null;

      if (subGrp.size() == 1)
      {
         obj = subGrp.firstElement();
      }
      else
      {
         obj = subGrp;
      }

      if (obj != null)
      {
         obj.setTag("marker");

         JDRAngle angle = null;
         double radians = 0.0;
         int orientType = markerElement.getOrientType();

         switch (orientType)
         {
            case SVGMarkerOrientAttribute.AUTO:
               isFirst = false;
            case SVGMarkerOrientAttribute.AUTO_START_REVERSE:
               radians = Math.atan2(gradient.getY(), gradient.getX());
            break;
            case SVGMarkerOrientAttribute.ANGLE:
               isFirst = false;
               angle = markerElement.getOrientAngle();
               radians = angle.toRadians();
            break;
         }

         if (radians != 0.0)
         {
            obj.rotate(ref, radians);
         }

         BBox box = obj.getStorageBBox();

         double objW = storageUnit.toUnit(box.getWidth(), unit);
         double objH = storageUnit.toUnit(box.getHeight(), unit);

         double scaleX = markerWidth/bounds.getWidth();
         double scaleY = markerHeight/bounds.getHeight();

         if (markerElement.isUnitStrokeWidth())
         {
            JDRBasicStroke stroke = null;
            JDRStroke s = shape.getStroke();

            if (s instanceof JDRBasicStroke)
            {
               stroke = (JDRBasicStroke)s;
            }

            if (stroke != null)
            {
               JDRLength penW = stroke.getPenWidth();

               double pen = penW.getValue(unit);

               scaleX *= pen;
               scaleY *= pen;
            }
         }

         if (isFirst)
         {
            obj.scale(ref, -scaleX, -scaleY);
         }
         else
         {
            obj.scale(ref, scaleX, scaleY);
         }

         obj.translate(shiftX, shiftY);

         group.add(obj);
      }

   }

   protected void addShapes(JDRShape shape) throws InvalidFormatException
   {
      JDRCompleteObject parent = shape.getParent();

      if (parent != null && parent instanceof JDRGroup)
      {
         JDRGroup group = (JDRGroup)parent;

         JDRPathIterator ptIt = shape.getIterator();

         Point2D ref = markerElement.getRefPoint();

         ref = new Point2D.Double(
            handler.toStorageUnit(ref.getX()),
            handler.toStorageUnit(ref.getY())
           );

         Point2D pt = new Point2D.Double();
         boolean isFirstSeg = true;

         while (ptIt.hasNext())
         {
            JDRPathSegment seg = ptIt.next();

            boolean isEndSeg = !ptIt.hasNext();

            if (isFirstSeg)
            {
               if (start)
               {
                  pt.setLocation(seg.getStartX(), seg.getStartY());

                  addMarkerShape(shape, group, pt, ref, true, seg.getdP0());
               }

               if (mid)
               {
                  pt.setLocation(seg.getEndX(), seg.getEndY());

                  addMarkerShape(shape, group, pt, ref, false, seg.getdP1());
               }
            }
            else if (isEndSeg)
            {
               if (end)
               {
                  pt.setLocation(seg.getEndX(), seg.getEndY());

                  addMarkerShape(shape, group, pt, ref, false, seg.getdP1());
               }
            }
            else if (mid)
            {
               pt.setLocation(seg.getEndX(), seg.getEndY());

               addMarkerShape(shape, group, pt, ref, false, seg.getdP1());
            }

            isFirstSeg = false;
         }
      }
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      ImportSettings importSettings = handler.getImportSettings();

      if (importSettings.markers == ImportSettings.Markers.IGNORE)
      {// do nothing
      }
      else if (object instanceof JDRShape && markerElement != null)
      {
         JDRShape shape = (JDRShape)object;

         if (importSettings.markers == ImportSettings.Markers.ADD_SHAPES)
         {// add shapes

            try
            {
               addShapes(shape);
            }
            catch (InvalidFormatException e)
            {
               handler.warning(e);
            }

         }
         else
         {
            JDRMarker marker = markerElement.getMarker();

            if (marker != null)
            {
               int orientType = markerElement.getOrientType();
               boolean isUnitStrokeWidth = markerElement.isUnitStrokeWidth();

               JDRStroke stroke = shape.getStroke();

               if (stroke instanceof JDRBasicStroke)
               {
                  JDRBasicStroke basicStroke = (JDRBasicStroke)stroke;

                  JDRMarker copy;
                  JDRLength penW = basicStroke.getPenWidth();

                  JDRUnit unit = handler.getDefaultUnit();
                  CanvasGraphics cg = handler.getCanvasGraphics();

                  if (start)
                  {
                     copy = (JDRMarker)marker.clone();
                     copy.setPenWidth(penW);
                     basicStroke.setStartArrow(copy);

                     if (isUnitStrokeWidth && !marker.usesLineWidth()
                           && marker.isResizable())
                     {
                        JDRLength l = copy.getSize();

                        if (l != null)
                        {
                           l.multiply(penW);
                        }

                        l = copy.getWidth();

                        if (l != null)
                        {
                           l.multiply(penW);
                        }
                     }

                     if (orientType != SVGMarkerOrientAttribute.AUTO_START_REVERSE)
                     {
                        // JDRMarker always reverses start marker
                        // so reverse to keep it forward

                        copy.setReversed(true);
                     }
                  }

                  if (mid)
                  {
                     copy = (JDRMarker)marker.clone();
                     copy.setPenWidth(penW);
                     basicStroke.setMidArrow(copy);

                     if (isUnitStrokeWidth && !marker.usesLineWidth()
                           && marker.isResizable())
                     {
                        JDRLength l = copy.getSize();

                        if (l != null)
                        {
                           l.multiply(penW);
                        }

                        l = copy.getWidth();

                        if (l != null)
                        {
                           l.multiply(penW);
                        }
                     }
                  }

                  if (end)
                  {
                     copy = (JDRMarker)marker.clone();
                     copy.setPenWidth(penW);
                     basicStroke.setEndArrow(copy);

                     if (isUnitStrokeWidth && !marker.usesLineWidth()
                           && marker.isResizable())
                     {
                        JDRLength l = copy.getSize();

                        if (l != null)
                        {
                           l.multiply(penW);
                        }

                        l = copy.getWidth();

                        if (l != null)
                        {
                           l.multiply(penW);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public Object getValue()
   {
      return markerElement;
   }

   private String name;
   SVGMarkerElement markerElement;
   boolean start, mid, end;
}
