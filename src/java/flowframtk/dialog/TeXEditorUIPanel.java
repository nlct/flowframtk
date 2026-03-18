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

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.*;

public class TeXEditorUIPanel extends JPanel
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

      gbc.gridx=0;
      gbc.gridy++;

      JLabel colWidthLabel = resources.createAppLabel("texeditorui.num_columns");
      add(colWidthLabel, gbc);

      colNumModel = new SpinnerNumberModel(80, 1, 10000, 1);
      JSpinner colNumSpinner = new JSpinner(colNumModel);
      colWidthLabel.setLabelFor(colNumSpinner);

      gbc.gridx++;
      add(colNumSpinner, gbc);

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
         "button.choose_colour", "choose_colour", this, null);
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
         "button.choose_colour", "choose_colour", this, null);
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

   public void initialise(FlowframTk application)
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

   public void okay(FlowframTk application)
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
   private JPanel commentColorPanel, csColorPanel;
   private JColorChooser colorChooser;
   private JLabel commentColorLabel, csColorLabel;
   private JButton commentSelectButton, csSelectButton;

   private JRadioButton leftButton, rightButton, aboveButton, belowButton;

   private JDRResources resources;
}
