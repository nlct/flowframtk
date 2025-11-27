// File          : FLFSelector.java
// Description   : Dialog for setting flowframe data
// Creation Date : 6th February 2006
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
import java.awt.geom.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for setting flowframe data.
 * @see ArrowStyleSelector
 * @author Nicola L C Talbot
 */

public class FLFSelector extends JDialog
   implements ActionListener,ItemListener
{
   public FLFSelector(FlowframTk application)
   {
      super(application,
         application.getResources().getMessage("flowframe.title"),true);
      application_ = application;

      JDRResources resources = getResources();

      JComponent mainPanel = Box.createVerticalBox();
      getContentPane().add(new JScrollPane(mainPanel), "Center");

      JLabelGroup labelGroup1 = new JLabelGroup();
      JLabelGroup labelGroup2 = new JLabelGroup();

      JComponent row;

      row = createRow();
      mainPanel.add(row);

      JLabel typeLabel = resources.createAppLabel("flowframe.type");
      labelGroup1.add(typeLabel);
      row.add(typeLabel);

      row.add(resources.createLabelSpacer());

      type = new JComboBox<String>(
         new String[]
         {
            resources.getMessage("flowframe.static"),
            resources.getMessage("flowframe.flow"),
            resources.getMessage("flowframe.dynamic"),
            resources.getMessage("flowframe.none")
         }
      );
      type.addItemListener(this);
      typeLabel.setLabelFor(type);
      row.add(type);

      row.add(resources.createButtonSpacer());

      labelLabel = resources.createAppLabel("flowframe.label");
      labelGroup2.add(labelLabel);
      row.add(labelLabel);

      row.add(resources.createLabelSpacer());

      labelText = new JTextField(20);
      labelLabel.setLabelFor(labelText);
      row.add(labelText);

      clampCompMaxHeight(row);

      row = createRow();
      mainPanel.add(row);

      borderLabel = getResources().createAppLabel("flowframe.border");
      row.add(borderLabel);
      labelGroup1.add(borderLabel);

      row.add(resources.createLabelSpacer());

      border = new JComboBox<String>(
         new String[]
         {
             resources.getMessage("flowframe.border_none"),
             resources.getMessage("flowframe.border_as_shown")
         }
      );
      border.setSelectedItem(
         resources.getMessage("flowframe.border_as_shown"));
      borderLabel.setLabelFor(border);
      row.add(border);

      row.add(resources.createButtonSpacer());

      pagesLabel = resources.createAppLabel("flowframe.pages");
      row.add(pagesLabel);
      labelGroup2.add(pagesLabel);

      row.add(resources.createLabelSpacer());

      pageList = new JComboBox<String>(
         new String[]
         {
            resources.getMessage("flowframe.pages_all"),
            resources.getMessage("flowframe.pages_none"),
            resources.getMessage("flowframe.pages_odd"),
            resources.getMessage("flowframe.pages_even")
         }
      );
      pageList.setEditable(true);
      pageList.setPrototypeDisplayValue("<0,0,0,0-0,>0");
      pagesLabel.setLabelFor(pageList);
      row.add(pageList);
      row.add(Box.createHorizontalGlue());

      clampCompMaxHeight(row);

      row = createRow();
      mainPanel.add(row);

      shapeLabel = getResources().createAppLabel("flowframe.shape");
      row.add(shapeLabel);
      labelGroup1.add(shapeLabel);

      row.add(resources.createLabelSpacer());

      shapeBox = new JComboBox<String>(
         new String[]
         {
            resources.getMessage("flowframe.shape_standard"),
            resources.getMessage("flowframe.shape_parshape"),
            resources.getMessage("flowframe.shape_shapepar")
          }
      );
      shapeBox.addItemListener(this);
      shapeLabel.setLabelFor(shapeBox);
      row.add(shapeBox);

      row.add(resources.createButtonSpacer());

      alignLabel = getResources().createAppLabel("flowframe.align");
      row.add(alignLabel);
      labelGroup2.add(alignLabel);

      row.add(resources.createLabelSpacer());

      alignBox = new JComboBox<String>(
         new String[]
         {
            resources.getMessage("flowframe.align_top"),
            resources.getMessage("flowframe.align_middle"),
            resources.getMessage("flowframe.align_bottom")
          }
      );
      alignLabel.setLabelFor(alignBox);
      row.add(alignBox);
      row.add(Box.createHorizontalGlue());

      clampCompMaxHeight(row);

      JLabelGroup.setSameMinPrefMaxWidth(type, border, shapeBox);

      mainPanel.add(Box.createVerticalStrut(10));

      row = createRow();
      mainPanel.add(row);

      styleCmdsLabel = resources.createAppLabel("flowframe.style_cmds");
      row.add(styleCmdsLabel);

      row.add(resources.createLabelSpacer());

      styleCmdsField = new JTextField(20);
      styleCmdsLabel.setLabelFor(styleCmdsField);
      row.add(styleCmdsField);

      row.add(resources.createLabelSpacer());

      row.add(resources.createAppInfoField("flowframe.style_cmds.info"));

      clampCompMaxHeight(row);

      margins = new MarginPanel(application,
       resources.getMessage("flowframe.frame_margins"), true);
      margins.setAlignmentX(Component.LEFT_ALIGNMENT);
      mainPanel.add(margins);

      JTextField infoArea = resources.createAppInfoField("flowframe.twoside_note");
      infoArea.setAlignmentX(Component.LEFT_ALIGNMENT);
      mainPanel.add(infoArea);

      clampCompMaxHeight(infoArea);

      JComponent evenXShiftBox = createRow();
      mainPanel.add(evenXShiftBox);

      evenXShiftLabel = 
         resources.createAppLabel("flowframe.even_x_shift");
      evenXShiftBox.add(evenXShiftLabel);

      evenXShiftBox.add(resources.createLabelSpacer());

      evenXShiftLength = resources.createLengthPanel();
      evenXShiftLabel.setLabelFor(evenXShiftLength);
      evenXShiftBox.add(evenXShiftLength);

      evenXShiftBox.add(Box.createHorizontalGlue());

      clampCompMaxHeight(evenXShiftBox);

      row = createRow();
      mainPanel.add(row);

      computeSymXShiftButton = resources.createAppJButton(
        "flowframe", "compute_sym_x_shift", this);

      row.add(computeSymXShiftButton);

      clampCompMaxHeight(evenXShiftBox);

      JComponent evenYShiftBox = createRow();
      mainPanel.add(evenYShiftBox);

      evenYShiftLabel = 
         resources.createAppLabel("flowframe.even_y_shift");
      evenYShiftBox.add(evenYShiftLabel);

      evenYShiftBox.add(resources.createLabelSpacer());

      evenYShiftLength = resources.createLengthPanel();
      evenYShiftLabel.setLabelFor(evenYShiftLength);
      evenYShiftBox.add(evenYShiftLength);

      evenYShiftBox.add(Box.createHorizontalGlue());

      JComponent contentsPane = new JPanel(new BorderLayout());
      contentsPane.setAlignmentX(Component.LEFT_ALIGNMENT);

      mainPanel.add(contentsPane);

      contentsLabel = getResources().createAppLabel("flowframe.contents");

      contentsPane.add(contentsLabel, "North");

      contentsViewer = new JTextArea(6, 10);
      contentsViewer.setEditable(false);
      contentsViewer.setOpaque(false);
      contentsViewer.setLineWrap(true);
      contentsViewer.setWrapStyleWord(true);
      contentsViewer.setFont(application_.getTeXEditorFont());

      JScrollPane contentsSp = new JScrollPane(contentsViewer);
      contentsSp.setMinimumSize(contentsViewer.getPreferredSize());

      contentsPane.add(contentsSp, "Center");

      JComponent buttonBox = Box.createVerticalBox();
      contentsPane.add(buttonBox, "East");

      editContentsButton = resources.createAppJButton(
        "flowframe", "edit", this);
      buttonBox.add(editContentsButton);

      clearBox = resources.createAppCheckBox("flowframe", "clear", false, null);
      buttonBox.add(clearBox);

      mainPanel.add(Box.createVerticalGlue());

      JPanel p2 = new JPanel();

      resources.createOkayCancelHelpButtons(this, p2, this, "sec:framedef");

      getContentPane().add(p2, "South");

      pack();
      setLocationRelativeTo(application_);
   }

   protected JComponent createRow()
   {
      JComponent row = Box.createHorizontalBox();
      row.setAlignmentX(Component.LEFT_ALIGNMENT);
   
      return row;
   }

   protected void clampCompMaxHeight(JComponent row)
   {
      getResources().clampCompMaxHeight(row, 0, 20);
   }  

   public void display()
   {
      mainPanel = application_.getCurrentFrame();

      object = mainPanel.getSelectedObject();

      typeblock = mainPanel.getCanvas().getTypeblockBounds();

      computeSymXShiftButton.setEnabled(typeblock != null && object != null);

      if (object != null)
      {
         setValues(object.getFlowFrame());
      }
      else
      {
         contentsViewer.setText("");
      }

      setVisible(true);
   }

   public void okay()
   {
      FlowFrame flowframe=null;

      CanvasGraphics cg = mainPanel.getCanvasGraphics();

      int idx = type.getSelectedIndex();
      if (idx != NONE)
      {
         // check label supplied
         if (labelText.getText().equals(""))
         {
            getResources().error(this,
               getResources().getMessage("error.no_idl"));
            return;
         }

         // check unique label
         if (!mainPanel.isUniqueLabel(idx,object,labelText.getText()))
         {
            getResources().error(this,
               getResources().getMessage("error.idl_exists"));
            return;
         }

         String pages = (String)pageList.getSelectedItem();

         if (pages.equals(
              getResources().getMessage("flowframe.pages_all")))
         {
            pages = "all";
         }
         else if (pages.equals(
            getResources().getMessage("flowframe.pages_odd")))
         {
            pages = "odd";
         }
         else if (pages.equals(
            getResources().getMessage("flowframe.pages_even")))
         {
            pages = "even";
         }
         else if (pages.equals(
            getResources().getMessage("flowframe.pages_none")))
         {
            pages = "none";
         }

         // check page list is valid
         if (!FlowFrame.isValidPageList(pages))
         {
            getResources().error(this,
               getResources().getMessage("error.invalid_frame-page-list"));
            return;
         }
         
         flowframe  = new FlowFrame(cg, idx, 
            border.getSelectedIndex()==1, labelText.getText(),
            pages);

         JDRUnit unit = cg.getStorageUnit();

         if (margins.isEnabled())
         {
            flowframe.setLeft(margins.left(unit));
            flowframe.setRight(margins.right(unit));
            flowframe.setTop(margins.top(unit));
            flowframe.setBottom(margins.bottom(unit));
         }
         else
         {
            flowframe.setLeft(0);
            flowframe.setRight(0);
            flowframe.setTop(0);
            flowframe.setBottom(0);
         }

         flowframe.setEvenXShift(evenXShiftLength.getValue(unit));
         flowframe.setEvenYShift(evenYShiftLength.getValue(unit));

         if ((idx == FlowFrame.STATIC || idx == FlowFrame.DYNAMIC))
         {
            flowframe.setShape(shapeBox.getSelectedIndex());

            flowframe.setVAlign(alignBox.getSelectedIndex());

            flowframe.setContents(contentsViewer.getText());

            flowframe.setClear(clearBox.isSelected());

            if (idx == FlowFrame.DYNAMIC)
            {
               flowframe.setStyleCommands(styleCmdsField.getText());
            }
         }
         else
         {
            flowframe.setShape(FlowFrame.STANDARD);
            flowframe.setVAlign(FlowFrame.CENTER);
            flowframe.setContents(null);
         }
      }

      setVisible(false);
      if (object != null) mainPanel.setFlowFrame(object, flowframe);
      object=null;
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
         object=null;
      }
      else if (action.equals("edit"))
      {
         String contents = application_.displayTeXEditorDialog(
           contentsViewer.getText());

         if (contents != null)
         {
            contentsViewer.setText(contents);
         }
      }
      else if (action.equals("compute_sym_x_shift"))
      {
         double x0 = typeblock.getX();
         double x1 = x0 + typeblock.getWidth();

         BBox bbox = object.getStorageBBox();
         double w = bbox.getWidth();
         double bx = bbox.getMinX();

         double shift = x0 + x1 - 2*bx - w;

         evenXShiftLength.setValue(shift, 
            object.getCanvasGraphics().getStorageUnit());
      }
   }

   public void itemStateChanged(ItemEvent evt)
   {
      Object source = evt.getSource();

      if (evt.getStateChange() == ItemEvent.SELECTED)
      {
         if (source == type)
         {
            int idx = type.getSelectedIndex();

            boolean isStaticOrDynamic = 
              (idx == STATIC || idx == DYNAMIC);

            boolean enableShape = (isStaticOrDynamic
               && (object instanceof JDRPath));

            shapeLabel.setEnabled(enableShape);
            shapeBox.setEnabled(enableShape);

            if (!alignBox.isEnabled() && isStaticOrDynamic)
            {
               alignBox.setSelectedIndex(
                  idx == STATIC ? FlowFrame.CENTER : FlowFrame.TOP);
            }

            alignLabel.setEnabled(isStaticOrDynamic);
            alignBox.setEnabled(isStaticOrDynamic);
            clearBox.setEnabled(isStaticOrDynamic);
            enableContents(isStaticOrDynamic);

            styleCmdsLabel.setEnabled(idx == DYNAMIC);
            styleCmdsField.setEnabled(styleCmdsLabel.isEnabled());

            if (idx == NONE)
            {
               border.setEnabled(false);
               borderLabel.setEnabled(false);
               labelText.setEnabled(false);
               labelLabel.setEnabled(false);
               pageList.setEnabled(false);
               pagesLabel.setEnabled(false);
               margins.setEnabled(false);
               computeSymXShiftButton.setEnabled(false);
               evenXShiftLength.setEnabled(false);
               evenYShiftLength.setEnabled(false);
               evenXShiftLabel.setEnabled(false);
               evenYShiftLabel.setEnabled(false);
            }
            else
            {
               boolean enableBorderAndMargins =
                 ((shapeBox.getSelectedIndex()==FlowFrame.STANDARD)
                   || !enableShape);

               border.setEnabled(enableBorderAndMargins);
               borderLabel.setEnabled(enableBorderAndMargins);
               labelText.setEnabled(true);
               labelLabel.setEnabled(true);
               pageList.setEnabled(true);
               pagesLabel.setEnabled(true);
               margins.setEnabled(enableBorderAndMargins);
               computeSymXShiftButton.setEnabled(typeblock != null
                  && object != null);
               evenXShiftLength.setEnabled(true);
               evenYShiftLength.setEnabled(true);
               evenXShiftLabel.setEnabled(true);
               evenYShiftLabel.setEnabled(true);
            }
         }
         else if (source == shapeBox)
         {
            boolean enabled = (shapeBox.getSelectedIndex()==FlowFrame.STANDARD);
            margins.setEnabled(enabled);
            border.setEnabled(enabled);
         }
      }
   }

   public void setValues(FlowFrame flowframe)
   {
      JDRUnit unit = mainPanel.getCanvasGraphics().getStorageUnit();

      if (flowframe == null)
      {
         FlowFrame typeblock = mainPanel.getTypeblock();

         type.setSelectedIndex(NONE);
         labelText.setText("");
         evenXShiftLength.setValue(
          typeblock==null ? 0.0 : typeblock.getEvenYShift(), unit);
         evenYShiftLength.setValue(0.0, unit);
         margins.setMargins(unit, 0.0, 0.0, 0.0, 0.0);
         contentsViewer.setText("");
         shapeBox.setSelectedIndex(FlowFrame.STANDARD);
         clearBox.setSelected(false);
         styleCmdsField.setText("");
      }
      else
      {
         int idx = flowframe.getType();
         type.setSelectedIndex(idx);
         border.setSelectedIndex(flowframe.border?1:0);

         String pages = flowframe.getPages();

         if (pages.equals("all"))
         {
            pageList.setSelectedItem(
               getResources().getMessage("flowframe.pages_all"));
         }
         else if (pages.equals("odd"))
         {
            pageList.setSelectedItem(
               getResources().getMessage("flowframe.pages_odd"));
         }
         else if (pages.equals("even"))
         {
            pageList.setSelectedItem(
               getResources().getMessage("flowframe.pages_even"));
         }
         else if (pages.equals("none"))
         {
            pageList.setSelectedItem(
               getResources().getMessage("flowframe.pages_none"));
         }
         else
         {
            pageList.setSelectedItem(pages);
         }

         labelText.setText(flowframe.getLabel());

         margins.setMargins(unit,
                            flowframe.getLeft(), flowframe.getRight(),
                            flowframe.getTop(), flowframe.getBottom());

         evenXShiftLength.setValue(flowframe.getEvenXShift(), unit);
         evenYShiftLength.setValue(flowframe.getEvenYShift(), unit);

         shapeBox.setSelectedIndex(flowframe.getShape());
         alignBox.setSelectedIndex(flowframe.getVAlign());
         clearBox.setSelected(flowframe.isClearOn());

         if (idx == DYNAMIC)
         {
            styleCmdsLabel.setEnabled(true);
            styleCmdsField.setEnabled(true);
            String cmds = flowframe.getStyleCommands();
            styleCmdsField.setText(cmds == null ? "" : cmds);
         }
         else
         {
            styleCmdsField.setEnabled(false);
            styleCmdsLabel.setEnabled(false);
            styleCmdsField.setText("");
         }

         boolean isStaticOrDynamic = 
           (idx == STATIC || idx == DYNAMIC);

         boolean enableShape = (isStaticOrDynamic
            && (object instanceof JDRPath));

         shapeLabel.setEnabled(enableShape);
         shapeBox.setEnabled(enableShape);

         alignLabel.setEnabled(isStaticOrDynamic);
         alignBox.setEnabled(isStaticOrDynamic);
         clearBox.setEnabled(isStaticOrDynamic);
         enableContents(isStaticOrDynamic);

         String contents = flowframe.getContents();

         if (!isStaticOrDynamic) contents = "";

         contentsViewer.setText(contents == null ? "" : contents);
      }
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "FLFSelector:"+eol;
      str += "has focus: "+hasFocus()+eol;
      str += "type box has focus: "+type.hasFocus()+eol;
      str += "border box has focus: "+border.hasFocus()+eol;
      str += "page list box has focus: "+pageList.hasFocus()+eol;
      str += "label field has focus: "+labelText.hasFocus()+eol;

      return str+eol;
   }

   private void enableContents(boolean enabled)
   {
      editContentsButton.setEnabled(enabled);
      contentsViewer.setEnabled(enabled);
      contentsLabel.setEnabled(enabled);
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;
   private JDRCompleteObject object=null;
   private Rectangle2D typeblock = null;

   private JComboBox<String> type, border, pageList;
   private JTextField labelText;
   private JLabel labelLabel, borderLabel, pagesLabel; 
   private MarginPanel margins;
   public static final int STATIC=0, FLOW=1, DYNAMIC=2, NONE=3;

   private JComboBox shapeBox;
   private JLabel shapeLabel;

   private JComboBox alignBox;
   private JLabel alignLabel;

   private LengthPanel evenXShiftLength, evenYShiftLength;
   private JLabel evenXShiftLabel, evenYShiftLabel;

   private JButton computeSymXShiftButton;

   private JButton editContentsButton;
   private JTextArea contentsViewer;
   private JLabel contentsLabel;

   private JCheckBox clearBox;
   private JLabel styleCmdsLabel;
   private JTextField styleCmdsField;

   private JDRFrame mainPanel;
}
