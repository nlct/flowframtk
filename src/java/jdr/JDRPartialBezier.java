// File          : JDRPartialBezier.java
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

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a partial B&eacute;zier curve.
 * @author Nicola L C Talbot
 * @see JDRPath
 */
public class JDRPartialBezier extends JDRPartialSegment 
{
   /**
    * Creates a new partial B&eacute; with null starting point and
    * null line of symmetry.
    */
   public JDRPartialBezier(CanvasGraphics cg)
   {
      super(cg);
      control = new JDRPoint(cg);
   }

   /**
    * Creates a copy. 
    */ 
   public JDRPartialBezier(JDRPartialBezier curve)
   {
      super(curve);
      control = (JDRPoint)curve.control.clone();
   }

   /**
    * Creates a new partial B&eacute; with given starting point and
    * line of symmetry.
    */
   public JDRPartialBezier(JDRPoint point, JDRLine line)
   {
      super(point, line);
      control = new JDRPoint(getCanvasGraphics());
      flatten();
   }

   public JDRPartialBezier(CanvasGraphics cg, JDRPoint point, JDRLine line)
   {
      super(cg, point, line);
      control = new JDRPoint(getCanvasGraphics());
      flatten();
   }

   /**
    * Creates a new partial B&eacute; with given starting point,
    * control point and line of symmetry.
    */
   public JDRPartialBezier(JDRPoint point, JDRPoint c, JDRLine line)
   {
      this(point == null && line == null ? c.getCanvasGraphics()
        : (point == null ? line.getCanvasGraphics() : point.getCanvasGraphics()),
      point, line);
   }

   public JDRPartialBezier(CanvasGraphics cg, JDRPoint point, JDRPoint c, JDRLine line)
   {
      super(cg, point, line);
      control = c;
   }

   public Object clone()
   {
      return new JDRPartialBezier(this);
   }

   public void flatten()
   {
      Point2D dP = ((JDRPartialSegment)this).getdP();
      setGradient(dP);
   }

   public void setGradient(Point2D dP)
   {
      setGradient(dP.getX(), dP.getY());
   }

   public void setGradient(double dpX, double dpY)
   {
      control.set(dpX/3.0 + start.x, dpY/3.0 + start.y);
   }

   public Point2D getdP0()
   {
      return new Point2D.Double(3*(control.x-start.x),
                                3*(control.y-start.y));
   }

   public Point2D getdP1()
   {
      Point2D c2 = getControl2();
      Point2D p = start.getReflection(line_);

      p.setLocation(3*(p.getX()-c2.getX()),
                    3*(p.getY()-c2.getY()));

      return p;
   }

   public Point2D getP(double t)
   {
      Point2D c2 = getControl2();
      Point2D p = start.getReflection(line_);

      double one_minus_t = (1-t);
      double one_minus_t_sq = one_minus_t*one_minus_t;
      double t_sq = t*t;
      double J0 = one_minus_t_sq*one_minus_t;
      double J1 = 3*t*one_minus_t_sq;
      double J2 = 3*t_sq*one_minus_t;
      double J3 = t_sq*t;

      p.setLocation
      (
         J0*start.x + J1*control.x + J2*c2.getX() + J3*p.getX(),
         J0*start.y + J1*control.y + J2*c2.getY() + J3*p.getY()
      );

      return p;
   }

   public void setEditedControls(boolean flag)
   {
      super.setEditedControls(flag);
      control.selected = flag;
   }

   public JDRPoint getEditedControl()
   {
      if (start.selected) return start;
      if (control.selected) return control;

      return null;
   }


   public void setSelected(boolean flag)
   {
      selected = flag;

      if (flag == false)
      {
         start.selected = false;
         control.selected = false;
      }
   }

   public boolean isSelected()
   {
      return control.selected;
   }

   public JDRPoint getControl1()
   {
      return control;
   }

   public void setControl(double x, double y)
   {
      control.x = x;
      control.y = y;
   }

   public Point2D getControl2()
   {
      return control.getReflection(line_);
   }

   public JDRPathSegment getReflection(JDRLine line)
   {
      return new JDRPartialBezier(getEnd(),
        new JDRPoint(getCanvasGraphics(), getControl2()), line);
   }

   public JDRSegment getFullSegment()
   {
      return new JDRBezier(getStart(), (JDRPoint)control.clone(), 
         new JDRPoint(getCanvasGraphics(), getControl2()), getEnd());
   }

   public void savePgf(TeX tex)
      throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      Point2D end = start.getReflection(line_);
      Point2D c2 = getControl2();

      AffineTransform af = tex.getTransform();

