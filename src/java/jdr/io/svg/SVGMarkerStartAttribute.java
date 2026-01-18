package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.JDRMarker;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGMarkerStartAttribute extends SVGAbstractMarkerAttribute
{
   protected SVGMarkerStartAttribute(SVGHandler handler)
   {
      super(handler, "marker-start");
   }

   public static SVGMarkerStartAttribute valueOf(SVGHandler handler,
       String valueString)
    throws SVGException
   {
      SVGMarkerStartAttribute attr = new SVGMarkerStartAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   @Override
   public Object clone()
   {
      SVGMarkerStartAttribute attr = new SVGMarkerStartAttribute(handler);

      attr.makeEqual(this);

      return attr;
   }

   @Override
   public void addMarker(JDRBasicStroke stroke, JDRMarker marker)
   {
      stroke.setStartArrow(marker);
   }

}
