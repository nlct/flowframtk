package com.dickimawbooks.jdr.io.svg;

import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGTextPathElement extends SVGTextElement
{
   public SVGTextPathElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "textPath";
   }

   @Override
   public void addAttributes(String uri, Attributes attr)
   {
      super.addAttributes(uri, attr);

      addAttribute("path", attr);
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      if (name.equals("path"))
      {
         return new SVGPathDataAttribute(handler, name, value);
      }
      else
      {
         return super.createElementAttribute(name, value);
      }
   }

   protected void assignTextAncestor() throws SVGException
   {
      textElement = getTextAncestor();

      if (textElement == null)
      {
         throw new ElementNotInsideException(this, "text");
      }
   }

   @Override
   protected void createTemplate() throws SVGException
   {
      if (textElement == null)
      {
         super.createTemplate();
      }
      else
      {
         templateText = (JDRText)textElement.templateText.clone();

         applyTextAttributes(templateText);
      }
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
      assignTextAncestor();

      createTemplate();

      if (textElement != null)
      {
         textElement.process();

         path = getPathData("path");

         if (path == null)
         {
            String ref = getHref();

            if (ref != null && !ref.isEmpty())
            {
               try
               {
                  SVGAbstractElement element = handler.getElement(new URI(ref));

                  if (element == null)
                  {
                     throw new UnknownReferenceException(element, ref);
                  }

                  if (element instanceof SVGShape)
                  {
                     refShape = (SVGShape)element;
                  }
                  else
                  {
                     throw new ElementNotShapeException(this, ref, element.getName());
                  }
               }
               catch (URISyntaxException e)
               {
                  throw new InvalidAttributeValueException(this, "href", ref, e);
               }
            }
         }

      }
   }

   @Override
   public void endElement() throws InvalidFormatException
   {
      String text = getContents().trim();

      if (textElement == null || text.isEmpty()) return;

      PathIterator pi;

      if (path == null)
      {
         pi = refShape.getPathIterator();
      }
      else
      {
         pi = path.getPathIterator(null);
      }

      if (pi != null)
      {
         JDRPath jdrPath = JDRPath.getPath(getCanvasGraphics(), pi);
         JDRText textArea = createTextArea(text);

         textPath = new JDRTextPath(jdrPath, textArea);

         textPath.setDescription(textArea.getDescription());

         textElement.append(textPath);
      }

      clearContents();
   }

   @Override
   public Object clone()
   {
      SVGTextPathElement element = new SVGTextPathElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   public void makeEqual(SVGTextPathElement other)
   {
      super.makeEqual(other);

      textElement = other.textElement;
      path = other.path;
      refShape = other.refShape;
      textPath = other.textPath;
   }

   SVGTextElement textElement;
   SVGShape refShape;
   JDRTextPath textPath;
   Path2D path;
}
