// File          : ConfigUISettingsDialog.java
// Description   : Dialog for configuring UI settings
// Creation Date : 2014-05-04
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006 Nicola L.C. Talbot

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
import java.util.Locale;
import java.util.Arrays;
import java.util.Vector;
import java.util.Enumeration;
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

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.filter.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for configuring UI settings.
 */

public class ConfigUISettingsDialog extends JDialog
   implements ActionListener
{
   public ConfigUISettingsDialog(FlowframTk application)
   {
      super(application,
         application.getResources().getString("configui.title"), true);
      application_ = application;

      JTabbedPane tabbedPane = new JTabbedPane();
      getContentPane().add(tabbedPane, "Center");

      int idx=0;

      JComponent graphicsPanel = new JPanel(new GridBagLayout());

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.anchor=GridBagConstraints.NORTH;
      gbc.gridx=0;
      gbc.gridy=0;
      gbc.weightx=0;
      gbc.weighty=0.5;

      renderPanel = new RenderPanel(getResources());

      graphicsPanel.add(renderPanel, gbc);

      dragScale = new DragScalePanel(getResources());

      gbc.gridy++;
      graphicsPanel.add(dragScale, gbc);

      editPathPanel = new EditPathPanel(application_);
      editPathPanel.setBorder(BorderFactory.createTitledBorder(
        editPathPanel.getName()));

      gbc.gridy++;
      graphicsPanel.add(editPathPanel, gbc);

      controlPointsPanel = new ControlPointsPanel(
         application.getDefaultCanvasGraphics(), getResources());

      gbc.gridy++;
      graphicsPanel.add(controlPointsPanel, gbc);

      tabbedPane.addTab(getResources().getString("graphics.title"),
         null, new JScrollPane(graphicsPanel),
         getResources().getString("tooltip.graphics"));
      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("graphics.mnemonic"));

      JComponent annotationsPanel = new JPanel(new GridBagLayout());

      gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.BOTH;
      gbc.anchor=GridBagConstraints.NORTH;
      gbc.gridx=0;
      gbc.gridy=0;
      gbc.weightx=0;
      gbc.weighty=1;

      annoteFontPanel = new AnnoteFontPanel(application_);

      annotationsPanel.add(annoteFontPanel, gbc);

      gbc.gridy++;
      splashScreenSettingsPanel = new SplashScreenSettingsPanel(application_);

      annotationsPanel.add(splashScreenSettingsPanel, gbc);

      tabbedPane.addTab(getResources().getString("annotations.title"),
         null, new JScrollPane(annotationsPanel),
         getResources().getString("annotations.tooltip"));
      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("annotations.mnemonic"));

      langPanel = new LanguagePanel(application);

      tabbedPane.addTab(getResources().getString("lang.title"),
         null, new JScrollPane(langPanel),
         getResources().getString("lang.tooltip"));
      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("lang.mnemonic"));

      acceleratorPanel = new AcceleratorPanel(this);

      tabbedPane.addTab(getResources().getString("accelerators.title"),
         null, acceleratorPanel,
         getResources().getString("accelerators.tooltip"));
      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("accelerators.mnemonic"));

      rulerFormatPanel = new RulerFormatPanel(application);
      rulerFormatPanel.setBorder(BorderFactory.createLoweredBevelBorder());

      tabbedPane.addTab(getResources().getString("borders.title"),
         null, new JScrollPane(rulerFormatPanel),
         getResources().getString("borders.tooltip"));
      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("ruler.mnemonic"));

      normalizePanel = new NormalizePanel(getResources()); 

      tabbedPane.addTab(getResources().getString("normalize.title"),
         null, new JScrollPane(normalizePanel),
         getResources().getString("tooltip.normalize"));
      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("normalize.mnemonic"));

      texEditorUIPanel = new TeXEditorUIPanel(application_);

      tabbedPane.addTab(getResources().getString("texeditorui.title"),
         null, new JScrollPane(texEditorUIPanel),
         getResources().getString("texeditorui.tooltip"));
      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("texeditorui.mnemonic"));

      lookAndFeelPanel = new LookAndFeelPanel(application_);

      tabbedPane.addTab(getResources().getString("lookandfeel.title"),
         null, new JScrollPane(lookAndFeelPanel),
         getResources().getString("lookandfeel.tooltip"));
      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("lookandfeel.mnemonic"));

      vectorizeBitmapUIPanel = new VectorizeBitmapUIPanel(application_);

      tabbedPane.addTab(getResources().getString("vectorizeui.title"),
         null, new JScrollPane(vectorizeBitmapUIPanel),
         getResources().getString("vectorizeui.tooltip"));
      tabbedPane.setMnemonicAt(idx++,
         getResources().getCodePoint("vectorizeui.mnemonic"));

      // OK/Cancel Button panel
      JPanel p = new JPanel();
      getContentPane().add(p, "South");

      p.add(getResources().createOkayButton(this));
      p.add(getResources().createCancelButton(this));
      p.add(getResources().createHelpButton("configureuidialog"));

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
      dragScale.initialise(application_);
      renderPanel.initialise(application_);
      controlPointsPanel.initialise();
      rulerFormatPanel.initialise(application_);
      normalizePanel.initialise(application_);
      langPanel.initialise();
      texEditorUIPanel.initialise(application_);
      annoteFontPanel.initialise(application_.getSettings());
      lookAndFeelPanel.initialise();
      vectorizeBitmapUIPanel.initialise();
      splashScreenSettingsPanel.initialise();
      editPathPanel.initialise();

      setVisible(true);
   }

   public void okay()
   {
      try
      {
         rulerFormatPanel.okay(application_);
         normalizePanel.okay(application_);
      }
      catch (InvalidFormatException e)
      {
         getResources().error(this, e.getMessage());
         return;
      }

      try
      {
         langPanel.okay();
      }
      catch (NumberFormatException e)
      {
         getResources().error(this, e.getMessage());
         return;
      }

      dragScale.okay(application_);
      controlPointsPanel.okay(application_);
      renderPanel.okay(application_);
      texEditorUIPanel.okay(application_);
      annoteFontPanel.okay(application_.getSettings());
      lookAndFeelPanel.okay();
      vectorizeBitmapUIPanel.okay();
      splashScreenSettingsPanel.okay();
      editPathPanel.okay();

      application_.updateAllFrames();

      setVisible(false);
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "ConfigUISettingsDialog:"+eol;
      str += "has focus: "+hasFocus()+eol;

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private DragScalePanel dragScale;
   private RenderPanel renderPanel;
   private LanguagePanel langPanel;
   private ControlPointsPanel controlPointsPanel;
   private RulerFormatPanel rulerFormatPanel;
   private AcceleratorPanel acceleratorPanel;
   private NormalizePanel normalizePanel;
   private TeXEditorUIPanel texEditorUIPanel;
   private AnnoteFontPanel annoteFontPanel;
   private LookAndFeelPanel lookAndFeelPanel;
   private VectorizeBitmapUIPanel vectorizeBitmapUIPanel;
   private SplashScreenSettingsPanel splashScreenSettingsPanel;
   private EditPathPanel editPathPanel;

   private FlowframTk application_;
}

class EditPathPanel extends JPanel
{
   public EditPathPanel(FlowframTk application)
   {
      super(null);
      this.application = application;
      setName(getResources().getString("editpathui.title"));
      setAlignmentX(0f);
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      canvasClickBox = getResources().createAppCheckBox("editpathui",
        "canvasclick", false, null);

      add(canvasClickBox);

      ignoreLockBox = getResources().createAppCheckBox("editpathui",
        "ignorelock", false, null);
      add(ignoreLockBox);

   }

   public void initialise()
   {
      FlowframTkSettings settings = application.getSettings();

      canvasClickBox.setSelected(settings.canvasClickExitsPathEdit);
      ignoreLockBox.setSelected(settings.selectControlIgnoresLock);
   }

   public void okay()
   {
      FlowframTkSettings settings = application.getSettings();

      settings.canvasClickExitsPathEdit = canvasClickBox.isSelected();
      settings.selectControlIgnoresLock = ignoreLockBox.isSelected();
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   private FlowframTk application;
   private JCheckBox canvasClickBox, ignoreLockBox;
}

class SplashScreenSettingsPanel extends JPanel
{
   public SplashScreenSettingsPanel(FlowframTk gui)
   {
      super(null);
      this.resources = gui.getResources();

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      setBorder(BorderFactory.createTitledBorder(
         resources.getString("splash.title")));

      infoFontSelector = new JavaFontSelector(gui, 
        "splash.infofont.name",
        "splash.infofont.bold",
        "splash.infofont.italic",
        "splash.infofont.size");

      add(infoFontSelector);

      versionFontSelector = new JavaFontSelector(gui, 
        "splash.versionfont.name",
        "splash.versionfont.bold",
        "splash.versionfont.italic",
        "splash.versionfont.size");

      add(versionFontSelector);
   }

   public void initialise()
   {
      infoFontSelector.setSelectedFont(resources.getStartUpInfoFont());
      versionFontSelector.setSelectedFont(resources.getStartUpVersionFont());
   }

   public void okay()
   {
      resources.setStartUpInfoFont(infoFontSelector.getSelectedFont());
      resources.setStartUpVersionFont(versionFontSelector.getSelectedFont());
   }

   private JDRResources resources;

   private JavaFontSelector infoFontSelector;
   private JavaFontSelector versionFontSelector;
}

class RenderPanel extends JPanel
{
   public RenderPanel(JDRResources resources)
   {
      super(new GridLayout(2, 3));

      add(resources.createAppLabel("render.anti_alias"));

      ButtonGroup antialiasGroup = new ButtonGroup();

      antialiasOn = resources.createAppRadioButton("render", "on",
         antialiasGroup, false, null);

      add(antialiasOn);

      antialiasOff = resources.createAppRadioButton("render", "off",
         antialiasGroup, true, null);

      add(antialiasOff);

      add(resources.createAppLabel("render.rendering"));

      ButtonGroup renderGroup = new ButtonGroup();

      quality = resources.createAppRadioButton("render", "quality",
        renderGroup, false, null);

      add(quality);

      speed = resources.createAppRadioButton("render", "speed", 
         renderGroup, true, null);

      add(speed);
   }


   public void initialise(FlowframTk application)
   {
      if (application.getAntiAlias())
      {
         antialiasOn.setSelected(true);
      }
      else
      {
         antialiasOff.setSelected(true);
      }

      if (application.getRenderQuality())
      {
         quality.setSelected(true);
      }
      else
      {
         speed.setSelected(true);
      }
   }

   public void okay(FlowframTk application)
   {
      application.setRendering(antialiasOn.isSelected(),
                               quality.isSelected());
   }

