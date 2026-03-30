/*
    Copyright (C) 2026 Nicola L.C. Talbot

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

import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.*;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdrresources.JDRResources;

/**
 * Panel for specifying a transformation matrix.
 * @author Nicola L C Talbot
 */

public class TransformationMatrixPanel extends JPanel
 implements ActionListener
{
   public TransformationMatrixPanel(JDRResources resources, String parentTag)
   {
      super(null);

      this.resources = resources;
      init(parentTag);
   }

   void init(String parentTag)
   {
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      setAlignmentX(0f);

      JLabel label = resources.createAppLabel(parentTag);
      add(label);

      add(Box.createVerticalStrut(10));

      matrixComp = new JPanel(new GridLayout(3, 3, 4, 4));
      matrixComp.setAlignmentX(0f);
      add(matrixComp);

      models = new SpinnerNumberModel[6];
      spinners = new JSpinner[6];

      for (int i = 0; i < models.length; i++)
      {
         models[i] = new SpinnerNumberModel(
            Double.valueOf(0), null, null, Double.valueOf(1));
         spinners[i] = new JSpinner(models[i]);
         resources.clampCompMaxHeight(spinners[i], 0, 0);
      }

      JLabelGroup labelGroup = new JLabelGroup();

      addToMatrixComp(labelGroup, spinners[0], parentTag+".m00");
      addToMatrixComp(labelGroup, spinners[2], parentTag+".m01");
      addToMatrixComp(labelGroup, spinners[4], parentTag+".m02");

      addToMatrixComp(labelGroup, spinners[1], parentTag+".m10");
      addToMatrixComp(labelGroup, spinners[3], parentTag+".m11");
      addToMatrixComp(labelGroup, spinners[5], parentTag+".m12");

      matrixComp.add(createFixedNumber("0"));
      matrixComp.add(createFixedNumber("0"));
      matrixComp.add(createFixedNumber("1"));

      resources.clampCompMax(matrixComp, 5, 5);

      add(Box.createVerticalStrut(10));

      JComponent row = new JPanel(new FlowLayout(FlowLayout.TRAILING));
      row.setAlignmentX(0f);
      add(row);

      row.add(resources.createAppButton(parentTag, "subset_to_identity", this));
      row.add(resources.createButtonSpacer());
      row.add(resources.createAppButton(parentTag, "set_to_identity", this));
      row.add(resources.createButtonSpacer());
      row.add(resources.createAppButton(parentTag, "reset", this));

      add(Box.createVerticalGlue());
   }

   void addToMatrixComp(JLabelGroup labelGrp, JSpinner element, String tag)
   {
      JComponent panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      matrixComp.add(panel);

      JLabel label = resources.createAppLabel(tag);
      labelGrp.add(label);
      label.setLabelFor(element);
      panel.add(label);
      panel.add(resources.createLabelSpacer());
      panel.add(element);

      JSpinner.NumberEditor editor = (JSpinner.NumberEditor)element.getEditor();
      JFormattedTextField field = editor.getTextField();
      field.setColumns(6);
   }

   JComponent createFixedNumber(String val)
   {
      JLabel label = new JLabel(val, SwingConstants.CENTER);
      return label;
   }

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("set_to_identity"))
      {
         models[0].setValue(Double.valueOf(1));
         models[1].setValue(Double.valueOf(0));
         models[2].setValue(Double.valueOf(0));
         models[3].setValue(Double.valueOf(1));
         models[4].setValue(Double.valueOf(0));
         models[5].setValue(Double.valueOf(0));
      }
      else if (action.equals("subset_to_identity"))
      {
         models[0].setValue(Double.valueOf(1));
         models[1].setValue(Double.valueOf(0));
         models[2].setValue(Double.valueOf(0));
         models[3].setValue(Double.valueOf(1));
      }
      else if (action.equals("reset"))
      {
         setMatrix(matrix);
      }
   }

   public void setMatrix(double[] matrix)
   {
      this.matrix = matrix;

      for (int i = 0; i < models.length; i++)
      {
         models[i].setValue(Double.valueOf(matrix[i]));
      }
   }

   public double[] getMatrix(double[] matrix)
   {
      if (matrix == null)
      {
         matrix = new double[models.length];
      }

      for (int i = 0; i < models.length; i++)
      {
         matrix[i] = models[i].getNumber().doubleValue();
      }

      return matrix;
   }

   JSpinner[] spinners;
   SpinnerNumberModel[] models;
   double[] matrix;
   JComponent matrixComp;
   JDRResources resources;
}
