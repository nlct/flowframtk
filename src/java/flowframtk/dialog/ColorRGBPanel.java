// File          : ColorRGBPanel.java
// Description   : Panel for selecting RGB paint
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
 * Panel for selecting RGB paint.
 * @author Nicola L C Talbot
 */

public class ColorRGBPanel extends JPanel
   implements AdjustmentListener,SingleColourSelector
{
   public ColorRGBPanel(JDRResources resources)
   {
      this.resources = resources;
      initialise();
      addAdjustmentListener(this);
   }

   public ColorRGBPanel(JDRResources resources, AdjustmentListener al)
   {
      this.resources = resources;
      initialise();
      addAdjustmentListener(al);
      addAdjustmentListener(this);
   }

   public void addAdjustmentListener(AdjustmentListener al)
   {
      redSB.addAdjustmentListener(al);
      greenSB.addAdjustmentListener(al);
      blueSB.addAdjustmentListener(al);
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
      redLabel = new JLabel(getResources().getMessage("paintselector.red"),
                            SwingConstants.RIGHT);
      sliders.add(redLabel, gbc);

      redSB = new JScrollBar(Adjustable.HORIZONTAL, 0,0,0,100);
      redSB.setName("red");
      gbc.gridy = 0;
      gbc.gridx = 1;
      gbc.gridwidth = 4;
      gbc.gridheight = 1;
      sliders.add(redSB, gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.WEST;
      sliders.add(redText = new PercentageField(0), gbc);
      redText.getDocument().addDocumentListener(
          new TextFieldSBarListener(redText,redSB));

      redSB.setBlockIncrement(10);

      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      gbc.fill = GridBagConstraints.BOTH;
      greenLabel = new JLabel(getResources().getMessage("paintselector.green"),
                              SwingConstants.RIGHT);
      sliders.add(greenLabel, gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 3;
      greenSB = new JScrollBar(Adjustable.HORIZONTAL, 0,0,0,100);
      greenSB.setName("green");
      sliders.add(greenSB,gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      sliders.add(greenText = new PercentageField(0),gbc);
      greenText.getDocument().addDocumentListener(
          new TextFieldSBarListener(greenText,greenSB));
      greenSB.setBlockIncrement(10);

      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      blueLabel = new JLabel(getResources().getMessage("paintselector.blue"),
                             SwingConstants.RIGHT);
      sliders.add(blueLabel, gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 4;
      blueSB = new JScrollBar(Adjustable.HORIZONTAL, 0,0,0,100);
      blueSB.setName("blue");
      sliders.add(blueSB,gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      sliders.add(blueText = new PercentageField(0),gbc);
      blueText.getDocument().addDocumentListener(
          new TextFieldSBarListener(blueText,blueSB));
      blueSB.setBlockIncrement(10);

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
      alphaSB.setName("alpha");
      sliders.add(alphaSB,gbc);
      gbc.gridx = 5;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.NONE;
      sliders.add(alphaText = new PercentageField(100),gbc);
      alphaText.getDocument().addDocumentListener(
          new TextFieldSBarListener(alphaText,alphaSB));
      alphaSB.setBlockIncrement(10);
   }

   @Override
   public boolean requestDefaultColourFocus()
   {
      return redText.requestFocusInWindow();
   }

   @Override
   public void adjustmentValueChanged(AdjustmentEvent evt)
   {
      Object src = evt.getSource();

      // if text field is empty then it's likely being adjusted

      if (src == redSB)
      {
         if (!redText.getText().isEmpty()
          && redText.getInt() != (redSB.getValue()))
         {
            redText.setValue(redSB.getValue());
         }
      }
      else if (src == greenSB)
      {
         if (!greenText.getText().isEmpty()
           && greenText.getInt() != (greenSB.getValue()))
         {
            greenText.setValue(greenSB.getValue());
         }
      }
      else if (src == blueSB)
      {
         if (!blueText.getText().isEmpty()
          && blueText.getInt() != (blueSB.getValue()))
         {
            blueText.setValue(blueSB.getValue());
         }
      }
      else if (src == alphaSB)
      {
         if (!alphaText.getText().isEmpty()
            && alphaText.getInt() != (alphaSB.getValue()))
         {
            alphaText.setValue(alphaSB.getValue());
         }
      }
   }

   @Override
   public JDRPaint getPaint(CanvasGraphics cg)
   {
      return new JDRColor(cg, 0.01*redSB.getValue(),
                          0.01*greenSB.getValue(),
                          0.01*blueSB.getValue(),
                          0.01*alphaSB.getValue());
   }

   @Override
   public Color getColor()
   {
      return new Color(0.01f*redSB.getValue(),
                       0.01f*greenSB.getValue(),
                       0.01f*blueSB.getValue(),
                       0.01f*alphaSB.getValue());
   }

   @Override
   public void setPaint(Color c)
   {
      double factor = 100.0/255.0;
      redSB.setValue((int)Math.round((c.getRed()*factor)));
      greenSB.setValue((int)Math.round((c.getGreen()*factor)));
      blueSB.setValue((int)Math.round((c.getBlue()*factor)));
      alphaSB.setValue((int)Math.round((c.getAlpha()*factor)));
   }

   @Override
   public void setPaint(JDRPaint paint)
   {
      JDRColor c = paint.getJDRColor();

      redSB.setValue((int)Math.round((c.getRed()*100.0)));
      greenSB.setValue((int)Math.round((c.getGreen()*100.0)));
      blueSB.setValue((int)Math.round((c.getBlue()*100.0)));
      alphaSB.setValue((int)Math.round((c.getAlpha()*100.0)));
   }

   public void setEnabled(boolean flag)
   {
      redSB.setEnabled(flag);
      greenSB.setEnabled(flag);
      blueSB.setEnabled(flag);
      alphaSB.setEnabled(flag);
      redText.setEnabled(flag);
      greenText.setEnabled(flag);
      blueText.setEnabled(flag);
      alphaText.setEnabled(flag);
      redLabel.setEnabled(flag);
      greenLabel.setEnabled(flag);
      blueLabel.setEnabled(flag);
      alphaLabel.setEnabled(flag);
   }

   public JDRResources getResources()
   {
      return resources;
   }

   private JScrollBar redSB, greenSB, blueSB, alphaSB;
   private PercentageField redText, greenText, blueText, alphaText;
   private JLabel redLabel, greenLabel, blueLabel, alphaLabel;

   private JDRResources resources;
}
