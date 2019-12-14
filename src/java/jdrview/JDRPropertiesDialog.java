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
         application.getResources().getString("fileproperties.title"));

      setLayout(new BorderLayout());

      app = application;

      addKeyListener(this);

      Box box = Box.createVerticalBox();

      Dimension dim;

      // file name
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      nameLabel = new JLabel(
         getResources().getString("fileproperties.filename"),
         SwingConstants.RIGHT);

      dim = nameLabel.getPreferredSize();

      int width = dim.width;
      int height = dim.height;

      panel.add(nameLabel);

      nameField = new JLabel();
      panel.add(nameField);

      box.add(panel);

      // file size
      panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      sizeLabel = new JLabel(
         getResources().getString("fileproperties.filesize"),
         SwingConstants.RIGHT);

      dim = sizeLabel.getPreferredSize();

      if (dim.width > width)
      {
         width = dim.width;
      }
      
      if (dim.height > height)
      {
         height = dim.height;
      }

      panel.add(sizeLabel);

      sizeField = new JLabel();
      panel.add(sizeField);

      box.add(panel);

      // file format

      panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      formatLabel = new JLabel(getResources().getString(
         "fileproperties.fileformat"),
         SwingConstants.RIGHT);

      dim = formatLabel.getPreferredSize();

      if (dim.width > width)
      {
         width = dim.width;
      }
      
      if (dim.height > height)
      {
         height = dim.height;
      }

      panel.add(formatLabel);

      formatField = new JLabel();
      panel.add(formatField);

      box.add(panel);

      // image bounds

      panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      boundsLabel = new JLabel(
         getResources().getString("fileproperties.bounds"),
         SwingConstants.RIGHT);

      dim = boundsLabel.getPreferredSize();

      if (dim.width > width)
      {
         width = dim.width;
      }
      
      if (dim.height > height)
      {
         height = dim.height;
      }

      panel.add(boundsLabel);

      boundsField = new JLabel();
      panel.add(boundsField);

      box.add(panel);

      // paper
      panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      paperLabel = new JLabel(
         getResources().getString("fileproperties.paper"),
         SwingConstants.RIGHT);

      dim = paperLabel.getPreferredSize();

      if (dim.width > width)
      {
         width = dim.width;
      }
      
      if (dim.height > height)
      {
         height = dim.height;
      }

      panel.add(paperLabel);

      paperField = new JLabel();
      panel.add(paperField);

      box.add(panel);

      // image description

      panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      descriptionLabel = new JLabel(
         getResources().getString("fileproperties.description"),
         SwingConstants.RIGHT);

      dim = descriptionLabel.getPreferredSize();

      if (dim.width > width)
      {
         width = dim.width;
      }
      
      if (dim.height > height)
      {
         height = dim.height;
      }

      panel.add(descriptionLabel);

      descriptionField = new JLabel();
      panel.add(descriptionField);

      box.add(panel);

      // last modified

      panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      modifiedLabel = new JLabel(
         getResources().getString("fileproperties.modified"),
         SwingConstants.RIGHT);

      dim = modifiedLabel.getPreferredSize();

      if (dim.width > width)
      {
         width = dim.width;
      }
      
      if (dim.height > height)
      {
         height = dim.height;
      }

      panel.add(modifiedLabel);

      modifiedField = new JLabel();
      panel.add(modifiedField);

      box.add(panel);

      dim = new Dimension(width, height);

      nameLabel.setPreferredSize(dim);
      sizeLabel.setPreferredSize(dim);
      formatLabel.setPreferredSize(dim);
      boundsLabel.setPreferredSize(dim);
      paperLabel.setPreferredSize(dim);
      descriptionLabel.setPreferredSize(dim);
      modifiedLabel.setPreferredSize(dim);

      getContentPane().add(box, "Center");

      okayButton = new JButton(getResources().getString("label.okay"));
      okayButton.setMnemonic(
         getResources().getChar("label.okay.mnemonic"));
      okayButton.addKeyListener(this);

      okayButton.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               setVisible(false);
            }
         });

      panel = new JPanel();
      panel.add(okayButton);

      getContentPane().add(panel, "South");

      pack();

      setLocationRelativeTo(application);
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
         sizeField.setText(getResources().getStringWithValue(
            "fileproperties.mbytes", df.format(fileSize/1000000.0)));
      }
      else if (fileSize > 1000)
      {
         sizeField.setText(getResources().getStringWithValue(
            "fileproperties.kbytes", (fileSize/1000.0)));
      }
      else
      {
         sizeField.setText(getResources().getStringWithValue(
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
            getResources().getString("fileproperties.empty"));
      }
      else
      {
         BBox box = image.getStorageBBox();

         if (box == null)
         {
            boundsField.setText(
               getResources().getString("fileproperties.empty"));
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
            getResources().getString("fileproperties.nonesupplied"));
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

   private JButton okayButton;

   private JLabel nameLabel, sizeLabel, formatLabel, boundsLabel,
      descriptionLabel, modifiedLabel, paperLabel;

   private JLabel nameField, sizeField, formatField, boundsField,
      descriptionField, modifiedField, paperField;

   private static DecimalFormat df = new DecimalFormat("0.##");
}
