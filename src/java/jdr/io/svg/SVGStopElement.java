package com.dickimawbooks.jdr.io.svg;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGStopElement extends SVGAbstractElement
{
   public SVGStopElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "stop";
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("offset", attr);
      addAttribute("stop-color", attr);
      addAttribute("stop-opacity", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      if (name.equals("offset"))
      {
         return SVGDoubleAttribute.valueOf(handler, name, value);
      }
      else if (name.equals("stop-opacity"))
      {
         return SVGDoubleAttribute.valueOf(handler, name, value);
      }
      else if (name.equals("stop-color"))
      {
         return SVGPaintAttribute.valueOf(this, name, value);
      }

      return super.createElementAttribute(name, value);
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      SVGGradientElement gradElem = getGradientAncestor();

      if (gradElem == null)
      {
         throw new ElementNotInsideException(this, "linearGradient|radialGradient");
      }

      opacity = getDoubleAttribute("stop-opacity", 1.0);
      paintAttr = getPaintAttribute("stop-color");
      offsetAttr = getNumberAttribute("offset");

      paint = paintAttr.getPaint();

      if (paint == null)
      {
         paint = new JDRColor(getCanvasGraphics());
      }

      paint.setAlpha(opacity);

      gradElem.addStop(this);
   }

   @Override
   public JDRPaint getPaint()
   {
      return paint;
   }

   public SVGNumberAttribute getOffset()
   {
      return offsetAttr;
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      return null;
   }

   @Override
   public void setDescription(String text)
   {
   }

   @Override
   public void setTitle(String text)
   {
   }

   @Override
   public Object clone()
   {
      SVGStopElement elem = new SVGStopElement(handler, parent);
      elem.makeEqual(this);
      return elem;
   }

   public void makeEqual(SVGStopElement other)
   {
      super.makeEqual(other);
      opacity = other.opacity;

      if (other.paintAttr == null)
      {
         paintAttr = null;
      }
      else if (paintAttr == null)
      {
         paintAttr = (SVGPaintAttribute)other.paintAttr.clone();
      }
      else
      {
         paintAttr.makeEqual(other.paintAttr);
      }

      if (other.offsetAttr == null)
      {
         offsetAttr = null;
      }
      else if (offsetAttr == null)
      {
         offsetAttr = (SVGNumberAttribute)other.offsetAttr.clone();
      }
      else
      {
         offsetAttr.makeEqual(other.offsetAttr);
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

   double opacity;
   SVGPaintAttribute paintAttr;
   SVGNumberAttribute offsetAttr;
   JDRPaint paint;
}
