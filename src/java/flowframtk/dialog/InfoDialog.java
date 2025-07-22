// File          : InfoDialog.java
// Description   : Dialog for displaying status information
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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.*;

public class InfoDialog extends JDialog
   implements ActionListener
{
   public InfoDialog(FlowframTk application)
   {
      super(application,
         application.getResources().getMessage("info.title"), true);
      application_ = application;

      textArea = new JTextArea(10,20);
      textArea.setEditable(false);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);

      getContentPane().add(new JScrollPane(textArea), "Center");

      // OK/Cancel Button panel

      JPanel p = new JPanel();
      getContentPane().add(p, "South");

      p.add(getResources().createOkayButton(this));
      p.add(getResources().createCancelButton(this));

      helpButton = getResources().createDialogButton("label.help",
      "help", null, null, null);

      p.add(helpButton);

      pack();
      setLocationRelativeTo(application_);
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

   public void display(String text)
   {
      display(text, null);
   }

   public void display(String text, String helpId)
   {
      textArea.setText(text);

      if (helpId == null)
      {
         helpButton.setEnabled(false);
         helpButton.setVisible(false);
      }
      else
      {
         helpButton.setEnabled(true);
         helpButton.setVisible(true);
         getResources().enableHelpOnButton(helpButton, helpId);
      }

      setVisible(true);
   }

   public void okay()
   {
      setVisible(false);
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;

   private JTextArea textArea;

   private AbstractButton helpButton;
}
