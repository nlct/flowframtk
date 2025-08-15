// File          : ConfigTeXSettingsDialog.java
// Description   : Dialog for configuring TeX/LaTeX settings
// Creation Date : 2014-06-03
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.image.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.filter.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for configuring TeX/LaTeX settings.
 */

public class ConfigTeXSettingsDialog extends JDialog
   implements ActionListener
{
   public ConfigTeXSettingsDialog(FlowframTk application)
   {
      super(application,
         application.getResources().getMessage("texconfig.title"), true);
      application_ = application;
      setIconImage(getResources().getSmallAppIcon().getImage());

      tabbedPane = new JTabbedPane();
      getContentPane().add(tabbedPane, "Center");

      int idx=0;

      CanvasGraphics cg = application.getDefaultCanvasGraphics();

      texSettings = new TeXSettingsPanel(application);

      tabbedPane.addTab(getResources().getMessage("clssettings.title"),
         null, new JScrollPane(texSettings),
         getResources().getMessage("clssettings.tooltip"));

      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("clssettings.mnemonic"));

      flfConfigPanel = new FlfConfigPanel(getResources());

      tabbedPane.addTab(getResources().getMessage("flfsettings.title"),
         null, new JScrollPane(flfConfigPanel),
         getResources().getMessage("flfsettings.tooltip"));

      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("flfsettings.mnemonic"));

      textConfigPanel = new TextConfigPanel(this, application);

      tabbedPane.addTab(getResources().getMessage("textconfig.title"),
         null, new JScrollPane(textConfigPanel),
         getResources().getMessage("textconfig.tooltip"));

      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("textconfig.mnemonic"));

      preambleConfigPanel = new PreambleConfigPanel(this, application);

      tabbedPane.addTab(getResources().getMessage("preambleconfig.title"),
         null, new JScrollPane(preambleConfigPanel),
         getResources().getMessage("preambleconfig.tooltip"));

      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("preambleconfig.mnemonic"));


      // OK/Cancel Button panel

      JPanel p = new JPanel();
      getContentPane().add(p, "South");

      p.add(getResources().createOkayButton(getRootPane(), this));
      p.add(getResources().createCancelButton(this));

      try
      {
         p.add(getResources().createHelpDialogButton(this, "sec:texconfig"));
      }
      catch (HelpSetNotInitialisedException e)
      {
         getResources().internalError(null, e);
      }

      pack();
      Dimension dim = getSize();
      dim.height = 400;
      setSize(dim);
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

   public void display(JDRFrame frame)
   {
      this.frame = frame;
      CanvasGraphics cg = frame.getCanvasGraphics();

      texSettings.initialise(frame);
      flfConfigPanel.initialise(application_, cg);
      textConfigPanel.initialise(cg);
      preambleConfigPanel.initialise();

      if (getFocusOwner() == null)
      {
         Component comp = tabbedPane.getSelectedComponent();

         if (comp != null)
         {
            comp.requestFocusInWindow();
         }
      }

      setVisible(true);
   }

   public void okay()
   {
      textConfigPanel.okay();
      flfConfigPanel.okay(application_);
      texSettings.okay(frame);

      try
      {
         preambleConfigPanel.okay();
      }
      catch (IOException e)
      {
         getResources().error(this, e);
      }

      application_.repaint();

      setVisible(false);
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "ConfigTeXSettingsDialog:"+eol;
      str += "has focus: "+hasFocus()+eol;

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private TextConfigPanel textConfigPanel;
   private TeXSettingsPanel texSettings;
   private FlfConfigPanel flfConfigPanel;
   private PreambleConfigPanel preambleConfigPanel;
   private JTabbedPane tabbedPane;

   private FlowframTk application_;
   private JDRFrame frame;
}

