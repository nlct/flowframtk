// File          : MarginPanel.java
// Description   : Panel for specifying margins
// Creation Date : 6th February 2006
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
import java.awt.geom.Rectangle2D;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.FlowframTk;
import com.dickimawbooks.flowframtk.JDRFrame;

/**
 * Panel for specifying margins.
 * @author Nicola L C Talbot
 */

public class MarginPanel extends JPanel implements ActionListener
{
   public MarginPanel(FlowframTk application)
   {
      this(application, application.getResources().getMessage("flowframe.margins"), false);
   }

   public MarginPanel(FlowframTk application, String labelText, boolean addNoneBox)
   {
      super(new BorderLayout());

      this.application = application;
      JDRResources resources = application.getResources();

      JComponent topPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

      add(topPanel, "North");

      bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

      add(bottomPanel, "South");

      lengthComp = Box.createVerticalBox();
      add(lengthComp, "Center");

      label = new JLabel(labelText);

      topPanel.add(label);

      if (addNoneBox)
      {
         topPanel.add(resources.createButtonSpacer());

         noneBox = resources.createAppCheckBox("flowframe.margins", "none", false, null);
         noneBox.addChangeListener(new ChangeListener()
          {
             @Override
             public void stateChanged(ChangeEvent evt)
             {
                lengthComp.setVisible(!noneBox.isSelected());
                bottomPanel.setVisible(lengthComp.isVisible());

                if (noneBox.isSelected())
                {
                   updateWidth();
                   updateHeight();
                }
             }
          });

         topPanel.add(noneBox);
      }

      JLabelGroup labelGroup = new JLabelGroup();

      JComponent row = new JPanel(new FlowLayout(FlowLayout.LEADING));
      lengthComp.add(row);

      leftLabel = resources.createAppLabel("flowframe.margins.left");
      labelGroup.add(leftLabel);

      row.add(leftLabel);
      row.add(resources.createLabelSpacer());

      leftText = resources.createNonNegativeLengthPanel();
      leftLabel.setLabelFor(leftText);
      adjustField(leftText);

      row.add(leftText);

      row.add(resources.createButtonSpacer());

      rightLabel = resources.createAppLabel("flowframe.margins.right");
      labelGroup.add(rightLabel);

      row.add(rightLabel);
      row.add(resources.createLabelSpacer());

      rightText = resources.createNonNegativeLengthPanel();
      rightLabel.setLabelFor(rightText);
      adjustField(rightText);

      row.add(rightText);

      row.add(resources.createButtonSpacer());

      row.add(
         resources.createDialogButton("flowframe.margins", "xbalance", this, null));

      row = new JPanel(new FlowLayout(FlowLayout.LEADING));
      lengthComp.add(row);

      topLabel = resources.createAppLabel("flowframe.margins.top");
      labelGroup.add(topLabel);

      row.add(topLabel);
      row.add(resources.createLabelSpacer());

      topText = resources.createNonNegativeLengthPanel();
      topLabel.setLabelFor(topText);
      adjustField(topText);
      row.add(topText);

      row.add(resources.createButtonSpacer());

      bottomLabel = resources.createAppLabel("flowframe.margins.bottom");
      labelGroup.add(bottomLabel);

      row.add(bottomLabel);
      row.add(resources.createLabelSpacer());

      bottomText = resources.createNonNegativeLengthPanel();
      bottomLabel.setLabelFor(bottomText);
      adjustField(bottomText);
      row.add(bottomText);

      row.add(resources.createButtonSpacer());

      row.add(
         resources.createDialogButton("flowframe.margins", "ybalance", this, null));

      bottomPanel.add(resources.createAppLabel("flowframe.margins.width"));

      bottomPanel.add(resources.createLabelSpacer());

      widthField = resources.createAppInfoField(12);
      bottomPanel.add(widthField);

      bottomPanel.add(resources.createButtonSpacer());

      bottomPanel.add(resources.createAppLabel("flowframe.margins.height"));

      bottomPanel.add(resources.createLabelSpacer());

      heightField = resources.createAppInfoField(12);
      bottomPanel.add(heightField);

      ChangeListener widthListener = new ChangeListener()
       {
         @Override
         public void stateChanged(ChangeEvent evt)
         {
            updateWidth();
         }
       };

      leftText.addChangeListener(widthListener);
      rightText.addChangeListener(widthListener);

      ChangeListener heightListener = new ChangeListener()
       {
         @Override
         public void stateChanged(ChangeEvent evt)
         {
            updateHeight();
         }
       };

      topText.addChangeListener(heightListener);
      bottomText.addChangeListener(heightListener);
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

      if (noneBox != null && left == 0.0 && right == 0.0
           && top == 0.0 && bottom == 0.0)
      {
         noneBox.setSelected(true);
      }
   }

