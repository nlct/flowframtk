// File          : ExportToPdfSettings.java
// Description   : Dialog box used to setup pdflatex
// Creation Date : 2014-05-07
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

import java.io.File;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box used to setup pdflatex setting.
 */

public class ExportToPdfSettings extends JDialog
   implements ActionListener
{
   public ExportToPdfSettings(FlowframTk application, JDRAppSelector appSelector)
   {
      super(application, 
         application.getResources().getString("appselect.setup"),true);
      this.application = application;

      Box mainPanel = Box.createVerticalBox();
      mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      JTextArea textArea = getResources().createAppInfoArea("appselect.info");
      textArea.setAlignmentX(Component.LEFT_ALIGNMENT);

      mainPanel.add(textArea);

      mainPanel.add(new JLabel(getResources().getStringWithValue(
         "appselect.query.location", "pdflatex")));

      String pdflatexApp = application.getPdfLaTeXApp();

      if (pdflatexApp == null)
      {
         File file = appSelector.findApp("pdflatex");

         if (file != null)
         {
            pdflatexApp = file.getAbsolutePath();
         }
      }

      pdflatexField = new FileField(getResources(), this, pdflatexApp, 
         appSelector.getFileChooser());
      mainPanel.add(pdflatexField);

      getContentPane().add(mainPanel, "Center");

      JPanel buttonPanel = new JPanel();
      buttonPanel.add(getResources().createOkayButton(this));
      buttonPanel.add(getResources().createCancelButton(this));

      getContentPane().add(buttonPanel, "South");
      pack();

      textArea.setMinimumSize(textArea.getPreferredSize());
      pack();
      setLocationRelativeTo(application);
   }

   public boolean display()
   {
      String pdflatexApp = pdflatexField.getFileName();

      if (pdflatexApp == null || pdflatexApp.isEmpty())
      {
         pdflatexApp = application.getPdfLaTeXApp();
      }

      if (pdflatexApp != null && !pdflatexApp.isEmpty())
      {
         pdflatexField.setFileName(pdflatexApp);
      }

      success = false;
      setVisible(true);

      return success;
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("okay"))
      {
         application.setPdfLaTeXApp(pdflatexField.getFileName());
         success = true;
         setVisible(false);
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
   private boolean success = false;

   private FileField pdflatexField;
}
