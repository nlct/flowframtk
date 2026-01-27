// File          : IsoGridPanel.java
// Description   : Panel in which to specify isometric grid settings
// Creation Date : 2014-06-06
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

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
public class IsoGridPanel extends GridPanel
{
   public IsoGridPanel(JDRResources resources)
   {
      super();

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      Box row = Box.createHorizontalBox();
      add(row);

      JLabelGroup labelGroup = new JLabelGroup();

      JLabel majorLabel = resources.createAppLabel("grid.major");
      row.add(majorLabel);
      labelGroup.add(majorLabel);

      row.add(resources.createLabelSpacer());

      majorDivisionsModel = new SpinnerNumberModel(
        Double.valueOf(100), Double.valueOf(0), null, Double.valueOf(1));
      majorDivisionsSpinner = new JSpinner(majorDivisionsModel);
      majorLabel.setLabelFor(majorDivisionsSpinner);
      row.add(majorDivisionsSpinner);

      unitBox = new JComboBox<String>(JDRUnit.UNIT_LABELS);
      unitBox.setSelectedIndex(JDRUnit.BP);
      row.add(unitBox);

      row.add(Box.createHorizontalGlue());

      row = Box.createHorizontalBox();
      add(row);

      JLabel subdivisionsLabel = resources.createAppLabel("grid.sub_divisions");

      row.add(subdivisionsLabel);
      labelGroup.add(subdivisionsLabel);

      row.add(resources.createLabelSpacer());

      subDivisionsModel = new SpinnerNumberModel(
         Integer.valueOf(10), Integer.valueOf(1), null, Integer.valueOf(1));
      subDivisionsSpinner = new JSpinner(subDivisionsModel);
      subdivisionsLabel.setLabelFor(subDivisionsSpinner);

      row.add(subDivisionsSpinner);

      row.add(Box.createHorizontalStrut(
         (int)unitBox.getPreferredSize().getWidth()));

      row.add(Box.createHorizontalGlue());

      Dimension dim = majorDivisionsSpinner.getPreferredSize();
      dim.width = Integer.MAX_VALUE;
      int height = dim.height;
      majorDivisionsSpinner.setMaximumSize(dim);

      dim = unitBox.getPreferredSize();
      unitBox.setMaximumSize(dim);

      dim = subDivisionsSpinner.getPreferredSize();
      dim.width = Integer.MAX_VALUE;
      subDivisionsSpinner.setMaximumSize(dim);

      add(Box.createVerticalStrut(4*height));

      add(Box.createVerticalGlue());
   }

   @Override
   public void requestDefaultFieldFocus()
   {
      majorDivisionsSpinner.requestFocusInWindow();
   }

   @Override
   public void setGrid(JDRGrid grid)
   {
      JDRIsoGrid isoGrid = (JDRIsoGrid)grid;

      setMajor(isoGrid.getMajorInterval());
      setSubDivisions(isoGrid.getSubDivisions());
      setUnit(isoGrid.getUnit());
   }

   @Override
   public JDRGrid getGrid(JDRGrid grid)
   {
      if (grid instanceof JDRIsoGrid)
      {
         JDRIsoGrid g = (JDRIsoGrid)grid;

         g.set(getUnit(), getMajor(), getSubDivisions());
      }
      else
      {
         grid = new JDRIsoGrid(grid.getCanvasGraphics(), getUnit(), getMajor(),
            getSubDivisions());
      }

      return grid;
   }

   public double getMajor()
   {
      double major = majorDivisionsModel.getNumber().doubleValue();

      return major <= 0.0 ? 1.0 : major;
   }

   @Override
   protected void setMajor(double value)
   {
      majorDivisionsModel.setValue(Double.valueOf(value));
   }

   public int getSubDivisions()
   {
      return Math.max(1, subDivisionsModel.getNumber().intValue());
   }

   @Override
   protected void setSubDivisions(int value)
   {
      subDivisionsModel.setValue(Integer.valueOf(value));
   }

   @Override
   public void setUnit(JDRUnit unit)
   {
      unitBox.setSelectedIndex(unit.getID());
   }

   @Override
   public JDRUnit getUnit()
   {
      return JDRUnit.getUnit(unitBox.getSelectedIndex()); 
   }

   @Override
   public void addUnitChangeListener(ItemListener listener)
   {
      unitBox.addItemListener(listener);
   }

   private JSpinner majorDivisionsSpinner, subDivisionsSpinner;
   private SpinnerNumberModel majorDivisionsModel, subDivisionsModel;
   private JComboBox<String> unitBox;
}
