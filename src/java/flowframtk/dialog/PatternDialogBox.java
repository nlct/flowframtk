// File          : PatternDialogBox.java
// Description   : Dialog box for converting shapes to rotational
//                 patterns
// Creation Date : 9th Sept 2010
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

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.InvalidValueException;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for converting shapes to patterns.
 */

public class PatternDialogBox extends JDialog
   implements ActionListener
{
   public PatternDialogBox(FlowframTk application)
   {
      super(application,
         application.getResources().getMessage("pattern.title"), true);
      application_ = application;

      Box box = Box.createVerticalBox();

      JPanel panel = new JPanel();

      JLabel label = getResources().createAppLabel("pattern.replicas");
      panel.add(label);

      replicaField = NumberSpinnerField.createPositiveIntField(1);
      label.setLabelFor(replicaField);
      panel.add(replicaField);

      JLabel modeLabel = getResources().createAppLabel("pattern.mode");
      panel.add(modeLabel);

      ButtonGroup modeBG = new ButtonGroup();

      singlePath = getResources().createAppRadioButton("pattern", 
         "mode.single", modeBG, true, null);

      modeBG.add(singlePath);

      panel.add(singlePath);

      multiPath = getResources().createAppRadioButton("pattern",
         "mode.multi", modeBG, false, null);

      modeBG.add(multiPath);

      panel.add(multiPath);

      box.add(panel);

      showPath = getResources().createAppCheckBox("pattern", "show_original",
         true, null);

      box.add(showPath);

      tabbedPane = new JTabbedPane();

      rotPanel = new JPanel();

      rotAnglePanel = getResources().createAnglePanel("pattern.rotate");

      rotAnglePanel.setDegrees(90);

      rotPanel.add(rotAnglePanel);

      tabbedPane.addTab(getResources().getMessage("pattern.rotational"),
         null, rotPanel);

      tabbedPane.setMnemonicAt(0,
         getResources().getCodePoint("pattern.rotational.mnemonic"));

      scaledPanel = new JPanel();

      label = getResources().createAppLabel("pattern.scale.x");
      scaledPanel.add(label);

      scaleXField = new NumberSpinnerField(2.0);

      label.setLabelFor(scaleXField);
      scaledPanel.add(scaleXField);

      label = getResources().createAppLabel("scale.y");
      scaledPanel.add(label);

      scaleYField = new NumberSpinnerField(2.0);

      label.setLabelFor(scaleYField);
      scaledPanel.add(scaleYField);

      tabbedPane.addTab(getResources().getMessage("pattern.scaled"),
         null, scaledPanel);

      tabbedPane.setMnemonicAt(1,
         getResources().getCodePoint("pattern.scaled.mnemonic"));

      spiralPanel = new JPanel();

      spiralAnglePanel = getResources().createAnglePanel("pattern.rotate");

      spiralAnglePanel.setDegrees(20);

      spiralPanel.add(spiralAnglePanel);

      spiralDistancePanel = getResources().createNonNegativeLengthPanel(
         "pattern.spiral.distance");

      spiralDistancePanel.setValue(10, JDRUnit.bp);

      spiralPanel.add(spiralDistancePanel);

      tabbedPane.addTab(getResources().getMessage("pattern.spiral"),
         null, spiralPanel);

      tabbedPane.setMnemonicAt(2,
         getResources().getCodePoint("pattern.spiral.mnemonic"));

      box.add(tabbedPane);

      getContentPane().add(box, "Center");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(getRootPane(), this));
      p2.add(getResources().createCancelButton(this));

      try
      {
         p2.add(getResources().createHelpDialogButton(this, "sec:patterns"));
      }
      catch (HelpSetNotInitialisedException e)
      {
         getResources().internalError(null, e);
      }

      getContentPane().add(p2, "South");

      pack();

      setLocationRelativeTo(application);
   }

   public void display()
   {
      display(-1);
   }

   public void display(int objectIndex)
   {
      JDRPattern pattern=null;
      index = objectIndex;

      JDRFrame mainPanel = application_.getCurrentFrame();

      if (index != -1)
      {
         JDRCompleteObject object = mainPanel.getObject(index);

         pattern = object.getPattern();
      }

      if (pattern != null)
      {
         CanvasGraphics cg = pattern.getCanvasGraphics();

         replicaField.setValue(pattern.getNumReplicas());

         if (pattern instanceof JDRRotationalPattern)
         {
            JDRRotationalPattern rotPattern 
               = (JDRRotationalPattern)pattern;

            rotAnglePanel.setValue(rotPattern.getRotationAngle());

            tabbedPane.setSelectedComponent(rotPanel);
         }
         else if (pattern instanceof JDRScaledPattern)
         {
            JDRScaledPattern scaledPattern
               = (JDRScaledPattern)pattern;

            scaleXField.setValue(scaledPattern.getScaleX());
            scaleYField.setValue(scaledPattern.getScaleY());

            tabbedPane.setSelectedComponent(scaledPanel);
         }
         else if (pattern instanceof JDRSpiralPattern)
         {
            JDRSpiralPattern spiralPattern 
               = (JDRSpiralPattern)pattern;

            spiralAnglePanel.setValue(spiralPattern.getRotationAngle());

            spiralDistancePanel.setValue(spiralPattern.getDistance(),
              cg.getStorageUnit());

            tabbedPane.setSelectedComponent(spiralPanel);
         }

         if (pattern.isSinglePath())
         {
            singlePath.setSelected(true);
         }
         else
         {
            multiPath.setSelected(true);
         }

         showPath.setSelected(pattern.showOriginal());
      }

      setVisible(true);
      replicaField.requestFocusInWindow();
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

   public JDRPattern getPattern(CanvasGraphics cg)
   throws NullPointerException,InvalidValueException
   {
      Component comp = tabbedPane.getSelectedComponent();

      int replicas = replicaField.getInt();

      if (replicas < 1) replicas = 1;

      if (comp == rotPanel)
      {
         return new JDRRotationalPattern
            (cg, null, null, rotAnglePanel.getValue(),
             replicas, singlePath.isSelected(),
             showPath.isSelected());

      }
      else if (comp == scaledPanel)
      {
         double scaleX = scaleXField.getDouble();
         double scaleY = scaleYField.getDouble();

         if (scaleX == 0.0)
         {
            throw new InvalidValueException("scaled-factor-x", "0", cg);
         }

         if (scaleY == 0.0)
         {
            throw new InvalidValueException("scaled-factor-y", "0", cg);
         }

         return new JDRScaledPattern
            (cg, null, null, scaleX, scaleY, replicas,
             singlePath.isSelected(),
             showPath.isSelected());
      }
      else if (comp == spiralPanel)
      {
         return new JDRSpiralPattern
            (cg, null, null, null,
             spiralAnglePanel.getValue(),
             spiralDistancePanel.getValue(cg.getStorageUnit()), 
             replicas, singlePath.isSelected(),
             showPath.isSelected());

      }

      throw new NullPointerException();
   }

   public void okay()
   {
      JDRFrame frame = application_.getCurrentFrame();

      try
      {
         JDRPattern pattern = getPattern(frame.getCanvasGraphics());

         setVisible(false);

         if (index == -1)
         {
         
            frame.convertSelectedToPattern(pattern);
         }
         else
         {
            frame.updatePattern(index, pattern);
         }
      }
      catch (InvalidValueException e)
      {
         getResources().error(this, e.getMessage());
      }
      catch (Throwable e)
      {
         getResources().internalError(this, e);
      }
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "RotationalPatternDialogBox:"+eol;
      str += "has focus: "+hasFocus()+eol;
      str += "replica field has focus: "+replicaField.hasFocus()+eol;

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

   private AnglePanel rotAnglePanel, spiralAnglePanel;

   private NumberSpinnerField scaleXField, scaleYField;
   private NonNegativeLengthPanel spiralDistancePanel;

   private NumberSpinnerField replicaField;
   private FlowframTk application_;

   private JRadioButton singlePath, multiPath;
   private JCheckBox showPath;

   private JTabbedPane tabbedPane;

   private int index=-1;

   private JPanel rotPanel, scaledPanel, spiralPanel;
}