   private JRadioButton antialiasOn, antialiasOff;
   private JRadioButton quality, speed;
}

class ControlPointsPanel extends JPanel
{
   public ControlPointsPanel(CanvasGraphics cg, JDRResources resources)
   {
      super(new GridLayout(2,2));

      this.resources = resources;

      controlPaintPanels = new ControlPaintPanel[8];

      // Standard control colour

      addControlPaintPanels(this, 0, "standard", new JDRPoint(cg));

      // Symmetry line control colour

      addControlPaintPanels(this, 2, "symmetry",
         new JDRSymmetryLinePoint(cg));

      // Pattern anchor control colour

      addControlPaintPanels(this, 4, "patternanchor",
         new JDRPatternAnchorPoint(cg));

      // Pattern adjust control colour

      addControlPaintPanels(this, 6, "patternadjust",
         new JDRPatternAdjustPoint(cg));

      // Adjust label dimension so they are all the same width

      Dimension dim = controlPaintPanels[0].getLabel().getPreferredSize();

      double labelWidth = dim.getWidth();
      double labelHeight = dim.getHeight();

      dim = controlPaintPanels[1].getLabel().getPreferredSize();

      if (dim.getWidth() > labelWidth)
      {
         labelWidth = dim.getWidth();
      }

      if (dim.getHeight() > labelHeight)
      {
         labelHeight = dim.getHeight();
      }

      dim.setSize(labelWidth, labelHeight);

      for (int i = 0; i < controlPaintPanels.length; i++)
      {
         controlPaintPanels[i].getLabel().setPreferredSize(dim);
      }
   }

   private void addControlPaintPanels(JComponent parent,
      int index, String tag, JDRPoint point)
   {
      Box box = Box.createVerticalBox();

      box.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(),
        resources.getString("render.control."+tag)));

      controlPaintPanels[index] = new ControlPaintPanel(resources,
         point,
         resources.getMessage("render.control.choosertitle",
           resources.getString("render.control."+tag),
           resources.getString("render.control.unselected")));

      box.add(controlPaintPanels[index]);

      JDRPoint selectedPoint = (JDRPoint)point.clone();
      selectedPoint.setSelected(true);

      controlPaintPanels[index+1] = new ControlPaintPanel(resources,
         selectedPoint,
         resources.getMessage("render.control.choosertitle",
           resources.getString("render.control."+tag),
           resources.getString("render.control.selected")));

      box.add(controlPaintPanels[index+1]);

      parent.add(box);
   }

   public void initialise()
   {
      for (int i = 0; i < controlPaintPanels.length; i++)
      {
         controlPaintPanels[i].initialise();
      }
   }

   public void okay(FlowframTk application)
   {
      for (int i = 0; i < controlPaintPanels.length; i++)
      {
         controlPaintPanels[i].updatePaint();
      }
   }

   private ControlPaintPanel[] controlPaintPanels;

   private JDRResources resources;
}

class ControlPaintPanel extends JPanel implements ActionListener
{
   public ControlPaintPanel(JDRResources resources, JDRPoint p, 
      String chooserTitle)
   {
      this(resources, p, chooserTitle, 10.0, 10.0);
   }

   public ControlPaintPanel(JDRResources resources, JDRPoint p, 
      String chooserTitle, double ptSizeX, double ptSizeY)
   {
      point = p;

      pointSize = new Dimension((int)Math.round(ptSizeX),
                                (int)Math.round(ptSizeY));

      point.x = ptSizeX*0.5+1;
      point.y = ptSizeY*0.5+1;

      label = new JLabel(point.isSelected() ?
                     resources.getString("render.control.selected") :
                     resources.getString("render.control.unselected"));

      paint = point.isSelected() ? point.getSelectedPaint()
                                 : point.getUnselectedPaint();

      samplePanelSize = new Dimension(pointSize.width+2, pointSize.height+2);

      samplePanel = new JPanel()
         {
            public void paintComponent(Graphics g)
            {
               super.paintComponent(g);

               point.draw((Graphics2D)g, pointSize, paint, 1.0, 1.0);
            }

            public Color getBackground()
            {
               return Color.white;
            }

            public Dimension getPreferredSize()
            {
               return samplePanelSize;
            }

            public Dimension getMinimumSize()
            {
               return samplePanelSize;
            }
         };

      add(samplePanel);

      add(label);

      selectButton = resources.createDialogButton("label.choose",
        "choose", this, null);

      add(selectButton);

      colorChooser = new JColorChooser((Color)paint);

      colorChooser.setPreviewPanel
      (
         new JPanel()
         {
            public void paintComponent(Graphics g)
            {
               super.paintComponent(g);

               point.draw((Graphics2D)g, pointSize, colorChooser.getColor(),
                 1.0, 1.0);
            }

            public Color getBackground()
            {
               return Color.white;
            }

            public Dimension getPreferredSize()
            {
               return samplePanelSize;
            }

            public Dimension getMinimumSize()
            {
               return samplePanelSize;
            }
         }
      );

      colorDialog = JColorChooser.createDialog
      (
         this, chooserTitle, true, colorChooser,
         new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               paint = colorChooser.getColor();
               samplePanel.repaint();
            }
         },
         null
      );
   }

   public void initialise()
   {
      paint = point.isSelected() ? point.getSelectedPaint()
                                 : point.getUnselectedPaint();
   }

   public void updatePaint()
   {
      if (point.isSelected())
      {
         point.setSelectedPaint(paint);
      }
      else
      {
         point.setUnselectedPaint(paint);
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource();

      if (source == selectButton)
      {
         colorDialog.setVisible(true);
      }
   }

   public JLabel getLabel()
   {
      return label;
   }

   public String getLabelText()
   {
      return label.getText();
   }

   private JDRPoint point;
   private Color paint;
   private Dimension pointSize;

   private JPanel samplePanel;
   private JButton selectButton;
   private JColorChooser colorChooser;
   private JDialog colorDialog;

   private Dimension samplePanelSize;

   private JLabel label;
}

class DragScalePanel extends JPanel
{
   public DragScalePanel(JDRResources resources)
   {
      super();

      add(new JLabel(resources.getString("hotspots.title")));
      setToolTipText(resources.getString("tooltip.hotspots"));

      ButtonGroup group = new ButtonGroup();

      enable = resources.createDialogRadio(
         "hotspots.enable", "hotspots", null, group, false);

      add(enable);

      disable = resources.createDialogRadio(
         "hotspots.disable", "nohotspots", null, group, true);

      add(disable);
   }

   public void initialise(FlowframTk application)
   {
      boolean flag = application.dragScaleEnabled();

      if (flag)
      {
         enable.setSelected(flag);
      }
      else
      {
         disable.setSelected(!flag);
      }
   }

   public void okay(FlowframTk application)
   {
      application.setDragScale(enable.isSelected());
   }

   private JRadioButton enable, disable;
}

