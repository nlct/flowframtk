// File          : JDRPropertiesDialog.java
// Description   : Dialog box displaying file properties
// Creation Date : 4th June 2008
// Author        : Nicola L C Talbot
//               http://www.dickimaw-books.com/

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

package com.dickimawbooks.jdrview;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import com.dickimawbooks.texjavahelplib.JLabelGroup;
import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;

/**
 * File properties dialog box.
 * @author Nicola L C Talbot
 */
public class JDRPropertiesDialog extends JDialog 
   implements KeyListener
{
   /**
    * Creates instance of this panel.
    * @param application the application using this panel
    */
   public JDRPropertiesDialog(JDRView application)
   {
      super(application,
         application.getResources().getMessage("fileproperties.title"));

      setLayout(new BorderLayout());

      app = application;
      TeXJavaHelpLib helpLib = app.getResources().getHelpLib();

      addKeyListener(this);

      JComponent mainComp = Box.createVerticalBox();
      JLabelGroup labelGrp = new JLabelGroup();

      // File name

      JComponent row = Box.createHorizontalBox();
      row.setAlignmentX(0);
      mainComp.add(row);

      nameField = createField();

      nameLabel = helpLib.createJLabel(labelGrp,
          "fileproperties.filename", nameField);
      row.add(nameLabel);
      row.add(nameField);

      // File size

      row = Box.createHorizontalBox();
      row.setAlignmentX(0);
      mainComp.add(row);

      sizeField = createField();

      sizeLabel = helpLib.createJLabel(labelGrp,
          "fileproperties.filesize", sizeField);
      row.add(sizeLabel);
      row.add(sizeField);

      // File format

      row = Box.createHorizontalBox();
      row.setAlignmentX(0);
      mainComp.add(row);

      formatField = createField();

      formatLabel = helpLib.createJLabel(labelGrp,
          "fileproperties.fileformat", formatField);
      row.add(formatLabel);
      row.add(formatField);

      // Image bounds

      row = Box.createHorizontalBox();
      row.setAlignmentX(0);
      mainComp.add(row);

      boundsField = createField();

      boundsLabel = helpLib.createJLabel(labelGrp,
          "fileproperties.bounds", boundsField);
      row.add(boundsLabel);
      row.add(boundsField);

      // Paper size

      row = Box.createHorizontalBox();
      row.setAlignmentX(0);
      mainComp.add(row);

      paperField = createField();

      paperLabel = helpLib.createJLabel(labelGrp,
          "fileproperties.paper", paperField);
      row.add(paperLabel);
      row.add(paperField);

      // Image description

      row = Box.createHorizontalBox();
      row.setAlignmentX(0);
      mainComp.add(row);

      descriptionField = createField();

      descriptionLabel = helpLib.createJLabel(labelGrp,
          "fileproperties.description", descriptionField);
      row.add(descriptionLabel);
      row.add(descriptionField);

      // Last modified date

      row = Box.createHorizontalBox();
      row.setAlignmentX(0);
      mainComp.add(row);

      modifiedField = createField();

      modifiedLabel = helpLib.createJLabel(labelGrp,
          "fileproperties.modified", modifiedField);
      row.add(modifiedLabel);
      row.add(modifiedField);

      getContentPane().add(mainComp, "Center");

      JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
      panel.add(getResources().getHelpLib().createCloseButton((JDialog)this));

      getContentPane().add(panel, "South");

      pack();

      setLocationRelativeTo(application);
   }

   protected JTextField createField()
   {
      JTextField field = new JTextField(32);
      field.setEditable(false);
      return field;
   }

   public void keyPressed(KeyEvent e)
   {
      int keyCode = e.getKeyCode();

      if (keyCode == KeyEvent.VK_ENTER)
      {
         setVisible(false);
      }
   }

   public void keyReleased(KeyEvent e)
   {
   }

   public void keyTyped(KeyEvent e)
   {
   }

   public void display(File file, String format, JDRGroup image,
      JDRPaper paper)
   {
      nameField.setText(file.getName());

      long fileSize = file.length();

      if (fileSize > 1000000)
      {
         sizeField.setText(getResources().getMessage(
            "fileproperties.mbytes", df.format(fileSize/1000000.0)));
      }
      else if (fileSize > 1000)
      {
         sizeField.setText(getResources().getMessage(
            "fileproperties.kbytes", (fileSize/1000.0)));
      }
      else
      {
         sizeField.setText(getResources().getMessage(
            "fileproperties.bytes", fileSize));
      }

      formatField.setText(format);

      String desc = image.getDescription();

      if (image == null || desc == null)
      {
         descriptionField.setText("");
      }
      else
      {
         descriptionField.setText(desc);
      }

      modifiedField.setText(
         DateFormat.getDateInstance().format(
            new Date(file.lastModified())));

      if (image == null)
      {
         boundsField.setText(
            getResources().getMessage("fileproperties.empty"));
      }
      else
      {
         BBox box = image.getStorageBBox();

         if (box == null)
         {
            boundsField.setText(
               getResources().getMessage("fileproperties.empty"));
         }
         else
         {
            boundsField.setText(
               ""+df.format(box.getMinX())+" "
                 +df.format(box.getMinY())+" "
                 +df.format(box.getMaxX())+" "
                 +df.format(box.getMaxY()));
         }
      }

      if (paper == null)
      {
         paperField.setText(
            getResources().getMessage("fileproperties.nonesupplied"));
      }
      else
      {
         paperField.setText(paper.toString());
      }

      pack();

      setVisible(true);
   }

   public JDRResources getResources()
   {
      return app.getResources();
   }

   private JDRView app;

   private JLabel nameLabel, sizeLabel, formatLabel, boundsLabel,
      descriptionLabel, modifiedLabel, paperLabel;

   private JTextField nameField, sizeField, formatField, boundsField,
      descriptionField, modifiedField, paperField;

   private static DecimalFormat df = new DecimalFormat("0.##");
}
