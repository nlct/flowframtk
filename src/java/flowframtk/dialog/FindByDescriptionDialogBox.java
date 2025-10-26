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
import java.util.regex.PatternSyntaxException;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

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

      JComponent upperComp = Box.createVerticalBox();

      filterButton = resources.createAppCheckBox(
       "findbydescription", "filter", true, null);
      filterButton.setAlignmentX(0.0f);

      upperComp.add(filterButton);
      upperComp.add(createFilterPanel());

      filterButton.addItemListener(new ItemListener()
       {
          @Override
          public void itemStateChanged(ItemEvent evt)
          {
             filterPanel.setVisible(filterButton.isSelected());
             mainComp.resetToPreferredSizes();
          }
       });

      descriptionModel = new DefaultListModel<FindListItem>();
      descriptionBox = new JList<FindListItem>(descriptionModel);

      descriptionBox.setPrototypeCellValue(
       new FindListItem(null, "Rotational Pattern 000 (360)"+getTitle()));


      JScrollPane descSp = new JScrollPane(descriptionBox);

      mainComp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        new JScrollPane(upperComp), descSp);

      getContentPane().add(mainComp, "Center");

      JPanel p2 = new JPanel();

      resources.createOkayCancelHelpButtons(this, p2, this, "mi:findbydesc");

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
        "findbydescription.match", "all", bg, true, null);

      panel.add(filterAllButton);

      filterAnyButton = resources.createAppRadioButton(
        "findbydescription.match", "any", bg, false, null);

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

      JLabelGroup labelGrp = new JLabelGroup();

      filterDesc = new MatchStringComponent(
        resources, "findbydescription.match_description", labelGrp);
      filterPanel.add(filterDesc);

      filterTag = new MatchStringComponent(
        resources, "findbydescription.match_tag", labelGrp);
      filterPanel.add(filterTag);

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
      filterDesc.reset();
      filterTag.reset();
   }

   public void display(boolean deselect)
   {
      setTitle(deselect ? findByTitle : addByTitle);

      currentFrame = application_.getCurrentFrame();
      paths = currentFrame.getAllPaths();
      deselect_ = deselect;
      update();
      descriptionBox.requestFocusInWindow();
      setVisible(true);
   }

   protected boolean filterAccepts(JDRCompleteObject object,
        String actualDescription, String displayedDescription)
   throws PatternSyntaxException
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

         boolean descMatch = filterDesc.accepts(actualDescription, displayedDescription);

         if (any)
         {
            if (descMatch) return true;
         }
         else
         {
            if (!descMatch) return false;

            match = (match && descMatch);
         }

         String tag = object.getTag();

         boolean tagMatch = filterTag.accepts(tag, tag);

         if (any)
         {
            if (tagMatch) return true;
         }
         else
         {
            if (!tagMatch) return false;

            match = (match && tagMatch);
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

      try
      {
         for (int i = 0, n = paths.size(); i < n; i++)
         {
            JDRCompleteObject object = paths.get(i); 

            if (!currentFrame.isObjectVisible(object)) continue;

            String description = object.getDescription();
            String displayedDescription = description;

            if (description.isEmpty())
            {
               displayedDescription = getResources().getDefaultDescription(object);
            }

            if (filterAccepts(object, description, displayedDescription))
            {
               descriptionModel.addElement(
                 new FindListItem(object, displayedDescription));
            }
         }
      }
      catch (PatternSyntaxException e)
      {
         getResources().error(this, 
           getResources().getMessage("error.invalid_regex",
             e.getPattern()));
      }

      revalidate();
   }

   public void okay()
   {
      int minIdx = descriptionBox.getMinSelectionIndex();

      if (minIdx == -1)
      {
         getResources().error(this, getResources().getMessage("error.invalid_no_item"));

         return;
      }

      int maxIdx = descriptionBox.getMaxSelectionIndex();

      if (deselect_)
      {
         currentFrame.deselectAll();
      }

      if (minIdx == maxIdx)
      {
         currentFrame.selectObjectAndScroll(descriptionModel.get(minIdx).getObject());
      }
      else
      {
         int[] indexes = descriptionBox.getSelectedIndices();

         JDRCompleteObject[] objects = new JDRCompleteObject[indexes.length];

         for (int i=0; i < indexes.length; i++)
         {
            objects[i] = descriptionModel.get(i).getObject();
         }

         currentFrame.selectObjectsAndScroll(objects);
      }

      setVisible(false);
   }

   @Override
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

   private JSplitPane mainComp;
   private JList<FindListItem> descriptionBox;
   private DefaultListModel<FindListItem> descriptionModel;

   private JCheckBox filterButton;
   private JComponent filterPanel;
   private JRadioButton filterAllButton, filterAnyButton;
   private JButton updateButton, resetButton;

   private MatchStringComponent filterDesc, filterTag;

   private HashMap<String,JCheckBox> filterObjectClassMap;

   private FlowframTk application_;
   private JDRFrame currentFrame = null;
   private JDRGroup paths;
   private boolean deselect_=true;
   private boolean doPack=true;

   private String findByTitle, addByTitle;
}

