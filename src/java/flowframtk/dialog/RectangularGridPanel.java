// File          : RectangularGridPanel.java
// Description   : Panel in which to specify rectangular grid settings
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

      Box row = Box.createHorizontalBox();
      add(row);

      JLabel majorLabel = resources.createAppLabel("grid.major");
      row.add(majorLabel);

      majorDivisions = new NonNegativeIntField(100);
      majorLabel.setLabelFor(majorDivisions);
      row.add(majorDivisions);

      unitBox = new JComboBox<String>(JDRUnit.UNIT_LABELS);
      unitBox.setSelectedIndex(JDRUnit.BP);
      row.add(unitBox);

      row = Box.createHorizontalBox();
      add(row);

      JLabel subdivisionsLabel = resources.createAppLabel("grid.sub_divisions");

      row.add(subdivisionsLabel);

      subDivisions = new NonNegativeIntField(10);
      subdivisionsLabel.setLabelFor(subDivisions);

      row.add(subDivisions);

      row.add(Box.createHorizontalStrut(
         (int)unitBox.getPreferredSize().getWidth()));

      Dimension majorLabelDim = majorLabel.getPreferredSize();

      Dimension subDivLabelDim = subdivisionsLabel.getPreferredSize();

      int maxWidth = 
        (int)Math.max(majorLabelDim.getWidth(), subDivLabelDim.getWidth());

      majorLabelDim.width = maxWidth;
      subDivLabelDim.width = maxWidth;

      majorLabel.setPreferredSize(majorLabelDim);
      subdivisionsLabel.setPreferredSize(subDivLabelDim);

      Dimension dim = majorDivisions.getPreferredSize();
      dim.width = Integer.MAX_VALUE;
      majorDivisions.setMaximumSize(dim);

      dim = unitBox.getPreferredSize();
      unitBox.setMaximumSize(dim);

      dim = subDivisions.getPreferredSize();
      dim.width = Integer.MAX_VALUE;
      subDivisions.setMaximumSize(dim);

      add(Box.createVerticalGlue());
   }

   public void requestDefaultFieldFocus()
   {
      majorDivisions.requestFocusInWindow();
   }

   public void setGrid(JDRGrid grid)
   {
      majorDivisions.setValue(
         (int)((JDRRectangularGrid)grid).getMajorInterval());
      subDivisions.setValue(((JDRRectangularGrid)grid).getSubDivisions());
      setUnit(((JDRRectangularGrid)grid).getUnit());
   }

   public JDRGrid getGrid(JDRGrid grid)
   {
      if (grid instanceof JDRRectangularGrid)
      {
         JDRRectangularGrid g = (JDRRectangularGrid)grid;

         g.set(getUnit(), getMajor(), getSubDivisions());
      }
      else
      {
         grid = new JDRRectangularGrid(grid.getCanvasGraphics(),
            getUnit(), getMajor(), getSubDivisions());
      }

      return grid;
   }

   public int getMajor()
   {
      int d = majorDivisions.getInt();
      if (d == 0) d = 1;

      return d;
   }

   public int getSubDivisions()
   {
      int d = subDivisions.getInt();
      if (d == 0) d = 1;

      return d;
   }

   public void setUnit(JDRUnit unit)
   {
      unitBox.setSelectedIndex(unit.getID());
   }

   public JDRUnit getUnit()
   {
      return JDRUnit.getUnit(unitBox.getSelectedIndex()); 
   }

   private NonNegativeIntField majorDivisions, subDivisions;
   private JComboBox<String> unitBox;
}
