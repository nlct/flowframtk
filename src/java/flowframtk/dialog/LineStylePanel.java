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

import com.dickimawbooks.texjavahelplib.JLabelGroup;

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

      JComponent mainPanel = Box.createVerticalBox();

      // line width panel
      lineWidthPanel = new LineWidthPanel(selector_);
      lineWidthPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
      lineWidthPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      mainPanel.add(lineWidthPanel);

      // dash pattern panel

      dashPatternPanel = new DashPatternPanel(selector_);
      dashPatternPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      mainPanel.add(dashPatternPanel);

      JComponent capAndJoinPanel = createRow(); 

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

      mainPanel.add(capAndJoinPanel);

      // start marker 

      JLabelGroup labelGroup = new JLabelGroup();
      String placeholder = getResources().getMessage("arrow.placeholder");

      JComponent startMarkerPanel = createRow();
      mainPanel.add(startMarkerPanel);

      JLabel startArrowLabel = getResources().createAppLabel(
         "linestyle.arrow.start");
      labelGroup.add(startArrowLabel);
      startMarkerPanel.add(startArrowLabel);

      startArrowDesc = new JLabel(placeholder);
      startArrowDesc.setOpaque(true);
      startArrowDesc.setBackground(Color.white);
      startArrowDesc.setBorder(
         BorderFactory.createLoweredBevelBorder());

      Dimension dim = startArrowDesc.getPreferredSize();
      startArrowDesc.setPreferredSize(dim);

      startMarkerPanel.add(startArrowDesc);

      startArrowDialog = new ArrowStyleDialog(selector_,
         ArrowStylePanel.START);

      startArrowButton = getResources().createDialogButton("button.choose",
         "choose", this, null);
      startArrowLabel.setLabelFor(startArrowButton);

      startMarkerPanel.add(startArrowButton);

      // mid marker

      JComponent midMarkerPanel = createRow();
      mainPanel.add(midMarkerPanel);

      JLabel midArrowLabel = getResources().createAppLabel(
         "linestyle.arrow.mid");
      labelGroup.add(midArrowLabel);

      midMarkerPanel.add(midArrowLabel);

      midArrowDesc = new JLabel(placeholder);
      midArrowDesc.setOpaque(true);
      midArrowDesc.setBackground(Color.white);
      midArrowDesc.setBorder(
         BorderFactory.createLoweredBevelBorder());

      midArrowDesc.setPreferredSize(dim);
      midMarkerPanel.add(midArrowDesc);

      midArrowDialog = new ArrowStyleDialog(selector_,
         ArrowStylePanel.MID);

      midArrowButton = getResources().createDialogButton("button.choose",
        "choose", this, null);
      midArrowLabel.setLabelFor(midArrowButton);

      midMarkerPanel.add(midArrowButton);

      // end marker

      JComponent endMarkerPanel = createRow();
      mainPanel.add(endMarkerPanel);

      JLabel endArrowLabel = getResources().createAppLabel(
         "linestyle.arrow.end");
      labelGroup.add(endArrowLabel);

      endMarkerPanel.add(endArrowLabel);

      endArrowDesc = new JLabel(placeholder);
      endArrowDesc.setBackground(Color.white);
      endArrowDesc.setOpaque(true);
      endArrowDesc.setBackground(Color.white);
      endArrowDesc.setBorder(
         BorderFactory.createLoweredBevelBorder());

      endArrowDesc.setPreferredSize(dim);

      endMarkerPanel.add(endArrowDesc);

      endArrowDialog = new ArrowStyleDialog(selector_,
         ArrowStylePanel.END);

      endArrowButton = getResources().createDialogButton("button.choose",
         "choose", this, null);
      endArrowLabel.setLabelFor(endArrowButton);

      endMarkerPanel.add(endArrowButton);

      // winding rule
      JComponent windingRulePanel = createRow();

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

      mainPanel.add(windingRulePanel);

      mainPanel.add(Box.createVerticalStrut(10));
      mainPanel.add(Box.createVerticalGlue());

      setViewportView(mainPanel);
   }

   protected JComponent createRow()
   {
      JComponent row = new JPanel(new FlowLayout(FlowLayout.LEADING));

      row.setAlignmentX(Component.LEFT_ALIGNMENT);

      return row;
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
