// File          : VectorizeBitmapDialog.java
// Description   : Dialog box for vectorizing a bitmap
// Date          : 25th May 2011
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
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for scanning a bitmap.
 * @author Nicola L C Talbot
 */
public class VectorizeBitmapDialog extends JDialog
   implements ActionListener
{
   public VectorizeBitmapDialog(FlowframTk application)
   {
      super(application, application.getResources().getString("vectorize.title"),
            true);
      application_ = application;

      Box p = Box.createVerticalBox();

      JPanel incPanel = new JPanel();

      xIncPanel = getResources().createNonNegativeIntPanel("vectorize.xinc", 2);
      xIncPanel.add(Box.createHorizontalGlue());
      incPanel.add(xIncPanel);

      yIncPanel = getResources().createNonNegativeIntPanel("vectorize.yinc", 2);
      yIncPanel.add(Box.createHorizontalGlue());
      incPanel.add(yIncPanel);

      p.add(incPanel);

      JComponent straightenPanel = Box.createVerticalBox();

      straightenPanel.setBorder(BorderFactory.createEtchedBorder());

      straightenBox = getResources().createAppCheckBox("vectorize", 
        "straighten", false, this);

      straightenPanel.add(straightenBox);

      tolerance = getResources().createNonNegativeDoublePanel(
         "vectorize.tolerance", 1.0);

      straightenPanel.add(tolerance);

      p.add(straightenPanel);

      JComponent smoothPanel = Box.createVerticalBox();
      smoothPanel.setBorder(BorderFactory.createEtchedBorder());

      smoothBox = getResources().createAppCheckBox("vectorize",
         "smooth", false, this);

      smoothPanel.add(smoothBox);

      JComponent smoothParametersPanel = new JPanel();

      smoothParametersPanel.setLayout(new GridLayout(5, 2));

      chi = getResources().createNonNegativeDoublePanel("vectorize.chi", 2.0);

      smoothParametersPanel.add(chi);

      delta = getResources().createNonNegativeDoublePanel("vectorize.delta", 0.01);

      smoothParametersPanel.add(delta);

      gamma = getResources().createNonNegativeDoublePanel("vectorize.gamma", 0.5);

      smoothParametersPanel.add(gamma);

      rho = getResources().createNonNegativeDoublePanel("vectorize.rho", 1.0);

      smoothParametersPanel.add(rho);

      sigma = getResources().createNonNegativeDoublePanel("vectorize.sigma", 0.5);

      smoothParametersPanel.add(sigma);

      smoothTolFun = getResources().createNonNegativeDoublePanel(
         "vectorize.smoothTolFun", 1e-6);

      smoothParametersPanel.add(smoothTolFun);

      smoothTol = getResources().createNonNegativeDoublePanel(
         "vectorize.smoothTol", 1e-6);

      smoothParametersPanel.add(smoothTol);

      maxIter = getResources().createNonNegativeIntPanel("vectorize.maxIter", 200);

      smoothParametersPanel.add(maxIter);

      maxFunEvals = getResources().createNonNegativeIntPanel(
         "vectorize.maxFunEvals", 1000);

      smoothParametersPanel.add(maxFunEvals);

      flatness = getResources().createNonNegativeDoublePanel("vectorize.flatness", 1.0);

      smoothParametersPanel.add(flatness);

      smoothPanel.add(smoothParametersPanel);

      p.add(smoothPanel);

      JPanel colPanel = new JPanel();

      colPanel.add(getResources().createAppLabel("vectorize.base"));

      base = Color.black;

      swatch = new JPanel()
      {
         public Dimension getPreferredSize()
         {
            return new Dimension(40,20);
         }

         public Color getBackground()
         {
            return base;
         }
      };

      colPanel.add(swatch);

      JButton colorChooserButton = getResources().createAppJButton(
         "vectorize", "selectbase", this);

      colPanel.add(colorChooserButton);

      p.add(colPanel);

      colorChooser = new JColorChooser(swatch.getBackground());

      colorChooserDialog = JColorChooser.createDialog(application,
         getResources().getString("vectorize.selectbase.title"),
         true,
         colorChooser,
         new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               base = colorChooser.getColor();
               swatch.repaint();
            }
         },
         new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
            }
         }
      );

      getContentPane().add(p, "Center");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(this));
      p2.add(getResources().createCancelButton(this));

      getContentPane().add(p2, "South");
      pack();
      setLocationRelativeTo(application);
   }

   public void display()
   {
      mainPanel = application_.getCurrentFrame();
      bitmap = mainPanel.getSelectedBitmap();
      setVisible(true);
   }

   private void setStraighten(boolean flag)
   {
      if (straightenBox.isSelected() != flag)
      {
         straightenBox.setSelected(flag);
      }

      tolerance.setEnabled(flag);
   }

   private void setSmooth(boolean flag)
   {
      if (smoothBox.isSelected() != flag)
      {
         smoothBox.setSelected(flag);
      }

      chi.setEnabled(flag);
      delta.setEnabled(flag);
      gamma.setEnabled(flag);
      rho.setEnabled(flag);
      sigma.setEnabled(flag);
      smoothTolFun.setEnabled(flag);
      smoothTol.setEnabled(flag);
      maxIter.setEnabled(flag);
      maxFunEvals.setEnabled(flag);
      flatness.setEnabled(flag);
   }

   public int getXInc()
   {
      return xIncPanel.getValue();
   }

   public int getYInc()
   {
      return yIncPanel.getValue();
   }

   public Color getBase()
   {
      return base;
   }

   public double getStraightenTolerance()
   {
      return tolerance.getValue();
   }

   public double getChi()
   {
      return chi.getValue();
   }

   public double getDelta()
   {
      return delta.getValue();
   }

   public double getGamma()
   {
      return gamma.getValue();
   }

   public double getRho()
   {
      return rho.getValue();
   }

   public double getSigma()
   {
      return sigma.getValue();
   }

   public double getSmoothTolFun()
   {
      return smoothTolFun.getValue();
   }

   public double getSmoothTol()
   {
      return smoothTol.getValue();
   }

   public int getMaxIter()
   {
      return maxIter.getValue();
   }

   public int getMaxFunEvals()
   {
      return maxFunEvals.getValue();
   }

   public double getFlatness()
   {
      return flatness.getValue();
   }

   public boolean isStraightenSelected()
   {
      return straightenBox.isSelected();
   }

   public boolean isSmoothSelected()
   {
      return smoothBox.isSelected();
   }

   public void okay()
   {
      ScanImage scanImage = new ScanImage(mainPanel, bitmap,
         this);
      scanImage.performScan();
      setVisible(false);
   }

   public void actionPerformed(ActionEvent e)
   {
      String action = e.getActionCommand();

      if (action == null) return;

      if (action.equals("okay"))
      {
         okay();
      } 
      else if (action.equals("cancel"))
      {
         setVisible(false);
      }
      else if (action.equals("selectbase"))
      {
         colorChooserDialog.setVisible(true);
      }
      else if (action.equals("straighten"))
      {
         setStraighten(straightenBox.isSelected());
      }
      else if (action.equals("smooth"))
      {
         setSmooth(smoothBox.isSelected());
      }
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;
   private JDRFrame mainPanel;
   private JDRBitmap bitmap;
   private Color base;
   private JDialog colorChooserDialog;
   private JColorChooser colorChooser;

   private NonNegativeIntPanel xIncPanel, yIncPanel;
   private NonNegativeDoublePanel tolerance;
   private JCheckBox straightenBox, smoothBox;
   private NonNegativeDoublePanel chi, delta, gamma, rho, sigma,
      smoothTolFun, smoothTol, flatness;
   private NonNegativeIntPanel maxIter, maxFunEvals;

   private Component swatch;
}
