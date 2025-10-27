// File          : RadialGridPanel.java
// Description   : Panel in which to specify radial grid settings
// Creation Date : 14th Sept 2010
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
 * Panel in which to specify radial grid settings.
 * @author Nicola L C Talbot
 */
public class RadialGridPanel extends GridPanel
{
   public RadialGridPanel(JDRResources resources)
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
      unitBox.setSelectedIndex(JDRUnit.BP);
      row.add(unitBox);

      row.add(Box.createHorizontalGlue());

      int strut = (int)unitBox.getPreferredSize().getWidth();

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

      row.add(Box.createHorizontalStrut(strut));

      row.add(Box.createHorizontalGlue());

      row = Box.createHorizontalBox();
      add(row);

      spokesLabel = resources.createAppLabel("grid.spokes");
      row.add(spokesLabel);
      labelGroup.add(spokesLabel);

      row.add(resources.createLabelSpacer());

      spokesModel = new SpinnerNumberModel(
         Integer.valueOf(8), Integer.valueOf(1), null, Integer.valueOf(1));
      spokesSpinner = new JSpinner(spokesModel);
      spokesLabel.setLabelFor(spokesSpinner);
      row.add(spokesSpinner);

      row.add(Box.createHorizontalStrut(strut));

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

      dim = spokesSpinner.getPreferredSize();
      dim.width = Integer.MAX_VALUE;
      spokesSpinner.setMaximumSize(dim);

      add(Box.createVerticalStrut(3*height));

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
      setMajor((int)((JDRRadialGrid)grid).getMajorInterval());
      setSubDivisions(((JDRRadialGrid)grid).getSubDivisions());
      setUnit(((JDRRadialGrid)grid).getUnit());
      setSpokes(((JDRRadialGrid)grid).getSpokes());
   }

   @Override
   public JDRGrid getGrid(JDRGrid grid)
   {
      if (grid instanceof JDRRadialGrid)
      {
         JDRRadialGrid g = (JDRRadialGrid)grid;
         g.set(getUnit(), getMajor(), getSubDivisions(), getSpokes());
      }
      else
      {
         grid = new JDRRadialGrid(grid.getCanvasGraphics(), 
            getUnit(), getMajor(), getSubDivisions(), getSpokes());
      }

      return grid;
   }

   public void setSpokes(int spokes)
   {
      spokesModel.setValue(Integer.valueOf(spokes));
   }

   @Override
   protected void setSubDivisions(int value)
   {
      subDivisionsModel.setValue(Integer.valueOf(value));
   }

   @Override
   protected void setMajor(int value)
   {
      majorDivisionsModel.setValue(Integer.valueOf(value));
   }

   public int getSpokes()
   {
      int s = spokesModel.getNumber().intValue();

      if (s == 0) s = 1;

      return s;
   }

   public int getMajor()
   {
      int d = majorDivisionsModel.getNumber().intValue();

      if (d == 0) d = 1;

      return d;
   }

   public int getSubDivisions()
   {
      int d = subDivisionsModel.getNumber().intValue();
      if (d == 0) d = 1;

      return d;
   }

   @Override
   public void setUnit(JDRUnit unit)
   {
      unitBox.setSelectedIndex(unit.getID());
   }

   public JDRUnit getUnit()
   {
      return JDRUnit.getUnit(unitBox.getSelectedIndex());
   }

   private JSpinner majorDivisionsSpinner, subDivisionsSpinner, spokesSpinner;
   private SpinnerNumberModel majorDivisionsModel, subDivisionsModel, spokesModel;
   private JComboBox<String> unitBox; 
   private JLabel spokesLabel;
}