      tex.println("\\pgfpathcurveto{"
        + tex.point(cg, af, control.getX(), control.getY()) + "}{"
        + tex.point(cg, af, c2.getX(), c2.getY()) + "}{"
        + tex.point(cg, af, end.getX(), end.getY())+"}");
   }

   public void saveSVG(SVG svg) throws IOException
   {
      svg.println("C ");
      control.saveSVG(svg);
      svg.savePoint(getControl2());
      svg.savePoint(getEnd2D());
   }

   public void saveEPS(PrintWriter out) throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      control.saveEPS(out);
      EPS.saveStoragePoint(cg, out, getControl2());
      EPS.saveStoragePoint(cg, out, getEnd2D());
      out.println("curveto");
   }

   /**
    * Appends this curve to the given path.
    * @param path the path to which this segment must be appended
    */
   public void appendToGeneralPath(Path2D path)
   {
      Point2D p = start.getReflection(line_);
      Point2D c = control.getReflection(line_);

      path.curveTo(
         control.getX(), control.getY(),
         c.getX(), c.getY(),
         p.getX(), p.getY());
   }

   public JDRPathSegment convertToSegment()
   {
      return new JDRPartialSegment(start, line_);
   }

   public JDRPathSegment convertToLine()
   {
      return new JDRPartialLine(start, line_);
   }

   public JDRPathSegment convertToBezier()
   {
      return this;
   }

   public void drawControls(boolean endPoint)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Point2D p = start.getReflection(line_);
      Point2D c2 = control.getReflection(line_);

      Graphics2D g2 = cg.getGraphics();

      Stroke oldStroke = g2.getStroke();
      Paint oldPaint = g2.getPaint();

      g2.setStroke(JDRSegment.guideStroke);
      g2.setPaint(isSelected() ? start.getSelectedPaint() : draftColor);

      double storageToCompX = cg.storageToComponentX(1.0);
      double storageToCompY = cg.storageToComponentY(1.0);

      Path2D path = new Path2D.Double();

      path.moveTo(storageToCompX*start.x, storageToCompY*start.y);
      path.lineTo(storageToCompX*control.x, storageToCompY*control.y);
      path.lineTo(storageToCompX*c2.getX(), storageToCompY*c2.getY());
      path.lineTo(storageToCompX*p.getX(), storageToCompY*p.getY());
      g2.draw(path);

      g2.setStroke(oldStroke);
      g2.setPaint(oldPaint);

      control.draw();
   }

   public void draw()
   {
      CanvasGraphics cg = getCanvasGraphics();

      double scaleX = cg.storageToComponentX(1.0);
      double scaleY = cg.storageToComponentY(1.0);

      Point2D p = start.getReflection(line_);
      Point2D c = control.getReflection(line_);

      Graphics2D g2 = cg.getGraphics();

      g2.draw(new CubicCurve2D.Double(scaleX*start.x, scaleY*start.y,
         scaleX*control.getX(), scaleY*control.getY(),
         scaleX*c.getX(), scaleY*c.getY(),
         scaleX*p.getX(), scaleY*p.getY()));
   }

   public void drawSelectedNoControls()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();

      Stroke oldStroke = g2.getStroke();
      Paint oldPaint = g2.getPaint();

      g2.setPaint(start.getSelectedPaint());
      g2.setStroke(JDRSegment.guideStroke);

      draw();

      g2.setStroke(oldStroke);
      g2.setPaint(oldPaint);
   }

   public void print(Graphics2D g2)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Point2D p = start.getReflection(line_);
      Point2D c = control.getReflection(line_);

      if (cg.getStorageUnitID() == JDRUnit.BP)
      {
         g2.draw(new CubicCurve2D.Double(start.x, start.y,
            control.getX(), control.getY(),
            c.getX(), c.getY(),
            p.getX(), p.getY()));
         return;
      }

      double scale = cg.storageToBp(1.0);

      g2.draw(new CubicCurve2D.Double(scale*start.x, scale*start.y,
         scale*control.getX(), scale*control.getY(),
         scale*c.getX(), scale*c.getY(),
         scale*p.getX(), scale*p.getY()));
   }

   public void drawDraft(boolean drawEnd)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();

      draw();

      if (isSelected())
      {
         Stroke oldStroke = g2.getStroke();
         g2.setPaint(start.getSelectedPaint());
         g2.setStroke(JDRSegment.guideStroke);

         draw();

         g2.setStroke(oldStroke);
      }

      drawControls(drawEnd);
   }

   public BBox getStorageControlBBox()
   {
      BBox box = super.getStorageControlBBox();

      control.mergeStorageBBox(box);
      box.merge(control.getReflection(line_));

      return box;
   }

   public void mergeStorageControlBBox(BBox box)
   {
      super.mergeStorageControlBBox(box);
      control.mergeStorageBBox(box);
      box.merge(control.getReflection(line_));
   }

   @Override
   public void transform(double[] matrix)
   {
      control.transform(matrix);
   }

   @Override
   public void transform(AffineTransform af)
   {
      control.transform(af);
   }

   public void translate(double x, double y)
   {
      control.translate(x, y);
   }

   public void rotate(Point2D p, double angle)
   {
      control.rotate(p, angle);
   }

   public void rotate(double angle)
   {
      control.rotate(angle);
   }

   public void scale(Point2D p, double sx, double sy)
   {
      control.scale(p, sx, sy);
   }

   public void scale(double sx, double sy)
   {
      control.scale(sx, sy);
   }

   public void shear(Point2D p, double sx, double sy)
   {
      control.shear(p, sx, sy);
   }

   public void shear(double sx, double sy)
   {
      control.shear(sx, sy);
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (!(obj instanceof JDRPartialBezier)) return false;

      JDRPartialBezier seg = (JDRPartialBezier)obj;

      return seg.start.equals(start)
          && seg.control.equals(control)
          && line_.equals(seg.getSymmetryLine());
   }

   public JDRObjectLoaderListener getListener()
   {
      return listener;
   }

   public int controlCount()
   {
      return 2;
   }

   public JDRPoint getControl(int index)
      throws IndexOutOfBoundsException
   {
      if (index == 0) return start;

      if (index == 1) return control;

      throw new IndexOutOfBoundsException("No control point at index "+index);
   }

   public int getControlIndex(JDRPoint point)
      throws NoSuchElementException
   {
      if (point == start) return 0;

      if (point == control) return 1;

      throw new NoSuchElementException();
   }

   public BBox getBBox()
   {
      Point2D endPt = getEnd2D();
      Point2D c2 = getControl2();

      double minX = start.x;
      double minY = start.y;
      double maxX = start.x;
      double maxY = start.y;

      if (minX > control.x)    minX = control.x;
      if (minX > c2.getX())    minX = c2.getX();
      if (minX > endPt.getX()) minX = endPt.getX();

      if (minY > control.y)    minY = control.y;
      if (minY > c2.getY())    minY = c2.getY();
      if (minY > endPt.getY()) minY = endPt.getY();

      if (maxX < control.x)    maxX = control.x;
      if (maxX < c2.getX())    maxX = c2.getX();
      if (maxX < endPt.getX()) maxX = endPt.getX();

      if (maxY < control.y)    maxY = control.y;
      if (maxY < c2.getY())    maxY = c2.getY();
      if (maxY < endPt.getX()) maxY = endPt.getX();

      return new BBox(getCanvasGraphics(), minX, minY, maxX, maxY);
   }

   public boolean isGap() {return false;}

   public boolean isCurve() {return true;}

   public String info()
   {
      return "partial Bezier: start="+start.info()
        +", control="+control.info()
        +", symmetry="+line_.info();
   }

   public String getDetails()
   {
      JDRMessageDictionary msg = getCanvasGraphics().getMessageDictionary();

      String type = msg.getMessageWithFallback(
       "class."+getClass().getCanonicalName(),
       getClass().getSimpleName());
       
      Point2D dp0 = getdP(0.0);
      Point2D dp1 = getdP(1.0);

      Point2D endPt = getEnd2D();

      double angle = JDRLine.getVectorAngle(dp0.getX(), dp0.getY(),
       dp1.getX(), dp1.getY());
      
      return String.format("%s %s",
         msg.getMessageWithFallback(
        "segmentinfo.details.curve",
        "Segment type: {0}; P(0)=({1},{2}), P(1)=({3},{4}); P'(0) = ({5},{6}); P'(1) = ({7},{8}); angle={9}.",
        type, start.x, start.y, endPt.getX(), endPt.getY(), dp0.getX(), dp0.getY(),
        dp1.getX(), dp1.getY(), angle),
        msg.getMessageWithFallback(
        "segmentinfo.details.symmetry",
        "Line of symmetry: ({0},{1})--({2},{3})",
        line_.start.x, line_.start.y, line_.end.x, line_.end.y)
       );
   }

   public String toString()
   {
      return "JDRPartialBezier[start="+start+",control="+control
        +",symmetry="+line_+"]";
   }

   public int getSegmentFlag()
   {
      return SEGMENT_FLAG_PARTIAL_CURVE;
   }

   public void makeEqual(JDRPartialBezier obj)
   {
      super.makeEqual(obj);
      control.makeEqual(obj.control);
   }

   protected JDRPoint control;

   private static JDRPartialBezierLoaderListener listener
      = new JDRPartialBezierLoaderListener();
}
