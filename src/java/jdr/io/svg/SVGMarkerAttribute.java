package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.Point2D;

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

   protected void addMarkerShape(JDRGroup group, Point2D pt,
     Point2D ref, boolean reflect, Point2D gradient)
   throws InvalidFormatException
   {
      double shiftX = pt.getX() - ref.getX();
      double shiftY = pt.getY() - ref.getY();

      JDRGroup subGrp = new JDRGroup(group.getCanvasGraphics());

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

         if (reflect)
         {
            obj.scale(ref, -1, 1);
         }

         obj.translate(shiftX, shiftY);

// TODO scale and orient

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

                  addMarkerShape(group, pt, ref, true, seg.getdP0());
               }

               if (mid)
               {
                  pt.setLocation(seg.getEndX(), seg.getEndY());

                  addMarkerShape(group, pt, ref, false, seg.getdP1());
               }
            }
            else if (isEndSeg)
            {
               if (end)
               {
                  pt.setLocation(seg.getEndX(), seg.getEndY());

                  addMarkerShape(group, pt, ref, false, seg.getdP1());
               }
            }
            else if (mid)
            {
               pt.setLocation(seg.getEndX(), seg.getEndY());

               addMarkerShape(group, pt, ref, false, seg.getdP1());
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

               JDRStroke stroke = shape.getStroke();

               if (stroke instanceof JDRBasicStroke)
               {
                  JDRBasicStroke basicStroke = (JDRBasicStroke)stroke;

                  JDRMarker copy;
                  JDRLength penW = basicStroke.getPenWidth();

                  JDRUnit unit = handler.getDefaultUnit();
                  CanvasGraphics cg = handler.getCanvasGraphics();

                  JDRLength markerLength1 = marker.getSize();
                  JDRLength markerLength2 = marker.getWidth();

                  double pw = penW.getValue(unit);

                  if (markerLength1 != null)
                  {
                     markerLength1 = new JDRLength(cg, 
                          markerLength1.getValue(unit)*pw, unit);
                  }

                  if (markerLength2 != null)
                  {
                     markerLength2 = new JDRLength(cg, 
                          markerLength2.getValue(unit)*pw, unit);
                  }

                  if (start)
                  {
                     copy = (JDRMarker)marker.clone();
                     copy.setPenWidth(penW);
                     basicStroke.setStartArrow(copy);

                     if (markerLength1 != null)
                     {
                        marker.setSize(markerLength1);
                     }

                     if (markerLength2 != null)
                     {
                        marker.setWidth(markerLength2);
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

                     if (markerLength1 != null)
                     {
                        marker.setSize((JDRLength)markerLength1.clone());
                     }

                     if (markerLength2 != null)
                     {
                        marker.setWidth((JDRLength)markerLength2.clone());
                     }

                  }

                  if (end)
                  {
                     copy = (JDRMarker)marker.clone();
                     copy.setPenWidth(penW);
                     basicStroke.setEndArrow(copy);

                     if (markerLength1 != null)
                     {
                        marker.setSize((JDRLength)markerLength1.clone());
                     }

                     if (markerLength2 != null)
                     {
                        marker.setWidth((JDRLength)markerLength2.clone());
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
