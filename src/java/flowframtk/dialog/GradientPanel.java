// File          : GradientPanel.java
// Description   : Panel for selecting gradient paint
// Creation Date : 13th August 2008
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
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Panel for selecting gradient paint.
 * @see ColorPanel
 * @author Nicola L C Talbot
 */

public class GradientPanel extends JPanel
   implements ActionListener,AdjustmentListener
{
   public GradientPanel(JDRSelector selector)
   {
      super();
      selector_ = selector;

      defaultPaint = new JDRGradient(selector.getCanvasGraphics());

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      // start colour
      JLabel startLabel = getResources().createAppLabel("colour.start");
      startLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

      add(startLabel);

      startPanel = new ColorPanel(this);

      startPanel.setMnemonics(
         getResources().getCodePoint("colour.rgb.mnemonic"),
         getResources().getCodePoint("colour.cmyk.mnemonic"));
      startPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      add(startPanel);

      // end colour
      JLabel endLabel = getResources().createAppLabel("colour.end");
      endLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

      add(endLabel);

      endPanel = new ColorPanel(this);

      endPanel.setMnemonics(
         getResources().getCodePoint("colour.rgb2.mnemonic"),
         getResources().getCodePoint("colour.cmyk2.mnemonic"));
      endPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      add(endPanel);

      // type and direction
      JPanel gradientTypePanel = new JPanel();
      gradientTypePanel.setLayout(new BoxLayout(gradientTypePanel,
         BoxLayout.LINE_AXIS));
      gradientTypePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      ButtonGroup gradientTypeGroup = new ButtonGroup();

      linearButton = getResources().createAppRadioButton("colour",
         "linear", gradientTypeGroup, true, this);
      linearButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      linearButton.setAlignmentY(Component.TOP_ALIGNMENT);
      gradientTypePanel.add(linearButton);

      linearDirectionPanel = new LinearGradientDirectionPanel(this);
      linearDirectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      linearDirectionPanel.setAlignmentY(Component.TOP_ALIGNMENT);
      gradientTypePanel.add(linearDirectionPanel);

      radialButton = getResources().createAppRadioButton("colour", "radial",
         gradientTypeGroup, false, this);
      radialButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      radialButton.setAlignmentY(Component.TOP_ALIGNMENT);
      gradientTypePanel.add(radialButton);

      radialDirectionPanel = new RadialGradientDirectionPanel(this);
      radialDirectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      radialDirectionPanel.setAlignmentY(Component.TOP_ALIGNMENT);
      gradientTypePanel.add(radialDirectionPanel);

      add(gradientTypePanel);

      setDefaults();
   }

   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();

      if (source == linearButton)
      {
         linearDirectionPanel.setEnabled(true);
         radialDirectionPanel.setEnabled(false);
      }
      else if (source == radialButton)
      {
         linearDirectionPanel.setEnabled(false);
         radialDirectionPanel.setEnabled(true);
      }

      selector_.repaintSample();
   }

   public void adjustmentValueChanged(AdjustmentEvent evt)
   {
      selector_.repaintSample();
   }

   public void setPaint(Color paint)
   {
      startPanel.setPaint(paint);
      endPanel.setPaint(paint);
   }

   public void setPaint(JDRPaint paint)
   {
      startPanel.setPaint(((JDRShading)paint).getStartColor());

      endPanel.setPaint(((JDRShading)paint).getEndColor());

      if (paint instanceof JDRGradient)
      {
         linearDirectionPanel.setDirection(
            ((JDRGradient)paint).getDirection());
         linearButton.setSelected(true);

         linearDirectionPanel.setEnabled(true);
         radialDirectionPanel.setEnabled(false);
      }
      else
      {
         radialDirectionPanel.setDirection(
            ((JDRRadial)paint).getStartLocation());
         radialButton.setSelected(true);

         linearDirectionPanel.setEnabled(false);
         radialDirectionPanel.setEnabled(true);
      }
   }

   public JDRPaint getPaint(CanvasGraphics cg)
   {
      JDRPaint paint=null;

      JDRPaint startPaint = startPanel.getPaint(cg);
      JDRPaint endPaint = endPanel.getPaint(cg);

      if (linearButton.isSelected())
      {
         paint = new JDRGradient(
            linearDirectionPanel.getDirection(),
            startPaint, endPaint);
      }
      else
      {
         paint = new JDRRadial(
            radialDirectionPanel.getDirection(),
            startPaint, endPaint);
      }

      return paint;
   }

   public void setDefaults()
   {
      setPaint(defaultPaint);
      selector_.repaint();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "GradientPanel:"+eol;
      str += "old paint: "+oldPaint+eol;

      return str+eol;
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   private JDRSelector selector_;
   private ColorPanel startPanel, endPanel;

   private JDRPaint defaultPaint;

   private JDRPaint oldPaint = defaultPaint;

   private LinearGradientDirectionPanel linearDirectionPanel;
   private RadialGradientDirectionPanel radialDirectionPanel;

   private JRadioButton linearButton, radialButton;
}
