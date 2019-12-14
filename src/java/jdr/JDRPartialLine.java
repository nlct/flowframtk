// File          : JDRPartialLine.java
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

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a partial line.
 * @author Nicola L C Talbot
 * @see JDRPath
 */
public class JDRPartialLine extends JDRPartialSegment 
{
   /**
    * Creates a new partial line with null starting point and
    * null line of symmetry.
    */
   public JDRPartialLine(CanvasGraphics cg)
   {
      super(cg);
   }

   /**
    * Creates a copy.
    */ 
   public JDRPartialLine(JDRPartialLine line)
   {
      super(line);
   }

   /**
    * Creates a new partial line with given starting point and
    * line of symmetry.
    */
   public JDRPartialLine(JDRPoint point, JDRLine line)
   {
      super(point, line);
   }

   public JDRPartialLine(CanvasGraphics cg, JDRPoint point, JDRLine line)
   {
      super(cg, point, line);
   }

   public Object clone()
   {
      return new JDRPartialLine(this);
   }

   public JDRPathSegment getReflection(JDRLine line)
   {
      return new JDRPartialLine(getEnd(), line);
   }

   public JDRSegment getFullSegment()
   {
      return new JDRLine(getStart(), getEnd());
   }

   public void savePgf(TeX tex)
     throws IOException
   {
      Point2D p = start.getReflection(line_);

      AffineTransform af = tex.getTransform();

      tex.println("\\pgfpathlineto{"+tex.point(getCanvasGraphics(),
         af, p.getX(), p.getY())+"}");
   }

   public void saveSVG(SVG svg) throws IOException
   {
      svg.println("L ");
      svg.savePoint(getEnd2D());
   }

   public void saveEPS(PrintWriter out) throws IOException
   {
      EPS.saveStoragePoint(getCanvasGraphics(), out, getEnd2D());
      out.println("lineto");
   }

   /**
    * Appends this line to the given path.
    * @param path the path to which this segment must be appended
    */
   public void appendToGeneralPath(Path2D path)
   {
      Point2D p = start.getReflection(line_);

      path.lineTo(p.getX(), p.getY());
   }

   public JDRPathSegment convertToSegment()
   {
      return new JDRPartialSegment(start, line_);
   }

   public JDRPathSegment convertToLine()
   {
      return this;
   }

   public JDRPathSegment convertToBezier()
   {
      return new JDRPartialBezier(start, line_);
   }

   public void drawControls(Graphics g, boolean endPoint, double scale)
   {
   }

   public void draw()
   {
      CanvasGraphics cg = getCanvasGraphics();
      Point2D p = start.getReflection(line_);

      cg.drawMagLine(start.x, start.y,
                     p.getX(), p.getY());
   }

   public void drawDraft(boolean drawEnd)
   {
      if (isSelected())
      {
         drawSelectedNoControls();
      }
      else
      {
         draw();
      }

      drawControls(drawEnd);
   }

   public void print(Graphics2D g2)
   {
      Point2D p = start.getReflection(line_);

      CanvasGraphics cg = getCanvasGraphics();

      if (cg.getStorageUnitID() == JDRUnit.BP)
      {
         g2.drawLine((int)start.x, (int)start.y,
                     (int)p.getX(), (int)p.getY());
         return;
      }

      double scale = cg.storageToBp(1.0);

      g2.drawLine((int)(scale*start.x), (int)(scale*start.y),
                 (int)(scale*p.getX()), (int)(scale*p.getY()));
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (!(obj instanceof JDRPartialLine)) return false;

      JDRPartialLine seg = (JDRPartialLine)obj;

      return seg.start.equals(start)
          && line_.equals(seg.getSymmetryLine());
   }

   public JDRObjectLoaderListener getListener()
   {
      return listener;
   }

   public boolean isGap() {return false;}

   public String info()
   {
      return "partial line: start="+start.info()
        +", symmetry="+line_.info();
   }

   public String toString()
   {
      return "JDRPartialLine[start="+start+",symmetry="+line_+"]";
   }

   public int getSegmentFlag()
   {
      return SEGMENT_FLAG_PARTIAL_LINE;
   }

   private static JDRPartialLineLoaderListener listener
      = new JDRPartialLineLoaderListener();
}
