// File          : RotateDialogBox.java
// Description   : Dialog box for rotating objects
// Creation Date : 1st February 2006
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
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for rotating objects.
 */

public class RotateDialogBox extends JDialog
   implements ActionListener
{
   public RotateDialogBox(FlowframTk application)
   {
      super(application,
         application.getResources().getString("rotate.title"), true);
      application_ = application;

      JDRResources resources = application.getResources();

      anglePanel = resources.createAnglePanel("rotate.rotateby");

      anglePanel.setDegrees(0);

      getContentPane().add(anglePanel, "Center");

      JPanel p2 = new JPanel();

      p2.add(resources.createOkayButton(this));
      p2.add(resources.createCancelButton(this));
      p2.add(resources.createHelpButton("sec:rotate"));

      getContentPane().add(p2, "South");

      pack();
      setLocationRelativeTo(application);
   }

   public void display()
   {
      setVisible(true);
      anglePanel.requestValueFocus();
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
      application_.getCurrentFrame()
         .rotateSelectedPaths(anglePanel.getValue());
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "RotateDialogBox:"+eol;
      str += "has focus: "+hasFocus()+eol;

      ActionMap actionMap = getRootPane().getActionMap();
      str += "action map: "+eol;

      Object[] allKeys = actionMap.allKeys();

      for (int i = 0; i < allKeys.length; i++)
      {
         str += "Key: "+allKeys[i]+" Action: "+actionMap.get(allKeys[i])+eol;
      }

      return str+eol;
   }

   private FlowframTk application_;

   private AnglePanel anglePanel;
}
