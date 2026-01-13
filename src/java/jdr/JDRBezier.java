// File          : JDRBezier.java
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
import java.awt.geom.*;
import java.util.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a cubic B&eacute;zier curve. A cubic 
 * B&eacute;zier curve is a parametric curve <b>P</b>(<i>t</i>) 
 * described by four points:
 * the start point <b>P</b>(0) = <b>p</b><sub>0</sub>, 
 * the first curvature control point <b>c</b><sub>1</sub>,
 * the second curvature control point <b>c</b><sub>2</sub> and the end
 * point <b>P</b>(1) = <b>p</b><sub>1</sub>.
 *<p>
 * The parametric form is given by:
 * <br>
 * <b>P</b>(<i>t</i>) = <i>J</i><sub>0</sub> <b>p</b><sub>0</sub>
 * + <i>J</i><sub>1</sub> <b>c</b><sub>1</sub>
 * + <i>J</i><sub>2</sub> <b>c</b><sub>2</sub>
 * + <i>J</i><sub>3</sub> <b>p</b><sub>1</sub>
 * <br>
 * where
 * <br>
 * <i>J</i><sub>0</sub> = (1-<i>t</i>)<sup>3</sup><br>
 * <i>J</i><sub>1</sub> = 3<i>t</i>(1-<i>t</i>)<sup>2</sup><br>
 * <i>J</i><sub>2</sub> = 3<i>t</i><sup>2</sup>(1-<i>t</i>)<br>
 * <i>J</i><sub>3</sub> = <i>t</i><sup>3</sup><br>
 * <p>
 * <center>
 * <table width=60%>
 * <tr align=center><td colspan=2>
 * <img src="images/bezier.png" alt="[annoted image of Bezier curve]">
 * </td></tr>
 * <tr><th valign=top>Figure&nbsp;1</th>
 * <td>A cubic B&eacute;zier curve is described by four points</td></tr>
 * </table>
 * </center>
 * @author Nicola L C Talbot
 */
public class JDRBezier extends JDRSegment
{
   /**
    * Creates a straight line B&eacute;zier segment.
    * The control points are set using {@link #flatten()}.
    * @param p0 the starting point
    * @param p1 the end point
    */
   public JDRBezier(CanvasGraphics cg, Point p0, Point p1)
   {
      super(cg, p0, p1);

      control1 = new JDRPoint(cg);
      control2 = new JDRPoint(cg);

      flatten();
   }

   /**
    * Creates a copy of another curve.
    */ 
   public JDRBezier(JDRBezier curve)
   {
      super(curve);

      control1 = (JDRPoint)curve.control1.clone();
      control2 = (JDRPoint)curve.control2.clone();
   }

   /**
    * Creates a straight line B&eacute;zier segment.
    * The control points are set using {@link #flatten()}.
    * @param p0 the starting point
    * @param p1 the end point
    */
   public JDRBezier(CanvasGraphics cg, Point2D p0, Point2D p1)
   {
      super(cg, p0, p1);

      control1 = new JDRPoint(cg);
      control2 = new JDRPoint(cg);

      flatten();
   }

   /**
    * Creates a straight line B&eacute;zier segment.
    * The control points are set using {@link #flatten()}.
    * @param p0 the starting point
    * @param p1 the end point
    */
   public JDRBezier(JDRPoint p0, JDRPoint p1)
   {
      super(p0, p1);

      CanvasGraphics cg = getCanvasGraphics();

      control1 = new JDRPoint(cg);
      control2 = new JDRPoint(cg);

      flatten();
   }

   /**
    * Creates a B&eacute;zier segment.
    * @param p0 the starting point
    * @param c1 the first control point
    * @param c2 the second control point
    * @param p1 the end point
    */
   public JDRBezier(CanvasGraphics cg, Point p0, Point c1, Point c2, Point p1)
   {
      super(cg, p0, p1);
      control1 = new JDRPoint(cg, c1);
      control2 = new JDRPoint(cg, c2);
   }

   /**
    * Creates a B&eacute;zier segment.
    * @param p0 the starting point
    * @param c1 the first control point
    * @param c2 the second control point
    * @param p1 the end point
    */
   public JDRBezier(CanvasGraphics cg, 
     Point2D p0, Point2D c1, Point2D c2, Point2D p1)
   {
      super(cg, p0, p1);
      control1 = new JDRPoint(cg, c1);
      control2 = new JDRPoint(cg, c2);
   }

   /**
    * Creates a B&eacute;zier segment.
    * @param p0 the starting point
    * @param c1 the first control point
    * @param c2 the second control point
    * @param p1 the end point
    */
   public JDRBezier(JDRPoint p0, JDRPoint c1, JDRPoint c2, JDRPoint p1)
   {
      super(p0, p1);
      control1 = c1;
      control2 = c2;
   }

   /**
    * Creates a B&eacute;zier segment.
    * @param p0x the x co-ordinate of the starting point
    * @param p0y the y co-ordinate of the starting point
    * @param c1x the x co-ordinate of first control point
    * @param c1y the y co-ordinate of first control point
    * @param c2x the x co-ordinate of the second control point
    * @param c2y the y co-ordinate of the second control point
    * @param p1x the x co-ordinate of the end point
    * @param p1y the y co-ordinate of the end point
    */
   public JDRBezier(CanvasGraphics cg, double p0x, double p0y,
                 double c1x, double c1y,
                 double c2x, double c2y,
                 double p1x, double p1y)
   {
      super(new JDRPoint(cg, p0x, p0y), new JDRPoint(cg, p1x, p1y));

      control1 = new JDRPoint(cg, c1x, c1y);
      control2 = new JDRPoint(cg, c2x, c2y);
   }

   /**
    * Shifts the control points of this segment to make it straight.
    */
   public void flatten()
   {
      Point2D dP = ((JDRSegment)this).getdP();
      setGradients(dP, dP);
   }

