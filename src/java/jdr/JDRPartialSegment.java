// File          : JDRPartialSegment.java
// Creation Date : 26th July 2010
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
import java.awt.geom.*;
import java.util.*;

import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a partial segment. This is used as a joining
 * segment for symmetric paths where the end point isn't anchored to
 * the line of symmetry.
 * @author Nicola L C Talbot
 * @see JDRPath
 */
public class JDRPartialSegment extends JDRObject 
  implements JDRPathSegment,JDRConstants
{
   /**
    * Creates a new partial segment with null starting point and
    * null line of symmetry.
    */
   public JDRPartialSegment(CanvasGraphics cg)
   {
      super(cg);
      start = null;
   }

   /**
    * Creates a copy of another partial segment. 
    */ 
   public JDRPartialSegment(JDRPartialSegment segment)
   {
      super(segment);
      setStartMarker(segment.startMarker);
      setEndMarker(segment.endMarker);

      if (segment.start != null)
      {
         start = (JDRPoint)segment.start.clone();
      }

      if (segment.line_ != null)
      {
         line_ = new JDRLine(segment.line_);
      }
   }

   /**
    * Creates a new partial segment with given starting point and
    * line of symmetry.
    */
   public JDRPartialSegment(JDRPoint point, JDRLine line)
   {
      this(point == null 
         ? line.getCanvasGraphics() : point.getCanvasGraphics(),
           point, line);
   }

   public JDRPartialSegment(CanvasGraphics cg, JDRPoint point, JDRLine line)
   {
      super(cg);
      start = point;
      line_ = line;
   }

   public void setStart(JDRPoint point)
   {
      start = point;
   }

   public void setStart(Point2D point)
   {
      if (start == null)
      {
         start = new JDRPoint(getCanvasGraphics(), point);
      }
      else
      {
         start.x = point.getX();
         start.y = point.getY();
      }
   }

   public void setEnd(JDRPoint point)
   {
      Point2D p = point.getReflection(line_); 

      if (start == null)
      {
         start = new JDRPoint(getCanvasGraphics(), p);
      }
      else
      {
         start.x = p.getX();
         start.y = p.getY();
      }
   }

   public JDRPathSegment getReflection(JDRLine line)
   {
      return new JDRPartialSegment(getEnd(), line);
   }

   public JDRPathSegment reverse()
   {
     return getReflection(line_);
   }

   public void setSymmetryLine(JDRLine line)
   {
      line_ = line;
   }

   public JDRLine getSymmetryLine()
   {
      return line_;
   }

   public JDRPathSegment split()
   {
      Point2D midP = getP(0.5);

      return new JDRPartialSegment(
         new JDRPoint(getCanvasGraphics(), midP), line_);
   }

   public Object clone()
   {
      return new JDRPartialSegment(this);
   }

   /**
    * Appends this segment to the given path.
    * @param path the path to which this segment must be appended
    */
   public void appendToGeneralPath(Path2D path)
   {
      Point2D p = start.getReflection(line_);

      path.moveTo(p.getX(), p.getY());
   }

   public JDRPathSegment convertToSegment()
   {
      return this;
   }

   public JDRPathSegment convertToLine()
   {
      return new JDRPartialLine(start, line_);
   }

   public JDRPathSegment convertToBezier()
   {
      return new JDRPartialBezier(start, line_);
   }

   public double getStartX()
   {
      return start.x;
   }

   public double getStartY()
   {
      return start.y;
   }

   public JDRPoint getStart()
   {
      return start;
   }

   public JDRPoint getEnd()
   {
      Point2D p = start.getReflection(line_);

      return new JDRPoint(start.getCanvasGraphics(), p);
   }

   public Point2D getEnd2D()
   {
      return start.getReflection(line_);
   }

   public JDRSegment getFullSegment()
   {
      return new JDRSegment(getStart(), getEnd());
   }

   public void savePgf(TeX tex)
     throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      Point2D p = start.getReflection(line_);

      AffineTransform af = tex.getTransform();

      tex.println("\\pgfpathmoveto{"+tex.point(cg, af, p.getX(), p.getY())+"}");
   }

   public double getEndX()
   {
      Point2D p = start.getReflection(line_);

      return p.getX();
   }

   public double getEndY()
   {
      Point2D p = start.getReflection(line_);

      return p.getY();
   }

   public Point2D getP(double t)
   {
      Point2D p = start.getReflection(line_);

      return new Point2D.Double(start.x+(p.getX()-start.x)*t,
                                start.y+(p.getY()-start.y)*t);
   }

   public Point2D getdP(double t)
   {
      return getdP();
   }

   public Point2D getdP()
   {
      Point2D p = getEnd2D();

      p.setLocation(p.getX()-start.getX(),
                    p.getY()-start.getY());

      return p;
   }

   public void flatten()
   {
   }

   public Point2D getdP0()
   {
      return getdP();
   }

   public Point2D getdP1()
   {
      return getdP();
   }

   public void setSelected(boolean flag)
   {
      selected = flag;

      if (flag == false)
      {
         start.selected = false;
      }
   }

   public void setEditedControls(boolean flag)
   {
      start.selected = flag;
   }

   public JDRPoint getEditedControl()
   {
      return start.selected ? start : null;

   }

   public boolean isEdited()
   {
      return false;
   }

   public void drawControls(boolean endPoint)
   {
   }

   public void drawSelectedNoControls()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Point2D p = start.getReflection(line_);

      Stroke oldStroke = cg.getStroke();
      Paint oldPaint = cg.getPaint();

      cg.setPaint(start.getSelectedPaint());
      cg.setStroke(JDRSegment.guideStroke);

      cg.drawMagLine(start.x, start.y, p.getX(), p.getY());

      cg.setStroke(oldStroke);
      cg.setPaint(oldPaint);
   }

   public void drawDraft(boolean drawEnd)
   {
      if (isSelected())
      {
         drawSelectedNoControls();
      }

      drawControls(drawEnd);
   }

   public void draw()
   {
   }

   public void draw(FlowFrame parentFrame)
   {
      draw();
   }

   public void print(Graphics2D g2)
   {
   }

   /**
    * Gets this segment's bounding box including control points.
    */
   public BBox getStorageControlBBox()
   {
      BBox box = start.getStorageControlBBox();

      box.mergeStorageControl(start.getReflection(line_));

      return box;
   }

   public void mergeStorageControlBBox(BBox box)
   {
      start.mergeStorageControlBBox(box);
      box.mergeStorageControl(start.getReflection(line_));
   }

   /**
    * Doesn't transform the starting point. Only transforms any
    * associated control points.
    */
   public void transform(double[] matrix)
   {
   }


   /**
    * Doesn't translate the starting point. Only translates any
    * associated control points.
    */
   public void translate(double x, double y)
   {
   }

   /**
    * Doesn't rotate the starting point. Only rotates any
    * associated control points.
    */
   public void rotate(Point2D p, double angle)
   {
   }

   /**
    * Doesn't rotate the starting point. Only rotates any
    * associated control points.
    */
   public void rotate(double angle)
   {
   }

   /**
    * Doesn't scale the starting point. Only scales any
    * associated control points.
    */
   public void scale(Point2D p, double sx, double sy)
   {
   }

   /**
    * Doesn't scale the starting point. Only scales any
    * associated control points.
    */
   public void scale(double sx, double sy)
   {
   }

   /**
    * Doesn't shear the starting point. Only shears any
    * associated control points.
    */
   public void shear(Point2D p, double sx, double sy)
   {
   }

   /**
    * Doesn't shear the starting point. Only shears any
    * associated control points.
    */
   public void shear(double sx, double sy)
   {
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (!(obj instanceof JDRPartialSegment)) return false;

      JDRPartialSegment seg = (JDRPartialSegment)obj;

      return seg.start.equals(start)
          && line_.equals(seg.getSymmetryLine());
   }

   public void makeEqual(JDRPartialSegment seg)
   {
      super.makeEqual(seg);

      if (seg.start == null)
      {
         start = null;
      }
      else if (start == null)
      {
         start = new JDRPoint(seg.start);
      }
      else
      {
         start = seg.start;
      }

      if (seg.line_ == null)
      {
         line_ = null;
      }
      else if (line_ == null)
      {
         line_ = new JDRLine(seg.line_);
      }
      else
      {
         line_.makeEqual(seg.line_);
      }

      setCanvasGraphics(seg.getCanvasGraphics());
      setStartMarker(seg.getStartMarker());
      setEndMarker(seg.getEndMarker());
   }

   public void saveSVG(SVG svg, String attr) throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      svg.println("M ");
      svg.savePoint(getEnd2D());
   }

   public void saveEPS(PrintWriter out) throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      EPS.saveStoragePoint(cg, out, getEnd2D());
   }

   public JDRMarker getStartMarker()
   {
      return startMarker;
   }

   public JDRMarker getEndMarker()
   {
      return endMarker;
   }

   public void setStartMarker(JDRMarker marker)
   {
      startMarker = marker;
   }

   public void setEndMarker(JDRMarker marker)
   {
      endMarker = marker;
   }

   public int controlCount()
   {
      return 1;
   }

   public JDRPoint getControl(int index)
      throws IndexOutOfBoundsException
   {
      if (index == 0) return start;

      throw new IndexOutOfBoundsException("No control point at index "+index);
   }

   public int getControlIndex(JDRPoint point)
      throws NoSuchElementException
   {
      if (point == start) return 0;

      throw new NoSuchElementException();
   }

   public JDRPoint getControlFromStoragePoint(
     double storagePointX, double storagePointY,
     boolean endPoint)
   {
      if (start.containsStoragePoint(storagePointX, storagePointY))
      {
         return start;
      }

      return null;
   }

   public JDRObjectLoaderListener getListener()
   {
      return listener;
   }

   public String info()
   {
      return "partial segment: start="+start.info()
        +", symmetry="+line_.info();
   }

   public BBox getStorageBBox()
   {
      Point2D endPt = getEnd2D();

      double minX = (start.x < endPt.getX() ? start.x : endPt.getX());
      double minY = (start.y < endPt.getY() ? start.y : endPt.getY());
      double maxX = (start.x > endPt.getX() ? start.x : endPt.getX());
      double maxY = (start.y > endPt.getY() ? start.y : endPt.getY());

      return new BBox(getCanvasGraphics(), minX, minY, maxX, maxY);
   }

   public void fade(double alpha)
   {
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      super.setCanvasGraphics(cg);

      for (int i = 0, n = controlCount(); i < n; i++)
      {
         JDRPoint pt = getControl(i);

         if (pt != null)
         {
            pt.setCanvasGraphics(cg);
         }
      }

      if (line_ != null)
      {
         line_.setCanvasGraphics(cg);
      }

      if (startMarker != null)
      {
         startMarker.setCanvasGraphics(cg);
      }

      if (endMarker != null)
      {
         endMarker.setCanvasGraphics(cg);
      }
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      for (int i = 0, n = controlCount(); i < n; i++)
      {
         getControl(i).applyCanvasGraphics(cg);
      }

      line_.applyCanvasGraphics(cg);

      if (startMarker != null)
      {
         startMarker.applyCanvasGraphics(cg);
      }

      if (endMarker != null)
      {
         endMarker.applyCanvasGraphics(cg);
      }

      super.setCanvasGraphics(cg);
   }

   public boolean isGap() {return true;}

   public String toString()
   {
      return "JDRPartialSegment[start="+start+",symmetry="+line_+"]";
   }

   public int getSegmentFlag()
   {
      return SEGMENT_FLAG_PARTIAL_MOVE;
   }

   private JDRMarker startMarker=null, endMarker=null;

   protected JDRPoint start;
   protected JDRLine line_;

   private static JDRPartialSegmentLoaderListener listener
      = new JDRPartialSegmentLoaderListener();
}
