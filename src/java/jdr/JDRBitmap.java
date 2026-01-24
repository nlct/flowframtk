// File          : JDRBitmap.java
// Creation Date : 1st February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006 Nicola L.C. Talbot

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.dickimawbooks.jdr;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.util.*;

import javax.swing.*;
import javax.imageio.*;
import javax.imageio.stream.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a bitmap JDR object.
 * Note that this simply creates a link to an existing bitmap. The
 * actual bitmap will not be modified.
 * @author Nicola L C Talbot
 */
public class JDRBitmap extends JDRCompleteObject
{
   /**
    * Creates a new bitmap JDR object.
    * This loads a bitmap from the given location.
    * @param fullpathname the name of the bitmap file
    * @throws FileNotFoundException if file doesn't exist
    * @throws InvalidImageFormatException if file isn't a recognised image
    */
   public JDRBitmap(CanvasGraphics cg,
      String fullpathname)
      throws FileNotFoundException,
             InvalidImageFormatException
   {
      super(cg);

      init(fullpathname, "");
   }

   /**
    * Creates a new bitmap JDR object.
    * This loads a bitmap from the given location.
    * @param file the bitmap file
    * @throws FileNotFoundException if file doesn't exist
    * @throws InvalidImageFormatException if file isn't a recognised image
    */
   public JDRBitmap(CanvasGraphics cg, File file)
      throws FileNotFoundException,
             InvalidImageFormatException
   {
      super(cg);

      init(file, "");
   }

   /**
    * Creates a new bitmap JDR object.
    * This loads a bitmap from the given location.
    * @param fullpathname the name of the bitmap file
    * @param latexpathname the pathname to use when importing the
    * image in a LaTeX file
    * @throws FileNotFoundException if file doesn't exist
    * @throws InvalidImageFormatException if file isn't a recognised image
    */
   public JDRBitmap(CanvasGraphics cg,
     String fullpathname, String latexpathname)
      throws FileNotFoundException,
             InvalidImageFormatException
   {
      super(cg);
      init(fullpathname, latexpathname);
   }

   /**
    * Creates a new bitmap JDR object.
    * This loads a bitmap from the given location.
    * @param file the bitmap file
    * @param latexpathname the pathname to use when importing the
    * image in a LaTeX file
    * @throws FileNotFoundException if file doesn't exist
    * @throws InvalidImageFormatException if file isn't a recognised image
    */
   public JDRBitmap(CanvasGraphics cg,
     File file, String latexpathname)
      throws FileNotFoundException,
             InvalidImageFormatException
   {
      super(cg);
      init(file, latexpathname);
   }

   /**
    * Creates a copy. 
    */ 
   public JDRBitmap(JDRBitmap bitmap)
   {
      super(bitmap);

      this.latexCommand = bitmap.latexCommand;
      this.filename_ = bitmap.filename_;
      this.imageFile = bitmap.imageFile;
      this.latexlinkname_ = bitmap.latexlinkname_;
      this.name_ = bitmap.name_;
      this.ic = bitmap.ic;
      this.imageLoaded = bitmap.imageLoaded;

      this.affineTransform = new AffineTransform(bitmap.affineTransform);
   }

   private void init(String fullpathname, String latexpathname)
      throws FileNotFoundException,
             InvalidImageFormatException
   {
      filename_      = fullpathname;
      latexlinkname_ = latexpathname;

      imageFile = new File(filename_);

      if (!imageFile.exists())
      {
         throw new FileNotFoundException(imageFile.getAbsolutePath());
      }

      String imageFormat = null;

      try
      {
         imageFormat = getImageFormat(imageFile);
      }
      catch (IOException e)
      {
         throw new InvalidImageFormatException(imageFile.getAbsolutePath(),
           getCanvasGraphics(), e);
      }

      if (imageFormat == null)
      {
         throw new InvalidImageFormatException(imageFile.getAbsolutePath(),
           getCanvasGraphics());
      }

      name_ = imageFile.getName();

      imageLoaded = true;

      Image image = Toolkit.getDefaultToolkit().createImage(filename_);
      ic = new ImageIcon(image);

      imageLoaded =
         (ic.getImageLoadStatus() == MediaTracker.COMPLETE);

      affineTransform = new AffineTransform();
   }


