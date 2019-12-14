// File          : ArrowScissorsDownFilled.java
// Creation Date : 13th May 2008
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

package com.dickimawbooks.jdr.marker;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Filled downward facing broken scissor marker.
 * The basic marker shape looks like:
 * <img src="../images/scissorDownFilledMarker.png" alt="[filled downward partial scissors]">
 * This marker's shape depends on the given marker size. The shape
 * is offset by half the line width.
 * See {@link JDRMarker} for a description of markers.
 *
 * @author Nicola L C Talbot
 */
public class ArrowScissorsDownFilled extends JDRMarker
{
   /**
    * Creates filled downward partial scissor marker of given size.
    * The marker may be repeated and/or reversed.
    */
   public ArrowScissorsDownFilled(JDRLength penwidth, int repeat,
                      boolean isReversed, JDRLength markerSize)
   {
      super(penwidth, repeat, isReversed, markerSize);

      type = ARROW_SCISSORS_DOWN_FILLED;
   }

   public String getID()
   {
      return reversed ?
           "arrow-r"+repeated+"scissorsdownfilled":
           "arrow-"+repeated+"scissorsdownfilled";
   }

   /**
    * Gets the path describing the basic shape of this marker.
    */
   public GeneralPath getGeneralPath()
   {
      JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

      double markerSize = size.getValue(storageUnit);

      GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

      double scale = 0.02*markerSize;
      float halfW = (float)(0.5*penWidth.getValue(storageUnit));
      double dx = -16.0;

      path.moveTo((float)(scale*(16.0+dx)), halfW);
      path.lineTo((float)(scale*(-0.89+dx)), (float)(scale*27.36)+halfW);
      path.lineTo((float)(scale*(71.99+dx)), (float)(scale*23.73)+halfW);
      path.curveTo((float)(scale*(66.88+dx)), (float)(scale*29.50)+halfW,
                   (float)(scale*(64.58+dx)), (float)(scale*35.73)+halfW,
                   (float)(scale*(44.12+dx)), (float)(scale*39.02)+halfW);
      path.curveTo((float)(scale*(23.66+dx)), (float)(scale*42.31)+halfW,
                   (float)(scale*(6.81+dx)), (float)(scale*44.57)+halfW,
                   (float)(scale*(-14.53+dx)), (float)(scale*46.29)+halfW);
      path.curveTo((float)(scale*(-23.70+dx)), (float)(scale*61.16)+halfW,
                   (float)(scale*(-35.06+dx)), (float)(scale*64.70)+halfW,
                   (float)(scale*(-42.31+dx)), (float)(scale*71.17)+halfW);
      path.curveTo((float)(scale*(-35.62+dx)), (float)(scale*77.03)+halfW,
                   (float)(scale*(-34.28+dx)), (float)(scale*87.24)+halfW,
                   (float)(scale*(-38.47+dx)), (float)(scale*92.97)+halfW);
      path.curveTo((float)(scale*(-42.65+dx)), (float)(scale*98.70)+halfW,
                   (float)(scale*(-50.15+dx)), (float)(scale*106.04)+halfW,
                   (float)(scale*(-61.74+dx)), (float)(scale*102.64)+halfW);
      path.curveTo((float)(scale*(-73.32+dx)), (float)(scale*99.24)+halfW,
                   (float)(scale*(-73.31+dx)), (float)(scale*84.57)+halfW,
                   (float)(scale*(-65.69+dx)), (float)(scale*77.98)+halfW);
      path.curveTo((float)(scale*(-61.37+dx)), (float)(scale*72.61)+halfW,
                   (float)(scale*(-55.85+dx)), (float)(scale*70.67)+halfW,
                   (float)(scale*(-50.40+dx)), (float)(scale*68.37)+halfW);
      path.curveTo((float)(scale*(-37.80+dx)), (float)(scale*65.44)+halfW,
                   (float)(scale*(-30.61+dx)), (float)(scale*54.44)+halfW,
                   (float)(scale*(-23.88+dx)), (float)(scale*43.13)+halfW);
      path.curveTo((float)(scale*(-30.57+dx)), (float)(scale*39.95)+halfW,
                   (float)(scale*(-45.48+dx)), (float)(scale*34.71)+halfW,
                   (float)(scale*(-54.52+dx)), (float)(scale*39.31)+halfW);
      path.curveTo((float)(scale*(-61.31+dx)), (float)(scale*43.13)+halfW,
                   (float)(scale*(-73.21+dx)), (float)(scale*47.76)+halfW,
                   (float)(scale*(-80.35+dx)), (float)(scale*42.42)+halfW);
      path.curveTo((float)(scale*(-87.32+dx)), (float)(scale*36.86)+halfW,
                   (float)(scale*(-87.66+dx)), (float)(scale*23.41)+halfW,
                   (float)(scale*(-81.39+dx)), (float)(scale*17.82)+halfW);
      path.curveTo((float)(scale*(-75.11+dx)), (float)(scale*12.23)+halfW,
                   (float)(scale*(-65.26+dx)), (float)(scale*7.87)+halfW,
                   (float)(scale*(-56.02+dx)), (float)(scale*14.98)+halfW);
      path.curveTo((float)(scale*(-46.42+dx)), (float)(scale*22.90)+halfW,
                   (float)(scale*(-54.20+dx)), (float)(scale*28.69)+halfW,
                   (float)(scale*(-50.98+dx)), (float)(scale*33.31)+halfW);
      path.curveTo((float)(scale*(-40.23+dx)), (float)(scale*31.22)+halfW,
                   (float)(scale*(-29.57+dx)), (float)(scale*29.72)+halfW,
                   (float)(scale*(-17.69+dx)), (float)(scale*29.55)+halfW);
      path.lineTo((float)(scale*dx), halfW);
      path.lineTo((float)(scale*(16.0+dx)), halfW);
      path.moveTo((float)(scale*(-65.66+dx)), (float)(scale*91.14)+halfW);
      path.curveTo((float)(scale*(-63.83+dx)), (float)(scale*96.45)+halfW,
                   (float)(scale*(-60.24+dx)), (float)(scale*100.72)+halfW,
                   (float)(scale*(-51.38+dx)), (float)(scale*97.94)+halfW);
      path.curveTo((float)(scale*(-42.51+dx)), (float)(scale*94.58)+halfW,
                   (float)(scale*(-40.29+dx)), (float)(scale*87.91)+halfW,
                   (float)(scale*(-41.05+dx)), (float)(scale*83.43)+halfW);
      path.curveTo((float)(scale*(-41.28+dx)), (float)(scale*76.84)+halfW,
                   (float)(scale*(-49.66+dx)), (float)(scale*72.37)+halfW,
                   (float)(scale*(-55.21+dx)), (float)(scale*75.65)+halfW);
      path.curveTo((float)(scale*(-60.75+dx)), (float)(scale*78.93)+halfW,
                   (float)(scale*(-67.05+dx)), (float)(scale*84.98)+halfW,
                   (float)(scale*(-65.66+dx)), (float)(scale*91.14)+halfW);
      path.moveTo((float)(scale*(-56.45+dx)), (float)(scale*25.53)+halfW);
      path.curveTo((float)(scale*(-57.49+dx)), (float)(scale*19.66)+halfW,
                   (float)(scale*(-63.81+dx)), (float)(scale*15.59)+halfW,
                   (float)(scale*(-70.39+dx)), (float)(scale*16.75)+halfW);
      path.curveTo((float)(scale*(-76.97+dx)), (float)(scale*17.91)+halfW,
                   (float)(scale*(-82.37+dx)), (float)(scale*25.30)+halfW,
                   (float)(scale*(-81.33+dx)), (float)(scale*31.17)+halfW);
      path.curveTo((float)(scale*(-80.30+dx)), (float)(scale*37.03)+halfW,
                   (float)(scale*(-73.69+dx)), (float)(scale*40.48)+halfW,
                   (float)(scale*(-67.11+dx)), (float)(scale*39.32)+halfW);
      path.curveTo((float)(scale*(-60.53+dx)), (float)(scale*38.16)+halfW,
                   (float)(scale*(-55.42+dx)), (float)(scale*31.40)+halfW,
                   (float)(scale*(-56.45+dx)), (float)(scale*25.53)+halfW);
      path.moveTo((float)(scale*(16.0+dx)), halfW);
      path.closePath();

      return path;
   }

   public boolean isResizable()
   {
      return true;
   }

   public boolean usesLineWidth()
   {
      return true;
   }

   /**
    * Not implemented (returns empty string).
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "";
   }

   public Object clone()
   {
      JDRMarker marker = new ArrowScissorsDownFilled(penWidth, repeated,
                                         reversed, (JDRLength)size.clone());
      makeEqual(marker);

      return marker;
   }

   public boolean hasXAxisSymmetry()
   {
      return false;
   }
}
