// File          : ConfigSettingsDialog.java
// Description   : Dialog for configuring application settings
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
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

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.filter.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for configuring application settings.
 */

public class ConfigSettingsDialog extends JDialog
   implements ActionListener
{
   public ConfigSettingsDialog(FlowframTk application, 
      JDRAppSelector appSelector)
   {
      super(application,
         application.getResources().getMessage("config.title"), true);
      application_ = application;

      JDRResources resources = application.getResources();

      JTabbedPane tabbedPane = new JTabbedPane();
      getContentPane().add(tabbedPane, "Center");

      int idx=0;

      CanvasGraphics cg = application.getDefaultCanvasGraphics();

      JComponent panel = Box.createVerticalBox();

      controlPointsPanel = new ControlSizePanel(resources, cg);
      panel.add(controlPointsPanel);

      storageUnitPanel = new StorageUnitPanel(resources);
      storageUnitPanel.setBorder(BorderFactory.createLoweredBevelBorder());

      panel.add(storageUnitPanel);

      tabbedPane.addTab(resources.getMessage("controls.title"),
         null, panel,
         resources.getMessage("tooltip.controls"));
      tabbedPane.setMnemonicAt(idx++,
         resources.getCodePoint("controls.mnemonic"));

      dirPanel = new DirPanel(resources);

      tabbedPane.addTab(resources.getMessage("startdir.title"),
         null, dirPanel, resources.getMessage("tooltip.startdir"));
      tabbedPane.setMnemonicAt(idx++,
         resources.getCodePoint("startdir.mnemonic"));

      jdrPanel = new JDRSettingsPanel(resources);

      tabbedPane.addTab(resources.getMessage("jdr.title"), null,
        jdrPanel, resources.getMessage("tooltip.jdr"));
      tabbedPane.setMnemonicAt(idx++,
        resources.getCodePoint("jdr.mnemonic"));

      initAppSettingsPanel = new InitAppSettingsPanel(resources);

      tabbedPane.addTab(resources.getMessage("initsettings.title"),
         null, initAppSettingsPanel,
         resources.getMessage("tooltip.initsettings"));
      tabbedPane.setMnemonicAt(idx++,
         resources.getCodePoint("initsettings.mnemonic"));

      bitmapPanel = new BitmapPanel(resources);

      tabbedPane.addTab(resources.getMessage("bitmapconfig.title"), null,
        bitmapPanel, resources.getMessage("bitmapconfig.tooltip"));
      tabbedPane.setMnemonicAt(idx++,
        resources.getCodePoint("bitmapconfig.mnemonic"));

      processesPanel = new ProcessesPanel(application, appSelector);

      tabbedPane.addTab(resources.getMessage("processes.title"), null,
        new JScrollPane(processesPanel), resources.getMessage("processes.tooltip"));
      tabbedPane.setMnemonicAt(idx++,
        resources.getCodePoint("processes.mnemonic"));

      // OK/Cancel Button panel

      JPanel p = new JPanel();
      getContentPane().add(p, "South");

      resources.createOkayCancelHelpButtons(this, p, this, "sec:configuredialog");

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

   public void display()
   {
      JDRFrame frame = application_.getCurrentFrame();

      CanvasGraphics cg;

      if (frame == null)
      {
         cg = application_.getDefaultCanvasGraphics();
      }
      else
      {
         cg = frame.getCanvasGraphics();
      }

      controlPointsPanel.initialise(cg);
      dirPanel.initialise(application_, cg);
      jdrPanel.initialise(application_, cg);
      storageUnitPanel.initialise(application_, cg);
      initAppSettingsPanel.initialise(application_, cg);
      bitmapPanel.initialise(application_, cg);
      processesPanel.initialise(application_);

      setVisible(true);
   }

   public void okay()
   {
      controlPointsPanel.okay(application_);
      dirPanel.okay(application_);
      jdrPanel.okay(application_);
      storageUnitPanel.okay(application_);
      initAppSettingsPanel.okay(application_);
      bitmapPanel.okay(application_);
      processesPanel.okay(application_);

      application_.repaint();

      setVisible(false);
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "ConfigSettingsDialog:"+eol;
      str += "has focus: "+hasFocus()+eol;

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private DirPanel dirPanel;
   private JDRSettingsPanel jdrPanel;
   private InitAppSettingsPanel initAppSettingsPanel;
   private ControlSizePanel controlPointsPanel;
   private StorageUnitPanel storageUnitPanel;
   private BitmapPanel bitmapPanel;
   private ProcessesPanel processesPanel;

   private FlowframTk application_;
}

class JDRSettingsPanel extends JPanel
{
   public JDRSettingsPanel(JDRResources resources)
   {
      super();

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      add(Box.createVerticalStrut(10));
      Box b = Box.createHorizontalBox();
      add(b);

      JLabel saveLabel = resources.createAppLabel("jdr.on_save");

      b.add(saveLabel);

      String[] saveStrings = new String[3];

      saveStrings[JDRAJR.ALL_SETTINGS] 
         = resources.getMessage("jdr.on_save.all");

      saveStrings[JDRAJR.NO_SETTINGS]
         = resources.getMessage("jdr.on_save.none");

      saveStrings[JDRAJR.PAPER_ONLY]
         = resources.getMessage("jdr.on_save.paper");

      saveBox = new JComboBox<String>(saveStrings);
      saveLabel.setLabelFor(saveBox);

      saveBox.setPreferredSize(saveBox.getMinimumSize());
      saveBox.setMaximumSize(saveBox.getMinimumSize());

      b.add(saveBox);
      b.add(Box.createHorizontalGlue());

      add(Box.createVerticalStrut(10));
      b = Box.createHorizontalBox();
      add(b);

      JLabel loadLabel = resources.createAppLabel("jdr.on_load");

      Dimension saveLabelDim = saveLabel.getPreferredSize();
      Dimension loadLabelDim = loadLabel.getPreferredSize();

      int width = (int)Math.max(saveLabelDim.getWidth(),
                                loadLabelDim.getWidth());

      saveLabelDim.width = width;
      loadLabelDim.width = width;

      saveLabel.setPreferredSize(saveLabelDim);
      loadLabel.setPreferredSize(loadLabelDim);

      b.add(loadLabel);

      String[] loadStrings = new String[3];

      loadStrings[JDRAJR.ALL_SETTINGS]
          = resources.getMessage("jdr.on_load.all");

      loadStrings[JDRAJR.NO_SETTINGS]
          = resources.getMessage("jdr.on_load.none");

      loadStrings[JDRAJR.PAPER_ONLY]
          = resources.getMessage("jdr.on_load.paper");

      loadBox = new JComboBox<String>(loadStrings);
      loadLabel.setLabelFor(loadBox);

      loadBox.setPreferredSize(loadBox.getMinimumSize());
      loadBox.setMaximumSize(loadBox.getMinimumSize());

      b.add(loadBox);
      b.add(Box.createHorizontalGlue());

      add(Box.createVerticalStrut(12));
      b = Box.createHorizontalBox();
      add(b);

      warnOld = resources.createAppCheckBox("jdr", "warn_load_old", true, null);

      b.add(warnOld);
      b.add(Box.createHorizontalGlue());

      add(Box.createVerticalGlue());
   }

   public void initialise(FlowframTk application, CanvasGraphics cg)
   {
      saveBox.setSelectedIndex(application.getSaveJDRsettings());
      loadBox.setSelectedIndex(application.useJDRsettings());

      warnOld.setSelected(application.warnOnOldJdr());
   }

   public void okay(FlowframTk application)
   {
      application.setJDRsettings(saveBox.getSelectedIndex(),
        loadBox.getSelectedIndex(), warnOld.isSelected());
   }

   private JComboBox<String> saveBox, loadBox;
   private JCheckBox warnOld;
}

class BitmapPanel extends JPanel
{
   public BitmapPanel(JDRResources resources)
   {
      super(new BorderLayout());

      relativePaths = resources.createAppCheckBox(
         "bitmapconfig", "relative_paths", false, null);

      add(relativePaths, "North");

      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      add(panel, "Center");

      JLabel latexCmdLabel = resources.createAppLabel(
         "bitmapconfig.default_command");
      panel.add(latexCmdLabel);

      latexCmdBox = new JComboBox<String>(
        new String[]{"\\pgfimage", "\\includegraphics"});
      latexCmdBox.setEditable(true);
      latexCmdLabel.setLabelFor(latexCmdBox);

      panel.add(latexCmdBox);
   }

   public void initialise(FlowframTk application, CanvasGraphics cg)
   {
      relativePaths.setSelected(application.useRelativeBitmaps());
      latexCmdBox.setSelectedItem(application.getDefaultBitmapCommand());
   }

   public void okay(FlowframTk application)
   {
      application.setRelativeBitmaps(relativePaths.isSelected());
      application.setDefaultBitmapCommand(
         (String)latexCmdBox.getSelectedItem());
   }

   private JCheckBox relativePaths;
   private JComboBox<String> latexCmdBox;
}

class InitAppSettingsPanel extends JPanel
{
   public InitAppSettingsPanel(JDRResources resources)
   {
      super();

      Box b = Box.createVerticalBox();

      add(b);

      ButtonGroup useGroup = new ButtonGroup();

      useLast = resources.createAppRadioButton("initsettings", "last",
        useGroup, true, null);
      useLast.setAlignmentX(Component.LEFT_ALIGNMENT);
      b.add(useLast);

      useCurrent = resources.createAppRadioButton("initsettings", "current",
        useGroup, false, null);
      useCurrent.setAlignmentX(Component.LEFT_ALIGNMENT);
      b.add(useCurrent);

      useDefault = resources.createAppRadioButton("initsettings", "default",
        useGroup, false, null);
      useDefault.setAlignmentX(Component.LEFT_ALIGNMENT);
      b.add(useDefault);
   }

   public void initialise(FlowframTk application, CanvasGraphics cg)
   {
      original = application.initSettings();

      switch (original)
      {
         case FlowframTkSettings.INIT_LAST :
            useLast.setSelected(true);
         break;
         case FlowframTkSettings.INIT_DEFAULT :
            useDefault.setSelected(true);
         break;
         case FlowframTkSettings.INIT_USER :
            useCurrent.setSelected(true);
         break;
      }
   }

   public void okay(FlowframTk application)
   {
      int selected;

      if (useLast.isSelected())
      {
         selected = FlowframTkSettings.INIT_LAST;
      }
      else if (useDefault.isSelected())
      {
         selected = FlowframTkSettings.INIT_DEFAULT;
      }
      else
      {
         selected = FlowframTkSettings.INIT_USER;
      }

      if (selected != original)
      {
         application.setInitSettings(selected);
      }
   }

   private JRadioButton useLast, useDefault, useCurrent;

   private int original = -1;
}

class DirPanel extends JPanel
   implements ActionListener
{
   public DirPanel(JDRResources resources)
   {
      super();

      ButtonGroup group = new ButtonGroup();

      Box box = Box.createVerticalBox();

      cwd = resources.createAppRadioButton("startdir", "cwd", group,
         true, this);
      cwd.setAlignmentX(Component.LEFT_ALIGNMENT);

      box.add(cwd);

      lastDir = resources.createAppRadioButton("startdir", "last", group,
         false, this);
      lastDir.setAlignmentX(Component.LEFT_ALIGNMENT);

      box.add(lastDir);

      Box panel = Box.createHorizontalBox();

      panel.setAlignmentX(Component.LEFT_ALIGNMENT);

      named = resources.createAppRadioButton("startdir", "named", group,
         false, this);

      panel.add(named);

      directory = new JTextField(10);
      panel.add(directory);

      browse = resources.createAppJButton("startdir", "browse", this);

      panel.add(browse);

      box.add(panel);

      add(box);

      fc = new JFileChooser();
      fc.setCurrentDirectory(new File("."));
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.setApproveButtonText(resources.getMessage("button.okay"));
      fc.setApproveButtonMnemonic(resources.getCodePoint("button.okay.mnemonic"));
   }

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource();

      if (source == cwd || source == lastDir)
      {
         directory.setEnabled(false);
         browse.setEnabled(false);
      }
      else if (source == named)
      {
         directory.setEnabled(true);
         browse.setEnabled(true);
      }
      else if (source == browse)
      {
         int result = fc.showOpenDialog(this);

         if (result == JFileChooser.APPROVE_OPTION)
         {
            directory.setText(fc.getSelectedFile().getAbsolutePath());
         }
      }
   }

   public void initialise(FlowframTk application, CanvasGraphics cg)
   {
      int type = application.getStartDirType();

      switch (type)
      {
         case FlowframTkSettings.STARTDIR_CWD:
            cwd.setSelected(true);
            directory.setEnabled(false);
            browse.setEnabled(false);
         break;
         case FlowframTkSettings.STARTDIR_LAST:
            lastDir.setSelected(true);
            directory.setEnabled(false);
            browse.setEnabled(false);
         break;
         case FlowframTkSettings.STARTDIR_NAMED:
            named.setSelected(true);
            directory.setText(application.getStartDirectory());
            directory.setEnabled(true);
            browse.setEnabled(true);
         break;
         default :
            application.getResources().internalError(this,
               application.getResources().getMessage(
                 "internal_error.invalid_startdir")+ ": " +type);
      }
   }

   public void okay(FlowframTk application)
   {
      int type;

      if (cwd.isSelected())
      {
         type = FlowframTkSettings.STARTDIR_CWD;
      }
      else if (lastDir.isSelected())
      {
         type = FlowframTkSettings.STARTDIR_LAST;
      }
      else
      {
         type = FlowframTkSettings.STARTDIR_NAMED;
      }

      application.setStartDirectory(type, directory.getText());
   }

   private JRadioButton cwd, lastDir, named;
   private JButton browse;
   private JTextField directory;
   private JFileChooser fc;
}

class ControlSizePanel extends JPanel
{
   public ControlSizePanel(JDRResources resources, CanvasGraphics cg)
   {
      super();

      setBorder(BorderFactory.createLoweredBevelBorder());

      JLabel label = resources.createAppLabel("controls.control_size");
      add(label);

      controlSize = resources.createNonNegativeLengthPanel();
      label.setLabelFor(controlSize.getTextField());
      controlSize.setLength(new JDRLength(resources.getMessageSystem(), 
         10, JDRUnit.bp));

      add(controlSize);

      scaleControlsBox = resources.createAppCheckBox("controls",
         "scale_controls", false, null);

      scaleControlsBox.setSelected(cg.isScaleControlPointsEnabled());
      add(scaleControlsBox);

   }

   public void initialise(CanvasGraphics cg)
   {
      controlSize.setLength(cg.getPointSize());
      scaleControlsBox.setSelected(cg.isScaleControlPointsEnabled());
   }

   public void okay(FlowframTk application)
   {
      application.setPointSize(controlSize.getLength(),
                               scaleControlsBox.isSelected());
   }

   private NonNegativeLengthPanel controlSize;

   private JCheckBox scaleControlsBox;
}

class ProcessesPanel extends JPanel implements ItemListener
{
   public ProcessesPanel(FlowframTk application, JDRAppSelector appSelector)
   {
      super(null);

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      setAlignmentX(Component.LEFT_ALIGNMENT);

      JDRResources resources = application.getResources();

      JLabelGroup grp = new JLabelGroup();
      JLabel label;

      JLabel timeoutLabel = resources.createAppLabel("processes.timeout");
      grp.add(timeoutLabel);

      timeoutModel = new SpinnerNumberModel(
        300000L, Long.valueOf(0L), null, Long.valueOf(1));

      timeoutField = new JSpinner(timeoutModel);
      JSpinner.DefaultEditor ed = (JSpinner.DefaultEditor)timeoutField.getEditor();
      ed.getTextField().setColumns(9);

      timeoutLabel.setLabelFor(timeoutField);

      JComponent row = Box.createHorizontalBox();
      row.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(row);

      row.add(timeoutLabel);
      row.add(timeoutField);
      row.add(new JLabel(resources.getMessage("processes.millisecs")));
      row.add(Box.createHorizontalGlue());

      label = resources.createAppLabel("processes.pdflatex");
      grp.add(label);

      pdflatexPanel = new ProcessSettingsPanel(application, appSelector,
        "pdflatex", label, grp);

      add(pdflatexPanel);

      label = resources.createAppLabel("processes.pdftopng");
      grp.add(label);

      pdftopngPanel = new ProcessSettingsPanel(application, appSelector, 
       application.getPdfToPngApp(), label, grp);

      add(pdftopngPanel);

      supportEpsSvgExportBox = resources.createAppCheckBox("processes",
         "support_eps_svg", application.isSupportExportEpsSvgEnabled(), null);
      supportEpsSvgExportBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      supportEpsSvgExportBox.addItemListener(this);

      add(supportEpsSvgExportBox);

      label = resources.createAppLabel("processes.latex");
      grp.add(label);

      latexPanel = new ProcessSettingsPanel(application, appSelector,
        "latex", label, grp);

      add(latexPanel);

      label = resources.createAppLabel("processes.dvips");
      grp.add(label);

      dvipsPanel = new ProcessSettingsPanel(application, appSelector,
        "dvips", label, grp);

      add(dvipsPanel);

      label = resources.createAppLabel("processes.dvisvgm");
      grp.add(label);

      dvisvgmPanel = new ProcessSettingsPanel(application, appSelector,
        "dvisvgm", label, grp);

      add(dvisvgmPanel);

      JTextArea libgsArea =
         resources.createAppInfoArea(20, "appselect.libgs");

      libgsInfoRow = Box.createHorizontalBox();
      libgsInfoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(libgsInfoRow);

      libgsInfoRow.add(libgsArea);

      String libGs = application.getLibgs();

      libgsRow = Box.createHorizontalBox();
      libgsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
      add(libgsRow);

      label = resources.createAppLabel("processes.libgs");
      grp.add(label);

      libgsField = new FileField(resources, this, libGs,
         appSelector.getFileChooser());
      libgsField.setLabel(label);

      libgsField.getEastComponent().add(Box.createHorizontalStrut(20));

      libgsRow.add(label);
      libgsRow.add(libgsField);

      add(Box.createVerticalGlue());

      boolean supportDviOptions = supportEpsSvgExportBox.isSelected();

      latexPanel.setVisible(supportDviOptions);
      dvipsPanel.setVisible(supportDviOptions);
      dvisvgmPanel.setVisible(supportDviOptions);
      libgsInfoRow.setVisible(supportDviOptions);
      libgsRow.setVisible(supportDviOptions);
   }

   @Override
   public void itemStateChanged(ItemEvent evt)
   {
      if (evt.getSource() == supportEpsSvgExportBox)
      {
         boolean supportDviOptions = supportEpsSvgExportBox.isSelected();

         latexPanel.setVisible(supportDviOptions);
         dvipsPanel.setVisible(supportDviOptions);
         dvisvgmPanel.setVisible(supportDviOptions);
         libgsInfoRow.setVisible(supportDviOptions);
         libgsRow.setVisible(supportDviOptions);
      }
   }

   public void initialise(FlowframTk application)
   {
      ExportSettings exportSettings = application.getExportSettings();

      pdflatexPanel.initialise(exportSettings.pdfLaTeXApp,
        exportSettings.pdfLaTeXOptions);

      pdftopngPanel.initialise(exportSettings.pdftopngApp,
        exportSettings.pdftopngOptions);

      boolean supportDviOptions = application.isSupportExportEpsSvgEnabled();

      supportEpsSvgExportBox.setSelected(supportDviOptions);

      latexPanel.initialise(exportSettings.dviLaTeXApp,
        exportSettings.dviLaTeXOptions);

      dvipsPanel.initialise(exportSettings.dvipsApp,
        exportSettings.dvipsOptions);

      dvisvgmPanel.initialise(exportSettings.dvisvgmApp,
        exportSettings.dvisvgmOptions);

      libgsField.setFileName(exportSettings.libgs);

      timeoutModel.setValue(Long.valueOf(exportSettings.timeout));
   }

   public void okay(FlowframTk application)
   {
      ExportSettings exportSettings = application.getExportSettings();

      // PDF LaTeX
      exportSettings.pdfLaTeXApp = pdflatexPanel.getFileName();
      exportSettings.pdfLaTeXOptions = pdflatexPanel.getOptionArray();

      // PDF to PNG
      exportSettings.pdftopngApp = pdftopngPanel.getFileName();
      exportSettings.pdftopngOptions = pdftopngPanel.getOptionArray();

      exportSettings.timeout = timeoutModel.getNumber().longValue();

      boolean supportDviOptions = supportEpsSvgExportBox.isSelected();

      if (application.isSupportExportEpsSvgEnabled()
           != supportDviOptions)
      {
         application.setSupportExportEpsSvg(supportDviOptions);
      }

      if (supportDviOptions)
      {
         // DVI LaTeX
         exportSettings.dviLaTeXApp = latexPanel.getFileName();
         exportSettings.dviLaTeXOptions = latexPanel.getOptionArray();

         // dvips
         exportSettings.dvipsApp = dvipsPanel.getFileName();
         exportSettings.dvipsOptions = dvipsPanel.getOptionArray();

         // dvisvgm
         exportSettings.dvisvgmApp = dvisvgmPanel.getFileName();
         exportSettings.dvisvgmOptions = dvisvgmPanel.getOptionArray();

         exportSettings.libgs = libgsField.getFileName();
      }
   }

   private FileField libgsField;

   private ProcessSettingsPanel latexPanel, pdflatexPanel,
     dvipsPanel, dvisvgmPanel, pdftopngPanel;
   private JComponent libgsRow, libgsInfoRow;

   private JCheckBox supportEpsSvgExportBox;

   private JSpinner timeoutField;
   private SpinnerNumberModel timeoutModel;
}

