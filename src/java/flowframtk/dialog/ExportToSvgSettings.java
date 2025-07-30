// File          : ExportToSvgSettings.java
// Description   : Dialog box used to setup latex + dvisvgm paths
// Creation Date : 2014-05-07
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2014-2025 Nicola L.C. Talbot

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
import java.io.FilenameFilter;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box used to setup latex + dvisvgm settings.
 */

public class ExportToSvgSettings extends JDialog
   implements ActionListener
{
   public ExportToSvgSettings(FlowframTk application, JDRAppSelector appSelector)
   {
      super(application, 
         application.getResources().getMessage("appselect.setup"),true);
      this.application = application;

      Box mainPanel = Box.createVerticalBox();
      mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      JTextArea textArea = 
         getResources().createAppInfoArea("appselect.info");
      textArea.setAlignmentX(Component.LEFT_ALIGNMENT);

      mainPanel.add(textArea);

      mainPanel.add(new JLabel(getResources().getMessage(
         "appselect.query.location", "latex")));

      String latexApp = application.getLaTeXApp();

      if (latexApp == null)
      {
         File file = appSelector.findApp("latex");

         if (file != null)
         {
            latexApp = file.getAbsolutePath();
         }
      }

      latexField = new FileField(getResources(), this, latexApp, 
         appSelector.getFileChooser());
      mainPanel.add(latexField);

      mainPanel.add(new JLabel(getResources().getMessage(
         "appselect.query.location", "dvisvgm")));

      String dvisvgmApp = application.getDvisvgmApp();

      if (dvisvgmApp == null)
      {
         File file = appSelector.findApp("dvisvgm");

         if (file != null)
         {
            dvisvgmApp = file.getAbsolutePath();
         }
      }

      dvisvgmField = new FileField(getResources(), this, dvisvgmApp, 
         appSelector.getFileChooser());
      mainPanel.add(dvisvgmField);

      JTextArea libgsArea = 
         getResources().createAppInfoArea("appselect.libgs");
      libgsArea.setAlignmentX(Component.LEFT_ALIGNMENT);

      mainPanel.add(libgsArea);

      String libGs = application.getLibgs();

      if (libGs == null)
      {
         libGs = getDefaultLibgs(appSelector);
      }

      libgsField = new FileField(getResources(), this, libGs, 
         appSelector.getFileChooser());
      mainPanel.add(libgsField);

      getContentPane().add(mainPanel, "Center");

      JPanel buttonPanel = new JPanel();
      buttonPanel.add(getResources().createOkayButton(getRootPane(), this));
      buttonPanel.add(getResources().createCancelButton(this));

      getContentPane().add(buttonPanel, "South");

      pack();
      textArea.setMinimumSize(textArea.getPreferredSize());
      libgsArea.setMinimumSize(libgsArea.getPreferredSize());
      pack();
      setLocationRelativeTo(application);
   }

   public boolean display()
   {
      String latexApp = latexField.getFileName();

      if (latexApp == null || latexApp.isEmpty())
      {
         latexApp = application.getLaTeXApp();
      }

      if (latexApp != null && !latexApp.isEmpty())
      {
         latexField.setFileName(latexApp);
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
         application.setLaTeXApp(latexField.getFileName());
         application.setDvisvgmApp(dvisvgmField.getFileName());
         application.setLibgs(libgsField.getFileName());
         success = true;
         setVisible(false);
      }
      else if (action.equals("cancel"))
      {
         setVisible(false);
      }
   }

   public static String getDefaultLibgs(JDRAppSelector appSelector)
   {
      String libGs = System.getenv("LIBGS");

      if (libGs == null)
      {
         if (!System.getProperty("os.name").toLowerCase().contains("win"))
         {
            libGs = findGsLib();
         }

         if (libGs == null)
         {
            File file = appSelector.findApp("libgs.so", "gsdll32.dll", 
               "gsdll64.dll");

            if (file != null)
            {
               libGs = file.getAbsolutePath();
            }
         }
      }

      return libGs;
   }

   public static String findGsLib()
   {
      File dir = new File("/usr/lib");

      FilenameFilter filter = new FilenameFilter()
       {
          public boolean accept(File dir, String name)
          {
             return name.startsWith("libgs.so");
          }
       
       };

      String[] list = dir.list(filter);

      if (list != null && list.length > 0)
      {
         return list[0];
      }

      dir = new File("/usr/lib64");

      list = dir.list(filter);

      if (list != null && list.length > 0)
      {
         return list[0];
      }

      dir = new File("/usr/local/lib");

      list = dir.list(filter);

      if (list != null && list.length > 0)
      {
         return list[0];
      }

      dir = new File("/usr/local/lib64");

      list = dir.list(filter);

      if (list != null && list.length > 0)
      {
         return list[0];
      }

      return null;
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   private FlowframTk application;
   private boolean success = false;

   private FileField latexField;
   private FileField dvisvgmField;
   private FileField libgsField;
}