class TeXSettingsPanel extends Box
   implements ItemListener,ActionListener
{
   public TeXSettingsPanel(FlowframTk application)
   {
      super(BoxLayout.Y_AXIS);
      this.application_ = application;

      Box b2 = Box.createHorizontalBox();
      b2.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(b2);

      JLabel label = getResources().createAppLabel("clssettings.normalsize");

      label.setAlignmentX(Component.LEFT_ALIGNMENT);
      b2.add(label);

      b2.add(Box.createHorizontalStrut(10));

      sizeBox = new JComboBox<Integer>(availableSizes);
      sizeBox.addItemListener(this);
      label.setLabelFor(sizeBox);
      b2.add(sizeBox);
      sizeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      sizeBox.setMaximumSize(sizeBox.getPreferredSize());

      useRelativeFontDeclarations = getResources().createAppCheckBox(
         "clssettings", "relative_fontsize", true, null);
      useRelativeFontDeclarations.setAlignmentX(Component.LEFT_ALIGNMENT);
      b2.add(useRelativeFontDeclarations);

      updateImageBox = getResources().createAppCheckBox("clssettings",
        "update", application_.isLaTeXFontUpdateEnabled(), null);

      updateImageBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(updateImageBox);

      add(Box.createVerticalStrut(10));

      JTextArea textarea = getResources().createAppInfoArea("clssettings.note");
      textarea.setAlignmentX(Component.LEFT_ALIGNMENT);
      textarea.setLineWrap(false);
      Dimension dim = textarea.getMaximumSize();
      dim.height = (int)textarea.getPreferredSize().getHeight();
      textarea.setMaximumSize(dim);

      add(textarea);
      add(Box.createVerticalStrut(10));

      example = new JLabel("\\documentclass[10pt]{article}");
      example.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(example);

      add(Box.createVerticalStrut(10));

      b2 = Box.createHorizontalBox();
      b2.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(b2);

      ButtonGroup bg = new ButtonGroup();

      useDefaultCls = getResources().createAppRadioButton("clssettings",
         "default_cls", bg, true, this);
      useDefaultCls.setAlignmentX(Component.LEFT_ALIGNMENT);
      b2.add(useDefaultCls);

      b2.add(Box.createHorizontalStrut(20));

      useCustomCls = getResources().createAppRadioButton("clssettings",
         "custom_cls", bg, false, this);
      useCustomCls.setAlignmentX(Component.LEFT_ALIGNMENT);
      b2.add(useCustomCls);

      customClsField = new JTextField(10);
      customClsField.setAlignmentX(Component.LEFT_ALIGNMENT);
      b2.add(customClsField);

      customClsField.setMaximumSize(customClsField.getPreferredSize());

      usePdfInfo = getResources().createAppCheckBox("clssettings",
        "pdfinfo", false, null);
      usePdfInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(usePdfInfo);

      useTypeblockAsBoundingBox = getResources().createAppCheckBox("clssettings",
        "use_typeblock_as_bbox", false, null);
      useTypeblockAsBoundingBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(useTypeblockAsBoundingBox);

      add(Box.createVerticalGlue());
   }

   public void initialise(JDRFrame frame)
   {
      useRelativeFontDeclarations.setSelected(
         application_.useRelativeFontDeclarations());

      usePdfInfo.setSelected(application_.usePdfInfo());

      int normalSize = (int)frame.getNormalSize();

      for (int i = 0; i < availableSizes.length; i++)
      {
         if (availableSizes[i].intValue() == normalSize)
         {
            sizeBox.setSelectedIndex(i);
            break;
         }
      }

      String cls = frame.getCanvasGraphics().getDocClass();

      if (cls != null && !cls.isEmpty())
      {
         useCustomCls.setSelected(true);
         customClsField.setEnabled(true);
         customClsField.setText(cls);
      }
      else
      {
         useDefaultCls.setSelected(true);
         customClsField.setText("");
         customClsField.setEnabled(false);
      }

      updateExample(normalSize);

      useTypeblockAsBoundingBox.setSelected(
       application_.getSettings().useTypeblockAsBoundingBox);
   }

   public void updateExample(int normalsize)
   {
      String cls = customClsField.getText();

      if (useDefaultCls.isSelected() || cls.isEmpty())
      {
         if (normalsize >= 10 && normalsize <= 12)
         {
            example.setText(
               "\\documentclass["+normalsize+"pt]{article}");
         }
         else if (normalsize >= 24)
         {
            example.setText("\\documentclass{a0poster}");
         }
         else
         {
            example.setText(
               "\\documentclass["+normalsize+"pt]{extarticle}");
         }
      }
      else
      {
         example.setText(
            "\\documentclass["+normalsize+"pt]{"+cls+"}");
      }
   }

   public void itemStateChanged(ItemEvent e)
   {
      int normalsize = ((Integer)sizeBox.getSelectedItem()).intValue();
      updateExample(normalsize);
   }

   public void okay(JDRFrame frame)
   {
      application_.setRelativeFontDeclarations(
         useRelativeFontDeclarations.isSelected());
      application_.setUsePdfInfoEnabled(usePdfInfo.isSelected());

      double normalsize = ((Integer)sizeBox.getSelectedItem()).intValue();

      if (normalsize != frame.getNormalSize())
      {
         frame.getCanvas().setNormalSize(normalsize);

         if (updateImageBox.isSelected())
         {
            frame.updateLaTeXFontSize();
         }
      }

      application_.setLaTeXFontUpdate(getLaTeXFontUpdate());

      frame.getCanvas().setDocClass(
        useDefaultCls.isSelected() ? null : customClsField.getText());

      application_.getSettings().useTypeblockAsBoundingBox
        = useTypeblockAsBoundingBox.isSelected();
   }

   public boolean getLaTeXFontUpdate()
   {
      return updateImageBox.isSelected();
   }

   public void setLaTeXFontUpdate(boolean update)
   {
      updateImageBox.setSelected(update);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("default_cls"))
      {
         customClsField.setEnabled(false);
      }
      else if (action.equals("custom_cls"))
      {
         customClsField.setEnabled(true);
         customClsField.requestFocusInWindow();
      }

      int normalsize = ((Integer)sizeBox.getSelectedItem()).intValue();
      updateExample(normalsize);
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private JComboBox<Integer> sizeBox;
   private JLabel example;

   private static final Integer[] availableSizes = new Integer[]
         {
            new Integer(25),
            new Integer(20),
            new Integer(17),
            new Integer(14),
            new Integer(12),
            new Integer(11),
            new Integer(10),
            new Integer(9),
            new Integer(8)
         };

   private JCheckBox updateImageBox;

   private JRadioButton useDefaultCls, useCustomCls;

   private JTextField customClsField;

   private JCheckBox useRelativeFontDeclarations, usePdfInfo;

   private JCheckBox useTypeblockAsBoundingBox;

   private FlowframTk application_;
}

