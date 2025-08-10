// File          : DisplayPageDialog.java
// Description   : Dialog to specify which pages to display
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

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Display pages dialog box.
 * This dialog box is used to specify whether to show objects that
 * have flowframe data defined on: all pages, odd pages, even
 * pages or a specific page number.
 * @author Nicola L C Talbot
 */
public class DisplayPageDialog extends JDialog
   implements ActionListener
{
   public DisplayPageDialog(FlowframTk application)
   {
      super(application,
            application.getResources().getMessage("displaypage.title"),true);
      application_ = application;

      JPanel p1 = new JPanel();
      p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));
      p1.setAlignmentX(Component.LEFT_ALIGNMENT);

      JLabel label = getResources().createAppLabel("displaypage.label");
      label.setAlignmentX(Component.LEFT_ALIGNMENT);
      p1.add(label);

      JPanel optionPanel = new JPanel();
      optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      p1.add(optionPanel);

      ButtonGroup group = new ButtonGroup();

      allButton = getResources().createAppRadioButton("displaypage", "all",
         group, true, this);

      allButton.setActionCommand("disableuser");
      optionPanel.add(allButton);

      oddButton = getResources().createAppRadioButton("displaypage", "odd",
         group, false, this);

      oddButton.setActionCommand("disableuser");
      optionPanel.add(oddButton);

      evenButton = getResources().createAppRadioButton("displaypage", "even",
         group, false, this);

      evenButton.setActionCommand("disableuser");
      optionPanel.add(evenButton);

      userButton = getResources().createAppRadioButton("displaypage", "user",
         group, false, this);

      userButton.setActionCommand("enableuser");
      optionPanel.add(userButton);

      userBox = new NonNegativeIntField(1);
      optionPanel.add(userBox);

      JLabel noteLabel = getResources().createAppLabel("displaypage.note");

      noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      p1.add(noteLabel);

      getContentPane().add(p1, "Center");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(getRootPane(), this));
      p2.add(getResources().createCancelButton(this));

      try
      {
         p2.add(getResources().createHelpDialogButton(this, "sec:displaypage"));
      }
      catch (HelpSetNotInitialisedException e)
      {
         getResources().internalError(null, e);
      }

      getContentPane().add(p2, "South");

      pack();
      setLocationRelativeTo(application_);
   }

   public void display()
   {
      mainPanel = application_.getCurrentFrame();

      switch (mainPanel.getDisplayPage())
      {
         case JDRCanvas.PAGES_ALL:
            allButton.setSelected(true);
            userBox.setEnabled(false);
            break;
         case JDRCanvas.PAGES_ODD:
            oddButton.setSelected(true);
            userBox.setEnabled(false);
            break;
         case JDRCanvas.PAGES_EVEN:
            evenButton.setSelected(true);
            userBox.setEnabled(false);
            break;
         default:
            userButton.setSelected(true);
            userBox.setEnabled(true);
            userBox.requestFocusInWindow();
      }

      setVisible(true);
   }

   public void okay()
   {
      if (allButton.isSelected())
      {
         mainPanel.setDisplayPage(JDRCanvas.PAGES_ALL);
      }
      else if (oddButton.isSelected())
      {
         mainPanel.setDisplayPage(JDRCanvas.PAGES_ODD);
      }
      else if (evenButton.isSelected())
      {
         mainPanel.setDisplayPage(JDRCanvas.PAGES_EVEN);
      }
      else
      {
         mainPanel.setDisplayPage(userBox.getInt());
      }
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
      else if (action.equals("disableuser"))
      {
         userBox.setEnabled(false);
      }
      else if (action.equals("enableuser"))
      {
         userBox.setEnabled(true);
         userBox.requestFocusInWindow();
      }
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "DisplayPageDialog:"+eol;
      str += "has focus: "+hasFocus()+eol;
      str += "user box has focus: "+userBox.hasFocus()+eol;
      str += "all button has focus: "+allButton.hasFocus()+eol;
      str += "odd button has focus: "+oddButton.hasFocus()+eol;
      str += "even button has focus: "+evenButton.hasFocus()+eol;
      str += "user button has focus: "+userButton.hasFocus()+eol;

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;
   private NonNegativeIntField userBox;
   private JDRFrame mainPanel = null;

   private JRadioButton allButton, oddButton, evenButton, userButton;
}
