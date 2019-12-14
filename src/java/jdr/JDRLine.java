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
