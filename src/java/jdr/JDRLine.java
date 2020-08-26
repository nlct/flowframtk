// File          : JDRLine.java
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

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;
/**
 * Class representing a line segment.
 * @author Nicola L C Talbot
 * @see JDRPath
 */

public class JDRLine extends JDRSegment
{
   /**
    * Creates a line segment between the given start and end points.
    * @param p0x the x co-ordinate of the start
    * @param p0y the y co-ordinate of the start
    * @param p1x the x co-ordinate of the end
    * @param p1y the y co-ordinate of the end
    */
   public JDRLine(CanvasGraphics cg, 
      double p0x, double p0y, double p1x, double p1y)
   {
      super(cg, p0x, p0y, p1x, p1y);
   }

   /**
    * Creates a copy of another line. 
    */ 
   public JDRLine(JDRLine line)
   {
      super(line);
   }

   /**
    * Creates a line segment between the given start and end points.
    * @param p0 the start location
    * @param p1 the end location
    */
   public JDRLine(CanvasGraphics cg, Point p0, Point p1)
   {
      super(cg, p0, p1);
   }

   /**
    * Creates a line segment between the given start and end points.
    * @param p0 the start location
    * @param p1 the end location
    */
   public JDRLine(CanvasGraphics cg, Point2D p0, Point2D p1)
   {
      super(cg, p0, p1);
   }

   /**
    * Creates a line segment between the given start and end points.
    * @param p0 the start location
    * @param p1 the end location
    */
   public JDRLine(JDRPoint p0, JDRPoint p1)
   {
      super(p0, p1);
   }

   /**
    * Creates a line segment whose start and end points are at the
    * origin.
    */
   public JDRLine(CanvasGraphics cg)
   {
      super(cg);
   }

   public JDRLine reverse()
   {
      JDRLine line = new JDRLine(this);

      line.start.x = end.x;
      line.start.y = end.y;
      line.end.x   = start.x;
      line.end.y   = start.y;

      return line;
   }

   public JDRPathSegment split()
   {
      JDRPoint midPt = new JDRPoint(getCanvasGraphics(), getP(0.5));

      JDRLine newSegment = new JDRLine(midPt, end);

      end = midPt;

      return newSegment;
   }

   public Object clone()
   {
      return new JDRLine(this);
   }

   public boolean equals(Object obj)
   {
      if (!super.equals(obj)) return false;

      // super.equals(obj) has already checked start and end

      return (obj instanceof JDRLine);
   }

   public void draw()
   {
      CanvasGraphics cg = getCanvasGraphics();

      cg.drawMagLine((int)start.x, (int)start.y, (int)end.x, (int)end.y);
   }

   public void print(Graphics2D g2)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (cg.getStorageUnitID() == JDRUnit.BP)
      {
         g2.drawLine((int)start.x, (int)start.y, (int)end.x, (int)end.y);
         return;
      }

      double scale = cg.storageToBp(1.0);

