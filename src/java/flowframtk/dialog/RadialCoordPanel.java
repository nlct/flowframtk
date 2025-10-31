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
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

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

      pageCentredButton = resources.createAppCheckBox("coordinates", "radial_page", true, null);
      add(pageCentredButton);

      pageCentredButton.addItemListener(new ItemListener()
       {
          @Override
          public void itemStateChanged(ItemEvent evt)
          {
             if (paper != null)
             {
                double x = 0.5*paper.getWidth();
                double y = 0.5*paper.getHeight();

                if (isPageCentred())
                {
                   translate(JDRUnit.bp, -x, -y);
                }
                else
                {
                   translate(JDRUnit.bp, x, y);
                }
             }
          }
       });

      setName(resources.getMessage("grid.radial"));
   }

   public void requestCoordFocus()
   {
      radiusPanel.getTextField().requestFocusInWindow();
   }

   public void setPageCentred(boolean pageCentred)
   {
      pageCentredButton.setSelected(pageCentred);
   }

   public boolean isPageCentred()
   {
      return pageCentredButton.isSelected();
   }

   public void setCoords(double x, double y, JDRUnit unit, JDRPaper paper)
   {
      this.paper = paper;

      JDRRadialPoint p = new JDRRadialPoint(0, 
        new JDRAngle(resources.getMessageSystem(), 0, anglePanel.getUnit()));

      double bpX = unit.toBp(x);
      double bpY = unit.toBp(y);

      if (isPageCentred())
      {
         bpX -= 0.5*paper.getWidth();
         bpY -= 0.5*paper.getHeight();
      }

      p.setLocation(bpX, bpY);

      radiusPanel.setValue(unit.fromBp(p.getRadius()), unit);

      anglePanel.setValue(p.getAngle());
   }

   public void translate(JDRUnit unit, double dx, double dy)
   {
      if (paper != null)
      {
         JDRLength xlen = getXCoord();
         JDRLength ylen = getYCoord();

         setCoords(xlen.getValue(unit)+dx, ylen.getValue(unit)+dy, unit, paper);
      }
   }

   public JDRLength getXCoord()
   {
      JDRLength coord = radiusPanel.getLength();

      coord.scale(Math.cos(anglePanel.getValue().toRadians()));

      if (isPageCentred())
      {
         coord.add(0.5*paper.getWidth(), JDRUnit.bp);
      }

      return coord;
   }

   public JDRLength getYCoord()
   {
      JDRLength coord = radiusPanel.getLength();

      coord.scale(Math.sin(anglePanel.getValue().toRadians()));

      if (isPageCentred())
      {
         coord.add(0.5*paper.getHeight(), JDRUnit.bp);
      }

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
   private JCheckBox pageCentredButton;

   private JDRPaper paper;

   private JDRResources resources;
}
