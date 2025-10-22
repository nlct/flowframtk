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
import javax.swing.filechooser.FileFilter;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdr.io.ImportSettings;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.filter.*;
import com.dickimawbooks.flowframtk.*;

public class ImportDialog extends JDialog
  implements ActionListener,ItemListener
{
   public ImportDialog(FlowframTk application,
    JFileChooser importFC)
   {
      super(application, 
         application.getResources().getMessage("import.title"), true);
      this.application = application;
      this.importFC = importFC;

      importFC.addActionListener(this);

      JDRResources resources = getResources();
      importSettings = new ImportSettings(resources.getMessageDictionary());

      JComponent row;

      JLabel label = resources.createAppLabel("import.file");

      importFileField = new FileField(resources, this, "", importFC,
       JFileChooser.FILES_ONLY, label);

      formatField = new JTextField(resources.getMessage("import.type.ACORN_DRAW"));
      formatField.setEditable(false);
      formatField.setBorder(null);

      importFileField.getEastComponent().add(resources.createButtonSpacer());
      importFileField.getEastComponent().add(formatField);

      getContentPane().add(importFileField, "North");

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

      label = resources.createAppLabel("import.bitmap_dir");
      labelGrp.add(label);

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

   public void display()
   {
      importSettings.currentFile = null;
      bitmapNamePrefixField.setText("");

      setVisible(true);
   }

   protected void update()
   {
      update(importFileField.getFile());
   }

   protected void update(File file)
   {
      ImportSettings.Type type = ImportSettings.Type.ACORN_DRAW;

      if (file == null)
      {
         importSettings.currentFile = null;
         bitmapNamePrefixField.setText("");
      }
      else
      {
         FileFilter filter = importFC.getFileFilter();

         AbstractJDRFileFilter jdrFileFilter = null;

         if (filter instanceof AbstractJDRFileFilter)
         {
            jdrFileFilter = (AbstractJDRFileFilter)filter;
         }

         if (jdrFileFilter == null || !jdrFileFilter.accept(file))
         {
            for (FileFilter f : importFC.getChoosableFileFilters())
            {
               if (f instanceof AbstractJDRFileFilter
                    && f.accept(file))
               {
                  jdrFileFilter = (AbstractJDRFileFilter)f;
                  break;
               }
            }
         }

         if (jdrFileFilter instanceof EpsFileFilter)
         {
            type = ImportSettings.Type.EPS;
         }
         else if (jdrFileFilter instanceof SvgFileFilter)
         {
            type = ImportSettings.Type.SVG;
         }

         update(type, file);
      }

      formatField.setText(getResources().getMessage("import.type."+type));
   }

   protected void update(ImportSettings.Type type, File file)
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
         setVisible(false);
      }
      else if (action.equals("ApproveSelection"))
      {
         // file chooser selection approved

         update(importFC.getSelectedFile());
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
      File file = importFileField.getFile();

      if (file == null)
      {
         getResources().error(this, getResources().getMessage("error.no_filename"));

         return;
      }

      if (!file.equals(importSettings.currentFile))
      {
         // file field has been edited instead of using file chooser
         update(file);
      }

      importSettings.useMappings = useMappingsButton.isSelected();
      importSettings.extractBitmaps = extractBitmapsButton.isSelected();

      if (importSettings.extractBitmaps)
      {
         importSettings.bitmapDir = bitmapDirField.getFile();
         importSettings.bitmapNamePrefix = bitmapNamePrefixField.getText();
      }

      setVisible(false);
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
   FileField importFileField, bitmapDirField;
   JTextField bitmapNamePrefixField;
   JComponent bitmapComp;
   JFileChooser importFC;
   JTextField formatField;
}
