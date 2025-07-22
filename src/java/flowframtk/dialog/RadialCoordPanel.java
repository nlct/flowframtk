// File        : RadialCoordPanel.java
// Description : Panel for entering radial coordinates
// Date        : 2012-03-05
// Author      : Nicola L. C. Talbot
//               http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2025 Nicola L.C. Talbot

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

package com.dickimawbooks.flowframtk.dialog;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

public class RadialCoordPanel extends JPanel implements CoordPanel
{
   public RadialCoordPanel(JDRResources resources)
   {
      super();
      this.resources = resources;

      radiusPanel = resources.createNonNegativeLengthPanel(
         "coordinates.radius");
      add(radiusPanel);

      anglePanel = resources.createAnglePanel("coordinates.angle");

      add(anglePanel);

      setName(resources.getMessage("grid.radial"));
   }

   public void requestCoordFocus()
   {
      radiusPanel.getTextField().requestFocusInWindow();
   }

   public void setCoords(double x, double y, JDRUnit unit, JDRPaper paper)
   {
      this.paper = paper;

      JDRRadialPoint p = new JDRRadialPoint(0, 
        new JDRAngle(resources.getMessageSystem(), 0, anglePanel.getUnit()));

      p.setLocation(unit.toBp(x)-0.5*paper.getWidth(),
                    unit.toBp(y)-0.5*paper.getHeight());

      radiusPanel.setValue(unit.fromBp(p.getRadius()), unit);

      anglePanel.setValue(p.getAngle());
   }

   public JDRLength getXCoord()
   {
      JDRLength coord = radiusPanel.getLength();

      coord.scale(Math.cos(anglePanel.getValue().toRadians()));

      coord.add(0.5*paper.getWidth(), JDRUnit.bp);

      return coord;
   }

   public JDRLength getYCoord()
   {
      JDRLength coord = radiusPanel.getLength();

      coord.scale(Math.sin(anglePanel.getValue().toRadians()));

      coord.add(0.5*paper.getHeight(), JDRUnit.bp);

      return coord;
   }

   public void setUnit(JDRUnit unit)
   {
      radiusPanel.setUnit(unit);
   }

   public void addCoordinateChangeListener(ChangeListener listener)
   {
      radiusPanel.addChangeListener(listener);
      anglePanel.addChangeListener(listener);
   }

   public void setEnabled(boolean enable)
   {
      super.setEnabled(enable);
      radiusPanel.setEnabled(enable);
      anglePanel.setEnabled(enable);
   }

   private NonNegativeLengthPanel radiusPanel;

   private AnglePanel anglePanel;

   private JDRPaper paper;

   private JDRResources resources;
}
