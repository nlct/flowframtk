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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Compponent for setting typeblock.
 * @author Nicola L C Talbot
 */

public class TypeblockPanel extends JPanel
   implements ActionListener
{
   public TypeblockPanel(FlowframTk application)
   {
      super(null);
      application_ = application;

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      JDRResources resources = getResources();

      JTextArea infoArea = resources.createAppInfoArea("typeblock.info", 20);
      infoArea.setAlignmentX(0.0f);

      add(infoArea);

      JComponent settingsPanel = Box.createVerticalBox();
      settingsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      add(settingsPanel);

      margins = new MarginPanel(resources);
      margins.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanel.add(margins);

      JPanel row = new JPanel();
      row.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanel.add(row);

      computeFromPathButton = resources.createAppJButton(
        "typeblock", "compute_from_path", this);
      computeFromPathButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      row.add(computeFromPathButton);

      row = new JPanel(new FlowLayout(FlowLayout.LEADING));
      row.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanel.add(row);

      adjustWidthPanel = resources.createLengthPanel(
         "typeblock.adjust_width_label");
      row.add(adjustWidthPanel);

      JButton adjustWidthButton = resources.createAppJButton(
         "typeblock", "adjust_width", this);
      row.add(adjustWidthButton);

      settingsPanel.add(Box.createVerticalStrut(10));

      normalsizeInfoLabel = new JLabel(
         resources.getMessage("typeblock.current_normalsize", 10, 12));

      settingsPanel.add(normalsizeInfoLabel);

      row = new JPanel(new FlowLayout(FlowLayout.LEADING));
      row.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanel.add(row);

      row.add(resources.createAppLabel("typeblock.adjust_height_label"));

      ButtonGroup bg = new ButtonGroup();

      useBaselineButton = resources.createAppRadioButton(
         "typeblock", "use_baseline", bg, true, this);
      row.add(useBaselineButton);

      userButton = resources.createAppRadioButton(
         "typeblock", "use_other", bg, false, this);
      row.add(userButton);

      adjustHeightPanel = resources.createLengthPanel();
      row.add(adjustHeightPanel);

      adjustHeightPanel.setEnabled(false);

      JButton adjustHeightButton = resources.createAppJButton(
         "typeblock", "adjust_height", this);
      row.add(adjustHeightButton);

      settingsPanel.add(Box.createVerticalStrut(10));

      row = new JPanel();
      row.setAlignmentX(Component.LEFT_ALIGNMENT);
      settingsPanel.add(row);

      shiftPanel = resources.createLengthPanel("typeblock.hshift");
      shiftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      row.add(shiftPanel);

      JButton computeSymShiftButton = resources.createAppJButton(
         "typeblock", "compute_sym_shift", this);
      computeSymShiftButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      row.add(computeSymShiftButton);

      infoArea = resources.createAppInfoArea("typeblock.hshift_info", 20);
      infoArea.setAlignmentX(0.0f);
      add(infoArea);

      Dimension dim = getPreferredSize();
      dim.height += 100;
      setPreferredSize(dim);
   }

   public void requestDefaultComponentFocus()
   {
      margins.requestDefaultComponentFocus();
   }

   public void updateComponent(FlowFrame typeblock, JDRUnit unit)
   {
      if (typeblock != null)
      {
         CanvasGraphics cg = typeblock.getCanvasGraphics();
         JDRUnit storageUnit = cg.getStorageUnit();

         margins.setMargins(unit,
                            storageUnit.toUnit(typeblock.getLeft(), unit),
                            storageUnit.toUnit(typeblock.getRight(), unit),
                            storageUnit.toUnit(typeblock.getTop(), unit),
                            storageUnit.toUnit(typeblock.getBottom(), unit));
         shiftPanel.setValue(
           storageUnit.toUnit(typeblock.getEvenXShift(), unit), unit);
      }
      else
      {
         double def = unit.fromBp(72);
         margins.setMargins(unit, def, def, def, def);
         shiftPanel.setValue(0.0, JDRUnit.pt);
         adjustWidthPanel.setValue(1.0, JDRUnit.pc);
      }

      JDRCanvas canvas = application_.getCurrentFrame().getCanvas();

      selectedPath = canvas.getSelectedPath();

      computeFromPathButton.setEnabled(selectedPath != null);

      LaTeXFontBase latexFonts = canvas.getCanvasGraphics()
         .getLaTeXFontBase();

      normalsizeInfoLabel.setText(getResources().getMessage(
         "typeblock.current_normalsize",
           latexFonts.getNormalSize(),
           latexFonts.getBaselineskip(LaTeXFontBase.NORMALSIZE)
         ));
   }

   public void apply()
   {
      JDRFrame settingsPanel = application_.getCurrentFrame();

      JDRUnit unit = settingsPanel.getCanvasGraphics().getStorageUnit();

      settingsPanel.setTypeblock(margins.left(unit), margins.right(unit),
                             margins.top(unit), margins.bottom(unit),
                             shiftPanel.getValue(unit));
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("compute_sym_shift"))
      {
         JDRFrame settingsPanel = application_.getCurrentFrame();
         JDRUnit unit = settingsPanel.getCanvasGraphics().getStorageUnit();
         double left = margins.left(unit);
         double right = margins.right(unit);

         shiftPanel.setValue(right-left, unit);
      }
      else if (action.equals("compute_from_path"))
      {
         if (selectedPath == null) return;

         Rectangle2D bounds = selectedPath.getGeneralPath().getBounds2D();

         double left = bounds.getX();
         double top = bounds.getY();

         JDRFrame frame = application_.getCurrentFrame();
         JDRUnit unit = frame.getCanvasGraphics().getStorageUnit();

         double paperWidth = frame.getStoragePaperWidth();
         double paperHeight = frame.getStoragePaperHeight();

         double right = paperWidth-left-bounds.getWidth();
         double bottom = paperHeight-top-bounds.getHeight();

         margins.setMargins(unit, left, right, top, bottom);
      }
      else if (action.equals("use_baseline"))
      {
         adjustHeightPanel.setEnabled(false);
      }
      else if (action.equals("use_other"))
      {
         adjustHeightPanel.setEnabled(true);
      }
      else if (action.equals("adjust_width"))
      {
         JDRLength length = adjustWidthPanel.getLength();

         JDRFrame frame = application_.getCurrentFrame();

         JDRUnit unit = length.getUnit();

         double paperWidth = 
            JDRUnit.bp.toUnit(frame.getBpPaperWidth(), unit);

         double left = margins.left(unit);
         double right = margins.right(unit);

         double width = paperWidth-left-right;

         int n = (int)Math.round(width/length.getValue());

         width = n * length.getValue();

         right = paperWidth - width - left;

         margins.setRight(right, unit);
      }
      else if (action.equals("adjust_height"))
      {
         if (useBaselineButton.isSelected())
         {
            JDRFrame frame = application_.getCurrentFrame();
            adjustHeight(frame.getNormalSize(), JDRUnit.pt);
         }
         else
         {
            adjustHeight(adjustHeightPanel.getLength());
         }
      }
   }

   private void adjustHeight(JDRLength length)
   {
      adjustHeight(length.getValue(), length.getUnit());
   }

   private void adjustHeight(double length, JDRUnit unit)
   {
      JDRFrame frame = application_.getCurrentFrame();

      double paperHeight = 
         JDRUnit.bp.toUnit(frame.getBpPaperHeight(), unit);

      double top = margins.top(unit);
      double bottom = margins.bottom(unit);

      double height = paperHeight-top-bottom;

      int n = (int)Math.round(height/length);

      height = n * length;

      bottom = paperHeight - height - top;

      margins.setBottom(bottom, unit);
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;
   private MarginPanel margins;

   private JButton computeFromPathButton;

   private LengthPanel shiftPanel, adjustWidthPanel, adjustHeightPanel;

   private JRadioButton useBaselineButton, userButton;

   private JDRPath selectedPath;

   private JLabel normalsizeInfoLabel;
}
