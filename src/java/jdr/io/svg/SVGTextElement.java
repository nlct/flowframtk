package com.dickimawbooks.jdr.io.svg;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGTextElement extends SVGAbstractElement
{
   public SVGTextElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
      objects = new JDRGroup(handler.getCanvasGraphics());
   }

   @Override
   public String getName()
   {
      return "text";
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
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
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("x"))
      {
         attr = SVGLengthArrayAttribute.valueOf(handler, name, value);
      }
      else if (name.equals("y"))
      {
         attr = SVGLengthArrayAttribute.valueOf(handler, name, value);
      }
      else if (name.equals("dx"))
      {
         attr = SVGLengthArrayAttribute.valueOf(handler, name, value);
      }
      else if (name.equals("dy"))
      {
         attr = SVGLengthArrayAttribute.valueOf(handler, name, value);
      }
      else if (name.equals("rotate"))
      {
         attr = SVGAngleArrayAttribute.valueOf(handler, name, value);
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
   public void startElement() throws InvalidFormatException
   {
      templateText = new JDRText(getCanvasGraphics(),
         handler.createDefaultFont(), "");

      templateText.setTextPaint(handler.createDefaultTextPaint());
      applyTextAttributes(templateText);

      xArray = getLengthArrayAttribute("x", false);
      yArray = getLengthArrayAttribute("y", false);
      dxArray = getLengthArrayAttribute("dx", false);
      dyArray = getLengthArrayAttribute("dy", false);
      angleArray = getAngleArrayAttribute("rotate", false);

      x = 0;
      y = 0;
      angle = null;
      maxListItems = 1;
      currentListIndex = 0;

      if (xArray != null)
      {
         maxListItems = Math.max(maxListItems, xArray.length);
      }

      if (yArray != null)
      {
         maxListItems = Math.max(maxListItems, yArray.length);
      }

      if (dxArray != null)
      {
         maxListItems = Math.max(maxListItems, dxArray.length);
      }

      if (dyArray != null)
      {
         maxListItems = Math.max(maxListItems, dyArray.length);
      }

      if (angleArray != null)
      {
         maxListItems = Math.max(maxListItems, angleArray.length);
      }

   }

   @Override
   public void endElement() throws InvalidFormatException
   {
      process();
   }

   protected void process() throws InvalidFormatException
   {
      String desc = objects.getDescription();

      if (desc == null)
      {
         desc = contents.toString().replaceAll("\\R", " ");
      }
      else
      {
         desc += contents.toString().replaceAll("\\R", " ");
      }

      objects.setDescription(desc);

      for (int i = 0; i < contents.length(); )
      {
         int cp = contents.codePointAt(i);
         int numChars = Character.charCount(cp);

         boolean separate = false;

         if (xArray != null && xArray.length > currentListIndex)
         {
            x = xArray[currentListIndex].getStorageValue(this, true);
            separate = (xArray.length > 1);
         }
         else if (dxArray != null && dxArray.length > currentListIndex)
         {
            Point2D lastP = handler.getLastTextPosition();
            double dx = dxArray[currentListIndex].getStorageValue(this, true);
            x = (lastP == null ? dx : lastP.getX() + dx);
            separate = true;
         }

         if (yArray != null && yArray.length > currentListIndex)
         {
            y = yArray[currentListIndex].getStorageValue(this, false);
            separate = (yArray.length > 1);
         }
         else if (dyArray != null && dyArray.length > currentListIndex)
         {
            Point2D lastP = handler.getLastTextPosition();
            double dy = dyArray[currentListIndex].getStorageValue(this, false);
            y = (lastP == null ? dy : lastP.getY() + dy);
            separate = true;
         }

         if (angleArray != null)
         {
            if (angleArray.length > currentListIndex)
            {
               angle = angleArray[currentListIndex];
               radians = angle.getRadians();
            }

            separate = true;
         }

         String text;

         if (separate)
         {
            text = contents.substring(i, i+numChars);
         }
         else
         {
            text = contents.substring(i);
            numChars = text.length();
         }

         JDRText textArea = createTextArea(text);

         if (!text.trim().isEmpty())
         {
            objects.add(textArea);
         }

         currentListIndex++;
         i += numChars;
      }

      clearContents();
   }

   public void append(JDRCompleteObject obj) throws InvalidFormatException
   {
      objects.add(obj);

      String desc = objects.getDescription();
      String objDesc = obj.getDescription();

      if (objDesc != null)
      {
         if (desc == null)
         {
            desc = objDesc;
         }
         else
         {
            desc += objDesc;
         }
      }

      objects.setDescription(desc);
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      if (objects.isEmpty())
      {
         return null;
      }
      else if (objects.size() == 1)
      {
         JDRCompleteObject obj = objects.firstElement();

         group.add(obj);
         return obj;
      }
      else
      {
         group.add(objects);
         return objects;
      }
   }

   protected void setLaTeXText(JDRText textArea)
   {
      handler.setLaTeXText(textArea);
   }

   protected JDRText createTextArea(String text)
   throws InvalidFormatException
   {
      CanvasGraphics cg = getCanvasGraphics();

      Point2D p = new Point2D.Double(x, y);

      JDRText textArea = new JDRText(cg, p, templateText.getJDRFont(), text);

      textArea.setTextPaint(templateText.getTextPaint());

      setLaTeXText(textArea);
      handler.setLastTextPosition(p);

      if (angle != null)
      {
         textArea.rotate(p, radians);
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

      double width = textArea.getWidth();

      if (width > 0)
      {
         x += width;
      }
      else
      {
         Graphics2D g2 = cg.getGraphics();

         if (g2 != null)
         {
            FontMetrics fm = g2.getFontMetrics(textArea.getFont());
            x += fm.stringWidth(text);
         }
      }

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
      SVGTextElement element = new SVGTextElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   String title, description;
   JDRGroup objects;
   int currentListIndex, maxListItems;
   JDRText templateText;

   SVGLengthAttribute[] xArray;
   SVGLengthAttribute[] yArray;
   SVGLengthAttribute[] dxArray;
   SVGLengthAttribute[] dyArray;
   SVGAngleAttribute[] angleArray;

   double x, y, radians;

   SVGAngleAttribute angle;
}