   /**
    * Reverses the direction of this segment.
    */
   public JDRBezier reverse()
   {
      JDRBezier curve = (JDRBezier)clone();

      curve.start.x = end.x;
      curve.start.y = end.y;
      curve.control1.x = control2.x;
      curve.control1.y = control2.y;
      curve.control2.x = control1.x;
      curve.control2.y = control1.y;
      curve.end.x = start.x;
      curve.end.y = start.y;

      return curve;
   }

   /**
    * Inverts this segment.
    */
   public JDRBezier invert()
   {
      double p1x = 2*start.x-end.x;
      double p1y = 2*start.y-end.y;
      double c2x = 2*start.x-control2.x;
      double c2y = 2*start.y-control2.y;
      double c1x = 2*start.x-control1.x;
      double c1y = 2*start.y-control1.y;

      return new JDRBezier(getCanvasGraphics(),
         start.x,start.y, c1x, c1y, c2x, c2y, p1x, p1y);
   }

   /**
    * Creates a cubic B&eacute;zier segment from a quadratic 
    * B&eacute;zier.
    * @param b0x the x co-ordinate of the starting point
    * @param b0y the y co-ordinate of the starting point
    * @param b1x the x co-ordinate of the quadratic control point
    * @param b1y the y co-ordinate of the quadratic control point
    * @param b2x the x co-ordinate of the end point
    * @param b2y the y co-ordinate of the end point
    * @return the cubic equivalent of the quadratic B&eacute;zier curve
    */
   public static JDRBezier quadToCubic(CanvasGraphics cg,
                             double b0x, double b0y,
                             double b1x, double b1y,
                             double b2x, double b2y)
   {
      Point2D c0 = new Point2D.Double(b0x, b0y);

      Point2D c1 = new Point2D.Double((2*b1x+b0x)/3,(2*b1y+b0y)/3);
      Point2D c2 = new Point2D.Double((b2x+2*b1x)/3,(b2y+2*b1y)/3);

      Point2D c3 = new Point2D.Double(b2x, b2y);

      return new JDRBezier(cg, c0,c1,c2,c3);
   }

   /**
    * Sets the points defining this B&eacute;zier curve.
    * @param p0 the starting point
    * @param c1 the first control point
    * @param c2 the second control point
    * @param p1 the end point
    */
   public void set(Point p0, Point c1, Point c2, Point p1)
   {
      start.x = p0.x;
      start.y = p0.y;
      control1.x = c1.x;
      control1.y = c1.y;
      control2.x = c2.x;
      control2.y = c2.y;
      end.x = p1.x;
      end.y = p1.y;
   }

   /**
    * Sets the points defining this B&eacute;zier curve.
    * @param p0 the starting point
    * @param c1 the first control point
    * @param c2 the second control point
    * @param p1 the end point
    */
   public void set(Point2D p0, Point2D c1, Point2D c2, Point2D p1)
   {
      start.x = p0.getX();
      start.y = p0.getY();
      control1.x = c1.getX();
      control1.y = c1.getY();
      control2.x = c2.getX();
      control2.y = c2.getY();
      end.x = p1.getX();
      end.y = p1.getY();
   }

   /**
    * Sets the start and end gradients of this segment.
    * Moves the control points of this segment so that gradients at the
    * start and end points are as specified.
    * @param dP0 the required gradient at the start of the curve
    * @param dP1 the required gradient at the end of the curve
    */
   public void setGradients(Point2D dP0, Point2D dP1)
   {
      control1.set(dP0.getX()/3+start.x,
                   dP0.getY()/3+start.y);
      control2.set(end.x-dP1.getX()/3,
                   end.y-dP1.getY()/3);
   }

   /**
    * Sets the start gradient of this segment.
    * Moves the control points of this segment so that gradient at the
    * start point is as specified.
    * @param dP0 the required gradient at the start of the curve
    */
   public void setStartGradient(JDRPoint dP0)
   {
      setStartGradient(dP0.x, dP0.y);
   }

   /**
    * Sets the start gradient of this segment.
    * Moves the control points of this segment so that gradient at the
    * start point is as specified.
    * @param dP0 the required gradient at the start of the curve
    */
   public void setStartGradient(Point2D dP0)
   {
      setStartGradient(dP0.getX(), dP0.getY());
   }

   /**
    * Sets the start gradient of this segment.
    * Moves the control points of this segment so that gradient at the
    * start point is as specified.
    * @param dP0x the x co-ordinate of the required gradient
    * @param dP0y the y co-ordinate of the required gradient
    */
   public void setStartGradient(double dP0x, double dP0y)
   {
      control1.x = dP0x/3+start.x;
      control1.y = dP0y/3+start.y;
   }

   /**
    * Sets the end gradient of this segment.
    * Moves the control points of this segment so that gradient at the
    * end point is as specified.
    * @param dP1 the required gradient at the end of the curve
    */
   public void setEndGradient(JDRPoint dP1)
   {
      setEndGradient(dP1.x, dP1.y);
   }

   /**
    * Sets the end gradient of this segment.
    * Moves the control points of this segment so that gradient at the
    * end point is as specified.
    * @param dP1 the required gradient at the end of the curve
    */
   public void setEndGradient(Point2D dP1)
   {
      setEndGradient(dP1.getX(), dP1.getY());
   }

   /**
    * Sets the end gradient of this segment.
    * Moves the control points of this segment so that gradient at the
    * end point is as specified.
    * @param dP1x the x co-ordinate of the required gradient
    * @param dP1y the y co-ordinate of the required gradient
    */
   public void setEndGradient(double dP1x, double dP1y)
   {
      control2.x = end.x-dP1x/3;
      control2.y = end.y-dP1y/3;
   }

   /**
    * Gets the gradient at the start of this segment.
    * @return the gradient at t=0
    * @see #getdP1()
    */
   public Point2D getdP0()
   {
      return new Point2D.Double(3*(control1.x-start.x), 
                                3*(control1.y-start.y));
   }

