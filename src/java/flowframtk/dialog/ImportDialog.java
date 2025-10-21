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

import java.io.File;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdr.io.ImportSettings;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.*;

public class ImportDialog extends JDialog
  implements ActionListener,ItemListener
{
   public ImportDialog(FlowframTk application)
   {
      super(application, 
         application.getResources().getMessage("import.title"), true);
      this.application = application;

      JDRResources resources = getResources();
      importSettings = new ImportSettings(resources.getMessageDictionary());

      JLabelGroup labelGrp = new JLabelGroup();

      JComponent mainComp = Box.createVerticalBox();
      getContentPane().add(mainComp, "Center");

      useMappingsButton = resources.createAppCheckBox("import", "use_mappings",
         true, null);
      mainComp.add(useMappingsButton);

      extractBitmapsButton = resources.createAppCheckBox("import", "extract_bitmaps",
         true, null);
      extractBitmapsButton.addItemListener(this);
      mainComp.add(extractBitmapsButton);

      bitmapComp = Box.createVerticalBox();
      bitmapComp.setAlignmentX(0f);
      mainComp.add(bitmapComp);

      JLabel label = resources.createAppLabel("import.bitmap_dir");
      labelGrp.add(label);

      JComponent row;

      bitmapDirField = new FileField(resources, this, "",
        new JFileChooser(), JFileChooser.DIRECTORIES_ONLY, label);

      bitmapDirField.setAlignmentX(0f);
      bitmapComp.add(bitmapDirField);

      row = Box.createHorizontalBox();
      row.setAlignmentX(0f);
      bitmapComp.add(row);

      label = resources.createAppLabel("import.bitmap_prefix");
      labelGrp.add(label);
      row.add(label);

      bitmapNamePrefixField = new JTextField(12);
      label.setLabelFor(bitmapNamePrefixField);
      row.add(bitmapNamePrefixField);

      JComponent buttonComp = new JPanel();
      getContentPane().add(buttonComp, "South");

      resources.createOkayCancelHelpButtons(this, buttonComp, this, "sec:importimage");

      pack();

      extractBitmapsButton.setSelected(false);

      setLocationRelativeTo(application);
   }

   public void display(ImportSettings.Type type, File file)
   {
      importSettings.type = type;
      importSettings.currentFile = file;

      if (bitmapNamePrefixField.getText().isEmpty())
      {
         String base = file.getName();
         int idx;

         if (type == ImportSettings.Type.ACORN_DRAW)
         {
            idx = base.lastIndexOf(",");
         }
         else
         {
            idx = base.lastIndexOf(".");
         }

         if (idx > -1)
         {
            base = base.substring(0, idx);
         }

         bitmapNamePrefixField.setText(base);
      }

      setVisible(true);
   }

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action.equals("okay"))
      {
         okay();
      }
      else if (action.equals("cancel"))
      {
         if (!importSettings.extractBitmaps)
         {
            bitmapNamePrefixField.setText("");
         }

         setVisible(false);
      }
   }

   @Override
   public void itemStateChanged(ItemEvent evt)
   {
      Object src = evt.getSource();

      if (src == extractBitmapsButton)
      {
         bitmapComp.setVisible(extractBitmapsButton.isSelected());
      }
   }

   protected void okay()
   {
      importSettings.useMappings = useMappingsButton.isSelected();
      importSettings.extractBitmaps = extractBitmapsButton.isSelected();

      if (importSettings.extractBitmaps)
      {
         importSettings.bitmapDir = bitmapDirField.getFile();
         importSettings.bitmapNamePrefix = bitmapNamePrefixField.getText();
      }
      else
      {
         bitmapNamePrefixField.setText("");
      }

      application.importImage(importSettings);
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   ImportSettings importSettings;
   FlowframTk application;

   JCheckBox useMappingsButton;
   JCheckBox extractBitmapsButton;
   FileField bitmapDirField;
   JTextField bitmapNamePrefixField;
   JComponent bitmapComp;
}
