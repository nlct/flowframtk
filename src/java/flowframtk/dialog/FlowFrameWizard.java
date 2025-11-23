/*
    Copyright (C) 2025 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
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

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;
import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.InvalidFormatException;
import com.dickimawbooks.jdr.exceptions.InvalidValueException;
import com.dickimawbooks.jdr.io.ExportSettings;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.LengthPanel;
import com.dickimawbooks.flowframtk.*;

public class FlowFrameWizard extends JDialog
 implements ActionListener,
   ItemListener,
   ChangeListener,
   DocumentListener,
   ListSelectionListener
{
   public FlowFrameWizard(FlowframTk application)
   {
      super(application, application.getResources().getMessage("flfwizard.title"), true);
      this.application = application;
      mainTitle = getTitle();

      JDRResources resources = getResources();

      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      addWindowListener(new WindowAdapter()
       {
          public void windowClosing(WindowEvent evt)
          {
             cancel();
          }
       });

      cardLayout = new CardLayout();
      mainPanel = new JPanel(cardLayout);

      cardComponents = new JComponent[MAX_CARDS];

      cardComponents[CARD_CLS] = createClsComp();
      mainPanel.add(new JScrollPane(cardComponents[CARD_CLS]), "cls");

      typeblockPanel = new TypeblockPanel(application);
      typeblockPanel.setName(resources.getMessage("flfwizard.typeblock.title"));
      typeblockPanel.addChangeListener(this);

      cardComponents[CARD_TYPEBLOCK] = typeblockPanel;
      mainPanel.add(new JScrollPane(cardComponents[CARD_TYPEBLOCK]), "typeblock");

      cardComponents[CARD_FRAME_TYPE] = createFrameTypeComp();
      mainPanel.add(new JScrollPane(cardComponents[CARD_FRAME_TYPE]), "frametype");

      cardComponents[CARD_LABEL_PAGES] = createLabelPagesComp();
      mainPanel.add(new JScrollPane(cardComponents[CARD_LABEL_PAGES]), "labelpages");

      cardComponents[CARD_FRAME_STYLE] = createFrameStyleComp();
      mainPanel.add(new JScrollPane(cardComponents[CARD_FRAME_STYLE]), "framestyle");

      cardComponents[CARD_FRAME_MARGINS] = createFrameMarginsComp();
      mainPanel.add(new JScrollPane(cardComponents[CARD_FRAME_MARGINS]), "framemargins");

      cardComponents[CARD_FINISH] = createFinishComp();
      mainPanel.add(cardComponents[CARD_FINISH], "finish");

      getContentPane().add(mainPanel, "Center");

      JComponent buttonPanel = new JPanel(new BorderLayout());

      JPanel leftButtonPanel = new JPanel();
      buttonPanel.add(leftButtonPanel, "West");

      prevButton = resources.createJButton("flfwizard", "previous", this);

      if (prevButton.getToolTipText() == null)
      {
         prevButton.setToolTipText(prevButton.getText());
      }

      prevButton.setHorizontalTextPosition(SwingConstants.TRAILING);
      leftButtonPanel.add(prevButton);

      JPanel rightButtonPanel = new JPanel();
      buttonPanel.add(rightButtonPanel, "East");

      nextButton = resources.createJButton("flfwizard", "next", this);

      if (nextButton.getToolTipText() == null)
      {
         nextButton.setToolTipText(nextButton.getText());
      }

      nextButton.setHorizontalTextPosition(SwingConstants.LEADING);
      rightButtonPanel.add(nextButton);

      getRootPane().setDefaultButton(nextButton);

      Dimension dim = leftButtonPanel.getPreferredSize();
      int maxWidth = dim.width;
      int maxHeight = dim.height;

      for (int i = 0; i < MAX_CARDS; i++)
      {
         prevButton.setText(cardComponents[i].getName());

         dim = leftButtonPanel.getPreferredSize();

         if (dim.width > maxWidth)
         {
            maxWidth = dim.width;
         }

         if (dim.height > maxHeight)
         {
            maxHeight = dim.height;
         }
      }

      dim.width = maxWidth;
      dim.height = maxHeight;

      leftButtonPanel.setMinimumSize(dim);
      leftButtonPanel.setPreferredSize(dim);
      rightButtonPanel.setMinimumSize(dim);
      rightButtonPanel.setPreferredSize(dim);

      JPanel p = new JPanel();
      buttonPanel.add(p, "Center");

      p.add(resources.createCancelButton(this));
      p.add(resources.createButtonSpacer());

      try
      {
         p.add(resources.createHelpDialogButton(this, "sec:flowframe"));
      }
      catch (HelpSetNotInitialisedException e)
      {
         resources.internalError(null, e);
      }

      getContentPane().add(buttonPanel, "South");

      pack();
      setLocationRelativeTo(application);
   }

   protected JComponent createRow()
   {
      JComponent row = Box.createHorizontalBox();
      row.setAlignmentX(Component.LEFT_ALIGNMENT);

      return row;
   }

   protected JComponent createClsComp()
   {
      JDRResources resources = getResources();
      JComponent row;

      JComponent clsComp = new JPanel(new BorderLayout());
      clsComp.setName(resources.getMessage("flfwizard.cls.title"));

      JTextArea info = resources.createAppInfoArea("flfwizard.cls.info", INFO_MAX_COLS);
      info.setOpaque(true);
      info.setAlignmentX(Component.LEFT_ALIGNMENT);

      clsComp.add(info, "North");

      JComponent comp = Box.createVerticalBox();
      clsComp.add(comp, "Center");

      row = createRow();
      comp.add(row);

      ButtonGroup bg = new ButtonGroup();

      useDefaultCls = getResources().createAppRadioButton("flfwizard.cls",
         "default_cls", bg, true, null);
      useDefaultCls.setAlignmentX(Component.LEFT_ALIGNMENT);
      useDefaultCls.addItemListener(this);
      row.add(useDefaultCls);
   
      row.add(Box.createHorizontalStrut(20));

      useCustomCls = getResources().createAppRadioButton("flfwizard.cls",
         "custom_cls", bg, false, null);
      useCustomCls.setAlignmentX(Component.LEFT_ALIGNMENT);
      useCustomCls.addItemListener(this);
      row.add(useCustomCls);

      customClsField = new JTextField(10);
      customClsField.setAlignmentX(Component.LEFT_ALIGNMENT);
      customClsField.getDocument().addDocumentListener(this);
      row.add(customClsField);

      customClsField.setMaximumSize(customClsField.getPreferredSize());

      clampCompMax(row);

      comp.add(Box.createVerticalStrut(20));

      row = createRow();
      comp.add(row);

      JLabel label = getResources().createAppLabel("flfwizard.cls.normalsize");
      label.setAlignmentX(Component.LEFT_ALIGNMENT);

      row.add(label);

      row.add(resources.createLabelSpacer());

      fontSizeBox = new JComboBox<Integer>(availableSizes);
      fontSizeBox.addItemListener(this);
      label.setLabelFor(fontSizeBox);
      row.add(fontSizeBox);
      fontSizeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      fontSizeBox.setMaximumSize(fontSizeBox.getPreferredSize());

      row.add(resources.createButtonSpacer());

      useRelativeFontDeclarations = getResources().createAppCheckBox(
         "flfwizard.cls", "relative_fontsize", true, null);
      useRelativeFontDeclarations.setAlignmentX(Component.LEFT_ALIGNMENT);
      useRelativeFontDeclarations.addItemListener(this);
      row.add(useRelativeFontDeclarations);

      clampCompMax(row);

      row = createRow();
      comp.add(row);

      updateImageBox = getResources().createAppCheckBox("flfwizard.cls",
        "update", application.isLaTeXFontUpdateEnabled(), null);

      updateImageBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      row.add(updateImageBox);

      clampCompMax(row);

      comp.add(Box.createVerticalStrut(20));

      row = createRow();
      comp.add(row);

      row.add(resources.createAppLabel("flfwizard.labelpages.pages"));
      row.add(resources.createLabelSpacer());

      bg = new ButtonGroup();

      relativePagesBox = createRadioButton(
        "flfwizard.labelpages", "relative", bg, true);
      row.add(relativePagesBox);

      absolutePagesBox = createRadioButton(
        "flfwizard.labelpages", "absolute", bg, false);
      row.add(absolutePagesBox);

      clampCompMax(row);

      comp.add(Box.createVerticalStrut(20));

      row = createRow();
      comp.add(row);

      JLabelGroup labelGrp = new JLabelGroup();

      label = resources.createAppLabel("flfwizard.current_grid_unit");

      row.add(label);
      labelGrp.add(label);
      row.add(resources.createLabelSpacer());

      currentGridUnitField = resources.createAppInfoField(3);
      row.add(currentGridUnitField);

      clampCompMaxHeight(row);

      row = createRow();
      comp.add(row);

      label = resources.createAppLabel("flfwizard.current_storage_unit");

      row.add(label);
      labelGrp.add(label);
      row.add(resources.createLabelSpacer());

      currentStorageUnitField = resources.createAppInfoField(3);
      row.add(currentStorageUnitField);

      clampCompMaxHeight(row);

      row = createRow();
      comp.add(row);

      label = resources.createAppLabel("flfwizard.current_paper");

      row.add(label);
      labelGrp.add(label);
      row.add(resources.createLabelSpacer());

      currentPaperField = resources.createAppInfoField(INFO_MAX_COLS/2);
      row.add(currentPaperField);

      clampCompMaxHeight(row);

      comp.add(Box.createVerticalGlue());

      return clsComp;
   }

   public JComponent createFrameTypeComp()
   {
      JDRResources resources = getResources();
      JComponent comp = Box.createVerticalBox();

      comp.setName(resources.getMessage("flfwizard.frametype.title"));

      emptyImageLabel = new JLabel(resources.getMessage("flfwizard.frametype.empty_image"));
      emptyImageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      comp.add(emptyImageLabel);

      JLabel label = resources.createAppLabel("flfwizard.frametype.object");
      comp.add(label);

      objectList = new JDRCompleteObjectJList();
      comp.setAlignmentX(Component.LEFT_ALIGNMENT);
      objectList.setPrototype(resources.getMessage("flfwizard.frametype.object.placeholder"));
      objectList.addListSelectionListener(this);
      label.setLabelFor(objectList);

      objectList.addMouseListener(new MouseAdapter()
       {
          @Override
          public void mouseClicked(MouseEvent evt)
          {
             if (evt.getClickCount() == 2)
             {
                int index = objectList.locationToIndex(evt.getPoint());

                if (index > -1)
                {
                   JDRCompleteObject object = objectList.getObject(index);
                   frame.getCanvas().selectObjectAndDeselectRest(object, true);
                }
             }
          }
       });

      JScrollPane sp = new JScrollPane(objectList);
      sp.setAlignmentX(Component.LEFT_ALIGNMENT);

      comp.add(sp);

      selectedObjectSetText = resources.getMessage("flfwizard.frametype.selected_object_set");
      selectedObjectNotSetText = resources.getMessage("flfwizard.frametype.selected_object_notset");

      selectedObjectLabel = new JLabel(
         selectedObjectSetText.length() > selectedObjectNotSetText.length()
          ? selectedObjectSetText : selectedObjectNotSetText);

      selectedObjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      comp.add(selectedObjectLabel);

      ButtonGroup bg = new ButtonGroup();

      flowBox = createRadioButton("flfwizard.frametype", "flow", bg, true);
      comp.add(flowBox);

      staticBox = createRadioButton("flfwizard.frametype", "static", bg, false);
      comp.add(staticBox);

      dynamicBox = createRadioButton("flfwizard.frametype", "dynamic", bg, false);
      comp.add(dynamicBox);

      headerBox = createRadioButton("flfwizard.frametype", "header", bg, false);
      comp.add(headerBox);

      evenHeaderBox = createRadioButton("flfwizard.frametype", "evenheader", bg, false);
      comp.add(evenHeaderBox);

      footerBox = createRadioButton("flfwizard.frametype", "footer", bg, false);
      comp.add(footerBox);

      evenFooterBox = createRadioButton("flfwizard.frametype", "evenfooter", bg, false);
      comp.add(evenFooterBox);

      thumbtabBox = createRadioButton("flfwizard.frametype", "thumbtab", bg, false);
      comp.add(thumbtabBox);

      thumbtabIndexBox = createRadioButton("flfwizard.frametype", "thumbtabindex", bg, false);
      comp.add(thumbtabIndexBox);

      evenThumbtabBox = createRadioButton("flfwizard.frametype", "eventhumbtab", bg, false);
      comp.add(evenThumbtabBox);

      evenThumbtabIndexBox = createRadioButton("flfwizard.frametype", "eventhumbtabindex", bg, false);
      comp.add(evenThumbtabIndexBox);

      return comp;
   }

   public JComponent createLabelPagesComp()
   {
      JDRResources resources = getResources();
      JComponent comp = Box.createVerticalBox();
      JComponent row;

      comp.setName(resources.getMessage("flfwizard.labelpages.title"));

      JLabel infoLabel = resources.createAppLabel("flfwizard.labelpages.frame_label.info");
      infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      comp.add(infoLabel);

      row = createRow();
      comp.add(row);

      JLabel label = resources.createAppLabel("flfwizard.labelpages.frame_type");
      row.add(label);
      row.add(resources.createLabelSpacer());

      flowText = resources.getMessage("flowframe.flow");
      int n = flowText.length();
      staticText = resources.getMessage("flowframe.static");
      n = Math.max(n, staticText.length());
      dynamicText = resources.getMessage("flowframe.dynamic");
      n = Math.max(n, dynamicText.length());

      typeField = new JTextField(n+1);
      typeField.setEditable(false);
      row.add(typeField);

      row.add(resources.createButtonSpacer());

      label = resources.createAppLabel("flfwizard.labelpages.frame_label");
      row.add(label);
      row.add(resources.createLabelSpacer());

      labelField = new JTextField(20);
      labelField.getDocument().addDocumentListener(this);
      label.setLabelFor(labelField);

      row.add(labelField);

      clampCompMax(row);

      row = createRow();
      comp.add(row);

      specialInfoLabelField = resources.createAppInfoField(
       "flfwizard.labelpages.frame_label.special_info", "eventhumbtabindex000");
      specialInfoLabelField.setOpaque(true);
      row.add(specialInfoLabelField);

      clampCompMax(row);

      comp.add(Box.createVerticalStrut(20));

      row = createRow();
      comp.add(row);

      label = resources.createAppLabel("flfwizard.labelpages.pagelist");
      row.add(label);
      row.add(resources.createLabelSpacer());

      pagesAllText = resources.getMessage("flowframe.pages_all");
      pagesOddText = resources.getMessage("flowframe.pages_odd");
      pagesEvenText = resources.getMessage("flowframe.pages_even");
      pagesNoneText = resources.getMessage("flowframe.pages_none");

      pageList = new JComboBox<String>(new String[]
       {
          pagesAllText,
          pagesOddText,
          pagesEvenText,
          pagesNoneText
       });
      pageList.setEditable(true);
      pageList.addItemListener(this);
      label.setLabelFor(pageList);
      row.add(pageList);

      clampCompMax(row);

      row = createRow();

      comp.add(row);

      JLabel currentPagesLabel = resources.createAppLabel("flfwizard.labelpages.pages");
      row.add(currentPagesLabel);

      row.add(resources.createLabelSpacer());

      currentPageSettingField = resources.createAppInfoField("flfwizard.labelpages.relative");

      String absStr = resources.getMessage("flfwizard.labelpages.absolute");

      if (absStr.length() > currentPageSettingField.getText().length())
      {
         currentPageSettingField.setText(absStr);
      }

      row.add(currentPageSettingField);

      clampCompMaxHeight(row);

      row = createRow();
      comp.add(row);

      JTextArea pagesInfoField = resources.createAppInfoArea(
       "flfwizard.labelpages.pages.info", INFO_MAX_COLS);
      pagesInfoField.setOpaque(true);

      row.add(pagesInfoField);

      return comp;
   }

   protected JComponent createFrameStyleComp()
   {
      JDRResources resources = getResources();
      JComponent comp = Box.createVerticalBox();
      JComponent row;

      comp.setName(resources.getMessage("flfwizard.style.title"));

      row = createRow();
      comp.add(row);

      JTextArea borderInfoField = resources.createAppInfoArea(INFO_MAX_COLS,
       "flfwizard.style.border.info");
      borderInfoField.setOpaque(true);

      row.add(borderInfoField);
      clampCompMax(row);

      row = createRow();
      comp.add(row);

      useObjectAsBorder = resources.createAppCheckBox(
        "flfwizard.style", "border", false, null);
      useObjectAsBorder.addItemListener(this);

      row.add(useObjectAsBorder);

      clampCompMax(row);

      comp.add(Box.createVerticalStrut(10));

      styleCmdsComp = Box.createVerticalBox();
      comp.add(styleCmdsComp);

      row = createRow();
      styleCmdsComp.add(row);

      JLabel label = resources.createAppLabel("flfwizard.style.cmds");
      row.add(label);
      row.add(resources.createLabelSpacer());

      dynamicStyleField = new JTextField("\\relax ", 30);
      label.setLabelFor(dynamicStyleField);
      dynamicStyleField.getDocument().addDocumentListener(this);
      row.add(dynamicStyleField);

      row = createRow();
      styleCmdsComp.add(row);

      JTextArea info = resources.createAppInfoArea(INFO_MAX_COLS,
       "flfwizard.style.cmds.info");
      row.add(info);
      info.setOpaque(true);

      clampCompMax(styleCmdsComp);

      comp.add(Box.createVerticalStrut(10));
      row = createRow();
      comp.add(row);

      clearBox = resources.createAppCheckBox(
        "flfwizard.style", "clear", false, null);
      clearBox.addItemListener(this);

      row.add(clearBox);

      comp.add(Box.createVerticalStrut(10));

      shapeAlignComp = createRow();
      comp.add(shapeAlignComp);

      label = resources.createAppLabel("flfwizard.style.valign");
      shapeAlignComp.add(label);
      shapeAlignComp.add(resources.createLabelSpacer());

      valignBox = new JComboBox<String>(
        new String[]
         {
           resources.getMessage("flowframe.align_top"), // FlowFrame.TOP
           resources.getMessage("flowframe.align_middle"), // FlowFrame.CENTER
           resources.getMessage("flowframe.align_bottom") // FlowFrame.BOTTOM
         }
       );
      label.setLabelFor(valignBox);
      valignBox.addItemListener(this);
      shapeAlignComp.add(valignBox);

      shapeAlignComp.add(resources.createButtonSpacer());

      label = resources.createAppLabel("flfwizard.style.shape");
      shapeAlignComp.add(label);
      shapeAlignComp.add(resources.createLabelSpacer());

      shapeBox = new JComboBox<String>(
       new String[]
        {
           resources.getMessage("flowframe.shape_standard"), // FlowFrame.STANDARD
           resources.getMessage("flowframe.shape_parshape"), // FlowFrame.PARSHAPE
           resources.getMessage("flowframe.shape_shapepar") // FlowFrame.SHAPEPAR
        });
      label.setLabelFor(shapeBox);
      shapeBox.addItemListener(this);
      shapeAlignComp.add(shapeBox);

      clampCompMax(shapeAlignComp);

      marginParComp = createRow();
      comp.add(marginParComp);

      label = resources.createAppLabel("flfwizard.style.marginpos");
      marginParComp.add(label);
      marginParComp.add(resources.createLabelSpacer());

      marginParBox = new JComboBox<String>(
       new String[]
        {
           resources.getMessage("flowframe.marginpar.outer"), // FlowFrame.MARGIN_OUTER
           resources.getMessage("flowframe.marginpar.inner"), // FlowFrame.MARGIN_INNER
           resources.getMessage("flowframe.marginpar.left"), // FlowFrame.MARGIN_LEFT
           resources.getMessage("flowframe.marginpar.right") // FlowFrame.MARGIN_RIGHT
        });
      label.setLabelFor(marginParBox);
      marginParComp.add(marginParBox);

      clampCompMax(marginParComp);

      comp.add(Box.createVerticalStrut(10));
      row = createRow();
      comp.add(row);

      contentInfoStaticText = resources.getMessage("flfwizard.style.content_info.static");
      int n = contentInfoStaticText.length();
      String text = contentInfoStaticText;

      contentInfoDynamicText = resources.getMessage("flfwizard.style.content_info.dynamic");

      if (contentInfoDynamicText.length() > n)
      {
         n = contentInfoDynamicText.length();
         text = contentInfoDynamicText;
      }

      contentInfoFlowText = resources.getMessage("flfwizard.style.content_info.flow");

      if (contentInfoFlowText.length() > n)
      {
         text = contentInfoFlowText;
      }

      contentInfoArea = resources.createAppInfoArea(INFO_MAX_COLS);
      contentInfoArea.setText(text);
      contentInfoArea.setOpaque(true);
      row.add(contentInfoArea);

      clampCompMax(row);

      comp.add(Box.createVerticalStrut(10));
      row = createRow();
      comp.add(row);

      editContentButton = resources.createJButton("flfwizard.content", "edit", this);

      row.add(editContentButton);

      contentsViewer = new JTextArea();
      contentsViewer.setEditable(false);

      row = createRow();
      comp.add(row);

      contentsViewerSp = new JScrollPane(contentsViewer);

      row.add(contentsViewerSp);

      comp.add(Box.createVerticalGlue());

      return comp;
   }

   protected JComponent createFrameMarginsComp()
   {
      JDRResources resources = getResources();
      JComponent comp = Box.createVerticalBox();
      JComponent row;

      comp.setName(resources.getMessage("flfwizard.dimensions.title"));

      row = createRow();
      comp.add(row);

      JTextArea info = resources.createAppInfoArea(INFO_MAX_COLS, 
        "flfwizard.dimensions.margins.info");
      info.setAlignmentX(Component.LEFT_ALIGNMENT);
      row.add(info);

      clampCompMax(row);

      margins = new MarginPanel(application, 
       resources.getMessage("flowframe.frame_margins"), true);
      margins.setAlignmentX(Component.LEFT_ALIGNMENT);
      comp.add(margins);
      clampCompMax(margins);

      comp.add(Box.createVerticalStrut(10));

      row = createRow();
      comp.add(row);

      info = resources.createAppInfoArea(INFO_MAX_COLS, "flowframe.twoside_note");
      info.setAlignmentX(Component.LEFT_ALIGNMENT);
      row.add(info);

      clampCompMax(row);

      row = createRow();
      comp.add(row);

      evenXShiftLength = resources.createLengthPanel("flfwizard.dimensions.even_x_shift");
      row.add(evenXShiftLength);

      row.add(resources.createButtonSpacer());

      JButton btn = resources.createAppJButton("flowframe", "compute_sym_x_shift", this);
      row.add(btn);

      clampCompMax(row);

      comp.add(Box.createVerticalStrut(10));

      evenYShiftLength = resources.createLengthPanel("flfwizard.dimensions.even_y_shift");
      evenYShiftLength.setAlignmentX(Component.LEFT_ALIGNMENT);
      comp.add(evenYShiftLength);
      clampCompMax(evenYShiftLength);

      comp.add(Box.createVerticalGlue());

      return comp;
   }

   protected JComponent createFinishComp()
   {
      JDRResources resources = getResources();
      JComponent comp = Box.createVerticalBox();

      comp.setName(resources.getMessage("flfwizard.finish.title"));

      comp.add(Box.createVerticalGlue());

      return comp;
   }

   protected JRadioButton createRadioButton(String parentId, String action, ButtonGroup bg,
      boolean selected)
   {
      JRadioButton btn = getResources().createAppRadioButton(parentId, action, bg, selected,
        null);
      btn.setAlignmentX(Component.LEFT_ALIGNMENT);
      btn.addItemListener(this);

      return btn;
   }

   protected void clampCompMax(JComponent row)
   {
      Dimension dim = row.getPreferredSize();
      dim.height += 20;
      row.setMaximumSize(dim);
   }

   protected void clampCompMaxHeight(JComponent row)
   {
      Dimension dim = row.getPreferredSize();
      int h = dim.height + 20;
      dim = row.getMaximumSize();
      dim.height = h;
      row.setMaximumSize(dim);
   }

   protected void initialiseCls()
   {
      CanvasGraphics cg = frame.getCanvasGraphics();

      ExportSettings exportSettings = frame.getExportSettings();

      useRelativeFontDeclarations.setSelected(
         application.useRelativeFontDeclarations());

      int normalSize = (int)frame.getNormalSize(); 

      for (int i = 0; i < availableSizes.length; i++)
      {
         if (availableSizes[i].intValue() == normalSize)
         {
            fontSizeBox.setSelectedIndex(i);
            break;
         }
      }

      String cls = cg.getDocClass();

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

      if (cg.useAbsolutePages())
      {
         absolutePagesBox.setSelected(true);
      }
      else
      {
         relativePagesBox.setSelected(true);
      }

      currentGridUnitField.setText(cg.getGrid().getMainUnit().getLabel());

      JDRUnit unit = cg.getStorageUnit();
      currentStorageUnitField.setText(unit.getLabel());

      JDRPaper paper = cg.getPaper();
      currentPaperField.setText(paper.getName(cg.getMessageDictionary(),
        unit, "flfwizard.current_paper.portrait",
        "flfwizard.current_paper.landscape",
        "flfwizard.current_paper.user"));
   }

   protected void updateSpecialBooleans(String label)
   {
      if (label.equals("header"))
      {
         hasHeader = true;
      }
      else if (label.equals("evenheader"))
      {
         hasEvenHeader = true;
      }
      else if (label.equals("footer"))
      {
         hasFooter = true;
      }
      else if (label.equals("evenfooter"))
      {
         hasEvenFooter = true;
      }
      else if (label.startsWith("thumbtab"))
      {
         String suffix = label.substring(8);
         boolean index = false;

         if (suffix.startsWith("index"))
         {
            suffix = suffix.substring(5);
            index = true;
         }

         try
         {
            int n = Integer.parseInt(suffix);

            if (index)
            {
               if (maxThumbtabIndex < n)
               {
                  maxThumbtabIndex = n;
               }
            }
            else
            {
               if (maxThumbtab < n)
               {
                  maxThumbtab = n;
               }
            }
         }
         catch (NumberFormatException e)
         {
         }
      }
      else if (label.startsWith("eventhumbtab"))
      {
         String suffix = label.substring(8);
         boolean index = false;

         if (suffix.startsWith("index"))
         {
            suffix = suffix.substring(5);
            index = true;
         }

         try
         {
            int n = Integer.parseInt(suffix);

            if (index)
            {
               if (maxEvenThumbtabIndex < n)
               {
                  maxEvenThumbtabIndex = n;
               }
            }
            else
            {
               if (maxEvenThumbtab < n)
               {
                  maxEvenThumbtab = n;
               }
            }
         }
         catch (NumberFormatException e)
         {
         }
      }
   }

   protected void initialiseFlowFrame()
   {
      initialiseFlowFrame(true);
   }

   protected void initialiseFlowFrame(boolean repopulate)
   {
      if (repopulate)
      {
         JDRGroup objects = frame.getAllObjects();

         objectList.removeAllElements();

         selectedObject = null;
         selectedObjectIndex = -1;

         hasHeader = false;
         hasEvenHeader = false;
         hasFooter = false;
         hasEvenFooter = false;
         maxThumbtab = 0;
         maxEvenThumbtab = 0;
         maxThumbtabIndex = 0;
         maxEvenThumbtabIndex = 0;

         for (int i = 0; i < objects.size(); i++)
         {
            JDRCompleteObject object = objects.get(i);

            objectList.addObject(object, getResources());

            if (selectedObject == null && object.isSelected())
            {
               objectList.setSelectedIndex(i);
            }

            FlowFrame flowframe = object.getFlowFrame();

            if (flowframe != null && flowframe.getType() == FlowFrame.DYNAMIC)
            {
               updateSpecialBooleans(flowframe.getLabel());
            }
         }

         emptyImageLabel.setVisible(objects.isEmpty());
      }

      dynamicStyleField.setText("");

      if (selectedObject != null)
      {
         FlowFrame flowframe = selectedObject.getFlowFrame();

         if (flowframe == null)
         {
            pageList.setSelectedItem(pagesAllText);
            labelField.setText("");
         }
         else
         {
            labelField.setText(flowframe.getLabel());
            String pages = flowframe.getPages();

            if (pages.equals("all"))
            {
               pageList.setSelectedItem(pagesAllText);
            }
            else if (pages.equals("even"))
            {
               pageList.setSelectedItem(pagesEvenText);
            }
            else if (pages.equals("odd"))
            {
               pageList.setSelectedItem(pagesOddText);
            }
            else if (pages.equals("none"))
            {
               pageList.setSelectedItem(pagesNoneText);
            }
            else
            {
               pageList.setSelectedItem(pages);
            }

            useObjectAsBorder.setSelected(flowframe.isBorderOn());
            clearBox.setSelected(flowframe.isClearOn());

            if (flowframe.hasStyleCommands())
            {
               dynamicStyleField.setText(flowframe.getStyleCommands());
            }
            else
            {
               dynamicStyleField.setText("");
            }

            valignBox.setSelectedIndex(flowframe.getVAlign());
            shapeBox.setSelectedIndex(flowframe.getShape());
            marginParBox.setSelectedIndex(flowframe.getMarginPosition());

            String contents = flowframe.getContents();

            contentsViewer.setText(contents == null ? "" : contents);
         }
      }

      CanvasGraphics cg = frame.getCanvasGraphics();

      if (cg.useAbsolutePages())
      {
         currentPageSettingField.setText(absolutePagesBox.getText());
      }
      else
      {
         currentPageSettingField.setText(absolutePagesBox.getText());
      }

      updateFrameTypeComp();
   }

   protected void updateFrameTypeComp()
   {
      currentFrameType = -1;
      typeField.setText("");

      if (selectedObject == null)
      {
         selectedObjectLabel.setText(selectedObjectNotSetText);
         selectedObjectLabel.setEnabled(false);
         headerBox.setEnabled(false);
         evenHeaderBox.setEnabled(false);
         footerBox.setEnabled(false);
         evenFooterBox.setEnabled(false);
         thumbtabBox.setEnabled(false);
         evenThumbtabBox.setEnabled(false);
         thumbtabIndexBox.setEnabled(false);
         evenThumbtabIndexBox.setEnabled(false);
         staticBox.setEnabled(false);
         dynamicBox.setEnabled(false);
         flowBox.setEnabled(false);
         pageList.setSelectedItem(pagesAllText);
      }
      else
      {
         selectedObjectLabel.setEnabled(true);

         FlowFrame flowframe = selectedObject.getFlowFrame();
         boolean enableSpecial = (flowframe == null);

         if (enableSpecial)
         {
            selectedObjectLabel.setText(selectedObjectNotSetText);
         }
         else
         {
            currentFrameType = flowframe.getType();
            selectedObjectLabel.setText(selectedObjectSetText);
         }

         headerBox.setEnabled(!hasHeader && enableSpecial);
         evenHeaderBox.setEnabled(!hasEvenHeader && enableSpecial);
         footerBox.setEnabled(!hasFooter && enableSpecial);
         evenFooterBox.setEnabled(!hasEvenFooter && enableSpecial);

         if (!enableSpecial
           || (!headerBox.isEnabled() && headerBox.isSelected())
           || (!evenHeaderBox.isEnabled() && evenHeaderBox.isSelected())
           || (!footerBox.isEnabled() && footerBox.isSelected())
           || (!evenFooterBox.isEnabled() && evenFooterBox.isSelected())
            )
         {
            switch (currentFrameType)
            {
               case FlowFrame.STATIC:
                 staticBox.setSelected(true);
               break;
               case FlowFrame.DYNAMIC:
                 dynamicBox.setSelected(true);
               break;
               default:
                 flowBox.setSelected(true);
            }
         }

         thumbtabBox.setEnabled(enableSpecial);
         thumbtabIndexBox.setEnabled(enableSpecial);
         evenThumbtabBox.setEnabled(enableSpecial);
         evenThumbtabIndexBox.setEnabled(enableSpecial);

         staticBox.setEnabled(true);
         dynamicBox.setEnabled(true);
         flowBox.setEnabled(true);

      }
   }

   public void display(JDRFrame frame)
   {
      this.frame = frame;
      initialiseCls();

      currentTypeblock = frame.getTypeblock();
      typeblockPanel.updateComponent(currentTypeblock, frame.getUnit());

      initialiseFlowFrame();

      if (currentTypeblock == null)
      {
         currentCard = 0;
         cardLayout.first(mainPanel);
      }
      else
      {
         currentCard = CARD_FRAME_TYPE;
         cardLayout.show(mainPanel, "frametype");
      }

      CanvasGraphics cg = frame.getCanvasGraphics();

      margins.setUnit(cg.getStorageUnit());

      updateCardButtons();

      clsModified = false;
      typeblockModified = false;
      setVisible(true);
   }

   protected void applyCls()
   {
      if (clsModified)
      {
         application.setRelativeFontDeclarations(
            useRelativeFontDeclarations.isSelected());

         double normalsize = ((Integer)fontSizeBox.getSelectedItem()).intValue();

         if (normalsize != frame.getNormalSize())
         {
            frame.getCanvas().setNormalSize(normalsize);

            if (updateImageBox.isSelected())
            {
               frame.updateLaTeXFontSize();
            }
         }

         application.setLaTeXFontUpdate(updateImageBox.isSelected());

         frame.getCanvas().setDocClass(
           useDefaultCls.isSelected() ? null : customClsField.getText());

         CanvasGraphics cg = frame.getCanvasGraphics();

         if (cg.useAbsolutePages())
         {
            if (relativePagesBox.isSelected())
            {
               frame.getCanvas().setUseAbsolutePages(false);
            }
         }
         else
         {
            if (absolutePagesBox.isSelected())
            {
               frame.getCanvas().setUseAbsolutePages(true);
            }
         }

         clsModified = false;
      }

   }

   protected void applyTypeblock()
   {
      if (typeblockModified)
      {
         typeblockPanel.apply(frame);
         typeblockModified = false;
         currentTypeblock = frame.getTypeblock();
      }
   }

   protected void applyFlowFrame() throws InvalidFormatException
   {
      CanvasGraphics cg = frame.getCanvasGraphics();
      JDRResources resources = getResources();

      String label = labelField.getText().trim();

      // check label supplied
      if (label.isEmpty())
      {
         throw new InvalidValueException("frame-idl", label, cg);
      }

      // check unique label
      if (!frame.isUniqueLabel(currentFrameType, selectedObject, label))
      {
         throw new InvalidFormatException(resources.getMessage("error.idl_exists"));
      }

      String pages = getPageList();

      // check page list is valid
      if (!FlowFrame.isValidPageList(pages))
      {
         throw new InvalidValueException("frame-page-list", pages, cg);
      }

      boolean hasBorder = useObjectAsBorder.isSelected();

      FlowFrame flowframe = new FlowFrame(cg, currentFrameType,
        hasBorder, label, pages);

      if (currentFrameType == FlowFrame.DYNAMIC
       || currentFrameType == FlowFrame.STATIC)
      {
         flowframe.setClear(clearBox.isSelected());
         flowframe.setVAlign(valignBox.getSelectedIndex());

         flowframe.setShape(shapeBox.getSelectedIndex());

         if (currentFrameType == FlowFrame.DYNAMIC)
         {
            flowframe.setStyleCommands(dynamicStyleField.getText().trim());
         }

         flowframe.setContents(contentsViewer.getText());
      }
      else
      {
         flowframe.setMarginPosition(marginParBox.getSelectedIndex());
      }

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

      frame.setFlowFrame(selectedObject, flowframe);
   }

   protected String getPageList()
   {
      String pages = (String)pageList.getSelectedItem();

      if (pages.equals(pagesAllText))
      {
         pages = "all";
      }
      else if (pages.equals(pagesOddText))
      {
         pages = "odd";
      }
      else if (pages.equals(pagesEvenText))
      {
         pages = "even";
      }
      else if (pages.equals(pagesNoneText))
      {
         pages = "none";
      }

      return pages;
   }

   public void cancel()
   {
      if (currentCard != CARD_FRAME_TYPE 
           && getResources().confirm(this, 
            getResources().getMessage("flfwizard.confirm_cancel")) != JOptionPane.YES_OPTION)
      {
         return;
      }

      setVisible(false);
   }

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("cancel"))
      {
         cancel();
      }
      else if (action.equals("edit"))
      {
         String contents = application.displayTeXEditorDialog(
           contentsViewer.getText());

         if (contents != null)
         {
            contentsViewer.setText(contents);
         }
      }
      else if (action.equals("compute_sym_x_shift"))
      {
         CanvasGraphics cg = frame.getCanvasGraphics();

         Rectangle2D bounds = currentTypeblock.getBounds2D(
            cg.getStoragePaperWidth(),
            cg.getStoragePaperHeight());

         double x0 = bounds.getX();
         double x1 = x0 + bounds.getWidth();
         
         BBox bbox = selectedObject.getStorageBBox();
         double w = bbox.getWidth();
         double bx = bbox.getMinX();
         
         double shift = x0 + x1 - 2*bx - w;
      
         evenXShiftLength.setValue(shift, cg.getStorageUnit());
      }
      else if (action.equals("previous"))
      {
         if (currentCard > 0)
         {
            cardLayout.previous(mainPanel);
            currentCard--;
            updateCardButtons();
         }
      }
      else if (action.equals("next"))
      {
         if (currentCard < MAX_CARDS - 1)
         {
            if (!canMoveNext())
            {
               return;
            }

            cardLayout.next(mainPanel);
            currentCard++;

            if (currentCard == CARD_TYPEBLOCK)
            {
               applyCls();
            }
            else if (currentCard == CARD_FRAME_TYPE)
            {
               if (currentTypeblock == null)
               {
                  typeblockModified = true;
               }

               applyTypeblock();
            }
            else if (currentCard == CARD_FRAME_MARGINS)
            {
               margins.setNone(!useObjectAsBorder.isSelected());
            }
            else if (currentCard == CARD_FINISH)
            {
               try
               {
                  applyFlowFrame();

                  int n = objectList.getElementCount();

                  if (n > 1 && getResources().confirm(this, 
                       getResources().getMessage("flfwizard.finish.query_repeat"))
                       == JOptionPane.YES_OPTION)
                  {
                     String label = labelField.getText();
                     objectList.updateDescription(selectedObjectIndex, getResources());
                     selectedObjectIndex++;

                     if (selectedObjectIndex >= n)
                     {
                        selectedObjectIndex = 0;
                     }

                     objectList.setSelectedIndex(selectedObjectIndex);
                     selectedObject = objectList.getObject(selectedObjectIndex);

                     if (isSpecial)
                     {
                        updateSpecialBooleans(label);
                        isSpecial = false;
                     }

                     currentCard = CARD_FRAME_TYPE;
                     cardLayout.show(mainPanel, "frametype");

                     initialiseFlowFrame(false);
                  }
                  else
                  {
                     setVisible(false);
                  }
               }
               catch (Throwable e)
               {
                  getResources().error(this, e.getMessage());
               }
            }

            updateCardButtons();
         }
      }
   }

   public boolean canMoveNext()
   {
      if (currentCard == CARD_FRAME_TYPE)
      {
         updateFlowFrameWidgets();
      }
      else if (currentCard == CARD_LABEL_PAGES)
      {
         JDRResources resources = getResources();

         String label = labelField.getText().trim();

         if (label.isEmpty())
         {
            resources.error(this,
              resources.getMessage("error.no_idl"));
            labelField.requestFocusInWindow();
            return false;
         }

         // check unique label
         if (!frame.isUniqueLabel(selectedObjectIndex,selectedObject,label))
         {
            resources.error(this,
               resources.getMessage("error.idl_exists"));
            return false;
         }

         String pages = getPageList();

         // check page list is valid
         if (!FlowFrame.isValidPageList(pages))
         {
            resources.error(this,
               resources.getMessage("error.invalid_frame-page-list"));
            return false;
         }
      }

      return true;
   }

   protected void updateFlowFrameWidgets()
   {
      FlowFrame flowframe = selectedObject.getFlowFrame();
      String currentLabel = "";

      if (flowframe != null)
      {
         currentLabel = flowframe.getLabel();
      }

      if (flowBox.isSelected())
      {
         currentFrameType = FlowFrame.FLOW;
         typeField.setText(flowText);
         updateSpecialLabelInfo("");
      }
      else if (staticBox.isSelected())
      {
         currentFrameType = FlowFrame.STATIC;
         typeField.setText(staticText);
         updateSpecialLabelInfo("");
      }
      else
      {
         currentFrameType = FlowFrame.DYNAMIC;
         typeField.setText(dynamicText);

         if (headerBox.isSelected())
         {
            currentLabel = "header";
         }
         else if (evenHeaderBox.isSelected())
         {
            currentLabel = "evenheader";
            pageList.setSelectedItem(pagesEvenText);
         }
         else if (footerBox.isSelected())
         {
            currentLabel = "footer";
         }
         else if (evenFooterBox.isSelected())
         {
            currentLabel = "evenfooter";
            pageList.setSelectedItem(pagesEvenText);
         }
         else if (thumbtabBox.isSelected())
         {
            if (!currentLabel.startsWith("thumbtab"))
            {
               currentLabel = "thumbtab"+maxThumbtab;
            }
         }
         else if (thumbtabIndexBox.isSelected())
         {
            if (!currentLabel.startsWith("thumbtabindex"))
            {
               currentLabel = "thumbtabindex"+maxThumbtabIndex;
            }
         }
         else if (evenThumbtabBox.isSelected())
         {
            if (!currentLabel.startsWith("eventhumbtab"))
            {
               currentLabel = "eventhumbtab"+maxEvenThumbtab;
            }

            pageList.setSelectedItem(pagesEvenText);
         }
         else if (evenThumbtabIndexBox.isSelected())
         {
            if (!currentLabel.startsWith("eventhumbtabindex"))
            {
               currentLabel = "eventhumbtabindex"+maxEvenThumbtab;
            }

            pageList.setSelectedItem(pagesEvenText);
         }

         updateSpecialLabelInfo(currentLabel);
      }

      labelField.setText(currentLabel);

      if (currentFrameType == FlowFrame.STATIC || currentFrameType == FlowFrame.DYNAMIC)
      {
         clearBox.setVisible(!isSpecial);
         shapeAlignComp.setVisible(true);
         marginParComp.setVisible(false);

         if (selectedObject instanceof JDRShape)
         {
            shapeBox.setEnabled(true);
            margins.setVisible(shapeBox.getSelectedIndex() == FlowFrame.STANDARD);
         }
         else
         {
            shapeBox.setSelectedIndex(FlowFrame.STANDARD);
            shapeBox.setEnabled(false);
            margins.setVisible(true);
         }

         if (currentFrameType == FlowFrame.STATIC)
         {
            styleCmdsComp.setVisible(false);
            contentInfoArea.setText(contentInfoStaticText);

            setEditContentVisible(true);
         }
         else
         {
            styleCmdsComp.setVisible(true);
            contentInfoArea.setText(contentInfoDynamicText);

            setEditContentVisible(!isSpecial);
         }
      }
      else
      {
         clearBox.setVisible(false);
         styleCmdsComp.setVisible(false);
         shapeAlignComp.setVisible(false);
         marginParComp.setVisible(true);
         contentInfoArea.setText(contentInfoFlowText);
         setEditContentVisible(false);
         shapeBox.setSelectedIndex(FlowFrame.STANDARD);
         margins.setVisible(true);
      }

      CanvasGraphics cg = frame.getCanvasGraphics();
      JDRUnit unit = cg.getStorageUnit();

      if (flowframe == null)
      {
         if (!evenXShiftLength.getUnit().equals(unit))
         {
            evenXShiftLength.setValue(0.0, unit);
         }

         if (!evenYShiftLength.getUnit().equals(unit))
         {
            evenYShiftLength.setValue(0.0, unit);
         }
      }
      else
      {
         if (margins.isVisible())
         {
            margins.setMargins(unit,
                               flowframe.getLeft(), flowframe.getRight(),
                               flowframe.getTop(), flowframe.getBottom());
         }

         evenXShiftLength.setValue(flowframe.getEvenXShift(), unit);
         evenYShiftLength.setValue(flowframe.getEvenYShift(), unit);
      }
   }

   protected void setEditContentVisible(boolean isVisible)
   {
      contentsViewerSp.setVisible(isVisible);
      editContentButton.setVisible(isVisible);
   }

   protected void updateCardButtons()
   {
      setTitle(getResources().getMessage("flfwizard.fulltitle",
        mainTitle, cardComponents[currentCard].getName()));

      if (currentCard > 0)
      {
         prevButton.setVisible(true);
         prevButton.setText(cardComponents[currentCard-1].getName());
      }
      else
      {
         prevButton.setVisible(false);
      }

      if (currentCard == CARD_LABEL_PAGES)
      {
         labelField.requestFocusInWindow();
      }

      if (currentCard < MAX_CARDS - 1)
      {
         nextButton.setVisible(true);
         nextButton.setText(cardComponents[currentCard+1].getName());

         nextButton.setEnabled(!(currentCard >= CARD_FRAME_TYPE && selectedObject == null));
      }
      else
      {
         nextButton.setVisible(false);
      }
   }

   @Override
   public void itemStateChanged(ItemEvent evt)
   {
      Object src = evt.getSource();

      int state = evt.getStateChange();

      if (state == ItemEvent.SELECTED || state == ItemEvent.DESELECTED)
      {
         settingChanged(src);
      }

      if (src == useDefaultCls || src == useCustomCls)
      {
         customClsField.setEnabled(useCustomCls.isSelected());
      }
      else if (src == useObjectAsBorder)
      {
         margins.setNone(!useObjectAsBorder.isSelected());
      }
   }

   @Override
   public void stateChanged(ChangeEvent evt)
   {
      settingChanged(evt.getSource());
   }

   protected void settingChanged(Object src)
   {
      switch (currentCard)
      {
         case CARD_CLS:
            clsModified = true;
         break;
         case CARD_TYPEBLOCK:
            typeblockModified = true;
         break;
         case CARD_FRAME_TYPE:
            nextButton.setEnabled(selectedObject != null);
         break;
         case CARD_FRAME_STYLE:

            if (src == shapeBox)
            {
               margins.setEnabled(shapeBox.getSelectedIndex() == FlowFrame.STANDARD);
            }

         break;
         case CARD_LABEL_PAGES:

            if (src == labelField.getDocument())
            {
               if (currentFrameType == FlowFrame.DYNAMIC)
               {
                  String label = labelField.getText();

                  if (label.equals("footer")
                      || label.equals("evenfooter")
                      || label.equals("header")
                      || label.equals("evenheader")
                     )
                  {
                     updateSpecialLabelInfo(label);
                  }
                  else if (label.startsWith("thumbtab") || label.startsWith("eventhumbtab"))
                  {
                     int idx = 8;

                     if (label.charAt(0) == 'e')
                     {
                        idx += 4;
                     }

                     if (label.substring(8).startsWith("index"))
                     {
                        idx += 5;
                     }

                     try
                     {
                        Integer.parseInt(label.substring(idx));
                        updateSpecialLabelInfo(label);
                     }
                     catch (NumberFormatException e)
                     {
                        updateSpecialLabelInfo("");
                     }
                  }
                  else
                  {
                     updateSpecialLabelInfo("");
                  }
               }
               else
               {
                  updateSpecialLabelInfo("");
               }
            }

         break;
      }
   }

   protected void updateSpecialLabelInfo(String label)
   {
      if (label.isEmpty())
      {
         specialInfoLabelField.setVisible(false);
         isSpecial = false;
      }
      else
      {
         specialInfoLabelField.setText(
           getResources().getMessage(
           "flfwizard.labelpages.frame_label.special_info", label));

         specialInfoLabelField.setVisible(true);
         isSpecial = true;
      }
   }

   @Override
   public void valueChanged(ListSelectionEvent evt)
   {
      if (evt.getSource() == objectList && !evt.getValueIsAdjusting())
      {
         selectedObject = objectList.getSelectedObject();
         selectedObjectIndex = objectList.getSelectedIndex();
         updateFrameTypeComp();
         settingChanged(evt.getSource());
      }
   }

   @Override
   public void changedUpdate(DocumentEvent evt)
   {
      settingChanged(evt.getDocument());
   }

   @Override
   public void insertUpdate(DocumentEvent evt)
   {
      settingChanged(evt.getDocument());
   }

   @Override
   public void removeUpdate(DocumentEvent evt)
   {
      settingChanged(evt.getDocument());
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   FlowframTk application;
   JDRFrame frame;
   FlowFrame currentTypeblock;

   JComponent mainPanel;
   CardLayout cardLayout;
   JButton prevButton, nextButton;

   JRadioButton useDefaultCls, useCustomCls;
   JTextField customClsField;

   JCheckBox updateImageBox;

   JComboBox<Integer> fontSizeBox;
   JCheckBox useRelativeFontDeclarations;

   private static final Integer[] availableSizes = new Integer[]
      {
         Integer.valueOf(25),
         Integer.valueOf(20),
         Integer.valueOf(17),
         Integer.valueOf(14),
         Integer.valueOf(12),
         Integer.valueOf(11),
         Integer.valueOf(10),
         Integer.valueOf(9),
         Integer.valueOf(8)
      };

   boolean clsModified, typeblockModified;

   TypeblockPanel typeblockPanel;
   JDRCompleteObjectJList objectList;
   JDRCompleteObject selectedObject;
   int selectedObjectIndex = -1;
   JLabel emptyImageLabel, selectedObjectLabel;
   String selectedObjectSetText, selectedObjectNotSetText;
   boolean hasHeader, hasEvenHeader, hasFooter, hasEvenFooter;
   int maxThumbtab, maxEvenThumbtab, maxThumbtabIndex, maxEvenThumbtabIndex;

   JRadioButton headerBox, evenHeaderBox, footerBox, evenFooterBox, thumbtabBox,
     evenThumbtabBox, thumbtabIndexBox,
     evenThumbtabIndexBox, staticBox, dynamicBox, flowBox;

   int currentFrameType = -1;
   JTextField typeField, labelField;
   String flowText, staticText, dynamicText;
   JTextField specialInfoLabelField;
   boolean isSpecial = false;

   JRadioButton relativePagesBox, absolutePagesBox;

   JTextField currentPageSettingField, currentGridUnitField, currentStorageUnitField,
    currentPaperField;

   JComboBox<String> pageList;
   String pagesAllText, pagesOddText, pagesEvenText, pagesNoneText;

   JCheckBox useObjectAsBorder, clearBox;
   JTextField dynamicStyleField;
   JComboBox<String> valignBox, shapeBox, marginParBox;

   JComponent styleCmdsComp, shapeAlignComp, marginParComp;
   JTextArea contentInfoArea;
   String contentInfoStaticText, contentInfoDynamicText, contentInfoFlowText;

   JTextArea contentsViewer;
   JScrollPane contentsViewerSp;
   JButton editContentButton;

   MarginPanel margins;
   LengthPanel evenXShiftLength, evenYShiftLength;

   public static final int INFO_MAX_COLS=60;

   int currentCard;
   public static final int CARD_CLS=0;
   public static final int CARD_TYPEBLOCK=1;
   public static final int CARD_FRAME_TYPE=2;
   public static final int CARD_LABEL_PAGES=3;
   public static final int CARD_FRAME_STYLE=4;
   public static final int CARD_FRAME_MARGINS=5;
   public static final int CARD_FINISH=6;

   public static final int MAX_CARDS = CARD_FINISH + 1;

   JComponent[] cardComponents;
   private String mainTitle;
}
