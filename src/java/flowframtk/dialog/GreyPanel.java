// File          : GreyPanel.java
// Description   : Panel for selecting grey scale
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

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Panel for selecting grey scale.
 * @author Nicola L C Talbot
 */

public class GreyPanel extends JPanel
   implements AdjustmentListener,SingleColourSelector
{
   public GreyPanel(JDRResources resources)
   {
      this.resources = resources;
      initialise();
   }

   public GreyPanel(JDRResources resources, AdjustmentListener al)
   {
      this.resources = resources;
      initialise();
      addAdjustmentListener(al);
   }

   public void addAdjustmentListener(AdjustmentListener al)
   {
      greySB.addAdjustmentListener(al);
      alphaSB.addAdjustmentListener(al);
   }

   public void initialise()
   {
      setLayout(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.weightx = 100;
      constraints.weighty = 100;
      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.gridwidth = 2;
      constraints.gridheight = 1;
      constraints.fill = GridBagConstraints.HORIZONTAL;

      JPanel sliders = new JPanel();
      add(sliders,constraints);

      sliders.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.weightx = 100;
      gbc.weighty = 100;
      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      greyLabel = new JLabel(getResources().getMessage("colour.grey"),
                            SwingConstants.RIGHT);
      sliders.add(greyLabel, gbc);

      greySB = new JScrollBar(Adjustable.HORIZONTAL, 0,0,0,100);
      gbc.gridy = 0;
      gbc.gridx = 1;
      gbc.gridwidth = 4;
      gbc.gridheight = 1;
      sliders.add(greySB, gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.WEST;
      sliders.add(greyText = new PercentageField(0), gbc);
      greyText.getDocument().addDocumentListener(
          new TextFieldSBarListener(greyText,greySB));

      greySB.setBlockIncrement(10);
      greySB.addAdjustmentListener(this);

      gbc.gridx = 0;
      gbc.gridy++;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      gbc.fill = GridBagConstraints.BOTH;
      alphaLabel = new JLabel(getResources().getMessage("colour.alpha"),
                              SwingConstants.RIGHT);
      sliders.add(alphaLabel, gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 4;
      alphaSB = new JScrollBar(Adjustable.HORIZONTAL,100,0,0,100);
      sliders.add(alphaSB,gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      sliders.add(alphaText = new PercentageField(100),gbc);
      alphaText.getDocument().addDocumentListener(
          new TextFieldSBarListener(alphaText,alphaSB));
      alphaSB.setBlockIncrement(10);
      alphaSB.addAdjustmentListener(this);
   }

   public boolean requestDefaultColourFocus()
   {
      return greyText.requestFocusInWindow();
   }

   public void adjustmentValueChanged(AdjustmentEvent evt)
   {
      if (greyText.getInt() != (greySB.getValue()))
      {
         greyText.setValue(greySB.getValue());
      }

      if (alphaText.getInt() != (alphaSB.getValue()))
      {
         alphaText.setValue(alphaSB.getValue());
      }
   }

   public JDRPaint getPaint(CanvasGraphics cg)
   {
      return new JDRGray(cg, 0.01*greySB.getValue(),
                         0.01*alphaSB.getValue());
   }

   public void setPaint(JDRPaint paint)
   {
      JDRGray g = paint.getJDRGray();

      greySB.setValue((int)Math.round((g.getGray()*100.0)));
      alphaSB.setValue((int)Math.round((g.getAlpha()*100.0)));
   }

   public void setPaint(Color paint)
   {
      double gray = (paint.getRed()+paint.getGreen()+paint.getBlue())
                  / (255.0*3);

      greySB.setValue((int)Math.round((gray*100.0)));
      alphaSB.setValue((int)Math.round((paint.getAlpha()*100.0/255.0)));
   }

   public void setEnabled(boolean flag)
   {
      greySB.setEnabled(flag);
      alphaSB.setEnabled(flag);
      greyText.setEnabled(flag);
      alphaText.setEnabled(flag);
      greyLabel.setEnabled(flag);
      alphaLabel.setEnabled(flag);
   }

   public JDRResources getResources()
   {
      return resources;
   }

   private JScrollBar greySB, alphaSB;
   private PercentageField greyText, alphaText;
   private JLabel greyLabel, alphaLabel;

   private JDRResources resources;
}