   private void init(File file, String latexpathname)
      throws FileNotFoundException,
             InvalidImageFormatException
   {
      imageFile      = file;
      filename_      = imageFile.getAbsolutePath();
      latexlinkname_ = latexpathname;

      if (!file.exists())
      {
         throw new FileNotFoundException(filename_);
      }

      String imageFormat = null;

      try
      {
         imageFormat = getImageFormat(imageFile);
      }
      catch (IOException e)
      {
         throw new InvalidImageFormatException(imageFile.getAbsolutePath(),
           getCanvasGraphics(), e);
      }

      if (imageFormat == null)
      {
         throw new InvalidImageFormatException(imageFile.getAbsolutePath(),
           getCanvasGraphics());
      }

      name_ = imageFile.getName();

      imageLoaded = true;

      Image image = Toolkit.getDefaultToolkit().createImage(filename_);
      ic = new ImageIcon(image);

      imageLoaded =
         (ic.getImageLoadStatus() == MediaTracker.COMPLETE);

      affineTransform = new AffineTransform();
   }

   /**
    * Converts given path to a valid LaTeX path. If the directory
    * separator is a backslash, it converts all occurrences of
    * a backslash with a forward slash, otherwise it just returns
    * the original filename.
    * @param pathname original path
    * @return LaTeX path equivalent
    */
   public static String getLaTeXPath(String pathname)
   {
      String lpath = pathname;

      if (File.separator.equals("\\"))
      {
         StringTokenizer t = new StringTokenizer(pathname, "\\");
         lpath = t.nextToken();

         while (t.hasMoreTokens())
         {
            lpath += "/" + t.nextToken();
         }
      }

      return lpath;
   }

   private static String getImageFormat(File file)
     throws IOException
   {
      ImageInputStream stream = null;

      try
      {
         stream = ImageIO.createImageInputStream(file);

         Iterator it = ImageIO.getImageReaders(stream); 

         if (!it.hasNext())
         {
            return null;
         }

         ImageReader reader = (ImageReader)it.next();

         return reader.getFormatName();
      }
      finally
      {
         if (stream != null)
         {
            stream.close();
         }
      }
   }

   public boolean isDraft()
   {
      return !imageLoaded;
   }

   /**
    * Checks if given filename is valid. If the file does not 
    * exist and the browser utility is set then the user is
    * requested to specify a new file name or discard. If 
    * the file doesn't exist and the user discards the link
    * or if no browse utility exists returns null, otherwise
    * returns either the original filename (if it exists) or
    * the new file name chosen by the user.
    * @param originalFilename the original filename
    * @return the original filename if it exists or the filename
    * requested by the user or null
    */
   public static String checkFilename(JDRAJR jdr, String originalFilename)
   {
      String filename = originalFilename;

      File file = jdr.resolveFile(filename);

      CanvasGraphics cg = jdr.getCanvasGraphics();

      if (!file.exists())
      {
         cg.setBitmapReplaced(true);

         int result = JOptionPane.NO_OPTION;

         if (cg.allowsBitmapBrowse())
         {
            // give user opportunity to select new filename
            result = cg.getBrowseDialog(filename);
         }
         else
         {
            jdr.getMessageSystem().getPublisher().publishMessages(
               MessageInfo.createWarning(new FileNotFoundException(
                jdr.getMessageSystem().getMessageWithFallback(
                  "error.file_not_found_with_name", "File not found: {0}",
                  filename))));
            return null;
         }

         if (result == JOptionPane.YES_OPTION)
         {
            filename = cg.chooseBitmap(file);

            if (filename != null)
            {
               file = new File(filename);

               if (!file.exists())
               {
                  return checkFilename(jdr, filename);
               }
            }
            else
            {
               return null;
            }
         }
         else
         {
            return null;
         }
      }

      String imageFormat = null;

      try
      {
         imageFormat = getImageFormat(file);
      }
      catch (IOException e)
      {
         jdr.getMessageSystem().getPublisher().publishMessages(
            MessageInfo.createWarning(e));
      }

      if (imageFormat == null)
      {
         int result = JOptionPane.NO_OPTION;

         if (cg.allowsBitmapBrowse())
         {
            // give user opportunity to select new filename
            result = cg.getInvalidFormatDialog(filename);
         }
         else
         {
            jdr.getMessageSystem().getPublisher().publishMessages(
               MessageInfo.createWarning(
                 new InvalidImageFormatException(filename, cg)));
            return null;
         }

         if (result == JOptionPane.YES_OPTION)
         {
            filename = cg.chooseBitmap(file);

            if (filename != null)
            {
               file = new File(filename);

               imageFormat = null;

               if (file.exists())
               {
                  try
                  {
                     imageFormat = getImageFormat(file);
                  }
                  catch (IOException e)
                  {
                     jdr.getMessageSystem().getPublisher().publishMessages(
                         MessageInfo.createWarning(e));
                  }
               }

               if (imageFormat == null)
               {
                  return checkFilename(jdr, filename);
               }
            }
            else
            {
               return null;
            }
         }
         else
         {
            return null;
         }
      }

      return filename;
   }

