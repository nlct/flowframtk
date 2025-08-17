/*
    Copyright (C) 2025 Nicola L.C. Talbot

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
import java.awt.geom.*;
import java.util.*;

import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * A move that closes a sub path.
 * @author Nicola L C Talbot
 * @see JDRPath
 */
public class JDRClosingMove extends JDRSegment
implements JDRPathChangeListener
{
   protected JDRClosingMove(CanvasGraphics cg)
   {
      super(cg);
   }

   /**
    * Creates a copy of a segment. 
    */ 
   public JDRClosingMove(JDRClosingMove segment)
   {
      super(segment);
      path = segment.path;
      path.addPathChangeListener(this);
   }

   /**
    * Creates a new segment with given start and end locations.
    * The start of the sub path can't be found until this segment is
    * added to a path.
    * @param p0 the starting point
    * @param p1 the end point
    */
   public JDRClosingMove(CanvasGraphics cg, double x0, double y0, double x1, double y1)
    throws ClosingMoveException
   {
      super(cg);

      start = new JDRPoint(getCanvasGraphics(), x0, y0);
      end = new JDRPoint(getCanvasGraphics(), x1, y1);
   }

   /**
    * Creates a new segment with given start and end locations.
    * @param p0 the starting point
    * @param p1 the end point
    * @param path the path
    * @param index this segment's index in the path
    * (or the index it will have if it's about to be added)
    */
   public JDRClosingMove(JDRPoint p0, JDRPoint p1, JDRPath path, int index)
    throws ClosingMoveException
   {
      super(p0.getCanvasGraphics());

      start = p0;
      end = p1;

      updateSubPathStart(path, index);
      path.addPathChangeListener(this);
   }

   /**
    * Creates a new segment with given start and end locations.
    * @param x0 the starting point x coord
    * @param y0 the starting point y coord
    * @param x1 the end point x coord
    * @param y1 the end point y coord
    * @param path the path
    * @param index this segment's index in the path
    * (or the index it will have if it's about to be added)
    */
   public JDRClosingMove(double x0, double y0, double x1, double y1,
     JDRPath path, int index)
    throws ClosingMoveException
   {
      super(path.getCanvasGraphics());

      start = new JDRPoint(getCanvasGraphics(), x0, y0);
      end = new JDRPoint(getCanvasGraphics(), x1, y1);

      updateSubPathStart(path, index);
      path.addPathChangeListener(this);
   }

   /**
    * Creates a new segment with given start locations.
    * The end is set to the start of the sub path.
    * @param x the starting point x coord
    * @param y the starting point y coord
    * @param path the path
    * @param index this segment's index in the path
    * (or the index it will have if it's about to be added)
    * @param subPathStartSeg the sub path's starting segment
    */
   public JDRClosingMove(double x, double y, JDRPath path, int index,
      JDRSegment subPathStartSeg)
   {
      super(path.getCanvasGraphics());

      this.path = path;
      this.pathSegmentIndex = index;
      start = new JDRPoint(getCanvasGraphics(), x, y);
      subPathStart = subPathStartSeg.start;
      end = subPathStart;
      path.addPathChangeListener(this);
   }

   /**
    * Creates a new segment with given start locations.
    * The end is set to the start of the sub path.
    * @param x0 the start point x coord
    * @param y0 the start point y coord
    * @param x1 the end point x coord
    * @param y1 the end point y coord
    * @param path the path
    * @param index this segment's index in the path
    * (or the index it will have if it's about to be added)
    * @param subPathStartSeg the sub path's starting segment
    */
   public JDRClosingMove(double x0, double y0,
      double x1, double y1, JDRPath path, int index,
      JDRSegment subPathStartSeg)
   {
      super(path.getCanvasGraphics());

      this.path = path;
      this.pathSegmentIndex = index;
      start = new JDRPoint(getCanvasGraphics(), x0, y0);
      end = new JDRPoint(getCanvasGraphics(), x1, y1);

      subPathStart = subPathStartSeg.start;

      path.addPathChangeListener(this);
   }

   public JDRClosingMove(JDRPoint p0, JDRPoint p1, JDRPath path, int index,
      JDRSegment subPathStartSeg)
   {
      super(path.getCanvasGraphics());

      this.path = path;
      this.pathSegmentIndex = index;
      start = p0;
      end = p1;

      subPathStart = subPathStartSeg.start;

      path.addPathChangeListener(this);
   }

   public void updateSubPathStart(JDRPath path, int index)
    throws ClosingMoveException
   {
      this.path = path;
      this.pathSegmentIndex = index;

      JDRPoint newStart = null;

      JDRPathSegment prevSeg = null;

      for (int i = index-1; i >= 0; i--)
      {
         JDRPathSegment seg = path.get(i);

         if (seg.isGap())
         {
            if (prevSeg == null)
            {
               throw new ClosingMoveException(path, this, index);
            }

            newStart = prevSeg.getStart();
            break;
         } 
         else if (i == 0)
         {
            newStart = seg.getStart();
         }

         prevSeg = seg;
      }

      if (newStart == null)
      {
         throw new ClosingMoveException(path, this, index);
      }

      subPathStart = newStart;
   }

   @Override
   public void pathChanged(JDRPathChangeEvent evt)
   throws ClosingMoveException
   {
      if (path == null)
      {
         path = evt.getPath();
      }

      switch (evt.getChangeType())
      {
         case SEGMENT_ADDED:
            if (evt.getNewSegment() == this)
            {
               if (path != evt.getPath() || pathSegmentIndex != evt.getIndex()
                  || subPathStart == null)
               {
                  if (path != evt.getPath())
                  {
                     path.removePathChangeListener(this);
                     path = evt.getPath();
                  }

                  pathSegmentIndex = evt.getIndex();
                  updateSubPathStart(path, pathSegmentIndex);
               }
            }
            else if (evt.getIndex() < pathSegmentIndex)
            {
               updateSubPathStart(path, pathSegmentIndex);
            }
         break;
         case SEGMENT_INSERTED:
            if (evt.getNewSegment() == this)
            {
               if (path != evt.getPath() || pathSegmentIndex != evt.getIndex()
                  || subPathStart == null)
               {
                  if (path != evt.getPath())
                  {
                     path.removePathChangeListener(this);
                     path = evt.getPath();
                  }

                  pathSegmentIndex = evt.getIndex();
                  updateSubPathStart(path, pathSegmentIndex);
               }
            }
            else if (evt.getIndex() <= pathSegmentIndex)
            {
               pathSegmentIndex++;
               updateSubPathStart(path, pathSegmentIndex);
            }
         break;
         case SEGMENT_REMOVED:

            if (evt.getOldSegment() == this)
            {
               path.removePathChangeListener(this);
               path = null;
               pathSegmentIndex = -1;
               subPathStart = null;
            }
            else if (evt.getIndex() < pathSegmentIndex)
            {
               updateSubPathStart(path, pathSegmentIndex);
            }

         break;
         case SEGMENT_CHANGED:

            if (evt.getOldSegment() == this)
            {
               path.removePathChangeListener(this);
               path = null;
               pathSegmentIndex = -1;
               subPathStart = null;
            }
            else if (evt.getNewSegment() == this)
            {
               if (path != evt.getPath())
               {
                  path.removePathChangeListener(this);
                  path = evt.getPath();
               }

               pathSegmentIndex = evt.getIndex();
               updateSubPathStart(path, pathSegmentIndex);
            }
            else if (evt.getIndex() < pathSegmentIndex)
            {
               updateSubPathStart(path, pathSegmentIndex);
            }

         break;
         case PATH_CHANGED:

            int idx = -1; 

            for (int i = path.size()-1; i >= 0; i--)
            {
               JDRPathSegment seg = path.get(i);

               if (seg == this)
               {
                  idx = i;
                  break;
               }
            }

            if (pathSegmentIndex > -1)
            {
               updateSubPathStart(path, idx);
            }
            else
            {
               path.removePathChangeListener(this);
               path = null;
               pathSegmentIndex = -1;
               subPathStart = null;
            }

         break;
      }
   }

   public JDRSegment convertToNonClosingMove()
   {
      return (JDRSegment)convertToSegment();
   }

   public BBox getReflectedBBox(JDRLine line)
   {
      Point2D p = end.getReflection(line);

      double minX = p.getX();
      double minY = p.getY();
      double maxX = p.getX();
      double maxY = p.getY();

      for (int i = 0, n = controlCount(); i < n; i++)
      {
         p = getControl(i).getReflection(line);

         if (p.getX() < minX)
         {
            minX = p.getX();
         }
         else if (p.getX() > maxX)
         {
            maxX = p.getX();
         }

         if (p.getY() < minY)
         {
            minY = p.getY();
         }
         else if (p.getY() > maxY)
         {
            maxY = p.getY();
         }
      }

      if (subPathStart != null)
      {
         p = subPathStart.getReflection(line);

         if (p.getX() < minX)
         {
            minX = p.getX();
         }
         else if (p.getX() > maxX)
         {
            maxX = p.getX();
         }

         if (p.getY() < minY)
         {
            minY = p.getY();
         }
         else if (p.getY() > maxY)
         {
            maxY = p.getY();
         }
      }

      p = null;

      return new BBox(getCanvasGraphics(), minX, minY, maxX, maxY);
   }

   public void mergeReflectedBBox(JDRLine line, BBox box)
   {
      double minX = box.getMinX();
      double minY = box.getMinY();
      double maxX = box.getMaxX();
      double maxY = box.getMaxY();

      Point2D p = end.getReflection(line);

      if (p.getX() < minX)
      {
         minX = p.getX();
      }
      else if (p.getX() > maxX)
      {
         maxX = p.getX();
      }

      if (p.getY() < minY)
      {
         minY = p.getY();
      }
      else if (p.getY() > maxY)
      {
         maxY = p.getY();
      }

      for (int i = 0, n = controlCount(); i < n; i++)
      {
         p = getControl(i).getReflection(line);

         if (p.getX() < minX)
         {
            minX = p.getX();
         }
         else if (p.getX() > maxX)
         {
            maxX = p.getX();
         }

         if (p.getY() < minY)
         {
            minY = p.getY();
         }
         else if (p.getY() > maxY)
         {
            maxY = p.getY();
         }
      }

      if (subPathStart != null)
      {
         p = subPathStart.getReflection(line);

         if (p.getX() < minX)
         {
            minX = p.getX();
         }
         else if (p.getX() > maxX)
         {
            maxX = p.getX();
         }

         if (p.getY() < minY)
         {
            minY = p.getY();
         }
         else if (p.getY() > maxY)
         {
            maxY = p.getY();
         }
      }

      box.reset(minX, minY, maxX, maxY);
   }

   /**
    * Gets this segment's bounding box.
    */
   public BBox getStorageBBox()
   {
      double minX = end.x;
      double minY = end.y;
      double maxX = end.x;
      double maxY = end.y;

      for (int i = 0, n = controlCount(); i < n; i++)
      {
         JDRPoint p = getControl(i);

         if (p.x < minX)
         {
            minX = p.x;
         }
         else if (p.x > maxX)
         {
            maxX = p.x;
         }

         if (p.y < minY)
         {
            minY = p.y;
         }
         else if (p.y > maxY)
         {
            maxY = p.y;
         }
      }

      if (subPathStart != null)
      {
         if (subPathStart.x < minX)
         {
            minX = subPathStart.x;
         }
         else if (subPathStart.x > maxX)
         {
            maxX = subPathStart.x;
         }

         if (subPathStart.y < minY)
         {
            minY = subPathStart.y;
         }
         else if (subPathStart.y > maxY)
         {
            maxY = subPathStart.y;
         }
      }

      return new BBox(getCanvasGraphics(), minX, minY, maxX, maxY);
   }

   public void mergeStorageBBox(BBox box)
   {
      double minX = end.x;
      double minY = end.y;
      double maxX = end.x;
      double maxY = end.y;

      for (int i = 0, n = controlCount(); i < n; i++)
      {
         JDRPoint p = getControl(i);

         if (p.x < minX)
         {
            minX = p.x;
         }
         else if (p.x > maxX)
         {
            maxX = p.x;
         }

         if (p.y < minY)
         {
            minY = p.y;
         }
         else if (p.y > maxY)
         {
            maxY = p.y;
         }
      }

      if (subPathStart != null)
      {
         if (subPathStart.x < minX)
         {
            minX = subPathStart.x;
         }
         else if (subPathStart.x > maxX)
         {
            maxX = subPathStart.x;
         }

         if (subPathStart.y < minY)
         {
            minY = subPathStart.y;
         }
         else if (subPathStart.y > maxY)
         {
            maxY = subPathStart.y;
         }
      }

      box.merge(minX, minY, maxX, maxY);
   }

   public BBox getStorageControlBBox()
   {
      BBox box = super.getStorageControlBBox();

      if (subPathStart != null)
      {
         subPathStart.mergeStorageControlBBox(box);
      }

      return box;
   }

   public void mergeStorageControlBBox(BBox box)
   {
      super.mergeStorageControlBBox(box);

      if (subPathStart != null)
      {
         subPathStart.mergeStorageControlBBox(box);
      }
   }

   /**
    * Gets the gradient vector for this segment.
    * @return gradient vector
    */
   public Point2D getdP()
   {
      if (subPathStart == null)
      {
         return super.getdP();
      }
      else
      {
         return JDRLine.getGradient(start.x, start.y, subPathStart.x, subPathStart.y);
      }
   }

   public void print(Graphics2D g2)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (cg.getStorageUnitID() == JDRUnit.BP)
      {
         g2.drawLine((int)start.x, (int)start.y,
           (int)subPathStart.x, (int)subPathStart.y);
         return;
      }

      double scale = cg.storageToBp(1.0);

      g2.drawLine((int)(scale*start.x), (int)(scale*start.y),
                  (int)(scale*subPathStart.x), (int)(scale*subPathStart.y));

   }

   public void drawSelectedNoControls()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Stroke oldStroke = cg.getStroke();
      Paint oldPaint = cg.getPaint();

      if (subPathStart == null)
      {
         cg.setPaint(start.getSelectedPaint());
         cg.setStroke(guideStroke);

         cg.drawMagLine(start.x, start.y, end.x, end.y);
      }
      else
      {
         cg.setPaint(draftColor);
         cg.drawMagLine(start.x, start.y, subPathStart.x, subPathStart.y);

         cg.setPaint(start.getSelectedPaint());
         cg.setStroke(guideStroke);

         cg.drawMagLine(subPathStart.x, subPathStart.y, end.x, end.y);
      }

      cg.setStroke(oldStroke);
      cg.setPaint(oldPaint);
   }

   public void drawDraft(boolean drawEnd)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (isSelected())
      {
         drawSelectedNoControls();
      }
      else if (subPathStart != null)
      {
         cg.setPaint(draftColor);
         cg.drawMagLine(start.x, start.y, subPathStart.x, subPathStart.y);
      }

      drawControls(drawEnd);
   }

   /**
    * Gets a copy of this segment.
    * @return a copy of this segment
    */
   public Object clone()
   {
      return new JDRClosingMove(this);
   }

   /**
    * Makes this segment the same as another segment.
    * @param seg the other segment
    */
   public void makeEqual(JDRSegment seg)
   {
      super.makeEqual(seg);

      if (seg instanceof JDRClosingMove)
      {
         subPathStart = ((JDRClosingMove)seg).subPathStart;
         path = ((JDRClosingMove)seg).path;
         pathSegmentIndex = ((JDRClosingMove)seg).pathSegmentIndex;
      }
   }

   /**
    * Returns true if this object is the same as another object.
    * @param obj the other object
    */
   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;

      if (!(obj instanceof JDRClosingMove)) return false;

      JDRClosingMove s = (JDRClosingMove)obj;

      return (start.equals(s.start) && end.equals(s.end)
         && subPathStart == s.subPathStart
         && pathSegmentIndex == s.pathSegmentIndex);
   }

   public JDRSegment reverse()
   {
      JDRClosingMove segment = new JDRClosingMove(this);

      segment.start.x = end.x;
      segment.start.y = end.y;
      segment.end.x   = start.x;
      segment.end.y   = start.y;

      // the index and sub path should get updated when the reversed
      // segment is added to a new path

      return segment;
   }


   public void saveSVG(SVG svg, String attr)
      throws IOException
   {
      svg.print("Z ");
      svg.print("M ");
      end.saveSVG(svg, attr);
   }

   public void saveEPS(PrintWriter out) throws IOException
   {
      out.println("closepath");
      end.saveEPS(out);
      out.println("moveto");
   }

   public void savePgf(TeX tex)
     throws IOException
   {
      tex.println("\\pgfpathclose");
      tex.print("\\pgfpathmoveto{");
      end.savePgf(tex);
      tex.println("}");
   }

   /**
    * Appends this segment to the given path. The start
    * point is not included.
    * @param path the path on which to append this segment.
    * This has changed from GeneralPath to Path2D.
    */
   public void appendToGeneralPath(Path2D path)
   {
      path.closePath();
      path.moveTo(end.x, end.y);
   }

   /**
    * Appends the reflection of this segment to the given path.
    * TODO 
    * @param path the path on which to append the reflection of this segment
    * @param line the line of symmetry
    */
   public void appendReflectionToGeneralPath(Path2D path, JDRLine line)
   {
      Point2D p = start.getReflection(line);

      path.closePath();
      path.moveTo(p.getX(), p.getY());
   }

   public JDRObjectLoaderListener getListener()
   {
      return listener;
   }

   /**
    * Gets string representation of this object.
    * @return string representation of this object
    */
   public String toString()
   {
      return String.format("%s:(%f,%f)(%f,%f)(%f%f),startMarker=%s,endMarker=%s",
       getClass().getSimpleName(),
       start, start.y, subPathStart.x, subPathStart.y,
       end.x, end.y, startMarker, endMarker);
   }


   public String info()
   {
      return "closingmove["+start.info()+","+subPathStart.info()+","+end.info()+"]";
   }

   public int getSegmentFlag()
   {
      return SEGMENT_FLAG_CLOSING_MOVE;
   }

   /**
    * The starting point of the sub-path.
    */
   protected JDRPoint subPathStart;
   protected JDRPath path;
   protected int pathSegmentIndex;

   private static JDRClosingMoveLoaderListener listener
      = new JDRClosingMoveLoaderListener();

}
