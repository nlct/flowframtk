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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.*;

public class TeXEditorUIDialog extends JDialog
   implements ActionListener
{
   public TeXEditorUIDialog(FlowframTk application)
   {
      super(application,
            application.getResources().getMessage("texeditorui.title"),
            true);
      application_ = application;

      texEditorUIPanel = new TeXEditorUIPanel(application);

      getContentPane().add(texEditorUIPanel, "Center");

      // OK/Cancel Button panel

      JPanel p = new JPanel();
      getContentPane().add(p, "South");

      getResources().createOkayCancelHelpButtons(this, p, this, "sec:texeditorui");

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

   public void display()
   {
      texEditorUIPanel.initialise(application_);

      setVisible(true);
   }

   public void okay()
   {
      texEditorUIPanel.okay(application_);

      setVisible(false);
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private TeXEditorUIPanel texEditorUIPanel;

   private FlowframTk application_;
}
