// File          : ScaleDialogBox.java
// Description   : Dialog box for scaling objects
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

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for scaling objects.
 */

public class ScaleDialogBox extends JDialog
   implements ActionListener
{
   public ScaleDialogBox(FlowframTk application)
   {
      super(application, 
         application.getResources().getMessage("scale.title"), true);
      application_ = application;

      int width  = 300;
      int height = 150;
      setSize(width,height);
      setLocationRelativeTo(application);

      JPanel panel = new JPanel();

      panel.setLayout(new GridLayout(3,2));

      ButtonGroup bg = new ButtonGroup();

      scaleXButton = getResources().createAppRadioButton("scale", "x",
        bg, false, this);

      panel.add(scaleXButton);

      scaleXField = new DoubleField(1.0F);
      panel.add(scaleXField);

      scaleYButton = getResources().createAppRadioButton("scale", "y",
         bg, false, this);
      panel.add(scaleYButton);

      scaleYField = new DoubleField(1.0F);
      panel.add(scaleYField);

      scaleButton = getResources().createAppRadioButton("scale", "both",
         bg, true, this);
      panel.add(scaleButton);

      scaleField = new DoubleField(1.0F);
      panel.add(scaleField);

      scaleXField.setEnabled(false);
      scaleYField.setEnabled(false);
      scaleField.setEnabled(true);

      scaleField.requestFocusInWindow();

      getContentPane().add(panel, "Center");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(this));
      p2.add(getResources().createCancelButton(this));
      p2.add(getResources().createHelpDialogButton(this, "sec:scaleobjects"));

      getContentPane().add(p2, "South");
   }

   public void display()
   {
      if (scaleXButton.isSelected())
      {
         scaleXField.requestFocusInWindow();
      }
      else if (scaleYButton.isSelected())
      {
         scaleYField.requestFocusInWindow();
      }
      else
      {
         scaleField.requestFocusInWindow();
      }

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
      else if (action.equals("x"))
      {
         scaleXField.setEnabled(true);
         scaleYField.setEnabled(false);
         scaleField.setEnabled(false);
         scaleXField.requestFocusInWindow();
      }
      else if (action.equals("y"))
      {
         scaleXField.setEnabled(false);
         scaleYField.setEnabled(true);
         scaleField.setEnabled(false);
         scaleYField.requestFocusInWindow();
      }
      else if (action.equals("both"))
      {
              scaleXField.setEnabled(false);
              scaleYField.setEnabled(false);
              scaleField.setEnabled(true);
              scaleField.requestFocusInWindow();
      }
   }

   public void okay()
   {
      setVisible(false);
      JDRFrame mainPanel = application_.getCurrentFrame();

      if (scaleXButton.isSelected())
      {
         mainPanel.scaleXSelectedPaths(scaleXField.getDouble());
      }
      else if (scaleYButton.isSelected())
      {
         mainPanel.scaleYSelectedPaths(scaleYField.getDouble());
      }
      else
      {
         mainPanel.scaleSelectedPaths(scaleField.getDouble());
      }
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "RotateDialogBox:"+eol;
      str += "has focus: "+hasFocus()+eol;
      str += "scale x field has focus: "+scaleXField.hasFocus()+eol;
      str += "scale y field has focus: "+scaleYField.hasFocus()+eol;
      str += "scale field has focus: "+scaleField.hasFocus()+eol;
      str += "scale x button has focus: "+scaleXButton.hasFocus()+eol;
      str += "scale y button has focus: "+scaleYButton.hasFocus()+eol;
      str += "scale button has focus: "+scaleButton.hasFocus()+eol;

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

   private DoubleField scaleXField, scaleYField, scaleField;
   private FlowframTk application_;
   private JRadioButton scaleXButton, scaleYButton, scaleButton;
}