      g2.drawLine((int)(scale*start.x), (int)(scale*start.y),
                  (int)(scale*end.x), (int)(scale*end.y));
   }

   public void drawDraft(boolean drawEnd)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (isSelected())
      {
         drawSelectedNoControls();
      }
      else
      {
         cg.setPaint(draftColor);
         cg.drawMagLine(start.x, start.y, end.x, end.y);
      }

      drawControls(drawEnd);
   }

   public void saveSVG(SVG svg, String attr) 
     throws IOException
   {
      svg.print("L ");
      end.saveSVG(svg, attr);
   }

   public void saveEPS(PrintWriter out) throws IOException
   {
      end.saveEPS(out);
      out.println("lineto");
   }

   public void savePgf(TeX tex)
     throws IOException
   {
      tex.print("\\pgfpathlineto{");
      end.savePgf(tex);
      tex.println("}");
   }

   public void appendToGeneralPath(Path2D path)
   {
      path.lineTo(end.x, end.y);
   }

   public void appendReflectionToGeneralPath(Path2D path, JDRLine line)
   {
      Point2D p = start.getReflection(line);

      path.lineTo(p.getX(), p.getY());
   }

   public JDRPathSegment getReflection(JDRLine line)
   {
      Point2D p1 = start.getReflection(line);
      Point2D p2 = end.getReflection(line);

      return new JDRLine(getCanvasGraphics(), p1, p2);
   }

   /**
    * Gets the transformation representing a reflection in this
    * line.
    * @param af if non-null, the result is stored in here
    * @return the transformation representing a reflection in this
    * line.
    */
   public AffineTransform getReflectionTransform(AffineTransform af)
   {
      if (af == null)
      {
         af = new AffineTransform();
      }

      double sx = start.getX();
      double sy = start.getY();

      double dx = sx - end.getX();
      double dy = sy - end.getY();

      double dxsq = dx*dx;
      double dysq = dy*dy;

      double oneoversqdist = 1.0/(dxsq + dysq);

      if (Double.isInfinite(oneoversqdist)
       || Double.isNaN(oneoversqdist))
      {
         // Line of reflection has collapsed to a point
         // so no reflection can be formed. Just return original
         // identity transformation.

         af.setToIdentity();
         return af;
      }

      double one_over_msq_plus_1 = oneoversqdist * dxsq;

      double m_over_msq_plus_1 = dy*dx * oneoversqdist;

      double a00 = 2.0 * one_over_msq_plus_1 - 1.0;
      double a01 = 2.0 * m_over_msq_plus_1;
      double a02 = 2.0 * (1.0 - one_over_msq_plus_1) * sx
                 - 2.0 * m_over_msq_plus_1 * sy;

      double a10 = 2.0 * m_over_msq_plus_1;
      double a11 = 1.0 - 2 * one_over_msq_plus_1;
      double a12 = 2.0 * one_over_msq_plus_1 * sy
                 - 2.0 * m_over_msq_plus_1 * sx;

      af.setTransform(a00, a10, a01, a11, a02, a12);

      return af;
   }

   public AffineTransform getComponentReflectionTransform(AffineTransform af)
   {
      if (af == null)
      {
         af = new AffineTransform();
      }

      CanvasGraphics cg = getCanvasGraphics();

      double storageToCompX = cg.storageToComponentX(1.0);
      double storageToCompY = cg.storageToComponentY(1.0);

      double sx = storageToCompX*start.getX();
      double sy = storageToCompY*start.getY();
      double ex = storageToCompX*end.getX();
      double ey = storageToCompY*end.getY();

      double dx = sx - ex;
      double dy = sy - ey;

      double dxsq = dx*dx;
      double dysq = dy*dy;

      double oneoversqdist = 1.0/(dxsq + dysq);

      if (Double.isInfinite(oneoversqdist)
       || Double.isNaN(oneoversqdist))
      {
         // Line of reflection has collapsed to a point
         // so no reflection can be formed. Just return original
         // identity transformation.

         return af;
      }

      double one_over_msq_plus_1 = oneoversqdist * dxsq;

      double m_over_msq_plus_1 = dy*dx * oneoversqdist;

      double a00 = 2.0 * one_over_msq_plus_1 - 1.0;
      double a01 = 2.0 * m_over_msq_plus_1;
      double a02 = 2.0 * (1.0 - one_over_msq_plus_1) * sx
                 - 2.0 * m_over_msq_plus_1 * sy;

      double a10 = 2.0 * m_over_msq_plus_1;
      double a11 = 1.0 - 2 * one_over_msq_plus_1;
      double a12 = 2.0 * one_over_msq_plus_1 * sy
                 - 2.0 * m_over_msq_plus_1 * sx;

      af.setTransform(a00, a10, a01, a11, a02, a12);


      return af;
   }

   public double getAngle(JDRLine line)
   {
      return getAngle(start.x, start.y, end.x, end.y,
        line.start.x, line.start.y, line.end.x, line.end.y);
   }

   /*
    * Gets angle between the lines (p0, p1) and (p2, p3).
    */ 
   public static double getAngle(
     double p0x, double p0y, 
     double p1x, double p1y, 
     double p2x, double p2y,
     double p3x, double p3y
     )
   {
      double v1_x = p1x - p0x;
      double v1_y = p1y - p0y;

      double v2_x = p3x - p2x;
      double v2_y = p3y - p2y;

      return getVectorAngle(v1_x, v1_y, v2_x, v2_y);
   }

   /*
    * Gets angle between vectors v1 and v2.
    */ 
   public static double getVectorAngle(Point2D v1, Point2D v2)
   {
      return getVectorAngle(v1.getX(), v1.getY(), v2.getX(), v2.getY());
   }

   public static double getVectorAngle(double v1_x, double v1_y,
     double v2_x, double v2_y)
   {
      double length1 = Math.sqrt(v1_x*v1_x + v1_y * v1_y);
      double length2 = Math.sqrt(v2_x*v2_x + v2_y * v2_y); 

      double factor = 1.0/(length1 * length2);

      if (Double.isNaN(factor))
      {
         return 0.0;
      }

      double dotproduct = v1_x * v2_x + v1_y * v2_y;

      double angle = Math.acos(dotproduct * factor);

      if (Double.isNaN(angle))
      {
         angle = Math.PI;
      }

      return angle;
   }

   public static double getSquareLength(Point2D p1, Point2D p2)
   {
      return Point2D.distanceSq(p1.getX(), p1.getY(), p2.getX(), p2.getY());
   }

   public static double getLength(Point2D p1, Point2D p2)
   {
      return Point2D.distance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
   }

   public double getLength()
   {
      return Point2D.distance(start.x, start.y, end.x, end.y);
   }

   public static Point2D getGradient(Point2D p0, Point2D p1)
   {
      return getGradient(p0.getX(), p0.getY(), p1.getX(), p1.getY());
   }

   public static Point2D getGradient(double x0, double y0, double x1, double y1)
   {
      return new Point2D.Double(x1-x0, y1-y0);
   }

   public static Point2D getMidPoint(Point2D p0, Point2D p1)
   {
      return getMidPoint(p0.getX(), p0.getY(), p1.getX(), p1.getY());
   }

   public static Point2D getMidPoint(double p0x, double p0y, double p1x, double p1y)
   {
      return new Point2D.Double(p0x+0.5*(p1x-p0x), p0y+0.5*(p1y-p0y));
   }

   /**
    * Gets point on line closest to given point.
    * The line is defined by r1 and r2 but the closest point may be
    * outside those end points. 
    * @param r1 first point on line
    * @param r2 second point on line
    * @param p given point
    * @param limit if true limit to the end points
    * @return the closest point on the line to p or r1 if the point
    * can't be computed (most likely because r1 and r2 coincide)
   */
   public static Point2D getClosestPointAlongLine(Point2D r1, Point2D r2, Point2D p, boolean limit)
   {
      // get closest point on r1-r2 to p

      double diff_y = r1.getY() - r2.getY();
      double diff_x = r1.getX() - r2.getX();
      double m = diff_y/diff_x;

      double m_sq = 0.0;
      double orthog_m=0.0;
      double orthog_m_sq = 0.0;
      boolean use_orthog = false;
      double factor, x, y;

      if (Double.isNaN(m) || Double.isInfinite(m))
      {
         use_orthog = true;
         orthog_m = -diff_x/diff_y;
         orthog_m_sq = orthog_m*orthog_m;
         factor = 1.0/(1.0 + orthog_m_sq);
      }
      else
      {
         m_sq = m*m;
         factor = 1.0/(1.0 + m_sq);
      }

      if (use_orthog)
      {
         x = (r1.getX()+orthog_m*(r1.getY()-p.getY())
                +orthog_m_sq*p.getX())*factor;
         y = orthog_m * (x - p.getX()) + p.getY();
      }
      else
      {
         x = (m_sq*r1.getX()+m*(p.getY()-r1.getY())+p.getX())*factor;
         y = m * (x - r1.getX()) + r1.getY();
      }

      if (Double.isNaN(x) || Double.isNaN(y))
      {// most likely caused by r1 = r2
         return r1;
      }

      if (limit)
      {
         double t = (x + y - r1.getX() - r1.getY())
                  / (r2.getX() - r1.getX() + r2.getY() - r1.getY());

         if (t <= 0.0)
         {
            return r1;
         }

         if (t >= 1.0)
         {
            return r2;
         }
      }

      return new Point2D.Double(x, y);
   }

   public static Point2D getClosestPointAlongLine(Point2D r1, Point2D r2, Point2D p)
   {
      return getClosestPointAlongLine(r1, r2, p, false);
   }

   public JDRObjectLoaderListener getListener()
   {
      return listener;
   }

   public String toString()
   {
      return "JDRLine:("+start.x+","+start.y+")("+end.x+","+end.y+"),startMarker="+startMarker+",endMarker="+endMarker;
   }

   public String info()
   {
      return "line["+start.info()+","+end.info()+"]";
   }

   public boolean isGap() {return false;}

   public int getSegmentFlag()
   {
      return SEGMENT_FLAG_LINE;
   }

   private static JDRLineLoaderListener listener
      = new JDRLineLoaderListener();
}
