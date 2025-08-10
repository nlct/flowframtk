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

      JTabbedPane tabbedPane = new JTabbedPane();
      getContentPane().add(tabbedPane, "Center");

      int idx=0;

      CanvasGraphics cg = application.getDefaultCanvasGraphics();

      JComponent panel = Box.createVerticalBox();

      controlPointsPanel = new ControlSizePanel(getResources(), cg);
      panel.add(controlPointsPanel);

      storageUnitPanel = new StorageUnitPanel(getResources());
      storageUnitPanel.setBorder(BorderFactory.createLoweredBevelBorder());

      panel.add(storageUnitPanel);

      tabbedPane.addTab(getResources().getMessage("controls.title"),
         null, panel,
         getResources().getMessage("tooltip.controls"));
      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("controls.mnemonic"));

      dirPanel = new DirPanel(getResources());

      tabbedPane.addTab(getResources().getMessage("startdir.title"),
         null, dirPanel, getResources().getMessage("tooltip.startdir"));
      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("startdir.mnemonic"));

      jdrPanel = new JDRSettingsPanel(getResources());

      tabbedPane.addTab(getResources().getMessage("jdr.title"), null,
        jdrPanel, getResources().getMessage("tooltip.jdr"));
      tabbedPane.setMnemonicAt(idx++,
        getResources().getCodePoint("jdr.mnemonic"));

      initAppSettingsPanel = new InitAppSettingsPanel(getResources());

      tabbedPane.addTab(getResources().getMessage("initsettings.title"),
         null, initAppSettingsPanel,
         getResources().getMessage("tooltip.initsettings"));
      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("initsettings.mnemonic"));

      bitmapPanel = new BitmapPanel(getResources());

      tabbedPane.addTab(getResources().getMessage("bitmapconfig.title"), null,
        bitmapPanel, getResources().getMessage("bitmapconfig.tooltip"));
      tabbedPane.setMnemonicAt(idx++,
        getResources().getCodePoint("bitmapconfig.mnemonic"));

      processesPanel = new ProcessesPanel(application, appSelector);

      tabbedPane.addTab(getResources().getMessage("processes.title"), null,
        processesPanel, getResources().getMessage("processes.tooltip"));
      tabbedPane.setMnemonicAt(idx++,
        getResources().getCodePoint("processes.mnemonic"));

      // OK/Cancel Button panel

      JPanel p = new JPanel();
      getContentPane().add(p, "South");

      p.add(getResources().createOkayButton(getRootPane(), this));
      p.add(getResources().createCancelButton(this));

      try
      {
         p.add(getResources().createHelpDialogButton(this, "sec:configuredialog"));
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

      JLabel label = resources.createAppLabel("render.control_size");
      add(label);

      controlSize = resources.createNonNegativeLengthPanel();
      label.setLabelFor(controlSize.getTextField());
      controlSize.setLength(new JDRLength(resources.getMessageSystem(), 
         10, JDRUnit.bp));

      add(controlSize);

      scaleControlsBox = resources.createAppCheckBox("render",
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

class ProcessesPanel extends JPanel
{
   public ProcessesPanel(FlowframTk application, JDRAppSelector appSelector)
   {
      super(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();

      JDRResources resources = application.getResources();

      gbc.gridx=0;
      gbc.gridy=0;
      gbc.gridwidth=1;
      gbc.gridheight=1;
      gbc.fill=GridBagConstraints.BOTH;
      gbc.anchor=GridBagConstraints.LINE_START;
      gbc.weightx=1;
      gbc.weighty=1;

      String latexApp = application.getLaTeXApp();
      String pdflatexApp = application.getPdfLaTeXApp();
      String dvipsApp = application.getDvipsApp();
      String dvisvgmApp = application.getDvisvgmApp();
      String libGs = application.getLibgs();

      if (dvisvgmApp == null || dvisvgmApp.isEmpty())
      {
         File file = appSelector.findApp("dvisvgm");

         if (file != null)
         {
            dvisvgmApp = file.getAbsolutePath();
         }

         // User's version of dvisvgm may have been precompiled to
         // use PostScript, so only attempt to locate libgs if
         // dvisvgm hasn't been set.
         if (libGs == null)
         {
            libGs = ExportToSvgSettings.getDefaultLibgs(appSelector);
         }
      }

      if (latexApp == null)
      {
         File file = appSelector.findApp("latex");

         if (file != null)
         {
            latexApp = file.getAbsolutePath();
         }
      }

      if (pdflatexApp == null)
      {
         File file = appSelector.findApp("pdflatex");

         if (file != null)
         {
            pdflatexApp = file.getAbsolutePath();
         }
      }

      if (dvipsApp == null)
      {
         File file = appSelector.findApp("dvips");

         if (file != null)
         {
            dvipsApp = file.getAbsolutePath();
         }
      }

      if (dvisvgmApp == null)
      {
         File file = appSelector.findApp("dvisvgm");

         if (file != null)
         {
            dvisvgmApp = file.getAbsolutePath();
         }
      }

      JLabel latexLabel = resources.createAppLabel("processes.latex");

      latexField = new FileField(resources, this, latexApp,
         appSelector.getFileChooser());
      latexField.setLabel(latexLabel);

      JLabel pdflatexLabel = resources.createAppLabel("processes.pdflatex");

      pdflatexField = new FileField(resources, this, pdflatexApp,
         appSelector.getFileChooser());
      pdflatexField.setLabel(pdflatexLabel);

      JLabel dvipsLabel = resources.createAppLabel("processes.dvips");

      dvipsField = new FileField(resources, this, dvipsApp,
         appSelector.getFileChooser());
      dvipsField.setLabel(dvipsLabel);

      JLabel dvisvgmLabel = resources.createAppLabel("processes.dvisvgm");

      dvisvgmField = new FileField(resources, this, dvisvgmApp,
         appSelector.getFileChooser());
      dvisvgmField.setLabel(dvisvgmLabel);

      JLabel libgsLabel = resources.createAppLabel("processes.libgs");

      libgsField = new FileField(resources, this, libGs,
         appSelector.getFileChooser());
      libgsField.setLabel(libgsLabel);

      JTextArea libgsArea =
         resources.createAppInfoArea("appselect.libgs");
      libgsArea.setAlignmentX(Component.LEFT_ALIGNMENT);

      JLabel timeoutLabel = resources.createAppLabel("processes.timeout");
      timeoutField = new NonNegativeLongField(300000L);
      timeoutField.setColumns(8);
      timeoutLabel.setLabelFor(timeoutField);

      add(latexLabel, gbc);
      gbc.gridx++;
      add(latexField, gbc);

      gbc.gridy++;
      gbc.gridx=0;

      add(pdflatexLabel, gbc);
      gbc.gridx++;
      add(pdflatexField, gbc);

      gbc.gridy++;
      gbc.gridx=0;

      add(dvipsLabel, gbc);
      gbc.gridx++;
      add(dvipsField, gbc);

      gbc.gridy++;
      gbc.gridx=0;

      add(dvisvgmLabel, gbc);
      gbc.gridx++;
      add(dvisvgmField, gbc);

      gbc.gridy++;
      gbc.gridx=0;
      gbc.gridwidth=2;

      add(libgsArea, gbc);

      gbc.gridy++;
      gbc.gridwidth=1;

      add(libgsLabel, gbc);
      gbc.gridx++;
      add(libgsField, gbc);

      gbc.gridy++;
      gbc.gridx=0;

      gbc.fill=GridBagConstraints.HORIZONTAL;
      add(timeoutLabel, gbc);
      gbc.gridx++;

      Box box = Box.createHorizontalBox();
      box.add(timeoutField);
      box.add(new JLabel(resources.getMessage("processes.millisecs")));

      add(box, gbc);
   }

   public void initialise(FlowframTk application)
   {
      String latexApp = application.getLaTeXApp();
      String pdflatexApp = application.getPdfLaTeXApp();
      String dvipsApp = application.getDvipsApp();
      String dvisvgmApp = application.getDvisvgmApp();
      String libGs = application.getLibgs();

      if (latexApp != null && !latexApp.isEmpty())
      {
         latexField.setFileName(latexApp);
      }

      if (pdflatexApp != null && !pdflatexApp.isEmpty())
      {
         pdflatexField.setFileName(pdflatexApp);
      }

      if (dvipsApp != null && !dvipsApp.isEmpty())
      {
         dvipsField.setFileName(dvipsApp);
      }

      if (dvipsApp != null && !dvipsApp.isEmpty())
      {
         dvipsField.setFileName(dvipsApp);

         libgsField.setFileName(libGs == null ? "" : libGs);
      }
      else if (libGs != null && !libGs.isEmpty())
      {
         libgsField.setFileName(libGs);
      }

      timeoutField.setValue(application.getMaxProcessTime());
   }

   public void okay(FlowframTk application)
   {
      application.setLaTeXApp(latexField.getFileName());
      application.setPdfLaTeXApp(pdflatexField.getFileName());
      application.setDvipsApp(dvipsField.getFileName());
      application.setDvisvgmApp(dvisvgmField.getFileName());
      application.setLibgs(libgsField.getFileName());
      application.setMaxProcessTime(timeoutField.getLong());
   }

   private FileField latexField, pdflatexField, dvipsField,
     dvisvgmField, libgsField;

   private NonNegativeLongField timeoutField;
}