class LanguagePanel extends JPanel
   implements ActionListener, ListSelectionListener
{
   public LanguagePanel(FlowframTk application)
   {
      super(new GridBagLayout());

      this.application = application;

      GridBagConstraints gbc = new GridBagConstraints();

      gbc.gridx=0;
      gbc.gridy=0;
      gbc.gridwidth=2;
      gbc.gridheight=1;
      gbc.fill=GridBagConstraints.HORIZONTAL;
      gbc.anchor=GridBagConstraints.LINE_START;
      gbc.weightx=1;
      gbc.weighty=1;

      add(getResources().createAppInfoArea("lang.info"), gbc);

      gbc.fill=GridBagConstraints.NONE;
      gbc.gridy++;
      gbc.gridwidth=1;

      JLabel dictLabel = getResources().createAppLabel("lang.dict");

      add(dictLabel, gbc);

      dictLangBox = new JComboBox<String>(getResources().getAvailableDictLanguages());
      dictLabel.setLabelFor(dictLangBox);
      dictLangBox.setToolTipText(dictLabel.getToolTipText());

      gbc.gridx++;
      add(dictLangBox, gbc);

      String id = getResources().getDictLocaleId();

      if (id != null)
      {
         dictLangBox.setSelectedItem(id);
      }

      JLabel helpLabel = getResources().createAppLabel("lang.help");

      gbc.gridx=0;
      gbc.gridy++;
      add(helpLabel, gbc);

      helpLangBox = new JComboBox<String>(
         getResources().getAvailableHelpLanguages(
           application.getInvoker().getName().toLowerCase()
         ));
      helpLabel.setLabelFor(helpLangBox);

      helpLangBox.setToolTipText(helpLabel.getToolTipText());

      gbc.gridx++;
      add(helpLangBox, gbc);

      id = getResources().getHelpLocaleId();

      if (id != null)
      {
         helpLangBox.setSelectedItem(id);
      }

      gbc.gridwidth=2;
      gbc.gridx=0;
      gbc.gridy++;
      gbc.fill=GridBagConstraints.BOTH;

      add(getResources().createAppLabel("lang.unicode.blocks"), gbc);

      gbc.gridy++;

      add(getResources().createAppInfoArea("lang.unicode.info"), gbc);

      int[][] ranges = application.getUnicodeRanges();

      Object[][] data = new Object[ranges.length][2];

      for (int i = 0; i < ranges.length; i++)
      {
         data[i][0] = String.format("%05X", ranges[i][0]);
         data[i][1] = String.format("%05X", ranges[i][1]);
      }

      Object[] columnNames = new Object[2];
      columnNames[0] = getResources().getString("lang.unicode.start");
      columnNames[1] = getResources().getString("lang.unicode.end");

      unicodeTable = new JTable(new DefaultTableModel(data, columnNames))
      {
         public void tableChanged(TableModelEvent e)
         {
            super.tableChanged(e);
            changed = true;
         }
      };

      unicodeTable.getSelectionModel().addListSelectionListener(this);

      gbc.gridx=0;
      gbc.gridy++;
      gbc.gridwidth=2;
      gbc.fill=GridBagConstraints.NONE;

      JPanel panel = new JPanel(new BorderLayout());

      panel.add(new JScrollPane(unicodeTable), "Center");

      Box box = Box.createVerticalBox();
      panel.add(box, "East");

      JButton addButton =  getResources().createDialogButton(
        "lang.unicode.add_block", "add.unicode_block", this, null,
        getResources().getString("lang.unicode.add_block.tooltip"));
      box.add(addButton);

      removeButton =  getResources().createDialogButton(
        "lang.unicode.remove_block", "remove.unicode_block", this, null,
        getResources().getString("lang.unicode.remove_block.tooltip"));
      box.add(removeButton);

      add(panel, gbc);

      changed = false;
   }

   public void initialise()
   {
      dictLangBox.setSelectedItem(application.getDictId());
      helpLangBox.setSelectedItem(application.getHelpId());

      if (changed)
      {
         int[][] ranges = application.getUnicodeRanges();

         Object[][] data = new Object[ranges.length][2];

         for (int i = 0; i < ranges.length; i++)
         {
            data[i][0] = String.format("%05X", ranges[i][0]);
            data[i][1] = String.format("%05X", ranges[i][1]);
         }

         Object[] columnNames = new Object[2];
         columnNames[0] = getResources().getString("lang.unicode.start");
         columnNames[1] = getResources().getString("lang.unicode.end");

         ((DefaultTableModel)unicodeTable.getModel())
            .setDataVector(data, columnNames);
      }

      removeButton.setEnabled(unicodeTable.getSelectedRow() != -1);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("remove.unicode_block"))
      {
         int[] indexes = unicodeTable.getSelectedRows();

         DefaultTableModel model = (DefaultTableModel)unicodeTable.getModel();

         for (int i = indexes.length-1; i >= 0; i--)
         {
            model.removeRow(indexes[i]);
         }
      }
      else if (action.equals("add.unicode_block"))
      {
         DefaultTableModel model = (DefaultTableModel)unicodeTable.getModel();

         model.addRow(new String[] {"0", "0"});

         if (unicodeTable.editCellAt(model.getRowCount()-1, 0))
         {
            unicodeTable.getEditorComponent().requestFocus();
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

      if (source == unicodeTable.getSelectionModel())
      {
         removeButton.setEnabled(unicodeTable.getSelectedRow() != -1);
      }
   }

   public void okay()
     throws NumberFormatException
   {
      DefaultTableModel model = (DefaultTableModel)unicodeTable.getModel();

      int[][] ranges = new int[model.getRowCount()][2];

      for (int i = 0; i < ranges.length; i++)
      {
         for (int j = 0; j < 2; j++)
         {
           String value = model.getValueAt(i, j).toString();

           try
           {
              ranges[i][j] = Integer.parseInt(value, 16);
           }
           catch (NumberFormatException e)
           {
              throw new NumberFormatException(
                getResources().getMessage(
               "lang.unicode.invalid_hex", value, (i+1), (j+1)));
           }
         }
      }

      application.setUnicodeRanges(ranges);

      application.setDictId((String)dictLangBox.getSelectedItem());
      application.setHelpId((String)helpLangBox.getSelectedItem());

      changed = false;
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   private JComboBox<String> dictLangBox, helpLangBox;

   private JTable unicodeTable;

   private JButton removeButton;

   private FlowframTk application;

   private boolean changed;
}

class RulerFormatPanel extends JPanel implements ActionListener
{
   public RulerFormatPanel(FlowframTk application)
   {
      super(null);
      resources = application.getResources();

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      setAlignmentX(0f);

      JComponent toolBarComp = createToolBarArea();
      toolBarComp.setBorder(BorderFactory.createTitledBorder(
         resources.getString("borders.toolbars.title")));
      toolBarComp.setAlignmentX(0f);

      add(toolBarComp);

      add(Box.createVerticalStrut(10));

      JComponent rulerComp = createRulerArea(application);
      rulerComp.setBorder(BorderFactory.createTitledBorder(
         resources.getString("borders.rulers.title")));
      rulerComp.setAlignmentX(0f);

      add(rulerComp);

      add(Box.createVerticalStrut(10));

      JComponent statusBarComp = createStatusArea(application);
      statusBarComp.setAlignmentX(0f);
      statusBarComp.setBorder(BorderFactory.createTitledBorder(
        resources.getString("borders.status.title")));

      add(statusBarComp);

      add(Box.createVerticalGlue());
   }

   private JComponent createToolBarArea()
   {
      JComponent comp = Box.createVerticalBox();
      comp.setAlignmentX(JComponent.LEFT_ALIGNMENT);

      showToolsBox = resources.createAppCheckBox(
         "borders", "showtools", true, this);
      showToolsBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      comp.add(showToolsBox);

      toolBarLocationComp = new JPanel(new GridBagLayout());
      toolBarLocationComp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      comp.add(toolBarLocationComp);

      GridBagConstraints gbc = new GridBagConstraints();

      gbc.gridx=0;
      gbc.gridy=0;
      gbc.fill=GridBagConstraints.NONE;
      gbc.anchor=GridBagConstraints.LINE_START;
      gbc.weightx=0.1;
      gbc.weighty=0.25;
      gbc.gridheight=1;
      gbc.gridwidth=1;

      JLabel toolBarLabel = resources.createAppLabel("borders.vtoolbar");
      toolBarLocationComp.add(toolBarLabel, gbc);

      ButtonGroup bg = new ButtonGroup();

      gbc.gridx++;
      gbc.weightx=0.1;

      toolbarLeft = resources.createDialogRadio(
         "borders.vtoolbar.left", "toolbarleft", null, bg, true);
      toolBarLocationComp.add(toolbarLeft, gbc);

      gbc.gridx++;

      toolbarRight = resources.createDialogRadio(
         "borders.vtoolbar.right", "toolbarright", null, bg, true);
      toolBarLocationComp.add(toolbarRight, gbc);

      gbc.gridx++;
      gbc.weightx=0.25;
      toolBarLocationComp.add(
        new JLabel(resources.getString("borders.vtoolbar.info")));

      return comp;
   }

   private JComponent createRulerArea(FlowframTk application)
   {
      sampleTopComp = new JPanel();
      sampleSideComp = new JPanel();

      JComponent rulerComp = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();

      gbc.gridy=0;
      gbc.gridx=0;
      gbc.anchor=GridBagConstraints.BASELINE_LEADING;
      gbc.fill=GridBagConstraints.NONE;
      gbc.weighty=0.25;
      gbc.weightx=0.1;
      gbc.gridwidth=1;

      JLabel heightLabel = resources.createAppLabel("borders.rulerheight");

      rulerComp.add(heightLabel, gbc);

      heightModel = new SpinnerNumberModel(25, 1, 100, 1);
      JSpinner heightField = new JSpinner(heightModel);
      heightLabel.setLabelFor(heightField);

      gbc.gridx++;
      rulerComp.add(heightField, gbc);

      heightField.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            Dimension dim = sampleTopComp.getPreferredSize();
            dim.height = heightModel.getNumber().intValue();

            sampleTopComp.setPreferredSize(dim);
            sampleTopComp.revalidate();
         }
      });

      JPanel sampleView = new JPanel();
      sampleView.setBackground(Color.white);
      JScrollPane sp = new JScrollPane(sampleView);
      sp.setRowHeaderView(sampleSideComp);
      sp.setColumnHeaderView(sampleTopComp);

      gbc.gridx++;
      gbc.gridheight=2;
      gbc.fill=GridBagConstraints.BOTH;
      rulerComp.add(sp, gbc);

      gbc.gridheight=1;
      gbc.fill=GridBagConstraints.NONE;

      gbc.gridx=0;
      gbc.gridy++;
      JLabel widthLabel = resources.createAppLabel("borders.rulerwidth");

      rulerComp.add(widthLabel, gbc);

      widthModel = new SpinnerNumberModel(25, 1, 100, 1);
      JSpinner widthField = new JSpinner(widthModel);
      widthLabel.setLabelFor(widthField);

      gbc.gridx++;
      rulerComp.add(widthField, gbc);

      widthField.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            Dimension dim = sampleSideComp.getPreferredSize();
            dim.width = widthModel.getNumber().intValue();

            sampleSideComp.setPreferredSize(dim);
            sampleSideComp.revalidate();
         }
      });

      gbc.gridy++;
      gbc.gridx=0;
      gbc.gridwidth=1;

      JLabel rulerFontNameLabel = 
         resources.createAppLabel("borders.rulerfont.name");
      rulerComp.add(rulerFontNameLabel, gbc);

      gbc.gridx++;
      gbc.gridwidth=2;

      rulerFontSelector = new JavaFontSelector(application, 
        rulerFontNameLabel,
        "borders.rulerfont.bold",
        "borders.rulerfont.italic",
        "borders.rulerfont.size");

      rulerComp.add(rulerFontSelector, gbc);

      JLabel patternLabel = resources.createAppLabel("borders.rulerpattern");

      gbc.gridy++;
      gbc.gridx=0;
      gbc.gridwidth=1;

      rulerComp.add(patternLabel, gbc);

      patternField = new JTextField(14);
      patternLabel.setLabelFor(patternField);

      gbc.gridx++;
      gbc.gridwidth=2;
      rulerComp.add(patternField, gbc);

      JLabel localeLabel = resources.createAppLabel("borders.rulerlocale");

      gbc.gridy++;
      gbc.gridx=0;
      gbc.gridwidth=1;

      rulerComp.add(localeLabel, gbc);

      Locale[] locales = NumberFormat.getAvailableLocales();

      Arrays.sort(locales, new java.util.Comparator<Locale>()
      {
         public int compare(Locale locale1, Locale locale2)
         {
            return locale1.getDisplayName().compareTo(locale2.getDisplayName());
         }
      });

      localeBox = new JComboBox<Locale>(locales);
      localeLabel.setLabelFor(localeBox);
      localeBox.setRenderer(new LocaleComboBoxRenderer());

      gbc.gridx++;
      gbc.gridwidth=2;
      rulerComp.add(localeBox, gbc);

      gbc.gridy++;
      gbc.gridx=0;
      gbc.gridwidth=3;
      gbc.fill=GridBagConstraints.BOTH;

      rulerComp.add(resources.createAppInfoArea("borders.showruler.info",
           resources.getString("settings.label"),
           resources.getString("settings.rulers")),
        gbc);

      return rulerComp;
   }

   private JComponent createStatusArea(FlowframTk application)
   {
      JComponent statusComp = new JPanel(new GridBagLayout());

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridy=0;
      gbc.gridx=0;
      gbc.fill=GridBagConstraints.NONE;
      gbc.anchor=GridBagConstraints.BASELINE_LEADING;
      gbc.weightx=0.1;
      gbc.weighty=0.25;

      JLabel statusFontNameLabel = 
         resources.createAppLabel("borders.statusfont.name");
      statusComp.add(statusFontNameLabel, gbc);

      gbc.gridx++;

      statusFontSelector = new JavaFontSelector(application, 
        statusFontNameLabel,
        "borders.statusfont.bold",
        "borders.statusfont.italic",
        "borders.statusfont.size");
      statusComp.add(statusFontSelector, gbc);

      gbc.gridy++;
      gbc.gridx=0;
      gbc.gridwidth=2;

      showStatusBox = resources.createAppCheckBox(
        "borders", "showstatus", true, this);
      statusComp.add(showStatusBox, gbc);

      statusSettings = createStatusSettings();
      gbc.gridy++;
      gbc.fill=GridBagConstraints.HORIZONTAL;
      statusComp.add(statusSettings, gbc);

      return statusComp;
   }

   private JComponent createStatusSettings()
   {
      JComponent comp = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();

      gbc.fill=GridBagConstraints.NONE;
      gbc.anchor=GridBagConstraints.BASELINE_LEADING;
      gbc.gridx=0;
      gbc.gridy=0;

      showPositionBox = resources.createAppCheckBox("borders", 
        "showstatuspos", true, null);
      comp.add(showPositionBox, gbc);

      gbc.gridx++;

      showUnitBox = resources.createAppCheckBox("borders", 
        "showstatusunit", true, null);
      comp.add(showUnitBox, gbc);

      gbc.gridx++;

      showModifiedBox = resources.createAppCheckBox("borders", 
        "showstatusmodified", true, null);
      comp.add(showModifiedBox, gbc);

      gbc.gridx++;

      showLockBox = resources.createAppCheckBox("borders", 
        "showstatuslock", true, null);
      comp.add(showLockBox, gbc);

      gbc.gridx=0;
      gbc.gridy++;

      showZoomBox = resources.createAppCheckBox("borders", 
        "showstatuszoom", true, null);
      comp.add(showZoomBox, gbc);

      gbc.gridx++;

      showHelpBox = resources.createAppCheckBox("borders", 
        "showstatushelp", true, null);
      comp.add(showHelpBox, gbc);

      gbc.gridx++;

      showInfoBox = resources.createAppCheckBox("borders", 
        "showstatusinfo", true, null);
      comp.add(showInfoBox, gbc);

      return comp;
   }

   public void initialise(FlowframTk application)
   {
      int rulerWidth = application.getVRulerWidth();
      int rulerHeight = application.getHRulerHeight();

      patternField.setText(application.getRulerFormat().toLocalizedPattern());
      localeBox.setSelectedItem(application.getRulerLocale());
      widthModel.setValue(new Integer(rulerWidth));
      heightModel.setValue(new Integer(rulerHeight));

      boolean left = application.getVerticalToolBarLocation().equals("West");
      toolbarLeft.setSelected(left);
      toolbarRight.setSelected(!left);

      rulerFontSelector.setSelectedFont(application.getRulerFont());
      statusFontSelector.setSelectedFont(application.getStatusFont());

      showToolsBox.setSelected(application.hasToolBars());
      showStatusBox.setSelected(application.hasStatusBar());

      toolBarLocationComp.setVisible(showToolsBox.isSelected());
      statusSettings.setVisible(showStatusBox.isSelected());

      StatusBar statusBar = application.getStatusBar();
      showPositionBox.setSelected(statusBar.isPositionVisible());
      showUnitBox.setSelected(statusBar.isUnitVisible());
      showLockBox.setSelected(statusBar.isLockVisible());
      showModifiedBox.setSelected(statusBar.isModifiedVisible());
      showZoomBox.setSelected(statusBar.isZoomVisible());
      showHelpBox.setSelected(statusBar.isHelpVisible());
      showInfoBox.setSelected(statusBar.isInfoVisible());

      Dimension dim = sampleTopComp.getPreferredSize();
      dim.height = rulerHeight;

      sampleTopComp.setPreferredSize(dim);

      dim = sampleSideComp.getPreferredSize();
      dim.width = rulerWidth;

      sampleSideComp.setPreferredSize(dim);
      revalidate();
   }

   public void okay(FlowframTk application)
    throws InvalidFormatException
   {
      String pattern = patternField.getText();

      if (pattern.isEmpty())
      {
         throw new InvalidFormatException(
           resources.getMessage("error.invalid_ruler_pattern",
             pattern));
      }

      try
      {
         application.setRulerConf( 
            rulerFontSelector.getSelectedFont(),
            widthModel.getNumber().intValue(),
            heightModel.getNumber().intValue(),
            pattern,
            localeBox.getItemAt(localeBox.getSelectedIndex()));
      }
      catch (IllegalArgumentException e)
      {
         throw new InvalidFormatException(e.getMessage(), e);
      }

      application.setVerticalToolBarLocation(toolbarLeft.isSelected() ?
         "West" : "East");

      application.setToolBarsVisible(showToolsBox.isSelected());
      application.setStatusBarVisible(showStatusBox.isSelected());

      StatusBar statusBar = application.getStatusBar();

      statusBar.setPositionVisible(showPositionBox.isSelected());
      statusBar.setUnitVisible(showUnitBox.isSelected());
      statusBar.setLockVisible(showLockBox.isSelected());
      statusBar.setModifiedVisible(showModifiedBox.isSelected());
      statusBar.setZoomVisible(showZoomBox.isSelected());
      statusBar.setHelpVisible(showHelpBox.isSelected());
      statusBar.setInfoVisible(showInfoBox.isSelected());

      application.setStatusFont(statusFontSelector.getSelectedFont());
      application.getStatusBar().recalculate();
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("showstatus"))
      {
         statusSettings.setVisible(showStatusBox.isSelected());
      }
      else if (action.equals("showtools"))
      {
         toolBarLocationComp.setVisible(showToolsBox.isSelected());
      }
   }

   private JTextField patternField;

   private JComboBox<Locale> localeBox;

   private SpinnerNumberModel widthModel, heightModel, statusHeightModel;

   private JRadioButton toolbarLeft, toolbarRight;

   private JavaFontSelector rulerFontSelector, statusFontSelector;

   private JCheckBox showToolsBox, showStatusBox;

   private JComponent sampleTopComp, sampleSideComp;

   private JComponent statusSettings, toolBarLocationComp;

   private JCheckBox showZoomBox, showPositionBox, showModifiedBox,
     showLockBox, showInfoBox, showUnitBox, showHelpBox;

   private JDRResources resources;
}

