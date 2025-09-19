/*
    Copyright (C) 2013-2025 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
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

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.dickimawbooks.jdrresources.JDRResources;

public class FileField extends JPanel
  implements ActionListener
{
   public FileField(JDRResources resources, Container parent, 
      JFileChooser fileChooser)
   {
      this(resources, parent, null, fileChooser, JFileChooser.FILES_ONLY);
   }

   public FileField(JDRResources resources, Container parent, 
      JFileChooser fileChooser, JLabel label)
   {
      this(resources, parent, null, fileChooser, JFileChooser.FILES_ONLY, label);
   }

   public FileField(JDRResources resources, Container parent, 
      JFileChooser fileChooser, int mode)
   {
      this(resources, parent, null, fileChooser, mode);
   }

   public FileField(JDRResources resources, Container parent, String fileName, 
      JFileChooser fileChooser)
   {
      this(resources, parent, fileName, fileChooser, JFileChooser.FILES_ONLY);
   }

   public FileField(JDRResources resources, Container parent, String fileName, 
      JFileChooser fileChooser, JLabel label)
   {
      this(resources, parent, fileName, fileChooser, JFileChooser.FILES_ONLY,
        label);
   }

   public FileField(JDRResources resources, Container parent, String fileName, 
      JFileChooser fileChooser, int mode)
   {
      this(resources, parent, fileName, fileChooser, mode, null);
   }

   public FileField(JDRResources resources, Container parent, String fileName, 
      JFileChooser fileChooser, int mode, JLabel label)
   {
      super(new BorderLayout());

      this.resources = resources;
      this.fileChooser = fileChooser;
      this.parent = parent;
      this.mode = mode;

      Box mainPanel = Box.createVerticalBox();
      add(mainPanel, "Center");
      textField = new JTextField(fileName == null ? "" : fileName, 20);

      mainPanel.add(Box.createVerticalStrut(5));
      mainPanel.add(textField);
      mainPanel.add(Box.createVerticalStrut(5));

      eastComp = Box.createHorizontalBox();
      add(eastComp, "East");

      button = getResources().createDialogButton("button", "selectfile", this, null);

      eastComp.add(button);

      westComp = Box.createHorizontalBox();
      add(westComp, "West");

      if (label != null)
      {
         westComp.add(label);
         label.setLabelFor(textField);
      }

      setAlignmentY(Component.CENTER_ALIGNMENT);
      setAlignmentX(Component.LEFT_ALIGNMENT);
   }

   public void setAlignmentY(float align)
   {
      super.setAlignmentY(align);
      textField.setAlignmentY(align);
      button.setAlignmentY(align);
   }

   public void setAlignmentX(float align)
   {
      super.setAlignmentX(align);
      textField.setAlignmentX(align);
      button.setAlignmentX(align);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("selectfile"))
      {
         fileChooser.setFileSelectionMode(mode);

         File file = getFile();

         if (file != null)
         {
            fileChooser.setCurrentDirectory(file.getParentFile());

            fileChooser.setSelectedFile(file);
         }

         fileChooser.setApproveButtonMnemonic(
            getResources().getCodePoint("file.select.mnemonic"));

         if (fileChooser.showDialog(parent,
            getResources().getMessage("file.select"))
            == JFileChooser.APPROVE_OPTION)
         {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
         }
      }
   }

   public boolean requestFocusInWindow()
   {
      return textField.requestFocusInWindow();
   }

   public JTextField getTextField()
   {
      return textField;
   }

   public File getFile()
   {
      String fileName = getFileName();

      if (fileName == null || fileName.equals("")) return null;

      return fileName.contains(File.separator) 
         ? new File(fileName)
         : new File(fileChooser.getCurrentDirectory(), fileName);
   }

   public String getFileName()
   {
      return textField.getText();
   }

   public void setFileName(String name)
   {
      textField.setText(name == null ? "" : name);
   }

   public void setCurrentDirectory(String dirPath)
   {
      setCurrentDirectory(new File(dirPath));
   }

   public void setCurrentDirectory(File dir)
   {
      fileChooser.setCurrentDirectory(dir);
   }

   public void setFile(File file)
   {
      setCurrentDirectory(file.getParentFile());
      setFileName(file.toString());
      fileChooser.setSelectedFile(file);
   }

   public void setEnabled(boolean flag)
   {
      super.setEnabled(flag);

      textField.setEnabled(flag);
      button.setEnabled(flag);
   }

   public void setLabel(JLabel label)
   {
      label.setLabelFor(textField);
   }

   public JComponent getEastComponent()
   {
      return eastComp;
   }

   public JComponent getWestComponent()
   {
      return westComp;
   }

   public JDRResources getResources()
   {
      return resources;
   }

   private JTextField textField;

   private JButton button;
   private JComponent eastComp, westComp;

   private JFileChooser fileChooser;

   private Container parent;

   private int mode;

   private JDRResources resources;
}
