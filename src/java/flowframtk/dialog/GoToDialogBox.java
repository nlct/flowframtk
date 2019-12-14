// File          : GoToDialogBox.java
// Description   : Dialog box in which to specify new location
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
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box in which to specify a new location.
 * @author Nicola L C Talbot
 */
public class GoToDialogBox extends JDialog
   implements ActionListener
{
   public GoToDialogBox(FlowframTk application)
   {
      super(application, application.getResources().getString("goto.title"),
            true);
      application_ = application;
      setLocationRelativeTo(application_);

      locationPane = new LocationPane(getResources());

      getContentPane().add(locationPane, "Center");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(this));
      p2.add(getResources().createCancelButton(this));
      p2.add(getResources().createHelpButton("sec:goto"));

      getContentPane().add(p2, "South");

      pack();
   }

   public void display()
   {
      mainPanel = application_.getCurrentFrame();
      JDRCanvas canvas = mainPanel.getCanvas();
      Point pt = canvas.getMousePosition(true);

      CanvasGraphics cg = mainPanel.getCanvasGraphics();

      double x = 0;
      double y = 0;

      if (pt != null)
      {
         x = cg.componentXToStorage(pt.x);
         y = cg.componentYToStorage(pt.y);
      }

      locationPane.setCoords(x, y, cg.getStorageUnit(),
         mainPanel.getPaper(), mainPanel.getGrid());

      setVisible(true);
   }

   public void okay()
   {
      mainPanel.goToCoordinate(
        locationPane.getXCoord(), locationPane.getYCoord());
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

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "GoToDialog:"+eol;
      str += "has focus: "+hasFocus()+eol;

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;

   private LocationPane locationPane;

   private JDRFrame mainPanel = null;
}