class FlfConfigPanel extends Box
{
   public FlfConfigPanel(JDRResources resources)
   {
      super(BoxLayout.Y_AXIS);

      Box shapeparBox = Box.createHorizontalBox();
      shapeparBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(shapeparBox);

      JLabel shapeparLabel = new JLabel(
         resources.getMessage("flfsettings.shapeparcs"));
      shapeparLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      shapeparBox.add(shapeparLabel);

      ButtonGroup bg = new ButtonGroup();

      hPaddingShapeparOn = new JRadioButton("\\shapepar");
      hPaddingShapeparOn.setAlignmentX(Component.LEFT_ALIGNMENT);
      shapeparBox.add(hPaddingShapeparOn);
      bg.add(hPaddingShapeparOn);

      hPaddingShapeparOff = new JRadioButton("\\Shapepar");
      hPaddingShapeparOff.setAlignmentX(Component.LEFT_ALIGNMENT);
      shapeparBox.add(hPaddingShapeparOff);
      bg.add(hPaddingShapeparOff);

      Box pagesBox = Box.createHorizontalBox();
      pagesBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(pagesBox);

      JLabel pagesLabel = new JLabel(
          resources.getMessage("flfsettings.pages_opt"));
      pagesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      pagesBox.add(pagesLabel);

      bg = new ButtonGroup();

      useAbsPages = resources.createAppRadioButton("flfsettings",
         "pages_opt.absolute", bg, false, null);
      useAbsPages.setAlignmentX(Component.LEFT_ALIGNMENT);
      pagesBox.add(useAbsPages);

      useRelPages = resources.createAppRadioButton("flfsettings",
         "pages_opt.relative", bg, true, null);
      useRelPages.setAlignmentX(Component.LEFT_ALIGNMENT);
      pagesBox.add(useRelPages);

   }

   public void initialise(FlowframTk application, CanvasGraphics cg)
   {
      hPaddingShapeparOn.setSelected(application.useHPaddingShapepar());
      hPaddingShapeparOff.setSelected(!hPaddingShapeparOn.isSelected());

      if (cg.useAbsolutePages())
      {
         useAbsPages.setSelected(true);
      }
      else
      {
         useRelPages.setSelected(true);
      }
   }

   public void okay(FlowframTk application)
   {
      application.setHPaddingShapepar(hPaddingShapeparOn.isSelected());

      JDRFrame frame = application.getCurrentFrame();

      if (frame != null)
      {
         frame.getCanvas().setUseAbsolutePages(useAbsPages.isSelected());
      }
      else
      {
         application.setUseAbsolutePages(useAbsPages.isSelected());
      }

   }

   private JRadioButton hPaddingShapeparOn, hPaddingShapeparOff;

   private JRadioButton useAbsPages, useRelPages;
}

