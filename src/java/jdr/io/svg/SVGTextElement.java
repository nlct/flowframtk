package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGTextElement extends SVGAbstractElement
{
   public SVGTextElement(SVGHandler handler,
     SVGAbstractElement parent, String uri, Attributes attr)
     throws InvalidFormatException
   {
      super(handler, parent, uri, attr);
   }

   @Override
   public String getName()
   {
      return "text";
   }

   @Override
   protected void addAttributes(String uri, Attributes attr)
     throws InvalidFormatException
   {
      super.addAttributes(uri, attr);

      addTextAttributes(uri, attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws InvalidFormatException
   {
      SVGAttribute attr;

      if (name.equals("x"))
      {
         attr = new SVGLengthArrayAttribute(handler, name, value);
      }
      else if (name.equals("y"))
      {
         attr = new SVGLengthArrayAttribute(handler, name, value);
      }
      else if (name.equals("dx"))
      {
         attr = new SVGLengthArrayAttribute(handler, name, value);
      }
      else if (name.equals("dy"))
      {
         attr = new SVGLengthArrayAttribute(handler, name, value);
      }
      else if (name.equals("rotate"))
      {
         attr = new SVGAngleArrayAttribute(handler, name, value);
      }
      else
      {
         attr = createTextStyleAttribute(name, value);

         if (attr == null)
         {
            attr = super.createElementAttribute(name, value);
         }
      }

      return attr;
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      CanvasGraphics cg = group.getCanvasGraphics();

// TODO support list values (displace individual glyphs)

      SVGLength[] xArray = getLengthArrayAttribute("x");
      SVGLength[] yArray = getLengthArrayAttribute("y");
      SVGLength[] dxArray = getLengthArrayAttribute("dx");
      SVGLength[] dyArray = getLengthArrayAttribute("dy");
      SVGAngleAttribute[] angleArray = getAngleArrayAttribute("rotate");

      double x = 0;
      double y = 0;

      if (xArray != null && xArray.length > 0)
      {
         x = xArray[0].getStorageValue(this, true);
      }
      else if (dxArray != null && dxArray.length > 0)
      {
         Point2D lastP = handler.getLastTextPosition();
         double dx = dxArray[0].getStorageValue(this, true);
         x = (lastP == null ? dx : lastP.getX() + dx);
      }

      if (yArray != null && yArray.length > 0)
      {
         y = yArray[0].getStorageValue(this, false);
      }
      else if (dyArray != null && dyArray.length > 0)
      {
         Point2D lastP = handler.getLastTextPosition();
         double dy = dyArray[0].getStorageValue(this, false);
         y = (lastP == null ? dy : lastP.getY() + dy);
      }

      Point2D p = new Point2D.Double(x, y);

      JDRText textArea = new JDRText(cg, p, handler.createDefaultFont(),
        getContents());

      handler.setLastTextPosition(p);

      textArea.setTextPaint(handler.createDefaultTextPaint());

      AffineTransform af = getTransform();

      if (af != null)
      {
         double[] matrix = new double[6];

         af.getMatrix(matrix);

         textArea.transform(matrix);
      }

      if (angleArray != null && angleArray.length > 0)
      {
         textArea.rotate(angleArray[0].getRadians());
      }

      String desc = null;

      if (title != null && !title.isEmpty())
      {
         desc = title;
      }
      else if (description != null && !description.isEmpty())
      {
         desc = description;
      }

      if (desc != null)
      {
         textArea.setDescription(desc.replaceAll("\\R", " "));
      }

      applyTextAttributes(textArea);

      group.add(textArea);

      return textArea;
   }

   @Override
   public void setDescription(String text)
   {
      if (text != null)
      {
         description = text.trim();
      }
   }

   @Override
   public void setTitle(String text)
   {
      if (text != null)
      {
         title = text.trim();
      }
   }

   @Override
   public Object clone()
   {
      try
      {
         SVGTextElement element = new SVGTextElement(handler, null, null, null);

         element.makeEqual(this);

         return element;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   String title, description;
}
