// File          : JDRPatternAdjustPoint.java
// Creation Date : 9th April 2011
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
 * Class representing a pattern offset control point.
 * @author Nicola L C Talbot
 */
public class JDRPatternAdjustPoint extends JDRPoint
{
   /**
    * Creates a new control point at the origin.
    */
   public JDRPatternAdjustPoint(CanvasGraphics cg)
   {
      super(cg);
   }

   /**
    * Creates a copy of a point.
    */ 
   public JDRPatternAdjustPoint(JDRPoint p)
   {
      super(p);
   }

   /**
    * Creates a control point at the given location.
    * @param p the location of the new point
    */
   public JDRPatternAdjustPoint(CanvasGraphics cg, Point p)
   {
      super(cg, p);
   }

   /**
    * Creates a control point at the given location.
    * @param p the location of the new point
    */
   public JDRPatternAdjustPoint(CanvasGraphics cg, Point2D p)
   {
      super(cg, p);
   }

   /**
    * Creates a control point at the given location.
    * @param px the x co-ordinate
    * @param py the y co-ordinate
    */
   public JDRPatternAdjustPoint(CanvasGraphics cg, double px, double py)
   {
      super(cg, px, py);
   }

   public Color getSelectedPaint()
   {
      return patternAdjustSelectColor;
   }

   public void setSelectedPaint(Color paint)
   {
      patternAdjustSelectColor = paint;
   }

   public Color getUnselectedPaint()
   {
      return patternAdjustColor;
   }

   public void setUnselectedPaint(Color paint)
   {
      patternAdjustColor = paint;
   }

   public Object clone()
   {
      return new JDRPatternAdjustPoint(this);
   }

   public int getControlFlag()
   {
      int flag = CONTROL_FLAG_PATTERN_ADJUST;

      if (isAnchored())
      {
         flag = (flag | CONTROL_FLAG_ANCHORED);
      }

      return flag;
   }

   /**
    * Gets string representation of this point.
    * @return string representation of this point
    */
   public String toString()
   {
      return new String("JDRPatternAdjustPoint("+x+","+y+")");
   }

   public static Color patternAdjustColor = new Color(0,255,255,200);
   public static Color patternAdjustSelectColor = new Color(0,127,127,200);
}