class TextConfigPanel extends JPanel 
  implements ActionListener, ListSelectionListener
{
   public TextConfigPanel(JDialog parent, FlowframTk application)
   {
      super(new BorderLayout());

      resources = application.getResources();
      this.application = application;

      fileChooser = new JFileChooser();

      add(createTopComponent(), "North");

      JTabbedPane tabbedPane = new JTabbedPane();
      tabbedPane.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(tabbedPane, "Center");

      tabbedPane.addTab(resources.getMessage("textconfig.textmappings"),
        resources.getSmallButtonIcon("textarea"),
        createTextMappingsComponent(),
        resources.getToolTipText("textconfig.textmappings"));
      tabbedPane.setMnemonicAt(0, 
        resources.getCodePoint("textconfig.textmappings.mnemonic"));

      tabbedPane.addTab(
        resources.getMessage("textconfig.mathmappings"),
        resources.getSmallButtonIcon("math"),
        createMathsMappingsComponent(),
        resources.getToolTipText("textconfig.mathmappings"));
      tabbedPane.setMnemonicAt(1, 
        resources.getCodePoint("textconfig.mathmappings.mnemonic"));

      removedTextMappings = new Vector<Integer>();
      removedMathMappings = new Vector<Integer>();

      texMapDialog = new TeXMapDialog(parent, resources);
   }

   private JComponent createTopComponent()
   {
      Box box = Box.createVerticalBox();
      box.setAlignmentX(Component.LEFT_ALIGNMENT);

      autoAdjustAnchorBox = resources.createAppCheckBox(
        "textconfig", "anchor", true, null);

      autoAdjustAnchorBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      box.add(autoAdjustAnchorBox);

      Box textualShadingComp = Box.createHorizontalBox();
      textualShadingComp.setAlignmentX(Component.LEFT_ALIGNMENT);
      box.add(textualShadingComp);

      JLabel textualShadingLabel = 
         resources.createAppLabel("textconfig.textualshading");
      textualShadingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      textualShadingComp.add(textualShadingLabel);
      textualShadingComp.add(Box.createHorizontalStrut(10));

      textualShading = new JComboBox<String>(new String[]
      {
          resources.getMessage("textconfig.textualshading.average"),
          resources.getMessage("textconfig.textualshading.start"),
          resources.getMessage("textconfig.textualshading.end"),
          resources.getMessage("textconfig.textualshading.path")
      });

      textualShadingLabel.setLabelFor(textualShading);
      textualShading.setAlignmentX(Component.LEFT_ALIGNMENT);
      textualShadingComp.add(textualShading);
      textualShadingComp.add(Box.createHorizontalGlue());

      Box textPathOutlineComp = Box.createHorizontalBox();
      textPathOutlineComp.setAlignmentX(Component.LEFT_ALIGNMENT);
      box.add(textPathOutlineComp);

      JLabel textPathOutlineLabel = 
         resources.createAppLabel("textconfig.textpathoutline");
      textPathOutlineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      textPathOutlineComp.add(textPathOutlineLabel);
      textPathOutlineComp.add(Box.createHorizontalStrut(10));

      textPathOutline = new JComboBox<String>(new String[]
      {
          resources.getMessage("textconfig.textpathoutline.path"),
          resources.getMessage("textconfig.textpathoutline.ignore")
      });

      textPathOutlineLabel.setLabelFor(textPathOutline);
      textPathOutline.setAlignmentX(Component.LEFT_ALIGNMENT);
      textPathOutlineComp.add(textPathOutline);
      textPathOutlineComp.add(Box.createHorizontalGlue());

      return box;
   }

   private JComponent createTextMappingsComponent()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setAlignmentX(Component.LEFT_ALIGNMENT);

      autoEscapeTextSpCharBox = resources.createAppCheckBox(
        "textconfig", "escape", true, this);

      panel.add(autoEscapeTextSpCharBox, "North");

      Box box = Box.createVerticalBox();
      panel.add(box, "East");

      addTextMapButton = resources.createDialogButton(
        "textconfig.textmappings.add", "textmappings.add", this, null, 
        resources.getMessage("textconfig.textmappings.add.tooltip"));
      box.add(addTextMapButton);

      removeTextMapButton = resources.createDialogButton(
        "textconfig.textmappings.remove", "textmappings.remove", this, null, 
        resources.getMessage("textconfig.textmappings.remove.tooltip"));
      box.add(removeTextMapButton);

      box.add(resources.createDialogButton("textconfig.textmappings.import",
        "textmappings.import", this, null));

      textMapData = new Vector<TeXMapRow>();

      textModeMappings = application.getTextModeMappings();

      columnNames = new Vector<String>(2);
      columnNames.add(resources.getMessage("textconfig.mapping.codepoint"));
      columnNames.add(resources.getMessage("textconfig.mapping.character"));
      columnNames.add(resources.getMessage("textconfig.mapping.command"));
      columnNames.add(resources.getMessage("textconfig.mapping.package"));

      textMapTable = new JTable()
      {
         public boolean isCellEditable(int row, int column)
         {
            return column > 1;
         }

         public Dimension getPreferredScrollableViewportSize()
         {
            Dimension dim = super.getPreferredScrollableViewportSize();

            dim.height = 10*(getRowHeight()+getRowMargin());

            return dim;
         }
      };

      updateTextMappings();
      textMapTable.setModel(new DefaultTableModel(textMapData, columnNames));

      ListSelectionModel listModel = textMapTable.getSelectionModel();
      listModel.addListSelectionListener(this);

      textMapTable.setRowSorter(
         new TableRowSorter<TableModel>(textMapTable.getModel()));

      panel.add(new JScrollPane(textMapTable));

      return panel;
   }

   private JComponent createMathsMappingsComponent()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setAlignmentX(Component.LEFT_ALIGNMENT);

      autoEscapeMathSymBox = resources.createAppCheckBox(
        "textconfig", "escape.mathchar", true, this);

      panel.add(autoEscapeMathSymBox, "North");

      Box box = Box.createVerticalBox();
      panel.add(box, "East");

      addMathMapButton = resources.createDialogButton(
        "textconfig.mathmappings.add", "mathmappings.add", this, null, 
        resources.getMessage("textconfig.mathmappings.add.tooltip"));
      box.add(addMathMapButton);

      removeMathMapButton = resources.createDialogButton(
        "textconfig.mathmappings.remove", "mathmappings.remove", this, null, 
        resources.getMessage("textconfig.mathmappings.remove.tooltip"));
      box.add(removeMathMapButton);

      box.add(resources.createDialogButton("textconfig.mathmappings.import",
         "mathmappings.import", this, null), "West");

      mathMapData = new Vector<TeXMapRow>();

      mathModeMappings = application.getMathModeMappings();

      mathMapTable = new JTable()
      {
         public boolean isCellEditable(int row, int column)
         {
            return column > 1;
         }
      };

      updateMathMappings();
      mathMapTable.setModel(new DefaultTableModel(mathMapData, columnNames));

      ListSelectionModel listModel = mathMapTable.getSelectionModel();
      listModel.addListSelectionListener(this);

      mathMapTable.setRowSorter(
         new TableRowSorter<TableModel>(mathMapTable.getModel()));

      panel.add(new JScrollPane(mathMapTable));

      return panel;
   }

   private void updateTextMappings()
   {
      textMapData.clear();

      for (Enumeration<Integer> en = textModeMappings.keys();
           en.hasMoreElements();)
      {
         Integer key = en.nextElement();

         textMapData.add(new TeXMapRow(key, textModeMappings.get(key)));
      }
   }

   private void updateMathMappings()
   {
      mathMapData.clear();

      for (Enumeration<Integer> en = mathModeMappings.keys();
           en.hasMoreElements();)
      {
         Integer key = en.nextElement();

         mathMapData.add(new TeXMapRow(key, mathModeMappings.get(key)));
      }
   }

   public void initialise(CanvasGraphics cg)
   {
      autoAdjustAnchorBox.setSelected(application.isAutoAnchorEnabled());

      textualShading.setSelectedIndex(
         application.getTextualExportShadingSetting());
      textPathOutline.setSelectedIndex(
         application.getTextPathExportOutlineSetting());

      autoEscapeTextSpCharBox.setSelected(
         application.isAutoEscapeSpCharsEnabled());

      updateEnableTextMappings();
      updateEnableMathMappings();

      removedTextMappings.clear();
      removedMathMappings.clear();

      updateTextMappings();
      updateMathMappings();

      textMapTable.setModel(new DefaultTableModel(textMapData, columnNames));
      mathMapTable.setModel(new DefaultTableModel(mathMapData, columnNames));
   }

   public void okay()
   {
      application.setAutoAnchor(autoAdjustAnchorBox.isSelected());
      application.setAutoEscapeSpChars(autoEscapeTextSpCharBox.isSelected());
      application.setTextualExportShadingSetting(
         textualShading.getSelectedIndex());
      application.setTextPathExportOutlineSetting(
         textPathOutline.getSelectedIndex());

      for (TeXMapRow row : textMapData)
      {
         Integer key = row.getKey();
         String command = row.getCommand();

         if (command.matches("\\\\[a-zA-Z]+"))
         {
            command += " ";
         }

         textModeMappings.put(key, command, row.getStyName());
      }

      for (TeXMapRow row : mathMapData)
      {
         Integer key = row.getKey();
         String command = row.getCommand();

         if (command.matches("\\\\[a-zA-Z]+"))
         {
            command += " ";
         }

         mathModeMappings.put(key, command, row.getStyName());
      }

      for (Integer key : removedTextMappings)
      {
         textModeMappings.remove(key);
      }

      for (Integer key : removedMathMappings)
      {
         mathModeMappings.remove(key);
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("escape"))
      {
         updateEnableTextMappings();
      }
      else if (action.equals("escape.mathchar"))
      {
         updateEnableMathMappings();
      }
      else if (action.equals("textmappings.remove"))
      {
         int[] indexes = textMapTable.getSelectedRows();

         DefaultTableModel model = (DefaultTableModel)textMapTable.getModel();

         for (int i = indexes.length-1; i >= 0; i--)
         {
            TeXMapRow row = textMapData.get(indexes[i]);

            removedTextMappings.add(row.getKey());

            model.removeRow(indexes[i]);
         }
      }
      else if (action.equals("mathmappings.remove"))
      {
         int[] indexes = mathMapTable.getSelectedRows();

         DefaultTableModel model = (DefaultTableModel)mathMapTable.getModel();

         for (int i = indexes.length-1; i >= 0; i--)
         {
            TeXMapRow row = mathMapData.get(indexes[i]);

            removedMathMappings.add(row.getKey());

            model.removeRow(indexes[i]);
         }
      }
      else if (action.equals("textmappings.add"))
      {
         texMapDialog.setTitle(resources.getMessage("textconfig.textmappings.add"));

         TeXMapRow map = texMapDialog.requestMapping();

         if (map != null)
         {
            addMap((DefaultTableModel)textMapTable.getModel(), map);
         }
      }
      else if (action.equals("mathmappings.add"))
      {
         texMapDialog.setTitle(resources.getMessage("textconfig.mathmappings.add"));

         TeXMapRow map = texMapDialog.requestMapping();

         if (map != null)
         {
            addMap((DefaultTableModel)mathMapTable.getModel(), map);
         }
      }
      else if (action.equals("textmappings.import"))
      {
         if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
         {
            importTextMappings(fileChooser.getSelectedFile());
         }
      }
      else if (action.equals("mathmappings.import"))
      {
         if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
         {
            importMathMappings(fileChooser.getSelectedFile());
         }
      }
   }

   public void addMap(DefaultTableModel model, TeXMapRow map)
   {
      // check if this mapping already exists

      String key = TeXMapRow.formatKey(map.getKey());

      for (int i = 0; i < model.getRowCount(); i++)
      {
         Object code = model.getValueAt(i, 0);

         if (key.equals(code))
         {
            model.setValueAt(map.getCommand(), i, 2);
            model.setValueAt(map.getStyName(), i, 3);
            return;
         }
      }

      model.addRow(map);
   }


   public void importTextMappings(File file)
   {
      try
      {
         importMapping(true, (DefaultTableModel)textMapTable.getModel(), file);
      }
      catch (IOException e)
      {
         resources.error(this, e);
      }
   }

   public void importMathMappings(File file)
   {
      try
      {
         importMapping(false, (DefaultTableModel)mathMapTable.getModel(), file);
      }
      catch (IOException e)
      {
         resources.error(this, e);
      }
   }

   private void importMapping(boolean textMode,
     DefaultTableModel model, File file)
      throws IOException
   {
      BufferedReader reader = null;

      try
      {
         reader = new BufferedReader(new FileReader(file));

         String line;
         int lineNum = 0;

         while ((line = reader.readLine()) != null)
         {
            lineNum++;

            if (line.startsWith("#") || line.isEmpty())
            {
               continue;
            }

            String[] split = line.split("\t");

            if (split == null || split.length < 3)
            {
               throw new IOException(
                  resources.getMessage(lineNum, "error.io.invalid_map", line)
               );
            }

            int codepoint;

            try
            {
               codepoint = Integer.parseInt(split[0], 16);
            }
            catch (NumberFormatException e)
            {
               throw new IOException(
                  resources.getMessage(lineNum,
                     "error.io.invalid_map", line), e);
            }

            String cmd = split[1];

            Matcher m = CS_SUFFIX.matcher(cmd);

            if (textMode)
            {
               if (m.matches())
               {
                  int type = Character.getType(codepoint);

                  if (type == Character.COMBINING_SPACING_MARK
                   || type == Character.CONNECTOR_PUNCTUATION
                   || type == Character.MODIFIER_SYMBOL
                   || type == Character.MODIFIER_LETTER)
                  {
                     cmd = m.group(1)+" ";
                  }
                  else
                  {
                     cmd = m.group(1)+"{}";
                  }
               }
            }
            else
            {
               if (m.matches())
               {
                  cmd = m.group(1)+" ";
               }
            }

            addMap(model, new TeXMapRow(codepoint, cmd, split[2]));
         }
      }
      finally
      {
         if (reader != null)
         {
            reader.close();
         }
      }
   }

   public void valueChanged(ListSelectionEvent evt)
   {
      if (evt.getValueIsAdjusting())
      {
         return;
      }

      Object source = evt.getSource();

      if (source == textMapTable.getSelectionModel())
      {
         removeTextMapButton.setEnabled(textMapTable.getSelectedRow() != -1);
      }
      else if (source == mathMapTable.getSelectionModel())
      {
         removeMathMapButton.setEnabled(mathMapTable.getSelectedRow() != -1);
      }
   }

   public void updateEnableTextMappings()
   {
      boolean enable = autoEscapeTextSpCharBox.isSelected();
      textMapTable.setVisible(enable);
      addTextMapButton.setEnabled(enable);
      removeTextMapButton.setEnabled(enable
         && (textMapTable.getSelectedRow() != -1));
   }

   public void updateEnableMathMappings()
   {
      boolean enable = autoEscapeMathSymBox.isSelected();
      mathMapTable.setVisible(enable);
      addMathMapButton.setEnabled(enable);
      removeMathMapButton.setEnabled(enable
         && (mathMapTable.getSelectedRow() != -1));
   }

   private JCheckBox autoAdjustAnchorBox, autoEscapeTextSpCharBox,
     autoEscapeMathSymBox;

   private JDRButton addTextMapButton, removeTextMapButton;
   private JDRButton addMathMapButton, removeMathMapButton;

   private Vector<TeXMapRow> textMapData;
   private Vector<TeXMapRow> mathMapData;

   private Vector<Integer> removedTextMappings, removedMathMappings;

   private TextModeMappings textModeMappings;
   private MathModeMappings mathModeMappings;

   private JTable textMapTable, mathMapTable;

   private Vector<String> columnNames;

   private TeXMapDialog texMapDialog;

   private JComboBox<String> textualShading, textPathOutline;

   private JDRResources resources;
   private FlowframTk application;

   private JFileChooser fileChooser;

   private static final Pattern CS_SUFFIX =
      Pattern.compile("(.*\\\\[a-zA-Z]+) *");
}

