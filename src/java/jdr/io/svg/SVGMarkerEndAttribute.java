package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.JDRMarker;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGMarkerEndAttribute extends SVGAbstractMarkerAttribute
{
   protected SVGMarkerEndAttribute(SVGHandler handler)
   {
      super(handler, "marker-end");
   }

   public static SVGMarkerEndAttribute valueOf(SVGHandler handler,
       String valueString)
    throws SVGException
   {
      SVGMarkerEndAttribute attr = new SVGMarkerEndAttribute(handler);
      attr.parse(valueString);
      return attr;
   }

   @Override
   public Object clone()
   {
      SVGMarkerEndAttribute attr = new SVGMarkerEndAttribute(handler);

      attr.makeEqual(this);

      return attr;
   }

   @Override
   public void addMarker(JDRBasicStroke stroke, JDRMarker marker)
   {
      stroke.setEndArrow(marker);
   }

}
