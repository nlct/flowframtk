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

import com.dickimawbooks.texjavahelplib.JLabelGroup;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.*;

public class TeXEditorUIPanel extends JPanel
   implements ActionListener
{
   public TeXEditorUIPanel(FlowframTk application)
   {
      super(null);

      this.application = application;
      resources = application.getResources();

      init();
   }

   protected void init()
   {
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      setAlignmentX(0f);

      JTextArea infoArea = resources.createAppInfoArea("texeditorui.info");
      infoArea.setRows(3);
      infoArea.setAlignmentX(0f);
      add(infoArea);

      JLabelGroup labelGroup = new JLabelGroup();
      JComponent row = createRow();
      add(row);

      JLabel fontNameLabel = resources.createAppLabel("texeditorui.font");
      labelGroup.add(fontNameLabel);
      row.add(fontNameLabel);

      fontSelector = new JavaFontSelector(application,
        fontNameLabel, null, null, "texeditorui.fontsize");

      row.add(resources.createLabelSpacer());
      row.add(fontSelector);

      row = createRow();
      add(row);

      JLabel colWidthLabel = resources.createAppLabel("texeditorui.num_columns");
      labelGroup.add(colWidthLabel);
      row.add(colWidthLabel);

      colNumModel = new SpinnerNumberModel(80, 1, 10000, 1);
      JSpinner colNumSpinner = new JSpinner(colNumModel);
      colWidthLabel.setLabelFor(colNumSpinner);

      row.add(resources.createLabelSpacer());
      row.add(colNumSpinner);

      row.add(Box.createVerticalStrut(20));

      highlightCheckBox = resources.createAppCheckBox("texeditorui",
        "highlight", true, this);

      add(highlightCheckBox);

      row = createRow();
      add(row);

      commentColorLabel = resources.createAppLabel("texeditorui.comment");
      labelGroup.add(commentColorLabel);

      row.add(commentColorLabel);

      commentColorPanel = createSwatch();

      row.add(resources.createLabelSpacer());
      row.add(commentColorPanel);

      row.add(resources.createButtonSpacer());

      commentSelectButton = resources.createDialogButton(
         "button.choose_colour", "choose_colour", this, null);
      commentSelectButton.setActionCommand("commentselect");
      commentColorLabel.setLabelFor(commentSelectButton);

      row.add(commentSelectButton);

      row = createRow();
      add(row);

      csColorLabel = resources.createAppLabel("texeditorui.cs");
      labelGroup.add(csColorLabel);

      row.add(csColorLabel);

      csColorPanel = createSwatch();

      row.add(resources.createLabelSpacer());
      row.add(csColorPanel);

      row.add(resources.createButtonSpacer());

      csSelectButton = resources.createDialogButton(
         "button.choose_colour", "choose_colour", this, null);
      csSelectButton.setActionCommand("csselect");
      csColorLabel.setLabelFor(csSelectButton);

      row.add(csSelectButton);

      colorChooser = new JColorChooser();

      add(Box.createVerticalStrut(20));

      JComponent prefSizeComp = new JPanel(new GridBagLayout());
      prefSizeComp.setAlignmentX(0f);
      GridBagConstraints prefSizeGbc = new GridBagConstraints();

      add(prefSizeComp);

      prefSizeComp.setBorder(BorderFactory.createTitledBorder(
         resources.getMessage("texeditorui.dim")));

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
         resources.getMessage("texeditorui.width.tooltip"));
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
         resources.getMessage("texeditorui.height.tooltip"));
      heightLabel.setLabelFor(heightField);
      heightLabel.setToolTipText(heightField.getToolTipText());

      prefSizeGbc.gridx++;
      prefSizeComp.add(heightField, prefSizeGbc);

      row.add(Box.createVerticalStrut(20));

      JComponent splitComp = new JPanel(new GridBagLayout());
      splitComp.setAlignmentX(0f);
      add(splitComp);

      GridBagConstraints splitGbc = new GridBagConstraints();
      splitGbc.fill=GridBagConstraints.NONE;
      splitGbc.gridx=0;
      splitGbc.gridy=0;

      splitComp.setBorder(BorderFactory.createTitledBorder(
        resources.getMessage("texeditorui.split")));

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

   protected JComponent createRow()
   {
      JComponent row = new JPanel(new FlowLayout(FlowLayout.LEADING));
      row.setAlignmentX(0f);
      return row;
   }

   protected JComponent createSwatch()
   {
      JComponent comp = new JPanel();
      Dimension dim = new Dimension(60, 20);
      comp.setPreferredSize(dim);
      comp.setMinimumSize(dim);
      comp.setMaximumSize(dim);
      comp.setOpaque(true);

      return comp;
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("commentselect"))
      {
         Color color = colorChooser.showDialog(this, 
            resources.getMessage("texeditorui.comment"),
            commentColorPanel.getBackground());

         if (color != null)
         {
            commentColorPanel.setBackground(color);
         }
      }
      else if (action.equals("csselect"))
      {
         Color color = colorChooser.showDialog(this, 
            resources.getMessage("texeditorui.cs"),
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

   public void initialise()
   {
      Color col = application.getCommentHighlight();
      commentColorPanel.setBackground(col);

      col = application.getControlSequenceHighlight();
      csColorPanel.setBackground(col);

      setHighlight(application.isSyntaxHighlightingOn());

      fontSelector.setSelectedFont(application.getTeXEditorFont());

      widthModel.setValue(Integer.valueOf(application.getTeXEditorWidth()));
      heightModel.setValue(Integer.valueOf(application.getTeXEditorHeight()));

      FlowframTkSettings settings = application.getSettings();

      colNumModel.setValue(Integer.valueOf(settings.getCodeBlockEditorMaxColumns()));

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

   public void okay()
   {
      FlowframTkSettings settings = application.getSettings();

      application.setTeXEditorWidth(widthModel.getNumber().intValue());
      application.setTeXEditorHeight(heightModel.getNumber().intValue());

      settings.setCodeBlockEditorMaxColumns(colNumModel.getNumber().intValue());

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
   private SpinnerNumberModel widthModel, heightModel, colNumModel;
   private JCheckBox highlightCheckBox;
   private JComponent commentColorPanel, csColorPanel;
   private JColorChooser colorChooser;
   private JLabel commentColorLabel, csColorLabel;
   private JButton commentSelectButton, csSelectButton;

   private JRadioButton leftButton, rightButton, aboveButton, belowButton;

   private JDRResources resources;
   private FlowframTk application;
}