class LocaleComboBoxRenderer extends JLabel
  implements ListCellRenderer<Locale>
{
   protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

   public LocaleComboBoxRenderer()
   {
      super();
      setOpaque(true);
      setBorder(noFocusBorder);
   }

   public Component getListCellRendererComponent(JList<? extends Locale> list,
     Locale value, int index, boolean isSelected, boolean cellHasFocus)
   {
      if (isSelected)
      {
         setBackground(list.getSelectionBackground());
         setForeground(list.getSelectionForeground());
      }
      else
      {
         setBackground(list.getBackground());
         setForeground(list.getForeground());
      }

      setFont(list.getFont());

      setText(value.getDisplayName());

      return this;
   }
}

class AcceleratorPanel extends JPanel
{
   public AcceleratorPanel(ConfigUISettingsDialog owner)
   {
      super(new BorderLayout());
      this.resources = owner.getResources();

      JTextArea info = new JTextArea(
         resources.getString("accelerators.info"));
      info.setEditable(false);
      info.setOpaque(false);
      info.setLineWrap(true);
      info.setWrapStyleWord(true);

      add(info, "North");

      data = new Vector<AcceleratorRow>();

      for (Enumeration<Object> propertyNames
         = resources.getAcceleratorPropertyNames();
         propertyNames.hasMoreElements(); )
      {
         String propName = propertyNames.nextElement().toString();

         AcceleratorRow row = new AcceleratorRow(propName, resources);

         int n = data.size();

         if (n == 0)
         {
            data.add(row);
            continue;
         }

         String thisLabel = row.firstElement();
         boolean done = false;

         for (int i = 0; i < n; i++)
         {
            String label = data.get(i).getPropertyName();

            if (thisLabel.compareTo(label) < 0)
            {
               data.add(i, row);
               done = true;
               break;
            }
         }

         if (!done)
         {
            data.add(row);
         }
      }

      Vector<String> columnNames = new Vector<String>(3);

      columnNames.add(resources.getString("accelerators.column.propname"));
      columnNames.add(resources.getString("accelerators.column.function"));
      columnNames.add(resources.getString("accelerators.column.keystroke"));

      table = new JTable(data, columnNames)
      {
         public boolean isCellEditable(int row, int col) {return false;}
      };

      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      table.setRowSorter(
         new TableRowSorter<TableModel>(table.getModel()));

      table.addMouseListener(new MouseAdapter()
      {
          public void mouseClicked(MouseEvent evt)
          {
             if (evt.getClickCount() == 2)
             {
                int rowIdx = table.getSelectedRow();

                if (rowIdx != -1)
                {
                   editRow(rowIdx);
                }
             }
          }
      });

      add(new JScrollPane(table), "Center");

      dialog = new AcceleratorDialog(owner, this);
   }

   public void editRow(int rowIdx)
   {
      dialog.display(data.get(rowIdx), rowIdx);
   }

   public void rowModified(int rowIdx)
   {
      table.tableChanged(new TableModelEvent(table.getModel(), rowIdx));
   }

   public JDRResources getResources()
   {
      return resources;
   }

   private JTable table;
   private Vector<AcceleratorRow> data;
   private AcceleratorDialog dialog;
   private JDRResources resources;
}

class AcceleratorRow extends Vector<String>
{
   public AcceleratorRow(String propertyName, JDRResources resources)
   {
      super(2);

      add(propertyName);
      add(resources.getString(propertyName));
      add(resources.getAcceleratorString(propertyName));
   }

   public String getPropertyName()
   {
      return get(0);
   }

   public String getFunction()
   {
      return get(1);
   }

   public String getAccelerator()
   {
      return get(2);
   }

   public void setAccelerator(String keyStroke)
   {
      set(2, keyStroke);
   }

   public void setAccelerator(KeyStroke keyStroke)
   {
      setAccelerator(keyStroke.toString());
   }
}

