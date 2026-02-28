// File          : ScaleDialogBox.java
// Description   : Dialog box for scaling objects
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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for scaling objects.
 */

public class ScaleDialogBox extends JDialog
   implements ActionListener,ItemListener
{
   public ScaleDialogBox(FlowframTk application)
   {
      super(application, 
         application.getResources().getMessage("scale.title"), true);
      application_ = application;

      JDRResources resources = getResources();

      int width  = 300;
      int height = 150;
      setSize(width,height);
      setLocationRelativeTo(application);

      JComponent mainComp = Box.createVerticalBox();
      mainComp.setAlignmentX(JComponent.LEFT_ALIGNMENT);

      getContentPane().add(new JScrollPane(mainComp), "Center");

      JComponent row = createRow();
      mainComp.add(row);

      ButtonGroup bg = new ButtonGroup();

      scaleXButton = resources.createAppRadioButton("scale", "x",
        bg, false, null);

      scaleXButton.addItemListener(this);

      row.add(scaleXButton);

      row.add(resources.createLabelSpacer());

      scaleXSpinnerModel = new SpinnerNumberModel(
         Double.valueOf(1.0), null, null, Double.valueOf(0.25));

      scaleXSpinner = new JSpinner(scaleXSpinnerModel);
      setSpinnerColumns(scaleXSpinner, 6);
      row.add(scaleXSpinner);

      row.add(Box.createHorizontalGlue());

      row = createRow();
      mainComp.add(row);

      scaleYButton = resources.createAppRadioButton("scale", "y",
         bg, false, null);
      row.add(scaleYButton);

      scaleYButton.addItemListener(this);

      row.add(resources.createLabelSpacer());

      scaleYSpinnerModel = new SpinnerNumberModel(
         Double.valueOf(1.0), null, null, Double.valueOf(0.25));

      scaleYSpinner = new JSpinner(scaleYSpinnerModel);
      setSpinnerColumns(scaleYSpinner, 6);
      row.add(scaleYSpinner);

      row.add(Box.createHorizontalGlue());

      row = createRow();
      mainComp.add(row);

      scaleButton = resources.createAppRadioButton("scale", "both",
         bg, true, this);
      row.add(scaleButton);

      scaleButton.addItemListener(this);

      row.add(resources.createLabelSpacer());

      bothScaleXLabelComp = Box.createHorizontalBox();
      row.add(bothScaleXLabelComp);

      JLabelGroup labelGroup = new JLabelGroup();

      JLabel label = resources.createAppLabel("scale.both.x");
      labelGroup.add(label);
      bothScaleXLabelComp.add(label);
      bothScaleXLabelComp.add(resources.createLabelSpacer());

      scaleSpinnerModel = new SpinnerNumberModel(
         Double.valueOf(1.0), null, null, Double.valueOf(0.25));

      scaleSpinner = new JSpinner(scaleSpinnerModel);
      label.setLabelFor(scaleSpinner);
      setSpinnerColumns(scaleSpinner, 6);
      row.add(scaleSpinner);

      row.add(Box.createHorizontalGlue());

      row = createRow();
      mainComp.add(row);

      keepAspectButton = resources.createAppCheckBox("scale", "keep_aspect", false, null);
      keepAspectButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      row.add(keepAspectButton);

      row.add(resources.createLabelSpacer());

      bothScaleYLabelComp = Box.createHorizontalBox();
      row.add(bothScaleYLabelComp);

      label = resources.createAppLabel("scale.both.y");
      labelGroup.add(label);
      bothScaleYLabelComp.add(label);
      bothScaleYLabelComp.add(resources.createLabelSpacer());

      nonAspectScaleYSpinnerModel = new SpinnerNumberModel(
         Double.valueOf(1.0), null, null, Double.valueOf(0.25));

      nonAspectScaleYSpinner = new JSpinner(nonAspectScaleYSpinnerModel);
      label.setLabelFor(nonAspectScaleYSpinner);
      setSpinnerColumns(nonAspectScaleYSpinner, 6);
      row.add(nonAspectScaleYSpinner);
      nonAspectScaleYSpinner.setEnabled(!keepAspectButton.isSelected());

      keepAspectButton.addItemListener(this);

      JLabelGroup.setSameMinPrefMaxWidth(scaleXButton, scaleYButton, scaleButton,
       keepAspectButton);

      JLabelGroup.setSameMinPrefMaxWidth(scaleXSpinner, scaleYSpinner, 
       scaleSpinner, nonAspectScaleYSpinner);

      row.add(Box.createHorizontalGlue());

      row = createRow();
      mainComp.add(row);

      anchorXComp = new AnchorXPanel(application);
      anchorXComp.setSelectedAnchor(AnchorX.LEFT);
      row.add(anchorXComp);

      row.add(resources.createButtonSpacer());

      anchorYComp = new AnchorYPanel(application);
      anchorYComp.setSelectedAnchor(AnchorY.TOP);
      row.add(anchorYComp);

      mainComp.add(Box.createVerticalStrut(10));

      row = createRow();
      mainComp.add(row);

      row.add(resources.createAppLabel("scale.calculate"));
      row.add(Box.createHorizontalGlue());

      row = createRow();
      mainComp.add(row);

      labelGroup = new JLabelGroup();

      label = resources.createAppLabel("scale.width");
      row.add(label);
      labelGroup.add(label);

      row.add(resources.createLabelSpacer());

      widthNumberModel = new SpinnerNumberModel(50, -1000, 1000, 1);
      widthSpinner = new JSpinner(widthNumberModel);

      row.add(widthSpinner);

      label.setLabelFor(widthSpinner);

      row.add(resources.createAppLabel("scale.percent_of"));

      row.add(resources.createLabelSpacer());

      widthCardLayout = new CardLayout();
      widthComp = new JPanel(widthCardLayout);

      row.add(widthComp);

      paperWidthComp = resources.createAppLabel("scale.paper_width");

      widthComp.add(paperWidthComp, "paper");

      widthComboBox = new JComboBox<String>(
        new String[]
         {
           resources.getMessage("scale.paper_width"),
           resources.getMessage("scale.typeblock_width")
         }
      );

      widthComp.add(widthComboBox, "choice");

      row.add(resources.createButtonSpacer());

      row.add(resources.createDialogButton("scale", "calc_x", this, null));
      row.add(resources.createLabelSpacer());
      row.add(Box.createHorizontalGlue());

      resources.clampCompMaxHeight(row, 0, 20);

      row = createRow();
      mainComp.add(row);

      label = resources.createAppLabel("scale.height");
      row.add(label);
      labelGroup.add(label);

      row.add(resources.createLabelSpacer());

      heightNumberModel = new SpinnerNumberModel(50, -1000, 1000, 1);
      heightSpinner = new JSpinner(heightNumberModel);

      row.add(heightSpinner);

      label.setLabelFor(heightSpinner);

      row.add(resources.createAppLabel("scale.percent_of"));

      row.add(resources.createLabelSpacer());

      heightCardLayout = new CardLayout();
      heightComp = new JPanel(heightCardLayout);

      paperHeightComp = resources.createAppLabel("scale.paper_height");
      row.add(heightComp);

      heightComp.add(paperHeightComp, "paper");

      heightComboBox = new JComboBox<String>(
        new String[]
         {
           resources.getMessage("scale.paper_height"),
           resources.getMessage("scale.typeblock_height")
         }
      );

      heightComp.add(heightComboBox, "choice");

      row.add(resources.createButtonSpacer());

      row.add(resources.createDialogButton("scale", "calc_y", this, null));

      row.add(resources.createLabelSpacer());
      row.add(Box.createHorizontalGlue());

      resources.clampCompMaxHeight(row, 0, 20);

      mainComp.add(Box.createVerticalStrut(10));

      JTextArea info = resources.createAppInfoArea(40, "scale.calculate_info");
      info.setRows(5);
      info.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      mainComp.add(info);

      mainComp.add(Box.createVerticalGlue());

      JLabelGroup.setSameMinPrefMaxWidth(widthSpinner, heightSpinner);
      JLabelGroup.setSameMinPrefMaxWidth(widthComp, heightComp);

      JPanel p2 = new JPanel();

      resources.createOkayCancelHelpButtons(this, p2, this, "sec:scaleobjects");

      getContentPane().add(p2, "South");

      pack();

      scaleXSpinner.setEnabled(false);
      scaleYSpinner.setEnabled(false);
      scaleSpinner.setEnabled(true);
      keepAspectButton.setSelected(true);
   }

   protected JComponent createRow()
   {
      JComponent row = Box.createHorizontalBox();
      row.setAlignmentX(JComponent.LEFT_ALIGNMENT);

      return row;
   }

   public void display()
   {
      if (scaleXButton.isSelected())
      {
         requestSpinnerFocus(scaleXSpinner);
      }
      else if (scaleYButton.isSelected())
      {
         requestSpinnerFocus(scaleYSpinner);
      }
      else
      {
         requestSpinnerFocus(scaleSpinner);
      }

      frame = application_.getCurrentFrame();
      typeblock = frame.getTypeblock();
      selectedObject = null;

      if (typeblock == null)
      {
         widthCardLayout.show(widthComp, "paper");
         heightCardLayout.show(heightComp, "paper");
      }
      else
      {
         widthCardLayout.show(widthComp, "choice");
         heightCardLayout.show(heightComp, "choice");
      }

      setVisible(true);
   }

   @Override
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
      else if (action.equals("calc_x"))
      {
         if (selectedObject == null)
         {
            getSelectedBounds();
         }

         if (selectedWidth == 0.0)
         {
            getResources().error(this, getResources().getMessage("error.nan_zero_width"));
         }

         int pc = widthNumberModel.getNumber().intValue();
         double totalWidth = paperWidth;

         if (typeblock != null && widthComboBox.isVisible()
               && widthComboBox.getSelectedIndex() == TYPEBLOCK)
         {
            totalWidth -= typeblock.getLeft();
            totalWidth -= typeblock.getRight();
         }

         Double scaleX = Double.valueOf(0.01 * (pc * totalWidth) / selectedWidth);

         if (scaleX.isNaN() || scaleX.isInfinite())
         {
            getResources().error(this, getResources().getMessage("error.nan_or_infinite"));
         }
         else
         {
            if (scaleSpinner.isEnabled())
            {
               scaleSpinner.setValue(scaleX);
            }
            else
            {
               if (!scaleXSpinner.isEnabled())
               {
                  scaleXButton.setSelected(true);
               }

               scaleXSpinner.setValue(scaleX);
            }
         }
      }
      else if (action.equals("calc_y"))
      {
         if (selectedObject == null)
         {
            getSelectedBounds();
         }

         if (selectedHeight == 0.0)
         {
            getResources().error(this, getResources().getMessage("error.nan_zero_height"));
         }

         int pc = heightNumberModel.getNumber().intValue();
         double totalHeight = paperHeight;

         if (typeblock != null && heightComboBox.isVisible()
               && heightComboBox.getSelectedIndex() == TYPEBLOCK)
         {
            totalHeight -= typeblock.getLeft();
            totalHeight -= typeblock.getRight();
         }

         Double scaleY = Double.valueOf(0.01 * (pc * totalHeight) / selectedHeight);

         if (scaleY.isNaN() || scaleY.isInfinite(scaleY))
         {
            getResources().error(this, getResources().getMessage("error.nan_or_infinite"));
         }
         else
         {
            if (scaleButton.isSelected())
            {
               nonAspectScaleYSpinner.setValue(scaleY);

               if (keepAspectButton.isSelected())
               {
                  scaleSpinner.setValue(scaleY);
               }
            }
            else
            {
               if (!scaleYSpinner.isEnabled())
               {
                  scaleYButton.setSelected(true);
               }

               scaleYSpinner.setValue(scaleY);
            }
         }
      }
   }

   @Override
   public void itemStateChanged(ItemEvent evt)
   {
      if (evt.getSource() == keepAspectButton)
      {
         if (scaleButton.isSelected())
         {
            nonAspectScaleYSpinner.setEnabled(!keepAspectButton.isSelected());
            nonAspectScaleYSpinner.setVisible(nonAspectScaleYSpinner.isEnabled());
            bothScaleXLabelComp.setVisible(nonAspectScaleYSpinner.isVisible());
            bothScaleYLabelComp.setVisible(nonAspectScaleYSpinner.isVisible());

            if (evt.getStateChange() == ItemEvent.DESELECTED)
            {
               requestSpinnerFocus(nonAspectScaleYSpinner);
            }
         }
      }
      else if (evt.getStateChange() == ItemEvent.SELECTED)
      {
         if (scaleXButton.isSelected())
         {
            scaleXSpinner.setEnabled(true);
            scaleYSpinner.setEnabled(false);
            scaleSpinner.setEnabled(false);
            keepAspectButton.setEnabled(false);
            nonAspectScaleYSpinner.setEnabled(false);
            requestSpinnerFocus(scaleXSpinner);
         }
         else if (scaleYButton.isSelected())
         {
            scaleXSpinner.setEnabled(false);
            scaleYSpinner.setEnabled(true);
            scaleSpinner.setEnabled(false);
            keepAspectButton.setEnabled(false);
            nonAspectScaleYSpinner.setEnabled(false);
            requestSpinnerFocus(scaleYSpinner);
         }
         else if (scaleButton.isSelected())
         {
            scaleXSpinner.setEnabled(false);
            scaleYSpinner.setEnabled(false);
            scaleSpinner.setEnabled(true);
            keepAspectButton.setEnabled(true);
            nonAspectScaleYSpinner.setEnabled(!keepAspectButton.isSelected());
            requestSpinnerFocus(scaleSpinner);
         }
      }
   }

   protected void requestSpinnerFocus(JSpinner spinner)
   {
      JComponent editor = spinner.getEditor();

      if (editor instanceof JSpinner.DefaultEditor)
      {
         ((JSpinner.DefaultEditor)editor).getTextField().requestFocusInWindow();
      }
      else
      {
         spinner.requestFocusInWindow();
      }
   }

   protected void setSpinnerColumns(JSpinner spinner, int cols)
   {
      JComponent editor = spinner.getEditor();

      if (editor instanceof JSpinner.DefaultEditor)
      {
         ((JSpinner.DefaultEditor)editor).getTextField().setColumns(cols);
      }
   }

   protected void getSelectedBounds()
   {
      selectedObject = frame.getSelectedObject();

      if (selectedObject instanceof JDRShape)
      {
         Rectangle2D bounds = ((JDRShape)selectedObject).getGeneralPath().getBounds2D();
         selectedWidth = bounds.getWidth();
         selectedHeight = bounds.getHeight();
      }
      else
      {
         BBox box = selectedObject.getStorageBBox();

         selectedWidth = box.getWidth();
         selectedHeight = box.getHeight();
      }

      paperWidth = frame.getStoragePaperWidth();
      paperHeight = frame.getStoragePaperHeight();
   }

   public void okay()
   {
      setVisible(false);

      if (scaleXButton.isSelected())
      {
         frame.scaleXSelectedPaths(scaleXSpinnerModel.getNumber().doubleValue(),
          anchorXComp.getSelectedAnchor(),
          anchorYComp.getSelectedAnchor());
      }
      else if (scaleYButton.isSelected())
      {
         frame.scaleYSelectedPaths(scaleYSpinnerModel.getNumber().doubleValue(),
          anchorXComp.getSelectedAnchor(),
          anchorYComp.getSelectedAnchor());
      }
      else if (keepAspectButton.isSelected())
      {
         frame.scaleSelectedPaths(scaleSpinnerModel.getNumber().doubleValue(),
          anchorXComp.getSelectedAnchor(),
          anchorYComp.getSelectedAnchor());
      }
      else
      {
         frame.scaleSelectedPaths(
           scaleSpinnerModel.getNumber().doubleValue(),
           nonAspectScaleYSpinnerModel.getNumber().doubleValue(),
          anchorXComp.getSelectedAnchor(),
          anchorYComp.getSelectedAnchor());
      }
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "RotateDialogBox:"+eol;
      str += "has focus: "+hasFocus()+eol;
      str += "scale x field has focus: "+scaleXSpinner.hasFocus()+eol;
      str += "scale y field has focus: "+scaleYSpinner.hasFocus()+eol;
      str += "scale field has focus: "+scaleSpinner.hasFocus()+eol;
      str += "scale x button has focus: "+scaleXButton.hasFocus()+eol;
      str += "scale y button has focus: "+scaleYButton.hasFocus()+eol;
      str += "scale button has focus: "+scaleButton.hasFocus()+eol;

      ActionMap actionMap = getRootPane().getActionMap();
      str += "action map: "+eol;

      Object[] allKeys = actionMap.allKeys();

      for (int i = 0; i < allKeys.length; i++)
      {
         str += "Key: "+allKeys[i]+" Action: "+actionMap.get(allKeys[i])+eol;
      }

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private SpinnerNumberModel scaleXSpinnerModel, scaleYSpinnerModel, scaleSpinnerModel,
    nonAspectScaleYSpinnerModel;
   private JSpinner scaleXSpinner, scaleYSpinner, scaleSpinner,
    nonAspectScaleYSpinner;

   private JRadioButton scaleXButton, scaleYButton, scaleButton;

   private AnchorXPanel anchorXComp;
   private AnchorYPanel anchorYComp;

   SpinnerNumberModel widthNumberModel, heightNumberModel;
   JSpinner widthSpinner, heightSpinner;

   JCheckBox keepAspectButton;

   JComponent bothScaleXLabelComp, bothScaleYLabelComp;

   JComponent widthComp, heightComp, paperWidthComp, paperHeightComp;
   CardLayout widthCardLayout, heightCardLayout;

   JComboBox<String> widthComboBox, heightComboBox;

   static final int PAPER=0, TYPEBLOCK=1;

   private FlowframTk application_;
   private JDRFrame frame;
   private FlowFrame typeblock;

   JDRCompleteObject selectedObject;
   double selectedWidth = 0.0, selectedHeight = 0.0,
          paperWidth = 0.0, paperHeight = 0.0;
}
