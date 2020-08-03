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
    * Creates a copy. 
    */ 
   public JDRBitmap(JDRBitmap bitmap)
   {
      super(bitmap);

      this.latexCommand = bitmap.latexCommand;
      this.filename_ = bitmap.filename_;
      this.latexlinkname_ = bitmap.latexlinkname_;
      this.name_ = bitmap.name_;
      this.ic = bitmap.ic;
      this.imageLoaded = bitmap.imageLoaded;

      this.flatmatrix = new double[bitmap.flatmatrix.length];

      for (int i = 0; i < flatmatrix.length; i++)
      {
         this.flatmatrix[i] = bitmap.flatmatrix[i];
      }
   }

   private void init(String fullpathname, String latexpathname)
      throws FileNotFoundException,
             InvalidImageFormatException
   {
      filename_      = fullpathname;
      latexlinkname_ = latexpathname;

      File f = new File(filename_);

      if (!f.exists())
      {
         throw new FileNotFoundException(f.getAbsolutePath());
      }

      String imageFormat = null;

      try
      {
         imageFormat = getImageFormat(f);
      }
      catch (IOException e)
      {
         throw new InvalidImageFormatException(f.getAbsolutePath(),
           getCanvasGraphics(), e);
      }

      if (imageFormat == null)
      {
         throw new InvalidImageFormatException(f.getAbsolutePath(),
           getCanvasGraphics());
      }

      name_ = f.getName();

      imageLoaded = true;

      Image image = Toolkit.getDefaultToolkit().createImage(filename_);
      ic = new ImageIcon(image);

      imageLoaded =
         (ic.getImageLoadStatus() == MediaTracker.COMPLETE);

      flatmatrix = new double[6];
      reset();
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

      File file = new File(filename);

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
               MessageInfo.createWarning(new FileNotFoundException(filename)));
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
      flatmatrix[0] = 1.0;
      flatmatrix[1] = 0.0;
      flatmatrix[2] = 0.0;
      flatmatrix[3] = 1.0;
      flatmatrix[4] = 0.0;
      flatmatrix[5] = 0.0;
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
      File file = new File(filename_);
      name_ = file.getName();

      if (!file.exists())
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
            String filename = cg.chooseBitmap(file);

            if (filename != null)
            {
               filename_ = filename;

               file = new File(filename_);
               name_ = file.getName();

               if (!file.exists())
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

      if (!description.isEmpty())
      {
         tex.comment(description);
      }

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
         name = getLaTeXPath(tex.relativize(filename_).toString());
      }

      int idx = name.lastIndexOf(".");

      tex.print(latexCommand
          + "[width="
          + tex.length(cg, unit.fromBp(w))
          + ",height="
          + tex.length(cg, unit.fromBp(h))+"]{");

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
    */
   public void rotate(double angle)
   {
      BBox box = getStorageBBox();
      double x = box.getMinX()+0.5*box.getWidth();
      double y = box.getMinY()+0.5*box.getHeight();

      rotate(new Point2D.Double(x, y), angle);
   }

   /**
    * Rotates this object about the given point.
    */
   public void rotate(Point2D p, double angle)
   {
      // rotate about point p

      // shift p back to origin
      flatmatrix[4] -= p.getX();
      flatmatrix[5] -= p.getY();

      // rotate
      double cosTheta = Math.cos(-angle);
      double sinTheta = Math.sin(-angle);

      double m0 = flatmatrix[0];
      double m1 = flatmatrix[1];
      double m2 = flatmatrix[2];
      double m3 = flatmatrix[3];
      double m4 = flatmatrix[4];
      double m5 = flatmatrix[5];

      flatmatrix[0] = m0*cosTheta+m1*sinTheta;
      flatmatrix[1] = m1*cosTheta-m0*sinTheta;
      flatmatrix[2] = m2*cosTheta+m3*sinTheta;
      flatmatrix[3] = m3*cosTheta-m2*sinTheta;
      flatmatrix[4] = m4*cosTheta+m5*sinTheta;
      flatmatrix[5] = m5*cosTheta-m4*sinTheta;

      // shift p back
      flatmatrix[4] += p.getX();
      flatmatrix[5] += p.getY();
   }

   public void scaleX(Point2D p, double factor)
   {
      scale(p, factor, 1.0);
   }

   public void scaleY(Point2D p, double factor)
   {
      scale(p,1.0,factor);
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

      scale(new Point2D.Double(x, y), factorX, factorY);
   }

   /**
    * Scales this object relative to the given point.
    */
   public void scale(Point2D p, double factorX, double factorY)
   {
      // shift p back to origin
      flatmatrix[4] -= p.getX();
      flatmatrix[5] -= p.getY();

      // scale
      flatmatrix[0] *= factorX;
      flatmatrix[1] *= factorY;
      flatmatrix[2] *= factorX;
      flatmatrix[3] *= factorY;
      flatmatrix[4] *= factorX;
      flatmatrix[5] *= factorY;

      // shift back to p
      flatmatrix[4] += p.getX();
      flatmatrix[5] += p.getY();
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

      shear(new Point2D.Double(x, y), factorX, factorY);
   }

   /**
    * Shears this object relative to the given point.
    */
   public void shear(Point2D p, double factor)
   {
      shear(p,factor,factor);
   }

   /**
    * Shears this object relative to the given point.
    */
   public void shear(Point2D p, double factorX, double factorY)
   {
      // shift p back to origin
      flatmatrix[4] -= p.getX();
      flatmatrix[5] -= p.getY();

      // shear

      double m0 = flatmatrix[0];
      double m1 = flatmatrix[1];
      double m2 = flatmatrix[2];
      double m3 = flatmatrix[3];
      double m4 = flatmatrix[4];
      double m5 = flatmatrix[5];

      flatmatrix[0] = m0-factorX*m1;
      flatmatrix[1] = m1-factorY*m0;
      flatmatrix[2] = m2-factorX*m3;
      flatmatrix[3] = m3-factorY*m2;
      flatmatrix[4] = m4-factorX*m5;
      flatmatrix[5] = m5-factorY*m4;

      // shift back to p
      flatmatrix[4] += p.getX();
      flatmatrix[5] += p.getY();
   }

   /**
    * Translates this object.
    */
   public void translate(double x, double y)
   {
      flatmatrix[4] += x;
      flatmatrix[5] += y;
   }

   /**
    * Gets this object's (bp) transformation. This is the transformation
    * that is applied to the bitmap when it is drawn or exported.
    * @return this object's transformation
    */
   public AffineTransform getAffineTransform()
   {
      return new AffineTransform(flatmatrix);
   }

   /**
    * Transforms this object.
    * @param matrix the transformation matrix to apply to this object
    */
   public void transform(double[] matrix)
   {
      AffineTransform af = getAffineTransform();
      af.concatenate(new AffineTransform(matrix));
      af.getMatrix(flatmatrix);
   }

   /**
    * Transforms this object.
    * @param trans the transformation to apply to this object
    */
   public void transform(AffineTransform trans)
   {
      AffineTransform af = getAffineTransform();
      af.concatenate(trans);
      af.getMatrix(flatmatrix);
   }

   /**
    * Preconcatenates this object's transformation matrix with
    * the given transformation.
    * @param trans the new transformation to apply
    */
   public void preConcatenate(AffineTransform trans)
   {
      AffineTransform af = getAffineTransform();
      af.preConcatenate(trans);
      af.getMatrix(flatmatrix);
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
      return new JDRPoint(getCanvasGraphics(),flatmatrix[4], flatmatrix[5]);
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

         AffineTransform af = new AffineTransform(flatmatrix[0],
           flatmatrix[1], flatmatrix[2], flatmatrix[3],
           flatmatrix[4]+hoffset, flatmatrix[5]+voffset);

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

      for (int i = 0; i < 6; i++)
      {
         if (flatmatrix[i] != b.flatmatrix[i]) return false;
      }

      return true;
   }

   /**
    * Gets the transformation matrix applied to this object.
    * @param matrix on exit contains this object's transformation
    * matrix
    */
   public void getTransformation(double[] matrix)
   {
      for (int i = 0; i < 6; i++) matrix[i] = flatmatrix[i];
   }

   /**
    * Sets this object's transformation matrix.
    * @param matrix the new transformation matrix
    */
   public void setTransformation(double[] matrix)
   {
      for (int i = 0; i < 6; i++) flatmatrix[i] = matrix[i];
   }

   /**
    * Sets this object's transformation matrix.
    * @param af the new transformation
    */
   public void setTransformation(AffineTransform af)
   {
      af.getMatrix(flatmatrix);
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
       svg.println("      transform=\"matrix("
       + flatmatrix[0] + "," + flatmatrix[1] +"," 
       + flatmatrix[2] + "," + flatmatrix[3] +","
       + flatmatrix[4] + "," + flatmatrix[5] +")\"");
       svg.println("      width=\""
         +ic.getIconWidth()+"pt\"");
       svg.println("      height=\""
         +ic.getIconHeight()+"pt\"");
       svg.println("      xlink:href=\""+filename_+"\">");
       svg.println("   <desc>");
       svg.println(filename_);
       svg.println("   </desc>");
       svg.println("   </image>");
   } 

   public JDRObjectLoaderListener getListener()
   {
      return bitmapListener;
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "Bitmap:"+eol;
      str += "name: "+name_+eol;
      str += "filename: "+filename_+eol;
      str += "transformation matrix: ["
        + "["+flatmatrix[0]+","+flatmatrix[2]+","+flatmatrix[4]+"]"
        + "["+flatmatrix[1]+","+flatmatrix[3]+","+flatmatrix[5]+"]"
        + "]" +eol;
      str += "LaTeX link name: "+latexlinkname_+eol;
      str += "LaTeX command: "+latexCommand+eol;
      str += "image loaded: "+imageLoaded+eol;
      str += "image icon: "+ic+eol;

      return str+super.info();
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
       Math.round(100*flatmatrix[4])/100,
       Math.round(100*flatmatrix[5])/100};
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

         flatmatrix[4] *= factor;
         flatmatrix[5] *= factor;
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
    * LaTeX document. This is initialised to "\pgfimage".
    */
   private volatile String latexCommand="\\includegraphics";

   private volatile double[] flatmatrix;
   private volatile String filename_, latexlinkname_, name_;

   private volatile ImageIcon ic;

   private static JDRBitmapListener bitmapListener = new JDRBitmapListener();

   private volatile boolean imageLoaded;

   public static Color draftBackgroundColor = new Color(240,240,240,200);
}
