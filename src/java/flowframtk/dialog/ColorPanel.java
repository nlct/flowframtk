// File          : ColorPanel.java
// Description   : Panel for selecting non-gradient paint
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

/**
 * Panel for selecting non-gradient paint.
 * @author Nicola L C Talbot
 */

public class ColorPanel extends JPanel
   implements ActionListener,ChangeListener
{
   public ColorPanel(JDRResources resources)
   {
      this.resources = resources;
      rgbPanel = new ColorRGBPanel(resources);
      cmykPanel = new ColorCMYKPanel(resources);
      hsbPanel = new ColorHSBPanel(resources);
      greyPanel = new GreyPanel(resources);
      initialise();
   }

   public ColorPanel(GradientPanel gradientPanel)
   {
      this.resources = gradientPanel.getResources();
      rgbPanel = new ColorRGBPanel(resources, gradientPanel);
      cmykPanel = new ColorCMYKPanel(resources, gradientPanel);
      hsbPanel = new ColorHSBPanel(resources, gradientPanel);
      greyPanel = new GreyPanel(resources, gradientPanel);
      initialise();
   }
   public ColorPanel(JDRResources resources, AdjustmentListener al)
   {
      this.resources = resources;
      rgbPanel = new ColorRGBPanel(resources, al);
      cmykPanel = new ColorCMYKPanel(resources, al);
      hsbPanel = new ColorHSBPanel(resources, al);
      greyPanel = new GreyPanel(resources, al);
      initialise();
   }

   public void addAdjustmentListener(AdjustmentListener al)
   {
      rgbPanel.addAdjustmentListener(al);
      cmykPanel.addAdjustmentListener(al);
      hsbPanel.addAdjustmentListener(al);
      greyPanel.addAdjustmentListener(al);
   }

   public void initialise()
   {
      setLayout(new GridBagLayout());

      GridBagConstraints constraints = new GridBagConstraints();

      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.gridwidth = 2;
      constraints.gridheight = 1;
      constraints.weightx = 0;
      constraints.weighty = 100;
      constraints.anchor= GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;

      tabbedPane = new JTabbedPane();
      tabbedPane.setTabPlacement(JTabbedPane.RIGHT);
      tabbedPane.setAlignmentY(Component.TOP_ALIGNMENT);
      add(tabbedPane, constraints);

      // RGB selector panel

      tabbedPane.addTab(getResources().getString("colour.rgb"),
                        null, rgbPanel,
                        getResources().getString("tooltip.rgb"));
      tabbedPane.addChangeListener(this);
      currentPanel = rgbPanel;

      // CMYK selector panel

      tabbedPane.addTab(getResources().getString("colour.cmyk"),
                        null, cmykPanel,
                        getResources().getString("tooltip.cmyk"));

      // HSB selector panel

      tabbedPane.addTab(getResources().getString("colour.hsb"),
                        null, hsbPanel,
                        getResources().getString("tooltip.hsb"));

      // Grey selector panel

      tabbedPane.addTab(getResources().getString("colour.grey"),
                        null, greyPanel,
                        getResources().getString("tooltip.grey"));

      // predefined colour panel
      constraints.gridx = 2;
      constraints.gridy = 0;
      constraints.gridwidth = 1;
      constraints.gridheight = 1;
      constraints.weightx = 0;
      constraints.anchor= GridBagConstraints.EAST;
      predefinedPanel  = new JPanel();
      predefinedPanel.setAlignmentY(Component.TOP_ALIGNMENT);
      add(predefinedPanel, constraints);
      predefinedPanel.setLayout(new GridLayout(4,5));
      predefinedButtons = new ColorButton[MAX_PREDEFINED];
      predefined_n = 0;

      addPredefinedColor(Color.black,
         getResources().getString("tooltip.swatch.black"));
      addPredefinedColor(Color.darkGray,
         getResources().getString("tooltip.swatch.darkgrey"));
      addPredefinedColor(Color.gray,
         getResources().getString("tooltip.swatch.grey"));
      addPredefinedColor(Color.lightGray,
         getResources().getString("tooltip.swatch.lightgrey"));
      addPredefinedColor(Color.white,
         getResources().getString("tooltip.swatch.white"));

      addPredefinedColor(new Color(0,0,128),
         getResources().getString("tooltip.swatch.darkblue"));
      addPredefinedColor(Color.blue,
         getResources().getString("tooltip.swatch.blue"));
      addPredefinedColor(Color.cyan,
         getResources().getString("tooltip.swatch.cyan"));
      addPredefinedColor(Color.green,
         getResources().getString("tooltip.swatch.green"));
      addPredefinedColor(new Color(0,128,0),
         getResources().getString("tooltip.swatch.darkgreen"));

      addPredefinedColor(Color.yellow,
         getResources().getString("tooltip.swatch.yellow"));
      addPredefinedColor(cmyk(0.0,0.1,0.84,0),
         getResources().getString("tooltip.swatch.goldenrod"));
      addPredefinedColor(Color.orange,
         getResources().getString("tooltip.swatch.orange"));
      addPredefinedColor(cmyk(0.14,0.42,0.56,0.0),
         getResources().getString("tooltip.swatch.tan"));
      addPredefinedColor(cmyk(0.0,0.72,1.0,0.45),
         getResources().getString("tooltip.swatch.rawsienna"));

      addPredefinedColor(Color.red,
         getResources().getString("tooltip.swatch.red"));
      addPredefinedColor(new Color(128,0,0),
         getResources().getString("tooltip.swatch.darkred"));
      addPredefinedColor(cmyk(0.32,0.64,0.0,0.0),
         getResources().getString("tooltip.swatch.orchid"));
      addPredefinedColor(Color.magenta,
         getResources().getString("tooltip.swatch.magenta"));
      addPredefinedColor(Color.pink,
         getResources().getString("tooltip.swatch.pink"));
   }

   private Color cmyk(double cyan, double magenta, double yellow,
      double key)
   {
      double red   = 1.0-Math.min(1.0,cyan*(1-key)+key);
      double green = 1.0-Math.min(1.0,magenta*(1-key)+key);
      double blue  = 1.0-Math.min(1.0,yellow*(1-key)+key);

      return new Color((float)red, (float)green, (float)blue);
   }

   public void addPredefinedColor(Color c, String name)
   {
      predefinedButtons[predefined_n] = new ColorButton(c);
      predefinedPanel.add(predefinedButtons[predefined_n]);
      predefinedButtons[predefined_n].addActionListener(this);
      predefinedButtons[predefined_n].setToolTipText(name);
      predefined_n++;
   }

   public void setMnemonics(char rgbMnemonic, char cmykMnemonic)
   {
      // set mnemonics
      tabbedPane.setMnemonicAt(0, rgbMnemonic);
      tabbedPane.setMnemonicAt(1, cmykMnemonic);
   }

   public void setMnemonics(char rgbMnemonic, char cmykMnemonic,
      char hsbMnemonic, char greyMnemonic)
   {
      // set mnemonics
      tabbedPane.setMnemonicAt(0, rgbMnemonic);
      tabbedPane.setMnemonicAt(1, cmykMnemonic);
      tabbedPane.setMnemonicAt(2, hsbMnemonic);
      tabbedPane.setMnemonicAt(3, greyMnemonic);
   }

   public boolean requestDefaultColourFocus()
   {
      currentPanel = (SingleColourSelector)
         tabbedPane.getSelectedComponent();

      return currentPanel.requestDefaultColourFocus();
   }

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource();

      if (source instanceof ColorButton)
      {
         currentPanel = (SingleColourSelector)
            tabbedPane.getSelectedComponent();

         currentPanel.setPaint(((ColorButton)source).getBackground());
      }
   }

   public void stateChanged(ChangeEvent e)
   {
      JDRPaint paint = currentPanel.getPaint(null);

      currentPanel = (SingleColourSelector)
         tabbedPane.getSelectedComponent();

      currentPanel.setPaint(paint);
      currentPanel.requestDefaultColourFocus();
   }

   public JDRPaint getPaint(CanvasGraphics cg)
   {
      currentPanel = (SingleColourSelector)
         tabbedPane.getSelectedComponent();

      return currentPanel.getPaint(cg);
   }

   public void setPaint(Color paint)
   {
      currentPanel.setPaint(paint);
   }

   public void setPaint(JDRPaint paint)
   {
      if (paint instanceof JDRColor)
      {
         tabbedPane.setSelectedComponent(rgbPanel);
         currentPanel = rgbPanel;
      }
      else if (paint instanceof JDRColorCMYK)
      {
         tabbedPane.setSelectedComponent(cmykPanel);
         currentPanel = cmykPanel;
      }
      else if (paint instanceof JDRColorHSB)
      {
         tabbedPane.setSelectedComponent(hsbPanel);
         currentPanel = hsbPanel;
      }
      else if (paint instanceof JDRGray)
      {
         tabbedPane.setSelectedComponent(greyPanel);
         currentPanel = greyPanel;
      }
      else if (paint instanceof JDRShading)
      {
         setPaint(((JDRShading)paint).getStartColor());
         return;
      }
      else
      {
         setPaint(paint.getJDRColor());
         return;
      }

      currentPanel.setPaint(paint);
   }

   public void setEnabled(boolean flag)
   {
      rgbPanel.setEnabled(flag);
      cmykPanel.setEnabled(flag);
      hsbPanel.setEnabled(flag);
      greyPanel.setEnabled(flag);
      tabbedPane.setEnabled(flag);

      for (int i = 0; i < predefined_n; i++)
      {
         predefinedButtons[i].setVisible(flag);
      }
   }

   public JDRResources getResources()
   {
      return resources;
   }

   private ColorRGBPanel rgbPanel;
   private ColorCMYKPanel cmykPanel;
   private ColorHSBPanel hsbPanel;
   private GreyPanel greyPanel;

   private JTabbedPane tabbedPane;

   private SingleColourSelector currentPanel;

   // predefined colour buttons
   private ColorButton[] predefinedButtons;
   private static final int MAX_PREDEFINED=20; 

   private JPanel predefinedPanel;
   private int predefined_n=0;

   private JDRResources resources;
}
