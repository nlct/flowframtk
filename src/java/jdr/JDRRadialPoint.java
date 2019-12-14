// File          : JDRRadialPoint.java
// Description   : Represents a radial point.
// Author        : Nicola L.C. Talbot
// Creation Date : 16th August 2010
//              http://www.dickimaw-books.com/

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

import java.awt.geom.*;

import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;

/**
 * Class representing a radial co-ordinate.
 * @author Nicola L C Talbot
 */
public class JDRRadialPoint extends Point2D
{
   /**
    * Creates a new point at the origin.
    */
   public JDRRadialPoint(JDRMessageDictionary msgDict)
   {
      radius = 0;
      angle = new JDRAngle(msgDict);
   }

   /**
    * Creates a new point with the given radius and angle (in
    * radians). The radius must be non-negative.
    */
   public JDRRadialPoint(double theRadius, JDRAngle theAngle)
      throws JdrIllegalArgumentException
   {
      setRadius(theRadius);
      setAngle(theAngle);
   }

   /**
    * Sets the radius and angle.
    */
   public void setParameters(double theRadius, JDRAngle theAngle)
      throws JdrIllegalArgumentException
   {
      setRadius(theRadius);
      setAngle(theAngle);
   }

   /**
    * Sets the angle.
    */
   public void setAngle(JDRAngle theAngle)
   {
      if (theAngle == null)
      {
         throw new NullPointerException();
      }

      if (angle == null)
      {
         angle = theAngle;
      }
      else
      {
         angle.makeEqual(theAngle);
      }
   }

   /**
    * Sets the radius.
    */
   public void setRadius(double theRadius)
      throws JdrIllegalArgumentException
   {
      if (radius < 0)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.RADIUS, theRadius,
           getMessageDictionary());
      }

      radius = theRadius;
   }

   /**
    * Gets the radius (in storage units).
    */
   public double getRadius()
   {
      return radius;
   }

   /**
    * Gets the angle.
    */
   public JDRAngle getAngle()
   {
      return angle;
   }

   public double getX()
   {
      return radius*Math.cos(angle.toRadians());
   }

   public double getY()
   {
      return radius*Math.sin(angle.toRadians());
   }

   /**
    * Gets this point in (X,Y) form.
    * @return this point in (X,Y) form
    */
   public Point2D.Double getPoint2D()
   {
      return new Point2D.Double(getX(), getY());
   }

   /**
    * Converts this point to (X,Y) form. The result is stored in the
    * argument.
    * @param target object in which to store the result
    */
   public void getPoint2D(Point2D.Double target)
   {
      target.setLocation(getX(), getY());
   }

   /**
    * Creates a radial point from the given (X,Y) point.
    */
   public static JDRRadialPoint createRadialPoint(JDRMessageDictionary msgDict, 
      Point2D point)
   {
      JDRRadialPoint radialP = new JDRRadialPoint(msgDict);
      radialP.setLocation(point);

      return radialP;
   }

   /**
    * Sets this to the given point.
    * @param point the specified point to which this should be set
    */
   public void setLocation(Point2D point)
   {
      if (point instanceof JDRRadialPoint)
      {
         angle.makeEqual(((JDRRadialPoint)point).getAngle());
         radius = ((JDRRadialPoint)point).getRadius();
      }
      else
      {
         setLocation(point.getX(), point.getY());
      }
   }

   /**
    * Sets this to the radial equivalent of the given (X,Y) point.
    * @param x
    * @param y
    */
   public void setLocation(double x, double y)
   {
      angle.fromRadians(Math.atan2(y, x));
      radius = Math.sqrt(x*x +y*y);
   }

   public Object clone()
   {
      JDRRadialPoint p = new JDRRadialPoint(getMessageDictionary());

      p.makeEqual(this);

      return p;
   }

   public void makeEqual(JDRRadialPoint p)
   {
      radius = p.radius;

      angle.makeEqual(p.angle);
   }

   public String toString()
   {
      return "JDRRadialPoint[radius="+radius+",angle="+angle+"]";
   }

   public void setMessageSystem(JDRMessageDictionary msgDict)
   {
      angle.setMessageSystem(msgDict);
   }

   public JDRMessageDictionary getMessageDictionary()
   {
      return angle.getMessageDictionary();
   }

   private double radius;
   private JDRAngle angle;
}
