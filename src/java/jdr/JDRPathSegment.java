// File          : JDRPathSegment.java
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

import com.dickimawbooks.jdr.marker.*;
import com.dickimawbooks.jdr.io.TeX;
import com.dickimawbooks.jdr.io.SVG;
import com.dickimawbooks.jdr.io.JDRObjectLoaderListener;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Path segment interface.
 * @author Nicola L C Talbot
 */

public interface JDRPathSegment extends Cloneable,Serializable
{
   /**
    * Gets a copy of this segment.
    */
   public Object clone();

   public void savePgf(TeX tex) throws IOException;

   public void translate(double x, double y);

   public void rotate(Point2D p, double angle);

   public void scale(Point2D p, double sx, double sy);

   public void shear(Point2D p, double sx, double sy);

   public void transform(double[] matrix);

   public double getStartX();

   public double getStartY();

   public double getEndX();

   public double getEndY();

   public JDRPoint getStart();

   public JDRPoint getEnd();

   public void setStart(JDRPoint p);

   public void setStart(Point2D p);

   public void setEnd(JDRPoint p);

   public JDRPathSegment getReflection(JDRLine line);

   public JDRPathSegment reverse();

   public void setEditedControls(boolean flag);

   public JDRPoint getEditedControl();

   public void setSelected(boolean flag);

   public boolean isSelected();

   public JDRPathSegment convertToLine();
   public JDRPathSegment convertToSegment();
   public JDRPathSegment convertToBezier();

   public BBox getStorageControlBBox();

   public void mergeStorageControlBBox(BBox box);

   public BBox getBpControlBBox();

   public void mergeBpControlBBox(BBox box);

   public void appendToGeneralPath(Path2D path);

   public void draw();

   public void drawSelectedNoControls();

   public void drawControls(boolean endPoint);

   /**
    * Splits this segment in half. Returns the other half.
    * @return the end half of this segment before it was split
    */
   public JDRPathSegment split();

   /**
    * Draws this segment in draft mode.
    * @param drawEnd flag indicating whether to draw the end point
    */
   public void drawDraft(boolean drawEnd);

   public void saveSVG(SVG svg) throws IOException;

   public void saveEPS(PrintWriter out) throws IOException;

   public Point2D getP(double t);

   /**
    * Gets the gradient vector for the line between the start and
    * end points of this segment.
    * @return gradient vector
    */
   public Point2D getdP();

   public void flatten();

   /**
    * Gets the gradient vector for this segment at t.
    * @param t the parameter used to describe this segment
    * @return gradient vector
    * @see #getdP0()
    * @see #getdP1()
    */
   public Point2D getdP(double t);

   /**
    * Gets the gradient vector for this segment at t=0.
    * @return gradient vector at t=0
    */
   public Point2D getdP0();

   /**
    * Gets the gradient vector for this segment at t=1.
    * @return gradient vector at t=1
    */
   public Point2D getdP1();

   public JDRMarker getStartMarker();

   public JDRMarker getEndMarker();

   public void setStartMarker(JDRMarker marker);

   public void setEndMarker(JDRMarker marker);

   public int controlCount();

   public JDRPoint getControl(int index) throws IndexOutOfBoundsException;

   public int getControlIndex(JDRPoint point) throws NoSuchElementException;

   public BBox getStorageBBox();

   /**
    * Returns true if this represents a gap in the shape.
    */
   public boolean isGap();

   public boolean isCurve();

   public void setCanvasGraphics(CanvasGraphics cg);

   public CanvasGraphics getCanvasGraphics();

   public JDRObjectLoaderListener getListener();

   public int getSegmentFlag();

   public String getDetails();
}