   /**
    * Resets the transformation matrix.
    */
   public void reset()
   {
      affineTransform.setToIdentity();
   }

   /**
    * Reloads the bitmap from its source file. This is available in
    * the event that an external application has modified the 
    * original bitmap since it was loaded.
    * @return true if successfully loaded or false if the file can't
    * be refreshed (for example, if the bitmap is no longer in its
    * original location)
    */
   public boolean refresh()
   {
      imageFile = new File(filename_);
      name_ = imageFile.getName();

      if (!imageFile.exists())
      {
         CanvasGraphics cg = getCanvasGraphics();
         cg.setBitmapReplaced(true);

         int result = JOptionPane.NO_OPTION;

         if (cg.allowsBitmapBrowse())
         {
            // give user opportunity to select new filename
            result = cg.getCantRefreshDialog(filename_);
         }
         else
         {
            cg.getMessageSystem().getPublisher().publishMessages(
              MessageInfo.createWarning(new FileNotFoundException(name_)));

            return false;
         }

         if (result == JOptionPane.YES_OPTION)
         {
            String filename = cg.chooseBitmap(imageFile);

            if (filename != null)
            {
               filename_ = filename;

               imageFile = new File(filename_);
               name_ = imageFile.getName();

               if (!imageFile.exists())
               {
                  refresh();
               }

               if (File.separator.equals("\\"))
               {
                  StringTokenizer t = new StringTokenizer(filename_, "\\");
                  latexlinkname_ = t.nextToken();

                  while (t.hasMoreTokens())
                  {
                     latexlinkname_ += File.separator + t.nextToken();
                  }
               }
               else
               {
                  latexlinkname_ = filename_;
               }
            }
            else
            {
               return false;
            }
         }
         else
         {
            return false;
         }
      }

      Image image = ic.getImage();
      image.flush();
      image = Toolkit.getDefaultToolkit().createImage(filename_);
      ic = new ImageIcon(image);

      imageLoaded =
         (ic.getImageLoadStatus() == MediaTracker.COMPLETE);

      return true;
   }

   /**
    * Print the PGF commands to display this bitmap.
    */
   public void savePgf(TeX tex)
    throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      AffineTransform concat = new AffineTransform();
      concat.concatenate(tex.getTransform());
      concat.concatenate(getAffineTransform());
      concat.concatenate(new AffineTransform(1, 0, 0, -1, 0, 0));

      tex.println("\\begin{pgfscope}");
      tex.println(tex.transform(cg, concat));

      tex.println("\\pgflowlevelsynccm");
      tex.print("\\pgfputat{\\pgfpoint{0pt}{0pt}}{");

      tex.print("\\pgftext[top,left]{");

      double w = ic.getIconWidth();
      double h = ic.getIconHeight();

      JDRUnit unit = cg.getStorageUnit();

      String name = latexlinkname_;

      if (name == null || name.isEmpty())
      {
         name = getLaTeXPath(tex.relativize(imageFile).toString());
      }

      tex.print(latexCommand
          + "[width="
          + tex.length(cg, unit.fromBp(w))
          + ",height="
          + tex.length(cg, unit.fromBp(h))+"]{");

      int idx = name.lastIndexOf(".");

      if (idx > -1)
      {
         tex.print(name.substring(0,idx));
      }
      else
      {
         tex.print(name);
      }

      tex.println("}}}");
      tex.println("\\end{pgfscope}");