class TeXMapRow extends Vector<String>
{
   public TeXMapRow(Integer codePoint, TeXLookup lookup)
   {
      this(codePoint, lookup.getCommand(), lookup.getStyName());
   }

   public TeXMapRow(Integer codePoint, String mapping, String styName)
   {
      super(4);

      this.codePoint = codePoint;

      add(formatKey(codePoint));
      add(new String(new int[]{codePoint.intValue()}, 0, 1));
      add(mapping);
      add(styName == null || styName.equals("none") ? "" : styName);
   }

   public static String formatKey(Integer key)
   {
      return String.format("U+%05X", key.intValue());
   }

   public Integer getKey()
   {
      return codePoint;
   }

   public String getCommand()
   {
      return get(2);
   }

   public String getStyName()
   {
      return get(3);
   }

   private Integer codePoint;
}

class TeXMapDialog extends JDialog
  implements ActionListener,KeyListener
{
   public TeXMapDialog(JDialog parent, JDRResources resources)
   {
      super(parent, resources.getMessage("textconfig.mapping.title"), true);

      this.resources = resources;
      setIconImage(resources.getSmallAppIcon().getImage());

      JPanel mainPanel = new JPanel();
      getContentPane().add(mainPanel, "Center");

      JLabel hexLabel = resources.createAppLabel("textconfig.mapping.codepoint");
      mainPanel.add(hexLabel);

      symbolHexField = new HexField(0);
      symbolHexField.setColumns(5);
      symbolHexField.addKeyListener(this);
      hexLabel.setLabelFor(symbolHexField);
      mainPanel.add(symbolHexField);

      JLabel symbolLabel = resources.createAppLabel("textconfig.mapping.character");
      mainPanel.add(symbolLabel);

      symbolField = new JTextField();
      symbolField.setColumns(2);
      symbolField.setHorizontalAlignment(JTextField.CENTER);
      symbolField.addKeyListener(this);
      symbolLabel.setLabelFor(symbolField);
      mainPanel.add(symbolField);

      symbolField.setDocument(new PlainDocument()
      {
         public void insertString(int offs, String str, AttributeSet a)
            throws BadLocationException
         {
            if (str == null) return;

            String oldString = getText(0, getLength());
            String newString = oldString.substring(0,offs)
               +str+oldString.substring(offs);

            if (newString.length() <= 1)
            {
               super.insertString(offs, str, a);
            }
         }
      });

      JLabel commandLabel = resources.createAppLabel("textconfig.mapping.command");
      mainPanel.add(commandLabel);

      commandField = new JTextField(10);
      commandLabel.setLabelFor(commandField);
      mainPanel.add(commandField);

      JLabel styLabel = resources.createAppLabel("textconfig.mapping.package");
      mainPanel.add(styLabel);

      styField = new JTextField(8);
      styLabel.setLabelFor(styField);
      mainPanel.add(styField);

      JPanel buttonPanel = new JPanel();
      getContentPane().add(buttonPanel, "South");

      buttonPanel.add(resources.createOkayButton(this));
      buttonPanel.add(resources.createCancelButton(this));

      try
      {
         buttonPanel.add(resources.createHelpDialogButton(this, "mi:newtexmappings"));
      }
      catch (HelpSetNotInitialisedException e)
      {
         resources.internalError(null, e);
      }

      pack();
      setLocationRelativeTo(parent);
   }

   public TeXMapRow requestMapping()
   {
      symbolHexField.setText("");
      symbolField.setText("");
      commandField.setText("");
      styField.setText("");

      success = false;
      setVisible(true);

      if (success)
      {
         return new TeXMapRow(new Integer(symbolHexField.getInt()),
            getCommand(), getStyName());
      }
      else
      {
         return null;
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("okay"))
      {
         if (symbolHexField.getInt() == 0)
         {
            resources.error(this,
               resources.getMessage("error.invalid_codepoint",
                  symbolHexField.getText()));

            return;
         }

         success = true;
         setVisible(false);
      }
      else if (action.equals("cancel"))
      {
         setVisible(false);
      }
   }

   public String getCommand()
   {
      return commandField.getText();
   }

   public String getStyName()
   {
      return styField.getText();
   }

   public void keyPressed(KeyEvent evt)
   {
   }

   public void keyReleased(KeyEvent evt)
   {
      Object source = evt.getSource();

      if (source == symbolHexField)
      {
         updateSymbolField();
      }
      else if (source == symbolField)
      {
         updateSymbolHexField();
      }
   }

   public void keyTyped(KeyEvent evt)
   {
      Object source = evt.getSource();

      if (source == symbolHexField)
      {
         updateSymbolField();
      }
      else if (source == symbolField)
      {
         updateSymbolHexField();
      }
   }

   private void updateSymbolField()
   {
      int codePoint = 0;

      try
      {
         codePoint = symbolHexField.getInt();
      }
      catch (NumberFormatException e)
      {
      }

      if (codePoint > 0 && Character.isDefined(codePoint))
      {
         StringBuffer buffer = new StringBuffer(2);
         buffer.appendCodePoint(codePoint);
         symbolField.setText(buffer.toString());
      }
      else
      {
         symbolField.setText("");
      }
   }

   private void updateSymbolHexField()
   {
      String text = symbolField.getText();

      if (text.isEmpty())
      {
         symbolHexField.setText("");
      }
      else
      {
         symbolHexField.setValue(text.codePointAt(0));
      }
   }

   private boolean success;
   private JTextField symbolField, commandField, styField;
   private HexField symbolHexField;
   private JDRResources resources;
}

