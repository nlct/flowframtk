// File          : JoinStylePanel.java
// Description   : Panel for selecting join style
// Creation Date : 6th February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006 Nicola L.C. Talbot

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

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;
/**
 * Panel for selecting join style.
 * @author Nicola L C Talbot
 */

public class JoinStylePanel extends JPanel implements ItemListener
{
   public JoinStylePanel(JDRSelector selector)
   {
      selector_ = selector;

      JLabel joinStylesLabel = getResources().createAppLabel("linestyle.join");
      add(joinStylesLabel);

      String[] joinStyles = {
         getResources().getString("linestyle.join.mitre"),
         getResources().getString("linestyle.join.round"),
         getResources().getString("linestyle.join.bevel")};
      joinStyleBox = new JComboBox<String>(joinStyles);
      add(joinStyleBox);

      joinStyleBox.addItemListener(this);

      mitreLimitLabel = getResources().createAppLabel("linestyle.mitre_limit");

      add(mitreLimitLabel);

      mitreLimitField = new NonNegativeDoubleField(10);
      mitreLimitLabel.setLabelFor(mitreLimitField);
      mitreLimitField.getDocument().addDocumentListener(
          new TextFieldSampleListener(
             selector_.getSamplePathPanel()));

      add(mitreLimitField);

   }

   public void itemStateChanged(ItemEvent e)
   {
      Object source = e.getSource();

      if (source == joinStyleBox)
      {
         boolean flag = (getJoinStyle() == BasicStroke.JOIN_MITER);

         mitreLimitLabel.setEnabled(flag);
         mitreLimitField.setEnabled(flag);
      }

      selector_.repaintSample();
   }

   public JDRBasicStroke getStroke()
   {
      JDRFrame frame = selector_.application_.getCurrentFrame();

      JDRBasicStroke stroke;

      if (frame == null)
      {
         stroke = new JDRBasicStroke(selector_.getCanvasGraphics());
      }
      else
      {
         stroke = (JDRBasicStroke)frame.getSelectedStroke().clone();
      }

      int style = getJoinStyle();

      stroke.setJoinStyle(style);

      if (style == BasicStroke.JOIN_MITER)
      {
         stroke.setMitreLimit(getMitreLimit());
      }

      return stroke;
   }

   public void setStroke(JDRBasicStroke stroke)
   {
      int style = stroke.getJoinStyle();
      setJoinStyle(style);

      if (style == BasicStroke.JOIN_MITER)
      {
         setMitreLimit(stroke.getMitreLimit());
      }
   }

   public int getJoinStyle()
   {
      return joinStyleBox.getSelectedIndex();
   }

   public void setJoinStyle(int style)
   {
      joinStyleBox.setSelectedIndex(style);
   }

   protected double getEnteredMitreLimit()
   {
      return mitreLimitField.getDouble();
   }

   public double getMitreLimit()
   {
      double limit = mitreLimitField.getDouble();

      return limit < 1 ? 1.0 : limit;
   }

   public void setMitreLimit(double limit)
   {
      mitreLimitField.setValue(limit < 1 ? 1.0 : limit);
   }

   public void setDefaults()
   {
      setJoinStyle(BasicStroke.JOIN_MITER);
      setMitreLimit(10.0f);
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   private JDRSelector selector_;

   private JComboBox joinStyleBox;
   private JLabel mitreLimitLabel;
   private NonNegativeDoubleField mitreLimitField;
}
