package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.JDRMarker;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGMarkerAttribute extends SVGAbstractMarkerAttribute
{
   protected SVGMarkerAttribute(SVGHandler handler)
   {
      super(handler, "marker");
   }

   public static SVGMarkerAttribute valueOf(SVGHandler handler,
       String valueString)
    throws SVGException
   {
      SVGMarkerAttribute attr = new SVGMarkerAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   @Override
   public Object clone()
   {
      SVGMarkerAttribute attr = new SVGMarkerAttribute(handler);

      attr.makeEqual(this);

      return attr;
   }

   @Override
   public void addMarker(JDRBasicStroke stroke, JDRMarker marker)
   {
      stroke.setStartArrow((JDRMarker)marker.clone());
      stroke.setMidArrow((JDRMarker)marker.clone());
      stroke.setEndArrow(marker);
   }

}
