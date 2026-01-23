package com.dickimawbooks.jdr.io.svg;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.net.URI;
import java.awt.geom.AffineTransform;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGImageElement extends SVGAbstractElement
{
   public SVGImageElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super(handler, parent);
   }

   @Override
   public String getName()
   {
      return "image";
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

      String ref = getHref();

      if (ref == null)
      {
         throw new ElementMissingAttributeException(this, "href");
      }

      try
      {
         uriRef = new URI(ref);

         if (!uriRef.isAbsolute())
         {
            URI resolvedUri = handler.getSVG().resolve(uriRef);

            if ("file".equals(resolvedUri.getScheme()))
            {
               imageFile = new File(resolvedUri);
            }
            else
            {
               imageFile = new File(resolvedUri.getPath());
            }
         }
         else if ("file".equals(uriRef.getScheme()))
         {
            imageFile = new File(uriRef);
         }
         else
         {
            throw new RemoteRefUnsupportedException(this, ref);
         }
      }
      catch (Exception e)
      {
         throw new InvalidAttributeValueException(this, "href", ref, e);
      }

      SVGLengthAttribute xAttr = getLengthAttribute("x", false);
      SVGLengthAttribute yAttr = getLengthAttribute("y", false);

      x = (xAttr == null ? 0 : xAttr.doubleValue(this));
      y = (yAttr == null ? 0 : yAttr.doubleValue(this));
   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws SVGException
   {
      SVGAttribute attr;

      if (name.equals("x"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value);
      }
      else if (name.equals("y"))
      {
         attr = SVGLengthAttribute.valueOf(handler, name, value);
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
      if (imageFile == null) return null;

      CanvasGraphics cg = group.getCanvasGraphics();

      JDRBitmap bitmap;

      try
      {
         bitmap = new JDRBitmap(cg, imageFile);
      }
      catch (FileNotFoundException e)
      {
         throw new RefNotFoundException(this, uriRef, imageFile, e);
      }

      if (elementWidth > 0 || elementHeight > 0)
      {
         int icW = bitmap.getIconWidth();
         int icH = bitmap.getIconHeight();

         if (elementWidth > 0)
         {
            double bpW = handler.getStorageUnit().toBp(elementWidth);

            if (elementHeight > 0)
            {
               double bpH = handler.getStorageUnit().toBp(elementHeight);

               if (icW == (int)Math.round(bpW) && icH == (int)Math.round(bpH))
               {
                  // do nothing
               }
               else
               {
                  double scaleX = bpW/icW;
                  double scaleY = bpH/icH;

                  bitmap.scale(scaleX, scaleY);
               }
            }
            else
            {
               double scaleX = bpW/icW;
               bitmap.scaleX(scaleX);
            }
         }
         else
         {
            double bpH = handler.getStorageUnit().toBp(elementHeight);
            double scaleY = bpH/icH;
            bitmap.scaleY(scaleY);
         }
      }

      if (x != 0 || y != 0)
      {
         bitmap.translate(x, y);
      }

      AffineTransform af = getTransform();

      if (af != null)
      {
         double[] matrix = new double[6];

         af.getMatrix(matrix);

         bitmap.transform(matrix);
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
         bitmap.setDescription(desc.replaceAll("\\R", " "));
      }

      bitmap.setTag(getName());

      group.add(bitmap);

      return bitmap;
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
      SVGImageElement element = new SVGImageElement(handler, null);

      element.makeEqual(this);

      return element;
   }

   public void makeEqual(SVGImageElement other)
   {
      super.makeEqual(other);

      title = other.title;
      description = other.description;
      imageFile = other.imageFile;
      uriRef = other.uriRef;
      x = other.x;
      y = other.y;
   }

   String title, description;
   File imageFile;
   URI uriRef;
   double x, y;
}