class PreambleConfigPanel extends JPanel
  implements ActionListener,MouseListener
{
   public PreambleConfigPanel(JDialog parent, FlowframTk application)
   {
      super(new BorderLayout());
      this.application = application;
      this.parent = parent;

      File file = application.getConfigPreambleFile();

      add(getResources().createAppInfoArea("preambleconfig.info",
        file.toString()), BorderLayout.NORTH);

      popupM = new JPopupMenu();

      texEditorPanel = new TeXEditorPanel(application, parent, popupM,
         popupM);

      popupM.addSeparator();

      JDRButtonItem reloadItem = getResources().createButtonItem(
        "preambleconfig", "reload", this, texEditorPanel.getToolBar(), popupM);

      add(texEditorPanel, BorderLayout.CENTER);

      texEditorPanel.addMouseListener(this);
      texEditorPanel.getTextPane().addMouseListener(this);

      try
      {
         reload();
      }
      catch (IOException e)
      {
         getResources().debugMessage(e);
      }
   }

   public void reload() throws IOException
   {
      texEditorPanel.initialise(application.getConfigPreamble());
   }

   public void initialise()
   {
      texEditorPanel.setModified(false);
      texEditorPanel.getTextPane().requestFocusInWindow();
   }

   public void okay() throws IOException
   {
      if (texEditorPanel.isModified())
      {
         File file = application.getConfigPreambleFile();

         PrintWriter out = null;

         try
         {
            out = new PrintWriter(file);

            out.println(texEditorPanel.getText());
         }
         finally
         {
            if (out != null) out.close();
         }
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("reload"))
      {
         try
         {
            reload();
         }
         catch (IOException e)
         {
            getResources().error(parent, e);
         }
      }
   }

   public void mouseClicked(MouseEvent evt)
   {
   }

   public void mouseExited(MouseEvent evt)
   {
   }

   public void mouseEntered(MouseEvent evt)
   {
   }

   public void mousePressed(MouseEvent evt)
   {
      if (evt.isPopupTrigger())
      {
         popupM.show(evt.getComponent(), evt.getX(), evt.getY());
      }
   }

   public void mouseReleased(MouseEvent evt)
   {
      if (evt.isPopupTrigger())
      {
         popupM.show(evt.getComponent(), evt.getX(), evt.getY());
      }
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   private FlowframTk application;

   private TeXEditorPanel texEditorPanel;

   private JPopupMenu popupM;

   private JDialog parent;
}
