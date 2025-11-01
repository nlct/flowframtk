// File          : ColorCMYKPanel.java
// Description   : Panel for selecting CMYK paint
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
 * Panel for selecting CMYK paint.
 * @author Nicola L C Talbot
 */

public class ColorCMYKPanel extends JPanel
   implements AdjustmentListener,SingleColourSelector
{
   public ColorCMYKPanel(JDRResources resources)
   {
      this.resources = resources;
      initialise();
   }

   public ColorCMYKPanel(JDRResources resources, AdjustmentListener al)
   {
      this.resources = resources;
      initialise();
      addAdjustmentListener(al);
   }

   public void addAdjustmentListener(AdjustmentListener al)
   {
      cyanSB.addAdjustmentListener(al);
      magentaSB.addAdjustmentListener(al);
      yellowSB.addAdjustmentListener(al);
      blackSB.addAdjustmentListener(al);
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
      cyanLabel = new JLabel(getResources().getMessage("paintselector.cyan"),
                             SwingConstants.RIGHT);
      sliders.add(cyanLabel, gbc);

      cyanSB = new JScrollBar(Adjustable.HORIZONTAL, 0,0,0,100);
      gbc.gridy = 0;
      gbc.gridx = 1;
      gbc.gridwidth = 4;
      gbc.gridheight = 1;
      sliders.add(cyanSB, gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.WEST;
      sliders.add(cyanText = new PercentageField(0), gbc);
      cyanText.getDocument().addDocumentListener(
          new TextFieldSBarListener(cyanText, cyanSB));
      cyanSB.setBlockIncrement(10);
      cyanSB.addAdjustmentListener(this);

      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      gbc.fill = GridBagConstraints.BOTH;
      magentaLabel = new JLabel(getResources().getMessage("paintselector.magenta"),
                                SwingConstants.RIGHT);
      sliders.add(magentaLabel,gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 3;
      magentaSB = new JScrollBar(Adjustable.HORIZONTAL, 0,0,0,100);
      sliders.add(magentaSB,gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      sliders.add(magentaText = new PercentageField(0),gbc);
      magentaText.getDocument().addDocumentListener(
          new TextFieldSBarListener(magentaText,magentaSB));
      magentaSB.setBlockIncrement(10);
      magentaSB.addAdjustmentListener(this);

      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      yellowLabel = new JLabel(getResources().getMessage("paintselector.yellow"),
                               SwingConstants.RIGHT);
      sliders.add(yellowLabel, gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 4;
      yellowSB = new JScrollBar(Adjustable.HORIZONTAL, 0,0,0,100);
      sliders.add(yellowSB, gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      sliders.add(yellowText = new PercentageField(0),gbc);
      yellowText.getDocument().addDocumentListener(
          new TextFieldSBarListener(yellowText,yellowSB));
      yellowSB.setBlockIncrement(10);
      yellowSB.addAdjustmentListener(this);

      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      gbc.fill = GridBagConstraints.BOTH;
      blackLabel = new JLabel(getResources().getMessage("paintselector.black"),
                              SwingConstants.RIGHT);
      sliders.add(blackLabel, gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 4;
      blackSB = new JScrollBar(Adjustable.HORIZONTAL,0,0,0,100);
      sliders.add(blackSB,gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      sliders.add(blackText = new PercentageField(0),gbc);
      blackText.getDocument().addDocumentListener(
          new TextFieldSBarListener(blackText,blackSB));
      blackSB.setBlockIncrement(10);
      blackSB.addAdjustmentListener(this);

      gbc.gridx = 0;
      gbc.gridy = 4;
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
          new TextFieldSBarListener(alphaText, alphaSB));
      alphaSB.setBlockIncrement(10);
      alphaSB.addAdjustmentListener(this);
   }

   @Override
   public boolean requestDefaultColourFocus()
   {
      return cyanText.requestFocusInWindow();
   }

   public void adjustmentValueChanged(AdjustmentEvent evt)
   {
      if (cyanText.getInt() != (cyanSB.getValue()))
      {
         cyanText.setValue(cyanSB.getValue());
      }

      if (magentaText.getInt() != (magentaSB.getValue()))
      {
         magentaText.setValue(magentaSB.getValue());
      }

      if (yellowText.getInt() != (yellowSB.getValue()))
      {
         yellowText.setValue(yellowSB.getValue());
      }

      if (blackText.getInt() != (blackSB.getValue()))
      {
         blackText.setValue(blackSB.getValue());
      }

      if (alphaText.getInt() != (alphaSB.getValue()))
      {
         alphaText.setValue(alphaSB.getValue());
      }
   }

   @Override
   public JDRPaint getPaint(CanvasGraphics cg)
   {
      return new JDRColorCMYK(cg, 0.01*cyanText.getInt(),
                          0.01*magentaText.getInt(),
                          0.01*yellowText.getInt(),
                          0.01*blackText.getInt(),
                          0.01*alphaText.getInt());
   }

   @Override
   public Color getColor()
   {
      double cyan    = 0.01*cyanText.getInt();
      double magenta = 0.01*magentaText.getInt();
      double yellow  = 0.01*yellowText.getInt();
      double key     = 0.01*blackText.getInt();

      double red   = 1.0-Math.min(1.0,cyan*(1-key)+key);
      double green = 1.0-Math.min(1.0,magenta*(1-key)+key);
      double blue  = 1.0-Math.min(1.0,yellow*(1-key)+key);

      return new Color((float)red, (float)green, (float)blue, 0.01f*alphaText.getInt());
   }

   @Override
   public void setPaint(JDRPaint paint)
   {
      JDRColorCMYK c = paint.getJDRColorCMYK();

      cyanSB.setValue((int)Math.round(100.0*c.getCyan()));
      magentaSB.setValue((int)Math.round(100.0*c.getMagenta()));
      yellowSB.setValue((int)Math.round(100.0*c.getYellow()));
      blackSB.setValue((int)Math.round(100.0*c.getKey()));
      alphaSB.setValue((int)Math.round(100.0*c.getAlpha()));
   }

   @Override
   public void setPaint(Color paint)
   {
      double red = paint.getRed()/255.0;
      double green = paint.getGreen()/255.0;
      double blue = paint.getBlue()/255.0;

      double black   = Math.min(1.0-red,
                       Math.min(1.0-green,1.0-blue));
      double cyan    = 0;
      double magenta = 0;
      double yellow  = 0;

      if (black < 1)
      {
         cyan    = (1.0-red-black)/(1.0-black);
         magenta = (1.0-green-black)/(1.0-black);
         yellow  = (1.0-blue-black)/(1.0-black);
      }

      cyanSB.setValue((int)Math.round(100.0*cyan));
      magentaSB.setValue((int)Math.round(100.0*magenta));
      yellowSB.setValue((int)Math.round(100.0*yellow));
      blackSB.setValue((int)Math.round(100.0*black));
      alphaSB.setValue((int)Math.round(100.0*paint.getAlpha()/255.0));
   }

   public void setEnabled(boolean flag)
   {
      cyanSB.setEnabled(flag);
      magentaSB.setEnabled(flag);
      yellowSB.setEnabled(flag);
      blackSB.setEnabled(flag);
      alphaSB.setEnabled(flag);
      cyanText.setEnabled(flag);
      magentaText.setEnabled(flag);
      yellowText.setEnabled(flag);
      blackText.setEnabled(flag);
      alphaText.setEnabled(flag);
      cyanLabel.setEnabled(flag);
      magentaLabel.setEnabled(flag);
      yellowLabel.setEnabled(flag);
      blackLabel.setEnabled(flag);
      alphaLabel.setEnabled(flag);
   }

   public JDRResources getResources()
   {
      return resources;
   }

   private JScrollBar cyanSB, magentaSB, yellowSB, blackSB, alphaSB;
   private PercentageField cyanText, magentaText, 
                               yellowText, blackText, alphaText;
   private JLabel cyanLabel, magentaLabel, yellowLabel, blackLabel,
                  alphaLabel;

   private JDRResources resources;
}