class FindListItem
{
   public FindListItem(JDRCompleteObject object, 
      String displayedDescription)
   {
      this.object = object;
      this.displayedDescription = displayedDescription;
   }

   public String toString()
   {
      return displayedDescription;
   }

   public JDRCompleteObject getObject()
   {
      return object;
   }

   JDRCompleteObject object;
   String displayedDescription;
}

class MatchStringComponent extends JPanel implements ItemListener
{
   public MatchStringComponent(JDRResources resources, String labelTag, JLabelGroup labelGrp)
   {
      super(new FlowLayout(FlowLayout.LEADING));

      JLabel label = resources.createAppLabel(labelTag);
      add(label);

      if (labelGrp != null)
      {
         labelGrp.add(label);
      }

      opBox = new JComboBox<String>
       (
          new String[]
           {
              resources.getMessage("findbydescription.str_match_op.any"),
              resources.getMessage("findbydescription.str_match_op.set"),
              resources.getMessage("findbydescription.str_match_op.not_set"),
              resources.getMessage("findbydescription.str_match_op.equals"),
              resources.getMessage("findbydescription.str_match_op.not_equals"),
              resources.getMessage("findbydescription.str_match_op.contains"),
              resources.getMessage("findbydescription.str_match_op.regex")
           }
       );

      add(opBox);
      opBox.setName(resources.getMessage("findbydescription.str_match_op"));

      inputCompLayout = new CardLayout();
      inputComp = new JPanel(inputCompLayout);

      add(inputComp);

      stringField = new JTextField(20);
      inputComp.add(stringField, "string");

      regexField = new JTextField(20);
      regexField.setToolTipText(resources.getMessage("findbydescription.regex.tooltip"));

      inputComp.add(regexField, "regex");

      inputComp.setVisible(false);
      opBox.addItemListener(this);
   }

   public void reset()
   {
      opBox.setSelectedIndex(STRING_MATCH_ANY);
   }

   public boolean accepts(String orgStr, String displayedStr)
    throws PatternSyntaxException
   {
      switch (opBox.getSelectedIndex())
      {
         case STRING_MATCH_ANY:
           return true;
         case STRING_MATCH_SET:
           return !orgStr.isEmpty();
         case STRING_MATCH_NOT_SET:
           return orgStr.isEmpty();
         case STRING_MATCH_EQUALS:
           return displayedStr.equals(stringField.getText());
         case STRING_MATCH_NOT_EQUALS:
           return !displayedStr.equals(stringField.getText());
         case STRING_MATCH_CONTAINS:
           return displayedStr.contains(stringField.getText());
         case STRING_MATCH_REGEX:
           return displayedStr.matches(regexField.getText());
      }

      return true;
   }

   @Override
   public void itemStateChanged(ItemEvent evt)
   {
      if (evt.getStateChange() == ItemEvent.SELECTED)
      {
         switch (opBox.getSelectedIndex())
         {
            case STRING_MATCH_EQUALS:
            case STRING_MATCH_NOT_EQUALS:
            case STRING_MATCH_CONTAINS:
              inputComp.setVisible(true);
              inputCompLayout.show(inputComp, "string");
            break;
            case STRING_MATCH_REGEX:
              inputComp.setVisible(true);
              inputCompLayout.show(inputComp, "regex");
            break;
            default:
              inputComp.setVisible(false);
         }
      }
   }

   public String getRegex()
   {
      return regexField.getText();
   }

   JComboBox<String> opBox;
   JComponent inputComp;
   CardLayout inputCompLayout;
   JTextField stringField, regexField;

   static final int STRING_MATCH_ANY = 0;
   static final int STRING_MATCH_SET=1;
   static final int STRING_MATCH_NOT_SET=2;
   static final int STRING_MATCH_EQUALS=3;
   static final int STRING_MATCH_NOT_EQUALS=4;
   static final int STRING_MATCH_CONTAINS=5;
   static final int STRING_MATCH_REGEX=6;
}
