// File          : MarginPanel.java
// Description   : Panel for specifying margins
// Creation Date : 6th February 2006
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
package com.dickimawbooks.flowframtk.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Panel for specifying margins.
 * @author Nicola L C Talbot
 */

public class MarginPanel extends JPanel
{
   public MarginPanel(JDRResources resources)
   {
      super();

      setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();

      gbc.gridwidth  = 1;
      gbc.gridheight = 1;
      gbc.gridx      = 0;
      gbc.gridy      = 0;
      gbc.weightx    = 100;
      gbc.weighty    = 100;
      gbc.anchor     = GridBagConstraints.EAST;
      gbc.fill       = GridBagConstraints.HORIZONTAL;

      label = resources.createAppLabel("flowframe.margins");
      add(label, gbc);

      gbc.gridx++;
      gbc.gridy++;

      leftLabel = resources.createAppLabel("flowframe.margin.left");

      add(leftLabel, gbc);

      gbc.gridx++;

      leftText = resources.createNonNegativeLengthPanel();
      leftLabel.setLabelFor(leftText);
      adjustField(leftText);

      add(leftText, gbc);

      gbc.gridx++;

      rightLabel = resources.createAppLabel("flowframe.margin.right");

      add(rightLabel, gbc);

      gbc.gridx++;

      rightText = resources.createNonNegativeLengthPanel();
      rightLabel.setLabelFor(rightText);
      adjustField(rightText);

      add(rightText, gbc);

      gbc.gridx = 1;
      gbc.gridy++;

      topLabel = resources.createAppLabel("flowframe.margin.top");

      add(topLabel, gbc);

      gbc.gridx++;

      topText = resources.createNonNegativeLengthPanel();
      topLabel.setLabelFor(topText);
      adjustField(topText);
      add(topText, gbc);

      gbc.gridx++;

      bottomLabel = resources.createAppLabel("flowframe.margin.bottom");

      add(bottomLabel, gbc);

      gbc.gridx++;

      bottomText = resources.createNonNegativeLengthPanel();
      bottomLabel.setLabelFor(bottomText);
      adjustField(bottomText);
      add(bottomText, gbc);
   }

   private void adjustField(LengthPanel panel)
   {
      JTextField field = panel.getTextField();
      field.setMinimumSize(field.getPreferredSize());
   }

   public void setMargins(JDRUnit unit,
                          double left, double right,
                          double top, double bottom)
   {
      leftText.setValue(left, unit);
      rightText.setValue(right, unit);
      topText.setValue(top, unit);
      bottomText.setValue(bottom, unit);
   }

   public void setMargins(JDRLength left, JDRLength right,
                          JDRLength top, JDRLength bottom)
   {
      leftText.setLength(left);
      rightText.setLength(right);
      topText.setLength(top);
      bottomText.setLength(bottom);
   }

   public double left(JDRUnit unit)
   {
      return leftText.getValue(unit);
   }

   public double right(JDRUnit unit)
   {
      return rightText.getValue(unit);
   }

   public double top(JDRUnit unit)
   {
      return topText.getValue(unit);
   }

   public double bottom(JDRUnit unit)
   {
      return bottomText.getValue(unit);
   }

   public void setEnabled(boolean flag)
   {
      label.setEnabled(flag);
      leftText.setEnabled(flag);
      rightText.setEnabled(flag);
      topText.setEnabled(flag);
      bottomText.setEnabled(flag);
      leftLabel.setEnabled(flag);
      rightLabel.setEnabled(flag);
      topLabel.setEnabled(flag);
      bottomLabel.setEnabled(flag);
   }

   public void setUnit(JDRUnit unit)
   {
      leftText.setUnit(unit);
      rightText.setUnit(unit);
      topText.setUnit(unit);
      bottomText.setUnit(unit);
   }

   public void setRight(double right, JDRUnit unit)
   {
      rightText.setValue(right, unit);
   }

   public void setLeft(double left, JDRUnit unit)
   {
      leftText.setValue(left, unit);
   }

   public void setTop(double top, JDRUnit unit)
   {
      topText.setValue(top, unit);
   }

   public void setBottom(double bottom, JDRUnit unit)
   {
      bottomText.setValue(bottom, unit);
   }

   private NonNegativeLengthPanel leftText, rightText, topText, bottomText;
   private JLabel label, leftLabel, rightLabel, topLabel, bottomLabel;
}