   /**
    * Gets the gradient at the end of this segment.
    * @return the gradient at t=1
    * @see #getdP0()
    */
   public Point2D getdP1()
   {
      return new Point2D.Double(3*(end.x-control2.x), 
                                3*(end.y-control2.y));
   }

   /**
    * Gets the point at t along this segment.
    * @return the required point
    */
   public Point2D getP(double t)
   {
      return getP(t, start.x, start.y, control1.x, control1.y,
       control2.x, control2.y, end.x, end.y);
   }

   public static Point2D getP(double t, CubicCurve2D curve)
   {
      return getP(t, curve.getX1(), curve.getY1(),
       curve.getCtrlX1(), curve.getCtrlY1(),
       curve.getCtrlX2(), curve.getCtrlY2(),
       curve.getX2(), curve.getY2());
   }

   public static Point2D getP(double t,
     double p0x, double p0y, double p1x, double p1y,
     double p2x, double p2y, double p3x, double p3y)
   {
      double one_minus_t = (1-t);
      double one_minus_t_sq = one_minus_t*one_minus_t;
      double t_sq = t*t;
      double J0 = one_minus_t_sq*one_minus_t;
      double J1 = 3*t*one_minus_t_sq;
      double J2 = 3*t_sq*one_minus_t;
      double J3 = t_sq*t;

      double x = J0*p0x + J1*p1x + J2*p2x + J3*p3x;
      double y = J0*p0y + J1*p1y + J2*p2y + J3*p3y;

      return new Point2D.Double(x, y);
   }

   /**
    * Gets the gradient at t for this segment.
    * @return the required gradient
    */
   public Point2D getdP(double t)
   {
      return getdP(t, start.x, start.y, control1.x, control1.y,
       control2.x, control2.y, end.x, end.y);
   }

   public static Point2D getdP(double t,
     double p0x, double p0y, double p1x, double p1y,
     double p2x, double p2y, double p3x, double p3y)
   {
      double dJ0 = - 3*(1-t)*(1-t);
      double dJ1 = 3*(1-t)*(1-3*t);
      double dJ2 = 3*t*(2-3*t);
      double dJ3 = 3*t*t;

      double x = dJ0*p0x+dJ1*p1x+dJ2*p2x+dJ3*p3x;
      double y = dJ0*p0y+dJ1*p1y+dJ2*p2y+dJ3*p3y;

      return new Point2D.Double(x,y);
   }

   /**
    * Gets the value of t for which p(t) is the closest point to 
    * (x0,y0) using t0 as starting value.
    * @param t0 starting value of t
    * @param x0 x co-ordinate of target point
    * @param y0 y co-ordinate of target point
    * @return value of t for which p(t) is closest to (x0,y0)
    */
   public double getT(double t0, double x0, double y0)
   {
      double t = t0;
      double oldE = 100000;

      for (int k = 0; k < 100; k++)
      {
         Point2D pt = getP(t);

         double r0 = pt.getX() - x0;
         double r1 = pt.getY() - y0;

         double E = r0*r0 + r1*r1;

         if (Math.abs(E-oldE) < 1e-6) break;

         Point2D dP = getdP(t);

         double g = 2*dP.getX()*r0 + 2*dP.getY()*r1;
         double h = 2*(dP.getX()*dP.getX() + dP.getY()*dP.getY());

         double deltaT = -g/h;

         t += deltaT;
      }

      return t;
   }

   /**
    * Gets the value of t for which p(t) is the closest point to 
    * (x0,y0) and p'(t) is closest to the given gradient.
    * @param x x co-ordinate of target point
    * @param y y co-ordinate of target point
    * @param dx x co-ordinate of target gradient
    * @param dy y co-ordinate of target gradient
    * @param tinc increment of t
    * @return value of t for which p(t) is closest to (x,y) and
    * p'(t) is closest to (dx,dy)
    */
   public double getT(double x, double y, double dx, double dy, double tinc)
   {
      double tval = 0;

      double oldDiffP = Double.MAX_VALUE;
      double oldDiffG = Double.MAX_VALUE;

      for (double t = 0; t < 1.0; t += tinc)
      {
         Point2D pt = getP(t);

         double r0 = pt.getX() - x;
         double r1 = pt.getY() - y;

         double diffP = r0*r0 + r1*r1;

         Point2D dp = getdP(t);

         r0 = dp.getX() - dx;
         r1 = dp.getY() - dy;

         double diffG = r0*r0 + r1*r1;

         if ((diffP < oldDiffP) && (diffG < oldDiffG))
         {
            oldDiffP = diffP;
            oldDiffG = diffG;
            tval = t;
         }
      }

      return tval;
   }

   /**
    * Transforms this curve. The transformation matrix should be
    * stored as the flat matrix [m00, m10, m01, m11, m02, m12].
    * @param matrix the transformation matrix
    */
   public void transform(double[] matrix)
   {
      start.transform(matrix);
      end.transform(matrix);
      control1.transform(matrix);
      control2.transform(matrix);
   }

   /**
    * Transforms this curve. 
    * @param af the affine transformation
    */
   public void transform(AffineTransform af)
   {
      start.transform(af);
      end.transform(af);
      control1.transform(af);
      control2.transform(af);
   }

   /**
    * Translates this curve. Translates the start, end and control
    * points defining this curve by (x,y).
    * @param x the x-shift
    * @param y the y-shift
    */
   public void translate(double x, double y)
   {
      start.translate(x,y);
      end.translate(x,y);
      control1.translate(x,y);
      control2.translate(x,y);
   }

   public void translate(double x, double y, boolean endPoint)
   {
      start.translate(x,y);
      control1.translate(x,y);
      control2.translate(x,y);
      if (endPoint) end.translate(x,y);
   }


   /**
    * Scales this curve.
    * @param factorX the x scale factor
    * @param factorY the y scale factor
    */
   public void scale(double factorX, double factorY)
   {
      start.scale(factorX, factorY);
      end.scale(factorX, factorY);
      control1.scale(factorX, factorY);
      control2.scale(factorX, factorY);
   }

