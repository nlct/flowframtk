/*
    Copyright (C) 2013 Nicola L.C. Talbot
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

public class FileField extends Box
  implements ActionListener
{
   public FileField(JDRResources resources, Container parent, 
      JFileChooser fileChooser)
   {
      this(resources, parent, null, fileChooser, JFileChooser.FILES_ONLY);
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
      JFileChooser fileChooser, int mode)
   {
      super(BoxLayout.Y_AXIS);

      this.resources = resources;
      this.fileChooser = fileChooser;
      this.parent = parent;
      this.mode = mode;

      add(Box.createVerticalGlue());

      Box box = Box.createHorizontalBox();
      add(box);

      textField = new JTextField(fileName == null ? "" : fileName, 20);

      Dimension dim = textField.getPreferredSize();
      dim.width = (int)textField.getMaximumSize().getWidth();

      textField.setMaximumSize(dim);

      box.add(textField);

      button = getResources().createDialogButton("label.choose", "open",
         this, null, getResources().getString("tooltip.choose_file"));

      box.add(button);

      add(Box.createVerticalGlue());

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

      if (action.equals("open"))
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
            getResources().getString("file.select"))
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
      textField.setText(name);
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

   public JDRResources getResources()
   {
      return resources;
   }

   private JTextField textField;

   private JButton button;

   private JFileChooser fileChooser;

   private Container parent;

   private int mode;

   private JDRResources resources;
}
