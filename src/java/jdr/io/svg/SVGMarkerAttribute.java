package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.JDRMarker;

import com.dickimawbooks.jdr.exceptions.*;

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
        "marker", true, false, false);
      attr.parse(valueString);
      return attr;
   }

   public static SVGMarkerAttribute createMid(SVGHandler handler,
       String valueString)
    throws SVGException
   {
      SVGMarkerAttribute attr = new SVGMarkerAttribute(handler, 
        "marker", false, true, false);
      attr.parse(valueString);
      return attr;
   }

   public static SVGMarkerAttribute createEnd(SVGHandler handler,
       String valueString)
    throws SVGException
   {
      SVGMarkerAttribute attr = new SVGMarkerAttribute(handler, 
        "marker", false, false, true);
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

   public void makeEqual(SVGMarkerAttribute attr)
   {
      super.makeEqual(attr);
      markerElement = attr.markerElement;
      start = attr.start;
      mid = attr.mid;
      end = attr.end;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      if (object instanceof JDRShape && markerElement != null)
      {
         JDRMarker marker = markerElement.getMarker();

         if (marker != null)
         {
            JDRStroke stroke = ((JDRShape)object).getStroke();

            if (stroke instanceof JDRBasicStroke)
            {
               JDRBasicStroke basicStroke = (JDRBasicStroke)stroke;

               JDRMarker copy;
               JDRLength penW = basicStroke.getPenWidth();

               if (start)
               {
                  copy = (JDRMarker)marker.clone();
                  copy.setPenWidth(penW);
                  stroke.setStartArrow(copy);
               }

               if (mid)
               {
                  copy = (JDRMarker)marker.clone();
                  copy.setPenWidth(penW);
                  stroke.setMidArrow(copy);
               }

               if (end)
               {
                  copy = (JDRMarker)marker.clone();
                  copy.setPenWidth(penW);
                  stroke.setEndArrow(copy);
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
