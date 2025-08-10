// File          : ShearDialogBox.java
// Description   : Dialog box for shearing objects
// Creation Date : 1st February 2006
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

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for shearing objects.
 */

public class ShearDialogBox extends JDialog
   implements ActionListener
{
   public ShearDialogBox(FlowframTk application)
   {
      super(application, 
        application.getResources().getMessage("shear.title"), true);
      application_ = application;

      int width  = 300;
      int height = 150;
      setSize(width,height);

      JPanel panel = new JPanel();

      panel.setLayout(new GridLayout(3,2));

      JLabel xLabel = getResources().createAppLabel("shear.x");
      panel.add(xLabel);

      shearXField = new DoubleField(0.0F);
      xLabel.setLabelFor(shearXField);
      panel.add(shearXField);

      JLabel yLabel = getResources().createAppLabel("shear.y");
      panel.add(yLabel);

      shearYField = new DoubleField(0.0F);
      yLabel.setLabelFor(shearYField);
      panel.add(shearYField);

      getContentPane().add(panel, "Center");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(getRootPane(), this));
      p2.add(getResources().createCancelButton(this));

      try
      {
         p2.add(getResources().createHelpDialogButton(this, "sec:shearobjects"));
      }
      catch (HelpSetNotInitialisedException e)
      {
         getResources().internalError(null, e);
      }

      getContentPane().add(p2, "South");

      setLocationRelativeTo(application);
   }

   public void display()
   {
      shearXField.requestFocusInWindow();
      setVisible(true);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

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

   public void okay()
   {
      setVisible(false);

      application_.getCurrentFrame().shearSelectedPaths(
          shearXField.getDouble(), shearYField.getDouble());
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "ShearDialogBox:"+eol;
      str += "has focus: "+hasFocus()+eol;
      str += "shear x field has focus: "+shearXField.hasFocus()+eol;
      str += "shear y field has focus: "+shearYField.hasFocus()+eol;

      ActionMap actionMap = getRootPane().getActionMap();
      str += "action map: "+eol;

      Object[] allKeys = actionMap.allKeys();

      for (int i = 0; i < allKeys.length; i++)
      {
         str += "Key: "+allKeys[i]+" Action: "+actionMap.get(allKeys[i])+eol;
      }

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private DoubleField shearXField, shearYField;
   private FlowframTk application_;
}

