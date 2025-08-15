// File          : ColorHSBPanel.java
// Description   : Panel for selecting HSB paint
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
 * Panel for selecting HSB paint
 * @author Nicola L C Talbot
 */

public class ColorHSBPanel extends JPanel
   implements AdjustmentListener,SingleColourSelector
{
   public ColorHSBPanel(JDRResources resources)
   {
      this.resources = resources;
      initialise();
   }

   public ColorHSBPanel(JDRResources resources, AdjustmentListener al)
   {
      this.resources = resources;
      initialise();
      addAdjustmentListener(al);
   }

   public void addAdjustmentListener(AdjustmentListener al)
   {
      hueSB.addAdjustmentListener(al);
      saturationSB.addAdjustmentListener(al);
      brightnessSB.addAdjustmentListener(al);
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
      hueLabel = new JLabel(getResources().getMessage("paintselector.hue"),
                            SwingConstants.RIGHT);
      sliders.add(hueLabel, gbc);

      hueSB = new JScrollBar(Adjustable.HORIZONTAL, 0,0,0,359);
      gbc.gridy = 0;
      gbc.gridx = 1;
      gbc.gridwidth = 4;
      gbc.gridheight = 1;
      sliders.add(hueSB, gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.WEST;
      sliders.add(hueText = new IntRangeField(0, 0, 359), gbc);
      hueText.getDocument().addDocumentListener(
          new TextFieldSBarListener(hueText,hueSB));

      hueSB.setBlockIncrement(10);
      hueSB.addAdjustmentListener(this);

      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      gbc.fill = GridBagConstraints.BOTH;
      saturationLabel = new JLabel(getResources().getMessage("paintselector.saturation"),
                              SwingConstants.RIGHT);
      sliders.add(saturationLabel, gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 3;
      saturationSB = new JScrollBar(Adjustable.HORIZONTAL, 0,0,0,100);
      sliders.add(saturationSB,gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      sliders.add(saturationText = new PercentageField(0),gbc);
      saturationText.getDocument().addDocumentListener(
          new TextFieldSBarListener(saturationText,saturationSB));
      saturationSB.setBlockIncrement(10);
      saturationSB.addAdjustmentListener(this);

      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      brightnessLabel = new JLabel(getResources().getMessage("paintselector.brightness"),
                             SwingConstants.RIGHT);
      sliders.add(brightnessLabel, gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 4;
      brightnessSB = new JScrollBar(Adjustable.HORIZONTAL, 0,0,0,100);
      sliders.add(brightnessSB,gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      sliders.add(brightnessText = new PercentageField(0),gbc);
      brightnessText.getDocument().addDocumentListener(
          new TextFieldSBarListener(brightnessText,brightnessSB));
      brightnessSB.setBlockIncrement(10);
      brightnessSB.addAdjustmentListener(this);

      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      gbc.fill = GridBagConstraints.BOTH;
      alphaLabel = new JLabel(getResources().getMessage("paintselector.alpha"),
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
      return hueText.requestFocusInWindow();
   }

   public void adjustmentValueChanged(AdjustmentEvent evt)
   {
      if (hueText.getInt() != (hueSB.getValue()))
      {
         hueText.setValue(hueSB.getValue());
      }

      if (saturationText.getInt() != (saturationSB.getValue()))
      {
         saturationText.setValue(saturationSB.getValue());
      }

      if (brightnessText.getInt() != (brightnessSB.getValue()))
      {
         brightnessText.setValue(brightnessSB.getValue());
      }

      if (alphaText.getInt() != (alphaSB.getValue()))
      {
         alphaText.setValue(alphaSB.getValue());
      }
   }

   public JDRPaint getPaint(CanvasGraphics cg)
   {
      return new JDRColorHSB(cg, hueSB.getValue(),
                          0.01*saturationSB.getValue(),
                          0.01*brightnessSB.getValue(),
                          0.01*alphaSB.getValue());
   }

   public void setPaint(JDRPaint paint)
   {
      JDRColorHSB c = paint.getJDRColorHSB();

      hueSB.setValue((int)Math.round(c.getHue()));
      saturationSB.setValue((int)Math.round((c.getSaturation()*100.0)));
      brightnessSB.setValue((int)Math.round((c.getBrightness()*100.0)));
      alphaSB.setValue((int)Math.round((c.getAlpha()*100.0)));
   }

   public void setPaint(Color paint)
   {
      paint.RGBtoHSB(paint.getRed(), paint.getGreen(), 
         paint.getBlue(), hsbvals);

      hueSB.setValue((int)Math.round(hsbvals[0]));
      saturationSB.setValue((int)Math.round((hsbvals[1]*100.0)));
      brightnessSB.setValue((int)Math.round((hsbvals[2]*100.0)));
      alphaSB.setValue((int)Math.round((paint.getAlpha()*100.0/255.0)));
   }

   public void setEnabled(boolean flag)
   {
      hueSB.setEnabled(flag);
      saturationSB.setEnabled(flag);
      brightnessSB.setEnabled(flag);
      alphaSB.setEnabled(flag);
      hueText.setEnabled(flag);
      saturationText.setEnabled(flag);
      brightnessText.setEnabled(flag);
      alphaText.setEnabled(flag);
      hueLabel.setEnabled(flag);
      saturationLabel.setEnabled(flag);
      brightnessLabel.setEnabled(flag);
      alphaLabel.setEnabled(flag);
   }

   public JDRResources getResources()
   {
      return resources;
   }

   private JScrollBar hueSB, saturationSB, brightnessSB, alphaSB;
   private PercentageField saturationText, brightnessText, alphaText;
   private IntRangeField hueText;
   private JLabel hueLabel, saturationLabel, brightnessLabel, alphaLabel;

   private float[] hsbvals = new float[3];

   private JDRResources resources;
}
