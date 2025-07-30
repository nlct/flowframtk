// File          : SetTransformDialogBox.java
// Description   : Dialog box in which to set text area transformation matrix
// Date          : 1st February 2006
// Last Modified : 9th June 2008
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
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box in which to specify a text area's transformation matrix.
 * @author Nicola L C Talbot
 */
public class SetTransformDialogBox extends JDialog
   implements ActionListener
{
   public SetTransformDialogBox(FlowframTk application)
   {
      super(application, 
         application.getResources().getMessage("settransform.title"),
            true);
      setSize(300,150);
      application_ = application;
      setLocationRelativeTo(application_);

      JPanel p1 = new JPanel();

      p1.setLayout(new GridLayout(3,3));

      field = new DoubleField[6];

      field[0] = new DoubleField(1);
      p1.add(field[0]);

      field[2] = new DoubleField(0);
      p1.add(field[2]);

      field[4] = new DoubleField(0);
      p1.add(field[4]);

      field[1] = new DoubleField(0);
      p1.add(field[1]);

      field[3] = new DoubleField(1);
      p1.add(field[3]);

      field[5] = new DoubleField(0);
      p1.add(field[5]);

      p1.add(new JLabel("0"));

      p1.add(new JLabel("0"));

      p1.add(new JLabel("1"));

      for (int i = 0; i < 6; i++)
      {
         field[i].setHorizontalAlignment(JTextField.LEFT);
      }

      getContentPane().add(p1, "Center");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(getRootPane(), this));
      p2.add(getResources().createCancelButton(this));
      p2.add(getResources().createHelpDialogButton(this, "sec:textmatrix"));

      getContentPane().add(p2, "South");
   }

   public void initialise()
   {
      mainPanel = application_.getCurrentFrame();
      JDRCanvas canvas = mainPanel.getCanvas();

      JDRTextual text = canvas.getSelectedTextual();

      if (text != null)
      {
         double[] matrix = text.getTransformation(null);

         for (int i = 0; i < 6; i++)
         {
            field[i].setValue(matrix[i]);
            field[i].setCaretPosition(0);
         }

         field[0].requestFocusInWindow();
         setVisible(true);
      }
   }

   public void okay()
   {
      mainPanel = application_.getCurrentFrame();
      JDRCanvas canvas = mainPanel.getCanvas();

      double[] matrix = new double[6];

      for (int i = 0; i < 6; i++)
      {
         matrix[i] = field[i].getDouble();
      }

      canvas.setSelectedTextTransform(matrix);
      setVisible(false);
   }

   public void actionPerformed(ActionEvent e)
   {
      String action = e.getActionCommand();

      if (action.equals("okay"))
      {
         okay();
      } 
      else if (action.equals("cancel"))
      {
         setVisible(false);
      }
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "SetTransformDialogBox:"+eol;
      str += "has focus: "+hasFocus()+eol;

      for (int i = 0; i < field.length; i++)
      {
         str += "field["+i+"] has focus: "+field[i].hasFocus()+eol;
      }

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;
   private DoubleField[] field;

   private JDRFrame mainPanel = null;
}
