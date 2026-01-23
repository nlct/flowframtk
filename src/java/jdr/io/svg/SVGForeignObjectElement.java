package com.dickimawbooks.jdr.io.svg;

import java.awt.geom.Rectangle2D;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGForeignObjectElement extends SVGAbstractElement
{
   public SVGForeignObjectElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "foreignObject";
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("x", attr);
      addAttribute("y", attr);
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      super.startElement();

      containsVerb = false;

      String ns = getNameSpace();

      if (ns != null && !ns.equals("http://www.w3.org/1999/xhtml"))
      {
         throw new UnsupportedNameSpaceException(this, ns);
      }

      SVGLengthAttribute xAttr = getLengthAttribute("x");
      SVGLengthAttribute yAttr = getLengthAttribute("y");

      double x = 0;
      double y = 0;

      if (xAttr != null)
      {
         x = xAttr.doubleValue(this);
      }

      if (yAttr != null)
      {
         y = yAttr.doubleValue(this);
      }

      if (viewBoxBounds == null)
      {
         viewBoxBounds = new Rectangle2D.Double(x, y,
           getElementWidth(), getElementHeight());
      }
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("x"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, true);
      }
      else if (name.equals("y"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value, false);
      }
      else
      {
         attr = super.createElementAttribute(name, value);
      }

      return attr;
   }

   @Override
   public JDRCompleteObject addToImage(JDRGroup group)
     throws InvalidFormatException
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRCompleteObject lastObj = (group.isEmpty() ? null : group.lastElement());
      JDRCompleteObject obj = null;

      Rectangle2D rect;

      boolean border = true;
      int frameType = (containsVerb ? FlowFrame.STATIC : FlowFrame.DYNAMIC);


      if (lastObj == null || lastObj.getFlowFrame() != null)
      {
         border = false;
         rect = getViewportBounds();

         JDRPath path = JDRPath.constructRectangle(cg,
          rect.getMinX(), rect.getMinY(), rect.getMaxX(), rect.getMaxY());

         path.setStroke(new JDRBasicStroke(cg));
         path.setLinePaint(new JDRColor(cg));
         path.setFillPaint(new JDRTransparent(cg));

         group.add(path);

         obj = path;
      }
      else
      {
         obj = lastObj;

         BBox box = obj.getStorageBBox();

         rect = new Rectangle2D.Double(box.getMinX(), box.getMinY(),
           box.getWidth(), box.getHeight());
      }

      String label = getId();

      if (label == null || label.isEmpty())
      {
         frameCount++;
         label = "foreignObject"+frameCount;
      }

      FlowFrame flowframe = new FlowFrame(cg, frameType, border, label, "all");

      obj.setFlowFrame(flowframe);

      flowframe.setContents(contents.toString());

      if (border)
      {
         if (viewBoxBounds.getWidth() < rect.getWidth()
              && viewBoxBounds.getHeight() < rect.getHeight())
         {
            double left = rect.getMinX() - viewBoxBounds.getMinX();
            double right = viewBoxBounds.getMaxX() - rect.getMaxX();
            double top = rect.getMinY() - viewBoxBounds.getMinY();
            double bottom = viewBoxBounds.getMaxY() - rect.getMaxY();

            if (left > 0 && right > 0
                 && left + right < rect.getWidth()
               )
            {
               flowframe.setLeft(left);
               flowframe.setRight(right);
            }

            if (top > 0 && bottom > 0
                 && top + bottom < rect.getHeight()
               )
            {
               flowframe.setTop(top);
               flowframe.setBottom(bottom);
            }
         }
      }

      handler.registerHasFlowFrameData();

      return obj == lastObj ? null : obj;
   }

   public void appendParToFrameContent()
   {
      trimEndContents();
      contents.append(String.format("%n"));
   }

   public void appendToFrameContent(CharSequence text)
   {
      contents.append(text);
   }

   public void appendToFrameContent(CharSequence text, boolean containsVerb)
   {
      contents.append(text);

      if (containsVerb)
      {
         this.containsVerb = true;
      }
   }

   public boolean isVerbatim()
   {
      return false;
   }

   @Override
   public void addToContents(char[] ch, int start, int length)
   {
      if (isVerbatim())
      {
         contents.append(ch, start, length);
         return;
      }

      SVG svg = handler.getSVG();
      boolean hasTextMappings = svg.hasTextMappings();
      boolean parseMaths = supportsParseMaths();

      String buffer = new String(ch, start, length);

      for (int i = 0; i < buffer.length(); )
      {
         int cp = buffer.codePointAt(i);
         i += Character.charCount(cp);

         if (contents.length() == 0 && Character.isWhitespace(cp))
         {
            // skip leading spaces
         }
         else if (parseMaths)
         {
            int nextCp = (i < buffer.length() ? buffer.codePointAt(i) : -1);

            switch (currentMode)
            {
               case INLINE_MATHS:
                  if (cp == '\\' && nextCp == '$')
                  {
                     contents.appendCodePoint(cp);
                     contents.appendCodePoint(nextCp);
                     i++;
                  }
                  else if (cp == '$')
                  {
                     contents.appendCodePoint(cp);
                     currentMode = Mode.NORMAL;
                  }
                  else
                  {
                     contents.appendCodePoint(cp);
                  }
               break;
               case DISPLAY_MATHS:
                  if (cp == '\\' && nextCp == '$')
                  {
                     contents.appendCodePoint(cp);
                     contents.appendCodePoint(nextCp);
                     i++;
                  }
                  else if ((cp == '\\' && nextCp == ']')
                     || (cp == '$' && nextCp == '$'))
                  {
                     currentMode = Mode.NORMAL;
                     contents.appendCodePoint(cp);
                     contents.appendCodePoint(nextCp);
                     i++;
                  }
                  else
                  {
                     contents.appendCodePoint(cp);
                  }
               break;
               case NORMAL:
                  if (cp == '\\')
                  {
                     if (nextCp == '[')
                     {
                        currentMode = Mode.DISPLAY_MATHS;
                        contents.append("\\[");
                        i++;
                     }
                     else if (nextCp == '$' || nextCp == '\\')
                     {
                        contents.appendCodePoint(cp);
                        contents.appendCodePoint(nextCp);
                        i++;
                     }
                     else if (hasTextMappings)
                     {
                        contents.append(svg.applyTextMapping(cp));
                     }
                     else
                     {
                        contents.appendCodePoint(cp);
                     }
                  }
                  else if (cp == '$')
                  {
                     if (nextCp == '$')
                     {
                        currentMode = Mode.DISPLAY_MATHS;
                     }
                     else
                     {
                        currentMode = Mode.INLINE_MATHS;
                     }

                     contents.appendCodePoint(cp);
                     contents.appendCodePoint(nextCp);
                     i++;
                  }
                  else if (hasTextMappings)
                  {
                     contents.append(svg.applyTextMapping(cp));
                  }
                  else
                  {
                     contents.appendCodePoint(cp);
                  }
               break;
            }
         }
         else if (hasTextMappings)
         {
            contents.append(svg.applyTextMapping(cp));
         }
         else
         {
            contents.appendCodePoint(cp);
         }
      }
   }

   public Object clone()
   {
      SVGForeignObjectElement element = new SVGForeignObjectElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   public void makeEqual(SVGForeignObjectElement other)
   {
      super.makeEqual(other);

      containsVerb = other.containsVerb;
   }

   @Override
   public void setDescription(String text)
   {
   }

   @Override
   public void setTitle(String text)
   {
   }

   static enum Mode
   {
      NORMAL, INLINE_MATHS, DISPLAY_MATHS;
   }

   Mode currentMode = Mode.NORMAL;

   boolean containsVerb;

   static int frameCount;
}