   /**
    * Scales this curve about a given point. The end point will 
    * only be scaled if endPoint is true.
    * @param endPoint if true scale end point as well
    */
   public void scale(Point2D p, double factorX, double factorY,
                     boolean endPoint)
   {
      start.scale(p, factorX, factorY);
      control1.scale(p, factorX, factorY);
      control2.scale(p, factorX, factorY);
      if (endPoint) end.scale(p, factorX, factorY);
   }

   /**
    * Scales this curve along the x-axis.
    * @param factor the x scale factor
    */
   public void scaleX(double factor)
   {
      start.scaleX(factor);
      end.scaleX(factor);
      control1.scaleX(factor);
      control2.scaleX(factor);
   }

   /**
    * Scales this curve along the y-axis.
    * @param factor the y scale factor
    */
   public void scaleY(double factor)
   {
      start.scaleY(factor);
      end.scaleY(factor);
      control1.scaleY(factor);
      control2.scaleY(factor);
   }

   /**
    * Shears this curve.
    * @param factorX the x shear factor
    * @param factorY the y shear factor
    */
   public void shear(double factorX, double factorY)
   {
      start.shear(factorX, factorY);
      end.shear(factorX, factorY);
      control1.shear(factorX, factorY);
      control2.shear(factorX, factorY);
   }

   /**
    * Shears curve about a given point. The end point will only 
    * be sheared if endPoint is true.
    * @param endPoint if true shear end point as well
    */
   public void shear(Point2D p,
                     double factorX, double factorY,
                     boolean endPoint)
   {
      start.shear(p, factorX, factorY);
      control1.shear(p, factorX, factorY);
      control2.shear(p, factorX, factorY);
      if (endPoint) end.shear(p, factorX, factorY);
   }

   /**
    * Rotates this curve.
    * @param angle the angle of rotation
    */
   public void rotate(double angle)
   {
      start.rotate(angle);
      end.rotate(angle);
      control1.rotate(angle);
      control2.rotate(angle);
   }

   /**
    * Rotates curve about a given point. The end point will only 
    * be rotated if endPoint is true.
    * @param p the point about which to rotate
    * @param angle the angle of rotation
    * @param endPoint if true rotate end point as well
    */
   public void rotate(Point2D p, double angle,
                     boolean endPoint)
   {
      start.rotate(p, angle);
      control1.rotate(p, angle);
      control2.rotate(p, angle);
      if (endPoint) end.rotate(p, angle);
   }

   /**
    * Draws the points defining this curve.
    * @param endPoint determines whether the end point should be drawn
    */
   public void drawControls(boolean endPoint)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Stroke oldStroke = cg.getStroke();
      Paint oldPaint = cg.getPaint();

      cg.setStroke(guideStroke);
      cg.setPaint(isSelected() ? start.getSelectedPaint() : draftColor);

      double storageToCompX = cg.storageToComponentX(1.0);
      double storageToCompY = cg.storageToComponentY(1.0);

      Path2D guides = new Path2D.Double();

      guides.moveTo(storageToCompX*start.getX(), storageToCompY*start.getY());
      guides.lineTo(storageToCompX*control1.getX(),
                    storageToCompY*control1.getY());
      guides.lineTo(storageToCompX*control2.getX(),
                    storageToCompY*control2.getY());
      guides.lineTo(storageToCompX*end.getX(), storageToCompY*end.getY());

      cg.draw(guides);

      cg.setStroke(oldStroke);
      cg.setPaint(oldPaint);

