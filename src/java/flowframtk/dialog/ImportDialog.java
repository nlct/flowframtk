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
   public ImportDialog(FlowframTk application, JFileChooser importFC)
   {
      super(application, 
         application.getResources().getMessage("import.title"), true);
      this.application = application;
      this.importFC = importFC;

      init();
   }

   private void init()
   {
      importFC.addActionListener(this);

      JDRResources resources = getResources();
      importSettings = new ImportSettings(resources.getMessageDictionary());

      JComponent row;

      JLabel label = resources.createAppLabel("import.file");

      importFileField = new FileField(resources, this, "", importFC,
       JFileChooser.FILES_ONLY, label);

      formatField = new JTextField(resources.getMessage("import.type.ACORN_DRAW"));
      formatField.setColumns(formatField.getText().length()+2);
      formatField.setEditable(false);
      formatField.setBorder(null);

      importFileField.getEastComponent().add(resources.createButtonSpacer());
      importFileField.getEastComponent().add(formatField);

      getContentPane().add(importFileField, "North");

      JLabelGroup labelGrp = new JLabelGroup();

      JComponent mainComp = Box.createVerticalBox();
      getContentPane().add(new JScrollPane(mainComp), "Center");

      useMappingsButton = resources.createAppCheckBox("import", "use_mappings",
         true, null);
      mainComp.add(useMappingsButton);

      row = createRow();
      mainComp.add(row);

      checkMathsButton = resources.createAppCheckBox("import", "check_maths",
         true, null);
      row.add(checkMathsButton);

      checkMathsButton.addItemListener(this);

      cssClassesComp = new JPanel();
      row.add(cssClassesComp);

      cssClassesComp.add(resources.createButtonSpacer());

      onlyCssClassesBox = resources.createAppCheckBox("import",
        "check_maths_css_classes", false, null);
      onlyCssClassesBox.addItemListener(this);

      cssClassesComp.add(onlyCssClassesBox);

      cssClassesComp.add(resources.createLabelSpacer());

      cssClassesField = new JTextField(20);
      cssClassesComp.add(cssClassesField);
      cssClassesField.setEnabled(false);

      resources.clampCompMaxHeight(cssClassesComp, 0, 0);

      markerComp = createRow();
      mainComp.add(markerComp);

      markerComp.add(resources.createAppLabel("import.markers"));

      ButtonGroup bg = new ButtonGroup();
      markersIgnoreButton = resources.createAppRadioButton(
        "import.markers", "ignore", bg, false, null);
      markerComp.add(markersIgnoreButton);

      markersShapesButton = resources.createAppRadioButton(
        "import.markers", "add_shapes", bg, false, null);
      markerComp.add(markersShapesButton);

      markersMarkerButton = resources.createAppRadioButton(
        "import.markers", "markers", bg, false, null);
      markerComp.add(markersMarkerButton);

      paperComp = createRow();
      mainComp.add(paperComp);

      paperComp.add(resources.createAppLabel("import.paper"));

      bg = new ButtonGroup();
      paperCurrentButton = resources.createAppRadioButton(
        "import.paper", "current", bg, true, null);
      paperComp.add(paperCurrentButton);

      paperCustomButton = resources.createAppRadioButton(
        "import.paper", "custom", bg, false, null);
      paperComp.add(paperCustomButton);

      paperPredefinedButton = resources.createAppRadioButton(
        "import.paper", "predefined", bg, false, null);
      paperComp.add(paperPredefinedButton);


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

      row = createRow();
      bitmapComp.add(row);

      label = resources.createAppLabel("import.bitmap_prefix");
      labelGrp.add(label);
      row.add(label);

      bitmapNamePrefixField = new JTextField(12);
      label.setLabelFor(bitmapNamePrefixField);
      row.add(bitmapNamePrefixField);

      mainComp.add(Box.createVerticalStrut(20));

      rememberBox = resources.createAppCheckBox("import", "remember", true, null);
      rememberBox.setAlignmentX(0f);
      mainComp.add(rememberBox);

      mainComp.add(Box.createVerticalGlue());

      JComponent buttonComp = new JPanel();
      getContentPane().add(buttonComp, "South");

      resources.createOkayCancelHelpButtons(this, buttonComp, this, "sec:importimage");

      pack();

      extractBitmapsButton.setSelected(false);

      setLocationRelativeTo(application);
   }

   protected JComponent createRow()
   {
      JComponent row = Box.createHorizontalBox();
      row.setAlignmentX(0f);
      return row;
   }

   public void display()
   {
      importSettings.copyFrom(application.getSettings().getImportSettings());

      switch (importSettings.markers)
      {
         case IGNORE:
           markersIgnoreButton.setSelected(true);
         break;
         case ADD_SHAPES:
           markersShapesButton.setSelected(true);
         break;
         case MARKER:
           markersMarkerButton.setSelected(true);
         break;
      }

      switch (importSettings.paper)
      {
         case CURRENT:
            paperCurrentButton.setSelected(true);
         break;
         case CUSTOM:
            paperCustomButton.setSelected(true);
         break;
         case PREDEFINED:
            paperPredefinedButton.setSelected(true);
         break;
      }

      extractBitmapsButton.setSelected(importSettings.extractBitmaps);
      useMappingsButton.setSelected(importSettings.useMappings);

      if (importSettings.hasMathsCssClasses())
      {
         onlyCssClassesBox.setSelected(true);
         cssClassesField.setText(String.join(" ", importSettings.mathsCssClasses));
      }
      else
      {
         onlyCssClassesBox.setSelected(false);
      }

      checkMathsButton.setSelected(importSettings.parseMaths);

      update((File)null);

      setVisible(true);
   }

   protected void update()
   {
      update(importFileField.getFile());
   }

   protected void update(File file)
   {
      ImportSettings.Type type = importSettings.type;

      if (file == null)
      {
         importSettings.currentFile = null;
         bitmapNamePrefixField.setText("");

         FileFilter filter = importFC.getFileFilter();

         if (!(filter instanceof AbstractJDRFileFilter
              && ((AbstractJDRFileFilter)filter).supportsImportType(type)))
         {
            FileFilter[] filters = importFC.getChoosableFileFilters();

            for (int i = 0; i < filters.length; i++)
            {
               if (filters[i] instanceof AbstractJDRFileFilter
                    && ((AbstractJDRFileFilter)filters[i]).supportsImportType(type))
               {
                  importFC.setFileFilter(filters[i]);
                  break;
               }
            }
         }
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
         else
         {
            type = ImportSettings.Type.ACORN_DRAW;
         }

         update(type, file);
      }

      boolean isSVG = (type == ImportSettings.Type.SVG);

      cssClassesComp.setVisible(isSVG && checkMathsButton.isSelected());
      markerComp.setVisible(isSVG);
      paperComp.setVisible(isSVG);

      boolean embeddedBitmaps = !isSVG;

      extractBitmapsButton.setVisible(embeddedBitmaps);
      bitmapComp.setVisible(embeddedBitmaps && extractBitmapsButton.isSelected());

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
         bitmapComp.setVisible(
          extractBitmapsButton.isVisible() && extractBitmapsButton.isSelected());
      }
      else if (src == onlyCssClassesBox)
      {
         cssClassesField.setEnabled(onlyCssClassesBox.isSelected());

         if (cssClassesField.isEnabled())
         {
            cssClassesField.requestFocusInWindow();
         }
      }
      else if (src == checkMathsButton)
      {
         cssClassesComp.setVisible(checkMathsButton.isSelected());
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
      importSettings.parseMaths = checkMathsButton.isSelected();

      if (cssClassesComp.isVisible())
      {
         if (onlyCssClassesBox.isSelected())
         {
            String str = cssClassesField.getText().trim();

            if (str.isEmpty())
            {
               importSettings.mathsCssClasses = null;
            }
            else
            {
               importSettings.mathsCssClasses = str.split("\\s+|\\s*,\\s*");
            }
         }
         else
         {
            importSettings.mathsCssClasses = null;
         }
      }

      importSettings.extractBitmaps =
       extractBitmapsButton.isVisible() && extractBitmapsButton.isSelected();

      if (importSettings.extractBitmaps)
      {
         importSettings.bitmapDir = bitmapDirField.getFile();
         importSettings.bitmapNamePrefix = bitmapNamePrefixField.getText();
      }

      if (markerComp.isVisible())
      {
         if (markersIgnoreButton.isSelected())
         {
            importSettings.markers = ImportSettings.Markers.IGNORE;
         }
         else if (markersShapesButton.isSelected())
         {
            importSettings.markers = ImportSettings.Markers.ADD_SHAPES;
         }
         else if (markersMarkerButton.isSelected())
         {
            importSettings.markers = ImportSettings.Markers.MARKER;
         }
      }

      if (paperComp.isVisible())
      {
         if (paperCurrentButton.isSelected())
         {
            importSettings.paper = ImportSettings.Paper.CURRENT;
         }
         else if (paperCustomButton.isSelected())
         {
            importSettings.paper = ImportSettings.Paper.CUSTOM;
         }
         else if (paperPredefinedButton.isSelected())
         {
            importSettings.paper = ImportSettings.Paper.PREDEFINED;
         }
      }

      if (rememberBox.isSelected())
      {
         application.getSettings().getImportSettings().copyFrom(importSettings);
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

   JComponent cssClassesComp;
   JCheckBox onlyCssClassesBox;
   JTextField cssClassesField;

   JCheckBox useMappingsButton, checkMathsButton;
   JCheckBox extractBitmapsButton;
   FileField importFileField, bitmapDirField;
   JTextField bitmapNamePrefixField;
   JComponent bitmapComp;
   JFileChooser importFC;
   JTextField formatField;

   JComponent markerComp;
   JRadioButton markersIgnoreButton, markersShapesButton, markersMarkerButton;

   JComponent paperComp;
   JRadioButton paperCurrentButton, paperCustomButton, paperPredefinedButton;

   JCheckBox rememberBox;
}
