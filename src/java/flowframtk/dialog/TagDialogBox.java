/*
    Copyright (C) 2025 Nicola L.C. Talbot

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

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for specifying the tag for selected objects.
 * @author Nicola L C Talbot
 */
public class TagDialogBox extends JDialog
   implements ActionListener
{
   public TagDialogBox(FlowframTk application,
      String helpRef)
   {
      super(application, 
            application.getResources().getMessage("tag.title"),
            true);
      application_ = application;

      textField = new JTextField("", 20);

      getContentPane().add(textField, "Center");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(getRootPane(), this));
      p2.add(getResources().createCancelButton(this));

      try
      {
         p2.add(getResources().createHelpDialogButton(this, helpRef));
      }
      catch (HelpSetNotInitialisedException e)
      {
         getResources().internalError(null, e);
      }

      getContentPane().add(p2, "South");
      pack();
      setLocationRelativeTo(application);
   }

   public void initialise(String tag)
   {
      mainPanel = application_.getCurrentFrame();
      textField.setText(tag);
      textField.requestFocusInWindow();
      setVisible(true);
   }

   public void okay()
   {
      mainPanel.getCanvas().setSelectedTag(textField.getText().trim());
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
   }

   public String info()
   {
      return String.format(
       "TagDialogBox:%nhas focus: %s%ntext field has focus: %s%nText: %s%n",
        hasFocus(), textField.hasFocus(), textField.getText());
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private JTextField textField;
   private FlowframTk application_;
   private JDRFrame mainPanel = null;
}
