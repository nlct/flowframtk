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

import java.util.HashMap;
import java.util.Iterator;

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

      JDRResources resources = getResources();

      findByTitle = getTitle();
      addByTitle = resources.getMessage("addbydescription.title");

      JComponent mainPanel = Box.createVerticalBox();

      filterButton = resources.createAppCheckBox(
       "findbydescription", "filter", true, null);
      filterButton.setAlignmentX(0.0f);

      mainPanel.add(filterButton);
      mainPanel.add(createFilterPanel());

      filterButton.addItemListener(new ItemListener()
       {
          @Override
          public void itemStateChanged(ItemEvent evt)
          {
             filterPanel.setVisible(filterButton.isSelected());
          }
       });

      descriptionModel = new DefaultListModel<String>();
      descriptionBox = new JList<String>(descriptionModel);

      descriptionBox.setPrototypeCellValue(
       "Rotational Pattern 000 (360)"+getTitle());

      JScrollPane sp = new JScrollPane(descriptionBox);
      sp.setAlignmentX(0.0f);

      mainPanel.add(sp);

      getContentPane().add(new JScrollPane(mainPanel), "Center");

      JPanel p2 = new JPanel();

      p2.add(resources.createOkayButton(getRootPane(), this));
      p2.add(resources.createCancelButton(this));

      getContentPane().add(p2, "South");
      pack();

      filterButton.setSelected(false);

      setLocationRelativeTo(application);
   }

   protected JComponent createFilterPanel()
   {
      JDRResources resources = getResources();
      JComponent row, panel;

      filterPanel = Box.createVerticalBox();
      filterPanel.setAlignmentX(0.0f);

      row = new JPanel(new BorderLayout());
      filterPanel.add(row);

      resetButton = resources.createDialogButton(
       "findbydescription", "reset", this, null, null);

      row.add(resetButton, "West");

      updateButton = resources.createDialogButton(
       "findbydescription", "update", this, null, null);

      row.add(updateButton, "East");

      panel = new JPanel(new FlowLayout());
      row.add(panel, "Center");

      panel.add(resources.createAppLabel("findbydescription.match"));

      ButtonGroup bg = new ButtonGroup();

      filterAllButton = resources.createAppRadioButton(
        "findbydescription.match", "all", bg, false, null);

      panel.add(filterAllButton);

      filterAnyButton = resources.createAppRadioButton(
        "findbydescription.match", "any", bg, true, null);

      panel.add(filterAnyButton);

      row = Box.createHorizontalBox();
      filterPanel.add(row);

      row.add(resources.createAppLabel("findbydescription.object_type"));

      filterObjectClassMap = new HashMap<String,JCheckBox>();

      row.add(createFilterObjectClassCheckBox("JDRPath"));
      row.add(createFilterObjectClassCheckBox("JDRText"));
      row.add(createFilterObjectClassCheckBox("JDRBitmap"));
      row.add(createFilterObjectClassCheckBox("JDRGroup"));
      row.add(createFilterObjectClassCheckBox("JDRTextPath"));
      row.add(createFilterObjectClassCheckBox("JDRSymmetricPath"));
      row.add(createFilterObjectClassCheckBox("JDRRotationalPattern"));
      row.add(createFilterObjectClassCheckBox("JDRScaledPattern"));
      row.add(createFilterObjectClassCheckBox("JDRSpiralPattern"));

      return filterPanel;
   }

   protected JCheckBox createFilterObjectClassCheckBox(String className)
   {
      JCheckBox box = getResources().createAppCheckBox(
        "findbydescription.object_type", className, true, null);

      filterObjectClassMap.put(className, box);

      return box;
   }

   protected void resetFilter()
   {
      for (Iterator<String> it = filterObjectClassMap.keySet().iterator(); 
           it.hasNext(); )
      {
         String key = it.next();

         filterObjectClassMap.get(key).setSelected(true);
      }

      filterAnyButton.setSelected(true);
   }

   public void display(boolean deselect)
   {
      setTitle(deselect ? findByTitle : addByTitle);

      mainPanel = application_.getCurrentFrame();
      paths = mainPanel.getAllPaths();
      deselect_ = deselect;
      update();
      descriptionBox.requestFocusInWindow();
      setVisible(true);
   }

   protected boolean filterAccepts(JDRCompleteObject object, String description)
   {
      if (filterButton.isSelected())
      {
         boolean match = false;
         boolean any = filterAnyButton.isSelected();

         JCheckBox box = filterObjectClassMap.get(object.getClass().getSimpleName());

         if (box != null && box.isSelected())
         {
            if (any)
            {
               return true;
            }

            match = true;
         }

         return match;
      }
      else
      {
         return true;
      }
   }

   protected void update()
   {
      descriptionModel.removeAllElements();

      for (int i = 0, n = paths.size(); i < n; i++)
      {
         JDRCompleteObject object = paths.get(i); 

         String description = object.getDescription();

         if (description.isEmpty())
         {
            description = getResources().getDefaultDescription(object);
         }

         if (filterAccepts(object, description))
         {
            descriptionModel.addElement(description);
         }
      }

      revalidate();
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
      else if (action.equals("update"))
      {
         update();
      } 
      else if (action.equals("reset"))
      {
         resetFilter();
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

   private JList<String> descriptionBox;
   private DefaultListModel<String> descriptionModel;

   private JCheckBox filterButton;
   private JComponent filterPanel;
   private JRadioButton filterAllButton, filterAnyButton;
   private JButton updateButton, resetButton;

   private HashMap<String,JCheckBox> filterObjectClassMap;

   private FlowframTk application_;
   private JDRFrame mainPanel = null;
   private JDRGroup paths;
   private boolean deselect_=true;
   private boolean doPack=true;

   private String findByTitle, addByTitle;
}
