// File          : PaintPanel.java
// Description   : Panel for selecting paint
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

/**
 * Panel for selecting paint.
 * @author Nicola L C Talbot
 */

public class PaintPanel extends JPanel 
   implements AdjustmentListener,ActionListener
{
   public PaintPanel(JDRSelector selector)
   {
      super();

      selector_    = selector;

      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      Box buttonPanel = Box.createVerticalBox();
      buttonPanel.setAlignmentY(Component.TOP_ALIGNMENT);

      cards = new JPanel(new CardLayout());
      cards.setAlignmentY(Component.TOP_ALIGNMENT);

      cards.setBorder(BorderFactory.createLoweredBevelBorder());

      ButtonGroup buttonGroup = new ButtonGroup();

      // Transparent
      transparentButton = getResources().createAppRadioButton(
         "paintselector", "transparent", buttonGroup, false, this);
      transparentButton.setAlignmentX(Component.LEFT_ALIGNMENT);

      buttonPanel.add(transparentButton);

      nonePanel = new JPanel();
      nonePanel.setAlignmentY(Component.TOP_ALIGNMENT);

      cards.add(nonePanel, NONEPANEL);

      // Single colour

      singleColourButton = getResources().createAppRadioButton
         ("paintselector", "single", buttonGroup, false, this);

      singleColourButton.setAlignmentX(Component.LEFT_ALIGNMENT);

      buttonGroup.add(singleColourButton);
      buttonPanel.add(singleColourButton);

      singleColourPanel = new ColorPanel(getResources(), this,
         selector_.getApplication().getColorChooser());

      singleColourPanel.setAlignmentY(Component.TOP_ALIGNMENT);
      singleColourPanel.setMnemonics(
         getResources().getCodePoint("paintselector.rgb.mnemonic"),
         getResources().getCodePoint("paintselector.cmyk.mnemonic"));

      JPanel singleColourCard = new JPanel();
      singleColourCard.setLayout(new BorderLayout());
      singleColourCard.add(singleColourPanel, BorderLayout.NORTH);

      cards.add(singleColourCard, COLOURPANEL);

      // Gradient Color

      gradientColourButton = getResources().createAppRadioButton(
         "paintselector", "gradient", buttonGroup, false, this);
      gradientColourButton.setAlignmentX(Component.LEFT_ALIGNMENT);

      buttonPanel.add(gradientColourButton);

      gradientPanel = new GradientPanel(selector);
      gradientPanel.setAlignmentY(Component.TOP_ALIGNMENT);

      cards.add(gradientPanel, GRADIENTPANEL);

      Dimension dim1 = singleColourPanel.getPreferredSize();
      Dimension dim2 = gradientPanel.getPreferredSize();

      dim2.height -= dim1.height;

      singleColourCard.add(Box.createRigidArea(dim2));

      add(buttonPanel);
      add(Box.createHorizontalStrut(10));

      add(new JScrollPane(cards));
   }

   public JDRPaint getPaint(CanvasGraphics cg)
   {
      JDRPaint paint;

      if (singleColourButton.isSelected())
      {
         paint = singleColourPanel.getPaint(cg);
      }
      else if (gradientColourButton.isSelected())
      {
         paint = gradientPanel.getPaint(cg);
      }
      else
      {
         paint = new JDRTransparent(cg);
      }

      return paint;
   }

   public void setPaint(JDRPaint paint)
   {
      if (paint instanceof JDRTransparent)
      {
         transparentButton.setSelected(true);
         selectTransparent();
      }
      else if (paint instanceof JDRGradient)
      {
         gradientColourButton.setSelected(true);
         selectGradientColour();
         gradientPanel.setPaint(paint);

      }
      else if (paint instanceof JDRRadial)
      {
         gradientColourButton.setSelected(true);
         selectGradientColour();
         gradientPanel.setPaint(paint);
      }
      else
      {
         singleColourPanel.setPaint(paint);
         selectSingleColour();
      }

      selector_.repaintSample();
   }

   public void selectTransparent()
   {
      if (!transparentButton.isSelected())
      {
         transparentButton.setSelected(true);
      }
      CardLayout layout = (CardLayout)cards.getLayout();
      layout.show(cards, NONEPANEL);
   }

   public void selectSingleColour()
   {
      if (!singleColourButton.isSelected())
      {
         singleColourButton.setSelected(true);
      }
      CardLayout layout = (CardLayout)cards.getLayout();
      layout.show(cards, COLOURPANEL);
   }

   public void selectGradientColour()
   {
      if (!gradientColourButton.isSelected())
      {
         gradientColourButton.setSelected(true);
      }
      CardLayout layout = (CardLayout)cards.getLayout();
      layout.show(cards, GRADIENTPANEL);
   }

   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();

      if (source == transparentButton)
      {
         selectTransparent();
      }
      else if (source == singleColourButton)
      {
         selectSingleColour();
      }
      else if (source == gradientColourButton)
      {
         selectGradientColour();
      }

      selector_.repaintSample();
   }

   public void adjustmentValueChanged(AdjustmentEvent evt)
   {
      selector_.repaintSample();
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   private JRadioButton transparentButton;
   private JRadioButton singleColourButton;
   private JRadioButton gradientColourButton;

   private ColorPanel singleColourPanel;
   private GradientPanel gradientPanel;
   private JPanel nonePanel;

   private JPanel cards;

   private static final String NONEPANEL="None Panel";
   private static final String COLOURPANEL="Colour Panel";
   private static final String GRADIENTPANEL="Gradient Panel";

//   private JButton selectSingleColourButton;
//   private SingleColourDialog singleColourDialog;
//   private ColorPanel startGradientPanel, endGradientPanel;

//   private JPanel singleColourSwatch, gradientSwatch;

//   private GradientDialog gradientDialog;
//   private JButton selectGradientButton;
/*
   private JLabel startLabel, endLabel;

   private LinearGradientDirectionPanel linearDirectionPanel;
   private RadialGradientDirectionPanel radialDirectionPanel;

   private JRadioButton linearButton, radialButton;
*/

   private JDRSelector selector_;
}
