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
      addAttribute("width", attr);
      addAttribute("height", attr);
   }

   @Override
   public void startElement() throws InvalidFormatException
   {
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

   }

   @Override
   protected SVGAttribute createElementAttribute(String name, String value)
     throws InvalidFormatException
   {
      SVGAttribute attr;

      if (name.equals("x"))
      {
         attr = new SVGLengthAttribute(handler, name, value);
      }
      else if (name.equals("y"))
      {
         attr = new SVGLengthAttribute(handler, name, value);
      }
      else if (name.equals("width"))
      {
         attr = new SVGLengthAttribute(handler, name, value, true);
      }
      else if (name.equals("height"))
      {
         attr = new SVGLengthAttribute(handler, name, value, false);
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

      SVGLengthAttribute xAttr = getLengthAttribute("x", false);
      SVGLengthAttribute yAttr = getLengthAttribute("y", false);
      SVGLengthAttribute widthAttr = getLengthAttribute("width", false);
      SVGLengthAttribute heightAttr = getLengthAttribute("height", false);

      double x = (xAttr == null ? 0 : xAttr.getStorageValue(this, true));
      double y = (yAttr == null ? 0 : yAttr.getStorageValue(this, false));

      JDRBitmap bitmap;

      try
      {
         bitmap = new JDRBitmap(cg, imageFile);
      }
      catch (FileNotFoundException e)
      {
         throw new RefNotFoundException(this, uriRef, imageFile, e);
      }

      if (widthAttr != null || heightAttr != null)
      {
         int icW = bitmap.getIconWidth();
         int icH = bitmap.getIconHeight();

         if (widthAttr != null)
         {
            double storageW = widthAttr.getStorageValue(this, true);
            double bpW = handler.getStorageUnit().toBp(storageW);

            if (heightAttr != null)
            {
               double storageH = heightAttr.getStorageValue(this, false);
               double bpH = handler.getStorageUnit().toBp(storageH);

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
            double storageH = heightAttr.getStorageValue(this, false);
            double bpH = handler.getStorageUnit().toBp(storageH);
            double scaleY = bpH/icH;
            bitmap.scaleY(scaleY);
         }
      }

      if (xAttr != null || yAttr != null)
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

   String title, description;
   File imageFile;
   URI uriRef;
}
