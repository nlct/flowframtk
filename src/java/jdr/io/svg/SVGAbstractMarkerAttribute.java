package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.JDRMarker;

import com.dickimawbooks.jdr.exceptions.*;

public abstract class SVGAbstractMarkerAttribute extends SVGAbstractAttribute
{
   protected SVGAbstractMarkerAttribute(SVGHandler handler, String name)
   {
      super(handler);
      this.name = name;
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

   public void makeEqual(SVGAbstractMarkerAttribute attr)
   {
      super.makeEqual(attr);
      markerElement = attr.markerElement;
   }

   public abstract void addMarker(JDRBasicStroke stroke, JDRMarker marker);

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

               marker = (JDRMarker)marker.clone();

               marker.setPenWidth(basicStroke.getPenWidth());

               addMarker(basicStroke, marker);
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
}
