// File          : FindByDescriptionDialogBox.java
// Description   : Dialog box for finding an object from its description
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

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for finding an object by its description.
 * @author Nicola L C Talbot
 */
public class FindByDescriptionDialogBox extends JDialog
   implements ActionListener
{
   public FindByDescriptionDialogBox(FlowframTk application)
   {
      super(application,
            application.getResources().getMessage("findbydescription.title"),
            true);
      application_ = application;

      descriptionModel = new DefaultComboBoxModel<String>();
      descriptionBox = new JComboBox<String>(descriptionModel);

      // add a temporary element to help pack the container
      descriptionModel.addElement(getTitle());

      getContentPane().add(descriptionBox, "Center");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(this));
      p2.add(getResources().createCancelButton(this));

      getContentPane().add(p2, "South");
      pack();
      setLocationRelativeTo(application);
   }

   public void display(boolean deselect)
   {
      mainPanel = application_.getCurrentFrame();
      descriptionModel.removeAllElements();
      paths = mainPanel.getAllPaths();
      deselect_ = deselect;

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i); 
         String description = object.getDescription();

         if (description.isEmpty())
         {
            description = getResources().getDefaultDescription(object);
         }

         descriptionModel.addElement(description);
      }

      descriptionBox.requestFocusInWindow();
      setVisible(true);
   }

   public void okay()
   {
      int i = descriptionBox.getSelectedIndex();
      if (deselect_)
      {
         mainPanel.deselectAll();
      }
      mainPanel.selectObjectAndScroll(paths.get(i));
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

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private JComboBox<String> descriptionBox;
   private DefaultComboBoxModel<String> descriptionModel;
   private FlowframTk application_;
   private JDRFrame mainPanel = null;
   private JDRGroup paths;
   private boolean deselect_=true;
   private boolean doPack=true;
}
