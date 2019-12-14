// File          : FadeDialogBox.java
// Description   : Dialog box in which to specify fade value
// Date          : 2nd March 2012
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
 * Dialog box in which to specify a fade value.
 * @author Nicola L C Talbot
 */
public class FadeDialogBox extends JDialog
   implements ActionListener,ChangeListener
{
   public FadeDialogBox(FlowframTk application)
   {
      super(application, application.getResources().getString("fade.title"),
            true);
      setSize(300,110);
      application_ = application;
      setLocationRelativeTo(application_);

      slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
      slider.addChangeListener(this);

      slider.setMajorTickSpacing(10);
      slider.setPaintTicks(true);
      slider.setPaintLabels(true);

      getContentPane().add(slider, "North");

      samplePanel = new JPanel()
      {
         public void paintComponent(Graphics g)
         {
            super.paintComponent(g);

            if (object != null && slider.getValue() > 0)
            {
               object.print((Graphics2D)g);
            }
         }
      };

      samplePanel.setBackground(Color.white);

      JScrollPane sp = new JScrollPane(samplePanel);

      sp.setPreferredSize(new Dimension(300, 300));

      getContentPane().add(sp, "Center");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(this));
      p2.add(getResources().createCancelButton(this));
      p2.add(getResources().createHelpButton("sec:fade"));

      getContentPane().add(p2, "South");

      pack();
   }

   public void display()
   {
      mainPanel = application_.getCurrentFrame();
      canvas = mainPanel.getCanvas();
      selection = canvas.getSelection();

      object = null;

      for (int i = 0, n = selection.size(); i < n; i++)
      {
         JDRCompleteObject obj = selection.get(i);

         if (obj.hasShape() || obj.hasTextual())
         {
            object = obj;
            break;
         }
      }

      if (object == null)
      {
         getResources().internalError("No selected shape or textual to fade");
         return;
      }

      BBox box = selection.getBpBBox();

      samplePanel.setPreferredSize(new Dimension((int)Math.ceil(box.getMaxX()),
         (int)Math.ceil(box.getMaxY())));

      validate();

      factor = 1;

      setVisible(true);
   }

   public void okay()
   {
      canvas.fade(0.01*slider.getValue());
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
   }

   public void stateChanged(ChangeEvent e)
   {
      int value = slider.getValue();

      if (value == 0)
      {
         return;
      }

      double oldFactor = factor;
      factor = value*0.01;

      selection.fade(factor/oldFactor);

      samplePanel.repaint();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "FadeDialog:"+eol;
      str += "has focus: "+hasFocus()+eol;
      str += "slider: "+slider+eol;

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;
   private JDRCanvas canvas;
   private JSlider slider;
   private JPanel samplePanel;
   private JDRGroup selection;
   private JDRCompleteObject object;

   private double factor;

   private JDRFrame mainPanel = null;
}
