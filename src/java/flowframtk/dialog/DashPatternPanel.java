// File          : DashPatternPanel.java
// Description   : Panel for selecting solid/dash pattern
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

/**
 * Panel for selecting solid or dash.
 * @author Nicola L C Talbot
 */

public class DashPatternPanel extends JPanel
   implements ActionListener
{
   public DashPatternPanel(JDRSelector selector)
   {
      super();

      selector_ = selector;

      setLayout(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.weightx = 0;
      constraints.weighty = 100;
      constraints.gridwidth  = 1;
      constraints.gridheight = 1;
      constraints.anchor = GridBagConstraints.WEST;

      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.gridheight = 1;
      constraints.anchor = GridBagConstraints.NORTHWEST;

      ButtonGroup dashedGroup = new ButtonGroup();

      solidLine = getResources().createAppRadioButton("linestyle", 
        "solid", dashedGroup, true, this);
      add(solidLine, constraints);

      dashedLine = getResources().createAppRadioButton("linestyle", 
        "dashed", dashedGroup, false, this);

      constraints.gridx = 1;
      add(dashedLine, constraints);

      dashPatternBox = new DashPatternBox(
         selector_.getSamplePathPanel(), getResources());
      constraints.gridx = 2;
      constraints.gridwidth = 3;
      add(dashPatternBox, constraints);

   }

   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();

      if (source == dashedLine)
      {
         dashPatternBox.setEnabled(true);
      }
      else if (source == solidLine)
      {
         dashPatternBox.setEnabled(false);
      }
      selector_.repaintSample();
   }

   public void setStroke(JDRBasicStroke stroke)
   {
      setDashPattern(stroke.dashPattern);
   }

   public DashPattern getDashPattern(CanvasGraphics cg)
   {
      if (solidLine.isSelected())
      {
         return new DashPattern(cg, null);
      }
      else
      {
         return dashPatternBox.getValue(cg);
      }
   }

   public void setDashPattern(DashPattern dp)
   {
      if (dp.getStoragePattern() == null)
      {
         solidLine.setSelected(true);
         dashPatternBox.setEnabled(false);
      }
      else
      {
         dashedLine.setSelected(true);
         dashPatternBox.setEnabled(true);
         dashPatternBox.setValue(dp);
      }
   }

   public void setDefaults()
   {
      setDashPattern(new DashPattern(selector_.getCanvasGraphics(), 
         null));
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   // line style
   private DashPatternBox dashPatternBox;
   private JRadioButton solidLine, dashedLine;

   private JDRSelector selector_;
}
