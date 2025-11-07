// File          : FLFSetTypeblock.java
// Description   : Dialog for setting typeblock
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

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for setting typeblock.
 * @author Nicola L C Talbot
 */

public class FLFSetTypeblock extends JDialog
   implements ActionListener
{
   public FLFSetTypeblock(FlowframTk application)
   {
      super(application,
        application.getResources().getMessage("typeblock.title"),true);
      application_ = application;

      JDRResources resources = getResources();

      typeblockPanel = new TypeblockPanel(application);

      getContentPane().add(new JScrollPane(typeblockPanel), "Center");

      JPanel p2 = new JPanel();

      resources.createOkayCancelHelpButtons(this, p2, this, "sec:typeblock");

      getContentPane().add(p2, "South");

      pack();
      setLocationRelativeTo(application);
   }

   public void display(FlowFrame typeblock, JDRUnit unit)
   {
      typeblockPanel.updateComponent(typeblock, unit);
      typeblockPanel.requestDefaultComponentFocus();

      setVisible(true);
   }

   public void okay()
   {
      typeblockPanel.apply();

      setVisible(false);
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

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;
   private TypeblockPanel typeblockPanel;
}
