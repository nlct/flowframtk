// File          : StorageUnitDialog.java
// Description   : Dialog for setting the storage unit
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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.*;

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.*;

public class StorageUnitDialog extends JDialog
   implements ActionListener
{
   public StorageUnitDialog(FlowframTk application)
   {
      super(application,
            application.getResources().getMessage("storage_unit.title"),
            true);
      application_ = application;

      storageUnitPanel = new StorageUnitPanel(getResources());
      storageUnitPanel.setBorder(BorderFactory.createLoweredBevelBorder());

      getContentPane().add(storageUnitPanel, "Center");

      // OK/Cancel Button panel

      JPanel p = new JPanel();
      getContentPane().add(p, "South");

      p.add(getResources().createOkayButton(getRootPane(), this));
      p.add(getResources().createCancelButton(this));

      try
      {
         p.add(getResources().createHelpDialogButton(this, "sec:controlsettings"));
      }
      catch (HelpSetNotInitialisedException e)
      {
         getResources().debugMessage(e);
      }

      pack();
      setLocationRelativeTo(application_);
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

   public void display()
   {
      JDRFrame frame = application_.getCurrentFrame();

      CanvasGraphics cg;

      if (frame == null)
      {
         cg = application_.getDefaultCanvasGraphics();
      }
      else
      {
         cg = frame.getCanvasGraphics();
      }
      storageUnitPanel.initialise(application_, cg);

      setVisible(true);
   }

   public void okay()
   {
      storageUnitPanel.okay(application_);

      application_.repaint();

      setVisible(false);
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private StorageUnitPanel storageUnitPanel;

   private FlowframTk application_;
}
