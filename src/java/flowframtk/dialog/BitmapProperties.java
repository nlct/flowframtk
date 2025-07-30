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

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.filter.*;
import com.dickimawbooks.jdrresources.numfield.*;

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

      Box mainPanel = Box.createVerticalBox();

      getContentPane().add(mainPanel, "Center");

      mainPanel.add(Box.createVerticalStrut(10));

      Box rowBox = Box.createHorizontalBox();
      mainPanel.add(rowBox);

      JLabel filenameLabel = getResources().createAppLabel("label.filename");
      rowBox.add(filenameLabel);

      filename = new JTextField(getResources().getMessage("error.no_filename"));
      filenameLabel.setLabelFor(filename);
      rowBox.add(filename);

      browse = getResources().createDialogButton("browsebitmap.browse", "open",
         this, null, getResources().getMessage("tooltip.choose_file"));

      rowBox.add(browse);

      mainPanel.add(Box.createVerticalStrut(10));

      rowBox = Box.createHorizontalBox();
      mainPanel.add(rowBox);

      JLabel latexlinkLabel = 
         getResources().createAppLabel("bitmap_properties.latexfilename");
      rowBox.add(latexlinkLabel);

      autoBox = getResources().createAppCheckBox(
        "bitmap_properties", "auto_latexfilename", true, this);
      rowBox.add(autoBox);

      latexlinkText = new JTextField("");
      latexlinkLabel.setLabelFor(latexlinkText);
      latexlinkText.setEnabled(false);
      rowBox.add(latexlinkText);

      mainPanel.add(Box.createVerticalStrut(10));

      JTextArea infoArea = new JTextArea(getResources().getMessage(
         "bitmap_properties.path_note") + " "
        + (application.useRelativeBitmaps() ?
           getResources().getMessage("bitmap_properties.path.relative") :
           getResources().getMessage("bitmap_properties.path.absolute",
              filenameLabel.getText())));
      infoArea.setEditable(false);
      infoArea.setLineWrap(true);
      infoArea.setWrapStyleWord(true);
      infoArea.setOpaque(false);

      mainPanel.add(infoArea);

      mainPanel.add(Box.createVerticalStrut(10));

      rowBox = Box.createHorizontalBox();
      mainPanel.add(rowBox);

      JLabel commandLabel =
         getResources().createAppLabel("bitmap_properties.command");
      rowBox.add(commandLabel);

      latexCommand = new JTextField("\\pgfimage");
      rowBox.add(latexCommand);

      commandLabel.setLabelFor(latexCommand);

      mainPanel.add(Box.createVerticalStrut(10));

      rowBox = Box.createHorizontalBox();
      mainPanel.add(rowBox);

      JLabel matrixLabel = 
         getResources().createAppLabel("bitmap_properties.matrix");
      rowBox.add(matrixLabel);

      JPanel matrixPanel = new JPanel();
      matrixPanel.setAlignmentY(0.0f);

      matrixPanel.setLayout(new GridLayout(3,3));

      field = new DoubleField[6];
      int[] indexes = new int[] {0, 2, 4, 1, 3, 5};

      for (int i = 0; i < field.length; i++)
      {
         int j = indexes[i];

         field[j] = new DoubleField(0);
         field[j].setAlignmentY(0.0f);
         field[j].setHorizontalAlignment(JTextField.LEFT);
         matrixPanel.add(field[j]);
      }

      matrixLabel.setLabelFor(field[0]);

      for (int i = 0; i < 3; i++)
      {
         JLabel label = new JLabel(i == 2 ? "1" : "0");
         label.setAlignmentY(0.0f);
         matrixPanel.add(label);
      }

      rowBox.add(matrixPanel);

      Dimension filenameLabelDim = filenameLabel.getPreferredSize();
      Dimension latexlinkLabelDim = latexlinkLabel.getPreferredSize();
      Dimension commandLabelDim = commandLabel.getPreferredSize();
      Dimension matrixLabelDim = matrixLabel.getPreferredSize();

      int maxWidth = (int)Math.max
                     (Math.max(filenameLabelDim.getWidth(),
                               latexlinkLabelDim.getWidth()),
                      Math.max(commandLabelDim.getWidth(),
                               matrixLabelDim.getWidth())
                     ) + 10;

      filenameLabelDim.width = maxWidth;
      latexlinkLabelDim.width = maxWidth;
      commandLabelDim.width = maxWidth;
      matrixLabelDim.width = maxWidth;

      filenameLabel.setPreferredSize(filenameLabelDim);
      latexlinkLabel.setPreferredSize(latexlinkLabelDim);
      commandLabel.setPreferredSize(commandLabelDim);
      matrixLabel.setPreferredSize(matrixLabelDim);

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(getRootPane(), this));
      p2.add(getResources().createCancelButton(this));
      p2.add(getResources().createHelpDialogButton(this, "sec:bitmapprops"));

      getContentPane().add(p2, "South");

      pack();

      infoArea.setMinimumSize(infoArea.getPreferredSize());

      pack();

      setLocationRelativeTo(application);
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

      for (int i = 0; i < 6; i++)
      {
         field[i].setValue(matrix[i]);
         field[i].setCaretPosition(0);
      }
   }

   public void okay()
   {
      String newfilename = filename.getText();
      String newlatexname =
       autoBox.isSelected() ? "" : latexlinkText.getText();
      String command = latexCommand.getText();

      double[] matrix = new double[6];

      for (int i = 0; i < 6; i++)
      {
         matrix[i] = field[i].getDouble();
      }

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
      else if (action.equals("open"))
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
   private DoubleField[] field;
   private JButton browse;
   private JTextField filename, latexlinkText, latexCommand;
   private JDRBitmap bitmap=null;
   private JFileChooser fc_;

   private JCheckBox autoBox;
}