class AcceleratorDialog extends JDialog 
  implements ActionListener, ItemListener, KeyListener
{
   public AcceleratorDialog(JDialog owner, AcceleratorPanel accPanel)
   {
      super(owner, accPanel.getResources().getString("accelerator.set"), true);

      acceleratorPanel = accPanel;

      JComponent mainPanel = Box.createVerticalBox();
      mainPanel.setAlignmentX(0.0f);
      getContentPane().add(mainPanel, "Center");

      JPanel panel = new JPanel(new GridLayout(3, 2));
      panel.setAlignmentX(0.0f);
      mainPanel.add(panel);

      JLabel label = getResources().createAppLabel("accelerator.set.propname");
      label.setAlignmentX(0.0f);
      panel.add(label);

      propNameLabel = new JLabel();
      propNameLabel.setAlignmentX(0.0f);
      panel.add(propNameLabel);

      label = getResources().createAppLabel("accelerator.set.function");
      label.setAlignmentX(0.0f);
      panel.add(label);

      functionLabel = new JLabel();
      functionLabel.setAlignmentX(0.0f);
      panel.add(functionLabel);

      label = getResources().createAppLabel("accelerator.set.keystroke");
      panel.add(label);

      keystrokeLabel = new JLabel();
      keystrokeLabel.setAlignmentX(0.0f);
      panel.add(keystrokeLabel);

      ButtonGroup bg = new ButtonGroup();

      JRadioButton useSelector = getResources().createAppRadioButton(
         "accelerator.set", "useselector", bg, true, this);
      useSelector.setAlignmentX(0.0f);

      mainPanel.add(useSelector);

      Box selectorBox = Box.createHorizontalBox();
      selectorBox.setAlignmentX(0.0f);
      mainPanel.add(selectorBox);

      shiftBox = getResources().createAppCheckBox("accelerator.set",
        "shift", false, this);
      shiftBox.setAlignmentX(0.0f);
      shiftBox.setActionCommand("updatefromselection");
      selectorBox.add(shiftBox);

      ctrlBox = getResources().createAppCheckBox("accelerator.set",
        "ctrl", false, this);
      ctrlBox.setAlignmentX(0.0f);
      ctrlBox.setActionCommand("updatefromselection");
      selectorBox.add(ctrlBox);

      metaBox = getResources().createAppCheckBox("accelerator.set",
        "meta", false, this);
      metaBox.setAlignmentX(0.0f);
      metaBox.setActionCommand("updatefromselection");
      selectorBox.add(metaBox);

      altBox = getResources().createAppCheckBox("accelerator.set",
        "alt", false, this);
      altBox.setAlignmentX(0.0f);
      altBox.setActionCommand("updatefromselection");
      selectorBox.add(altBox);

      altGrBox = getResources().createAppCheckBox("accelerator.set",
        "altgr", false, this);
      altGrBox.setAlignmentX(0.0f);
      altGrBox.setActionCommand("updatefromselection");
      selectorBox.add(altGrBox);

      keystrokeBox = new JComboBox<KeyStroke>(KEY_STROKES);
      keystrokeBox.setAlignmentX(0.0f);
      keystrokeBox.addItemListener(this);
      selectorBox.add(keystrokeBox);

      JRadioButton useKeyStroke = getResources().createAppRadioButton(
         "accelerator.set", "usekeystroke", bg, false, this);
      useKeyStroke.setAlignmentX(0.0f);

      mainPanel.add(useKeyStroke);

      Box box = Box.createHorizontalBox();
      box.setAlignmentX(0.0f);
      mainPanel.add(box);

      enterLabel = 
        getResources().createAppLabel("accelerator.set.usekeystroke.label");
      enterLabel.setAlignmentX(0.0f);

      box.add(enterLabel);

      textfield = new JTextField();
      textfield.setAlignmentX(0.0f);
      textfield.addKeyListener(this);
      box.add(textfield);

      box.add(Box.createHorizontalGlue());

      updateEnabled(useSelector.isSelected());

      JPanel p = new JPanel();
      getContentPane().add(p, "South");

      p.add(getResources().createOkayButton(this));
      p.add(getResources().createCancelButton(this));

      pack();
      setLocationRelativeTo(owner);
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
      else if (action.equals("useselector"))
      {
         updateEnabled(true);
      }
      else if (action.equals("usekeystroke"))
      {
         updateEnabled(false);
         textfield.requestFocusInWindow();
      }
      else if (action.equals("updatefromselection"))
      {
         updateKeyStrokeFromSelection();
      }
   }

   public void itemStateChanged(ItemEvent evt)
   {
      if (evt.getSource() == keystrokeBox)
      {
         if (evt.getStateChange() == ItemEvent.SELECTED)
         {
            updateKeyStrokeFromSelection();
         }
      }
   }

   public void keyPressed(KeyEvent evt)
   {
      evt.consume();
   }

   public void keyTyped(KeyEvent evt)
   {
      evt.consume();
   }

   public void keyReleased(KeyEvent evt)
   {
      int keyCode = evt.getKeyCode();
      int modifiers = evt.getModifiers();

      if (!evt.isActionKey())
      {
         if (keyCode == KeyEvent.VK_SHIFT
          || keyCode == KeyEvent.VK_CONTROL
          || keyCode == KeyEvent.VK_META
          || keyCode == KeyEvent.VK_ALT
          || keyCode == KeyEvent.VK_ALT_GRAPH)
         {
            return;
         }
      }

      keystrokeLabel.setText(
         KeyStroke.getKeyStroke(keyCode, modifiers).toString());
      textfield.setText(keystrokeLabel.getText());

      evt.consume();
   }

   private void updateEnabled(boolean useSelector)
   {
      textfield.setEnabled(!useSelector);
      enterLabel.setEnabled(!useSelector);

      shiftBox.setEnabled(useSelector);
      ctrlBox.setEnabled(useSelector);
      metaBox.setEnabled(useSelector);
      altBox.setEnabled(useSelector);
      altGrBox.setEnabled(useSelector);
      keystrokeBox.setEnabled(useSelector);

      if (useSelector)
      {
         KeyStroke keyStroke = KeyStroke.getKeyStroke(keystrokeLabel.getText());

         if (keyStroke != null)
         {
            updateSelector(keyStroke.getKeyCode(), keyStroke.getModifiers());
         }
      }
      else
      {
         textfield.setText("");
      }
   }

   public void display(AcceleratorRow row, int index)
   {
      if (row == null)
      {
         throw new NullPointerException();
      }

      String keystrokeString = row.getAccelerator();

      this.row = row;
      this.rowIndex = index;

      propNameLabel.setText(row.getPropertyName());
      functionLabel.setText(row.getFunction());
      keystrokeLabel.setText(keystrokeString);

      KeyStroke keyStroke = KeyStroke.getKeyStroke(keystrokeString);

      updateSelector(keyStroke.getKeyCode(), keyStroke.getModifiers());
      textfield.setText("");

      setVisible(true);
   }

   private void updateSelector(int code, int modifiers)
   {
      shiftBox.setSelected((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0);
      ctrlBox.setSelected((modifiers & InputEvent.CTRL_DOWN_MASK) != 0);
      metaBox.setSelected((modifiers & InputEvent.META_DOWN_MASK) != 0);
      altBox.setSelected((modifiers & InputEvent.ALT_DOWN_MASK) != 0);
      altGrBox.setSelected((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0);
      keystrokeBox.setSelectedItem(KeyStroke.getKeyStroke(code, 0));
   }

   private void updateKeyStrokeFromSelection()
   {
      KeyStroke keyStroke = (KeyStroke)keystrokeBox.getSelectedItem();

      if (keyStroke == null)
      {
         return;
      }

      int modifiers = 0;

      if (shiftBox.isSelected())
      {
         modifiers = modifiers | InputEvent.SHIFT_DOWN_MASK;
      }

      if (ctrlBox.isSelected())
      {
         modifiers = modifiers | InputEvent.CTRL_DOWN_MASK;
      }

      if (metaBox.isSelected())
      {
         modifiers = modifiers | InputEvent.META_DOWN_MASK;
      }

      if (altBox.isSelected())
      {
         modifiers = modifiers | InputEvent.ALT_DOWN_MASK;
      }

      if (altGrBox.isSelected())
      {
         modifiers = modifiers | InputEvent.ALT_GRAPH_DOWN_MASK;
      }

      keyStroke = KeyStroke.getKeyStroke(keyStroke.getKeyCode(),
         modifiers);

      keystrokeLabel.setText(keyStroke.toString());
   }

   public void okay()
   {
      row.setAccelerator(keystrokeLabel.getText());
      getResources().setAccelerator(
         propNameLabel.getText(), keystrokeLabel.getText());
      acceleratorPanel.rowModified(rowIndex);
      setVisible(false);
   }

   public JDRResources getResources()
   {
      return acceleratorPanel.getResources();
   }

   private AcceleratorRow row;
   private int rowIndex = -1;
   private JLabel propNameLabel, functionLabel, keystrokeLabel, enterLabel;

   private JTextField textfield;

   private JCheckBox shiftBox, ctrlBox, metaBox, altBox, altGrBox;

   private JComboBox<KeyStroke> keystrokeBox;

   private AcceleratorPanel acceleratorPanel;

   private static final KeyStroke[] KEY_STROKES = new KeyStroke[]
   {
      KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_0, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_1, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_2, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_3, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_4, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_5, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_6, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_7, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_8, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_9, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_A, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_B, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_C, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_D, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_E, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_G, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_H, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_I, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_J, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_K, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_L, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_M, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_N, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_O, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_P, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_R, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_S, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_T, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_U, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_V, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_W, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_X, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F13, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F14, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F15, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F16, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F17, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F18, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F19, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F20, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F21, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F22, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F23, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_F24, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_AMPERSAND, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_ASTERISK, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_AT, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_BRACELEFT, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_BRACERIGHT, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_CANCEL, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_CIRCUMFLEX, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_CLEAR, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_COMPOSE, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_CONVERT, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_COPY, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_CUT, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_DECIMAL, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_DOLLAR, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_EURO_SIGN, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_EXCLAMATION_MARK, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_FINAL, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_FIND, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_GREATER, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_HELP, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_HIRAGANA, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_INVERTED_EXCLAMATION_MARK, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_JAPANESE_HIRAGANA, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_JAPANESE_KATAKANA, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_JAPANESE_ROMAN, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_KANA, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_KANA_LOCK, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_KANJI, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_KATAKANA, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_LEFT_PARENTHESIS, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_LESS, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_NUMBER_SIGN, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_PASTE, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_PAUSE, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_PROPS, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_QUOTE, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_QUOTEDBL, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT_PARENTHESIS, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_ROMAN_CHARACTERS, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_SCROLL_LOCK, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_SEPARATOR, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_STOP, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_UNDERSCORE, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_UNDO, 0),
      KeyStroke.getKeyStroke(KeyEvent.VK_WINDOWS, 0)
   };
}

class NormalizePanel extends JPanel
   implements ActionListener, DocumentListener
{
   public NormalizePanel(JDRResources resources)
   {
      super(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();

      this.resources = resources;

      gbc.gridx=0;
      gbc.gridy=0;
      gbc.gridwidth=2;
      gbc.gridheight=1;
      gbc.fill=GridBagConstraints.BOTH;
      gbc.anchor=GridBagConstraints.LINE_START;

      JTextArea infoArea = resources.createAppInfoArea("normalize.info");
      infoArea.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      add(infoArea, gbc);

      JPanel panel = new JPanel();

      JLabel xNormLabel = resources.createAppLabel("normalize.norm.x");
      xNormField = new NonNegativeDoubleField(1.0);
      xNormLabel.setLabelFor(xNormField);

      panel.add(xNormLabel);
      panel.add(xNormField);

      JLabel yNormLabel = resources.createAppLabel("normalize.norm.y");
      yNormField = new NonNegativeDoubleField(1.0);
      yNormLabel.setLabelFor(yNormField);

      panel.add(yNormLabel);
      panel.add(yNormField);

      xNormField.setColumns(6);
      yNormField.setColumns(6);
      xNormField.setValue(CanvasGraphics.normTransformX);
      yNormField.setValue(CanvasGraphics.normTransformY);
      xNormField.getDocument().addDocumentListener(this);
      yNormField.getDocument().addDocumentListener(this);

      panel.add(resources.createAppJButton("normalize", "reset", this));

      gbc.gridy++;
      gbc.gridx=0;
      gbc.gridwidth=2;
      gbc.fill=GridBagConstraints.NONE;
      gbc.anchor=GridBagConstraints.CENTER;

      add(panel, gbc);

      JTextArea contInfoArea =
         resources.createAppInfoArea("normalize.info.cont");
      contInfoArea.setAlignmentX(JComponent.LEFT_ALIGNMENT);

      gbc.gridx=0;
      gbc.gridy++;
      gbc.gridwidth=2;
      gbc.fill=GridBagConstraints.BOTH;
      gbc.anchor=GridBagConstraints.LINE_START;

      add(contInfoArea, gbc);

      gbc.gridx=0;
      gbc.gridy++;
      gbc.gridwidth=1;
      gbc.insets = new Insets(2, 0, 2, 0);
      gbc.fill=GridBagConstraints.NONE;
      gbc.anchor=GridBagConstraints.CENTER;

      add(resources.createAppLabel("normalize.nonorm_horizontal"), gbc);

      gbc.gridx++;

      add(resources.createAppLabel("normalize.nonorm_vertical"), gbc);

      JPanel noNormXPanel = new JPanel()
      {
         public void paintComponent(Graphics g)
         {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;

            AffineTransform oldAf = g2.getTransform();

            Dimension dim = getSize();

            g2.translate(0.5*(dim.width)-36, 0.5*(dim.height)-36);

            if (renderHints != null)
            {
               g2.setRenderingHints(renderHints);
            }

            g2.drawLine(0, 36, 72, 36);

            g2.setTransform(oldAf);
         }
      };

      JPanel noNormYPanel = new JPanel()
      {
         public void paintComponent(Graphics g)
         {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;

            AffineTransform oldAf = g2.getTransform();

            Dimension dim = getSize();

            g2.translate(0.5*(dim.width)-36, 0.5*(dim.height)-36);

            if (renderHints != null)
            {
               g2.setRenderingHints(renderHints);
            }

            g2.drawLine(36, 0, 36, 72);

            g2.setTransform(oldAf);
         }
      };

      Dimension dim = new Dimension(76,76);

      noNormXPanel.setMinimumSize(dim);
      noNormXPanel.setPreferredSize(dim);
      noNormYPanel.setMinimumSize(dim);
      noNormYPanel.setPreferredSize(dim);

      noNormXPanel.setBackground(Color.white);
      noNormYPanel.setBackground(Color.white);

      gbc.gridx=0;
      gbc.gridy++;
      gbc.fill=GridBagConstraints.BOTH;

      add(noNormXPanel, gbc);

      gbc.gridx++;

      add(noNormYPanel, gbc);

      xLengthPanel = resources.createNonNegativeLengthPanel();
      xLengthPanel.setValue(1.0, JDRUnit.in);
      xLengthPanel.getTextField().setColumns(6);

      JButton computeNormXButton = resources.createAppJButton(
        "normalize", "x_compute_norm", this);

      yLengthPanel = resources.createNonNegativeLengthPanel();
      yLengthPanel.setValue(1.0, JDRUnit.in);
      yLengthPanel.getTextField().setColumns(6);

      JButton computeNormYButton = resources.createAppJButton(
        "normalize", "y_compute_norm", this);

      gbc.gridx=0;
      gbc.gridy++;
      gbc.fill=GridBagConstraints.EAST;

      add(xLengthPanel, gbc);

      gbc.gridx++;

      add(yLengthPanel, gbc);

      gbc.gridx=0;
      gbc.gridy++;
      gbc.fill=GridBagConstraints.NONE;

      add(computeNormXButton, gbc);

      gbc.gridx++;

      add(computeNormYButton, gbc);

      samplePanel = new JPanel()
      {
         public void paintComponent(Graphics g)
         {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;

            AffineTransform oldAf = g2.getTransform();

            Dimension dim = getSize();

            int w = (int)Math.ceil(72.0*xNormField.getDouble());
            int h = (int)Math.ceil(72.0*yNormField.getDouble());

            g2.translate(0.5*(dim.width-w), 0.5*(dim.height-h));

            if (renderHints != null)
            {
               g2.setRenderingHints(renderHints);
            }

            g2.drawRect(0, 0, w, h);

            g2.setTransform(oldAf);
         }

         public Dimension getMinimumSize()
         {
            return new Dimension
            (
              (int)Math.ceil(72.0*xNormField.getDouble()),
              (int)Math.ceil(72.0*yNormField.getDouble())
            );
         }

         public Dimension getPreferredSize()
         {
            return new Dimension
            (
              10 + (int)Math.ceil(72.0*xNormField.getDouble()),
              10 + (int)Math.ceil(72.0*yNormField.getDouble())
            );
         }
      };

      samplePanel.setBackground(Color.white);

      gbc.gridx=0;
      gbc.gridy++;
      gbc.gridwidth=2;
      gbc.fill=GridBagConstraints.NONE;
      gbc.anchor=GridBagConstraints.CENTER;

      add(resources.createAppLabel("normalize.norm_lines"), gbc);

      gbc.gridy++;
      gbc.fill=GridBagConstraints.BOTH;

      add(samplePanel, gbc);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("x_compute_norm"))
      {
         double compValue = xLengthPanel.getValue(JDRUnit.bp);

         double normX = 72.0 / compValue;

         if (Double.isInfinite(normX)
          || Double.isNaN(normX))
         {
            resources.error(this,
               resources.getMessage("normalize.invalid", 
                 xLengthPanel.getLength().toString()));
         }
         else
         {
            xNormField.setValue(normX);
         }
      }
      else if (action.equals("y_compute_norm"))
      {
         double compValue = yLengthPanel.getValue(JDRUnit.bp);

         double normY = 72.0 / compValue;

         if (Double.isInfinite(normY)
          || Double.isNaN(normY))
         {
            resources.error(this,
               resources.getMessage("normalize.invalid", 
                 yLengthPanel.getLength().toString()));
         }
         else
         {
            yNormField.setValue(normY);
         }
      }
      else if (action.equals("reset"))
      {
         xNormField.setValue(1.0);
         yNormField.setValue(1.0);
      }
   }

   public void initialise(FlowframTk application)
   {
      xNormField.setValue(CanvasGraphics.normTransformX);
      yNormField.setValue(CanvasGraphics.normTransformY);
      renderHints = application.getRenderingHints();
      samplePanel.revalidate();
   }

   public void okay(FlowframTk application)
     throws InvalidFormatException
   {
      double xNorm = xNormField.getDouble();

      double inv = 1.0/xNorm;

      if (Double.isInfinite(inv)
        ||Double.isNaN(inv))
      {
         throw new InvalidFormatException(
            resources.getMessage("normalize.invalid", xNorm));
      }

      double yNorm = yNormField.getDouble();

      inv = 1.0/yNorm;

      if (Double.isInfinite(inv)
        ||Double.isNaN(inv))
      {
         throw new InvalidFormatException(
            resources.getMessage("normalize.invalid", yNorm));
      }

      CanvasGraphics.normTransformX = xNorm;
      CanvasGraphics.normTransformY = yNorm;
   }

   public void insertUpdate(DocumentEvent evt)
   {
      if (samplePanel != null)
      {
         samplePanel.revalidate();
         samplePanel.repaint();
      }
   }

   public void removeUpdate(DocumentEvent evt)
   {
      if (samplePanel != null)
      {
         samplePanel.revalidate();
         samplePanel.repaint();
      }
   }

   public void changedUpdate(DocumentEvent evt)
   {
      if (samplePanel != null)
      {
         samplePanel.revalidate();
         samplePanel.repaint();
      }
   }

   private NonNegativeDoubleField xNormField, yNormField;

   private NonNegativeLengthPanel xLengthPanel, yLengthPanel;

   private JPanel samplePanel;

   private RenderingHints renderHints;

   private JDRResources resources;
}

class TeXEditorUIPanel extends JPanel
   implements ActionListener
{
   public TeXEditorUIPanel(FlowframTk application)
   {
      super(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();

      resources = application.getResources();

      gbc.gridx=0;
      gbc.gridy=0;
      gbc.gridwidth=2;
      gbc.gridheight=1;
      gbc.insets=new Insets(2, 2, 2, 2);
      gbc.fill=GridBagConstraints.HORIZONTAL;
      gbc.anchor=GridBagConstraints.LINE_START;

      add(resources.createAppInfoArea("texeditorui.info"), gbc);
      gbc.gridy++;
      add(Box.createVerticalStrut(10), gbc);

      gbc.gridwidth=1;
      gbc.fill=GridBagConstraints.NONE;
      gbc.gridy++;
      JLabel fontNameLabel = resources.createAppLabel("texeditorui.font");
      add(fontNameLabel, gbc);

      fontSelector = new JavaFontSelector(application,
        fontNameLabel, null, null, "texeditorui.fontsize");

      gbc.gridx++;
      add(fontSelector, gbc);

      highlightCheckBox = resources.createAppCheckBox("texeditorui",
        "highlight", true, this);

      gbc.gridwidth = 2;
      gbc.gridx=0;
      gbc.gridy++;

      add(highlightCheckBox, gbc);

      gbc.gridwidth = 1;
      gbc.gridx=0;
      gbc.gridy++;

      commentColorLabel = resources.createAppLabel("texeditorui.comment");

      gbc.gridy++;
      add(commentColorLabel, gbc);

      JComponent panel = Box.createHorizontalBox();

      gbc.gridx++;
      add(panel, gbc);

      commentColorPanel = new JPanel();
      commentColorPanel.setPreferredSize(new Dimension(60, 20));
      commentColorPanel.setOpaque(true);

      panel.add(commentColorPanel);

      panel.add(Box.createHorizontalStrut(10));

      commentSelectButton = resources.createDialogButton(
         "label.choose", "choose", this, null);
      commentSelectButton.setActionCommand("commentselect");
      commentColorLabel.setLabelFor(commentSelectButton);

      panel.add(commentSelectButton);

      csColorLabel = resources.createAppLabel("texeditorui.cs");

      gbc.gridx=0;
      gbc.gridy++;
      add(csColorLabel, gbc);

      panel = Box.createHorizontalBox();

      gbc.gridx++;
      add(panel, gbc);

      csColorPanel = new JPanel();
      csColorPanel.setPreferredSize(new Dimension(60, 20));
      csColorPanel.setOpaque(true);

      panel.add(csColorPanel);

      panel.add(Box.createHorizontalStrut(10));

      csSelectButton = resources.createDialogButton(
         "label.choose", "choose", this, null);
      csSelectButton.setActionCommand("csselect");
      csColorLabel.setLabelFor(csSelectButton);

      panel.add(csSelectButton);

      colorChooser = new JColorChooser();

      gbc.gridx=0;
      gbc.gridy++;
      gbc.gridwidth=2;
      gbc.fill=GridBagConstraints.BOTH;
      add(Box.createVerticalStrut(20), gbc);

      gbc.gridy++;
      gbc.gridx=0;
      gbc.gridwidth=3;

      JComponent prefSizeComp = new JPanel(new GridBagLayout());
      GridBagConstraints prefSizeGbc = new GridBagConstraints();

      add(prefSizeComp, gbc);

      prefSizeComp.setBorder(BorderFactory.createTitledBorder(
         resources.getString("texeditorui.dim")));

      JLabel widthLabel = resources.createAppLabel("texeditorui.width");

      prefSizeGbc.gridy=0;
      prefSizeGbc.gridx=0;
      prefSizeGbc.gridwidth=1;
      prefSizeGbc.fill=GridBagConstraints.NONE;
      prefSizeGbc.anchor=GridBagConstraints.LINE_START;

      prefSizeComp.add(widthLabel, prefSizeGbc);

      widthModel = new SpinnerNumberModel(10, 1, 100, 1);
      JSpinner widthField = new JSpinner(widthModel);
      widthField.setToolTipText(
         resources.getString("texeditorui.width.tooltip"));
      widthLabel.setLabelFor(widthField);
      widthLabel.setToolTipText(widthField.getToolTipText());

      prefSizeGbc.gridx++;
      prefSizeComp.add(widthField, prefSizeGbc);

      JLabel heightLabel = resources.createAppLabel("texeditorui.height");

      prefSizeGbc.gridy++;
      prefSizeGbc.gridx=0;

      prefSizeComp.add(heightLabel, prefSizeGbc);

      heightModel = new SpinnerNumberModel(10, 1, 100, 1);
      JSpinner heightField = new JSpinner(heightModel);
      heightField.setToolTipText(
         resources.getString("texeditorui.height.tooltip"));
      heightLabel.setLabelFor(heightField);
      heightLabel.setToolTipText(heightField.getToolTipText());

      prefSizeGbc.gridx++;
      prefSizeComp.add(heightField, prefSizeGbc);

      gbc.gridy++;
      gbc.gridx=0;
      gbc.gridwidth=3;

      add(Box.createVerticalStrut(20), gbc);

      gbc.gridy++;
      JComponent splitComp = new JPanel(new GridBagLayout());
      add(splitComp, gbc);

      GridBagConstraints splitGbc = new GridBagConstraints();
      splitGbc.fill=GridBagConstraints.NONE;
      splitGbc.gridx=0;
      splitGbc.gridy=0;

      splitComp.setBorder(BorderFactory.createTitledBorder(
        resources.getString("texeditorui.split")));

      ButtonGroup bg = new ButtonGroup();

      leftButton = resources.createDialogRadio(
         "texeditorui.preambleleft", "preambleleft", null, bg, false);

      splitComp.add(leftButton, splitGbc);

      splitGbc.gridx++;

      rightButton = resources.createDialogRadio(
         "texeditorui.preambleright", "preambleright", null, bg, false);
      splitComp.add(rightButton, splitGbc);

      splitGbc.gridy++;
      splitGbc.gridx=0;

      aboveButton = resources.createDialogRadio(
         "texeditorui.preambleabove", "preambleabove", null, bg, false);
      splitComp.add(aboveButton, splitGbc);

      splitGbc.gridx++;

      belowButton = resources.createDialogRadio(
         "texeditorui.preamblebelow", "preamblebelow", null, bg, false);
      splitComp.add(belowButton, splitGbc);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("commentselect"))
      {
         Color color = colorChooser.showDialog(this, 
            resources.getString("texeditorui.comment"),
            commentColorPanel.getBackground());

         if (color != null)
         {
            commentColorPanel.setBackground(color);
         }
      }
      else if (action.equals("csselect"))
      {
         Color color = colorChooser.showDialog(this, 
            resources.getString("texeditorui.cs"),
            csColorPanel.getBackground());

         if (color != null)
         {
            csColorPanel.setBackground(color);
         }
      }
      else if (action.equals("highlight"))
      {
         updateHighlight();
      }
   }

   public void initialise(FlowframTk application)
   {
      Color col = application.getCommentHighlight();
      commentColorPanel.setBackground(col);

      col = application.getControlSequenceHighlight();
      csColorPanel.setBackground(col);

      setHighlight(application.isSyntaxHighlightingOn());

      fontSelector.setSelectedFont(application.getTeXEditorFont());

      widthModel.setValue(new Integer(application.getTeXEditorWidth()));
      heightModel.setValue(new Integer(application.getTeXEditorHeight()));

      FlowframTkSettings settings = application.getSettings();

      if (settings.getCanvasSplit() == JSplitPane.HORIZONTAL_SPLIT)
      {
         if (settings.isCanvasFirst())
         {
            rightButton.setSelected(true);
         }
         else
         {
            leftButton.setSelected(true);
         }
      }
      else
      {
         if (settings.isCanvasFirst())
         {
            belowButton.setSelected(true);
         }
         else
         {
            aboveButton.setSelected(true);
         }
      }
   }

   public void okay(FlowframTk application)
   {
      application.setTeXEditorWidth(widthModel.getNumber().intValue());
      application.setTeXEditorHeight(heightModel.getNumber().intValue());

      boolean enabled = highlightCheckBox.isSelected();
      application.setSyntaxHighlighting(enabled);

      if (enabled)
      {
         application.setCommentHighlight(commentColorPanel.getBackground());
         application.setControlSequenceHighlight(csColorPanel.getBackground());
      }

      Font f = fontSelector.getSelectedFont();

      application.setTeXEditorFont(f.getName(), f.getSize());

      application.updateTeXEditorStyles();

      if (leftButton.isSelected() || rightButton.isSelected())
      {
         application.setCanvasSplit(JSplitPane.HORIZONTAL_SPLIT,
           rightButton.isSelected());
      }
      else
      {
         application.setCanvasSplit(JSplitPane.VERTICAL_SPLIT,
           belowButton.isSelected());
      }
   }

   private void setHighlight(boolean enabled)
   {
      highlightCheckBox.setSelected(enabled);

      updateHighlight();
   }

   private void updateHighlight()
   {
      boolean enabled = highlightCheckBox.isSelected();

      commentColorLabel.setEnabled(enabled);
      commentSelectButton.setEnabled(enabled);
      commentColorPanel.setOpaque(enabled);
      commentColorPanel.repaint();

      csColorLabel.setEnabled(enabled);
      csSelectButton.setEnabled(enabled);
      csColorPanel.setOpaque(enabled);
      csColorPanel.repaint();
   }

   private JavaFontSelector fontSelector;
   private SpinnerNumberModel widthModel, heightModel;
   private JCheckBox highlightCheckBox;
   private JPanel commentColorPanel, csColorPanel;
   private JColorChooser colorChooser;
   private JLabel commentColorLabel, csColorLabel;
   private JButton commentSelectButton, csSelectButton;

   private JRadioButton leftButton, rightButton, aboveButton, belowButton;

   private JDRResources resources;
}

class AnnoteFontPanel extends JavaFontSelector
{
   public AnnoteFontPanel(FlowframTk application)
   {
      super(application, "annote.font", 
       "annote.font.bold", "annote.font.italic", "annote.fontsize");
   }

   public void initialise(FlowframTkSettings settings)
   {
      setSelectedFont(settings.getAnnoteFont());
   }

   public void okay(FlowframTkSettings settings)
   {
      JDRCompleteObject.annoteFont = getSelectedFont();
      settings.setAnnoteFont(JDRCompleteObject.annoteFont);
   }
}

class LookAndFeelPanel extends JPanel
  implements ActionListener
{
   public LookAndFeelPanel(FlowframTk application)
   {
      super(null);
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      this.application = application;

      add(getResources().createAppInfoArea("lookandfeel.info"));

      add(Box.createVerticalStrut(10));

      JComponent lfPanel = new JPanel();
      add(lfPanel);

      JLabel nameLabel = getResources().createAppLabel("lookandfeel.name");
      lfPanel.add(nameLabel);

      info = UIManager.getInstalledLookAndFeels();

      String[] names = new String[info.length];

      for (int i = 0; i < names.length; i++)
      {
         names[i] = info[i].getName();
      }

      lookAndFeelNamesBox = new JComboBox<String>(names);
      nameLabel.setLabelFor(lookAndFeelNamesBox);

      lfPanel.add(lookAndFeelNamesBox);

      add(Box.createVerticalStrut(10));

      JPanel dbPanel = new JPanel(new BorderLayout());
      dbPanel.setBorder(BorderFactory.createTitledBorder(
          getResources().getString("lookandfeel.dialog_button_styles")));

      add(dbPanel);

      asGeneralBox = getResources().createAppCheckBox(
        "lookandfeel.dialog", "as_general", true, this);

      dbPanel.add(asGeneralBox, BorderLayout.NORTH);

      dialogButtonPanel = new JPanel(new GridBagLayout());
      dbPanel.add(dialogButtonPanel, BorderLayout.CENTER);

      dialogStyleButtons = new ButtonGroup();

      GridBagConstraints gbc = new GridBagConstraints();

      gbc.gridx=0;
      gbc.gridy=0;
      gbc.gridwidth=1;
      gbc.gridheight=1;
      gbc.weighty=0.25;
      gbc.insets=new Insets(2, 2, 2, 2);
      gbc.fill=GridBagConstraints.NONE;
      gbc.anchor=GridBagConstraints.LINE_START;

      dialogButtonPanel.add(
         getResources().createAppLabel("lookandfeel.button.style"), gbc);

      gbc.gridx++;
      gbc.gridwidth=3;

      dialogButtonPanel.add(
         getResources().createAppLabel("lookandfeel.samples"), gbc);

      gbc.gridx=1;
      gbc.gridy++;
      gbc.gridwidth=1;

      dialogButtonPanel.add(
       getResources().createAppLabel("lookandfeel.press"), gbc);

      gbc.gridx++;
      dialogButtonPanel.add(
       getResources().createAppLabel("lookandfeel.toggle"), gbc);

      gbc.gridx++;
      dialogButtonPanel.add(
       getResources().createAppLabel("lookandfeel.radio"), gbc);

      gbc.gridy++;
      gbc.gridx=0;

      for (int i = 0; i < JDRResources.DIALOG_BUTTON_TAGS.length; i++)
      {
         gbc.gridx = 0;

         String tag = JDRResources.DIALOG_BUTTON_TAGS[i];

         AbstractButton button = getResources().createAppRadioButton(
           "lookandfeel", tag, dialogStyleButtons,
           false, null);

         dialogButtonPanel.add(button, gbc);

         gbc.gridx++;

         button = getResources().createDialogButton(i,
           "label.close", "close", null, null, null);
         button.setMnemonic('\0');

         dialogButtonPanel.add(button, gbc);

         gbc.gridx++;
         button = getResources().createDialogToggle(i,
           "annote.font.bold", "bold", null, null, null);
         button.setMnemonic('\0');
         dialogButtonPanel.add(button, gbc);

         ButtonGroup bg = new ButtonGroup();
         JComponent radioComp = Box.createVerticalBox();

         gbc.gridx++;
         dialogButtonPanel.add(radioComp, gbc);

         button = getResources().createDialogRadio(i,
           "texeditorui.preambleabove", "preambleabove", null, bg, true,
            null, null);
         button.setMnemonic('\0');

         radioComp.add(button);

         button = getResources().createDialogRadio(i,
           "texeditorui.preamblebelow", "preamblebelow", null, bg, false,
            null, null);
         button.setMnemonic('\0');
         radioComp.add(button);

         gbc.gridy++;
      }

      add(Box.createVerticalStrut(10));

      JPanel stylesPanel = new JPanel(new GridBagLayout());
      add(stylesPanel);

      gbc = new GridBagConstraints();

      gbc.gridx=0;
      gbc.gridy=0;
      gbc.gridwidth=1;
      gbc.weighty=0.25;
      gbc.gridheight=1;
      gbc.insets=new Insets(2, 2, 2, 2);
      gbc.fill=GridBagConstraints.NONE;
      gbc.anchor=GridBagConstraints.LINE_START;

      stylesPanel.setBorder(BorderFactory.createTitledBorder(
         getResources().getString("lookandfeel.button_styles")));

      stylesPanel.add(
         getResources().createAppLabel("lookandfeel.button.style"), gbc);

      gbc.gridwidth=3;
      gbc.gridx++;

      stylesPanel.add(
       getResources().createAppLabel("lookandfeel.samples"), gbc);

      gbc.gridx=1;
      gbc.gridy++;
      gbc.gridwidth=1;

      stylesPanel.add(
       getResources().createAppLabel("lookandfeel.press"), gbc);

      gbc.gridx++;
      stylesPanel.add(
       getResources().createAppLabel("lookandfeel.toggle"), gbc);

      gbc.gridx++;
      stylesPanel.add(
       getResources().createAppLabel("lookandfeel.radio"), gbc);

      styleButtons = new ButtonGroup();

      for (int i = 0; i < JDRResources.BUTTON_STYLES.length; i++)
      {
         JDRButtonStyle style = JDRResources.BUTTON_STYLES[i];

         AbstractButton button = getResources().createAppRadioButton(
           "lookandfeel.button_style", style.getName(), styleButtons,
           false, null);

         gbc.gridx=0;
         gbc.gridy++;
         gbc.gridwidth=1;

         stylesPanel.add(button, gbc);

         gbc.gridx++;

         button = style.createButton(getResources(),
             getResources().getString("label.okay"), "okay", null, null);
         button.setMnemonic('\0');

         stylesPanel.add(button, gbc);

         gbc.gridx++;

         button = style.createToggle(getResources(),
             getResources().getString("label.edit_path"), "editPath", null, null);
         button.setMnemonic('\0');

         stylesPanel.add(button, gbc);

         ButtonGroup grp = new ButtonGroup();
         JComponent radioComp = Box.createVerticalBox();

         button = style.createTool(getResources(),
               getResources().getString("tools.select"), "select", null, 
               grp, true, null);
         button.setMnemonic('\0');

         radioComp.add(button);

         button = style.createTool(getResources(),
               getResources().getString("tools.text"), "text", null,
               grp, false, null);
         button.setMnemonic('\0');

         radioComp.add(button);

         gbc.gridx++;
         stylesPanel.add(radioComp, gbc);
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("as_general"))
      {
         dialogButtonPanel.setVisible(!asGeneralBox.isSelected());
      }
   }

   public void initialise()
   {
      String currentLookAndFeel = application.getSettings().getLookAndFeel();

      if (currentLookAndFeel != null)
      {
         for (int i = 0; i < info.length; i++)
         {
            if (currentLookAndFeel.equals(info[i].getClassName()))
            {
               lookAndFeelNamesBox.setSelectedIndex(i);
               break;
            }
         }
      }

      String style = application.getSettings().getButtonStyle();
      styleButtons.clearSelection();

      for (Enumeration<AbstractButton> en = styleButtons.getElements();
           en.hasMoreElements(); )
      {
         AbstractButton button = en.nextElement();

         if (style.equals(button.getActionCommand()))
         {
            button.setSelected(true);
         }
      }

      int dialogStyle = application.getSettings().getDialogButtonStyle();

      if (dialogStyle == JDRResources.DIALOG_BUTTON_AS_GENERAL)
      {
         dialogButtonPanel.setVisible(false);
         asGeneralBox.setSelected(true);
      }
      else
      {
         dialogButtonPanel.setVisible(true);
         asGeneralBox.setSelected(false);

         int idx = 0;

         for (Enumeration<AbstractButton> en = dialogStyleButtons.getElements();
              en.hasMoreElements(); )
         {
            AbstractButton button = en.nextElement();

            if (idx == dialogStyle)
            {
               button.setSelected(true);
               break;
            }

            idx++;
         }
      }
   }

   public void okay()
   {
      int idx = lookAndFeelNamesBox.getSelectedIndex();

      application.getSettings().setLookAndFeel(info[idx].getClassName());

      for (Enumeration<AbstractButton> en = styleButtons.getElements();
           en.hasMoreElements(); )
      {
         AbstractButton button = en.nextElement();

         if (button.isSelected())
         {
            application.getSettings().setButtonStyle(button.getActionCommand());
         }
      }

      if (asGeneralBox.isSelected())
      {
         application.getSettings().setDialogButtonStyle(
            JDRResources.DIALOG_BUTTON_AS_GENERAL);
      }
      else
      {
         idx = 0;

         for (Enumeration<AbstractButton> en = dialogStyleButtons.getElements();
              en.hasMoreElements(); )
         {
            AbstractButton button = en.nextElement();

            if (button.isSelected())
            {
               application.getSettings().setDialogButtonStyle(idx);
               break;
            }

            idx++;
         }
      }
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   private JComboBox<String> lookAndFeelNamesBox;
   private UIManager.LookAndFeelInfo[] info;
   private ButtonGroup styleButtons, dialogStyleButtons;

   private JCheckBox asGeneralBox;
   private JComponent dialogButtonPanel;

   private FlowframTk application;
}

class VectorizeBitmapUIPanel extends JPanel
  implements ActionListener,ChangeListener
{
   public VectorizeBitmapUIPanel(FlowframTk application)
   {
      super(null);
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      this.application = application;
      JDRResources resources = application.getResources();

      JComponent comp = Box.createHorizontalBox();
      add(comp);

      JLabel notRegionLabel = resources.createAppLabel("vectorizeui.notregion");
      comp.add(notRegionLabel);

      Dimension dim = notRegionLabel.getPreferredSize();
      int maxWidth = dim.width;
      int maxHeight = dim.height;

      notRegionColorPanel = createSwatch();
      comp.add(notRegionColorPanel);

      selectNotRegionButton = resources.createDialogButton("label.choose",
        "choose", this, null);
      selectNotRegionButton.setActionCommand("notregion");
      notRegionLabel.setLabelFor(selectNotRegionButton);
      comp.add(selectNotRegionButton);
      comp.add(Box.createHorizontalGlue());

      comp = Box.createHorizontalBox();
      add(comp);

      JLabel pathLabel = resources.createAppLabel("vectorizeui.current_path");
      comp.add(pathLabel);

      dim = pathLabel.getPreferredSize();

      if (dim.width > maxWidth)
      {
         maxWidth = dim.width;
      }

      if (dim.height > maxHeight)
      {
         maxHeight = dim.height;
      }

      pathColorPanel = createSwatch();
      comp.add(pathColorPanel);

      selectPathButton = resources.createDialogButton("label.choose",
        "choose", this, null);
      selectPathButton.setActionCommand("path");
      pathLabel.setLabelFor(selectPathButton);
      comp.add(selectPathButton);
      comp.add(Box.createHorizontalGlue());

      comp = Box.createHorizontalBox();
      add(comp);

      JLabel connectorLabel = resources.createAppLabel("vectorizeui.connector");
      comp.add(connectorLabel);

      dim = connectorLabel.getPreferredSize();

      if (dim.width > maxWidth)
      {
         maxWidth = dim.width;
      }

      if (dim.height > maxHeight)
      {
         maxHeight = dim.height;
      }

      connectorColorPanel = createSwatch();
      comp.add(connectorColorPanel);

      selectConnectorButton = resources.createDialogButton("label.choose",
        "choose", this, null);
      selectConnectorButton.setActionCommand("connector");
      connectorLabel.setLabelFor(selectConnectorButton);
      comp.add(selectConnectorButton);
      comp.add(Box.createHorizontalGlue());

      comp = Box.createHorizontalBox();
      add(comp);

      JLabel dragLabel = resources.createAppLabel("vectorizeui.drag");
      comp.add(dragLabel);

      dim = dragLabel.getPreferredSize();

      if (dim.width > maxWidth)
      {
         maxWidth = dim.width;
      }

      if (dim.height > maxHeight)
      {
         maxHeight = dim.height;
      }

      dragColorPanel = createSwatch();
      comp.add(dragColorPanel);

      selectDragButton = resources.createDialogButton("label.choose",
        "choose", this, null);
      selectDragButton.setActionCommand("drag");
      dragLabel.setLabelFor(selectDragButton);

      comp.add(selectDragButton);
      comp.add(Box.createHorizontalGlue());

      comp = Box.createHorizontalBox();
      add(comp);

      JLabel controlLabel = resources.createAppLabel("vectorizeui.control");
      comp.add(controlLabel);

      dim = controlLabel.getPreferredSize();

      if (dim.width > maxWidth)
      {
         maxWidth = dim.width;
      }

      controlPointPanel = new JPanel()
      {
         public void paintComponent(Graphics g)
         {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;

            int controlSize = getControlSize();

            g2.setColor(getForeground());
            Dimension dim = getSize();

            g2.drawRect((dim.width-controlSize)/2,
                        (dim.height-controlSize)/2,
                        controlSize, controlSize);
         }
      };

      controlPointPanel.setBackground(Color.WHITE);
      controlPointPanel.setOpaque(true);

      dim = dragColorPanel.getPreferredSize();
      dim.height = dim.width;

      controlPointPanel.setPreferredSize(dim);
      controlPointPanel.setMaximumSize(dim);

      comp.add(controlPointPanel);

      controlButton = resources.createDialogButton("label.choose",
        "choose", this, null);
      controlButton.setActionCommand("control");
      controlLabel.setLabelFor(controlButton);

      comp.add(controlButton);

      JLabel sizeLabel = resources.createAppLabel("vectorizeui.control_size");
      comp.add(sizeLabel);

      controlPointSizeModel = new SpinnerNumberModel(4, 0, 100, 1);
      controlSizeSpinner = new JSpinner(controlPointSizeModel);
      controlSizeSpinner.addChangeListener(this);
      comp.add(controlSizeSpinner);
      sizeLabel.setLabelFor(controlSizeSpinner);

      dim = controlSizeSpinner.getPreferredSize();
      controlSizeSpinner.setMaximumSize(dim);

      comp.add(Box.createHorizontalGlue());

      dim = new Dimension(maxWidth, maxHeight);
      notRegionLabel.setPreferredSize(dim);
      pathLabel.setPreferredSize(dim);
      connectorLabel.setPreferredSize(dim);
      dragLabel.setPreferredSize(dim);
      controlLabel.setPreferredSize(dim);

      add(Box.createVerticalGlue());
   }

   private JComponent createSwatch()
   {
      JPanel panel = new JPanel();
      Dimension dim = new Dimension(60, 20);
      panel.setPreferredSize(dim);
      panel.setMaximumSize(dim);
      panel.setOpaque(true);

      return panel;
   }

   public int getControlSize()
   {
      return controlPointSizeModel == null ? 4 : controlPointSizeModel.getNumber().intValue();
   }

   public void initialise()
   {
      FlowframTkSettings settings = application.getSettings();

      notRegionColorPanel.setBackground(settings.getVectorizeNotRegion());
      pathColorPanel.setBackground(settings.getVectorizeLine());
      connectorColorPanel.setBackground(settings.getVectorizeConnector());
      dragColorPanel.setBackground(settings.getVectorizeDrag());
      controlPointPanel.setForeground(settings.getVectorizeControlColor());
      controlPointSizeModel.setValue(
         Integer.valueOf(settings.getVectorizeControlSize()));

      modified = false;
   }

   public void okay()
   {
      if (modified)
      {
         FlowframTkSettings settings = application.getSettings();

         settings.setVectorizeNotRegion(notRegionColorPanel.getBackground());
         settings.setVectorizeLine(pathColorPanel.getBackground());
         settings.setVectorizeConnector(connectorColorPanel.getBackground());
         settings.setVectorizeDrag(dragColorPanel.getBackground());

         settings.setVectorizeControlColor(controlPointPanel.getForeground());
         settings.setVectorizeControlSize(controlPointSizeModel.getNumber());

         application.repaintVectorizeBitmapDialog();
      }
   }

   public void stateChanged(ChangeEvent e)
   {
      if (e.getSource() == controlSizeSpinner)
      {
         controlPointPanel.repaint();
         modified = true;
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      JDRResources resources = application.getResources();

      if (action.equals("notregion"))
      {
         Color color = colorChooser.showDialog(this, 
            resources.getString("vectorizeui.notregion"),
            notRegionColorPanel.getBackground());

         if (color != null)
         {
            notRegionColorPanel.setBackground(color);
            modified = true;
         }
      }
      else if (action.equals("path"))
      {
         Color color = colorChooser.showDialog(this, 
            resources.getString("vectorizeui.current_path"),
            pathColorPanel.getBackground());

         if (color != null)
         {
            pathColorPanel.setBackground(color);
            modified = true;
         }
      }
      else if (action.equals("connector"))
      {
         Color color = colorChooser.showDialog(this, 
            resources.getString("vectorizeui.connector"),
            connectorColorPanel.getBackground());

         if (color != null)
         {
            connectorColorPanel.setBackground(color);
            modified = true;
         }
      }
      else if (action.equals("drag"))
      {
         Color color = colorChooser.showDialog(this, 
            resources.getString("vectorizeui.drag"),
            dragColorPanel.getBackground());

         if (color != null)
         {
            dragColorPanel.setBackground(color);
            modified = true;
         }
      }
      else if (action.equals("control"))
      {
         Color color = colorChooser.showDialog(this, 
            resources.getString("vectorizeui.control"),
            controlPointPanel.getForeground());

         if (color != null)
         {
            controlPointPanel.setForeground(color);
            modified = true;
         }
      }
   }

   private FlowframTk application;

   private JColorChooser colorChooser;

   private JComponent notRegionColorPanel, pathColorPanel,
    connectorColorPanel, dragColorPanel, controlPointPanel;

   private JButton selectNotRegionButton, selectPathButton,
     selectConnectorButton, selectDragButton, controlButton;

   private SpinnerNumberModel controlPointSizeModel;

   private JSpinner controlSizeSpinner;

   private boolean modified=false;
}
