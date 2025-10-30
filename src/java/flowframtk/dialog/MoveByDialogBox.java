// File          : MoveByDialogBox.java
// Description   : Dialog box used to move objects.
// Creation Date : 1st February 2006
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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box in which to specify how far to move objects.
 * @author Nicola L C Talbot
 */
public class MoveByDialogBox extends JDialog
   implements ActionListener
{
   public MoveByDialogBox(FlowframTk application)
   {
      super(application, application.getResources().getMessage("moveby.title"),
            true);
      application_ = application;
      JDRResources resources = application.getResources();

      JComponent mainComp = Box.createVerticalBox();
      getContentPane().add(mainComp, "Center");

      locationPane = new RectangularCoordPanel(getResources());
      mainComp.add(locationPane);

      JComponent row = Box.createHorizontalBox();
      mainComp.add(row);

      row.add(resources.createAppLabel("moveby.calculate"));
      row.add(Box.createHorizontalGlue());

      row = Box.createHorizontalBox();
      mainComp.add(row);

      row.add(resources.createButtonSpacer());

      widthNumberModel = new SpinnerNumberModel(50, -1000, 1000, 1);
      widthSpinner = new JSpinner(widthNumberModel);

      row.add(widthSpinner);

      JLabelGroup labelGroup = new JLabelGroup();

      JLabel label = resources.createAppLabel("moveby.percent_width");
      label.setLabelFor(widthSpinner);
      row.add(label);
      labelGroup.add(label);

      row.add(resources.createButtonSpacer());

      row.add(resources.createDialogButton("moveby", "calc_x", this, null));
      row.add(Box.createHorizontalGlue());

      row = Box.createHorizontalBox();
      mainComp.add(row);

      row.add(resources.createButtonSpacer());

      heightNumberModel = new SpinnerNumberModel(50, -1000, 1000, 1);
      heightSpinner = new JSpinner(heightNumberModel);

      row.add(heightSpinner);

      label = resources.createAppLabel("moveby.percent_height");
      label.setLabelFor(heightSpinner);
      row.add(label);
      labelGroup.add(label);

      row.add(resources.createButtonSpacer());

      row.add(resources.createDialogButton("moveby", "calc_y", this, null));
      row.add(Box.createHorizontalGlue());

      JPanel p2 = new JPanel();

      resources.createOkayCancelHelpButtons(this, p2, this, "sec:moveobjects");

      getContentPane().add(p2, "South");

      pack();
      setLocationRelativeTo(application_);
   }

   public void initialise()
   {
      JDRFrame prevFrame = mainPanel;

      mainPanel = application_.getCurrentFrame();

      if (mainPanel != prevFrame)
      {
         locationPane.setUnit(mainPanel.getUnit());
      }

      setVisible(true);
   }

   public void okay()
   {
      mainPanel.getCanvas().moveSelectedObjects
        (locationPane.getXCoord(), locationPane.getYCoord());

      setVisible(false);
   }

   public void actionPerformed(ActionEvent e)
   {
      String action = e.getActionCommand();

      if (action == null) return;

      if (action.equals("okay"))
      {
         okay();
      } 
      else if (action.equals("cancel"))
      {
         setVisible(false);
      }
      else if (action.equals("calc_x"))
      {
         int pc = widthNumberModel.getNumber().intValue();
         double bpW = mainPanel.getBpPaperWidth();

         JDRUnit unit = locationPane.getUnitX();

         if (pc == 1)
         {
            locationPane.setXCoord(unit.fromBp(bpW), unit);
         }
         else if (pc == 0)
         {
            locationPane.setXCoord(0.0, unit);
         }
         else
         {
            locationPane.setXCoord(unit.fromBp(0.01*pc * bpW), unit);
         }
      }
      else if (action.equals("calc_y"))
      {
         int pc = heightNumberModel.getNumber().intValue();
         double bpH = mainPanel.getBpPaperHeight();

         JDRUnit unit = locationPane.getUnitY();

         if (pc == 1)
         {
            locationPane.setYCoord(unit.fromBp(bpH), unit);
         }
         else if (pc == 0)
         {
            locationPane.setYCoord(0.0, unit);
         }
         else
         {
            locationPane.setYCoord(unit.fromBp(0.01*pc * bpH), unit);
         }
      }
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "MoveByDialog:"+eol;
      str += "has focus: "+hasFocus()+eol;

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;
   RectangularCoordPanel locationPane;

   SpinnerNumberModel widthNumberModel, heightNumberModel;
   JSpinner widthSpinner, heightSpinner;

   private JDRFrame mainPanel = null;
}
