// File          : ShearDialogBox.java
// Description   : Dialog box for shearing objects
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

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for shearing objects.
 */

public class ShearDialogBox extends JDialog
   implements ActionListener
{
   public ShearDialogBox(FlowframTk application)
   {
      super(application, 
        application.getResources().getMessage("shear.title"), true);
      application_ = application;

      JDRResources resources = application.getResources();

      JComponent mainComp = Box.createVerticalBox();
      mainComp.setAlignmentX(0.0f);

      getContentPane().add(mainComp, "Center");

      JComponent row = createRow();
      mainComp.add(row);

      JLabel xLabel = resources.createAppLabel("shear.x");
      row.add(xLabel);

      row.add(resources.createLabelSpacer());

      shearXSpinnerModel = new SpinnerNumberModel(
         Double.valueOf(0.0), null, null, Double.valueOf(0.25));

      shearXSpinner = new JSpinner(shearXSpinnerModel);
      setSpinnerColumns(shearXSpinner, 6);

      xLabel.setLabelFor(shearXSpinner);
      row.add(shearXSpinner);

      row = createRow();
      mainComp.add(row);

      JLabel yLabel = resources.createAppLabel("shear.y");
      row.add(yLabel);

      row.add(resources.createLabelSpacer());

      shearYSpinnerModel = new SpinnerNumberModel(
         Double.valueOf(0.0), null, null, Double.valueOf(0.25));

      shearYSpinner = new JSpinner(shearYSpinnerModel);
      setSpinnerColumns(shearYSpinner, 6);

      yLabel.setLabelFor(shearYSpinner);
      row.add(shearYSpinner);

      mainComp.add(Box.createVerticalStrut(10));

      row = createRow();
      mainComp.add(row);

      anchorXComp = new AnchorXPanel(application);
      anchorXComp.setSelectedAnchor(AnchorX.LEFT);
      row.add(anchorXComp);

      row.add(resources.createButtonSpacer());

      anchorYComp = new AnchorYPanel(application);
      anchorYComp.setSelectedAnchor(AnchorY.BOTTOM);
      row.add(anchorYComp);

      mainComp.add(Box.createVerticalStrut(10));

      JPanel btnPanel = new JPanel();

      resources.createOkayCancelHelpButtons(this, btnPanel, this, "sec:shearobjects");

      getContentPane().add(btnPanel, "South");

      pack();
      setLocationRelativeTo(application);
   }

   protected JComponent createRow()
   {
      JComponent row = Box.createHorizontalBox();
      row.setAlignmentX(0.0f);

      return row;
   }

   protected void requestSpinnerFocus(JSpinner spinner)
   {
      JComponent editor = spinner.getEditor();

      if (editor instanceof JSpinner.DefaultEditor)
      {
         ((JSpinner.DefaultEditor)editor).getTextField().requestFocusInWindow();
      }
      else
      {
         spinner.requestFocusInWindow();
      }
   }

   protected void setSpinnerColumns(JSpinner spinner, int cols)
   {
      JComponent editor = spinner.getEditor();

      if (editor instanceof JSpinner.DefaultEditor)
      {
         ((JSpinner.DefaultEditor)editor).getTextField().setColumns(cols);
      }
   }

   public void display()
   {
      requestSpinnerFocus(shearXSpinner);
      setVisible(true);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("okay"))
      {
         okay();
      }
      else if (action.equals("cancel"))
      {
         setVisible(false);
      }
   }

   public void okay()
   {
      setVisible(false);

      application_.getCurrentFrame().shearSelectedPaths(
          shearXSpinnerModel.getNumber().doubleValue(), 
          shearYSpinnerModel.getNumber().doubleValue(),
          anchorXComp.getSelectedAnchor(),
          anchorYComp.getSelectedAnchor());
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "ShearDialogBox:"+eol;
      str += "has focus: "+hasFocus()+eol;
      str += "shear x field has focus: "+shearXSpinner.hasFocus()+eol;
      str += "shear y field has focus: "+shearYSpinner.hasFocus()+eol;

      ActionMap actionMap = getRootPane().getActionMap();
      str += "action map: "+eol;

      Object[] allKeys = actionMap.allKeys();

      for (int i = 0; i < allKeys.length; i++)
      {
         str += "Key: "+allKeys[i]+" Action: "+actionMap.get(allKeys[i])+eol;
      }

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;

   private SpinnerNumberModel shearXSpinnerModel, shearYSpinnerModel;
   private JSpinner shearXSpinner, shearYSpinner;

   private AnchorXPanel anchorXComp;
   private AnchorYPanel anchorYComp;
}