      if (tex.isConvertBitmapToEpsEnabled())
      {
         // Bitmap needs to be converted to eps if EPS/PS version
         // doesn't already exist.

         String baseName = filename_;

         idx = baseName.lastIndexOf(".");

         if (idx > 0)
         {
            baseName = baseName.substring(0, idx);
         }

         File psFile = new File(baseName + ".ps");

         if (psFile.exists())
         {
            return;
         }

         psFile = new File(baseName + ".PS");

         if (psFile.exists())
         {
            return;
         }

         psFile = new File(baseName + ".EPS");

         if (psFile.exists())
         {
            return;
         }

         psFile = new File(baseName + ".eps");

         if (psFile.exists())
         {
            return;
         }

         JDRGroup grp = new JDRGroup(getCanvasGraphics());
         grp.add(this);

         PrintWriter out = null;

         try
         {
            out = new PrintWriter(new FileWriter(psFile));
            EPS.save(grp, out, "jdr2eps");
         }
         finally
         {
            if (out != null)
            {
               out.close();
            }
         }
      }
   }

   /**
    * Rotates this object about its centre.
    * @param angle the angle of rotation in radians
    */
   public void rotate(double angle)
   {
      BBox box = getStorageBBox();
      double x = box.getMinX()+0.5*box.getWidth();
      double y = box.getMinY()+0.5*box.getHeight();

      affineTransform.preConcatenate(
       AffineTransform.getRotateInstance(angle, x, y));
   }

   /**
    * Rotates this object about the given point.
    * @param angle the angle of rotation in radians
    */
   public void rotate(Point2D p, double angle)
   {
      affineTransform.preConcatenate(
       AffineTransform.getRotateInstance(angle, p.getX(), p.getY()));
   }

   public void scaleX(Point2D p, double factor)
   {
      scale(p, factor, 1.0);
   }

   public void scaleY(Point2D p, double factor)
   {
      scale(p, 1.0, factor);
   }

   /**
    * Scales this object.
    */
   public void scale(double factorX, double factorY)
   {
      //scale relative to top left
      BBox box = getStorageBBox();
      double x = box.getMinX();
      double y = box.getMinY();

      double scaleX = affineTransform.getScaleX() * factorX;
      double scaleY = affineTransform.getScaleY() * factorY;
      double shearX = affineTransform.getShearX() * factorX;
      double shearY = affineTransform.getShearY() * factorY;
      double tx = (affineTransform.getTranslateX() - x) * factorX + x;
      double ty = (affineTransform.getTranslateY() - y) * factorY + y;

      affineTransform.setTransform(scaleX, shearY, shearX, scaleY, tx, ty);
   }

   /**
    * Scales this object relative to the given point.
    */
   public void scale(Point2D p, double factorX, double factorY)
   {
      double x = p.getX();
      double y = p.getY();

      double scaleX = affineTransform.getScaleX() * factorX;
      double scaleY = affineTransform.getScaleY() * factorY;
      double shearX = affineTransform.getShearX() * factorX;
      double shearY = affineTransform.getShearY() * factorY;
      double tx = (affineTransform.getTranslateX() - x) * factorX + x;
      double ty = (affineTransform.getTranslateY() - y) * factorY + y;

      affineTransform.setTransform(scaleX, shearY, shearX, scaleY, tx, ty);
   }

   /**
    * Shears this object.
    */
   public void shear(double factorX, double factorY)
   {
      //shear relative to bottom left
      BBox box = getStorageBBox();
      double x = box.getMinX();
      double y = box.getMaxY();

      double scaleX = affineTransform.getScaleX();
      double scaleY = affineTransform.getScaleY();
      double shearX = affineTransform.getShearX();
      double shearY = affineTransform.getShearY();
      double tx = affineTransform.getTranslateX() - x;
      double ty = affineTransform.getTranslateY() - y;

      affineTransform.setTransform(
        scaleX - factorX * shearY,
        shearY - factorY * scaleX,
        shearX - factorX * scaleY,
        scaleY - factorY * shearX,
        tx - factorX * ty + x,
        ty - factorY * tx + y
      );
   }

   /**
    * Shears this object relative to the given point.
    */
   public void shear(Point2D p, double factor)
   {
      shear(p, factor, factor);
   }

   /**
    * Shears this object relative to the given point.
    */
   public void shear(Point2D p, double factorX, double factorY)
   {
      double x = p.getX();
      double y = p.getY();
      double scaleX = affineTransform.getScaleX();
      double scaleY = affineTransform.getScaleY();
      double shearX = affineTransform.getShearX();
      double shearY = affineTransform.getShearY();
      double tx = affineTransform.getTranslateX() - x;
      double ty = affineTransform.getTranslateY() - y;

      affineTransform.setTransform(
        scaleX - factorX * shearY,
        shearY - factorY * scaleX,
        shearX - factorX * scaleY,
        scaleY - factorY * shearX,
        tx - factorX * ty + x,
        ty - factorY * tx + y
      );
   }

   /**
    * Translates this object.
    */
   public void translate(double x, double y)
   {
      double scaleX = affineTransform.getScaleX();
      double scaleY = affineTransform.getScaleY();
      double shearX = affineTransform.getShearX();
      double shearY = affineTransform.getShearY();
      double tx = affineTransform.getTranslateX();
      double ty = affineTransform.getTranslateY();

      affineTransform.setTransform(scaleX, shearY, shearX, scaleY, tx+x, ty+y);
   }

   /**
    * Gets this object's (bp) transformation. This is the transformation
    * that is applied to the bitmap when it is drawn or exported.
    * NB this no longer returns a copy.
    * @return this object's transformation
    */
   public AffineTransform getAffineTransform()
   {
      return affineTransform;
   }

   /**
    * Transforms this object.
    * @param matrix the transformation matrix to apply to this object
    */
   @Override
   public void transform(double[] matrix)
   {
      transform(new AffineTransform(matrix));
   }

   /**
    * Transforms this object.
    * @param trans the transformation to apply to this object
    */
   public void transform(AffineTransform trans)
   {
      affineTransform.concatenate(trans);
   }

   /**
    * Preconcatenates this object's transformation matrix with
    * the given transformation.
    * @param trans the new transformation to apply
    */
   public void preConcatenate(AffineTransform trans)
   {
      affineTransform.preConcatenate(trans);
   }

   /**
    * Gets this object's bounding box.
    */
   public BBox getStorageBBox()
   {
      Rectangle2D rect = getStorageBounds();

      BBox bbox = new BBox(getCanvasGraphics(),
                           rect.getX(), rect.getY(),
                           rect.getX()+rect.getWidth(),
                           rect.getY()+rect.getHeight());
      return bbox;
   }

   /**
    * Gets the outline of this object. This will be a rectangle
    * of the same dimensions as the bitmap which has been 
    * transformed using this object's transformation matrix.
    * @return outline of this object
    */
   public Shape getStorageOutline()
   {
      CanvasGraphics cg = getCanvasGraphics();

      JDRUnit unit = cg.getStorageUnit();

      Shape outline =  new GeneralPath(
         new Rectangle2D.Double(0, 0,
            unit.fromBp(ic.getIconWidth()), 
            unit.fromBp(ic.getIconHeight())));
      AffineTransform af = getAffineTransform();
      outline = af.createTransformedShape(outline);

      return outline;
   }

   /**
    * Gets this object's bounds. This is the smallest rectangle that
    * encompasses this object's outline.
    * @see #getOutline()
    * @return this object's bounds
    */
   public Rectangle2D getStorageBounds()
   {
      return getStorageOutline().getBounds2D();
   }

   /**
    * Gets the top left corner of this object's bounds.
    * @return top left corner of this object's bounds
    */
   public JDRPoint getStart()
   {
      double x = affineTransform.getTranslateX();
      double y = affineTransform.getTranslateY();

      return new JDRPoint(getCanvasGraphics(), x, y);
   }

   /**
    * Gets the top left corner of this object's bounds.
    * This is in fact the same as {@link #getStart()}.
    * @return top left corner of this object's bounds
    * @see #getBounds()
    */
   public JDRPoint getEnd()
   {
      return getStart();
   }

   /**
    * Gets the mid point of this object's bounds.
    * @return mid point of this object's bounds
    * @see #getBounds(CanvasGraphics)
    */
   public Point2D getCentre()
   {
      BBox box = getStorageBBox();
      return new Point2D.Double(box.getMidX(),box.getMidY());
   }

   /**
    * Gets the file name (including path) in which the bitmap is stored.
    * @return bitmap location
    */
   public String getFilename()
   {
      return filename_;
   }

   public File getFile()
   {
      return imageFile;
   }

   public String getName()
   {
      return name_;
   }

   /**
    * Gets the link name used when importing this bitmap into a
    * LaTeX file.
    * @return LaTeX link name
    */
   public String getLaTeXLinkName()
   {
      return latexlinkname_;
   }

   /**
    * Sets the link location and refreshes the image.
    * @param fullfilename bitmap location
    * @param latexLinkName link name used when importing this
    * bitmap into a LaTeX file
    * @see #getFilename()
    * @see #getLaTeXLinkName()
    * @see #refresh()
    */
   public void setProperties(String fullfilename,
                             String latexLinkName)
   {
      boolean imageChanged = !filename_.equals(fullfilename);

      filename_      = fullfilename;
      imageFile      = new File(fullfilename);
      latexlinkname_ = latexLinkName;

      if (imageChanged)
      {
         refresh();
      }
   }

   public void setProperties(File file,
                             String latexLinkName)
   {
      boolean imageChanged = !file.equals(imageFile);

      imageFile      = file;
      filename_      = imageFile.getAbsolutePath();
      latexlinkname_ = latexLinkName;

      if (imageChanged)
      {
         refresh();
      }
   }

   public void draw(FlowFrame parentFrame)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (parentFrame == null)
      {
         parentFrame = getFlowFrame();
      }

      if (imageLoaded)
      {
         double hoffset = 0.0;
         double voffset = 0.0;

         if (parentFrame != null && cg.isEvenPage())
         { 
            hoffset = parentFrame.getEvenXShift();
            voffset = parentFrame.getEvenYShift();
         }

         AffineTransform af = new AffineTransform(affineTransform);
         af.translate(hoffset, voffset);

         double bpToStorage = cg.bpToStorage(1.0);

         af.scale(bpToStorage, bpToStorage);

         cg.drawImage(ic.getImage(), af);
      }
      else
      {
         Paint oldPaint = cg.getPaint();

         cg.setPaint(draftBackgroundColor);
         Rectangle2D bitmapBounds = getStorageBounds();

         if (parentFrame != null && cg.isEvenPage())
         {
            bitmapBounds.setRect(
               bitmapBounds.getX() + parentFrame.getEvenXShift(),
               bitmapBounds.getY() + parentFrame.getEvenYShift(), 
               bitmapBounds.getWidth(), bitmapBounds.getHeight());
         }

         cg.fill(bitmapBounds);

         cg.setPaint(Color.black);

         Font oldFont = cg.getFont();
         cg.setFont(annoteFont);

         cg.drawString("["+name_+"]",
            bitmapBounds.getX(),
            bitmapBounds.getY()+0.5*bitmapBounds.getHeight());
         cg.setFont(oldFont);
         cg.setPaint(oldPaint);
      }

      drawFlowFrame();
   }

   public void print(Graphics2D g2)
   {
      if (imageLoaded)
      {
         CanvasGraphics cg = getCanvasGraphics();

         AffineTransform oldAf = g2.getTransform();
         double storageToBp = cg.storageToBp(1.0);
         g2.scale(storageToBp, storageToBp);

         AffineTransform af = getAffineTransform();

         double bpToStorage = cg.bpToStorage(1.0);

         af.scale(bpToStorage, bpToStorage);
         g2.drawImage(ic.getImage(), af, cg.getComponent());

         g2.setTransform(oldAf);
      }
   }

   /**
    * Draws this bitmap applying the relevant transformation.
    * The draft parameter is ignored as it doesn't apply to bitmaps.
    * @param g the graphics device
    * @param draft ignored
    */
   public void draw( boolean draft)
   {
      draw();
   }

   public Object clone()
   {
      return new JDRBitmap(this);
   }

   /**
    * Makes this object identical to the given object.
    * @param bitmap the other bitmap that this bitmap should be
    * set to
    */
   public void makeEqual(JDRBitmap bitmap)
   {
      super.makeEqual(bitmap);
      ic = bitmap.ic;
      filename_ = bitmap.getFilename();
      imageFile = bitmap.imageFile;
      name_ = bitmap.name_;
      latexlinkname_ = bitmap.getLaTeXLinkName();
      latexCommand = bitmap.latexCommand;
      double[] matrix = new double[6];
      bitmap.getTransformation(matrix);
      setTransformation(matrix);
      imageLoaded = bitmap.imageLoaded;
   }

   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;

      if (!(obj instanceof JDRBitmap)) return false;

      JDRBitmap b = (JDRBitmap)obj;

      if (!filename_.equals(b.filename_)) return false;
      if (!latexlinkname_.equals(b.latexlinkname_)) return false;
      if (!latexCommand.equals(b.latexCommand)) return false;

      if (!affineTransform.equals(b.affineTransform)) return false;

      return true;
   }

   /**
    * Gets the transformation matrix applied to this object.
    * @param matrix on exit contains this object's transformation
    * matrix
    */
   public void getTransformation(double[] matrix)
   {
      affineTransform.getMatrix(matrix);
   }

   /**
    * Sets this object's transformation matrix.
    * @param matrix the new transformation matrix
    */
   public void setTransformation(double[] matrix)
   {
      affineTransform.setTransform(matrix[0], matrix[1],
       matrix[2], matrix[3], matrix[4], matrix[5]);
   }

   /**
    * Sets this object's transformation matrix.
    * @param af the new transformation
    */
   public void setTransformation(AffineTransform af)
   {
      affineTransform.setTransform(af);
   }

   /**
    * Saves this object in EPS format.
    * Does nothing if image is in draft mode.
    */
   public void saveEPS(PrintWriter out)
      throws IOException
   {
      if (isDraft())
      {
         return;
      }

      CanvasGraphics cg = getCanvasGraphics();

      JDRUnit unit = cg.getStorageUnit();

      Image image = ic.getImage();
      int w = (int)Math.ceil(image.getWidth(null));
      int h = (int)Math.ceil(image.getHeight(null));

      BufferedImage buffImage = null;

      if (image instanceof BufferedImage)
      {
         buffImage = (BufferedImage)image;
      }
      else
      {
         int type = getColorType(image);

         buffImage = new BufferedImage(w, h, type);
      }

      Graphics g = buffImage.createGraphics();

      g.drawImage(image, 0, 0, null);

      g.dispose();

      int scanLength = w;
      int scanLines = h;
      int bitsPerSample=8;

      out.println("gsave");

      double[] flatmatrix = new double[6];
      affineTransform.getMatrix(flatmatrix);

      out.println("/bitmapmat ["+flatmatrix[0]+" "+flatmatrix[1]+" "
                     +flatmatrix[2]+" "+flatmatrix[3]+" "
                     +unit.toBp(flatmatrix[4])+" "
                     +unit.toBp(flatmatrix[5])+"]def");
      out.println("/bitmapinv matrix def");

      out.println("/DeviceRGB setcolorspace");
      out.println("<<");
      out.println("  /ImageType 1");
      out.println("  /Width "+scanLength);
      out.println("  /Height "+scanLines);
      out.println("  /BitsPerComponent "+bitsPerSample);
      out.println("  /Decode [0 1 0 1 0 1]");
      out.println("  /ImageMatrix bitmapmat bitmapinv invertmatrix");
      out.println("  /DataSource currentfile /ASCIIHexDecode filter");
      out.println(">>");
      out.println("image");

      int n = w*h;
      int[] rgb = new int[n];

      buffImage.getRGB(0, 0, w, h, rgb, 0, w);

      for (int i = 0; i < n; i++)
      {
         int red = ((rgb[i] & 0x00ff0000)>>16);
         int green = ((rgb[i] & 0x0000ff00)>>8);
         int blue = (rgb[i] & 0x000000ff);

         out.print(hexChar(red>>4));
         out.print(hexChar(red & 0x0f));
         out.print(hexChar(green>>4));
         out.print(hexChar(green & 0x0f));
         out.print(hexChar(blue>>4));
         out.print(hexChar(blue & 0x0f));

         if (i%10 == 9)
         {
            out.println();
         }
         else
         {
            out.print(" ");
         }
      }

      out.println(">");
      out.println("grestore");
   }

   /**
    * Returns the EPS level supported by this object.
    */
   public int psLevel()
   {
      return 2;
   }

   private char hexChar(int val)
   {
      switch (val)
      {
         case 1: return '1';
         case 2: return '2';
         case 3: return '3';
         case 4: return '4';
         case 5: return '5';
         case 6: return '6';
         case 7: return '7';
         case 8: return '8';
         case 9: return '9';
         case 10: return 'a';
         case 11: return 'b';
         case 12: return 'c';
         case 13: return 'd';
         case 14: return 'e';
         case 15: return 'f';
      }

      return '0';
   }

   private String hexColourString(int argb)
   {
      int rgb = argb & 0x00ffffff;

      String hexString = Integer.toHexString(rgb);

      int n = 6 - hexString.length();

      String prefix = "";

      for (int i = 0; i < n; i++)
      {
         prefix += "0";
      }

      return prefix+hexString;
   }

   private int getColorType(Image image)
   {
      PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);

      try
      {
         pg.grabPixels();
      }
      catch (InterruptedException e)
      {
      }

      ColorModel model = pg.getColorModel();

      if (model.hasAlpha())
      {
         return BufferedImage.TYPE_INT_ARGB;
      }
      else
      {
         return BufferedImage.TYPE_INT_RGB;
      }
   }

   /**
    * Saves this object in SVG format.
    */
   public void saveSVG(SVG svg, String attr)
      throws IOException
   {
       svg.println("   <image "+attr+" x=\"0\" y=\"0\"");
       svg.println("      " + svg.transform(affineTransform));
       svg.println("      width=\""
         +ic.getIconWidth()+"pt\"");
       svg.println("      height=\""
         +ic.getIconHeight()+"pt\"");
       svg.print("      href=\""+svg.encodeAttributeValue(filename_, true)+"\" ");

       String title = getDescription();

       if (title != null && !title.isEmpty())
       {
          svg.println(">");
          svg.print("   <title>");
          svg.print(svg.encodeContent(title));
          svg.println("</title>");
          svg.println("   </image>");

       }
       else
       {
          svg.println("/>");
       }
   } 

   @Override
   public void writeSVGdefs(SVG svg) throws IOException
   {
   } 

   public JDRObjectLoaderListener getListener()
   {
      return bitmapListener;
   }

   public String info(String prefix)
   {
      JDRMessage msgSys = getCanvasGraphics().getMessageSystem();
      String eol = String.format("%n");

      StringBuilder builder = new StringBuilder();

      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
        "objectinfo.bitmap", "Bitmap:"));

      builder.append(eol);
      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
        "objectinfo.bitmap.image_name", "Name: {0}", name_));

      builder.append(eol);
      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
        "objectinfo.bitmap.filename", "Filename: {0}", filename_));

      double[] matrix = new double[6];
      affineTransform.getMatrix(matrix);

      builder.append(eol);
      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
       "objectinfo.matrix",
        "Transformation matrix: [ [ {0} {2} {4} ] [ {1} {3} {5} ] ]",
        matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]));

      builder.append(eol);
      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
        "objectinfo.bitmap.latex_path", "LaTeX path: {0}", latexlinkname_));

      builder.append(eol);
      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
        "objectinfo.bitmap.latex_command", "LaTeX command: {0}", latexCommand));

      if (msgSys.isDebuggingOn())
      {
         builder.append(eol);
         builder.append(prefix);

         if (imageLoaded)
         {
            builder.append(msgSys.getMessageWithFallback(
              "objectinfo.bitmap.image_loaded", "Image has been loaded"));
         }
         else
         {
            builder.append(msgSys.getMessageWithFallback(
              "objectinfo.bitmap.image_loaded", "Image has not been loaded"));
         }

         builder.append(eol);
         builder.append(prefix);

         builder.append(msgSys.getMessageWithFallback(
           "objectinfo.bitmap.image_icon", "Image icon: {0}", ic));
      }

      builder.append(super.info(prefix));

      return builder.toString();
   }

   public JDRTextual getTextual()
   {
      return null;
   }

   public boolean hasTextual()
   {
      return false;
   }

   public boolean hasShape()
   {
      return false;
   }

   public boolean hasSymmetricPath()
   {
      return false;
   }

   public JDRSymmetricPath getSymmetricPath()
   {
      return null;
   }

   public boolean hasPattern()
   {
      return false;
   }

   public JDRPattern getPattern()
   {
      return null;
   }

   public Object[] getDescriptionInfo()
   {
      return new Object[] {name_,
       Math.round(100*affineTransform.getTranslateX())/100,
       Math.round(100*affineTransform.getTranslateY())/100};
   }

   public ImageIcon getImageIcon()
   {
      return ic;
   }

   public Image getImage()
   {
      return ic.getImage();
   }

   public int getIconWidth()
   {
      return ic.getIconWidth();
   }

   public int getIconHeight()
   {
      return ic.getIconHeight();
   }

   /**
    * Does nothing. TODO: see if fading can be applied to ImageIcon.
    */
   public void fade(double value)
   {
   }

   public void drawControls(boolean endPt)
   {
   }

   public JDRPoint getControlFromStoragePoint(
      double storagePointX, double storagePointY, boolean endPoint)
   {
      return null;
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      JDRUnit oldUnit = getCanvasGraphics().getStorageUnit();
      JDRUnit newUnit = cg.getStorageUnit();

      if (oldUnit.getID() != newUnit.getID())
      {
         double factor = oldUnit.toUnit(1.0, newUnit);

         double scaleX = affineTransform.getScaleX();//m00
         double scaleY = affineTransform.getScaleY();//m11

         double shearX = affineTransform.getShearX();//m01
         double shearY = affineTransform.getShearY();//m10

         double x = affineTransform.getTranslateX() * factor;
         double y = affineTransform.getTranslateY() * factor;

         affineTransform.setTransform(scaleX, shearY,
           shearX, scaleY, x, y);
      }

      setCanvasGraphics(cg);
   }

   public int getObjectFlag()
   {
      return super.getObjectFlag() | SELECT_FLAG_BITMAP;
   }

   public void setLaTeXCommand(String cs)
   {
      if (cs == null)
      {
         throw new NullPointerException();
      }

      latexCommand = cs;
   }

   public String getLaTeXCommand()
   {
      return latexCommand;
   }

   /**
    * The LaTeX command to use to insert this image into a
    * LaTeX document. This is initialised to "\includegraphics".
    */
   private String latexCommand="\\includegraphics";

   private AffineTransform affineTransform;
   private String filename_, latexlinkname_, name_;
   private File imageFile;

   private ImageIcon ic;

   private static JDRBitmapListener bitmapListener = new JDRBitmapListener();

   private boolean imageLoaded;

   public static Color draftBackgroundColor = new Color(240,240,240,200);
}
