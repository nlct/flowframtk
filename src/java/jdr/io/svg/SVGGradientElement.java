package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public abstract class SVGGradientElement extends SVGAbstractElement
{
   public SVGGradientElement(SVGHandler handler, String name, SVGAbstractElement parent)
   {
      super(handler, parent);
      this.name = name;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("gradientUnits", attr);
      addAttribute("gradientTransform", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      if (name.equals("gradientUnits"))
      {
         return SVGGradientUnitsAttribute.valueOf(handler, value);
      }
      else if (name.equals("gradientTransform"))
      {
         return SVGTransformAttribute.valueOf(handler, value);
      }

      return super.createElementAttribute(name, value);
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      return null;
   }

   @Override
   public JDRPaint getPaint()
   {  
      return paint;
   } 

   @Override
   public void setDescription(String text)
   {
   }

   @Override
   public void setTitle(String text)
   {
   }

   public void addStop(SVGStopElement stopElem)
   {
      if (minStopElement == null)
      {
         minStopElement = stopElem;
      }
      else
      {
         double stopElemOff = stopElem.getOffset().doubleValue(this);
         double minOff = minStopElement.getOffset().doubleValue(this);

         if (stopElemOff < minOff)
         {
            if (maxStopElement == null)
            {
               maxStopElement = minStopElement;
            }

            minStopElement = stopElem;
         }
         else if (maxStopElement == null
               || maxStopElement.getOffset().doubleValue(this) < stopElemOff
                 )
         {
            maxStopElement = stopElem;
         }
      }
   }

   public void makeEqual(SVGGradientElement other)
   {
      super.makeEqual(other);

      if (other.minStopElement == null)
      {
         minStopElement = null;
      }
      else if (minStopElement == null)
      {
         minStopElement = (SVGStopElement)other.minStopElement.clone();
      }
      else
      {
         minStopElement.makeEqual(other.minStopElement);
      }

      if (other.maxStopElement == null)
      {
         maxStopElement = null;
      }
      else if (maxStopElement == null)
      {
         maxStopElement = (SVGStopElement)other.maxStopElement.clone();
      }
      else
      {
         maxStopElement.makeEqual(other.maxStopElement);
      }

      if (other.paint == null)
      {
         paint = null;
      }
      else if (paint == null)
      {
         paint = (JDRPaint)other.paint.clone();
      }
      else
      {
         paint.makeEqual(other.paint);
      }
   }

   String name;
   JDRPaint paint;
   SVGStopElement minStopElement, maxStopElement;
}
