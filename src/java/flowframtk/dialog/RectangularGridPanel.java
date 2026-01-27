// File          : RectangularGridPanel.java
// Description   : Panel in which to specify rectangular grid settings
// Creation Date : 14th Sept 2010
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2026 Nicola L.C. Talbot

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
import java.awt.geom.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Panel in which to specify rectangular grid settings.
 * @author Nicola L C Talbot
 */
public class RectangularGridPanel extends GridPanel
{
   public RectangularGridPanel(JDRResources resources)
   {
      super();

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      JLabelGroup labelGroup1 = new JLabelGroup();
      JLabelGroup labelGroup2 = new JLabelGroup();

      majorPanel = new LinkedLengthsPanel(resources, "grid.major");
      add(majorPanel);

      labelGroup1.add(majorPanel.getNumberLabel1());
      labelGroup2.add(majorPanel.getNumberLabel2());

      subDivisionsPanel = new LinkedNumbersPanel(resources, 
        "grid.sub_divisions",
        Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1));

      add(subDivisionsPanel);

      labelGroup1.add(subDivisionsPanel.getNumberLabel1());
      labelGroup2.add(subDivisionsPanel.getNumberLabel2());

      resources.clampCompMaxHeight(majorPanel, 10, 10);
      resources.clampCompMaxHeight(subDivisionsPanel, 10, 10);

      add(Box.createVerticalGlue());
   }

   @Override
   public void requestDefaultFieldFocus()
   {
      majorPanel.getNumberComponent1().getComponent().requestFocusInWindow();
   }

   @Override
   public void setGrid(JDRGrid grid)
   {
      JDRRectangularGrid rectGrid = (JDRRectangularGrid)grid;

      setMajor(
           rectGrid.getMajorXInterval(),
           rectGrid.getMajorYInterval(),
           rectGrid.getUnit()
         );

      setSubDivisions(
        rectGrid.getSubDivisionsX(),
        rectGrid.getSubDivisionsY()
      );
   }

   @Override
   public JDRGrid getGrid(JDRGrid grid)
   {
      double majorX = getMajorX();
      double majorY = (majorPanel.isLinked() ? majorX : getMajorY());

      int subDivX = getSubDivisionsX();
      int subDivY = (subDivisionsPanel.isLinked() ? subDivX : getSubDivisionsY());

      if (grid instanceof JDRRectangularGrid)
      {
         JDRRectangularGrid g = (JDRRectangularGrid)grid;

         g.set(getUnit(), majorX, majorY, subDivX, subDivY);
      }
      else
      {
         grid = new JDRRectangularGrid(grid.getCanvasGraphics(),
            getUnit(), majorX, majorY, subDivX, subDivY);
      }

      return grid;
   }

   public double getMajorX()
   {
      double major = majorPanel.getValue1();

      return major <= 0.0 ? 1.0 : major;
   }

   public double getMajorY()
   {
      double major = majorPanel.getValue2();

      return major <= 0.0 ? 1.0 : major;
   }

   @Override
   protected void setMajor(double value)
   {
      majorPanel.setValue(value, value, getUnit());
      majorPanel.setLinked(true);
   }

   protected void setMajor(double value1, double value2, JDRUnit unit)
   {
      majorPanel.setValue(value1, value2, unit);
   }

   public int getSubDivisions()
   {
      return getSubDivisionsX();
   }

   public int getSubDivisionsX()
   {
      return Math.max(1, subDivisionsPanel.getValue1().intValue());
   }

   public int getSubDivisionsY()
   {
      return Math.max(1, subDivisionsPanel.getValue2().intValue());
   }

   protected void setSubDivisions(int value)
   {
      setSubDivisions(value, value);
      subDivisionsPanel.setLinked(true);
   }

   protected void setSubDivisions(int value1, int value2)
   {
      subDivisionsPanel.setValue(value1, value2);
   }

   @Override
   public void setUnit(JDRUnit unit)
   {
      majorPanel.setUnit(unit);
   }

   @Override
   public JDRUnit getUnit()
   {
      return majorPanel.getUnit(); 
   }

   @Override
   public void addUnitChangeListener(ItemListener listener)
   {
      majorPanel.getUnitField().addItemListener(listener);
   }

   LinkedLengthsPanel majorPanel;
   LinkedNumbersPanel subDivisionsPanel;
}
