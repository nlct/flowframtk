// File          : BitmapProperties.java
// Description   : Dialog to set bitmap properties
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
import java.awt.image.*;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.filter.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box to set bitmap properties.
 * @author Nicola L C Talbot
 */

public class BitmapProperties extends JDialog
   implements ActionListener
{
   public BitmapProperties(FlowframTk application, JFileChooser fc)
   {
      super(application,
            application.getResources().getMessage("bitmap_properties.title"),
            true);
      application_ = application;
      fc_ = fc;

      init();
   }

   protected void init()
   {
      JDRResources resources = getResources();

      JLabelGroup labelGroup = new JLabelGroup();
      Box mainPanel = Box.createVerticalBox();
      mainPanel.setAlignmentX(0.0f);

      getContentPane().add(mainPanel, "Center");

      mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

      JComponent rowBox = createRow();
      mainPanel.add(rowBox);

      JLabel filenameLabel = resources.createAppLabel("label.filename");
      labelGroup.add(filenameLabel);
      rowBox.add(filenameLabel);

      rowBox.add(resources.createLabelSpacer());

      filename = new JTextField(TEXTFIELD_COLUMNS);
      filenameLabel.setLabelFor(filename);
      rowBox.add(filename);

      browse = resources.createDialogButton("browsebitmap", "browse",
         this, null);

      rowBox.add(browse);
      rowBox.add(Box.createHorizontalGlue());

      resources.clampCompMaxHeight(rowBox, 0, 0);

      mainPanel.add(Box.createVerticalStrut(10));

      rowBox = createRow();
      mainPanel.add(rowBox);

      JLabel latexlinkLabel = 
         resources.createAppLabel("bitmap_properties.latexfilename");
      labelGroup.add(latexlinkLabel);
      rowBox.add(latexlinkLabel);

      rowBox.add(resources.createLabelSpacer());

      autoBox = resources.createAppCheckBox(
        "bitmap_properties", "auto_latexfilename", true, this);
      rowBox.add(autoBox);

      latexlinkText = new JTextField(TEXTFIELD_COLUMNS);
      latexlinkLabel.setLabelFor(latexlinkText);
      latexlinkText.setEnabled(false);
      rowBox.add(latexlinkText);
      rowBox.add(Box.createHorizontalGlue());

      resources.clampCompMaxHeight(rowBox, 0, 0);

      mainPanel.add(Box.createVerticalStrut(10));

      JTextArea infoArea = resources.createAppInfoArea(TEXTFIELD_COLUMNS);
      infoArea.setAlignmentX(0.0f);

      infoArea.setText(resources.getMessage(
         "bitmap_properties.path_note") + " "
        + (application_.useRelativeBitmaps() ?
           resources.getMessage("bitmap_properties.path.relative") :
           resources.getMessage("bitmap_properties.path.absolute",
              filenameLabel.getText())));

      mainPanel.add(infoArea);

      mainPanel.add(Box.createVerticalStrut(10));

      rowBox = createRow();
      mainPanel.add(rowBox);

      JLabel commandLabel =
         getResources().createAppLabel("bitmap_properties.command");
      labelGroup.add(commandLabel);
      rowBox.add(commandLabel);

      rowBox.add(resources.createLabelSpacer());

      latexCommand = new JTextField(TEXTFIELD_COLUMNS);
      rowBox.add(latexCommand);
      rowBox.add(Box.createHorizontalGlue());

      commandLabel.setLabelFor(latexCommand);

      resources.clampCompMaxHeight(rowBox, 0, 0);

      mainPanel.add(Box.createVerticalStrut(10));

      matrixPanel = new TransformationMatrixPanel(resources, "bitmap_properties.matrix");
      matrixPanel.setBorder(BorderFactory.createLoweredSoftBevelBorder());

      matrixPanel.setAlignmentX(0.0f);
      mainPanel.add(matrixPanel);

      mainPanel.add(Box.createVerticalGlue());

      JPanel p2 = new JPanel();

      resources.createOkayCancelHelpButtons(this, p2, this, "sec:bitmapprops");

      getContentPane().add(p2, "South");

      pack();

      setLocationRelativeTo(application_);
   }

   protected JComponent createRow()
   {
      JComponent comp = Box.createHorizontalBox();
      comp.setAlignmentX(0.0f);

      return comp;
   }

   public void initialise()
   {
      JDRFrame frame = application_.getCurrentFrame();
      setProperties(frame.getSelectedBitmap());
      filename.requestFocusInWindow();
      setVisible(true);
   }

   public void browseAction()
   {
      int result = fc_.showOpenDialog(this);

      if (result == JFileChooser.APPROVE_OPTION)
      {
         String newfilename = fc_.getSelectedFile().getAbsolutePath();
         String linkname;
         if (File.separator.equals("\\"))
         {
            StringTokenizer t = new StringTokenizer(newfilename, "\\");
            linkname = t.nextToken();

            while (t.hasMoreTokens())
            {
               linkname += File.separator + t.nextToken();
            }
         }
         else
         {
            linkname = newfilename;
         }

         filename.setText(newfilename);
         latexlinkText.setText(linkname);
      }
   }

   public void setProperties(JDRBitmap db)
   {
      if (db == null)
      {
         getResources().internalError(this,
            getResources().getMessage("internal_error.no_bitmap"));
         return;
      }

      bitmap = db;
      filename.setText(bitmap.getFilename());
      latexlinkText.setText(bitmap.getLaTeXLinkName());
      latexCommand.setText(bitmap.getLaTeXCommand());

      double[] matrix = new double[6];

      db.getTransformation(matrix);

      matrixPanel.setMatrix(matrix);
   }

   public void okay()
   {
      String newfilename = filename.getText();
      String newlatexname =
       autoBox.isSelected() ? "" : latexlinkText.getText();
      String command = latexCommand.getText();

      double[] matrix = new double[6];

      matrixPanel.getMatrix(matrix);

      File file = new File(newfilename);

      if (!file.exists())
      {
         getResources().error(this, 
                       new String[]
                       {"'"+newfilename+"'",
                       getResources().getMessage("error.not_found")});
      }
      else
      {
         setVisible(false);
         JDRFrame frame = application_.getCurrentFrame();
         frame.setBitmapProperties(bitmap,
            newfilename, newlatexname, command, matrix);
         bitmap=null;
      }
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
         bitmap=null;
         setVisible(false);
      }
      else if (action.equals("browse"))
      {
         browseAction();
      }
      else if (action.equals("auto_latexfilename"))
      {
         latexlinkText.setEnabled(!autoBox.isSelected());

         if (latexlinkText.isEnabled() && latexlinkText.getText().isEmpty())
         {
            latexlinkText.setText(JDRBitmap.getLaTeXPath(filename.getText()));
         }
      }
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;
   private JButton browse;
   private JTextField filename, latexlinkText, latexCommand;
   private JDRBitmap bitmap=null;
   private JFileChooser fc_;
   private TransformationMatrixPanel matrixPanel;

   private JCheckBox autoBox;

   static final int TEXTFIELD_COLUMNS=32;
}
