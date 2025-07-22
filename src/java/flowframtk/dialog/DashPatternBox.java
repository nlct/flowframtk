// File          : DashPatternBoxPanel.java
// Description   : Panel for selecting dash pattern
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
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Panel for selecting dash pattern.
 * @author Nicola L C Talbot
 */

class DashPatternBox extends JPanel implements ActionListener
{
   public DashPatternBox(SamplePanel panel, JDRResources resources)
   {
      super();
      samplePanel = panel;

      setLayout(new GridLayout(2, 3));

      offsetPanel = resources.createNonNegativeLengthPanel("dash.offset");
      offsetPanel.getDocument().addDocumentListener(
         new TextFieldSampleListener(panel));
      offsetPanel.getTextField().setColumns(2);
      add(offsetPanel);

      dashPanel = resources.createNonNegativeLengthPanel("dash.label");
      dashPanel.getDocument().addDocumentListener(
         new TextFieldSampleListener(panel));
      dashPanel.getTextField().setColumns(2);
      dashPanel.setValue(10.0, JDRUnit.bp);
      add(dashPanel);

      gapPanel = resources.createNonNegativeLengthPanel("dash.gap");
      gapPanel.getDocument().addDocumentListener(
         new TextFieldSampleListener(panel));
      gapPanel.getTextField().setColumns(2);
      gapPanel.setValue(5.0, JDRUnit.bp);
      add(gapPanel);

      secondary = resources.createAppCheckBox("dash", "secondary", false, this);
      add(secondary);

      dash2Panel = resources.createNonNegativeLengthPanel("dash.label");
      dash2Panel.getDocument().addDocumentListener(
         new TextFieldSampleListener(panel));
      dash2Panel.getTextField().setColumns(2);
      dash2Panel.setValue(1.0, JDRUnit.bp);
      add(dash2Panel);

      gap2Panel = resources.createNonNegativeLengthPanel("dash.gap");
      gap2Panel.getDocument().addDocumentListener(
         new TextFieldSampleListener(panel));
      gap2Panel.getTextField().setColumns(2);
      gap2Panel.setValue(5.0, JDRUnit.bp);
      add(gap2Panel);

      setSecondaryDash(false);
   }

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource();

      if (source == secondary)
      {
         enableSecondaryDash(secondary.isSelected());
         samplePanel.updateSamples();
      }
   }

   public void setEnabled(boolean flag)
   {
      dashPanel.setEnabled(flag);
      gapPanel.setEnabled(flag);
      offsetPanel.setEnabled(flag);
      secondary.setEnabled(flag);

      if (flag == false)
      {
         secondary.setSelected(false);
         enableSecondaryDash(false);
      }
      else
      {
         dashPanel.getTextField().requestFocusInWindow();
      }
   }

   public DashPattern getValue(CanvasGraphics cg)
   {
      JDRUnit unit = cg.getStorageUnit();

      float dash = (float)dashPanel.getValue(unit);
      float gap  = (float)gapPanel.getValue(unit);

      if (dash == 0.0f && gap == 0.0f)
      {
         dash = (float)unit.fromBp(1.0);
      }

      if (secondary.isSelected())
      {
         float dash2 = (float)dash2Panel.getValue(unit);
         float gap2  = (float)gap2Panel.getValue(unit);

         if (dash2 == 0.0f && gap2 == 0.0f)
         {
            dash2 = (float)unit.fromBp(1.0);
         }

         return new DashPattern(cg, new float[] {dash, gap, dash2, gap2},
                                (float)offsetPanel.getValue(unit));
      }
      else
      {
         return new DashPattern(cg, new float[] {dash, gap},
                                (float)offsetPanel.getValue(unit));
      }
   }

   public void setValue(DashPattern dp)
   {
      setSecondaryDash(false);

      JDRUnit unit = dp.getCanvasGraphics().getStorageUnit();

      float[] pattern = dp.getStoragePattern();

      if (pattern == null)
      {
         dashPanel.setValue(unit.fromBp(1.0), unit);
         gapPanel.setValue(0.0, unit);
      }
      else if (pattern.length == 1)
      {
         dashPanel.setValue(pattern[0], unit);
         gapPanel.setValue(0.0, unit);
      }
      else
      {
         dashPanel.setValue(pattern[0], unit);
         gapPanel.setValue(pattern[1], unit);

         if (pattern.length >= 4)
         {
            dash2Panel.setValue(pattern[2], unit);
            gap2Panel.setValue(pattern[3], unit);
            setSecondaryDash(true);
         }
      }

      offsetPanel.setValue(dp.getStorageOffset(), unit);
   }

   public void setSecondaryDash(boolean flag)
   {
      secondary.setSelected(flag);
      enableSecondaryDash(flag);
   }

   public void enableSecondaryDash(boolean flag)
   {
      dash2Panel.setEnabled(flag);
      gap2Panel.setEnabled(flag);
   }

   private NonNegativeLengthPanel dashPanel, gapPanel, offsetPanel,
      dash2Panel, gap2Panel;
   private JCheckBox secondary;
   private SamplePanel samplePanel;
}