      start.draw();
      control1.draw();
      control2.draw();
      if (endPoint) end.draw();
   }

   /**
    * Draws this curve.
    */
   public void draw()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();

      double storageToCompX = cg.storageToComponentX(1.0);
      double storageToCompY = cg.storageToComponentY(1.0);

      g2.draw(new CubicCurve2D.Double(
               storageToCompX*start.getX(), storageToCompY*start.getY(),
               storageToCompX*control1.getX(), storageToCompY*control1.getY(),
               storageToCompX*control2.getX(), storageToCompY*control2.getY(),
               storageToCompX*end.getX(), storageToCompY*end.getY()));
   }

   public void print(Graphics2D g2)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (cg.getStorageUnitID() == JDRUnit.BP)
      {
         g2.draw(new CubicCurve2D.Double(start.x, start.y,
                                         control1.x, control1.y,
                                         control2.x, control2.y,
                                         end.x, end.y));
         return;
      }

      double scale = cg.storageToBp(1.0);

      g2.draw(new CubicCurve2D.Double(scale*start.x, scale*start.y,
                                      scale*control1.x, scale*control1.y,
                                      scale*control2.x, scale*control2.y,
                                      scale*end.x, scale*end.y));
   }

   public void drawSelectedNoControls()
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();
      Stroke oldStroke = g2.getStroke();
      Paint oldPaint = g2.getPaint();

      g2.setPaint(start.getSelectedPaint());
      g2.setStroke(guideStroke);

      draw();

      g2.setPaint(oldPaint);
      g2.setStroke(oldStroke);
   }

   public void drawDraft(boolean drawEnd)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();
      Stroke oldStroke = g2.getStroke();

      g2.setPaint(JDRObject.draftColor);

      draw();

      double scaleX = cg.storageToComponentX(1.0);
      double scaleY = cg.storageToComponentY(1.0);

      // Draw guides
      
      Path2D path = new Path2D.Double();
      path.moveTo(scaleX*start.x, scaleY*start.y);
      path.lineTo(scaleX*control1.x, scaleY*control1.y);
      path.lineTo(scaleX*control2.x, scaleY*control2.y);
      path.lineTo(scaleX*end.x, scaleY*end.y);

      if (isSelected())
      {
         g2.draw(path);

         g2.setPaint(start.getSelectedPaint());
         g2.setStroke(guideStroke);

         draw();
         g2.draw(path);
      }
      else
      {
         g2.setStroke(guideStroke);
         g2.draw(path);
      }

      g2.setStroke(oldStroke);

      drawControls(drawEnd);
   }

   /**
    * Makes adjacent curves continous. 
    * This assumes the end point of first segment coincides with start
    * point of second segment.
    * @param first the first B&eacute;zier curve
    * @param second the second B&eacute;zier curve
   */
   public static void makeContinuous(JDRBezier first, JDRBezier second)
   {
     // second.control1.x = 2*first.end.x - first.control2.x;
     // second.control1.y = 2*first.end.y - first.control2.y;
     first.control2.x = 2*first.end.x - second.control1.x;
     first.control2.y = 2*first.end.y - second.control1.y;
   }

   /**
    * Creates a new B&eacute;zier curve following on from given 
    * segment.
    * The given segment is not necessarily a B&eacute;zier curve.
    * @param previous the given segment
    * @param endP the end point of the new B&eacute;zier curve
    */
   public static JDRBezier constructBezier(JDRSegment previous, Point endP)
   {
      return constructBezier(previous,
                             new Point2D.Double(endP.x,endP.y));
   }

   /**
    * Creates a new B&eacute;zier curve following on from given 
    * segment.
    * The given segment is not necessarily a B&eacute;zier curve.
    * @param previous the given segment
    * @param endP the end point of the new B&eacute;zier curve
    */
   public static JDRBezier constructBezier(JDRSegment previous, Point2D endP)
   {
      JDRBezier curve = new JDRBezier(previous.end, 
         new JDRPoint(previous.getCanvasGraphics(), endP));

      if (!(previous instanceof JDRBezier))
      {
         Point2D dP = previous.getdP();
         curve.control1 = new JDRPoint(previous.getCanvasGraphics(),
            dP.getX()/3+curve.start.x, 
            dP.getY()/3+curve.start.y);
         return curve;
      }

      JDRBezier prevCurve = (JDRBezier)previous;

      double c0x = prevCurve.start.x;
      double c0y = prevCurve.start.y;
      double c3x = prevCurve.end.x;
      double c3y = prevCurve.end.y;
      double d3x = endP.getX();
      double d3y = endP.getY();

      double b0x = (c0x-c3x)/3;
      double b0y = (c0y-c3y)/3;
      double b1x = (d3x+5*c3x)/3;
      double b1y = (d3y+5*c3y)/3;
      double b2x = -4*c3x;
      double b2y = -4*c3y;

      double d2x = (3*b1x-b0x+b2x)/2;
      double d2y = (3*b1y-b0y+b2y)/2;

      double c2x = b1x-d2x;
      double c2y = b1y-d2y;

      double c1x = b0x+c2x;
      double c1y = b0y+c2y;

      double d1x = 2*c3x-c2x;
      double d1y = 2*c3y-c2y;

      prevCurve.control1.x = c1x;
      prevCurve.control1.y = c1y;

      prevCurve.control2.x = c2x;
      prevCurve.control2.y = c2y;

      curve.control1.x = d1x;
      curve.control1.y = d1y;

      curve.control2.x = d2x;
      curve.control2.y = d2y;

      return curve;
   }

   public JDRPathSegment split()
   {
      CanvasGraphics cg = getCanvasGraphics();

      double b0x = start.x;
      double b0y = start.y;
      double b1x = control1.x;
      double b1y = control1.y;
      double b2x = control2.x;
      double b2y = control2.y;
      double b3x = end.x;
      double b3y = end.y;

      JDRPoint midPt = new JDRPoint(cg, (b3x+3*b2x+3*b1x+b0x)/8,
                                   (b3y+3*b2y+3*b1y+b0y)/8);

      JDRPoint c1 = new JDRPoint(cg, 0.5*(b1x+b0x), 0.5*(b1y+b0y));
      JDRPoint c2 = new JDRPoint(cg, 0.25*(b2x+2*b1x+b0x),
                                 0.25*(b2y+2*b1y+b0y));

      JDRPoint d1 = new JDRPoint(cg, 0.25*(b3x+2*b2x+b1x),
                                 0.25*(b3y+2*b2y+b1y));
      JDRPoint d2 = new JDRPoint(cg, 0.5*(b3x+b2x),
                                 0.5*(b3y+b2y));

      JDRBezier newSegment = new JDRBezier(midPt, d1, d2, end);

      control1 = c1;
      control2 = c2;
      end = midPt;

      return newSegment;
   }

   /**
    * Creates a copy of this object.
    * @return copy of this object
    */
   public Object clone()
   {
      JDRBezier curve = new JDRBezier((JDRPoint)start.clone(), 
                                (JDRPoint)control1.clone(), 
                                (JDRPoint)control2.clone(), 
                                (JDRPoint)end.clone());
      return curve;
   }

   /**
    * Makes this curve equal to another curve.
    * @param curve the other curve
    */
   public void makeEqual(JDRBezier curve)
   {
      super.makeEqual((JDRSegment)curve);
      control1.makeEqual(curve.control1);
      control2.makeEqual(curve.control2);
   }

   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;

      if (!(obj instanceof JDRSegment)) return false;

      JDRBezier c = (JDRBezier)obj;

      // super.equals(obj) has already checked start and end

      return (control1.equals(c.control1)
           && control2.equals(c.control2));
   }

   /**
    * Sets the flag indicating whether the control points are 
    * being edited.
    * @param flag indicates whether control points are being edited
    */
   public void setEditedControls(boolean flag)
   {
      super.setEditedControls(flag);
      control1.selected = flag;
      control2.selected = flag;
   }

   /**
    * Returns control point that is currently being edited. If no
    * control points are being edited, null is return.
    * @return edited control point or null
    */
   public JDRPoint getEditedControl()
   {
      if (start.selected) return start;
      if (control1.selected) return control1;
      if (control2.selected) return control2;
      if (end.selected) return end;
      return null;
   }

   /**
    * Gets the control point in which given point is inside.
    * If more than one control point contains p, the control point
    * is chosen in the following order: start point, first 
    * curvature control, second curvature control. 
    * The dimensions of the control points are given
    * by {@link JDRPoint#pointSize}.
    * @param p the given point
    * @param endPoint if true include the end point in the search
    * otherwise don't check the end point.
    * @return the control point that contains the given point or
    * <code>null</code> if none of the control points contain that point
    */
   public JDRPoint getControlStorage(
      double storageX, double storageY, boolean endPoint)
   {
      if (control2.containsStoragePoint(storageX, storageY))
      {
         return control2;
      }

      if (control1.containsStoragePoint(storageX, storageY))
      {
         return control1;
      }

      if (start.containsStoragePoint(storageX, storageY))
      {
         return start;
      }

      if (endPoint && end.containsStoragePoint(storageX, storageY))
      {
         return end;
      }

      return null;
   }

   /**
    * Gets the first curvature control point.
    * @return the first curvature control point
    * @see #setControl1(double,double)
    * @see #getControl2()
    */
   public JDRPoint getControl1()
   {
      return control1;
   }

   /**
    * Gets the second curvature control point.
    * @return the second curvature control point
    * @see #setControl2(double,double)
    * @see #getControl1()
    */
   public JDRPoint getControl2()
   {
      return control2;
   }

   /**
    * Sets the first curvature control point.
    * @param newX x co-ordinate
    * @param newY y co-ordinate
    * @see #getControl1()
    * @see #setControl2(double,double)
    */
   public void setControl1(double newX, double newY)
   {
      control1.x = newX;
      control1.y = newY;
   }

   /**
    * Sets the second curvature control point.
    * @param newX x co-ordinate
    * @param newY y co-ordinate
    * @see #getControl2()
    * @see #setControl1(double,double)
    */
   public void setControl2(double newX, double newY)
   {
      control2.x = newX;
      control2.y = newY;
   }

   /**
    * Sets the flag determining whether this curve is being edited.
    * @param flag indicates whether this curve is being edited
    */
   public void setSelected(boolean flag)
   {
      selected = flag;
      if (flag == false)
      {
         start.selected = false;
         control1.selected = false;
         control2.selected = false;
         end.selected = false;
      }
   }

   public void saveSVG(SVG svg, String attr)
      throws IOException
   {
      svg.print("C ");
      control1.saveSVG(svg, attr);
      control2.saveSVG(svg, attr);
      end.saveSVG(svg, attr);
   }

   public void saveEPS(PrintWriter out) throws IOException
   {
      control1.saveEPS(out);
      control2.saveEPS(out);
      end.saveEPS(out);
      out.println("curveto");
   }

   public void savePgf(TeX tex)
     throws IOException
   {
      tex.print("\\pgfpathcurveto{");
      control1.savePgf(tex);
      tex.print("}{");
      control2.savePgf(tex);
      tex.print("}{");
      end.savePgf(tex);
      tex.println("}");
   }

   public void appendToGeneralPath(Path2D path)
   {
      path.curveTo(control1.x, control1.y,
                   control2.x, control2.y,
                   end.x, end.y);
   }

   public void appendReflectionToGeneralPath(Path2D path, JDRLine line)
   {
      Point2D c1 = control1.getReflection(line);
      Point2D c2 = control2.getReflection(line);
      Point2D p  = start.getReflection(line);

      path.curveTo(c2.getX(), c2.getY(),
                   c1.getX(), c1.getY(),
                   p.getX(), p.getY());
   }

   public JDRPathSegment getReflection(JDRLine line)
   {
      Point2D p1 = start.getReflection(line);
      Point2D c1 = control1.getReflection(line);
      Point2D c2 = control2.getReflection(line);
      Point2D p2 = end.getReflection(line);

      return new JDRBezier(getCanvasGraphics(), p1, c1, c2, p2);
   }

   /**
    * Gets string representation of this object.
    */
   public String toString()
   {
      return "JDRBezier:("+start.x+","+start.y+")("
          +control1.x+","+control1.y+")("
          +control2.x+","+control2.y+")("
          +end.x+","+end.y+"),startMarker="+startMarker+",endMarker="+endMarker;
   }

   public JDRObjectLoaderListener getListener()
   {
      return listener;
   }

   public String info()
   {
      return "bezier["+start.info()
      +","+control1+","+control2+","+end.info()+"]";
   }

   public String getDetails()
   {
      JDRMessageDictionary msg = getCanvasGraphics().getMessageDictionary();

      String type = msg.getMessageWithFallback("class."+getClass().getCanonicalName(),
       getClass().getSimpleName());

      Point2D dp0 = getdP0();
      Point2D dp1 = getdP1();

      double angle = JDRLine.getVectorAngle(dp0.getX(), dp0.getY(),
       dp1.getX(), dp1.getY());

      String segmentDetails = msg.getMessageWithFallback(
        "segmentinfo.details.curve",
        "Segment type: {0}; P(0)=({1},{2}), P(1)=({3},{4}); P''(0) = ({5},{6}); P''(1) = ({7},{8}); angle between P''(0) and P''(1): {9} rad ({10} degrees).",
        type, start.x, start.y, end.x, end.y, dp0.getX(), dp0.getY(),
        dp1.getX(), dp1.getY(), angle, Math.toDegrees(angle)
       );

      String stationaryInfo = "";

      double[] t = getStationaryPositions();

      if (t == null)
      {
         stationaryInfo = msg.getMessageWithFallback(
           "segmentinfo.details.bezier.stationary_none", 
           "No stationary points found");
      }
      else if (t.length == 1)
      {
         Point2D p = getP(t[0]);

         stationaryInfo = msg.getMessageWithFallback(
           "segmentinfo.details.bezier.stationary_1",
           "1 stationary point found: t={0}, P(t)=({1},{2}).",
           t[0], p.getX(), p.getY());
      }
      else if (t.length == 2)
      {
         Point2D p1 = getP(t[0]);
         Point2D p2 = getP(t[1]);

         stationaryInfo = msg.getMessageWithFallback(
           "segmentinfo.details.bezier.stationary_2",
           "2 stationary points found: t={0}, P(t)=({1},{2}) and t={3}, P(t)=({4},{5}).",
           t[0], p1.getX(), p1.getY(), t[1], p2.getX(), p2.getY());
      }

      return String.format("%s %s", segmentDetails, stationaryInfo);
   }

   /*
    Returns an array of t values corresponding to stationary points or
    null if none found. The curve has the parametric equation:
\[
P(t) = (1-t^3)p_0 + 3(1-t^2)tp_1 + 3(1-t)t^2p_2 + t^3p_3
\]
The derivative is
\[
P'(t) = 3(1-t)^2(p_1-p_0) + 6(1-t)t(p_2-p_1)+3t^2(p_3-p_2)
\]
Stationary points occur when $\frac{dy}{dx}=0$. In the case of a
parametric function $\frac{dy}{dx}=\frac{\frac{dy}{dt}}{\frac{dx}{dt}}$
where $\frac{dx}{dt}=P^x'(t)$ and $\frac{dy}{dt}=P^y'(t)$.
This means that $P^y'(t) = 0$ and $P^x'(t) \ne 0$.
\[
P^y'(t) = t^2[3(p_1^y-p_2^y)+p_3^y-p_0^y] 
+ 2t[p_0^y+p_2^y-2p_1^y]
+ p_1^y - p_0^y 
\]
 
Note that t may be outside the range [0-1] which means that the 
stationary point is outside of this segment. Use limit=true to only return possible values of t within that range
    */ 
   public static double[] getStationaryPositions(
     double p0x, double p0y, double p1x, double p1y,
     double p2x, double p2y, double p3x, double p3y)
   {
      return getStationaryPositions(p0x, p0y, p1x, p1y,
       p2x, p2y, p3x, p3y, false);
   }

   public static double[] getStationaryPositions(
     double p0x, double p0y, double p1x, double p1y,
     double p2x, double p2y, double p3x, double p3y,
     boolean limit)
   {
      double a = 3*(p1y-p2y) + p3y - p0y;
      double b = 2*(p0y+p2y-2*p1y);
      double c = p1y - p0y;

      double sq = b*b - 4*a*c;

      if (sq < 0)
      {
         return null;
      }

      double factor = 1.0/(2.0*a);

      if (Double.isNaN(factor))
      {
         return null;
      }

      if (sq == 0)
      {
         double t = - b * factor;

         if (Double.isNaN(t) || limit && (t < 0.0 || t > 1.0))
         {
            return null;
         }

         return new double[] { t };
      }

      double root = Math.sqrt(sq);

      double t1 = factor*(root - b);
      double t2 = -factor*(b + root);

      if (Double.isNaN(t1) || Double.isInfinite(t1)
             || (limit && (t1 < 0.0 || t1 > 1.0)))
      {
         if (Double.isNaN(t2) || Double.isInfinite(t2)
             || (limit && (t2 < 0.0 || t2 > 1.0)))
         {
            return null;
         }

         return new double[] {t2};
      }
      else if (Double.isNaN(t2) || Double.isInfinite(t2)
             || (limit && (t2 < 0.0 || t2 > 1.0)))
      {
         return new double[] {t1};
      }

      if (t1 < t2)
      {
         return new double[] {t1, t2};
      }
      
      return new double[] {t2, t1};
   }

   public static double[] getStationaryPositions(CubicCurve2D curve)
   {
      return getStationaryPositions(curve.getX1(), curve.getY1(),
        curve.getCtrlX1(), curve.getCtrlY1(), 
        curve.getCtrlX2(), curve.getCtrlY2(), 
        curve.getX2(), curve.getY2());
   }

   public static double[] getStationaryPositions(CubicCurve2D curve,
     boolean limit)
   {
      return getStationaryPositions(curve.getX1(), curve.getY1(),
        curve.getCtrlX1(), curve.getCtrlY1(), 
        curve.getCtrlX2(), curve.getCtrlY2(), 
        curve.getX2(), curve.getY2(), limit);
   }

   public double[] getStationaryPositions()
   {
      return getStationaryPositions(start.x, start.y,
        control1.x, control1.y, control2.x, control2.y, end.x, end.y);
   }

   public double[] getStationaryPositions(boolean limit)
   {
      return getStationaryPositions(start.x, start.y,
        control1.x, control1.y, control2.x, control2.y, end.x, end.y, limit);
   }

   /**
    *  Gets approximate closest point. The actual solution to this
    *  requires solving a 5th order polynomial, so this method
    *  simply iterates over sample points.
    *  @param q the point near the curve
    *  @param p0 curve start point P(0)
    *  @param c1 first control
    *  @param c2 second control
    *  @param p1 curve end point P(1)
    *  @param numDivisions
    *  @param resultP used to store the corresponding P(t)
    *  @param result if not null used to store [t, square_distance]
    *  @return either result parameter, if not null, or new
    *  array [t, square_distance]
    */ 
   public static double[] getClosest(Point2D q, 
     Point2D p0, Point2D c1, Point2D c2, Point2D p1, int numDivisions,
     Point2D resultP, double[] result)
   {
      if (numDivisions < 0)
      {
         throw new IllegalArgumentException(
           "Invalid number of divisions "+numDivisions);
      }

      int prevBestIdx = 0;
      double prevBestT = 0.0;
      Point2D prevBestP = p0;
      double prevBest = Point2D.distanceSq(p0.getX(), p0.getY(),
        q.getX(), q.getY());

      int bestIdx = 0;
      double bestT = 0.0;
      Point2D bestP = p0; 
      double best = prevBest;

      double t = 0.0;
      double inc = 1.0/(numDivisions+1);

      for (int i = 1; i <= numDivisions; i++)
      {
         t += inc;

         Point2D pt = getP(t, p0.getX(), p0.getY(), c1.getX(), c1.getY(),
           c2.getX(), c2.getY(), p1.getX(), p1.getY());

         double d = Point2D.distanceSq(pt.getX(), pt.getY(),
           q.getX(), q.getY());

         if (d < best)
         {
            prevBestIdx = bestIdx;
            prevBestP = bestP;
            prevBest = best;
            prevBestT = bestT;

            bestIdx = i;
            bestP = pt;
            best = d;
            bestT = t;
         }
      } 

      t = 1.0;

      double d = Point2D.distanceSq(p1.getX(), p1.getY(),
        q.getX(), q.getY());

      if (d < best)
      {
         prevBestIdx = bestIdx;
         prevBestP = bestP;
         prevBest = best;
         prevBestT = bestT;

         bestIdx = numDivisions+1;
         bestP = p1;
         best = d;
         bestT = t;
      }

      if (bestIdx - prevBestIdx == 1)
      {
         t = prevBestT + 0.5*inc;

         Point2D pt = getP(t, p0.getX(), p0.getY(), c1.getX(), c1.getY(),
           c2.getX(), c2.getY(), p1.getX(), p1.getY());

         d = Point2D.distanceSq(pt.getX(), pt.getY(),
           q.getX(), q.getY());

         if (d < best)
         {
            bestP = pt;
            best = d;
            bestT = t;
         }
      }

      resultP.setLocation(bestP);

      if (result == null)
      {
         result = new double[2];
      }

      result[0] = bestT;
      result[1] = best;

      return result;
   }

   public static double[] getClosest(Point2D q, 
     Point2D p0, Point2D c1, Point2D c2, Point2D p1, 
     double startT, double incT,
     Point2D resultP, double[] result)
   {
      if (startT < 0.0 || startT > 1.0)
      {
         throw new IllegalArgumentException(
           "Invalid start t "+startT);
      }

      if (incT <= 0.0 || incT > 1.0)
      {
         throw new IllegalArgumentException(
           "Invalid t increment "+incT);
      }

      int prevBestIdx = 0;
      double prevBestT = startT;
      Point2D prevBestP = getP(startT, p0.getX(), p0.getY(), c1.getX(), c1.getY(),
           c2.getX(), c2.getY(), p1.getX(), p1.getY());
      double prevBest = Point2D.distanceSq(
        prevBestP.getX(), prevBestP.getY(),
        q.getX(), q.getY());

      int bestIdx = prevBestIdx;
      double bestT = prevBestT;
      Point2D bestP = prevBestP; 
      double best = prevBest;

      int i = 0;

      for (double t = startT+incT; t < 1.0; t += incT)
      {
         i++;

         Point2D pt = getP(t, p0.getX(), p0.getY(), c1.getX(), c1.getY(),
           c2.getX(), c2.getY(), p1.getX(), p1.getY());

         double d = Point2D.distanceSq(pt.getX(), pt.getY(),
           q.getX(), q.getY());

         if (d < best)
         {
            prevBestIdx = bestIdx;
            prevBestP = bestP;
            prevBest = best;
            prevBestT = bestT;

            bestIdx = i;
            bestP = pt;
            best = d;
            bestT = t;
         }
      } 

      double t = 1.0;

      double d = Point2D.distanceSq(p1.getX(), p1.getY(),
        q.getX(), q.getY());

      if (d < best)
      {
         prevBestIdx = bestIdx;
         prevBestP = bestP;
         prevBest = best;
         prevBestT = bestT;

         bestIdx = i+1;
         bestP = p1;
         best = d;
         bestT = t;
      }

      if (bestIdx - prevBestIdx == 1)
      {
         t = prevBestT + 0.5*incT;

         Point2D pt = getP(t, p0.getX(), p0.getY(), c1.getX(), c1.getY(),
           c2.getX(), c2.getY(), p1.getX(), p1.getY());

         d = Point2D.distanceSq(pt.getX(), pt.getY(),
           q.getX(), q.getY());

         if (d < best)
         {
            bestP = pt;
            best = d;
            bestT = t;
         }
      }

      resultP.setLocation(bestP);

      if (result == null)
      {
         result = new double[2];
      }

      result[0] = bestT;
      result[1] = best;

      return result;
   }

   public static double getEstimatedLength(double p0x, double p0y,
     double p1x, double p1y, double p2x, double p2y, double p3x, double p3y)
   {
      return 0.5*(
          Point2D.distance(p0x, p0y, p1x, p1y)
        + Point2D.distance(p2x, p2y, p1x, p1y)
        + Point2D.distance(p3x, p3y, p2x, p2y)
        + Point2D.distance(p0x, p0y, p3x, p3y));
   }

   public double getEstimatedLength()
   {
      return getEstimatedLength(start.x, start.y, 
        control1.x, control1.y, control2.x, control2.y, end.x, end.y);
   }

   public int controlCount()
   {
      return 3;
   }

   public JDRPoint getControl(int index)
      throws IndexOutOfBoundsException
   {
      if (index == 0) return start;

      if (index == 1) return control1;

      if (index == 2) return control2;

      throw new IndexOutOfBoundsException("No control point at index "+index);
   }

   public int getControlIndex(JDRPoint point)
      throws NoSuchElementException
   {
      if (point == start) return 0;

      if (point == control1) return 1;

      if (point == control2) return 2;

      throw new NoSuchElementException();
   }

   public boolean isGap() {return false;}

   public boolean isCurve() {return true;}

   public int getSegmentFlag()
   {
      return SEGMENT_FLAG_CURVE;
   }

   /**
    * The first curvature control point.
    */
   protected JDRPoint control1;
   /**
    * The second curvature control point.
    */
   protected JDRPoint control2;

   private static JDRBezierLoaderListener listener
      = new JDRBezierLoaderListener();
}