   public void setMargins(JDRLength left, JDRLength right,
                          JDRLength top, JDRLength bottom)
   {
      leftText.setLength(left);
      rightText.setLength(right);
      topText.setLength(top);
      bottomText.setLength(bottom);

      if (noneBox != null && left.getValue() == 0.0 && right.getValue() == 0.0
           && top.getValue() == 0.0 && bottom.getValue() == 0.0)
      {
         noneBox.setSelected(true);
      }
   }

   public void requestDefaultComponentFocus()
   {
      if (isNoneOn())
      {
         noneBox.requestFocusInWindow();
      }
      else
      {
         leftText.getTextField().requestFocusInWindow();
      }
   }

   public boolean isAllUnit(JDRUnit unit)
   {
      return leftText.getUnit().equals(unit)
          && rightText.getUnit().equals(unit)
          && topText.getUnit().equals(unit)
          && bottomText.getUnit().equals(unit);
   }

   public double left(JDRUnit unit)
   {
      return isNoneOn() ? 0.0 : leftText.getValue(unit);
   }

   public double right(JDRUnit unit)
   {
      return isNoneOn() ? 0.0 : rightText.getValue(unit);
   }

   public double top(JDRUnit unit)
   {
      return isNoneOn() ? 0.0 : topText.getValue(unit);
   }

   public double bottom(JDRUnit unit)
   {
      return isNoneOn() ? 0.0 : bottomText.getValue(unit);
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

      if (noneBox != null)
      {
         noneBox.setEnabled(flag);
      }
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

   public void setNone(boolean on)
   {
      if (noneBox != null)
      {
         noneBox.setSelected(on);
      }
   }

   public boolean isNoneOn()
   {
      return noneBox != null && noneBox.isSelected();
   }

   public void addChangeListener(ChangeListener listener)
   {
      leftText.addChangeListener(listener);
      rightText.addChangeListener(listener);
      topText.addChangeListener(listener);
      bottomText.addChangeListener(listener);

      if (noneBox != null)
      {
         noneBox.addChangeListener(listener);
      }
   }

   protected void updateWidth()
   {
      if (refBounds != null)
      {
         JDRUnit unit = rightText.getUnit();

         double refWidth = unit.fromUnit(refBounds.getWidth(), refUnit);

         double width = refWidth
                      - rightText.getValue(unit)
                      - leftText.getValue(unit);

         widthField.setText(String.format("%f%s", width, unit.getLabel()));
      }
      else
      {
         widthField.setText("");
      }
   }

   protected void updateHeight()
   {
      if (refBounds != null)
      {
         JDRUnit unit = bottomText.getUnit();

         double refHeight = unit.fromUnit(refBounds.getHeight(), refUnit);

         double height = refHeight
                       - topText.getValue(unit)
                       - bottomText.getValue(unit);

         heightField.setText(String.format("%f%s", height, unit.getLabel()));
      }
      else
      {
         heightField.setText("");
      }
   }

   public void setReferenceBounds(Rectangle2D bounds, JDRUnit unit)
   {
      refBounds = bounds;
      refUnit = unit;
   }

   public void setReferenceBounds(BBox box, JDRUnit unit)
   {
      refBounds = new Rectangle2D.Double(box.getMinX(), box.getMinY(),
        box.getWidth(), box.getHeight());

      refUnit = unit;
   }

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action.equals("xbalance"))
      {
         JDRUnit unit = rightText.getUnit();
         double x = 0.5*(rightText.getValue(unit) + leftText.getValue(unit));

         leftText.setValue(x, unit);
         rightText.setValue(x, unit);
      }
      else if (action.equals("ybalance"))
      {
         JDRUnit unit = bottomText.getUnit();
         double y = 0.5*(bottomText.getValue(unit) + topText.getValue(unit));

         topText.setValue(y, unit);
         bottomText.setValue(y, unit);
      }
   }

   private JComponent lengthComp;
   private NonNegativeLengthPanel leftText, rightText, topText, bottomText;
   private JLabel label, leftLabel, rightLabel, topLabel, bottomLabel;
   private JCheckBox noneBox;
   private JTextField widthField, heightField;
   private JComponent bottomPanel;
   private FlowframTk application;

   private Rectangle2D refBounds;
   private JDRUnit refUnit;
}
