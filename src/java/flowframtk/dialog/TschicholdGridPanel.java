// File          : TschicholdGridPanel.java
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
public class TschicholdGridPanel extends GridPanel
{
   public TschicholdGridPanel(JDRResources resources)
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
         Integer.valueOf(100), Integer.valueOf(1), null, Integer.valueOf(1));
      majorDivisionsSpinner = new JSpinner(majorDivisionsModel);
      majorLabel.setLabelFor(majorDivisionsSpinner);
      row.add(majorDivisionsSpinner);

      unitBox = new JComboBox<String>(JDRUnit.UNIT_LABELS);
      unitBox.setSelectedIndex(JDRUnit.PT);
      row.add(unitBox);

      row.add(Box.createHorizontalGlue());

      row = Box.createHorizontalBox();
      add(row);

      JLabel subdivisionsLabel = resources.createAppLabel(
         "grid.sub_divisions");

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

      Dimension dim = majorDivisionsSpinner.getPreferredSize();
      dim.width = Integer.MAX_VALUE;
      majorDivisionsSpinner.setMaximumSize(dim);

      dim = unitBox.getPreferredSize();
      unitBox.setMaximumSize(dim);

      dim = subDivisionsSpinner.getPreferredSize();
      dim.width = Integer.MAX_VALUE;
      subDivisionsSpinner.setMaximumSize(dim);

      add(resources.createAppInfoArea("grid.tschichold.info"));

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
      setMajor(
         (int)((JDRTschicholdGrid)grid).getMajorInterval());
      setSubDivisions(((JDRTschicholdGrid)grid).getSubDivisions());
      setUnit(((JDRTschicholdGrid)grid).getUnit());
   }

   @Override
   public JDRGrid getGrid(JDRGrid grid)
   {
      if (grid instanceof JDRTschicholdGrid)
      {
         JDRTschicholdGrid g = (JDRTschicholdGrid)grid;

         g.set(getUnit(), getMajor(), getSubDivisions());
      }
      else
      {
         grid = new JDRTschicholdGrid(grid.getCanvasGraphics(),
            getUnit(), getMajor(), getSubDivisions());
      }

      return grid;
   }

   public int getMajor()
   {
      int d = majorDivisionsModel.getNumber().intValue();
      if (d == 0) d = 1;

      return d;
   }

   @Override
   protected void setMajor(int value)
   {
      majorDivisionsModel.setValue(Integer.valueOf(value));
   }

   public int getSubDivisions()
   {
      int d = subDivisionsModel.getNumber().intValue();
      if (d == 0) d = 1;

      return d;
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
