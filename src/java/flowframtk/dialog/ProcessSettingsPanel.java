/*
    Copyright (C) 2025 Nicola L.C. Talbot

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
import javax.swing.table.*;
import javax.swing.event.*;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Component used to setup external process setting.
 */

public class ProcessSettingsPanel extends JPanel implements ActionListener
{
   public ProcessSettingsPanel(FlowframTk application, JDRAppSelector appSelector,
      String appname)
   {
     this(application, appSelector, appname, null, null);
   }

   public ProcessSettingsPanel(FlowframTk application, JDRAppSelector appSelector,
      String appname, JLabel fileFieldLabel, JLabelGroup labelGrp)
   {
      super(new BorderLayout());
      this.application = application;

      fileField = new FileField(getResources(), this, appname, 
         appSelector.getFileChooser(), fileFieldLabel);

      fileField.getEastComponent().add(Box.createHorizontalStrut(20));

      add(fileField, "North");

      eastComp = Box.createVerticalBox();
      add(eastComp, "East");

      JComponent row = Box.createHorizontalBox();
      eastComp.add(row);

      addButton =  getResources().createDialogButton(
        "appselect", "add", this, (KeyStroke)null);
      row.add(addButton);

      upButton =  getResources().createDialogButton(
        "appselect", "up", this, (KeyStroke)null);
      row.add(upButton);

      row.add(Box.createHorizontalStrut(20));

      row = Box.createHorizontalBox();
      eastComp.add(row);

      removeButton =  getResources().createDialogButton(
        "appselect", "remove", this, (KeyStroke)null);
      row.add(removeButton);

      downButton =  getResources().createDialogButton(
        "appselect", "down", this, (KeyStroke)null);
      row.add(downButton);

      row.add(Box.createHorizontalStrut(20));

      optionsTableModel = new DefaultTableModel(2, 1);
      optionsTable = new JTable(optionsTableModel)
        {
          @Override
           public void valueChanged(ListSelectionEvent evt)
           {
              super.valueChanged(evt);

              if (!evt.getValueIsAdjusting())
              {
                 int n = getSelectedRowCount();
                 removeButton.setEnabled(n > 0 && getRowCount() > 1);

                 if (n == 1)
                 {
                    upButton.setEnabled(getSelectedRow() > 0);
                    downButton.setEnabled(getSelectedRow() < getRowCount()-1);
                 }
                 else
                 {
                    upButton.setEnabled(false);
                    downButton.setEnabled(false);
                 }
              }
           }
        };

      optionsTable.setRowSelectionAllowed(true);
      optionsTable.setColumnSelectionAllowed(false);

      add(optionsTable, "Center");

      JLabel optionsLabel = getResources().createAppLabel("appselect.options");

      if (labelGrp != null)
      {
         labelGrp.add(optionsLabel);
      }

      westComp = new JPanel(new BorderLayout());
      add(westComp, "West");

      westComp.add(optionsLabel, "North");

   }

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("add"))
      {
         optionsTableModel.addRow(new String[] { "" });
         removeButton.setEnabled(true);
      }
      else if (action.equals("remove"))
      {
         int[] rowIdxs = optionsTable.getSelectedRows();

         for (int i = rowIdxs.length - 1; i >= 0; i--)
         {
            optionsTableModel.removeRow(rowIdxs[i]);
         }
      }
      else if (action.equals("up"))
      {
         int rowIdx = optionsTable.getSelectedRow();
         int j = rowIdx-1;
         optionsTableModel.moveRow(rowIdx, rowIdx, j);
         optionsTable.setRowSelectionInterval(j, j);
      }
      else if (action.equals("down"))
      {
         int rowIdx = optionsTable.getSelectedRow();
         int j = rowIdx+1;
         optionsTableModel.moveRow(rowIdx, rowIdx, j);
         optionsTable.setRowSelectionInterval(j, j);
      }
   }

   public void initialise(String appPath, String options)
   {
      initialise(appPath, options.split("\t"));
   }

   public void initialise(String appPath, String[] options)
   {
      fileField.setFileName(appPath);

      optionsTableModel.setNumRows(options.length);

      for (int i = 0; i < options.length; i++)
      {
         optionsTableModel.setValueAt(options[i], i, 0);
      }

      removeButton.setEnabled(options.length > 1);
      upButton.setEnabled(false);
      downButton.setEnabled(false);
   }

   public String getFileName()
   {
      return fileField.getFileName();
   }

   public String getOptions()
   {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < optionsTableModel.getRowCount(); i++)
      {
         int idx = optionsTable.convertRowIndexToModel(i);
         String val = optionsTableModel.getValueAt(idx, 0).toString().trim();

         if (!val.isEmpty())
         {
            if (builder.length() > 0)
            {
               builder.append('\t');
            }

            builder.append(val);
         }
      }

      return builder.toString();
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   public JComponent getWestComponent()
   {
      return westComp;
   }

   private FlowframTk application;

   private JComponent westComp, eastComp;
   private FileField fileField;
   private JTable optionsTable;
   private DefaultTableModel optionsTableModel;
   private JButton addButton, removeButton, upButton, downButton;
}
