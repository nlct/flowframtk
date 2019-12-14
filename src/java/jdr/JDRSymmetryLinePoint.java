// File          : JDRSymmetryLinePoint.java
// Creation Date : 8th April 2011
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

import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a pattern anchor control point.
 * @author Nicola L C Talbot
 */
public class JDRSymmetryLinePoint extends JDRPoint
{
   /**
    * Creates a new control point at the origin.
    */
   public JDRSymmetryLinePoint(CanvasGraphics cg)
   {
      super(cg);
   }

   /**
    * Creates a copy of a point.
    */ 
   public JDRSymmetryLinePoint(JDRPoint p)
   {
      super(p);
   }

   /**
    * Creates a control point at the given location.
    * @param p the location of the new point
    */
   public JDRSymmetryLinePoint(CanvasGraphics cg, Point p)
   {
      super(cg, p);
   }

   /**
    * Creates a control point at the given location.
    * @param p the location of the new point
    */
   public JDRSymmetryLinePoint(CanvasGraphics cg, Point2D p)
   {
      super(cg, p);
   }

   /**
    * Creates a control point at the given location.
    * @param px the x co-ordinate
    * @param py the y co-ordinate
    */
   public JDRSymmetryLinePoint(CanvasGraphics cg, double px, double py)
   {
      super(cg, px, py);
   }

   public Color getSelectedPaint()
   {
      return symmetrySelectedColor;
   }

   public void setSelectedPaint(Color paint)
   {
      symmetrySelectedColor = paint;
   }

   public Color getUnselectedPaint()
   {
      return symmetryPointColor;
   }

   public void setUnselectedPaint(Color paint)
   {
      symmetryPointColor = paint;
   }

   public Object clone()
   {
      return new JDRSymmetryLinePoint(this);
   }

   public int getControlFlag()
   {
      int flag = CONTROL_FLAG_REGULAR;

      if (isAnchored())
      {
         flag = (flag | CONTROL_FLAG_SYMMETRY_LINE);
      }

      return flag;
   }

   /**
    * Gets string representation of this point.
    * @return string representation of this point
    */
   public String toString()
   {
      return new String("JDRSymmetryLinePoint("+x+","+y+")");
   }

   public static Color symmetryPointColor = new Color(102,102,255,200);
   public static Color symmetrySelectedColor = new Color(0,0,125,200);
}
