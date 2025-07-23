// File          : LineStylePanel.java
// Description   : Panel for selecting line style
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
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Panel for selecting line style.
 * @author Nicola L C Talbot
 */

public class LineStylePanel extends JScrollPane
   implements ItemListener,ActionListener
{
   public LineStylePanel(JDRSelector selector)
   {
      super();
      selector_ = selector;

      JPanel p = new JPanel();

      p.setLayout(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();

      constraints.gridx=0;
      constraints.gridy=0;
      constraints.gridwidth=3;
      constraints.gridheight=1;
      constraints.fill=GridBagConstraints.NONE;
      constraints.anchor=GridBagConstraints.LINE_START;
      constraints.weightx=1;
      constraints.weighty=1;

      // line width panel
      lineWidthPanel = new LineWidthPanel(selector_);
      lineWidthPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
      lineWidthPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      p.add(lineWidthPanel, constraints);

      constraints.gridy++;
      // dash pattern panel
      dashPatternPanel = new DashPatternPanel(selector_);
      dashPatternPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      p.add(dashPatternPanel, constraints);

      constraints.gridy++;
      JPanel capAndJoinPanel 
         = new JPanel(new FlowLayout(FlowLayout.LEADING)); 
      capAndJoinPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      // cap style
      JLabel capStyleLabel = getResources().createAppLabel("linestyle.cap");
      capAndJoinPanel.add(capStyleLabel);

      String[] capStyles = {
         getResources().getMessage("linestyle.cap.butt"),
         getResources().getMessage("linestyle.cap.round"),
         getResources().getMessage("linestyle.cap.square")};
      capStyleBox = new JComboBox<String>(capStyles);
      capStyleLabel.setLabelFor(capStyleBox);
      capAndJoinPanel.add(capStyleBox);
      capStyleBox.addItemListener(this);

      // join style

      joinStylePanel = new JoinStylePanel(selector_);
      capAndJoinPanel.add(joinStylePanel);

      p.add(capAndJoinPanel, constraints);

      Insets oldInsets = constraints.insets;

      // start marker 

      constraints.gridy++;
      constraints.gridwidth=1;
      constraints.insets = new Insets(0, 5, 0, 0);
      JLabel startArrowLabel = getResources().createAppLabel(
         "linestyle.arrow.start");
      p.add(startArrowLabel, constraints);

      startArrowDesc = new JLabel("None");
      startArrowDesc.setOpaque(true);
      startArrowDesc.setBackground(Color.white);
      startArrowDesc.setBorder(
         BorderFactory.createLoweredBevelBorder());

      Dimension dim = startArrowDesc.getPreferredSize();
      dim.width = 250;
      startArrowDesc.setPreferredSize(dim);

      constraints.gridx++;
      p.add(startArrowDesc, constraints);

      startArrowDialog = new ArrowStyleDialog(selector_,
         ArrowStylePanel.START);

      startArrowButton = getResources().createDialogButton("button.choose",
         "choose", this, null);
      startArrowLabel.setLabelFor(startArrowButton);

      constraints.gridx++;
      p.add(startArrowButton, constraints);

      constraints.gridy++;
      constraints.gridx=0;
      // mid marker

      JLabel midArrowLabel = getResources().createAppLabel(
         "linestyle.arrow.mid");
      p.add(midArrowLabel, constraints);

      midArrowDesc = new JLabel("None");
      midArrowDesc.setOpaque(true);
      midArrowDesc.setBackground(Color.white);
      midArrowDesc.setBorder(
         BorderFactory.createLoweredBevelBorder());

      midArrowDesc.setPreferredSize(dim);
      constraints.gridx++;
      p.add(midArrowDesc, constraints);

      midArrowDialog = new ArrowStyleDialog(selector_,
         ArrowStylePanel.MID);

      midArrowButton = getResources().createDialogButton("button.choose",
        "choose", this, null);
      midArrowLabel.setLabelFor(midArrowButton);

      constraints.gridx++;
      p.add(midArrowButton, constraints);

      // end marker

      JLabel endArrowLabel = getResources().createAppLabel(
         "linestyle.arrow.end");

      constraints.gridy++;
      constraints.gridx = 0;
      p.add(endArrowLabel, constraints);

      endArrowDesc = new JLabel("None");
      endArrowDesc.setBackground(Color.white);
      endArrowDesc.setOpaque(true);
      endArrowDesc.setBackground(Color.white);
      endArrowDesc.setBorder(
         BorderFactory.createLoweredBevelBorder());

      endArrowDesc.setPreferredSize(dim);

      constraints.gridx++;
      p.add(endArrowDesc, constraints);

      endArrowDialog = new ArrowStyleDialog(selector_,
         ArrowStylePanel.END);

      endArrowButton = getResources().createDialogButton("button.choose",
         "choose", this, null);
      endArrowLabel.setLabelFor(endArrowButton);

      constraints.gridx++;
      p.add(endArrowButton, constraints);

      // winding rule
      JPanel windingRulePanel = new JPanel();
      windingRulePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      JLabel windingRuleLabel = getResources().createAppLabel(
         "linestyle.winding_rule");
      windingRulePanel.add(windingRuleLabel);

      String[] windingRules = new String[]
         {getResources().getMessage("linestyle.winding_rule.eo"),
         getResources().getMessage("linestyle.winding_rule.nz")};
      windingRuleBox = new JComboBox<String>(windingRules);
      windingRuleLabel.setLabelFor(windingRuleBox);

      windingRulePanel.add(windingRuleBox);
      windingRuleBox.addItemListener(this);

      constraints.gridy++;
      constraints.gridx=0;
      constraints.gridwidth=3;
      constraints.insets = oldInsets;
      p.add(windingRulePanel, constraints);

      setViewportView(p);
   }

   public void itemStateChanged(ItemEvent e)
   {
      Object source = e.getSource();

      selector_.repaintSample();
   }

   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();

      if (source == startArrowButton)
      {
          startArrowDialog.init();
          startArrowDesc.setText(startArrowDialog.getDescription());
      }
      else if (source == midArrowButton)
      {
          midArrowDialog.init();
          midArrowDesc.setText(midArrowDialog.getDescription());
      }
      else if (source == endArrowButton)
      {
          endArrowDialog.init();
          endArrowDesc.setText(endArrowDialog.getDescription());
      }

      selector_.repaintSample();
   }

   public JDRBasicStroke getStroke(CanvasGraphics cg)
   {
      JDRBasicStroke stroke;

      stroke = new JDRBasicStroke(cg,
                                getPenWidth(),
                                getCapStyle(),
                                getJoinStyle(),
                                getMitreLimit(),
                                getDashPattern(cg),
                                getWindingRule());

      stroke.setStartArrow(getStartMarker());
      stroke.setMidArrow(getMidMarker());
      stroke.setEndArrow(getEndMarker());

      return stroke;
   }

   public void setStroke(JDRBasicStroke stroke)
   {
      setPenWidth(stroke.getPenWidth());
      setCapStyle(stroke.getCapStyle());
      setJoinStyle(stroke.getJoinStyle());
      setMitreLimit(stroke.getMitreLimit());

      setDashPattern(stroke.dashPattern);

      setStartMarker(stroke.getStartArrow());
      startArrowDesc.setText(startArrowDialog.getDescription());

      setMidMarker(stroke.getMidArrow());
      midArrowDesc.setText(midArrowDialog.getDescription());

      setEndMarker(stroke.getEndArrow());
      endArrowDesc.setText(endArrowDialog.getDescription());

      setWindingRule(stroke.getWindingRule());
   }

   public JDRLength getPenWidth()
   {
      return lineWidthPanel.getPenWidth();
   }

   public void setPenWidth(JDRLength width)
   {
      lineWidthPanel.setPenWidth(width);
   }

   public void setPenWidth(double width, JDRUnit unit)
   {
      lineWidthPanel.setPenWidth(width, unit);
   }

   public int getCapStyle()
   {
      return capStyleBox.getSelectedIndex();
   }

   public int getJoinStyle()
   {
      return joinStylePanel.getJoinStyle();
   }

   public int getWindingRule()
   {
      return windingRuleBox.getSelectedIndex();
   }

   public void setCapStyle(int style)
   {
      capStyleBox.setSelectedIndex(style);
   }

   public void setJoinStyle(int style)
   {
      joinStylePanel.setJoinStyle(style);
   }

   public JDRMarker getStartMarker()
   {
      return startArrowDialog.getMarker();
   }

   public JDRMarker getMidMarker()
   {
      return midArrowDialog.getMarker();
   }

   public JDRMarker getEndMarker()
   {
      return endArrowDialog.getMarker();
   }

   public void setStartMarker(JDRMarker marker)
   {
      startArrowDialog.setMarker(marker);
   }

   public void setMidMarker(JDRMarker marker)
   {
      midArrowDialog.setMarker(marker);
   }

   public void setEndMarker(JDRMarker marker)
   {
      endArrowDialog.setMarker(marker);
   }

   public void setWindingRule(int rule)
   {
      windingRuleBox.setSelectedIndex(rule);
   }

   protected double getEnteredMitreLimit()
   {
      return joinStylePanel.getEnteredMitreLimit();
   }

   public double getMitreLimit()
   {
      return joinStylePanel.getMitreLimit();
   }

   public void setMitreLimit(double limit)
   {
      joinStylePanel.setMitreLimit(limit);
   }

   public DashPattern getDashPattern(CanvasGraphics cg)
   {
      return dashPatternPanel.getDashPattern(cg);
   }

   public void setDashPattern(DashPattern dp)
   {
      dashPatternPanel.setDashPattern(dp);
   }

   public void setDefaults()
   {
      lineWidthPanel.setDefaults();

      dashPatternPanel.setDefaults();
      setCapStyle(BasicStroke.CAP_SQUARE);
      joinStylePanel.setDefaults();

      startArrowDialog.setDefaults();
      startArrowDesc.setText(startArrowDialog.getDescription());
      midArrowDialog.setDefaults();
      midArrowDesc.setText(midArrowDialog.getDescription());
      endArrowDialog.setDefaults();
      endArrowDesc.setText(endArrowDialog.getDescription());

      setWindingRule(GeneralPath.WIND_EVEN_ODD);
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   // cap style
   private JComboBox<String> capStyleBox;

   // start arrow dialog box
   private ArrowStyleDialog startArrowDialog;

   private JLabel startArrowDesc;

   private JButton startArrowButton;

   // mid arrow dialog box
   private ArrowStyleDialog midArrowDialog;

   private JLabel midArrowDesc;

   private JButton midArrowButton;

   // end arrow dialog box

   private ArrowStyleDialog endArrowDialog;

   private JLabel endArrowDesc;

   private JButton endArrowButton;

   // winding rule
   private JComboBox<String> windingRuleBox;

   private JDRSelector selector_;

   // line width
   private LineWidthPanel lineWidthPanel;

   // dash pattern
   private DashPatternPanel dashPatternPanel;

   // join style
   private JoinStylePanel joinStylePanel;
}
