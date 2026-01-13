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

      addAttribute("x", attr);
      addAttribute("y", attr);
      addAttribute("dx", attr);
      addAttribute("dy", attr);
      addAttribute("rotate", attr);
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

      SVGLength[] xArray = getLengthArrayAttribute("x");
      SVGLength[] yArray = getLengthArrayAttribute("y");
      SVGLength[] dxArray = getLengthArrayAttribute("dx");
      SVGLength[] dyArray = getLengthArrayAttribute("dy");
      SVGAngleAttribute[] angleArray = getAngleArrayAttribute("rotate");

      double x = 0;
      double y = 0;

      SVGAngleAttribute angle = null;

      int numObjects = 1;

      if (xArray != null)
      {
         numObjects = Math.max(numObjects, xArray.length);
      }

      if (yArray != null)
      {
         numObjects = Math.max(numObjects, yArray.length);
      }

      if (dxArray != null)
      {
         numObjects = Math.max(numObjects, dxArray.length);
      }

      if (dyArray != null)
      {
         numObjects = Math.max(numObjects, dyArray.length);
      }

      if (angleArray != null)
      {
         numObjects = Math.max(numObjects, angleArray.length);
      }

      String contents = getContents();
      JDRText textArea = null;
      JDRGroup subGrp = null;

      numObjects = Math.min(numObjects, contents.length());

      if (numObjects > 1)
      {
         subGrp = new JDRGroup(cg);
      }

      for (int i = 0, j = 0; i < numObjects && j < contents.length(); i++)
      {
         if (xArray != null && xArray.length > i)
         {
            x = xArray[i].getStorageValue(this, true);
         }
         else if (dxArray != null && dxArray.length > i)
         {
            Point2D lastP = handler.getLastTextPosition();
            double dx = dxArray[i].getStorageValue(this, true);
            x = (lastP == null ? dx : lastP.getX() + dx);
         }

         if (yArray != null && yArray.length > i)
         {
            y = yArray[i].getStorageValue(this, false);
         }
         else if (dyArray != null && dyArray.length > 0)
         {
            Point2D lastP = handler.getLastTextPosition();
            double dy = dyArray[i].getStorageValue(this, false);
            y = (lastP == null ? dy : lastP.getY() + dy);
         }

         if (angleArray != null && angleArray.length > 0)
         {
            angle = angleArray[i];
         }

         if (subGrp == null)
         {
            textArea = createTextArea(cg, x, y, angle, contents);
         }
         else if (i == numObjects - 1)
         {
            textArea = createTextArea(cg, x, y, angle, contents.substring(j));
            subGrp.add(textArea);
         }
         else
         {
            int cp = contents.codePointAt(j);
            int n = Character.charCount(cp);

            textArea = createTextArea(cg, x, y, angle, contents.substring(j, j+n));
            subGrp.add(textArea);

            j += n;
         }
      }

      if (subGrp == null || subGrp.size() == 1)
      {
         group.add(textArea);
         return textArea;
      }
      else
      {
         group.add(subGrp);
         return subGrp;
      }
   }

   protected JDRText createTextArea(CanvasGraphics cg,
     double x, double y, SVGAngleAttribute angle, String text)
   throws InvalidFormatException
   {
      Point2D p = new Point2D.Double(x, y);

      JDRText textArea = new JDRText(cg, p, handler.createDefaultFont(),
        text);

      handler.setLaTeXText(textArea);
      handler.setLastTextPosition(p);

      textArea.setTextPaint(handler.createDefaultTextPaint());

      if (angle != null)
      {
         textArea.rotate(angle.getRadians());
      }

      AffineTransform af = getTransform();

      if (af != null)
      {
         double[] matrix = new double[6];

         af.getMatrix(matrix);

         textArea.transform(matrix);
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
