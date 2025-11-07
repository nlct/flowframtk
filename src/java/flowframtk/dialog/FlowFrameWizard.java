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

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.ExportSettings;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.*;

public class FlowFrameWizard extends JDialog
 implements ActionListener,ItemListener,ChangeListener,ListSelectionListener
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

      getContentPane().add(mainPanel, "Center");

      JComponent buttonPanel = new JPanel(new BorderLayout());

      JPanel leftButtonPanel = new JPanel();
      buttonPanel.add(leftButtonPanel, "West");

      prevButton = resources.createJButton("flfwizard", "previous", this);
      prevButton.setToolTipText(prevButton.getText());
      prevButton.setHorizontalTextPosition(SwingConstants.TRAILING);
      leftButtonPanel.add(prevButton);

      JPanel rightButtonPanel = new JPanel();
      buttonPanel.add(rightButtonPanel, "East");

      nextButton = resources.createJButton("flfwizard", "next", this);
      nextButton.setToolTipText(nextButton.getText());
      nextButton.setHorizontalTextPosition(SwingConstants.LEADING);
      rightButtonPanel.add(nextButton);

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

      okayButton = resources.createOkayCancelHelpButtons(this, p, this, "sec:flowframe");
      okayButton.setEnabled(false);
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

      JTextArea info = resources.createAppInfoArea("flfwizard.cls.info", 40);
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
      row.add(customClsField);

      customClsField.setMaximumSize(customClsField.getPreferredSize());

      clampRowMax(row);

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

      clampRowMax(row);

      row = createRow();
      comp.add(row);

      updateImageBox = getResources().createAppCheckBox("flfwizard.cls",
        "update", application.isLaTeXFontUpdateEnabled(), null);

      updateImageBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      row.add(updateImageBox);

      clampRowMax(row);

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

      objectList = new JDRCompleteObjectJList();
      comp.setAlignmentX(Component.LEFT_ALIGNMENT);
      objectList.setPrototype(resources.getMessage("flfwizard.frametype.object.placeholder"));
      objectList.addListSelectionListener(this);

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

      staticBox = createRadioButton("flfwizard.frametype", "static", bg, false);
      comp.add(staticBox);

      dynamicBox = createRadioButton("flfwizard.frametype", "dynamic", bg, false);
      comp.add(dynamicBox);

      flowBox = createRadioButton("flfwizard.frametype", "flow", bg, true);
      comp.add(flowBox);

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

   protected void clampRowMax(JComponent row)
   {
      Dimension dim = row.getPreferredSize();
      dim.height += 20;
      row.setMaximumSize(dim);
   }

   protected void initialiseCls()
   {
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

   }

   protected void initialiseFrameType()
   {
      JDRGroup objects = frame.getAllObjects();
      objectList.removeAllElements();
      selectedObject = null;

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
            String label = flowframe.getLabel();

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
      }

      emptyImageLabel.setVisible(objects.isEmpty());

      updateFrameTypeComp();
   }

   protected void updateFrameTypeComp()
   {
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
            selectedObjectLabel.setText(selectedObjectSetText);
         }

         headerBox.setEnabled(!hasHeader && enableSpecial);
         evenHeaderBox.setEnabled(!hasEvenHeader && enableSpecial);
         footerBox.setEnabled(!hasFooter && enableSpecial);
         evenFooterBox.setEnabled(!hasEvenFooter && enableSpecial);

         if (!headerBox.isEnabled() && headerBox.isSelected())
         {
            flowBox.setSelected(true);
         }

         if (!evenHeaderBox.isEnabled() && evenHeaderBox.isSelected())
         {
            flowBox.setSelected(true);
         }

         if (!footerBox.isEnabled() && footerBox.isSelected())
         {
            flowBox.setSelected(true);
         }

         if (!evenFooterBox.isEnabled() && evenFooterBox.isSelected())
         {
            flowBox.setSelected(true);
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

      initialiseFrameType();

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

      updateCardButtons();

      clsModified = false;
      typeblockModified = false;
      okayButton.setEnabled(false);
      setVisible(true);
   }

   public void okay()
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
      }

      if (typeblockModified)
      {
         typeblockPanel.apply(frame);
      }

      setVisible(false);
   }

   public void cancel()
   {
      if ((clsModified || typeblockModified)
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

      if (action.equals("okay"))
      {
         okay();
      }
      else if (action.equals("cancel"))
      {
         cancel();
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
            cardLayout.next(mainPanel);
            currentCard++;

            if (currentCard == CARD_FRAME_TYPE && !typeblockModified && currentTypeblock == null)
            {
               typeblockModified = true;
               okayButton.setEnabled(true);
            }

            updateCardButtons();
         }
      }
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
            frametypeModified = true;
         break;
      }

      okayButton.setEnabled(true);
   }

   @Override
   public void valueChanged(ListSelectionEvent evt)
   {
      if (evt.getSource() == objectList && !evt.getValueIsAdjusting())
      {
         selectedObject = objectList.getSelectedObject();
         updateFrameTypeComp();
      }
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
   JButton okayButton, prevButton, nextButton;

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

   boolean clsModified, typeblockModified, frametypeModified;

   TypeblockPanel typeblockPanel;
   JDRCompleteObjectJList objectList;
   JDRCompleteObject selectedObject;
   JLabel emptyImageLabel, selectedObjectLabel;
   String selectedObjectSetText, selectedObjectNotSetText;
   boolean hasHeader, hasEvenHeader, hasFooter, hasEvenFooter;
   int maxThumbtab, maxEvenThumbtab, maxThumbtabIndex, maxEvenThumbtabIndex;

   JRadioButton headerBox, evenHeaderBox, footerBox, evenFooterBox, thumbtabBox,
     evenThumbtabBox, thumbtabIndexBox,
     evenThumbtabIndexBox, staticBox, dynamicBox, flowBox;

   int currentCard;
   public static final int CARD_CLS=0;
   public static final int CARD_TYPEBLOCK=1;
   public static final int CARD_FRAME_TYPE=2;

   public static final int MAX_CARDS = CARD_FRAME_TYPE + 1;

   JComponent[] cardComponents;
   private String mainTitle;
}
