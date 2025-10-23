// File          : JDRCompleteObject.java
// Creation Date : 18th August 2010
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
import java.util.Vector;
import java.util.NoSuchElementException;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.lang.Math;
import java.text.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;

import javax.swing.*;

import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a complete JDR object. (As opposed to part of
 * an object, such as a segment in a path)
 * Each complete JDR object may have any of the following style attributes:
 * draw colour (the line colour or text colour), fill colour (for
 * paths only), draft colour (for paths only) and 
 * stroke (paths only).
 * <p>
 * All objects may have an associated description and flowframe 
 * information.
 * @author Nicola L C Talbot
 */

public abstract class JDRCompleteObject extends JDRObject
  implements JDRConstants
{
    public JDRCompleteObject(CanvasGraphics cg)
    {
       super(cg);
    }

    public JDRCompleteObject(JDRCompleteObject obj)
    {
       super(obj);

       if (obj.flowframe != null)
       {
          flowframe = new FlowFrame(obj.flowframe);
       }

       description = obj.description;
       tag = obj.tag;
       editMode = obj.editMode;
       parent = obj.parent;
       index_ = obj.index_;
    }

   /**
    * Gets this object's extent including extra visible information
    * such as annotations or control points.
    * @param cg the graphics information
    * @return this object's extent (in bp units)
    * @see #getBpBBox()
    * @see #getBpControlBBox()
    */
   public BBox getBpExtent()
   {
      BBox justBBox = getBpBBox();

      if (justBBox == null)
      {
         return null;
      }

      BBox box = justBBox;

      if (isEdited())
      {
         BBox controlBBox = getBpControlBBox();

         if (controlBBox != null) box.merge(controlBBox);
      }

      if (flowframe != null)
      {
         box.merge(flowframe.getLabelBounds(justBBox));
      }

      return box;
   }

   /**
    * Determines if this object is completely inside the given box.
    * If this object is being edited,
    * checks to see if the bounding box given by
    * {@link #getStorageControlBBox()} lies inside the given box, otherwise
    * checks to see if the bounding box given by
    * {@link #getStorageBBox()} lies inside the given box.
    * @param cg the graphics information
    * @param box the area under inspection (in storage units)
    * @return true if this object is completely inside the given box
    * otherwise false
    * @see #intersects(BBox)
    */
   public boolean isCompletelyInsideStorageBox(BBox box)
   {
      BBox thisbox = (isEdited() ? getStorageControlBBox() : getStorageBBox());

      if (thisbox == null)
      {
         return false;
      }

      if (box.contains(thisbox.getMinX(),thisbox.getMinY())
          && 
          box.contains(thisbox.getMinX(),thisbox.getMaxY())
          &&
          box.contains(thisbox.getMaxX(),thisbox.getMinY())
          &&
          box.contains(thisbox.getMaxX(),thisbox.getMaxY()))
      {
         return true;
      }

      return false;
   }

   public boolean isCompletelyInsideBpBox(BBox box)
   {
      BBox thisbox = (isEdited() ? getBpControlBBox() : getBpBBox());

      if (thisbox == null)
      {
         return false;
      }

      if (box.contains(thisbox.getMinX(),thisbox.getMinY())
          && 
          box.contains(thisbox.getMinX(),thisbox.getMaxY())
          &&
          box.contains(thisbox.getMaxX(),thisbox.getMinY())
          &&
          box.contains(thisbox.getMaxX(),thisbox.getMaxY()))
      {
         return true;
      }

      return false;
   }

   public boolean isCompletelyInsideComponentBox(BBox box)
   {
      BBox thisbox = (isEdited() ? getComponentControlBBox() : 
         getComponentBBox());

      if (thisbox == null)
      {
         return false;
      }

      if (box.contains(thisbox.getMinX(),thisbox.getMinY())
          && 
          box.contains(thisbox.getMinX(),thisbox.getMaxY())
          &&
          box.contains(thisbox.getMaxX(),thisbox.getMinY())
          &&
          box.contains(thisbox.getMaxX(),thisbox.getMaxY()))
      {
         return true;
      }

      return false;
   }

   /**
    * Determines if this object intersects the given box. This object
    * may only be partially inside. If this object is being edited
    * checks to see if the bounding box given by
    * {@link #getStorageControlBBox()} lies inside the given box, otherwise
    * checks to see if the bounding box given by
    * {@link #getStorageBBox()} lies inside the given box.
    * @param box the area under inspection (in storage units)
    * @return true if this object is completely or partially inside 
    * the given box otherwise false
    * @see #isCompletelyInsideStorageBox(BBox)
    * @see #intersects(Rectangle)
    */
   public boolean intersectsStorageBox(BBox box)
   {
      BBox thisbox = (isEdited() ? getStorageControlBBox() : getStorageBBox());

      if (thisbox == null)
      {
         return false;
      }

      return thisbox.intersects(box);
   }

   /**
    * Determines if this object intersects the given box. This object
    * may only be partially inside. If this object is being edited,
    * checks to see if the bounding box given by
    * {@link #getStorageControlBBox()} lies inside the given box, otherwise
    * checks to see if the bounding box given by
    * {@link #getStorageBBox()} lies inside the given box.
    * @param box the area under inspection
    * @return true if this object is completely or partially inside 
    * the given box otherwise false
    * @see #intersects(BBox)
    * @see #isCompletelyInsideStorageBox(BBox)
    */
   public boolean intersectsStorageRect(Rectangle rect)
   {
      BBox thisbox = (isEdited() ? getStorageControlBBox() : getStorageBBox());

      if (thisbox == null)
      {
         return false;
      }

      return thisbox.intersects(rect.getX(), rect.getY(), rect.getWidth(),
        rect.getHeight());
   }

   public boolean intersectsBpBox(BBox box)
   {
      BBox thisbox = (isEdited() ? getBpControlBBox() : getBpBBox());

      if (thisbox == null)
      {
         return false;
      }

      return thisbox.intersects(box.getMinX(),
         box.getMinY(), box.getWidth(), box.getHeight());
   }

   public boolean intersectsBpRect(Rectangle rect)
   {
      BBox thisbox = (isEdited() ? getBpControlBBox() : getBpBBox());

      if (thisbox == null)
      {
         return false;
      }

      return thisbox.intersects(rect.getX(), rect.getY(), rect.getWidth(),
        rect.getHeight());
   }

   /**
    * Determines if the given point lies inside this object's bounding
    * box.
    * @param bpPoint the point under inspection
    * @return true if the point lies inside this object's bounding
    * box otherwise false
    */
   public boolean containsBpPoint(JDRPoint bpPoint)
   {
      return containsBpPoint(bpPoint.getX(), bpPoint.getY());
   }

   /**
    * Determines if the given point lies inside this object's bounding
    * box.
    * @param bpPoint the point under inspection
    * @return true if the point lies inside this object's bounding
    * box otherwise false
    */
   public boolean containsBpPoint(Point bpPoint)
   {
      return containsBpPoint(bpPoint.getX(), bpPoint.getY());
   }

   /**
    * Determines if the given point lies inside this object's bounding
    * box.
    * @param p the point under inspection
    * @return true if the point lies inside this object's bounding
    * box otherwise false
    */
   public boolean containsBpPoint(Point2D p)
   {
      return containsBpPoint(p.getX(), p.getY());
   }

   public boolean containsBpPoint(double x, double y)
   {
      BBox box = getBpBBox();

      if (box == null)
      {
         return false;
      }

      return box.contains(x, y);
   }

   public boolean containsStoragePoint(double x, double y)
   {
      BBox box = getStorageBBox();

      if (box == null)
      {
         return false;
      }

      return box.contains(x, y);
   }

   public boolean containsComponentPoint(double x, double y)
   {
      BBox box = getComponentBBox();

      if (box == null)
      {
         return false;
      }

      return box.contains(x, y);
   }

   /**
    * Refreshes this object. Only applicable for bitmaps or groups
    * containing bitmaps or distorted objects with a bitmap as the
    * underlying object. 
    * @return true if successful otherwise false
    */
   public boolean refresh()
   {
      return true;
   }

   /**
    * Resets the transformation matrix. Only applicable for bitmaps,
    * text areas and distorted objects.
    */
   public void reset()
   {
   }

   /**
    * Updates this object's bounds. Only applicable for text areas.
    */
   public void updateBounds()
   {
   }

   /**
    * Shifts this object horizontally so that it is left aligned with the given
    * x co-ordinate.
    * @param leftmostX the x co-ordinate along which to align this 
    * object
    * @see #rightAlign(double)
    * @see #centreAlign(double)
    */
   public void leftAlign(double leftmostX)
   {
      BBox bbox = getStorageBBox();

      if (bbox != null)
      {
         translate(leftmostX-bbox.getMinX(), 0);
      }
   }

   /**
    * Shifts this object horizontally so that it is right aligned with the given
    * x co-ordinate.
    * @param rightmostX the x co-ordinate along which to align this 
    * object
    * @see #leftAlign(double)
    * @see #centreAlign(double)
    */
   public void rightAlign(double rightmostX)
   {
      BBox bbox = getStorageBBox();

      if (bbox != null)
      {
         translate(rightmostX-bbox.getMaxX(), 0);
      }
   }

   /**
    * Shifts this object horizontally so that it is centred at the given
    * x co-ordinate.
    * @param centreX the x co-ordinate along which to align this 
    * object
    * @see #leftAlign(double)
    * @see #rightAlign(double)
    */
   public void centreAlign(double centreX)
   {
      BBox bbox = getStorageBBox();

      if (bbox != null)
      {
         double mid = bbox.getMinX()
                    + 0.5*(bbox.getMaxX()-bbox.getMinX());
         translate(centreX-mid, 0);
      }
   }

   /**
    * Shifts this object vertically so that its top is aligned along the given
    * y co-ordinate.
    * @param topmostY the y co-ordinate along which to align this 
    * object
    * @see #middleAlign(double)
    * @see #bottomAlign(double)
    */
   public void topAlign(double topmostY)
   {
      BBox bbox = getStorageBBox();

      if (bbox != null)
      {
         translate(0, topmostY-bbox.getMinY());
      }
   }

   /**
    * Shifts this object vertically so that its middle is aligned along the given
    * y co-ordinate.
    * @param middleY the y co-ordinate along which to align this 
    * object
    * @see #topAlign(double)
    * @see #bottomAlign(double)
    */
   public void middleAlign(double middleY)
   {
      BBox bbox = getStorageBBox();

      if (bbox != null)
      {
         double mid = bbox.getMinY()
                    + 0.5*(bbox.getMaxY()-bbox.getMinY());
         translate(0, middleY-mid);
      }
   }

   /**
    * Shifts this object vertically so that its bottom is aligned along the given
    * y co-ordinate.
    * @param bottommostY the y co-ordinate along which to align this 
    * object
    * @see #topAlign(double)
    * @see #middleAlign(double)
    */
   public void bottomAlign( double bottommostY)
   {
      BBox bbox = getStorageBBox();

      if (bbox != null)
      {
         translate(0, bottommostY-bbox.getMaxY());
      }
   }

   /**
    * Draw this object's associated flowframe information.
    * This method uses {@link FlowFrame#draw(BBox)}
    * if flowframe data has been set for this object. If no
    * flowframe data has been assigned to this object, nothing
    * happens.
    * The Graphics2D component of the CanvasGraphics object should
    * be set before this method is called.
    */
   public void drawFlowFrame()
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (flowframe != null)
      {
         BBox box = getStorageBBox();

         if (cg.isEvenPage())
         {
            FlowFrame typeblock = getTypeblock();

            if (typeblock != null)
            {
               box.translate(-typeblock.getEvenXShift(), 0.0);
            }
         }

         cg.setColor(Color.lightGray);
         flowframe.draw(box);
      }
   }

   public abstract void print(Graphics2D g2);

   public void draw()
   {
      draw((FlowFrame)null);
   }

   public abstract void draw(FlowFrame parentFrame);

   /**
    * Draws this object.
    * The Graphics2D component of the CanvasGraphics object should
    * be set before this method is called.
    * @param draft determines whether to use draft mode
    */
   public void draw(boolean draft)
   {
      draw(draft, (FlowFrame)null);
   }

   public void draw(boolean draft, FlowFrame parentFrame)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (draft)
      {
         cg.setPaint(draftColor);
         drawDraft(parentFrame);
      }
      else
      {
         draw(parentFrame);

         drawFlowFrame();
      }
   }

   public void drawDraft()
   {
      drawDraft((FlowFrame)null);
   }

   public void drawDraft(FlowFrame parentFrame)
   {
      draw(parentFrame);
   }

   /**
    * Writes the flowframe data associated with this object in
    * a LaTeX file. This method calls 
    * {@link FlowFrame#tex(JDRObject,Rectangle2D,PrintWriter,double)}.
    * If no flowframe data is assigned to this object, nothing happens.
    * @param typeblock the document's typeblock
    * @param out the output stream
    * @param baselineskip the document's baselineskip
    * @param unit the unit of measurement
    * @throws IOException if an I/O error occurs
    * @throws InvalidShapeException if frame has a nonstandard
    * shape but the required shape command can't reproduce the
    * required shape
    */
   public void saveFlowframe(TeX tex, Rectangle2D typeblock,
                             double baselineskip, boolean useHPaddingShapepar)
      throws IOException,InvalidShapeException
   {
      if (flowframe != null)
      {
         flowframe.tex(tex, this, typeblock, baselineskip, useHPaddingShapepar);
      }
   }

   public void makeEqual(JDRObject object)
   {
      super.makeEqual(object);

      JDRCompleteObject obj = (JDRCompleteObject)object;

      description = obj.description;
      tag = obj.tag;
      parent = obj.parent;
      index_ = obj.index_;
      editMode = obj.editMode;

      if (obj.flowframe != null)
      {
         if (flowframe == null)
         {
            flowframe = new FlowFrame(obj.flowframe);
         }
         else
         {
            flowframe.makeEqual(obj.flowframe);
         }
      }
      else
      {
         flowframe = null;
      }
   }

   public boolean equals(Object o)
   {
      if (!super.equals(o)) return false;

      if (!(o instanceof JDRCompleteObject))
      {
         return false;
      }

      JDRCompleteObject obj = (JDRCompleteObject)o;

      if (flowframe == null)
      {
         if (obj.flowframe != null) return false;
      }
      else
      {
         if (!flowframe.equals(obj.flowframe)) return false;
      }

      if (!description.equals(obj.description)) return false;

      if (!tag.equals(obj.tag)) return false;

      //if (parent != obj.parent) return false;

      return true;
   }

   public JDRCompleteObject getParent()
   {
      return parent;
   }

   /**
    * Gets information about this object.
    * @return string containing this object's information
    */
   public String info()
   {
      return String.format("description: %s%ntag: %s%nflowframe: %s%nis selected: %s%nbounding box:%s%nhash code: %s%n",
        description, tag, flowframe, isSelected(), getStorageBBox().info(), 
        hashCode());
   }

   /**
    * Rotates this object about the centre of the object's bounding
    * box.
    * @param angle the angle of rotation
    */
   public void rotate(double angle)
   {
      BBox box = getStorageBBox();

      if (box != null)
      {
         Point2D p = new Point2D.Double(box.getMidX(), box.getMidY());

         rotate(p, angle);
      }
   }

   /**
    * Rotates this object about the given point.
    * @param p the point of rotation
    * @param angle the angle of rotation
    */
   public abstract void rotate(Point2D p, double angle);

   /**
    * Scales this object relative to the top left hand corner of its
    * bounding box.
    * @param scalex the x scale factor
    * @param scaley the y scale factor
    */
   public void scale(double scalex, double scaley)
   {
      BBox box = getStorageBBox();

      if (box != null)
      {
         Point2D p = new Point2D.Double(box.getMinX(), box.getMinY());

         scale(p, scalex, scaley);
      }
   }

   /**
    * Scales this object relative to the given point.
    * @param p the given point
    * @param scalex the x scale factor
    * @param scaley the y scale factor
    */
   public abstract void scale(Point2D p, double scalex, double scaley);

   /**
    * Shears this object relative to the bottom left corner.
    * @param factorX the x shear factor
    * @param factorY the y shear factor
    */
   public void shear(double factorX, double factorY)
   {
      BBox box = getStorageBBox();

      if (box != null)
      {
         Point2D p = new Point2D.Double(box.getMinX(), box.getMaxY());

         shear(p, factorX, factorY);
      }
   }

   /**
    * Shears this object relative to the given point.
    * @param p the given point
    * @param shearx the x shear factor
    * @param sheary the y shear factor
    */
   public abstract void shear(Point2D p, double shearx, double sheary);

   /**
    * Translates this object.
    * @param x the x translation
    * @param y the y translation
    */
   public abstract void translate(double x, double y);

   /**
    * Transforms this object.
    * @param matrix the transformation matrix
    */
   public abstract void transform(double[] matrix);

   public abstract Object clone();

   /**
    * Sets the edit mode.
    * @param mode if true this object is in edit mode
    */
   public void setEditMode(boolean mode)
   {
      editMode = mode;
   }

   /**
    * Determines if this object is in edit mode.
    * @return true if this object is in edit mode
    */
   public boolean isEdited()
   {
      return editMode;
   }

   /**
    * Determines if this object has a textual element.
    * @return true if this object is either an instance of
    * {@link JDRTextual} or has a dependent element that is an
    * instance of {@link JDRTextual}
    */
   public abstract boolean hasTextual();

   /**
    * Gets the textual element.
    * @return this object, if this is an instance of {@link 
    * JDRTextual}, or the textual element of this object, or null if 
    * this object doesn't have a textual element.
    * @see #hasTextual()
    */
   public abstract JDRTextual getTextual();

   public abstract boolean hasShape();

   /**
    * Determines if this object has a symmetrical element.
    * @return true if this object is an instance of {@link
    * JDRSymmetricPath} or has a dependent element that is an
    * instance of {@link JDRSymmetricPath}
    */
   public abstract boolean hasSymmetricPath();

   /**
    * Gets the symmetric element.
    * @return this object, if this is an instance of {@link
    * JDRSymmetricPath} or the underlying symmetric path of this
    * object if it has one, or null otherwise
    */
   public abstract JDRSymmetricPath getSymmetricPath();

   /**
    * Determines if this object has a pattern. 
    */
   public abstract boolean hasPattern();

   /**
    * Gets the pattern element.
    * @return this object, if this is an instance of {@link
    * JDRPattern} or the underlying pattern of this
    * object if it has one, or null otherwise
    */
   public abstract JDRPattern getPattern();

   public boolean isDistortable()
   {
      return false;
   }

   /**
    * Gets information strings for "Find by Description" box.
    * (Information given when description hasn't been set.)
    */
   public abstract Object[] getDescriptionInfo();

   /**
    * Multiplies alpha by given factor.
    * @param value reduction factor
    */
   public abstract void fade(double value);

   public int getHotspotFromStoragePoint(Point2D storagePoint)
   {
      BBox box = getStorageBBox();

      if (flowframe != null && canvasGraphics.isEvenPage())
      {
         box.translate(-flowframe.getEvenXShift(),
                       -flowframe.getEvenYShift());
      }

      return box.getHotspotFromStoragePoint(storagePoint);
   }

   public int getHotspotFromBpPoint(Point2D bpPoint)
   {
      BBox box = getStorageBBox();

      if (flowframe != null && canvasGraphics.isEvenPage())
      {
         box.translate(-flowframe.getEvenXShift(),
                       -flowframe.getEvenYShift());
      }

      return box.getHotspotFromBpPoint(bpPoint);
   }

   public int getHotspotFromComponentPoint(Point2D compPoint)
   {
      BBox box = getStorageBBox();

      if (flowframe != null && canvasGraphics.isEvenPage())
      {
         box.translate(-flowframe.getEvenXShift(),
                       -flowframe.getEvenYShift());
      }

      return box.getHotspotFromComponentPoint(compPoint);
   }

   public JDRPoint getTopLeftHS()
   {
      return getStorageBBox().getTopLeft();
   }

   public JDRPoint getBottomLeftHS()
   {
      return getStorageBBox().getBottomLeft();
   }

   public JDRPoint getBottomRightHS()
   {
      return getStorageBBox().getBottomRight();
   }

   public JDRPoint getTopRightHS()
   {
      return getStorageBBox().getTopRight();
   }

   public JDRPoint getCentreHS()
   {
      return getStorageBBox().getCentre();
   }

   public BBox getDragBBox()
   {
      return getStorageBBox();
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      super.setCanvasGraphics(cg);

      if (flowframe != null)
      {
         flowframe.setCanvasGraphics(cg);
      }
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      if (flowframe != null)
      {
         flowframe.applyCanvasGraphics(cg);
      }

      super.setCanvasGraphics(cg);
   }

   public int getObjectFlag()
   {
      return SELECT_FLAG_OBJECT;
   }

   public FlowFrame getFlowFrame()
   {
      return flowframe;
   }

   public synchronized void setFlowFrame(FlowFrame f)
   {
      flowframe = f;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String desc)
   {
      description = desc;
   }

   public String getTag()
   {
      return tag;
   }

   public void setTag(String newTag)
   {
      tag = newTag;
   }

   public int getIndex()
   {
      return index_;
   }

   public FlowFrame getTypeblock()
   {
      if (flowframe != null && flowframe.getType() == FlowFrame.TYPEBLOCK)
      {
         return flowframe;
      }

      if (parent == null)
      {
         return null;
      }

      return parent.getTypeblock();
   }

   /**
    * This object's associated flowframe data. Initialised to 
    * <code>null</code> (no flowframe data).
    */
   protected volatile FlowFrame flowframe=null;
   /**
    * This object's description. Initialised to an empty string.
    */
   protected volatile String description="";

   /**
    * This object's tag. Initialised to an empty string.
    */
   protected String tag="";

   /**
    * Font used for annotations, such as flowframe information
    * or bitmap draft mode.
    */
   public static Font annoteFont = new Font("SansSerif", Font.PLAIN, 10);

   protected volatile boolean editMode=false;

   /**
    * This object's parent. Null if this object is not part of
    * a group.
    */
   protected volatile JDRCompleteObject parent = null;

   /*
    * This object's index with reference to its parent.
    */ 
   protected volatile int index_ = -1;
}
