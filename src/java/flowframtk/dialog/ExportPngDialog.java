// File          : ExportPngDialog.java
// Description   : Dialog box for specifying export to png options
// Creation Date : 2015-10-16
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2015-2025 Nicola L.C. Talbot

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

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

public class ExportPngDialog extends JDialog
   implements ActionListener
{
   public ExportPngDialog(FlowframTk application)
   {
      super(application, 
            application.getResources().getMessage("exportpng.title"),
            true);
      this.application = application;

      JComponent mainPanel = Box.createVerticalBox();

      useAlphaBox = getResources().createAppCheckBox("exportpng",
        "usealpha", false, null);
      mainPanel.add(useAlphaBox);

      encapBox = getResources().createAppCheckBox("exportpng", "crop",
        true, null);
      mainPanel.add(encapBox);

      getContentPane().add(mainPanel, "Center");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(getRootPane(), this));
      p2.add(getResources().createCancelButton(this));

      try
      {
         p2.add(getResources().createHelpDialogButton(this, "sec:exportpng"));
      }
      catch (HelpSetNotInitialisedException e)
      {
         getResources().internalError(null, e);
      }

      getContentPane().add(p2, "South");

      pack();
      setLocationRelativeTo(application);
   }

   public boolean display()
   {
      success = false;
      useAlphaBox.setSelected(application.getSettings().useExportPngAlpha());
      encapBox.setSelected(application.getSettings().isExportPngEncap());

      setVisible(true);

      return success;
   }

   public void okay()
   {
      application.getSettings().setExportPngAlpha(useAlphaBox.isSelected());
      application.getSettings().setExportPngEncap(encapBox.isSelected());

      success = true;

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

   public JDRResources getResources()
   {
      return application.getResources();
   }

   private FlowframTk application;

   private JCheckBox useAlphaBox, encapBox;

   private boolean success;
}
