package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.JDRMarker;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGMarkerMidAttribute extends SVGAbstractMarkerAttribute
{
   protected SVGMarkerMidAttribute(SVGHandler handler)
   {
      super(handler, "marker-mid");
   }

   public static SVGMarkerMidAttribute valueOf(SVGHandler handler,
       String valueString)
    throws SVGException
   {
      SVGMarkerMidAttribute attr = new SVGMarkerMidAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   @Override
   public Object clone()
   {
      SVGMarkerMidAttribute attr = new SVGMarkerMidAttribute(handler);

      attr.makeEqual(this);

      return attr;
   }

   @Override
   public void addMarker(JDRBasicStroke stroke, JDRMarker marker)
   {
      stroke.setMidArrow(marker);
   }

}
